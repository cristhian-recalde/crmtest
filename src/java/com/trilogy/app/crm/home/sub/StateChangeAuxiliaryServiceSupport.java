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

package com.trilogy.app.crm.home.sub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bas.recharge.SuspensionSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.DiscountAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.ProvisionableAuxSvcExtension;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;



/**
 * Support Auxiliary Service feature for Transitioning Subscribers between States.
 *
 * @author larry.xia@redknee.com
 */
public final class StateChangeAuxiliaryServiceSupport
{

    /**
     * Creates a new <code>StateChangeAuxiliaryServiceSupport</code> instance. This
     * method is made private to prevent instantiation of utility class.
     */
    private StateChangeAuxiliaryServiceSupport()
    {
        // empty
    }


    /**
     * Attempts to provision auxiliary services of the subscriber to HLR.
     *
     * @param ctx
     *            The operating context.
     * @param newSub
     *            The subscriber to be provisioned.
     * @throws HomeException
     *             Thrown if HLR provisioning of one or more auxiliary service fails.
     */
    public static void updateHlrToProvisioning(final Context ctx, final Subscriber newSub, Object caller) throws HomeException
    {
        final Collection<SubscriberAuxiliaryService> auxServiceCol = new ArrayList<SubscriberAuxiliaryService>(newSub.getAuxiliaryServices(ctx));

        if (LogSupport.isDebugEnabled(ctx))
        {
            final String msg = "Attempting to provision AuxiliaryService for Subscriber " + newSub.getId()
                + " , the AuxiliaryService it contains are: \n" + auxServiceCol;
            new DebugLogMsg(StateChangeAuxiliaryServiceSupport.class, msg, null).log(ctx);
        }

        if (auxServiceCol != null)
        {
            for (final SubscriberAuxiliaryService subService : auxServiceCol)
            {   
                try
                {
                    final AuxiliaryService service = subService.getAuxiliaryService(ctx);
                    
                    if (!AuxiliaryServiceTypeEnum.MultiSIM.equals(service.getType())
                            && service.isHLRProvisionable() 
                            && isProvOnSuspendDisable(ctx, service))
                    {
                        provisionHlr(ctx, subService, service, newSub,caller);
                    }
                }
                catch (final HomeException e)
                {
                    new InfoLogMsg(StateChangeAuxiliaryServiceSupport.class, "AuxilaryService ["
                        + subService.getAuxiliaryServiceIdentifier() + "] for Subscriber [" + newSub.getId()
                        + "] can not be provisioned in HLR", e).log(ctx);
                    // TODO rethrow?
                }

            }
        }
    }
    
