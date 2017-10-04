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

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteriaHome;
import com.trilogy.app.crm.bean.ReleaseScheduleConfigurationEnum;

/**
 * A suite of tests for Auto Deposit Release Criteria creations.
 *
 * @author cindy.wong@redknee.com
 */
public class TestAutoDepositReleaseCriteria extends DepositReleaseTestCase
{
    /**
     * Next identifier to use for criteria creation.
     */
    private long criteriaId_ = 1001L;

    /**
     * Creates a new <code>TestAutoDepositReleaseCriteria</code>.
     *
     * @param name
     *            Name of the test suite.
     */
    public TestAutoDepositReleaseCriteria(final String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by standard JUnit tools (i.e.,
     * those that do not provide a context).
     *
     * @return A new suite of Tests for execution.
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
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestAutoDepositReleaseCriteria.class);

        return suite;
    }

    /**
     * Sets up the environment required for this test suite.
     *
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#setUp()
     */
    @Override
    public final void setUp()
    {
        super.setUp();
        // add decorator
        {
            getContext().put(
                AutoDepositReleaseCriteriaHome.class,
                new ValidatingHome(new AutoDepositReleaseCriteriaValidator(), (Home) getContext().get(
                    AutoDepositReleaseCriteriaHome.class)));
        }
        super.setUpTestLicense();
    }

    /**
     * Tests creation of valid criteria.
     *
     * @throws HomeException
     *             Thrown if there are home-related errors.
     */
    public final void testValidCriteria() throws HomeException
    {
        int count = 0;
        // base criteria: calculated on 15th, hold for 45 days, release 50% each
        // time, minimum balance $10
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 45, 1000, 50.0);
        count++;

        // 1st of month
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 1, 45, 1000, 50.0);
        count++;

        // 28th of month
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 28, 45, 1000, 50.0);
        count++;

        // hold for 1 day
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 2, 1000, 50.0);
        count++;

        // hold for 28 days
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 28, 1000, 50.0);
        count++;

        // hold for 27 days
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 27, 1000, 50.0);
        count++;

        // hold for 29 days
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 29, 1000, 50.0);
        count++;

        // hold for 15 days
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 15, 1000, 50.0);
        count++;

        // hold for 60 days
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 60, 1000, 50.0);
        count++;

        // hold for 90 days
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 90, 1000, 50.0);
        count++;

        // release 100%
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 45, 1000, 100.0);
        count++;

        // release 10%
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 45, 1000, 10.0);
        count++;

        // release 0%
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 45, 1000, 0.0);
        count++;

        // hold for 90 days, release 100%
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 90, 1000, 100.0);
        count++;

        // minimum balance $0
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 45, 0, 50.0);
        count++;

        // same day as bill cycle
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAYS_BEFORE_BILL_CYCLE, 0, 45, 1000, 50.0);
        count++;

        // 1 day before bill cycle
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAYS_BEFORE_BILL_CYCLE, 1, 45, 1000, 50.0);
        count++;

        // 15 days before bill cycle
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAYS_BEFORE_BILL_CYCLE, 15, 45, 1000, 50.0);
        count++;

        // 28 days before bill cycle
        assertValidCriteria(ReleaseScheduleConfigurationEnum.DAYS_BEFORE_BILL_CYCLE, 28, 45, 1000, 50.0);
        count++;

        // make sure the sizes match
        int newSize = -1;
        newSize = ((Home) getContext().get(AutoDepositReleaseCriteriaHome.class)).selectAll(getContext()).size();
        assertEquals("Size of criteria home does not match expected value", count, newSize);
    }

    /**
     * Test if invalid criteria are added to home incorrectly.
     */
    public final void testInvalidCriteria()
    {
        // 0th of month
        assertInvalidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 0, 45, 1000, 50.0,
            "Day of month cannot be 0");

        // 29th of month
        assertInvalidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 29, 45, 1000, 50.0,
            "Day of month cannot be 29th");

        // 29 days before bill cycle
        assertInvalidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 29, 45, 1000, 50.0,
            "Cannot be 29 days before bill cycle");

        // negative release schedule
        assertInvalidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, -1, 45, 1000, 50.0,
            "Release schedule cannot be negative");

        // same-day deposit release
        assertInvalidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 0, 1000, 50.0,
            "Service duration cannot be 0");

        // negative duration
        assertInvalidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, -1, 1000, 50.0,
            "Service duration cannot be negative");

        // negative minimum balance
        assertInvalidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 45, -1, 50.0,
            "Minimum balance cannot be negative");

        // negative percentage
        assertInvalidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 45, 1000, -1,
            "Percentage cannot be below 0");

        // percentage over 100
        assertInvalidCriteria(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH, 15, 45, 1000, 101,
            "Percentage cannot be over 100");
    }

    /**
     * Asserts the criteria specified is invalid.
     *
     * @param releaseScheduleConfiguration
     *            Whether the schdule is day of month or days before bill cycle.
     * @param schedule
     *            The release schedule.
     * @param duration
     *            How many days deposit is hold for after the most recent deposit change.
     * @param min
     *            Minimum deposit balance.
     * @param percent
     *            Percentage of the remaining deposit to be released.
     * @param message
     *            Custom fail message.
     */
    private void assertInvalidCriteria(final ReleaseScheduleConfigurationEnum releaseScheduleConfiguration,
        final int schedule, final int duration, final int min, final double percent, final String message)
    {
        try
        {
            addCriteria(releaseScheduleConfiguration, schedule, duration, min, percent);
            fail(message);
        }
        catch (final HomeException exception)
        {
            // do nothing
        }
        catch (final IllegalArgumentException exception)
        {
            // do nothing
        }
    }

    /**
     * Asserts the criteria specified is valid.
     *
     * @param releaseScheduleConfiguration
     *            Whether the schdule is day of month or days before bill cycle.
     * @param schedule
     *            The release schedule.
     * @param duration
     *            How many days deposit is hold for after the most recent deposit change.
     * @param min
     *            Minimum deposit balance.
     * @param percent
     *            Percentage of the remaining deposit to be released.
     */
    private void assertValidCriteria(final ReleaseScheduleConfigurationEnum releaseScheduleConfiguration,
        final int schedule, final int duration, final int min, final double percent)
    {
        try
        {
            addCriteria(releaseScheduleConfiguration, schedule, duration, min, percent);
        }
        catch (final HomeException exception)
        {
            fail("billCycle=" + releaseScheduleConfiguration + ", schedule=" + schedule + ",duration=" + duration
                + ",min=" + min + ",percent=" + percent + "\n" + getStackTrace(exception));
        }
    }

    /**
     * Adds a <code>AutoDepositReleaseCriteria</code> to the home.
     *
     * @param releaseScheduleConfiguration
     *            Whether the schdule is day of month or days before bill cycle.
     * @param schedule
     *            The release schedule.
     * @param duration
     *            How many days deposit is hold for after the most recent deposit change.
     * @param min
     *            Minimum deposit balance.
     * @param percent
     *            Percentage of the remaining deposit to be released.
     * @throws HomeException
     *             Thrown if there are problems adding the criteria to home.
     */
    private void addCriteria(final ReleaseScheduleConfigurationEnum releaseScheduleConfiguration,
        final int schedule, final int duration, final int min, final double percent) throws HomeException
    {
        final AutoDepositReleaseCriteria criteria = new AutoDepositReleaseCriteria();
        criteria.setReleaseScheduleConfiguration(releaseScheduleConfiguration);
        criteria.setDepositReleaseAdjustmentType(AdjustmentTypeEnum.DepositRelease.getIndex());
        criteria.setDepositReleasePercent(percent);
        criteria.setMinimumDepositReleaseAmount(min);
        criteria.setReleaseSchedule(schedule);
        criteria.setServiceDuration(duration);
        criteria.setIdentifier(criteriaId_);
        criteriaId_++;

        ((Home) getContext().get(AutoDepositReleaseCriteriaHome.class)).create(criteria);
    }
}
