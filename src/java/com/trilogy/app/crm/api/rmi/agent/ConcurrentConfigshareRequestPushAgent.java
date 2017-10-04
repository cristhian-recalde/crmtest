/*
� * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.rmi.agent;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author sgaidhani
 * @since 9.7.1
 * This Agent is supposed to push the config share request with given home 
 * and visitor and applicable where clause if any.
 */
public class ConcurrentConfigshareRequestPushAgent implements ContextAgent{

	@Override
	public void execute(Context ctx) throws AgentException {

		LogSupport.info(ctx, this, "Inside ConcurrentConfigshareRequestPushAgent:execute()");

		ConfigShareTask task = (ConfigShareTask) ctx.get(ConfigShareTask.class);

		if(task != null)
		{
			Home home = task.getHome();
			Visitor visitor  = task.getVisitor();
			Object condition = task.getCondition();

			try 
			{
				if(home == null)
				{
					LogSupport.major(ctx, this, "Home not found in the task. Unable to execute config share task.");
					return;
				}

				if(visitor == null)
				{
					LogSupport.major(ctx, this, "Visitor not found in the task. Unable to execute config share task.");
					return;
				}


				if(condition == null)
				{
					home.forEach(ctx, visitor);
				}
				else
				{
					home.forEach(ctx, visitor, condition);
				}
			} 
			catch (HomeException e) 
			{
				StringBuffer sb = new StringBuffer();
				sb.append("HomeExeption encouterred while trying to ConfigShare Requests for :");
				sb.append("Home [");
				sb.append(home);
				sb.append("], Visitor [");
				sb.append(visitor);
				sb.append("], Condition [");
				sb.append(condition);

				LogSupport.major(ctx, this, sb.toString(), e);
			}
		}
		else
		{
			LogSupport.minor(ctx, this, "No ConfigShareTask found in context. Unable to proceed further.");
		}
	}
}
