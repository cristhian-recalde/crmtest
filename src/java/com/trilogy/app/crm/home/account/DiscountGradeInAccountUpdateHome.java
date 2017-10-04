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
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.DiscountGrade;
import com.trilogy.app.crm.bean.DiscountGradeHome;
import com.trilogy.app.crm.bean.DiscountGradeXInfo;
import com.trilogy.app.crm.bean.DiscountStrategyEnum;
import com.trilogy.app.crm.bean.GroupTypeEnum;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.session.SessionInfo;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author vikash.kumar@redknee.com
 * @since 2016-10-16 
 * Purpose: To add entry of default DiscountGrade into Account
 *        table once a account is created.
 */

public class DiscountGradeInAccountUpdateHome extends HomeProxy {

	private static String MODULE = DiscountGradeInAccountUpdateHome.class.getName();

	public DiscountGradeInAccountUpdateHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
	public Object store(final Context ctx, final Object obj) throws HomeException {
		LogSupport.info(ctx, MODULE, "storeRecord : Starting update of Discount Grade in account table");
		
		Account account = (Account) obj;
		
		//Validating DiscountGrade, if it is not present in db then throw exception.
		//by pass validation in case of empty discountGrade.
		if (!account.getDiscountGrade().isEmpty()) 
		{
			Home home = (Home) ctx.get(DiscountGradeHome.class);
			And filter = new And();
			filter.add(new EQ(DiscountGradeXInfo.SPID, account.getSpid()));
			filter.add(new EQ(DiscountGradeXInfo.IDENTIFIER, account.getDiscountGrade()));

			Collection<DiscountGrade> col = home.select(ctx, filter);
			if (col.size() <= 0) {
				String message = ". Unable to find DiscountGrade:  " + account.getDiscountGrade();
				throw new HomeException(message);
			}
		}
		
		if((obj instanceof Account)&& (obj !=null))
		{	
			Home crmSpidHome = (Home) ctx.get(com.redknee.app.crm.bean.CRMSpidHome.class);
			CRMSpid spid = (CRMSpid) crmSpidHome.find(account.getSpid());

			//1. For DiscountStrategyEnum.ROOT & GROUP account, updating discountGrade in Account table with new value provided.
			//2. For DiscountStrategyEnum.SELF & SUBSCRIBER account, updating discountGrade in Account table with new value provided.
			//3. Other scenario, setting null as discountGrade in Account table.
			if ((spid.getDiscountStrategy().equals(DiscountStrategyEnum.ROOT) && GroupTypeEnum.GROUP.equals(account.getGroupType()) )  || 				
				(spid.getDiscountStrategy().equals(DiscountStrategyEnum.ROOT) && GroupTypeEnum.SUBSCRIBER.equals(account.getGroupType()) && account.getParentBAN().isEmpty() ) ||
				(spid.getDiscountStrategy().equals(DiscountStrategyEnum.SELF) && GroupTypeEnum.SUBSCRIBER.equals(account.getGroupType()) ))
			{
				return super.store(ctx, account);
			}
			else
			{
				account.setDiscountGrade("");
			}		
		}
		return super.store(ctx, obj);
		
	}

	@Override
	public Object create(final Context ctx, final Object obj) throws HomeException {
		LogSupport.info(ctx, MODULE, "createRecord : New entry in account table for Discount Grade.");
		Account account = (Account) obj;
		Home home = (Home) ctx.get(DiscountGradeHome.class);
		
		Home crmSpidHome = (Home) ctx.get(com.redknee.app.crm.bean.CRMSpidHome.class);
		CRMSpid spid = (CRMSpid) crmSpidHome.find(account.getSpid());

		//1. For DiscountStrategyEnum.ROOT & GROUP account, setting default discountGrade in Account table.
		//2. For DiscountStrategyEnum.SELF & SUBSCRIBER account, setting default discountGrade in Account table.
		//3. Other scenario, setting null as discountGrade in Account table.
		if ((spid.getDiscountStrategy().equals(DiscountStrategyEnum.ROOT) && GroupTypeEnum.GROUP.equals(account.getGroupType()) )  || 				
			(spid.getDiscountStrategy().equals(DiscountStrategyEnum.ROOT) && GroupTypeEnum.SUBSCRIBER.equals(account.getGroupType()) && account.getParentBAN().isEmpty() ) ||
			(spid.getDiscountStrategy().equals(DiscountStrategyEnum.SELF) && GroupTypeEnum.SUBSCRIBER.equals(account.getGroupType()) ))
		{	
			And filter = new And();
			filter.add(new EQ(DiscountGradeXInfo.SPID, account.getSpid()));
			filter.add(new EQ(DiscountGradeXInfo.IS_DEFAULT, Boolean.TRUE));

			// Fetch only default Discount Grade
			Collection<DiscountGrade> col = home.select(ctx, filter);
			if (col.size() > 0) {
				for (DiscountGrade dbBean : col) {
					account.setDiscountGrade(dbBean.getIdentifier());
				}
			} else {
				account.setDiscountGrade("");
			}
		}
		else
		{
			account.setDiscountGrade("");
		}
		return super.create(ctx, account);
	}
}
