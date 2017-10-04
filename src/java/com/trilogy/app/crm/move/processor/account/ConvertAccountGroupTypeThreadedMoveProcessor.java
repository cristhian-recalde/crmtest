package com.trilogy.app.crm.move.processor.account;

import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.ConvertAccountGroupTypeProducerAgent;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;

public class ConvertAccountGroupTypeThreadedMoveProcessor<CAGTR extends ConvertAccountGroupTypeRequest> extends MoveProcessorProxy<CAGTR>{

	
	public ConvertAccountGroupTypeThreadedMoveProcessor(MoveProcessor<CAGTR> delegate)
    {
        super(delegate);
    }
	
	@Override
	public void validate(Context ctx) throws IllegalStateException {
		
		CompoundIllegalStateException cise = new CompoundIllegalStateException();
		
		ConvertAccountGroupTypeProducerAgent<CAGTR> producerAgent = (ConvertAccountGroupTypeProducerAgent<CAGTR>) ctx.get(ConvertAccountGroupTypeProducerAgent.class);
		if(producerAgent == null)
		{
			cise.thrown(new IllegalStateException("ConvertAccountGroupTypeProducerAgent not found in context. Cannot validate offline data migration process."));
			
		}
		cise.throwAll();
		super.validate(ctx);
	}

	@Override
	public void move(Context ctx) throws MoveException {

		//This should not call super. Instead the consumer agent will take the delegate and call super on that.
		//super.move(ctx);
		
		CAGTR request = this.getRequest();
		
		ConvertAccountGroupTypeProducerAgent<CAGTR> producerAgent = (ConvertAccountGroupTypeProducerAgent<CAGTR>) ctx.get(ConvertAccountGroupTypeProducerAgent.class);
		if(producerAgent == null)
		{
			throw new MoveException(request, "ConvertAccountGroupTypeProducerAgent not found in context. Cannot initiate offline data migration process.");
		}
		producerAgent.produceAccountGroupConversionThread(ctx, request,this.getDelegate());
	}
	
}
