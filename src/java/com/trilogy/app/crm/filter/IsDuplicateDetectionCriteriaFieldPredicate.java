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

import javax.servlet.http.HttpServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

import com.trilogy.app.crm.support.DuplicateAccountDetectionSupport;

/**
 * Determines if the provided field name is that of a duplicate check criteria
 * field.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-10-05
 */
public class IsDuplicateDetectionCriteriaFieldPredicate implements Predicate
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static IsDuplicateDetectionCriteriaFieldPredicate instance =
	    new IsDuplicateDetectionCriteriaFieldPredicate();

	public static IsDuplicateDetectionCriteriaFieldPredicate instance()
	{
		return instance;
	}

	/**
	 * Determines if the provided field name is that of a duplicate check
	 * criteria field.
	 * 
	 * @param ctx
	 *            Operating context.
	 * @param obj
	 *            Field name.
	 * @return Whether the provided field name is that of a duplicate check
	 *         criteria field.
	 * @throws AbortVisitException
	 * @see com.redknee.framework.xhome.filter.Predicate#f
	 */
	@Override
	public boolean f(Context ctx, Object obj) throws AbortVisitException
	{
		boolean result = false;

		HttpServletRequest req =
		    (HttpServletRequest) ctx.get(HttpServletRequest.class);
		if (req != null)
		{
			String value =
			    req.getParameter(DuplicateAccountDetectionSupport
			        .getCriteriaCheckKey(obj.toString()));
			result = (value != null && !value.isEmpty());
		}
		return result;
	}

}
