/*
 * Created on Apr 1, 2003
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.bas.promotion.home;

import java.util.Date;

import com.trilogy.app.crm.bean.HandsetPromotionHistoryXDBHome;
import com.trilogy.app.crm.bean.HandsetPromotionHistoryXInfo;
import com.trilogy.app.crm.support.MultiDbSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * add update 
 * 
 * @author kwong
 *
 */
public class HandsetPromotionHistoryAdaptedHome extends HomeProxy
{

	public HandsetPromotionHistoryAdaptedHome(Context ctx, Home delegate)
	{
		super(delegate);
		setContext(ctx);
	}

	public void synch(Context ctx, Predicate agent, Date startDate, Date endDate, String where) throws HomeException
	{
		final String currTime = String.valueOf(System.currentTimeMillis());

		// update all history with start date, end date and current time as generation date
		// need generation date to filter "strange data" in future time, which should be manually          
		// only update whatever needed to, with outdate startdate and endate

		final String update_statement = "UPDATE " + getTableName(ctx) + " SET " + " startdate = "
			+ String.valueOf(startDate.getTime()) + ", " + " enddate = " + String.valueOf(endDate.getTime()) + ", " + " generation = "
			+ currTime + " WHERE " + where + " and generation < " + currTime + " and (startdate < "
			+ String.valueOf(startDate.getTime()) + " or enddate < " + String.valueOf(endDate.getTime()) + ")";

		if (LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this, update_statement, null).log(ctx);
		}

		XDB xdb = (XDB) ctx.get(XDB.class);

		xdb.execute(ctx, update_statement);
	}

	/**
	 * @param ctx
	 * @return
	 */
	private String getTableName(Context ctx)
	{
		// TODO: get the real sql name from the delegate (PaulSperneac)
		return HandsetPromotionHistoryXInfo.DEFAULT_TABLE_NAME;
	}

	public Object cmd(Context ctx, Object arg) throws HomeException
	{
		if (arg instanceof SyncHistoryCmd)
		{
			SyncHistoryCmd cmd = (SyncHistoryCmd) arg;
			synch(ctx, new Predicate()
			{
				public boolean f(Context _ctx, Object bean)
				{
					return true;
				}
			}, cmd.getStartDate(), cmd.getEndDate(), cmd.getWhere());
		}
		else
		{
			return super.cmd(ctx, arg);
		}
		return null;
	}

}
