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
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;

/**
 * For every Service in the list of  subscriber service, it will execute the Service Action update executor
 * @author arturo.medina@redknee.com
 * 
 * Ported from CRM 7_4.
 * @since CRM 8.2
 * @author angie.li@redknee.com
 */
public class ServiceUpdateActionWorker
{

    /**
     * Sets up the map of agents to execute for every service
     * @param agents
     */
    public void setAgents(Map<ServiceTypeEnum, ServiceStateUpdateAgent> agents)
    {
        agents_ = agents;
    }

    /**
     * Gets the price plan version of the subscriber and for evey service on the PPV
     * The worker will call the suspend agent to suspend the service
     * @param ctx
     * @param subscriber
     * @throws StateChangeException 
     */
    @SuppressWarnings("unchecked")
    public void executeUpdateForServices(Context ctx, Subscriber subscriber) throws StateChangeException
    {
        CompoundIllegalStateException exceptionsServiceUpdate = null;
        Collection<Service> services;
        try
        {
            services = ServiceSupport.transformServiceIdToObjects(ctx, SubscriberServicesSupport
                    .getServicesEligibleForProvisioning(ctx, subscriber.getId()));
        }
        catch (HomeException homeException)
        {
            // TODO Auto-generated catch block
            throw new StateChangeException(homeException);
        }
        for (Service service : services)
        {
            try
            {
                executeUpdateForServices(ctx, subscriber, service);
            }
            catch (HomeException e)
            {
                if (null == exceptionsServiceUpdate)
                {
                    exceptionsServiceUpdate = new CompoundIllegalStateException();
                }
                new MajorLogMsg(this, "Home exception when trying to update the service", e).log(ctx);
                exceptionsServiceUpdate.thrown(e);
            }
        }
        if (null != exceptionsServiceUpdate && exceptionsServiceUpdate.getSize() > 0)
        {
            throw new StateChangeException(exceptionsServiceUpdate);
        }
        new DebugLogMsg(this, "executeUpdateForServices(ctx,Subscriber) completed successfully", null).log(ctx);
    }


    /**
     * Executes the service state change for every service fee in the list of services 
     * @param ctx
     * @param sub 
     * @param service
     */
    public void executeUpdateForServices(Context ctx, Subscriber sub, Service service) throws HomeException
    {
        if (service != null)
        {
            try
            {
                ServiceStateUpdateAgent agent = agents_.get(service.getType());
                if (agent != null)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Agent registered for service, calling for service ["
                                + service.getIdentifier() + "] and type [" + service.getType() +"]");
                    }
                    agent.update(ctx, sub, service);
                }
                else
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "No agent registered for service, ignoring this service ", null).log(ctx);
                    }
                }
            }
            catch (HomeException e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this,
                            "Home exception when trying to get the service ID [" + service.getIdentifier() +"]", e).log(ctx);
                }
                throw e;
            }
        }
    }
    
    /**
     * the agents to execute
     */
    private Map<ServiceTypeEnum, ServiceStateUpdateAgent> agents_;

}
