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
package com.trilogy.app.crm.api.rmi.impl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;

/**
 * Unit tests for the com.redknee.app.crm.api.rmi package.
 *
 * @author victor.stratan@redknee.com
 */
public class TestPackage extends TestCase
{
    public static final String APPLICATION_CONTEXT =
        TestPackage.class.getName() + ".APPLICATION_CONTEXT";
    
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
        
        final TestSuite suite = new TestSuite("com.redknee.app.crm.api.rmi.impl.*");
        suite.addTest(ApiErrorHandlingTest.suite(context));
        suite.addTest(MobileNumbersImplTest.suite(context));
        suite.addTest(TransactionsImplTest.suite(context));
        suite.addTest(CallDetailsImplTest.suite(context));
        suite.addTest(ServicesAndBundlesImplTest.suite(context));
        suite.addTest(SubscribersImplTest.suite(context));
        return suite;
    }
    
    /**
     * Returns a CRMRequestHeader for the unit test query
     * @return
     */
    public static CRMRequestHeader createRequestHeader()
    {
        CRMRequestHeader header = new CRMRequestHeader ();
        header.setUsername("rkadm");
        header.setPassword("rkadm");
        header.setTransactionID("1234567890");
        return header;
    }
}
