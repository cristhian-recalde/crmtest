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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.CommonUnprovisionAgent;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionHlrGatewayHome;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.provision.gateway.SPGParameter;
import com.trilogy.app.crm.provision.service.ErrorCode;
import com.trilogy.app.crm.provision.service.param.CommandID;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;

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
 * Common class for Wimax resume agents.
 *
 * @author Anuradha.malvadkar@redknee.com @9.7.2
 */
public class WimaxResumeAgent extends CommonResumeAgent
{

     
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

         com.redknee.app.crm.bean.Service service = (Service) ctx.get(Service.class, null);
         if(service ==null)
         {
        	  service = (com.redknee.app.crm.bean.Service) ctx.get(com.redknee.app.crm.bean.core.Service.class,null);
         }

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
                     throw new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx).getResumeErrorMessage(
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
