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

package com.trilogy.app.crm.bas.recharge;


import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.context.PMContextAgent;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Account;


/**
 * This class implements visitor with thread pool, and delegates actually recurring charge
 * on account to ProcessAccountVisitor.
 * 
 * @author joe.chen@redknee.com
 */
public class ProcessAccountThreadPoolVisitor extends ContextAwareSupport implements Visitor, RechargeConstants
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
	

    /**
     * Create a new instance of <code>ProcessAccountThreadPoolVisitor</code>.
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
    public ProcessAccountThreadPoolVisitor(final Context ctx, final int threads, final int queueSize,
        final ContextAgent delegate, final LifecycleAgentSupport agent)
	{
		setContext(ctx);
        threadPool_ = new ThreadPool(POOL_NAME, queueSize, threads, new PMContextAgent(POOL_NAME, ProcessAccountThreadPoolVisitor.class.getSimpleName(), delegate));
        agent_ = agent;
	}
	
    public ProcessAccountThreadPoolVisitor(final Context ctx, final int threads, final int queueSize,
            final ContextAgent delegate)
        {
            this(ctx, threads, queueSize, delegate, null);
        }

	/**
     * Creates recurring charge for each account.
     *
     * @param ctx
     *            The operating context.
     * @param obj
     *            The account to generate recurring charge for.
	 */
    public void visit(final Context ctx, final Object obj)
	{
        if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running.  Remaining subscriptions will be processed next time.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }

        final Context subContext = ctx.createSubContext();
        final Account account = (Account) obj;
        subContext.put(Account.class, account);
        
        try
        {
            threadPool_.execute(subContext);
        }
        catch (final AgentException e)
        {
            new MajorLogMsg(this, "Cannot recurring charge for account: " + account.getBAN() + "," + e.getMessage(), e)
                .log(getContext());
            
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(this, "Unexpected error for recurring charge of account: " + account.getBAN() + ","
                + t.getMessage(), t).log(getContext());
            
        }
		
	}


    /**
     * Returns the thread pool.
     *
     * @return Thread pool.
     */
    protected ThreadPool getPool()
    {
        return threadPool_;
    }

    /**
     * Thread pool.
     */
    private final ThreadPool threadPool_;
    
    private final LifecycleAgentSupport agent_;

    private final static String POOL_NAME = "Recurring Charge (Account)";

}
