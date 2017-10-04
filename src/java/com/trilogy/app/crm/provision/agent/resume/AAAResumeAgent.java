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

import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.aaa.AAAClient;
import com.trilogy.app.crm.provision.AAAAgentBase;
import com.trilogy.app.crm.provision.AAAProvisionAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Provides a ContextAgent for suspend AAA services.
 *
 * @author kumaran.sivasubramaniam@redknee.com
 */
public
class AAAResumeAgent extends AAAAgentBase 
{
    
    
    public void execute(final Context context)
    throws AgentException
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this," AAA Resume currently follows the same logic as Provision", null).log(context);
        }
        
        new AAAProvisionAgent().execute(context);
        
    }

    @Override
    protected void processSubscriber(Context context, AAAClient client, Service service, Subscriber subscriber)
            throws AgentException
    {
        // TODO Auto-generated method stub
        
    }

} // class
