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
package com.trilogy.app.crm.poller;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.ERFilterPair;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * A suite of test cases for CRMPoller.
 *
 * @author larry.xia@redknee.com
 */
public class TestCrmPoller extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestCrmPoller(final String name)
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

        final TestSuite suite = new TestSuite(TestCrmPoller.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();
    }

    public void testERfilterMatch()
    {
        final List<String> params = new ArrayList<String>();
        params.add("1");

        final List<ERFilterPair> filters = new ArrayList<ERFilterPair>();
        final ERFilterPair filter = new ERFilterPair();
        filter.setId("442");
        filter.setPosition(0);
        filter.setFilterPattern("0|1");
        filters.add(filter);

        assertEquals(false, CRMProcessorSupport.filterER(getContext(), params, filters));
    }

    public void testERfilterNotMatch()
    {
        final List<String> params = new ArrayList<String>();
        params.add("3");

        final List<ERFilterPair> filters = new ArrayList<ERFilterPair>();
        final ERFilterPair filter = new ERFilterPair();
        filter.setId("442");
        filter.setPosition(0);
        filter.setFilterPattern("0|1");
        filters.add(filter);

        assertEquals(true, CRMProcessorSupport.filterER(getContext(), params, filters));
    }

    /**
     * {@inheritDoc}
     */
    public void tearDown()
    {

        super.tearDown();
    }

    /**
     * Tests that the validate() method works according to the intent.
     * Test case for invalid MSISDN provided.
     */
    public void testValidate_invalidMSISDN()
    {

    }
}
