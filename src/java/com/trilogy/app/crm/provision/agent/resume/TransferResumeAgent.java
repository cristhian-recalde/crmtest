/**
 * @Filename : TransferUnProvisionAgent.java
 * @Author   : Daniel Zhang
 * @Date     : Jul 17, 2004
 * 
 *  Copyright (c) Redknee, 2004
 *        - all rights reserved
 */

package com.trilogy.app.crm.provision.agent.resume;

import com.trilogy.app.crm.provision.TransferProvisionAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Class Description: just a place holder
 */
public class TransferResumeAgent
extends CommonResumeAgent
{
	
	public void execute(Context ctx)
		throws AgentException
	{
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this," Transfer Resume currently follows the same logic as Provision", null).log(ctx);
        }
        
	    new TransferProvisionAgent().execute(ctx);
	}
}