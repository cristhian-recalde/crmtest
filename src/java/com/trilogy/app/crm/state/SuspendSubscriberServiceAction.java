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

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;

/**
 * Manages the suspension of the subscriber as well as the service suspension for that particular subscriber
 * For now the only logic this class will support is suspending services. More suspend logic can be added to the subcriber.
 * 
 * @author arturo.medina@redknee.com
 * 
 * Ported from CRM 7_4, part of the Subscriber State Service Update provisioning module.
 * @since CRM 8.2
 * @author angie.li@redknee.com
 */
public class SuspendSubscriberServiceAction implements SubscriberServiceStateAction
{

    /**
     * Default constructor, initializes a worker to suspend services
     */
    public SuspendSubscriberServiceAction()
    {
        initSuspendServiceWorker();
    }
    
    /**
     * Initialize the suspend service worker
     */
    private void initSuspendServiceWorker()
    {
        worker_ = new ServiceUpdateActionWorker();
        
        Map<ServiceTypeEnum, ServiceStateUpdateAgent> agents = new HashMap<ServiceTypeEnum, ServiceStateUpdateAgent>();
        
        /* Adding the Alcatel Suspend handler here means that it will be handled in the same way for
         * Postpaid and Prepaid subscriptions, even though we have said in design discussions that
         * CRM doesn't _strictly_ support Alcatel SSC services for prepaid accounts.  */ 
        agents.put(ServiceTypeEnum.ALCATEL_SSC, new SuspendAlcatelServiceUpdateAgent());
        agents.put(ServiceTypeEnum.BLACKBERRY, new SuspendBlackberryServiceUpdateAgent());
        agents.put(ServiceTypeEnum.EXTERNAL_PRICE_PLAN, new SuspendExternalPriceplanServiceUpdateAgent());
        agents.put(ServiceTypeEnum.WIMAX, new SuspendWimaxServiceUpdateAgent());
        
        worker_.setAgents(agents);
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void execute(Context ctx, Subscriber subscriber)
            throws StateChangeException
    {
        suspendServices(ctx, subscriber);
    }


    /**
     * Suspends the services for that particular subscriber
     * @param ctx
     * @param subscriber
     * @throws StateChangeException
     */
    private void suspendServices(Context ctx, Subscriber subscriber) throws StateChangeException
    {
        worker_.executeUpdateForServices(ctx, subscriber);
    }


    /**
     * The worker that suspends the services for a particular subscriber
     */
    private ServiceUpdateActionWorker worker_;
}
