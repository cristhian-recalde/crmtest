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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteriaHome;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.DunningConfigurationEnum;
import com.trilogy.app.crm.bean.ReleaseScheduleConfigurationEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CreditCategorySupport;
import com.trilogy.app.crm.support.DepositSupport;
import com.trilogy.app.crm.support.SpidSupport;

/**
 * A visitor, intended for subscriber home, for auto deposit release.
 *
 * @author cindy.wong@redknee.com
 */
public abstract class SubscriberReleaseVisitor implements Visitor
{
    /**
     * Error message when subscriber has empty deposit balance.
     */
    public static final String SUBSCRIBER_NO_DEPOSIT = "Subscriber deposit balance is zero";

    /**
     * Fragment of error message ("Cannot find bill cycle for account").
     */
    public static final String CANNOT_FIND_BILL_CYCLE_FOR_ACCOUNT = "Cannot find bill cycle for account ";

    /**
     * Fragment of error message ("for credit category").
     */
    public static final String FOR_CREDIT_CATEGORY = " for credit category ";

    /**
     * Fragment of error message ("Cannot find auto deposit release criteria").
     */
    public static final String CANNOT_FIND_AUTO_DEPOSIT_RELEASE_CRITERIA =
        "Cannot find auto deposit release criteria ";

    /**
     * Fragment of error message ("for account").
     */
    public static final String FOR_ACCOUNT = " for account ";

    /**
     * Fragment of error message ("of criteria").
     */
    public static final String OF_CRITERIA = " of criteria ";

    /**
     * Fragment of error message ("Cannot find adjustment type").
     */
    public static final String CANNOT_FIND_ADJUSTMENT_TYPE = "Cannot find adjustment type ";

    /**
     * Fragment of error message ("Cannot find account").
     */
    public static final String CANNOT_FIND_ACCOUNT = "Cannot find account ";

    /**
     * Fragment of error message ("Cannot find credit category").
     */
    public static final String CANNOT_FIND_CREDIT_CATEGORY = "Cannot find credit category ";

    /**
     * Error message when SPID does not exist in context.
     */
    public static final String SPID_DOES_NOT_EXIST_IN_CONTEXT = "Service provider does not exist in context: ";

    /**
     * Creates a new abstract subscriber release visitor.
     *
     */
    protected SubscriberReleaseVisitor()
    {
        // empty
    }

    /**
     * Initializes the subscriber release visitor. Once the visitor is initialized, it becomes immutable.
     *
     * @param context
     *            The operating context.
     * @param criteria
     *            The deposit release criteria used by this visitor.
     * @param serviceProvider
     *            The SPID of the service provider being visited.
     * @param creator
     *            Strategy used to create deposit release transaction.
     * @param activeDate
     *            The date to act upon.
     */
    public final synchronized void initalize(final Context context, final AutoDepositReleaseCriteria criteria,
        final int serviceProvider, final DepositReleaseTransactionCreator creator, final Calendar activeDate)
    {
        RuntimeException exception = null;
        if (isInitialized())
        {
            exception = new IllegalStateException("This visitor has already been initialized");
        }
        else if (criteria == null)
        {
            exception = new IllegalArgumentException("Criteria cannot be null");
        }
        else if (activeDate == null)
        {
            exception = new IllegalArgumentException("Calendar cannot be null");
        }
        else if (serviceProvider < 0)
        {
            exception = new IllegalArgumentException("Service provider out of range");
        }
        else if (creator == null)
        {
            exception = new IllegalArgumentException("Transaction creator cannot be null");
        }

        if (exception != null)
        {
            new DebugLogMsg(this, "Cannot initialize visitor", exception).log(context);
            throw exception;
        }

        setSpidCriteria(criteria);
        setActiveDate(activeDate);
        setServiceProvider(serviceProvider);
        setTransactionCreator(creator);
        initialized_ = true;
    }

    /**
     * Returns an uninitialized prototype of this visitor.
     *
     * @return An uninitialized prototype of this visitor.
     */
    public abstract SubscriberReleaseVisitor prototype();

    /**
     * Increments number of subscribers visited.
     */
    protected final synchronized void incrementNumVisits()
    {
        numVisits_++;
    }

    /**
     * Returns the number of subscribers visited since the last reset.
     *
     * @return Number of times visit() is called.
     */
    public final synchronized int getNumVisits()
    {
        return numVisits_;
    }

    /**
     * Resets the counter of subscribers visited to 0.
     */
    public final synchronized void resetNumVisits()
    {
        numVisits_ = 0;
    }

