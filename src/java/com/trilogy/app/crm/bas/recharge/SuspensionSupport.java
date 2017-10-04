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

package com.trilogy.app.crm.bas.recharge;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.client.TFAAuxiliarServiceClientException;
import com.trilogy.app.crm.client.aaa.AAAClient;
import com.trilogy.app.crm.client.aaa.AAAClientException;
import com.trilogy.app.crm.client.aaa.AAAClientFactory;
import com.trilogy.app.crm.client.ipcg.IpcgClient;
import com.trilogy.app.crm.client.ipcg.IpcgClientFactory;
import com.trilogy.app.crm.client.ipcg.IpcgSubProvException;
import com.trilogy.app.crm.client.ringbacktone.RBTClient;
import com.trilogy.app.crm.client.ringbacktone.RBTClientException;
import com.trilogy.app.crm.client.ringbacktone.RBTClientFactory;
import com.trilogy.app.crm.client.smsb.AppSmsbClient;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.extension.auxiliaryservice.PRBTAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.SPGAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtension;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtensionXInfo;
import com.trilogy.app.crm.ff.BlacklistWhitelistPLPSupport;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.ff.PersonalListPlanSupport;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.log.MultiSimProvisioningTypeEnum;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.provision.URCSPromotionUnprovisionAgent;
import com.trilogy.app.crm.provision.agent.resume.GenericResumeAgent;
import com.trilogy.app.crm.provision.agent.resume.WimaxResumeAgent;
import com.trilogy.app.crm.provision.agent.suspend.WimaxSuspendAgent;
import com.trilogy.app.crm.provision.agent.suspend.GenericSuspendAgent;
import com.trilogy.app.crm.provision.gateway.SPGServiceProvisionCollector;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.app.crm.state.ResumeBlackberryServiceUpdateAgent;
import com.trilogy.app.crm.state.SuspendBlackberryServiceUpdateAgent;
import com.trilogy.app.crm.subscriber.provision.TFAAuxServiceSupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.voicemail.VoiceMailConstants;
import com.trilogy.app.crm.voicemail.VoiceMailServer;
import com.trilogy.app.crm.voicemail.client.ExternalProvisionResult;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiConstants;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.osa.ecp.provision.ErrorCode;


/**
 * Support class for suspension.
 *
 * @author cindy.wong@redknee.com
 * @since 12-May-08
 */
public final class SuspensionSupport
{
    /**
     * Creates a new <code>SuspensionSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private SuspensionSupport()
    {
        // empty
    }

    /**
     * Whether the SMS message sent should include unsubscribe footer.
     *
     * @param packages
     *            The packages being suspended.
     * @param services
     *            The services being suspended.
     * @param bundles
     *            The bundles being suspended.
     * @param auxServices
     *            The auxiliary services being suspended.
     * @return Whether to send the unsusbscribe footer.
     */
    public static boolean willUnsubscribe(final Context ctx, final Map packages, final Map services, final Map bundles,
        final Map auxServices)
    {
        boolean result = false;
        final Iterator it = auxServices.values().iterator();
        while (it.hasNext())
        {
            final AuxiliaryService auxSrv = (AuxiliaryService) it.next();

            if (auxSrv.isPLP(ctx)
                || auxSrv.getType() == AuxiliaryServiceTypeEnum.HomeZone)
            {
                result = true;
                break;
            }
        }

        return result;
    }
    


    /**
     * Converts a {@link ServiceFee2} object to a {@link Service} object.
     *
     * @param ctx
     *            The operating context.
     * @param serviceFee
     *            The service fee object to be converted.
     * @param sub
     *            The subscriber this conversion is for.
     * @return The service object created from the service fee.
     */
    public static Service serviceFromServiceFee(final Context ctx, final ServiceFee2 serviceFee, final Subscriber sub)
    {
        try
        {
            return serviceFee.getService(ctx);
        }
        catch (final HomeException e)
        {
            LogSupport.major(ctx, SuspensionSupport.class, "Problem occurred while retrieveing service "
                + serviceFee.getServiceId() + " for subscriber " + sub.getId(), e);
        }
        return null;
    }


    /**
     * Convert a {@link BundleFee} object to a {@link BundleProfile} object.
     *
     * @param ctx
     *            The operating context.
     * @param bundleFee
     *            The bundle fee to be converted.
     * @param sub
     *            The subscriber this conversion is for.
     * @return The bundle object created from the bundle fee.
     */
    public static BundleProfile bundleFromBundleFee(final Context ctx, final BundleFee bundleFee,
        final Subscriber sub)
    {
        try
        {
            return bundleFee.getBundleProfile(ctx, sub.getSpid());
        }
        catch (final Exception e)
        {
            LogSupport.major(ctx, SuspensionSupport.class, "Problem occurred while retrieveing bundle profile "
                + bundleFee.getId() + " for subscriber " + sub.getId(), e);
        }
        return null;
    }


    /**
     * Adds subscriber to a closed user group.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber to be added.
     * @param service
     *            The service associated with the CUG.
     * @throws HomeException
     *             Thrown if there are problems adding the subscriber to the given CUG.
     */
    private static void addSubscriberToCUG(final Context ctx, final Subscriber subscriber,
        final AuxiliaryService service) throws HomeException
    {
        final String msisdn = subscriber.getMSISDN();
        long cugIdentifier = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPIDENTIFIER;
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            cugIdentifier = callingGroupAuxSvcExtension.getCallingGroupIdentifier();
        }
        
