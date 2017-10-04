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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberHomezone;
import com.trilogy.app.crm.bean.SubscriberHomezoneHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.ProvisionableAuxSvcExtension;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.SubscriberAuxiliaryServiceCharger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class AuxiliaryServicesProvisioningLifecycleAgent extends LifecycleAgentScheduledTask
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a AuxiliaryServicesProvisioningLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public AuxiliaryServicesProvisioningLifecycleAgent(Context ctx, final String agentId) throws AgentException
    {
        super(ctx, agentId);
    }
    
    protected void start(Context ctx) throws LifecycleException
    {
        final Date runningDate = getRunningDate(ctx);

        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Provisioning Auxiliary Services for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
            sb.append("'.");
            LogSupport.debug(ctx, this, sb.toString());
        }
        
        Context subContext = ctx.createSubContext();

        final Home subAuxServiceHome = (Home) subContext.get(SubscriberAuxiliaryServiceHome.class);
        final Home subscriberHome = (Home) subContext.get(SubscriberHome.class);
        final Home subHomezoneHome = (Home) subContext.get(SubscriberHomezoneHome.class);

        subContext.put(CommonTime.RUNNING_DATE, runningDate);
        subContext.put(CronConstants.FROM_CRON_AGENT_CTX_KEY, Boolean.TRUE);

        Collection auxiliaryServicesToProvision = getAssociationsToBeProvisioned(subContext, runningDate);

        if (auxiliaryServicesToProvision != null && auxiliaryServicesToProvision.size()>0)
        {
            final Map<String, List<SubscriberAuxiliaryService>> serviceBySubIdMap = SubscriberAuxiliaryServiceSupport.buildSubIdAssociationMap(subContext, auxiliaryServicesToProvision);

            for (final String subId : serviceBySubIdMap.keySet())
            {
                if (!LifecycleStateEnum.RUNNING.equals(this.getState()))
                {
                    String msg = "Lifecycle agent '" + this.getAgentId() + "' no longer running.  Remaining auxiliary services will be processed next time.";
                    new InfoLogMsg(this, msg, null).log(ctx);
                    throw new AbortVisitException(msg);
                }

                final List<SubscriberAuxiliaryService> associations = serviceBySubIdMap.get(subId);
                Subscriber subscriber = null;
                HomeException exception = null;
                try
                {
                    subscriber = (Subscriber) subscriberHome.find(subContext, new EQ(SubscriberXInfo.ID, subId));
                }
                catch (final HomeException e)
                {
                    exception = e;
                }

                if (subscriber == null)
                {
                    new MinorLogMsg(this, " ProvisionAuxiliaryServiceAgent Can not find subscriber with id: "
                        + subId, exception).log(subContext);
                }
                else if (associations != null)
                {
                    provisionSubscriber(subContext, subscriber, associations, subAuxServiceHome, subHomezoneHome, runningDate);
                }
            }
        }
        else
        {
            new InfoLogMsg(this, "No auxiliary services to be provisioned found for date '"
                + CoreERLogger.formatERDateDayOnly(runningDate) + "'", null).log(subContext);
        }

    }
    
    /**
     * Provision the pending auxiliary services of a single subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber being processed.
     * @param associations
     *            The list of associations of this subscriber to process.
     * @param subAuxServiceHome
     *            Subscriber-auxiliary service home.
     * @param subHomezoneHome
     *            Subscriber-homezone home.
     * @param runningDate
     *            Date this process appears for.
     */
    private void provisionSubscriber(final Context ctx, final Subscriber subscriber, final List<SubscriberAuxiliaryService> associations,
        final Home subAuxServiceHome, final Home subHomezoneHome, final Date runningDate) throws AbortVisitException
    {
        /*
         * TODO - Don't process subscriber if it's deactivated?
         */

        /*
         * start to set auxiliary service and future auxiliaryService field for this
         * subscriber
         */
        for (final SubscriberAuxiliaryService association : associations)
        {
            if (!LifecycleStateEnum.RUNNING.equals(this.getState()))
            {
                String msg = "Lifecycle agent '" + this.getAgentId() + "' no longer running.  Remaining auxiliary services will be processed next time.";
                new InfoLogMsg(this, msg, null).log(ctx);
                throw new AbortVisitException(msg);
            }

            provisionAssociation(ctx, association, subscriber, subAuxServiceHome, subHomezoneHome);
        }

        /*
         * Actually there is no explicit need of setting these services to subscriber and
         * then calling SubscriberHome.store instead we can directly call
         * SubscriberAuxiliaryServiceHome.create() and remove
         */

        /*
         * If this collection is empty then no point in storing that because to be valid
         * something should be there, if we are adding future auxiliary service at least
         * it should be there. This check is put because upon any error in getting correct
         * data in above steps, the store action may remove current active auxiliary
         * services erroneously
         */

        /*
         * if(!existingActiveAuxSvcs.isEmpty()) {
         * subscriber.setAuxiliaryServices(existingActiveAuxSvcs);
         * subscriber.setFutureAuxiliaryServices(existingFutureAuxSvcs); try {
         * subscriberHome.store(ctx,subscriber); } catch(Exception e) { new
         * MajorLogMsg(this, "ProvisionAuxiliaryServiceAgent can not Provision
         * AuxiliaryService for subscriber " + subId, e).log(ctx); } // catch }
         */
    }


    /**
     * Provision the pending auxiliary services of a single subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param association
     *            The association being processed.
     * @param subscriber
     *            The subscriber being processed.
     * @param subAuxServiceHome
     *            Subscriber-auxiliary service home.
     * @param subHomezoneHome
     *            Subscriber-homezone home.
     */
    private void provisionAssociation(final Context ctx, final SubscriberAuxiliaryService association,
        final Subscriber subscriber, final Home subAuxServiceHome, final Home subHomezoneHome)
    {
        if (association.getContext() == null)
        {
            association.setContext(ctx);
        }

        AuxiliaryService service = null;
        try
        {
            service = association.getAuxiliaryService(ctx);
        }
        catch (final HomeException e)
        {
            new MajorLogMsg(this, "ProvisionAuxiliaryServiceAgent can not Provision AuxiliaryService for subscriber "
                + subscriber.getId(), e).log(ctx);
        }

        if (service != null
                && !(subscriber.getState().equals(SubscriberStateEnum.SUSPENDED)
                && (service.isHLRProvisionable() || service.getType().equals(AuxiliaryServiceTypeEnum.HomeZone))))
        {
            boolean provisionOnSuspendDisable = ProvisionableAuxSvcExtension.DEFAULT_PROVONSUSPENDDISABLE;
            ProvisionableAuxSvcExtension provisionableAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service, ProvisionableAuxSvcExtension.class);
            if (provisionableAuxSvcExtension!=null)
            {
                provisionOnSuspendDisable = provisionableAuxSvcExtension.isProvOnSuspendDisable();
            }
            else 
            {
                LogSupport.minor(ctx, this,
                        "Unable to find required extension of type '" + ProvisionableAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + service.getIdentifier());
            }
            
            if (provisionOnSuspendDisable)
            {
                setExtraParams(ctx, subHomezoneHome, service, association);
    
                try
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        final StringBuilder sb = new StringBuilder();
                        sb.append("Removing SubscriberAuxiliaryService association ");
                        sb.append(association.getIdentifier());
                        sb.append(" [subscriber=");
                        sb.append(association.getSubscriberIdentifier());
                        sb.append(", auxiliaryService=");
                        sb.append(association.getAuxiliaryServiceIdentifier());
                        sb.append("] to ensure proper unprovision (if applicable)");
                        LogSupport.debug(ctx, this, sb.toString(), null);
                    }
                    subAuxServiceHome.remove(ctx, association);
                    // going to provision it to set to true
                    association.setProvisioned(true);
    
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        final StringBuilder sb = new StringBuilder();
                        sb.append("Re-creating SubscriberAuxiliaryService association ");
                        sb.append(association.getIdentifier());
                        sb.append(" [subscriber=");
                        sb.append(association.getSubscriberIdentifier());
                        sb.append(", auxiliaryService=");
                        sb.append(association.getAuxiliaryServiceIdentifier());
                        sb.append("] to ensure proper provision");
                        LogSupport.debug(ctx, this, sb.toString(), null);
                    }
                    SubscriberAuxiliaryServiceSupport.createSubscriberAuxiliaryService(ctx, association);
                    try
                    {
                        NoteSupportHelper.get(ctx).addSubscriberNote(
                                ctx,
                                subscriber.getId(),
                                "Subscriber updating succeeded\nSubscriber Auxiliary Service "
                                        + association.getAuxiliaryServiceIdentifier() + " provisioned.",
                                SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE);
                    }
                    catch (HomeException e)
                    {
                        LogSupport.minor(ctx,  this, "Unable to log note: " + e.getMessage(), e);
                    }
                    CrmCharger  charger = new SubscriberAuxiliaryServiceCharger(subscriber, association);
                    charger.charge(ctx, null); 
                }
                catch (final HomeException he)
                {
                    new MajorLogMsg(this,
                        "ProvisionAuxiliaryServiceAgent can not Provision AuxiliaryService for subscriber "
                            + subscriber.getId(), he).log(ctx);
                }
            }
        }
    }
    
    /**
     * Sets homezone extra parameters.
     *
     * @param ctx
     *            The operating context.
     * @param subHZHome
     *            Subscriber-homezone association home.
     * @param auxSvc
     *            Auxiliary service.
     * @param subAuxSvc
     *            Subscriber-auxiliary service association for which the extra parameters
     *            are set.
     */
    private void setExtraParams(final Context ctx, final Home subHZHome, final AuxiliaryService auxSvc,
        final SubscriberAuxiliaryService subAuxSvc)
    {
        subAuxSvc.setType(auxSvc.getType());

        if (auxSvc.getType() != null)
        {
            switch (auxSvc.getType().getIndex())
            {
                case AuxiliaryServiceTypeEnum.HomeZone_INDEX:
                    SubscriberHomezone subHomezoneRecord = null;
                    try
                    {
                        subHomezoneRecord = (SubscriberHomezone) subHZHome.find(ctx,
                            Long.valueOf(subAuxSvc.getIdentifier()));
                    }
                    catch (final HomeException he)
                    {
                        new MajorLogMsg(this, "Can not find corresponding subscriber homezone record for subscriber:"
                            + subAuxSvc.getSubscriberIdentifier(), he).log(ctx);
                    }

                    if (subHomezoneRecord != null)
                    {
                        subAuxSvc.setHzCellID(subHomezoneRecord.getHzCellID());
                        subAuxSvc.setHzX(subHomezoneRecord.getHzX());
                        subAuxSvc.setHzY(subHomezoneRecord.getHzY());
                        subAuxSvc.setHzPriority(subHomezoneRecord.getHzPriority());
                    }
                    break;
                case AuxiliaryServiceTypeEnum.AdditionalMsisdn_INDEX:
                    String bearerType = null;
                    AddMsisdnAuxSvcExtension extension = ExtensionSupportHelper.get(ctx).getExtension(ctx , auxSvc, AddMsisdnAuxSvcExtension.class);
                    if (extension!=null)
                    {
                        bearerType = extension.getBearerType();
                    }
                    else 
                    {
                        LogSupport.minor(ctx, this,
                                "Unable to find required extension of type '" + AddMsisdnAuxSvcExtension.class.getSimpleName()
                                        + "' for auxiliary service " + auxSvc.getIdentifier());
                    }
                    subAuxSvc.setBearerType(bearerType);
                    Msisdn msisdn = null;
                    try
                    {
                        msisdn = AdditionalMsisdnAuxiliaryServiceSupport.getAMsisdn(ctx, subAuxSvc);
                    }
                    catch (final HomeException exception)
                    {
                        new MajorLogMsg(this, "Can not find AMSISDN for auxiliary service " + auxSvc.getIdentifier()
                            + " of subscribr:" + subAuxSvc.getSubscriberIdentifier(), exception).log(ctx);
                    }
                    if (msisdn != null)
                    {
                        subAuxSvc.setAMsisdn(msisdn.getMsisdn());
                    }
                    break;
            }
        }
    }
    
    /**
     * Retrieves all associations which should be provisioned but currently aren't.
     *
     * @param ctx
     *            The operating context.
     * @param subAuxServiceHome
     *            Subscriber auxiliary service home.
     * @param runningDate
     *            The date used to determine eligibility.
     * @return A collection of all associations which should be provisioned but currently
     *         aren't.
     */
    private Collection<SubscriberAuxiliaryService> getAssociationsToBeProvisioned(final Context ctx,
        final Date runningDate)
    {
        Collection<SubscriberAuxiliaryService> result = null;
        try
        {
            final And predicate = new And();
            predicate.add(new LTE(SubscriberAuxiliaryServiceXInfo.START_DATE, CalendarSupportHelper.get(ctx).getEndOfDay(runningDate)));
            predicate.add(new GT(SubscriberAuxiliaryServiceXInfo.END_DATE, CalendarSupportHelper.get(ctx).getEndOfDay(runningDate)));
            predicate.add(new EQ(SubscriberAuxiliaryServiceXInfo.PROVISIONED, Boolean.FALSE));
            
            result = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberAuxiliaryService.class, predicate);
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(this, "Failed to look-up AuxiliaryService associations for startDate= " + runningDate, t)
                .log(ctx);
        }
        return result;
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
