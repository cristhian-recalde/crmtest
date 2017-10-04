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
package com.trilogy.app.crm.account;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.state.InOneOfStatesPredicate;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;

/**
 * @author Angie Li
 *
 * Implementation of the AccountHierarchyService interface methods.
 * 
 * Given the BAN of the root account, the methods defined in this interface will
 * return results on queries involving this account's the entire topology (a.k.a. 
 * heirarchy -- its non-responsible sub-accounts).
 * 
 * There is a need for this, since searches started at the subscriber level (via the GUI)
 * have the in the context filtered SubscriberHome according to the BAN the subscriber 
 * belongs to.
 * 
 * To avoid filtered Homes in the context. We install this service that contains its own
 * context.
 */
public class AccountHierarchyServiceImpl extends ContextAwareSupport 
    implements AccountHierarchyService 
{

    public AccountHierarchyServiceImpl(Context ctx)
    {
        setContext(ctx);
    }
    
    public int getNumberOfActivePostpaidSubscribersInTopology(Account parentAccount)
    {
        Collection subs = null;
        try
        {
            // this method always returns a non-NULL collection
            subs = AccountSupport.getNonResponsibleSubscribers(getContext(), parentAccount);
            
            subs = CollectionSupportHelper.get(getContext()).findAll(getContext(), subs, new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
            
            subs = CollectionSupportHelper.get(getContext()).findAll(getContext(), subs,new InOneOfStatesPredicate(
                    SubscriberStateEnum.IN_ARREARS, 
                    SubscriberStateEnum.NON_PAYMENT_WARN,
                    SubscriberStateEnum.IN_COLLECTION,
                    SubscriberStateEnum.PROMISE_TO_PAY,
                    SubscriberStateEnum.NON_PAYMENT_SUSPENDED,
                    SubscriberStateEnum.ACTIVE));
            
        }
        catch(HomeException exp)
        {
            if(LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this,"Failed to retrieve all Active Subscribers from account="
                        + parentAccount.getBAN(),exp).log(getContext());
            }
            return 0; //If an exception is thrown during the check, return 0.
        }
        return subs.size();
    }

}
