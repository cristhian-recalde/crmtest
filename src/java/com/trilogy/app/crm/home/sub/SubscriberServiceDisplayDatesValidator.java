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
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.home.DateValidator;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.Lookup;


/**
 * Validates the dates of a {@link SubscriberServices} of a {@link Subscriber}.
 *
 * @author cindy.wong@redknee.com
 */
public class SubscriberServiceDisplayDatesValidator extends DateValidator
{

    /**
     * Create a new instance of <code>SubscriberServiceDisplayDatesValidator</code>.
     */
    protected SubscriberServiceDisplayDatesValidator()
    {
        super();
    }


    /**
     * Returns an instance of <code>SubscriberServiceDisplayDatesValidator</code>.
     *
     * @return An instance of <code>SubscriberServiceDisplayDatesValidator</code>.
     */
    public static SubscriberServiceDisplayDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberServiceDisplayDatesValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        Set newServiceSet = null;
        Set oldServiceSet = null;

        final Subscriber newSubscriber = (Subscriber) object;
        final Subscriber oldSubscriber = (Subscriber) context.get(Lookup.OLDSUBSCRIBER);
        newServiceSet = newSubscriber.getIntentToProvisionServices();
        if (oldSubscriber != null)
        {
            oldServiceSet = oldSubscriber.getAllNonUnprovisionedStateServices();
        }

        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
        if (config == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: GeneralConfig does not exist in context");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

		validateDates(context, newSubscriber, oldServiceSet, newServiceSet,
		    config);
    }


    /**
     * Validates the dates of two sets of {@link SubscriberServices}.
     *
     * @param oldServiceSet
     *            The old set of services; can be null.
     * @param newServiceSet
     *            The new set of services; can be null.
     * @param config
     *            The configuration storing the dates.
     * @throws IllegalStateException
     *             Thrown if one or more dates is invalid.
     */
	protected static void validateDates(Context context,
	    final Subscriber subscriber, final Set oldServiceSet,
	    final Set newServiceSet, final GeneralConfig config)
        throws IllegalStateException
    {
        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();
        final SortedMap<Long, SubscriberServices> oldServiceMap = convertSetToSortedMap(oldServiceSet, exceptions);
        final SortedMap<Long, SubscriberServices> newServiceMap = convertSetToSortedMap(newServiceSet, exceptions);

        for (final Map.Entry<Long, SubscriberServices> entry : newServiceMap.entrySet())
        {
            final Long key = entry.getKey();
            final SubscriberServices newService = entry.getValue();
            final SubscriberServices oldService = oldServiceMap.get(key);

            try
            {
                if (subscriber!=null && subscriber.isPrepaid())
                {
                	// TT#13041321003 Fixed. For bulkload, the period for startDate can be supplied via context from the bean-shell. 
                	// Otherwise it will default as per existing implementation.
                	int period = context.getInt("ALLOWED_PERIOD_FOR_SUBSCRIBER_START_DATE_BULKLOAD", 0);
					validAfter(context, oldService, newService,
					    SubscriberServicesXInfo.START_DATE,
                            Calendar.DAY_OF_MONTH, period);
                }
				validatePrior(context, oldService, newService,
				    SubscriberServicesXInfo.START_DATE, config);
                // protect against really, really off dates.
				validBefore(context, oldService, newService,
				    SubscriberServicesXInfo.START_DATE, Calendar.YEAR, 100);
            }
            catch (final IllegalPropertyArgumentException exception)
            {
                exceptions.thrown(exception);
            }
            try
            {
				validatePrior(context, oldService, newService,
				    SubscriberServicesXInfo.END_DATE, config);

                validateEndDateAfterStartDate(oldService, newService);

                // protect against really, really off dates.
				validBefore(context, oldService, newService,
				    SubscriberServicesXInfo.END_DATE, Calendar.YEAR, 100);
            }
            catch (final IllegalPropertyArgumentException exception)
            {
                exceptions.thrown(exception);
            }
        }

        exceptions.throwAll();
    }


    /**
     * Validates the end date of a service is later than the start date. If the end date
     * is not later than the start date, {@link IllegalPropertyArgumentException} is
     * thrown.
     *
     * @param oldService
     *            Old service to be validated.
     * @param service
     *            The service to validate.
     */
    protected static void validateEndDateAfterStartDate(final SubscriberServices oldService,
        final SubscriberServices service)
    {
        boolean validate = true;

        if (oldService != null && service != null)
        {
            final Date oldStartDate = oldService.getStartDate();
            final Date newStartDate = service.getStartDate();
            final Date oldEndDate = oldService.getEndDate();
            final Date newEndDate = service.getEndDate();

            validate = !SafetyUtil.safeEquals(oldStartDate, newStartDate)
                || !SafetyUtil.safeEquals(oldEndDate, newEndDate);
        }

        if (!service.isMandatory() && validate)
        {
            // end date must be later than start date
            final Date startDate = CalendarSupportHelper.get().getDateWithNoTimeOfDay(service.getStartDate());
            final Date endDate = CalendarSupportHelper.get().getDateWithNoTimeOfDay(service.getEndDate());
            if (!endDate.after(startDate))
            {
                throw new IllegalPropertyArgumentException(SubscriberServicesXInfo.END_DATE,
                    "Service end date must be later than start date");
            }
        }
    }


    /**
     * Converts a raw {@link Set} to a generic typed {@link SortedMap}.
     *
     * @param serviceSet
     *            The set being converted.
     * @param exceptions
     *            Exception holder.
     * @return A map of long to {@link SubscriberServices}.
     */
    protected static SortedMap<Long, SubscriberServices> convertSetToSortedMap(final Set serviceSet,
        final CompoundIllegalStateException exceptions)
    {
        final SortedMap<Long, SubscriberServices> serviceMap = new TreeMap<Long, SubscriberServices>();

        if (serviceSet != null)
        {
            for (final Iterator iterator = serviceSet.iterator(); iterator.hasNext();)
            {
                final Object service = iterator.next();
                if (service instanceof SubscriberServices)
                {
                    serviceMap.put(Long.valueOf(((SubscriberServices) service).getServiceId()),
                        (SubscriberServices) service);
                }
                else
                {
                    final IllegalStateException exception = new IllegalStateException(
                        "System Error: expecting SubscriberServiceDisplay in the Set only, getting "
                            + service.getClass() + " instead");
                    exceptions.thrown(exception);
                }
            }
        }
        return serviceMap;
    }

    /**
     * Singleton instance.
     */
    private static SubscriberServiceDisplayDatesValidator instance;
}
