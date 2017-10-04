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
package com.trilogy.app.crm.home;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.log.PricePlanVersionDeletionEventRecord;
import com.trilogy.app.crm.priceplan.task.PricePlanVersionModificationLifecycleAgent;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;

/**
 * Provides a decorator for the PricePlanVersionHome that updates the owning
 * PricePlan's "NextVersion" property automatically whenever a new version is
 * created.
 * 
 * @author gary.anderson@redknee.com
 * @author cindy.wong@redknee.com
 */
public class PricePlanVersionHomeProxy extends HomeProxy
{
	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new PricePlanVersionHomeProxy for the given delegate.
	 * 
	 * @param ctx
	 * @param delegate
	 *            The home to which this decorator delegates.
	 */
	public PricePlanVersionHomeProxy(final Context ctx, final Home delegate)
	{
		super(ctx, delegate);
	}

	/**
	 * Creates the new version and updated the "NextVersion" property of the
	 * owning price plan.
	 */
	@Override
	public Object create(final Context ctx, final Object obj)
	    throws HomeException
	{
		Common.OM_PRICE_PLAN_VERSION_MODIFICATION.attempt(ctx);
		final PricePlanVersion proposedVersion = (PricePlanVersion) obj;
		final boolean isNewPricePlan = (proposedVersion.getVersion() == 0);
		try
		{
			final PricePlan plan = getPricePlanForVersion(ctx, proposedVersion);
			final PricePlanVersion previousVersion =
			    PricePlanSupport.findHighestVersion(ctx, plan);

            if (previousVersion != null && (!previousVersion.isActivated()))
            {
                throw new HomeException(
                        "Cannot create a new version, unless the previous version is activated or deleted without activation. Otherwise, edit the previous one.");
            }
            else if (previousVersion == null && proposedVersion.getVersion() > 1)
            {
                throw new HomeException("Cannot create a new version, unless previous version activated or deleted.");
            }	
            
            Map packages=proposedVersion.getServicePackageVersion().getPackageFees();
    

               if(packages!=null && packages.size()>0)
               {
                   for(Iterator i=packages.keySet().iterator();i.hasNext();)
                   {
                       try
                       {
                           ServicePackageFee fee=(ServicePackageFee) packages.get(i.next());
                           
                           ServicePackage servicePackage=HomeSupportHelper.get(ctx).findBean(ctx, ServicePackage.class, Integer.valueOf(fee.getPackageId()));
                          // ServicePackage p=(ServicePackage) home.find(ctx, Integer.valueOf(fee.getPackageId()));
                           if(servicePackage!=null)
                           {
                               servicePackage.updateTotalCharge(ctx);
                               fee.setFee(servicePackage.getTotalCharge());
                           }
                           else
                           {
                               if(LogSupport.isDebugEnabled(ctx))
                               {
                                   new DebugLogMsg(this,"Cannot find package with id: "+fee.getPackageId(),null).log(ctx);
                               }
                           }
                       }
                       catch(HomeException e)
                       {
                           if(LogSupport.isDebugEnabled(ctx))
                           {
                               new DebugLogMsg(this,e.getMessage(),e).log(ctx);
                           }
                           LogSupport.minor(ctx, this, "Unable to retrieve Service Package : " + e.getMessage(), e);
                       }
                   }
               }
			
			// verify activate date
			assertActivateDate(ctx, null, proposedVersion);

			// claim the version number first
			claimNextVersionIdentifier(ctx, plan, proposedVersion);
            
			if (isNewPricePlan)
            {
                new OMLogMsg(Common.OM_MODULE,
                    Common.OM_PRICE_PLAN_CREATION_ATTEMPT).log(ctx);
            }

			/*
			 * Update the created date to make sure it's as close to the
			 * creation as
			 * possible.
			 */
			proposedVersion.setCreatedDate(new Date());

			final PricePlanVersion newVersion =
			    (PricePlanVersion) super.create(ctx, proposedVersion);

			if (isNewPricePlan)
			{
                ERLogger.createPricePlanEr(ctx, plan, proposedVersion);
                
				new OMLogMsg(Common.OM_MODULE,
				    Common.OM_PRICE_PLAN_CREATION_SUCCESS).log(ctx);
			}

            PricePlanSupport.logPricePlanModificationER(ctx, plan, newVersion,
                previousVersion);

			Common.OM_PRICE_PLAN_VERSION_MODIFICATION.success(ctx);
			return newVersion;
		}
		catch (final HomeException exception)
		{
			if (isNewPricePlan)
			{
				new OMLogMsg(Common.OM_MODULE,
				    Common.OM_PRICE_PLAN_CREATION_FAIL).log(ctx);
			}
			Common.OM_PRICE_PLAN_VERSION_MODIFICATION.failure(ctx);
			throw exception;
		}
	}

