/**
 * @Filename : TransferUnProvisionAgent.java
 * @Author   : Daniel Zhang
 * @Date     : Jul 17, 2004
 * 
 *  Copyright (c) Redknee, 2004
 *        - all rights reserved
 */

package com.trilogy.app.crm.provision.agent.suspend;

import com.trilogy.app.crm.provision.TransferUnprovisionAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Class Description: just a place holder
 */
public class TransferSuspendAgent
extends CommonSuspendAgent
{
	
	public void execute(Context ctx) throws AgentException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, " Transfer Suspend currently follows the same logic as Unprovision", null).log(ctx);
        }
        new TransferUnprovisionAgent().execute(ctx);
    }
}