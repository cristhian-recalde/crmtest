/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Validates whether group account credit category allows maximum number of subscriptions
 * to be saved .
 * 
 * @author bdhavalshankh
 * @since 9.5.1
 */
public final class SubscriberCounterValidator implements Validator
{

    /**
     * Create a new instance of <code>SubscriberCounterValidator</code>.
     */
    protected SubscriberCounterValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SubscriberCounterValidator</code>.
     *
     * @return An instance of <code>SubscriberCounterValidator</code>.
     */
    public static SubscriberCounterValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberCounterValidator();
        }
        return instance;
    }

    /**
     * Singleton instance.
     */
    private static SubscriberCounterValidator instance;


    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        validateSubscriber(ctx, obj);        
    }

    /**
     * This methods validates whether the subscriber is allowed to be saved or not.
     * The maximum number of allowed subscriptions under a group account is set at credit category level.
     * And the counter of subscriptions is saved in account table . 
     * If the counter reaches upto the maximum limit, application must not allow saving the subscription .
     * 
     *   @param ctx Context
     *   @param obj Subscription reference
     *   
     */
    void validateSubscriber(final Context ctx, final Object obj) throws IllegalStateException
    {
    
        try
        {
            final Subscriber sub = (Subscriber) obj;
            final Account parentAccount = sub.getAccount(ctx);
            if (!parentAccount.isResponsible())
            {
                Account respParent = parentAccount.getResponsibleParentAccount(ctx);
                if (respParent != null && (respParent.getGroupType().getIndex() == GroupTypeEnum.GROUP_INDEX || respParent.getGroupType().getIndex() == GroupTypeEnum.GROUP_POOLED_INDEX )&& respParent.isResponsible())
                {
                    if(respParent.isPostpaid() || respParent.isHybrid())
                    {
                        CreditCategory groupCc = HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class,
                                respParent.getCreditCategory());
                        if (groupCc != null)
                        {
                            if (groupCc.getMaxSubscriptionsAllowed() > 0
                                    && respParent.getTotalNumOfSubscriptions() >= groupCc.getMaxSubscriptionsAllowed())
                            {
                                throw new IllegalStateException("Maximum allowed subscriptions at credit category with id : "+groupCc.getCode()+" is "+groupCc.getMaxSubscriptionsAllowed()+" which got exceeded for group account with BAN : "+respParent.getBAN()+" . Can not save subscription.");
                            }
                        }
                    }
                   
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Exception occured while validating subscription : "+e.getMessage());
        }
    }
}
