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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.xml.rpc.holders.BooleanHolder;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberInvoice;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.pos.support.AccumulatorSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.PointOfSaleConfigurationSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
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
 * Visits MSISDNs and creates/updates SubscruiberAccumulator records for each msisdn with
 * the daily calculations or newer invoice information.
 *
 * @author arturo.medina@redknee.com
 */

public class SubscriberAccumulatorVisitor extends AbstractAccumulatorVisitor
{

    /**
     * Constructor that sets the end date for our lookup range.
     *
     * @param ctx
     *            The operating context.
     * @param date
     *            End date of the lookup range.
     * @param logWriter
     *            Log writer.
     * @param agent
     */
    public SubscriberAccumulatorVisitor(final Context ctx, final Date date, final POSLogWriter logWriter,  LifecycleAgentScheduledTask agent)
    {
        super(ctx, date, logWriter);
        agent_ = agent;
    }

    public SubscriberAccumulatorVisitor(final Context ctx, final Date date, final POSLogWriter logWriter)
    {
        this(ctx, date, logWriter, null);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj)
    {
    	if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()) && !LifecycleStateEnum.RUN.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running. Remaining subscriptions will be processed next time.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }

        // Log OMs
        final PMLogMsg pmLogMsg = new PMLogMsg(getClass().getName(), "visit");
        new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_MSISDNACCUMULATOR_VISIT).log(ctx);
        incrementNumberVisited();

        final Msisdn msisdn = (Msisdn) obj;

        try
        {
            processMSISDNAccumulator(ctx, msisdn);
        }
        catch (final HomeException e)
        {
            final POSProcessorException pe = new POSProcessorException(getClass().getName(),
                     "POS Account Accumulator failed for msisdn=" + msisdn.getMsisdn(), 
                     e);
            logger.thrown(pe);

            // Log OMs
            new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_MSISDNCCUMULATOR_UPDATE_FAILURE).log(ctx);
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
    }


    public AccountAccumulator processSubscriberAccumulatorForAccount(final Context ctx, final Account account) throws HomeException
    {
        AccountAccumulator result = new AccountAccumulator();
        result.setBan(account.getBAN());
        result.setDateOfExtraction(currentDate_);
        

        Collection<Subscriber> subscribers = account.getSubscribers(ctx);
        if (subscribers!=null && subscribers.size()>0)
        {
            long totalAmount = 0;
            long taxAmount = 0;
            long discountAmount = 0;
            Date invoiceDate = new Date(0);
            Date extractionDate = new Date(0);
            Transaction lastPayment = null;

            for (Subscriber subscriber : subscribers)
            {
                Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, subscriber.getMSISDN());
                BooleanHolder creation = new BooleanHolder();
                SubscriberAccumulator subAccumulator = processSubscriberAccumulator(ctx, msisdn, creation);

                totalAmount += subAccumulator.getInvoiceAmount();
                
                Date subInvoiceDate = getInvoiceDate(ctx, subAccumulator.getInvoiceDueDate(), account.getSpid());
                if (invoiceDate.before(subInvoiceDate))
                {
                    invoiceDate = subInvoiceDate;
                }
                
                if (extractionDate.before(subAccumulator.getDateOfExtraction()))
                {
                    extractionDate = subAccumulator.getDateOfExtraction();
                }
                
                if (lastPayment == null && subAccumulator.getPaymentTransactions().size() > 0)
                {
                    lastPayment = (Transaction) subAccumulator.getPaymentTransactions().get(0);
                }
                Iterator iter = subAccumulator.getPaymentTransactions().iterator();
                while (iter.hasNext())
                {
                    Transaction paymentTrans = (Transaction) iter.next();
                    if (paymentTrans.getTransDate().after(lastPayment.getTransDate()))
                    {
                        lastPayment = paymentTrans;
                    }
                }
            }

            result.setBalanceForward(0);
            result.setBalance(totalAmount);
            result.setTaxAmount(taxAmount);
            result.setDiscountAmount(discountAmount);
            result.setBillingDate(invoiceDate);
            result.setDateOfExtraction(extractionDate);
            if (lastPayment!=null)
            {
                result.setLastPaymentAmount(lastPayment.getAmount()*-1);
                result.setLastPaymentDate(lastPayment.getReceiveDate());
            }
        } 
        
        return result;
    }
    
    public SubscriberAccumulator processSubscriberAccumulator(final Context ctx, final Msisdn msisdn, BooleanHolder creation) throws HomeException
    {
        // Some Msisdn's are not assined yet so no need to process this bean
        if (msisdn.getSubscriberID() == null || msisdn.getSubscriberID().length() <= 0)
        {
            LogSupport.debug(ctx, this, "MSISDN " + msisdn.getMsisdn() + " Has not been assigned yet, skipping it");
            return null;
        }

        SubscriberInvoice previousInvoice = null;
        final Home accumulatorHome = (Home) ctx.get(ctx, SubscriberAccumulatorHome.class);

        SubscriberAccumulator accumulator = (SubscriberAccumulator) accumulatorHome.find(new EQ(
            SubscriberAccumulatorXInfo.MSISDN, msisdn.getMsisdn()));
        AccumulatorDataCache data = new AccumulatorDataCache();        
        try
        {
            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            previousInvoice = service.getMostRecentSubscriberInvoice(ctx, msisdn.getSubscriberID(), currentDate_);
        }
        catch (CalculationServiceException e)
        {
            LogSupport.major(ctx, this, "failed to get invoice before date : " + currentDate_,e);
        }
        if (accumulator == null)
        {
            accumulator = initializeAccumulatorFromMsisdn(ctx, msisdn, previousInvoice);

        }
        else if (accumulator.getDateOfExtraction() == null)
        {
            /*
             * If the dateOfExtraction attribute is null then something has messed up the
             * data. An error should be logged, and the data should be regenerated.
             */
            final String msg = "POS Account Accumulator for MSISDN =" + msisdn.getMsisdn()
                + " had NULL data. Performing calculations for this account again to get complete data.";
            logger.writeToLog(msg);

            accumulator = initializeAccumulatorFromMsisdn(ctx, msisdn, previousInvoice);

        }
        else if (accumulator != null 
                 && !isDateOfExtractionBeforeCurrentDate(accumulator.getDateOfExtraction()))
        {
            return accumulator;
        }
        // Update using Invoice if today is the Billing cycle date.
        else if (isNewInvoiceAvailable(ctx, msisdn, accumulator, previousInvoice))
        {
            LogSupport.debug(ctx, this, "MSISDN " + msisdn.getMsisdn()
                + " New Invoice generated creating a new data based on it ");

            accumulator = initializeAccumulatorFromInvoice(ctx, msisdn, accumulator, previousInvoice);
        }

        // Create accumulator in DB for the recently initialized account accumulator
        if (accumulator.getMsisdn().length() == 0)
        {
            LogSupport.debug(ctx, this, "MSISDN " + msisdn.getMsisdn() + " has a new accumulator  creating it...");

            accumulator.setMsisdn(msisdn.getMsisdn());
            accumulator.setBan(getBan(ctx, msisdn.getSubscriberID()));
            accumulator.setSubscriberId(msisdn.getSubscriberID());
            //accumulator.setDateOfExtraction(currentDate_);
            
            creation.value = true;

        }
        else
        {
            // Accumulate the Transactions of the last day.
            data = getDailyAccumulator(ctx, msisdn, previousInvoice, accumulator);
            LogSupport.debug(ctx, this, "MSISDN " + msisdn.getMsisdn()
                + " Updating the new accumulator with Adjustments " + data.getAdjustments() + " and balance : "
                + (accumulator.getBalance() - data.getAdjustments()));
            accumulator = updateAccumulator(ctx, accumulator, msisdn, data, previousInvoice);
            accumulator.setBan(getBan(ctx, msisdn.getSubscriberID()));
            accumulator.setSubscriberId(msisdn.getSubscriberID());
            //accumulator.setDateOfExtraction(currentDate_);
            creation.value = false;
        }
        logger.writeToLog("Extracted msisdn=" + msisdn.getMsisdn());

        return accumulator;
    }

    /**
     * Accumulator which processes by MSISDN.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            MSISDN to be processed.
     * @throws HomeException
     *             Thrown if there are problems processing a MSISDN.
     */
    public void processMSISDNAccumulator(final Context ctx, final Msisdn msisdn) throws HomeException
    {
        BooleanHolder creation = new BooleanHolder();
        SubscriberAccumulator accumulator = processSubscriberAccumulator(ctx, msisdn, creation);
        
        if (accumulator != null 
                && !isDateOfExtractionBeforeCurrentDate(accumulator.getDateOfExtraction()))
        {
            /*
             * We won't accumulate if the accumulation has already been run. Stopped using
             * LastModified to compare to currentDate_, since LastModified is always
             * updated with LastModifiedAwareHome instead of currentDate_.
             */
            LogSupport.debug(ctx, this, "MSISDN " + msisdn.getMsisdn()
                    + " Data of extraction is after the current POS process skipping it ");
            return;
        }
        else
        {
            final Home accumulatorHome = (Home) ctx.get(ctx, SubscriberAccumulatorHome.class);
            
            if (accumulator!=null)
            {
                // Log OMs
                new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_ACCOUNTACCUMULATOR_UPDATE_ATTEMPT).log(ctx);
                incrementNumberProcessed();
                
                if (creation.value)
                {
                	if(currentDate_ != null)
                	{
                		accumulator.setDateOfExtraction(currentDate_);
                	}
                	accumulatorHome.create(ctx, accumulator);
                }
                else
                {
                	if(currentDate_ != null)
                	{
                		accumulator.setDateOfExtraction(currentDate_);
                	}
                	accumulatorHome.store(ctx, accumulator);
                }
                
                // Log OMs
                new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_ACCOUNTACCUMULATOR_UPDATE_SUCCESS).log(ctx);
                incrementNumberSuccessfullyProcessed();
            }
        }
    }


    /**
     * Updates the accumulator.
     *
     * @param ctx
     *            The operating context.
     * @param accumulator
     *            Accumulator to be updated.
     * @param msisdn
     *            MSISDN of the accumulator to be updated.
     * @param data
     *            Cached data.
     * @param previousInvoice
     *            Previous invoice.
     * @return The updated accumulator.
     */
    private SubscriberAccumulator updateAccumulator(final Context ctx, final SubscriberAccumulator accumulator,
        final Msisdn msisdn, final AccumulatorDataCache data, final SubscriberInvoice previousInvoice)
    {
        final long adjustments = data.getAdjustments();

        accumulator.setBalance(accumulator.getBalance() + adjustments);
        accumulator.setAdjustments(accumulator.getAdjustments() + adjustments);
        return accumulator;
    }


    private Date getInvoiceDate(final Context ctx, final Date dueDate, final int spidValue)
    {
        Date date = null;
        CRMSpid spid = null;

        if (dueDate == null)
        {
            return new Date(0);
        }

        try
        {
            spid = SpidSupport.getCRMSpid(ctx, spidValue);
        }
        catch (final HomeException e)
        {
            LogSupport.major(ctx, this, "Home exception : " + e.getMessage(), e);
        }

        if (spid != null)
        {
            final int numberOfDays = spid.getPaymentDuePeriod();

            date = CalendarSupportHelper.get(ctx).findDateDaysBefore(numberOfDays, dueDate);
        }
        else
        {
            date = new Date(0);
        }

        return date;
    }
    

    /**
     * Returns the due date of the previous invoice.
     *
     * @param ctx
     *            The operating context.
     * @param previousInvoice
     *            Previous invoice.
     * @param spId
     *            Service provider identifier.
     * @return Due date of the previous invoice.
     */
    private Date getDueDate(final Context ctx, final SubscriberInvoice previousInvoice, final int spId)
    {
        Date date = null;
        CRMSpid spid = null;

        if (previousInvoice == null)
        {
            return new Date(0);
        }

        try
        {
            spid = SpidSupport.getCRMSpid(ctx, spId);
        }
        catch (final HomeException e)
        {
            LogSupport.major(ctx, this, "Home exception : " + e.getMessage(), e);
        }

        if (spid != null)
        {
            final int numberOfDays = spid.getPaymentDuePeriod();

            date = calculateDays(previousInvoice.getInvoiceDate(), numberOfDays);
        }
        else
        {
            date = new Date(0);
        }

        return date;
    }


    /**
     * Calculates days after a date.
     *
     * @param date
     *            Date to be determined.
     * @param days
     *            Number of days after the provided date.
     * @return The date which is <code>days</code> after <code>date</code>.
     */
    private Date calculateDays(final Date date, final int days)
    {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }


    /**
     * Returns the daily accumulator.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            MSISDN being accumulated.
     * @param previousInvoice
     *            Previous invoice.
     * @param accumulator
     *            Accumulator.
     * @return The data cache containing the daily accumulation.
     */
    private AccumulatorDataCache getDailyAccumulator(final Context ctx, final Msisdn msisdn,
        final SubscriberInvoice previousInvoice, final SubscriberAccumulator accumulator)
    {
        final Date midnightToday = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(currentDate_);
        Date midnightYesterday = CalendarSupportHelper.get(ctx).getDayBefore(midnightToday);
        AccumulatorDataCache data = null;

        if (accumulator.getDateOfExtraction().before(midnightYesterday))
        {
            /*
             * Accumulation will span more than one day. It will be accumulated from the
             * last time the AccountAccumulator was run until the currentDate_.
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
            final PMLogMsg pmInvoiceLogMsg = new PMLogMsg(getClass().getName(),
                "createDataAccumulator");

            try
            {
                data = AccumulatorSupport.getMSISDNAccumulation(ctx, msisdn, startDate, midnightToday);
            }
            catch (final HomeException e)
            {
                LogSupport.major(ctx, this, e.getMessage(), e);
            }
            
            PointOfSaleConfiguration config = PointOfSaleConfigurationSupport.getPOSConfig(ctx);
            if (config.isExportNonResponsibleAccounts() && ctx.getBoolean(PointOfSale.NON_RESPONSIBLE_ACCOUNT_PROCESSING, false))
            {
                try
                {
                    accumulator.setPaymentTransactions(new ArrayList(AccumulatorSupport.getPaymentTransactions(ctx, msisdn, startDate, midnightToday)));
                }
                catch (final HomeException e)
                {
                    LogSupport.major(ctx, this, e.getMessage(), e);
                    accumulator.setPaymentTransactions(new ArrayList());
                }
            }


            pmInvoiceLogMsg.log(ctx);
        }

        return data;
    }


    /**
     * Initializes an accumulator from the invoice.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            MSISDN being accumulated.
     * @param accumulator
     *            Accumulator to be initialized.
     * @param previousInvoice
     *            Invoice to initialize the accumulator from.
     * @return The initialized accumulator.
     */
    private SubscriberAccumulator initializeAccumulatorFromInvoice(final Context ctx, final Msisdn msisdn,
        final SubscriberAccumulator accumulator, final SubscriberInvoice previousInvoice)
    {
        accumulator.setBalance(previousInvoice.getTotalAmount());
        accumulator.setInvoiceAmount(previousInvoice.getTotalAmount());
        accumulator.setInvoiceDueDate(getDueDate(ctx, previousInvoice, msisdn.getSpid()));
        accumulator.setBillingDate(previousInvoice.getInvoiceDate());
        accumulator.setDateOfExtraction(previousInvoice.getInvoiceDate());
        return accumulator;
    }


    /**
     * Returns whether a new invoice is available.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            MSISDN being accumulated.
     * @param accumulator
     *            Accumulator of this subscriber.
     * @param previousInvoice
     *            Previous invoice.
     * @return Returns <code>true</code> if there is a newer invoice, <code>false</code>
     *         otherwise.
     */
    private boolean isNewInvoiceAvailable(final Context ctx, final Msisdn msisdn,
        final SubscriberAccumulator accumulator, final SubscriberInvoice previousInvoice)
    {
        if (previousInvoice != null)
        {
            return accumulator.getBillingDate().before(previousInvoice.getInvoiceDate());
        }
        return false;
    }


    /**
     * Returns the BAN of the subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriberID
     *            Subscriber ID.
     * @return The BAN of the subscriber.
     */
    private String getBan(final Context context, final String subscriberID)
    {
        /*
         * [Cindy Wong] 2008-02-14: This lookup has become expensive as the original
         * assumption on BAN being part of the subscriber ID is no longer valid.
         */
        Subscriber subscriber = null;
        try
        {
            subscriber = SubscriberSupport.lookupSubscriberLimited(context, subscriberID);
        }
        catch (final HomeException exception)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                final StringBuilder sb = new StringBuilder();
                sb.append(exception.getClass().getSimpleName());
                sb.append(" caught in ");
                sb.append("SubscriberAccumulatorVisitor.getBan(): ");
                if (exception.getMessage() != null)
                {
                    sb.append(exception.getMessage());
                }
                LogSupport.debug(context, this, sb.toString(), exception);
            }

        }
        if (subscriber != null)
        {
            return subscriber.getBAN();
        }
        return "";
    }


    /**
     * Initializes an accumulator from MSISDN.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            The MSISDN of the accumulator to be initialized.
     * @param previousInvoice
     *            Previous invoice.
     * @return The initialized accumulator.
     */
    private SubscriberAccumulator initializeAccumulatorFromMsisdn(final Context ctx, final Msisdn msisdn,
        final SubscriberInvoice previousInvoice)
    {
        SubscriberAccumulator accumulator = new SubscriberAccumulator();
        AccumulatorDataCache data;

        if (previousInvoice != null)
        {
            accumulator = initializeAccumulatorFromInvoice(ctx, msisdn, accumulator, previousInvoice);

            data = getMonthToDateAccumulator(ctx, msisdn, previousInvoice);

            accumulator.setBalance(accumulator.getBalance() + data.getAdjustments());
            accumulator.setAdjustments(accumulator.getAdjustments() + data.getAdjustments());
        }
        // else we would want the default values
        return accumulator;
    }


    /**
     * Returns a month-to-date accumulator.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            MSISDN being looked up.
     * @param previousInvoice
     *            Previous invoice.
     * @return A cache containing the month-to-date data.
     */
    private AccumulatorDataCache getMonthToDateAccumulator(final Context ctx, final Msisdn msisdn,
        final SubscriberInvoice previousInvoice)
    {
        final Date start = CalendarSupportHelper.get(ctx).getDayBefore(previousInvoice.getInvoiceDate());
        final Date end = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(currentDate_);
        AccumulatorDataCache data = null;

        if (start.before(end))
        {
            try
            {
                data = AccumulatorSupport.getMSISDNAccumulation(ctx, msisdn, start, end);
            }
            catch (final HomeException e)
            {
                LogSupport.major(ctx, this, e.getMessage(), e);
            }
        }

        return data;
    }

    private final LifecycleAgentSupport agent_;

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -1353656814690094274L;

}
