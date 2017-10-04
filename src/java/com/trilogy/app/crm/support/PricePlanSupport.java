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
package com.trilogy.app.crm.support;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xdb.visitor.SingleValueXDBVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bas.recharge.FeeServicePair;
import com.trilogy.app.crm.bean.ChargingLevelEnum;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.PricePlanFunctionEnum;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanStateEnum;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionID;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestXDBHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestXInfo;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.log.EventRecord;
import com.trilogy.app.crm.log.PricePlanModificationEventRecord;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Provides utilities for PricePlan and PricePlanVersions.
 * 
 * @author gary.anderson@redknee.com
 */
public final class PricePlanSupport
{

	/**
	 * Creates a new <code>PricePlanSupport</code> instance. This method is made
	 * private
	 * to prevent instantiation of utility class.
	 */
	private PricePlanSupport()
	{
		// empty
	}

	/**
	 * Gets the current version of the given plan.
	 * 
	 * @param context
	 *            The operating context.
	 * @param identifier
	 *            The identifier of the plan for which to get the current
	 *            version.
	 * @return The current version of the given plan or null if no such version
	 *         exists.
	 * @exception IllegalArgumentException
	 *                Thrown if the given parameters are null.
	 * @exception HomeException
	 *                Thrown if there is a problem accessing Home data in the
	 *                context.
	 */
	public static PricePlanVersion getCurrentVersion(final Context context,
	    final long identifier) throws HomeException
	{
		Context appCtx = (Context)context.get("app");
		if(appCtx == null)
		{
			for (appCtx = context; 
				 appCtx != null && !"app".equals(appCtx.getName());
				 appCtx = (Context) appCtx.get(".."))
			{
				// NOP - for loop does everything
			}
		}
		
		Context appSubctx = appCtx.createSubContext(); //Use this SubCtx as it goes deep inside to serviceFee2 and incoming one has got sub
		
		final PricePlan plan = getPlan(appSubctx, identifier);

		if (plan != null)
		{
			return getVersion(appSubctx, plan, plan.getCurrentVersion());
		}
		
		return null;
	}

	/**
	 * Gets the current version of the given plan.
	 * 
	 * @param context
	 *            The operating context.
	 * @param plan
	 *            The plan for which to get the current version.
	 * @return The current version of the given plan or null if no such version
	 *         exists.
	 * @exception IllegalArgumentException
	 *                Thrown if the given parameters are null.
	 * @exception HomeException
	 *                Thrown if there is a problem accessing Home data in the
	 *                context.
	 */
	public static PricePlanVersion getCurrentVersion(final Context context,
	    final PricePlan plan) throws HomeException
	{
		return getVersion(context, plan, plan.getCurrentVersion());
	}

	/**
	 * Gets a specific PricePlan.
	 * 
	 * @param context
	 *            The operating context.
	 * @param identifier
	 *            The identifier of the plan to return.
	 * @return The identified PricePlan or null if no such price plan exists.
	 * @exception IllegalArgumentException
	 *                Thrown if the given parameters are null.
	 * @exception HomeException
	 *                Thrown if there is a problem accessing Home data in the
	 *                context.
	 */
	public static PricePlan
	    getPlan(final Context context, final long identifier)
	        throws HomeException
	{
		if (context == null)
		{
			throw new IllegalArgumentException("The context parameter is null.");
		}

		return HomeSupportHelper.get(context).findBean(context,
		    PricePlan.class, identifier);
	}

	/**
	 * Gets all the PricePlan for a spid.
	 * 
	 * @param context
	 *            The operating context.
	 * @param spid
	 *            The identifier of the service provider.
	 * @return List of PricePlan or null if no such price plan exists.
	 * @exception IllegalArgumentException
	 *                Thrown if the given parameters are null.
	 * @exception HomeException
	 *                Thrown if there is a problem accessing Home data in the
	 *                context.
	 */
	public static Collection getPricePlanList(final Context context,
	    final int spid) throws HomeException
	{
		if (context == null)
		{
			throw new IllegalArgumentException("The context parameter is null.");
		}

		final Home home = (Home) context.get(PricePlanHome.class);

		if (home == null)
		{
			throw new HomeException(
			    "Failed to locate PricePlanHome in the context.");
		}
		return getPricePlanList(context, home, spid);
	}

