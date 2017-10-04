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
package com.trilogy.app.crm.transaction;

import java.util.Collection;
import java.util.Date;
import java.util.TreeMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.DeploymentTypeEnum;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCycleUsageHome;
import com.trilogy.app.crm.bean.SubscriberCycleUsageTransientHome;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.invoice.factory.AccountHierarchyInvoiceCalculationFactory;
import com.trilogy.app.crm.invoice.factory.InvoiceCalculationFactory;
import com.trilogy.app.crm.invoice.factory.SubscriberCalculationFactoryImpl;
import com.trilogy.app.crm.invoice.factory.SubscriberCalculatorFactory;
import com.trilogy.app.crm.provision.BASServiceServer;
import com.trilogy.app.crm.provision.xgen.BASService;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PaymentPlanSupport;
import com.trilogy.app.crm.unit_test.LicensedTestCase;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;
import com.trilogy.app.crm.unit_test.TestSetupAdjustmentTypes;
import com.trilogy.app.crm.unit_test.TestSetupCallDetails;
import com.trilogy.app.crm.unit_test.TestSetupInvoiceHistory;
import com.trilogy.app.crm.unit_test.TestSetupPaymentPlan;
import com.trilogy.app.crm.unit_test.TestSetupTransactions;

/**
 * Unit test to test Account-level Payment splitting:
 * using the AccountPaymentTransactionProcessor.handleRegularAccountTransaction method.
 * 
 * A few assumptions (defaults) made for all tests created in this class:
 *  (1)    Default Currency is used
 *  (2)    Only Default Adjustment Types are in the system (and SystemAdjustmentTypeMapping)
 *  (3)    Account Invoice Calculations work (this unit test bypasses month-to-date calculations, and simply
 *         relies on Invoiced balances to record Total Owing.
 *  (4)    Payment Plan History Feature is disabled, Payment Plan feature is enabled, but only a few test cases will exercise that part of the code.
 *  (5)    Payment Plan with inactive subscribers works (Scenario 4 uses this, but I will not write tests with this scenario.  We should do it later
 *         when we do know it works.  For now Scenario 5 will test Payment Plan without the wrinkle of inactive subscribers with outstanding balances.)
 * For each test there is a small amount of setup to do prior to launching 
 * AccountPaymentTransactionProcessor.handleRegularAccountTransaction:
 *   (a)    Setup the �previous� Invoices (Account and Subscriber)
 *   (b)    Install the relevant Account in to the Context using com.redknee.app.crm.bean.Account Class as the key, and
 *   (c)    Create the Payment Transaction to be processed
 *   
 *   
 * The tests are organized by testing Regular Payment and Over Payment on different Scenarios
 * (small payment, exact payment, over-payment), with 3 different System Payment Configuration.
 * Each test sets up the scenario and chooses one of the following configuration schemes:
 *   (Config1) Allow Payments to inactive subscribers, no priority to paying inactive subscribers balances
 *   (Config2) Do not allow Payments to inactive subscribers.
 *   (Config3) Allow Payments to inactive subscribers, with priority to paying inactive subscribers balances ***This 
 *   configuration has been deprecated.  See the Note below "@since 8.2".  
 * See the Scenario definitions below for the details of how the balances are setup for the account.
 * 
 * For this unit test, it really matters little if the Amount Owing is invoiced or is not yet Invoiced.  That is 
 * why the setup doesn't create fake call details and transactions.  Instead we efficiently establish that there is 
 * an amount owing by creating old Invoice Records. 
 * 
 * @author Angie Li
 * 
 * @since 8.2
 * With the change in implementation at revisions 15973, 16354 and 16356, the CRM 7.3 Payment Splitting Logic 
 * was ported from 7.3.  The logic implementation in CRM 7.3 did not include payment splitting to the Payment
 * Plan Loan.  The implementation of Payments to Payment Plan was introduced in CRM 8.2.
 * The unit test has to change not to reference RegularAccountPaymentProportioningCalculator. 
 * 
 * Another change with the introduction of CRM 7.3 logic was to deprecate the configuration of SPID: "Allow
 * payments priority to deactivated subscribers".  This means we don't need to test Config3.  These tests have 
 * been commented out with the exception of testPaymentWithNoActivePostpaidSubscribers.
 * 
 * TODO:
     * Write scenarios with payments to 
     * + Accounts with Non responsible accounts.
     * + Group Pooled Accounts
     * + PPSM accounts
     * + Account Payments below the minimum payment threshold
 *
 */
