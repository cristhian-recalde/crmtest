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
package com.trilogy.app.crm.state;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.provision.agent.resume.AlcatelResumeAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * This agent calls the Alcatel SSC Resume Agent to resume the Alcatel Service.
 * @author angie.li@redknee.com
 *
 */
public class ResumeAlcatelServiceUpdateAgent implements ServiceStateUpdateAgent
{

    public void update(Context ctx, Subscriber subscriber, Service service)
            throws HomeException 
    {
        Context subCtx = ctx.createSubContext();
        
        subCtx.put(Subscriber.class, subscriber);
        subCtx.put(Service.class, service);
        
        AlcatelResumeAgent agent = new AlcatelResumeAgent();
        try
        {
            agent.execute(subCtx);
        }
        catch (AgentException e)
        {
            /* Do we want to continue to throw this exception?
             * All exceptions from Alcatel provisioning are not supposed to halt provisioning in CRM.
             * The errors have already been logged by the agent above.
             */ 
            throw new HomeException(e.getMessage(), e);
        }
        
    }

}
