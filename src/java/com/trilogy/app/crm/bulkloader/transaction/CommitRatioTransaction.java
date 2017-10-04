package com.trilogy.app.crm.bulkloader.transaction;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.txn.DefaultTransaction;
import com.trilogy.framework.xhome.txn.TransactionException;

public class CommitRatioTransaction extends DefaultTransaction 
{
	
	private CommitRatioCounter commitRatio_  = null;
	
	public CommitRatioTransaction(Context ctx, int commitRatio) 
	{
		super(ctx);
		commitRatio_ = new CommitRatioCounter(commitRatio);
	}

	public CommitRatioTransaction(Context ctx) 
	{
		this(ctx, 0);
	}

	@Override
	public void commit() throws TransactionException
	{
		if(commitRatio_.incrementAndCheck())
		{
			super.commit();
		}
	}

	public void flush() throws TransactionException
	{
		commitRatio_.resetCounter();
		super.commit();
	}
}
