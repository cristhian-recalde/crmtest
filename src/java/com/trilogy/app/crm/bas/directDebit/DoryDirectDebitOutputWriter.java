package com.trilogy.app.crm.bas.directDebit;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

import com.trilogy.app.crm.bean.CurrencyPrecision;
import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.PaymentMethodConstants;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class DoryDirectDebitOutputWriter 
implements DirectDebitOutputWriter
{

	public DoryDirectDebitOutputWriter()
	{
		
	}
	
	synchronized public void init(Context ctx, String path, String bankCode, String extension, int spid)  
	throws Exception
	{
		if (this.output == null )
		{	
			this.precision_ = (CurrencyPrecision) ctx.get(CurrencyPrecision.class);
			this.lineCount = 0;
			this.totalAmount = 0;
			this.outDirectory = path; 
			this.bankCode = bankCode; 
			this.spid = spid; 
			this.fileNmae  = getFileName(ctx);
			this.extension = extension; 
			if (this.extension == null )
			{	
				this.extension = ".PMT"; 
			}
			
			this.output = new PrintWriter(new File(path+File.separator + this.fileNmae  + "." + extension)); 
			//this.output = new PrintWriter(new File(path+ this.fileNmae + "." + extension )); 

			this.outputHeader();

		}	
	}
	
	private String getFileName(Context ctx)
	throws HomeException
	{
        

        this.fileID = this.getFileSequence(ctx);
        
        Formatter fmt = new Formatter(); 
        
        fmt.format("%5s%05d", this.bankCode, this.fileID); 
		return fmt.toString(); 
	}
	
	
	
	@Override
	synchronized public void close() 
	{
		if (output != null)
		{	
			this.outputFooter();
			output.flush(); 
			output.close();
			output = null; 
		}	
		
	}

	
	synchronized private void outputFooter() 
	{
        if (precision_ == null)
        {
            Context ctx = ContextLocator.locate();
            if (ctx == null || !ctx.has(CurrencyPrecision.class))
            {
                String msg = "Cannot find currency precision object in the context.";
                throw new RuntimeException(msg);
            }
            else
            {
                precision_ = (CurrencyPrecision) ctx.get(CurrencyPrecision.class);
            }
        }

        double amount = (double) BigDecimal.valueOf(totalAmount, precision_.getStoragePrecision()).setScale(precision_.getDisplayPrecision(), RoundingMode.HALF_EVEN).doubleValue(); 
		output.printf(TRIAL_FORMAT, lineCount, amount); 
	}

	
	synchronized private void outputHeader() 
	{
		Calendar cal = Calendar.getInstance();
	    
	    output.printf(HEADER_FORMAT, bankCode, this.fileID, cal, cal, cal, cal, cal);
	}

	@Override
	public void printLine(Context ctx, DirectDebitRecord record)
	{
		if (output == null )
		{
			return; 
		}
		
		String paymentType = DIRECT_DEBIT_METHOD_DEBIT; 
		String cardNumber = record.getBankAccount();

		CurrencyPrecision precision = (CurrencyPrecision) ctx.get(CurrencyPrecision.class);
        if (precision == null)
        {
            String msg = "Cannot find currency precision object in the context.";
            LogSupport.minor(ctx, this,
                    msg);
            throw new RuntimeException(msg);
        }

        double amount = BigDecimal.valueOf(record.getBillAmount(), precision.getStoragePrecision()).setScale(precision.getDisplayPrecision(), RoundingMode.HALF_EVEN).doubleValue();

        if (record.getTransactionMethod() == PaymentMethodConstants.PAYMENT_METHOD_CREDIT_CARD)
		{
			paymentType = DIRECT_DEBIT_METHOD_CRDIT;
			cardNumber = record.getDecodedCreditCard();
		}

			synchronized (this.output)
			{
			
		        Date expDate = record.getPMethodExpiryDate();

				output.printf(LINE_FORMAT, 
						record.getId(), 
						paymentType, 
						record.getBAN(), 
						record.getBillNumber(),
						bankCode, 
						amount, 
						record.getDueDate(), record.getDueDate(), record.getDueDate(), 
						cardNumber, 
						expDate, expDate, expDate,
						record.getHolderName(), 
						record.getCustomerAccountName()
				
				);
				lineCount++;
				this.totalAmount = this.totalAmount + record.getBillAmount(); 
			}
		}	

	
	private long getFileSequence(Context ctx)
	throws HomeException
	{
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(
                ctx,
                IdentifierEnum.DIRECT_DEBIT_FILE_ID,
                0,
                99999l);

       return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
                ctx,
                IdentifierEnum.DIRECT_DEBIT_FILE_ID,
                null); 
	}
	
	
	
	String fileNmae=null; 
	PrintWriter output = null; 
	long totalAmount=0l; 
	long lineCount=0; 
	long fileID = -1; 
	String bankCode="";
	String outDirectory=null; 
	int spid; 
	String extension; 
	

	private CurrencyPrecision precision_ = null;
	
	public static final String HEADER_FORMAT = "88%5s%05d%td/%tm/%tY%tl:%tM\n";
	public static final String TRIAL_FORMAT = "99%012d%012.2f";
	public static final String LINE_FORMAT = "%010d%2sDD%8s%16s%5s%012.2f%td/%tm/%tY%-20s%td-%tb-%ty%-50s%-150s\n";
	
	public static String DIRECT_DEBIT_METHOD_DEBIT = "15"; 
	public static String DIRECT_DEBIT_METHOD_CRDIT = "83";
	
}
