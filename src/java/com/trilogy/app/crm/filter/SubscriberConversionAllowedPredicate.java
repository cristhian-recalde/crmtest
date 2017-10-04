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

import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.AuthSupport;


/**
 * A predicate that determines whether or not a given subscriber can be converted from
 * postpaid to prepaid or visa versa.
 *
 * This was refactored out of com.redknee.app.crm.web.control.ConvergeSubscriberWebControl
 * 
 * @author Aaron Gourley
 * @since 7.5
 *
 */
public class SubscriberConversionAllowedPredicate implements Predicate
{
    public static final String CONVERSION_PERMISSION = "conversion";
    
    private static SubscriberConversionAllowedPredicate instance_ = null;
    public static SubscriberConversionAllowedPredicate instance()
    {
        if( instance_ == null )
        {
            instance_ = new SubscriberConversionAllowedPredicate();
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
                // check permission first
                if (AuthSupport.hasPermission(ctx, new SimplePermission(CONVERSION_PERMISSION)))
                {
                    if (!isValidSubscriberState(sub))
                    {
                        return false;
                    }

                    Home acctHome = (Home)ctx.get(AccountHome.class);
                    Account acct = (Account)acctHome.find(ctx,sub.getBAN());
                    if(acct==null)
                    {
                        return true;
                    }

                    switch (acct.getState().getIndex())
                    {
                    case AccountStateEnum.NON_PAYMENT_SUSPENDED_INDEX: //dunned
                    case AccountStateEnum.IN_ARREARS_INDEX: // in arrear
                    case AccountStateEnum.INACTIVE_INDEX:
                    case AccountStateEnum.PROMISE_TO_PAY_INDEX:
                        return false;
                    default:
                        if( SubscriberTypeEnum.POSTPAID.equals(sub.getSubscriberType())
                                && sub.isPooledGroupLeader(ctx) )
                        {
                            // Do not allow users to change the subscriber type
                            // of a group leader to PREPAID.
                            return false;
                        }
                        //return sub.getAmountOwing() == 0 ;
                        return true;
                    }
                }
                return false;
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, e.getMessage(), e).log(ctx);
                return true;
            }
        }
        return false;
    }
    
    private boolean isValidSubscriberState(Subscriber sub)
    {
        switch (sub.getState().getIndex())
        {
        case SubscriberStateEnum.PENDING_INDEX:
        case SubscriberStateEnum.AVAILABLE_INDEX:
        case SubscriberStateEnum.INACTIVE_INDEX:
        case SubscriberStateEnum.LOCKED_INDEX:
            return false;
        }
        return true;
    }
}
