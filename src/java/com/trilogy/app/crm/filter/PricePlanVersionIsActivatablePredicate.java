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

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.support.CalendarSupportHelper;

/**
 * Predicate to determine whether a price plan version is activatable. A price
 * plan version is activatable if it has not yet been activated, and the
 * activateDate is on or before today.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class PricePlanVersionIsActivatablePredicate implements Predicate
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Determines whether a price plan version is activatable. A price plan
	 * version is activatable if it has not yet been activated, and the
	 * activateDate is on or before today.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param obj
	 *            Price plan version.
	 * @return Whether the price plan version is activatable.
	 * @throws AbortVisitException
	 *             Thrown if there are errors.
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context,
	 *      java.lang.Object)
	 */
	@Override
	public boolean f(Context ctx, Object obj) throws AbortVisitException
	{
		if (!(obj instanceof PricePlanVersion))
		{
			LogSupport.debug(ctx, this,
			    "No price plan version supplied; proceeding...");
			return false;
		}

		boolean result = false;
		PricePlanVersion version = (PricePlanVersion) obj;
		Date tomorrow = CalendarSupportHelper.get(ctx).getDayAfter(new Date());
		if (!version.isActivated())
		{
			Date activateDate = version.getActivateDate();
			result = (activateDate != null && activateDate.before(tomorrow));
		}

		if (LogSupport.isDebugEnabled(ctx))
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Price Plan ");
			sb.append(version.getId());
			sb.append(" version ");
			sb.append(version.getVersion());
			if (result)
			{
				sb.append(" is activatable.");
			}
			else
			{
				sb.append(" is not activatable.");
			}
			LogSupport.debug(ctx, this, sb.toString());
		}

		return result;
	}

}
