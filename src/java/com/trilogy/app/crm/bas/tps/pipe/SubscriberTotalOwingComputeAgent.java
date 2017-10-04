/*
 * Created on Oct 28, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.trilogy.app.crm.bas.tps.pipe;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.*;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.EntryLogMsg;

/**
 * Calculate the total owing of subscriber
 * 
 * @author Larry Xia
 *
  */

// INSPECTED: 03/12/2003 ltse
public class SubscriberTotalOwingComputeAgent extends PipelineAgent {
	
	public SubscriberTotalOwingComputeAgent(ContextAgent delegate)
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

	public void execute(Context ctx)
	   throws AgentException
	{
		Transaction trans = (Transaction) ctx.get(Transaction.class); 
		long totalOwing = 0;
		long taxOwing = 0;
		
		try
		{
			
	   		//Invoice invoice = ( Invoice) ctx.get(Invoice.class); 
			Date invoiceDate = (Date) ctx.get(TPSPipeConstant.INVOICE_INVOICE_DATE); 
			Home home = (Home) ctx.get(TransactionHome.class);
	        
	        //REVIEW (comment): According to the FS, we now simply calculate 
	        // totalPaid/taxPaid by querying non-deposit payment. both negative and positive
	        // TODO: Review FS to see if any changes should be made here
			
        	And and = new And();
        	and.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, trans.getSubscriberID()));
        	and.add(new In(TransactionXInfo.ADJUSTMENT_TYPE, AdjustmentTypeSupportHelper.get(ctx).getPaymentsButDepositCodes(ctx)));


	   		if (invoiceDate != null)
	   		{
				totalOwing = ((Long) ctx.get(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TOTAL_OWING)).longValue();   
				taxOwing = ((Long) ctx.get(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TAX_OWING)).longValue(); 	
 
 				// get all credits (not including the deposits) since the invoice date
				and.add(new GTE(TransactionXInfo.RECEIVE_DATE, invoiceDate));
	   		}

	        home = home.where(ctx,and);

	   		Collection transSet = home.selectAll(ctx);
 			
			if ( transSet == null  || transSet.size() < 1) {
				
				ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TOTAL_OWING, Long.valueOf( totalOwing));
				ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TAX_OWING, Long.valueOf(taxOwing));
				pass(ctx, this, "Transaction not found. Total owing:" + 
					totalOwing + 
					" , taxOwing:" + 
					taxOwing);
			
			} else {
				// the payment is always negative
				totalOwing += totalPaid( transSet);
				taxOwing += taxPaid(transSet);
				ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TOTAL_OWING, Long.valueOf( totalOwing));
				ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TAX_OWING, Long.valueOf(taxOwing));
				pass(ctx, this, "Transaction found. Total owing:" + 
					totalOwing  + 
					" , taxOwing:" + 
					taxOwing);
			
			}

		 } catch ( Exception e){
		 	
		 	ERLogger.genAccountAdjustmentER(ctx,
		 		trans,
			    TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
		 		TPSPipeConstant.FAIL_TO_QUERY_TRANSACTION_TABLE);


			// send out alarm
 			new EntryLogMsg(10534, this, "","", new String[]{"Adjustment table searching fails"}, e).log(ctx);
			fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_TO_QUERY_TRANSACTION_TABLE);
		 }
		

	  }
	  
	/**
	 * calculate the total payment 
	 * 
	 * @param transactions 
	 *           A collection of payment transaction
 	 */

	public long totalPaid( Collection transactions){
	    long ret = 0;
	  Iterator it = transactions.iterator();
	  while ( it.hasNext() ){
		  Transaction trans = (Transaction) it.next();
		  ret  += trans.getAmount(); 
	  }
	  	
	  return ret; 
	}

	/**
	 * calculate the total tax paid 
	 * 
	 * @param transactions 
	 *           A collection of payment transaction
	 */
	
	public long taxPaid( Collection transactions){
	    long ret = 0;
	  Iterator it = transactions.iterator();
	  while ( it.hasNext() ){
		  Transaction trans = (Transaction) it.next();
		  ret  += trans.getTaxPaid(); 
	  }
	  	
	  return ret; 
	}
	  
}