	/**
	 * Returns all price plans belonging to a service provider.
	 * 
	 * @param context
	 *            The operating context.
	 * @param home
	 *            Price plan home.
	 * @param spid
	 *            Service provider.
	 * @return A collection of all price plans belonging to a service provider.
	 * @throws HomeException
	 *             Thrown if there are problems looking up the price plans.
	 */
	public static Collection getPricePlanList(final Context context,
	    final Home home, final int spid) throws HomeException
	{
		if (context == null)
		{
			throw new IllegalArgumentException("The context parameter is null.");
		}
		final EQ condition = new EQ(PricePlanXInfo.SPID, Integer.valueOf(spid));
		return home.select(context, condition);
	}

	/**
	 * Returns all price plans of a specific type (prepaid, postpaid) and
	 * technology
	 * belonging to a service provider.
	 * 
	 * @param context
	 *            The operating context.
	 * @param spid
	 *            Service provider identifier.
	 * @param pricePlanType
	 *            Price plan type.
	 * @param technology
	 *            Technology type.
	 * @return A collection of price plans of the provided price plan type and
	 *         technology
	 *         belonging to the provided service provider.
	 * @throws HomeException
	 *             Thrown if there are problems looking up the price plans.
	 */
	public static Collection getPricePlanList(final Context context,
	    final int spid, final SubscriberTypeEnum pricePlanType,
	    final TechnologyEnum technology) throws HomeException
	{
		if (context == null)
		{
			throw new IllegalArgumentException("The context parameter is null.");
		}

		final Home home = (Home) context.get(PricePlanHome.class);

		if (home == null)
		{
			throw new HomeException(
			    "Failed to locate PricePlanHome in the context.");
		}
		return getPricePlanList(context, home, spid, pricePlanType, technology);
	}

	/**
	 * Returns all price plans of a specific type (prepaid, postpaid) and
	 * technology
	 * belonging to a service provider.
	 * 
	 * @param context
	 *            The operating context.
	 * @param home
	 *            Price plan home.
	 * @param spid
	 *            Service provider identifier.
	 * @param pricePlanType
	 *            Price plan type.
	 * @param technology
	 *            Technology type.
	 * @return A collection of price plans of the provided price plan type and
	 *         technology
	 *         belonging to the provided service provider.
	 * @throws HomeException
	 *             Thrown if there are problems looking up the price plans.
	 */
	public static Collection
	    getPricePlanList(final Context context, final Home home,
	        final int spid, final SubscriberTypeEnum pricePlanType,
	        final TechnologyEnum technology) throws HomeException
	{
		if (context == null)
		{
			throw new IllegalArgumentException("The context parameter is null.");
		}
		final And condition = new And();
		condition.add(new EQ(PricePlanXInfo.SPID, Integer.valueOf(spid)));
		condition.add(new EQ(PricePlanXInfo.PRICE_PLAN_TYPE, pricePlanType));
		condition.add(new EQ(PricePlanXInfo.TECHNOLOGY, technology));
		return home.select(context, condition);
	}

	/**
	 * Gets a version of the given plan.
	 * 
	 * @param context
	 *            The operating context.
	 * @param plan
	 *            The plan for which to get a version.
	 * @param version
	 *            The version of the plan to return.
	 * @return A version of the given plan or null if no such version exists.
	 * @exception IllegalArgumentException
	 *                Thrown if the given parameters are null.
	 * @exception HomeException
	 *                Thrown if there is a problem accessing Home data in the
	 *                context.
	 */
	public static PricePlanVersion getVersion(final Context context,
	    final PricePlan plan, final int version) throws HomeException
	{
		if (plan == null)
		{
			throw new IllegalArgumentException(
			    "The Price Plan parameter is null.");
		}

		return getVersion(context, plan.getId(), version);
	}

