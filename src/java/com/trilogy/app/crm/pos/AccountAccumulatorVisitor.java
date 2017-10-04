/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.pos;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.pos.support.AccumulatorSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PointOfSaleConfigurationSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * Visits Accounts and creates/updates AccountAccumulator records
 * for each account with the daily calculations or newer
 * invoice information.
 * 
 * For testing purposes, a currentDate parameter was added to the Account Accumulator.
 * The currentDate will replace all the "new Date()" initializations, so that we may 
 * run this cron task for dates other than today.   
 * @author Angie Li
 */
public class AccountAccumulatorVisitor extends AbstractAccumulatorVisitor
{


	/** 
     * Constructor that sets the end date for our lookup range. 
     * @param date
     */
    public AccountAccumulatorVisitor(Context ctx, final Date date, POSLogWriter logWriter, LifecycleAgentScheduledTask agent)
    {
    	super(ctx, date, logWriter);
    	agent_ = agent;
    }
    
    public AccountAccumulatorVisitor(Context ctx, final Date date, POSLogWriter logWriter)
    {
        this(ctx, date, logWriter, null);
    }

    public void visit(Context ctx, Object obj) 
    {
    	 if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()) && !LifecycleStateEnum.RUN.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running. Remaining accounts will be processed next time.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }

        //Log OMs
        final PMLogMsg pmLogMsg = new PMLogMsg(getClass().getName(), "visit");
        new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_ACCOUNTACCUMULATOR_VISIT).log(ctx);
        incrementNumberVisited();
        
        Account account = (Account) obj;
        
        try
        {
            processAccumulator(ctx, account);
        }
        catch (HomeException e)
        {
            POSProcessorException pe = new POSProcessorException(getClass().getName(), 
                    "POS Account Accumulator failed for account BAN=" + account.getBAN(), 
                    e);
            logger.thrown(pe);
            
            //  Log OMs
            new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_ACCOUNTACCUMULATOR_UPDATE_FAILURE).log(ctx);
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
    }
    
    public void processAccumulator(Context ctx, Account account ) throws HomeException
    {
        Invoice previousInvoice = null; 
        Home accumulatorHome = (Home) ctx.get(ctx, AccountAccumulatorHome.class);
        
        AccountAccumulator accumulator = (AccountAccumulator) accumulatorHome.find(new EQ(AccountAccumulatorXInfo.BAN,account.getBAN()));
        AccumulatorDataCache data = new AccumulatorDataCache();
        CalculationService service = (CalculationService) ctx.get(CalculationService.class);
        try
        {
            previousInvoice = service.getMostRecentInvoice(ctx, account.getBAN(), currentDate_);
        }
        catch (CalculationServiceException e)
        {
            
        }
        
        if (accumulator == null)
        {
            accumulator = initializeAccountAccumulatorFromAccount(ctx, 
            		account, 
            		accumulator, 
            		previousInvoice,
            		data);
            

        }
        else if (accumulator.getDateOfExtraction() == null) 
        {
            /* If the dateOfExtraction attribute is null then something has messed up the data.
             * An error should be logged, and the data should be regenerated. */
            String msg = "POS Account Accumulator for account BAN=" 
                         + account.getBAN()
                         + " had NULL data. Performing calculations for this account again to get complete data.";
            logger.writeToLog(msg);
            
            accumulator = initializeAccountAccumulatorFromAccount(ctx, account, accumulator, previousInvoice, data);

        }
        else if (accumulator != null 
                && !isDateOfExtractionBeforeCurrentDate(accumulator.getDateOfExtraction()))
        {
            /* We won't accumulate if the accumulation has already been run.
             * Stopped using LastModified to compare to currentDate_, since LastModified
             * is always updated with LastModifiedAwareHome instead of currentDate_.
             */
        	

            return;
        }
        //  Update using Invoice if today is the Billing cycle date.
        else if (isNewInvoiceAvailable(ctx, account, accumulator, previousInvoice))
        {
            accumulator = initializeAccountAccumulatorFromInvoice(ctx, account, accumulator, previousInvoice);
            

        }
        
        //Log OMs
        new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_ACCOUNTACCUMULATOR_UPDATE_ATTEMPT).log(ctx);
        incrementNumberProcessed();
        
        //Create accumulator in DB for the recently initialized account accumulator
        if (accumulator.getBan().length() == 0)
        {
            accumulator.setBan(account.getBAN());
            accumulator.setDateOfExtraction(currentDate_);

            PointOfSaleConfiguration config = PointOfSaleConfigurationSupport.getPOSConfig(ctx);
            if (config.isExportNonResponsibleAccounts() && !account.isResponsible() && account.isIndividual(ctx))
            {
                Context subCtx = ctx.createSubContext();
                subCtx.put(PointOfSale.NON_RESPONSIBLE_ACCOUNT_PROCESSING, Boolean.TRUE);
                SubscriberAccumulatorVisitor subVisitor = new SubscriberAccumulatorVisitor(subCtx, getCurrentDate(), getLogger());
                accumulator = subVisitor.processSubscriberAccumulatorForAccount(subCtx, account);
            }

            accumulatorHome.create(ctx, accumulator);

        }
        else
        {
            // Accumulate the Transactions of the last day.
            data = getDailyAccumulator(ctx, account, previousInvoice, accumulator);
            accumulator = updateAccumulator(ctx, accumulator, account,data, previousInvoice);
            accumulator.setDateOfExtraction(currentDate_);
            accumulatorHome.store(ctx, accumulator);

        }
        logger.writeToLog("Extracted account=" + account.getBAN());
        
        //  Log OMs
        new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_ACCOUNTACCUMULATOR_UPDATE_SUCCESS).log(ctx);
        incrementNumberSuccessfullyProcessed();
    }
    
    /**
     * Initializes the Account Accumulator for this account with values from the most recent invoice.
     * 
     * Initialize Balance using the Total Outstanding amount from the last invoice minus payments-to-date.
     * 
     * @param account
     * @param accumulator
     */
    private AccountAccumulator initializeAccountAccumulatorFromAccount(Context ctx, 
            Account account, 
            AccountAccumulator accumulator, 
            Invoice previousInvoice,
            AccumulatorDataCache data)
    {
    	
        accumulator = new AccountAccumulator();

        if (previousInvoice != null)
        {
            accumulator = initializeAccountAccumulatorFromInvoice(ctx, account, accumulator, previousInvoice);
            
            data = getMonthToDateAccumulator(ctx, account, previousInvoice);
            	
            accumulator.setBalance(accumulator.getBalance() + data.getPaymentsReceived());
            //Payemtns don't have taxes so no need to add the taxes to the accumulator
            //accumulator.setTaxAmount(accumulator.getTaxAmount() + InvoiceCalculationSupport.getTotalTax(calculation));
            accumulator.setDiscountAmount(accumulator.getDiscountAmount() + data.getDiscountAmount());
        }
        //else we would want the default values
        return accumulator;
    }
    
    /**
     * Initialize the Account Accumulator with all the values from the most recent Invoice
     * 
     * @param ctx
     * @param account
     * @param accumulator
     * @param previousInvoice
     */
    private AccountAccumulator initializeAccountAccumulatorFromInvoice(Context ctx,
            Account account,
            AccountAccumulator accumulator, 
            Invoice invoice)
    {
        accumulator.setBalance(invoice.getTotalAmount());
        accumulator.setTaxAmount(invoice.getTaxAmount());
        accumulator.setDiscountAmount(invoice.getDiscountAmount());
        // Note: Payments received are negative.
        accumulator.setBalanceForward(invoice.getPreviousBalance() + invoice.getPaymentAmount());
        accumulator.setBillingDate(invoice.getInvoiceDate());
        accumulator.setDateOfExtraction(invoice.getInvoiceDate());   //So the "daily" accumulations will update from this date.
        return accumulator;
    }
    


    /** 
     * Accumulate from midnight the night before (inclusive) to the midnight of currentDate_ 
     * It is important to exclude all transactions already processed by the invoice 
     * generation. So the lowerbound of the daily accumulation will be the later date 
     * between midnightYesterday and Invoice Date.
     * 
     * 01/09/2006: Adding ability to accumulate since last accumulation date (a range 
     * longer than one day). 
     * 
     * @param ctx
     * @param account
     * @param data 
     * @return expect a null result if Start Date is not before the End Date in the range provided, or 
     * if the most recent invoice was generated today. 
     */
    private AccumulatorDataCache getDailyAccumulator(
            Context ctx, 
            Account account, 
            Invoice previousInvoice,
            AccountAccumulator accumulator)
    {
        Date midnightToday = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(currentDate_);
        Date midnightYesterday = CalendarSupportHelper.get(ctx).getDayBefore(midnightToday);
        AccumulatorDataCache data = null;
        
        
        if (accumulator.getDateOfExtraction().before(midnightYesterday))
        {
            /* Accumulation will span more than one day. It will be accumulated 
             * from the last time the AccountAccumulator was run until the 
             * currentDate_.
             */ 
            midnightYesterday = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(accumulator.getDateOfExtraction());
        }
        
        Date startDate = midnightYesterday;
        if (previousInvoice != null 
                && midnightYesterday.before(previousInvoice.getInvoiceDate()))
        {
            startDate = previousInvoice.getInvoiceDate();
        }

        if (startDate.before(midnightToday))
        {
            final PMLogMsg pmInvoiceLogMsg = new PMLogMsg(AccountAccumulatorVisitor.class.getName(), "createDataAccumulator");
            
            try 
            {
				data = AccumulatorSupport.getAccumulation(ctx, account, startDate, midnightToday);
			}
            catch (HomeException e) 
            {
            	LogSupport.major(ctx, this, e.getMessage(), e);
			}
            
            pmInvoiceLogMsg.log(ctx);
        }
        
        return data;
    }
    
    /**
     * Accumulate transactions since the last invoice generated date until the currentDate_
     * @param ctx
     * @param account
     * @param previousInvoice
     * @param data 
     * @return null when previous Invoice date is after the currentDate_
     */
    private AccumulatorDataCache getMonthToDateAccumulator(Context ctx, Account account, Invoice previousInvoice)
    {
        Date start = CalendarSupportHelper.get(ctx).getDayBefore(previousInvoice.getInvoiceDate());
        Date end = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(currentDate_);
        AccumulatorDataCache data = null;
        
        if (start.before(end))
        {
        	try
        	{
				data = AccumulatorSupport.getAccumulation(ctx, account, start, end);
			}
        	catch (HomeException e) 
        	{
            	LogSupport.major(ctx, this, e.getMessage(), e);
			}
        }
        
        return data;
    }
    
    
    
    /**
     * Returns the latest Payment Transaction made and NULL if there was none made
     * @param ctx 
     * @param accumulator 
     * @param previousInvoice 
     * 
     * @return
     */
    private Transaction getLastPayment(Context ctx, Account account, AccountAccumulator accumulator, Invoice previousInvoice)
    {
        Date midnightToday = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(currentDate_);
        Date midnightYesterday = CalendarSupportHelper.get(ctx).getDayBefore(midnightToday);
        
        Transaction lastPayment = null;
        
        if (accumulator.getDateOfExtraction().before(midnightYesterday))
        {
            /* Accumulation will span more than one day. It will be accumulated 
             * from the last time the AccountAccumulator was run until the 
             * currentDate_.
             */ 
            midnightYesterday = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(accumulator.getDateOfExtraction());
        }
        
        Date startDate = midnightYesterday;
        if (previousInvoice != null 
                && midnightYesterday.before(previousInvoice.getInvoiceDate()))
        {
            startDate = previousInvoice.getInvoiceDate();
        }

        final PMLogMsg pmInvoiceLogMsg = new PMLogMsg(AccountAccumulatorVisitor.class.getName(), "getLastTransaction");
        
        try
        {
			List payments = (List) AccumulatorSupport.getPaymentTransactions(ctx, account, startDate, midnightToday);

			if (payments.size() > 0)
		    {
		    	lastPayment = (Transaction) payments.get(0);
		    }
		    Iterator iter = payments.iterator();
		    while (iter.hasNext())
		    {
		    	Transaction paymentTrans = (Transaction) iter.next();
		        if (paymentTrans.getTransDate().after(lastPayment.getTransDate()))
		        {
		        	lastPayment = paymentTrans;
		        }
		    }
		}
        catch (HomeException e) 
        {
        	LogSupport.major(ctx, this, e.getMessage(), e);
		}
        
        pmInvoiceLogMsg.log(ctx);

        return lastPayment;
    }
    
    /**
     * Update the AccountAccumulator passed in with the values from the InvoiceCalculationBase.
     * @param ctx
     * @param accumulator
     * @param account
     * @param data
     * @return should always return a non-null AccountAccumulator. 
     */
    private AccountAccumulator updateAccumulator(Context ctx, 
            AccountAccumulator accumulator, 
            Account account, 
            AccumulatorDataCache data,
            Invoice previousInvoice)
    {
        // TT6030331467: payment plan payments received are included in the getPaymentsReceived
        long totalPayments = data.getPaymentsReceived();
        
        accumulator.setBalance(accumulator.getBalance()+ totalPayments); 
        accumulator.setDiscountAmount(accumulator.getDiscountAmount() 
                + data.getDiscountAmount());
        Transaction lastPayment = getLastPayment(ctx, account, accumulator, previousInvoice);
        if (lastPayment != null)
        {
            //negate since all payment transactions are -ve
            accumulator.setLastPaymentAmount(lastPayment.getAmount()*-1); 
            accumulator.setLastPaymentDate(lastPayment.getReceiveDate());
        }
        accumulator.setBalanceForward(accumulator.getBalanceForward() + totalPayments);
        return accumulator;
    }
    
    /**
     * Updates the Invoice value passed in with the most recent invoice.
     * Returns TRUE when there is an Invoice generated after the billing date we have on record.
     * Returns FALSE otherwise and if the previous invoice == NULL
     * @param ctx
     * @param account
     * @param accumulator
     * @param previousInvoice
     * @return
     */
    private boolean isNewInvoiceAvailable(Context ctx, 
            Account account, 
            AccountAccumulator accumulator, 
            Invoice previousInvoice)
    {
        if (previousInvoice != null)
        {
             return accumulator.getBillingDate().before(previousInvoice.getInvoiceDate());
        }
        return false;
    }

    private final LifecycleAgentSupport agent_;

    /**
	 * 
	 */
	private static final long serialVersionUID = -897336823465941643L;

}
