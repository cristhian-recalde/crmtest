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
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccntGrpScreeningTemp;
import com.trilogy.app.crm.bean.AccntGrpScreeningTempXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * This home is responsible to create list of group screenig template classes if specified in GUI
 * 
 * @author ankit.nagpal
 * @since 9.9
 *
 */

public class AccountsGroupScreeningTemplateUpdateHome extends HomeProxy {

	/**
	 * default serial version id
	 */
	private static final long serialVersionUID = 1L;
	
	public AccountsGroupScreeningTemplateUpdateHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}
	
	
	
	@Override
	 public Object create(Context ctx, Object obj)
			 throws HomeException, HomeInternalException {
		
		Account account = (Account)obj;
		super.create(ctx, account);

		Set <Long>set = account.getScreeningTemplateApplicable();
		if (set.size()!= 0) {
			
			for (Long screeningTemplate : set) {
				AccntGrpScreeningTemp accountsGroupScreeningTemplate = new AccntGrpScreeningTemp();
				accountsGroupScreeningTemplate.setBAN(account.getBAN());
				accountsGroupScreeningTemplate.setScreeningTemplate(screeningTemplate);
				accountsGroupScreeningTemplate.setSpid(account.getSpid());

				HomeSupportHelper.get(ctx).createBean(ctx, accountsGroupScreeningTemplate);
			}
		}
		return obj;
	}
	


	@Override
	public Object store(Context ctx, Object obj)
			throws HomeException, HomeInternalException {

		Account account = (Account)obj;

		Set <Long>newSet = account.getScreeningTemplateApplicable();
		

		And and = new And ();
		and.add(new EQ(AccntGrpScreeningTempXInfo.BAN, account.getBAN()));
		and.add(new EQ(AccntGrpScreeningTempXInfo.SPID, account.getSpid()));
		
		Collection<AccntGrpScreeningTemp> coll = HomeSupportHelper.get(ctx).getBeans(ctx, AccntGrpScreeningTemp.class, and);
		
		if (newSet.size() != 0 || coll.size() != 0) {
			
			/**
			 * remove all previously stored bean for this ban and spid
			 */
			for (AccntGrpScreeningTemp acc : coll){
				HomeSupportHelper.get(ctx).removeBean(ctx, acc);
			}
			
			/**
			 *  Create new entries
			 */
			for (Long screeningTemplate : newSet) {
				AccntGrpScreeningTemp accountsGroupScreeningTemplate = new AccntGrpScreeningTemp();
				accountsGroupScreeningTemplate.setBAN(account.getBAN());
				accountsGroupScreeningTemplate.setScreeningTemplate(screeningTemplate);
				accountsGroupScreeningTemplate.setSpid(account.getSpid());

				HomeSupportHelper.get(ctx).createBean(ctx, accountsGroupScreeningTemplate);
			}
		}
		return getDelegate(ctx).store(ctx, obj);
	}
	
}
