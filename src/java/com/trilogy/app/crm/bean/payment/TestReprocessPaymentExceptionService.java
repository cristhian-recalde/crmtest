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
package com.trilogy.app.crm.bean.payment;

import java.security.Principal;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.home.TransactionRedirectionHome;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;
import com.trilogy.app.crm.unit_test.TestSetupAdjustmentTypes;
import com.trilogy.app.crm.unit_test.TestSetupCallDetails;
import com.trilogy.app.crm.unit_test.TestSetupInvoiceHistory;
import com.trilogy.app.crm.unit_test.TestSetupPaymentPlan;
import com.trilogy.app.crm.unit_test.TestSetupTransactions;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Unit test for ReprocessPaymentExceptionService.  Originally, this class was
 * meant to be run online.
 * 
 * I have made changes for it to run off-line as well. Still fail running this test 
 * off-line because the Subscriber's earliest activity date is done by 
 * SubscriberSupport.lookupEarliestActivityDate and it is using some low-level 
 * SQL query in to the Transaction table.
 * 
 * @author angie.li
 *
 */
public class TestReprocessPaymentExceptionService extends ContextAwareTestCase 
{
    public TestReprocessPaymentExceptionService(String name)
    {
        super(name);
    }
    
    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestReprocessPaymentExceptionService.class);

        return suite;
    }
    
    //INHERIT
    public void setUp()
    {
        super.setUp();
        
        setupEnvironment();
    }
    
    // INHERIT
    public void tearDown()
    {
        //tear down here
        deleteAccountsSubscribers();
        
        super.tearDown();
    }
    
    /**
     * Test Processing a Payment Exception that failed due to PaymentFailureTypeEnum.SUBSCRIBER.
     * @throws Exception
     */
    public void testPaymentExceptionForSubscriber()
        throws Exception
    {
        PaymentException record = 
            setupPaymentExceptionRecord(PaymentFailureTypeEnum.SUBSCRIBER, TestSetupAccountHierarchy.ACCOUNT1_BAN, 
                TestSetupAccountHierarchy.SUB1_ID, TestSetupAccountHierarchy.SUB1_MSISDN, 
                TestSetupAccountHierarchy.SUB1_ID);
        
        ReprocessPaymentExceptionService.instance().service(getContext(), record);
        
        verifyPaymentTransaction(record);
    }
    
    /**
     * Test Processing a Payment Exception that failed due to PaymentFailureTypeEnum.MULTISUB
     * @throws Exception
     */
    public void testPaymentExceptionForMultiSubscribers()
    throws Exception
    {
        PaymentException record = 
            setupPaymentExceptionRecord(
                    PaymentFailureTypeEnum.MULTISUB, 
                    TestSetupAccountHierarchy.ACCOUNT2_BAN, 
                    TestSetupAccountHierarchy.SUB2_ID + "," + TestSetupAccountHierarchy.SUB4_ID , 
                    TestSetupAccountHierarchy.SUB2_MSISDN, 
                    TestSetupAccountHierarchy.SUB2_ID);

        ReprocessPaymentExceptionService.instance().service(getContext(), record);

        verifyPaymentTransaction(record);
    }
    
    /**
     * Test Processing a Payment Exception that failed due to PaymentFailureTypeEnum.ACCOUNT
     * @throws Exception
     */
    public void testPaymentExceptionForAccount()
        throws Exception
    {
        //TEST: Account with one Subscriber 
        PaymentException record = 
            setupPaymentExceptionRecord(
                    PaymentFailureTypeEnum.ACCOUNT, 
                    TestSetupAccountHierarchy.ACCOUNT1_BAN, 
                    "", 
                    TestSetupAccountHierarchy.SUB1_MSISDN, 
                    "");

        ReprocessPaymentExceptionService.instance().service(getContext(), record);

        //Only one subscriber in this Account
        record.setSelectedSubcriberID(TestSetupAccountHierarchy.SUB1_ID);
        verifyPaymentTransaction(record);
    }
    
    /**
     * Test Processing a Payment Exception that failed due to PaymentFailureTypeEnum.ACCOUNT
     * with Multiple Subscriber to which we split the original payment.
     * 
     * Angie Li: So many issues with this test!  
     * First, using a Transient home for Transactions was failing the test because creating
     * the InvoiceCalculation relies on looking up the Earliest Activity Date.  The support 
     * method that looks up the earliest activity date, does so using direct SQL queries.  
     * As expected, the SQL queries turn up nothing on Transient Homes.
     * Then, when we changed to an XDB home for Transactions, continually ran into trouble 
     * when inserting in to the test table.  The test pipeline attempts to insert the Transient 
     * fields for some reason.
     * Marking this test case as BROKEN until we figure out how to get around these problems.
     * 
     * @throws Throwable
     */
    public void BROKENtestPaymentExceptionForAccountWithMultipleSubscribers()
        throws Throwable
    {
        //TEST: Account with multiple Subscribers;  ACCOUNT2_BAN has 2 subscribers that can take payments.
        PaymentException record = 
            setupPaymentExceptionRecord(
                    PaymentFailureTypeEnum.ACCOUNT, 
                    TestSetupAccountHierarchy.ACCOUNT2_BAN, 
                    "", 
                    TestSetupAccountHierarchy.SUB2_MSISDN, 
                    "");

        try
        {
            ReprocessPaymentExceptionService.instance().service(getContext(), record);
        }
        catch (ServletException se)
        {
            if (se.getCause() != null)
            {
                // Remove the ServletException decorator.
                throw se.getCause();
            }
            else
            {
                throw se;
            }
        }

        record.setSelectedSubcriberID(TestSetupAccountHierarchy.SUB1_ID);
        record.setAmount(record.getAmount()/2);
        verifyPaymentTransaction(record);
    }

    /**
     * Return a Payment Exception record with the given information.
     * @param failureType
     * @param ban
     * @param subscriberIds
     * @param msisdn
     * @param selectedSub
     * @return
     */
    private PaymentException setupPaymentExceptionRecord(
            final PaymentFailureTypeEnum failureType,
            final String ban,
            final String subscriberIds,
            final String msisdn,
            final String selectedSub)
    {
        PaymentException record = new PaymentException();
        record.setType(failureType);
        record.setBan(ban);
        record.setSubscriberIds(subscriberIds);
        record.setMsisdn(msisdn);
        record.setAmount(DEFAULT_AMOUNT);
        record.setAgent(DEFAULT_AGENT);
        record.setAdjustmentType(DEFAULT_ADJUSTMENTTYPE);
        record.setTransDate(DEFAULT_TRANSDATE);
        record.setExtTransactionId(DEFAULT_EXTTRANSID);
        record.setTpsFileName(DEFAULT_FILENAME);
        record.setLocationCode(DEFAULT_LOCATION);
        record.setPaymentDetails(DEFAULT_PAYMENTDETAILS);
        record.setTransactionMethod(DEFAULT_TRANSMETHOD); //Default is Cash
        record.setLastAttemptAgent(DEFAULT_LASTAGENT);
        record.setSelectedSubcriberID(selectedSub);
        
        return record;
    }
    
    /**
     * Install the Transient Homes
     */
    private void setupEnvironment()
    {
        // This test triggers an action in the Invoice Server.  Install Account Hierarchy in the TransientHomes.
        TestSetupAccountHierarchy.setup(getContext(), false);
        /* Due to the nature of the SubscriberSupport.lookupEarliestActivityDate() method, the Transaction
         * pipeline will have to be installed as an XDB home.  */
        TestSetupTransactions.setup(getContext(), false);
        decorateTransactionHome();
        TestSetupAdjustmentTypes.setup(getContext());
        
        // If this test is running off-line then do additional setup.
        if (getContext().getBoolean(com.redknee.app.crm.TestPackage.APPLICATION_CONTEXT, false) == false)
        {
            User principal = (User) getContext().get(Principal.class);
            if (principal == null)
            {
                principal = new User();
                principal.setId("Unit_Test_User");
                getContext().put(Principal.class, principal);
            }
            TestSetupPaymentPlan.setup(getContext());
            TestSetupInvoiceHistory.setup(getContext());
            TestSetupCallDetails.setup(getContext(), false, true);
            /* Still fail running this test off-line because the Subscriber's 
             * earliest activity date is done by SubscriberSupport.lookupEarliestActivityDate
             * and it is using some low-level SQL query in to the Transaction table.
             */
        }
    }
    
    /**
     * For this unit test, the part of the Transaction pipeline that splits Account level payments
     * must be installed in to the test pipeline.
     */
    private void decorateTransactionHome()
    {
        Home transHome = (Home) getContext().get(TransactionHome.class);
        transHome = new TransactionRedirectionHome(transHome);
        getContext().put(TransactionHome.class, transHome);
    }
    
    /**
     * Delete test records.
     */
    private void deleteAccountsSubscribers()
    {
        TestSetupAccountHierarchy.completelyTearDown(getContext());
        TestSetupTransactions.deleteTransactions(getContext());
        TestSetupAdjustmentTypes.completelyTeardown(getContext());
    }
    
    /**
     * Given a Payment Exception, verifies that there exists a Transaction
     * record that corresponds to the Payment Exception.
     * @param record
     * @throws HomeException
     */
    private void verifyPaymentTransaction(final PaymentException record)
        throws HomeException
    {
        Home transHome = (Home) getContext().get(TransactionHome.class);
        
        And predicate = new And();
        predicate.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, record.getSelectedSubcriberID()));
        predicate.add(new EQ(TransactionXInfo.BAN, record.getBan()));
        if (record.getType().equals(PaymentFailureTypeEnum.ACCOUNT))
        {
            predicate.add(new EQ(TransactionXInfo.PAYEE, PayeeEnum.Account));
        }
        else
        {
            predicate.add(new EQ(TransactionXInfo.PAYEE, PayeeEnum.Subscriber));
        }
        predicate.add(new EQ(TransactionXInfo.MSISDN, record.getMsisdn()));
        predicate.add(new EQ(TransactionXInfo.AMOUNT, Long.valueOf(record.getAmount())));
        predicate.add(new EQ(TransactionXInfo.AGENT, record.getAgent()));
        predicate.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, Integer.valueOf(record.getAdjustmentType())));
        predicate.add(new EQ(TransactionXInfo.ACTION, AdjustmentTypeActionEnum.DEBIT)); 
        predicate.add(new EQ(TransactionXInfo.GLCODE, 
                        AdjustmentTypeSupportHelper.get(getContext()).getGLCodeForAdjustmentType(getContext(), 
                        record.getAdjustmentType(), 
                        TestSetupAccountHierarchy.SPID_ID)));
        predicate.add(new EQ(TransactionXInfo.TRANS_DATE, record.getTransDate()));
        predicate.add(new EQ(TransactionXInfo.EXT_TRANSACTION_ID, record.getExtTransactionId()));
        predicate.add(new EQ(TransactionXInfo.LOCATION_CODE, record.getLocationCode()));
        predicate.add(new EQ(TransactionXInfo.PAYMENT_DETAILS, record.getPaymentDetails()));
        predicate.add(new EQ(TransactionXInfo.TRANSACTION_METHOD, Long.valueOf(record.getTransactionMethod())));
        
        Collection col = transHome.where(getContext(), predicate).selectAll();
        assertEquals(1, col.size()); // Should be one Transaction 
        
    }
    
    private final String DEFAULT_AGENT = "unit_test";
    private final int DEFAULT_ADJUSTMENTTYPE = AdjustmentTypeEnum.StandardPayments_INDEX;
    private final Date DEFAULT_TRANSDATE = new Date();
    private final String DEFAULT_EXTTRANSID = "123456";
    private final long DEFAULT_AMOUNT = 10000;
    private final String DEFAULT_FILENAME = "TPS_UNIT_TEST.tps";
    private final String DEFAULT_LOCATION = "90210";
    private final String DEFAULT_PAYMENTDETAILS = "In Full";
    private final int DEFAULT_TRANSMETHOD = 1;
    private final String DEFAULT_LASTAGENT = "rkadm";
    
}
