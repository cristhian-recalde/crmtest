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
import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CreditLimitAdjustment;
import com.trilogy.app.crm.bean.CreditLimitAdjustmentHome;
import com.trilogy.app.crm.bean.CreditLimitAdjustmentTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.UserGroupSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;

/**
 * Unit test for <code>CreditLimitPermissionValidator</code>.
 *
 * @author cindy.wong@redknee.com
 */
public class TestCreditLimitPermissionValidator extends ContextAwareTestCase
{
    /**
     * The maximum value is currency value in a test case.
     */
    public static final long MAX_CURRENCY_VALUE = 20000L;

    /**
     * Creates a new <code>TestCreditLimitPermissionValidator</code> test case.
     *
     * @param name
     *            Name of the test case.
     */
    public TestCreditLimitPermissionValidator(final String name)
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

        final TestSuite suite = new TestSuite(TestCreditLimitPermissionValidator.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void setUp()
    {
        super.setUp();

        UnitTestSupport.installLicenseManager(getContext());

        // set up group
        group_.setName("Test group");
        group_.setNumberOfCreditLimitAdjustments(5);
        group_.setDurationOfCreditLimitAdjustments(10);
        group_.setGroup(group_.getName());
        getContext().put(Group.class, group_);

        // set up user
        final User user = new User();
        user.setActivated(true);
        user.setEnabled(true);
        user.setGroup(group_.getName());
        user.setId("Test user");
        getContext().put(Principal.class, user);

        // add homes
        getContext().put(CreditLimitAdjustmentHome.class, new CreditLimitAdjustmentTransientHome(getContext()));
        getContext().put(SubscriberHome.class, new SubscriberTransientHome(getContext()));
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitPermissionValidator#validate}.
     *
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    public final void testValidateNewSubscriber() throws HomeException
    {
        UnitTestSupport.createLicense(getContext(), UserGroupSupport.CREDIT_LIMIT_PERMISSION_LICENSE_KEY);
        final Validator validator = CreditLimitPermissionValidator.getInstance();

        final Subscriber subscriber = new Subscriber();
        subscriber.setId("123-1");
        validator.validate(getContext(), subscriber);
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitPermissionValidator#validate} when License is
     * disabled.
     *
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    public final void testValidateLicenseDisabled() throws HomeException
    {
        validate(true, true, true);
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitPermissionValidator#validate}.
     *
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    public final void testValidateLimit() throws HomeException
    {
        UnitTestSupport.createLicense(getContext(), UserGroupSupport.CREDIT_LIMIT_PERMISSION_LICENSE_KEY);
        validate(true, false, true);
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitPermissionValidator#validate}.
     *
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    public final void testValidateDuration() throws HomeException
    {
        UnitTestSupport.createLicense(getContext(), UserGroupSupport.CREDIT_LIMIT_PERMISSION_LICENSE_KEY);

        final Validator validator = CreditLimitPermissionValidator.getInstance();
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -group_.getDurationOfCreditLimitAdjustments() - 1);
        final Date expiredDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        final Date expiryDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, group_.getDurationOfCreditLimitAdjustments() / 2);
        final Date unexpiredDate = calendar.getTime();
        long limit = getRandomCurrencyValue();

        final Subscriber subscriber = new Subscriber();
        subscriber.setId("123-1");
        subscriber.setCreditLimit(limit++);
        validator.validate(getContext(), subscriber);
        ((Home) getContext().get(SubscriberHome.class)).create(getContext(), subscriber);

        // create expired entries
        createEntries(subscriber, group_.getNumberOfCreditLimitAdjustments(), expiredDate, true);

        // create entry on expiry date
        createEntries(subscriber, group_.getNumberOfCreditLimitAdjustments(), expiryDate, true);

        // create entry on unexpired date
        createEntries(subscriber, group_.getNumberOfCreditLimitAdjustments(), unexpiredDate, true);

        subscriber.setCreditLimit(limit++);
        try
        {
            validator.validate(getContext(), subscriber);
            fail("Credit Limit change should be prohibited.");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Exception successfully thrown", exception).log(getContext());
        }
    }

    /**
     * Common logic to test {@link com.redknee.app.crm.home.sub.CreditLimitPermissionValidator#validate}.
     *
     * @param overLimit
     *            Whether the number of credit limit adjustment entries generated should be over the group's limit.
     * @param expectPass
     *            Whether this case is expected to pass or fail.
     * @param storeSubscriber
     *            Whether subscriber should be stored in home for valid entries.
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    private void validate(final boolean overLimit, final boolean expectPass, final boolean storeSubscriber)
        throws HomeException
    {
        final Validator validator = CreditLimitPermissionValidator.getInstance();
        final Date date = new Date();
        long limit = getRandomCurrencyValue();
        final Subscriber subscriber = new Subscriber();
        subscriber.setId("123-1");
        subscriber.setCreditLimit(limit++);
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        validator.validate(getContext(), subscriber);
        ((Home) getContext().get(SubscriberHome.class)).create(getContext(), subscriber);

        final int totalEntries = group_.getNumberOfCreditLimitAdjustments() - 1;

        createEntries(subscriber, totalEntries, date, storeSubscriber);

        if (overLimit)
        {
            createEntries(subscriber, 1, date, false);
        }

        subscriber.setCreditLimit(limit++);

        try
        {
            validator.validate(getContext(), subscriber);
            if (!expectPass)
            {
                fail("Credit Limit change should be prohibited.");
            }
        }
        catch (IllegalStateException exception)
        {
            if (!expectPass)
            {
                new DebugLogMsg(this, "Exception successfully thrown", exception).log(getContext());
            }
            else
            {
                throw exception;
            }
        }
    }

    /**
     * Create credit limit adjustment history entries for the provided subscriber.
     *
     * @param subscriber
     *            The subscriber to create entry for.
     * @param totalEntries
     *            Total number of entries to create.
     * @param date
     *            The date these entries should bet set to.
     * @param storeSubscriber
     *            Whether the subscriber should be stored in the home after each entry is created.
     * @throws HomeException
     *             Thrown by home.
     */
    private void createEntries(final Subscriber subscriber, final int totalEntries, final Date date,
        final boolean storeSubscriber) throws HomeException
    {
        long limit = getRandomCurrencyValue();

        // create entries
        for (int i = 0; i < totalEntries; i++)
        {
            subscriber.setCreditLimit(limit++);
            CreditLimitPermissionValidator.getInstance().validate(getContext(), subscriber);

            final CreditLimitAdjustment adjustment = new CreditLimitAdjustment();
            adjustment.setAgent(((User) getContext().get(Principal.class)).getId());
            adjustment.setAdjustmentDate(date);
            adjustment.setIdentifier(identifier_++);
            ((Home) getContext().get(CreditLimitAdjustmentHome.class)).create(getContext(), adjustment);
            if (storeSubscriber)
            {
                ((Home) getContext().get(SubscriberHome.class)).store(getContext(), subscriber);
            }
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitPermissionValidator#validate} when credit limit
     * permission feature is disabled in context.
     *
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    public final void testDisabledInContext() throws HomeException
    {
        UnitTestSupport.createLicense(getContext(), UserGroupSupport.CREDIT_LIMIT_PERMISSION_LICENSE_KEY);
        UserGroupSupport.disableCreditLimitPermission(getContext());
        validate(true, true, true);
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitPermissionValidator#validate} when credit limit
     * permission feature is enabled in context.
     *
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    public final void testEnabledInContext() throws HomeException
    {
        UnitTestSupport.createLicense(getContext(), UserGroupSupport.CREDIT_LIMIT_PERMISSION_LICENSE_KEY);
        UserGroupSupport.enableCreditLimitPermission(getContext());
        validate(true, false, true);
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.CreditLimitPermissionValidator#validate} when subscriber type
     * is updated.
     *
     * @throws HomeException Thrown if there are home-related problems
     */
    public final void testSubscriberTypeChanged() throws HomeException
    {
        UnitTestSupport.createLicense(getContext(), UserGroupSupport.CREDIT_LIMIT_PERMISSION_LICENSE_KEY);
        final Validator validator = CreditLimitPermissionValidator.getInstance();
        final Date date = new Date();
        long limit = getRandomCurrencyValue();
        final Subscriber subscriber = new Subscriber();
        subscriber.setId("123-1");
        subscriber.setCreditLimit(limit++);
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        validator.validate(getContext(), subscriber);
        ((Home) getContext().get(SubscriberHome.class)).create(getContext(), subscriber);

        final int totalEntries = group_.getNumberOfCreditLimitAdjustments() - 1;

        createEntries(subscriber, totalEntries, date, true);

        // create some more
        createEntries(subscriber, 1, date, false);

        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setCreditLimit(limit++);
        validator.validate(getContext(), subscriber);
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
     * Group for testing.
     */
    private CRMGroup group_ = new CRMGroup();

    /**
     * Credit Limit Adjustment identifier.
     */
    private long identifier_ = 1;
}
