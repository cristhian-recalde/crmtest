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

package com.trilogy.app.crm.home;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import junit.framework.JUnit4TestAdapter;

import org.junit.BeforeClass;
import org.junit.Test;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.home.account.AccountDatesValidator;

/**
 * Unit test for {@link DateValidator}.
 *
 * @author cindy.wong@redknee.com
 */
public class TestDateValidator
{

    /**
     * Returns a JUnit 3 test suite for backward compatiblity.
     *
     * @return A Junit 3 test suite for backward compatibility.
     */
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestDateValidator.class);
    }

    /**
     * Sets up the environment for testing.
     */
    @BeforeClass
    public static void setUpBeforeClass()
    {
        config = new GeneralConfig();
        validator = new DateValidator()
        {
            @Override
            public void validate(final Context ctx, final Object obj)
            {
                // empty
            }
        };
    }

    /**
     * Test method for {@link DateValidator#validatePrior} where the value {@link GeneralConfig#getValidDateYearsPrior}
     * is set to 0.
     */
    @Test
    public final void testValidatePriorZero()
    {
        config.setValidDateYearsPrior(0);
        final Account account = new Account();
        final Calendar calendar = Calendar.getInstance();
        account.setContractStartDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), null, account,
		    AccountXInfo.CONTRACT_START_DATE, config);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        account.setContractStartDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), null, account,
		    AccountXInfo.CONTRACT_START_DATE, config);

        calendar.setTimeInMillis(DateValidator.getBeginningOfDay().getTimeInMillis());
        account.setContractStartDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), null, account,
		    AccountXInfo.CONTRACT_START_DATE, config);
    }

    /**
     * Test method for {@link DateValidator#validatePrior} where {@link GeneralConfig#getValidDateYearsPrior} is set to
     * 0 and the date is out of range.
     */
    @Test(expected = IllegalPropertyArgumentException.class)
    public final void testValidatePriorZeroOutOfBounds()
    {
        config.setValidDateYearsPrior(0);
        final Account account = new Account();
        final Calendar calendar = DateValidator.getBeginningOfDay();
        calendar.add(Calendar.MILLISECOND, -1);
        account.setContractStartDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), null, account,
		    AccountXInfo.CONTRACT_START_DATE, config);
    }

    /**
     * Test method for {@link DateValidator#validatePrior} where the bean is new and the date is set to a valid date.
     */
    @Test
    public final void testValidatePriorNewBeanValidDate()
    {
        config.setValidDateYearsPrior(1);
        final Subscriber subscriber = new Subscriber();

        subscriber.setDepositDate(null);
		DateValidator.validatePrior(ContextLocator.locate(), null, subscriber,
		    SubscriberXInfo.DEPOSIT_DATE, config);

        final Calendar calendar = Calendar.getInstance();
        subscriber.setDepositDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), null, subscriber,
		    SubscriberXInfo.DEPOSIT_DATE, config);

        calendar.add(Calendar.MONTH, -1);
        subscriber.setDepositDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), null, subscriber,
		    SubscriberXInfo.DEPOSIT_DATE, config);

        calendar.add(Calendar.YEAR, 2);
        subscriber.setDepositDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), null, subscriber,
		    SubscriberXInfo.DEPOSIT_DATE, config);

        calendar.setTimeInMillis(DateValidator.getBeginningOfDay().getTimeInMillis());
        calendar.add(Calendar.YEAR, -1);
        subscriber.setDepositDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), null, subscriber,
		    SubscriberXInfo.DEPOSIT_DATE, config);
    }

    /**
     * Test method for {@link DateValidator#validatePrior} where the date is invalid.
     */
    @Test(expected = IllegalPropertyArgumentException.class)
    public final void testValidatePriorNewBeanOutOfBounds()
    {
        config.setValidDateYearsPrior(2);
        final CRMSpid spid = new CRMSpid();

        final Calendar calendar = DateValidator.getBeginningOfDay();
        calendar.add(Calendar.YEAR, -config.getValidDateYearsPrior());
        calendar.add(Calendar.MILLISECOND, -1);
        spid.setEffectiveDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), null, spid,
		    CRMSpidXInfo.EFFECTIVE_DATE, config);
    }

    /**
     * Test method for {@link DateValidator#validatePrior} where the date has not been modified and is valid.
     */
    @Test
    public final void testValidatePriorUnchangedValidDate()
    {
        config.setValidDateYearsPrior(1);
        final Account account = new Account();

        final Calendar calendar = Calendar.getInstance();
        account.setContractEndDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), account, account,
		    AccountXInfo.CONTRACT_END_DATE, config);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        account.setContractEndDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), account, account,
		    AccountXInfo.CONTRACT_END_DATE, config);

        calendar.add(Calendar.MONTH, -1);
        account.setContractEndDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), account, account,
		    AccountXInfo.CONTRACT_END_DATE, config);

        calendar.setTimeInMillis(DateValidator.getBeginningOfDay().getTimeInMillis());
        calendar.add(Calendar.YEAR, -1);
        account.setContractEndDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), account, account,
		    AccountXInfo.CONTRACT_END_DATE, config);
    }

    /**
     * Test method for {@link DateValidator#validatePrior} is out of range but is not modified.
     */
    @Test
    public final void testValidatePriorUnchangedInvalidDate()
    {
        config.setValidDateYearsPrior(1);
        final Account account = new Account();

        account.setContractEndDate(null);
		DateValidator.validatePrior(ContextLocator.locate(), account, account,
		    AccountXInfo.CONTRACT_END_DATE, config);

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        account.setContractEndDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), account, account,
		    AccountXInfo.CONTRACT_END_DATE, config);
    }

    /**
     * Test method for {@link DateValidator#validatePrior} when the date is modified to a valid date.
     */
    @Test
    public final void testValidatePriorChangedValidDate()
    {
        config.setValidDateYearsPrior(1);
        final Subscriber newSubscriber = new Subscriber();
        final Subscriber oldSubscriber = new Subscriber();
        final Calendar calendar = Calendar.getInstance();
        final Calendar oldCalendar = Calendar.getInstance();

        oldSubscriber.setDepositDate(oldCalendar.getTime());
        newSubscriber.setDepositDate(null);
		DateValidator.validatePrior(ContextLocator.locate(), oldSubscriber,
		    newSubscriber, SubscriberXInfo.DEPOSIT_DATE, config);

        oldCalendar.add(Calendar.YEAR, config.getValidDateYearsPrior() - 1);
        oldSubscriber.setDepositDate(oldCalendar.getTime());
        newSubscriber.setDepositDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), oldSubscriber,
		    newSubscriber, SubscriberXInfo.DEPOSIT_DATE, config);

        calendar.add(Calendar.MONTH, -1);
        oldSubscriber.setDepositDate(oldCalendar.getTime());
        newSubscriber.setDepositDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), oldSubscriber,
		    newSubscriber, SubscriberXInfo.DEPOSIT_DATE, config);

        calendar.add(Calendar.YEAR, 2);
        newSubscriber.setDepositDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), oldSubscriber,
		    newSubscriber, SubscriberXInfo.DEPOSIT_DATE, config);

        calendar.setTimeInMillis(DateValidator.getBeginningOfDay().getTimeInMillis());
        calendar.add(Calendar.YEAR, -1);
        newSubscriber.setDepositDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), oldSubscriber,
		    newSubscriber, SubscriberXInfo.DEPOSIT_DATE, config);
    }

    /**
     * Test method for {@link DateValidator#validatePrior} where the date is changed and is invalid.
     */
    @Test(expected = IllegalPropertyArgumentException.class)
    public final void testValidatePriorChangedOutOfRange()
    {
        config.setValidDateYearsPrior(2);
        final CRMSpid newSpid = new CRMSpid();
        final CRMSpid oldSpid = new CRMSpid();

        final Calendar calendar = DateValidator.getBeginningOfDay();
        calendar.add(Calendar.YEAR, -config.getValidDateYearsPrior());
        calendar.add(Calendar.MILLISECOND, -1);
        newSpid.setEffectiveDate(calendar.getTime());
		DateValidator.validatePrior(ContextLocator.locate(), oldSpid, newSpid,
		    CRMSpidXInfo.EFFECTIVE_DATE, config);
    }

    /**
     * Test method for {@link DateValidator#validateAfter}.
     */
    @Test
    public final void testValidateAfter()
    {
        final int days = 10;
        config.setValidDateDaysAfter(days);
        final Subscriber subscriber = new Subscriber();
        Calendar calendar = Calendar.getInstance();

        // PASS case : date is today
        subscriber.setStartDate(calendar.getTime());
		DateValidator.validateAfter(ContextLocator.locate(), null, subscriber,
		    SubscriberXInfo.START_DATE, config);

        // PASS case : date is in the past
        calendar.add(Calendar.MONTH, -1);
        subscriber.setStartDate(calendar.getTime());
		DateValidator.validateAfter(ContextLocator.locate(), null, subscriber,
		    SubscriberXInfo.START_DATE, config);

        // PASS case : date is 10 days after today
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        subscriber.setStartDate(calendar.getTime());
		DateValidator.validateAfter(ContextLocator.locate(), null, subscriber,
		    SubscriberXInfo.START_DATE, config);
    }

    /**
     * Test method for {@link DateValidator#validateAfter} where the date is out of bounds.
     */
    @Test(expected = IllegalPropertyArgumentException.class)
    public final void testValidateAfterOutOfRange()
    {
        final int days = 10;
        config.setValidDateDaysAfter(days);
        final Subscriber subscriber = new Subscriber();
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        calendar.add(Calendar.MILLISECOND, 1);
        subscriber.setStartDate(calendar.getTime());
		DateValidator.validateAfter(ContextLocator.locate(), null, subscriber,
		    SubscriberXInfo.START_DATE, config);
    }

    /**
     * Test method for {@link AccountDatesValidator#validateDateOfBirth} where the maximum age is tested.
     */
    @Test
    public final void testValidateDateOfBirthMaximumAge()
    {
        final Account account = new Account();
        final Calendar calendar = Calendar.getInstance();
        final int maxAge = 10;
        config.setMinimumAge(1);
        config.setMaximumAge(maxAge);

        // PASS case : oldest possible
        calendar.add(Calendar.YEAR, -config.getMaximumAge() - 1);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        account.setDateOfBirth(calendar.getTime());
        AccountDatesValidator.validateDateOfBirth(null, -1, null, null, account, AccountXInfo.DATE_OF_BIRTH);
    }

    /**
     * Test method for {@link AccountDatesValidator#validateDateOfBirth} where the age is over the maximum allowed age.
     */
    @Test(expected = IllegalPropertyArgumentException.class)
    public final void testValidateDateOfBirthTooOld()
    {
        final Account account = new Account();
        final Calendar calendar = Calendar.getInstance();
        final int maxAge = 10;
        config.setMinimumAge(1);
        config.setMaximumAge(maxAge);

        // FAIL case : older than allowed
        calendar.add(Calendar.YEAR, -config.getMaximumAge() - 1);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        account.setDateOfBirth(calendar.getTime());
        AccountDatesValidator.validateDateOfBirth(null, -1, null, null, account, AccountXInfo.DATE_OF_BIRTH);
    }

    /**
     * Test method for {@link AccountDatesValidator#validateDateOfBirth} where maximum and minimum ages are same.
     */
    @Test
    public final void testValidateDateOfBirthSameMaxMin()
    {
        final Account account = new Account();
        Calendar calendar = Calendar.getInstance();
        final int maxAge = 10;
        config.setMinimumAge(maxAge);
        config.setMaximumAge(maxAge);

        // max age
        calendar.add(Calendar.YEAR, -config.getMaximumAge() - 1);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        account.setDateOfBirth(calendar.getTime());
        AccountDatesValidator.validateDateOfBirth(null, -1, null, null, account, AccountXInfo.DATE_OF_BIRTH);

        // min age
        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -config.getMinimumAge());
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        account.setDateOfBirth(calendar.getTime());
        AccountDatesValidator.validateDateOfBirth(null, -1, null, null, account, AccountXInfo.DATE_OF_BIRTH);
    }

    /**
     * Test method for {@link AccountDatesValidator#validateDateOfBirth} where maximum and minimum ages are same, and the age
     * provided is too old.
     */
    @Test(expected = IllegalPropertyArgumentException.class)
    public final void testValidateDateOfBirthSameMaxMinTooOld()
    {
        final Account account = new Account();
        final Calendar calendar = Calendar.getInstance();
        final int maxAge = 10;
        config.setMinimumAge(maxAge);
        config.setMaximumAge(maxAge);
        calendar.add(Calendar.YEAR, -config.getMaximumAge() - 1);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        account.setDateOfBirth(calendar.getTime());
        AccountDatesValidator.validateDateOfBirth(null, -1, null, null, account, AccountXInfo.DATE_OF_BIRTH);
    }

    /**
     * Test method for {@link AccountDatesValidator#validateDateOfBirth} where the minimum age is tested.
     */
    @Test
    public final void testValidateDateOfBirthMinimumAge()
    {
        final Account account = new Account();
        final Calendar calendar = Calendar.getInstance();
        final int maxAge = 10;

        // PASS case : yongest possible
        config.setMinimumAge(1);
        config.setMaximumAge(maxAge);
        calendar.add(Calendar.YEAR, -1);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        account.setDateOfBirth(calendar.getTime());
        AccountDatesValidator.validateDateOfBirth(null, -1, null, null, account, AccountXInfo.DATE_OF_BIRTH);
    }

    /**
     * Test method for {@link AccountDatesValidator#validateDateOfBirth} where the age is too young.
     */
    @Test(expected = IllegalPropertyArgumentException.class)
    public final void testValidateDateOfBirthMinimumAgeTooYoung()
    {
        final Account account = new Account();
        final Calendar calendar = Calendar.getInstance();
        final int maxAge = 10;
        config.setMinimumAge(1);
        config.setMaximumAge(maxAge);
        calendar.add(Calendar.YEAR, -1);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        calendar.add(Calendar.MILLISECOND, 1);
        account.setDateOfBirth(calendar.getTime());
        AccountDatesValidator.validateDateOfBirth(null, -1, null, null, account, AccountXInfo.DATE_OF_BIRTH);
    }

    /**
     * Test method for {@link AccountDatesValidator#validateDateOfBirth} where the maximum and minimum ages are the same, and
     * the age is too young.
     */
    @Test(expected = IllegalPropertyArgumentException.class)
    public final void testValidateDateOfBirthSameMaxMinTooYoung()
    {
        final Account account = new Account();
        final Calendar calendar = Calendar.getInstance();
        final int maxAge = 10;
        config.setMinimumAge(maxAge);
        config.setMaximumAge(maxAge);
        calendar.add(Calendar.YEAR, -config.getMinimumAge());
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        account.setDateOfBirth(calendar.getTime());
        AccountDatesValidator.validateDateOfBirth(null, -1, null, null, account, AccountXInfo.DATE_OF_BIRTH);

        // FAIL case : yonger than allowed
        calendar.add(Calendar.MILLISECOND, 1);
        account.setDateOfBirth(calendar.getTime());
        AccountDatesValidator.validateDateOfBirth(null, -1, null, null, account, AccountXInfo.DATE_OF_BIRTH);
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.DateValidator#getEndOfDay()}.
     */
    @Test
    public final void testGetEndOfDay()
    {
        final Calendar calendar = Calendar.getInstance();
        final Calendar endOfDay = DateValidator.getEndOfDay();

        assertTrue(endOfDay.after(calendar));
        assertEquals(endOfDay.get(Calendar.YEAR), calendar.get(Calendar.YEAR));
        assertEquals(endOfDay.get(Calendar.MONTH), calendar.get(Calendar.MONTH));
        assertEquals(endOfDay.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        calendar.setTimeInMillis(endOfDay.getTimeInMillis());
        calendar.add(Calendar.MILLISECOND, 1);
        assertTrue(endOfDay.get(Calendar.DAY_OF_MONTH) != calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.DateValidator#getBeginningOfDay()}.
     */
    @Test
    public final void testGetBeginningOfDay()
    {
        final Calendar calendar = Calendar.getInstance();
        final Calendar startOfDay = DateValidator.getBeginningOfDay();

        assertTrue(startOfDay.before(calendar));
        assertEquals(startOfDay.get(Calendar.YEAR), calendar.get(Calendar.YEAR));
        assertEquals(startOfDay.get(Calendar.MONTH), calendar.get(Calendar.MONTH));
        assertEquals(startOfDay.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        calendar.setTimeInMillis(startOfDay.getTimeInMillis());
        calendar.add(Calendar.MILLISECOND, -1);
        assertTrue(startOfDay.get(Calendar.DAY_OF_MONTH) != calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Test method for {@link DateValidator#needsValidation}.
     */
    @Test
    public final void testNeedsValidation()
    {
        final CRMSpid oldSpid = new CRMSpid();
        final CRMSpid newSpid = new CRMSpid();
        final Date date = new Date();

        // FALSE case : both beans are null
        assertFalse(DateValidator.needsValidation(null, null, CRMSpidXInfo.EFFECTIVE_DATE));

        // FALSE case : no old bean, and property not set in new bean
        newSpid.setEffectiveDate(null);
        assertFalse(DateValidator.needsValidation(oldSpid, newSpid, CRMSpidXInfo.EFFECTIVE_DATE));

        // FALSE case : property not set in old bean & new bean
        oldSpid.setEffectiveDate(null);
        newSpid.setEffectiveDate(null);
        assertFalse(DateValidator.needsValidation(oldSpid, newSpid, CRMSpidXInfo.EFFECTIVE_DATE));

        // FALSE case : property set in old bean but not in new bean
        oldSpid.setEffectiveDate(date);
        newSpid.setEffectiveDate(null);
        assertFalse(DateValidator.needsValidation(oldSpid, newSpid, CRMSpidXInfo.EFFECTIVE_DATE));

        // FALSE case : no new bean
        assertFalse(DateValidator.needsValidation(oldSpid, null, CRMSpidXInfo.EFFECTIVE_DATE));

        // FALSE case : property unchanged from old bean to new bean
        oldSpid.setEffectiveDate(date);
        newSpid.setEffectiveDate(date);
        assertFalse(DateValidator.needsValidation(oldSpid, newSpid, CRMSpidXInfo.EFFECTIVE_DATE));

        // TRUE case : property changed
        oldSpid.setEffectiveDate(date);
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        newSpid.setEffectiveDate(calendar.getTime());
        assertTrue(DateValidator.needsValidation(oldSpid, newSpid, CRMSpidXInfo.EFFECTIVE_DATE));

        // TRUE case : new bean
        assertTrue(DateValidator.needsValidation(null, newSpid, CRMSpidXInfo.EFFECTIVE_DATE));
    }

    /**
     * Test method for {@link DateValidator#needsValidation} where the property is not specified.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testNeedsValidationNullProperty()
    {
        DateValidator.needsValidation(new CRMSpid(), new CRMSpid(), null);
    }

    /**
     * Test method for {@link DateValidator#needsValidation} where the property is not of the bean type.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testNeedsValidationWrongProperty()
    {
        DateValidator.needsValidation(new CRMSpid(), new CRMSpid(), AccountXInfo.DATE_OF_BIRTH);
    }

    /**
     * Test method for {@link DateValidator#needsValidation} where the property is not a date.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testNeedsValidationWrongPropertyDataType()
    {
        DateValidator.needsValidation(new CRMSpid(), new CRMSpid(), CRMSpidXInfo.AUTO_BILL_CYCLE);
    }

    /**
     * Test method for {@link DateValidator#needsValidation} where the property does not belong to the old bean.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testNeedsValidationWrongOldBeanType()
    {
        DateValidator.needsValidation(new Account(), new CRMSpid(), CRMSpidXInfo.EFFECTIVE_DATE);
    }

    /**
     * Test method for {@link DateValidator#needsValidation} where the property does not belong to the new bean.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testNeedsValidationWrongNewBeanType()
    {
        DateValidator.needsValidation(new CRMSpid(), new Subscriber(), CRMSpidXInfo.EFFECTIVE_DATE);
    }

    /**
     * Test method for {@link DateValidator#validBefore} where the property is an invalid date.
     */
    @Test(expected = IllegalPropertyArgumentException.class)
    public final void testValidBeforeInvalid()
    {
        final Account account = new Account();
        final Calendar calendar = Calendar.getInstance();
        account.setLastBillDate(calendar.getTime());
		DateValidator.validBefore(ContextLocator.locate(), null, account,
		    AccountXInfo.LAST_BILL_DATE, Calendar.DAY_OF_MONTH, -1);
    }

    /**
     * Test method for {@link DateValidator#validAfter} where the property is an invalid date.
     */
    @Test(expected = IllegalPropertyArgumentException.class)
    public final void testValidAfterInvalid()
    {
        final Account account = new Account();
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        account.setLastBillDate(calendar.getTime());
		DateValidator.validAfter(ContextLocator.locate(), null, account,
		    AccountXInfo.LAST_BILL_DATE, Calendar.DAY_OF_MONTH, 0);
    }

    /**
     * Test method for {@link DateValidator#validBefore} where the property is a valid date.
     */
    @Test
    public final void testValidBeforeValid()
    {
        final Account account = new Account();
        final Calendar calendar = Calendar.getInstance();
        account.setLastBillDate(calendar.getTime());
		DateValidator.validBefore(ContextLocator.locate(), null, account,
		    AccountXInfo.LAST_BILL_DATE, Calendar.DAY_OF_MONTH, 0);
		DateValidator.validBefore(ContextLocator.locate(), null, account,
		    AccountXInfo.LAST_BILL_DATE, Calendar.MONTH, 1);

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        account.setLastBillDate(calendar.getTime());
		DateValidator.validBefore(ContextLocator.locate(), null, account,
		    AccountXInfo.LAST_BILL_DATE, Calendar.DAY_OF_MONTH, 0);
		DateValidator.validBefore(ContextLocator.locate(), null, account,
		    AccountXInfo.LAST_BILL_DATE, Calendar.DAY_OF_MONTH, -1);
    }

    /**
     * Test method for {@link DateValidator#validAfter} where the property is a valid date.
     */
    @Test
    public final void testValidAfterValid()
    {
        final Account account = new Account();
        final Calendar calendar = Calendar.getInstance();
        account.setLastBillDate(calendar.getTime());
		DateValidator.validAfter(ContextLocator.locate(), null, account,
		    AccountXInfo.LAST_BILL_DATE, Calendar.DAY_OF_MONTH, 0);
		DateValidator.validAfter(ContextLocator.locate(), null, account,
		    AccountXInfo.LAST_BILL_DATE, Calendar.MONTH, -1);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        account.setLastBillDate(calendar.getTime());
		DateValidator.validAfter(ContextLocator.locate(), null, account,
		    AccountXInfo.LAST_BILL_DATE, Calendar.DAY_OF_MONTH, 1);
		DateValidator.validAfter(ContextLocator.locate(), null, account,
		    AccountXInfo.LAST_BILL_DATE, Calendar.MONTH, 0);
    }

    /**
     * Configuration to be used.
     */
    private static GeneralConfig config;

    /**
     * Date validator to be tested.
     */
    private static DateValidator validator;
}
