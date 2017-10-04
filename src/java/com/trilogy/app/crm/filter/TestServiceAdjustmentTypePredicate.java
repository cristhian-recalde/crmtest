/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;


/**
 * A suite of test cases for ServiceAdjustmentTypePredicate.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestServiceAdjustmentTypePredicate
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestServiceAdjustmentTypePredicate(final String name)
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

        final TestSuite suite = new TestSuite(TestServiceAdjustmentTypePredicate.class);

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
     * Tests that the f() method works according to the intent.
     */
    public void testF()
    {
        final ServiceBaseByAdjustmentType predicate =
            new ServiceBaseByAdjustmentType(123);
        
        final Service service1 = new Service();
        service1.setID(1);
        service1.setAdjustmentType(123);
        assertTrue(
            "The given AdjustmentType code is the same as the one in the Service",
            predicate.f(getContext(),service1));
        
        final Service service2 = new Service();
        service2.setID(2);
        service2.setAdjustmentType(456);
        assertFalse(
            "The given AdjustmentType code is NOT the one in the Service",
            predicate.f(getContext(),service2));
    }
}
