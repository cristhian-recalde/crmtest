/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class PTPUpdatingHome extends HomeProxy
{

    public PTPUpdatingHome(Context ctx, Home home)
    {
        super(ctx, home);
    }


    public Object store(Context ctx, Object obj) throws HomeException
    {
        Account newAccount = (Account) obj;
        Account oldAccount = AccountSupport.getAccount(getContext(), newAccount.getBAN());
        Date currentDate_ = new Date();
        if(oldAccount!=null)
        {
	        if (newAccount.getState().equals(AccountStateEnum.PROMISE_TO_PAY)
	                && !oldAccount.getState().equals(AccountStateEnum.PROMISE_TO_PAY))
	        {
	            final Date ptpIntervalStartDate = newAccount.getPromiseToPayStartDate();
	            Home ccHome = (Home) ctx.get(CreditCategoryHome.class);
	            CreditCategory cc;
	            try
	            {
	                cc = (CreditCategory) ccHome.find(ctx, Integer.valueOf(newAccount.getCreditCategory()));
	            }
	            catch (Exception e)
	            {
	                new DebugLogMsg(this, "Exception in finding credit category", e).log(ctx);
	                throw new HomeException("Error retrieving the credit category for credit category number "
	                        + newAccount.getCreditCategory(), e);
	            }
	            if (cc == null)
	            {
	                new DebugLogMsg(this, "Credit category " + newAccount.getCreditCategory() + " not found", null).log(ctx);
	                throw new HomeException("Could not find the credit category for credit category number "
	                        + newAccount.getCreditCategory());
	            }
	            if(ptpIntervalStartDate != null)
	            {
		            Calendar startDate = Calendar.getInstance();
		            startDate.setTime(ptpIntervalStartDate);
		            startDate.add(Calendar.DAY_OF_YEAR, cc.getMaxPTPInterval());
		            Date maxDate = startDate.getTime();
		            if (maxDate.before(currentDate_))
		            {
		                newAccount.setPromiseToPayStartDate(currentDate_);
		                newAccount.setCurrentNumPTPTransitions(0);
		                new InfoLogMsg(this, "Reset PTP Interval date to current date: " + currentDate_, null).log(ctx);
		            }
	            }
	            else
	            {
	                newAccount.setPromiseToPayStartDate(currentDate_);
	            }
	            newAccount.setCurrentNumPTPTransitions((newAccount.getCurrentNumPTPTransitions() + 1));
	            if (newAccount.getCurrentNumPTPTransitions() == cc.getMaxNumberPTP())
	            {
	                try
	                {
	                    //generate ER 1107
	                    ERLogger.genPTPResetER(ctx, newAccount, cc, oldAccount.getState());
	                }
	                catch (Exception e)
	                {
	                    new MinorLogMsg(this, "Error generating ER 1107 - BAN: " + newAccount.getBAN(), e).log(ctx);
	                    throw new HomeException("Error generating ER 1107 - BAN: " + newAccount.getBAN(), e);
	                }
	            }
	            else if (newAccount.getCurrentNumPTPTransitions() > cc.getMaxNumberPTP())
	            {
	                //rollback
	                newAccount.setCurrentNumPTPTransitions((newAccount.getCurrentNumPTPTransitions() - 1));
	                throw new HomeException(
	                        "The maximum number of transitions to the PTP state has occured for the account "
	                                + newAccount.getBAN());
	            }
	        }
	        else if (oldAccount.getState().equals(AccountStateEnum.PROMISE_TO_PAY)
	                && !newAccount.getState().equals(AccountStateEnum.PROMISE_TO_PAY))
	        {
	            newAccount.setPromiseToPayDate(null);
	        }
        }
        //if no exception were thrown till now..continue..
        return super.store(ctx, newAccount);
    }
}