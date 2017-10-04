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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.support.BillCycleSupport;

public class AccountBillCycleValidator implements Validator
{
	protected static AccountBillCycleValidator instance__=null;
	
	public static AccountBillCycleValidator instance()
	{
		if(instance__==null)
		{
			instance__=new AccountBillCycleValidator();
		}
		
		return instance__;
	}
	
   /**
    * check to see if the service provider selected has any Bill Cycle
    */
public void validate(Context ctx, Object obj)
      throws IllegalStateException
   {
      CompoundIllegalStateException el = new CompoundIllegalStateException();
      Account account = (Account)obj;
      int spid = account.getSpid();
      Home billCycleHome = (Home)ctx.get(BillCycleHome.class);

      try
      {
         BillCycle billCycle = (BillCycle) billCycleHome.find(ctx,
               new EQ(BillCycleXInfo.SPID, Integer.valueOf(spid)));

         // Report error if no Bill Cycle can be found for the SP selected.
         if (billCycle == null)
         {
            el.thrown(new IllegalPropertyArgumentException(
                    AccountXInfo.BILL_CYCLE_ID,
                     "No bill cycle defined for the Service Provider selected."));
         }
         else
         {
             if (account.isPostpaid()
                     && account.getBillCycleID() >= BillCycleSupport.AUTO_BILL_CYCLE_START_ID)
             {
                 el.thrown(new IllegalPropertyArgumentException(
                         AccountXInfo.BILL_CYCLE_ID,
                         "Postpaid account can not use bill cycle " + account.getBillCycleID()
                         + " because it is reserved for use by the auto bill cycles for prepaid accounts."));
             }
             else
             {
                 Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
                 if (oldAccount != null
                         && oldAccount.getBillCycleID() != account.getBillCycleID())
                 {
                     final BillCycle oldBC = oldAccount.getBillCycle(ctx);
                     final BillCycle newBC = account.getBillCycle(ctx);

                     if (newBC == null)
                     {
                         el.thrown(new IllegalPropertyArgumentException(
                                 AccountXInfo.BILL_CYCLE_ID,
                                 "Bill Cycle with ID " + account.getBillCycleID() + " does not exist."));
                     }
                     else if (oldBC != null
                             && newBC.getDayOfMonth() != oldBC.getDayOfMonth()
                             && oldBC.getDayOfMonth() != BillCycleSupport.SPECIAL_BILL_CYCLE_DAY)
                     {
                         AccountCategory cat = account.getAccountCategory(ctx);
                         if (!cat.isAllowBillCycleChange())
                         {
                             el.thrown(new IllegalPropertyArgumentException(
                                     AccountXInfo.BILL_CYCLE_ID,
                                     "Changing bill cycle not supported when the billing dates of the old and new bill cycles do not match (old day of month = "
                                     + oldBC.getDayOfMonth() + ", new day of month = " + newBC.getDayOfMonth() + "."));
                         }
                         else if (!ctx.getBoolean(Common.DURING_BILL_CYCLE_CHANGE, false))
                         {
                             el.thrown(new IllegalPropertyArgumentException(
                                     AccountXInfo.BILL_CYCLE_ID,
                                     "Changing bill cycle directly via account update not supported when the billing dates of the old and new bill cycles do not match (old day of month = "
                                     + oldBC.getDayOfMonth() + ", new day of month = " + newBC.getDayOfMonth() + "."));
                         }
                     }
                 }
             }
         }
      }
      catch (HomeException hEx)
      {
          if (LogSupport.isDebugEnabled(ctx))
          {
             new DebugLogMsg(this, "Error validating account bill cycle: " + hEx.getMessage(), hEx).log(ctx);
          }
      }

      el.throwAll();
   }
}
