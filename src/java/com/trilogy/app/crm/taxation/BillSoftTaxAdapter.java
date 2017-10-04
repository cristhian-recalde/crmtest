package com.trilogy.app.crm.taxation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import billsoft.eztax.BusinessClass;
import billsoft.eztax.CustomerType;
import billsoft.eztax.EZTaxException;
import billsoft.eztax.EZTaxSession;
import billsoft.eztax.ServiceClass;
import billsoft.eztax.TaxData;
import billsoft.eztax.Transaction;
import billsoft.eztax.ZipAddress;

import com.trilogy.framework.core.bean.Application;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.support.CurrencyPrecisionSupportHelper;

import eztaxconstants.ServiceType;
import eztaxconstants.TransactionType;

/**
 * Bill Soft Tax Adapter class. This will calculate tax for credit card top-up.
 * This class act as an interface for communicating with EZTax jar which will perform actual tax calculation based on msisdn.
 * Added during US Tax Support system. 
 * 
 * @author shailesh.makhijani@redknee.com
 * @since 9.6.0
 *
 */

public class BillSoftTaxAdapter implements TaxAdapter
{
	/**
	 * fileLocs is the file where all paths to db files are defined. Billsoft looks for this file when it initializes.
	 * If billsoft doesnt find this file it will throw an error.
	 */
	private final String FILELOCS = System.getProperty("billSoft.filelocs", "/opt/redknee/app/crm/current/etc/billsoft/filelocs.txt");
	private final String BILLSOFT_LOGGING ="BILLSOFT.LOGGING";
	
	
	@Override
	public long calculatePaymentTax(Context ctx, int spid, String msisdn, long amount , ZipAddress zipAddress) throws HomeException
	{
		PMLogMsg pm = new PMLogMsg(BillSoftTaxAdapter.class.getName(), "calculatePaymentTax()");

		EZTaxSession eztaxSession = null;
		boolean isLogEnabled=ctx.getBoolean(BILLSOFT_LOGGING, false); //default logging is false
		
		if(LogSupport.isDebugEnabled(ctx)){
			LogSupport.debug(ctx, this, "billsoft.filelocs = " + FILELOCS);
			LogSupport.debug(ctx, this, "BILLSOFT.LOGGING " + isLogEnabled);
		}
		
		try {
			eztaxSession = new EZTaxSession(isLogEnabled, FILELOCS); 

			Transaction trans = eztaxSession.createTransaction();			
			final Currency currency = CurrencyPrecisionSupportHelper.get(ctx).getCurrency(ctx,getCurrencyCode(ctx));
	
            String msisdnPrefix = ((msisdn != null) && (msisdn.length() >=6)) ? msisdn.substring(0, 6): null;//passing first 6 digits of msisdns.
            if( (msisdnPrefix != null && msisdnPrefix.length()> 0) )
            {
            	fillTransactionData(trans, convertLongValuetoDecimal(ctx, currency, amount), spid, Long.parseLong(msisdnPrefix),null);
            }else if (zipAddress != null) {
            	fillTransactionData(trans, convertLongValuetoDecimal(ctx, currency, amount), spid, 0 ,zipAddress);
            }
            else
            {
            	throw new HomeException("Invalid MSISDN : " + msisdn);
            }

			TaxData[] taxes;
			taxes = trans.calculateTaxes();
			
			return convertDecimalValueToLong(ctx, currency, getTotalTaxAmount(taxes, ctx));
		}catch (EZTaxException ex) {
			LogSupport.major(ctx, this, ex.getMessage(), ex);
			throw new HomeException(ex.getMessage());
		}catch(Throwable t){
			LogSupport.major(ctx, this, t.getMessage(), t);
			throw new HomeException(t.getMessage());
		}
		finally {
			if (eztaxSession != null) {
				eztaxSession.close();
			}
			pm.log(ctx);
			new OMLogMsg(Common.OM_MODULE, BillSoftTaxAdapter.class.getName()).log(ctx);
		}
	}


