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

import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseConfigurationEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.deposit.DepositReleaseTestCase;
import com.trilogy.app.crm.deposit.DepositReleaseTestSupport;

/**
 * Test suite for <code>AutoDepositReleaseScheduleHome</code>.
 *
 * @author cindy.wong@redknee.com
 */
public class TestAutoDepositReleaseScheduleHome extends DepositReleaseTestCase
{
    /**
     * The acceptable time difference when comparing two dates.
     */
    public static final long ACCEPTABLE_TIME_DIFFERENCE = 5000L;

    /**
     * Error message when subscriber is not found.
     */
    public static final String SUBSCRIBER_NOT_FOUND_MESSAGE = "Subscriber not found";

    /**
     * Error message when deposit does not match expected value.
     */
    public static final String DEPOSIT_NOT_MATCH_MESSAGE = "Deposit amount does not match expected value";

    /**
     * Error message when deposit date does not match expected value.
     */
    public static final String DEPOSIT_DATE_NOT_MATCH_MESSAGE = "Deposit date does not match expected value";

    /**
     * Error message when next deposit release date does not match expected value.
     */
    public static final String NEXT_DEPOSIT_RELEASE_DATE_NOT_MATCH_MESSAGE =
        "Next deposit release date does not match expected value";

    /**
     * Error message when next deposit release date is not null when it should be.
     */
    public static final String NEXT_DEPOSIT_RELEASE_DATE_NOT_NULL_MESSAGE = "Next deposit release date should be null";

    /**
     * A date for reference.
     */
    private final Calendar referenceCalendar_ = Calendar.getInstance();

    /**
     * Auto deposit criteria of SPID.
     */
    private AutoDepositReleaseCriteria spidCriteria_;

    /**
     * Auto depsoit criteria of credit category.
     */
    private AutoDepositReleaseCriteria creditCategoryCriteria_;

    /**
     * Credit category not using custom criteria.
     */
    private CreditCategory creditCategoryWithoutCriteria_;

    /**
     * Credit category with custom criteria active.
     */
    private CreditCategory creditCategoryWithCriteria_;

    /**
     * SPID used for testing.
     */
    private CRMSpid spid_;

    /**
     * Account in credit category without custom criteria.
     */
    private Account accountWithoutCreditCategoryCriteria_;

    /**
     * Account in credit category with custom criteria.
     */
    private Account accountWithCreditCategoryCriteria_;

    /**
     * Constructs a test case with the given name.
     *
     * @param name
     *            Name of the test case
     */
    public TestAutoDepositReleaseScheduleHome(final String name)
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

        final TestSuite suite = new TestSuite(TestAutoDepositReleaseScheduleHome.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
        super.setUp();
        setUpCriteria();

        spid_ = DepositReleaseTestSupport.createSpid(1, true, spidCriteria_.getIdentifier());
        createBean(spid_);

        setUpCreditCategory();
        setUpAccount();

        // add decorator
        {
            getContext().put(SubscriberHome.class,
                new AutoDepositReleaseScheduleHome(getContext(), (Home) getContext().get(SubscriberHome.class)));
        }
        super.setUpTestLicense();
    }

    /**
     * Sets up accounts for testing.
     */
    private void setUpAccount()
    {
        final int billCycle = 1;
        // add an account in credit category without custom criteria
        {
            accountWithoutCreditCategoryCriteria_ = DepositReleaseTestSupport.createAccount(spid_.getId(),
                creditCategoryWithoutCriteria_.getCode(), billCycle);
            createBean(accountWithoutCreditCategoryCriteria_);
        }

        // add an account in credit category with custom criteria
        {
            accountWithCreditCategoryCriteria_ = DepositReleaseTestSupport.createAccount(spid_.getSpid(),
                creditCategoryWithCriteria_.getCode(), billCycle);
            createBean(accountWithCreditCategoryCriteria_);
        }
    }

