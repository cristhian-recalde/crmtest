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
package com.trilogy.app.crm.priceplan;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequest;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestHome;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * Provides a lifecycle agent to control the thread that checks the
 * PricePlanUpdateHome for updates to perform.
 *
 * @author victor.stratan@redknee.com
 * @author gary.anderson@redknee.com
 */
public class PricePlanVersionUpdateVisitor implements Visitor
{

    /**
     * Called by home forEach() or by ParallelVisitor's ThreadPool
     * Processes the request. Should be thread safe.
     *
     * @param ctx the Operating context
     * @param obj the request bean
     * @throws AgentException
     * @throws AbortVisitException
     */
    public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
    {
    	try
    	{
	    	final Home home = (Home) ctx.get(PricePlanVersionUpdateRequestHome.class);
	    	final PricePlanVersionUpdateRequest request = (PricePlanVersionUpdateRequest)home.find(obj);
	        processRequest(ctx, request);
    	}
    	catch(Exception e)
    	{
    		LogSupport.minor(ctx, this, "Error while Processing on requestId:" + obj,e);
    		throw new AgentException(e);
    	}
    }



    /**
     * Gets the identified subscriber.
     *
     * @param context The operating context.
     * @param identifier The identifier of the subscriber.
     * @return The subscriber if one exists; null otherwise.
     * @throws HomeException Thrown if there are problems accessing Home information in
     * the context.
     */
    private Subscriber getSubscriber(final Context context, final String identifier)
        throws HomeException
    {
        final Home home = (Home) context.get(SubscriberHome.class);
        return (Subscriber) home.find(context, identifier);
    }

    /**
     * Gets the updated Set of ServiceFee identifiers (Integer) for the given
     * subscriber for moving to the new price plan version.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to get the updated set.
     * @param newVersion The new version to which the subscriber is moving.
     * @return The updated Set of ServiceFee identifiers (Integer).
     */
    public static Set getUpdatedSubscriberServicesSet(final Context context,
            final Subscriber subscriber, final PricePlanVersion newVersion)
    {
        // TODO - 2004-10-13 - Move this method to a support class.

        final Set newServices = newVersion.getServices(context);

        final Set result = new HashSet();
        result.addAll(subscriber.getServices());

        final Map fees = newVersion.getServiceFees(context);

        for (final Iterator i = newServices.iterator(); i.hasNext();)
        {
            final Number id = (Number) i.next();
            final ServiceFee2 fee = (ServiceFee2) fees.get(id);


            // we don't need to check that the !subscriber.getServices().contains(id)
            // because the result is a set and it will take care of this case
            if (fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY)
                    || fee.getServicePreference().equals(ServicePreferenceEnum.DEFAULT))
            {
                result.add(id);
            }
        }

        for (final Iterator j = subscriber.getServices().iterator(); j.hasNext();)
        {
            final Number id = (Number) j.next();

            try
            {
                if (!newServices.contains(id))
                {
                    result.remove(id);
                }
            }
            catch (NumberFormatException e)
            {
                // it must be either the .c or .mode customize items
                // ignore it
            }
        }