	/**
	 * Fills a Transaction object with transaction data.
	 * @param transaction Transaction object.
	 */
	private void fillTransactionData(Transaction transaction, double amount, int spid, long msisdn, ZipAddress zipAddress) {

		transaction.setTransactionType(TransactionType.SALES); 
		transaction.setServiceType(ServiceType.DEBIT_WIRELESS);
		transaction.setBillToNpaNxx(msisdn); 
		if (msisdn != 0) {
			transaction.setBillToNpaNxx(msisdn); 
		} else {
			transaction.setBillToAddress(zipAddress); 
		}
		transaction.setCustomerNumber(String.valueOf(spid));
		
		/*
		 * The Charge field specifies the amount of the transaction to be taxed. 
		 * This amount will be passed through EZTax to rate the tax based on the specified transaction/service pair
		 */
		transaction.setCharge(amount); //converting amount to higher denomination(Ex. dollar/cents)
		transaction.setMinutes(0);
		transaction.setBusinessClass(BusinessClass.CLEC);
		transaction.setDate(Calendar.getInstance().getTime());
		transaction.setCustomerType      (CustomerType.BUSINESS);

		transaction.setFacilitiesBased   (false);
		transaction.setFranchise         (false);
		transaction.setIncorporated      (true);
		transaction.setInvoiceNumber     (Calendar.getInstance().getTimeInMillis());
		transaction.setLifeline          (false);
		transaction.setLines             (1);
		transaction.setLocations         (1);
		transaction.setRegulated         (true);
		transaction.setServiceClass      (ServiceClass.PRIMARY_LONG_DISTANCE);
		transaction.setSale(true);

	}
	
	

	/**
	 * Iterate through all the taxes and return total tax amount.
	 * @return total tax amount
	 * @param Taxdata and context
	 */
	private double getTotalTaxAmount(TaxData[] taxes,  Context ctx) {
		if (taxes == null) {
			LogSupport.info(ctx, this, "No taxes were returned for this transaction");
			return 0;
		}
		
		double totalTaxAmount=0.0;
		DecimalFormat df = new DecimalFormat("##.##");
		List <String>taxData =new ArrayList<String>();
		taxData.add("Results for tax calculation (" + taxes.length + " taxes)"+"\n");
		for (TaxData tax : taxes) {
			taxData.add("    Tax Level:          " + tax.getTaxLevel()+"\n");
			taxData.add("	 Tax Description:    " + tax.getDescription()+"\n");
			taxData.add("	 Tax Type:           " + tax.getTaxType()+"\n"); 
			taxData.add("	 Jurisdiction PCode: " + tax.getPCode()+"\n");
			taxData.add("	 Tax Rate:           " + df.format(tax.getRate())+"\n");
			taxData.add("	 Tax Amount:         " + df.format(tax.getTaxAmount())+"\n");
			taxData.add("	 Calc Type:          " + tax.getCalcType()+"\n");
			taxData.add("	 Taxable Measure:    " + df.format(tax.getTaxableMeasure())+"\n");
			taxData.add("	 Exempt Sale Amount: " + df.format(tax.getExemptSaleAmount())+"\n");
			taxData.add("	 Billable:           " + tax.getBillable()+"\n");
			taxData.add("	 Compliance:         " + tax.getCompliance()+"\n");
			taxData.add("	 Surcharge:          " + tax.isSurcharge()+"\n");
			taxData.add("----------------------------------------------------------------"+"\n");
			
			totalTaxAmount+=tax.getTaxAmount();
		}
		
		totalTaxAmount=Double.valueOf(df.format(totalTaxAmount)).doubleValue();
		taxData.add(" Total Tax Amount " + totalTaxAmount );
		LogSupport.info(ctx, this, taxData.toString());
		
		taxData=null;
		return totalTaxAmount;
	}
	
	public String getCurrencyCode(Context ctx)
	{
		Application application = (Application) ctx.get(Application.class);
		if (application != null)
		{
			return application.getLocaleIsoCurrency();
		}
		else
		{
			new MinorLogMsg(ctx, "Unable to apply currency format.  Application config not available.", null).log(ctx);
		}
		return null;
	}
	
	public long convertDecimalValueToLong(Context ctx, Currency currency, double value)
	{
		// Shift the decimal place so that no precision is lost when converting
		// to a long
		double shiftedValue = value;
		for (int i = 0; i < currency.getPrecision(); i++)
		{
			shiftedValue *= 10;
		}

		// Convert to long
		return (long)(shiftedValue);
	}
		
	public double convertLongValuetoDecimal(Context ctx, Currency currency, long value)
	{	
		double shiftValue=value;
		for(int i =1; i<=currency.getPrecision(); i++){
			shiftValue /=10f;
		}
		return shiftValue;
	}
}

