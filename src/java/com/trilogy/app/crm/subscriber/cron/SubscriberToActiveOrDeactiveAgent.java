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
package com.trilogy.app.crm.subscriber.cron;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.filter.EitherPredicate;
import com.trilogy.app.crm.subscriber.filter.SubscriberPostScheduleStartDatePredicate;
import com.trilogy.app.crm.subscriber.filter.SubscriberPostScheduledEndDatePredicate;

/**
 * @author jchen
 *
 * Activate or Deactivate subscribers 
 * not longer in use, function move to SubscriberFutureActiveOrDeactiveAgent.
 */
public class SubscriberToActiveOrDeactiveAgent   implements ContextAgent
{

	public SubscriberToActiveOrDeactiveAgent()
	{
		
	}
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
	public void execute(Context context) throws AgentException 
	{
		try {
			//The following task is covered in the agent ActivePendingSubAgent.java
			//processSubscribersToActivated(context);
			processSubscribersToDeactivated(context);
		}
		catch(HomeException e)
		{
			throw new AgentException("SubscriberToActiveOrDeactiveTimer error," + e, e);
		}

	}
	
	void processSubscribersToActivated(final Context ctx)
    	throws HomeException
	{
		
		processSubscribers(ctx, getSubscribersToActivated(ctx), SubscriberStateEnum.ACTIVE_INDEX);
	}
	
	void processSubscribersToDeactivated(final Context ctx)
		throws HomeException
	{
		processSubscribers(ctx, getSubscribersToDeactivated(ctx), SubscriberStateEnum.INACTIVE_INDEX);		
	}
	
	public final static int MAX_ERROR = 10;
	protected void processSubscribers(final Context ctx, Collection subs, int targetState)
	{
		Iterator it = subs.iterator();
		Home home = (Home)ctx.get(SubscriberHome.class);
		int maxErr = 0;
		while (it.hasNext())
		{
			Subscriber sub = (Subscriber)it.next();
			sub.setState(SubscriberStateEnum.get((short)targetState));
			try
			{
				home.store(ctx, sub);
			}
			catch(HomeException e)
			{
				maxErr++;
				new DebugLogMsg(this, "Error for processing scheduled subscribers for state " + targetState + ", sub=" + sub.getId(), e).log(ctx);
				if (maxErr > MAX_ERROR)
                {
                    break;
                }
			}
		}
	}
	
	Collection getSubscribersToActivated(Context ctx) throws HomeException
	{
		final Home subHome = (Home) ctx.get(SubscriberHome.class);
		final SubscriberPostScheduleStartDatePredicate predicate = new SubscriberPostScheduleStartDatePredicate(new Date());
		  Collection subscribers = subHome.where(ctx,new EitherPredicate(predicate, predicate.createStatement(ctx))).selectAll();
	    return subscribers;
	}
	
	Collection getSubscribersToDeactivated(Context ctx) throws HomeException
	{
		final Home subHome = (Home) ctx.get(SubscriberHome.class);
		
		// TODO - USE ELANG!
		final SubscriberPostScheduledEndDatePredicate predicate = new SubscriberPostScheduledEndDatePredicate(new Date());
		Collection subscribers = subHome.where(ctx,new EitherPredicate(predicate, predicate.createStatement(ctx))).selectAll();
		
	    return subscribers;
	}
}