public class TestAccountPaymentTransactionProcessor extends LicensedTestCase 
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestAccountPaymentTransactionProcessor(final String name)
    {
        //Enable splitting payments over Payment Plan for this unit test
        //Disable Payment Plan History Feature for this unit test
        super(name,new String[]{PaymentPlanSupport.PAYMENT_PLAN_LICENSE_KEY, PaymentPlanSupport.PAYMENT_PLAN_HISTORY_DISABLE});
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

        final TestSuite suite = new TestSuite(TestAccountPaymentTransactionProcessor.class);

        // Ensure that the RUNNING_UNIT_TEST is set to true.
        context.put(com.redknee.app.crm.TestPackage.RUNNING_UNIT_TEST, true);
        
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
    }
    
    /**
     * Sets up all necessary homes for Invoice Calculation and Payments at account level
     * @param context
     */
    public static void setup(Context context)
    {
        //Testing Single Node Deployment
        context.put(DeploymentTypeEnum.class, DeploymentTypeEnum.STANDALONE);
        
        TestSetupAccountHierarchy.setup(context, false);
        TestSetupAdjustmentTypes.setup(context);
        TestSetupInvoiceHistory.setup(context);
        TestSetupTransactions.setup(context, false);
        TestSetupCallDetails.setup(context, false, false);
        
        /* We have to install the AccountPaymentTransactionProcessor decorating the Transaction pipeline so that 
         * it may delegate and result in creating a persistent Transaction in the system.  Install of this has to happen
         * in the setup method. */
        Home delegate = (Home)context.get(TransactionHome.class);
        paymentProcessor = new AccountPaymentTransactionProcessor(delegate);
        
        //Install the CalculationFactories: from ServiceInstall.installInvoiceServices()
        context.put(InvoiceCalculationFactory.class, new AccountHierarchyInvoiceCalculationFactory());
        context.put(SubscriberCalculatorFactory.class, new SubscriberCalculationFactoryImpl());
        
        // Disable Payment Plan License lookup.  Always include Payment Plan in calculations 
        context.put(PaymentPlanSupport.DISABLE_LICENSE_KEY, Boolean.TRUE);
        TestSetupPaymentPlan.setup(context);
        
        // Install Empty Subscriber Cycle Usage
        context.put(SubscriberCycleUsageHome.class, new SubscriberCycleUsageTransientHome(context));
        
        /* General Configuration holds the configuration for Ratio Threshold. For use in
         * Payment Splitting logic */ 
        setupGeneralConfig(context);
        
        //Install the BAS Service (for getting Payment Plan Accumulations)
        setupServiceInstall(context);
    }
    
    @Override
    public void tearDown()
    {
        tearDown(getContext());
        
        super.tearDown();        
    }
    
    public static void tearDown(Context context)
    {
        TestSetupTransactions.deleteTransactions(context);
        TestSetupInvoiceHistory.tearDown(context);
        TestSetupAccountHierarchy.completelyTearDown(context);
        TestSetupAdjustmentTypes.completelyTeardown(context);
        TestSetupPaymentPlan.completelyTearDown(context);
    }
    
    //
    /**
     * Currently only makes Account level payments
     * Make a payment in the amount given.  The amount is unsigned 
     */
    private Transaction createPaymentTransaction(String accountId, long paymentAmountInCents)
    {
        Transaction trans =  TestSetupTransactions.formTransaction(
                        accountId, 
                        "", 
                        "", 
                        AdjustmentTypeActionEnum.CREDIT, 
                        AdjustmentTypeEnum.StandardPayments_INDEX,
                        paymentAmountInCents, 
                        0L,
                        PAYMENT_DATE);
        return trans;
    }
    
    /**
     * Configure system to split payments over 1 cent, and over 1% of the owing threshold
     * @param context
     */
    private static void setupGeneralConfig(Context context)
    {
        GeneralConfig config = new GeneralConfig();
        config.setRatioThreshold(1); // Set Ratio Threshold to 1%
        config.setCurrencyPrecision(1); // Set Currency Precision to $0.01
        context.put(GeneralConfig.class, config);
    }
    
    /**
     * Updates the SPID configuration to test different payment scenarios.
     * @param ctx
     * @param inactive  will set the CRMSpid.PaymentAcctLevelToInactive.  Allows Payments to deactivated subscribers.
     */
    private static void setupSpidConfig(Context ctx, boolean inactive) 
    {
        try
        {
            Home spidHome = (Home) ctx.get(CRMSpidHome.class);

            CRMSpid spid = (CRMSpid) spidHome.find(ctx, Integer.valueOf(TestSetupAccountHierarchy.SPID_ID));
            spid.setPaymentAcctLevelToInactive(inactive);
            spidHome.store(spid);
        }
        catch(HomeException e)
        {
            fail("Failed to setup SPID. " + e.getMessage());
        }
    }
    
    private static void setupServiceInstall(Context ctx)
    {
        try
        {
            final Context ctx1 = ctx.createSubContext();

            final BASServiceServer basServer = new BASServiceServer(ctx1);
            ctx.put(BASService.class, basServer);
        }
        catch (final IllegalArgumentException e)
        {
            fail("Failed setup. " + e.getMessage());
        }
    }
    
    private static void installAccountInContext(Context context, String ban)
    {
        try
        {
            Home home = (Home) context.get(AccountHome.class);
            Account account = (Account) home.find(context, ban);
            context.put(Account.class, account);
        }
        catch (Exception e)
        {
            fail("Failed to install the Account in the Context. " + e.getMessage());
        }
    }
    
    /**
     * Prints all the current transactions in the debug logs.
     * Only for debug use.
     * @param ctx
     */
    private static void printAllTransactions(Context ctx)
    {
        try
        {
            Home home = (Home) ctx.get(TransactionHome.class);
            Collection<Transaction> all = home.selectAll();
            for(Transaction trans: all)
            {
                final StringBuilder sb = new StringBuilder();
                sb.append("Transaction receiptNum=");
                sb.append(trans.getReceiptNum());
                sb.append(" BAN=");
                sb.append(trans.getBAN());
                sb.append(" SubId=");
                sb.append(trans.getSubscriberID());
                sb.append(" Msisdn=");
                sb.append(trans.getMSISDN());
                sb.append(" Amount=");
                sb.append(trans.getAmount());
                sb.append(" AdjType=");
                sb.append(trans.getAdjustmentType());
                sb.append(" transDate=");
                sb.append(trans.getTransDate());
                sb.append(" receiveDate=");
                sb.append(trans.getReceiveDate());

                LogSupport.debug(ctx, TestAccountPaymentTransactionProcessor.class.getName(), sb.toString());
            }

        }
        catch(HomeException e)
        {
            LogSupport.debug(ctx, TestAccountPaymentTransactionProcessor.class.getName(), 
                    "Exception while printing all Transactions: " + e.getMessage());
        }
    }
    
    /** Verify the result transactions using the given matching parameters
     */
    private void verifyResultTransaction(Context ctx,
            final String msisdn,
            final String subId,
            final long amountInCents,
            final int adjustmentType)
    {
        
        Home tHome = (Home) ctx.get(TransactionHome.class);
        
        Or or = new Or();
        or.add(new EQ(TransactionXInfo.MSISDN, msisdn));
        or.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, subId));
        
        And predicate = new And();
        predicate.add(new EQ(TransactionXInfo.TRANS_DATE, PAYMENT_DATE));
        predicate.add(or);
        predicate.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, new Integer(adjustmentType)));
        
        try
        {
            Collection<Transaction> transactions = tHome.where(ctx, predicate).selectAll(ctx);
            assertEquals(1, transactions.size());
            for(Transaction t: transactions)
            {
                //Verify transaction amount was given only to the active subscriber.
                assertEquals("Result Transaction has incorrect amount. sub=" + subId, amountInCents, t.getAmount());
            }
        }
        catch(HomeException e)
        {
            fail("Failed test due to exception. " + e.getMessage());
        }
    }
    
    /**
     * Assert that there are no existing transactions with the given criteria.
     * @param ctx
     * @param predicate
     */
    private void verifyNoTransactions(Context ctx, Predicate predicate)
    {
        try
        {
            And predicate2 = new And();
            predicate2.add(new EQ(TransactionXInfo.TRANS_DATE, PAYMENT_DATE));
            predicate2.add(predicate);
            
            Home tHome = (Home) ctx.get(TransactionHome.class);
            Collection<Transaction> transactions = tHome.where(ctx, predicate2).selectAll();
        
            String msg = "Failure: There should be no transactions for predicate=" + predicate.toString();
            assertTrue(msg, transactions.size() == 0);
        }
        catch(HomeException e)
        {
            fail("Failed verifyNoTransactions due to exception: " + e.getMessage());
        }
    }
    
    /**
     * Assert that there are no existing transactions for the given subscriber.
     * @param ctx
     * @param msisdn
     * @param subId
     */
    private void verifyNoTransactionsForSubscriber(Context ctx, final String subId)
    {
        EQ predicate = new EQ(TransactionXInfo.SUBSCRIBER_ID, subId);
        verifyNoTransactions(ctx, predicate);
    }
    
    /**
     * Verify that there are no extra transactions created by the pipeline.
     * @param ctx
     * @param numOldTrans
     * @param numNewTrans
     * @throws HomeException
     */
    private void verifyNewTransactions(Context ctx, final int numNewTrans, final long totalAmount)
    {
        try
        {
            Home home = (Home) ctx.get(TransactionHome.class);
            EQ predicate = new EQ(TransactionXInfo.TRANS_DATE, PAYMENT_DATE);
            
            Collection<Transaction> transactions = home.where(ctx, predicate).selectAll(ctx);
            
            int count = 0;
            long total = 0;
            for (Transaction trans : transactions)
            {
                count++;
                total += trans.getAmount();
            }

            assertEquals("The number of expected New Transaction doesn't match.", numNewTrans, count);
            assertEquals("The sum of all New Transaction doesn't match the expected value.", totalAmount, total);
        }
        catch(HomeException e)
        {
            fail("Failed to verify results due to " + e.getMessage());
        }
    }
    
    
    /*
     * Scenario Setup
     */
    
    private void setupPrepaidSubscriber(Context ctx)
    {
        //Add Prepaid Subscriber
        try
        {
            TestSetupAccountHierarchy.setupSubscriber(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                PREPAID_SUB, SubscriberTypeEnum.PREPAID, PREPAID_SUB_MSISDN,
                SubscriberStateEnum.ACTIVE, TestSetupAccountHierarchy.START_DATE);
        }
        catch(Exception e)
        {
            fail("Failed to set up new Prepaid Subscriber due to exception " + e.getMessage());
        }
        
        //Add Activity for this subscriber.
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                PREPAID_SUB_MSISDN, PREPAID_SUB, AdjustmentTypeActionEnum.CREDIT, 
                AdjustmentTypeEnum.InitialBalance_INDEX, 
                20000, 20000, TestSetupAccountHierarchy.START_DATE);
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                PREPAID_SUB_MSISDN, PREPAID_SUB, AdjustmentTypeActionEnum.DEBIT, 
                AdjustmentTypeEnum.RecurringCharges_INDEX, 
                500, 19500, CalendarSupportHelper.get(ctx).findDateDaysAfter(1, TestSetupAccountHierarchy.START_DATE));
        
        // Verify the Transactions were saved
        verifyPrepaidSubTransactions(ctx);
    }
    
    /**
     * Assert only the original transactions for the prepaid subscriber are in the system.
     * @param ctx
     */
    private void verifyPrepaidSubTransactions(Context ctx)
    {
        Home tHome = (Home) ctx.get(TransactionHome.class);
        Or predicate = new Or();
        predicate.add(new EQ(TransactionXInfo.MSISDN, PREPAID_SUB_MSISDN));
        predicate.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, PREPAID_SUB));
        try
        {
            Collection<Transaction> transactions = tHome.where(ctx, predicate).selectAll();
            assertEquals(2, transactions.size());
        }
        catch(HomeException e)
        {
            fail("Failed to setup Prepaid transactions due to exception. " + e.getMessage());
        }
    }
    
    /**
     * Setup payment scenario: Allow Payments to inactive subscribers, no priority
     * @param ctx
     */
    private void setupConfig1(Context ctx)
    {
        boolean allowPaymentsToInactive = true;
        setupSpidConfig(getContext(), allowPaymentsToInactive); 
    }
    
    /**
     * Setup payment scenario: do not allow payments to inactive subscribers
     * @param ctx
     */
    private void setupConfig2(Context ctx)
    {
        boolean allowPaymentsToInactive = false;
        setupSpidConfig(getContext(), allowPaymentsToInactive); 
    }
    
    /**
     * Setup payment scenario: Allow payments to inactive subscribers and given them priority.
     * @param ctx
     */
    private void setupConfig3(Context ctx)
    {
        boolean allowPaymentsToInactive = true;
        setupSpidConfig(getContext(), allowPaymentsToInactive); 
    }
    
    
    /** Scenario 1
     * Account 4:
     * Subscriber 5: Postpaid, Inactive, Last Invoiced Amount $-10
     * Subscriber 6: Postpaid, Active, Last Invoiced Amount $30
     * Subscriber P: Prepaid, Active, has active transactions
     * 
     * Total Owing: $30 (Although one subscriber has $10CR, the total amount owing is $30 (credit amounts are 
     * not considered in the TotalOwingOfSubscribersWithoutPaymentPlan calculation.)
     */
    private void setupScenario1(Context ctx)
    {
        setupPrepaidSubscriber(ctx);
        
        /* Setup the dummy Invoices for TestSetupAccountHierarchy.ACCOUNT4_BAN
         */
        TreeMap<String, Integer> subscriberAmounts = new TreeMap<String, Integer>();
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB5_ID, Integer.valueOf(-10));
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB6_ID, Integer.valueOf(30));
        TestSetupInvoiceHistory.setupInvoices(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, subscriberAmounts);
        // Install the Account in the context for use in AccountPaymentTransactionProcessor.writePaymentReports
        installAccountInContext(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN);
    }
    
    /** Scenario 2
     * Account 4:
     * Subscriber 5: Postpaid, Inactive, Last Invoiced Amount $150
     * Subscriber 6: Postpaid, Active, Last Invoiced Amount $50
     * Subscriber X: Postpaid, Active, Last Invoiced Amount $-50 
     * Subscriber Y: Postpaid, Active, Last Invoiced Amount $0 
     * Subscriber P: Prepaid, Active, has active transactions
     * 
     * Total Owing: $200 (Although one subscriber has $50CR, the total amount owing is $200 (credit amounts are 
     * not considered in the TotalOwingOfSubscribersWithoutPaymentPlan calculation.)
     */
    private void setupScenario2(Context ctx)
    {
        setupPrepaidSubscriber(ctx);
        
        //Add more Subscribers
        try
        {
            TestSetupAccountHierarchy.setupSubscriber(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                THIRD_SUB, SubscriberTypeEnum.POSTPAID, THIRD_SUB_MSISDN,
                SubscriberStateEnum.ACTIVE, TestSetupAccountHierarchy.START_DATE);
            
            TestSetupAccountHierarchy.setupSubscriber(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                    FOURTH_SUB, SubscriberTypeEnum.POSTPAID, FOURTH_SUB_MSISDN,
                    SubscriberStateEnum.ACTIVE, TestSetupAccountHierarchy.START_DATE);
        }
        catch(Exception e)
        {
            fail("Failed to set up new Subscribers.");
        }
        //Setup the dummy Invoices for TestSetupAccountHierarchy.ACCOUNT4_BAN
        TreeMap<String, Integer> subscriberAmounts = new TreeMap<String, Integer>();
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB5_ID, Integer.valueOf(150));
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB6_ID, Integer.valueOf(50));
        subscriberAmounts.put(THIRD_SUB, Integer.valueOf(-50));
        subscriberAmounts.put(FOURTH_SUB, Integer.valueOf(0));
        TestSetupInvoiceHistory.setupInvoices(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, subscriberAmounts);
        // Install the Account in the context for use in AccountPaymentTransactionProcessor.writePaymentReports
        installAccountInContext(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN);
    }
    
    /** Scenario 3
     * Account 4:
     * Subscriber 5: Postpaid, Inactive, Last Invoiced Amount $250
     * Subscriber 6: Postpaid, Inactive, Last Invoiced Amount $-50
     * Subscriber P: Prepaid, Active, has active transactions
     * 
     * Total Owing: $250 (Although one subscriber has $50CR, the total amount owing is $250 (credit amounts are 
     * not considered in the TotalOwingOfSubscribersWithoutPaymentPlan calculation.)
     */
    private void setupScenario3(Context ctx)
    {
        try
        {
            //Set All Subscribers in Account to Inactive.
            Home sHome = (Home) ctx.get(SubscriberHome.class);
            
            /* TODO: Change this lookup to find all Subscriber Accounts in Group Account ACCOUNT4_BAN, and then search for all Subscriptions
             * in that set of SubscriberAccounts.
             */
            
            Or predicate = new Or();
            /* Since New Subscriber Accounts were added to the hierarchy we can't search subscribers by the root ban. */
            //predicate.add(new EQ(SubscriberXInfo.BAN, TestSetupAccountHierarchy.ACCOUNT4_BAN));
            predicate.add(new EQ(SubscriberXInfo.ID, TestSetupAccountHierarchy.SUB5_ID));
            predicate.add(new EQ(SubscriberXInfo.ID, TestSetupAccountHierarchy.SUB6_ID));
            Collection<Subscriber> subs = sHome.where(ctx, predicate).selectAll(); 
            for(Subscriber sub: subs)
            {
                sub.setState(SubscriberStateEnum.INACTIVE);
                sHome.store(sub);
            }
            setupPrepaidSubscriber(ctx);
        }
        catch(HomeException e)
        {
            fail("Failed to set up Subscribers due to: " + e.getMessage());
        }
        //Setup the dummy Invoices for TestSetupAccountHierarchy.ACCOUNT4_BAN
        TreeMap<String, Integer> subscriberAmounts = new TreeMap<String, Integer>();
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB5_ID, Integer.valueOf(250));
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB6_ID, Integer.valueOf(-50));
        TestSetupInvoiceHistory.setupInvoices(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, subscriberAmounts);
        // Install the Account in the context for use in AccountPaymentTransactionProcessor.writePaymentReports
        installAccountInContext(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN);
    }
    
    
    /** Scenario 4 Payment Plan Test
     * Account 4:
     * Subscriber 5: Postpaid, Inactive, Last Invoiced Amount $150
     * Subscriber 6: Postpaid, Active, Last Invoiced Amount $50
     * Subscriber X: Postpaid, Active, Last Invoiced Amount $-50 
     * Subscriber Y: Postpaid, Active, Last Invoiced Amount $0 
     * Subscriber P: Prepaid, Active, has active transactions
     * 
     * All Postpaid subscribers (5,6,X,Y) are enrolled in a Payment Plan Program.
     * Payment Plan details: TestSetupPaymentPlan.DEFAULT_PAYMENT_PLAN.
     * Original Loan Amount: $1000
     * Number of Installments: 4
     * Number of Charged Installments: 1 ($250 over 4 Subscribers -- Presumably, the inactive subscriber was part 
     *                                 of the loan program before it was deactivated.  
     *                                ***Big Assumption**  Payment Plan with inactive subscribers works!
     *                                TT 9011400001 tracks issues with inactive subscribers and Payment Plan.
     * 
     * 
     * Total Owing: $200 (Although one subscriber has $50CR, the total amount owing is $200 (credit amounts are 
     * not considered in the TotalOwingOfSubscribersWithoutPaymentPlan calculation.)
     */
    private void setupScenario4(Context ctx)
    {
        setupPrepaidSubscriber(ctx);
        
        //Add more Subscribers
        try
        {
            TestSetupAccountHierarchy.setupSubscriber(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                THIRD_SUB, SubscriberTypeEnum.POSTPAID, THIRD_SUB_MSISDN,
                SubscriberStateEnum.ACTIVE, TestSetupAccountHierarchy.START_DATE);
            
            TestSetupAccountHierarchy.setupSubscriber(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                    FOURTH_SUB, SubscriberTypeEnum.POSTPAID, FOURTH_SUB_MSISDN,
                    SubscriberStateEnum.ACTIVE, TestSetupAccountHierarchy.START_DATE);
        }
        catch(Exception e)
        {
            fail("Failed to set up new Subscribers.");
        }
        //Setup the dummy Invoices for TestSetupAccountHierarchy.ACCOUNT4_BAN
        TreeMap<String, Integer> subscriberAmounts = new TreeMap<String, Integer>();
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB5_ID, Integer.valueOf(150));
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB6_ID, Integer.valueOf(50));
        subscriberAmounts.put(THIRD_SUB, Integer.valueOf(-50));
        subscriberAmounts.put(FOURTH_SUB, Integer.valueOf(0));
        TestSetupInvoiceHistory.setupInvoices(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, subscriberAmounts);
        // Install the Account in the context for use in AccountPaymentTransactionProcessor.writePaymentReports
        installAccountInContext(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN);
    }
    
    
    /** Scenario 5 Payment Plan Test
     * Account 4:
     * Subscriber 5: Postpaid, Inactive, Last Invoiced Amount $0  ChargesWithoutPaymentPlan=$0
     * Subscriber 6: Postpaid, Active, Last Invoiced Amount $120+MonthlyInstallmentAmount,  ChargesWithoutPaymentPlan=$120
     * Subscriber X: Postpaid, Active, Last Invoiced Amount $-100+MonthlyInstallmentAmount,  ChargesWithoutPaymentPlan=$-100 
     * Subscriber Y: Postpaid, Active, Last Invoiced Amount $MonthlyInstallmentAmount,  ChargesWithoutPaymentPlan=0 
     * Subscriber P: Prepaid, Active, has active transactions
     * 
     * Three Postpaid subscribers (6,X,Y) are enrolled in a Payment Plan Program.
     * Payment Plan details: TestSetupPaymentPlan.DEFAULT_PAYMENT_PLAN.
     * Original Loan Amount: $1000
     * Number of Installments: 4
     * Number of Charged Installments: 1 ($250 over 3 Subscribers)
     * 
     * Create Transactions for Payment Plan Credits and 1st installment Charges.
     * 
     * Total Owing: $120 (Although one subscriber has $100CR, the total amount owing is $120 (credit amounts are 
     * not considered in the TotalOwingOfSubscribersWithoutPaymentPlan calculation.)
     */
    private void setupScenario5(Context ctx)
    {
        setupPrepaidSubscriber(ctx);
        
        //Add more Subscribers
        try
        {
            TestSetupAccountHierarchy.setupSubscriber(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                THIRD_SUB, SubscriberTypeEnum.POSTPAID, THIRD_SUB_MSISDN,
                SubscriberStateEnum.ACTIVE, TestSetupAccountHierarchy.START_DATE);
            
            TestSetupAccountHierarchy.setupSubscriber(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                    FOURTH_SUB, SubscriberTypeEnum.POSTPAID, FOURTH_SUB_MSISDN,
                    SubscriberStateEnum.ACTIVE, TestSetupAccountHierarchy.START_DATE);
        }
        catch(Exception e)
        {
            fail("Failed to set up new Subscribers.");
        }
        
        //$1000 in cents
        int loanInCents = 100000;
        //Percentage of Payment Plan loan by the specified subscriber.
        double ppPercent_SUB6 = 0.70;
        double ppPercent_THIRD_SUB = 0.20;
        double ppPercent_FOURTH_SUB = 0.10;
        int ppAmount_SUB6 = Double.valueOf(loanInCents * ppPercent_SUB6).intValue();
        int ppAmount_THIRD_SUB = Double.valueOf(loanInCents * ppPercent_THIRD_SUB).intValue();
        int ppAmount_FOURTH_SUB = Double.valueOf(loanInCents * ppPercent_FOURTH_SUB).intValue();
        int monthlyInstallment = loanInCents/TestSetupPaymentPlan.DEFAULT_NUM_INSTALLMENTS;
        /* Payment Plan Installment Charges should be charging using a ratio of the original loan amount.
         * Original implementation of Payment Plan doesn't charge the Installments according to ratio, 
         * but instead charges evenly over all subscribers.  This means the Account total owing is balanced
         * but the subscriber total owing will not necessarily be balanced.  
         * 
         * @since 8.3:
         * This test case will by charging Installments by ratio of the incurred loan
         * even Payment Plan Payment splitting algorithm, as follows.*/
        int ppInstallment_SUB6 = Double.valueOf(monthlyInstallment * ppPercent_SUB6).intValue();
        int ppInstallment_THIRD_SUB = Double.valueOf(monthlyInstallment * ppPercent_THIRD_SUB).intValue();
        int ppInstallment_FOURTH_SUB = Double.valueOf(monthlyInstallment * ppPercent_FOURTH_SUB).intValue();
        
        
        TestSetupPaymentPlan.enrollIntoPaymentPlan(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                loanInCents, PAYMENT_PLAN_START_DATE, 1, 1);
        Date invoiceDate = CalendarSupportHelper.get(ctx).findDateMonthsAfter(1, PAYMENT_PLAN_START_DATE);
        
        //Setup the dummy Invoices for TestSetupAccountHierarchy.ACCOUNT4_BAN, two Billing Periods
        //Billing Period 1, Before enrolling into Payment Plan
        TreeMap<String, Integer> subscriberAmounts = new TreeMap<String, Integer>();
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB5_ID, Integer.valueOf(0));
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB6_ID, ppAmount_SUB6);
        subscriberAmounts.put(THIRD_SUB, ppAmount_THIRD_SUB);
        subscriberAmounts.put(FOURTH_SUB, ppAmount_FOURTH_SUB);
        TestSetupInvoiceHistory.setupInvoices(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, subscriberAmounts);
        
        //Payment Plan Activity
        //Create the Payment Plan Transactions (the only charge records that cannot be "faked" using invoices.
        //Payment Plan Credits for all owing subscribers.
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                AdjustmentTypeActionEnum.CREDIT, AdjustmentTypeEnum.PaymentPlanLoanCredit_INDEX, 
                Double.valueOf(loanInCents * ppPercent_SUB6).intValue(), 
                0, PAYMENT_PLAN_START_DATE);
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                THIRD_SUB_MSISDN, THIRD_SUB, 
                AdjustmentTypeActionEnum.CREDIT, AdjustmentTypeEnum.PaymentPlanLoanCredit_INDEX, 
                Double.valueOf(loanInCents * ppPercent_THIRD_SUB).intValue(), 
                0, PAYMENT_PLAN_START_DATE);
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                FOURTH_SUB_MSISDN, FOURTH_SUB, 
                AdjustmentTypeActionEnum.CREDIT, AdjustmentTypeEnum.PaymentPlanLoanCredit_INDEX, 
                Double.valueOf(loanInCents * ppPercent_FOURTH_SUB).intValue(), 
                0, PAYMENT_PLAN_START_DATE);
        //Payment Plan 1st Installment Charges: Charge evenMonthlyInstallment to each subscriber
        Date chargeDate = CalendarSupportHelper.get(ctx).findDateDaysAfter(CommonTime.YEARS_IN_FUTURE, PAYMENT_PLAN_START_DATE);
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                AdjustmentTypeActionEnum.DEBIT, AdjustmentTypeEnum.PaymentPlanLoanAdjustment_INDEX, 
                ppInstallment_SUB6, 0, chargeDate);
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                THIRD_SUB_MSISDN, THIRD_SUB, 
                AdjustmentTypeActionEnum.DEBIT, AdjustmentTypeEnum.PaymentPlanLoanAdjustment_INDEX, 
                ppInstallment_THIRD_SUB, 0, chargeDate);
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                FOURTH_SUB_MSISDN, FOURTH_SUB, 
                AdjustmentTypeActionEnum.DEBIT, AdjustmentTypeEnum.PaymentPlanLoanAdjustment_INDEX, 
                ppInstallment_FOURTH_SUB, 0, chargeDate);
        
        printAllTransactions(ctx);
        
        //Billing Period 2, after enrolling in Payment Plan and 1st installment
        int accountTotal = 0;
        int accountTotalWithoutPaymentPlan = 0;
        //SUB5_ID has no new activity
        TestSetupInvoiceHistory.createSubscriberInvoice(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                TestSetupAccountHierarchy.SUB5_ID, invoiceDate, 0, 0);
        //SUB6_ID has new activity, it has amount owing (Payment Plan charges)
        int newCharges = 12000;
        TestSetupInvoiceHistory.createSubscriberInvoice(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                TestSetupAccountHierarchy.SUB6_ID, invoiceDate, newCharges+ppInstallment_SUB6, newCharges);
        accountTotal += newCharges + ppInstallment_SUB6;
        accountTotalWithoutPaymentPlan += newCharges;
        
        //THIRD_SUB has new activity, it received extra credit during the month; it has credit amount.
        newCharges = -10000;
        TestSetupInvoiceHistory.createSubscriberInvoice(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                THIRD_SUB, invoiceDate, newCharges + ppInstallment_THIRD_SUB, newCharges);
        accountTotal += newCharges + ppInstallment_THIRD_SUB;
        accountTotalWithoutPaymentPlan += newCharges;
        
        //FOURTH_SUB has no new activity, it has paid it's recurring charges, it has no amount owing.
        newCharges = 0;
        TestSetupInvoiceHistory.createSubscriberInvoice(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                FOURTH_SUB, invoiceDate, newCharges + ppInstallment_FOURTH_SUB, newCharges);
        accountTotal += newCharges + ppInstallment_FOURTH_SUB;
        accountTotalWithoutPaymentPlan += newCharges;
        
        //Account Total
        TestSetupInvoiceHistory.createAccountInvoice(ctx, 
                TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                invoiceDate, 
                accountTotal, 
                accountTotalWithoutPaymentPlan, 
                (loanInCents-(monthlyInstallment)));
        // Install the Account in the context for use in AccountPaymentTransactionProcessor.writePaymentReports
        installAccountInContext(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN);
        
        TestSetupInvoiceHistory.printAllInvoiceRecords(ctx);
    }
    
    /** Scenario 6
     * Non Responsible Sub Accounts
     * ACCOUNT1_BAN (Responsible), Last Invoiced Amount $300
     *   -> Subscriber: SUB1_ID (Postpaid, Active), Last Invoiced Amount $150
     *   
     *   => Child Account: ACCOUNT2_BAN  (Non-responsible), Last Invoiced Amount $150
     *       --> Subscriber: SUB2_ID (Postpaid, Active), Last Invoiced Amount $50
     *       --> Subscriber: SUB3_ID (Postpaid, Active), Last Invoiced Amount $-40
     *       --> Subscriber: SUB4_ID (Postpaid, Inactive), Last Invoiced Amount $100 
     * 
     * Total Owing: $300 (Although one subscriber has $40CR, the total amount owing is $300 (credit amounts are 
     * not considered in the TotalOwingOfSubscribersWithoutPaymentPlan calculation.)
     */
    private void setupScenario6(Context ctx)
    {
        //Setup the dummy Invoices for TestSetupAccountHierarchy.ACCOUNT1_BAN
        TreeMap<String, Integer> subscriberAmounts = new TreeMap<String, Integer>();
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB1_ID, Integer.valueOf(150));
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB2_ID, Integer.valueOf(50));
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB3_ID, Integer.valueOf(-40));
        subscriberAmounts.put(TestSetupAccountHierarchy.SUB4_ID, Integer.valueOf(100));
        TestSetupInvoiceHistory.setupInvoices(ctx, TestSetupAccountHierarchy.ACCOUNT1_BAN, subscriberAmounts);
        // Install the Account in the context for use in AccountPaymentTransactionProcessor.writePaymentReports
        installAccountInContext(ctx, TestSetupAccountHierarchy.ACCOUNT1_BAN);
    }
    
    /** Scenario 7
     * No Invoices, no transaction activity
     * Account 4:
     * Subscriber 5: Postpaid, Inactive, No Previous Invoice, $-10 Transaction
     * Subscriber 6: Postpaid, Active, No Previous Invoice, $30 Transaction
     * Subscriber P: Prepaid, Active, has active transactions
     * 
     * Total Owing: $0 
     */
    private void setupScenario7(Context ctx)
    {
        setupPrepaidSubscriber(ctx);
        
        //Subscriber 5, $10 One Time Credit
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                TestSetupAccountHierarchy.SUB5_MSISDN, TestSetupAccountHierarchy.SUB5_ID,
                AdjustmentTypeActionEnum.CREDIT, AdjustmentTypeEnum.OneTimeCharges_INDEX, 
                -1000, -1000, TestSetupInvoiceHistory.DEFAULT_INVOICEDATE);

        //Subscriber 6,  $30 Recurring Charge
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID,
                AdjustmentTypeActionEnum.DEBIT, AdjustmentTypeEnum.RecurringCharges_INDEX, 
                3000, 3000, TestSetupInvoiceHistory.DEFAULT_INVOICEDATE);
        
        // Install the Account in the context for use in AccountPaymentTransactionProcessor.writePaymentReports
        installAccountInContext(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN);
    }
    
    /** Scenario 8
     * No Invoices, no transaction activity
     * Account 4:
     * Account 4:
     * Subscriber 5: Postpaid, Inactive, No Previous Invoice, Transactions total $150
     * Subscriber 6: Postpaid, Active, No Previous Invoice, Transactions total $50
     * Subscriber X: Postpaid, Active, No Previous Invoice, Transactions total $-50 
     * Subscriber Y: Postpaid, Active, No Previous Invoice, Transactions total $0 
     * Subscriber P: Prepaid, Active, has active transactions
     * 
     * Total Owing: $0 
     */
    private void setupScenario8(Context ctx)
    {
        setupPrepaidSubscriber(ctx);
        
        //Add more Subscribers
        try
        {
            TestSetupAccountHierarchy.setupSubscriber(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                THIRD_SUB, SubscriberTypeEnum.POSTPAID, THIRD_SUB_MSISDN,
                SubscriberStateEnum.ACTIVE, TestSetupAccountHierarchy.START_DATE);
            
            TestSetupAccountHierarchy.setupSubscriber(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                    FOURTH_SUB, SubscriberTypeEnum.POSTPAID, FOURTH_SUB_MSISDN,
                    SubscriberStateEnum.ACTIVE, TestSetupAccountHierarchy.START_DATE);
        }
        catch(Exception e)
        {
            fail("Failed to set up new Subscribers.");
        }
        
        //Subscriber 5, $10 One Time Credit
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                TestSetupAccountHierarchy.SUB5_MSISDN, TestSetupAccountHierarchy.SUB5_ID,
                AdjustmentTypeActionEnum.DEBIT, AdjustmentTypeEnum.OneTimeCharges_INDEX, 
                15000, 15000, TestSetupInvoiceHistory.DEFAULT_INVOICEDATE);

        //Subscriber 6, $50 Recurring Charge
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID,
                AdjustmentTypeActionEnum.DEBIT, AdjustmentTypeEnum.RecurringCharges_INDEX, 
                5000, 5000, TestSetupInvoiceHistory.DEFAULT_INVOICEDATE);
        
        //THIRD_SUB, $10 Recurring Charges
        TestSetupTransactions.createTransaction(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN, 
                THIRD_SUB_MSISDN, THIRD_SUB,
                AdjustmentTypeActionEnum.CREDIT, AdjustmentTypeEnum.OneTimeCharges_INDEX, 
                -5000, -5000, TestSetupInvoiceHistory.DEFAULT_INVOICEDATE);
        
        // Install the Account in the context for use in AccountPaymentTransactionProcessor.writePaymentReports
        installAccountInContext(ctx, TestSetupAccountHierarchy.ACCOUNT4_BAN);
    }
    
    /*TODO:
     * Write scenarios with payments to 
     * + Accounts with Non responsible accounts.
     * + Group Pooled Accounts
     * + PPSM accounts
     * + Account Payments below the minimum payment threshold
     */ 
    
    /* 
     * Unit Tests
     */
    
    public void testSetup()
    {
        TestSetupAccountHierarchy.testSetup(getContext());
        TestSetupTransactions.testSetup(getContext());
        TestSetupAdjustmentTypes.testSetup(getContext());
        
        // Verify No Transactions for TestSetupAccountHierarchy.ACCOUNT4_BAN exist. 
        Or predicate = new Or();
        predicate.add(new EQ(TransactionXInfo.BAN, TestSetupAccountHierarchy.ACCOUNT4_BAN));
        predicate.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, TestSetupAccountHierarchy.SUB5_ID));
        predicate.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, TestSetupAccountHierarchy.SUB6_ID));
        predicate.add(new EQ(TransactionXInfo.MSISDN, TestSetupAccountHierarchy.SUB5_MSISDN));
        predicate.add(new EQ(TransactionXInfo.MSISDN, TestSetupAccountHierarchy.SUB6_MSISDN));
        
        verifyNoTransactions(getContext(), predicate);
    }
    
    
    /** testPaymentWithScenario1Config1
     * Allow Payments to inactive subscribers, no priority to inactive subscribers
     * Scenario 1, Total Owing: $30 
     * Account-level Payment Amount: $5 
     */
    public void testPaymentWithScenario1Config1()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentWithScenario1Config1>>> \n\n");
        
        //Setup payment scenario: Allow Payments to inactive subscribers, no priority
        setupConfig1(getContext());
        setupScenario1(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -500;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
    
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The transaction should be given entirely to the active subscriber, 
         * since the inactive subscriber has a credit total. */
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB6_ID);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.info(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST1 end: <<<testPaymentWithScenario1Config1>>> \n\n");
    }

    /** testPaymentWithScenario1Config2
     * Don't Allow payments to inactive subscribers
     * Scenario 1, Total Owing: $30 
     * Account-level Payment Amount: $5 
     */
    public void testPaymentWithScenario1Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentWithScenario1Config2>>> \n\n");
        
        //Setup payment scenario: do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario1(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values) 
        long paymentAmountInCents = -500;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
    
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The transaction should be given entirely to the active subscriber, 
         * since the inactive subscriber has a credit total. */
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB6_ID);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST end: <<<testPaymentWithScenario1Config2>>> \n\n");
    }

    /** testPaymentWithScenario1Config3
     * Allow payments to inactive subscribers and prioritize payments to inactive subscribers.
     * Scenario 1, Total Owing: $30 
     * Account-level Payment Amount: $5 
     */
    public void testPaymentWithScenario1Config3()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentWithScenario1Config3>>> \n\n");
        
        //Setup payment scenario: Allow payments to inactive subscribers and given them priority.
        setupConfig3(getContext());
        setupScenario1(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -500;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
    
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The transaction should be given entirely to the active subscriber, 
         * since the inactive subscriber has a credit total. */
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB6_ID);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST end: <<<testPaymentWithScenario1Config3>>> \n\n");
    }
    
    
    /** testOverPaymentWithScenario1Config1
     * Allow Payments to inactive subscribers, no priority to inactive subscribers
     * Scenario 1, Total Owing: $30 
     * Account-level Payment Amount: $40 
     */
    public void testOverPaymentWithScenario1Config1()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testOverPaymentWithScenario1Config1>>> \n\n");
        
        //Setup payment scenario: Allow Payments to inactive subscribers, no priority
        setupConfig1(getContext());
        setupScenario1(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -4000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);

        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The transaction should be given entirely to the active subscriber, 
         * since the inactive subscriber has a credit total. */
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB6_ID);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST end: <<<testOverPaymentWithScenario1Config1>>> \n\n");
    }
    
    
    /** testOverPaymentWithScenario1Config2
     * Don't allow Payments to inactive subscribers
     * Scenario 1, Total Owing: $30 
     * Account-level Payment Amount: $40 
     */
    public void testOverPaymentWithScenario1Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testOverPaymentWithScenario1Config2>>> \n\n");
        
        //Setup payment scenario: do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario1(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        int paymentAmountInCents = -4000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);

        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The transaction should be given entirely to the active subscriber, 
         * since the inactive subscriber has a credit total. */
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB6_ID);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST end: <<<testOverPaymentWithScenario1Config2>>> \n\n");
    }
        
    
    /** 
     * testOverPaymentWithScenario1Config3
     * Allow payments to inactive subscribers and prioritize payments to inactive subscribers.
     * Scenario 1, Total Owing: $30 
     * Account-level Payment Amount: $40 
     */
    public void testOverPaymentWithScenario1Config3()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testOverPaymentWithScenario1Config3>>> \n\n");
        
        //Allow payments to inactive subscribers and give them priority.
        setupConfig3(getContext());
        setupScenario1(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values) 
        long paymentAmountInCents = -4000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
    
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The transaction should be given entirely to the active subscriber, 
         * since the inactive subscriber has a credit total. */
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB6_ID);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST end: <<<testOverPaymentWithScenario1Config3>>> \n\n");
    }

    /** testPaymentWithScenario2Config1
     * Allow Payments to inactive subscribers, no priority to inactive subscribers
     * Scenario 2, Total Owing: $200 
     * Account-level Payment Amount: $100 
     */
    public void testPaymentWithScenario2Config1()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentWithScenario2Config1>>> \n\n" );
        //Setup payment scenario: Allow Payments to inactive subscribers, no priority
        setupConfig1(getContext());
        setupScenario2(getContext());
        
        //Create Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -10000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The proportioning should be done according to the sub_owing_amt/account_owing_amt ratio,
         * with an active subscriber receiving the remainder of the payment at the end. */
        
        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
        
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN, TestSetupAccountHierarchy.SUB5_ID, 
                (int) (paymentAmountInCents * 0.75) , AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                (int) (paymentAmountInCents * 0.25), AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), THIRD_SUB);
        verifyNoTransactionsForSubscriber(getContext(), FOURTH_SUB);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 2, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testPaymentWithScenario2Config1>>> \n\n" );
    }

    /** testPaymentWithScenario2Config2
     * Don't allow payments to inactive subscribers
     * Scenario 2, Total Owing: $50 
     * Account-level Payment Amount: $20 
     */
    public void testPaymentWithScenario2Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentWithScenario2Config2>>> \n\n" );
        //Setup payment scenario: do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario2(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -2000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The proportioning should be done according to the sub_owing_amt/account_owing_amt ratio. */
        
        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
        
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), THIRD_SUB);
        verifyNoTransactionsForSubscriber(getContext(), FOURTH_SUB);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testPaymentWithScenario2Config2>>> \n\n" );
    }
    
    /** 
     * testPaymentWithScenario2Config3
     * Allow payments to inactive subscribers and prioritize payments to inactive subscribers.
     * Scenario 2, Total Owing: $200 
     * Account-level Payment Amount: $20 
     * @since 8.3 No different than previous test.
     */
