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
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.exception.RethrowExceptionListener;

/**
 * Validates that the subscription technology is allowed by the specified subscription type.
 *
 * @author victor.stratan@redknee.com
 */
public final class SubscriptionUniqueOnTypeValidator extends AbstractSubscriberValidator
{

    /**
     * Singleton instance.
     */
    private static SubscriptionUniqueOnTypeValidator instance;


    /**
     * Prevents initialization
     */
    private SubscriptionUniqueOnTypeValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SubscriptionClassSegmentAndTechnologyValidator</code>.
     *
     * @return An instance of <code>SubscriptionClassSegmentAndTechnologyValidator</code>.
     */
    public static SubscriptionUniqueOnTypeValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriptionUniqueOnTypeValidator();
        }

        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Object operation = ctx.get(HomeOperationEnum.class);
        if (!HomeOperationEnum.CREATE.equals(operation))
        {
            // validate ID only during create. ID does not change.
            return;
        }

        final RethrowExceptionListener exceptions = new RethrowExceptionListener();
        final Subscriber sub = (Subscriber) obj;
        Home home = (Home) ctx.get(SubscriberHome.class);

        Subscriber accountSubscription = null;
        try
        {
            And condition = new And();
            condition.add(new EQ(SubscriberXInfo.BAN, sub.getBAN()));
            condition.add(new EQ(SubscriberXInfo.SUBSCRIPTION_TYPE, Long.valueOf(sub.getSubscriptionType())));
            
            /*
             * Right now URCS does not allow multiple subscriptions of the same subscription type in the same individual account.
             * Nevertheless, when it is supported, this line should be uncommented so that CRM will only not allow multiple subscriptions
             * of the same subscription type with the same MSISDN.
             */
            // condition.add(new EQ(SubscriberXInfo.MSISDN, sub.getMSISDN()));

            Or stateOr = new Or();
            stateOr.add(new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
            stateOr.add(new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.MOVED));
            condition.add(new Not(stateOr));

            accountSubscription = (Subscriber) home.find(ctx, condition);
        }
        catch (final HomeException e)
        {
            exceptions.thrown(new IllegalStateException("Cannot retrieve current subscriptions for the Account \"" + sub.getBAN() + "\" and the MSISDN \""
                    + "\"", e));
        }

        if (accountSubscription!=null)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.SUBSCRIPTION_TYPE,
                    "This Subscription Type is already present in this Account for MSISDN \"" + sub.getMSISDN() 
                        + "\" with subscription ID \""
                        + accountSubscription.getId() + "\""));
        }

        exceptions.throwAllAsCompoundException();
    }

}