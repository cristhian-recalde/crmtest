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
package com.trilogy.app.crm.provision.agent.resume;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.alcatel.AlcatelProvisioning;
import com.trilogy.app.crm.client.alcatel.AlcatelProvisioningException;
import com.trilogy.app.crm.provision.CommonUnprovisionAgent;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.subscriber.provision.ProvisionResultCode;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * This agent will use the AlcatelProvisioning client's resumeService command to 
 * block a broadband service from the Alcatel SSC and to resume the service from the HLR.
 * @author angie.li@redknee.com
 * 
 * We don't want this agent to be invoked by the SubscriberProvisionServicesHome.
 * There are validations put in place to avoid resuming the service using that area of the 
 * subscriber provisioning pipeline.
 * 
 * This class is usually invoked using code Reflection.
 * ProvisioningSupport.resumeService() usually would call it.
 * 
 * Instead this class will be instantiated by ResumeAlcatelServiceUpdateAgent.
 *
 */
public class AlcatelResumeAgent extends CommonUnprovisionAgent 
{

    /**
     * Ensures the service is enabled on the Alcatel SSC,
     * and sends the HLR resume command for the Alcatel Service.
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
                throw new AgentException("Developer error: No subscriber to resume. No Subscriber is found in the context.");
            }

            service = (Service)ctx.get(Service.class, null);
            if (service == null)
            {
                throw new AgentException("Developer error: Alcatel SSC Service to resume not found in context");
            }
            else
            {
                if (subscriber.isPostpaid())
                {
                    hlrCmds = service.getResumeConfigs();
                }
                else if (subscriber.isPrepaid())
                {
                    hlrCmds = service.getPrepaidResumeConfigs();
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
            LogSupport.debug(ctx, this, "Attempting Alcatel Service resume with the following parameters:"
                    + " Subscriber ID= " + subscriber.getId()
                    + ", MSISDN= " + subscriber.getMSISDN() 
                    + ", CRM Service=[ " + service.getID()
                    + "], HLR cmds=[" + hlrCmds + "]");
        }

        try
        {
            // Resume Service on the Alcatel Provisioning Server
            resumeXMLProvisionableService(ctx, subscriber, service, hlrCmds, client);
        }
        finally
        {
            if ((null != hlrCmds) && (hlrCmds.length() > 0))
            {
                resumeServiceOnHLR(ctx, subscriber, service, hlrCmds);  
            }
            // Resume Service on the HLR
            // Any exceptions that occur during Alcatel Provisioning (previous step) will NOT abort HLR provisioning
            
        }
    }

    /**
     * Retrieve the correct XML request to resume the Alcatel Service to the SSC and send this
     * XML to the Alcatel SSC.
     * @param ctx
     * @param subscriber
     * @param service
     * @param hlrCmds
     */
    private void resumeXMLProvisionableService(Context ctx,
            Subscriber subscriber, Service service, String hlrCmds, AlcatelProvisioning client) 
    throws ProvisionAgentException
    {
        try
        {
            client.resumeService(ctx, service, subscriber);
        }
        catch (AlcatelProvisioningException e)
        {
            //ProvisionResultCode is used to keep track of all of the Provisioning errors and used later in Charging
            ProvisionResultCode rc = (ProvisionResultCode)ctx.get(ProvisionResultCode.class);
            ProvisionAgentException exception = new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx)
                    .getProvisionErrorMessage(ctx, ExternalAppEnum.ALCATEL_SSC, e.getResultCode(), service), e.getResultCode(),
                    ExternalAppEnum.ALCATEL_SSC, e);
            int lastResult = exception.getResultCode();
            if (rc != null)
            {
                rc.setResultAlcatel(e.getResultCode());
                rc.setResultLast(lastResult);
            }

            //SNMP trap: Alcatel Resume Agent - fail to resume service from Alcatel SSC - result code = {0}
            new EntryLogMsg(13788, this, "", subscriber.toString(), new java.lang.String[]
                                                                                         {
                String.valueOf(e.getResultCode())
                                                                                         }, null).log(ctx);

            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Alcatel Service Resume Failed for subscriber=" + subscriber.getId() 
                        + " service=" + service.getID() + " with error code [" + e.getResultCode()  + "]");
            }

            HTMLExceptionListener exps = (HTMLExceptionListener)ctx.get(HTMLExceptionListener.class);
            if (exps != null)
            {
                SubscriberSupport.generateOM(ctx, exps, lastResult, exception.getMessage(), e.getResultCode(), Common.OM_ALCATEL_SERVICE_RESUME_ERROR, this, e);
            }
            throw exception;
        }
    }


    /**
     * Do some logging before sending the Service's configured HLR provisioning command to the HLR
     * @param ctx
     * @param subscriber
     * @param service
     * @param hlrCmds
     * @throws AgentException
     */
    private void resumeServiceOnHLR(
            Context ctx,
            Subscriber subscriber, 
            Service service, 
            String hlrCmds)
    throws AgentException
    {
        // look for the SogClient for the subscriber's HLR ID
        short hlrId = subscriber.getHlrId();

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Resume Alcatel service " + service.getName() + "(ID="
                    + service.getID() + ") on the HLR (HLR ID=" + hlrId  + "). "
                    + " for subscriber " + subscriber.getId());
        }

        /* Forward Alcatel HLR command to the HLR. */  
        callHlr(ctx, true,subscriber,service,null,null);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Successfully sent Resume Service command to the HLR for subscriber " 
                    + subscriber.getId());
        }
    }

}
