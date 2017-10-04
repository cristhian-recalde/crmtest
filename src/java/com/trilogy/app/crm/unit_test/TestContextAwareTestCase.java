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
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Provides a base for creating JUnit TestCase implementations that are context
 * aware.  Derived classes should call setParentContext() to set the parent
 * context from which subcontexts will be created.  The setUp() method creates
 * the subcontext returned by getContext(), and the tearDown() discards that
 * subcontext.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestContextAwareTestCase
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestContextAwareTestCase(final String name)
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

        final TestSuite suite = new TestSuite(TestContextAwareTestCase.class);

        return suite;
    }


    /**
     * Tests that the context is properly set from the context support.
     */
    public void testContextSetting()
    {
        final String contextName = getClass().getName();

        final Context context = getContext();
        assertNotNull("Initially, the context should not be null.", context);
        assertEquals("Correct context name.", context.getName(), contextName);

        tearDown();
        assertNull("tearDown() should set the context to null.", getContext());

        setUp();
        final Context newContext = getContext();
        assertNotNull("The setUp() context should not be null.", newContext);
        assertEquals("Correct context name.", context.getName(), contextName);

        assertNotSame(
            "The initial context should not be the same as the newly set-up context.",
            context,
            newContext);
    }
    
    /**
     * Tests that logging is properly tested within unit tests.
     */
    public void testLoggingSupport()
    {
        final Context context = getContext();
        
        assertTrue("Debug logging shoul be enabled by default.", LogSupport.isDebugEnabled(context));
        
        new InfoLogMsg(this, "Logging works.", null).log(context);
    }

} // class
