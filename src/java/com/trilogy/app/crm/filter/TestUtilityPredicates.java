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
package com.trilogy.app.crm.filter;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;


/**
 * A suite of test cases for the utiltity Predicates provided in this package.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestUtilityPredicates
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestUtilityPredicates(final String name)
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

        final TestSuite suite = new TestSuite(TestUtilityPredicates.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();
    }


    // INHERIT
    public void tearDown()
    {
        super.tearDown();
    }


    /**
     * Tests the CompareEqualsPredicate.
     */
    public void testCompareEqualsPredicate()
    {
        try
        {
            new CompareEqualsPredicate(null);

            fail("Null march parameter should raise exception.");
        }
        catch (final IllegalArgumentException exception)
        {
            // Empty
        }

        assertTrue("Equal items.", new CompareEqualsPredicate(Integer.valueOf(3)).f(getContext(), Integer.valueOf(3)));
        assertFalse("Less than object.", new CompareEqualsPredicate(Integer.valueOf(3)).f(getContext(), Integer.valueOf(2)));
        assertFalse("Greater than object.", new CompareEqualsPredicate(Integer.valueOf(3)).f(getContext(), Integer.valueOf(4)));
    }


    /**
     * Tests the CompareGreaterThanZeroPredicate.
     */
    public void testCompareGreaterThanZeroPredicate()
    {
        try
        {
            new CompareGreaterThanZeroPredicate(null);

            fail("Null march parameter should raise exception.");
        }
        catch (final IllegalArgumentException exception)
        {
            // Empty
        }

        assertFalse("Equal items.", new CompareGreaterThanZeroPredicate(Integer.valueOf(3)).f(getContext(), Integer.valueOf(3)));
        assertTrue("Less than object.", new CompareGreaterThanZeroPredicate(Integer.valueOf(3)).f(getContext(), Integer.valueOf(2)));
    }


    /**
     * Tests the CompareLessThanZeroPredicate.
     */
    public void testCompareLessThanZeroPredicate()
    {
        try
        {
            new CompareLessThanZeroPredicate(null);

            fail("Null march parameter should raise exception.");
        }
        catch (final IllegalArgumentException exception)
        {
            // Empty
        }

        assertFalse("Equal items.", new CompareLessThanZeroPredicate(Integer.valueOf(3)).f(getContext(), Integer.valueOf(3)));
        assertFalse("Less than object.", new CompareLessThanZeroPredicate(Integer.valueOf(3)).f(getContext(), Integer.valueOf(2)));
        assertTrue("Greater than object.", new CompareLessThanZeroPredicate(Integer.valueOf(3)).f(getContext(), Integer.valueOf(4)));
    }


    /**
     * Tests the EqualsPredicate.
     */
    public void testEqualsPredicate()
    {
        try
        {
            new EqualsPredicate(null);

            fail("Null march parameter should raise exception.");
        }
        catch (final IllegalArgumentException exception)
        {
            // Empty
        }

        final Integer match = Integer.valueOf(4);
        final Integer successfulMatch = new Integer(4);

        assertEquals("Objects are equal.", match, successfulMatch);
        assertNotSame("Objects are not the same.", match, successfulMatch);

        final EqualsPredicate predicate = new EqualsPredicate(match);

        assertTrue("Expected match.", predicate.f(getContext(),successfulMatch));

        assertFalse("Less than check", predicate.f(getContext(),Integer.valueOf(3)));
        assertFalse("Greater than check", predicate.f(getContext(),Integer.valueOf(5)));
    }


    /**
     * Tests the SamePredicate.
     */
    public void testSamePredicate()
    {
        try
        {
            new SamePredicate(null);

            fail("Null march parameter should raise exception.");
        }
        catch (final IllegalArgumentException exception)
        {
            // Empty
        }

        final Integer match = Integer.valueOf(4);
        final Integer unsuccessfulMatch = new Integer(4);

        assertEquals("Objects are equal.", match, unsuccessfulMatch);
        assertNotSame("Objects are not the same.", match, unsuccessfulMatch);

        final SamePredicate predicate = new SamePredicate(match);

        assertTrue("Expected match.", predicate.f(getContext(),match));
        assertFalse("Expected match.", predicate.f(getContext(),unsuccessfulMatch));
    }


} // class
