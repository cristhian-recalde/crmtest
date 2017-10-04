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

package com.trilogy.app.crm.deposit;

import java.io.IOException;
import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * Unit test for {@link com.redknee.app.crm.deposit.DefaultSubscriberReleaseVisitor}.
 *
 * @author cindy.wong@redknee.com
 */
public class TestSubscriberReleaseVisitor extends DepositReleaseTestCase
{
    /**
     * Error message when deposit should be released but not.
     */
    public static final String DEPOSIT_RELEASE_FAIL_MESSAGE = "Deposit should be released";

    /**
     * Error message when deposit should not be released but is.
     */
    public static final String DEPOSIT_NOT_RELEASE_FAIL_MESSAGE = "Deposit should not be released";

    /**
     * Error message when the amount of deposit released does not match the expected value.
     */
    public static final String WRONG_DEPOSIT_RELEASE_AMOUNT_MESSAGE = "The amount of deposit released is incorrect";

    /**
     * Error message when the date of the release is incorrect.
     */
    public static final String WRONG_RELEASE_DATE_MESSAGE = "The release date is incorrect";

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by standard JUnit tools (i.e.,
     * those that do not provide a context).
     *
     * @return A new suite of Tests for execution
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by the Redknee Xtest code,
     * which provides the application's operating context.
     *
     * @param context
     *            The operating context
     * @return A new suite of Tests for execution
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestSubscriberReleaseVisitor.class);

        return suite;
    }

    /**
     * SPID for testing.
     */
    private CRMSpid spid_ = new CRMSpid();

    /**
     * Credit category for testing, using SPID auto deposit release criteria.
     */
    private CreditCategory creditCategorySpid_ = new CreditCategory();

    /**
     * Credit category for testing, using custom auto deposit release criteria.
     */
    private CreditCategory creditCategoryCustom_ = new CreditCategory();

    /**
     * Bill cycle for testing, running on 1st of month.
     */
    private BillCycle billCycle1_ = new BillCycle();

    /**
     * Bill cycle for testing, running on 15th of month.
     */
    private BillCycle billCycle15_ = new BillCycle();

    /**
     * Creates a new test suite.
     *
     * @param name
     *            Name of test suite.
     */
    public TestSubscriberReleaseVisitor(final String name)
    {
        super(name);
    }

    /**
     * Sets up the environment for testing.
     *
     * @see com.redknee.app.crm.deposit.DepositReleaseTestCase#setUp()
     */
    @Override
    public final void setUp()
    {
        final int billCycleDay1 = 1;
        final int billCycleDay2 = 15;

        super.setUp();
        // add default adjustment type
        super.setUpAdjustmentType();

        // add spid
        {
            spid_ = DepositReleaseTestSupport.createSpid(1, true, 1);
            super.createBean(spid_);
        }

        // add credit categories
        {
            creditCategorySpid_ = DepositReleaseTestSupport.createCreditCategory(1, spid_.getSpid(), false, -1);
            createBean(creditCategorySpid_);

            creditCategoryCustom_ = DepositReleaseTestSupport.createCreditCategory(2, spid_.getSpid(), true, 1);
            createBean(creditCategoryCustom_);
        }

        // add bill cycles
        {
            billCycle1_ = DepositReleaseTestSupport.createBillCycle(1, spid_.getSpid(), billCycleDay1);
            createBean(billCycle1_);

            billCycle15_ = DepositReleaseTestSupport.createBillCycle(2, spid_.getSpid(), billCycleDay2);
            createBean(billCycle15_);
        }
        setUpTestLicense();
    }

    /**
     * Test method for {@link com.redknee.app.crm.deposit.DefaultSubscriberReleaseVisitor#visit}.
     *
     * @throws AgentException
     *             Thrown by Visitor.visit().
     */
    public final void testOnBillCycle() throws AgentException
    {
        final Calendar activeDate = Calendar.getInstance();
        final int offset = 15;
        activeDate.set(Calendar.DAY_OF_MONTH, billCycle1_.getDayOfMonth() - offset);
        final long deposit = 1000;
        final double percent = 100.0;
        final long min = 10;
        final int duration = 60;

        // create criteria which releases 100%
        final AutoDepositReleaseCriteria criteria = DepositReleaseTestSupport.createCriteria(1,
            AdjustmentTypeEnum.DepositRelease_INDEX, percent, min, offset, false, duration);

        // create account
        final Account account = DepositReleaseTestSupport.createAccount(spid_.getSpid(), creditCategorySpid_.getCode(),
            billCycle1_.getBillCycleID());
        createBean(account);

        // create visitor for criteria
        final DummyDepositReleaseTransactionCreator creator = new DummyDepositReleaseTransactionCreator();
        final DefaultSubscriberReleaseVisitor visitor = new DefaultSubscriberReleaseVisitor(getContext(), criteria,
            spid_.getSpid(), creator, activeDate);

        // test eligible subscriber 1: exact time
        {
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, activeDate
                .getTime());
            visitor.visit(getContext(), subscriber);
            assertTrue(DEPOSIT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(subscriber.getId()));
            assertEquals(WRONG_DEPOSIT_RELEASE_AMOUNT_MESSAGE, creator.getReleasedAmounts().get(subscriber.getId())
                .longValue(), deposit);
            assertEquals(WRONG_RELEASE_DATE_MESSAGE, creator.getBillingDates().get(subscriber.getId()), subscriber
                .getNextDepositReleaseDate());
        }

        // test eligible subscriber 2: earlier
        {
            final Calendar releaseDate = (Calendar) activeDate.clone();
            releaseDate.add(Calendar.DAY_OF_MONTH, -1);
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, releaseDate
                .getTime());
            visitor.visit(getContext(), subscriber);
            assertTrue(DEPOSIT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(subscriber.getId()));
            assertEquals(WRONG_DEPOSIT_RELEASE_AMOUNT_MESSAGE, creator.getReleasedAmounts().get(subscriber.getId())
                .longValue(), deposit);
            assertEquals(WRONG_RELEASE_DATE_MESSAGE, creator.getBillingDates().get(subscriber.getId()), subscriber
                .getNextDepositReleaseDate());
        }

        // test ineligible subscriber 3: later
        {
            final Calendar releaseDate = (Calendar) activeDate.clone();
            releaseDate.add(Calendar.DAY_OF_MONTH, 1);
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, releaseDate
                .getTime());
            try
            {
                visitor.visit(getContext(), subscriber);
                assertFalse(DEPOSIT_NOT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(
                    subscriber.getId()));
            }
            catch (final AgentException exception)
            {
                assert true;
            }
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.deposit.DefaultSubscriberReleaseVisitor#visit}.
     */
    public final void testNotOnBillCycle()
    {
        final Calendar activeDate = Calendar.getInstance();
        final int offset = 1;
        activeDate.set(Calendar.DAY_OF_MONTH, billCycle1_.getDayOfMonth());
        final long deposit = 1000;
        final double percent = 100.0;
        final long min = 10;
        final int duration = 50;

        // create criteria which releases 100%
        final AutoDepositReleaseCriteria criteria = DepositReleaseTestSupport.createCriteria(1L,
            AdjustmentTypeEnum.DepositRelease_INDEX, percent, min, offset, false, duration);

        // create account
        final Account account = DepositReleaseTestSupport.createAccount(spid_.getSpid(), creditCategorySpid_.getCode(),
            billCycle1_.getBillCycleID());
        createBean(account);

        // create visitor for criteria
        final DummyDepositReleaseTransactionCreator creator = new DummyDepositReleaseTransactionCreator();
        final DefaultSubscriberReleaseVisitor visitor = new DefaultSubscriberReleaseVisitor(getContext(), criteria,
            spid_.getSpid(), creator, activeDate);

        // test eligible subscriber 1: exact time
        {
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, activeDate
                .getTime());
            try
            {
                visitor.visit(getContext(), subscriber);
                assertFalse(DEPOSIT_NOT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(
                    subscriber.getId()));
            }
            catch (final AgentException exception)
            {
                assert true;
            }
        }

        // test eligible subscriber 2: earlier
        {
            final Calendar releaseDate = (Calendar) activeDate.clone();
            releaseDate.add(Calendar.DAY_OF_MONTH, -1);
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, releaseDate
                .getTime());
            try
            {
                visitor.visit(getContext(), subscriber);
                assertFalse(DEPOSIT_NOT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(
                    subscriber.getId()));
            }
            catch (final AgentException exception)
            {
                assert true;
            }
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.deposit.DefaultSubscriberReleaseVisitor#visit}.
     *
     * @throws AgentException
     *             Thrown by Visitor.visit().
     */
    public final void testDayOfMonth() throws AgentException
    {
        final Calendar activeDate = Calendar.getInstance();
        final long deposit = 1000;
        final double percent = 100.0;
        final long min = 10;
        final int duration = 60;
        final Calendar calendar = (Calendar) activeDate.clone();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        // create criteria which releases 100%
        final AutoDepositReleaseCriteria criteria = DepositReleaseTestSupport.createCriteria(1L,
            AdjustmentTypeEnum.DepositRelease_INDEX, percent, min, calendar.get(Calendar.DAY_OF_MONTH), true,
            duration);

        // create account
        final Account account = DepositReleaseTestSupport.createAccount(spid_.getSpid(), creditCategorySpid_.getCode(),
            billCycle1_.getBillCycleID());
        createBean(account);

        // create visitor for criteria
        final DummyDepositReleaseTransactionCreator creator = new DummyDepositReleaseTransactionCreator();
        final DefaultSubscriberReleaseVisitor visitor = new DefaultSubscriberReleaseVisitor(getContext(), criteria,
            spid_.getSpid(), creator, activeDate);

        // test eligible subscriber 1: exact time
        {
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, activeDate
                .getTime());
            visitor.visit(getContext(), subscriber);
            assertTrue(DEPOSIT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(subscriber.getId()));
            assertEquals(WRONG_DEPOSIT_RELEASE_AMOUNT_MESSAGE, creator.getReleasedAmounts().get(subscriber.getId())
                .longValue(), deposit);
            assertEquals(WRONG_RELEASE_DATE_MESSAGE, creator.getBillingDates().get(subscriber.getId()), subscriber
                .getNextDepositReleaseDate());
        }

        // test eligible subscriber 2: earlier
        {
            final Calendar releaseDate = (Calendar) activeDate.clone();
            releaseDate.add(Calendar.DAY_OF_MONTH, -1);
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, releaseDate
                .getTime());
            visitor.visit(getContext(), subscriber);
            assertTrue(DEPOSIT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(subscriber.getId()));
            assertEquals(WRONG_DEPOSIT_RELEASE_AMOUNT_MESSAGE, creator.getReleasedAmounts().get(subscriber.getId())
                .longValue(), deposit);
            assertEquals(WRONG_RELEASE_DATE_MESSAGE, creator.getBillingDates().get(subscriber.getId()), subscriber
                .getNextDepositReleaseDate());
        }

        // test ineligible subscriber 3: later
        {
            final Calendar releaseDate = (Calendar) activeDate.clone();
            releaseDate.add(Calendar.DAY_OF_MONTH, 1);
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, releaseDate
                .getTime());
            try
            {
                visitor.visit(getContext(), subscriber);
                assertFalse(DEPOSIT_NOT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(
                    subscriber.getId()));
            }
            catch (final AgentException exception)
            {
                assert true;
            }
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.deposit.DefaultSubscriberReleaseVisitor#visit}.
     *
     * @throws AgentException
     *             Thrown by Visitor.visit.
     */
    public final void testPartialRelease() throws AgentException
    {
        final Calendar activeDate = Calendar.getInstance();
        final long deposit = 1000;
        final double percent = 50.0;
        final long min = 10;
        final long remaining = (long) (deposit * percent / PercentageWithMinimumReleaseCalculation.PERCENTAGE_BASE);
        final int duration = 60;
        final Calendar calendar = (Calendar) activeDate.clone();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        // create criteria which releases 50%
        final AutoDepositReleaseCriteria criteria = DepositReleaseTestSupport.createCriteria(1L,
            AdjustmentTypeEnum.DepositRelease_INDEX, percent, min, calendar.get(Calendar.DAY_OF_MONTH), true,
            duration);

        // create account
        final Account account = DepositReleaseTestSupport.createAccount(spid_.getSpid(), creditCategorySpid_.getCode(),
            billCycle1_.getBillCycleID());
        createBean(account);

        // create visitor for criteria
        final DummyDepositReleaseTransactionCreator creator = new DummyDepositReleaseTransactionCreator();
        final DefaultSubscriberReleaseVisitor visitor = new DefaultSubscriberReleaseVisitor(getContext(), criteria,
            spid_.getSpid(), creator, activeDate);

        // test eligible subscriber 1: exact time
        {
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, activeDate
                .getTime());
            visitor.visit(getContext(), subscriber);
            assertTrue(DEPOSIT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(subscriber.getId()));
            assertEquals(WRONG_DEPOSIT_RELEASE_AMOUNT_MESSAGE, creator.getReleasedAmounts().get(subscriber.getId())
                .longValue(), remaining);
            assertEquals(WRONG_RELEASE_DATE_MESSAGE, creator.getBillingDates().get(subscriber.getId()), subscriber
                .getNextDepositReleaseDate());
        }

        // test eligible subscriber 2: earlier
        {
            final Calendar releaseDate = (Calendar) activeDate.clone();
            releaseDate.add(Calendar.DAY_OF_MONTH, -1);
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, releaseDate
                .getTime());
            visitor.visit(getContext(), subscriber);
            assertTrue(DEPOSIT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(subscriber.getId()));
            assertEquals(WRONG_DEPOSIT_RELEASE_AMOUNT_MESSAGE, creator.getReleasedAmounts().get(subscriber.getId())
                .longValue(), remaining);
            assertEquals(WRONG_RELEASE_DATE_MESSAGE, creator.getBillingDates().get(subscriber.getId()), subscriber
                .getNextDepositReleaseDate());
        }

        // test ineligible subscriber 3: later
        {
            final Calendar releaseDate = (Calendar) activeDate.clone();
            releaseDate.add(Calendar.DAY_OF_MONTH, 1);
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, releaseDate
                .getTime());
            try
            {
                visitor.visit(getContext(), subscriber);
                assertFalse(DEPOSIT_NOT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(
                    subscriber.getId()));
            }
            catch (final AgentException exception)
            {
                assert true;
            }
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.deposit.DefaultSubscriberReleaseVisitor#visit}.
     *
     * @throws AgentException
     *             Thrown by Visitor.visit().
     */
    public final void testCustomCriteria() throws AgentException
    {
        final Calendar activeDate = Calendar.getInstance();
        final long deposit = 1000;
        final double percent1 = 100.0;
        final double percent2 = 50.0;
        final long min = 10;
        final long remaining = (long) (deposit * percent2 / PercentageWithMinimumReleaseCalculation.PERCENTAGE_BASE);
        final int duration = 60;

        // create criteria which releases 100%
        final AutoDepositReleaseCriteria criteria1 = DepositReleaseTestSupport.createCriteria(1L,
            AdjustmentTypeEnum.DepositRelease_INDEX, percent1, min, activeDate.get(Calendar.DAY_OF_MONTH), true,
            duration);

        final AutoDepositReleaseCriteria criteria2 = DepositReleaseTestSupport.createCriteria(2L,
            AdjustmentTypeEnum.DepositRelease_INDEX, percent2, min, activeDate.get(Calendar.DAY_OF_MONTH), true,
            duration);

        createBean(criteria1);
        createBean(criteria2);

        // update credit category
        creditCategoryCustom_.setAutoDepositReleaseCriteria(criteria2.getIdentifier());
        storeBean(creditCategoryCustom_);

        // create accounts
        final Account account1 = DepositReleaseTestSupport.createAccount(spid_.getSpid(),
            creditCategorySpid_.getCode(), billCycle1_.getBillCycleID());
        createBean(account1);

        // create accounts
        final Account account2 = DepositReleaseTestSupport.createAccount(spid_.getSpid(), creditCategoryCustom_
            .getCode(), billCycle1_.getBillCycleID());
        createBean(account2);

        // create visitor for criteria
        final DummyDepositReleaseTransactionCreator creator = new DummyDepositReleaseTransactionCreator();
        final DefaultSubscriberReleaseVisitor visitor = new DefaultSubscriberReleaseVisitor(getContext(), criteria1,
            spid_.getSpid(), creator, activeDate);

        // test eligible subscriber 1: account 1
        {
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account1, deposit, activeDate
                .getTime());
            visitor.visit(getContext(), subscriber);
            assertTrue(DEPOSIT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(subscriber.getId()));
            assertEquals(WRONG_DEPOSIT_RELEASE_AMOUNT_MESSAGE, creator.getReleasedAmounts().get(subscriber.getId())
                .longValue(), deposit);
            assertEquals(WRONG_RELEASE_DATE_MESSAGE, creator.getBillingDates().get(subscriber.getId()), subscriber
                .getNextDepositReleaseDate());
        }

        // test eligible subscriber 2: account 2
        {
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account2, deposit, activeDate
                .getTime());
            visitor.visit(getContext(), subscriber);
            assertTrue(DEPOSIT_RELEASE_FAIL_MESSAGE, creator.getReleasedAmounts().containsKey(subscriber.getId()));
            assertEquals(WRONG_DEPOSIT_RELEASE_AMOUNT_MESSAGE, creator.getReleasedAmounts().get(subscriber.getId())
                .longValue(), remaining);
            assertEquals(WRONG_RELEASE_DATE_MESSAGE, creator.getBillingDates().get(subscriber.getId()), subscriber
                .getNextDepositReleaseDate());
        }
    }

    /**
     * Makes sure the visitor is serializable.
     *
     * @throws IOException
     *             Thrown if there are IO errors.
     */
    public final void testSerializable() throws IOException
    {
        final AutoDepositReleaseCriteria criteria = DepositReleaseTestSupport.createCriteria(1L,
            AdjustmentTypeEnum.DepositRelease_INDEX, 100.0, 10L, 1, false, 10);

        // create visitor for criteria
        final DefaultDepositReleaseTransactionCreator creator = new DefaultDepositReleaseTransactionCreator();
        final DefaultSubscriberReleaseVisitor visitor = new DefaultSubscriberReleaseVisitor(getContext(), criteria,
            spid_.getSpid(), creator, Calendar.getInstance());
        serialize(visitor);
    }
}