	/**
	 * Gets a version of the given plan.
	 * 
	 * @param context
	 *            The operating context.
	 * @param identifier
	 *            The identifier of the plan for which to get a version.
	 * @param version
	 *            The version of the plan to return.
	 * @return A version of the given plan or null if no such version exists.
	 * @exception IllegalArgumentException
	 *                Thrown if the given parameters are null.
	 * @exception HomeException
	 *                Thrown if there is a problem accessing Home data in the
	 *                context.
	 */
	public static PricePlanVersion getVersion(final Context context,
	    final long identifier, final int version) throws HomeException
	{
		if (context == null)
		{
			throw new IllegalArgumentException("The context parameter is null.");
		}

		Context appCtx = (Context)context.get("app");
		if(appCtx == null)
		{
			for (appCtx = context; 
				 appCtx != null && !"app".equals(appCtx.getName());
				 appCtx = (Context) appCtx.get(".."))
			{
				// NOP - for loop does everything
			}
		}
		
		Context appSubctx = appCtx.createSubContext(); //Use this SubCtx as it goes deep inside to serviceFee2 and incoming one has got sub
		
		final And condition = new And();
		condition.add(new EQ(PricePlanVersionXInfo.ID, identifier));
		condition.add(new EQ(PricePlanVersionXInfo.VERSION, version));

		return HomeSupportHelper.get(context).findBean(appSubctx,
		    PricePlanVersion.class, condition);
	}

	/**
	 * Determines whether or not the given plan is already being updated for new
	 * price
	 * plan version information.
	 * 
	 * @param context
	 *            The operating context.
	 * @param plan
	 *            The PricePlan on which to check.
	 * @return True if the plan is already being updated for price plan
	 *         information; false
	 *         otherwise.
	 * @exception IllegalArgumentException
	 *                Thrown if the given parameters are null.
	 * @exception HomeException
	 *                Thrown if there is a problem accessing Home data in the
	 *                context.
	 */
	public static boolean isPricePlanVersionUpdating(final Context context,
	    final PricePlan plan) throws HomeException
	{
		if (context == null)
		{
			throw new IllegalArgumentException("The context parameter is null.");
		}

		if (plan == null)
		{
			throw new IllegalArgumentException(
			    "The PricePlan parameter is null.");
		}

		return isPricePlanVersionUpdating(context, plan.getId());
	}

	/**
	 * Determines whether or not the given plan is already being updated for new
	 * price
	 * plan version information.
	 * 
	 * @param context
	 *            The operating context.
	 * @param identifier
	 *            The identifier of the PricePlan on which to check.
	 * @return True if the plan is already being updated for price plan
	 *         information; false
	 *         otherwise.
	 * @exception IllegalArgumentException
	 *                Thrown if the given parameters are null.
	 * @exception HomeException
	 *                Thrown if there is a problem accessing Home data in the
	 *                context.
	 */
	public static boolean isPricePlanVersionUpdating(final Context context,
	    final long identifier) throws HomeException
	{
		final XDB xdb = (XDB) context.get(XDB.class);

		int count = -1;

		try
		{
			final String tableName =
			    MultiDbSupportHelper.get(context).getTableName(context,
			        PricePlanVersionUpdateRequestHome.class,
			        PricePlanVersionUpdateRequestXInfo.DEFAULT_TABLE_NAME);

			final XStatement sql = new XStatement()
			{

				@Override
				public String createStatement(final Context xCtx)
				{
					return "select count(*) from " + tableName
					    + " where PricePlanIdentifier  = ?";
				}

				@Override
				public void
				    set(final Context xCtx, final XPreparedStatement ps)
				        throws SQLException
				{
					ps.setLong(identifier);
				}
			};

			count = SingleValueXDBVisitor.getInt(context, xdb, sql);
		}
		catch (final HomeException exception)
		{
			final IllegalStateException newException =
			    new IllegalStateException(
			        "Failed to determine existing priceplanversionupdaterequest of \""
			            + identifier + "\"");
			newException.initCause(exception);
			throw newException;
		}

		return count != 0;
	}