    /**
     * Sets up credit categories for testing.
     */
    private void setUpCreditCategory()
    {
        final int code1 = 1;
        final int code2 = 3;

        // add a credit category without custom criteria
        creditCategoryWithoutCriteria_ = DepositReleaseTestSupport.createCreditCategory(code1, spid_.getSpid(), false,
            0);
        createBean(creditCategoryWithoutCriteria_);

        // add a credit category with custom criteria
        creditCategoryWithCriteria_ = DepositReleaseTestSupport.createCreditCategory(code2, spid_.getSpid(), true,
            creditCategoryCriteria_.getIdentifier());
        createBean(creditCategoryWithCriteria_);
    }

    /**
     * Sets up a SPID with auto deposit enabled.
     */
    private void setUpSpidWithAutoDepositRelease()
    {
        spid_.setUseAutoDepositRelease(AutoDepositReleaseConfigurationEnum.YES);
        storeBean(spid_);
    }

    /**
     * Sets up a SPID with auto deposit disabled.
     */
    private void setUpSpidWithoutAutoDepositRelease()
    {
        spid_.setUseAutoDepositRelease(AutoDepositReleaseConfigurationEnum.NO);
        storeBean(spid_);
    }

    /**
     * Sets up auto deposit release criteria for testing.
     */
    private void setUpCriteria()
    {
        final double percent = 100.0;
        final long min = 100L;
        final int schedule = 15;
        final int duration1 = 60;
        final int duration2 = 120;

        // add a criteria for spid
        spidCriteria_ = DepositReleaseTestSupport.createCriteria(1L, AdjustmentTypeEnum.DepositRelease_INDEX,
            percent, min, schedule, true, duration1);
        createBean(spidCriteria_);

        // add a criteria for credit category
        creditCategoryCriteria_ = DepositReleaseTestSupport.createCriteria(2L,
            AdjustmentTypeEnum.DepositRelease_INDEX, percent, min, schedule, false, duration2);
        createBean(creditCategoryCriteria_);
    }

