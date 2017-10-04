/*
 * Created on Oct 29, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.trilogy.app.crm.bas.tps.pipe;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TPSConfig;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CallDetailSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
/**
 *	Calculate subscriber balance 
 *  
 *  @author Larry Xia
 *
  */

// INSPECTED: 03/12/2003 ltse

public class SubscriberPaymentCalculateAgent extends PipelineAgent {
	


	public SubscriberPaymentCalculateAgent(ContextAgent delegate)
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
		Long totalOwing =(Long) ctx.get(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TOTAL_OWING); 
		Long taxOwing = (Long) ctx.get(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TAX_OWING);
		Transaction trans = (Transaction) ctx.get(Transaction.class);
		TPSConfig config = (TPSConfig) ctx.get(TPSConfig.class); 
		long totalCharge = 0;
		long totalUsage = 0;
		com.redknee.app.crm.bean.core.Transaction txn =  (com.redknee.app.crm.bean.core.Transaction) ctx.get(Transaction.class);
		boolean isPaymentReversal = false;
		try
        {
            isPaymentReversal = CoreTransactionSupportHelper.get(ctx).isPaymentReversal(ctx, txn);
        }
        catch (HomeException e1)
        {
            LogSupport.minor(ctx, this, "Exception occured while resolving adjustment type " +
                    "category for transaction with receipt number"+trans.getReceiptNum());
        }
		/**
		 *  TT#13070722003.
		 *  On Payment Reversal, Monthly Usage is being reset . This is because on payment reversal, BSS is forwarding  debit request to OCG which should not be the case.
         *  Instead the request to update balance should go via provisioning client.
		 **/
		if ( trans.getAmount() != 0 && config.getOCG() && !isPaymentReversal) 
		{
			pass(ctx, this, "Payment amount " + trans.getAmount());

		} else {
				
 		try
 		{
			totalCharge = getCharge( ctx); 
			ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TOTAL_CHARGE, 
				Long.valueOf(totalCharge));
		 
		} catch ( Exception e)
		{
			ERLogger.genAccountAdjustmentER(ctx, 
				trans,
			    TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
				TPSPipeConstant.FAIL_TO_CAL_TOTAL_CHARGES);
		
			// send out alarm
			new EntryLogMsg(10534, this, "","", new String[]{"When calculating totalCharges/totalUsage, Adjustment History table searching fails"}, e).log(ctx);
			fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_TO_CAL_TOTAL_CHARGES);
			return;
		}
		
		try
		{
 			totalUsage = getUsage(ctx); 	
 			ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_TOTAL_USAGE, 
 			       Long.valueOf(totalUsage));
 			
 			long balance = ((totalOwing.longValue() - taxOwing.longValue()) -
 						  (trans.getAmount() - trans.getTaxPaid()) +
 						  totalCharge + 
 						  totalUsage);  	 
 						  
			ctx.put(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_BALANCE, 
				Long.valueOf(balance));
				
			pass(ctx, this, "Calculate total charges: " + 
				totalCharge  + 
				", totalUsage: " + 
				totalUsage + 
				", balance: " + balance);

		} 
		catch ( Exception e)
		{
			ERLogger.genAccountAdjustmentER(ctx,
		  		trans,
			    TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
		  		TPSPipeConstant.FAIL_TO_CAL_TOTAL_USAGE);

			// send out alarm
			new EntryLogMsg(10534, this, "","", new String[]{"call detail table searching fails"}, e).log(ctx);
			fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_TO_CAL_TOTAL_USAGE);
		}
	}	
	}
	
	/**
	 * Get total charge 
	 * 
	 * @param ctx
	 *           A context
	 * @exception AgentException
	 *               thrown if one of the services fails to initialize
	 */
  
	public long getCharge( Context ctx) throws Exception
	{
		long ret = 0;
		Date invoiceDate = (Date) ctx.get(TPSPipeConstant.INVOICE_INVOICE_DATE); 		
		
		if (invoiceDate == null)
		{
			invoiceDate = (Date) ctx.get(TPSPipeConstant.ACCOUNT_INVOICE_DATE); 
		}

		Transaction trans = ( Transaction) ctx.get(Transaction.class);
		
        And filter = new And();
        filter.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, trans.getSubscriberID()));
        
        Set adjTypeSet = AdjustmentTypeSupportHelper.get(ctx).getPaymentsCodes(ctx);
        adjTypeSet.addAll(AdjustmentTypeSupportHelper.get(ctx).getSelfAndDescendantCodes(ctx,
                AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, 
                        AdjustmentTypeEnum.DepositPayments)));
        filter.add(new Not(new In(TransactionXInfo.ADJUSTMENT_TYPE, adjTypeSet)));
        if (invoiceDate != null)
        {
            filter.add(new GTE(TransactionXInfo.RECEIVE_DATE, invoiceDate));
        }

        Number totalAmount = HomeSupportHelper.get(ctx).sum(ctx, TransactionXInfo.AMOUNT, filter);
        Number totalTaxPaid = HomeSupportHelper.get(ctx).sum(ctx, TransactionXInfo.TAX_PAID, filter);
        
        return totalAmount.longValue() - totalTaxPaid.longValue();
	}
	
	/**
	 * get recent total usage
	 * @param ctx
	 *           A context
	 * @exception AgentException
	 *               thrown if one of the services fails to initialize
	 */

	public long getUsage(Context ctx)
	throws Exception
	{
		long ret = 0; 
		Account account = (Account) ctx.get(Account.class);  
		Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
        Date prevInvoiceDate = new Date(0);
        Date previousBillingDate = new Date(0);
        if (account != null){
            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            Invoice invoice =  service.getMostRecentInvoice(ctx, account.getBAN());            
            prevInvoiceDate =  invoice==null? new Date(0):invoice.getGeneratedDate();
            previousBillingDate =  invoice==null? new Date(0):invoice.getInvoiceDate();
        } 
            final Iterator it_detail =
                CallDetailSupportHelper.get(ctx).getCallDetailsForSubscriberIDHome(
                    ctx,
                    subscriber.getId(),
                    previousBillingDate,
                    new Date(), 
					prevInvoiceDate  ).selectAll().iterator();
		
		while ( it_detail.hasNext() ){
			CallDetail detail = (CallDetail) it_detail.next(); 
			ret += detail.getCharge(); 
		} 
 
		return ret; 
	}

	private static final String SUM_ALIAS = "CALLDETAIL_SUM";
	private static final String TRANSACTION_SUM = "TRANSACTION_SUM";

}