	/**
	 * Determines whether or not the given subscriber is already being updated
	 * for price
	 * plan information.
	 * 
	 * @param context
	 *            The operating context.
	 * @param subscriber
	 *            The subscriber on which to check.
	 * @return True if the subscriber is already being updated for price plan
	 *         information;
	 *         false otherwise.
	 * @exception IllegalArgumentException
	 *                Thrown if the given parameters are null.
	 * @exception HomeException
	 *                Thrown if there is a problem accessing Home data in the
	 *                context.
	 */
	public static boolean isSubscriberPricePlanUpdating(final Context context,
	    final Subscriber subscriber) throws HomeException
	{
		if (context == null)
		{
			throw new IllegalArgumentException("The context parameter is null.");
		}

		if (subscriber == null)
		{
			throw new IllegalArgumentException(
			    "The Subscriber parameter is null.");
		}

		return isSubscriberPricePlanUpdating(context, subscriber.getId());
	}

	/**
	 * Determines whether or not the given subscriber is already being updated
	 * for price
	 * plan information.
	 * 
	 * @param context
	 *            The operating context.
	 * @param identifier
	 *            The identifier of the subscriber on which to check.
	 * @return True if the subscriber is already being updated for price plan
	 *         information;
	 *         false otherwise.
	 * @exception IllegalArgumentException
	 *                Thrown if the given parameters are null.
	 * @exception HomeException
	 *                Thrown if there is a problem accessing Home data in the
	 *                context.
	 */
	public static boolean isSubscriberPricePlanUpdating(final Context context,
	    final String identifier) throws HomeException
	{
		if (context == null)
		{
			throw new IllegalArgumentException("The context parameter is null.");
		}

		if (identifier == null)
		{
			throw new IllegalArgumentException(
			    "The identifier parameter is null.");
		}

		final Home home =
		    (Home) context.get(PricePlanVersionUpdateRequestHome.class);

		if (home == null)
		{
			throw new HomeException(
			    "Failed to locate PricePlanVersionUpdateRequestHome in the context.");
		}

		final Object value = home.find(context, identifier);

		return value != null;
	}

	public static Collection getServiceFees(final Context ctx,
	    final long pricePlanId)
	{
		try
		{
			final PricePlanVersion plan =
			    PricePlanSupport.getCurrentVersion(ctx, pricePlanId);
			if (plan != null)
			{
				final Map serviceFees = plan.getServiceFees(ctx);
				if (serviceFees != null)
				{
					return serviceFees.values();
				}
			}

		}
		catch (final Exception e)
		{
			// TODO ignoring the error and returning an empty collection can
			// mask a real issue
			// TODO and lead to costly lengthy painful DEBUGING
			new DebugLogMsg(SubscriberServicesSupport.class, e.getMessage(), e)
			    .log(ctx);
		}
		// if error return empty collection, not null
		return new ArrayList();
	}

	public static Collection getAllServicesInPPV(final Context ctx,
	    final PricePlanVersion ppv)
	{

		final Collection serviceFees = new HashSet();
		serviceFees.addAll(ppv.getServicePackageVersion().getServiceFees()
		    .values());
		final Collection packages =
		    ppv.getServicePackageVersion().getPackageFees().values();
		if (packages != null)
		{
			for (final Iterator i = packages.iterator(); i.hasNext();)
			{
				final ServicePackageFee fee = (ServicePackageFee) i.next();
				try
				{
					final ServicePackageVersion spv =
					    ServicePackageSupportHelper.get(ctx).getCurrentVersion(ctx,
					        fee.getPackageId());
					if(spv!=null)
					    serviceFees.addAll(spv.getServiceFees().values());
				}
				catch (final HomeException e)
				{
					new MinorLogMsg(PricePlanSupport.class,
					    "fail to find service pacakger version for "
					        + fee.getPackageId(), e).log(ctx);
				}
			}
		}

		return serviceFees;
    }


    public static Map getBundleIds(final Context ctx, final PricePlan pp) throws HomeException
    {
        return getBundleIds(ctx, pp, false);
    }


