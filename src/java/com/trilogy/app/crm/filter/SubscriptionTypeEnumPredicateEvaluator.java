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
package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionClass;
import com.trilogy.app.crm.bean.account.SubscriptionClassHome;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Evaluates the subscription type for a subscriber to see if the 
 * enum can be displayed or not
 * @author arturo.medina@redknee.com
 *
 */
public class SubscriptionTypeEnumPredicateEvaluator extends AbstractPredicateEvaluator implements
        EnumPredicateEvaluator
{

    /**
     * @param delegate
     */
    public SubscriptionTypeEnumPredicateEvaluator(EnumPredicateEvaluator delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    public boolean evaluate(Context ctx, AbstractEnum value)
    {
        boolean result = false;
        
        if (value == null || !(value instanceof SubscriberTypeEnum))
        {
            return result;
        }
        
        Subscriber subscriber = null;
        
        Object obj = ctx.get(AbstractWebControl.BEAN);
        
        if ( obj instanceof ConvertSubscriptionBillingTypeRequest )
        {
            ConvertSubscriptionBillingTypeRequest conversion = (ConvertSubscriptionBillingTypeRequest) obj;
            subscriber = conversion.getOldSubscription(ctx);
        }
        else
        {
            subscriber = (Subscriber) obj;
        }
        
        SubscriberTypeEnum subType = (SubscriberTypeEnum)value;

        if (subscriber != null)
        {
            try
            {
                Home home = (Home) ctx.get(SubscriptionClassHome.class);

                SubscriptionClass subClass = (SubscriptionClass) home.find(ctx, Long.valueOf(subscriber.getSubscriptionClass()));
                
                if (subClass != null)
                {
                    if (subType.getIndex() == subClass.getSegmentType() ||
                            subClass.getSegmentType() == SubscriberTypeEnum.HYBRID_INDEX)
                    {
                        result = delegate(ctx, value);
                    }
                }

            }
            catch (HomeException e)
            {
                LogSupport.major(ctx, this, "Home Exception occured while getting the subscriber type ", e);
            }
        }
        
        return result;
    }

}
