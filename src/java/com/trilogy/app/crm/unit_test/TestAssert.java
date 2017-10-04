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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;


/**
 * A suite of test cases for Assert.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestAssert
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestAssert(final String name)
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

        final TestSuite suite = new TestSuite(TestAssert.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();
    }


    /**
     * {@inheritDoc}
     */
    public void tearDown()
    {
        super.tearDown();
    }


    /**
     * Tests that the assertEquals(Date, Date, precision) method works.
     *
     * @exception ParseException Thrown if the dates do not parse properly.
     */
    public void testAssertDateDatePrecision()
        throws ParseException
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setLenient(false);

        Assert.assertEquals(
            format.parse("2005-03-14 14:44:21.152"),
            format.parse("2005-03-14 14:44:21.152"),
            0);

        Assert.assertEquals(
            format.parse("2005-03-14 14:44:21.0"),
            format.parse("2005-03-14 14:44:21.152"),
            152);

        try
        {
            Assert.assertEquals(
                format.parse("2005-03-14 14:44:21.0"),
                format.parse("2005-03-14 14:44:21.152"),
                151);

            fail("Difference outside of allowance should fail.");
        }
        catch (final AssertionFailedError error)
        {
            // Empty
        }

        Assert.assertEquals(
            format.parse("2005-03-14 14:44:21.152"),
            format.parse("2005-03-14 14:44:21.0"),
            152);

        try
        {
            Assert.assertEquals(
                format.parse("2005-03-14 14:44:21.152"),
                format.parse("2005-03-14 14:44:21.0"),
                151);

            fail("Difference outside of allowance should fail.");
        }
        catch (final AssertionFailedError error)
        {
            // Empty
        }
    }

} // class
