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
package com.trilogy.app.crm.sequenceId;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;


/**
 * A suite of test cases for the com.redknee.app.crm.sequenceId package.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestPackage
    extends TestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestPackage(final String name)
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
        // Ensure that the APPLICATION_CONTEXT is set.
        context.put(APPLICATION_CONTEXT, context.getBoolean(APPLICATION_CONTEXT, true));

        final TestSuite suite = new TestSuite("com.redknee.app.crm.sequenceId.*");
        suite.addTest(TestAdjustmentTypeCodeSettingHome.suite(context));
        suite.addTest(TestOnDemandSequence.suite(context));

        return suite;
    }
    
    public static final String APPLICATION_CONTEXT =
        TestPackage.class.getName() + ".APPLICATION_CONTEXT";

} // class
