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
package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.SubscriptionTypeAware;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;

/**
 * Provides enum choice filtering for the Service screen to shows only the
 * enum values allowed for the currently selected SubscriptionType.
 *
 * @author victor.stratan@redknee.com
 */
public class ServiceTypeOnSubTypeFilteringPredicate implements Predicate
{
    public boolean f(final Context ctx, final Object obj) throws AbortVisitException
    {
        if (!(obj instanceof ServiceTypeEnum))
        {
            LogSupport.debug(ctx, this, "Only ServiceTypeEnum is supported");
            return false;
        }

        final Object bean = ctx.get(AbstractWebControl.BEAN);
        if (!(bean instanceof SubscriptionTypeAware))
        {
            LogSupport.debug(ctx, this, "Only Subscription-Type-Aware edit screens are supported");
            return false;
        }

        final SubscriptionTypeAware typeAwareBean = (SubscriptionTypeAware) bean;

        if (obj != ServiceTypeEnum.GENERIC)
        {
            SubscriptionType subscriptionType = typeAwareBean.getSubscriptionType(ctx);
            
            if (obj == ServiceTypeEnum.ALCATEL_SSC)
            {
                return subscriptionType.isOfType(SubscriptionTypeEnum.BROADBAND);
            }
            else if(obj == ServiceTypeEnum.DATA)
            {
            	return (subscriptionType.isOfType(SubscriptionTypeEnum.BROADBAND) 
            			|| subscriptionType.isOfType(SubscriptionTypeEnum.AIRTIME));
            }
            else if(obj == ServiceTypeEnum.VOICE)
            {
            	return (subscriptionType.isOfType(SubscriptionTypeEnum.WIRE_LINE)
            			|| subscriptionType.isOfType(SubscriptionTypeEnum.AIRTIME));
            }
            
            return subscriptionType.isOfType(SubscriptionTypeEnum.AIRTIME);
        }

        return true;
    }

    /**
     * Provides access to a singleton instance of this class.
     *
     * @return A singleton instance of this class.
     */
    public static ServiceTypeOnSubTypeFilteringPredicate getInstance()
    {
        return instance_;
    }

    /**
     * A singleton instance of this class.
     */
    private static final ServiceTypeOnSubTypeFilteringPredicate instance_ =
        new ServiceTypeOnSubTypeFilteringPredicate();

} // class