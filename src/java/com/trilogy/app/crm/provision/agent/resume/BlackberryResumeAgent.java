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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.state.ResumeBlackberryServiceUpdateAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

public class BlackberryResumeAgent extends CommonResumeAgent
{

    /**
     * @param ctx
     * 
     */
    public void execute(Context ctx) throws AgentException
    {
        Subscriber subscriber = (Subscriber)ctx.get(Subscriber.class);
        if (subscriber == null)
        {
            throw new AgentException("System error: No subscriber to provision");
        }

        Service service = (Service)ctx.get(Service.class);
        
        if (service == null)
        {
            throw new AgentException("System error: Service for blackberry provisioning not found in context");
        }
        
        try
        {
            new ResumeBlackberryServiceUpdateAgent().update(ctx, subscriber, service);
        }
        catch (HomeException e)
        {
            throw new AgentException("BlackBerry service resume failed: " + e.getMessage(), e);
        }
    }
}