        try
        {
            final FFECareRmiService client = FFClosedUserGroupSupport.getFFRmiService(ctx, SuspensionSupport.class);

            final int result = client.cugAddSub(cugIdentifier, msisdn);

            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                throw new HomeException("Friends and Family service returned "
                    + CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result)
                    + " while attempting to add subscriber " + msisdn + " from closed user group " + cugIdentifier);
            }
        }
        catch (final FFEcareException e)
        {
            throw new HomeException(e);
        }
        catch (final RemoteException exception)
        {
            throw new HomeException("Failed to add subscriber " + msisdn + " from closed user group "
                + cugIdentifier, exception);
        }
    }


    /**
     * Suspend auxiliary services of a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber whose auxiliary services are being suspended.
     * @param map
     *            The map of auxiliary services to be suspended. This is a map of (ID,
     *            auxiliary services).
     * @param suspend
     *            Whether to suspend or unsuspend.
     * @throws HomeException 
     */
    public static boolean suspendAuxServices(final Context ctx, final Subscriber sub,
            final Map<Long, Map<Long, SubscriberAuxiliaryService>> map, final boolean suspend, Object caller) 
    {
        StringBuilder details = new StringBuilder();
        details.append(suspend?"SUSPENSION":"UNSUSPENSION");
        details.append(": SubscriberId='");
        details.append(sub.getId());
        details.append("'");
        
        boolean result = true;
        final List<SubscriberAuxiliaryService> toRemove = new LinkedList<SubscriberAuxiliaryService>();

        for (final Long id : map.keySet())
        {
            AuxiliaryService auxSrv = null;
            SubscriberAuxiliaryService subAuxService = null;
            try
            {
                Map<Long, SubscriberAuxiliaryService> secMap = (map.get(id));
                subAuxService = secMap.values().iterator().next();
                if (subAuxService!=null)
                {
                    auxSrv = subAuxService.getAuxiliaryService(ctx);                  
                }
                else
                {
					auxSrv =
					    AuxiliaryServiceSupport.getAuxiliaryService(ctx,
					        id.longValue());
                }
            }
            catch (final HomeException exception)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append("SuspendEntitiesVisitor.suspendAuxServices(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(ctx, SuspendEntitiesVisitor.class, sb.toString(), exception);
                }
                result = false;
                continue;
            }

            if (suspend)
            {
                subAuxService.setProvisionAction(com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.SUSPEND);
            }
            else
            {
                subAuxService.setProvisionAction(com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.RESUME);
            }   
            
            final PMLogMsg pmLogMsg;
            if (auxSrv.getType() == AuxiliaryServiceTypeEnum.Voicemail)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 voicemail auxiliary service", details.toString());
                if (suspendVoiceMail(ctx, suspend, sub, auxSrv) != SUCCESS)
                {
                    result = false;
                    subAuxService.setProvisionActionState(false);
                } else 
                {
                    subAuxService.setProvisionActionState(true);
                }
                
            }
            
            else if(auxSrv.getType() == AuxiliaryServiceTypeEnum.TFA){
          	  pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 TFA auxiliary service", details.toString());

          	  try {

          		  if(suspend)
          		  {
          			  TFAAuxServiceSupport.unprovisionSuspendedService(ctx, sub, auxSrv);
          		  }
          		  else
          		  {
          			  TFAAuxServiceSupport.provisionSuspendedService(ctx,sub,auxSrv);
          		  }
          		  
          		  subAuxService.setProvisionActionState(false);     

          	  }catch (TFAAuxiliarServiceClientException e) {

          		  result = false;
          		  LogSupport.major(ctx, SuspensionSupport.class,
          				  "Problem occurred while updating TFA auxiliary service " + auxSrv.getIdentifier()
          				  + " for subscriber " + sub.getId() + " to TFA server", e);
          		  
          	  } catch (HomeException e) {
          		  
         		 LogSupport.major(ctx, SuspensionSupport.class,
                          "Problem occurred while updating TFA auxiliary service " + auxSrv.getIdentifier()
                              + " for subscriber " + sub.getId() + " to TFA server", e);
                  result = false; 
				}
          }      
            else if (auxSrv.getType() == AuxiliaryServiceTypeEnum.MultiSIM)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 multi-SIM auxiliary service", details.toString());
                
                // Create the suspension ER for the service having secondary ID = DEFAULT
                for (final Map.Entry<Long, SubscriberAuxiliaryService> entry : map.get(id).entrySet())
                {
                    final SubscriberAuxiliaryService association = entry.getValue();
                    if (association != null)
                    {
                        final MultiSimProvisioningTypeEnum suspensionType;
                        if (suspend)
                        {
                            suspensionType = MultiSimProvisioningTypeEnum.SUSPENDED;
                        }
                        else
                        {
                            suspensionType = MultiSimProvisioningTypeEnum.UNSUSPENDED;
                        }
                        
                        handleMultiSimSuspensionEvent(ctx, suspensionType, association);
                    }
                    
                }
            }
            else if (auxSrv.isHLRProvisionable())
            {                
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 HLR auxiliary service", details.toString());
                if (suspendHlrAuxService(ctx, sub, subAuxService, auxSrv, suspend, caller) != SUCCESS)
                {
                    result =false;
                } else if (auxSrv.getType().getIndex() == AuxiliaryServiceTypeEnum.PRBT_INDEX)
                {
                    long rbtId = PRBTAuxSvcExtension.DEFAULT_RBTID;
                    PRBTAuxSvcExtension prbtAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxSrv, PRBTAuxSvcExtension.class);
                    if (prbtAuxSvcExtension!=null)
                    {
                        rbtId = prbtAuxSvcExtension.getRbtId();
                    }
                    RBTClient client = RBTClientFactory.locateClient(rbtId);
                    
                    if (client != null)
                    {
                       
                        Context subCtx   = ctx.createSubContext();
                        subCtx.put(AuxiliaryService.class, auxSrv);
                    
                        try
                        {
                            if (suspend)
                            {
                                client.updateSubscriberSuspend(subCtx, sub.getMsisdn());
                             }
                            else
                            {
                                client.updateSubscriberReactivate(subCtx, sub.getMsisdn());;
                             }
                            subAuxService.setProvisionActionState(true);
                        }
                        catch (RBTClientException e)
                        {
                            subAuxService.setProvisionActionState(false);
                            LogSupport.major(ctx, SuspensionSupport.class,
                                    "Problem occurred while updating PRBT auxiliary service " + auxSrv.getIdentifier()
                                        + " for subscriber " + sub.getId() + " to RBT server", e);
                            result = false;                        
                        }
                    } else
                    {
                        subAuxService.setProvisionActionState(false);                        
                        LogSupport.major(ctx, SuspensionSupport.class,
                                "Problem occurred while deprovisioning PRBT auxiliary service " + auxSrv.getIdentifier()
                                    + " for subscriber " + sub.getId(), null);
                        result = false;
                    }
                }
                    
            }             
            else if (auxSrv.getType() == AuxiliaryServiceTypeEnum.HomeZone && suspend)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 homezone auxiliary service", details.toString());
                // react only to suspend actions
                // for reprovisioning Subscribers must call

                // put a marker to not attempt charging aux srv manipulation
                final Context subCtx = ctx.createSubContext();
                //subCtx.put(SubscriberAuxiliaryServiceChargingHome.BYPASS_CHARGING, Boolean.TRUE);

                try
                {
                    for (final Long secondaryId : map.get(id).keySet())
                    {
                        final SubscriberAuxiliaryService association = map.get(id).get(secondaryId);
                        SubscriberAuxiliaryServiceSupport.removeAssociationForSubscriber(subCtx, sub, auxSrv
                            .getIdentifier(), SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED);
                        toRemove.add(association);
                    }
                }
                catch (final HomeException e)
                {
                    subAuxService.setProvisionActionState(false);
                    LogSupport.major(ctx, SuspensionSupport.class,
                        "Problem occurred while deprovisioning HomeZone auxiliary service " + auxSrv.getIdentifier()
                            + " for Prepaid subscriber " + sub.getId() + " with insufficient balance.", e);
                    result = false;
                }

            }
            else if (auxSrv.getType() == AuxiliaryServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend service provisioning gateway", details.toString());
                ctx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                        String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_AUX_SERVICE));
                suspendGatewayServices(ctx, sub, auxSrv,suspend);
                
            }
            else if (auxSrv.getType() == AuxiliaryServiceTypeEnum.CallingGroup)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 calling group auxiliary service", details.toString());
                // put a marker to not attempt charging aux srv manipulation
                final Context subCtx = ctx.createSubContext();
               // subCtx.put(SubscriberAuxiliaryServiceChargingHome.BYPASS_CHARGING, Boolean.TRUE);

                CallingGroupTypeEnum callingGroupType = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPTYPE;
                CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxSrv, CallingGroupAuxSvcExtension.class);
                if (callingGroupAuxSvcExtension!=null)
                {
                    callingGroupType = callingGroupAuxSvcExtension.getCallingGroupType();
                }

                if (callingGroupType == CallingGroupTypeEnum.CUG)
                {
                    for (final Long secondaryId : map.get(id).keySet())
                    {
	                    try
	                    {
                            if (suspend)
                            {
                                ClosedUserGroupSupport.removeSubscriberFromCug(subCtx, secondaryId.longValue(), sub);
                               }
                            else
                            {
                                ClosedUserGroupSupport.addSubscriberToCug(subCtx, secondaryId.longValue(), sub, true);
                              }
                            subAuxService.setProvisionActionState(true);
	                    }
	                    catch (final HomeException e)
	                    {
	                        subAuxService.setProvisionActionState(false);
	                        
	                        LogSupport.major(ctx, SuspensionSupport.class,
	                            "Problem occurred while deprovisioning CUG auxiliary service " + auxSrv.getIdentifier()
	                                + " for Prepaid subscriber " + sub.getId() + " with insufficient balance.", e);
	                        result = false;
	                    }
                    }
                }
                
                else if (callingGroupType == CallingGroupTypeEnum.PLP)
                {
                	long plpIdentifier = 0L;
                	if (callingGroupAuxSvcExtension!=null)
                    {
                		plpIdentifier = callingGroupAuxSvcExtension.getCallingGroupIdentifier();
                    }
                    
                    try
                    {
                    	if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(SuspensionSupport.class, 
                                    "Attempting to Suspend Or Resume Personal List Plan : Suspend : " + suspend + ", PLP Auxiliary Service Identifier : "
                                    + auxSrv.getIdentifier() + ", PLP Identifier : " + plpIdentifier, null).log(ctx);
                        }
                    	PersonalListPlanSupport.suspendOrResumePLP(ctx,subAuxService,plpIdentifier,suspend);
                    	subAuxService.setProvisionActionState(true);
                    }
                    catch (final FFEcareException e)
                    {
                       	subAuxService.setProvisionActionState(false);
                       	result = false;
                    }
                    catch (final HomeException e)
                    {
                       	subAuxService.setProvisionActionState(false);
                       	result = false;
                    }
                }
                
            }
            else
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 generic service", details.toString());
            }
            
            SubscriberAuxiliaryServiceSupport.updateSubscriberAuxiliaryServiceOnXDBHomeDirectly(ctx, subAuxService); 
            if(result)
            {
            	try
            	{
					SubscriptionNotificationSupport.sendServiceStateChangeNotification(ctx, (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER), sub, null,subAuxService.getAuxiliaryService(ctx),NotificationTypeEnum.SERVICE_STATE_CHANGE,ServiceStateEnum.SUSPENDED);
				} 
            	catch (final HomeException e) 
				{
					LogSupport.major(ctx, "SuspendEntitiesVisitor",
                            "Home Exception when trying to send notification for suspension", e);
				}
            }
            pmLogMsg.log(ctx);
        }
        
        
        if (result)
        {
            // removing aux serveces that where deprovisioned
            for (final SubscriberAuxiliaryService association : toRemove)
            {
                try
                {
                    // map.remove(XBeans.getIdentifier(auxiliaryService));
                    sub.removeSuspendedAuxService(ctx, association);
                }
                catch (final HomeException e)
                { 
                     LogSupport.major(ctx, "SuspendEntitiesVisitor",
                            "Home Exception when trying to remove the Suspended Aux Service", e);
                }
            }
        }
        return result;
    }

    protected static void handleMultiSimSuspensionEvent(final Context ctx,
            final MultiSimProvisioningTypeEnum provisionType,
            final SubscriberAuxiliaryService association)
    {
        if (association.getSecondaryIdentifier() == SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
        {
            // Only log the ER for the primary service
            Collection<? extends Extension> extensions = null;
            try
            {
                Subscriber sub = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
                if (sub != null)
                {
                    extensions = sub.getExtensions();
                }
                else
                {
                    And filter = new And();
                    filter.add(new EQ(MultiSimSubExtensionXInfo.SUB_ID, association.getSubscriberIdentifier()));
                    filter.add(new EQ(MultiSimSubExtensionXInfo.AUX_SVC_ID, association.getAuxiliaryServiceIdentifier()));

                    extensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, MultiSimSubExtension.class, filter);
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(SuspensionSupport.class, "Error retrieving Multi-SIM extension subscription " + association.getSubscriberIdentifier() + " for auxiliary service " + association.getAuxiliaryServiceIdentifier(), e).log(ctx);
            }

            if (extensions != null)
            {
                for (Extension extension : extensions)
                {
                    if (extension instanceof MultiSimSubExtension
                            && ((MultiSimSubExtension) extension).getAuxSvcId() == association.getAuxiliaryServiceIdentifier())
                    {
                        ERLogger.createMultiSimProvisioningEr(ctx, (MultiSimSubExtension) extension, provisionType);
                    }
                }
            }
        }
        else
        {
            // Only send suspension/unsuspension HLR commands for individual SIMs
            Subscriber sub = null;
            try
            {
                sub = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(SuspensionSupport.class, "Error retrieving subscription " + association.getSubscriberIdentifier() + " for auxiliary service " + association.getAuxiliaryServiceIdentifier(), e).log(ctx);
            }
            
            if (sub != null)
            {
                String key = null;
                if (MultiSimProvisioningTypeEnum.SUSPENDED.equals(provisionType))
                {
                    key =  HLRConstants.PRV_CMD_TYPE_MULTISIM_SUSPEND;
                }
                else
                {
                    key = HLRConstants.PRV_CMD_TYPE_MULTISIM_UNSUSPEND;
                }
                
                if (key != null)
                {
                    // Put association in the context so that values can be retrieved from it for key/value replacement
                    Context sCtx = ctx.createSubContext();
                    sCtx.put(SubscriberAuxiliaryService.class, association);
                    
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(SuspensionSupport.class, 
                                "Attempting to send HLR command '" + key + "' for Multi-SIM auxiliary service [AuxSvcId="
                                + association.getAuxiliaryServiceIdentifier()
                                + ",SecondaryId=" + association.getSecondaryIdentifier()
                                + ",SubId=" + association.getSubscriberIdentifier() + "] for API query response.", null).log(ctx);
                    }

                    association.setProvisionAction(com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.SUSPEND);
                    
                    try
                    {
                        HlrSupport.updateHlr(sCtx, sub, key);
                        association.setProvisionActionState(true);
                    }
                    catch (ProvisionAgentException e)
                    {
                        association.setProvisionActionState(false);
                        new MinorLogMsg(SuspensionSupport.class, 
                                "Error sending HLR command '" + key + "' for subscriber " + sub.getId()
                                + "'s auxiliary service " + association.getAuxiliaryServiceIdentifier()
                                + " [RC=" + e.getResultCode() + ", Source RC=" + e.getSourceResultCode() + "]", e).log(ctx);
                        
                    }
                    catch (HomeException e)
                    {
                        association.setProvisionActionState(false);
                        new MinorLogMsg(SuspensionSupport.class, "Error sending HLR command '" + key + "' for subscriber " + sub.getId() + "'s auxiliary service " + association.getAuxiliaryServiceIdentifier(), e).log(ctx);
                    }
                }
            }
            else
            {
                association.setProvisionedState(com.redknee.app.crm.bean.service.ServiceStateEnum.SUSPENDEDWITHERRORS);
                new MinorLogMsg(SuspensionSupport.class, "Subscription " + association.getSubscriberIdentifier() + " not found for auxiliary service " + association.getAuxiliaryServiceIdentifier() + ".  No provisioning command for '" + provisionType + "' event will be sent!", null).log(ctx);
            }
            
            SubscriberAuxiliaryServiceSupport.updateSubscriberAuxiliaryServiceOnXDBHomeDirectly(ctx, association); 
        }
    }      
    
    


    private static boolean suspendGatewayServices(Context ctx, Subscriber sub, final Object service,
            boolean suspend)
    {
        boolean result = true;
        final Context subCtx = ctx.createSubContext();
        
        Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        if (oldSubscriber == null)
        {
            Home subHome = (Home) subCtx.get(SubscriberHome.class);
            try
            {
                oldSubscriber = (Subscriber) subHome.find(ctx, sub.getId());
                subCtx.put(Lookup.OLDSUBSCRIBER, oldSubscriber);
            }
            catch (HomeException homeEx)
            {
                new MinorLogMsg(SuspensionSupport.class, "Unable to find subscriber " +sub.getId(), homeEx).log(ctx);
                return false;
            }
        }
        
   
        if (suspend)
        {
            final SPGServiceProvisionCollector collector = new SPGServiceProvisionCollector();
            collector.newSub = sub;
            collector.oldSub = sub;
            subCtx.put(SPGServiceProvisionCollector.class, collector);
            subCtx.put(SUSPEND_GATEWAY_SERVICE, service);
            SPGAuxSvcExtension spgAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, (AuxiliaryService)service, SPGAuxSvcExtension.class);
            ServiceProvisioningGatewaySupport.updateSingleService(subCtx, sub, (ServiceBase)service, spgAuxSvcExtension.getSPGServiceType());
        }
        else
        {
            final SPGServiceProvisionCollector collector = new SPGServiceProvisionCollector();
            collector.newSub = sub;
            collector.oldSub = sub;
            subCtx.put(UNSUSPEND_GATEWAY_SERVICE, service);
            subCtx.put(SPGServiceProvisionCollector.class, collector);
            ServiceProvisioningGatewaySupport.updateSingleService(subCtx, sub, (ServiceBase)service, ((Service) service).getSPGServiceType());
        }
        return result;
    }


    /**
     * Suspend bundles of a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber whose bundles are being suspended.
     * @param map
     *            The map of bundles to be suspended. This is a map of (ID, BundleFee).
     * @param suspend
     *            Whether to suspend or unsuspend the bundles.
     */
    public static void suspendBundles(final Context ctx, final Subscriber sub, final Map map, final boolean suspend)
    {
        final Iterator it = map.values().iterator();
        
        StringBuilder details = new StringBuilder();
        details.append(suspend?"SUSPENSION":"UNSUSPENSION");
        details.append(": SubscriberId='");
        details.append(sub.getId());
        details.append("'");
        
        final CRMSubscriberBucketProfile bs = (CRMSubscriberBucketProfile) ctx.get(CRMSubscriberBucketProfile.class);

        if (bs != null)
        {
			while (it.hasNext())
			{
	            final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 bundle", details.toString());

	            final BundleFee bundleFee = (BundleFee) it.next();
				final BundleProfile bundle = bundleFromBundleFee(ctx, bundleFee, sub);
				if (bundle == null)
				{
					continue;
				}

				try
				{
					// sending enddate for suspend to active request 
					if(!suspend)
					{
						Date runningDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
						ctx.put(CURRENT_DATE, runningDate.getTime());
					}
					// If smart suspension is enabled, no prorate refund is done, and thus,
					// minutes should not be prorated and vice versa
					bs.updateBucketStatus(ctx, sub.getMSISDN(), sub.getSpid(), (int) sub.getSubscriptionType(),
					        bundle.getBundleId(), !suspend, !bundle.getSmartSuspensionEnabled() && !bundle.getRecurrenceScheme().isOneTime());
				}
				catch (final Exception e)
				{
					LogSupport.major(ctx, SuspensionSupport.class, "Problem occurred while suspending bundles for"
						+ " Prepaid subscriber " + sub.getId() + " with insufficient balance.", e);
				}

			pmLogMsg.log(ctx);
			}
		}
		else
		{
			LogSupport.major(ctx, SuspensionSupport.class, "No Service installed to call Bundle Manager ", null);
		}
    }

    
    public static int suspendHlrAuxService(Context ctx, Subscriber sub,
            SubscriberAuxiliaryService subAuxSrv, com.redknee.app.crm.bean.core.AuxiliaryService auxSrv, boolean suspend, Object caller)
    {
        

        try
        {
            if (suspend)
            {
                subAuxSrv.setProvisionAction(com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.SUSPEND);
                
                StateChangeAuxiliaryServiceSupport.unProvisionHlr(ctx, subAuxSrv, auxSrv, sub, caller);
               
               
             }
            else
            {
                subAuxSrv.setProvisionAction(com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.RESUME);
                StateChangeAuxiliaryServiceSupport.provisionHlr(ctx, subAuxSrv, auxSrv, sub, caller);
             }
            
            subAuxSrv.setProvisionActionState(true); 
        }
        catch (HomeException e)
        {
            subAuxSrv.setProvisionActionState(false); 
            LogSupport.debug(ctx, SuspendEntitiesVisitor.class.getName(),
                    "Problem occurred while deprovisioning Provisionable auxiliary service "
                    + auxSrv.getIdentifier()
                    + " for Prepaid subscriber " + sub.getId()
                    + " with insufficient balance.", e);
            return EXTERNAL_FAIL;
        } finally 
        {
            SubscriberAuxiliaryServiceSupport.updateSubscriberAuxiliaryServiceOnXDBHomeDirectly(ctx, subAuxSrv); 
        }
        return SUCCESS;
    }    

    public static int suspendGenericService(Context ctx, Subscriber sub,
            Service service, boolean suspend, boolean hlrOnly)
    {
        Context subCtx = ctx.createSubContext();
        subCtx.put(Service.class, service);
        subCtx.put(com.redknee.app.crm.bean.core.Service.class, service);
        subCtx.put(HLR_ONLY, hlrOnly);
        try
        {
            subCtx.put(Subscriber.class, sub);
            subCtx.put(Account.class, sub.getAccount(ctx));

            if (suspend)
            {
                new GenericSuspendAgent().execute(subCtx);
            }
            else
            {
                new GenericResumeAgent().execute(subCtx);
            }
            
            BlacklistWhitelistPLPSupport.suspendOrResume(ctx, sub, service, suspend);
        }
        catch (AgentException e)
        {
            
            LogSupport.debug(ctx, SuspendEntitiesVisitor.class.getName(),
                    "Problem occurred while (un)suspending generic service "
                    + service.getIdentifier()
                    + " for prepaid subscriber " + sub.getId()
                    + " with insufficient balance.", e);
            return EXTERNAL_FAIL;
        }
        catch ( Exception e )
        {
            LogSupport.debug(ctx, SuspendEntitiesVisitor.class.getName(),
                    "Problem occurred while (un)suspending generic service "
                    + service.getIdentifier()
                    + " for prepaid subscriber " + sub.getId()
                    + " with insufficient balance.", e);
            return EXTERNAL_FAIL;
        }
        
        return SUCCESS;
    }    

    
    public static int suspendWiMaxService(Context ctx, Subscriber sub,
            Service service, boolean suspend, boolean hlrOnly)
    {
        Context subCtx = ctx.createSubContext();
        subCtx.put(Service.class, service);
        subCtx.put(com.redknee.app.crm.bean.core.Service.class, service);
        subCtx.put(HLR_ONLY, hlrOnly);
        try
        {
            subCtx.put(Subscriber.class, sub);
            subCtx.put(Account.class, sub.getAccount(ctx));

            if (suspend)
            {
                new WimaxSuspendAgent().execute(subCtx);
            }
            else
            {
                new WimaxResumeAgent().execute(subCtx);
            }
            
        }
        catch (AgentException e)
        {
            
            LogSupport.debug(ctx, SuspendEntitiesVisitor.class.getName(),
                    "Problem occurred while (un)suspending wimax service "
                    + service.getIdentifier()
                    + " for  subscriber " + sub.getId(), e);
            return EXTERNAL_FAIL;
        }
        catch ( Exception e )
        {
            LogSupport.debug(ctx, SuspendEntitiesVisitor.class.getName(),
                    "Problem occurred while (un)suspending wimax service "
                    + service.getIdentifier()
                    + " for  subscriber " + sub.getId(),e);
                    
            return EXTERNAL_FAIL;
        }
        return SUCCESS;
    }    

    /**
     * Suspend voice mail service of a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param suspend
     *            Whether to suspend or unsuspend the service.
     * @param sub
     *            The subscriber whose voice mail service is being suspended.
     */
    public static int suspendVoiceMail(final Context ctx, final boolean suspend, final Subscriber sub,
        final ServiceBase service)
    {

        if (service instanceof Service && LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY)) 
        {   
           return suspendGenericService(ctx, sub, (Service)service, suspend, false); 
            
        } else 
        {

            final VoiceMailServer vmServer = (VoiceMailServer) ctx.get(VoiceMailServer.class);
            ExternalProvisionResult ret = null;

            if (suspend)
            {
                ret = vmServer.deactivate(ctx, sub, service);

            }
            else
            {
                ret = vmServer.activate(ctx, sub, service);

            }

            if (ret.getCrmVMResultCode() != VoiceMailConstants.RESULT_SUCCESS)
            {
                LogSupport.major(ctx, SuspensionSupport.class,
                        "Fail to update voice mail profile, ret = " + ret.getCrmVMResultCode(), null);
            }
        
        }
        return SUCCESS;
    }


    /**
     * Suspends the subscriber's services.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscrbier whose service are to be suspended.
     * @param map
     *            Map of services to be suspended. This is a map of (IDs, ServiceFee2).
     * @param suspend
     *            Whether to suspend or unsuspend services.
     */
    public static boolean suspendServices(final Context ctx, final Subscriber sub, final Map map, final boolean suspend)
    {
        return suspendServices(ctx, sub, map.values(), suspend);
    }
    
    public static boolean suspendServices(final Context ctx, final Subscriber sub, final Collection values, final boolean suspend)
    {
        boolean success = true;
        final Iterator it = values.iterator();
        
        StringBuilder details = new StringBuilder();
        details.append(suspend?"SUSPENSION":"UNSUSPENSION");
        details.append(": SubscriberId='");
        details.append(sub.getId());
        details.append("'");
        
        while (it.hasNext())
        {
            final PMLogMsg pmLogMsg;
            
            final ServiceFee2 serviceFee = (ServiceFee2) it.next();
            final Service service = serviceFromServiceFee(ctx, serviceFee, sub);
            if (service == null)
            {
                continue;
            }

            if (service.getType() == ServiceTypeEnum.VOICE)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 voice service", details.toString());
                // copied from SubscriberEcpProfileUpdateHome
                final AppEcpClient client = (AppEcpClient) ctx.get(AppEcpClient.class);
                int result = ErrorCode.SUBSCRIBER_NOT_FOUND;

                final int state;
                if (suspend)
                {
                    state = AppEcpClient.BARRED;
                }
                else
                {
                    state = AppEcpClient.ACTIVE;
                }
                result = client.updateSubscriberState(sub.getMSISDN(), state);
                if (result != ErrorCode.SUCCESS)
                {
                    success = false;
                    final String message = "provisioning result: failed to update ECP subscriber " + sub.getMSISDN()
                        + " [" + result + "]";
                    final Exception e = new ProvisioningHomeException(message, result, Common.OM_ECP_ERROR);
                    LogSupport.major(ctx, SuspensionSupport.class, "Problem occurred while suspending ECP service for"
                        + " Prepaid subscriber " + sub.getId() + " with insufficient balance.", e);
                }
            }
            else if (service.getType() == ServiceTypeEnum.SMS)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 SMS service", details.toString());
                final AppSmsbClient smsbClient = (AppSmsbClient) ctx.get(AppSmsbClient.class);
                if (smsbClient.enableSubscriber(sub.getMSISDN(), !suspend) != 0)
                {
                    success = false;
                }
            }
            else if (service.getType() == ServiceTypeEnum.DATA)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 data service", details.toString());
                final IpcgClient ipcgClient = IpcgClientFactory.locateClient(ctx, sub.getTechnology());
                try
                {
                    ipcgClient.setSubscriberEnabled(ctx, sub, !suspend);
                }
                catch (final IpcgSubProvException e)
                {
                    success = false;
                    LogSupport.major(ctx, SuspensionSupport.class, "Problem occurred while suspending IPCG service for"
                        + " Prepaid subscriber " + sub.getId() + " with insufficient balance.", e);
                }
            }
            else if (service.getType() == ServiceTypeEnum.VOICEMAIL)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 voicemail service", details.toString());
                success = success && (suspendVoiceMail(ctx, suspend, sub, service) == SUCCESS);
            }
            else if (service.getType() == ServiceTypeEnum.EVDO)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 EVDO service", details.toString());
                final AAAClient client = AAAClientFactory.locateClient(ctx);
                try
                {
                    if (suspend)
                    {
                        client.setProfileEnabled(ctx, sub, false);
                    }
                    else
                    {
                        client.setProfileEnabled(ctx, sub, true);
                    }
                }
                catch (AAAClientException e)
                {
                    success = false;
                    new MajorLogMsg(SuspendEntitiesVisitor.class.getName(),
                            "Problem occurred while suspending EVDO service for" + " Prepaid subscriber " + sub.getId()
                                    + " with insufficient balance. suspend "+suspend, e).log(ctx);
                }
            }
            else if (service.getType() == ServiceTypeEnum.BLACKBERRY)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 BlackBerry service", details.toString());
                try
                {
                    if (suspend)
                    {
                        new SuspendBlackberryServiceUpdateAgent().update(ctx, sub, service);
                    }
                    else
                    {
                        new ResumeBlackberryServiceUpdateAgent().update(ctx, sub, service);
                    }
                }
                catch (HomeException e)
                {
                    success = false;
                    LogSupport.major(ctx, SuspendEntitiesVisitor.class.getName(), 
                            "Problem occurred while suspending Blackberry service for" + " Prepaid subscriber " + sub.getId()
                            + " with insufficient balance. suspend " + suspend, e);
                }
            }
            else if (service.getType() == ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "Suspend service provisioning gateway", details.toString()); 
                ctx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                        String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_SERVICE));
                suspendGatewayServices(ctx, sub, service, suspend);                
            }
            else if (service.getType() == ServiceTypeEnum.URCS_PROMOTION)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "Suspend URCS Promotion", details.toString()); 
            	long serviceOption = URCSPromotionUnprovisionAgent.getURCSPromotionServiceOption(ctx, service.getID());
        		
        		if (serviceOption == -1)
        		{
        			LogSupport.major(ctx, SuspendEntitiesVisitor.class,"Unable to find service option for the Bolt on service");
        		}
        		try 
        		{
        			URCSPromotionAuxSvcExtension.provisionUrcsPromotion(ctx,
        					sub, serviceOption, !suspend);
        		} 
        		catch (ExtensionAssociationException ex) 
        		{
        			LogSupport.major(ctx, SuspendEntitiesVisitor.class,"Unable to (Un)-Suspend the urcs promption service");
        		}
            }            
            else if (service.getType() == ServiceTypeEnum.GENERIC)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 generic service", details.toString());
                suspendGenericService(ctx, sub, service, suspend, false);
                
            }
            else if (service.getType() == ServiceTypeEnum.EXTERNAL_PRICE_PLAN)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 external Priceplan service", details.toString());
                suspendGenericService(ctx, sub, service, suspend, false);
            }
            else if (service.getType() == ServiceTypeEnum.WIMAX)
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 Wimax  service", details.toString());
                suspendWiMaxService(ctx, sub, service, suspend, false);
            }
            else
            {
                pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 service", details.toString());
            }
            
            pmLogMsg.log(ctx);
            if(success && suspend)
            {
            	SubscriptionNotificationSupport.sendServiceStateChangeNotification(ctx, (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER), sub, service,null,NotificationTypeEnum.SERVICE_STATE_CHANGE,ServiceStateEnum.SUSPENDED);
            }
            else if(success && !suspend)
			{
            	SubscriptionNotificationSupport.sendServiceStateChangeNotification(ctx, (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER), sub, service,null,NotificationTypeEnum.SERVICE_STATE_CHANGE,ServiceStateEnum.PROVISIONED);
            }
            /* Here we do handle the ServiceType.ALCATEL_SSC here explicitly
             * because that service type is only applied to Postpaid subscriptions and not
             * to prepaid subscriptions.
             */
        }
        
        return success;
    }


    /**
     * Suspend packages of a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber whose packages are being suspended.
     * @param map
     *            The map of packages being suspended. This is a map of (ID,
     *            ServicePackage).
     * @param suspend
     *            Whether to suspend or unsuspend the packages.
     */
    public static void suspendPackages(final Context ctx, final Subscriber sub, final Map map, final boolean suspend)
    {
        final Iterator it = map.values().iterator();
        
        StringBuilder details = new StringBuilder();
        details.append(suspend?"SUSPENSION":"UNSUSPENSION");
        details.append(": SubscriberId='");
        details.append(sub.getId());
        details.append("'");

        while (it.hasNext())
        {
            final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "(Un)Suspend 1 package", details.toString());

            final ServicePackage pkg = (ServicePackage) it.next();
            final Map serviceMap = new HashMap();
            final Map bundleMap = new HashMap();

            final ServicePackageVersion ver = pkg.getCurrentVersion(ctx);

            final Map serviceFees = ver.getServiceFees();
            Iterator iter = serviceFees.values().iterator();
            while (iter.hasNext())
            {
                final ServiceFee2 serviceFee = (ServiceFee2) iter.next();
                serviceMap.put(XBeans.getIdentifier(serviceFee), serviceFee);
            }

            final Map bundleFees = ver.getBundleFees();
            iter = bundleFees.values().iterator();
            while (iter.hasNext())
            {
                final BundleFee bundleFee = (BundleFee) iter.next();
                bundleMap.put(XBeans.getIdentifier(bundleFee), bundleFee);
            }

            suspendServices(ctx, sub, serviceMap, suspend);
            suspendBundles(ctx, sub, bundleMap, suspend);

            pmLogMsg.log(ctx);
        }
    }
    

    public static final String SUSPEND_GATEWAY_SERVICE = "SUSPEND_GATEWAY_SERVICE";
    public static final String UNSUSPEND_GATEWAY_SERVICE = "UNSUSPEND_GATEWAY_SERVICE";
    public static final String CURRENT_DATE = "CURRENT_DATE";
    
    public static final String HLR_ONLY = "HLR_ONLY";
    public static final String PM_MODULE = "ServicesSuspension";

    
    public static final int SUCCESS = 0;
    public static final int EXTERNAL_FAIL = 1; 
    public static final int INTERNAL_FAIL = 2; 
    public static final int BOTH_FAIL = 3; 
    public static final int REMOVED = 4; 
}
