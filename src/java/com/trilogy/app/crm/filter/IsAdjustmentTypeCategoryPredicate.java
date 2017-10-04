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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;

import com.trilogy.app.crm.bean.AdjustmentType;

/**
 * Predicate to determine whether an adjustment type is a user adjustment
 * type.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-12-07
 */
public class IsAdjustmentTypeCategoryPredicate implements Predicate
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Returns whether the adjustment type is a user adjustment type.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param obj
	 *            The adjustment type in question.
	 * @return Whether the adjustment type is a user adjustment type.
	 * @see com.redknee.framework.xhome.filter.Predicate#f
	 */
	@Override
	public boolean f(Context ctx, Object obj)
	{
		AdjustmentType adjustmentType = (AdjustmentType) obj;
		if (adjustmentType != null)
		{
			return (adjustmentType.isCategory());
		}
		return true;
	}

}
