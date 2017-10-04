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
package com.trilogy.app.crm.home.calldetail;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailTransientHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for CallDetailValidatingHome.
 *
 * @author gary.anderson@redknee.com
 */
public class TestCallDetailValidatingHome
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestCallDetailValidatingHome(final String name)
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

        final TestSuite suite = new TestSuite(TestCallDetailValidatingHome.class);

        return suite;
    }


    /**
     * Creates a testing CallDetail Home pipeline that only has
     * CallDetailValidatingHome as a decorator.
     */
    public void setUp()
    {
        super.setUp();
        Home home = new CallDetailTransientHome(getContext());
        home = new CallDetailValidatingHome(home);
        getContext().put(CallDetailHome.class, home);
    }


    /**
     * Tests that a valid DestMSISDN is not rejected.
     */
    public void testValidDestMSISDN()
        throws HomeException
    {
        final Home home = (Home)getContext().get(CallDetailHome.class);

        // Blank
        {
            final CallDetail inputCallDetail = new CallDetail();
            inputCallDetail.setId(0);
            inputCallDetail.setDestMSISDN("");
            home.create(inputCallDetail);
        }

        // One digit
        {
            final CallDetail inputCallDetail = new CallDetail();
            inputCallDetail.setId(1);
            inputCallDetail.setDestMSISDN("1");
            home.create(inputCallDetail);
        }

        // 20 digits
        {
            final CallDetail inputCallDetail = new CallDetail();
            inputCallDetail.setId(2);
            inputCallDetail.setDestMSISDN("12345678901234567890");
            home.create(inputCallDetail);
        }

        // 10 digits and some non-digit characters
        {
            final CallDetail inputCallDetail = new CallDetail();
            inputCallDetail.setId(3);
            inputCallDetail.setDestMSISDN("a2*55534#34r1");
            home.create(inputCallDetail);
        }
    }


    /**
     * Tests that a DestMSISDN of greater than the model defined maximum is
     * truncated to the maximum.
     */
    public void testDestMSISDNTruncation()
        throws HomeException
    {
        final Home home = (Home)getContext().get(CallDetailHome.class);

        assertTrue("The Home should initially be empty.", home.selectAll().size() == 0);

        final CallDetail inputCallDetail = new CallDetail();
        inputCallDetail.setDestMSISDN("123456789012345678901");
    }

} // class
