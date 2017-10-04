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
package com.trilogy.app.crm.unit_test;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.InvoiceHome;
import com.trilogy.app.crm.bean.InvoiceTransientHome;
import com.trilogy.app.crm.bean.SubscriberInvoice;
import com.trilogy.app.crm.bean.SubscriberInvoiceHome;
import com.trilogy.app.crm.bean.SubscriberInvoiceTransientHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.utils.TransientHomeXDBCmdEmulator;
import com.trilogy.app.crm.xhome.adapter.TransientFieldResetAdapter;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;
/**
 * Utility sets up Invoice records for Accounts and Subscribers for Unit Testing.
 * 
 * Public Methods:
 * setupInvoices(Context, String, Map): Sets up Account and Subscriber Invoices.
 * 
 * 
 * Private Methods:
 * createAccountInvoice(Context, String, int): Create Account Invoice
 * createSubscriberInvoice(Context, String, String, int): Create Subscriber Invoice
 * @author ali
 *
 */
public class TestSetupInvoiceHistory extends ContextAwareTestCase 
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestSetupInvoiceHistory(final String name)
    {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * {@inheritDoc}
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestSetupInvoiceHistory.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
        super.setUp();
        
        setup(getContext());
        
        setupInvoices(getContext());
    }
    
    @Override
    public void tearDown()
    {
        tearDown(getContext());
        
        super.tearDown();
    }

    /**
     * Install the invoice homes into the context
     * @param context
     */
    public static void setup(Context context)
    {
        Home invoiceHome = new TransientFieldResettingHome(context, new InvoiceTransientHome(context));
        invoiceHome = new TransientHomeXDBCmdEmulator(context, invoiceHome);
        invoiceHome = new SortingHome(invoiceHome);  //For debug logging
        context.put(InvoiceHome.class, invoiceHome);
        
        Home subInvoiceHome = new TransientFieldResettingHome(context, new SubscriberInvoiceTransientHome(context));
        subInvoiceHome = new TransientHomeXDBCmdEmulator(context, subInvoiceHome);
        subInvoiceHome = new SortingHome(subInvoiceHome);  //for debug logging
        context.put(SubscriberInvoiceHome.class, subInvoiceHome);
    }
    
    private static void setupInvoices(Context context)
    {
        // Account owes total of $20
        createAccountInvoice(context, TestSetupAccountHierarchy.ACCOUNT4_BAN, 20, DEFAULT_INVOICEDATE);
        // Subscriber has credit of $10
        createSubscriberInvoice(context, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                TestSetupAccountHierarchy.SUB5_ID, -10, DEFAULT_INVOICEDATE);
        // Subscriber owes $30
        createSubscriberInvoice(context, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                TestSetupAccountHierarchy.SUB6_ID, 30, DEFAULT_INVOICEDATE);
    }
    
    /**
     * Creates Account Invoice with Total Amount Due accumulated from the amounts provided in the 
     * Subscriber Amounts Map, for the DEFAULT_INVOICEDATE.
     * @param context
     * @param parentBAN - Account Identifier for the Invoice Record
     * @param subscriberAmounts - Map<String, int>, map of Subscriber Identifiers (String) and Total Amount Due 
     * per for the indicated Subscriber.   
     */
    public static void setupInvoices(Context context, String parentBAN, Map subscriberAmounts)
    {
        setupInvoices(context, parentBAN, subscriberAmounts, DEFAULT_INVOICEDATE);
    }

    /**
     * Creates Account Invoice with Total Amount Due accumulated from the amounts provided in the 
     * Subscriber Amounts Map, for the given invoice date
     * 
     * @param context
     * @param parentBAN - Account Identifier of the parent (Group) account.
     * @param subscriberAmounts - Map<String, int>, map of Subscriber Identifiers (String) and Total Amount Due 
     * per for the indicated Subscriber.   
     */
    public static void setupInvoices(final Context context, final String parentBAN,
            final Map<String, Integer> subscriberAmounts, final Date invoiceDate)
    {
        int accountAmountOwing = 0;
        for (final Map.Entry<String, Integer> entry : subscriberAmounts.entrySet())
        {
            final String subscriberId = entry.getKey();
            final Integer totalAmountDollars = entry.getValue();
            createSubscriberInvoice(context, parentBAN, subscriberId, totalAmountDollars.intValue(), invoiceDate);
            accountAmountOwing += totalAmountDollars.intValue();
        }
        createAccountInvoice(context, parentBAN, accountAmountOwing, invoiceDate);
    }

    
    /**
     * Create Account Invoice with given parameters
     * @param context
     * @param BAN
     * @param totalAmountDollars
     * @param invoiceDate 
     */
    private static void createAccountInvoice(
            final Context context, 
            final String               BAN,
            final int                  totalAmountDollars, 
            final Date invoiceDate)
    {
        createAccountInvoice(context, BAN, invoiceDate, totalAmountDollars*100, totalAmountDollars*100, 0);
    }
    
    /**
     * Create Account Invoice with given parameters
     * @param context
     * @param BAN
     * @param invoiceDate
     * @param totalAmountInCents
     * @param chargesWithoutPaymentPlan is the total without the payment plan adjustments (charges),
     *                                     but it includes the payment plan credit.
     * @param paymentPlanBalance  is the Real-time PaymentPlan Uncharged Loan Remainder
     */
    public static void createAccountInvoice(
            final Context context, 
            final String BAN,
            final Date invoiceDate,
            final int totalAmountInCents,
            final int chargesWithoutPaymentPlan,
            final int paymentPlanBalance)
    {
        //Deliberately set some fields to Zero.  Others not set default to Zero.
        Invoice invoice = new Invoice();
        invoice.setBAN(BAN);
        invoice.setSpid(DEFAULT_SPID);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setGeneratedDate(invoiceDate);
        invoice.setCurrentAmount(totalAmountInCents);
        invoice.setTaxAmount(0);
        invoice.setTotalAmount(totalAmountInCents);
        invoice.setPaymentAmount(0);
        invoice.setRecurringCharges(0);
        invoice.setCurrentTaxAmount(0);
        invoice.setChargesWithoutPaymentPlan(chargesWithoutPaymentPlan);
        invoice.setPaymentPlanBalance(paymentPlanBalance);
        invoice.setBillCycleID(TestSetupAccountHierarchy.BILL_CYCLE_ID);
        invoice.setDueDate(CalendarSupportHelper.get(context).findDateDaysAfter(10, invoiceDate));
        invoice.setPdfExists(false);
        String id = String.valueOf(idCounter);
        invoice.setInvoiceId(id);
        invoice.setRootInvoiceId(id);
        idCounter++;
        
        try
        {
            Home home = (Home)context.get(InvoiceHome.class);
            invoice = (Invoice) home.create(context, invoice);
            LogSupport.debug(context, TestSetupInvoiceHistory.class.getName(), "A new INVOICE was created " + invoice);
            accountInvoiceCounter++;
        }
        catch(Exception e)
        {
            fail("Failed Account Invoice create. " + e.getMessage());
        }
    }
    
    /**
     * Create Subscriber Invoice with given parameters
     * 
     * As of CRM 8.0 MM, we have to manually add a layer of hierarchy to include the Invoice Records
     * for the Subscriber Accounts holding the subscription.  We will keep the convention of 
     * TestSetupAccountHierarchy and automatically create the Subscriber Account's invoice records
     * when asked to create the Subscriber Invoices.
     * 
     * @param context
     * @param groupAccountBAN
     * @param subscriberId
     * @param totalAmountDollars
     * @param invoiceDate TODO
     */
    private static void createSubscriberInvoice(Context context, 
            final String groupAccountBAN, 
            final String subscriberId, 
            final int totalAmountDollars, Date invoiceDate)
    {
    	String subscriberAccountBAN = TestSetupAccountHierarchy.getIndividualAccountIdentifier(subscriberId);
        createSubscriberInvoice(context, subscriberAccountBAN, 
        		subscriberId, invoiceDate, 
                totalAmountDollars*100, totalAmountDollars*100);
        
        createAccountInvoice(context, subscriberAccountBAN, totalAmountDollars, invoiceDate);
    }
    
    /**
     * Create Subscriber Invoice with given parameters
     * @param context
     * @param BAN
     * @param subscriberId
     * @param invoiceDate
     * @param totalAmountInCents
     * @param chargesWithoutPaymentPlan  is the total without the payment plan adjustments (charges), 
     *                                     but it includes the payment plan credit.
     */
    public static void createSubscriberInvoice(Context context, 
            final String BAN, 
            final String subscriberId, 
            final Date invoiceDate,
            final int totalAmountInCents,
            final int chargesWithoutPaymentPlan)
    {
        //Deliberately set some fields to Zero.  Others not set default to Zero.
        SubscriberInvoice invoice = new SubscriberInvoice();
        invoice.setIdentifier(subscriberId);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setBAN(BAN);
        invoice.setTaxAmount(0);
        invoice.setTotalAmount(totalAmountInCents);
        invoice.setInvoiceId(generateIdentifier());
        invoice.setChargesWithoutPaymentPlan(chargesWithoutPaymentPlan);

        try
        {
            Home home = (Home) context.get(SubscriberInvoiceHome.class);
            invoice = (SubscriberInvoice) home.create(context, invoice);
            LogSupport.debug(context, TestSetupInvoiceHistory.class.getName(), "A new SUBSCRIBERINVOICE was created " + invoice);
            subInvoiceCounter++;
        }
        catch(Exception e)
        {
            fail("Failed Subscriber Invoice create. " + e.getMessage());
        }
    }
    
    public static void tearDown(Context context)
    {
        try
        {
            ((Home)context.get(InvoiceHome.class)).removeAll();
            ((Home)context.get(SubscriberInvoiceHome.class)).removeAll();
        }
        catch(Exception e)
        {
            // Failing at the end of the tests. Do nothing. 
        }
    }
    
    private static String generateIdentifier()
    {
        idCounter++;
        return String.valueOf(idCounter);
        
    }
    
    /**
     * Print all Invoice Details in debug logs.
     * @param ctx
     */
    public static void printAllInvoiceRecords(Context ctx)
    {
        try
        {
            Home home = (Home) ctx.get(InvoiceHome.class);
            Collection<Invoice> allAccounts = home.selectAll();
            for(Invoice invoice:allAccounts)
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Account Invoice: BAN=");
                msg.append(invoice.getBAN());
                msg.append(" InvoiceDate=");
                msg.append(invoice.getInvoiceDate());
                msg.append(" TotalAmount=");
                msg.append(invoice.getTotalAmount());
                msg.append(" ChargesWithoutPaymentPlan=");
                msg.append(invoice.getChargesWithoutPaymentPlan());
                msg.append(" PreviousBalance=");
                msg.append(invoice.getPreviousBalance());
                msg.append(" BillCycleID=");
                msg.append(invoice.getBillCycleID());
                
                LogSupport.debug(ctx, TestSetupInvoiceHistory.class.getName(), msg.toString());
            }
            
            home = (Home) ctx.get(SubscriberInvoiceHome.class);
            Collection<SubscriberInvoice> allSubscribers = home.selectAll();
            for(SubscriberInvoice invoice:allSubscribers)
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Subscriber Invoice: BAN=");
                msg.append(invoice.getBAN());
                msg.append(" SubscriberId=");
                msg.append(invoice.getIdentifier());
                msg.append(" InvoiceDate=");
                msg.append(invoice.getInvoiceDate());
                msg.append(" TotalAmount=");
                msg.append(invoice.getTotalAmount());
                msg.append(" ChargesWithoutPaymentPlan=");
                msg.append(invoice.getChargesWithoutPaymentPlan());
                
                LogSupport.debug(ctx, TestSetupInvoiceHistory.class.getName(), msg.toString());
            }
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, TestSetupInvoiceHistory.class.getName(), 
                    "Error printing out all invoice records, due to " + e.getMessage());
        }
    }
    
    public void testSetup()
    {
        testSetup(getContext());
    }
    
    public static void testSetup(Context ctx)
    {
        try
        {
            Home accountHome = (Home) ctx.get(InvoiceHome.class);
            assertNotNull("No InvoiceHome in the context.", accountHome);
            Collection accountInvoices = accountHome.selectAll();
            assertTrue(accountInvoices.size() == accountInvoiceCounter);
        }
        catch(Exception e)
        {
            fail("Account Invoices not setup properly. " + e.getMessage());
        }

        try
        {
            Home subHome = (Home) ctx.get(SubscriberInvoiceHome.class);
            assertNotNull("No SubscriberInvoiceHome in the context.");
            Collection subInvoices = subHome.selectAll();
            assertTrue(subInvoices.size() == subInvoiceCounter);
        }
        catch(Exception e)
        {
            fail("Subscriber Invoices not setup properly. " + e.getMessage());
        }
    }
    
    final static int DEFAULT_SPID = TestSetupAccountHierarchy.SPID_ID;
    final public static Date DEFAULT_INVOICEDATE = CalendarSupportHelper.get().findDateMonthsAfter(1, TestSetupAccountHierarchy.START_DATE);
    static int idCounter = 0;
    static int accountInvoiceCounter = 0;
    static int subInvoiceCounter = 0;
}
