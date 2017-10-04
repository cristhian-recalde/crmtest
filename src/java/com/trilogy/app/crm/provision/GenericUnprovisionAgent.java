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

import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;


/**
 * Agent that uninstalls generic services from the HLR.
 *
 * @author candy.wong@redknee.com
 */
public class GenericUnprovisionAgent extends CommonUnprovisionAgent
{

    /**
     * Unprovision HLR services to HLR. Context must contain the subscriber to be
     * installed keyed by Subscriber.class. Context must contain Service to retrieve
     * additional params needed associated with this service.
     *
     * @param ctx
     *            The operating context.
     * @throws AgentException
     *             Thrown if there are problems unprovisioning HLR services.
     */
    public void execute(final Context ctx) throws AgentException
    {

        final Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
        if (subscriber == null)
        {
            throw new AgentException("System error: No subscriber to provision");
        }

        final Service service = (Service) ctx.get(Service.class);
        if (service == null)
        {
            throw new AgentException("System error: Service for SMS provisioning not found in context");
        }

        // look for the SogClient for the subscriber's HLR ID
        final short hlrId = subscriber.getHlrId();

        final String hlrCmds = ServiceSupport.getServiceUnProvisionConfigs(subscriber, service);

        if (hlrCmds == null || hlrCmds.length() == 0)
        {
            // no HLR commands means nothing to do
            return;
        }

        // execute HLR commands using HLR client
        callHlr(ctx, false,subscriber,service,null,null);
    }
    
    /**
     * Executes the HLR commands.
     *
     * @param ctx
     *            The operating context.
     * @param hlrId
     *            ID of the HLR to use.
     * @param hlrCmds
     *            HLR commands to send.
     * @param subscriber
     *            The subsciber in question.
     * @throws AgentException
     *             Thrown if the execution fails.
     */
    @Override
    public void callHlr(final Context ctx, final boolean isProvision, final Subscriber subscriber,
            final com.redknee.app.crm.bean.Service service, String aMsisdn, String bearTypeId) throws ProvisionAgentException
    {
        /**
         * TT#13061332020
         * 
         * We had to explicitly override this method (reason: Different behavior for ServiceTypeEnum.EXTERNAL_PRICE_PLAN) 
         * because onFailure of any external PP service , the result code is set as 
         * SubscriberProvisionHlrGatewayHome.HLR_ERROR. Because the error code is HLR_ERROR; the system does not revert to the 
         * old state of the subscriber on Failure . See SubscriberProvisioningLogHome.java line number: 311 :- which restricts
         * the subscriber's state change if the error code is HLR_ERROR. 
         * This is a hack solution but we have no other choice . 
         */
        if(!ServiceTypeEnum.EXTERNAL_PRICE_PLAN.equals(service.getType()))
        {
            super.callHlr(ctx, isProvision, subscriber, service, aMsisdn, bearTypeId);
        }
        else
        {
            int result = executeHlrCommand(ctx, isProvision, subscriber, service, aMsisdn, bearTypeId);
            if (result != 0)
            {
                ProvisionAgentException exp = new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx)
                        .getUnprovisionErrorMessage(ctx, ExternalAppEnum.SPG, result, service), result,
                        ExternalAppEnum.SPG);
                throw exp;
            }
        }
    }
    
}
