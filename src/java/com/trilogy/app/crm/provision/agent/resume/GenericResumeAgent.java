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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.provision.gateway.SPGParameter;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.provision.service.ErrorCode;
import com.trilogy.app.crm.provision.service.param.CommandID;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;


/**
 * Agent that suspend generic services from the HLR.
 *
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class GenericResumeAgent extends CommonResumeAgent
{

    /**
     * Resumes HLR services to HLR. Context must contain the subscriber to be
     * installed keyed by Subscriber.class. Context must contain Service to retrieve
     * additional params needed associated with this service.
     *
     * @param ctx
     *            The operating context.
     * @throws AgentException
     *             Thrown if there are problems resuming HLR services.
     */
    public void execute(final Context ctx) throws AgentException
    {
        final Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class, null);
        if (subscriber == null)
        {
            throw new AgentException("System error: No subscriber to provision");
        }

        if (ctx.get(Account.class, null) == null)
        {
            throw new AgentException("System error: subscriber's account not found");
        }

        final Service service = (Service) ctx.get(Service.class, null);


        // Call HLR
        if (SystemSupport.needsHlr(ctx))
        {
            Context subCtx = ctx.createSubContext(); 
            
            subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                    String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_SERVICE)); 
            final Set<Long> ids = new HashSet<Long>();
            ids.add(Long.valueOf(ServiceProvisioningGatewaySupport.HLR_SERVICE_SPG_SERVICE_ID));
            Subscriber oldSub = (Subscriber)ctx.get(Lookup.OLD_FROZEN_SUBSCRIBER, subscriber);
            
            subCtx.put(SPG_PROVISIONING_CUSTOM_OLD_IMSI,oldSub.getIMSI());
            subCtx.put(SPG_PROVISIONING_CUSTOM_OLD_VOICE_MSISDN, oldSub.getMsisdn()); 
            subCtx.put(SPG_PROVISIONING_CUSTOM_OLD_LANGUAGE, oldSub.getBillingLanguage());
            
            final Collection<SPGParameter>[] params = ServiceProvisioningGatewaySupport.collectParameterDefinitions(subCtx, ids);
            final Map<Integer, String> values = ServiceProvisioningGatewaySupport.collectParameters(subCtx, 
                    params[ServiceProvisioningGatewaySupport.ALL], subscriber);   
            
             
             try
            {
                 long response = ServiceProvisioningGatewaySupport.execute(subCtx, CommandID.SERVICE_RESUME,
                        service.getID(), values, subscriber);

                
                if (response  != ErrorCode.SUCCESS)
                {
                    throw new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx).getSuspensionErrorMessage(
                            ctx, ExternalAppEnum.HLR, (int)response, service), (int)response,
                            ExternalAppEnum.HLR);
                }
    
            }
            catch (Exception e)
            {
                throw new AgentException(e);
            }
        }
    }
}
