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
package com.trilogy.app.crm.subscriber;

import java.sql.SQLException;

import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.context.PMContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bas.recharge.RechargeConstants;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * 
 *
 * @author shailesh.makhijani
 * @since 9.7.0
 */

public class GenericProcessSubscriberThreadPoolVisitor extends  ContextAwareSupport 
	implements RechargeConstants, Visitor {

	private static final long serialVersionUID = 1L;
	/**
	 * Create a new instance of <code>SubscriberThreadPoolVisitor</code>.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param threads
	 *            Number of threads.
	 * @param queueSize
	 *            Queue size.
	 * @param delegate
	 *            Delegate of this visitor.
	 */
	public GenericProcessSubscriberThreadPoolVisitor(final Context ctx, final int threads, final int queueSize,
			final ContextAgent delegate)
	{
		this(ctx, threads, queueSize, delegate, null);
	}

	public GenericProcessSubscriberThreadPoolVisitor(final Context ctx, final int threads, final int queueSize,
			final ContextAgent delegate, final LifecycleAgentSupport agent)
	{
		setContext(ctx);
		threadPool_ = new ThreadPool(POOL_NAME, queueSize, threads, new PMContextAgent(POOL_NAME, GenericProcessSubscriberThreadPoolVisitor.class.getSimpleName() , delegate));
		agent_ = agent;
	}
	/**
	 * @param ctx
	 *            The operating context.
	 * @param obj
	 *            The subscriber whose failed services will be retried.
	 */
	public void visit(final Context ctx, final Object obj)
	{
		final Context subContext = ctx.createSubContext();
		String subscriberId;

		if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()))
		{
			String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running.  Remaining subscriptions will be processed next time.";
			new InfoLogMsg(this, msg, null).log(ctx);
			throw new AbortVisitException(msg);
		}

		try
		{
			subscriberId = ((XResultSet)obj).getString(1);
			try
			{
				Subscriber subscriber = SubscriberSupport.getSubscriber(subContext, subscriberId);
				subContext.put(Subscriber.class, subscriber);
				
				if (subscriber == null)
				{
					throw new HomeException("Developer error: Subscriber was not supposed to be null during recurring recharge");
				}

				threadPool_.execute(subContext);
			}
			catch (final AgentException e)
			{
				new MajorLogMsg(this, "Cannot process subscriber: " + subscriberId + "," + e.getMessage(), e)
				.log(getContext());

			}
			catch (final Throwable t)
			{
				new MajorLogMsg(this, "Unexpected error for subscriber: " + subscriberId + ","
						+ t.getMessage(), t).log(getContext());
			}
		}
		catch (SQLException sqlException)
		{
			new MinorLogMsg(this, "Unable to retrieve subscriber during retry for failed subscriber service process: " + sqlException.getMessage(), sqlException).log(getContext());
		}

	}


	/**
	 * Returns the thread pool.
	 *
	 * @return Thread pool.
	 */
	public ThreadPool getPool()
	{
		return threadPool_;
	}

	/**
	 * Thread pool.
	 */
	private final ThreadPool threadPool_;
	private final LifecycleAgentSupport agent_;
	private final static String POOL_NAME = "Retry Subscriber Services";
}
