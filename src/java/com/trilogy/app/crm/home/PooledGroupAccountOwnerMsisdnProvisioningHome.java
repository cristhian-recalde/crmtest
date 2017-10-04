/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.Date;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * This class upduates group-pooled accounts so that they are aware of which
 * subscriber is the owner (i.e., responsible for receiving SMS notification).
 *
 * @author jimmy.ng@redknee.com
 */
public class PooledGroupAccountOwnerMsisdnProvisioningHome extends HomeProxy
{
    /**
     * Creates a new PooledGroupAccountOwnerMsisdnProvisioningHome for the
     * given home.
     *
     * @param context  The operating context.
     * @param delegate The Home to which this object delegates.
     */
    public PooledGroupAccountOwnerMsisdnProvisioningHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }

    // INHERIT
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Account newAccount = (Account) obj;

        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);

        Object ret = null;

        ret = super.store(ctx, newAccount);

        if (!isNoChangeInGroupOwner(ctx, oldAccount, newAccount))
        {
            // If Owner MSISDN is not defined, leave it blank, this will prevent the SMS sending.
            // Sending SMS to poolMSISDN will fail because poolMSISDN is FAKE.

            setPoolGroupOwnerOnBMGT(ctx, newAccount, newAccount.getOwnerMSISDN());
        }

        return ret;
    }

    /**
     * Determine if there is any change in the Group Owner from the given old Account
     * to the given new Account.
     *
     * @param ctx the operating context
     * @param oldAccount The account before the update.
     * @param newAccount The account to be updated.
     * @return boolean True if the Group Owner should have no change between the two accounts; False otherwise.
     */
    private boolean isNoChangeInGroupOwner(final Context ctx, final Account oldAccount, final Account newAccount)
    {
		final boolean isOldAccountPooled = oldAccount.isPooled(ctx);
		final boolean isNewAccountPooled = newAccount.isPooled(ctx);

        if (!isOldAccountPooled && !isNewAccountPooled)
        {
            return true;
        }
        else if (isOldAccountPooled
                && isNewAccountPooled
                && oldAccount.getOwnerMSISDN().equals(newAccount.getOwnerMSISDN()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Set the group owner of a subscriber via the UPS interface.
     *
     * @param ctx the operating context
     * @param account The group Account for which to set an owner.
     * @param ownerMsisdn The MSISDN of the subscriber who is the owner.
     */
    private void setPoolGroupOwnerOnBMGT(final Context ctx, final Account account, final String ownerMsisdn)
    {
        final HTMLExceptionListener el = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);

        final Date now = new Date();

        final PoolExtension extention = account.getPoolExtension();
        final String poolMsisdn = extention.getPoolMSISDN();
        final Set<Long> pooledSubTypes = extention.getSubscriptionPoolProperties().keySet();

        for (Long subTypeId : pooledSubTypes)
        {
            try
            {
                final Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, poolMsisdn, subTypeId, now);
                setSubscriptionGroupOwnerOnBMGT(ctx, sub, ownerMsisdn);
            }
            catch (HomeException e)
            {
                if (el != null)
                {
                    el.thrown(e);
                }
                new MajorLogMsg(this, "Unable to modify the Pool Group Owner for pooled subscription", e).log(ctx);
            }
        }
    }

    /**
     * @param ctx the operating context
     * @param subscription The group Subscription for which to set an owner.
     * @param ownerMsisdn The MSISDN of the subscriber who is the owner.
     * @throws HomeException
     */
    private void setSubscriptionGroupOwnerOnBMGT(final Context ctx, final Subscriber subscription, final String ownerMsisdn)
        throws HomeException
    {
        final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
        if (client == null)
        {
            throw new HomeException("System error: SubscriberProfileProvisionClient not found in context");
        }

        try
        {
            client.updatePooledGroupOwner(ctx, subscription, ownerMsisdn);
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            throw new HomeException("provisioning result 3008: failed to set group owner due to BM error ("
                    + exception.getErrorCode() + ")", exception);
        }
    }
}

