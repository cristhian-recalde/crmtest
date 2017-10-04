package com.trilogy.app.crm.bulkloader;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * A Visitor which delegates task for this object to a ThreadPool.
 * 
 * @author abhurke
 *
 */
public class ThreadPoolVisitor implements Visitor 
{
	
	

	ThreadPool tp = null;
	
	
	public ThreadPoolVisitor(int threads, int queueSize, ContextAgent agent) {
		
		tp = new ThreadPool(queueSize, threads, agent);
	}
	
	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException 
	{
		
		Context subCtx = ctx.createSubContext();
		
		subCtx.put(BulkLoadSubscriber.class, obj);
		
		try
		{
			tp.execute(subCtx);
		}
		catch (AgentException e) 
		{
			LogSupport.major(subCtx, this, " -- BulkLoadSubscriber::ThreadPool -- " + obj, e);
		}
	}
	
	public void awaitCompletion() throws InterruptedException
	{
		synchronized (tp) 
		{
			while(tp.getQueueCount() > 0 || tp.getThreadCount() > 0)
			{
				tp.wait(1000);
			}
		}
	}
	
	public void shutdown()
	{
		tp.shutdown();		
	}
	
	public int getActiveThreadCount()
	{
		return tp.getThreadCount();
	}
	
}