    private static boolean isProvOnSuspendDisable(Context ctx, AuxiliaryService service)
    {
        boolean provisionOnSuspendDisable = ProvisionableAuxSvcExtension.DEFAULT_PROVONSUSPENDDISABLE;
        ProvisionableAuxSvcExtension provisionableAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service, ProvisionableAuxSvcExtension.class);
        if (provisionableAuxSvcExtension!=null)
        {
            provisionOnSuspendDisable = provisionableAuxSvcExtension.isProvOnSuspendDisable();
        }
        else 
        {
            LogSupport.minor(ctx, StateChangeAuxiliaryServiceSupport.class,
                    "Unable to find required extension of type '" + ProvisionableAuxSvcExtension.class.getSimpleName()
                            + "' for auxiliary service " + service.getIdentifier());
        }
        return provisionOnSuspendDisable;
    }


    /**
     * Attempts to unprovision auxiliary services of the subscriber to HLR.
     *
     * @param ctx
     *            The operating context.
     * @param newSub
     *            The subscriber to be unprovisioned.
     * @throws HomeException
     *             Thrown if HLR unprovisioning of one or more auxiliary service fails.
     */

    public static void updateHlrToUnProvisioning(final Context ctx, final Subscriber newSub, Object caller) throws HomeException
    {
        final Collection<SubscriberAuxiliaryService> auxServiceCol = new ArrayList<SubscriberAuxiliaryService>(newSub.getAuxiliaryServices(ctx));

        if (LogSupport.isDebugEnabled(ctx))
        {
            final String msg = "Attempting to unprovision AuxiliaryService for Subscriber " + newSub.getId()
                + " , the AuxiliaryService it contains are: \n" + auxServiceCol;
            new DebugLogMsg(StateChangeAuxiliaryServiceSupport.class, msg, null).log(ctx);
        }

        if (auxServiceCol != null)
        {
            for (final SubscriberAuxiliaryService subService : auxServiceCol)
            {
                try
                {
                    final AuxiliaryService service = subService.getAuxiliaryService(ctx);

                    if (!AuxiliaryServiceTypeEnum.MultiSIM.equals(service.getType())
                            && service.isHLRProvisionable() 
                            && isProvOnSuspendDisable(ctx, service))
                    {
                        unProvisionHlr(ctx, subService, service, newSub, caller);
                    }
                }
                catch (final HomeException e)
                {
                    new InfoLogMsg(StateChangeAuxiliaryServiceSupport.class, "AuxilaryService ["
                        + subService.getAuxiliaryServiceIdentifier() + "] for Subscriber [" + newSub.getId()
                        + "] can not be provisioned in HLR", e).log(ctx);
                    // TODO rethrow?
                }
            }
        }
    }


    public static void updateHlrToSuspend(Context ctx, Subscriber newSub, Object caller)
    {
        updateSuspendStateToHlr(ctx, newSub, true, caller);
    }


    public static void updateHlrToUnSuspend(Context ctx, Subscriber newSub, Object caller)
    {
        updateSuspendStateToHlr(ctx, newSub, false, caller);
    }

    public static void updateSuspendStateToHlr(Context ctx, Subscriber newSub, boolean suspend, Object caller)
    {
        final Collection<SubscriberAuxiliaryService> auxServiceCol = new ArrayList<SubscriberAuxiliaryService>(newSub.getAuxiliaryServices(ctx));

        if (auxServiceCol != null)
        {
            Map<Long, Map<Long, SubscriberAuxiliaryService>> map = new HashMap<Long, Map<Long, SubscriberAuxiliaryService>>();
            
            for (final SubscriberAuxiliaryService subService : auxServiceCol)
            {
                try
                {
                    AuxiliaryService auxSvc = subService.getAuxiliaryService(ctx);
                    if (AuxiliaryServiceTypeEnum.MultiSIM.equals(auxSvc.getType()))
                    {
                        Map<Long, SubscriberAuxiliaryService> assocMap = map.get(auxSvc.getIdentifier());
                        if (assocMap == null)
                        {
                            assocMap = new HashMap<Long, SubscriberAuxiliaryService>();
                            map.put(auxSvc.getIdentifier(), assocMap);
                        }
                        assocMap.put(subService.getSecondaryIdentifier(), subService);
                    }
                }
                catch (final HomeException e)
                {
                    new InfoLogMsg(StateChangeAuxiliaryServiceSupport.class, "AuxilaryService ["
                        + subService.getAuxiliaryServiceIdentifier() + "] for Subscriber [" + newSub.getId()
                        + "] can not be suspended in HLR", e).log(ctx);
                    // TODO rethrow?
                }
            }

            SuspensionSupport.suspendAuxServices(ctx, newSub, map, suspend, caller);
        }
    }


    /**
     * run HLR provision command for the given subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param association
     *            The subscriber-auxiliary service association.
     * @param service
     *            The auxiliary service for which to check the HLR provisioning.
     * @param subscriber
     *            The subscriber for which to run HLR provision command .
     * @throws HomeException
     */
    public static void provisionHlr(final Context ctx, final SubscriberAuxiliaryService association,
        final AuxiliaryService service, final Subscriber subscriber, Object caller) throws HomeException
    {
        if (service == null || subscriber == null || association == null)
        {
            throw new HomeException(
                "System error: Auxiliary service, subscriber, and the association must not be null");
        }
        Msisdn aMsisdn = null;
        String bearerType = null;
        if (service.getType() == AuxiliaryServiceTypeEnum.AdditionalMsisdn
            && AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(ctx))
        {
            aMsisdn = (Msisdn) ctx.get(Lookup.NEW_AMSISDN);
            if (aMsisdn == null)
            {
                aMsisdn = AdditionalMsisdnAuxiliaryServiceSupport.getAMsisdn(ctx, association, service, subscriber);
            }
            bearerType = AdditionalMsisdnAuxiliaryServiceSupport.getBearerType(ctx, association, service);
        }
        provisionHlr(ctx, service, association, subscriber, aMsisdn, bearerType, caller);
    }


    /**
     * run HLR provision command for the given subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param service
     *            The auxiliary service for which to check the HLR provisioning.
     * @param subscriber
     *            The subscriber for which to run HLR provision command .
     * @param aMsisdn
     *            The additional MSISDN to be provisioned. Use <code>null</code> if this
     *            is not an additional MSISDN HLR command.
     * @param bearerType
     *            The bearer type of the additional MSISDN to be provisioned. Use
     *            <code>null</code> if this is not an additional MSISDN HLR command.
     * @throws HomeException
     */
    private static void provisionHlr(final Context ctx, final AuxiliaryService service, final SubscriberAuxiliaryService association,
            final Subscriber subscriber, final Msisdn aMsisdn, final String bearerType, Object caller) throws HomeException
    {
        if (SystemSupport.needsHlr(ctx))
        {
            Context subCtx = ctx.createSubContext();
            populateDataHlrProvisioning(subCtx, service, association, subscriber, aMsisdn, bearerType);
            int resultCode = ServiceProvisioningGatewaySupport.prepareAndSendIndividualServiceToSPG(subCtx, subscriber, service,
                    ServiceProvisioningGatewaySupport.HLR_SERVICE_SPG_SERVICE_ID, true, caller);
            if (resultCode != 0)
            {
                String msg = "Provisioning update to Provisioning Gateway failed [SubID=" + subscriber.getId() + ",AuxSvcId=" + service.getIdentifier() + "].  Result Code [" + resultCode+ "]";
                new MinorLogMsg(StateChangeAuxiliaryServiceSupport.class, msg, null).log(ctx);
                throw new HomeException(msg);
            }
        }
    }


    /**
     * run HLR unprovision command for the given subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param service
     *            The auxiliary service for which to check the HLR unprovisioning.
     * @param subscriber
     *            The subscriber for which to run HLR unprovision command.
     * @throws HomeException
     */
    public static void unProvisionHlr(final Context ctx, final SubscriberAuxiliaryService association, final AuxiliaryService service, final Subscriber subscriber, Object caller)
        throws HomeException
    {
        if (service == null || subscriber == null)
        {
            throw new IllegalArgumentException("Auxiliary service and subscriber must not be null");
        }
        Msisdn aMsisdn = null;
        String bearerType = null;
        if (service.getType() == AuxiliaryServiceTypeEnum.AdditionalMsisdn
            && AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(ctx))
        {
            aMsisdn = AdditionalMsisdnAuxiliaryServiceSupport.getAMsisdn(ctx, service.getIdentifier(), subscriber
                .getId());
            AddMsisdnAuxSvcExtension extension = ExtensionSupportHelper.get(ctx).getExtension(ctx , service, AddMsisdnAuxSvcExtension.class);
            if (extension!=null)
            {
                bearerType = extension.getBearerType();
            }
            else 
            {
                LogSupport.minor(ctx, StateChangeAuxiliaryServiceSupport.class,
                        "Unable to find required extension of type '" + AddMsisdnAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + service.getIdentifier());
            }
        }
        unProvisionHlr(ctx, service, association, subscriber, aMsisdn, bearerType, caller);
    }


    private static void populateDataHlrProvisioning(final Context subCtx, final AuxiliaryService service,
            final SubscriberAuxiliaryService association, final Subscriber subscriber, final Msisdn aMsisdn, final String bearerType)
    {
        subCtx.put(AuxiliaryService.class, service);
        subCtx.put(SubscriberAuxiliaryService.class, association);
        subCtx.put(Subscriber.class, subscriber);
        if (aMsisdn != null)
        {
            subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_AMSISDN, aMsisdn.getMsisdn());
        }
        subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_BEAR_TYPE_ID, bearerType);
        subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_OLD_IMSI, subCtx.get(HLRConstants.HLR_PARAMKEY_OLD_IMSI_KEY));
        //subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_SERVICE_ID, String.valueOf(service.getID() + "_AUX"));
        subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_AUX_SERVICE));
        
        if (subscriber.getTechnology() == TechnologyEnum.GSM)
        {
            try
            {
                final GSMPackage pkg = PackageSupportHelper.get(subCtx).getGSMPackage(subCtx, subscriber.getPackageId());
                subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PACK_KI, pkg.getKI());
            }
            catch (HomeException homeEx)
            {
            }
        }
    }
    /**
     * run HLR unprovision command for the given subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param service
     *            The auxiliary service for which to check the HLR unprovisioning.
     * @param subscriber
     *            The subscriber for which to run HLR unprovision command.
     * @param aMsisdn
     *            The additional MSISDN to be unprovisioned. Use <code>null</code> if
     *            this is not an additional MSISDN HLR command.
     * @param bearerType
     *            The bearer type of the additional MSISDN to be unprovisioned. Use
     *            <code>null</code> if this is not an additional MSISDN HLR command.
     * @throws HomeException
     */
    private static void unProvisionHlr(final Context ctx, final AuxiliaryService service, final SubscriberAuxiliaryService association, final Subscriber subscriber,
        final Msisdn aMsisdn, final String bearerType, Object caller) throws HomeException
    {
        if (SystemSupport.needsHlr(ctx))
        {
            Context subCtx = ctx.createSubContext();
            populateDataHlrProvisioning(subCtx, service, association, subscriber, aMsisdn, bearerType);
            int resultCode = ServiceProvisioningGatewaySupport.prepareAndSendIndividualServiceToSPG(subCtx, subscriber, service,
                    ServiceProvisioningGatewaySupport.HLR_SERVICE_SPG_SERVICE_ID, false, caller);
            if (resultCode != 0)
            {
                String msg = "Unprovisioning update to Provisioning Gateway failed [SubID=" + subscriber.getId() + ",AuxSvcId=" + service.getIdentifier() + "].  Result Code [" + resultCode+ "]";
                new MinorLogMsg(StateChangeAuxiliaryServiceSupport.class, msg, null).log(ctx);
                throw new HomeException(msg);
            }
        }
    }


    /**
     * Determines whether the association is active.
     *
     * @param context
     *            The operating context.
     * @param association
     *            The association to be tested.
     * @return Returns <code>true</code> if the association is active,
     *         <code>false</code> otherwise.
     */
    public static boolean isActive(final Context context, final SubscriberAuxiliaryService association)
    {
        final Date runningDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(CalendarSupportHelper.get(context).getRunningDate(context));
        return !CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(association.getStartDate()).after(runningDate)
            && !CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(association.getEndDate()).before(runningDate);
    }


    public static boolean isProvisionOnSuspendDisable(final Context ctx, final Subscriber subscriber, final AuxiliaryService service)
    {
        /*
         * e.g when a suspended subscriber subscribes/unsubscribes a provisionable
         * auxiliary service that does not have "(Un)Provision On Suspend/Disable"
         * checked, we should send a HLR command
         */
        return isProvOnSuspendDisable(ctx, service) && SubscriberStateEnum.SUSPENDED.equals(subscriber.getState());
    }
}
