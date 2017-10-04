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
package com.trilogy.app.crm.support.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * A suite of test cases for CalendarSupportHelper.get().
 *
 * @author gary.anderson@redknee.com
 */
public class TestCalendarSupport extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestCalendarSupport(final String name)
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

        final TestSuite suite = new TestSuite(TestCalendarSupport.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
        super.setUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        super.tearDown();
    }

    /**
     * Tests that the time-of-day is properly cleared from the dates.
     *
     * @throws ParseException Thrown if the test's date formatting fails.
     */
    public void testGetDateWithNoTimeOfDay()
        throws ParseException
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        final Date inputDate = format.parse("2004-09-29 22:13:45.567");

        // Check the input date.
        {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputDate);
            assertEquals("Input Year", 2004, calendar.get(Calendar.YEAR));
            // NOTE - Month is indexed from zero.
            assertEquals("Input Month", 8, calendar.get(Calendar.MONTH));
            assertEquals("Input Day", 29, calendar.get(Calendar.DAY_OF_MONTH));
            assertEquals("Input Hour", 22, calendar.get(Calendar.HOUR_OF_DAY));
            assertEquals("Input Minute", 13, calendar.get(Calendar.MINUTE));
            assertEquals("Input Seconds", 45, calendar.get(Calendar.SECOND));
            assertEquals("Input Milliseconds", 567, calendar.get(Calendar.MILLISECOND));
        }

        final Date clearedDate = CalendarSupportHelper.get().getDateWithNoTimeOfDay(inputDate);

        // Check the cleared date.
        {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(clearedDate);
            assertEquals("Input Year", 2004, calendar.get(Calendar.YEAR));
            // NOTE - Month is indexed from zero.
            assertEquals("Input Month", 8, calendar.get(Calendar.MONTH));
            assertEquals("Input Day", 29, calendar.get(Calendar.DAY_OF_MONTH));
            assertEquals("Input Hour", 0, calendar.get(Calendar.HOUR_OF_DAY));
            assertEquals("Input Minute", 0, calendar.get(Calendar.MINUTE));
            assertEquals("Input Seconds", 0, calendar.get(Calendar.SECOND));
            assertEquals("Input Milliseconds", 0, calendar.get(Calendar.MILLISECOND));
        }

        assertNotSame("Cleared Date is new object.", inputDate, clearedDate);

        // Check the input date, it should not have been changed.
        {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputDate);
            assertEquals("Input Year", 2004, calendar.get(Calendar.YEAR));
            // NOTE - Month is indexed from zero.
            assertEquals("Input Month", 8, calendar.get(Calendar.MONTH));
            assertEquals("Input Day", 29, calendar.get(Calendar.DAY_OF_MONTH));
            assertEquals("Input Hour", 22, calendar.get(Calendar.HOUR_OF_DAY));
            assertEquals("Input Minute", 13, calendar.get(Calendar.MINUTE));
            assertEquals("Input Seconds", 45, calendar.get(Calendar.SECOND));
            assertEquals("Input Milliseconds", 567, calendar.get(Calendar.MILLISECOND));
        }
    }

    /**
     * Tests that the getDayAfter() method returns the day after the given date
     * with the time-of-day set to zero.
     *
     * @throws ParseException Thrown if the test's date formatting fails.
     */
    public void testGetDayAfter()
        throws ParseException
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        {
            final Date inputDate = format.parse("2004-09-29 22:13:45.567");
            final Date expectedDate = format.parse("2004-09-30 00:00:00.000");

            assertEquals(
                    "Within month.",
                    CalendarSupportHelper.get().getDayAfter(inputDate), expectedDate);
        }

        {
            final Date inputDate = format.parse("2004-09-30 22:13:45.567");
            final Date expectedDate = format.parse("2004-10-01 00:00:00.000");

            assertEquals(
                    "Between months, within year.",
                    CalendarSupportHelper.get().getDayAfter(inputDate), expectedDate);
        }

        {
            final Date inputDate = format.parse("2004-12-31 22:13:45.567");
            final Date expectedDate = format.parse("2005-01-01 00:00:00.000");

            assertEquals(
                    "Between years.",
                    CalendarSupportHelper.get().getDayAfter(inputDate), expectedDate);
        }
    }

    /**
     * Tests that the time-of-day is properly cleared from the dates.
     *
     * @throws ParseException Thrown if the test's date formatting fails.
     */
    public void testGetEndOfDay()
        throws ParseException
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        final Date inputDate = format.parse("2004-09-29 22:13:45.567");

        // Check the input date.
        {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputDate);
            assertEquals("Input Year", 2004, calendar.get(Calendar.YEAR));
            // NOTE - Month is indexed from zero.
            assertEquals("Input Month", 8, calendar.get(Calendar.MONTH));
            assertEquals("Input Day", 29, calendar.get(Calendar.DAY_OF_MONTH));
            assertEquals("Input Hour", 22, calendar.get(Calendar.HOUR_OF_DAY));
            assertEquals("Input Minute", 13, calendar.get(Calendar.MINUTE));
            assertEquals("Input Seconds", 45, calendar.get(Calendar.SECOND));
            assertEquals("Input Milliseconds", 567, calendar.get(Calendar.MILLISECOND));
        }

        final Date clearedDate = CalendarSupportHelper.get().getEndOfDay(inputDate);

        // Check the cleared date.
        {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(clearedDate);
            assertEquals("Input Year", 2004, calendar.get(Calendar.YEAR));
            // NOTE - Month is indexed from zero.
            assertEquals("Input Month", 8, calendar.get(Calendar.MONTH));
            assertEquals("Input Day", 29, calendar.get(Calendar.DAY_OF_MONTH));
            assertEquals("Input Hour", 23, calendar.get(Calendar.HOUR_OF_DAY));
            assertEquals("Input Minute", 59, calendar.get(Calendar.MINUTE));
            assertEquals("Input Seconds", 59, calendar.get(Calendar.SECOND));
            assertEquals("Input Milliseconds", 999, calendar.get(Calendar.MILLISECOND));
        }

        assertNotSame("Cleared Date is new object.", inputDate, clearedDate);

        // Check the input date, it should not have been changed.
        {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputDate);
            assertEquals("Input Year", 2004, calendar.get(Calendar.YEAR));
            // NOTE - Month is indexed from zero.
            assertEquals("Input Month", 8, calendar.get(Calendar.MONTH));
            assertEquals("Input Day", 29, calendar.get(Calendar.DAY_OF_MONTH));
            assertEquals("Input Hour", 22, calendar.get(Calendar.HOUR_OF_DAY));
            assertEquals("Input Minute", 13, calendar.get(Calendar.MINUTE));
            assertEquals("Input Seconds", 45, calendar.get(Calendar.SECOND));
            assertEquals("Input Milliseconds", 567, calendar.get(Calendar.MILLISECOND));
        }
    }

    /**
     * Tests that the getNumberOfDaysInMonth() method works according to the intent.
     */
    public void testGetNumberOfDaysInMonth()
    {
        final int numOfDaysInJan2003 = CalendarSupportHelper.get().getNumberOfDaysInMonth(Calendar.JANUARY, 2003);
        assertEquals("Month with 31 Days", 31, numOfDaysInJan2003);

        final int numOfDaysInApr2003 = CalendarSupportHelper.get().getNumberOfDaysInMonth(Calendar.APRIL, 2003);
        assertEquals("Month with 30 Days", 30, numOfDaysInApr2003);

        final int numOfDaysInFeb2003 = CalendarSupportHelper.get().getNumberOfDaysInMonth(Calendar.FEBRUARY, 2003);
        assertEquals("February 2003 has 28 Days", 28, numOfDaysInFeb2003);

        final int numOfDaysInFeb2004 = CalendarSupportHelper.get().getNumberOfDaysInMonth(Calendar.FEBRUARY, 2004);
        assertEquals("February 2004 has 29 Days", 29, numOfDaysInFeb2004);
    }

    /**
     * Tests that the getDayAfter() method returns the day after the given date
     * with the time-of-day set to zero.
     *
     * @throws ParseException Thrown if the test's date formatting fails.
     */
    public void testFindDateMonthsAfter()
        throws ParseException
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        {
            final Date inputDate = format.parse("2004-10-28");
            final int monthNum = 2;
            final Date expectedDate = format.parse("2004-12-28");

            assertEquals(
                    monthNum + " later",
                    CalendarSupportHelper.get().findDateMonthsAfter(monthNum, inputDate), expectedDate);
        }

        {
            final Date inputDate2 = format.parse("2004-6-28");
            final int monthNum2 = 7;
            final Date expectedDate2 = format.parse("2005-1-28");
            System.out.println(expectedDate2);
            assertEquals(
                    monthNum2 + " later",
                    CalendarSupportHelper.get().findDateMonthsAfter(monthNum2, inputDate2), expectedDate2);
        }
    }

    /**
     * Tests that the getYearsAfter() method returns the date after the given date
     * with the time-of-day set to zero.
     *
     * @throws ParseException Thrown if the test's date formatting fails.
     */
    public void testFindDateYearsAfter()
        throws ParseException
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        {
            final Date inputDate = format.parse("2004-10-28");
            final int yearNum = 20;
            final Date expectedDate = format.parse("2024-10-28");

            assertEquals(
                    yearNum + " later",
                    CalendarSupportHelper.get().findDateYearsAfter(yearNum, inputDate), expectedDate);
        }
        {
            final Date inputDate = format.parse("2004-10-28");
            final int yearNum = 4;
            final Date expectedDate = format.parse("2008-10-28");

            assertEquals(
                    yearNum + " later",
                    CalendarSupportHelper.get().findDateYearsAfter(yearNum, inputDate), expectedDate);
        }
    }

    /**
     * Tests that the getDaysAfter() method returns the date after the given date
     * with the time-of-day set to zero.
     *
     * @throws ParseException Thrown if the test's date formatting fails.
     */
    public void testFindDateDaysAfter()
        throws ParseException
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        {
            final Date inputDate = format.parse("2004-10-28");
            final int daysNum = 1;
            final Date expectedDate = format.parse("2004-10-29");

            assertEquals(
                    daysNum + " later",
                    CalendarSupportHelper.get().findDateDaysAfter(daysNum, inputDate), expectedDate);
        }
        {
            final Date inputDate = format.parse("2004-10-28");
            final int daysNum = 4;
            final Date expectedDate = format.parse("2004-11-01");

            assertEquals(
                    daysNum + " later",
                    CalendarSupportHelper.get().findDateDaysAfter(daysNum, inputDate), expectedDate);
        }
    }

    /**
     * Tests that the findBillingDayOfMonth() method returns a day that correspond to earlies legal bill cycle day.
     * with the time-of-day set to zero.
     *
     * @throws ParseException Thrown if the test's date formatting fails.
     */
    public void testFindBillingDayOfMonth()
        throws ParseException
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        {
            final Date inputDate = format.parse("2008-10-04");

            assertEquals(
                    "4th",
                    CalendarSupportHelper.get().findBillingDayOfMonth(inputDate), 4);
        }
        {
            final Date inputDate = format.parse("2008-10-28");

            assertEquals(
                    "28th",
                    CalendarSupportHelper.get().findBillingDayOfMonth(inputDate), 28);
        }
        {
            final Date inputDate = format.parse("2008-10-29");

            assertEquals(
                    "28th",
                    CalendarSupportHelper.get().findBillingDayOfMonth(inputDate), 28);
        }
        {
            final Date inputDate = format.parse("2008-10-30");

            assertEquals(
                    "28th",
                    CalendarSupportHelper.get().findBillingDayOfMonth(inputDate), 28);
        }
        {
            final Date inputDate = format.parse("2008-10-31");

            assertEquals(
                    "28th",
                    CalendarSupportHelper.get().findBillingDayOfMonth(inputDate), 28);
        }
    }
}
