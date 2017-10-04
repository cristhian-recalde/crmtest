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

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bas.recharge.CompoundVisitor;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.AccountSupport;

/**
 * @author rattapattu
 */
public class ChildSubBillCycleUpdateHome extends HomeProxy
{
    private static final long serialVersionUID = 1L;

    public ChildSubBillCycleUpdateHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
    
    @Override
    public Object store(Context ctx,final Object obj)
    throws HomeException
    {
        Account newAccount = (Account)obj; 
        Account oldAccount = AccountSupport.getAccount(ctx, newAccount.getBAN());
        
        newAccount = (Account) super.store(ctx, obj);

        if(oldAccount.getBillCycleID() != newAccount.getBillCycleID()
                && !AccountStateEnum.INACTIVE.equals(newAccount.getState()))
        {
            BillCycle oldBillCycle = null;
            BillCycle newBillCycle = null;
            try
            {
                oldBillCycle = oldAccount.getBillCycle(ctx);
                newBillCycle = newAccount.getBillCycle(ctx);
            }
            catch (HomeException e)
            {
                LogSupport.major(ctx,this,"Unable to retrieve Bill Cycle information for the Account :" + newAccount.getBAN(),e);
                HomeException ex = new HomeException("Unable to retrieve Bill Cycle information for the Account :" + newAccount.getBAN());
                throw ex;
            }
            if (oldBillCycle != null && newBillCycle != null
                    && oldBillCycle.getDayOfMonth() != -1
                    && oldBillCycle.getDayOfMonth() != newBillCycle.getDayOfMonth())
            {
                final short billCycleDay = Integer.valueOf(newBillCycle.getDayOfMonth()).shortValue();
                
                try
                {
                    CompoundVisitor v = new CompoundVisitor();
                    v.add(new UpdateIpcBillCycleVisitor(billCycleDay));
                    v.add(new UpdateSmsbBillCycleVisitor(billCycleDay));
                    
                    And filter = new And();
                    filter.add(new EQ(SubscriberXInfo.BAN, newAccount.getBAN()));
                    filter.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
                    ((Home) ctx.get(SubscriberHome.class)).forEach(ctx, v, filter);
                }
                catch (Throwable t)
                {
                    /*
                     * Errors happening at the client level is logged but we continue running.
                     * It doesn't make sense to abort since there is no rollback mechanism unless doing the same
                     * process with the previous billcycle date which could meet the same fate.
                     */
                    final String message = "Could not update BillCycle Subscribers under Account BAN ["
                        + newAccount.getBAN() + "]. Error [" + t.getMessage() + "]";
                    new MinorLogMsg(this, message, t).log(ctx);
                    ExceptionListener excl = (ExceptionListener) ctx.get(ExceptionListener.class);
                    if (null != excl)
                    {
                        excl.thrown(new IllegalStateException(message, t));
                    }
                }
            }
        }
        
        return newAccount;
    }
}