    /**
     * Test if next deposit release date is properly created upon creation of a subscriber whose credit category does
     * not have a custom criteria.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testSubscriberCreationWithoutCreditCategory() throws HomeException
    {
        // set up spid
        setUpSpidWithAutoDepositRelease();

        final long deposit = 10000L;
        final String msisdn = "1234567";
        final Subscriber original = DepositReleaseTestSupport.createSubscriber(accountWithoutCreditCategoryCriteria_,
            deposit, null);
        original.setMSISDN(msisdn);
        original.setDepositDate(referenceCalendar_.getTime());
        createBean(original);

        Subscriber retrieved = null;

        // calculate the proper date
        final Calendar expectedCalendar = (Calendar) referenceCalendar_.clone();
        expectedCalendar.add(Calendar.DAY_OF_MONTH, spidCriteria_.getServiceDuration());

        // retrieve subscriber
        retrieved = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(), original.getId());

        if (retrieved == null)
        {
            fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
        }
        else
        {
            assertEquals(DEPOSIT_NOT_MATCH_MESSAGE, original.getDeposit(getContext()), retrieved.getDeposit(getContext()));

            assertEquals(DEPOSIT_DATE_NOT_MATCH_MESSAGE, original.getDepositDate(), retrieved.getDepositDate());

            assertTrue(NEXT_DEPOSIT_RELEASE_DATE_NOT_MATCH_MESSAGE, Math.abs(expectedCalendar.getTimeInMillis()
                - retrieved.getNextDepositReleaseDate().getTime()) < ACCEPTABLE_TIME_DIFFERENCE);
        }
    }

    /**
     * Test if next deposit release date of a subscriber whose credit category does not have a custom criteria is
     * properly updated when deposit has changed.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testSubscriberDepositUpdateWithoutCreditCategory() throws HomeException
    {
        // set up spid
        setUpSpidWithAutoDepositRelease();

        final long deposit = 10000L;
        final String msisdn = "7654321";
        final Subscriber original = DepositReleaseTestSupport.createSubscriber(accountWithoutCreditCategoryCriteria_,
            deposit, null);
        original.setMSISDN(msisdn);
        original.setDepositDate(referenceCalendar_.getTime());
        createBean(original);

        Subscriber retrieved = null;
        Subscriber updated = null;

        // retrieve subscriber
        retrieved = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(), original.getId());

        if (retrieved == null)
        {
            fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
        }
        else
        {
            // update the subscriber
            final int updateDays = 100;
            final long updateDeposit = 500L;
            final Calendar updatedCalendar = (Calendar) referenceCalendar_.clone();
            updatedCalendar.add(Calendar.DAY_OF_MONTH, updateDays);
            final Calendar expectedCalendar = (Calendar) updatedCalendar.clone();
            expectedCalendar.add(Calendar.DAY_OF_MONTH, spidCriteria_.getServiceDuration());
            retrieved.setDeposit(updateDeposit);
            retrieved.setDepositDate(updatedCalendar.getTime());
            storeBean(retrieved);

            // retrieve the updated subscriber
            updated = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(), original.getId());
            if (updated == null)
            {
                fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
            }
            else
            {
                assertEquals(DEPOSIT_NOT_MATCH_MESSAGE, retrieved.getDeposit(getContext()), updated.getDeposit(getContext()));

                assertEquals(DEPOSIT_DATE_NOT_MATCH_MESSAGE, retrieved.getDepositDate(), updated.getDepositDate());

                assertTrue(NEXT_DEPOSIT_RELEASE_DATE_NOT_MATCH_MESSAGE, Math.abs(expectedCalendar.getTimeInMillis()
                    - updated.getNextDepositReleaseDate().getTime()) < ACCEPTABLE_TIME_DIFFERENCE);
            }
        }
    }

    /**
     * Test if next deposit release date is properly created upon creation of a subscriber whose credit category uses a
     * custom criteria.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testSubscriberCreationWithCreditCategory() throws HomeException
    {
        // set up spid
        setUpSpidWithAutoDepositRelease();

        final long deposit = 10000L;
        final String msisdn = "45678";
        final Subscriber original = DepositReleaseTestSupport.createSubscriber(accountWithCreditCategoryCriteria_,
            deposit, null);
        original.setMSISDN(msisdn);
        original.setDepositDate(referenceCalendar_.getTime());
        createBean(original);

        Subscriber retrieved = null;

        // calculate the proper date
        final Calendar expectedCalendar = (Calendar) referenceCalendar_.clone();
        expectedCalendar.add(Calendar.DAY_OF_MONTH, creditCategoryCriteria_.getServiceDuration());

        // retrieve subscriber
        retrieved = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(), original.getId());

        if (retrieved == null)
        {
            fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
        }
        else
        {
            assertEquals(DEPOSIT_NOT_MATCH_MESSAGE, original.getDeposit(getContext()), retrieved.getDeposit(getContext()));

            assertEquals(DEPOSIT_DATE_NOT_MATCH_MESSAGE, original.getDepositDate(), retrieved.getDepositDate());

            assertTrue(NEXT_DEPOSIT_RELEASE_DATE_NOT_MATCH_MESSAGE, Math.abs(expectedCalendar.getTimeInMillis()
                - retrieved.getNextDepositReleaseDate().getTime()) < ACCEPTABLE_TIME_DIFFERENCE);
        }
    }

    /**
     * Test if next deposit release date of a subscriber whose credit category uses a custom criteria is properly
     * updated when deposit has changed.
     *
     * @throws HomeException
     *             Thrown by Home.
     */
    public void testSubscriberDepositUpdateWithCreditCategory() throws HomeException
    {
        // set up spid
        setUpSpidWithAutoDepositRelease();

        final long deposit = 10000L;
        final String msisdn = "87654";
        final Subscriber original = DepositReleaseTestSupport.createSubscriber(accountWithCreditCategoryCriteria_,
            deposit, null);
        original.setMSISDN(msisdn);
        original.setDepositDate(referenceCalendar_.getTime());
        createBean(original);

        Subscriber retrieved = null;
        Subscriber updated = null;

        // retrieve subscriber
        retrieved = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(), original.getId());
        if (retrieved == null)
        {
            fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
        }
        else
        {
            final int updateDays = 100;
            final long updateDeposit = 500L;
            // update the subscriber
            final Calendar updatedCalendar = (Calendar) referenceCalendar_.clone();
            updatedCalendar.add(Calendar.DAY_OF_MONTH, updateDays);
            final Calendar expectedCalendar = (Calendar) updatedCalendar.clone();
            expectedCalendar.add(Calendar.DAY_OF_MONTH, creditCategoryCriteria_.getServiceDuration());
            retrieved.setDeposit(updateDeposit);
            retrieved.setDepositDate(updatedCalendar.getTime());
            storeBean(retrieved);

            // retrieve the updated subscriber
            updated = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(), original.getId());
            if (updated == null)
            {
                fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
            }
            else
            {
                assertEquals(DEPOSIT_NOT_MATCH_MESSAGE, retrieved.getDeposit(getContext()), updated.getDeposit(getContext()));

                assertEquals(DEPOSIT_DATE_NOT_MATCH_MESSAGE, retrieved.getDepositDate(), updated.getDepositDate());

                assertTrue(NEXT_DEPOSIT_RELEASE_DATE_NOT_MATCH_MESSAGE, Math.abs(expectedCalendar.getTimeInMillis()
                    - updated.getNextDepositReleaseDate().getTime()) < ACCEPTABLE_TIME_DIFFERENCE);
            }
        }
    }

    /**
     * Tests subscriber creation and update when auto deposit release is disabled at SPID.
     *
     * @throws HomeException
     *             Thrown by Home.
     */
    public void testSubscriberAutoDepositReleaseDisabled() throws HomeException
    {
        // set up spid with auto deposit release disabled
        setUpSpidWithoutAutoDepositRelease();

        final long deposit = 10000L;
        final String msisdn = "222222";
        final Subscriber original = DepositReleaseTestSupport.createSubscriber(accountWithoutCreditCategoryCriteria_,
            deposit, null);
        original.setMSISDN(msisdn);
        original.setDepositDate(referenceCalendar_.getTime());
        createBean(original);
        Subscriber retrieved = null;
        Subscriber updated = null;

        // retrieve subscriber
        retrieved = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(), original.getId());
        if (retrieved == null)
        {
            fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
        }
        else
        {
            // makes sure next deposit release date is not set
            assertEquals(NEXT_DEPOSIT_RELEASE_DATE_NOT_NULL_MESSAGE, null, retrieved.getNextDepositReleaseDate());

            // update the subscriber
            final int updateDays = 200;
            final long updateDeposit = 400L;

            final Calendar updatedCalendar = (Calendar) referenceCalendar_.clone();
            updatedCalendar.add(Calendar.DAY_OF_MONTH, updateDays);
            final Calendar expectedCalendar = (Calendar) updatedCalendar.clone();
            expectedCalendar.add(Calendar.DAY_OF_MONTH, creditCategoryCriteria_.getServiceDuration());
            retrieved.setDeposit(updateDeposit);
            retrieved.setDepositDate(updatedCalendar.getTime());
            storeBean(retrieved);

            // retrieve the updated subscriber
            updated = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).find(getContext(), original.getId());
            if (updated == null)
            {
                fail(SUBSCRIBER_NOT_FOUND_MESSAGE);
            }
            else
            {
                assertNull(NEXT_DEPOSIT_RELEASE_DATE_NOT_NULL_MESSAGE, retrieved.getNextDepositReleaseDate());

                assertEquals(DEPOSIT_NOT_MATCH_MESSAGE, retrieved.getDeposit(getContext()), updated.getDeposit(getContext()));

                assertTrue(DEPOSIT_DATE_NOT_MATCH_MESSAGE, Math.abs(retrieved.getDepositDate().getTime()
                    - updated.getDepositDate().getTime()) < ACCEPTABLE_TIME_DIFFERENCE);
            }
        }
    }
}
