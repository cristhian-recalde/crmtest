package com.trilogy.app.crm.creditcategoryupdate.task;



import com.trilogy.app.crm.creditcategoryupdate.visitor.accountprocessing.AccountCreditCategoryUpdateAssignmentVisitor;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;


public class CreditCategoryUpdateLifecycleAgent extends LifecycleAgentScheduledTask {

	
	private static final long serialVersionUID = 1L;
	

    /**
     * Creates a CreditCategoryUpdateLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public CreditCategoryUpdateLifecycleAgent(Context ctx, String agentId) throws AgentException
    {
        super(ctx, agentId);
    }
    
    /**
     * {@inheritDoc}
     * @throws HomeException 
     * @throws HomeInternalException 
     */
    protected void start(Context ctx) throws LifecycleException, HomeInternalException, HomeException
    {
        try
        {
        	AccountCreditCategoryUpdateAssignmentVisitor visitor = new AccountCreditCategoryUpdateAssignmentVisitor(this);
        	LogSupport.info(ctx, this,"Credit Category Update Scheduler is Started!!!!!!!!" );
        	visitor.visit(ctx,null);        	
        }
         catch (AbortVisitException e) {
			// TODO Auto-generated catch block
        	 final String message = e.getMessage();
             LogSupport.minor(ctx, getClass().getName(), message,e);
		} catch (AgentException e) {
			final String message = e.getMessage();
            LogSupport.minor(ctx, getClass().getName(), message,e);
		}
    	LogSupport.info(ctx, this,"Credit Category Update Scheduler is Ended!!!!!!!!" );
    }

}
