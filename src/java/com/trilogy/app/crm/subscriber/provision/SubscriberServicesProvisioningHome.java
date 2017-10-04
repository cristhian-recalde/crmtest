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
package com.trilogy.app.crm.subscriber.provision;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bas.recharge.SuspensionSupport;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCltc;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.cltc.ServiceCreditLimitCheckSupport;
import com.trilogy.app.crm.cltc.SubCltcOperationCode;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.provision.ProvisioningSupport;
import com.trilogy.app.crm.provision.SubscriberForServiceVO;
import com.trilogy.app.crm.subscriber.provision.calculation.CompoundServicesProvisioningCalculator;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;


/**
 * This home is responsible for subscriber services provisioning, unprovisioning,
 * suspending, resuming, unsuspending (in case of prepaid) and update during a subscriber
 * update.
 * This class relies on the calculation done by SubscriberServicesProvisioningCalculator
 * to decide which action should be performed to which services.
 * 
 * @author Marcio Marques
 * @since 8_6
 * 
 */
public class SubscriberServicesProvisioningHome  extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * There are some circular referencing, like deposit home/transaction, it
     * will call
     * into subscriber home store again.
     */
    public static final String CONTEXT_KEY_HOMEVISITINGCNT =
        "com.redknee.app.crm.home.sub.visitingcnt.";

    /**
     * Create a new instance of <code>SubscriberServicesProvisioningHome</code>.
     * 
     * @param ctx
     *            The operating context.
     * @param delegate
     *            Delegate of this home.
     */
    public SubscriberServicesProvisioningHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj)
        throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	final Subscriber newSubscriber = (Subscriber) obj;
        final Subscriber oldSubscriber = null;
        Context subCtx = ctx.createSubContext();

        try
        {
            CompoundServicesProvisioningCalculator calculator = new CompoundServicesProvisioningCalculator(oldSubscriber, newSubscriber);
            calculator.calculate(subCtx);
            
            final Map<ExternalAppEnum, ProvisionAgentException> provisionResultCodes =
                provisionServices(subCtx, newSubscriber, null, calculator.getServicesToProvision(subCtx));
            
            parseProvisioningResult(subCtx, provisionResultCodes, oldSubscriber, newSubscriber);
            
            final Map<ExternalAppEnum, ProvisionAgentException> suspendResultCodes =
                suspendServices(subCtx, newSubscriber, null, calculator.getServicesToSuspend(subCtx));
            
            parseProvisioningResult(subCtx, suspendResultCodes, oldSubscriber, newSubscriber);
            
            if (LogSupport.isEnabled(subCtx, SeverityEnum.INFO))
            {
                StringBuilder sb = createLogHeader("Subscriber services provisioning ended.", newSubscriber, true);
                appendServicesListToLog(sb, "Provisioned services", calculator.getServicesToProvision(subCtx), true);
                appendServicesListToLog(sb, "Suspended services", calculator.getServicesToSuspend(subCtx), false);
                LogSupport.info(subCtx, this, sb.toString(), null);
            }
        }
        catch (final HomeException exception)
        {
            if (exception instanceof ProvisioningHomeException)
            {
                final ProvisioningHomeException phe = (ProvisioningHomeException) exception;
                SubscriberProvisionResultCode.setProvisionLastResultCode(subCtx, phe.getResultCode());
            }
            SubscriberProvisionResultCode.addException(subCtx, "Provision Services Exception, " + exception.getMessage(),
                    exception, oldSubscriber, newSubscriber);
        }
        finally
        {
            markSubscriberHomeVisited(subCtx, newSubscriber);
        }
        
        return super.create(subCtx, newSubscriber);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj)
        throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
        final Subscriber newSubscriber = (Subscriber) obj;
        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final Context subCtx = ctx.createSubContext();

       
            try
            {
                if (!hasSubscriberHomeVisited(subCtx, newSubscriber))
                {
                    if (LogSupport.isDebugEnabled(subCtx))
                    {
                        StringBuilder sb = createLogHeader("Subscriber services provisioning", newSubscriber, true);
                        appendServicesListToLog(sb, "Selected services", newSubscriber.getServices(subCtx), true);
                        appendServicesListToLog(sb, "Selected provisioned services backup", 
                                newSubscriber.getProvisionedServicesBackup(subCtx).keySet(), true);
                        appendServicesListToLog(sb, "Services already provisioned", 
                                newSubscriber.getProvisionedServices(subCtx), false);
                        LogSupport.debug(subCtx, this, sb.toString(), null);
                    }

                    final SubscriberForServiceVO subSvcVO =
                        new SubscriberForServiceVO(subCtx, oldSubscriber, newSubscriber,
                            SubscriberForServiceVO.CALLED_FROM_STORE);
                    
                    if (isSubscriberStateChange(oldSubscriber, newSubscriber) || 
                            isMSISDNChange(oldSubscriber, newSubscriber) ||
                            isSubscriberConversion(oldSubscriber, newSubscriber))
                    {
                        subCtx.put(SubscriberForServiceVO.class, subSvcVO);
                    }
                    
                    CompoundServicesProvisioningCalculator calculator = new CompoundServicesProvisioningCalculator(oldSubscriber, newSubscriber);
                    
                    calculator.calculate(subCtx);
                    
                    updateServices(subCtx, oldSubscriber, newSubscriber, calculator.getServicesToUpdate(subCtx));
                    
                    unsuspendServices(subCtx, oldSubscriber, newSubscriber, calculator.getServicesToUnsuspend(subCtx));

                    // Unprovision services
                    final Map<ExternalAppEnum, ProvisionAgentException> unprovisionResultCodes =
                        unprovisionServices(subCtx, oldSubscriber, newSubscriber, calculator.getServicesToUnprovision(subCtx));

                    // Provision services
                    final Map<ExternalAppEnum, ProvisionAgentException> provisionResultCodes =
                        provisionServices(subCtx, newSubscriber, oldSubscriber, calculator.getServicesToProvision(subCtx));
                    
                    // Resume services
                    final Map<ExternalAppEnum, ProvisionAgentException> resumeResultCodes =
                        resumeServices(subCtx, newSubscriber, oldSubscriber, calculator.getServicesToResume(subCtx));
        
                    // Creating notes for CLCT changes.
                    if (isCLTCOperation(oldSubscriber, newSubscriber) || isCLTCSuspended(subCtx, newSubscriber, calculator.getServicesToSuspend(subCtx)))
                    {
                        ServiceCreditLimitCheckSupport.createSubscriberNoteCltcProvision(subCtx, newSubscriber, calculator.getServicesToResume(subCtx),
                                calculator.getServicesToSuspend(subCtx));
                        
                        if (isCLTCSuspended(subCtx, newSubscriber, calculator.getServicesToSuspend(subCtx)))
                        {
                            if (LogSupport.isDebugEnabled(subCtx) && calculator.getServicesToSuspend(subCtx)!=null && calculator.getServicesToSuspend(subCtx).size()>0)
                            {
                                StringBuilder sb = createLogHeader("CLCT suspension required", newSubscriber, true);
                                appendServicesListToLog(sb, "Services to be suspended", calculator.getServicesToSuspend(subCtx), false);
                                LogSupport.debug(subCtx, this, sb.toString(), null);
                            }

                            // Update those Subscriber Service records to suspended due to CLTC
                            ServiceCreditLimitCheckSupport.changeServiceStateDueToCLTC(subCtx, newSubscriber, calculator.getServicesToSuspend(subCtx));
                        }
                    }
                    

                    parseProvisioningResult(subCtx, unprovisionResultCodes, oldSubscriber, newSubscriber);

                    parseProvisioningResult(subCtx, provisionResultCodes, oldSubscriber, newSubscriber);

                    parseProvisioningResult(subCtx, resumeResultCodes, oldSubscriber, newSubscriber);

                    if (isSubscriberStateChange(oldSubscriber, newSubscriber))
                    {
                        performVoicemailStateChange(subCtx, oldSubscriber, newSubscriber);
                    }
                    
                    if (LogSupport.isEnabled(subCtx, SeverityEnum.INFO))
                    {
                        StringBuilder sb = createLogHeader("Subscriber services provisioning successfully ended.", newSubscriber, true);
                        appendServicesListToLog(sb, "Updated services", calculator.getServicesToUpdate(subCtx), true);
                        appendServicesListToLog(sb, "Unsuspended services", calculator.getServicesToUnsuspend(subCtx), true);
                        appendServicesListToLog(sb, "Unprovisioned services", calculator.getServicesToUnprovision(subCtx), true);
                        appendServicesListToLog(sb, "Provisioned services", calculator.getServicesToProvision(subCtx), true);
                        if (isCLTCSuspended(subCtx, newSubscriber, calculator.getServicesToSuspend(subCtx)))
                        {
                            appendServicesListToLog(sb, "Resumed services", calculator.getServicesToResume(subCtx), true);
                            appendServicesListToLog(sb, "Suspended services", calculator.getServicesToSuspend(subCtx), false);
                        }
                        else
                        {
                            appendServicesListToLog(sb, "Resumed services", calculator.getServicesToResume(subCtx), false);
                        }
                        LogSupport.info(subCtx, this, sb.toString(), null);
                    }
                }
            }
            catch (final HomeException exception)
            {
                if (LogSupport.isDebugEnabled(subCtx))
                {
                    StringBuilder sb = createLogHeader("Subscriber services provisioning ended with exception", newSubscriber, false);
                    sb.append("Exception: ");
                    sb.append(exception.getMessage());
                    LogSupport.debug(subCtx, this, sb.toString(), exception);
                }

                if (exception instanceof ProvisioningHomeException)
                {
                    final ProvisioningHomeException phe = (ProvisioningHomeException) exception;
                    SubscriberProvisionResultCode.setProvisionLastResultCode(subCtx, phe.getResultCode());
                }
                SubscriberProvisionResultCode.addException(subCtx, "Provision Services Exception, "
                        + exception.getMessage(), exception, oldSubscriber, newSubscriber);
            }
            finally
            {
                markSubscriberHomeVisited(subCtx, newSubscriber);
            }

        return super.store(subCtx, obj);

    }
    

    /**
     * Update subscriber service records in database.
     * @param ctx
     * @param oldSub
     * @param newSub
     * @param services
     * @throws HomeException
     */
    private void updateServices(final Context ctx, final Subscriber oldSub, final Subscriber newSub, final Collection<SubscriberServices> services)
            throws HomeException
    {
        for (final SubscriberServices service : services)
        {
            // Update record.
            SubscriberServicesSupport.updateSubscriberServiceRecord(ctx, service);
        }
    }

    
    /**
     * Unsuspend subscriber services.
     * @param ctx
     * @param oldSub
     * @param newSub
     * @param services
     * @throws HomeException
     */
    private void unsuspendServices(final Context ctx, final Subscriber oldSub, final Subscriber newSub, final Collection<SubscriberServices> services)
    throws HomeException
    {
        Collection<ServiceFee2> unsuspendedServices = new HashSet<ServiceFee2>();
        for (final SubscriberServices service : services)
        {
                Service svc = service.getService(ctx);
                service.setProvisionedState(ServiceStateEnum.PROVISIONED);
                unsuspendedServices.add((ServiceFee2) oldSub.getSuspendedServices(ctx).get(XBeans.getIdentifier(svc)));
                oldSub.getSuspendedServices(ctx).remove(XBeans.getIdentifier(svc));
                newSub.getSuspendedServices(ctx).remove(XBeans.getIdentifier(svc));
                SuspendedEntitySupport.removeSuspendedEntity(ctx, oldSub.getId(), svc.getID(),
                        SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, ServiceFee2.class);
        }
        // Unsuspend services.
        SuspensionSupport.suspendServices(ctx, oldSub, unsuspendedServices, false);
    }  
    

    /**
     * Resume subscriber services.
     * @param ctx
     * @param subscriber
     * @param refSubscriber
     * @param services
     * @return
     * @throws HomeException
     */
    private Map<ExternalAppEnum, ProvisionAgentException> resumeServices(final Context ctx, final Subscriber subscriber,
            final Subscriber refSubscriber, final Collection<SubscriberServices> services) throws HomeException
    {
        final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();

        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = createLogHeader("Resuming subscriber services", subscriber, true);
            appendServicesListToLog(sb, "Services to be resumed", 
                    services, true);
            LogSupport.debug(ctx, this, sb.toString(), null);
        }

        SubscriberServicesSupport.resumeSubscriberServices(ctx, refSubscriber, 
                new ArrayList<SubscriberServices>(services), 
                subscriber, resultCodes);
        
        return resultCodes;
    } 
    

    /**
     * Unprovision subscriber services.
     * @param ctx
     * @param subscriber
     * @param refSubscriber
     * @param services
     * @return
     * @throws HomeException
     */
    private Map<ExternalAppEnum, ProvisionAgentException> unprovisionServices(final Context ctx, final Subscriber subscriber,
            final Subscriber refSubscriber, final Collection<SubscriberServices> services) throws HomeException
    {
        final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();
        
        if(SubscriberProvisionHlrGatewayHome.isIntentToRemoveMsisnFromHlrGateway(ctx))
        {
            String msg = MessageFormat.format(
                "Skipping 'Services Unprovision HLR Commands' for subscriber {0}; ProvisionCommand Name: {1}; oldMsisdn: {2}; newMsisdn: {3}", 
                    new Object[]{subscriber.getId(), 
                        SubscriberProvisionHlrGatewayHome.getProvisionCommandName(ctx),
                            subscriber.getMsisdn(), refSubscriber.getMsisdn()});
            LogSupport.info(ctx, this, msg);
            
            return resultCodes;
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = createLogHeader("Unprovisioning subscriber services", subscriber, true);
            appendServicesListToLog(sb, "Services to be unprovision", 
                    services, true);
            LogSupport.debug(ctx, this, sb.toString(), null);
        }

        SubscriberServicesSupport.unprovisionSubscriberServices(ctx, subscriber, 
                new ArrayList<SubscriberServices>(services), 
                refSubscriber, resultCodes);
        
        return resultCodes;
    }    

    
    /**
     * Provision subscriber services.
     * @param ctx
     * @param subscriber
     * @param refSubscriber
     * @param services
     * @return
     * @throws HomeException
     */
    private Map<ExternalAppEnum, ProvisionAgentException> suspendServices(final Context ctx, final Subscriber subscriber,
            final Subscriber refSubscriber, final Collection<SubscriberServices> services) throws HomeException
    {
        final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();

        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = createLogHeader("Suspending subscriber services", subscriber, true);
            appendServicesListToLog(sb, "Services to be suspended", 
                    services, true);
            LogSupport.debug(ctx, this, sb.toString(), null);
        }

        SubscriberServicesSupport.suspendSubscriberServices(ctx, refSubscriber, 
                new ArrayList<SubscriberServices>(services), 
                subscriber, resultCodes);
        
        return resultCodes;
    }    
    /**
     * Provision all services.
     * @param ctx
     * @param subscriber
     * @return
     * @throws HomeException
     */
    private Map<ExternalAppEnum, ProvisionAgentException> provisionServices(final Context ctx, final Subscriber subscriber,
            final Subscriber refSubscriber, final Collection<SubscriberServices> services) throws HomeException
    {
        final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();

        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = createLogHeader("Provisioning subscriber services", subscriber, true);
            appendServicesListToLog(sb, "Services to be provisioned", 
                    services, true);
            LogSupport.debug(ctx, this, sb.toString(), null);
        }

        SubscriberServicesSupport.provisionSubscriberServices(ctx, refSubscriber, 
                new ArrayList<SubscriberServices>(services), 
                subscriber, resultCodes);
        
        return resultCodes;
    }    
            
 


    
    /**
     * Check if subscriber is CLTC suspended.
     * @param oldSub
     * @param newSub
     * @return
     */
    private boolean isCLTCSuspended(Context ctx, final Subscriber newSub, Collection<SubscriberServices> suspendServices)
    {
    	
    	Map<Long, Boolean> serviceTypeExtensionActionFlag = (Map<Long, Boolean>)ctx.get(Common.SERVICETYPE_EXTENSION_ACTION_FLAG);
    	if(serviceTypeExtensionActionFlag != null && suspendServices != null)
    	{
    		SubscriberCltc subscriberCltc = (SubscriberCltc) ctx.get(Common.ER_447_SUBSCRIBER_CLCT);
    		Iterator<SubscriberServices> itr = suspendServices.iterator();
        	while(itr.hasNext())
        	{
        		SubscriberServices service = itr.next();
        		
        		if(serviceTypeExtensionActionFlag.get(service.getServiceId()) == null)
        		{
        			break;
        		}
        		boolean flipActionFlag = serviceTypeExtensionActionFlag.get(service.getServiceId());
        		if( newSub.isClctChange() 
        				&& (subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_TOPPED 
                		|| subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_PROVISIONED) 
                		&& flipActionFlag)
            	{
        			return true;
        		}
        	}
    	}
//    	This condition was added to suspend the service if initial balance = final balance = 0 & operation is 11 or 8
    	if(newSub.isClctChange() 
    			&& (newSub.getCLCTOpertionType() == SubCltcOperationCode.BUNDLE_BALANCE_DECREASED 
    			||  newSub.getCLCTOpertionType() == SubCltcOperationCode.BUNDLE_BALANCE_DEPLETED))
    	{
    			 return true;
    	}
    	 
    	return newSub.isClctChange() ? newSub.getSubNewBalance() < newSub.getSubOldBalance() : false;
        
    }

    
    /**
     * Check if we're performing a CLTC operation.
     * @param oldSub
     * @param newSub
     * @return
     */
    private boolean isCLTCOperation(final Subscriber oldSub, final Subscriber newSub)
    {
        return newSub.isClctChange();
    }
    


    
    /**
     * Parse the provisioning result
     * @param ctx
     * @param resultCodes
     * @param oldSub
     * @param newSub
     */
    private static void parseProvisioningResult(final Context ctx,
            final Map<ExternalAppEnum, ProvisionAgentException> resultCodes, final Subscriber oldSub, final Subscriber newSub)
    {
        int lastResult = 0;
        
        for (ProvisionAgentException exception : resultCodes.values())
        {
            lastResult = logProvisioningExceptions(ctx, exception, oldSub, newSub);
        }
        
        if (lastResult>0)
        {
            SubscriberProvisionResultCode.setProvisionLastResultCode(ctx, lastResult);
        }
    }
    
 
    private static String getOMMeasurementName(final ExternalAppEnum externalApp)
    {
        String result = Common.OM_CRM_PROV_ERROR;
        
        if (ExternalAppEnum.SMS.equals(externalApp))
        {
            result = Common.OM_SMSB_ERROR;
        }
        else if (ExternalAppEnum.VOICE.equals(externalApp))
        {
            result = Common.OM_ECP_ERROR;
        }
        else if (ExternalAppEnum.DATA.equals(externalApp))
        {
            result = Common.OM_IPC_ERROR;
        }
        else if (ExternalAppEnum.HLR.equals(externalApp))
        {
            result = Common.OM_HLR_PROV_ERROR;
        }

        return result;
    }
    
    
    /**
     * Set the provision error code for the given key.
     * @param ctx
     * @param resultCode
     * @param key
     */
    private static void setProvisionErrorCode(final Context ctx, final int resultCode, final ExternalAppEnum externalApp)
    {
        if (ExternalAppEnum.SMS.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionSMSBErrorCode(ctx, resultCode);
        }
        else if (ExternalAppEnum.VOICE.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionEcpErrorCode(ctx, resultCode);
        }
        else if (ExternalAppEnum.DATA.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionIpcErrorCode(ctx, resultCode);
        }
        else if (ExternalAppEnum.BLACKBERRY.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionBlackberryErrorCode(ctx, resultCode);
        }
        else if (ExternalAppEnum.ALCATEL_SSC.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionAlcatelErrorCode(ctx, resultCode);
        }
        else if (ExternalAppEnum.HLR.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionHlrResultCode(ctx, resultCode);
        }
    }
    
    
    /**
     * Return module name.
     * @param key
     * @return
     */
    private static String getModuleName(final ExternalAppEnum externalApp)
    {
        String result = "UNKNOWN";
        if (ExternalAppEnum.SMS.equals(externalApp))
        {
            result = "URCS";
        }
        else if (ExternalAppEnum.VOICE.equals(externalApp))
        {
            result = "URCS";
        }
        else if (ExternalAppEnum.DATA.equals(externalApp))
        {
            result = "NGRC";
        }
        else if (ExternalAppEnum.BLACKBERRY.equals(externalApp))
        {
            result = "RIM Blackberry Provisioning System";
        }
        else if (ExternalAppEnum.HLR.equals(externalApp))
        {
            result = "HLR";
        }
        else if (ExternalAppEnum.VOICEMAIL.equals(externalApp))
        {
            result = "Voicemail server";
        }
        else if (ExternalAppEnum.ALCATEL_SSC.equals(externalApp))
        {
            result = "Alcatel SSC";
        }

        return result;
    }
    

    /**
     * Return module name.
     * @param key
     * @return
     */
    private static String getServiceTypeName(final ExternalAppEnum externalApp)
    {
        String result = "";
        if (ExternalAppEnum.SMS.equals(externalApp))
        {
            result = "SMS ";
        }
        else if (ExternalAppEnum.VOICE.equals(externalApp))
        {
            result = "voice ";
        }
        else if (ExternalAppEnum.DATA.equals(externalApp))
        {
            result = "data ";
        }
        else if (ExternalAppEnum.BLACKBERRY.equals(externalApp))
        {
            result = "Blackberry ";
        }
        else if (ExternalAppEnum.HLR.equals(externalApp))
        {
            result = "";
        }
        else if (ExternalAppEnum.VOICEMAIL.equals(externalApp))
        {
            result = "voicemail ";
        }
        else if (ExternalAppEnum.ALCATEL_SSC.equals(externalApp))
        {
            result = "";
        }

        return result;
    }
    

    /**
     * Put the ProvisionAgentException in the GUI, and generate an OM Log Message for it.
     * @param ctx
     * @param exception
     * @param oldSub
     * @param newSub
     * @return
     */
    private static int logProvisioningExceptions(final Context ctx, final ProvisionAgentException exception,
            final Subscriber oldSub, final Subscriber newSub)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Failed to provision ");
        sb.append(getServiceTypeName(exception.getExternalApp()));
        sb.append("service to ");
        sb.append(getModuleName(exception.getExternalApp()));
        sb.append(": ");
        sb.append(exception.getMessage());
        
        SubscriberProvisionResultCode.addException(ctx, sb.toString(), exception, oldSub, newSub);
        
        int result = exception.getSourceResultCode();
        
        if (result!=0)
        {
            setProvisionErrorCode(ctx, result, exception.getExternalApp());
            result = exception.getResultCode();
            String measurementName = getOMMeasurementName(exception.getExternalApp());
            new OMLogMsg(Common.OM_MODULE, measurementName).log(ctx);
        }
        
        return result;
    }
    


    /**
     * Performs state changes for voicemail service.
     * 
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     */
    public void performVoicemailStateChange(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        final Collection<SubscriberServices> provionedService;
        /*
         * Fix for TT5120827998 - update voicemail state for both PREPAID AND POSTPAID
         */
        /* if(newSub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID)) { */
        /*
         * Fix for TT6012429652 - Deactivation does not delete voicemail on Mpathix WS:
         * The last || is added to explicitly avoid unprovisioned being called when
         * reactivating a subscriber BUT at the same time allow unprovision to be called
         * when a subscriber is deactivated
         */
        if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, DEACTIVATION_STATES, ACTIVATION_STATES)
                || EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, ACTIVATION_STATES, DEACTIVATION_STATES)
                || EnumStateSupportHelper.get(ctx).isNotOneOfStates(oldSub, UNPROVISION_STATES)
                && EnumStateSupportHelper.get(ctx).isOneOfStates(newSub, UNPROVISION_STATES))
        {
            provionedService = newSub.getProvisionedSubscriberServices(ctx);

            for (final SubscriberServices service : provionedService)
            {
                if (SafetyUtil.safeEquals(service.getService(ctx).getType(),ServiceTypeEnum.VOICEMAIL))
                {
                    if (EnumStateSupportHelper.get(ctx).isNotOneOfStates(oldSub, UNPROVISION_STATES)
                            && EnumStateSupportHelper.get(ctx).isOneOfStates(newSub, UNPROVISION_STATES))
                    {
                        final Context subCtx = ctx.createSubContext();
                        subCtx.put(SubscriberForServiceVO.class, null);
                        List<SubscriberServices> unprovServices = new ArrayList<SubscriberServices>();
                        unprovServices.add(service);
                        try
                        {
                            SubscriberServicesSupport.unprovisionSubscriberServices(ctx, oldSub, unprovServices, newSub,
                                    new HashMap());
                        }
                        catch (final HomeException exp)
                        {
                            if (exp instanceof ProvisioningHomeException)
                            {
                                final ProvisioningHomeException phe = (ProvisioningHomeException) exp;
                                SubscriberProvisionResultCode.setProvisionLastResultCode(ctx, phe.getResultCode());
                            }
                            SubscriberProvisionResultCode.addException(ctx, "Provision Services Exception, "
                                    + exp.getMessage(), exp, oldSub, newSub);
                        }
                    }
                    else
                    {
                        callVoicemailUnprovisionAgent(ctx, oldSub, newSub, service.getService(ctx));
                    }
                    /*
                     * break as only one voicemail service is allowed per subscriber
                     */
                    break;
                }
            }
        }
    }
    

    /**
     * Performs state changes for voicemail service.
     * 
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     */
    public void performVoicemailReactivation(final Context ctx, final Subscriber subscriber, final Service service)
    {
        if (service.getType().equals(ServiceTypeEnum.VOICEMAIL))
        {
            SuspensionSupport.suspendVoiceMail(ctx, false, subscriber, service);
        }
    }
    
    
    /**
     * Calls voicemail unprovision agent.
     * 
     * @param ctx
     *            The operating context.
     * @param oldsub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     * @param service
     *            The voicemail service.
     */
    private void callVoicemailUnprovisionAgent(final Context ctx, final Subscriber oldsub, final Subscriber newSub,
            final Service service)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "provision the following service for subscriber " + newSub.getMSISDN() + ": "
                    + service.toString(), null).log(ctx);
        }
        ctx.put(Subscriber.class, newSub);
        ctx.put(Service.class, service);
        try
        {
            // can not call unprovisionService directly
            ProvisioningSupport.executeServiceProvisionHandler("Unprovision", newSub, service,
                    ProvisioningSupport.SERVICE_PACKAGE, ctx);
        }
        catch (final AgentException ae)
        {
            final HomeException he = new HomeException(
                    "Special unprovisioning for voicemail due to state change(Prepaid account)");
            he.initCause(ae);
            // suppressing
        }
    }
    

    /**
     * Returns the context key for subscriber visiting count.
     * 
     * @param sub
     *            The subscriber being processed.
     * @return The context key for subscriber visiting count.
     */
    static public String getSubscriberVisitingCountContextKey(
        final Subscriber sub)
    {
        return CONTEXT_KEY_HOMEVISITINGCNT + sub.getId();
    }

    
    /**
     * Marks the subscriber as visited.
     * 
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being processed.
     */
    void markSubscriberHomeVisited(final Context ctx, final Subscriber sub)
    {
        ctx.put(getSubscriberVisitingCountContextKey(sub), true);
    }

    
    /**
     * Determines whether the subscriber has been visited. This is required
     * because
     * subscriber pipeline, account pipeline, and transaction pipeline may end
     * up updating
     * each other.
     * 
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being processed.
     * @return Returns <code>true</code> if the subscriber has already been
     *         visited by
     *         this home, <code>false</code> otherwise.
     */
    static public boolean hasSubscriberHomeVisited(final Context ctx,
        final Subscriber sub)
    {
        return ctx.getBoolean(getSubscriberVisitingCountContextKey(sub), false);
    }

    
    private boolean isMSISDNChange(final Subscriber oldSubscriber, final Subscriber newSubscriber)
    {
        return oldSubscriber!=null && newSubscriber!=null && 
        (!SafetyUtil.safeEquals(oldSubscriber.getMSISDN(),newSubscriber.getMSISDN()));
    }
    
    
    private boolean isSubscriberConversion(final Subscriber oldSubscriber, final Subscriber newSubscriber)
    {
        return oldSubscriber!=null && newSubscriber!=null && 
        (!SafetyUtil.safeEquals(oldSubscriber.getSubscriberType(),newSubscriber.getSubscriberType()));
    }


    private boolean isSubscriberStateChange(final Subscriber oldSubscriber, final Subscriber newSubscriber)
    {
        return oldSubscriber!=null && newSubscriber!=null && 
        (!SafetyUtil.safeEquals(oldSubscriber.getState(),newSubscriber.getState()));
    }
    
    

    
    private StringBuilder createLogHeader(final String description, final Subscriber subscriber, final boolean appendComma)
    {
        StringBuilder sb = new StringBuilder(description);
        sb.append(": ");
        if (subscriber.isSubscriberIdSet())
        {
            sb.append("SubscriberId = '");
            sb.append(subscriber.getId());
            sb.append("', ");
        }
        sb.append("MSISDN = '");
        sb.append(subscriber.getMSISDN());
        sb.append("', BAN = '");
        sb.append(subscriber.getBAN());
        if (appendComma)
        {
            sb.append("', ");
        }
        else
        {
            sb.append("'.");
        }
        return sb;
    }


    private void appendServicesListToLog(final StringBuilder sb, final String name, final Collection services, final boolean appendComma)
    {
        sb.append(name);
        sb.append(" = '");
        sb.append(ServiceSupport.getServiceIdString(services));
        if (appendComma)
        {
            sb.append("', ");
        }
        else
        {
            sb.append("'.");
        }
    }


    /**
     * If the subscriber is going into any of these states, deactivation should be
     * performed.
     */
    private static final Collection<SubscriberStateEnum> DEACTIVATION_STATES = Arrays.asList(
            SubscriberStateEnum.SUSPENDED, SubscriberStateEnum.LOCKED);
    
    /**
     * If the subscriber is going into any of these states, activation should be
     * performed.
     */
    private static final Collection<SubscriberStateEnum> ACTIVATION_STATES = Arrays.asList(SubscriberStateEnum.ACTIVE,
            SubscriberStateEnum.PROMISE_TO_PAY);
    
    /**
     * If the subscriber is going into any of these states, unprovision should be
     * performed.
     */
    private static final Collection<SubscriberStateEnum> UNPROVISION_STATES = Arrays.asList(
            SubscriberStateEnum.PENDING, SubscriberStateEnum.INACTIVE);
    
    /**
     * Postpaid subscriber state change from other states to these states, unprovision
     * charge will apply, vice versa.
     */
    private static final Collection<SubscriberStateEnum> POSTPAID_UNPROVISION_STATES = Arrays.asList(
            SubscriberStateEnum.PENDING, SubscriberStateEnum.INACTIVE);
    
    /**
     * True prepaid subscriber state change from other states to these states, unprovision
     * charge will apply, vice versa. For Buzzard prepaid mode, even inactive state we
     * need services provisioned.
     */
    private static final Collection<SubscriberStateEnum> PREPAID_UNPROVISION_STATES = Arrays.asList(
            SubscriberStateEnum.PENDING, SubscriberStateEnum.INACTIVE);
    
}
