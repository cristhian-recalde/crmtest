/*
 * Created on Nov 28, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberInvoice;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;


/**
 * Look up the lattest invoice of this subscriber from database.
 * 
 * @author lxia
 *
  */
public class SubscriberInvoiceLookupAgent extends PipelineAgent {

	public SubscriberInvoiceLookupAgent(ContextAgent delegate)
	 {
		super(delegate);
	 }

	/**
 	 * 
	 * @param ctx
	 *           A context
	 * @exception AgentException
	 *               thrown if one of the services fails to initialize
	 */

	 @Override
    public void execute(Context ctx)
		throws AgentException
	 {
		  Subscriber sub = (Subscriber) ctx.get(Subscriber.class);   	
		  try{
                CalculationService service = (CalculationService) ctx.get(CalculationService.class);
                SubscriberInvoice lattest_invoice = service.getMostRecentSubscriberInvoice(ctx, sub.getId()); 
	
				ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TOTAL_OWING, 
					Long.valueOf(lattest_invoice.getTotalAmount()));
				ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TAX_OWING, 
					Long.valueOf(lattest_invoice.getTaxAmount()));
				ctx.put(TPSPipeConstant.INVOICE_INVOICE_DATE, 
					lattest_invoice.getInvoiceDate() ); 

				pass(ctx, this, "Subscriber invoice found");
		  } catch ( Exception e){
			  pass(ctx, this, "Subscriber invoice not found");
		  }
   	
	  }

}