    /**
     * Preliminary validation before processing the subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being visited.
     * @param serviceProvider
     *            The service provider this Auto Deposit Release is running on.
     * @param spidCriteria SPID-level auto deposit release criteria.
     * @param activeDate date on which this criteria is acting on.
     * @return Whether the subscriber is eligible for auto deposit release.
     * @throws AgentException
     *             Thrown if there are data problems.
     */
    protected boolean validate(final Context context, final Subscriber subscriber, final CRMSpid serviceProvider,
        final AutoDepositReleaseCriteria spidCriteria, final Calendar activeDate)
        throws AgentException
    {
        boolean valid = true;

        // test if auto deposit release is enabled
        if (!DepositSupport.isAutoDepositReleaseEnabled(context, serviceProvider))
        {
            final AbortVisitException ave = new AbortVisitException("Auto deposit release is disabled");
            new DebugLogMsg(this, ave.getMessage(), ave).log(context);
            throw ave;
        }

        // test spid
        if (subscriber.getSpid() != serviceProvider.getSpid())
        {
            final AgentException ae = new AgentException(serviceProvider.getSpid() + "Subscriber " + subscriber.getId()
                + " does not belong to SPID ");
            new DebugLogMsg(this, ae.getMessage(), ae).log(context);
            throw ae;
        }

        // get deposit release date
        if (subscriber.getNextDepositReleaseDate() == null)
        {
            final AgentException ae = new AgentException("Deposit release date not set for subscriber "
                + subscriber.getId());
            new DebugLogMsg(this, ae.getMessage(), ae).log(context);
            throw ae;
        }

        final Calendar releaseDate = Calendar.getInstance();
        releaseDate.setTime(subscriber.getNextDepositReleaseDate());
        if (releaseDate.after(activeDate))
        {
            final AgentException ae = new AgentException("Subscriber is not eligible for auto deposit release until "
                + subscriber.getNextDepositReleaseDate());
            new DebugLogMsg(this, ae.getMessage(), ae).log(context);
            throw ae;
        }

        // test deposit amount
        if (subscriber.getDeposit(context) <= 0)
        {
            new DebugLogMsg(this, SUBSCRIBER_NO_DEPOSIT, null).log(context);
            valid = false;
        }
        else
        {
            // check bill cycle day if it's relative to bill cycle
            valid = validateBillCycleDay(context, spidCriteria, subscriber.getBAN(), activeDate);
        }
        return valid;
    }

    /**
     * Validates whether subscribers of a specific BAN is eligible for auto deposit release on the provided date.
     *
     * @param context
     *            The operating context.
     * @param criteria
     *            The criteria used to determine release schedule.
     * @param ban
     *            The BAN of interest.
     * @param activeDate
     *            The date to act on.
     * @return <code>TRUE</code> if subscriber is eligible.
     * @throws AgentException
     *             Thrown if there are errors determining the BAN's eligibility.
     */
    protected static boolean validateBillCycleDay(final Context context, final AutoDepositReleaseCriteria criteria,
        final String ban, final Calendar activeDate) throws AgentException
    {
        boolean result = true;
        if (criteria.getReleaseScheduleConfiguration() == ReleaseScheduleConfigurationEnum.DAYS_BEFORE_BILL_CYCLE)
        {
            BillCycle billCycle;
            try
            {
                billCycle = BillCycleSupport.getBillCycleForBan(context, ban);
            }
            catch (HomeException exception)
            {
                final AgentException ae = new AgentException(CANNOT_FIND_BILL_CYCLE_FOR_ACCOUNT + ban, exception);
                new DebugLogMsg(DefaultSubscriberReleaseVisitor.class, ae.getMessage(), ae).log(context);
                throw ae;
            }
            if (billCycle == null)
            {
                final AgentException ae = new AgentException(CANNOT_FIND_BILL_CYCLE_FOR_ACCOUNT + ban);
                new DebugLogMsg(DefaultSubscriberReleaseVisitor.class, ae.getMessage(), ae).log(context);
                throw ae;
            }
            final Calendar calendar = (Calendar) activeDate.clone();
            calendar.add(Calendar.DAY_OF_MONTH, criteria.getReleaseSchedule());
            result = (billCycle.getDayOfMonth() == calendar.get(Calendar.DAY_OF_MONTH));
        }
        return result;
    }

