package com.trilogy.app.crm.bulkloader.transaction;

import java.util.ArrayDeque;
import java.util.concurrent.LinkedBlockingDeque;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.txn.TransactionException;
import com.trilogy.framework.xhome.xdb.JDBCXDB;
import com.trilogy.framework.xlog.log.LogSupport;


public class TransactionPool 
{
	
	private static final String TP_OVERUSED_MESSAGE = "All transactions are busy, now new transactions available.";
	private static final String TP_UNINITIALIZED = "Transaction Pool not initialized.";
	private static final String TP_CLOSE_EXCEPTION = "Exception closing Transaction: ";
	
	private final LinkedBlockingDeque<CommitRatioTransaction> pool_;
	private boolean dirty_ = Boolean.FALSE;
	private int size_ = 0;
	
	private Object monitor__ = new Object();

	public TransactionPool(int size)
	{
		pool_ = new LinkedBlockingDeque<CommitRatioTransaction>(size);
		size_ = size;
	}
	
	public void initialize(Context ctx, int commitRatio)
	{
		synchronized (monitor__) 
		{
			if(dirty_)
			{
				return;
			}

			for( int i = 0; i < size_ ; i++ )
			{
				pool_.addFirst(new CommitRatioTransaction(ctx.createSubContext(), commitRatio));
			}
			
			dirty_ = Boolean.TRUE;
		}
	}
	
	private boolean isInitialized()
	{
		return dirty_;
	}

	
	private boolean check()
	{
		synchronized (monitor__) 
		{
			if(pool_.size() == 0)
			{
				try
				{
					monitor__.wait(10000);
					return Boolean.TRUE;
				}
				catch(InterruptedException e)
				{
					return Boolean.FALSE;
				}
			}
			else
			{
				return Boolean.TRUE;
			}
		}
	}
	
	public CommitRatioTransaction poll()
	{
		//synchronized (monitor__) 
		//{
			if(isInitialized())
			{
				try
				{
					return pool_.takeFirst();
				}
				catch (Exception e)
				{
					throw new IllegalStateException(TP_OVERUSED_MESSAGE, e);
				}
			}
			else
			{
				throw new IllegalStateException(TP_UNINITIALIZED);
			}
		//}
	}
	
	public void release(CommitRatioTransaction transaction)
	{
		//synchronized (monitor__) 
		//{
			if(isInitialized())
			{
				try
				{
					pool_.putFirst(transaction);
				}
				catch(InterruptedException e)
				{
					// this will never happen as capacity of this pool will never increase at runtime.
				}
				//monitor__.notifyAll();
			}
			else
			{
				throw new IllegalStateException(TP_UNINITIALIZED);
			}
		//}
	}
	
	private void close(CommitRatioTransaction transaction)
	{
        JDBCXDB xdb = transaction.getXDB();

        transaction.setXDB(null);

        if (xdb != null)
        {
            xdb.close();
        }
	}
	
	public void close()
	{
		synchronized (monitor__)
		{
			if(isInitialized())
			{
				for( CommitRatioTransaction transaction : pool_ )
				{
					try
					{
						transaction.flush();
					}
					catch(TransactionException e)
					{
						LogSupport.major(transaction.getContext(), this, TP_CLOSE_EXCEPTION, e);
					}
					finally
					{
						close(transaction);
					}
				}
			}
			else
			{
				throw new IllegalStateException(TP_UNINITIALIZED);
			}
		}
	}
}
