/**
 * @Filename : TransferProvisionAgent.java
 * @Author   : Daniel Zhang
 * @Date     : Jul 17, 2004
 * 
 *  Copyright (c) Redknee, 2004
 *        - all rights reserved
 */

package com.trilogy.app.crm.provision;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Class Description: place holder class
 */
public class TransferProvisionAgent extends CommonProvisionAgent
{
	
	public void execute(Context ctx)
		throws AgentException
	{
		Subscriber subscriber= (Subscriber)ctx.get(Subscriber.class, null);
		if (subscriber == null)
		{
			throw new AgentException("System error: No subscriber to provision");
		}
		Service service;

		service = (Service)ctx.get(Service.class, null);

		if (service != null && LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this, "Provisioning service "+service.getName()
					+ " for subscriber:"+subscriber.getMSISDN(),
					null).log(ctx);
		}
	}
}
