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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.client.test;

import com.trilogy.framework.xhome.context.Context;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author rchen
 *
 */
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


    private static Test suite(Context ctx)
    {
        final TestSuite suite = new TestSuite(TestPackage.class.getPackage().getName() + ".*");
        
        suite.addTest(TestAppEcpClient.suite(ctx));
        suite.addTest(TestAppOcgClient.suite(ctx));
        suite.addTest(TestPricePlanMgmtClient.suite(ctx));
        suite.addTest(TestAppSmsbClient.suite(ctx));
        suite.addTest(TestAppPinManagerClient.suite(ctx));
        
        return suite;
    }

}
