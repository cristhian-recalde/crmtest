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

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AutoDepositReleaseConfigurationEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.DunningConfigurationEnum;
import com.trilogy.app.crm.bean.ReleaseScheduleConfigurationEnum;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * Support class for deposit release unit testing.
 *
 * @author cindy.wong@redknee.com
 */
public final class DepositReleaseTestSupport
{
    /**
     * Private constructor to prevent instantiation.
     */
    private DepositReleaseTestSupport()
    {
        // this class should not be instantiated
    }

    /**
     * Creates an Auto Deposit Release Criteria.
     *
     * @param id
     *            Identifier of the criteria.
     * @param adjustmentType
     *            The adjustment type used for deposit release transactions.
     * @param releasePercent
     *            The percentage of deposit to release
     * @param minReleaseAmount
     *            The minimum amount of deposit to release.
     * @param releaseSchedule
     *            The release schedule.
     * @param useDayOfMonth
     *            Whether release schedule is day of month (TRUE) or relative to bill cycle (FALSE).
     * @param duration
     *            How many days before deposit is deemed eligibile for auto release.
     * @return The criteria created.
     */
    public static AutoDepositReleaseCriteria createCriteria(final long id, final int adjustmentType,
        final double releasePercent, final long minReleaseAmount, final int releaseSchedule,
        final boolean useDayOfMonth, final int duration)
    {
        final AutoDepositReleaseCriteria criteria = new AutoDepositReleaseCriteria();
        criteria.setDepositReleaseAdjustmentType(adjustmentType);
        criteria.setDepositReleasePercent(releasePercent);
        criteria.setIdentifier(id);
        criteria.setMinimumDepositReleaseAmount(minReleaseAmount);
        criteria.setReleaseSchedule(releaseSchedule);
        if (useDayOfMonth)
        {
            criteria.setReleaseScheduleConfiguration(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH);
        }
        else
        {
            criteria.setReleaseScheduleConfiguration(ReleaseScheduleConfigurationEnum.DAYS_BEFORE_BILL_CYCLE);
        }
        criteria.setServiceDuration(duration);
        return criteria;
    }

    /**
     * Creates a credit category.
     *
     * @param code
     *            Credit category code.
     * @param spid
     *            Service provider ID.
     * @param customAutoDepositRelease
     *            Whether to use custom auto deposit release criteria.
     * @param criteriaId
     *            Auto Deposit Release Criteria ID.
     * @return The created credit category.
     */
    public static CreditCategory createCreditCategory(final int code, final int spid,
        final boolean customAutoDepositRelease, final long criteriaId)
    {
        final CreditCategory creditCategory = new CreditCategory();
        creditCategory.setCode(code);
        creditCategory.setSpid(spid);
        if (customAutoDepositRelease)
        {
            creditCategory.setAutoDepositReleaseConfiguration(DunningConfigurationEnum.CUSTOM);
        }
        else
        {
            creditCategory.setAutoDepositReleaseConfiguration(DunningConfigurationEnum.SERVICE_PROVIDER);
        }
        creditCategory.setAutoDepositReleaseCriteria(criteriaId);
        return creditCategory;
    }

    /**
     * Creates a new bill cycle.
     *
     * @param id
     *            Bill cycle ID.
     * @param spid
     *            Service provider ID.
     * @param dayOfMonth
     *            The day of month of this bill cycle.
     * @return The created bill cycle.
     */
    public static BillCycle createBillCycle(final int id, final int spid, final int dayOfMonth)
    {
        final BillCycle billCycle = new BillCycle();
        billCycle.setBillCycleID(id);
        billCycle.setIdentifier(id);
        billCycle.setSpid(spid);
        billCycle.setDayOfMonth(dayOfMonth);
        return billCycle;
    }

    /**
     * Create a service provider for testing.
     *
     * @param id
     *            ID of the service provider.
     * @param useAutoDepositRelease
     *            Whether to use auto deposit release.
     * @param criteriaId
     *            The auto deposit release criteria applicable to this service provider.
     * @return The service provider created.
     */
    public static CRMSpid createSpid(final int id, final boolean useAutoDepositRelease, final long criteriaId)
    {
        final CRMSpid spid = new CRMSpid();
        spid.setSpid(id);
        spid.setId(id);
        if (useAutoDepositRelease)
        {
            spid.setUseAutoDepositRelease(AutoDepositReleaseConfigurationEnum.YES);
        }
        else
        {
            spid.setUseAutoDepositRelease(AutoDepositReleaseConfigurationEnum.NO);
        }
        spid.setAutoDepositReleaseCriteria(criteriaId);
        return spid;
    }

    /**
     * Creates an account with unique BAN.
     *
     * @param spid
     *            Service provider ID.
     * @param creditCategory
     *            Credit category ID.
     * @param billCycle
     *            Bill cycle ID.
     * @return The account created.
     */
    public static Account createAccount(final int spid, final int creditCategory, final int billCycle)
    {
        final Account account = new Account();
        account.setBAN("" + nextBan);
        nextBan++;
        account.setSpid(spid);
        account.setBillCycleID(billCycle);
        account.setCreditCategory(creditCategory);
        return account;
    }

    /**
     * Creates a new subscriber of the provided account.
     *
     * @param account
     *            Account owning the subscriber.
     * @param deposit
     *            The deposit balance of the subscriber.
     * @param nextDepositReleaseDate
     *            The date on which the subscriber is eligible for deposit release.
     * @return The created subscriber.
     */
    public static Subscriber createSubscriber(final Account account, final long deposit,
        final Date nextDepositReleaseDate)
    {
        final Subscriber subscriber = new Subscriber();
        final String id = account.getBAN() + "-" + nextSubscriberId;
        nextSubscriberId++;
        subscriber.setId(id);
        subscriber.setBAN(account.getBAN());
        subscriber.setSpid(account.getSpid());
        subscriber.setDeposit(deposit);
        subscriber.setNextDepositReleaseDate(nextDepositReleaseDate);
        return subscriber;
    }

    /**
     * Next usable BAN.
     */
    private static int nextBan = 1;

    /**
     * Next usable subscriber ID.
     */
    private static int nextSubscriberId = 1;
}
