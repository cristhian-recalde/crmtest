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
package com.trilogy.app.crm.bean;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.Before;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * Unit tests for Subscription.
 *
 * @author victor.stratan@redknee.com
 */
public class TestSubscription extends ContextAwareTestCase
{
    /**
     * Creates a new TestService.
     *
     * @param name The name of the set of tests.
     */
    public TestSubscription(final String name)
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

        final TestSuite suite = new TestSuite(TestSubscription.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    protected void setUp()
    {
        super.setUp();

        final Context ctx = getContext();

        subscriber_ = new Subscriber();
        subscriber_.setBAN(BAN);
        subscriber_.setId(SUB_ID);

        subscriber_.setContext(ctx);

        home_ = new AuxiliaryServiceTransientHome(ctx);
        ctx.put(AuxiliaryServiceHome.class, home_);

        home_ = new SubscriberAuxiliaryServiceTransientHome(ctx);
        ctx.put(SubscriberAuxiliaryServiceHome.class, home_);
    }

    /**
     * Tests that lazy loaded collection is empty if no auxiliary service are subscribed.
     */
    public void testAuxiliaryServicesLazyLoadEmplty()
    {
        final Collection auxServices = subscriber_.getAuxiliaryServices(getContext());
        assertEquals("AuxiliaryServices collection should be empty", 0, auxServices.size());
    }

    /**
     * Tests that lazy loaded collection is NOT empty if auxiliary service are subscribed.
     *
     * @throws HomeException if Framework code throws exception
     */
    public void testAuxiliaryServicesLazyLoadNonEmplty() throws HomeException
    {
        final AuxiliaryService auxService = new AuxiliaryService();
        auxService.setIdentifier(AUX_SRV_ID);
        auxService.setSpid(SPID);

        final SubscriberAuxiliaryService serviceSubscription = new SubscriberAuxiliaryService();
        serviceSubscription.setSubscriberIdentifier(SUB_ID);
        serviceSubscription.setAuxiliaryServiceIdentifier(AUX_SRV_ID);

        home_.create(getContext(), serviceSubscription);

        final Collection auxServices = subscriber_.getAuxiliaryServices(getContext());
        assertEquals("AuxiliaryServices collection should not be empty", 1, auxServices.size());
    }

    public static final int SPID = 2;
    public static final String BAN = "123";
    public static final String SUB_ID = BAN + "-4";
    public static final long AUX_SRV_ID = 12L;

    private Home home_;
    private Subscriber subscriber_;
}
