/*
 *  InvoiceLookupAgent.java
 *
 *  Author : Larry Xia
 *  Date   : Oct 24, 2003
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;

import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

// INSPECTED: 07/11/2003 ltse
/**
  *
 */
public class InvoiceLookupAgent
   extends PipelineAgent
{

   public InvoiceLookupAgent(ContextAgent delegate)
   {
      super(delegate);
   }


   @Override
public void execute(Context ctx)
      throws AgentException
   {
			
	Transaction tran = (Transaction) ctx.get(Transaction.class); 
	try{
	        CalculationService service = (CalculationService) ctx.get(CalculationService.class);
	        Invoice lattest_invoice = service.getMostRecentInvoice(ctx, tran.getAcctNum()); 
         
			if (lattest_invoice == null) 
			{
				ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_TOTAL_OWING, 
						Long.valueOf(0));
					ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_TAX_OWING, 
						Long.valueOf(0));
				
				pass(ctx, this, "Invoice not found");
			} else {
				ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_TOTAL_OWING, 
					Long.valueOf(lattest_invoice.getTotalAmount()));
				ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_TAX_OWING, 
					Long.valueOf(lattest_invoice.getTaxAmount()));
				ctx.put(TPSPipeConstant.ACCOUNT_INVOICE_DATE, 
					lattest_invoice.getInvoiceDate() ); 

				pass(ctx, this, "Invoice found");
			}
		} catch ( Exception e){
			pass(ctx, this, "Invoice not found");
		}
   	
    }

}


