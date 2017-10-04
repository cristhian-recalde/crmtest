/*
 *  IPCUnprovisionAgent.java
 *
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
package com.trilogy.app.crm.provision.agent.suspend;

import com.trilogy.app.crm.provision.IPCUnprovisionAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author ksivasubramaniam
 *
 */
public class IPCSuspendAgent extends CommonSuspendAgent
{    
    public void execute(Context ctx) throws AgentException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this," IPC Suspend currently follows the same logic as Unprovision", null).log(ctx);
        }
        
        new IPCUnprovisionAgent().execute(ctx);
    }


}
