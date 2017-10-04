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
package com.trilogy.app.crm.support;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequest;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestTransientHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;


/**
 * A suite of test cases for PricePlanSupport.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestPricePlanSupport
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestPricePlanSupport(final String name)
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

        final TestSuite suite = new TestSuite(TestPricePlanSupport.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();

        (new com.redknee.app.crm.core.agent.BeanFactoryInstall()).execute(getContext());
    }


    // INHERIT
    public void tearDown()
    {
        super.tearDown();
    }

    
    /**
     * Tests that the getPlan() method works according to its documentation.
     */
    public void testGetPlan()
        throws HomeException
    {
        try
        {
            PricePlanSupport.getPlan(null, 1);

            fail("Null context parameter should raise exception.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }
        
        try
        {
            PricePlanSupport.getPlan(getContext(), 1);

            fail("No home in context should raise exception.");
        }
        catch (final HomeException exception)
        {
            // EMPTY
        }

        final Home home = new AdapterHome(
                getContext(), 
                new PricePlanTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlan, com.redknee.app.crm.bean.core.PricePlan>(
                        com.redknee.app.crm.bean.PricePlan.class, 
                        com.redknee.app.crm.bean.core.PricePlan.class));
        getContext().put(PricePlanHome.class, home);
        
        PricePlan plan = new PricePlan();
        plan.setId(13);

        plan = (PricePlan)home.create(getContext(),plan);

        assertNotNull("Created plan should not be null.", plan);

        // Look for a plan that we know does not exist.
        {
            final PricePlan nonexistantPlan =
                PricePlanSupport.getPlan(getContext(), 10);

            assertNull(
                "The nonexistant plan should be null.",
                nonexistantPlan);
        }

        // Look for the plan we know exists.
        {
            final PricePlan existingPlan =
                PricePlanSupport.getPlan(getContext(), 13);

            assertNotNull(
                "The existing plan should not be null.",
                existingPlan);

            assertEquals("The looked-up plan.", plan, existingPlan);
        }
    }


    /**
     * Tests that the getVersion() method works according to its documentation.
     */
    public void testGetVersion()
        throws HomeException
    {
        try
        {
            PricePlanSupport.getVersion(null, 1, 2);

            fail("Null context parameter should raise exception.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }
        
        try
        {
            PricePlanSupport.getVersion(getContext(), 1, 2);

            fail("No home in context should raise exception.");
        }
        catch (final HomeException exception)
        {
            // EMPTY
        }

        final Home home = new AdapterHome(
                getContext(), 
                new PricePlanVersionTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                        com.redknee.app.crm.bean.PricePlanVersion.class, 
                        com.redknee.app.crm.bean.core.PricePlanVersion.class));
        getContext().put(PricePlanVersionHome.class, home);

        PricePlanVersion version = new PricePlanVersion();
        version.setId(10);
        version.setVersion(16);

        version = (PricePlanVersion)home.create(getContext(),version);

        // Look for a version that we know does not exist.
        {
            final PricePlanVersion nonexistantVersion =
                PricePlanSupport.getVersion(getContext(), 10, 13);

            assertNull(
                "The nonexistant version should be null.",
                nonexistantVersion);
        }

        // Look for the version we know exists.
        {
            final PricePlanVersion existingVersion =
                PricePlanSupport.getVersion(getContext(), 10, 16);

            assertNotNull(
                "The existing version should not be null.",
                existingVersion);

            assertEquals("The looked-up version.", version, existingVersion);
        }
    }


    /**
     *
     */
    public void testIsSubscriberPricePlanUpdating()
        throws HomeException
    {
        try
        {
            PricePlanSupport.isSubscriberPricePlanUpdating(null, "");

            fail("Null context parameter should raise exception.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }
        
        try
        {
            PricePlanSupport.isSubscriberPricePlanUpdating(
                getContext(),
                (String)null);

            fail("Null identifier parameter should raise exception.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            PricePlanSupport.isSubscriberPricePlanUpdating(getContext(), "");

            fail("No home in context should raise exception.");
        }
        catch (final HomeException exception)
        {
            // EMPTY
        }

        final Home home = new PricePlanVersionUpdateRequestTransientHome(getContext());
        getContext().put(PricePlanVersionUpdateRequestHome.class, home);

        PricePlanVersionUpdateRequest request =
            new PricePlanVersionUpdateRequest();
        
        request.setSubscriberIdentifier("010101-0101");

        request = (PricePlanVersionUpdateRequest)home.create(getContext(),request);

        assertNotNull("The created request", request);

        // Look for a request that we know does not exist.
        {
            final boolean isUpdating =
                PricePlanSupport.isSubscriberPricePlanUpdating(
                    getContext(),
                    "FailureID");

            assertFalse(
                "Should not indicate that the subscriber is updating",
                isUpdating);
        }
        
        // Look for the request that we know exists.
        {
            final boolean isUpdating =
                PricePlanSupport.isSubscriberPricePlanUpdating(
                    getContext(),
                    "010101-0101");

            assertTrue(
                "Should indicate that the subscriber is updating",
                isUpdating);
        }
    }


    /**
     *
     */
    public void testIsPricePlanVersionUpdating()
        throws HomeException
    {
        try
        {
            PricePlanSupport.isPricePlanVersionUpdating(null, 6);

            fail("Null context parameter should raise exception.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }
        
        try
        {
            PricePlanSupport.isPricePlanVersionUpdating(getContext(), 6);

            fail("No home in context should raise exception.");
        }
        catch (final HomeException exception)
        {
            // EMPTY
        }
        
        final Home home = new PricePlanVersionUpdateRequestTransientHome(getContext());
        getContext().put(PricePlanVersionUpdateRequestHome.class, home);

        PricePlanVersionUpdateRequest request =
            new PricePlanVersionUpdateRequest();
        
        request.setSubscriberIdentifier("010101-0101");
        request.setPricePlanIdentifier(32);
        
        request = (PricePlanVersionUpdateRequest)home.create(getContext(),request);

        assertNotNull("The created request", request);

        // Look for a request that we know does not exist.
        {
            final boolean isUpdating =
                PricePlanSupport.isPricePlanVersionUpdating(
                    getContext(),
                    21);

            assertFalse(
                "Should not indicate that the plan is updating",
                isUpdating);
        }
        
        // Look for the request that we know exists.
        {
            final boolean isUpdating =
                PricePlanSupport.isPricePlanVersionUpdating(
                    getContext(),
                    32);

            assertTrue(
                "Should indicate that the plan is updating",
                isUpdating);
        }
    }

} // class
