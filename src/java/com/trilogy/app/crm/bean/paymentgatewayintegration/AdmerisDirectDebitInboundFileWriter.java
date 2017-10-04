package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.trilogy.app.crm.bean.CurrencyPrecision;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xlog.log.LogSupport;

public class AdmerisDirectDebitInboundFileWriter
{

	public AdmerisDirectDebitInboundFileWriter()
	{

	}

	synchronized public void init(Context ctx, String path, String bankCode, String fileName)  
			throws Exception
			{
		if (this.output == null )
		{	
			this.precision_ = (CurrencyPrecision) ctx.get(CurrencyPrecision.class);
			this.lineCount = 0;
			this.totalAmount = 0;
			this.outDirectory = path; 
			this.bankCode = bankCode; 
			this.fileName  = fileName;
			this.output = new PrintWriter(new File(path + File.separator + this.fileName)); 
			this.outputHeader();

		}	
			}

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
		String amountSign = amount >= 0 ? " " : "-";
		output.printf(FOOTER_FORMAT, lineCount, amount, amountSign); 
	}


	synchronized private void outputHeader() 
	{
		Date date = new Date();

		output.printf(HEADER_FORMAT, 
				date,
				date,
				date,
				date,
				date,
				date,
				this.bankCode,
				"");
	}

	public void printLine(Context ctx, String ban , long paymentAmount, Date paymentDate, String externalTransactionId)
	{
		if (output == null )
		{
			LogSupport.major(ctx, this, "AdmerisDirectDebitOuputWriter is found to be null.");
			return; 
		}

		CurrencyPrecision precision = (CurrencyPrecision) ctx.get(CurrencyPrecision.class);
		if (precision == null)
		{
			LogSupport.minor(ctx, this, "Cannot find currency precision object in the context.");
			return;
		}

		synchronized (this.output)
		{
			long amountIntegralPart =  BigDecimal.valueOf(paymentAmount, precision_.getStoragePrecision()).longValue();
			
			double fraction = paymentAmount / Math.pow(10, precision_.getStoragePrecision());
			String formattedAmount = String.format("%012.2f", fraction);
			int i = formattedAmount.indexOf('.');

			String fractionPart = "00";
			if(i>=0 && formattedAmount.length() >= i+3)
			{
				fractionPart = formattedAmount.substring(i+1, i +3);
			}

//			double fraction = paymentAmount % Math.pow(10, precision_.getStoragePrecision());
//			fraction = fraction / (Math.pow(10, (precision_.getStoragePrecision() - precision_.getDisplayPrecision())));
//			long fractionPart = (BigDecimal.valueOf(fraction)).setScale(0, RoundingMode.HALF_EVEN).longValue();


			String outputString = String.format(LINE_FORMAT, 
					ban,
					externalTransactionId,
					paymentDate,paymentDate,paymentDate,paymentDate,paymentDate,paymentDate,
					DIRECT_DEBIT_METHOD_CRDIT,
					ADMERIS_BANK_CODE,
					amountIntegralPart,
					fractionPart,
					"",
					"",
					"",
					"",
					"",
					"01",
					"",
					"");
			
			LogSupport.debug(ctx, this, "Writing record to inbound file. Record : "+ outputString);
			
			output.print(outputString);
		}

		this.totalAmount = this.totalAmount + paymentAmount; 
		lineCount++;
	}

	
	String fileName = ""; 
	PrintWriter output = null; 
	long totalAmount = 0l; 
	long lineCount = 0; 
	long fileID = -1; 
	String bankCode = "";
	String outDirectory = ""; 


	private CurrencyPrecision precision_ = null;

	public static final String HEADER_FORMAT = "1%td/%tm/%tY %tH:%tM:%tS%-20s%-126s\n";
	public static final String FOOTER_FORMAT = "3%05d%010.2f%1s";
	public static final String LINE_FORMAT = "2%21s%8s%td/%tm/%tY %tH:%tM:%tS%2s%6s%08d%2s%27s%20s%10s%19s%8s%2s%7s%21s\n";

	public static String DIRECT_DEBIT_METHOD_CRDIT = "81";
	public static String PAYMENT_TYPE = "01";
	public static String ADMERIS_BANK_CODE = "ADM";

}