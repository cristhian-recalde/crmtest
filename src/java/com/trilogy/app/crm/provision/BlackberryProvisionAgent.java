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
package com.trilogy.app.crm.provision;

import java.util.Arrays;

import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.blackberry.error.ErrorHandler;
import com.trilogy.app.crm.blackberry.error.RIMBlackBerryErrorCodes;
import com.trilogy.app.crm.state.SuspendBlackberryServiceUpdateAgent;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.service.blackberry.IServiceBlackberry;
import com.trilogy.service.blackberry.ServiceBlackberryException;
import com.trilogy.service.blackberry.ServiceBlackberryFactory;
import com.trilogy.service.blackberry.model.Attributes;

/**
 * This Provisioning Agent provisions the Subscriber and its services to the BlackBerry
 * Provisioning Server and sends the HLR command to activate the service on the HLR.
 * 
 * In some instances it will be necessary to synchronize the BlackBerry Provisioning 
 * System's profile with CRM's Subscriber/Subscriber Service's state.
 *  
 * @author angie.li
 *
 */
public class BlackberryProvisionAgent extends CommonProvisionAgent 
{
    public BlackberryProvisionAgent()
    {
        handler_ = BlackberrySupport.getErrorHandler();
    }

    /**
     * Installs BlackBerry services first to the BlackBerry System, then to the HLR.
     * Context must contain the subscriber to be installed keyed by Subscriber.class
     * Context must contain Service to retrieve additional params needed associated with this service
     * Context must contain IServiceBlackberry to provision to the BlackBerry Provisioning System
     * @param ctx
     * @throws AgentException
     */
    public void execute(Context ctx) throws AgentException 
    {
        Subscriber subscriber = null;
        Service service = null;
        String hlrCmds = null;    // to Provision APN to HLR

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Begin validation of BlackBerry Service Parameters.");
        }
        // Begin validation of parameters
        subscriber = (Subscriber)ctx.get(Subscriber.class, null);
        if (subscriber == null)
        {
            throw new AgentException("Developer error: No subscriber to provision");
        }

        service = (Service)ctx.get(Service.class, null);
        if (service == null)
        {
            throw new AgentException("Developer error: Service for BlackBerry provisioning not found in context");
        }
        else
        {
            if (subscriber.isPostpaid())
            {
                hlrCmds = service.getProvisionConfigs();
            }
            else if (subscriber.isPrepaid())
            {
                hlrCmds = service.getPrepaidProvisionConfigs();
            }
        }

        if ((hlrCmds == null) || (hlrCmds.length() == 0))
        {
            throw new AgentException("Configuration error: BlackBerry HLR commands for are missing for provisioning");
        }
        
