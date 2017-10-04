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

import java.util.Calendar;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.support.SubscriberServicesSupport;

/**
 * Validates the dates of a {@link SubscriberServices}.
 *
 * @author cindy.wong@redknee.com
 */
public class SubscriberServicesDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>SubscriberServiceDatesValidator</code>.
     */
    protected SubscriberServicesDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>SubscriberServiceDatesValidator</code>.
     *
     * @return An instance of <code>SubscriberServiceDatesValidator</code>.
     */
    public static SubscriberServicesDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberServicesDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final SubscriberServices newServices = (SubscriberServices) object;
        final Home home = (Home) context.get(SubscriberServicesHome.class);
        if (home == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: SubscriberServicesHome not found in context");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
        if (config == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: GeneralConfig not found in context");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        // If Cannot find old SubscriberServices from home, assuming null
        SubscriberServices oldServices = SubscriberServicesSupport.getSubscriberServiceRecord(context, 
                newServices.getSubscriberId(), newServices.getServiceId(), newServices.getPath());

        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();
        try
        {
			validatePrior(context, oldServices, newServices,
			    SubscriberServicesXInfo.START_DATE, config);
            // protect against really, really off dates.
			validBefore(context, oldServices, newServices,
			    SubscriberServicesXInfo.START_DATE, Calendar.YEAR, 100);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            exceptions.thrown(exception);
        }

        try
        {
			validatePrior(context, oldServices, newServices,
			    SubscriberServicesXInfo.END_DATE, config);
            // protect against really, really off dates.
			validBefore(context, oldServices, newServices,
			    SubscriberServicesXInfo.END_DATE, Calendar.YEAR, 100);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            exceptions.thrown(exception);
        }

        exceptions.throwAll();
    }

    /**
     * Singleton instance.
     */
    private static SubscriberServicesDatesValidator instance;
}
