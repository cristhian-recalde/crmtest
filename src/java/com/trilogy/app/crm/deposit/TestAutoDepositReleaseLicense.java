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

package com.trilogy.app.crm.deposit;

import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.home.sub.AutoDepositReleaseScheduleHome;

/**
 * Tests behaviour of Auto Deposit Release classes when license is disabled.
 *
 * @author cindy.wong@redknee.com
 */
public class TestAutoDepositReleaseLicense extends DepositReleaseTestCase
{
    /**
     * Error message used when subscriber is not found in home.
     */
    public static final String SUBSCRIBER_NOT_FOUND_MESSAGE = "Subscriber not found in home";

    /**
     * Error message used when next deposit release date is not null when it should be.
     */
    public static final String NEXT_DEPOSIT_RELEASE_DATE_NOT_NULL_MESSAGE = "NextDepositReleaseDate should be null";

    /**
     * Error message used when next deposit release date was modified when it should not be.
     */
    public static final String NEXT_DEPOSIT_RELEASE_DATE_CHANGED_MESSAGE =
        "NextDepositReleaseDAte should remain unchanged";

    /**
     * Error message used when deposit balance was modified when it should not be.
     */
    public static final String DEPOSIT_CHANGED_MESSAGE = "Deposit should remain unchanged";

    /**
     * Error message used when deposit date is modified when it should not be.
     */
    public static final String DEPOSIT_DATE_CHANGED_MESSAGE = "Deposit date should remain unchanged";

    /**
     * SPID used for testing.
     */
    private CRMSpid spid_;

    /**
     * Criteria used for testing.
     */
    private AutoDepositReleaseCriteria criteria_;

    /**
     * Creates a new <code>TestAutoDepositReleaseLicense</code> object.
     *
     * @param name
     *            Name of the test suite
     */
    public TestAutoDepositReleaseLicense(final String name)
    {
        super(name);
    }

    /**
     * Sets up the environment for testing.
     *
     * @see com.redknee.app.crm.deposit.DepositReleaseTestCase#setUp()
     */
    @Override
    public final void setUp()
    {
        super.setUp();
        // create criteria
        criteria_ = DepositReleaseTestSupport.createCriteria(1L, 1, 1, 1, 1, true, 1);
        createBean(criteria_);

        // create spid
        spid_ = DepositReleaseTestSupport.createSpid(1, true, criteria_.getIdentifier());
        createBean(spid_);

        createBean(DepositReleaseTestSupport.createCreditCategory(1, 1, false, 1L));

        // add decorator to subscriber home
        final Home home = (Home) getContext().get(SubscriberHome.class);
        getContext().put(SubscriberHome.class, new AutoDepositReleaseScheduleHome(getContext(), home));
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by standard JUnit tools (i.e.,
     * those that do not provide a context).
     *
     * @return A new suite of Tests for execution
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
     *            The operating context
     * @return A new suite of Tests for execution
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestAutoDepositReleaseLicense.class);

        return suite;
    }

    /**
     * Tests subscriber creation.
     *
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    public final void testSubscriberCreate() throws HomeException
    {
        final long deposit = 1000;
        final Account account = DepositReleaseTestSupport.createAccount(spid_.getSpid(), 1, 1);
        final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, null);
        createBean(account);
        createBean(subscriber);

        // retrieves subscriber
        final Home home = (Home) getContext().get(SubscriberHome.class);
        final Subscriber retrieved = (Subscriber) home.find(subscriber.getId());

        if (retrieved == null)
        {
            fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
        }
        else
        {
            assertNull(NEXT_DEPOSIT_RELEASE_DATE_NOT_NULL_MESSAGE, retrieved.getNextDepositReleaseDate());
        }
    }

    /**
     * Tests subscriber update.
     *
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    public final void testSubscriberUpdate() throws HomeException
    {
        final Calendar calendar = Calendar.getInstance();
        final long deposit = 1000;
        final Account account = DepositReleaseTestSupport.createAccount(spid_.getSpid(), 1, 1);
        final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, null);
        createBean(account);
        createBean(subscriber);

        // update subscriber's deposit date
        final Calendar updatedCalendar = (Calendar) calendar.clone();
        updatedCalendar.add(Calendar.YEAR, 1);
        subscriber.setDepositDate(updatedCalendar.getTime());
        storeBean(subscriber);

        // retrieves updated subscriber
        final Home home = (Home) getContext().get(SubscriberHome.class);
        final Subscriber retrieved = (Subscriber) home.find(subscriber.getId());

        if (retrieved == null)
        {
            fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
        }
        else
        {
            assertNull(NEXT_DEPOSIT_RELEASE_DATE_NOT_NULL_MESSAGE, retrieved.getNextDepositReleaseDate());
            assertEquals(DEPOSIT_DATE_CHANGED_MESSAGE, retrieved.getDepositDate(), subscriber.getDepositDate());
            assertEquals(DEPOSIT_CHANGED_MESSAGE, retrieved.getDeposit(getContext()), subscriber.getDeposit(getContext()));
        }
    }

    /**
     * Tests subscriber update after license is disabled.
     *
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    public final void testSubscriberUpdateAfterDisabled() throws HomeException
    {
        final Calendar calendar = Calendar.getInstance();
        final long deposit = 1000;
        final Account account = DepositReleaseTestSupport.createAccount(spid_.getSpid(), 1, 1);
        final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit, calendar.getTime());
        subscriber.setDepositDate(calendar.getTime());
        createBean(account);
        createBean(subscriber);

        // retrieves subscriber
        Subscriber retrieved = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(subscriber.getId());

        if (retrieved == null)
        {
            fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
        }
        else
        {
            assertEquals(NEXT_DEPOSIT_RELEASE_DATE_CHANGED_MESSAGE, retrieved.getNextDepositReleaseDate(),
                    subscriber.getNextDepositReleaseDate());
        }

        // update deposit date
        final Calendar updateDate = (Calendar) calendar.clone();
        updateDate.add(Calendar.MONTH, 1);
        subscriber.setDepositDate(updateDate.getTime());
        storeBean(subscriber);

        // retrieve updated subscriber
        retrieved = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(subscriber.getId());

        if (retrieved == null)
        {
            fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
        }
        else
        {
            assertEquals(NEXT_DEPOSIT_RELEASE_DATE_CHANGED_MESSAGE, retrieved.getNextDepositReleaseDate(),
                    subscriber.getNextDepositReleaseDate());
            assertEquals(DEPOSIT_DATE_CHANGED_MESSAGE, retrieved.getDepositDate(), subscriber.getDepositDate());
            assertEquals(DEPOSIT_CHANGED_MESSAGE, retrieved.getDeposit(getContext()), subscriber.getDeposit(getContext()));
        }
    }

    /**
     * Tests if deposits are left untouched when auto deposit release license is not installed.
     *
     * @throws AgentException
     *             Thrown if there are problems visiting the home.
     */
    public final void testAutoDepositRelease() throws AgentException
    {
        final int numAccounts = (int) (Math.random() * 50);
        super.setUpAccounts(spid_, numAccounts, -1, -1);
        final Calendar activeDate = Calendar.getInstance();
        final SubscriberReleaseVisitor subscriberVisitor = new DummySubscriberReleaseVisitor();
        final DepositReleaseTransactionCreator transactionCreator = new DummyDepositReleaseTransactionCreator();
        new AutoDepositRelease(spid_, activeDate.getTime(), subscriberVisitor, transactionCreator)
            .execute(getContext());
        assertEquals("No accounts should have been visited", subscriberVisitor.getNumVisits(), 0);
    }
}
