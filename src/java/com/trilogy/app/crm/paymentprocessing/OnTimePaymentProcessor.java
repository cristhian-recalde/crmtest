/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.paymentprocessing;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.invoice.AdjustmentTypeCategoryAccumulator;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * This on-time payment processor determines if the invoice has been paid
 * on-time. An invoice is considered paid on-time if it has zero or credit
 * balance, or if the full amount owing has been paid on or before the due date.
 * 
 * @author cindy.wong@redknee.com
 * 
 */
public class OnTimePaymentProcessor implements PaymentPromotionProcessor
{

    public static int PROMOTION_TYPE = 1;

    public static int QUALIFIED_ACTION = 1;

    public static int NOT_QUALIFIED_ACTION = 0;

    public static int ER_ID = 1140;

    public static String ER_NAME = "CRM Promotion Action";

    public static int ER_RECORD_CLASS = 700;

    private static PaymentPromotionProcessor INSTANCE = new OnTimePaymentProcessor();

    public static PaymentPromotionProcessor instance()
    {
        return INSTANCE;
    }

    protected OnTimePaymentProcessor()
    {
        // empty
    }

    /**
     * Determines if an invoice is paid in full on time. An invoice already
     * processed for on-time payment won't be reprocessed again.
     * 
     * @see com.redknee.app.crm.paymentprocessing.PaymentPromotionProcessor#processInvoice(java.lang.String,
     *      java.util.Date)
     * @param ban
     *            Account BAN
     * @param invoiceDate
     *            Invoice date
     */
    public void processInvoice(Context context, String ban, Date invoiceDate)
    {
        processInvoice(context, ban, invoiceDate, new Date(), false);
    }

    /**
     * Determines if an invoice is paid in full on time.
     * 
     * @see com.redknee.app.crm.paymentprocessing.PaymentPromotionProcessor#processInvoice(java.lang.String,
     *      java.util.Date, boolean)
     * @param ban
     *            Account BAN
     * @param invoiceDate
     *            invoice date
     * @param processDate
     *            date to process invoice.
     * @param forceReprocess
     *            Force reprocessing. If this flag is set to true, the invoice
     *            will be processed regardless if it has been processed before.
     */
    public void processInvoice(Context context, String ban, Date invoiceDate,
            Date processDate, boolean forceReprocess)
    {
        Invoice invoice = InvoiceSupport.getInvoice(context, ban, invoiceDate);
        if (invoice == null)
        {
            new InfoLogMsg(getClass(), "Cannot find invoice (BAN = " + ban
                    + ", date = " + invoiceDate + ")", null).log(context);
            return;
        }
        processInvoice(context, invoice, processDate, forceReprocess);
    }

    /**
     * Determines if an invoice is paid in full on time. An invoice already
     * processed for on-time payment won't be reprocessed again.
     * 
     * @see com.redknee.app.crm.paymentprocessing.PaymentPromotionProcessor#processInvoice(Invoice)
     * @param invoice
     *            Invoice
     */
    public void processInvoice(Context context, Invoice invoice)
    {
        processInvoice(context, invoice, new Date(), false);
    }

    /**
     * Determines if an invoice is paid in full on time.
     * 
     * @see com.redknee.app.crm.paymentprocessing.PaymentPromotionProcessor#processInvoice(Invoice,
     *      boolean)
     * @param invoice
     *            Account invoice
     * @param processDate
     *            Date to process invoice.
     * @param forceReprocess
     *            Force reprocessing. If this flag is set to true, the invoice
     *            will be processed regardless if it has been processed before.
     */
    public void processInvoice(Context context, Invoice invoice,
            Date processDate, boolean forceReprocess)
    {
        // looks up record
        InvoicePaymentRecord oldRecord = null;
        try
        {
            oldRecord = getRecord(context, invoice.getBAN(), invoice
                    .getInvoiceDate());
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(getClass(), "Cannot process invoice "
                    + invoice.getInvoiceId(), exception).log(context);
            return;
        }

        if (oldRecord != null && !forceReprocess)
        {
            new InfoLogMsg(getClass(), "Invoice " + invoice.getInvoiceId()
                    + " already processed, skipping", null).log(context);
            return;
        }
        else if (invoice.getDueDate().after(processDate))
        {
            new InfoLogMsg(getClass(), "Invoice " + invoice.getInvoiceId()
                    + " not processed because it is not yet due", null)
                    .log(context);
            return;
        }

        Account account = null;
        try
        {
            account = AccountSupport.getAccount(context, invoice.getBAN());
        }
        catch (HomeException exception)
        {
            new InfoLogMsg(getClass(), "Invoice " + invoice.getInvoiceId()
                    + " not processed because account " + invoice.getBAN()
                    + " is not found", exception).log(context);
            return;
        }

        InvoicePaymentRecord record = new InvoicePaymentRecord();
        record.setBAN(invoice.getBAN());
        record.setInvoiceDate(invoice.getInvoiceDate());
        record.setGeneratedDate(invoice.getGeneratedDate());
        record.setProcessed(true);
        record.setSpid(account.getSpid());
        record.setInvoiceId(invoice.getInvoiceId());
        record.setInvoiceAmount(invoice.getTotalAmount());
        record.setInvoiceTotalPayments(invoice.getPaymentAmount());

        long paymentMade = 0;
        try
        {
            paymentMade = calculatePaymentMadeOnTime(context, invoice);
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(getClass(),
                    "Cannot calculate payment made on time for invoice ("
                            + invoice.getInvoiceId(), exception).log(context);
        }

        record.setOnTime(-paymentMade >= invoice.getTotalAmount());
        record.setPayments(-paymentMade);

        if (invoice.getTotalAmount() <= 0)
        {
            record.setOnTime(true);
            new DebugLogMsg(getClass(), "Invoice " + invoice.getInvoiceId()
                    + " has no amount owing; marked as on-time", null)
                    .log(context);
        }
        new DebugLogMsg(getClass(), "Invoice Payment Processing Record: "
                + record, null).log(context);

        generateER(context, account.getSpid(), invoice, record);

        Home home = (Home) context.get(InvoicePaymentRecordHome.class);
        if (home == null)
        {
            new MinorLogMsg(getClass(), "Cannot find InvoicePaymentRecordHome",
                    null).log(context);
        }
        else
        {
            try
            {
                if (oldRecord != null)
                {
                    home.store(context, record);
                }
                else
                {
                    home.create(context, record);
                }
            }
            catch (HomeException exception)
            {
                new MinorLogMsg(getClass(),
                        "Cannot find create/store InvoicePaymentRecord: "
                                + record, exception).log(context);
            }
        }
    }

