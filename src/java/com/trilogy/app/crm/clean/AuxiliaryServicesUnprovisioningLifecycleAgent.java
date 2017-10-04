/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.clean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.SubscriberAuxiliaryServiceCharger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.FunctionVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.snippet.log.Logger;


public class AuxiliaryServicesUnprovisioningLifecycleAgent extends LifecycleAgentScheduledTask
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a AuxiliaryServicesUnprovisioningLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public AuxiliaryServicesUnprovisioningLifecycleAgent(Context ctx, final String agentId) throws AgentException
    {
        super(ctx, agentId);
    }
    
    protected void start(Context ctx) throws LifecycleException
    {
        final Date runningDate = getRunningDate(ctx);

        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Unprovisioning Auxiliary Services for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
            sb.append("'.");
            LogSupport.debug(ctx, this, sb.toString());
        }
        
        Collection<SubscriberAuxiliaryService> allEntry = null;
        Subscriber subscriber = null;

        Context subContext = ctx.createSubContext();

        final Home subAuxServiceHome = (Home) subContext.get(SubscriberAuxiliaryServiceHome.class);
        final Home subscriberHome = (Home) subContext.get(SubscriberHome.class);
        final Home auxServiceHome = (Home) subContext.get(AuxiliaryServiceHome.class);

        subContext.put(CommonTime.RUNNING_DATE, runningDate);
        // ctx.put(CronConstants.FROM_UNPROV_CRON_AGENT_CTX_KEY, Boolean.TRUE);

        allEntry = getAllAssociationsToBeUnprovisioned(subContext, subAuxServiceHome, runningDate);

        if (allEntry == null || allEntry.size() == 0)
        {
            new InfoLogMsg(this, "Auxiliary Services Unprovisioning task didn't find any entry that has endDate prior to '"
                + CoreERLogger.formatERDateDayOnly(runningDate) + "'", null).log(subContext);
            return;
        }

        final Map<String, List<SubscriberAuxiliaryService>> serviceBySubIdMap = SubscriberAuxiliaryServiceSupport.buildSubIdAssociationMap(subContext, allEntry);

        for (final String subId : serviceBySubIdMap.keySet())
        {
            if (!LifecycleStateEnum.RUNNING.equals(this.getState()))
            {
                String msg = "Lifecycle agent '" + this.getAgentId() + "' no longer running.  Remaining auxiliary services will be processed next time.";
                new InfoLogMsg(this, msg, null).log(ctx);
                throw new AbortVisitException(msg);
            }

            final List<SubscriberAuxiliaryService> associations = serviceBySubIdMap.get(subId);

            try
            {
                subscriber = (Subscriber) subscriberHome.find(ctx, subId);
            }
            catch (final HomeException e)
            {
                new MinorLogMsg(this, "UnProvisionAuxiliaryServiceAgentt Can not find subscriber with id: " + subId, e)
                    .log(subContext);
                subscriber = null;
            }

            if (subscriber == null)
            {
                new MinorLogMsg(this, " ProvisionAuxiliaryServiceAgent Can not find subscriber with id: " + subId, null)
                    .log(subContext);
                continue;
            }

            if (associations != null)
            {
                unprovisionSubscriber(subContext, subscriber, associations, subAuxServiceHome, runningDate);
            }
        }
    }
    
    /**
     * Unprovision the pending auxiliary services of a single subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber being processed.
     * @param associations
     *            The list of associations of this subscriber to process.
     * @param subAuxServiceHome
     *            Subscriber-auxiliary service home.
     * @param runningDate
     *            Date this process appears for.
     */
    private void unprovisionSubscriber(final Context ctx, final Subscriber subscriber, final List<SubscriberAuxiliaryService> associations,
        final Home subAuxServiceHome, final Date runningDate)
    {
        Collection existingActiveAuxSvcs = null;
        Collection existingFutureAuxSvcs = null;

        try
        {
            existingActiveAuxSvcs = SubscriberAuxiliaryServiceSupport.getActiveSubscriberAuxiliaryServices(ctx,
                subscriber, runningDate);
        }
        catch (final HomeException e)
        {
            new MajorLogMsg(this,
                "UnProvisionAuxiliaryServiceAgent can not retrieve active AuxiliaryService for subscriber "
                    + subscriber.getId(), e).log(ctx);
        }

        if (existingActiveAuxSvcs == null)
        {
            existingActiveAuxSvcs = new ArrayList();
        }

        try
        {
            existingFutureAuxSvcs = SubscriberAuxiliaryServiceSupport.getSubscriberFutureProvisionAuxSvcs(ctx,
                subscriber, runningDate);
        }
        catch (final HomeException e)
        {
            new MajorLogMsg(this,
                "UnProvisionAuxiliaryServiceAgent can not retrieve active future AuxiliaryService for subscriber "
                    + subscriber.getId(), e).log(ctx);
        }

        if (existingFutureAuxSvcs == null)
        {
            existingFutureAuxSvcs = new ArrayList();
        }

        // start to set auxiliaryservice and future auxiliaryService field
        // for this subscirber
        for (final SubscriberAuxiliaryService association : associations)
        {
            if (!LifecycleStateEnum.RUNNING.equals(this.getState()))
            {
                String msg = "Lifecycle agent '" + this.getAgentId() + "' no longer running.  Remaining auxiliary services will be processed next time.";
                new InfoLogMsg(this, msg, null).log(ctx);
                throw new AbortVisitException(msg);
            }
            unprovisionAssociation(ctx, subscriber, association, subAuxServiceHome);
        }
    }


    /**
     * Unrovision the pending auxiliary services of a single subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber being processed.
     * @param association
     *            The association being processed.
     * @param subAuxServiceHome
     *            Subscriber-auxiliary service home.
     */
    private void unprovisionAssociation(final Context ctx, final Subscriber subscriber,
        final SubscriberAuxiliaryService association, final Home subAuxServiceHome)
    {
        AuxiliaryService service = null;
        try
        {
            service = association.getAuxiliaryService(ctx);
        }
        catch (final HomeException e)
        {
            new MajorLogMsg(this, "UnProvisionAuxiliaryServiceAgent can not find AuxiliaryService ID ["
                + association.getAuxiliaryServiceIdentifier() + "] for subscriber" + subscriber.getId(), e).log(ctx);
        }

        if (service != null)
        {
            try
            {
                SubscriberAuxiliaryServiceSupport.removeSubscriberAuxiliaryService(ctx, association);

                try
                {
                    NoteSupportHelper.get(ctx).addSubscriberNote(
                            ctx,
                            subscriber.getId(),
                            "Subscriber updating succeeded\nSubscriber Auxiliary Service "
                                    + association.getAuxiliaryServiceIdentifier() + " unprovisioned.",
                            SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE);
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx,  this, "Unable to log note: " + e.getMessage(), e);
                }

                CrmCharger  charger = new SubscriberAuxiliaryServiceCharger(subscriber, association);
                charger.refund(ctx, null); 

            }
            catch (final HomeException he)
            {
                new MajorLogMsg(this,
                    "UnProvisionAuxiliaryServiceAgent can not UnProvision AuxiliaryService for subscriber "
                        + subscriber.getId() + " because AuxiliaryService "
                        + association.getAuxiliaryServiceIdentifier() + " can not be removed from subscriber profile.",
                    he).log(ctx);
            }
        }
        else
        {
            new MajorLogMsg(this,
                "UnProvisionAuxiliaryServiceAgent can not UnProvision AuxiliaryService for subscriber "
                    + subscriber.getId() + " because AuxiliaryService " + association.getAuxiliaryServiceIdentifier()
                    + " can not be found.", null).log(ctx);
            // TODO: leave it untouched or remove it ?
        }
    }


    /**
     * Retrieves all associations which should be unprovisioned but currently aren't.
     *
     * @param ctx
     *            The operating context.
     * @param subAuxServiceHome
     *            Subscriber auxiliary service home.
     * @param runningDate
     *            The date used to determine eligibility.
     * @return A collection of all associations which should be unprovisioned but
     *         currently aren't.
     */
    private Collection<SubscriberAuxiliaryService> getAllAssociationsToBeUnprovisioned(final Context ctx, final Home subAuxServiceHome,
        final Date runningDate)
    {
        Collection<SubscriberAuxiliaryService> allEntry = null;
        try
        {
            // find all Associations that have endDate being or before runningDate
            final And predicate = new And();
            predicate.add(new LT(SubscriberAuxiliaryServiceXInfo.END_DATE, CalendarSupportHelper.get(ctx)
                .getDateWithNoTimeOfDay(runningDate)));
            predicate.add(new EQ(SubscriberAuxiliaryServiceXInfo.PROVISIONED, Boolean.TRUE));

            allEntry = subAuxServiceHome.select(ctx, predicate);
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(this, "Failed to look-up AuxiliaryService associations for endDate < " + runningDate, t)
                .log(ctx);
        }
        return allEntry;
    }


    /**
     * Gets the running date.
     * 
     * @param context
     *            The operating context.
     * @return The "current date" for the dunning report generation run.
     * @throws AgentException
     *             thrown if any Exception is thrown during date parsing. Original
     *             Exception is linked.
     */
    private Date getRunningDate(final Context context) 
    {
        Date reportDate = getParameter1(context, Date.class);
        if (reportDate==null)
        {
            reportDate = new Date();
        }
        return reportDate;

    }
}