//    public void testPaymentWithScenario2Config3()
//    {
//        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
//                "UNIT TEST begin: <<<testPaymentWithScenario2Config3>>> \n\n" );
//        //Setup payment scenario:      * Allow payments to inactive subscribers and give them priority.
//        setupConfig3(getContext());
//        setupScenario2(getContext());
//        
//        //Create Over Payment transaction (Payments are negative signed values)
//        int paymentAmountInCents = -2000;
//        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
//        
//        //run Payment Splitting process.
//        try
//        {
//            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
//        }
//        catch(HomeException e)
//        {
//            fail("Failed proportioning due to " + e.getMessage());
//        }
//        
//        /* Verify the results.  
//         * The proportioning should be done according to the sub_owing_amt/account_owing_amt ratio,
//         * with an inactive subscriber receiving the remainder of the payment at the end. */
//        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.INACTIVE);
//        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB5_ID);
//        
//        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN, TestSetupAccountHierarchy.SUB5_ID, 
//                (int) (paymentAmountInCents * 0.75) , AdjustmentTypeEnum.StandardPayments_INDEX);
//        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
//                (int) (paymentAmountInCents * 0.25), AdjustmentTypeEnum.StandardPayments_INDEX);
//        verifyNoTransactionsForSubscriber(getContext(), THIRD_SUB);
//        verifyNoTransactionsForSubscriber(getContext(), FOURTH_SUB);
//        verifyPrepaidSubTransactions(getContext());
//        verifyNewTransactions(getContext(), 2, paymentAmountInCents);
//        
//        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
//            "UNIT TEST end: <<<testPaymentWithScenario2Config3>>> \n\n" );
//    }
    

    /** testExactPaymentWithScenario2Config1
     * Allow Payments to inactive subscribers, no priority to inactive subscribers
     * Scenario 2, Total Owing: $200 
     * Account-level Payment Amount: $200 
     */
    public void testExactPaymentWithScenario2Config1()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testExactPaymentWithScenario2Config1>>> \n\n" );
        //Setup payment scenario: Allow Payments to inactive subscribers, no priority
        setupConfig1(getContext());
        setupScenario2(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -20000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The proportioning should be done according to the sub_owing_amt/account_owing_amt ratio. */
        
        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
        
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN, TestSetupAccountHierarchy.SUB5_ID, 
                -15000, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                -5000, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), THIRD_SUB);
        verifyNoTransactionsForSubscriber(getContext(), FOURTH_SUB);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 2, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testExactPaymentWithScenario2Config1>>> \n\n" );
    }
    
    
    /** testExactPaymentWithScenario2Config2
     * Don't allow payments to inactive subscribers
     * Scenario 2, Total Owing: $50 
     * Account-level Payment Amount: $50 
     */
    public void testExactPaymentWithScenario2Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testExactPaymentWithScenario2Config2>>> \n\n" );
        //Setup payment scenario: do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario2(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -5000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The proportioning should be done according to the sub_owing_amt/account_owing_amt ratio. */
        
        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
        
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), THIRD_SUB);
        verifyNoTransactionsForSubscriber(getContext(), FOURTH_SUB);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testExactPaymentWithScenario2Config2>>> \n\n" );
    }
    
    /** testExactPaymentWithScenario2Config3
     * Allow payments to inactive subscribers and prioritize payments to inactive subscribers.
     * Scenario 2, Total Owing: $200 
     * Account-level Payment Amount: $200 
     * 
     * @since 8.3 No different than previous test.
     */