    /**
     * gets all transactions for itself and all non responsible accounts under
     * the tree
     * 
     * @param context
     *            The current context.
     * @param account
     *            account for which to retrieve the transactions.
     * @param start
     *            time interval start date.
     * @param end
     *            time interval end date
     * @return
     * @throws HomeException
     */
    protected Collection getTransactionsForAccountHierachy(
            final Context context, final Account account, final Date start,
            final Date end) throws HomeException
    {
        final Iterator it = AccountSupport.getNonResponsibleAccounts(context,
                account).iterator();
        final List allTrans = new ArrayList();
        while (it.hasNext())
        {
            final Account subAccount = (Account) it.next();
            final Collection subList = TransactionSupport
                    .getTransactionsForAccountInclusive(context, subAccount
                            .getBAN(), start, end);
            allTrans.addAll(subList);
        }

        return allTrans;
    }

    /**
     * Sums up the payments made between invoice date and due date.
     * 
     * @param context
     *            The operating context.
     * @param invoice
     *            Invoice to be calculated.
     * @return The payments made between invoice date and due date (inclusive).
     * @throws HomeException
     *             Thrown if there are problems calculating the payments made.
     */
    private long calculatePaymentMadeOnTime(Context context, Invoice invoice)
            throws HomeException
    {
        Account account = AccountSupport.getAccount(context, invoice.getBAN());
        try
        {
            final AdjustmentTypeCategoryAccumulator payments = new AdjustmentTypeCategoryAccumulator(
                    context, AdjustmentTypeEnum.StandardPayments);

            CalendarSupport calendarSupport = CalendarSupportHelper.get(context);
            Date dueDate = calendarSupport.getDateWithLastSecondofDay(invoice.getDueDate());

            final Collection transactions = getTransactionsForAccountHierachy(
                    context, account, invoice.getInvoiceDate(), dueDate);

            final Iterator transactionIterator = transactions.iterator();
            while (transactionIterator.hasNext())
            {
                final Transaction transaction = (Transaction) transactionIterator
                        .next();
                payments.accumulate(transaction);

            }

            return payments.getAmount();
        }
        catch (final HomeException exception)
        {
            throw new HomeException(
                    "Cannot calculate payment made before due date for invoice ("
                            + invoice + ")", exception);
        }
    }

    /**
     * Looks up an invoice payment record.
     * 
     * @param context
     *            The operating context.
     * @param ban
     *            Account BAN.
     * @param invoiceDate
     *            Invoice date.
     * @return The record found, or null if none exists.
     * @throws HomeException
     *             Thrown if there are problems looking up the record.
     */
    private InvoicePaymentRecord getRecord(Context context, String ban,
            Date invoiceDate) throws HomeException
    {
        Home home = (Home) context.get(InvoicePaymentRecordHome.class);
        if (home == null)
            throw new HomeException("Cannot find InvoicePaymentRecordHome");
        InvoicePaymentRecord record = new InvoicePaymentRecord();
        record.setBAN(ban);
        record.setInvoiceDate(invoiceDate);
        return (InvoicePaymentRecord) home.find(context, record);
    }

    private void generateER(Context context, int spid, Invoice invoice,
            InvoicePaymentRecord record)
    {
        Subscriber subscriber = SubscriberSupport
                .getSubscriber(context, invoice.getBAN(), invoice.getMSISDN(),
                        invoice.getInvoiceDate());
        String subId;
        String msisdn;
        if (subscriber == null)
        {
            subId = "";
            msisdn = invoice.getMSISDN();
        }
        else
        {
            subId = subscriber.getId();
            msisdn = subscriber.getMSISDN();
        }
        final String[] fields = new String[]
            {
                    invoice.getBAN(),
                    subId,
                    msisdn,
                    Integer.toString(PROMOTION_TYPE),
                    Integer.toString(record.isOnTime() ? QUALIFIED_ACTION
                            : NOT_QUALIFIED_ACTION),
                    new SimpleDateFormat("yyyy/MM/dd").format(invoice
                            .getInvoiceDate()),
                    new SimpleDateFormat("yyyy/MM/dd").format(invoice
                            .getDueDate()),
                    Long.toString(invoice.getTotalAmount()),
                    invoice.getInvoiceId(),
                    Long.toString(invoice.getPaymentAmount()),
                    Long.toString(record.getPayments()) };

        new ERLogMsg(ER_ID, ER_RECORD_CLASS, ER_NAME, spid, fields)
                .log(context);

    }
}
