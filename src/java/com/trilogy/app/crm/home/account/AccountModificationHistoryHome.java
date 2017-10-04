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

import java.util.Date;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountModificationHistory;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This home responsible to keep history of change in account state.
 * 
 * @author VijayG
 * @since 9.8
 *
 */

public class AccountModificationHistoryHome extends HomeProxy {

	/**
	 * default serial version id
	 */
	private static final long serialVersionUID = 1L;

	public AccountModificationHistoryHome(Context ctx, Home delegate) 
	{
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException 
	{
		Account account = (Account)obj;
		super.create(ctx, account);

		AccountModificationHistory accountModificationHistory = new AccountModificationHistory();
		accountModificationHistory.setBAN(account.getBAN());
		accountModificationHistory.setSpid(account.getSpid());
		accountModificationHistory.setOldState(account.getState().getIndex());
		accountModificationHistory.setNewState(account.getState().getIndex());
		accountModificationHistory.setDatecreated(new Date());
		HomeSupportHelper.get(ctx).createBean(ctx, accountModificationHistory);
		LogSupport.info(ctx, this, "Suceesfully added acoount modification details " + accountModificationHistory + "" +
				" in ACCOUNTMODIFICATIONHISTORY table.");
		return obj;
	}

	@Override
	public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException 
	{
		Account account = (Account)obj;
		Account oldAcct =  (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
		if(oldAcct != null && oldAcct.getState().getIndex() != account.getState().getIndex())
		{
			AccountModificationHistory accountModificationHistory = new AccountModificationHistory();
			accountModificationHistory.setBAN(account.getBAN());
			accountModificationHistory.setSpid(account.getSpid());
			accountModificationHistory.setOldState(oldAcct.getState().getIndex());
			accountModificationHistory.setNewState(account.getState().getIndex());
			accountModificationHistory.setDatecreated(new Date());
			HomeSupportHelper.get(ctx).createBean(ctx, accountModificationHistory);
			LogSupport.info(ctx, this, "Suceesfully added acoount modification details " + accountModificationHistory + "" +
					" in ACCOUNTMODIFICATIONHISTORY table.");
		}
		return getDelegate(ctx).store(ctx, obj);
	}
}
