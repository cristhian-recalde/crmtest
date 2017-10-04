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

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.webcontrol.HiddenWebControl;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.UserLinkWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.BillCycleHistoryWebControl;
import com.trilogy.app.crm.bean.BillCycleHistoryXInfo;
import com.trilogy.app.crm.bean.BillCycleKeyWebControl;

/**
 * @author cindy.wong@redknee.com
 * @since 2011-05-19
 */
public class CRMBillCycleHistoryWebControl extends
    BillCycleHistoryWebControl
{
	@Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		if (obj == null)
		{
			try
			{
				obj = XBeans.instantiate(BillCycleHistory.class, ctx);
			}
			catch (Exception exception)
			{
				LogSupport.minor(ctx, this,
				    "Exception caught while instantiating bean", exception);
			}
			if (obj == null)
				obj = new BillCycleHistory();
		}

		Context session = Session.getSession(ctx);
		Account acct = (Account) session.get(Account.class);
		if (ctx.has(Account.class))
		{
			acct = (Account) ctx.get(Account.class);
		}
		else
		{
			ctx.put(Account.class, acct);
		}

		int mode = ctx.getInt("MODE", EDIT_MODE);
		if (mode == CREATE_MODE && acct != null)
		{
			BillCycleHistory history = (BillCycleHistory) obj;
			history.setBAN(acct.getBAN());
			history.setOldBillCycleID(acct.getBillCycleID());
		}
		super.toWeb(ctx, out, name, obj);
	}

	@Override
	public Object fromWeb(Context ctx, ServletRequest req, String name)
	{
		Object obj = null;

		try
		{
			obj = XBeans.instantiate(BillCycleHistory.class, ctx);
		}
		catch (Exception e)
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this, e.getMessage(), e).log(ctx);
			}

			obj = new BillCycleHistory();
		}

		fromWeb(ctx, obj, req, name);
		return obj;
	}

	@Override
	public WebControl getAgentWebControl()
	{
		return CUSTOM_AGENT_WC;
	}

	@Override
	public WebControl getOldBillCycleIDWebControl()
	{
		return CUSTOM_OLDBILLCYCLEID_WC;
	}

	@Override
	public WebControl getNewBillCycleIDWebControl()
	{
		return CUSTOM_NEWBILLCYCLEID_WC;
	}

	private static final WebControl CUSTOM_AGENT_WC = new UserLinkWebControl(
	    "Edit Users", true);
	private static final WebControl CUSTOM_OLDBILLCYCLEID_WC =
	    new HiddenWebControl(new ReadOnlyWebControl(
	        new BillCycleDaySettingWebControl(new BillCycleKeyWebControl(true),
	        BillCycleHistoryXInfo.OLD_BILL_CYCLE_ID,
	            BillCycleHistoryXInfo.OLD_BILL_CYCLE_DAY)));
	private static final WebControl CUSTOM_NEWBILLCYCLEID_WC =
	    new BillCycleDaySettingWebControl(new BillCycleKeyWebControl(true),
	        BillCycleHistoryXInfo.NEW_BILL_CYCLE_ID,
	        BillCycleHistoryXInfo.NEW_BILL_CYCLE_DAY);

}
