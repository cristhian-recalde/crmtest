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
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountReasonCodeMapping;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingHome;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingXInfo;
import com.trilogy.app.crm.bean.AccountStateChangeReason;
import com.trilogy.app.crm.bean.AccountStateChangeReasonHome;
import com.trilogy.app.crm.bean.AccountStateChangeReasonXInfo;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMapping;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMappingHome;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMappingXInfo;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class AccountStateChangeReasonCodeUpdateHome extends HomeProxy{

	public AccountStateChangeReasonCodeUpdateHome(Context ctx,final Home home) {
		super(ctx, home);
	}
	
	 @Override
	    public Object store(final Context ctx, final Object obj) throws HomeException
	    {
		 Account curracc = (Account)obj;
		 if(curracc !=null)
		 {
			 Home mapHome = (Home)ctx.get(AccountReasonCodeMappingHome.class);
			 And filter = new And();
			 filter.add(new EQ(AccountReasonCodeMappingXInfo.ACCOUNT_STATE,curracc.getState()));
			 Collection<AccountReasonCodeMapping> mapCOll = mapHome.select(ctx, filter);
			 
			 if((curracc.getState().getIndex() == AccountStateEnum.INACTIVE_INDEX)||(curracc.getState().getIndex() == AccountStateEnum.IN_COLLECTION_INDEX))
			 {
				 if((curracc.getStateChangeReason() !=0)&&(curracc.getStateChangeReason() != -1))
				 {
					 Home reasonHome = (Home)ctx.get(AccountStateChangeReasonHome.class);				
					 				 
					 if(!mapCOll.isEmpty())
					 {
						 for (AccountReasonCodeMapping accountReasonCodeMapping : mapCOll)
						 {
							 if(accountReasonCodeMapping.getReasonCode() == curracc.getStateChangeReason())
							 {
								 curracc.setStateChangeReason(accountReasonCodeMapping.getReasonCode());
								 return super.store(ctx, curracc);
							 }
						 }
						 for (AccountReasonCodeMapping accountReasonCodeMapping : mapCOll)
						 {
							 if(accountReasonCodeMapping.getIsDefault() == Boolean.TRUE)
							 {
								 
								 curracc.setStateChangeReason(accountReasonCodeMapping.getReasonCode());
								 return super.store(ctx, curracc);
							 }
						 }
					 }
					 
				 } else if((curracc.getStateChangeReason() == -1)||(curracc.getStateChangeReason()==0))
				 {
					if(!mapCOll.isEmpty())
					{
						for (AccountReasonCodeMapping accountReasonCodeMapping : mapCOll)
						{
							if(accountReasonCodeMapping.getIsDefault()==Boolean.TRUE)
							 {
								 curracc.setStateChangeReason(accountReasonCodeMapping.getReasonCode());
							 }
						}
					} 
				 }
				return super.store(ctx, curracc);
			 }
			
			
			
			
			
		 }
		 return super.store(ctx, obj);
	    }
}