//    public void testExactPaymentWithScenario2Config3()
//    {
//        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
//                "UNIT TEST begin: <<<testExactPaymentWithScenario2Config3>>> \n\n" );
//        //Setup payment scenario:      * Allow payments to inactive subscribers and give them priority.
//        setupConfig3(getContext());
//        setupScenario2(getContext());
//        
//        //Create Over Payment transaction (Payments are negative signed values)
//        int paymentAmountInCents = -20000;
//        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
//        
//        //run Payment Splitting process.
//        try
//        {
//            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
//        }
//        catch(HomeException e)
//        {
//            fail("Failed proportioning due to " + e.getMessage());
//        }
//        
//        /* Verify the results.  
//         * The proportioning should be give the inactive subscriber all the payment, since
//         * the inactive subscriber has an owing balance that is greater than the payment amount.*/
//
//        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.INACTIVE);
//        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB5_ID);
//        
//        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN, TestSetupAccountHierarchy.SUB5_ID, 
//                15000, AdjustmentTypeEnum.StandardPayments_INDEX);
//        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
//                5000, AdjustmentTypeEnum.StandardPayments_INDEX);
//        verifyNoTransactionsForSubscriber(getContext(), THIRD_SUB);
//        verifyNoTransactionsForSubscriber(getContext(), FOURTH_SUB);
//        
//        verifyPrepaidSubTransactions(getContext());
//        verifyNewTransactions(getContext(), 2, paymentAmountInCents);
//        
//        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
//            "UNIT TEST end: <<<testExactPaymentWithScenario2Config3>>> \n\n" );
//    }
    
    
    /** 
     * testOverpaymentWithScenario2Config1
     * Config1 Allow Payments to inactive subscribers, no priority to inactive subscribers
     * Scenario 2, Total Owing: $200 
     * Account-level Payment Amount: $300 
     */
    public void testOverpaymentWithScenario2Config1()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testOverpaymentWithScenario2Config1>>> \n\n" );
        //Setup payment scenario: Allow Payments to inactive subscribers, no priority
        setupConfig1(getContext());
        setupScenario2(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -30000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        Subscriber subWithRemainder = paymentProcessor.getUnitTestSubAssignedWithPaymentRemainder(getContext());
        
        /* Verify the results.  
         * The Proportioning should pay all the owing amounts.  The Inactive subscribers will not get any extra 
         * overpayment amount.  The remaining Over Payment amount is split evenly among all the Active Postpaid Subscribers.*/
        
        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
        
        //OverPayment Amount per active subscriber in cents
        long overpaymentAmtFloor = Double.valueOf(Math.floor((Math.abs(paymentAmountInCents)-20000)/3)).longValue();
        long overpaymentAmtCeiling = overpaymentAmtFloor + 1; // in this case I can be sure that the largest value is 1 off.
        
        long value; // Payment transaction value to verify
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN, TestSetupAccountHierarchy.SUB5_ID, 
                -15000, AdjustmentTypeEnum.StandardPayments_INDEX);
        
        {
            if (subWithRemainder.getId().equals(TestSetupAccountHierarchy.SUB6_ID))
            {
                value = -(5000 + overpaymentAmtCeiling);
            }
            else
            {
                value = -(5000 + overpaymentAmtFloor);
            }
    
            verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                    value, AdjustmentTypeEnum.StandardPayments_INDEX);
        }
    
        {
            if (subWithRemainder.getId().equals(THIRD_SUB))
            {
                value = -overpaymentAmtCeiling;
            }
            else
            {
                value = -overpaymentAmtFloor;
            }
            verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
                    value, AdjustmentTypeEnum.StandardPayments_INDEX);
        }
        
        {
            if (subWithRemainder.getId().equals(FOURTH_SUB))
            {
                value = -overpaymentAmtCeiling;
            }
            else
            {
                value = -overpaymentAmtFloor;
            }
            verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                value, AdjustmentTypeEnum.StandardPayments_INDEX);
        }
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 4, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testOverpaymentWithScenario2Config1>>> \n\n" );
    }

    /** 
     * testOverpaymentWithScenario2Config2
     * Don't allow payments to inactive subscribers
     * Scenario 2, Total Owing: $200 
     * Account-level Payment Amount: $300 
     */
    public void testOverpaymentWithScenario2Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testOverpaymentWithScenario2Config2>>> \n\n" );
        //Setup payment scenario: do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario2(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -30000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        Subscriber subWithRemainder = paymentProcessor.getUnitTestSubAssignedWithPaymentRemainder(getContext());
        
        /* Verify the results.  
         * The Proportioning should pay all the owing amounts.  The Inactive subscribers will not get any extra 
         * overpayment amount.  The remaining Over Payment amount is split evenly among all the Active Postpaid 
         * Subscribers.*/
        
        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
        
        //OverPayment Amount per active subscriber in cents
        long overpaymentAmtFloor = Double.valueOf(Math.floor((Math.abs(paymentAmountInCents)-5000)/3)).longValue();
        long overpaymentAmtCeiling = overpaymentAmtFloor + 1; // in this case I can be sure that the largest value is 1 off.
        
        long value; // Payment transaction value to verify
        
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        
        {
            if (subWithRemainder.getId().equals(TestSetupAccountHierarchy.SUB6_ID))
            {
                value = -(5000 + overpaymentAmtCeiling);
            }
            else
            {
                value = -(5000 + overpaymentAmtFloor);
            }
            verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                    value , AdjustmentTypeEnum.StandardPayments_INDEX);
        }
        
        {
            if (subWithRemainder.getId().equals(THIRD_SUB))
            {
                value = -overpaymentAmtCeiling;
            }
            else
            {
                value = -overpaymentAmtFloor;
            }
            verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
                    value, AdjustmentTypeEnum.StandardPayments_INDEX);
        }
        
        {
            if (subWithRemainder.getId().equals(FOURTH_SUB))
            {
                value = -overpaymentAmtCeiling;
            }
            else
            {
                value = -overpaymentAmtFloor;
            }
            verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                    value, AdjustmentTypeEnum.StandardPayments_INDEX);
        }
        
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 3, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testOverpaymentWithScenario2Config2>>> \n\n" );
    }
    
    
    /** 
     * testOverpaymentWithScenario2Config3
     * Allow Payments to inactive subscribers, with priority to inactive subscribers
     * Scenario 2, Total Owing: $200 
     * Account-level Payment Amount: $300 
     * 
     * @since 8.3 No different than previous test.
     */
