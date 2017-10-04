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
package com.trilogy.app.crm.support;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;


/**
 * A suite of test cases for BillCycleSupport.
 *
 * @author gary.anderson@redknee.com
 */
public class TestBillCycleSupport extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestBillCycleSupport(final String name)
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

        final TestSuite suite = new TestSuite(TestBillCycleSupport.class);

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
     * Tests that the getCurrentCycleStartDate() method returns the expected
     * dates.
     */
    public void testGetCurrentCycleStartDate()
    {
        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(getContext()).getHandler(ChargingCycleEnum.MONTHLY);
        final Date inputDate = UnitTestSupport.parseDate("1973-09-16 10:15:33.315");
        final BillCycle cycle = new BillCycle();

        cycle.setDayOfMonth(15);
        assertEquals(
            "Day before given date's day.",
            UnitTestSupport.parseDate("1973-09-15 00:00:00.000"),
            handler.calculateCycleStartDate(getContext(), inputDate, cycle.getDayOfMonth(), cycle.getSpid()));

        cycle.setDayOfMonth(17);
        assertEquals(
            "Day after given date's day.",
            UnitTestSupport.parseDate("1973-08-17 00:00:00.000"),
            handler.calculateCycleStartDate(getContext(), inputDate, cycle.getDayOfMonth(), cycle.getSpid()));

        cycle.setDayOfMonth(16);
        assertEquals(
            "Day on given date's day.",
            UnitTestSupport.parseDate("1973-09-16 00:00:00.000"),
            handler.calculateCycleStartDate(getContext(), inputDate, cycle.getDayOfMonth(), cycle.getSpid()));
    }

    /**
     * Tests that the getCurrentCycleStartDate() throws an IllegalArgumentException if the value
     * for parammeter day is wrong.
     */
    public void testGetCurrentCycleStartDate_failure()
    {
        final BillCycle cycle = new BillCycle();

        try
        {
            cycle.setDayOfMonth(29);
            fail("IllegalArgumentException not thrown! Day cannot be greater then 28.");
        }
        catch (IllegalArgumentException e)
        {
        }
    }


    /**
     * Tests that the getPreviousCycleStartDate() method returns the expected
     * dates.
     */
    public void testGetPreviousCycleStartDate()
    {
        final Date inputDate = UnitTestSupport.parseDate("1973-01-16 10:15:33.315");
        final BillCycle cycle = new BillCycle();
        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(getContext()).getHandler(ChargingCycleEnum.MONTHLY);

        cycle.setDayOfMonth(15);
        assertEquals(
            "Day before given date's day.",
            UnitTestSupport.parseDate("1972-12-15 00:00:00.000"),
            CalendarSupportHelper.get(getContext()).findDateMonthsAfter(-1, handler.calculateCycleStartDate(getContext(), inputDate, cycle.getDayOfMonth(), cycle.getSpid())));

        cycle.setDayOfMonth(17);
        assertEquals(
            "Day after given date's day.",
            UnitTestSupport.parseDate("1972-11-17 00:00:00.000"),
            CalendarSupportHelper.get(getContext()).findDateMonthsAfter(-1, handler.calculateCycleStartDate(getContext(), inputDate, cycle.getDayOfMonth(), cycle.getSpid())));

        cycle.setDayOfMonth(16);
        assertEquals(
            "Day on given date's day.",
            UnitTestSupport.parseDate("1972-12-16 00:00:00.000"),
            CalendarSupportHelper.get(getContext()).findDateMonthsAfter(-1, handler.calculateCycleStartDate(getContext(), inputDate, cycle.getDayOfMonth(), cycle.getSpid())));
    }

} // class
