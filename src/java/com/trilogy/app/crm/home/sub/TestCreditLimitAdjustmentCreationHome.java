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

package com.trilogy.app.crm.home.sub;

import java.security.Principal;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidTransientHome;
import com.trilogy.app.crm.bean.CreditLimitAdjustment;
import com.trilogy.app.crm.bean.CreditLimitAdjustmentHome;
import com.trilogy.app.crm.bean.CreditLimitAdjustmentTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.support.UserGroupSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;

/**
 * Unit test for <code>CreditLimitAdjustmentCreationHome</code>.
 *
 * @author cindy.wong@redknee.com
 */
public class TestCreditLimitAdjustmentCreationHome extends ContextAwareTestCase
{
    /**
     * The maximum value is currency value in a test case.
     */
    public static final long MAX_CURRENCY_VALUE = 20000L;

    /**
     * Constructs a test case with the given name.
     *
     * @param name
     *            Name of the test case
     */
    public TestCreditLimitAdjustmentCreationHome(final String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by standard JUnit tools (i.e.,
     * those that do not provide a context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by the Redknee Xtest code,
     * which provides the application's operating context.
     *
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestCreditLimitAdjustmentCreationHome.class);

        return suite;
    }

    /**
     * Sets up test license for auto deposit release.
     */
    protected final void setUpTestLicense()
    {
        UnitTestSupport.installLicenseManager(getContext());
        try
        {
            UnitTestSupport.createLicense(getContext(), UserGroupSupport.CREDIT_LIMIT_PERMISSION_LICENSE_KEY);
        }
        catch (HomeException exception)
        {
            fail(exception.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        super.setUp();
        {
            getContext().put(CRMSpidHome.class, new CRMSpidTransientHome(getContext()));
            serviceProvider_.setSpid(1);
            try
            {
                ((Home) getContext().get(CRMSpidHome.class)).create(getContext(), serviceProvider_);
            }
            catch (HomeException exception)
            {
                fail(exception.getMessage());
            }
        }

        {
            subscriber_.setId("123-1");
            subscriber_.setSpid(serviceProvider_.getSpid());
            subscriber_.setCreditLimit(getRandomCurrencyValue());
            getContext().put(SubscriberHome.class,
                new CreditLimitAdjustmentCreationHome(getContext(), new SubscriberTransientHome(getContext())));
            try
            {
                ((Home) getContext().get(SubscriberHome.class)).create(getContext(), subscriber_);
            }
            catch (HomeException exception)
            {
                fail(exception.getMessage());
            }
        }

        getContext().put(CreditLimitAdjustmentHome.class, new CreditLimitAdjustmentTransientHome(getContext()));

        group_.setName("Test Group");
        getContext().put(Group.class, group_);

        user_.setId("Test User");
        user_.setSpid(serviceProvider_.getSpid());
        user_.setGroup(group_.getName());
        getContext().put(Principal.class, user_);
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitAdjustmentCreationHome#store} when credit limit is
     * changed.
     *
     * @throws HomeException
     *             Thrown by Home.
     */
    public final void testCreditLimitChange() throws HomeException
    {
        setUpTestLicense();

        final Subscriber subscriber = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(),
            subscriber_.getId());
        final long oldLimit = subscriber.getCreditLimit(getContext());
        final long newLimit = oldLimit + getRandomCurrencyValue();
        subscriber.setCreditLimit(newLimit);
        ((Home) getContext().get(SubscriberHome.class)).store(subscriber);
        final Subscriber storedSubscriber = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(
            getContext(), subscriber_.getId());
        assertEquals("Subscriber credit limit should be changed", newLimit, storedSubscriber.getCreditLimit(getContext()));

        final Collection collection = ((Home) getContext().get(CreditLimitAdjustmentHome.class))
            .selectAll(getContext());
        assertEquals("Entry must be created", 1, collection.size());
        for (Object object : collection)
        {
            final CreditLimitAdjustment entry = (CreditLimitAdjustment) object;
            assertEquals("User does not match", user_.getName(), entry.getAgent());
            assertEquals("Old credit limit does not match", oldLimit, entry.getOldCreditLimit());
            assertEquals("New credit limit does not match", newLimit, entry.getNewCreditLimit());
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitAdjustmentCreationHome#store} when credit limit is
     * not changed.
     *
     * @throws HomeException
     *             Thrown by Home.
     */
    public final void testCreditLimitUnchanged() throws HomeException
    {
        setUpTestLicense();

        final Collection oldCollection = ((Home) getContext().get(CreditLimitAdjustmentHome.class))
            .selectAll(getContext());

        final Subscriber subscriber = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(),
            subscriber_.getId());
        final long oldLimit = subscriber.getCreditLimit(getContext());
        subscriber.setDeposit(getRandomCurrencyValue());
        ((Home) getContext().get(SubscriberHome.class)).store(subscriber);
        final Subscriber storedSubscriber = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(
            getContext(), subscriber_.getId());
        assertEquals("Subscriber credit limit should be unchanged", oldLimit, storedSubscriber.getCreditLimit(getContext()));

        final Collection newCollection = ((Home) getContext().get(CreditLimitAdjustmentHome.class))
            .selectAll(getContext());
        assertEquals("Entry must be not created", oldCollection.size(), newCollection.size());
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitAdjustmentCreationHome#store} when this feature is
     * disabled in the context.
     *
     * @throws HomeException
     *             Thrown by Home.
     */
    public final void testCreditLimitPermissionDisabled() throws HomeException
    {
        setUpTestLicense();

        UserGroupSupport.disableCreditLimitPermission(getContext());
        final Collection oldCollection = ((Home) getContext().get(CreditLimitAdjustmentHome.class))
            .selectAll(getContext());

        final Subscriber subscriber = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(),
            subscriber_.getId());
        final long oldLimit = subscriber.getCreditLimit(getContext());
        final long newLimit = oldLimit + getRandomCurrencyValue();
        subscriber.setCreditLimit(newLimit);
        ((Home) getContext().get(SubscriberHome.class)).store(subscriber);
        final Subscriber storedSubscriber = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(
            getContext(), subscriber_.getId());
        assertEquals("Subscriber credit limit should be changed", newLimit, storedSubscriber.getCreditLimit(getContext()));

        final Collection newCollection = ((Home) getContext().get(CreditLimitAdjustmentHome.class))
            .selectAll(getContext());
        assertEquals("Entry must be not created", oldCollection.size(), newCollection.size());
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitAdjustmentCreationHome#store} when this feature is
     * explicitly enabled in the context.
     *
     * @throws HomeException
     *             Thrown by Home.
     */
    public final void testCreditLimitPermissionEnabled() throws HomeException
    {
        setUpTestLicense();

        UserGroupSupport.enableCreditLimitPermission(getContext());
        final Collection oldCollection = ((Home) getContext().get(CreditLimitAdjustmentHome.class))
            .selectAll(getContext());

        final Subscriber subscriber = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(),
            subscriber_.getId());
        final long oldLimit = subscriber.getCreditLimit(getContext());
        final long newLimit = oldLimit + getRandomCurrencyValue();
        subscriber.setCreditLimit(newLimit);
        ((Home) getContext().get(SubscriberHome.class)).store(subscriber);
        final Subscriber storedSubscriber = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(
            getContext(), subscriber_.getId());
        assertEquals("Subscriber credit limit should be changed", newLimit, storedSubscriber.getCreditLimit(getContext()));

        final Collection newCollection = ((Home) getContext().get(CreditLimitAdjustmentHome.class))
            .selectAll(getContext());
        assertEquals("Entry must be created", oldCollection.size() + 1, newCollection.size());
        for (Object object : newCollection)
        {
            final CreditLimitAdjustment entry = (CreditLimitAdjustment) object;
            assertEquals("User does not match", user_.getName(), entry.getAgent());
            assertEquals("Old credit limit does not match", oldLimit, entry.getOldCreditLimit());
            assertEquals("New credit limit does not match", newLimit, entry.getNewCreditLimit());
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitAdjustmentCreationHome#store} when the license is
     * not installed.
     *
     * @throws HomeException
     *             Thrown by Home.
     */
    public void testLicenseDisabled() throws HomeException
    {
        UnitTestSupport.installLicenseManager(getContext());

        final Collection oldCollection = ((Home) getContext().get(CreditLimitAdjustmentHome.class))
            .selectAll(getContext());

        final Subscriber subscriber = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(),
            subscriber_.getId());
        final long oldLimit = subscriber.getCreditLimit(getContext());
        final long newLimit = oldLimit + getRandomCurrencyValue();
        subscriber.setCreditLimit(newLimit);
        ((Home) getContext().get(SubscriberHome.class)).store(subscriber);
        final Subscriber storedSubscriber = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(
            getContext(), subscriber_.getId());
        assertEquals("Subscriber credit limit should be changed", newLimit, storedSubscriber.getCreditLimit(getContext()));

        final Collection newCollection = ((Home) getContext().get(CreditLimitAdjustmentHome.class))
            .selectAll(getContext());
        assertEquals("Entry must be not created", oldCollection.size(), newCollection.size());
    }

    /**
     * Generates a random currency value.
     *
     * @return A random currency value within the range of 0 to MAX_CURRENCY_VALUE.
     */
    private static long getRandomCurrencyValue()
    {
        return Math.round(Math.random() * MAX_CURRENCY_VALUE);
    }

    /**
     * Subscriber for testing.
     */
    private final Subscriber subscriber_ = new Subscriber();

    /**
     * Service provider for testing.
     */
    private final CRMSpid serviceProvider_ = new CRMSpid();

    /**
     * User group for testing.
     */
    private final CRMGroup group_ = new CRMGroup();

    /**
     * User for testing.
     */
    private final User user_ = new User();
}
