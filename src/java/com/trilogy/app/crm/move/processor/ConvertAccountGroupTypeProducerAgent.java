package com.trilogy.app.crm.move.processor;

import com.trilogy.app.crm.invoice.process.ProducerAgent;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Producer Agent to Produce back-end data migration threads.
 * @author sgaidhani
 * @since 9.5.1
 *
 */
public class ConvertAccountGroupTypeProducerAgent<CAGTR extends ConvertAccountGroupTypeRequest> extends ProducerAgent {
	
	public static final String PRODUCER_REQUEST = "PRODUCER_REQUEST";
	public static final String PRODUCER_DELEGATE = "PRODUCER_DELEGATE";


	public ConvertAccountGroupTypeProducerAgent(final Context ctx,
			final ContextAgent agent,
			final String threadName,
			final int threadSize,
			final int queueSize)
	{
		super(ctx, agent, threadName, threadSize, queueSize);
	}

	public void produceAccountGroupConversionThread(Context ctx, ConvertAccountGroupTypeRequest request, MoveProcessor<CAGTR> processor) 
	        throws MoveException
	{
		
		if(LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, this, "Producing offline data migration thread.");
    	}

		Context subContext = ctx.createSubContext();
		subContext.put(PRODUCER_REQUEST, request);
		subContext.put(PRODUCER_DELEGATE, processor);

		try 
		{
			if(isQueueExhausted())
			{
				throw new MoveException(null, "Cannot convert any further accounts. Please try again Later.");
			}
			else
			{
				execute(subContext);
			}
			
		}
		catch (AgentException e)
		{
			new MajorLogMsg(this, e.getMessage(),e).log(ctx);
			throw new MoveException(null, "Agent Exception encountered while trying to spawn thread.", e);
		}
	}
}
