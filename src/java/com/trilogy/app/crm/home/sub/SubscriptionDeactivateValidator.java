/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * Ensure that a subscription getting de-activatd or put in dormant state does not have
 * any disputes
 * 
 * @author simar.singh@redknee.com
 */
public final class SubscriptionDeactivateValidator implements Validator
{

    /**
     * Create a new instance of <code>SubscriberStateTypeValidator</code>.
     */
    protected SubscriptionDeactivateValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SubscriberStateTypeValidator</code>.
     * 
     * @return An instance of <code>SubscriberStateTypeValidator</code>.
     */
    public static SubscriptionDeactivateValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriptionDeactivateValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        if (!HomeOperationEnum.STORE.equals(ctx.get(HomeOperationEnum.class)))
        {
            // validating only store() operations
            return;
        }
        final Subscriber sub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final RethrowExceptionListener exceptions = new RethrowExceptionListener();
        final SubscriberProfileProvisionClient client;
        if (sub.isInFinalState() && oldSub != null && !oldSub.isInFinalState() && oldSub.isPrepaid()
                && SystemSupport.supportsAllowWriteOffForPrepaidSubscription(ctx) && !oldSub.isPooledGroupLeader(ctx))
   {
            if (!ctx.getBoolean(WRITE_OFF, true))
            {
                client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
                Parameters subscription = null;
                try
                {
                    subscription = client.querySubscriptionProfile(ctx, sub);
                    if (subscription != null && subscription.getBalance() != 0)
                    {
                        final IllegalPropertyArgumentException ex;
                        ex = new IllegalPropertyArgumentException(SubscriberXInfo.STATE,
                                "Prepaid and-or Wallet Subscription balance has to be Zero before Closing.");
                        exceptions.thrown(ex);
                    }
                }
                catch (HomeException e)
                {
                    final IllegalStateException ex;
                    ex = new IllegalStateException("Unable to retreive Subscription balance.", e);
                    exceptions.thrown(ex);
                }
                catch (SubscriberProfileProvisionException e)
                {
                    final IllegalStateException ex;
                    ex = new IllegalStateException("Unable to retreive Subscription balance.", e);
                    exceptions.thrown(ex);
                }
            }
        }
        exceptions.throwAllAsCompoundException();
    }

    /**
     * Singleton instance.
     */ 
    private static SubscriptionDeactivateValidator instance;
    public static final String WRITE_OFF = "IS_WRITE_OFF";
}