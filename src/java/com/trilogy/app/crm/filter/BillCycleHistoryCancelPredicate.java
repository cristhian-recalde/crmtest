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
package com.trilogy.app.crm.filter;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.BillCycleHistoryXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * @author cindy.wong@redknee.com
 * @since 9.1
 */
public class BillCycleHistoryCancelPredicate implements
    Predicate
{

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean f(Context ctx, Object obj) throws AbortVisitException
	{
		boolean result = false;
		if (obj instanceof BillCycleHistory)
		{
			BillCycleHistory bean = (BillCycleHistory) obj;
			if (SafetyUtil.safeEquals(BillCycleChangeStatusEnum.PENDING,
			    bean.getStatus()))
			{
				// check if it's the latest pending for this account
				And predicate = new And();
				predicate.add(new EQ(BillCycleHistoryXInfo.BAN, bean.getBAN()));
				predicate.add(new GTE(
				    BillCycleHistoryXInfo.BILL_CYCLE_CHANGE_DATE, bean
				        .getBillCycleChangeDate()));
				long newer = -1;
				try
				{
					newer =
					    HomeSupportHelper.get(ctx).getBeanCount(ctx,
					        BillCycleHistory.class, predicate);
				}
				catch (HomeException e)
				{
					if (LogSupport.isDebugEnabled(ctx))
					{
						LogSupport
						    .debug(
						        ctx,
						        this,
						        "Exception caught while retrieving bill cycle history",
						        e);
					}
				}
				result = newer == 0;
			}
		}
		return result;
	}

}