	/**
	 * Only unactivated versions can be removed.
	 */
	@Override
	public void remove(final Context ctx, final Object obj)
	    throws HomeException
	{
		try
		{
			Common.OM_PRICE_PLAN_VERSION_DELETION.attempt(ctx);

			final PricePlanVersion version = (PricePlanVersion) obj;
			final PricePlan plan = getPricePlanForVersion(ctx, version);

			if (plan.getCurrentVersion() >= version.getVersion())
			{
				throw new HomeException(
				    "Cannot remove an active, or previously activated version.");
			}

			rollback(ctx, plan, version);

			new PricePlanVersionDeletionEventRecord(plan, version, 0)
			    .generate(ctx);
			Common.OM_PRICE_PLAN_VERSION_DELETION.success(ctx);
		}
		catch (final HomeException exception)
		{
			Common.OM_PRICE_PLAN_VERSION_DELETION.failure(ctx);
			throw exception;
		}
	}

	/**
	 * PricePlanVersion can now be updated.
	 */
	@Override
	public Object store(final Context ctx, final Object obj)
	    throws HomeException
	{
		Common.OM_PRICE_PLAN_VERSION_MODIFICATION.attempt(ctx);
		final Home home = (Home) ctx.get(PricePlanVersionHome.class);
		final PricePlanVersion oldVersion =
		    (PricePlanVersion) home.find(ctx, obj);

		if (LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this, "Attempt to update version from "
			    + oldVersion + " to " + obj, null).log(ctx);
		}

		PricePlanVersion newVersion;
		
		// Validation is only done if it's not the price plan version modification agent that is trying to update the version.
		if (!ctx.getBoolean(PricePlanVersionModificationLifecycleAgent.PRICE_PLAN_VERSION_MODIFICATION_AGENT, Boolean.FALSE))
		{
    		if (isActivated(oldVersion))
    		{
    			throw new HomeException(
    			    "Versions may not be changed once activated.");
    		}
    
    		newVersion = (PricePlanVersion) obj;
    		final PricePlan plan = getPricePlanForVersion(ctx, newVersion);
    		final PricePlanVersion previousVersion =
    		    PricePlanSupport.findVersionBefore(ctx, plan,
    		        newVersion.getVersion());
    		final PricePlanVersion nextVersion =
    		    PricePlanSupport.findVersionAfter(ctx, plan,
    		        newVersion.getVersion());
    
    		// verify activate date
    		assertActivateDate(ctx, oldVersion, newVersion);
    
    		if (nextVersion != null)
    		{
    			assertActivateDateOrder(ctx, newVersion, nextVersion);
    		}
		}

		newVersion = (PricePlanVersion) super.store(ctx, obj);

		PricePlanSupport.logPricePlanModificationER(ctx,
		    newVersion.getPricePlan(ctx), newVersion, oldVersion);

