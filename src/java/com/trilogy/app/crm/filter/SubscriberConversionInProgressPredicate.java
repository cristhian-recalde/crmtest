/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;


/**
 * A predicate that determines whether or not a given subscriber is currently being converted
 * from postpaid to prepaid or visa versa.
 *
 * This was refactored out of com.redknee.app.crm.web.control.ConvergeSubscriberWebControl
 * 
 * @author Aaron Gourley
 * @since 7.5
 *
 */
public class SubscriberConversionInProgressPredicate implements Predicate
{
    private static SubscriberConversionInProgressPredicate instance_ = null;
    public static SubscriberConversionInProgressPredicate instance()
    {
        if( instance_ == null )
        {
            instance_ = new SubscriberConversionInProgressPredicate();
        }
        return instance_;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        if( obj instanceof Subscriber )
        {
            Subscriber sub = (Subscriber)obj;
            try
            {
                Subscriber oldSub = (Subscriber)ctx.get(Lookup.OLDSUBSCRIBER);
                if( oldSub == null )
                {
                    Home subHome = (Home)ctx.get(SubscriberHome.class);
                    oldSub = (Subscriber)subHome.find(ctx,sub.getId());
                    if( oldSub == null )
                    {
                        return false;
                    }   
                }

                if (oldSub.getSubscriberType() != sub.getSubscriberType())
                {
                    // if converting, force the state of subscriber to be untouched.
                    sub.setState(oldSub.getState());
                    // update the deposit and credit limit
                    PricePlanVersion plan  = sub.getRawPricePlanVersion(ctx);
                    if(plan != null)
                    {
                        sub.setDeposit(plan.getDeposit());
                        sub.setCreditLimit(plan.getCreditLimit());
                    }
                    return true;
                }
                return false;

            }
            catch(HomeException he)
            {
                return false;
            }
        }
        return false;
    }
}
