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
package com.trilogy.app.crm.provision;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for UpdatedPricePlanHandler.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestUpdatedPricePlanHandler
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestUpdatedPricePlanHandler(final String name)
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

        final TestSuite suite = new TestSuite(TestUpdatedPricePlanHandler.class);

        return suite;
    }


    // INHERIT
    @Override
    protected void setUp()
    {
        super.setUp();

        // Set-up the plan.
        {
            plan_ = new PricePlan();
            plan_.setId(5);
            plan_.setCurrentVersion(11);
        }
        
        // Set-up the old-version facet.
        {
            oldVersion_ = new PricePlanVersion();
            oldVersion_.setId(5);
            oldVersion_.setVersion(11);
        }

        // Set-up the new-version facet.
        {
            newVersion_ = new PricePlanVersion();
            newVersion_.setId(5);
            newVersion_.setVersion(12);
        }
    }


    // INHERIT
    @Override
    protected void tearDown()
    {
        super.tearDown();
        plan_ = null;
        oldVersion_ = null;
        newVersion_ = null;
    }


    /**
     * Simple test of the accessors.
     */
    public void testAccessors()
    {
        final UpdatedPricePlanHandler handler =
            new UpdatedPricePlanHandler(getContext(), plan_, newVersion_);

        assertEquals("Context", getContext(), handler.getContext());
        assertEquals("Plan", plan_, handler.getPricePlan());
        assertEquals("New Version", newVersion_, handler.getNewVersion());
    }

    /**
     * Tests that the constructor throws exceptions when it's supposed to.
     */
    public void testConstructor()
    {
        try
        {
            new UpdatedPricePlanHandler(
                null,
                plan_,
                newVersion_);

            fail("Null context parameter should have thrown.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            new UpdatedPricePlanHandler(
                getContext(),
                null,
                newVersion_);

            fail("Null plan parameter should have thrown.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            new UpdatedPricePlanHandler(
                getContext(),
                plan_,
                null);

            fail("Null new version parameter should have thrown.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            new UpdatedPricePlanHandler(getContext(), plan_, newVersion_);
        }
        catch (final IllegalArgumentException exception)
        {
            fail("Legal values should not throw: " + exception.getMessage());
        }
    }


    /**
     * Tests that the facets are set up properly for the other tests.
     */
    public void testFacets()
    {
        assertNotNull("Plan set-up.", plan_);
        assertNotNull("Old version set-up.", oldVersion_);
        assertNotNull("New version set-up.", newVersion_);

        assertEquals("Plan identifier set-up.", plan_.getId(), newVersion_.getId());
        assertEquals("Plan identifier set-up.", oldVersion_.getId(), newVersion_.getId());
        assertTrue("Version updated set-up.", oldVersion_.getVersion() < newVersion_.getVersion());

        tearDown();

        assertNull("Plan torn-down.", plan_);
        assertNull("Old version torn-down.", oldVersion_);
        assertNull("New version torn-down.", newVersion_);

        setUp();

        assertNotNull("Plan re-set-up.", plan_);
        assertNotNull("Old version re-set-up.", oldVersion_);
        assertNotNull("New version re-set-up.", newVersion_);

        assertEquals("Plan identifier re-set-up.", plan_.getId(), newVersion_.getId());
        assertEquals("Plan identifier re-set-up.", oldVersion_.getId(), newVersion_.getId());
        assertTrue("Version updated re-set-up.", oldVersion_.getVersion() < newVersion_.getVersion());
    }


    /**
     * A reusable plan facet.
     */
    private PricePlan plan_ = null;

    /**
     * A reusable old-version facet.
     */
    private PricePlanVersion oldVersion_ = null;

    /**
     * A reusable new-version facet.
     */
    private PricePlanVersion newVersion_ = null;

} // class
