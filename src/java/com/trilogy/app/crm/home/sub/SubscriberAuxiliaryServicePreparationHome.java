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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.Lookup;


/**
 * Prepares the subscriber auxiliary services selected by this subscriber.
 *
 * @author cindy.wong@redknee.com
 * @since Sep 17, 2007
 */
public class SubscriberAuxiliaryServicePreparationHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>SubscriberAuxiliaryServiceEndDateSettingHome</code>.
     *
     * @param delegate
     *            Delegate of this decorator.
     */
    public SubscriberAuxiliaryServicePreparationHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context context, final Object object) throws HomeException
    {
        final Subscriber subscriber = (Subscriber) object;
        prepareSubscriber(context, subscriber);
        return super.create(context, subscriber);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context context, final Object object) throws HomeException
    {
        final Subscriber subscriber = (Subscriber) object;
        
    	// validate the original setting, forbidden to change. 
    	// this validation was used to be in SubscriberAuxiliaryServiceDatesValidator. 
    	// it was conflicting with original function of this home. 
    	validate(context, subscriber );
    	
        prepareSubscriber(context, subscriber);
        return super.store(context, subscriber);
    }


    /**
     * Prepares all subscriber auxiliary service associations of a subscriber. This
     * includes removing the time of the date of the start date, setting the end date
     * appropriately based on the paymentNum, and sort the associations into immediate (or
     * existing/old) and future activations.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber whose subscriber auxiliary services are being prepared.
     * @throws HomeException
     *             Thrown if there are problems looking up the auxiliary services.
     */
    protected void prepareSubscriber(final Context context, final Subscriber subscriber) throws HomeException
    {
        final List<SubscriberAuxiliaryService> associations = subscriber.getAuxiliaryServices(context);

        final List<SubscriberAuxiliaryService> currentAssociations = new ArrayList<SubscriberAuxiliaryService>();
        final List<SubscriberAuxiliaryService> futureAssociations = new ArrayList<SubscriberAuxiliaryService>();

        final Date futureDate = CalendarSupportHelper.get(context).getEndOfDay(new Date());

        if (associations != null)
        {
            for (final SubscriberAuxiliaryService association : associations)
            {
                Date startDate = association.getStartDate();
                if (startDate == null)
                {
                    startDate = new Date();
                }
                startDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(startDate);
                association.setStartDate(startDate);
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);

                final AuxiliaryService service = association.getAuxiliaryService(context);
                if (service == null)
                {
                    throw new HomeException("Cannot find auxiliary service "
                        + association.getAuxiliaryServiceIdentifier());
                }

                association.setType(service.getType());

                // paymentNum overrides endDate
                if (association.getPaymentNum() > 0)
                {
                    int periodType;
                    int multiplier = association.getPaymentNum();
                    if (service.getChargingModeType() == ServicePeriodEnum.WEEKLY)
                    {
                        periodType = Calendar.WEEK_OF_YEAR;
                    }
                    else if (service.getChargingModeType() == ServicePeriodEnum.MONTHLY)
                    {
                        periodType = Calendar.MONTH;
                    }
                    else if (service.getChargingModeType() == ServicePeriodEnum.MULTIMONTHLY)
                    {
                        periodType = Calendar.MONTH;
                        Calendar startDateCalendar = Calendar.getInstance();
                        startDateCalendar.setTime(startDate);
                        if (startDateCalendar.get(Calendar.DAY_OF_MONTH) == subscriber.getAccount(context).getBillCycleDay(context))
                        {
                            multiplier = service.getRecurrenceInterval() * association.getPaymentNum();
                        }
                        else
                        {
                            // First payment is only 1 month.
                            multiplier = 1 + (service.getRecurrenceInterval() * (association.getPaymentNum() - 1));
                        }
                    }
                    else if (service.getChargingModeType() == ServicePeriodEnum.ANNUAL)
                    {
                        periodType = Calendar.YEAR;
                    }
                    else
                    {
                        throw new HomeException("Charging mode of auxiliary service "
                            + service.getIdentifier()
                            + " is not valid whith number of payments > 0");
                    }

                    calendar.add(periodType, multiplier);
                    association.setEndDate(calendar.getTime());

                    // clearing payment num because the value is transfered to the end date
                    association.setPaymentNum(0);
                }
                else
                {
                    Date endDate = association.getEndDate();
                    if (endDate == null)
                    {
                        calendar.add(Calendar.YEAR, 20);
                        endDate = calendar.getTime();
                    }
                    endDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(endDate);
                    association.setEndDate(endDate);
                }

                if (association.getStartDate().after(futureDate))
                {
                    futureAssociations.add(association);
                }
                else
                {
                    currentAssociations.add(association);
                }
            }
        }

        subscriber.setFutureAuxiliaryServices(futureAssociations);
        subscriber.setAuxiliaryServices(currentAssociations);
        
    }

    /**
     * {@inheritDoc}
     */
    public void validate(final Context context, final Object object) throws HomeException
    {
        final Subscriber subscriber = (Subscriber) object;
        
  
        final Collection<SubscriberAuxiliaryService> associations = subscriber.getAuxiliaryServices(context);
        for (final SubscriberAuxiliaryService association : associations)
        {
            association.setContext(context);
            validateAssociation(context, association);
        }

        /* Also validate future associations, unless/until the distinction is removed. */
        final Collection<SubscriberAuxiliaryService> futureAssociations = subscriber.getFutureAuxiliaryServices(context);
        for (final SubscriberAuxiliaryService association : futureAssociations)
        {
            association.setContext(context);
            validateAssociation(context, association);
        }

    }


    /**
     * Validate the dates of the provided association.
     *
     * @param context
     *            The operating context.
     * @param newAssociation
     *            The association being validated.
     * @param home
     *            Association home.
     * @param config
     *            Date validation configuration settings.
     * @param exceptions
     *            Exceptions listener; all exceptions to be thrown by this method should
     *            be added to this.
     */
    protected void validateAssociation(final Context context, 
    		final SubscriberAuxiliaryService newAssociation)
    throws HomeException
    {
        SubscriberAuxiliaryService oldAssociation = null;
        try
        {
        	final Home home = (Home) context.get(SubscriberAuxiliaryServiceHome.class);
        	oldAssociation = (SubscriberAuxiliaryService) home.find(context, newAssociation);
        }
        catch (final HomeException exception)
        {
            new DebugLogMsg(this, "Cannot find old SubscriberAuxiliaryService; assume null", exception).log(context);
        }
        
        /*
         * [Cindy]: merged validations from AuxiliaryServiceDateValidator.
         */
        if (oldAssociation != null)
        {
            if (!isOldSubscriberInAvailableState(context) && oldAssociation.isProvisioned())
            {
                final Date oldStartDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(oldAssociation.getStartDate());
                final Date newStartDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(newAssociation.getStartDate());
                if (!oldStartDate.equals(newStartDate))
                {
                    throw new HomeException("Start date cannot be modified when the auxiliary service is provisioned");
                }
            }
        }
        
        
    }


    /**
     * Determines whether the old subscriber was in Available state.
     *
     * @param context
     *            The operating context.
     * @return Whether the old subscriber was in Available state.
     */
    protected boolean isOldSubscriberInAvailableState(final Context context)
    {
        final Subscriber oldSubscriber = (Subscriber) context.get(Lookup.OLDSUBSCRIBER);
        boolean result = false;
        if (oldSubscriber != null)
        {
            result = SafetyUtil.safeEquals(oldSubscriber.getState(), SubscriberStateEnum.AVAILABLE);
        }
        return result;
    }
    
    
    

    /**
     * Determines whether a subscriber-auxiliary service association is for future
     * activation.
     *
     * @param association
     *            The subscriber auxiliary service association to be tested.
     * @param runningDate
     *            The date to test against.
     * @return Returns <code>true</code> if the association is in the future,
     *         <code>false</code> otherwise.
     */
    protected boolean isFutureAssociation(final SubscriberAuxiliaryService association, final Date runningDate)
    {
        return association.getStartDate().after(runningDate);
    }
}
