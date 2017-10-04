package com.trilogy.app.crm.poller.event;

import com.trilogy.app.crm.poller.event.ABMBalanceIncrementProcessor;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

public class DelayedABMBalanceIncrementProcessor extends ABMBalanceIncrementProcessor 
{
Context ctx ;
ABMBalanceIncrementProcessor  abm = null;

public DelayedABMBalanceIncrementProcessor(Context ctx, int queueSize, int threads)
{
	super(ctx,queueSize,threads);
	this.ctx=ctx;
}

public void process(long date,String erid, char[] record, int startIndex)
throws NumberFormatException, IndexOutOfBoundsException
{
	try{
		LogSupport.info(ctx,DelayedABMBalanceIncrementProcessor.class.getName(),"**Sleeping for 2 seconds");
		Thread.sleep(2000);
		
	}catch(Exception e){
		
		LogSupport.info(ctx,this,"ExeceptionOccured : " +e);
	}
    super.process(date, erid, record, startIndex);
}


}



