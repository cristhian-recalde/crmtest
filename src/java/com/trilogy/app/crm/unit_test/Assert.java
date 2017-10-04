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

import java.util.Date;


/**
 * Additional useful asserts.
 *
 * @author gary.anderson@redknee.com
 */
public final
class Assert
{
    /**
     * Prevent instantiation of this utility class.
     */
    private Assert()
    {
        // Empty
    }


    /**
     * Asserts that the two dates are equal to within the given number of
     * milliseconds.
     *
     * @param message The message to display if the asserticn fails.
     * @param expected The expected date.
     * @param actual The actual date.
     * @param allowance The maximum number of milliseconds difference allowed
     * for the two dates to still be considered equal.
     */
    public static void assertEquals(
        final String message,
        final Date expected,
        final Date actual,
        final long allowance)
    {
        final long difference = expected.getTime() - actual.getTime();
        if (Math.abs(difference) > allowance)
        {
            junit.framework.Assert.fail(
                message
                + ": actual <"
                + actual
                + "> differs from expected <"
                + expected
                + "> by "
                + difference
                + " milliseconds.");
        }
    }


    /**
     * Asserts that the two dates are equal to within the given number of
     * milliseconds.
     *
     * @param expected The expected date.
     * @param actual The actual date.
     * @param allowance The maximum number of milliseconds difference allowed
     * for the two dates to still be considered equal.
     */
    public static void assertEquals(
        final Date expected,
        final Date actual,
        final long allowance)
    {
        assertEquals("Not equal", expected, actual, allowance);
    }

} // class
