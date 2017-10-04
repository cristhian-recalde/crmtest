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
package com.trilogy.app.crm.home;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.AutoKeySet;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XStatement;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.Adjustment;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeTransientHome;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SystemAdjustTypeMappingHome;
import com.trilogy.app.crm.bean.SystemAdjustTypeMappingTransientHome;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.TransactionMethodHome;
import com.trilogy.app.crm.bean.TransactionMethodTransientHome;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for AdjustmentValidator. Due to changes in Adjustment Type
 * storage (now in DB), this is to be run with an Application context.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestAdjustmentValidator extends ContextAwareTestCase
{

    /**
     * Constructs a test case with the given name.
     *
     * @param name
     *            The name of the test.
     */
    public TestAdjustmentValidator(final String name)
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

        final TestSuite suite = new TestSuite(TestAdjustmentValidator.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
        super.setUp();

        // Set up a MSISDN home for testing.
        this.msisdnHome_ = new MsisdnTransientHome(getContext());
        final Msisdn msisdn = new Msisdn();
        msisdn.setMsisdn("123");
        // FIXME CANDY
        msisdn.setBAN("123");
        try
        {
            this.msisdnHome_.create(getContext(), msisdn);
        }
        catch (final HomeException e)
        {
            fail("Failed to create a MSISDN for testing");
        }
        getContext().put(MsisdnHome.class, this.msisdnHome_);

        // Set up a Subscriber home for testing.
        this.subscriberHome_ = new SubscriberTransientHome(getContext());
        final Subscriber subscriber = new Subscriber();
        subscriber.setId("123-1");
        subscriber.setBAN("123");
        subscriber.setMSISDN("123");
        try
        {
            this.subscriberHome_.create(getContext(), subscriber);
        }
        catch (final HomeException e)
        {
            fail("Failed to create a Subscriber for testing");
        }
        getContext().put(SubscriberHome.class, this.subscriberHome_);

        // Set up an Account home for testing.
        this.accountHome_ = new AccountTransientHome(getContext());
        final Account account = new Account();
        account.setBAN("123");
        try
        {
            this.accountHome_.create(getContext(), account);
        }
        catch (final HomeException e)
        {
            fail("Failed to create an Account for testing");
        }
        getContext().put(AccountHome.class, this.accountHome_);

        getContext().put(SystemAdjustTypeMappingHome.class, new SystemAdjustTypeMappingTransientHome(getContext()));
        
        
        // Set up an AdjustmentType home for testing.
        this.adjustmentTypeHome_ = new AdjustmentTypeTransientHome(getContext());
        final AdjustmentType adjustmentType = new AdjustmentType();
        adjustmentType.setCode(AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentTypeCodeByAdjustmentTypeEnum(getContext(),
            AdjustmentTypeEnum.StandardPayments));
        try
        {
            this.adjustmentTypeHome_.create(getContext(), adjustmentType);
        }
        catch (final HomeException e)
        {
            fail("Failed to create the StandardPayments AdjustmentType for testing");
        }
        getContext().put(AdjustmentTypeHome.class, this.adjustmentTypeHome_);

        // Set up a TransactionMethod home for testing.
        this.transactionMethodHome_ = new TransactionMethodTransientHome(getContext());
        final TransactionMethod transactionMethod = new TransactionMethod();
        transactionMethod.setIdentifier(123);
        try
        {
            this.transactionMethodHome_.create(getContext(), transactionMethod);
        }
        catch (final HomeException e)
        {
            fail("Failed to create a TransactionMethod for testing");
        }
        getContext().put(TransactionMethodHome.class, this.transactionMethodHome_);

        // Set up a VALID Adjustment (with all optional fields filled) for testing.
        this.adjustment_ = new Adjustment();
        this.adjustment_.setAcctNum("123");
        this.adjustment_.setMSISDN("123");
        this.adjustment_.setAdjustmentType(AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentTypeCodeByAdjustmentTypeEnum(
            getContext(), AdjustmentTypeEnum.StandardPayments));
        this.adjustment_.setAmount(1);
        this.adjustment_.setPaymentAgency("123");
        this.adjustment_.setLocationCode("123");
        this.adjustment_.setExtTransactionId("123");
        this.adjustment_.setPaymentDetails("Something");
		SimpleDateFormat format =
		    new SimpleDateFormat(
		        TransactionToAdjustmentAdapter.DATE_FORMAT_STRING);

		this.adjustment_.setTransDate(format.format(Calendar.getInstance()
		    .getTime()));
        this.adjustment_.setCSRInput("Something");
        this.adjustment_.setTransactionMethod(123);
        this.adjustment_.setCreditCardNumber("123456789");
        this.adjustment_.setExpDate("1208");

        // Set up a Test AdjustmentValidator.
        this.adjustmentValidator_ = new AdjustmentValidator(getContext());

        getContext().put(XDB.class, new NullXDB());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        // Tear down the Test AdjustmentValidator.
        this.adjustmentValidator_ = null;

        // Tear down the Test Adjustment.
        this.adjustment_ = null;

        // Tear down the Test TransactionMethod home.
        this.transactionMethodHome_ = null;

        // Tear down the Test AdjustmentType home.
        this.adjustmentTypeHome_ = null;

        // Tear down the Test Account home.
        this.accountHome_ = null;

        // Tear down the Test Subscriber home.
        this.subscriberHome_ = null;

        // Tear down the Test MSISDN home.
        this.msisdnHome_ = null;

        super.tearDown();
    }

    /**
     * Null XDB class.
     *
     * @author victor.stratan@redknee.com
     */
    class NullXDB implements XDB
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public void doConnection(final Context ctx, final Visitor visitor)
        {
            // empty
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void test(final Context ctx)
        {
            // empty
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public int execute(final Context ctx, final String stmt)
        {
            return 0;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public int execute(final Context ctx, final XStatement stmt)
        {
            return 0;
        }



        /**
         * {@inheritDoc}
         */

        @Override
        public Visitor find(final Context ctx, final Visitor visitor, final String stmt)
        {
            return null;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Visitor find(final Context ctx, final Visitor visitor, final XStatement stmt)
        {
            return null;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Visitor forEach(final Context ctx, final Visitor visitor, final String stmt)
        {
            return null;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Visitor forEach(final Context ctx, final Visitor visitor, final XStatement stmt)
        {
            return null;
        }


        @Override
        public int executeStoredProcedure(Context ctx, Visitor visitor, XStatement stmt) throws HomeException
        {

            return 0;
        }


        @Override
        public AutoKeySet executeInsert(Context ctx, XStatement stmt) throws HomeException
        {
            return null;
        }
    }


    /**
     * Tests that the validate() method works according to the intent. Test case for
     * invalid MSISDN provided.
     */
    public void testValidate_invalidMSISDN()
    {
        try
        {
            this.adjustment_.setMSISDN("456");
            this.adjustmentValidator_.validate(getContext(), this.adjustment_);

            final String formattedMsg = MessageFormat.format(
                "IllegalStateException should have thrown because MSISDN \"{0}\" "
                    + "is not for a subscriber in the account \"{1}\"",
                    this.adjustment_.getMSISDN(), this.adjustment_.getAcctNum());
            fail(formattedMsg);
        }
        catch (final IllegalStateException e)
        {
            // Do nothing
        }
    }


    /**
     * Tests that the validate() method works according to the intent. Test case for only
     * invalid account number provided.
     */
    public void testValidate_invalidAccount()
    {
        try
        {
            this.adjustment_.setAcctNum("456");
            this.adjustment_.setMSISDN(Adjustment.DEFAULT_MSISDN);
            this.adjustmentValidator_.validate(getContext(), this.adjustment_);

            final String formattedMsg = MessageFormat.format(
                "IllegalStateException should have thrown because account \"{0}\" does not exist",
                this.adjustment_.getAcctNum());
            fail(formattedMsg);
        }
        catch (final IllegalStateException e)
        {
            // Do nothing
        }
    }


    /**
     * Tests that the validate() method works according to the intent. Test case for only
     * invalid MSISDN provided.
     */
    public void testValidate_invalidMSISDNOnly()
    {
        try
        {
            this.adjustment_.setAcctNum(Adjustment.DEFAULT_BAN);
            this.adjustment_.setMSISDN("456");
            this.adjustmentValidator_.validate(getContext(), this.adjustment_);

            final String formattedMsg = MessageFormat.format(
                "IllegalStateException should have thrown because MSISDN \"{0}\" does not exist",
                this.adjustment_.getMSISDN());
            fail(formattedMsg);
        }
        catch (final IllegalStateException e)
        {
            // Do nothing
        }
    }


    /**
     * Tests that the validate() method works according to the intent. Test case for
     * neither MSISDN nor account number is provided.
     */
    public void testValidate_noAccountNoMSISDN()
    {
        try
        {
            this.adjustment_.setAcctNum(Adjustment.DEFAULT_BAN);
            this.adjustment_.setMSISDN(Adjustment.DEFAULT_MSISDN);
            this.adjustmentValidator_.validate(getContext(), this.adjustment_);

            fail("IllegalStateException should have thrown because neither MSISDN " + "nor account number is provided");
        }
        catch (final IllegalStateException e)
        {
            // Do nothing
        }
    }


    /**
     * Tests that the validate() method works according to the intent. Test case for
     * Recurring Charges adjustment type.
     */
    public void testValidate_recurringRechargeAdjustment()
    {
        try
        {
            final Home adjustmentTypeHome1 = (Home) getContext().get(AdjustmentTypeHome.class);
            final AdjustmentType adjustmentType = new AdjustmentType();
            adjustmentType.setCode(AdjustmentTypeEnum.RecurringCharges_INDEX);
            try
            {
                adjustmentTypeHome1.create(getContext(), adjustmentType);
            }
            catch (final HomeException e)
            {
                fail("Failed to create the RecurringCharges AdjustmentType for testing");
            }

            this.adjustment_.setAdjustmentType(AdjustmentTypeEnum.RecurringCharges_INDEX);
            this.adjustmentValidator_.validate(getContext(), this.adjustment_);

            fail("IllegalStateException should have thrown because adjustment type "
                + "belongs to Recurring Charges is not supported");
        }
        catch (final IllegalStateException e)
        {
            // Do nothing
        }
    }


    /**
     * Tests that the validate() method works according to the intent. Test case for no
     * Payment Agency is provided.
     */
    public void testValidate_noPaymentAgency()
    {
        {
            this.adjustment_.setPaymentAgency(Adjustment.DEFAULT_PAYMENTAGENCY);
            this.adjustmentValidator_.validate(getContext(), this.adjustment_);

            assertEquals("\"default\" should be used if no Payment Agency is provided", "default", this.adjustment_
                .getPaymentAgency());
        }
    }


    /**
     * Tests that the validate() method works according to the intent. Test case for no
     * Location Code is provided.
     */
    public void testValidate_noLocationCode()
    {
        try
        {
            this.adjustment_.setLocationCode(Adjustment.DEFAULT_LOCATIONCODE);
            this.adjustmentValidator_.validate(getContext(), this.adjustment_);

            fail("IllegalStateException should have thrown because Location Code " + "is mandatory");
        }
        catch (final IllegalStateException e)
        {
            // Do nothing
        }
    }

    /**
     * Tests that the validate() method works according to the intent. Test case for
     * Transaction Method not defined.
     */
    public void testValidate_undefinedTransactionMethod()
    {
        try
        {
            this.adjustment_.setTransactionMethod(456);
            this.adjustmentValidator_.validate(getContext(), this.adjustment_);

            final String formattedMsg = MessageFormat.format(
                "IllegalStateException should have thrown because Transaction Method \"{0}\" is not defined",
                String.valueOf(this.adjustment_.getTransactionMethod()));
            fail(formattedMsg);
        }
        catch (final IllegalStateException e)
        {
            // Do nothing
        }
    }

    /**
     * MSISDN home.
     */
    private Home msisdnHome_;

    /**
     * Subscriber home.
     */
    private Home subscriberHome_;

    /**
     * Account home.
     */
    private Home accountHome_;

    /**
     * Adjustment type home.
     */
    private Home adjustmentTypeHome_;

    /**
     * Transaction method home.
     */
    private Home transactionMethodHome_;

    /**
     * Adjustment.
     */
    private Adjustment adjustment_;

    /**
     * Adjustment validator.
     */
    private AdjustmentValidator adjustmentValidator_;
}
