package com.trilogy.app.crm.bulkloader;

import com.trilogy.app.crm.bulkloader.transaction.CommitRatioTransaction;
import com.trilogy.app.crm.bulkloader.transaction.TransactionPool;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.txn.DefaultTransaction;
import com.trilogy.framework.xhome.txn.Transaction;
import com.trilogy.framework.xhome.txn.TransactionException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * 
 * Agent that commits a DB transaction. non synchronized.
 * 
 * @author abhurke
 *
 */
public class CommittingAgent extends ContextAgentProxy 
{

	private final TransactionPool pool_;
	
	protected CommittingAgent(Context ctx, ContextAgent delegate, int size, int commitRatio)
	{
		super(delegate);
		pool_ = new TransactionPool(size + 1);
		pool_.initialize(ctx, commitRatio);
	}


	@Override
	public void execute(Context ctx) throws AgentException 
	{
		CommitRatioTransaction txn = pool_.poll();
		ctx.put(Transaction.class, txn);
		try
		{
			getDelegate().execute(ctx);
		}
		finally
		{
			ctx.put(Transaction.class, null);
			txn.commit();
			pool_.release(txn);
		}
	}
	
	public void flush()
	{
		pool_.close();
	}
}
