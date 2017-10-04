/*
 * Created on Jul 14, 2005
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
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.pipe.PipelineAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Will resave the subscriber if it finds the MODIFIED key in the context and its value is true
 * 
 * @author psperneac
 *
 */
public class ResaveSubscriber extends PipelineAgent
{
	public static final String MODIFIED="MODIFIED";
	
	public ResaveSubscriber()
	{
		super();
	}

	public ResaveSubscriber(ContextAgent delegate)
	{
		super(delegate);
	}

	/**
	 * @see com.redknee.framework.xhome.context.ContextAgentProxy#execute(com.redknee.framework.xhome.context.Context)
	 */
	public void execute(Context ctx) throws AgentException
	{
		if(isModified(ctx))
		{
			Home home=(Home)require(ctx,this,SubscriberHome.class);
			Subscriber sub=(Subscriber) require(ctx,this,Subscriber.class);
			
			try
			{
				home.store(ctx,sub);
			}
			catch (HomeException e)
			{
				if(LogSupport.isDebugEnabled(ctx))
				{
					new DebugLogMsg(this,e.getMessage(),e).log(ctx);
				}
				
				throw new AgentException(e);
			}
		}
		
		pass(ctx,this);
	}

	public static void setModified(Context ctx)
	{
		ctx.put(MODIFIED,true);
	}
	
	public static void setModified(Context ctx,boolean mod)
	{
		ctx.put(MODIFIED,mod);
	}
	
	public static boolean isModified(Context ctx)
	{
		return ctx.getBoolean(MODIFIED,false);
	}
}
