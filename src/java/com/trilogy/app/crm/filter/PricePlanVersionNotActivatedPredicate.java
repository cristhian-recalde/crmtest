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
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;

/**
 * Predicate to determine whether a price plan version has been activated. As
 * per suggested in TT#8111900049, a price plan version not yet activated should
 * be editable and/or deletable.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5 MR
 */
public class PricePlanVersionNotActivatedPredicate extends SimpleDeepClone implements Predicate
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for PricePlanVersionNotActivatedPredicate.
	 */
	public PricePlanVersionNotActivatedPredicate()
	{
		// empty
	}

	/**
	 * Returns <code>true</code> if the price plan version has been never been
	 * activated (i.e., the version after current version). In other words, this
	 * function returns <code>false</code> if the price plan version is the
	 * current or a previous version.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param obj
	 *            Price plan version.
	 * @return <code>true</code> if the price plan version has never been
	 *         activated.
	 * @throws AbortVisitException
	 *             Thrown if the operation should be terminated.
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context,
	 *      java.lang.Object)
	 */
	@Override
	public boolean f(Context ctx, Object obj) throws AbortVisitException
	{
		if (!(obj instanceof PricePlanVersion))
		{
			LogSupport.debug(ctx, this,
			    "No price plan version supplied; proceeding.");
			return false;
		}

		PricePlanVersion version = (PricePlanVersion) obj;
		PricePlan plan = null;

		try
		{
			plan = version.getPricePlan(ctx);
		}
		catch (HomeException exception)
		{
			LogSupport.info(ctx, this,
			    "Exception caught while looking up price plan for price plan "
			        + version.getId() + " version " + version.getVersion(),
			    exception);
			throw new AbortVisitException(exception);
		}

		if (plan == null)
		{
			LogSupport.info(ctx, this,
			    "Cannot find price plan " + version.getId() + " from version "
			        + version.getVersion());
			throw new AbortVisitException("Cannot find price plan "
			    + version.getId());
		}

		boolean notActivated =
		    plan.getCurrentVersion() < version.getVersion()
		    && (version.getActivation() == null || version.getActivation()
		        .getTime() == 0);

		if (LogSupport.isDebugEnabled(ctx))
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Price Plan ");
			sb.append(version.getId());
			sb.append(" version ");
			sb.append(version.getVersion());
			if (notActivated)
			{
				sb.append(" has not yet been activated.");
			}
			else
			{
				sb.append(" has already been activated.");
			}
			LogSupport.debug(ctx, this, sb.toString());
		}
		return notActivated;
	}

}
