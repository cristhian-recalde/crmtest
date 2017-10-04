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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for the PrivilegedAccessor class that allows private
 * methods to be executed on objects.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestPrivilegedAccessor
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestPrivilegedAccessor(final String name)
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

        final TestSuite suite = new TestSuite(TestPrivilegedAccessor.class);

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
     * Tests that we can access a private method with no arguments and that the
     * constructor and accessor work as expected.
     */
    public void testNoParameterMethod()
        throws NoSuchMethodException, InvocationTargetException
    {
        // Test case 1.
        {
            final GrandClass object = new GrandClass(3);
            assertEquals("Initialized value.", 3, object.getValue());
            assertEquals(
                "Value from private method.",
                Integer.valueOf(3),
                PrivilegedAccessor.invokeMethod(object, "privateGetValue"));
        }

        // Test case 2.
        {
            final GrandClass object = new GrandClass(7);
            assertEquals("Initialized value.", 7, object.getValue());
            assertEquals(
                "Value from private method.",
                Integer.valueOf(7),
                PrivilegedAccessor.invokeMethod(object, "privateGetValue"));
        }
    }


    /**
     * Tests that we can access a private method with one paramter, and that we
     * can access private methods from higher in the inheritance hierarchy.
     */
    public void testOneParameterMethod()
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        assertEquals(
            "Value from private method.",
            Integer.valueOf(13),
            PrivilegedAccessor.invokeMethod(new ParentClass(13), "privateGetValue"));

        final Method addToValue =
            PrivilegedAccessor.getMethod(
                ChildClass.class,
                "addToValue",
                new Class[]
                {
                    Integer.TYPE
                });

        assertEquals(
            "First addition.",
            Integer.valueOf(15),
            addToValue.invoke(
                new ParentClass(13),
                new Object[]
                {
                    Integer.valueOf(2)
                }));

        assertEquals(
            "First addition.",
            Integer.valueOf(11),
            addToValue.invoke(
                new ParentClass(5),
                new Object[]
                {
                    Integer.valueOf(6)
                }));
    }


    /**
     * Tests that we can access a private method with multiple paramters, and that we
     * can access private methods from higher in the inheritance hierarchy.
     */
    public void testMultiParameterMethod()
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        assertEquals(
            "Value from private method.",
            Integer.valueOf(4),
            PrivilegedAccessor.invokeMethod(new ChildClass(4), "privateGetValue"));

        final Method betweenValues =
            PrivilegedAccessor.getMethod(
                ChildClass.class,
                "isBetweenValues",
                new Class[]
                {
                    Integer.TYPE,
                    Integer.TYPE
                });

        assertEquals(
            "First comparison.",
            Boolean.TRUE,
            betweenValues.invoke(
                new ChildClass(4),
                new Object[]
                {
                    Integer.valueOf(2),
                    Integer.valueOf(8)
                }));

        assertEquals(
            "Second comparison.",
            Boolean.FALSE,
            betweenValues.invoke(
                new ChildClass(13),
                new Object[]
                {
                    Integer.valueOf(2),
                    Integer.valueOf(8)
                }));

        assertEquals(
            "Second comparison.",
            Boolean.FALSE,
            betweenValues.invoke(
                new ChildClass(-3),
                new Object[]
                {
                    Integer.valueOf(2),
                    Integer.valueOf(8)
                }));
    }

} // class

/**
 * The top of the test class hierarchy.
 */
class GrandClass
{
    public GrandClass(final int value)
    {
        value_ = value;
    }

    protected int getValue()
    {
        return value_;
    }

    private int privateGetValue()
    {
        return value_;
    }

    private final int value_;

} // private class


/**
 * The middle of the test class hierarchy.
 */
class ParentClass
    extends GrandClass
{
    public ParentClass(final int value)
    {
        super(value);
    }

    private int addToValue(final int value)
    {
        return getValue() + value;
    }

} // private class


/**
 * The bottom of the test class hierarchy.
 */
class ChildClass
    extends ParentClass
{
    public ChildClass(final int value)
    {
        super(value);
    }

    private boolean isBetweenValues(final int lower, final int upper)
    {
        return lower < getValue() && getValue() < upper;
    }

} // private class
