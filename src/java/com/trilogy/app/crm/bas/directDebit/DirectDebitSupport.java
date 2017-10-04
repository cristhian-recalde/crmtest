package com.trilogy.app.crm.bas.directDebit;

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.DirectDebitConstants;
import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.app.crm.bean.DirectDebitRecordXInfo;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class DirectDebitSupport
{

	static public void redoDirectDebit(Context ctx, Account acct)
	throws HomeException
	{
        And and = new And(); 
        
        and.add(new EQ(DirectDebitRecordXInfo.BAN, acct.getBAN())); 
        
        Or or = new Or(); 
        or.add(new EQ(DirectDebitRecordXInfo.STATE, Long.valueOf(DirectDebitConstants.DD_STATE_PENDING))); 
        or.add(new EQ(DirectDebitRecordXInfo.STATE, Long.valueOf(DirectDebitConstants.DD_STATE_CREATE))); 
        
        and.add(or); 
       
        Collection <DirectDebitRecord> c = HomeSupportHelper.get(ctx).getBeans(ctx, DirectDebitRecord.class, and); 
       
        
        for(DirectDebitRecord record : c)
        {	
        	try 
        	{
        		record.setState(DirectDebitConstants.DD_STATE_FAIL);
        		record.setReasonCode(DirectDebitConstants.DD_REASON_CODE_CANCLE);
        		HomeSupportHelper.get(ctx).storeBean(ctx, record);
        		// generate log message.
        		record.createCancelMessage(ctx);
        	} catch (Exception e)
        	{
        		new MinorLogMsg(DirectDebitSupport.class, "fail to update cancelled DDR record" , e).log(ctx); 
        	}
        }
        
        createNewRecord(ctx, acct); 
        
	}
	
	
	static public DirectDebitRecord  createNewRecord(Context ctx, Account acct)
	throws HomeException 
	{
		Invoice invoice = InvoiceSupport.getMostRecentInvoice(ctx, acct.getBAN());
		DirectDebitRecord record=null; 
		try 
		{
			record =  createNewRecord(ctx, invoice, acct);
			record.validate(ctx);
		}catch(Exception e)
		{
			DDRAccountValidationException ex =  new DDRAccountValidationException(e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex; 
		}
		return HomeSupportHelper.get(ctx).createBean(ctx, record);
	}
	
	
	static public DirectDebitRecord  createNewRecord(Context ctx, Invoice invoice, Account acct)
	{
		DirectDebitRecord record = new DirectDebitRecord(); 
		record.setAgent(SystemSupport.getAgent(ctx));
		
		record.setBAN(acct.getBAN());		
		record.setTransactionMethod(acct.getPaymentMethodType());
		record.setBankAccount(acct.getBankAccountNumber());
		record.setPMethodBankID(acct.getPMethodBankID());
		record.setPMethodCardTypeId(acct.getPMethodCardTypeId()); 
		record.setMaxAmount(acct.getMaxDebitAmount());
		record.setSpid(acct.getSpid());
		record.setHolderName(acct.getHolderName());
		record.setPMethodExpiryDate(acct.getPMethodExpiryDate());
		
		String accountName = acct.getFirstName().trim()+ " " + acct.getLastName().trim(); 
		
		if (accountName.length()> DirectDebitRecord.CUSTOMERACCOUNTNAME_WIDTH)
		{
		    accountName = accountName.substring(0, DirectDebitRecord.CUSTOMERACCOUNTNAME_WIDTH -1); 
		}
		record.setCustomerAccountName(accountName);
		
		record.setCreditCardNumber(acct.getDecodedCreditCard());
		 
		
		record.setInvoiceDate(invoice.getInvoiceDate());
		record.setDueDate(invoice.getDueDate());
		record.setInvoiceId(invoice.getInvoiceId());
		record.setBillAmount(invoice.getCurrentAmount());
		
		record.setRequestDate(new Date()); 
				
		return record; 
	}	
}