    /**
     * Retrieves the criteria used to determine auto deposit release for subscribers in this BAN.
     *
     * @param context
     *            The operating context.
     * @param ban
     *            The subscriber's BAN.
     * @param criteria
     *            The default SPID-level auto deposit release criteria.
     * @return The auto deposit release criteria for subscribers in the provided BAN.
     * @throws AgentException
     *             Thrown if there are problem determining the criteria used.
     */
    protected static AutoDepositReleaseCriteria getCriteria(final Context context, final String ban,
        final AutoDepositReleaseCriteria criteria) throws AgentException
    {
        AutoDepositReleaseCriteria result = criteria;
        Account account;
        try
        {
            account = (Account) ((Home) context.get(AccountHome.class)).find(ban);
        }
        catch (HomeException exception)
        {
            final AgentException ae = new AgentException(CANNOT_FIND_ACCOUNT + ban, exception);
            new DebugLogMsg(DefaultSubscriberReleaseVisitor.class, ae.getMessage(), ae).log(context);
            throw ae;
        }
        if (account == null)
        {
            throw new AgentException(CANNOT_FIND_ACCOUNT + ban);
        }
        // find credit category
        CreditCategory creditCategory;
        try
        {
            creditCategory = CreditCategorySupport.findCreditCategory(context, account.getCreditCategory());
        }
        catch (HomeException exception)
        {
            final AgentException ae = new AgentException(CANNOT_FIND_CREDIT_CATEGORY + account.getCreditCategory()
                + FOR_ACCOUNT + ban, exception);
            new DebugLogMsg(DefaultSubscriberReleaseVisitor.class, ae.getMessage(), ae).log(context);
            throw ae;
        }
        if (creditCategory == null)
        {
            final AgentException ae = new AgentException(CANNOT_FIND_CREDIT_CATEGORY + account.getCreditCategory()
                + FOR_ACCOUNT + ban);
            new DebugLogMsg(DefaultSubscriberReleaseVisitor.class, ae.getMessage(), ae).log(context);
            throw ae;
        }

        if (creditCategory.getAutoDepositReleaseConfiguration() == DunningConfigurationEnum.CUSTOM)
        {
            try
            {
                result = (AutoDepositReleaseCriteria) ((Home) context.get(AutoDepositReleaseCriteriaHome.class))
                    .find(context, Long.valueOf(creditCategory.getAutoDepositReleaseCriteria()));
            }
            catch (HomeException exception)
            {
                final AgentException ae = new AgentException(CANNOT_FIND_AUTO_DEPOSIT_RELEASE_CRITERIA
                    + creditCategory.getAutoDepositReleaseCriteria() + FOR_CREDIT_CATEGORY + creditCategory.getCode(),
                    exception);
                new DebugLogMsg(DefaultSubscriberReleaseVisitor.class, ae.getMessage(), ae).log(context);
                throw ae;
            }
            if (result == null)
            {
                final AgentException ae = new AgentException(CANNOT_FIND_AUTO_DEPOSIT_RELEASE_CRITERIA
                    + creditCategory.getAutoDepositReleaseCriteria() + FOR_CREDIT_CATEGORY + creditCategory.getCode());
                new DebugLogMsg(DefaultSubscriberReleaseVisitor.class, ae.getMessage(), ae).log(context);
                throw ae;
            }
        }
        return result;
    }

    /**
     * Retrieves the service provider.
     *
     * @param context
     *            The operating context.
     * @return The service provider object used by this visitor.
     */
    protected final CRMSpid getServiceProvider(final Context context)
    {
        CRMSpid serviceProvider;
        try
        {
            serviceProvider = SpidSupport.getCRMSpid(context, getServiceProvider());
        }
        catch (HomeException exception)
        {
            final AbortVisitException ave = new AbortVisitException(SPID_DOES_NOT_EXIST_IN_CONTEXT
                + getServiceProvider(), exception);
            new DebugLogMsg(this, ave.getMessage(), ave).log(context);
            throw ave;
        }

        if (serviceProvider == null)
        {
            final AbortVisitException ave = new AbortVisitException(SPID_DOES_NOT_EXIST_IN_CONTEXT
                + getServiceProvider());
            new DebugLogMsg(this, ave.getMessage(), ave).log(context);
            throw ave;
        }
        return serviceProvider;
    }

    /**
     * @param spidCriteria The <code>spidCriteria</code> to set.
     */
    protected void setSpidCriteria(final AutoDepositReleaseCriteria spidCriteria)
    {
        spidCriteria_ = spidCriteria;
    }

    /**
     * @return The <code>spidCriteria</code>.
     */
    protected AutoDepositReleaseCriteria getSpidCriteria()
    {
        return spidCriteria_;
    }

    /**
     * @param activeDate The <code>activeDate</code> to set.
     */
    protected void setActiveDate(final Calendar activeDate)
    {
        activeDate_ = activeDate;
    }

    /**
     * @return The <code>activeDate</code>.
     */
    protected Calendar getActiveDate()
    {
        return activeDate_;
    }

    /**
     * @param serviceProvider The <code>serviceProvider</code> to set.
     */
    protected void setServiceProvider(final int serviceProvider)
    {
        serviceProvider_ = serviceProvider;
    }

    /**
     * @return The <code>serviceProvider</code>.
     */
    protected int getServiceProvider()
    {
        return serviceProvider_;
    }

    /**
     * @return The <code>initialized</code>.
     */
    protected boolean isInitialized()
    {
        return initialized_;
    }

    /**
     * @param transactionCreator The <code>transactionCreator</code> to set.
     */
    protected void setTransactionCreator(final DepositReleaseTransactionCreator transactionCreator)
    {
        transactionCreator_ = transactionCreator;
    }

    /**
     * @return The <code>transactionCreator</code>.
     */
    protected DepositReleaseTransactionCreator getTransactionCreator()
    {
        return transactionCreator_;
    }

    /**
     * Number of times visit() is called.
     */
    private int numVisits_;

    /**
     * The deposit release criteria belonging to the SPID.
     */
    private AutoDepositReleaseCriteria spidCriteria_;

    /**
     * The date to act upon. Defaults to now.
     */
    private Calendar activeDate_;

    /**
     * The service provider being visited.
     */
    private int serviceProvider_;

    /**
     * Whether this visitor has been initialized.
     */
    private boolean initialized_;

    /**
     * Strategy used to create deposit release transaction.
     */
    private DepositReleaseTransactionCreator transactionCreator_;
}
