package com.trilogy.app.crm.creditcategoryupdate.visitor;

import java.sql.SQLException;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.creditcategoryupdate.AccountCreditCategoryUpdateContextAgent;
import com.trilogy.app.crm.creditcategoryupdate.visitor.accountprocessing.AccountCreditCategoryUpdateAssignmentVisitor;
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
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

public class AccountCreditCategoryUpdateThreadPoolVisitor extends ContextAwareSupport implements Visitor  
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ThreadPool threadPool_;
    
    private AccountCreditCategoryUpdateContextAgent visitor_;
    
    /**
     * Create a new AccountCreditCategoryUpdateAssignmentVisitor visitor.
     * @param ctx
     * @param threads
     * @param queueSize
     * @param accountCreditCategoryUpdateAssignmentVisitor
     * @param agent
     */
    public AccountCreditCategoryUpdateThreadPoolVisitor(final Context ctx, final int threads, final int queueSize, final AccountCreditCategoryUpdateAssignmentVisitor accountCreditCategoryUpdateAssignmentVisitor, final LifecycleAgentSupport agent)
    {
        setContext(ctx);
        visitor_ = new AccountCreditCategoryUpdateContextAgent(accountCreditCategoryUpdateAssignmentVisitor);
        threadPool_ = new ThreadPool(MODULE, queueSize, threads, new PMContextAgent(MODULE, AccountCreditCategoryUpdateThreadPoolVisitor.class.getSimpleName() , visitor_));
        agent_ = agent;
    }

  

	/**
     * {@inheritDoc}
     */
    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException 
    {
    	PMLogMsg pm = new PMLogMsg(this.getClass().getSimpleName(), "AssiginingCreditCategoryUpdateForAccount");
    	if (LogSupport.isDebugEnabled(ctx)){
    		LogSupport.debug(ctx, this,"AccountCreditCategoryUpdateThreadPoolVisitor is started!!!!" );
    	}
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
	        	}else{ 
	        		account = (Account)obj;
	        	}
        	try{
        			subContext.put(AccountCreditCategoryUpdateContextAgent.CREDITCATEGORYUPDATE_ACCOUNT, account);
        			if (LogSupport.isDebugEnabled(ctx)){
        				LogSupport.debug(ctx, this,"AccountCreditCategoryUpdateThreadPoolVisitor before executor is started:: "+account );
        			}
        			threadPool_.execute(subContext);
        			if (LogSupport.isDebugEnabled(ctx)){
        				LogSupport.debug(ctx, this,"AccountCreditCategoryUpdateThreadPoolVisitor after executor is ended:: "+account );
        			}
        	   }catch (AgentException e){
        		   new MinorLogMsg(this, "Error while running credit categiry update policy assignement process for account '" + account.getBAN() + "': " +  e.getMessage(), e).log(getContext());
        	   }catch (Throwable t){
        		   new MinorLogMsg(this, "Unexpected error while running the credit categiry update policy assignement process for account '" + account.getBAN() + "': " +  t.getMessage(), t).log(getContext());
        	}
        }catch (SQLException sqlException){
        			new MinorLogMsg(this, "Unable to retrieve account during task: "+ sqlException.getMessage(), sqlException).log(getContext());
       	}catch (HomeException e) {
       				new MinorLogMsg(this, "Unable to retrieve account during task: "+ e.getMessage(), e).log(getContext());
       	}finally
       	{
       		pm.log(ctx);
       	}
        if (LogSupport.isDebugEnabled(ctx)){
        	LogSupport.debug(ctx, this,"AccountCreditCategoryUpdateThreadPoolVisitor is ended!!!!" );
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

    public static final String MODULE = "Credit Category Update";
    

}

