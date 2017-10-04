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
package com.trilogy.app.crm.web.border;

import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.search.SearchBorder;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.BillCycleHistorySearch;
import com.trilogy.app.crm.bean.BillCycleHistorySearchWebControl;
import com.trilogy.app.crm.bean.BillCycleHistoryXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;

/**
 * @author cindy.wong@redknee.com
 * @since 9.1
 */
public class BillCycleHistorySearchBorder extends SearchBorder
{

	/**
	 * Constructor for BillCycleHistorySearchBorder.
	 * @param ctx
	 */
	public BillCycleHistorySearchBorder(Context ctx)
	{
		super(ctx, BillCycleHistory.class,
		    new BillCycleHistorySearchWebControl());
		addAgent(new ContextAgentProxy()
		{
			@Override
			public void execute(final Context ctx) throws AgentException
			{
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
				final BillCycleHistorySearch criteria =
				    (BillCycleHistorySearch) getCriteria(ctx);

				if (acct != null)
				{
					criteria.setBAN(acct.getBAN());
				}
				doSelect(ctx,
				    new EQ(BillCycleHistoryXInfo.BAN, criteria.getBAN()));
				delegate(ctx);
			}
		});

		addAgent(new ContextAgentProxy()
		{
			@Override
			public void execute(final Context ctx) throws AgentException
			{
				final BillCycleHistorySearch criteria =
				    (BillCycleHistorySearch) getCriteria(ctx);
				if (criteria.getStartDate() != null)
				{
					Date startDate =
					    CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
					        criteria.getStartDate());
					doSelect(ctx,
					    new GTE(BillCycleHistoryXInfo.BILL_CYCLE_CHANGE_DATE,
					        startDate));
				}
				delegate(ctx);
			}
		});

		addAgent(new ContextAgentProxy()
		{
			@Override
			public void execute(final Context ctx) throws AgentException
			{
				final BillCycleHistorySearch criteria =
				    (BillCycleHistorySearch) getCriteria(ctx);
				if (criteria.getEndDate() != null)
				{
					Date endDate =
					    CalendarSupportHelper.get(ctx)
					        .getDateWithLastSecondofDay(criteria.getEndDate());
					doSelect(ctx, new LTE(
					    BillCycleHistoryXInfo.BILL_CYCLE_CHANGE_DATE, endDate));
				}
				delegate(ctx);
			}
		});
	}
}
