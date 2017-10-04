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
package com.trilogy.app.crm.subscriber.filter;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bulkloader.AcctSubBulkLoadRequestServicer;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * This predicate determines whether or not the given subscriber's expiry date:
 *    a) is meaningful
 *    b) must be enforced
 *    
 * It is also used to show/hide the expiry field from the subscriber bean on the GUI
 *    
 * @author Aaron Gourley
 * @since 7.5
 *
 */
public class SubscriberUseExpiryPredicate implements Predicate
{
    public static final String PM_MODULE = SubscriberUseExpiryPredicate.class.getName();
    
    private static SubscriberUseExpiryPredicate instance_ = null;
    public static SubscriberUseExpiryPredicate instance()
    {
        if( instance_ == null )
        {
            instance_ = new SubscriberUseExpiryPredicate();
        }
        return instance_;
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context
     * .Context, java.lang.Object)
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        if (obj instanceof Subscriber)
        {
            Subscriber bean = (Subscriber) obj;
            if (bean.isPrepaid())
            {
                PMLogMsg pm = new PMLogMsg(PM_MODULE, " Prepaid expiry configuration check ");
                try
                {
                    if (!bean.isPooled(ctx))
                    {
                        // Non-pooled account, Prepaid subscriber:
                        // Check PrepaidNonPooledExpiry SPID property
                        return SpidSupport.getCRMSpid(ctx, bean.getSpid()).isPrepaidNonPooledExpiry();
                    }
                    else
                    {
                        if (bean.isPooledGroupLeader(ctx))
                        {
                            // Pooled account, Prepaid group owner subscriber:
                            // Pools never expire
                            return false;
                        }
                        else
                        {
                            // Pooled account, Prepaid member subscriber:
                            // Check PrepaidNonPooledExpiry SPID property
                            return SpidSupport.getCRMSpid(ctx, bean.getSpid()).isPrepaidPooledExpiry();
                        }
                    }
                }
                catch (HomeException he)
                {
                    new MinorLogMsg(this,
                            "Eception while applying expiry predicate to Subcriber[" + bean.getId() + "]", he).log(ctx);
                }
                finally
                {
                    pm.log(ctx);
                }
            }
            return !bean.isPostpaid();
        }
        return false;
    }
}
