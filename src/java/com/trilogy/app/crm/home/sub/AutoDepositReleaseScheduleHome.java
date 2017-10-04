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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AutoDepositReleaseConfigurationEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteriaHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.DunningConfigurationEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.support.DepositSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Provides a check of next automatic deposit release date of a subscriber. Upon creation of a subscriber and
 * modification of the subscriber's deposit, update the nextDepositReleaseDate according to the serviceDuration of the
 * AutoDepositReleaseCriteria associated with the credit category or service provider.
 *
 * @author cindy.wong@redknee.com
 */
public class AutoDepositReleaseScheduleHome extends HomeProxy
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1003238885768208669L;

    /**
     * Creates a new AutoDepositReleaseScheduleHome.
     *
     * @param context The operating context.
     * @param delegate The Home to which we delegate.
     */
    public AutoDepositReleaseScheduleHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }

    /**
     * Upon the creation of a subscriber, sets the value of <code>nextDepositReleaseDate</code> according to the
     * <code>AutoDepositReleaseCriteria</code> applicable to the subscriber.
     *
     * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     * @param context The operating context.
     * @param obj The new subscriber being created.
     * @return The updated subscriber.
     * @exception HomeException Thrown if there are problems accessing Home information in the context.
     */
    @Override
    public Object create(final Context context, final Object obj) throws HomeException
    {
    	LogSupport.debug(context, this, "SubscriberPipeline[create].....");
    	final Subscriber subscriber = (Subscriber) obj;
        final CRMSpid serviceProvider = getServiceProvider(context, subscriber.getSpid());

        // do not set date if auto deposit release is not enabled.
        if (DepositSupport.isAutoDepositReleaseEnabled(context, serviceProvider))
        {
            // retrieve criteria only if Auto Deposit Release is Enabled
            final AutoDepositReleaseCriteria criteria = getCriteria(context, subscriber);

            if (criteria != null)
            {
                final Calendar calendar = Calendar.getInstance();
                if (subscriber.getDepositDate() != null)
                {
                    calendar.setTime(subscriber.getDepositDate());
                    calendar.add(Calendar.DAY_OF_MONTH, criteria.getServiceDuration());
                    subscriber.setNextDepositReleaseDate(calendar.getTime());
                }
            }
        }
        return super.create(context, subscriber);
    }

    /**
     * Upon update of a subscriber, recalculate the value of <code>nextDepositReleaseDate</code> if the subscriber's
     * deposit has been modified. The calculation is done according to the <code>AutoDepositReleaseCriteria</code>
     * applicable to the subscriber.
     *
     * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     * @param context The operating context.
     * @param obj The subscriber being updated.
     * @return The updated subscriber with the new <code>nextDepositReleaseDate</code>.
     * @exception HomeException Thrown if there are problems accessing Home information in the context.
     */
    @Override
    public Object store(final Context context, final Object obj) throws HomeException
    {
    	LogSupport.debug(context, this, "SubscriberPipeline[store].....");
    	final Subscriber subscriber;
        try
        {
            subscriber = (Subscriber) obj;
        }
        catch (ClassCastException exception)
        {
            throw new HomeException("System Error: obj passed is not a Subscriber", exception);
        }
        if (subscriber == null)
        {
            throw new HomeException("System Error: obj passed is null");
        }

        // get the old subscriber
        final Subscriber oldSubscriber;
        try
        {
            oldSubscriber = getSubscriber(context, subscriber.getId());
        }
        catch (HomeException exception)
        {
            throw new HomeException("Cannot retrieve existing subscriber " + subscriber.getId(), exception);
        }
        if (oldSubscriber == null)
        {
            throw new HomeException("Subcriber " + subscriber.getId() + " does not exist in home");
        }
        // assert(subscriber.getId() == oldSubscriber.getId());

        // update the next release date if the deposit was changed
        final CRMSpid serviceProvider = getServiceProvider(context, subscriber.getSpid());
        if (DepositSupport.isAutoDepositReleaseEnabled(context, serviceProvider)
            && !SafetyUtil.safeEquals(subscriber.getDepositDate(),oldSubscriber.getDepositDate()))
        {
            final AutoDepositReleaseCriteria criteria;
            try
            {
                criteria = getCriteria(context, subscriber);
            }
            catch (HomeException exception)
            {
                throw new HomeException("Cannot retrieve AutoDepositReleaseCriteria for subscriber "
                    + subscriber.getId(), exception);
            }

            // do not update date if auto deposit release is not enabled
            if (criteria != null)
            {
                // calculate the next deposit release date
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(subscriber.getDepositDate());
                calendar.add(Calendar.DAY_OF_MONTH, criteria.getServiceDuration());

                subscriber.setNextDepositReleaseDate(calendar.getTime());
            }
        }

        return super.store(context, subscriber);
    }

    /**
     * Gets the service provider with the given SPID.
     *
     * @param context The operating context.
     * @param spid The service provider identifier.
     * @return The service provider.
     * @throws HomeException Thrown if there are problems accessing Home information in the context.
     */
    private CRMSpid getServiceProvider(final Context context, final int spid) throws HomeException
    {
        final Home home = (Home)context.get(CRMSpidHome.class);
        if (home == null)
        {
            throw new HomeException("Cannot find CRMSpidHome in context");
        }
        try
        {
            final CRMSpid serviceProvider = (CRMSpid)home.find(context, Integer.valueOf(spid));
            return serviceProvider;
        }
        catch (HomeException exception)
        {
            throw new HomeException("Cannot find service provider " + spid, exception);
        }
    }

    /**
     * Gets the subscriber for the given identifier.
     *
     * @param context The operating context.
     * @param identifier The subscriber identifier.
     * @return The subscriber.
     * @throws HomeException Thrown if there are problems accessing Home information in the context.
     */
    private Subscriber getSubscriber(final Context context, final String identifier) throws HomeException
    {
        // TODO 2007-05-30 try context first
        final Home home = (Home) context.get(SubscriberHome.class);
        if (home == null)
        {
            throw new HomeException("Cannot find SubscriberHome in context");
        }
        try
        {
            final Subscriber subscriber = (Subscriber) home.find(context, identifier);
            return subscriber;
        }
        catch (HomeException exception)
        {
            throw new HomeException("Cannot find subscriber " + identifier, exception);
        }
    }

    /**
     * Gets the account for the given BAN.
     *
     * @param context The operating context.
     * @param ban The BAN of the account.
     * @return The account.
     * @throws HomeException Thrown if there are problems accessing Home information in the context.
     */
    private Account getAccount(final Context context, final String ban) throws HomeException
    {
        Account account = (Account) context.get(Account.class);
        if (account==null || !account.getBAN().equals(ban))
        {
            final Home home = (Home) context.get(AccountHome.class);
            if (home == null)
            {
                throw new HomeException("Cannot find AccountHome in context");
            }
            try
            {
                account = (Account) home.find(context, ban);
            }
            catch (HomeException exception)
            {
                throw new HomeException("Cannot find account " + ban, exception);
            }
        }

        return account;
    }

    /**
     * Gets the credit category for the given identifier.
     *
     * @param context The operating context.
     * @param identifier The identifier of the credit category.
     * @return The credit category.
     * @throws HomeException Thrown if there are problems accessing Home information in the context.
     */
    private CreditCategory getCreditCategory(final Context context, final int identifier) throws HomeException
    {
        final Home home = (Home) context.get(CreditCategoryHome.class);
        if (home == null)
        {
            throw new HomeException("Cannot find CreditCategoryHome in context");
        }
        try
        {
            final CreditCategory creditCategory = (CreditCategory) home.find(context, Integer.valueOf(identifier));
            return creditCategory;
        }
        catch (HomeException exception)
        {
            throw new HomeException("Cannot find credit category " + identifier, exception);
        }
    }

    /**
     * Gets the auto deposit release criteria for the given subscriber.
     *
     * @param context The operating context.
     * @param subscriber The identifier of the subscriber.
     * @return The auto deposit release criteria for the given subscriber.
     * @throws HomeException Thrown if there are problems accessing home information in the context.
     */
    private AutoDepositReleaseCriteria getCriteria(final Context context, final Subscriber subscriber)
        throws HomeException
    {
        final Account account = getAccount(context, subscriber.getBAN());
        if (account == null)
        {
            throw new HomeException("Cannot find account BAN=" + subscriber.getBAN() + " subscriber="
                + subscriber.getId());
        }
        final CreditCategory creditCategory = getCreditCategory(context, account.getCreditCategory());
        if (creditCategory == null)
        {
            throw new HomeException("Cannot find credit category code=" + account.getCreditCategory());
        }

        final CRMSpid spid = SpidSupport.getCRMSpid(context, account.getSpid());
        if (spid == null)
        {
            throw new HomeException("Cannot find SPID " + account.getSpid() + " for subscriber=" + subscriber.getId());
        }

        // return null if auto deposit release is not enabled
        if (spid.getUseAutoDepositRelease().equals(AutoDepositReleaseConfigurationEnum.NO))
        {
            return null;
        }
        else
        {
            long id = -1;
            if (creditCategory.getAutoDepositReleaseConfiguration().equals(DunningConfigurationEnum.CUSTOM))
            {
                id = creditCategory.getAutoDepositReleaseCriteria();
            }
            else
            {
                id = spid.getAutoDepositReleaseCriteria();
            }

            final Home adrcHome = (Home) context.get(AutoDepositReleaseCriteriaHome.class);
            if (adrcHome == null)
            {
                throw new HomeException("Cannot find AutoDepositReleaseCriteriaHome in context");
            }
            final AutoDepositReleaseCriteria adrc;
            try
            {
                adrc = (AutoDepositReleaseCriteria) adrcHome.find(context, Long.valueOf(id));
            }
            catch (HomeException exception)
            {
                throw new HomeException(
                    "Cannot find AutoDepositReleaseCriteria for subscriber " + subscriber.getId(), exception);
            }
            if (adrc == null)
            {
                throw new HomeException("Cannot find AutoDepositReleaseCriteria " + id);
            }
            return adrc;
        }
    }
} // class
