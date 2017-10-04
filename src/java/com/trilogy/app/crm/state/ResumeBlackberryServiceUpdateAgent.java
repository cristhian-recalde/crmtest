/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.state;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.provision.gateway.SPGParameter;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.service.blackberry.IServiceBlackberry;
import com.trilogy.service.blackberry.ServiceBlackberryException;
import com.trilogy.service.blackberry.model.Attributes;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.provision.service.ErrorCode;
import com.trilogy.app.crm.provision.service.param.CommandID;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;

/**
 * @author arturo.medina@redknee.com
 *
 */
public class ResumeBlackberryServiceUpdateAgent extends AbstractBlackberryServiceUpdateAgent
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected void callBlackberryService(Context ctx,
            IServiceBlackberry bbService, Service service, Subscriber subscriber)
            throws ServiceBlackberryException
    {
        long[] services = BlackberrySupport.getBlackberryServicesIdsForService(ctx, service.getID());
        try
        {
            Attributes attr = BlackberrySupport.getBlackberryAttributesBasicAttributes(ctx, subscriber);
            bbService.resume(ctx, subscriber.getSpid(), services, attr);
        }
        catch (HomeException homeEx)
        {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHLRCommand(Subscriber subscriber, Service service)
    {
        return ServiceSupport.getServiceResumeConfigs(subscriber, service);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void callHlr(Context ctx, Service service, Subscriber subscriber) throws AgentException
    {
    if (SystemSupport.needsHlr(ctx))
    	{
    	ctx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_SERVICE));
    	final Set<Long> ids = new HashSet<Long>();
    	ids.add(Long.valueOf(ServiceProvisioningGatewaySupport.HLR_SERVICE_SPG_SERVICE_ID));
    	final Collection<SPGParameter>[] params = ServiceProvisioningGatewaySupport.collectParameterDefinitions(ctx, ids);
    	final Map<Integer, String> values = ServiceProvisioningGatewaySupport.collectParameters(ctx, 
                params[ServiceProvisioningGatewaySupport.ALL], subscriber);
    	if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Calling HLR to Resume Blackberry service " +service.toString());
        }
	    try{
	    	long response = ServiceProvisioningGatewaySupport.execute(ctx, CommandID.SERVICE_RESUME,
	    			service.getID(), values, subscriber);
	    
	    	if (response  != ErrorCode.SUCCESS)
	    	{
	    		if (LogSupport.isDebugEnabled(ctx))
	            {
	                LogSupport.debug(ctx, this, "Response from SPG :"+response);
	            }
	    		throw new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx).getSuspensionErrorMessage(
	    				ctx, ExternalAppEnum.HLR, (int)response, service), (int)response,
	    				ExternalAppEnum.HLR);
	    	}
	    }
	    catch (Exception e)
    	{
    		LogSupport.minor(ctx, this, "Exception: "+ e);
    		throw new AgentException(e);
    	}
    	}
    }
}
