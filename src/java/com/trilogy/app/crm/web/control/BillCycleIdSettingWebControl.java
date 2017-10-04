/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.support.AccountSupport;

/**
 * @author cindy.wong@redknee.com
 * @since 2011-05-20
 */
public class BillCycleIdSettingWebControl extends ProxyWebControl
{

	/**
	 * Constructor for BillCycleIdSettingWebControl.
	 * 
	 * @param delegate
	 */
	public BillCycleIdSettingWebControl(WebControl delegate)
	{
		super(delegate);
	}

	@Override
	public void fromWeb(Context ctx, Object p1,
	    javax.servlet.ServletRequest p2, String p3)
	{
		super.fromWeb(ctx, p1, p2, p3);
		int mode = ctx.getInt("MODE", DISPLAY_MODE);
		if (mode == CREATE_MODE)
		{
			setBillCycleId(ctx, (BillCycleHistory) p1);
		}
	}

	@Override
	public Object fromWeb(Context ctx, javax.servlet.ServletRequest p1,
	    String p2)
	{
		Object bean = super.fromWeb(ctx, p1, p2);
		setBillCycleId(ctx, (BillCycleHistory) bean);
		int mode = ctx.getInt("MODE", DISPLAY_MODE);
		if (mode == CREATE_MODE)
		{
			setBillCycleId(ctx, (BillCycleHistory) p1);
		}
		return bean;
	}

	@Override
	public void
	    toWeb(Context ctx, java.io.PrintWriter p1, String p2, Object p3)
	{
		setBillCycleId(ctx, (BillCycleHistory) p3);
		super.toWeb(ctx, p1, p2, p3);
	}

	private void setBillCycleId(Context ctx, BillCycleHistory bean)
	{
		Object ban = bean.getBAN();
		if (!(ban instanceof String))
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this, "BAN is not set");
			}
			return;
		}
		Account account = null;
		try
		{
			account = AccountSupport.getAccount(ctx, ban.toString());
		}
		catch (HomeException e)
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this,
				    "Exception caught when looking up account", e);
			}
			return;
		}
		if (account == null)
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this, "BAN is not set");
			}
			return;
		}
		bean.setOldBillCycleID(account.getBillCycleID());
	}

}