    public static Map getBundleIds(final Context ctx, final PricePlan pp, final boolean includeOptionalBundle) throws HomeException
    {
        final Map map = new HashMap();
        final PricePlanVersion ppv = getCurrentVersion(ctx, pp);
		final Map ppv_map = ppv.getServicePackageVersion().getBundleFees();
		final Map pkg_map = ppv.getServicePackageVersion().getPackageFees();
		final Home packageHome = (Home) ctx.get(ServicePackageHome.class);

		// 1. PricePlan Bundles
		for (final Iterator i = ppv_map.values().iterator(); i.hasNext();)
		{
			final BundleFee fee = (BundleFee) i.next();
			fee.setSource("Price Plan: " + pp.getName());
            if (includeOptionalBundle || 
                    fee.getServicePreference() == ServicePreferenceEnum.MANDATORY || 
                    fee.getServicePreference() == ServicePreferenceEnum.DEFAULT)
			{
				map.put(Long.valueOf(fee.getId()), fee);
			}
		}

		// 2. Package Bundles
		for (final Iterator i = pkg_map.values().iterator(); i.hasNext();)
		{
			try
			{
				final ServicePackageFee pkg_fee = (ServicePackageFee) i.next();
				final ServicePackage pkg =
				    (ServicePackage) packageHome.find(ctx,
				        Integer.valueOf(pkg_fee.getPackageId()));
				final Map bundles_map =
				    pkg.getCurrentVersion(ctx).getBundleFees();

				final StringBuilder sb = new StringBuilder();
				sb.append("Package: ");
				sb.append(pkg.getName());
				if (pkg.getChargingLevel().equals(ChargingLevelEnum.PACKAGE))
				{
					sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Fee: ");
					final Currency currency =
					    (Currency) ctx.get(Currency.class, Currency.DEFAULT);
					sb.append(currency.formatValue(pkg.getRecurringRecharge()));
				}
				for (final Iterator i2 = bundles_map.values().iterator(); i2
				    .hasNext();)
				{
					final BundleFee fee = (BundleFee) i2.next();
					if (pkg_fee.isMandatory())
					{
						fee.setServicePreference(ServicePreferenceEnum.MANDATORY);
					}
					fee.setSource(sb.toString());
					map.put(Long.valueOf(fee.getId()), fee);
				}
			}
			catch (final Throwable t)
			{
				// new MajorLogMsg(CLASS_NAME, "Internal Error " + sub.getId(),
				// t).log(ctx);
			}
		}

		return map;
	}

	public static PricePlan findPoolPricePlan(final Context ctx,
	    final int spid, final long subscriptionTypeId, SubscriberTypeEnum type)
	    throws HomeException
	{
		final Home home = (Home) ctx.get(PricePlanHome.class);

		final And condition = new And();
		condition.add(new GTE(PricePlanXInfo.ID, POOL_PP_ID_START));
		condition.add(new EQ(PricePlanXInfo.SPID, Integer.valueOf(spid)));
		condition.add(new EQ(PricePlanXInfo.SUBSCRIPTION_TYPE, Long
		    .valueOf(subscriptionTypeId)));
		condition.add(new EQ(PricePlanXInfo.PRICE_PLAN_TYPE, type));
		condition.add(new EQ(PricePlanXInfo.PRICE_PLAN_FUNCTION,
		    PricePlanFunctionEnum.POOL));
		final PricePlan result = (PricePlan) home.find(ctx, condition);
		return result;
	}

	private static PricePlan createPoolPricePlan(final Context ctx,
	    final int spid, final long subscriptionTypeId, SubscriberTypeEnum type)
	    throws HomeException
	{
		final PricePlan poolPP =
		    FrameworkSupportHelper.get(ctx).instantiateBean(ctx,
		        PricePlan.class);
		final Home ppHome = (Home) ctx.get(PricePlanHome.class);

		poolPP.setId(getNextPoolIdentifier(ctx));
		poolPP
		    .setName("Pool PP for [" + spid + ", " + subscriptionTypeId + "]");
		poolPP.setSpid(spid);
		poolPP.setSubscriptionType(subscriptionTypeId);
		poolPP.setSubscriptionLevel(SpidSupport.getPooledSubscriptionLevel(ctx,
		    spid));
		poolPP.setPricePlanType(type);
		poolPP.setPricePlanFunction(PricePlanFunctionEnum.POOL);
		poolPP.setState(PricePlanStateEnum.ACTIVE);//Previously, the state variable was int, defaulted to 0 which meant active.
		//Now, with state type changed to ENUM, and active index being 4, priceplan needs to be explicitly set active
		ppHome.create(ctx, poolPP);

		final PricePlanVersion poolPPV =
		    FrameworkSupportHelper.get(ctx).instantiateBean(ctx,
		        PricePlanVersion.class);
		final Home ppvHome = (Home) ctx.get(PricePlanVersionHome.class);
		
		poolPPV.setId(poolPP.getId());
		poolPPV.setVersion(1);
		poolPPV.setDescription("pool ppv");
		if (SubscriberTypeEnum.POSTPAID == type)
		{
			poolPPV.setCreditLimit(Integer.MAX_VALUE);
			poolPPV.setDeposit(0);
		}
		ppvHome.create(ctx, poolPPV);
	//	poolPP.setVersions(poolPPV);
		poolPP.setCurrentVersion(1);
		ppHome.store(ctx, poolPP);
		return poolPP;
	}