//    public void testOverpaymentWithScenario2Config3()
//    {
//        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
//                "UNIT TEST begin: <<<testOverpaymentWithScenario2Config3>>> \n\n" );
//        //Setup payment scenario: Allow Payments to inactive subscribers, giving them priority.
//        setupConfig3(getContext());
//        setupScenario2(getContext());
//        
//        //Create Over Payment transaction (Payments are negative signed values)
//        long paymentAmountInCents = -30000;
//        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
//        
//        //run Payment Splitting process.
//        try
//        {
//            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
//        }
//        catch(HomeException e)
//        {
//            fail("Failed proportioning due to " + e.getMessage());
//        }
//        
//        Subscriber subWithRemainder = paymentProcessor.getUnitTestSubAssignedWithPaymentRemainder(getContext());
//
//        
//        /* Verify the results.  
//         * The Proportioning should pay all the owing amounts.  The Inactive subscribers will not get any extra 
//         * over payment amount.  The remaining Over Payment amount is split evenly among all the Active Postpaid Subscribers.*/
//        
//        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
//        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.INACTIVE);
//        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB5_ID);
//        
//        //OverPayment Amount per active subscriber in cents
//        long overpaymentAmtFloor = Double.valueOf(Math.floor((paymentAmountInCents)-20000/(double)3)).intValue();
//        long overpaymentAmtCeiling = overpaymentAmtFloor + 1; // in this case I can be sure that the largest value is 1 off.
//
//        long value; // Payment transaction value to verify
//
//        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN, TestSetupAccountHierarchy.SUB5_ID, 
//                15000, AdjustmentTypeEnum.StandardPayments_INDEX);
//        
//        {
//            if (subWithRemainder.getId().equals(TestSetupAccountHierarchy.SUB6_ID))
//            {
//                value = -(5000 + overpaymentAmtCeiling);
//            }
//            else
//            {
//                value = -(5000 + overpaymentAmtFloor);
//            }
//            verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
//                value, AdjustmentTypeEnum.StandardPayments_INDEX);
//        }
//        
//        {
//            if (subWithRemainder.getId().equals(THIRD_SUB))
//            {
//                value = -overpaymentAmtCeiling;
//            }
//            else
//            {
//                value = -overpaymentAmtFloor;
//            }
//            verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
//                value, AdjustmentTypeEnum.StandardPayments_INDEX);
//        }
//
//        {
//            if (subWithRemainder.getId().equals(FOURTH_SUB))
//            {
//                value = -overpaymentAmtCeiling;
//            }
//            else
//            {
//                value = -overpaymentAmtFloor;
//            }
//            verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
//                value, AdjustmentTypeEnum.StandardPayments_INDEX);
//        }
//        verifyPrepaidSubTransactions(getContext());
//        verifyNewTransactions(getContext(), 4, paymentAmountInCents);
//        
//        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
//            "UNIT TEST end: <<<testOverpaymentWithScenario2Config3>>> \n\n" );
//    }

    /** testPaymentWithNoActivePostpaidSubscribers
     * Config2: Configuration doesn't allow payments to inactive subscribers. 
     * Scenario 3, Total Owing: $200
     * Account-level Payment Amount: $100 
     */
    public void testPaymentWithNoActivePostpaidSubscribers()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentWithNoActivePostpaidSubscribers>>> \n\n" );
        //Setup payment scenario: do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario3(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        int paymentAmountInCents = -10000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
            /* Previous to CRM 8.3, processing a payment to Deactivated Subscriptions would fail with an error.
             * Since porting the CRM 7.3 improved way of making Payments, payments to an account with no
             * active subscriptions forces a payment to any Deactivated subscription that has an OBO > 0.
             * 
             * //Previous to CRM 8.3, Test Should not succeed without error.
               fail("Proportioning is supposed to fail in this case.");
             */
            verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN, TestSetupAccountHierarchy.SUB5_ID, 
                    paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        }
        catch(HomeException e)
        {
            if (e.getCause() instanceof ProportioningCalculatorException)
            {
                printAllTransactions(getContext());
                verifyPrepaidSubTransactions(getContext());
                verifyNewTransactions(getContext(), 0, 0);
                LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                        "UNIT TEST end: <<<testPaymentWithNoActivePostpaidSubscribers>>> \n\n" );
            }
            else
            {
                fail("Failed proportioning due to " + e.getMessage());
            }
        }
    }
    
    
    /** testPaymentWithScenario5_1Config2
     * Do not allow payments to inactive subscribers
     * Scenario 5, Total Owing: $120, Total Installments charged: $250 and not invoiced (Total Loan: $1000)
     * Account-level Payment Amount: $10 
     * 
     * @since 8.3: Should pay the payment plan charges, even if  they haven't been invoiced yet.
     */
    public void testPaymentPlanPaymentWithScenario5Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentPlanPaymentWithScenario5Config2>>> \n\n" );
        //Do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario5(getContext());
        
        //Create Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -10000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The proportioning should pay the payment plan amount first. */

        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
                
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN);
        // Portion of loan to SUB6_ID is 70%
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                Double.valueOf(paymentAmountInCents * 0.7).intValue(), AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        // Portion of loan to SUB6_ID is 20%
        verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
                Double.valueOf(paymentAmountInCents * 0.2).intValue(), AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        // Portion of loan to SUB6_ID is 10%
        verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                Double.valueOf(paymentAmountInCents * 0.1).intValue(), AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 3, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testPaymentPlanPaymentWithScenario5Config2>>> \n\n" );
    }
    
    
    /** testPaymentWithScenario5Config2
     * Do not allow payments to inactive subscribers
     * Scenario 5, Total Owing: $120, Total Installments charged: $250 (Total Loan: $1000)
     * Account-level Payment Amount: $10  Over Payment to the Payment Plan Installment Charges overflows
     * to payments to regular charges.
     * 
     * @since 8.3 this test is still failing because we haven't developed a way to easily get the 
     * most deserving subscriber for the amount remainder.  It should be a subscriber with OBO still
     * unpaid, not just the first postpaid subscriber off a list.
     */
    public void testPaymentPlanOverPaymentPayingRegularChargesWithScenario5Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentPlanOverPaymentPayingRegularChargesWithScenario5Config2>>> \n\n" );
        //Do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario5(getContext());
        
        //Create Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -28000;
        long loanAmountInCents = 25000;
        long overpayment = paymentAmountInCents + loanAmountInCents; 
        //Since all charges are logged equally, split equally.
        long overpaymentSplit = overpayment/3;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The proportioning should pay the payment plan amount first. */

        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
                
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN);
        // Portion of loan to SUB6_ID is 70% of charged loans fees
        long value = Double.valueOf(-loanAmountInCents * 0.7).intValue();
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                value, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                -2765, AdjustmentTypeEnum.StandardPayments_INDEX);
        
        // Portion of loan to SUB6_ID is 20% of charged loans fees
        value = Double.valueOf(-loanAmountInCents * 0.2).intValue();
        verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
                value, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        
        // Portion of loan to SUB6_ID is 10% of charged loans fees
        value = Double.valueOf(-loanAmountInCents * 0.1).intValue();
        verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                value, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                -234, AdjustmentTypeEnum.StandardPayments_INDEX);
        
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 5, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testPaymentPlanOverPaymentPayingRegularChargesWithScenario5Config2>>> \n\n" );
    }
    
    /** testPaymentWithScenario5Config2
     * Do not allow payments to inactive subscribers
     * Scenario 5, Total Owing: $120, Total Installments charged: $250 (Total Loan: $1000)
     * Account-level Payment Amount: $10  Over Payment to the Payment Plan Installment Charges overflows
     * to payments to regular charges.
     * 
     * @since 8.3 this test is still failing because we haven't developed a way to easily get the 
     * most deserving subscriber for the amount remainder.  It should be a subscriber with OBO still
     * unpaid, not just the first postpaid subscriber off a list.
     */
    public void testPaymentPlanOverPaymentWithScenario5Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentPlanOverPaymentWithScenario5Config2>>> \n\n" );
        //Do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario5(getContext());
        
        //Create Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -80000;
        long loanAmountInCents = 25000;
        long overpayment = paymentAmountInCents + loanAmountInCents + 7000; 
        //Since all charges are logged equally, split equally.
        long overpaymentSplit = overpayment/3;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The proportioning should pay the payment plan amount first. */

        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
                
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN);
        // Portion of loan to SUB6_ID is 70% of charged loans fees
        long value = Double.valueOf(-loanAmountInCents * 0.7).intValue();
        // Artifact of uneven split
        long remainder = 100;
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                value + overpaymentSplit + remainder, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                -12000, AdjustmentTypeEnum.StandardPayments_INDEX);
        
        // Portion of loan to SUB6_ID is 20% of charged loans fees
        value = Double.valueOf(-loanAmountInCents * 0.2).intValue();
        verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
                value + overpaymentSplit, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
                -5000, AdjustmentTypeEnum.StandardPayments_INDEX);
        
        // Portion of loan to SUB6_ID is 10% of charged loans fees
        value = Double.valueOf(-loanAmountInCents * 0.1).intValue();
        verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                value + overpaymentSplit, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                overpaymentSplit, AdjustmentTypeEnum.StandardPayments_INDEX);
        
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 6, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testPaymentPlanOverPaymentWithScenario5Config2>>> \n\n" );
    }

    /** testExactPaymentWithScenario5Config2
     * Do not allow payments to inactive subscribers
     * Scenario 5, Total Owing: $120, Total Installments charged: $250 (Total Loan: $1000)
     * Account-level Payment Amount: $370
     */
    public void testExactPaymentWithScenario5Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testExactPaymentWithScenario5Config2>>> \n\n" );
        //Do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario5(getContext());
        
        //Create Payment transaction (Payments are negative signed values)
        int paymentAmountInCents = -37000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The proportioning should pay the payment plan amount first. */
        long portionForPaymenPlanPayment = 25000;
        long totalOwing = 12000; //from scenario setup.
        // Payment Plan remaining Loan
        long portionForPPOverPayment = 0;
        long portionForNormalPayment = totalOwing;
        long portionForNormalOverpayment = paymentAmountInCents - portionForPaymenPlanPayment 
                                           - portionForNormalPayment - portionForPPOverPayment;
        assertEquals("portionForNormalOverpayment should be 0", 0, portionForNormalOverpayment);
        
        verifyPrepaidSubTransactions(getContext());
        // 3 Payment Plan Transactions and 1 Transaction for the normal payment (normal charges)
        verifyNewTransactions(getContext(), 4, paymentAmountInCents);
        
        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
        /* For the sake of the remainder of the test we will assert that the subscriber that obtains the remainder
         * is THIRD_SUB, although it doesn't necessarily have to be.  This will make verifying the payment totals
         * more straight forward. The subscriber that obtains the remaining amount obtains the fraction large payment.
         */
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, THIRD_SUB);
        
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN);
        //Verify Payment Plan Payment Splitting
        long remainingPPPayments = portionForPaymenPlanPayment + portionForPPOverPayment;
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                portionForPaymenPlanPayment/3 + portionForPPOverPayment/3, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        remainingPPPayments -= portionForPaymenPlanPayment/3 + portionForPPOverPayment/3;
        verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                portionForPaymenPlanPayment/3 + portionForPPOverPayment/3, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        remainingPPPayments -= portionForPaymenPlanPayment/3 + portionForPPOverPayment/3;
        //verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
        //        8333 + portionForPPOverPayment/3, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
                remainingPPPayments, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        
        //Verify Over Payment Splitting
        long remainingNormalPayment = portionForNormalPayment + portionForNormalOverpayment;
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                portionForNormalPayment + portionForNormalOverpayment/3, AdjustmentTypeEnum.StandardPayments_INDEX);
        remainingNormalPayment += -(portionForNormalPayment + portionForNormalOverpayment/3);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testExactPaymentWithScenario5Config2>>> \n\n" );
    }
    
    
    /** testExactLoanPaymentWithScenario5Config2
     * Do not allow payments to inactive subscribers
     * Scenario 5, Total Owing: $120, Total Installments charged: $250 (Total Loan: $1000)
     * Account-level Payment Amount: $1120
     */
    public void testExactLoanPaymentWithScenario5Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testExactLoanPaymentWithScenario5Config2>>> \n\n" );
        //Do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario5(getContext());
        
        //Create Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -112000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The proportioning should pay the payment plan amount first. */
        long portionForPaymenPlanPayment = 25000;
        long totalOwing = 12000; //from scenario setup.
        // Payment Plan remaining Loan
        long portionForPPOverPayment = 100000 - portionForPaymenPlanPayment;
        long portionForNormalPayment = totalOwing;
        long portionForNormalOverpayment = paymentAmountInCents - portionForPaymenPlanPayment 
                                           - portionForNormalPayment - portionForPPOverPayment;
        assertEquals("portionForNormalOverpayment should be 0", 0, portionForNormalOverpayment);
        
        verifyPrepaidSubTransactions(getContext());
        // 3 Payment Plan Transactions and 1 Transaction for the normal payment (normal charges)
        verifyNewTransactions(getContext(), 4, paymentAmountInCents);
        
        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
        /* For the sake of the remainder of the test we will assert that the subscriber that obtains the remainder
         * is THIRD_SUB, although it doesn't necessarily have to be.  This will make verifying the payment totals
         * more straight forward. The subscriber that obtains the remaining amount obtains the fraction large payment.
         */
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, THIRD_SUB);
        
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN);
        //Verify Payment Plan Payment Splitting
        long remainingPPPayments = portionForPaymenPlanPayment + portionForPPOverPayment;
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                portionForPaymenPlanPayment/3 + portionForPPOverPayment/3, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        remainingPPPayments -= portionForPaymenPlanPayment/3 + portionForPPOverPayment/3;
        verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                portionForPaymenPlanPayment/3 + portionForPPOverPayment/3, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        remainingPPPayments -= portionForPaymenPlanPayment/3 + portionForPPOverPayment/3;
        //verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
        //        8333 + portionForPPOverPayment/3, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
                remainingPPPayments, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        
        //Verify Over Payment Splitting
        long remainingNormalPayment = portionForNormalPayment + portionForNormalOverpayment;
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                portionForNormalPayment + portionForNormalOverpayment/3, AdjustmentTypeEnum.StandardPayments_INDEX);
        remainingNormalPayment += -(portionForNormalPayment + portionForNormalOverpayment/3);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testExactLoanPaymentWithScenario5Config2>>> \n\n" );
    }
    
    
    /** testOverPaymentWithScenario5Config2
     * Do not allow payments to inactive subscribers
     * Scenario 5, Total Owing: $120, Total Loan: $1000
     * Account-level Payment Amount: $1500 
     */
    public void testOverPaymentWithScenario5Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testOverPaymentWithScenario5Config2>>> \n\n" );
        //Do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario5(getContext());
        
        //Create Payment transaction (Payments are negative signed values)
        int paymentAmountInCents = -150000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The proportioning should pay the payment plan amount first. */
        long portionForPaymenPlanPayment = 25000;
        long totalOwing = 12000; //from scenario setup.
        // Payment Plan remaining Loan
        long portionForPPOverPayment = 100000 - portionForPaymenPlanPayment;
        long portionForNormalPayment = totalOwing;
        long portionForNormalOverpayment = paymentAmountInCents - portionForPaymenPlanPayment 
                                           - portionForNormalPayment - portionForPPOverPayment;
        
        verifyPrepaidSubTransactions(getContext());
        // 3 Payment Plan Transactions and 3 Regular payment (over payment) transactions
        verifyNewTransactions(getContext(), 6, paymentAmountInCents);
        
        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
        /* For the sake of the remainder of the test we will assert that the subscriber that obtains the remainder
         * is THIRD_SUB, although it doesn't necessarily have to be.  This will make verifying the payment totals
         * more straight forward. The subscriber that obtains the remaining amount obtains the fraction large payment.
         */
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, THIRD_SUB);
        
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_MSISDN);
        //Verify Payment Plan Payment Splitting
        long remainingPPPayments = portionForPaymenPlanPayment + portionForPPOverPayment;
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                portionForPaymenPlanPayment/3 + portionForPPOverPayment/3, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        remainingPPPayments -= portionForPaymenPlanPayment/3 + portionForPPOverPayment/3;
        verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                portionForPaymenPlanPayment/3 + portionForPPOverPayment/3, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        remainingPPPayments -= portionForPaymenPlanPayment/3 + portionForPPOverPayment/3;
        //verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
        //        8333 + portionForPPOverPayment/3, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
                remainingPPPayments, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX);
        
        //Verify Over Payment Splitting
        long remainingNormalPayment = portionForNormalPayment + portionForNormalOverpayment;
        verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB,   
                portionForNormalOverpayment/3, AdjustmentTypeEnum.StandardPayments_INDEX);
        remainingNormalPayment -= portionForNormalOverpayment/3;
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                portionForNormalPayment + portionForNormalOverpayment/3, AdjustmentTypeEnum.StandardPayments_INDEX);
        remainingNormalPayment += -(portionForNormalPayment + portionForNormalOverpayment/3);
        //verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB,  
        //        portionForNormalOverpayment/3, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB,  
                remainingNormalPayment, AdjustmentTypeEnum.StandardPayments_INDEX);
        
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testOverPaymentWithScenario5Config2>>> \n\n" );
    }

    /**
     * Testing AbstractProportioningCalculator.testGetFirstPostpaidSubscriber
     * used to designate subAssignedForRemaining in the Proportioning Calculations.
     * 
     * Regardless of the system configuration, the subscriber selected to get the remaining payment
     * cannot be an inactive subscriber with existing credit balance.
     */
    public void testGetFirstPostpaidSubscriber()
    {
        setupScenario1(getContext());
        setupConfig3(getContext());
    
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB6_ID);
    }
    
    /** testPaymentReversalWithScenario1Config1
     * Allow Payments to inactive subscribers, no priority to inactive subscribers
     * Scenario 1, Total Owing: $30 
     * Account-level Payment Amount: $5 
     */
    public void testPaymentReversalWithScenario1Config1()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentReversalWithScenario1Config1>>> \n\n");
        
        //Setup payment scenario: Allow Payments to inactive subscribers, no priority
        setupConfig1(getContext());
        setupScenario1(getContext());
        
        //Create Over Payment Reversal transaction (Payment reversal amounts are unsigned amounts)
        long paymentAmountInCents = 500;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
    
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The transaction should be given entirely to the active subscriber, 
         * since the inactive subscriber has a credit total. */
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB6_ID);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.info(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST1 end: <<<testPaymentReversalWithScenario1Config1>>> \n\n");
    }

    /** testPaymentReversalWithScenario1Config2
     * Don't Allow payments to inactive subscribers
     * Scenario 1, Total Owing: $30 
     * Account-level Payment Reversal Amount: $5 
     */
    public void testPaymentReversalWithScenario1Config2()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentReversalWithScenario1Config2>>> \n\n");
        
        //Setup payment scenario: do not allow payments to inactive subscribers
        setupConfig2(getContext());
        setupScenario1(getContext());
        
        //Create Over Payment Reversal transaction  (unsigned amount)
        long paymentAmountInCents = 500;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
    
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The transaction should be given entirely to the active subscriber, 
         * since the inactive subscriber has a credit total. */
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB6_ID);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST end: <<<testPaymentWithScenario1Config2>>> \n\n");
    }
    
    /** 
     * testPaymentReversalWithScenario2Config1
     * Config1 Allow Payments to inactive subscribers, no priority to inactive subscribers
     * Scenario 2, Total Owing: $200 
     * Account-level Payment Amount: -$300 (Reversal)
     */
    public void testPaymentReversalWithScenario2Config1()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentReversalWithScenario2Config1>>> \n\n" );
        //Setup payment scenario: Allow Payments to inactive subscribers, no priority
        setupConfig1(getContext());
        setupScenario2(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = 30000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        Subscriber subWithRemainder = paymentProcessor.getUnitTestSubAssignedWithPaymentRemainder(getContext());
        
        /* Verify the results.  
         * The Proportioning should pay all the owing amounts.  The Inactive subscribers will not get any extra 
         * overpayment amount.  The remaining Over Payment amount is split evenly among all the Active Postpaid Subscribers.*/
        
        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
        
        //OverPayment Amount per active subscriber in cents
        long overpaymentAmtFloor = Double.valueOf(Math.floor(paymentAmountInCents/3)).intValue();
        long overpaymentAmtCeiling = overpaymentAmtFloor + 1; // in this case I can be sure that the largest value is 1 off.
        
        long value; // Payment transaction value to verify
        {
            if (subWithRemainder.getId().equals(TestSetupAccountHierarchy.SUB6_ID))
            {
                value = overpaymentAmtCeiling;
            }
            else
            {
                value = overpaymentAmtFloor;
            }
    
            verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                    value, AdjustmentTypeEnum.StandardPayments_INDEX);
        }
    
        {
            if (subWithRemainder.getId().equals(THIRD_SUB))
            {
                value = overpaymentAmtCeiling;
            }
            else
            {
                value = overpaymentAmtFloor;
            }
            verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
                    value, AdjustmentTypeEnum.StandardPayments_INDEX);
        }
        
        {
            if (subWithRemainder.getId().equals(FOURTH_SUB))
            {
                value = overpaymentAmtCeiling;
            }
            else
            {
                value = overpaymentAmtFloor;
            }
            verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                value, AdjustmentTypeEnum.StandardPayments_INDEX);
        }
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 3, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testPaymentReversalWithScenario2Config1>>> \n\n" );
    }
    
    /** testPaymentWithScenario7Config1
     * Allow Payments to inactive subscribers, no priority to inactive subscribers
     * Scenario 7, Total Owing: $0 
     * Account-level Payment Amount: $10 
     */
    public void testPaymentWithScenario7Config1()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentWithScenario7Config1>>> \n\n");
        
        //Setup payment scenario: Allow Payments to inactive subscribers, no priority
        setupConfig1(getContext());
        setupScenario7(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -1000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
    
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The transaction should be given entirely to the active subscriber, 
         * since the inactive subscriber has a credit total. */
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB6_ID);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.info(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST1 end: <<<testPaymentWithScenario7Config1>>> \n\n");
    }
    
    /** testOverPaymentWithScenario7Config1
     * Allow Payments to inactive subscribers, no priority to inactive subscribers
     * Scenario 7, Total Owing: $0 
     * Account-level Payment Amount: $40 
     */
    public void testOverPaymentWithScenario7Config1()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testOverPaymentWithScenario7Config1>>> \n\n");
        
        //Setup payment scenario: Allow Payments to inactive subscribers, no priority
        setupConfig1(getContext());
        setupScenario7(getContext());
        
        //Create Over Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -4000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);

        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The transaction should be given entirely to the active subscriber, 
         * since the inactive subscriber has a credit total. */
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                paymentAmountInCents, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        assertPriorityRecipient(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, TestSetupAccountHierarchy.SUB6_ID);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 1, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST end: <<<testOverPaymentWithScenario7Config1>>> \n\n");
    }
    
    /** testPaymentWithScenario8Config1
     * Allow Payments to inactive subscribers, no priority to inactive subscribers
     * Scenario 8, Total Owing: $0 
     * Account-level Payment Amount: $100 
     */
    public void testPaymentWithScenario8Config1()
    {
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
                "UNIT TEST begin: <<<testPaymentWithScenario8Config1>>> \n\n" );
        //Setup payment scenario: Allow Payments to inactive subscribers, no priority
        setupConfig1(getContext());
        setupScenario8(getContext());
        
        //Create Payment transaction (Payments are negative signed values)
        long paymentAmountInCents = -10000;
        Transaction transaction = createPaymentTransaction(TestSetupAccountHierarchy.ACCOUNT4_BAN, paymentAmountInCents);
        
        //run Payment Splitting process.
        try
        {
            paymentProcessor.handleRegularAccountTransaction(getContext(), transaction);
        }
        catch(HomeException e)
        {
            fail("Failed proportioning due to " + e.getMessage());
        }
        
        /* Verify the results.  
         * The proportioning should be done as an overpayment, all active subscriptions get an even share. */
        
        //Since there is more than one Postpaid Active Subscriber we cannot assert the SUB ID.
        assertPriorityRecipientState(getContext(), TestSetupAccountHierarchy.ACCOUNT4_BAN, SubscriberStateEnum.ACTIVE);
        
        //@since 8.3 Inactive accounts don't receive over payments
        verifyNoTransactionsForSubscriber(getContext(), TestSetupAccountHierarchy.SUB5_ID);
        
        //The choice for this subscriber being the assignee is arbitrary.  See Notes at the top of the test class. 
        verifyResultTransaction(getContext(), TestSetupAccountHierarchy.SUB6_MSISDN, TestSetupAccountHierarchy.SUB6_ID, 
                -3333, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyResultTransaction(getContext(), THIRD_SUB_MSISDN, THIRD_SUB, 
                -3334, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyResultTransaction(getContext(), FOURTH_SUB_MSISDN, FOURTH_SUB, 
                -3333, AdjustmentTypeEnum.StandardPayments_INDEX);
        verifyPrepaidSubTransactions(getContext());
        verifyNewTransactions(getContext(), 3, paymentAmountInCents);
        
        LogSupport.debug(getContext(), TestAccountPaymentTransactionProcessor.class.getName(),
            "UNIT TEST end: <<<testPaymentWithScenario8Config1>>> \n\n" );
    }
    
    /**
     * Verify that the recipient of the final payment is of this Subscriber Service Type.
     * Precondition: the paymentProcessor should have already executed the handleRegularPayments method.
     * @param ctx
     * @param ban
     * @param state
     */
    public void assertPriorityRecipientState(Context ctx, String ban, SubscriberStateEnum state)
    {
        Home home = (Home) getContext().get(AccountHome.class);
        Account account = null;
        try
        {
            account = (Account) home.find(ctx, ban);
            assertNotNull(account);
        }
        catch (HomeException e)
        {
            fail("Failed test setup. " + e.getMessage());
        }

        try
        {
            Home spidHome = (Home) getContext().get(CRMSpidHome.class);
            CRMSpid spid = (CRMSpid)spidHome.find(ctx, Integer.valueOf(TestSetupAccountHierarchy.SPID_ID));
            assertNotNull(spid);

            Transaction testTrans = new Transaction();
            testTrans.setAcctNum(account.getBAN());

            //the paymentProcessor should have already executed the handleRegularPayments method.
            final Subscriber paymentRecipient = paymentProcessor.getUnitTestSubAssignedWithPaymentRemainder(ctx);
            assertNotNull("No subscriber selected for balance of payment.", paymentRecipient);
            assertEquals("Subscriber selected for balance of payment was of incorrect Subscriber State. ", 
                    state, paymentRecipient.getState());
        }
        catch (HomeException e)
        {
            fail("Failed test setup. " + e.getMessage());
        }
    }
    
    public void assertPriorityRecipient(Context ctx, String ban, String expectedSubId)
    {
        /*
        Home home = (Home) getContext().get(AccountHome.class);
        Account account = null;
        try
        {
            account = (Account) home.find(ctx, ban);
            assertNotNull(account);
        }
        catch (HomeException e)
        {
            fail("Failed test setup. " + e.getMessage());
        }
        
        try
        {
            Home spidHome = (Home) getContext().get(CRMSpidHome.class);
            CRMSpid spid = (CRMSpid)spidHome.find(ctx, Integer.valueOf(TestSetupAccountHierarchy.SPID_ID));
            assertNotNull(spid);
            
            Transaction testTrans = new Transaction();
            testTrans.setAcctNum(account.getBAN());
            
            Collection<Subscriber> subs = paymentProcessor.get.getAllPostpaidSubscribers();
            
            Subscriber sub = paymentProcessor.getFirstPostpaidSubscriber(ctx, spid, subs);
            assertNotNull("No subscriber selected for balance of payment.", sub);
            assertEquals("Incorrect subscriber selected for balance of payment: ", expectedSubId, sub.getId());
        }
        catch (ProportioningCalculatorException e)
        {
            fail("Failed initializing payment calculator required for retrieving payment recipient.");
        }
        catch (HomeException e)
        {
            fail("Failed test setup. " + e.getMessage());
        }
        */
    }


    /**
     * Class variables
     */
    static AccountPaymentTransactionProcessor paymentProcessor;
    /**
     * Subscriber X
     */
    static final String THIRD_SUB = TestSetupAccountHierarchy.ACCOUNT4_BAN+"-3";
    static final String THIRD_SUB_MSISDN = "3339998888";
    /**
     * Subscriber Y
     */
    static final String FOURTH_SUB = TestSetupAccountHierarchy.ACCOUNT4_BAN+"-4";
    static final String FOURTH_SUB_MSISDN = "3339990000";
    /**
     * Subscriber P
     */
    static final String PREPAID_SUB = TestSetupAccountHierarchy.ACCOUNT4_BAN+"-5";
    static final String PREPAID_SUB_MSISDN = "3337775555";
    
    /**
     * Date used for payment splitting in this test. 
     */
    final Date PAYMENT_DATE = new Date();
    final Date PAYMENT_PLAN_START_DATE = CalendarSupportHelper.get().findDateMonthsAfter(2, TestSetupInvoiceHistory.DEFAULT_INVOICEDATE);
}
