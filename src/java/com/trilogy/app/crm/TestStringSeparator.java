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
package com.trilogy.app.crm;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.support.StringSeperator;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * This is an example of problems that can are encountered while using LRUCachingHome
 *
 * @author victor.stratan@redknee.com
 */
public class TestStringSeparator extends ContextAwareTestCase
{
    private static final int ID = 123;

    public TestStringSeparator(final String name)
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

        final TestSuite suite = new TestSuite(TestStringSeparator.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    protected void setUp()
    {
        super.setUp();

    }

    protected void tearDown()
    {
        super.tearDown();
    }

    public void testSimpleFields() throws HomeException
    {
        final StringSeperator sep = new StringSeperator("a,b,c", ',');

        int count = 0;

        while (sep.hasNext())
        {
            sep.next();
            count++;
        }

        assertEquals(3, count);
    }

    public void testQuotedFields() throws HomeException
    {
        final StringSeperator sep = new StringSeperator("\"a\",\"b,f\",c", ',');

        int count = 0;

        while (sep.hasNext())
        {
            sep.next();
            count++;
        }

        assertEquals(3, count);
    }

    public void testEmptyFields() throws HomeException
    {
        final StringSeperator sep = new StringSeperator(",,", ',');

        int count = 0;

        while (sep.hasNext())
        {
            sep.next();
            count++;
        }

        assertEquals(3, count);
    }

}
