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
package com.trilogy.app.crm.bean.calldetail;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for CallDetail.
 *
 * @author gary.anderson@redknee.com
 */
public class TestCallDetail
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestCallDetail(final String name)
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

        final TestSuite suite = new TestSuite(TestCallDetail.class);

        return suite;
    }


    /**
     * Tests that destMSISDN can be set to null in the CallDetail bean.
     */
    public void testNullDestMSISDN()
    {
        final CallDetail detail = new CallDetail();
        detail.setDestMSISDN(null);
        assertNull("The destMSISDN should be null, as set.", detail.getDestMSISDN());
    }


    /**
     * Tests that destMSISDN can be set to blank in the CallDetail bean.
     */
    public void testBlankDestMSISDN()
    {
        final CallDetail detail = new CallDetail();
        detail.setDestMSISDN("");
        assertEquals("The destMSISDN should be blank, as set.", "", detail.getDestMSISDN());
    }


    /**
     * Tests that the destMSISDN can be set to a string of digits and non-digits
     * less than the model specified maximum width.
     */
    public void testLessThanMaximumWidthDestMSISDN()
    {
        final String testMSISDN = "123abc123";
        assertTrue(
            "The test MSISDN must be less than the currently defined maximum width.",
            testMSISDN.length() < CallDetail.DESTMSISDN_WIDTH);

        final CallDetail detail = new CallDetail();
        detail.setDestMSISDN(testMSISDN);
        assertEquals(
            "The destMSISDN should equal the test MSISDN, as set.",
            testMSISDN, detail.getDestMSISDN());
    }


    /**
     * Tests that the destMSISDN can be set to a string of digits and non-digits
     * equal to the model specified maximum width.
     */
    public void testEqualToMaximumWidthDestMSISDN()
    {
        final String testMSISDN = "123abc123abc123abc12";
        assertEquals(
            "The test MSISDN must be equal to the currently defined maximum width.",
            CallDetail.DESTMSISDN_WIDTH, testMSISDN.length());

        final CallDetail detail = new CallDetail();
        detail.setDestMSISDN(testMSISDN);
        assertEquals(
            "The destMSISDN should equal the test MSISDN, as set.",
            testMSISDN, detail.getDestMSISDN());
    }


    /**
     * Tests that the destMSISDN can be set to a string of digits and non-digits
     * greater than the model specified maximum width.  The resulting destMSISDN
     * returned from the bean should be given distMSISDN truncated to the
     * maximum width.
     */
    public void testGreaterThanMaximumWidthDestMSISDN()
    {
        final String testMSISDN = "123abc123abc123abc123abc12";
        assertTrue(
            "The test MSISDN must be greater than the currently defined maximum width.",
            testMSISDN.length() > CallDetail.DESTMSISDN_WIDTH);

        final String resultingMSISDN = testMSISDN.substring(0, CallDetail.DESTMSISDN_WIDTH);

        assertEquals(
            "The length of the expected resulting MSISDN should be the maximum width.",
            CallDetail.DESTMSISDN_WIDTH, resultingMSISDN.length());

        assertTrue(
            "For this test to be valid, the length of the expected resulting MSISDN should be less than that of the test MSISDN.",
            resultingMSISDN.length() < testMSISDN.length());

        final CallDetail detail = new CallDetail();
        detail.setDestMSISDN(testMSISDN);
        assertEquals(
            "The length of the resulting destMSISDN should be equal to the maximum width.",
            CallDetail.DESTMSISDN_WIDTH, detail.getDestMSISDN().length());

        assertEquals(
            "The resulting destMSISDN should be equal to the expected truncated value.",
            resultingMSISDN, detail.getDestMSISDN());
    }

} // class
