package com.trilogy.app.crm.move.processor;

import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.LogSupport;

public class ConvertAccountGroupTypeConsumerAgent<CAGTR extends ConvertAccountGroupTypeRequest> implements ContextAgent {

	@Override
	public void execute(Context ctx) throws AgentException {
		
		if(LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, this, "Processing offline migration.");
    	}
		CAGTR request = (CAGTR) ctx.get(ConvertAccountGroupTypeProducerAgent.PRODUCER_REQUEST);
		MoveProcessor<CAGTR> moveProcessor = (MoveProcessor<CAGTR>) ctx.get(ConvertAccountGroupTypeProducerAgent.PRODUCER_DELEGATE);
		
		try 
		{
			moveProcessor.move(ctx);
		} 
		catch (MoveException e) 
		{
			LogSupport.major(ctx, this, "Move Exception occured while trying to perform back-end data migration",e);
		}
	}
}
