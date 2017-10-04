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

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.alcatel.AlcatelProvisioning;
import com.trilogy.app.crm.client.alcatel.AlcatelProvisioningException;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;

/**
 * This class is invoked using code Reflection.
 * ProvisioningSupport.provisionService() calls this.
 * 
 * This agent will use the AlcatelProvisioning client's createService command to 
 * provision a broadband service to the Alcatel SSC and to the HLR.
 * @author angie.li@redknee.com
 *
 */
public class AlcatelProvisionAgent extends CommonProvisionAgent 
{

    /**
     * Ensures the profile is created on the Alcatel SSC,
     * and sends the HLR provisioning command for the Alcatel Service.
     * Context must contain the subscriber to be installed keyed by Subscriber.class
     * Context must contain Service to retrieve additional params needed associated with this service
     * Context must contain Alcatel SSC Client (AlcatelProvisioning) to provision to the Alcatel SSC Provisioning Server
     */
    public void execute(Context ctx) throws AgentException 
    {
        Subscriber subscriber = null;
        Service service = null;
        String hlrCmds = null; 
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Begin validation of Alcatel Service Parameters.");
        }
        // Begin validation of parameters
        {
            subscriber = (Subscriber)ctx.get(Subscriber.class, null);
            if (subscriber == null)
            {
                throw new AgentException("Developer error: No subscriber to provision. No Subscriber is found in the context.");
            }

            service = (Service)ctx.get(Service.class, null);
            if (service == null)
            {
                throw new AgentException("Developer error: Service for Alcatel SSC provisioning not found in context");
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

        }
        
        AlcatelProvisioning client = (AlcatelProvisioning) ctx.get(AlcatelProvisioning.class);
        if (client == null)
        {
            throw new AgentException("System error: AlcatelProvisioning client is not found in the context.");
        }
        
        //Begin validation against provisioning for Alcatel Services to Subscriptions in Available and Pending states
        {
            if (subscriber.getState().equals(SubscriberStateEnum.AVAILABLE) || 
                    subscriber.getState().equals(SubscriberStateEnum.PENDING) )
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, 
                        "Skipping provisioning of Alcatel Service because the Subscriber is in the Available/Pending state.  " +
                        "The Service will be provisioned (activated) on the Alcatel SSC Provisioning Server when the Subscriber " +
                        "is Activated in CRM. Sub=" + subscriber.getId());
                }
                throw new SkipProvisioningException(this, subscriber.getId(), service.getIdentifier() +  " - " + service.getName(),
                        "Subscriber has not been activated in billing system.");
            }
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Attempting Alcatel provisioning with the following parameters:"
                    + " Subscriber ID= " + subscriber.getId()
                    + ", MSISDN= " + subscriber.getMSISDN() 
                    + ", CRM Service=[ " + service.getID()
                    + "], HLR cmds=[" + hlrCmds + "]");
        }
        
        try
        {
            // Provision to the Alcatel Provisioning Server
            provisionXMLProvisionableService(ctx, subscriber, service, hlrCmds, client);
        }
        finally
        {
            // Provisiong to HLR
            // Any exceptions that occur during BlackBerry Provisioning (previous step) will NOT abort HLR provisioning
            if ((null != hlrCmds) && (hlrCmds.length() > 0))
            {
                callHlr(ctx, true, subscriber, service,null,null); 
            }
        }
    }

    /**
     * Retrieve the correct XML request to provision the Alcatel Service to the SSC and send this
     * XML to the Alcatel SSC.
     * Parse the response to retrieve the external ID if the XML request was createAccount.
     * @param ctx
     * @param subscriber
     * @param service
     * @param hlrCmds
     */
    private void provisionXMLProvisionableService(Context ctx,
            Subscriber subscriber, Service service, String hlrCmds, AlcatelProvisioning client) 
    throws ProvisionAgentException
    {
        try
        {
            client.createService(ctx, service, subscriber);
        }
        catch (AlcatelProvisioningException e)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_ALCATEL_PROV_ERROR).log(ctx);
            
            // SNMP Trap AlcatelProvisionAgent - fail to add subscriber - result code = {0}
            new EntryLogMsg(13780, this, "", subscriber.toString(), new java.lang.String[]
            {
                String.valueOf(e.getResultCode())
            }, null).log(ctx);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Alcatel Service Provisionng Failed for subscriber=" + subscriber.getId() 
                        + " service=" + service.getID() + " with error code [" + e.getResultCode()  + "]");
            }
            //throw the ProvisionAgentException
            throw new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx)
                    .getProvisionErrorMessage(ctx, ExternalAppEnum.ALCATEL_SSC, e.getResultCode(), service), e.getResultCode(),
                    ExternalAppEnum.ALCATEL_SSC, e);
        }
    }
    
    


}
