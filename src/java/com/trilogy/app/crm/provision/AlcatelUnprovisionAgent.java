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
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.alcatel.AlcatelProvisioning;
import com.trilogy.app.crm.client.alcatel.AlcatelProvisioningException;
import com.trilogy.app.crm.subscriber.provision.ProvisionResultCode;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This class is invoked using code Reflection.
 * ProvisioningSupport.unprovisionService() calls this.
 * 
 * This agent will use the AlcatelProvisioning client's removeService command to 
 * unprovision a broadband service from the Alcatel SSC and to unprovision from the HLR.
 * @author angie.li@redknee.com
 *
 */
public class AlcatelUnprovisionAgent extends CommonUnprovisionAgent 
{

    /**
     * Ensures the service is disabled on the Alcatel SSC,
     * and sends the HLR unprovisioning command for the Alcatel Service.
     * Context must contain the subscriber to be installed keyed by Subscriber.class
     * Context must contain Service to retrieve additional params needed associated with this service
     * Context must contain AlcatelSSCClient to provision to the Alcatel SSC Provisioning Server
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
                    hlrCmds = service.getUnprovisionConfigs();
                }
                else if (subscriber.isPrepaid())
                {
                    hlrCmds = service.getPrepaidUnprovisionConfigs();
                }
            }

        }
        
        AlcatelProvisioning client = (AlcatelProvisioning) ctx.get(AlcatelProvisioning.class);
        if (client == null)
        {
            throw new AgentException("System error: AlcatelProvisioning client is not found in the context.");
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Attempting Alcatel Service unprovisioning with the following parameters:"
                    + " Subscriber ID= " + subscriber.getId()
                    + ", MSISDN= " + subscriber.getMSISDN() 
                    + ", CRM Service=[ " + service.getID()
                    + "], HLR cmds=[" + hlrCmds + "]");
        }
        
        try
        {
            // Unprovision to the Alcatel Provisioning Server
            unprovisionXMLProvisionableService(ctx, subscriber, service, hlrCmds, client);
        }
        finally
        {
            // Unprovisiong to HLR
            // Any exceptions that occur during Alcatel Provisioning (previous step) will NOT abort HLR provisioning
            if ((null != hlrCmds) && (hlrCmds.length() > 0))
            {
                callHlr(ctx, false,subscriber, service,null,null);
            }
            
        }
    }

    /**
     * Retrieve the correct XML request to unprovision the Alcatel Service to the SSC and send this
     * XML to the Alcatel SSC.
     * Parse the response to retrieve the external ID if the XML request was createAccount.
     * @param ctx
     * @param subscriber
     * @param service
     * @param hlrCmds
     */
    private void unprovisionXMLProvisionableService(Context ctx,
            Subscriber subscriber, Service service, String hlrCmds, AlcatelProvisioning client) 
    throws ProvisionAgentException
    {
        try
        {
            client.removeService(ctx, service, subscriber);
        }
        catch (AlcatelProvisioningException e)
        {
            //ProvisionResultCode is used to keep track of all of the Provisioning errors and used later in Charging
            ProvisionResultCode rc = (ProvisionResultCode)ctx.get(ProvisionResultCode.class);
            ProvisionAgentException exception = new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx)
                    .getUnprovisionErrorMessage(ctx, ExternalAppEnum.ALCATEL_SSC, e.getResultCode(), service), e.getResultCode(),
                    ExternalAppEnum.ALCATEL_SSC, e);
            

            int lastResult = exception.getResultCode();
            if (rc != null)
            {
                rc.setResultAlcatel(e.getResultCode());
                rc.setResultLast(lastResult);
            }
            
            //SNMP trap: AlcatelUnprovisionAgent - fail to remove service from Alcatel SSC - result code = {0}
            new EntryLogMsg(13782, this, "", subscriber.toString(), new java.lang.String[]
            {
                String.valueOf(e.getResultCode())
            }, null).log(ctx);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Alcatel Service Unprovisionng Failed for subscriber=" + subscriber.getId() 
                        + " service=" + service.getID() + " with error code [" + e.getResultCode()  + "]");
            }
            
            HTMLExceptionListener exps = (HTMLExceptionListener)ctx.get(HTMLExceptionListener.class);
            if (exps != null)
            {
                SubscriberSupport.generateOM(ctx, exps, exception.getResultCode(), exception.getMessage(),
                        exception.getSourceResultCode(), Common.OM_ALCATEL_SERVICE_UNPROV_ERROR, this, e);
            }
            else
            {
                throw exception;
            }
        }
    }
    
    


}
