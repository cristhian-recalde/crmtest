/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package com.trilogy.app.crm.home.account;

import java.util.Collection;

import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.OrderBy;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.OrderByHome;
import com.trilogy.app.crm.bean.BillingTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;

/*
 * The purpose of this class is to set bill cycle for Postpaid Subscriber creation, if bill cycle is not provided.(only via API/Bulk Load).
 * 
 * @author piyush.shirke@redknee.com
 * since 9.4.1
 */

public class AccountBillCycleCreateHome extends HomeProxy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    /**
     * Create a new instance of <code>AccountBillCycleCreateHome</code>.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            Delegate of this home.
     */
    public AccountBillCycleCreateHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }
	
	  @Override
	    public Object create(final Context ctx, final Object obj) throws HomeException
	    {
		  	Account account = (Account) obj;
		  	int  billcycleid = account.getBillCycleID();
			
		  	if(!account.isPostpaid() || billcycleid != -1)
				  return super.create(ctx, account);
		  	
			  Home BillCycleHome = (Home) ctx.get(com.redknee.app.crm.bean.BillCycleHome.class);
			  final OrderBy order = new OrderBy(BillCycleXInfo.DAY_OF_MONTH, true);
			  Home orderedHome = new OrderByHome(ctx, order, BillCycleHome );
			  BillCycleHome  = orderedHome ;
			  And autoBillCycleFilter = new And();
			  autoBillCycleFilter.add(new EQ(BillCycleXInfo.SPID, account.getSpid()));
			  autoBillCycleFilter.add(new LT(BillCycleXInfo.BILL_CYCLE_ID, CoreCrmConstants.AUTO_BILL_CYCLE_START_ID));
			  autoBillCycleFilter.add(new EQ(BillCycleXInfo.AUTO_BILL_CYCLE_SUPPORTED ,true));
			//  autoBillCycleFilter.add(new NEQ(BillCycleXInfo.BILLING_TYPE , BillingTypeEnum.PREPAID_INDEX));
			  Collection<BillCycle> col = BillCycleHome.select(ctx, autoBillCycleFilter);
			  
			  int minBillCycleDay = -1;
			  CalendarSupport calSupp = CalendarSupportHelper.get(ctx);
			  int currDay = calSupp.getDayOfMonth(calSupp.getRunningDate(ctx));

			  for (BillCycle  billCycle: col) {

				  int billcycleday = billCycle.getDayOfMonth();
				  if(minBillCycleDay == -1){
					  minBillCycleDay = billCycle.getBillCycleID();
				  }

				  if(billcycleday > currDay){
					  billcycleid = billCycle.getBillCycleID();
					  break ;
				  }
			  }
			  if(billcycleid == -1){
				  billcycleid = minBillCycleDay ;
			  }

			  account.setBillCycleID(billcycleid);

		  return super.create(ctx, account);
	    }
}
