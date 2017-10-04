/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.priceplan.rateplan;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xhome.visitor.PredicateVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.urcs.ParamUtil;
import com.trilogy.app.urcs.param.Parameter;
import com.trilogy.app.urcs.param.ParameterSetHolder;
import com.trilogy.app.urcs.provision.PricePlanMgmt;
import com.trilogy.app.urcs.provision.PricePlanMgmtParamID;
import com.trilogy.app.urcs.provision.RatePlanInfo;
import com.trilogy.app.urcs.provision.RatePlanType;

import com.trilogy.app.crm.bean.priceplan.RatePlan;
import com.trilogy.app.crm.bean.priceplan.RatePlanTypeEnum;
import com.trilogy.app.crm.bean.priceplan.RatePlanXInfo;
import com.trilogy.app.crm.client.PricePlanMgmtClientV2;
import com.trilogy.app.crm.client.PricePlanMgmtException;

/**
 * Use to retrieve RatePlans from URCS. This home provides the ability to
 * filter based on the RatePlanType if specified in the constructor. Note that
 * the requesting context must contain either a SpidAware bean (for bean-level
 * GUI requests)
 * or a User/Principal so that we can determine the SPID to use in the request
 * to URCS.
 * 
 * @author rpatel
 */
public class RatePlanCorbaHome extends HomeProxy
{
	private final RatePlanTypeEnum ratePlanType_;

	public RatePlanCorbaHome(Context ctx)
	{
		this(ctx, RatePlanTypeEnum.ALL);
	}