        //Begin validation against provisioning for subscribers in Available and Pending states
        if (subscriber.getState().equals(SubscriberStateEnum.AVAILABLE) || 
                subscriber.getState().equals(SubscriberStateEnum.PENDING) )
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, 
                    "Skipping provisioning of BlackBerry Service because the Subscriber is in the Available/Pending state.  " +
                    "The Service will be provisioned (activated) on the BlackBerry Provisioning System when the Subscriber " +
                    "is Activated in CRM. Sub=" + subscriber.getId());
            }
            throw new SkipProvisioningException(this, subscriber.getId(), service.getIdentifier() +  " - " + service.getName(),
                    "Subscriber has not been activated in CRM.");
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Attempting BlackBerry provisioning with the following parameters:"
                    + " Subscriber ID= " + subscriber.getId()
                    + ", CRM Service=[ " + service.getID()
                    + "], HLR cmds=[" + hlrCmds + "]");
        }
        
        // Provision to the BlackBerry Provisioning System
        provisionServiceOnBlackBerrySystem(ctx, subscriber, service, hlrCmds);
        
        // Provisiong to HLR
        // Any exceptions that occur during BlackBerry Provisioning (previous step) will abort HLR provisioning
        // execute HLR commands using HLR client
        callHlr(ctx, true,subscriber, service,null,null);

        if (BlackberrySupport.areBlackberryServicesProvisionedToIPC(ctx))
        {
            // Provision to the BlackBerry Provisioning System
            provisionServiceOnIPC(ctx, subscriber, service, hlrCmds);
        }
}
    
    /**
     * Activate the subscriber and its selected services on the BlackBerry Provisioning System 
     * @param ctx
     * @param subscriber
     * @param service
     * @param hlrCmds
     * @throws AgentException
     */
    private void provisionServiceOnBlackBerrySystem(Context ctx, Subscriber subscriber, Service service, String hlrCmds)
            throws AgentException
    {
        try
        {
            long[] services = BlackberrySupport.getBlackberryServicesIdsForService(ctx, service.getID());
            Attributes attributes = BlackberrySupport.getBlackberryAttributesBasicAttributes(ctx, subscriber);
            if (BlackberrySupport.isParamTrackingEnabled(ctx))
            {
                attributes.setMsisdn(subscriber.getMSISDN());
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Provisioning Blackberry Service. Calling IServiceBlackberry.activate "
                        + " with parameters: BlackBerry Service Identifiers=[" + Arrays.toString(services)
                        + "] attributes=[" + attributes + "]");
            }
            provisionToBlackBerryProvisioningSystem(ctx, subscriber, service, services, attributes);
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this,
                        "Successfully activated for Service in BlackBerry Provisioning System for subscriber "
                                + subscriber.getId());
            }
            synchWithBlackBerryExternalProfile(ctx, subscriber, service);
        }
        catch (HomeException ex)
        {
            LogSupport.minor(ctx, this, "Provisioning Blackberry Service. Unable Calling to obtain SIM Package ["
                    + subscriber.getIMSI() + " ] for sub " + subscriber.getId());
            throw new AgentException(ex);
        }
    }
    
    private void provisionServiceOnIPC(
            Context ctx,
            Subscriber subscriber, 
            Service service, 
            String hlrCmds)
        throws AgentException
    {
        ContextAgent agent = new IPCProvisionAgent();
        agent.execute(ctx);
    }

    /**
     * Forward call to activate service profile on BlackBerry Provisioning System.
     * @param ctx
     * @param services
     * @param attributes
     * @throws ServiceBlackberryException
     * @throws AgentException
     */
    private void provisionToBlackBerryProvisioningSystem(Context ctx, 
            final Subscriber subscriber, 
            final Service service, 
            long[] services, 
            Attributes attributes)
        throws AgentException
    {
        try
        {
        	final CompoundIllegalStateException compound = new CompoundIllegalStateException();
            IServiceBlackberry client = ServiceBlackberryFactory.getServiceBlackberry(ctx, subscriber.getSpid());
            
            if(client == null) 
            {
            	compound.thrown(
                        new IllegalPropertyArgumentException(
                            SubscriberXInfo.INTENT_TO_PROVISION_SERVICES, "Missing Blackberry provision configuration for SPID - " + subscriber.getSpid()));
            	compound.throwAll();
            }
            
            if (client != null)
            {
                PMLogMsg pm = new PMLogMsg("com.redknee.app.crm.provision.BlackberryProvisionAgent", 
                "IServiceBlackberry.activate");
                try
                {
                    client.activate(ctx, subscriber.getSpid(), services, attributes);
                }
                finally 
                {
                    pm.log(ctx);
                }
            }
            else
            {
                throw new AgentException("No ServiceBlackberry was found in the context.");
            }
            
        }
        catch (ServiceBlackberryException sbe)
        {
            /* It can be argued that if we're throwing an exception in this case, then
             * we don't have to invoke the Error handler now (it will get done by the catch (ProvisionAgentException).
             * However, since we have knowledge that the error handler now is generating an ER for BlackBerry
             * provisioning, we should do that here now.  A good time to remove it would be once, we have 
             * ported to CRM 8.0 (with Service Refactoring) */
            getErrorHandler().handleError(ctx, subscriber, service, sbe.getResultStatus(), sbe.getErrorCode(), sbe.getDescription());

            int errorCode = ExternalAppSupport.NO_CONNECTION;
            try
            {
                errorCode = Integer.parseInt(sbe.getErrorCode());
            }
            catch( NumberFormatException nfe)
            {
                // ignore the number format exception and just continue with the default code (-1).
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Error Code returned from BlackBerry Provisioning System is not a numeric " +
                    "error code.  Cannot determine the category of this error. ");
                }
                
                if (com.redknee.service.blackberry.model.ResultEnum.RIM_COMM_FAILURE.equals(sbe.getResultStatus()))
                {
                    errorCode = ExternalAppSupport.COMMUNICATION_FAILURE;
                }
            }

            // If the service was already active on RIM Provisioning Server, suppress the error.
            if (errorCode != RIMBlackBerryErrorCodes.SERVICE_ALREADY_ACTIVE)
            {
                //Otherwise report provisioning error and mark the service as provisioned with errors
                ProvisionAgentException ae = new ProvisionAgentException(ctx, 
                        ExternalAppSupportHelper.get(ctx).getProvisionErrorMessage(ctx,
                                ExternalAppEnum.BLACKBERRY, errorCode, service), 
                        errorCode, 
                        ExternalAppEnum.BLACKBERRY, sbe);
                throw ae;
            }
        }
    }
    


    
    /**
     * Synchronize BlackBerry external profile state after activation.  Right now, only Suspended state
     * is synchronized.
     * @param ctx
     * @param subscriber
     * @param service
     * @throws AgentException
     */
    private void synchWithBlackBerryExternalProfile(final Context ctx, 
            final Subscriber subscriber, 
            final Service service) 
        throws AgentException
    {
        try
        {
            // Sync Subscriber/Service state with BlackBerry Profile for suspension.
            if (needsBlackBerryProfileSuspension(ctx, subscriber, service))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Suspend service " + service.getIdentifier() +  
                            " on RIM BlackBerry System to synchronize with CRM Subscriber/Subscriber Service state.");
                }
                SuspendBlackberryServiceUpdateAgent agent = new SuspendBlackberryServiceUpdateAgent();
                agent.update(ctx, subscriber, service);
            }
        }
        catch (HomeException e)
        {
            if (e.getCause() instanceof ProvisionAgentException)
            {
                throw (ProvisionAgentException) e.getCause();
            }
            else
            {
                /* HomeExceptions are only thrown for failures to update HLR. See 
                 * AbstractBlackberryServiceUpdateAgent.update.  I would log a ProvisionAgentException against HLR,
                 * but SubscriberProvisionServicesHome parses such exceptions to mean that a Generic Service 
                 * failed to be provisioned.  Although we want the granularity to be able to tell that the HLR 
                 * command failed for BlackBerry Service provisioning, we cannot support it at this time.
                 * Log exception here against HLR in CRM 8.0+ (after Service Refactoring phase I).*/ 
                ProvisionAgentException ae = new ProvisionAgentException(ctx, 
                        ExternalAppSupportHelper.get(ctx).getSuspensionErrorMessage(ctx,
                                ExternalAppEnum.BLACKBERRY, -1, service), 
                        -1, 
                        ExternalAppEnum.BLACKBERRY, e);
                throw ae;
            }
        }
    }
    
    /**
     * There are cases where Subscriber Services' provision agent will be executed, even though
     * the Subscriber or Service is in the Suspended state.  (I.e. Msisdn swap, selecting new services
     * when CRM Subscriber is in Suspended State.)  In general, since we deal with RK External services
     * (Voice/SMS/Data) we don't really care about suspending the external profile when CRM sub is 
     * Suspended.  In the case of BlackBerry services, we want to maintain the synchronization between
     * RIM BB Service profile and the CRM Subscriber/Subscriber Service states.
     * 
     * Return TRUE, if the BlackBerry Service needs to be suspended to stay synchronized with the CRM
     * Subscriber and Subscriber Service state.  Otherwise, return false.
     * @param subscriber
     * @param service
     */
    private boolean needsBlackBerryProfileSuspension(final Context ctx, 
            final Subscriber subscriber, 
            final Service service)
    {
        boolean needsToSuspend = false;
        boolean isSuspendedEntity = false;
        
        try
        {
            isSuspendedEntity = SuspendedEntitySupport.isSuspendedEntity(ctx, 
                    subscriber.getId(), 
                    service.getID(), 
                    SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED,
                    ServiceFee2.class);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Failed to determine if the Service is a Suspended entity. " 
                        + e.getMessage(), e);
            }
        }
        
        if ( subscriber.getState().equals(SubscriberStateEnum.SUSPENDED)
                || subscriber.getState().equals(SubscriberStateEnum.LOCKED) 
                || subscriber.getState().equals(SubscriberStateEnum.EXPIRED)
                || isSuspendedEntity)
        {
            needsToSuspend = true;
        }
        
        return needsToSuspend;
    }

    private ErrorHandler getErrorHandler()
    {
        return handler_;
    }
    
    private ErrorHandler handler_;
}
