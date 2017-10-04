package com.trilogy.app.crm.bas.directDebit;

import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xhome.visitor.ParallelVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;

public class EnhancedParallVisitor
extends ParallelVisitor
{
	
	 public EnhancedParallVisitor(int threads, Visitor delegate)
	 {
		 super(threads, delegate); 
	 }
	
	 protected ThreadPool getPool()
	 {
	   return super.pool_; 
	 }
	 
	   public void shutdown(long wait)
	   throws Exception
	   {
	      if ( pool_ == null ) return;
	      
	      
    	  getPool().shutdown();
    	  getPool().awaitTerminationAfterShutdown(wait);
	      
	      pool_ = null;
	   }

	   public static final long TIME_OUT_FOR_SHUTTING_DOWN = 60 * 1000;
}
