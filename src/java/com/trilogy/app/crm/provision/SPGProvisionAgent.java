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
import com.trilogy.app.crm.provision.gateway.SPGSkippingException;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;


/**
 * 
 *
 * @author victor.stratan@redknee.com
 * @since 8.5
 */
public class SPGProvisionAgent implements ContextAgent
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {

        final Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class, null);
        if (subscriber == null)
        {
            throw new AgentException("System error: No subscriber in context");
        }

        final Service service = getService(ctx);
        if (!service.getType().equals(ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY))
        {
            throw new AgentException("System error: Incorrect service type mapping.");
        }
        
        Context subCtx = ctx.createSubContext(); 
        subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_SERVICE));
        
        //ServiceProvisioningGatewaySupport.serviceToRemove(ctx, Long.valueOf(service.getID()), service);
       int result = ServiceProvisioningGatewaySupport.prepareAndSendIndividualServiceToSPG(subCtx, subscriber, 
                service,Long.valueOf(service.getSPGServiceType()) ,true, this ) ;
       

       throw new SPGSkippingException("skip updating subscriber service " + result); 

    }

    public Service getService(final Context ctx) throws AgentException
    {
        final Service service = (Service) ctx.get(Service.class);
        if (service == null)
        {
            throw new AgentException("System error: Service for voice provisioning not found in context.");
         
        }
        return service;
    }
}
