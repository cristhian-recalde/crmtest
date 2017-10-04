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
package com.trilogy.app.crm.client;

import static com.redknee.app.crm.client.CorbaClientTrapIdDef.PRICE_PLAN_MGMT_SVC_DOWN;
import static com.redknee.app.crm.client.CorbaClientTrapIdDef.PRICE_PLAN_MGMT_SVC_UP;

import com.trilogy.app.urcs.provision.PricePlanMgmt;
import com.trilogy.app.urcs.provision.RatePlanInfo;
import com.trilogy.app.urcs.provision.RatePlanInfoSetHolder;
import com.trilogy.app.urcs.provision.RatePlanType;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * CORBA client for the PricePlanMgmt interface to URCS.
 * 
 * @author angie.li@redknee.com
 * @since CRM 8.2
 *        Support clustered corba client
 *        Refactored reusable elements to an abstract class AbstractCrmClient<T>
 * @author rchen
 * @since June 26, 2009
 */
public class PricePlanMgmtCorbaClient extends AbstractCrmClient<PricePlanMgmt>
    implements PricePlanMgmtClient
{

	/**
	 * Name of the CORBA client.
	 */
	private static final String CLIENT_NAME = "PricePlanMgmtClient";

	/**
	 * Service description.
	 */
	private static final String SERVICE_DESCRIPTION =
	    "CORBA client to update and query Price Plan/Rate Plan mapping";

	public PricePlanMgmtCorbaClient(final Context context)
	{
		super(context, CLIENT_NAME, SERVICE_DESCRIPTION, PricePlanMgmt.class,
		    PRICE_PLAN_MGMT_SVC_DOWN, PRICE_PLAN_MGMT_SVC_UP);
	}

	/**
	 * PricePlanMgmtException are exceptions that should be human readable
	 */
	@Override
	public void mapPricePlan(final int spid, final String pricePlanId,
	    final RatePlanType type, final String ratePlanId)
	    throws PricePlanMgmtException
	{
		final LogMsg pm =
		    new PMLogMsg(getModule(), "URCS_mapPricePlan",
		        "Associate (or disassociate) a Priceplan ID with a RatePlanID");
		short result = -1;
		try
		{
			final PricePlanMgmt service = getService();
			if (service == null)
			{
				final PricePlanMgmtException exception =
				    new PricePlanMgmtException(result,
				        "Cannot retrieve PricePlanMgmt CORBA service.");
				new MinorLogMsg(this, exception.getMessage(), exception)
				    .log(getContext());
				throw exception;
			}

			if (LogSupport.isDebugEnabled(getContext()))
			{
				new DebugLogMsg(this,
				    "Sending CORBA request PricePlanMgmt.mapPricePlan to URCS with parameters "
				        + toStringMapPricePlanRequest(spid, pricePlanId, type,
				            ratePlanId), null).log(getContext());
			}

			result = service.mapPricePlan(spid, pricePlanId, type, ratePlanId);

			if (result != PricePlanMgmt.CODE_SUCCESS)
			{
				StringBuilder msg = new StringBuilder();
				msg.append("URCS Price Plan/Rate Plan Mapping Request returned with this error: ");
				msg.append(PricePlanMgmtException.getVerboseResult(
				    getContext(), result));
				final PricePlanMgmtException exception =
				    new PricePlanMgmtException(result, msg.toString());
				msg.append(". Request=");
				msg.append(toStringMapPricePlanRequest(spid, pricePlanId, type,
				    ratePlanId));
				new DebugLogMsg(this, msg.toString(), null).log(getContext());
				throw exception;
			}
		}
		catch (final org.omg.CORBA.COMM_FAILURE e)
		{
			result = COMM_ERROR_RESULT_CODE;
			new MinorLogMsg(
			    this,
			    "CORBA communication with URCS PricePlanMgmt Service server failed while mapping Price Plan to Rate Plan with "
			        + toStringMapPricePlanRequest(spid, pricePlanId, type,
			            ratePlanId), e).log(getContext());
			final PricePlanMgmtException exception =
			    new PricePlanMgmtException(result,
			        "Communication failure occurred between CRM and URCS.  ");
			throw exception;
		}
		catch (final Throwable t)
		{
			result = COMMUNICATION_FAILURE;
		}
		finally
		{
			pm.log(getContext());
		}
	}

	/**
	 * This method signature can be changed. At the time of creation there
	 * wasn't a good idea of what this might be used for.
	 * For instance if we wanted to make it CORBA independent, then we should
	 * not use the CORBA Interface as return values
	 */
	@Override
	public RatePlanInfo[] queryRatePlans(final int spid,
	    final RatePlanType type, final String ratePlanIdPrefix)
	    throws PricePlanMgmtException
	{
		final LogMsg pm =
		    new PMLogMsg(getModule(), "URCS_queryRatePlans",
		        "Query the URCS server about what rateplans are currently provisioned");
		short result = NO_SERVICE_ERROR_CODE;
		RatePlanInfoSetHolder ratePlans = new RatePlanInfoSetHolder();

		final PricePlanMgmt service = getService();
		if (service == null)
		{
			final PricePlanMgmtException exception =
			    new PricePlanMgmtException(result,
			        "Cannot retrieve PricePlanMgmt CORBA service for Rate Plan query.");
			new MinorLogMsg(this, exception.getMessage(), exception)
			    .log(getContext());
			throw exception;
		}

		try
		{
			result =
			    service.queryRatePlans(spid, type, ratePlanIdPrefix, ratePlans);

			if (result != PricePlanMgmt.CODE_SUCCESS)
			{
				StringBuilder msg = new StringBuilder();
				msg.append("URCS Rate Plan Query Request returned with this error: ");
				msg.append(PricePlanMgmtException.getVerboseResult(
				    getContext(), result));
				final PricePlanMgmtException exception =
				    new PricePlanMgmtException(result, msg.toString());
				msg.append(" . Request=");
				msg.append(toStringQueryRatePlanRequest(spid, type,
				    ratePlanIdPrefix));
				new DebugLogMsg(this, msg.toString(), null).log(getContext());
				throw exception;
			}
		}
		catch (final org.omg.CORBA.COMM_FAILURE exception)
		{
			result = COMM_ERROR_RESULT_CODE;
			new MinorLogMsg(
			    this,
			    "CORBA communication with URCS PricePlanMgmt Service server failed while querying Rate Plan with request="
			        + toStringQueryRatePlanRequest(spid, type, ratePlanIdPrefix),
			    exception).log(getContext());
			throw new PricePlanMgmtException(
			    result,
			    "CORBA communication with URCS PricePlanMgmt Service server failed while querying Rate Plan.");
		}
		catch (final Throwable t)
		{
			result = COMMUNICATION_FAILURE;
			new MinorLogMsg(
			    this,
			    "CORBA communication with URCS PricePlanMgmt Service server failed while querying Rate Plan with request="
			        + toStringQueryRatePlanRequest(spid, type, ratePlanIdPrefix),
			    t).log(getContext());
			throw new PricePlanMgmtException(
			    result,
			    "CORBA communication with URCS PricePlanMgmt Service server failed while querying Rate Plan.");
		}
		finally
		{
			pm.log(getContext());
		}
		return ratePlans.value;
	}

	/**
	 * Used for Logging
	 * 
	 * @return
	 */
	private String getModule()
	{
		return this.getClass().getName();
	}

	private String toStringMapPricePlanRequest(long spid, String pricePlanId,
	    RatePlanType type, String ratePlanId)
	{
		StringBuilder msg = new StringBuilder();
		msg.append("[SPID=");
		msg.append(spid);
		msg.append(", PricePlanId=");
		msg.append(pricePlanId);
		msg.append(", RatePlanType=");
		msg.append(formatRatePlanTypeName(type));
		msg.append(", RatePlanId=");
		msg.append(ratePlanId);
		msg.append("]");
		return msg.toString();
	}

	private String toStringQueryRatePlanRequest(final int spid,
	    final RatePlanType type, final String ratePlanIdPrefix)
	{
		StringBuilder msg = new StringBuilder();
		msg.append("[SPID=");
		msg.append(spid);
		msg.append(", RatePlanType=");
		msg.append(formatRatePlanTypeName(type));
		msg.append(", ratePlanIdPrefix=");
		msg.append(ratePlanIdPrefix);
		msg.append("]");
		return msg.toString();
	}

	/**
	 * Convert Rate Plan Type to human readable text
	 * 
	 * @param type
	 * @return
	 */
	public static Object formatRatePlanTypeName(RatePlanType type)
	{
		String value;
		if (type.equals(RatePlanType.ALL))
		{
			value = type.value() + "(ALL)";
		}
		else if (type.equals(RatePlanType.VOICE))
		{
			value = type.value() + "(VOICE)";
		}
		else if (type.equals(RatePlanType.SMS))
		{
			value = type.value() + "(SMS)";
		}
		else if (type.equals(RatePlanType.DATA))
		{
			value = type.value() + "(DATA)";
		}
		else
		{
			value = String.valueOf(type.value());
		}
		return value;
	}
}