		return newVersion;
	}

	/**
	 * Returns whether a price plan version has been activated.
	 * 
	 * @param version
	 *            Price plan version to verify.
	 * @return Whether the price plan version has been activated.
	 */
	private boolean isActivated(final PricePlanVersion version)
	{
		if (version != null)
		{
			final Date activation = version.getActivation();
			return activation != null && activation.getTime() != 0;
		}
		return false;
	}

	/**
	 * Verifies the activate date of an older price plan version is before that
	 * of a newer price plan version.
	 * 
	 * @param context
	 *            The operating context.
	 * @param olderVersion
	 *            Older price plan version.
	 * @param newerVersion
	 *            Newer price plan version.
	 * @throws HomeException
	 *             Thrown if the activate dates do not follow version order.
	 */
	private void
	    assertActivateDateOrder(final Context context,
	        final PricePlanVersion olderVersion,
	        final PricePlanVersion newerVersion) throws HomeException
	{
		if (olderVersion != null && newerVersion != null)
		{
			if (olderVersion.getVersion() > newerVersion.getVersion())
			{
				assertActivateDateOrder(context, newerVersion, olderVersion);
			}
			else
			{
				final Date olderDate = olderVersion.getActivateDate();
				final Date earliestPossibleDate =
				    CalendarSupportHelper.get(context).getDayAfter(olderDate);
				final Date newerDate = newerVersion.getActivateDate();
				if (newerDate.before(earliestPossibleDate))
				{
					throw new HomeException("The activate date of price plan "
					    + olderVersion.getId()
					    + " version "
					    + newerVersion.getVersion()
					    + " ("
					    + CalendarSupportHelper.get(context).formatDate(
					        context, newerDate)
					    + ") must be later than that of version "
					    + olderVersion.getVersion()
					    + " ("
					    + CalendarSupportHelper.get(context).formatDate(
					        context, olderDate));
				}
			}
		}
	}

	/**
	 * Asserts that the activate date of a price plan version is on or after
	 * today.
	 * 
	 * @param context
	 *            The operating context.
	 * @param newVersion
	 *            Price plan version.
	 * @throws HomeException
	 *             Thrown if the activate date of the price plan version is set
	 *             in the past.
	 */
	private void assertActivateDate(final Context context,
	    final PricePlanVersion oldVersion, final PricePlanVersion newVersion)
	    throws HomeException
	{
		if (newVersion == null)
		{
			throw new HomeException("No price plan version provided");
		}
		final Date activateDate = newVersion.getActivateDate();
		if (activateDate == null)
		{
			throw new HomeException(
			    "Activate date is not set in price plan version");
		}
		final Date today =
		    CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(
		        new Date());

		if (isActivation(oldVersion, newVersion))
		{
			if (activateDate.after(today))
			{
				throw new HomeException(
				    "Cannot activate a price plan version with activateDate "
				        + CalendarSupportHelper.get(context).formatDate(
				            context, activateDate) + " in the future");
			}
		}
		else if (activateDate.before(today))
		{
			throw new HomeException("The activate date, \""
			    + CalendarSupportHelper.get(context).formatDate(context,
			        activateDate) + "\", may not be in the past or null.");
		}

	}

	/**
	 * Determines whether this is an activation.
	 * 
	 * @param oldVersion
	 *            Old price plan version.
	 * @param newVersion
	 *            New price plan version.
	 * @return Whether this is an activation.
	 */
	private boolean isActivation(PricePlanVersion oldVersion,
	    PricePlanVersion newVersion)
	{
		if (oldVersion != null
		    && (oldVersion.getActivation() == null || oldVersion
		        .getActivation().getTime() == 0))
		{
			return (newVersion.getActivation() != null && newVersion
			    .getActivation().getTime() != 0);
		}
		return false;
	}

	/**
	 * Claims the next version number, and updates the plan accordingly. The
	 * version number is set in the given proposed version.
	 * 
	 * @param plan
	 *            The PricePlan to which the version belongs.
	 * @param proposedVersion
	 *            The proposed new version of the plan.
	 * @exception HomeException
	 *                Thrown if there are problems accessing Home data
	 *                in the context.
	 */
	private void claimNextVersionIdentifier(final Context ctx,
	    final PricePlan plan, final PricePlanVersion proposedVersion)
	    throws HomeException
	{
		final int version = plan.getNextVersion();

		plan.setNextVersion(version + 1);
		HomeSupportHelper.get(ctx).storeBean(ctx, plan);

		// Explicitly override whatever version might have been set -- the
		// proposed version could not know for sure what it is to be.
		proposedVersion.setVersion(version);
	}

	/**
	 * Gets the PricePlan for a given PricePlanVersion.
	 * 
	 * @param version
	 *            The PricePlanVersion for which to get the PricePlan.
	 * @return The PricePlan.
	 * @exception HomeException
	 *                Thrown if the PricePlan cannot be found in the
	 *                context.
	 */
	private PricePlan getPricePlanForVersion(final Context ctx,
	    final PricePlanVersion version) throws HomeException
	{
		final PricePlan plan = version.getPricePlan(ctx);

		if (plan == null)
		{
			throw new HomeException("Invalid Price Plan ID " + version.getId());
		}

		return plan;
	}

	/**
	 * Used in create() to rollback a version that failed to notify ABM, and
	 * also from remove() as a normal part of removind an unwanted version.
	 * 
	 * @param plan
	 *            The price plan to which the version belongs.
	 * @param badVersion
	 *            The version to roll-back (undo).
	 * @exception HomeException
	 *                Thrown if there are problems encounterred
	 *                accessing Home data in the context.
	 */
	private void rollback(final Context ctx, final PricePlan plan,
	    final PricePlanVersion badVersion) throws HomeException
	{
		super.remove(ctx, badVersion);
	}
} // class
