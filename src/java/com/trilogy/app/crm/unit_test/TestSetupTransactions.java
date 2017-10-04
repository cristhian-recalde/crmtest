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

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionTransientHome;
import com.trilogy.app.crm.bean.TransactionXDBHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.payment.PaymentExceptionHome;
import com.trilogy.app.crm.bean.payment.PaymentExceptionTransientHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.unit_test.utils.TransientHomeXDBCmdEmulator;
import com.trilogy.app.crm.unit_test.utils.TransientSequenceIdentifiedSettingHome;
import com.trilogy.app.crm.unit_test.utils.TransientTransactionIdentifierSettingHome;
import com.trilogy.app.crm.xhome.home.TransactionIdentifierSettingHome;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

/**
 * Unit test sets up transactions for the Account Hierarchy in TestSetupAccounHierarchy, 
 * for use in General Unit testing.  
 * 
 * Installs the XDBHome depending on the parameter "installXDB" in the setup(ctx, boolean) 
 * method.  By default installs transient homes to run unit tests.
 * @author Angie Li
 *
 */
public class TestSetupTransactions extends ContextAwareTestCase
{


    public TestSetupTransactions(String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     * 
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by the Redknee Xtest code, which provides the application's operating context.
     * 
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);
        final TestSuite suite = new TestSuite(TestSetupTransactions.class);
        return suite;
    }

    /**
     * By default install only the Transient Homes.
     */
    @Override
    public void setUp()
    {
        super.setUp();
        setup(getContext(), false);
    }

    //  INHERIT
    @Override
    public void tearDown()
    {
        //tear down here
        deleteTransactions(getContext());

        super.tearDown();
    }


    /**
     * Creates account in an account hierarchy
     * @param context
     */
    public static void setup(Context ctx, final boolean installXDB)
    {
        if (ctx.getBoolean(TestSetupTransactions.class, true))
        {
            TestSetupIdentifierSequence.setup(ctx);

            setupTransactionHomes(ctx, installXDB);

            //Subscriber 1
            createAllTransactions(ctx, TestSetupAccountHierarchy.ACCOUNT1_BAN, TestSetupAccountHierarchy.SUB1_MSISDN, TestSetupAccountHierarchy.SUB1_ID);

            //Subscriber 2
            createAllTransactions(ctx, TestSetupAccountHierarchy.ACCOUNT2_BAN, TestSetupAccountHierarchy.SUB2_MSISDN, TestSetupAccountHierarchy.SUB2_ID);

            //Subscriber 3
            createAllTransactions(ctx, TestSetupAccountHierarchy.ACCOUNT2_BAN, TestSetupAccountHierarchy.SUB3_MSISDN, TestSetupAccountHierarchy.SUB3_ID);
            
            //Install TestSetupTransactions key to prevent setup from happening multiple times.
            ctx.put(TestSetupTransactions.class, false);
        }
        else
        {
            LogSupport.debug(ctx, TestSetupTransactions.class.getName(), "Skipping TestSetupTransactions setup again.");
        }
    }
    
    public void testSetup()
    {
       testSetup(getContext());
    }
    
    /**
     * Interface to be called by other tests.
     * @param ctx
     */
    public static void testSetup(Context ctx)
    {
        Home home = (Home) ctx.get(TransactionHome.class);
        try
        {
            
            Collection transactionsByID =
                CoreTransactionSupportHelper.get(ctx).getTransactionsForSubscriberID(
                    ctx,
                    TestSetupAccountHierarchy.SUB1_ID,
                    new Date(0),
                    new Date());
            assert(transactionsByID.size() > 0);
            
            transactionsByID =
                CoreTransactionSupportHelper.get(ctx).getTransactionsForSubscriberID(
                    ctx,
                    TestSetupAccountHierarchy.SUB2_ID,
                    new Date(0),
                    new Date());
            assert(transactionsByID.size() > 0);
            
            transactionsByID =
                CoreTransactionSupportHelper.get(ctx).getTransactionsForSubscriberID(
                    ctx,
                    TestSetupAccountHierarchy.SUB3_ID,
                    new Date(0),
                    new Date());
            assert(transactionsByID.size() > 0);
            
            
            Collection col = home.where(ctx, new EQ(TransactionXInfo.MSISDN, TestSetupAccountHierarchy.SUB1_MSISDN)).selectAll();
            assertTrue(col.size() > 0);
            col = home.where(ctx, new EQ(TransactionXInfo.MSISDN, TestSetupAccountHierarchy.SUB2_MSISDN)).selectAll();
            assertTrue(col.size() > 0);
            col = home.where(ctx, new EQ(TransactionXInfo.MSISDN, TestSetupAccountHierarchy.SUB3_MSISDN)).selectAll();
            assertTrue(col.size() > 0);
            
            Home pHome = (Home)ctx.get(PaymentExceptionHome.class);
            assertNotNull("Payment Exception Home is null in the context. ", pHome);
        }
        catch (HomeException e)
        {
            fail("Failed Setup. " + e.getMessage());
        }
    }
    
    /**
     * Install all Transaction Homes
     * + the TransactionHome
     * + the PaymentExceptionHome
     * @param ctx
     * @param installXDB TRUE indicates to install XDB Transaction home.  All other homes are transient.
     */
    public static void setupTransactionHomes(Context ctx, final boolean installXDB)
    {
        try
        {
            if (installXDB)
            {
                // Overwrite
                Home tHome = new TransactionXDBHome(ctx, DB_TABLE_TRANSACTION);
                tHome = new TransactionIdentifierSettingHome(ctx, tHome, "TransactionID_seq");
                tHome = new HomeProxy(ctx, tHome)
                {
                    @Override
                    public Object create(final Context ctx, final Object obj) throws HomeException
                    {
                        LogSupport.debug(ctx, TestSetupTransactions.class.getName(), 
                                "Processing this Transaction through TransactionHome Unit Test pipeline: " + obj.toString());

                        return super.create(ctx, obj);
                    }
                };
                tHome = new SortingHome(tHome); // For easy debug log reading
                
                ctx.put(TransactionHome.class, tHome);
            }
            else
            {
                Home tHome = new TransientFieldResettingHome(ctx, new TransactionTransientHome(ctx));
                tHome = new TransientTransactionIdentifierSettingHome(ctx, tHome);
                tHome = new TransientHomeXDBCmdEmulator(ctx, tHome);
                tHome = new HomeProxy(ctx, tHome)
                {
                    @Override
                    public Object create(final Context ctx, final Object obj) throws HomeException
                    {
                        LogSupport.debug(ctx, TestSetupTransactions.class.getName(), 
                                "Processing this Transaction through TransactionHome Unit Test pipeline: " + obj.toString());

                        return super.create(ctx, obj);
                    }
                };
                tHome = new SortingHome(tHome); // For easy debug log reading
                
                ctx.put(TransactionHome.class, tHome);
            }

        }
        catch (HomeException e)
        {
            fail("Failed Transaction Home Setup. " + e.getMessage());
        }
        
        // Install Payment Exception Home
        try
        {
            Home home = new TransientFieldResettingHome(ctx, new PaymentExceptionTransientHome(ctx));
            home = new TransientSequenceIdentifiedSettingHome(ctx, home, IdentifierEnum.PAYMENTEXCEPTION_ID);
            ctx.put(PaymentExceptionHome.class, home);
        }
        catch (HomeException e)
        {
            fail("Failed Payment Exception Home Setup. " + e.getMessage());
        }
        
    }
    
    /**
     * Delete all unit test records.
     * @param ctx
     */
    public static void deleteTransactions(Context ctx)
    {
        try
        {
            Home home = (Home) ctx.get(TransactionHome.class);
            home.removeAll(ctx);    
        }
        catch (HomeException e)
        {
            fail("Failed to remove all Transaction. " + e.getMessage());
        }
    }
    
    /**
     * Create unit test records
     * @param ctx
     * @param ban
     * @param msisdn
     * @param subscriberID
     */
    private static void createAllTransactions(
            Context ctx, 
            String ban,
            String msisdn,
            String subscriberID)
    {
        createAllTransactions(ctx, ban, msisdn, subscriberID, date_);
    }
    
    /**
     * Create unit test records
     * @param ctx
     * @param ban
     * @param msisdn
     * @param subscriberID
     */
    public static void createAllTransactions(
            Context ctx, 
            String ban,
            String msisdn,
            String subscriberID,
            Date date)
    {
        for (int i=0; i<3 ; i++)
        {
            long amountbal = i*1000;
            createTransaction(ctx, ban, msisdn, subscriberID, AdjustmentTypeActionEnum.DEBIT, oneTime, amountbal, amountbal, date);
            createTransaction(ctx, ban, msisdn, subscriberID, AdjustmentTypeActionEnum.DEBIT, recurr, amountbal, amountbal, date);
            createTransaction(ctx, ban, msisdn, subscriberID, AdjustmentTypeActionEnum.CREDIT, payment, amountbal, amountbal, date);
        }
    }
    
    public static Transaction formTransaction(
            final String ban, 
            final String msisdn,
            final String subscriberID,
            final AdjustmentTypeActionEnum action, 
            final short type, 
            final long amount, 
            final long balance)
    {
        return formTransaction(ban, msisdn, subscriberID, action, type, amount, balance, date_);
    }
    
    public static Transaction formTransaction(
            final String ban, 
            final String msisdn,
            final String subscriberID,
            final AdjustmentTypeActionEnum action, 
            final short type, 
            final long amount, 
            final long balance,
            Date transactionDate)
    {
        Transaction trans = new Transaction();
        trans.setAcctNum(ban);
        trans.setBAN(ban);
        trans.setAction(action);
        trans.setAdjustmentType(type);
        trans.setAgent("Unit Test");
        trans.setAmount(amount);
        trans.setBalance(balance);
        trans.setTransDate(transactionDate);
        trans.setSubscriberID(subscriberID);
        trans.setReceiveDate(transactionDate);
        date_ = CalendarSupportHelper.get().findDateDaysAfter(1, transactionDate);
        trans.setMSISDN(msisdn);
        trans.setReceiptNum(idCounter);
        
        return trans;
    }
    
    /**
     * Form and store the Transaction in the system, with the given attributes.
     * @param ctx
     * @param ban
     * @param msisdn
     * @param subscriberID
     * @param action
     * @param type
     * @param amount Amount in cents.
     * @param balance Value in cents.
     * @param transactionDate
     */
    public static void createTransaction(
            Context ctx, 
            final String ban, 
            final String msisdn,
            final String subscriberID,
            final AdjustmentTypeActionEnum action, 
            final short type, 
            final long amount, 
            final long balance,
            Date transactionDate)
    {
        Transaction trans = formTransaction(ban, msisdn, subscriberID, action, type, amount, balance, transactionDate);
        
        try
        {
            Home home = (Home) ctx.get(TransactionHome.class);
            home.create(ctx, trans);
            idCounter++;
        }
        catch (HomeException e)
        {
            new DebugLogMsg("TestSetupTransactions", "Failed to create transaction for sub=" + msisdn, e).log(ctx);
        }
    }
    
    private static short oneTime = AdjustmentTypeEnum.OneTimeCharges_INDEX;
    private static short recurr = AdjustmentTypeEnum.RecurringCharges_INDEX;
    private static short payment = AdjustmentTypeEnum.StandardPayments_INDEX;
    private static Date date_ = new Date(1137042001000L); //Jan 12, 2006
    private static String DB_TABLE_TRANSACTION = "UNITTESTTRANSACTION";
    private static int idCounter = 0;
   
}
