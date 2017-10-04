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
package com.trilogy.app.crm.home.account;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.support.AccountSupport;


/**
 *  Propagates account discount class (if changed) to its 
 *  non-reponsible subscibers
 * 
 * @author candy.wong@redknee.com
 */
public class ApplyAccountDiscountToSubscriberHome extends HomeProxy
{

    public ApplyAccountDiscountToSubscriberHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    public Object store(Context ctx, Object bean) throws HomeException
    {
        Account newAccount = (Account) bean;
        // find the old Account so that we can tell whether the discount class has changed
        Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        if (newAccount.getDiscountClass() != oldAccount.getDiscountClass())
        {
            try
            {
                Home subHome = (Home) ctx.get(SubscriberHome.class);
                // Fetch all subscribers (belonging to non-responsible accounts) under the
                // ancestor
                Collection nonRespSubs = AccountSupport.getNonResponsibleSubscribers(ctx, newAccount);
                if (subHome != null && nonRespSubs != null)
                {
                    for (Iterator i = nonRespSubs.iterator(); i.hasNext();)
                    {
                        Subscriber sub = (Subscriber) i.next();
                        sub.setDiscountClass(newAccount.getDiscountClass());
                        subHome.store(ctx, sub);
                    }
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "fail to apply account discount to subscribers for account ["
                        + newAccount.getBAN() + "] discountClass [" + newAccount.getDiscountClass() + "]", null)
                        .log(ctx);
            }
        }
        return super.store(ctx, bean);
    }
}
