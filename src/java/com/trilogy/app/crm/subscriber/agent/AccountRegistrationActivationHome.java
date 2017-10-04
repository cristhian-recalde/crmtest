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
package com.trilogy.app.crm.subscriber.agent;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.RegistrationStatusEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.filter.AccountRegistrationEnabledPredicate;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;


/**
 * This home switches the account's registration status from NOT_APPLICABLE to NOT_REGISTERED.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class AccountRegistrationActivationHome extends HomeProxy
{
    public AccountRegistrationActivationHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Subscriber result = (Subscriber) super.create(ctx, obj);
        if (needsRegistration(ctx, null, result))
        {
            updateAccountRegistrationStatus(ctx, result);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Subscriber result = (Subscriber) super.store(ctx, obj);
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        if (needsRegistration(ctx, oldSub, result))
        {
            updateAccountRegistrationStatus(ctx, result);
        }
        return result;
    }

    protected boolean needsRegistration(Context ctx, Subscriber oldSub, Subscriber newSub)
    {
        if (oldSub == null
                && EnumStateSupportHelper.get(ctx).stateEquals(
                        newSub, 
                        SubscriberStateEnum.ACTIVE))
        {
            return true;
        }
        else if (EnumStateSupportHelper.get(ctx).isEnteringState(
                        oldSub, newSub, 
                        SubscriberStateEnum.ACTIVE))
        {
            return true;
        }

        return false;
    }

    public void updateAccountRegistrationStatus(Context ctx, Subscriber sub)
    {
        Account account = null;
        try
        {
            account = sub.getAccount(ctx);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Failed to load account " + sub.getBAN(), e).log(ctx);
        }
        
        RegistrationStatusEnum notRegisteredStatus = RegistrationStatusEnum.NOT_REGISTERED;
        if (account != null)
        {
            int registrationStatus = account.getRegistrationStatus();
            if (registrationStatus == RegistrationStatusEnum.NOT_APPLICABLE.getIndex()
                    && new AccountRegistrationEnabledPredicate(true).f(ctx, account))
            {
                account.setRegistrationStatus(notRegisteredStatus);

                // This home is being used outside of Account pipeline.
                // Must explicitly invoke account pipeline.
                Account newAccount = null;
                try
                {
                    newAccount = HomeSupportHelper.get(ctx).storeBean(ctx, account);
                    ctx = ctx.createSubContext().put(Account.class, newAccount);
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Error occurred setting registration status of account " + account.getBAN()
                            + " to " + notRegisteredStatus + ": " + e.getMessage(), e).log(ctx);
                }

                if (newAccount == null
                        || newAccount.getRegistrationStatus() == RegistrationStatusEnum.NOT_REGISTERED_INDEX)
                {
                    new MinorLogMsg(this, "Failed to update registration status of account " + account.getBAN()
                            + " to " + notRegisteredStatus, null).log(ctx);
                }
            }
        }
        else
        {
            new MinorLogMsg(this, "Failed to update registration status of account to " + notRegisteredStatus
                    + ".  No account could be found to update.", null).log(ctx);
        }
    }

}
