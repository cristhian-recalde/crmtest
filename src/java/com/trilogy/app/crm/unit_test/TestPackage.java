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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;


/**
 * A suite of test cases for the com.redknee.app.crm.unit_test package.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestPackage
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
        final TestSuite suite = new TestSuite("com.redknee.app.crm.unit_test.*");
        suite.addTest(TestContextAwareTestCase.suite(context));
        suite.addTest(TestAssert.suite(context));
        suite.addTest(TestPrivilegedAccessor.suite(context));

        suite.addTest(TestSetupAccountHierarchy.suite(context));
        suite.addTest(TestSetupCallDetails.suite(context));
        suite.addTest(TestSetupAdjustmentTypes.suite(context));
        suite.addTest(TestSetupTransactions.suite(context));
        suite.addTest(TestSetupPricePlanAndServices.suite(context));
        suite.addTest(TestSetupIdentifierSequence.suite(context));
        return suite;
    }

} // class
