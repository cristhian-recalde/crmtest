package com.trilogy.app.crm.transaction;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

public class TestPackage extends TestCase 
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
        final TestSuite suite = new TestSuite("com.redknee.app.crm.transaction.*");
        suite.addTest(TestAccountPaymentTransactionProcessor.suite(context));
        return suite;
    }

} // class
