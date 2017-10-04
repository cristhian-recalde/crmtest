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
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.blackberry.error.ErrorHandler;
import com.trilogy.app.crm.blackberry.error.RIMBlackBerryErrorCodes;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.service.blackberry.IServiceBlackberry;
import com.trilogy.service.blackberry.ServiceBlackberryException;
import com.trilogy.service.blackberry.ServiceBlackberryFactory;
import com.trilogy.service.blackberry.model.Attributes;

public class BlackberryUnprovisionAgent extends CommonProvisionAgent 
{
    
    public BlackberryUnprovisionAgent()
    {
        handler_ = BlackberrySupport.getErrorHandler();
    }

    /**
     * Unprovision BlackBerry services from BlackBerry Provisioning System, and then to the HLR.
     * Context must contain the subscriber to be installed keyed by Subscriber.class
     * Context must contain Service to retrieve additional params needed associated with this service
     * Context must contain IServiceBlackberry to provision to the BlackBerry Provisioning System
     * @param ctx
     */
    public void execute(Context ctx) throws AgentException 
    {
        Subscriber subscriber = null;
        Service service = null;
        String hlrCmds = null;    // to Remove APN from HLR
        Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Begin validation of BlackBerry Service Parameters.");
        }
        // Begin validation of parameters
        subscriber = (Subscriber)ctx.get(Subscriber.class, null);
        if (subscriber == null)
        {
            throw new AgentException("Developer error: No subscriber to marked for unprovisioning.");
        }

        service = (Service)ctx.get(Service.class, null);
        if (service == null)
        {
            throw new AgentException("Developer error: Service for BlackBerry unprovisioning not found in context.");
        }
        else
        {
            if (subscriber.isPostpaid())
            {
                hlrCmds = service.getUnprovisionConfigs();
            }
            else if (subscriber.isPrepaid())
            {
                hlrCmds = service.getPrepaidUnprovisionConfigs();
            }
        }
        
        if ((hlrCmds == null) || (hlrCmds.length() == 0))
        {
            throw new AgentException("Configuration error: BlackBerry HLR commands are missing for unprovisioning.");
        }

        // Don't unprovision if old subscriber is in available/pending state.
        if (oldSubscriber != null &&
              EnumStateSupportHelper.get(ctx).isOneOfStates(oldSubscriber, new Enum[]
                        {SubscriberStateEnum.AVAILABLE, SubscriberStateEnum.PENDING}))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, 
                    "Skipping unprovisioning of BlackBerry Service because the Subscriber is moving from Available/Pending " + 
                    "state to active and therefore the service has not been provisioned to the BlackBerry Provisioning System." +
                    " Sub=" + subscriber.getId());
            }
            throw new SkipProvisioningException(this, subscriber.getId(), service.getIdentifier() +  " - " + service.getName(),
                    "Subscriber has not been activated in CRM.");
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Attempting BlackBerry service cancellation with the following parameters:"
                    + " Subscriber ID= " + subscriber.getId()
                    + ", CRM Service=[ " + service.getID()
                    + "], HLR cmds=[" + hlrCmds + "]");
        }
        
        // Deactivate Service on the BlackBerry Provisioning System
        deactivateServiceOnBlackBerrySystem(ctx, subscriber, service, hlrCmds);
        
        // Send unprovision command to HLR
        callHlr(ctx, false,subscriber, service,null,null);
        
        if (BlackberrySupport.areBlackberryServicesProvisionedToIPC(ctx))
        {
            // Provision to the BlackBerry Provisioning System
            unprovisionServiceOnIPC(ctx, subscriber, service, hlrCmds);
        }
        

    }
    
    private void unprovisionServiceOnIPC(
            Context ctx,
            Subscriber subscriber, 
            Service service, 
            String hlrCmds)
        throws AgentException
    {
        ContextAgent agent = new IPCUnprovisionAgent();
        agent.execute(ctx);
    }

    /**
     * Deactivate the subscriber and its selected services on the BlackBerry Provisioning System 
     * Any exceptions that occur during Provisioning to RIM Provisioning System will not abort 
     * the unprovisioning process.
     * 
     * @param ctx
     * @param subscriber
     * @param service
     * @param hlrCmds
     * @throws AgentException
     */
    private void deactivateServiceOnBlackBerrySystem(
            Context ctx,
            Subscriber subscriber, 
            Service service, 
            String hlrCmds)
        throws AgentException
    {
        try
        {
            long[] services = BlackberrySupport.getBlackberryServicesIdsForService(ctx, service.getID());
            Attributes attributes = BlackberrySupport.getBlackberryAttributesBasicAttributes(ctx, subscriber);

            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Unprovisioning Blackberry Service. Calling IServiceBlackberry.deactivate "
                        + " with parameters: BlackBerry Service Identifiers=[" + Arrays.toString(services)
                        + "] attributes=[" + attributes + "]");
            }
            
            provisionToBlackBerryProvisioningSystem(ctx, services, attributes, subscriber);

            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Successfully deactivated the Service in BlackBerry Provisioning System for subscriber " 
                        + subscriber.getId());
            }

        }
        catch (ServiceBlackberryException sbe)
        {
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

            // If the service was already deactivated on RIM Provisioning Server, suppress the error.
            // TT#9012000018: If the service has not been activated yet on RIM Provisioning Server, also suppress the error.
            if (errorCode != RIMBlackBerryErrorCodes.SERVICE_DEACTIVATED && errorCode != RIMBlackBerryErrorCodes.OLD_BILLING_NOT_FOUND)
            {
                ProvisionAgentException ae = new ProvisionAgentException(ctx, 
                        ExternalAppSupportHelper.get(ctx).getUnprovisionErrorMessage(ctx,
                                ExternalAppEnum.BLACKBERRY, errorCode, service), 
                        errorCode, 
                        ExternalAppEnum.BLACKBERRY, sbe);

                throw ae;
            }
            else
            {
                String msg = sbe.getMessage() != null ? sbe.getMessage() : sbe.getDescription();
                LogSupport.debug(ctx, this, "Blackberry service not activated or already deactivated on Blackberry Provisioning System: " +
                        msg + " (" + errorCode + ")");
            }
        }
        catch(HomeException ex)
        {
            LogSupport.minor(ctx, this, "UnProvisioning Blackberry Service. Unable Calling to obtain SIM Package ["
                    + subscriber.getIMSI() + " ] for sub " + subscriber.getId());
            throw new AgentException(ex);
        }
    }
    
    /**
     * Forward call to cancel service profile on BlackBerry Provisioning System.
     * @param ctx
     * @param services
     * @param attributes
     * @throws ServiceBlackberryException
     * @throws AgentException
     */
    private void provisionToBlackBerryProvisioningSystem(Context ctx, long[] services, Attributes attributes, Subscriber subscriber)
        throws ServiceBlackberryException, AgentException
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
            PMLogMsg pm = new PMLogMsg("com.redknee.app.crm.provision.BlackberryUnprovisionAgent", 
                    "IServiceBlackberry.deactivate");
            try
            {
                client.deactivate(ctx, subscriber.getSpid(), services, attributes);
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
    


    private ErrorHandler getErrorHandler()
    {
        return handler_;
    }
    
    private ErrorHandler handler_;
}