        return result;
    }

    /**
     * Processes the given request.
     *
     * @param context The operating context.
     * @param request The request to process.
     */
    void processRequest(final Context context, final PricePlanVersionUpdateRequest request)
    {
        Subscriber subscriber = null;
        try
        {
            subscriber = getSubscriber(context, request.getSubscriberIdentifier());

            final PricePlan plan = PricePlanSupport.getPlan(context, request.getPricePlanIdentifier());
            final PricePlanVersion newVersion = PricePlanSupport.getCurrentVersion(context, plan);

            final Home home = (Home) context.get(SubscriberHome.class);

            processSubscriber(context, subscriber, newVersion, home);
        }
        catch (final Throwable throwable)
        {
            final String message;

            //if first attempt fail, then move the request to error table
            backUpRequestError(context, request);

            if (subscriber != null)
            {
                message = "Failed to propagate new price plan version to subscriber " + subscriber.getId();
            }
            else
            {
                message = "Failed to propagate new price plan version to subscriber.";
            }

            new MajorLogMsg(this, message, throwable).log(context);
        }
        finally
        {
            removeRequest(context, request);
        }

    }

    /**
     * Updates a single subscriber from the old price plan version to the next.
     *
     * @param context The operating context.
     * @param subscriber The subscriber to process.
     * @param newVersion The new version to which the subscriber is moving.
     * @param subscriberHome The home to which the subscriber changes should be applied.
     * @throws HomeException Thrown if there are problems accessing Home data in the context.
     */
    protected void processSubscriber(final Context context,
            final Subscriber subscriber, final PricePlanVersion newVersion,
            final Home subscriberHome) throws HomeException
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Updating subscriber " + subscriber.getId()
                    + " (" + subscriber.getMSISDN()
                    + ") with new price plan version " + newVersion.getId()
                    + "-" + newVersion.getVersion(), null).log(context);
        }

        final PMLogMsg processPM = new PMLogMsg(PM_MODULE, "processSubscriber()");
        final PMLogMsg ppvPM = new PMLogMsg(PM_MODULE, "pp " + newVersion.getId() + " v " + newVersion.getVersion());

        try
        {
            final Account account = AccountSupport.getAccount(context, subscriber.getBAN());

            if (dropSubscriber(context, account, subscriber))
            {
                // Deactivated subscribers/account should not be updated. It is
                // expected that the subscriber will be updated when
                // reactivated.
                return;
            }

            if(subscriber.getPricePlan() != newVersion.getId())
            {
            	return;
            }
            
            subscriber.switchPricePlan(context, newVersion.getId(), newVersion.getVersion());

            subscriberHome.store(context, subscriber);
        }
        catch (Exception ex)
        {
            new MajorLogMsg(this, "Unable to update price plan version (" + newVersion.getId() + "=> " + newVersion.getVersion() + 
                    " for sub " + subscriber.getId(), ex).log(context);
        }
        finally
        {
            processPM.log(context);
            ppvPM.log(context);
        }
    }

    /**
     * Check if the subscriber should be ignored.
     * 
     * @param context the operating context
     * @param account the account to check
     * @param subscriber the subscriber to check
     * @return true if subscriber is to be ignored
     */
    private boolean dropSubscriber(final Context context, final Account account, final Subscriber subscriber)
    {
        boolean result = false;
        if (isAccountOrSubscriberInvalidState(account, subscriber))
        {
                new InfoLogMsg(this, "Can not Process subscriber " + subscriber.getId()
                        + " (" + subscriber.getMSISDN()
                        + ") ", null).log(context);
                result = true;
        }
        else
        {
            new InfoLogMsg(this, "Can Process subscriber " + subscriber.getId() + " (" + subscriber.getMSISDN() + ") ",
                    null).log(context);
        }
        return result;
    }

    /**
     * Checks that the account or subscriber is inactive.
     *
     * @param account the account to check
     * @param subscriber the subscriber to check
     * @return true if account or subscriber is inactive
     */
    private boolean isAccountOrSubscriberInvalidState(final Account account, final Subscriber subscriber)
    {
        return account == null
                || account.getState() == AccountStateEnum.INACTIVE
                || subscriber.getState() == SubscriberStateEnum.INACTIVE
                || account.getState() == AccountStateEnum.SUSPENDED
                || subscriber.getState() == SubscriberStateEnum.SUSPENDED 
                || subscriber.getState() == SubscriberStateEnum.PENDING
                || subscriber.getState().equals(SubscriberStateEnum.IN_ARREARS) 
                || subscriber.getState().equals(SubscriberStateEnum.IN_COLLECTION)
                || checkPrepaidExpired(subscriber);
    }

    /**
     * Checks if it is a Prepaid Expired subscriber.
     *
     * @param subscriber the subscriber to check
     * @return true if subscriber is Prepaid and Expired
     */
    private boolean checkPrepaidExpired(final Subscriber subscriber)
    {
        return subscriber.getSubscriberType() == SubscriberTypeEnum.PREPAID
                && (subscriber.getState() == SubscriberStateEnum.EXPIRED
                || subscriber.getStateWithExpired() == SubscriberStateEnum.EXPIRED
                || subscriber.getState() == SubscriberStateEnum.SUSPENDED);
    }

    /**
     * Removes a request from the request home.
     *
     * @param context The operating context.
     * @param request The request to remove.
     * @throws HomeException Thrown if there is a problem accessing the Home data in
     * the context.
     */
    private void removeRequest(final Context context, final PricePlanVersionUpdateRequest request)
    {
        final Home home = (Home) context.get(PricePlanVersionUpdateRequestHome.class);
        try
        {
            home.remove(context, request);
        }
        catch (final Throwable t)
        {
            new MinorLogMsg(this, t.getMessage(), t).log(context);
        }
    }

    private void backUpRequestError(final Context context, final PricePlanVersionUpdateRequest request)
    {
        final Home home = (Home) context.get(Common.PRICE_PLAN_VERSION_UPDATE_REQUEST_ERROR_HOME);
        try
        {
            home.create(context, request);
        }
        catch (final Throwable t)
        {
            new MinorLogMsg(this, "Unable to Backup the PricePlanVersion Update request.", t).log(context);
        }
    }

    /**
     * Used to identify this class's PMs.
     */
    private static final String PM_MODULE = PricePlanVersionUpdateVisitor.class.getName();

} // class
