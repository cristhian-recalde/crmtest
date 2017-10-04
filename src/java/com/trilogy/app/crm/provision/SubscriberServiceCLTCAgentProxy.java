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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Service;


/**
 * @author jchen
 */
public class SubscriberServiceCLTCAgentProxy extends ContextAgentProxy 
{
    public SubscriberServiceCLTCAgentProxy(ContextAgent delegate)
    {
        super(delegate);
    }
    
    @Override
    public void execute(Context ctx) throws AgentException
    {
        delegate(ctx);
        
        Subscriber subscriber = (Subscriber)ctx.get(Subscriber.class);
        Service service = (Service)ctx.get(Service.class);
		if (subscriber != null && service != null)
		{
		    if (getDelegate() instanceof CommonUnprovisionAgent)
            {
                subscriber.serviceUnProvisioned(ctx, service);
            }
            else if (getDelegate() instanceof CommonProvisionAgent)
            {
                subscriber.serviceProvisioned(ctx, service);
            }
            
		}
    }
}
