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
package com.trilogy.app.crm.home.account;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BirthdayPlan;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiConstants;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;

/**
 * Provides a mechanism to update the subscriber's birthday plan on Friends and Family.
 *
 * @author victor.stratan@redknee.com
 */
public class SubscriberBirthdayPlanUpdateHome extends HomeProxy
{
    /**
     * Serail version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new PersonalListPlanUpdateHome.
     *
     * @param delegate
     *            The Home to which we delegate.
     */
    public SubscriberBirthdayPlanUpdateHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Object result = super.store(ctx, obj);

        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.FNF_BIRTHDAYPLAN_LICENSE))
        {
            Account act = (Account) obj;
            if (act.isIndividual(ctx))
            {
                updateBirthdayPlan(ctx, act);
            }
        }

        return result;
    }

    /**
     * Gets the Friends and Family service.
     *
     * @param ctx
     *            The operating context.
     * @return The Friends and Family service.
     * @exception HomeException
     *                Thrown if the service cannot be located.
     */
    private static FFECareRmiService getFriendsAndFamilyService(final Context ctx) throws HomeException
    {
        try
        {
            return FFClosedUserGroupSupport.getFFRmiService(ctx, SubscriberBirthdayPlanUpdateHome.class);
        }
        catch (FFEcareException e)
        {
            throw new HomeException(e);
        }

    }


    /**
     * Desides if an update of the Bithday Plan on Friends and Family application is necessary.
     * If an update is necesary then the update method is called.
     *
     * @param ctx
     *            The operating context.
     * @param oldSubscriber
     *            The subscriber to update.
     * @exception HomeException
     *                Thrown if there are problems communicating with the services stored in the context.
     */
    private void updateBirthdayPlan(final Context ctx, final Account account) throws HomeException
    {
        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);

        final Date oldDateOfBirth = oldAccount.getDateOfBirth();
        final Date newDateOfBirth = account.getDateOfBirth();

        final boolean dateChanged = (oldDateOfBirth != null && !oldDateOfBirth.equals(newDateOfBirth))
                || (oldDateOfBirth != null && newDateOfBirth == null);

        // update the current plan or create the new plan if needed
        if (dateChanged)
        {
            final Collection<Subscriber> subscribers = account.getSubscribers(ctx);
            if (subscribers.size()==1)
            {
                updateBirthdayPlan(ctx, subscribers.iterator().next(), account.getDateOfBirth());
            }
        }
    }

    /**
     * Updates the date of birth and the plan ID in the subscriber's BithdayPlan in the Friends qnd Family application.
     *
     * @param ctx The operating context.
     * @param subscriber The subscriber to update.
     * @param birthDay The date of birth for the subscriber.
     * @exception HomeException Thrown if there are problems communicating with the services stored in the context.
     */
    public static void updateBirthdayPlan(final Context ctx, final Subscriber subscriber, final Date birthDay)
        throws HomeException
    {
        final FFECareRmiService service = getFriendsAndFamilyService(ctx);

        final BirthdayPlan plan = subscriber.getBirthdayPlan(ctx);
        
        if (plan!=null && birthDay!=null)
        {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(birthDay);
    
            final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            // Java month count starts at 0 for Jan. Should send 1 for Jan.
            final int month = cal.get(Calendar.MONTH) + 1;
            final String timeZone = TimeZone.getDefault().getID();

            try
            {
                final int result = service.updateBirthdayPlanForSub(subscriber.getMSISDN(), plan.getID(),
                        dayOfMonth, month, timeZone);
    
                if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
                {
                    throw new HomeException("Friends and Family service returned "
                        + CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result)
                        + " while attempting to update personal list plan.");
                }
            }
            catch (final RemoteException throwable)
            {
                throw new HomeException("Failed to update subscriber's personal list plan.", throwable);
            }
        }
    }

} // class
