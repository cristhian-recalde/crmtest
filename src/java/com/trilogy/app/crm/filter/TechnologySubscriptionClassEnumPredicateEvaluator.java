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
import com.trilogy.app.crm.bean.account.SubscriptionClass;
import com.trilogy.app.crm.bean.account.SubscriptionClassHome;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * DEcides to display the enum depending on the subscription class
 * @author arturo.medina@redknee.com
 *
 */
public class TechnologySubscriptionClassEnumPredicateEvaluator extends
        AbstractPredicateEvaluator implements EnumPredicateEvaluator
{

    /**
     * 
     * @param delegate
     */
    public TechnologySubscriptionClassEnumPredicateEvaluator(
            EnumPredicateEvaluator delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    public boolean evaluate(Context ctx, AbstractEnum value)
    {
        boolean result = false;
        
        if (value == null || !(value instanceof TechnologyEnum))
        {
            return result;
        }
        
        Subscriber subscriber = (Subscriber)ctx.get(AbstractWebControl.BEAN);
        TechnologyEnum subType = (TechnologyEnum)value;

        if (subscriber != null)
        {
            try
            {
                Home home = (Home) ctx.get(SubscriptionClassHome.class);

                SubscriptionClass subClass = (SubscriptionClass) home.find(ctx, Long.valueOf(subscriber.getSubscriptionClass()));
                
                if (subClass != null)
                {
                    if (subType.getIndex() == subClass.getTechnologyType() ||
                            subClass.getSegmentType() == TechnologyEnum.ANY_INDEX)
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Segment type of the subscription type " +
                                    subClass.getName() + 
                                    " is " + subClass.getName() + 
                                    " delegating the evaulation to the delegate");
                        }

                        result = delegate(ctx, value);
                    }
                    else
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Technology of subscription class " +
                                    subClass.getName() + 
                                    "doesn't match the technology enum " +
                                    subType);
                        }

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
