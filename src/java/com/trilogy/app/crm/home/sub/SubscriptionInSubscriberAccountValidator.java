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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * Validates that the subscription technology is allowed by the specified subscription type.
 *
 * @author victor.stratan@redknee.com
 */
public final class SubscriptionInSubscriberAccountValidator extends AbstractSubscriberValidator
{

    /**
     * Singleton instance.
     */
    private static SubscriptionInSubscriberAccountValidator instance;


    /**
     * Prevents initialization
     */
    private SubscriptionInSubscriberAccountValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SubscriptionClassSegmentAndTechnologyValidator</code>.
     *
     * @return An instance of <code>SubscriptionClassSegmentAndTechnologyValidator</code>.
     */
    public static SubscriptionInSubscriberAccountValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriptionInSubscriberAccountValidator();
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

        Account account = (Account) ctx.get(Lookup.ACCOUNT);

        if (!account.isIndividual(ctx) && sub.getPricePlan() < PricePlanSupport.POOL_PP_ID_START)
        {
            // price plan id > POOL_PP_ID_START means it's the hidden pool subscription
            exceptions.thrown(new IllegalStateException("Subscriptions can be created only in an Account of type "
                    + "Subscriber. Current Account \"" + sub.getBAN() + "\" is not a Subscriber Account."));
        }

        exceptions.throwAllAsCompoundException();
    }

}