package com.trilogy.app.crm.dunning.visitor.accountprocessing;

import java.util.Date;

import com.trilogy.app.crm.dunning.DunningAccountProcessingAgent;
import com.trilogy.app.crm.dunning.DunningAgent;
import com.trilogy.app.crm.dunning.DunningProcessHelper;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningXStatementVisitor;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;

public class DunningProcessXStatementVisitor extends AbstractDunningXStatementVisitor
{
	public DunningProcessXStatementVisitor(Date runningDate, LifecycleAgentSupport lifecycleAgent)
    {
        super(runningDate, new DunningProcessingAccountVisitor(runningDate), lifecycleAgent);
    }
	
	protected String getJoinSql(Context context) throws HomeInternalException, HomeException{
    	String sql = new String(" adbt.duedate >="+ DunningProcessHelper.getOldestDate(context,getRunningDate())+
    										  " and adbt.accumulateddebt > 0");
		return sql;
    }
	
	/**
     * {@inheritDoc}
     */
    public synchronized void onItemFailure()
    {
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void onItemSuccess()
    {
    }

	@Override
	protected DunningAgent getExecuter() {
		return new DunningAccountProcessingAgent(this);
	}
}
