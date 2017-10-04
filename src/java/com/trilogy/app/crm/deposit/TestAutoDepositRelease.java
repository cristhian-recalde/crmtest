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
import com.trilogy.framework.xhome.visitor.Visitor;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCategory;

/**
 * Unit test for {@link com.redknee.app.crm.deposit.AutoDepositRelease}.
 *
 * @author cindy.wong@redknee.com
 */
public class TestAutoDepositRelease extends DepositReleaseTestCase
{
    /**
     *
     */
    private static final int SLEEP_TIME = 5000;

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

        final TestSuite suite = new TestSuite(TestAutoDepositRelease.class);

        return suite;
    }

    /**
     * Creates a new test suite.
     *
     * @param name
     *            Name of test suite.
     */
    public TestAutoDepositRelease(final String name)
    {
        super(name);
    }

    /**
     * SPID for testing.
     */
    private CRMSpid spidDisabled_;

    /**
     * SPID for testing.
     */
    private CRMSpid spidDayOfMonth_;

    /**
     * SPID for testing.
     */
    private CRMSpid spidBillCycle_;

    /**
     * Criteria whose release schedule depends on day of month.
     */
    private AutoDepositReleaseCriteria criteriaDayOfMonth_;

    /**
     * Criteria whose release schedule depends on bill cycle day.
     */
    private AutoDepositReleaseCriteria criteriaBillCycle_;

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setUp()
    {
        super.setUp();
        setUpTestLicense();

        setUpCriteria();
        setUpSpid();
    }

    /**
     * Sets up credit category for the provided SPID.
     *
     * @param spid
     *            Service provider ID.
     * @return The credit category set up.
     */
    private CreditCategory setUpCreditCategory(final int spid)
    {
        final CreditCategory creditCategory = DepositReleaseTestSupport.createCreditCategory(nextId_, spid, false, -1);
        nextId_++;
        createBean(creditCategory);
        return creditCategory;
    }

    /**
     * Sets up bill cycles for the provided SPID.
     *
     * @param spid
     *            Service provider ID.
     * @return The bill cycles created.
     */
    private BillCycle[] setUpBillCycle(final int spid)
    {
        final int day1 = 1;
        final int day2 = 10;
        final BillCycle[] result = new BillCycle[2];
        result[0] = DepositReleaseTestSupport.createBillCycle(nextId_, spid, day1);
        nextId_++;
        createBean(result[0]);
        result[1] = DepositReleaseTestSupport.createBillCycle(nextId_, spid, day2);
        nextId_++;
        createBean(result[1]);
        return result;
    }

    /**
     * Sets up criteria for testing.
     */
    private void setUpCriteria()
    {
        final int dayOfMonth = 10;
        final int beforeBillCycle = 5;
        final long min = 1000L;
        final int duration = 60;
        final double percent = 100.0;

        criteriaDayOfMonth_ = DepositReleaseTestSupport.createCriteria(nextId_,
            AdjustmentTypeEnum.DepositRelease_INDEX, percent, min, dayOfMonth, true, duration);
        nextId_++;
        createBean(criteriaDayOfMonth_);

        criteriaBillCycle_ = DepositReleaseTestSupport.createCriteria(nextId_,
            AdjustmentTypeEnum.DepositRelease_INDEX, percent, min, beforeBillCycle, false, duration);
        nextId_++;
        createBean(criteriaBillCycle_);
    }

    /**
     * Sets up SPID for testing.
     */
    private void setUpSpid()
    {

        spidDisabled_ = DepositReleaseTestSupport.createSpid(nextId_, false, -1);
        nextId_++;
        createBean(spidDisabled_);

        spidDayOfMonth_ = DepositReleaseTestSupport.createSpid(nextId_, true, criteriaDayOfMonth_.getIdentifier());
        nextId_++;
        createBean(spidDayOfMonth_);

        spidBillCycle_ = DepositReleaseTestSupport.createSpid(nextId_, true, criteriaBillCycle_.getIdentifier());
        nextId_++;
        createBean(spidBillCycle_);
    }

    /**
     * Tests when SPID has auto deposit release disabled.
     *
     * @throws AgentException
     *             Thrown if there are problem executing auto deposit release.
     */
    public final void testDisabled() throws AgentException
    {
        final int numAccounts = (int) Math.round(Math.random() * 50);
        final BillCycle[] billCycles = setUpBillCycle(spidDisabled_.getId());
        final CreditCategory creditCategory = setUpCreditCategory(spidDisabled_.getId());
        setUpAccounts(spidDisabled_, numAccounts, billCycles[0].getBillCycleID(), creditCategory.getCode());
        final Calendar activeDate = Calendar.getInstance();
        final SubscriberReleaseVisitor subscriberVisitor = new DummySubscriberReleaseVisitor();
        final DepositReleaseTransactionCreator transactionCreator = new DummyDepositReleaseTransactionCreator();
        final AutoDepositRelease release = new AutoDepositRelease(spidDisabled_, activeDate.getTime(),
            subscriberVisitor, transactionCreator);
        release.execute(getContext());
        assertEquals("No accounts should be visited", release.getNumVisitedSubscribers(), 0);
    }

    /**
     * Test auto deposit release when it depends on day of month.
     *
     * @throws AgentException
     *             Thrown if there are problem with executing auto deposit release.
     */
    public final void testDayOfMonth() throws AgentException
    {
        final int accountsPerBillCycle = (int) Math.round(Math.random() * 50);
        final int numAccounts = accountsPerBillCycle * 2;
        final BillCycle[] billCycles = setUpBillCycle(spidDayOfMonth_.getId());
        final CreditCategory creditCategory = setUpCreditCategory(spidDayOfMonth_.getId());
        for (final BillCycle b : billCycles)
        {
            setUpAccounts(spidDayOfMonth_, accountsPerBillCycle, b.getBillCycleID(), creditCategory.getCode());
        }

        // case 1: date == day of month
        {
            final Calendar activeDate = Calendar.getInstance();
            activeDate.set(Calendar.DAY_OF_MONTH, criteriaDayOfMonth_.getReleaseSchedule());
            final SubscriberReleaseVisitor subscriberVisitor = new DummySubscriberReleaseVisitor();
            final DepositReleaseTransactionCreator transactionCreator = new DummyDepositReleaseTransactionCreator();
            final AutoDepositRelease release = new AutoDepositRelease(spidDayOfMonth_, activeDate.getTime(),
                subscriberVisitor, transactionCreator, false);
            release.execute(getContext());
            assertEquals("All subscribers should be visited", release.getNumVisitedSubscribers(), numAccounts);
        }

        // case 2: date != day of month
        {
            final int dayOfMonth = 5;
            final Calendar activeDate = Calendar.getInstance();
            activeDate.set(Calendar.DAY_OF_MONTH, criteriaDayOfMonth_.getReleaseSchedule());
            activeDate.add(Calendar.DAY_OF_MONTH, dayOfMonth);
            final SubscriberReleaseVisitor subscriberVisitor = new DummySubscriberReleaseVisitor();
            final DepositReleaseTransactionCreator transactionCreator = new DummyDepositReleaseTransactionCreator();
            final AutoDepositRelease release = new AutoDepositRelease(spidDayOfMonth_, activeDate.getTime(),
                subscriberVisitor, transactionCreator, false);
            release.execute(getContext());
            assertEquals("None of the accounts should be visited on the wrong day of month", release
                .getNumVisitedSubscribers(), 0);
        }
    }

    /**
     * Test auto deposit release when it is dependent on bill cycle day.
     *
     * @throws AgentException
     *             Thrown if there are problems executing the auto deposit release.
     */
    public final void testBillCycle() throws AgentException
    {
        final int accountsPerBillCycle = (int) Math.round(Math.random() * 50);
        final BillCycle[] billCycles = setUpBillCycle(spidBillCycle_.getId());
        final CreditCategory creditCategory = setUpCreditCategory(spidBillCycle_.getId());
        for (final BillCycle b : billCycles)
        {
            setUpAccounts(spidBillCycle_, accountsPerBillCycle, b.getBillCycleID(), creditCategory.getCode());
        }

        // case 1: date != release day for any bill cycle
        {
            final int dayOfMonth = 15;
            final Calendar activeDate = Calendar.getInstance();
            activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            final SubscriberReleaseVisitor subscriberVisitor = new DummySubscriberReleaseVisitor();
            final DepositReleaseTransactionCreator transactionCreator = new DummyDepositReleaseTransactionCreator();
            final AutoDepositRelease release = new AutoDepositRelease(spidBillCycle_, activeDate.getTime(),
                subscriberVisitor, transactionCreator, false);
            release.execute(getContext());
            assertEquals("Bill cycle does not match: no accounts should be visited",
                release.getNumVisitedSubscribers(), 0);
        }

        // case 2: date == 5th
        {
            final int dayOfMonth = 5;
            final Calendar activeDate = Calendar.getInstance();
            activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            final SubscriberReleaseVisitor subscriberVisitor = new DummySubscriberReleaseVisitor();
            final DepositReleaseTransactionCreator transactionCreator = new DummyDepositReleaseTransactionCreator();
            final AutoDepositRelease release = new AutoDepositRelease(spidBillCycle_, activeDate.getTime(),
                subscriberVisitor, transactionCreator, false);
            release.execute(getContext());
            assertEquals("Accounts in bill cycle 10th should be visited", release.getNumVisitedSubscribers(),
                accountsPerBillCycle);
        }

        // case 2: date == Jan 27th
        {
            final int dayOfMonth = 27;
            final Calendar activeDate = Calendar.getInstance();
            activeDate.set(Calendar.MONTH, Calendar.JANUARY);
            activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            final SubscriberReleaseVisitor subscriberVisitor = new DummySubscriberReleaseVisitor();
            final DepositReleaseTransactionCreator transactionCreator = new DummyDepositReleaseTransactionCreator();
            final AutoDepositRelease release = new AutoDepositRelease(spidBillCycle_, activeDate.getTime(),
                subscriberVisitor, transactionCreator, false);
            release.execute(getContext());
            assertEquals("Accounts in bill cycle 1st should be visited", release.getNumVisitedSubscribers(),
                accountsPerBillCycle);
        }
    }

    /**
     * Test auto deposit release when it is dependent on bill cycle day and day of month rollover is required.
     *
     * @throws AgentException
     *             Thrown if there are problems executing auto deposit release.
     */
    public final void testBillCycleRollOver() throws AgentException
    {
        final int accountsPerBillCycle = (int) Math.round(Math.random() * 50);
        final BillCycle[] billCycles = setUpBillCycle(spidBillCycle_.getId());
        final CreditCategory creditCategory = setUpCreditCategory(spidBillCycle_.getId());
        for (final BillCycle b : billCycles)
        {
            setUpAccounts(spidBillCycle_, accountsPerBillCycle, b.getBillCycleID(), creditCategory.getCode());
        }

        // case 1: date == Feb 24th, 2005 (not leap year)
        {
            final int dayOfMonth = 24;
            final int year = 2007;
            final Calendar activeDate = Calendar.getInstance();
            activeDate.set(Calendar.MONTH, Calendar.FEBRUARY);
            activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            activeDate.set(Calendar.YEAR, year);
            final SubscriberReleaseVisitor subscriberVisitor = new DummySubscriberReleaseVisitor();
            final DepositReleaseTransactionCreator transactionCreator = new DummyDepositReleaseTransactionCreator();
            final AutoDepositRelease release = new AutoDepositRelease(spidBillCycle_, activeDate.getTime(),
                subscriberVisitor, transactionCreator, false);
            release.execute(getContext());
            assertEquals("Accounts should be visited on Feb 24th, 2005", release.getNumVisitedSubscribers(),
                accountsPerBillCycle);
        }

        // case 2: date == Feb 24th, 2004 (leap year)
        {
            final int dayOfMonth = 24;
            final int year = 2004;
            final Calendar activeDate = Calendar.getInstance();
            activeDate.set(Calendar.MONTH, Calendar.FEBRUARY);
            activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            activeDate.set(Calendar.YEAR, year);
            final SubscriberReleaseVisitor subscriberVisitor = new DummySubscriberReleaseVisitor();
            final DepositReleaseTransactionCreator transactionCreator = new DummyDepositReleaseTransactionCreator();
            final AutoDepositRelease release = new AutoDepositRelease(spidBillCycle_, activeDate.getTime(),
                subscriberVisitor, transactionCreator, false);
            release.execute(getContext());
            assertEquals("No accounts should be visited on Feb 24, 2006", release.getNumVisitedSubscribers(), 0);
        }

        // case 3: date == April 26
        {
            final int dayOfMonth = 26;
            final Calendar activeDate = Calendar.getInstance();
            activeDate.set(Calendar.MONTH, Calendar.APRIL);
            activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            final SubscriberReleaseVisitor subscriberVisitor = new DummySubscriberReleaseVisitor();
            final DepositReleaseTransactionCreator transactionCreator = new DummyDepositReleaseTransactionCreator();
            final AutoDepositRelease release = new AutoDepositRelease(spidBillCycle_, activeDate.getTime(),
                subscriberVisitor, transactionCreator, false);
            release.execute(getContext());
            assertEquals("Accounts should be released on April 26", release.getNumVisitedSubscribers(),
                accountsPerBillCycle);
        }
    }

    /**
     * Test the serializability of parallel visitor.
     *
     * @throws AgentException
     *             Thrown by auto deposit release.
     * @throws IOException
     *             Thrown by serialization.
     */
    public final void testSerializable() throws AgentException, IOException
    {
        final int accountsPerBillCycle = (int) Math.round(Math.random() * 50);
        final BillCycle[] billCycles = setUpBillCycle(spidDayOfMonth_.getId());
        final CreditCategory creditCategory = setUpCreditCategory(spidDayOfMonth_.getId());
        for (final BillCycle b : billCycles)
        {
            setUpAccounts(spidDayOfMonth_, accountsPerBillCycle, b.getBillCycleID(), creditCategory.getCode());
        }

        // case 1: date == day of month
        {
            final Calendar activeDate = Calendar.getInstance();
            activeDate.set(Calendar.DAY_OF_MONTH, criteriaDayOfMonth_.getReleaseSchedule());
            final SubscriberReleaseVisitor subscriberVisitor = new DummySubscriberReleaseVisitor();
            final DepositReleaseTransactionCreator transactionCreator = new DummyDepositReleaseTransactionCreator();
            final AutoDepositRelease release = new AutoDepositRelease(spidDayOfMonth_, activeDate.getTime(),
                subscriberVisitor, transactionCreator, true);
            release.execute(getContext());

            try
            {
                Thread.sleep(SLEEP_TIME);
            }
            catch (final InterruptedException exception)
            {
                // do nothing
            }
            final Visitor visitor = release.getSubscriberVisitor();
            serialize(visitor);
        }
    }

    /**
     * ID counter for testing.
     */
    private int nextId_ = 1;
}
