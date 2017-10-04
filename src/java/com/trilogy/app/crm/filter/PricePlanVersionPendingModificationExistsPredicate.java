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

import com.trilogy.app.crm.bean.PPVModificationRequest;
import com.trilogy.app.crm.bean.PPVModificationRequestStateEnum;
import com.trilogy.app.crm.bean.PPVModificationRequestXInfo;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Predicate to determine whether a price plan version pending modification exists.
 * 
 * @author Marcio Marques
 * @since 9.2
 */
public class PricePlanVersionPendingModificationExistsPredicate implements Predicate
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for PricePlanVersionNotActivatedPredicate.
	 */
	public PricePlanVersionPendingModificationExistsPredicate()
	{
		// empty
	}

	/**
	 * Returns <code>true</code> if the price plan version pending modification exists.
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
		PPVModificationRequest request = null;

		try
		{
			And filter = new And();
			filter.add(new EQ(PPVModificationRequestXInfo.PRICE_PLAN_IDENTIFIER, Long.valueOf(version.getId())));
			filter.add(new EQ(PPVModificationRequestXInfo.PRICE_PLAN_VERSION, Integer.valueOf(version.getVersion())));
			filter.add(new EQ(PPVModificationRequestXInfo.STATUS, Integer.valueOf(PPVModificationRequestStateEnum.PENDING_INDEX)));
			request = HomeSupportHelper.get(ctx).findBean(ctx, PPVModificationRequest.class, filter);
		}
		catch (HomeException exception)
		{
			LogSupport.info(ctx, this,
			    "Exception caught while looking up price plan version modification request for price plan "
			        + version.getId() + " version " + version.getVersion(),
			    exception);
			throw new AbortVisitException(exception);
		}

		boolean result = false;
		
		if (request != null)
		{
			result = true;
		}

		if (LogSupport.isDebugEnabled(ctx))
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Pending Price Plan Modification Request for price plan ");
			sb.append(version.getId());
			sb.append(" version ");
			sb.append(version.getVersion());
			if (result)
			{
				sb.append(" exists.");
			}
			else
			{
				sb.append(" does not exists.");
			}
			LogSupport.debug(ctx, this, sb.toString());
		}
		
		return result;
	}

}
