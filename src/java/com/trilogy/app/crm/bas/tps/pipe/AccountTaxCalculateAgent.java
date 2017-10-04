/*
 * Created on Jul 22, 2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

/**
 * @author maalp
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AccountTaxCalculateAgent extends PipelineAgent
{
	public AccountTaxCalculateAgent(ContextAgent delegate)
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
		
		Long totalOwing = (Long) ctx.get(TPSPipeConstant.TPS_PIPE_ACCOUNT_TOTAL_OWING); 
		Long taxOwing = (Long) ctx.get(TPSPipeConstant.TPS_PIPE_ACCOUNT_TAX_OWING); 
		
		TaxAuthority tax_auth = (TaxAuthority) ctx.get(TaxAuthority.class);
		
		long taxAmount = 0;
		
		//add for TT:406156703, tax exemption
		Account acct = (Account) ctx.get(Account.class); 
		
		long tempTotalOwing = totalOwing.longValue();
		long tempTaxOwing = taxOwing.longValue();

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
				taxAmount = (long)( ( tempTotalOwing - transaction.getAmount() ) * tax_auth.getTaxRate() /(100 + tax_auth.getTaxRate()));
				
				long taxToBePaid = tempTaxOwing - taxAmount;
				
				if(isPayment && taxToBePaid >= Math.abs(transaction.getAmount()) )
				{
					taxToBePaid = transaction.getAmount();
				}
				else if (isPayment && taxToBePaid <= 0)
				{
					taxToBePaid = 0;
				}
				
				ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_TAX_AMOUNT, Long.valueOf(taxToBePaid));
			
				// MAALP: Group Pooled RFF
				// Reset transaction's values only for group pooled account
				if (acct.isPooled(ctx))
				{
					if ( taxToBePaid > tempTaxOwing ){
						transaction.setTaxPaid(tempTaxOwing);
					} else {
						transaction.setTaxPaid(taxToBePaid);
					}   
				}

			}else {
			 
				/* From the FS requirements: use this formula to calculate the taxable amounts.*/
				taxAmount = (long)((transaction.getAmount()- tempTotalOwing ) * tax_auth.getTaxRate() /(100 + tax_auth.getTaxRate())); 
				ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_TAX_AMOUNT, Long.valueOf(taxAmount));
				// MAALP: Group Pooled RFF
				// Reset transaction's values only for group pooled account
				long totalTax = tempTaxOwing + taxAmount;

				if (acct.isPooled(ctx))
				{
					transaction.setTaxPaid(totalTax);
				}
			}
		} else {
			ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_TAX_AMOUNT, Long.valueOf(taxAmount));
			// MAALP: Group Pooled RFF
			// Reset transaction's values only for group pooled account
			if (acct.isPooled(ctx))
			{
			    transaction.setTaxPaid( taxAmount);
			}
	 	}

		this.pass(ctx, this, "account tax is calculated (in cents): " + transaction.getTaxPaid()); 
 
	}


}
