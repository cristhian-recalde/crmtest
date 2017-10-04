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
package com.trilogy.app.crm.provision.agent.suspend;

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
 * This agent will use the AlcatelProvisioning client's suspendService command to 
 * block a broadband service from the Alcatel SSC and to suspend the service from the HLR.
 * @author angie.li@redknee.com
 * 
 * We don't want this agent to be invoked by the SubscriberProvisionServicesHome.
 * There are validations put in place to avoid suspending the service using that area of the 
 * subscriber provisioning pipeline.
 * 
 * This class is usually invoked using code Reflection.
 * ProvisioningSupport.suspendService() usually would call it.
 * 
 * Instead this class will be instantiated by SuspendAlcatelServiceUpdateAgent.
 *
 */
public class AlcatelSuspendAgent extends CommonUnprovisionAgent 
{

    /**
     * Ensures the service is disabled on the Alcatel SSC,
     * and sends the HLR suspend command for the Alcatel Service.
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
                throw new AgentException("Developer error: No subscriber to suspend. No Subscriber is found in the context.");
            }

            service = (Service)ctx.get(Service.class, null);
            if (service == null)
            {
                throw new AgentException("Developer error: Service for Alcatel SSC suspension not found in context");
            }
            else
            {
                if (subscriber.isPostpaid())
                {
                    hlrCmds = service.getSuspendConfigs();
                }
                else if (subscriber.isPrepaid())
                {
                    hlrCmds = service.getPrepaidSuspendConfigs();
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
            LogSupport.debug(ctx, this, "Attempting Alcatel Service suspension with the following parameters:"
                    + " Subscriber ID= " + subscriber.getId()
                    + ", MSISDN= " + subscriber.getMSISDN() 
                    + ", CRM Service=[ " + service.getID()
                    + "], HLR cmds=[" + hlrCmds + "]");
        }

        try
        {
            // Suspend Service on the Alcatel Provisioning Server
            suspendXMLProvisionableService(ctx, subscriber, service, hlrCmds, client);
        }
        finally
        {
            // Suspend Service on the HLR
            // Any exceptions that occur during Alcatel Provisioning (previous step) will
            // NOT abort HLR provisioning
            if ((null != hlrCmds) && (hlrCmds.length() > 0))
            {
                suspendServiceOnHLR(ctx, subscriber, service, hlrCmds);
            }
        }
    }

    /**
     * Retrieve the correct XML request to suspend the Alcatel Service to the SSC and send this
     * XML to the Alcatel SSC.
     * @param ctx
     * @param subscriber
     * @param service
     * @param hlrCmds
     */
    private void suspendXMLProvisionableService(Context ctx,
            Subscriber subscriber, Service service, String hlrCmds, AlcatelProvisioning client) 
    throws ProvisionAgentException
    {
        try
        {
            client.suspendService(ctx, service, subscriber);
        }
        catch (AlcatelProvisioningException e)
        {
            //ProvisionResultCode is used to keep track of all of the Provisioning errors and used later in Charging
            ProvisionResultCode rc = (ProvisionResultCode)ctx.get(ProvisionResultCode.class);
            ProvisionAgentException exception = new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx).getUnprovisionErrorMessage(ctx,
                    ExternalAppEnum.ALCATEL_SSC, e.getResultCode(), service), e.getResultCode(),
                    ExternalAppEnum.ALCATEL_SSC, e);
            

            int lastResult = exception.getResultCode();
            if (rc != null)
            {
                rc.setResultAlcatel(e.getResultCode());
                rc.setResultLast(lastResult);
            }

            //SNMP trap: Alcatel Suspend Agent - fail to suspend service from Alcatel SSC - result code = {0}
            new EntryLogMsg(13787, this, "", subscriber.toString(), new java.lang.String[]
                                                                                         {
                String.valueOf(e.getResultCode())
                                                                                         }, null).log(ctx);

            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Alcatel Service Suspension Failed for subscriber=" + subscriber.getId() 
                        + " service=" + service.getID() + " with error code [" + e.getResultCode()  + "]");
            }

            HTMLExceptionListener exps = (HTMLExceptionListener)ctx.get(HTMLExceptionListener.class);
            if (exps != null)
            {
                SubscriberSupport.generateOM(ctx, exps, lastResult, exception.getMessage(), e.getResultCode(), Common.OM_ALCATEL_SERVICE_SUSPEND_ERROR, this, e);
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
    private void suspendServiceOnHLR(
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
            LogSupport.debug(ctx, this, "Suspend Alcatel service " + service.getName() + "(ID="
                    + service.getID() + ") on the HLR (HLR ID=" + hlrId  + "). "
                    + " for subscriber " + subscriber.getId());
        }

        /* Forward Alcatel HLR command to the HLR. */  
        callHlr(ctx, false, subscriber, service, null, null);

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Successfully sent Suspend Service command to the HLR for subscriber " 
                    + subscriber.getId());
        }
    }

}
