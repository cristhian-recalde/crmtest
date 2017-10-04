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

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * Sets the bill cycle day property of the bean based on the bill cycle ID
 * property of the bean.
 * 
 * @author cindy.wong@redknee.com
 * @since 9.1
 */
public class BillCycleDaySettingWebControl extends ProxyWebControl
{

	public BillCycleDaySettingWebControl(WebControl delegate,
	    PropertyInfo billCycleIdProperty, PropertyInfo billCycleDayProperty)
	{
		super(delegate);
		billCycleIdProperty_ = billCycleIdProperty;
		billCycleDayProperty_ = billCycleDayProperty;
	}

	@Override
	public void fromWeb(Context ctx, Object p1,
	    javax.servlet.ServletRequest p2, String p3)
	{
		super.fromWeb(ctx, p1, p2, p3);
		Object bean = ctx.get(AbstractWebControl.BEAN);
		if (bean != null)
		{
			setBillCycleDay(ctx, (BillCycleHistory) bean);
		}
	}

	@Override
	public Object fromWeb(Context ctx, javax.servlet.ServletRequest p1,
	    String p2)
	{
		Object result = super.fromWeb(ctx, p1, p2);
		Object bean = ctx.get(AbstractWebControl.BEAN);
		if (bean != null)
		{
			setBillCycleDay(ctx, (BillCycleHistory) bean);
		}
		return result;
	}

	@Override
	public void
	    toWeb(Context ctx, java.io.PrintWriter p1, String p2, Object p3)
	{
		Object bean = ctx.get(AbstractWebControl.BEAN);
		if (bean != null)
		{
			setBillCycleDay(ctx, (BillCycleHistory) bean);
		}
		super.toWeb(ctx, p1, p2, p3);
	}

	private void setBillCycleDay(Context ctx, BillCycleHistory bean)
	{
		Object billCycleIdObj = billCycleIdProperty_.get(bean);
		int billCycleId = -1;
		if (!(billCycleIdObj instanceof Number)
		    || ((Number) billCycleIdObj).intValue() < 0)
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this,
				    "Bill Cycle ID is not set; use first");
			}
			HomeSupport homeSupport = HomeSupportHelper.get(ctx);
			Object minId = null;
			try
			{
				minId =
				    homeSupport.min(ctx, BillCycleXInfo.BILL_CYCLE_ID,
				        homeSupport.getHome(ctx, BillCycle.class),
				        True.instance());
			}
			catch (HomeException e)
			{
				if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this,
					    "Cannot find any available bill cycle");
				}
			}
			if (minId instanceof Number)
			{
				billCycleIdObj = minId;
			}
			else
			{
				return;
			}
		}
		billCycleId = ((Number) billCycleIdObj).intValue();
		BillCycle bc = null;
		try
		{
			bc = BillCycleSupport.getBillCycle(ctx, billCycleId);
		}
		catch (HomeException e)
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this,
				    "Exception caught when looking up bill cycle", e);
			}
			return;
		}
		if (bc == null)
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this, "Bill Cycle ID is not set");
			}
			return;
		}

		billCycleDayProperty_.set(bean, bc.getDayOfMonth());
	}

	private final PropertyInfo billCycleIdProperty_;
	private final PropertyInfo billCycleDayProperty_;
}
