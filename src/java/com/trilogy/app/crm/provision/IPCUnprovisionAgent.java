/*
 *  IPCUnprovisionAgent.java
 *
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

import java.util.Set;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.ipcg.IpcgClient;
import com.trilogy.app.crm.client.ipcg.IpcgClientFactory;
import com.trilogy.app.crm.client.ipcg.IpcgClientProxy;
import com.trilogy.app.crm.subscriber.provision.ProvisionResultCode;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class IPCUnprovisionAgent extends CommonUnprovisionAgent
{    
    // NOTE - 2006-06-07 - An unfortunate result of the current service
    // provisioning design and the requirement that EVDO service automatically
    // include IPCG service is that we must explicitly check here to see if EVDO
    // is also enabled -- we cannot unprovision IPCG if EVDO is still
    // provisioned.  However, since EVDO delegates to this agent when it
    // unprovisions, we must also allow it to override this check.  I hope that
    // this coupling of the two agents can be removed when service provisioning
    // is redesigned.

    /**
     * This key, when placed in the context, will cause this agent to skip the
     * check to see if EVDO service is also provisioned.  This is meant as a
     * device for the EVDO agent to be able to use this agent to unprovision
     * IPCG when EVDO is unprovisioned.
     */
    static final String OVERRIDE_EVDO_CHECK =
        IPCUnprovisionAgent.class.getName() + ".OVERRIDE_EVDO_CHECK";
    

    public void execute(Context ctx) throws AgentException
    {
        IpcgClient ipcClient;
        Subscriber subscriber;
        Service service;
        String hlrCmds = null;
        boolean isBlackberryService = false;

        subscriber = (Subscriber) ctx.get(Subscriber.class);
        if (subscriber == null)
        {
            throw new AgentException(
                    "System error: No subscriber to provision");
        }

        service = (Service) ctx.get(Service.class);
        if (service == null)
        {
            throw new AgentException(
                    "System error: Service for SMS provisioning not found in context");
        }
        else if (ServiceTypeEnum.BLACKBERRY.equals(service.getType()))
        {
            isBlackberryService = true;
        }

        if (service.getType() != ServiceTypeEnum.EVDO
            && isSeparateEVDOServiceSelected(ctx, subscriber))
        {
            return;
        }
        
        Set svcSet = subscriber.getServices();
        //if subscriber has no data services then   need to make call to remove the profile
        if (!IpcgClientProxy.hasDataService(ctx, svcSet))
        {
            return;
        } 
        ipcClient =
            IpcgClientFactory.locateClient(ctx, subscriber.getTechnology());
        if (ipcClient == null)
        {
            throw new AgentException(
                    "System error: IpcgClient not found in context");
        }

        int result = -1;
        try
        {
            //If subs. has data service then it is provisioned on the IPCG otherwise no. need to make call to remove the profile
            result = ipcClient.removeSubscriber(ctx, subscriber);
        }
        catch(Exception e)
        {
            final String msg = "Could not remove Subscriber [" + subscriber.getId() + " ] with MSISDN ["
                    + subscriber.getMSISDN() + "] form Data Service due to Error (return -1) [" + e.getMessage() + "]";
            new DebugLogMsg(this, msg, e);
            new MinorLogMsg(this, msg, null);
            result = -1;
        }

        //The following OM code is moved from original provisioninghome.java
        //Apr 07, 2005
        if (result != 0)
        {
            ProvisionAgentException exception = new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx)
                    .getUnprovisionErrorMessage(ctx, ExternalAppEnum.DATA, result, service), result,
                    ExternalAppEnum.DATA);

            ProvisionResultCode rc = (ProvisionResultCode) ctx.get(ProvisionResultCode.class);
            if (rc != null)
            {
                //SNMP trap: IPCUnprovisionAgent - fail to remove subscriber - result code = {0}
                new EntryLogMsg(13785, this, "", subscriber.toString(), new java.lang.String[]
                {
                    String.valueOf(result)
                }, null).log(ctx);
                rc.setResultIpc(result);
                rc.setResultLast(exception.getResultCode());
                HTMLExceptionListener exps = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
                if (exps != null)
                {
                    SubscriberSupport.generateOM(ctx, exps, exception.getResultCode(), exception.getMessage(), result, Common.OM_IPC_ERROR, this);
                }
                else
                {
                    throw exception;
                }
            }
            else
            {
                throw exception;
            }
        }

        // Only send HLR commands if this is not a blackberry service. Otherwise it will have been sent earlier.
        if (!isBlackberryService)
        {
            // look for the SogClient for the subscriber's HLR ID
            short hlrId = subscriber.getHlrId();

            if (subscriber.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
            {
                hlrCmds = service.getUnprovisionConfigs();
            }
            else if (subscriber.getSubscriberType() == SubscriberTypeEnum.PREPAID)
            {
                hlrCmds = service.getPrepaidUnprovisionConfigs();
            }
    
            if ((hlrCmds == null) || (hlrCmds.length() == 0))
            {
                // no HLR commands means nothing to do
                return;
            }
    
            // execute HLR commands using HLR client
            callHlr(ctx, false,subscriber,service,null,null);
        }

        /*
         * Leave this code temporarly, after a succesful test will be deleted
         * String[] commands = hlrCmds.split("\n"); for (int i=0; i
         * <commands.length; i++) { if (commands[i].trim().length() == 0) {
         * continue; }
         * 
         * if (LogSupport.isDebugEnabled(ctx)) { new DebugLogMsg(this, "HLR
         * command ["+i+"]:"+commands[i], null).log(ctx); }
         * 
         * if (commands[i].length() > 0) { commands[i] = replaceHLRCommand(ctx,
         * commands[i], subscriber);
         * 
         * EricssonMsg response = sogClient.executeCommand(commands[i]);
         * 
         * if (response == null || response.getCode() != 0) { // continue on new
         * InfoLogMsg(this, "HLR unprovisioning failed: " + response,
         * null).log(ctx); } } }
         */
    }


    /**
     * Checks to see if the subscriber being processed has a separate EVDO
     * service in their PricePlan.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to examine services.
     *
     * @return True if the subscriber has a separate EVDO service in their
     * PricePlan.
     *
     * @exception AgentException Thrown if the there are any problems accessing
     * Home data in the context.
     */
    boolean isSeparateEVDOServiceSelected(
        final Context context,
        final Subscriber subscriber)
        throws AgentException
    {
        try
        {
            return ServiceSupport.isServiceSelected(
                context,
                subscriber,
                ServiceTypeEnum.EVDO);
        }
        catch (final HomeException exception)
        {
            throw new AgentException(
                "Unanticipated exception encounterred while examining subscriber's services.",
                exception);
        }
    }
}
