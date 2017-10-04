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
import com.trilogy.app.crm.dunning.AccountDunningPolicyContextAgent;
import com.trilogy.app.crm.dunning.visitor.accountprocessing.AccountDunningPolicyAssignmentVisitor;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.context.PMContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * This visitor will use a threadpool to start the processing accounts for assigning Dunning policy
 * @author Sapan Modi
 */

public class AccountDunningPolicyThreadPoolVisitor extends ContextAwareSupport implements Visitor  
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ThreadPool threadPool_;
    
    private AccountDunningPolicyContextAgent visitor_;
    
    /**
     * Create a new AccountDunningPolicyThreadPoolVisitor visitor.
     * @param ctx
     * @param threads
     * @param queueSize
     * @param accountDunningPolicyAssignmentVisitor
     * @param agent
     */
    public AccountDunningPolicyThreadPoolVisitor(final Context ctx, final int threads, final int queueSize, final AccountDunningPolicyAssignmentVisitor accountDunningPolicyAssignmentVisitor, final LifecycleAgentSupport agent)
    {
        setContext(ctx);
        visitor_ = new AccountDunningPolicyContextAgent(accountDunningPolicyAssignmentVisitor);
        threadPool_ = new ThreadPool(MODULE, queueSize, threads, new PMContextAgent(MODULE, AccountDunningPolicyThreadPoolVisitor.class.getSimpleName() , visitor_));
        agent_ = agent;
    }

  

	/**
     * {@inheritDoc}
     */
    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException 
    {
    	PMLogMsg pm = new PMLogMsg(this.getClass().getSimpleName(), "AssiginingDunningPolicyForAccount");
        if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running.  Remaining accounts will be processed next time.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }

        Context subContext = ctx.createSubContext();
        Account account =null;
        try
        {
	        	if(!(obj instanceof Account)){
		        	String accountID = ((XResultSet)obj).getString(1);
		        	account = AccountSupport.getAccount(subContext, accountID);
	        	}else{ //Flow of BRE
	        		account = (Account)obj;
	        	}
        	try{
        			subContext.put(AccountDunningPolicyContextAgent.DUNNINGPOLICY_ACCOUNT, account);
        			threadPool_.execute(subContext);
        	   }catch (AgentException e){
        		   new MinorLogMsg(this, "Error while running dunning policy assignement process for account '" + account.getBAN() + "': " +  e.getMessage(), e).log(getContext());
        	   }catch (Throwable t){
        		   new MinorLogMsg(this, "Unexpected error while running the dunning policy assignement process for account '" + account.getBAN() + "': " +  t.getMessage(), t).log(getContext());
        	}
        }catch (SQLException sqlException){
        			new MinorLogMsg(this, "Unable to retrieve account during task: "+ sqlException.getMessage(), sqlException).log(getContext());
       	}catch (HomeException e) {
       				new MinorLogMsg(this, "Unable to retrieve account during task: "+ e.getMessage(), e).log(getContext());
       	}finally
       	{
       		pm.log(ctx);
       	}
    }
    public ThreadPool getPool() 
    {
        return threadPool_; 
    }
    
    public List<String> getFailedBANs()
    {
        return visitor_.getFailedAssignedBANs();
    }
    
    private final LifecycleAgentSupport agent_;

    public static final String MODULE = "Dunning Policy Assignment";
    

}
