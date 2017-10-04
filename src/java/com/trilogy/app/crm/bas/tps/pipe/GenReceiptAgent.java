/*
 *  GenReceiptAgent.java
 *
 *  Author : Larry Xia
 *  Date   : Oct 24, 2003
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.bas.tps.pipe;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.EntryLogMsg;

import com.trilogy.app.crm.bas.tps.IBISTPSProcessor;
import com.trilogy.app.crm.bas.tps.TPSProvisioningAgent;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GeneralConfig;
/*import com.trilogy.app.crm.bean.ReceiptConfig;
import com.trilogy.app.crm.bean.ReceiptOutputEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.TransactionMethodHome;
import com.trilogy.app.crm.report.PDFReportBuilder;
import com.trilogy.app.crm.report.PostscriptReportBuilder;
import com.trilogy.app.crm.report.ReceiptWriter;
import com.trilogy.app.crm.report.ReportBuilder;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;*/


/**
 * print out receipt, only when the transaction is done from GUI.
 * 
 * @author Larry Xia
  *
 */
public class GenReceiptAgent
   extends PipelineAgent
{
    /**
     * Putting a boolean value into the context with this key affects whether or
     * not the receipt will be created.  The value should be true if the receipt
     * should be generated; false otherwise.
     */
    public static final String GENERATE_RECEIPT = "GenReceiptAgent.GENERATE_RECEIPT";
    

   public GenReceiptAgent(ContextAgent delegate)
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
   public void execute(Context ctx) throws AgentException
   {/*
   		Account acct = (Account) ctx.get(Account.class); 
   		CRMSpid spidEntry = (CRMSpid) ctx.get(CRMSpid.class);
   		Transaction trans = (Transaction) ctx.get(Transaction.class);
   		 
   		//TPSConfig tpsconfig = (TPSConfig) ctx.get(TPSConfig.class); 
		GeneralConfig cfg = (GeneralConfig) ctx.get(GeneralConfig.class); 
		ReceiptConfig receiptConfig = (ReceiptConfig)ctx.get(ReceiptConfig.class);

	   	ReportBuilder builder = null;
	   	PrintWriter printWriter = null;
  		
   		try {
   			
   			// generate receipt only if this is a manual bal adjustments
   			if (ctx.has(IBISTPSProcessor.class))
   			{
   				pass(ctx, this, "No need to generate receipt for tps entry");
   				return;
   			}

            if (!ctx.getBoolean(GENERATE_RECEIPT, true))
            {
   				pass(ctx, this, "No need to generate receipt -- generation disabled.");
   				return;                
            }
   			
			ReceiptWriter writer =
				createReceiptWriter(ctx, acct, spidEntry, trans);
	
			// create builder
			String fileType = "ps"; 
			if ( receiptConfig.getReceiptOutPutType().equals( ReceiptOutputEnum.PDF)){
				fileType = "pdf"; 
				builder = new PDFReportBuilder();  
			}	
			else
			{
				builder = new PostscriptReportBuilder();
			}
			
			// create receipt
   			String dirName = receiptConfig.getReceiptDirectory() + File.separator;
   			mkReceiptDir(dirName);
			File file = new File(getFileName(ctx, acct, trans, fileType, dirName));
			file.createNewFile();
			
			printWriter = new PrintWriter(new FileWriter(file));  
			builder.setWriter(printWriter);
		
			writer.writeReceipt(ctx, builder);  
			builder.endDocument();  
			closeWriter(printWriter);
			pass(ctx, this, "Receipt printed");			
			
		}catch (Exception e) { 
			
   			closeWriter(printWriter);
  			// send out alarm
			new EntryLogMsg(10534, this, "","", new String[]{"Failed to print out receipt"}, e).log(ctx);
			
			pass(ctx, this, e.getMessage());

		}   	
    */}


	/*public void closeWriter(PrintWriter printWriter)
	{
		if ( printWriter != null)
		{
			printWriter.close();
		}
	}


	public String getFileName(
		Context ctx,
		Account acct,
		Transaction trans,
		String fileType,
		String dirName) {
		
		File tpsFile = (File) ctx.get(TPSPipeConstant.TPS_PIPE_TPS_FILE); 
		String tpsFileName = null;
		String fileName = dirName;
		
		if (tpsFile!=null)
		{
			tpsFileName= tpsFile.getName(); 
			
			fileName += tpsFileName.substring( 0, tpsFileName.lastIndexOf(TPSProvisioningAgent.getTpsAppendix(ctx))) +
								acct.getBAN() +
								"."+ fileType;
		}
		else
		{
			
			fileName += trans.getReceiptNum() + "." + acct.getBAN() +
				"."+ fileType;
		}
		
		return fileName;
	}


	public void mkReceiptDir(String dirName) throws IOException {
		File receiptDirectory = new File(dirName);
		if (!receiptDirectory.exists())
		{
			boolean success = receiptDirectory.mkdir();
			if (!success)
			{
				throw new IOException("Fail to create receipt directory " + dirName);
			}
		}
	}


	public ReceiptWriter createReceiptWriter(
		Context ctx,
		Account acct,
		CRMSpid spidEntry,
		Transaction trans) 
	{
		
		ReceiptWriter writer = new ReceiptWriter();  
		writer.setCustomerName(acct.getFirstName()+ " " + acct.getLastName());  
		writer.setCustomerAddressLine1(acct.getBillingAddress1());  
		writer.setCustomerAddressLine2(acct.getBillingAddress2());  
		writer.setCustomerAddressLine3(acct.getBillingCity() + 	
					" " + acct.getBillingProvince() +
					" " + acct.getBillingPostalCode() +
					" " + acct.getBillingCountry());  
		
		if (trans.getMSISDN().equals("0"))
		{
			writer.setCustomerTelephoneNumber("");
		}
		else
		{
			writer.setCustomerTelephoneNumber(trans.getMSISDN());  
		}
		writer.setCustomerAccountNumber(acct.getBAN());  
		writer.setPaymentAmount( spidEntry.getCurrency(), -trans.getAmount());  
		
		try
		{
			Home transactionMethodHome = (Home) ctx.get(TransactionMethodHome.class);
			TransactionMethod transMethod = (TransactionMethod)transactionMethodHome.find(ctx,
				Long.valueOf(trans.getTransactionMethod()));
			writer.setPaymentMethod(transMethod.getDescription());

		}
		catch (HomeException he)
		{

			new EntryLogMsg(10534, this, "","", new String[]{"Fail to find transactionMethod " + 
					trans.getTransactionMethod() + 
					" in the transactionMethodHome"}, he).log(ctx);
			writer.setPaymentMethod("");
		}
		
 		try
		{
			AdjustmentType type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeForRead(ctx,trans.getAdjustmentType());
			if ( type != null ){
			
				AdjustmentInfo adjustmentInfo = (AdjustmentInfo) 
					type.getAdjustmentSpidInfo().get(
						Integer.valueOf(trans.getSpid()));
				if (adjustmentInfo != null)
				{
 					writer.setPaymentType(adjustmentInfo.getInvoiceDesc());
				}
			}
		}
		catch (HomeException he)
		{

			new EntryLogMsg(10534, this, "","", new String[]{"Fail to find adjustment type " + 
					trans.getAdjustmentType() + 
					" in the AdjustmentTypeHome"}, he).log(ctx);
			writer.setPaymentType("");
		}
 
		writer.setPaymentDetails(trans.getPaymentDetails());  
		writer.setDate(trans.getTransDate());
		return writer;
	}*/

}


