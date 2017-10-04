/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.dunning.visitor;

import java.sql.SQLException;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.dunning.DunningAgent;
import com.trilogy.app.crm.dunning.DunningConstants;
import com.trilogy.app.crm.dunning.DunningProcessContextAgent;
import com.trilogy.app.crm.support.AccountSupport;
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
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * This visitor will use a threadpool to start the processing of dunning for accounts.
 * @author Marcio Marques
 */

public class DunningProcessThreadPoolVisitor extends ContextAwareSupport implements Visitor  
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ThreadPool threadPool_;
    
    private DunningAgent dunningAgent_;
    
    /**
     * Create a new DunningProcessThreadPoolVisitor visitor.
     * @param ctx
     * @param threads
     * @param queueSize
     * @param dunningVisitor
     * @param agent
     */
    public DunningProcessThreadPoolVisitor(final Context ctx, final int threads, final int queueSize, 
    		final LifecycleAgentSupport agent,DunningAgent executer)
    {
        setContext(ctx);
        dunningAgent_ = executer;
        threadPool_ = new ThreadPool(MODULE, queueSize, threads, new PMContextAgent(MODULE, DunningProcessThreadPoolVisitor.class.getSimpleName() , executer));
        agent_ = agent;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException 
    {
        if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running.  Remaining accounts will be processed next time.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }

        Context subContext = ctx.createSubContext();
        
        try{
    			subContext.put(DunningConstants.DUNNINGAGENT_OBJECT_TOPROCESS, obj);
    			threadPool_.execute(subContext);
    	   }catch (AgentException e){
    		   new MinorLogMsg(this, "Error while running dunning process for account "  + e.getMessage(), e).log(getContext());
    	   }catch (Throwable t){
    		   new MinorLogMsg(this, "Unexpected error while running the dunning process for account " + t.getMessage(), t).log(getContext());
    	}
        
    }
    protected ThreadPool getPool() 
    {
        return threadPool_; 
    }
    
    public List<String> getFailedBANs()
    {
        return dunningAgent_.getFailedBANs();
    }
    
    private final LifecycleAgentSupport agent_;

    public static final String MODULE = "Dunning process";
    

}
