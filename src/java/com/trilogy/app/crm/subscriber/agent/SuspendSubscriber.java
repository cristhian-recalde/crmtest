/*
 * Created on Jul 13, 2005
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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.subscriber.agent;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.pipe.PipelineAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

public class SuspendSubscriber extends PipelineAgent
{

	public SuspendSubscriber()
	{
		super();
	}

	public SuspendSubscriber(ContextAgent delegate)
	{
		super(delegate);
	}

	/**
	 * @see com.redknee.framework.xhome.context.ContextAgentProxy#execute(com.redknee.framework.xhome.context.Context)
	 */
	public void execute(Context ctx) throws AgentException
	{
		try
		{
			Subscriber sub=(Subscriber) require(ctx,this,Subscriber.class);
			
			if(sub.isAboveCreditLimit())
			{
			    //For TT#12121848012 
			    if(!sub.isPrepaid())
                {
                    sub.setState(SubscriberStateEnum.SUSPENDED);
                    ResaveSubscriber.setModified(ctx);
                }
			}
		}
		catch(Throwable th)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this,th.getMessage(),th).log(ctx);
			}
		}
		
		pass(ctx,this);
	}

	
}