	public static PricePlan getPoolPricePlan(final Context ctx, final int spid,
	    final long subscriptionTypeId, SubscriberTypeEnum type)
	    throws HomeException
	{
		PricePlan poolPP =
		    findPoolPricePlan(ctx, spid, subscriptionTypeId, type);
		if (poolPP == null)
		{
			poolPP = createPoolPricePlan(ctx, spid, subscriptionTypeId, type);
		}
		return poolPP;
	}

	/**
	 * Gets the next available identifier.
	 * 
	 * @return The next available identifier.
	 */
	private static long getNextPoolIdentifier(final Context ctx)
	    throws HomeException
	{
		IdentifierSequenceSupportHelper.get(ctx)
		    .ensureSequenceExists(ctx, IdentifierEnum.POOL_PRICE_PLAN_ID,
		        POOL_PP_ID_START, Long.MAX_VALUE);

		// TODO - 2004-08-04 - Should provide roll-over function. The defaults
		// should not require roll over for a very long time, but there is
		// nothing to prevent an admin from changing the next or end values.
		return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(ctx,
		    IdentifierEnum.POOL_PRICE_PLAN_ID, null);
	}

	/**
	 * Returns a sorted list of service fees this subscriber has.
	 * 
	 * @param sub
	 *            The subscriber being examined.
	 * @return A sorted list of {@link FeeServicePair} of the service fees this
	 *         subscriber has.
	 * @throws HomeException
	 *             Thrown if there are problems retrieving the services.
	 */
	public static
	    Collection<FeeServicePair>
	    getSubscriberServicesByExecutionOrder(Context ctx, final Subscriber sub)
	        throws HomeException
	{
		final List<FeeServicePair> sortedList = new ArrayList<FeeServicePair>();

		/*
		 * TODO 2007-04-16 Improve performance use getRawPricePlan() and remove
		 * non
		 * subscribed services
		 */
		for (ServiceFee2 fee : sub.getPricePlan(ctx).getServiceFees(ctx)
		    .values())
		{
			sortedList.add(new FeeServicePair(ctx, fee));
		}

		// FeeServicePair implements comparable, so no need for a custom
		// comparator.
		Collections.sort(sortedList);

		return sortedList;
	}

	/**
	 * Return a collection of ordered ServiceFees, ordered by execution order,
	 * from lowest execution order to highest.
	 * Services belonging to Packages are placed to the start of the sorted
	 * list.
	 * 
	 * @param ctx
	 * @param serviceFees
	 * @return
	 */
	public static Collection<ServiceFee2> getServiceByExecutionOrder(
	    final Context ctx, final Collection<ServiceFee2> serviceFees)
	    throws HomeException
	{
		final List<FeeServicePair> list = new ArrayList<FeeServicePair>();

		for (ServiceFee2 fee : serviceFees)
		{
			list.add(new FeeServicePair(ctx, fee));
		}

		return getServiceByExecutionOrder(ctx, list);
	}

	/**
	 * Return a collection of ordered ServiceFees, ordered by execution order,
	 * from lowest execution order to highest.
	 * Services belonging to Packages are placed to the start of the sorted
	 * list.
	 * 
	 * @param ctx
	 * @param serviceFees
	 * @param serviceIds
	 * @return
	 */
	public static Collection<ServiceFee2> getServiceByExecutionOrder(
	    final Context ctx, final Collection<ServiceFee2ID> serviceIds,
	    final Map<ServiceFee2ID, ServiceFee2> serviceFeeMap) throws HomeException
	{
		final List<FeeServicePair> list = new ArrayList<FeeServicePair>();

		// Add selected Service Fees to the list, unsorted
		for (final ServiceFee2ID serviceFee2ID : serviceIds)
		{
			list.add(new FeeServicePair(ctx, serviceFeeMap.get(serviceFee2ID)));
		}

		return getServiceByExecutionOrder(ctx, list);

	}

