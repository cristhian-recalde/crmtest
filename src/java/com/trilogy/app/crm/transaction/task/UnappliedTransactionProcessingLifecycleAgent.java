package com.trilogy.app.crm.transaction.task;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * <p>This CRON will visit every record in the UnappliedTransaction table and request credit for the respective
 * transaction amount. This CRON will then move the transaction from UnappliedTransaction to Transaction table.</p>
 * <br>
 * </p><b>Duplicate check:</b> If a transaction with the same externalTransactionId and amount is present in the Transaction table
 * then the transaction from the unappliedTransaction table will be treated as duplicate and will be deleted.</p>
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class UnappliedTransactionProcessingLifecycleAgent extends LifecycleAgentScheduledTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UnappliedTransactionProcessingLifecycleAgent(Context ctx, final String agentId) throws AgentException
    {
        super(ctx, agentId);
    }

    protected void start(Context ctx) throws LifecycleException
    {
		
		final Home unappliedTransactionHome = (Home) ctx.get(Common.UNAPPLIED_TRANSACTION_HOME);	
		final LifecycleAgentScheduledTask agent_ = this;
		try
		{
		    Visitor visitor = new UnappliedTransactionProcessingVisitor(agent_);
			unappliedTransactionHome.forEach(ctx, visitor);
		} 
        catch (AbortVisitException e)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("Lifecycle agent ");
            cause.append(getAgentId());
            cause.append(" was interrupted: ");
            cause.append(e.getMessage());
            LogSupport.major(ctx, this, cause.toString(), e);
        }
		catch (Exception e) 
		{
            final String message = "Unable to process all the unapplied transactions: " + e.getMessage();
            LogSupport.major(ctx, getClass().getName(), message, e);
		}

	}

}
