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
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.SubscriptionClass;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Validates that the subscription technology is allowed by the specified subscription type.
 *
 * @author victor.stratan@redknee.com
 */
public final class SubscriptionClassSegmentAndTechnologyValidator extends AbstractSubscriberValidator
{

    /**
     * Singleton instance.
     */
    private static SubscriptionClassSegmentAndTechnologyValidator instance;


    /**
     * Prevents initialization
     */
    private SubscriptionClassSegmentAndTechnologyValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SubscriptionClassSegmentAndTechnologyValidator</code>.
     *
     * @return An instance of <code>SubscriptionClassSegmentAndTechnologyValidator</code>.
     */
    public static SubscriptionClassSegmentAndTechnologyValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriptionClassSegmentAndTechnologyValidator();
        }

        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Object operation = ctx.get(HomeOperationEnum.class);
        if (!HomeOperationEnum.CREATE.equals(operation))
        {
            // validate ID only during create. ID does not change.
            return;
        }

        final Subscriber sub = (Subscriber) obj;
        final RethrowExceptionListener exceptions = new RethrowExceptionListener();

        final Account account = (Account) ctx.get(Lookup.ACCOUNT);
        if (account == null)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.BAN,
                    "Can't get account " + sub.getBAN() + " for subscriber " + sub.getId()));
            exceptions.throwAllAsCompoundException();
        }
        else if (!account.isIndividual(ctx))
        {
            // subscriptions in group accounts are special subscriptions, that are exempted from this validation
            return;
        }

        SubscriptionClass subClass = null;
        try
        {
            subClass = SubscriptionClass.getSubscriptionClassWithException(ctx, sub.getSubscriptionClass());
        }
        catch (final HomeException e)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.SUBSCRIPTION_CLASS, e));
        }


        if (subClass == null)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.SUBSCRIPTION_CLASS,
                    "Cannot obtain subscriber Subscription Class " + sub.getSubscriptionClass()));
        }
        else
        {
            if (subClass.getSegmentType() != SubscriberTypeEnum.HYBRID_INDEX
                && subClass.getSegmentType() != sub.getSubscriberType().getIndex())
            {
                exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.SUBSCRIBER_TYPE,
                        "Not allowed by selected Subscription Class."));
            }

            if (subClass.getTechnologyType() != TechnologyEnum.ANY_INDEX
                && subClass.getTechnologyType() != sub.getTechnology().getIndex())
            {
                exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.TECHNOLOGY,
                        "Not allowed by selected Subscription Class."));
            }
        }

        SubscriptionType subscriptionType = sub.getSubscriptionType(ctx);
        if (subscriptionType == null)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.SUBSCRIPTION_TYPE,
                    "Cannot obtain subscriber Subscription Type " + sub.getSubscriptionClass()));
        }
        else if (subscriptionType.isOfType(SubscriptionTypeEnum.PREPAID_CALLING_CARD))
        {
            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.SUBSCRIPTION_TYPE,
                    "Does not support creation of Prepaid calling card subscription type through CRM"));
        }
        
        exceptions.throwAllAsCompoundException();
    }

}