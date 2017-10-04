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

package com.trilogy.app.crm.billcycle;

import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/*
 * Validating Bill Cycle for Day of Month.
 * 
 * @author piyush.shirke@redknee.com
 * since 9.4.1
 */

public class BillCycleValidator implements Validator {

	public BillCycleValidator(){

	}

	@Override
	public void validate(Context ctx, Object obj)throws IllegalStateException {
	
		// This validation is not required when an existing BC is being updated.
		if( HomeOperationEnum.CREATE.equals(ctx.get(HomeOperationEnum.class)) )
		{
			try{
				CompoundIllegalStateException el = new CompoundIllegalStateException();
				BillCycle billCycle = (BillCycle)obj;
				if (LogSupport.isDebugEnabled(ctx))
				{
					new DebugLogMsg(this, "Bill cycle id   is "+billCycle.getBillCycleID()+ ",Day of Month :"+billCycle.getDayOfMonth(), null).log(ctx);
				}
				 
				if(billCycle.getBillCycleID()>=CoreCrmConstants.AUTO_BILL_CYCLE_START_ID){
					//Skipping validation prepaid autobillcycle id's
					return;
				}
				
				Home billCycleHome = (Home) ctx.get(com.redknee.app.crm.bean.BillCycleHome.class);
				int billCycleDayofMonth = billCycle.getDayOfMonth();
				
				final And condition1 = new And();
				condition1.add(new EQ(BillCycleXInfo.DAY_OF_MONTH, billCycleDayofMonth));
				condition1.add(new EQ(BillCycleXInfo.SPID,billCycle.getSpid()));
				
				
				billCycleHome = (Home) billCycleHome.where(ctx, condition1);
				if(billCycleHome.selectAll().size() > 0){
					
					new MinorLogMsg(this,"BillCycleValidator::validate():Bill Cycle already exist for day:"+billCycleDayofMonth,null).log(ctx);
					el.thrown(new IllegalPropertyArgumentException(
							"",	"Bill Cycle already exists for Day of Month: "+billCycleDayofMonth));
					el.throwAll();
				}
		
	
			}catch (HomeException e) {
	
				new MinorLogMsg(this,e.toString(),null).log(ctx);
			}
		}
	}

}