	/**
	 * Return a collection of ordered ServiceFees, ordered by execution order,
	 * from lowest execution order to highest.
	 * Services belonging to Packages are placed to the start of the sorted
	 * list.
	 * 
	 * @param ctx
	 * @param serviceFees
	 * @param serviceList
	 * @return
	 */
	public static Collection<ServiceFee2> getServiceByExecutionOrder(
	    final Context ctx, final List<FeeServicePair> serviceList)
	    throws HomeException
	{
		// FeeServicePair implements comparable, so no need for a custom
		// comparator.
		Collections.sort(serviceList);

		// Convert to a List of ServiceFees to return as a result.
		final Collection<ServiceFee2> resultList = new ArrayList<ServiceFee2>();
		StringBuilder msg =
		    new StringBuilder(
		        "The Service Fees in ascending executiong order are: ");

		for (final FeeServicePair pair : serviceList)
		{
			ServiceFee2 fee = pair.getFee();
			fee.setService(pair.getService());

			resultList.add(fee);
			msg.append(fee.getServiceId() + ", ");
		}

		if (LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, PricePlanSupport.class,
			    "getServiceByExecutionOrder::" + msg.toString());
		}

		return resultList;
	}

	public static Collection<ServiceFee2> getServiceByRefundOrder(
	    final Context ctx, Collection<Long> serviceIds,
	    Map<Long, ServiceFee2> serviceFeeMap)
	{
		List<ServiceFee2> svcFees = new ArrayList<ServiceFee2>();

		if (serviceIds != null && serviceIds.size() > 0)
		{
			for (Long svcId : serviceIds)
			{
				svcFees.add(serviceFeeMap.get(svcId));
			}

			Collections.sort(svcFees, new Comparator<ServiceFee2>()
			{
				@Override
				public int compare(ServiceFee2 fee1, ServiceFee2 fee2)
				{
					if (fee1 == fee2)
					{
						return 0;
					}
					if (fee1 == null)
					{
						return -1;
					}
					if (fee2 == null)
					{
						return 1;
					}
					if (fee1.equals(fee2))
					{
						return 0;
					}

					long lfee1 = fee1.getFee();
					long lfee2 = fee2.getFee();
					if (lfee1 >= 0 && lfee2 < 0)
					{
						return 1;
					}
					if (lfee1 < 0 && lfee2 >= 0)
					{
						return -1;
					}

					Service svc1 = null;
					Service svc2 = null;
					try
					{
						svc1 =
						    ServiceSupport.getService(ctx, fee1.getServiceId());
						svc2 =
						    ServiceSupport.getService(ctx, fee2.getServiceId());
					}
					catch (HomeException e)
					{
						if (svc1 == svc2)
						{
							return 0;
						}
						if (svc1 == null)
						{
							return -1;
						}
						else
						{
							return 1;
						}
					}

					return Service.PROVISIONING_ORDER.compare(svc1, svc2);
				}
			});
		}

		return svcFees;
	}

	public static final long POOL_PP_ID_START = 0x4000000000000000L;

	/**
	 * Finds the price plan version of a given price plan with the highest
	 * version number.
	 * 
	 * @param context
	 *            The operating context.
	 * @param plan
	 *            The plan for which to look up the most recent price plan
	 *            version.
	 * @return The price plan version with the highest version number.
	 * @throws HomeException
	 *             Thrown if there are problems accessing price plan or price
	 *             plan version home.
	 */
	public static PricePlanVersion findHighestVersion(final Context context,
	    final PricePlan plan) throws HomeException
	{
		return findVersionBefore(context, plan, Integer.MAX_VALUE);
	}

	/**
	 * Finds the price plan version of a given price plan with the highest
	 * version number prior to the given one.
	 * 
	 * @param context
	 *            The operating context.
	 * @param plan
	 *            The plan for which to look up the most recent price plan
	 *            version.
	 * @param version
	 *            The upper bound of the version number.
	 * @return The price plan version with the highest version number prior to
	 *         <code>version</code>.
	 * @throws HomeException
	 *             Thrown if there are problems accessing price plan or price
	 *             plan version home.
	 */
	public static PricePlanVersion findVersionBefore(final Context context,
	    final PricePlan plan, int version) throws HomeException
	{
        HomeSupport homeSupport = HomeSupportHelper.get(context);
        
	    And filter = new And();
	    filter.add(new EQ(PricePlanVersionXInfo.ID, plan.getId()));
	    filter.add(new LT(PricePlanVersionXInfo.VERSION, version));
	    
        Object maxVersionObj = homeSupport.max(context, PricePlanVersionXInfo.VERSION, filter);
        if (maxVersionObj instanceof Number)
        {
            Number maxVersion = (Number) maxVersionObj;
            return homeSupport.findBean(context, 
                    PricePlanVersion.class, 
                    new PricePlanVersionID(plan.getId(), maxVersion.intValue()));
        }
        
		return null;
	}

	/**
	 * Finds the price plan version of a given price plan after the given one.
	 * 
	 * @param context
	 *            The operating context.
	 * @param plan
	 *            The plan for which to look up.
	 * @param version
	 *            The lower bound of the version number.
	 * @return The price plan version after <code>version</code>.
	 * @throws HomeException
	 *             Thrown if there are problems accessing price plan or price
	 *             plan version home.
	 */
	public static PricePlanVersion findVersionAfter(final Context context,
	    final PricePlan plan, int version) throws HomeException
	{
        HomeSupport homeSupport = HomeSupportHelper.get(context);
        
        And filter = new And();
        filter.add(new EQ(PricePlanVersionXInfo.ID, plan.getId()));
        filter.add(new GT(PricePlanVersionXInfo.VERSION, version));
        
        Object minVersionObj = homeSupport.min(context, PricePlanVersionXInfo.VERSION, filter);
        if (minVersionObj instanceof Number)
        {
            Number minVersion = (Number) minVersionObj;
            return homeSupport.findBean(context, 
                    PricePlanVersion.class, 
                    new PricePlanVersionID(plan.getId(), minVersion.intValue()));
        }
        
        return null;
	}

    public static void logPricePlanModificationER(final Context ctx,
            final PricePlan plan,
            final PricePlanVersion newVersion,
            final PricePlanVersion previousVersion)
    {
        logPricePlanModificationER(ctx, plan, plan, newVersion, previousVersion);
    }
        

        /**
     * Logs an event record for price plan modification.
     * 
     * @param plan
     *            The owning price plan.
     * @param newVersion
     *            The new version of the price plan.
     * @param previousVersion
     *            The previous version of the price plan.
     */
    public static void logPricePlanModificationER(final Context ctx,
        final PricePlan oldPlan,
        final PricePlan plan,
        final PricePlanVersion newVersion,
        final PricePlanVersion previousVersion)
    {
    	final EventRecord record;
    
    	if (previousVersion != null)
    	{
    		record =
    		    new PricePlanModificationEventRecord(oldPlan, plan, previousVersion,
    		        newVersion);
    	}
    	else
    	{
    		record = new PricePlanModificationEventRecord(oldPlan, plan, newVersion);
    	}
    
    	record.generate(ctx);
    }
    
    /**
     * give back PricePlan description.
     * 
     * @param plan
     *            The used price plan.
     
     */
    public static String getStateDescription(final Context ctx,
        final PricePlan plan)
    {
        switch (plan.getState().getIndex())
        {
            case PricePlanStateEnum.ACTIVE_INDEX:
                return PricePlanStateEnum.ACTIVE.getDescription();
            case PricePlanStateEnum.GRANDFATHERED_INDEX:
                return PricePlanStateEnum.GRANDFATHERED.getDescription();
            case PricePlanStateEnum.INACTIVE_INDEX:
                return PricePlanStateEnum.INACTIVE.getDescription();
            case PricePlanStateEnum.PENDING_ACTIAVTION_INDEX:
                return PricePlanStateEnum.PENDING_ACTIAVTION.getDescription();
            default :
                return "";
        }
    }
} // class