	public RatePlanCorbaHome(Context ctx, RatePlanTypeEnum ratePlanType)
	{
		super(ctx);
		ratePlanType_ = ratePlanType;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.redknee.framework.xhome.home.HomeProxy#find(com.redknee.framework
	 * .xhome.context.Context, java.lang.Object)
	 */
	@Override
	public Object find(Context ctx, Object obj) throws HomeException,
	    HomeInternalException
	{
		try
		{
			// get all the RatePlans
			Collection col = select(ctx, True.instance());

			// find the ones that have a RatePlanId that matches the criteria
			ListBuildingVisitor result = new ListBuildingVisitor();
			Visitors.forEach(ctx, col, new PredicateVisitor(new EQ(
			    RatePlanXInfo.RATE_PLAN_ID, obj), result));

			// if there isn't any then return null, otherwise return the first
			// one (should only be one if the constraint is kept)
			return result.size() != 0 ? result.iterator().next() : null;
		}
		catch (AgentException e)
		{
			new DebugLogMsg(
			    this,
			    "Encountered a AgentException while trying to visit the rateplans for [ratePlanType="
			        + ratePlanType_.getDescription()
			        + "] for find() with [key="
			        + obj
			        + "]. Will throw HomeException on find().", e).log(ctx);
			throw new HomeException(
			    "Encountered a AgenetException while trying to perform find() with [key="
			        + obj + "] for [ratePlanType="
			        + ratePlanType_.getDescription() + "]", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.redknee.framework.xhome.home.HomeProxy#select(com.redknee.framework
	 * .xhome.context.Context, java.lang.Object)
	 */
	@Override
	public Collection select(Context ctx, Object obj) throws HomeException,
	    HomeInternalException
	{
		PricePlanMgmtClientV2 client = getClient(ctx);
		Collection col = new ArrayList();

		// first try and get the SPID from the SpidAware bean being operated on
		// (eg. PricePlan)
		Spid spid = MSP.getBeanSpid(ctx);
		if (spid == null)
		{
			// if there is no bean in the context then use the user's SPID
			new DebugLogMsg(
			    this,
			    "Unable to retrieve SPID from bean.  Will attempt retrieve SPID from user/principal...",
			    null).log(ctx);
			spid = MSP.getSpid(ctx);
			if (spid == null)
			{
				new DebugLogMsg(
				    this,
				    "Neither user or bean is spidAware and thus we cannot determine spid to send to URCS for RatePlan retrieval.  Failing the home.select() request with a HomeException.",
				    null).log(ctx);
				throw new HomeException(
				    "Unable to determine SPID from bean or user and as a result, unable to retrieve RatePlans from URCS.");
			}
		}

		PMLogMsg pm =
		    new PMLogMsg(RatePlanCorbaHome.class.getName(), "queryRatePlans");
		try
		{
			final Parameter[] inParams = new Parameter[]{};
			final ParameterSetHolder outParams = new ParameterSetHolder();
			// retrieve all the RatePlans that match our criteria
			RatePlanInfo[] ratePlanInfos =
			    client.queryRatePlans(ctx, spid.getId(),
			        RatePlanType.from_int(ratePlanType_.getIndex()), "", inParams, outParams);

			for (int i = 0; i < ratePlanInfos.length; i++)
			{
				// adapt and add them to our collection
				col.add(adapt(ratePlanInfos[i]));
			}

			return col;
		}
		catch (PricePlanMgmtException e)
		{
			String msg = null;
			switch (e.getResultCode())
			{
				case PricePlanMgmt.NO_RATEPLAN_FOUND:
				{
					msg =
					    "No rateplans found for [spid=" + spid.getId()
					        + ", ratePlanType="
					        + ratePlanType_.getDescription() + "].";
					new DebugLogMsg(this, msg, e).log(ctx);
					break;
				}
				case PricePlanMgmtClientV2.NO_SERVICE_ERROR_CODE:
				{
					msg =
					    "Rate plan service is unavailable at the moment.  Please check the connection and configurations if problem persists.";
					new MinorLogMsg(this, msg, e).log(ctx);
					break;
				}
				case PricePlanMgmt.ILLEGAL_RATEPLAN_TYPE:
				{
					msg =
					    "Rate plan type=" + ratePlanType_.getIndex()
					        + " is not understood by URCS.";
					new MinorLogMsg(this, msg, e).log(ctx);
					break;
				}
				case PricePlanMgmt.INTERNAL_ERROR:
				{
					msg =
					    "URCS internal error encountered while looking up rate plans for [spid="
					        + spid.getId()
					        + ", ratePlanType="
					        + ratePlanType_.getDescription()
					        + "].  Please check URCS configuration and version compatibility.";
					new MinorLogMsg(this, msg, e).log(ctx);
					break;
				}
				case PricePlanMgmtClientV2.COMM_ERROR_RESULT_CODE:
				{
					msg =
					    "Communication error encountered while looking up rate plans for [spid="
					        + spid.getId()
					        + ", ratePlanType="
					        + ratePlanType_.getDescription()
					        + "].  Please check connection configuration and URCS version compatbility.";
					new MinorLogMsg(this, msg, e).log(ctx);
					break;
				}
				default:
				{
					msg =
					    "Encountered a unexpected PricePlanMgmtException ["
					        + e.toString()
					        + "] while trying to call queryRatePlans for [spid="
					        + spid.getId() + ", ratePlanType="
					        + ratePlanType_.getDescription() + "]";
					new MinorLogMsg(this, msg, e).log(ctx);
					throw new HomeException(
					    "Encountered a unexpected exception while trying to call queryRatePlans for [spid="
					        + spid.getId()
					        + ", ratePlanType="
					        + ratePlanType_.getDescription() + "]", e);
				}
			}

			ExceptionListener el =
			    (ExceptionListener) ctx.get(HTMLExceptionListener.class);
			if (el == null)
			{
				el = (ExceptionListener) ctx.get(ExceptionListener.class);
			}

			if (msg != null && el != null)
			{
				PropertyInfo property =
				    (PropertyInfo) ctx.get(AbstractWebControl.PROPERTY);

				el.thrown(new IllegalPropertyArgumentException(property, msg));

			}
			return col;
		}
		finally
		{
			pm.log(ctx);
		}
	}

	private RatePlan adapt(RatePlanInfo ratePlanInfo)
	{
		RatePlan ratePlan = new RatePlan();
		ratePlan.setDescription(
				(ratePlanInfo.ratePlanName.length() > RatePlan.DESCRIPTION_WIDTH)?
						(ratePlanInfo.ratePlanName.substring(0, RatePlan.DESCRIPTION_WIDTH)):(ratePlanInfo.ratePlanName));
		ratePlan.setRatePlanId(ratePlanInfo.ratePlanId);
		ratePlan.setRatePlanType(RatePlanTypeEnum.get((short) ratePlanInfo.type
		    .value()));
		ratePlan.setSpid(ratePlanInfo.spid);
		return ratePlan;
	}

	/**
	 * Retrieve the URCS PricePlanMgmtClient
	 * 
	 * @param ctx
	 * @return
	 * @throws HomeException
	 */
	private PricePlanMgmtClientV2 getClient(final Context ctx)
	    throws HomeException
	{
		final PricePlanMgmtClientV2 client =
		    (PricePlanMgmtClientV2) ctx.get(PricePlanMgmtClientV2.class);
		if (client == null)
		{
			HomeException exception =
			    new HomeException(
			        "URCS PricePlanMgmtClientV2 is not found in context.");
			new MajorLogMsg(this, exception.getMessage(), exception).log(ctx);
			throw exception;
		}
		return client;
	}
}
