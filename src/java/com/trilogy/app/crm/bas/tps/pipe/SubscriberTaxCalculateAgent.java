/*
 * Created on Oct 28, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.trilogy.app.crm.bas.tps.pipe;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

/**
 * calculate how much tax should be paid
 * 
 * @author lxia
 *
  */
public class SubscriberTaxCalculateAgent extends PipelineAgent {
	
	public SubscriberTaxCalculateAgent(ContextAgent delegate)
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
		Transaction transaction = (Transaction) ctx.get(Transaction.class); 
 		Long totalOwing = (Long) ctx.get(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TOTAL_OWING); 
		Long taxOwing = (Long) ctx.get(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TAX_OWING); 
		TaxAuthority tax_auth = (TaxAuthority) ctx.get(TaxAuthority.class);
		long taxAmount =0;
		
		//add for TT:406156703, tax exemption
		Account acct = (Account) ctx.get(Account.class); 
		
		long tempTotalOwing = totalOwing.longValue();
		long tempTaxOwing = taxOwing.longValue();

		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "Total Owing:" + tempTotalOwing 
					+ " Tax Owing:" + tempTaxOwing + " Payment:" + transaction.getAmount() 
					+ " Adj Type:" + transaction.getAdjustmentType() 
					+ "Tax Rate:" + (tax_auth != null ? tax_auth.getTaxRate() : 0.0d) );
		}

		if ( !acct.isTaxExempted(ctx) ){
			if ( transaction.getAmount() < tempTotalOwing)
			{	
				
				/**
				 * 13031240054
				 */

				AdjustmentType adjustmentType = null; 
				
				try
				{
					adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, transaction.getAdjustmentType() );
				}
				catch(Exception e)
				{
					//if any exception arises, treat the following logic as in case of standard payment.
				}
			
				// there's code which changes -ve payment amount to +ve earlier in the pipeline.
				boolean isPayment = (adjustmentType == null || AdjustmentTypeActionEnum.CREDIT.equals(adjustmentType.getAction()) || transaction.getAmount() > 0 );
				
				
				/* From the FS requirements: use this formula to calculate the taxable amounts.*/
				taxAmount = (long)( (tempTotalOwing - transaction.getAmount()) * tax_auth.getTaxRate() /(100 + tax_auth.getTaxRate())); 
				
				long taxToBePaid = tempTaxOwing - taxAmount;
				
				if( isPayment && taxToBePaid >= transaction.getAmount() )
				{
					taxToBePaid = transaction.getAmount();
				}
				else if ( isPayment && taxToBePaid <= 0)
				{
					taxToBePaid = 0;
				}
				
				ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TAX_AMOUNT, Long.valueOf(taxToBePaid));
				
				
				if ( (taxToBePaid > tempTaxOwing) ){
					transaction.setTaxPaid(tempTaxOwing);
				} else {

					transaction.setTaxPaid(taxToBePaid);
				}

			}else {
			 
				/* In the case of an overpayment, we have no idea how much tax must be paid on the future charges. 
				 * We do our best to estimate this tax amount by applying the Subscriber Account's Tax Authority
				 * rate to the overpayment amount. */
				
				/* From the FS requirements: use this formula to calculate the taxable amounts.*/
				taxAmount = (long)((transaction.getAmount()- tempTotalOwing ) * tax_auth.getTaxRate() /(100 + tax_auth.getTaxRate())); 
				ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TAX_AMOUNT, Long.valueOf(taxAmount));
				long totalTax = tempTaxOwing + taxAmount;
				transaction.setTaxPaid(totalTax);
			}
		} else {
			ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TAX_AMOUNT, Long.valueOf(taxAmount)); 
			transaction.setTaxPaid( taxAmount);			
	 	}

		this.pass(ctx, this, "tax is calculated (in cents): " + transaction.getTaxPaid()); 
 
	}

}
