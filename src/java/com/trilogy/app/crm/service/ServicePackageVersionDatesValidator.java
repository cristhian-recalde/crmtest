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

package com.trilogy.app.crm.service;

import java.util.Calendar;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePackageVersionHome;
import com.trilogy.app.crm.bean.ServicePackageVersionXInfo;
import com.trilogy.app.crm.home.DateValidator;

/**
 * Validates the dates of a {@link ServicePackageVersion}.
 *
 * @author cindy.wong@redknee.com
 */
public class ServicePackageVersionDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>ServicePackageVersionDatesValidator</code>.
     */
    protected ServicePackageVersionDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>ServicePackageVersionDatesValidator</code>.
     *
     * @return An instance of <code>ServicePackageVersionDatesValidator</code>.
     */
    public static ServicePackageVersionDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new ServicePackageVersionDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final ServicePackageVersion newVersion = (ServicePackageVersion) object;

        final Home home = (Home) context.get(ServicePackageVersionHome.class);
        if (home == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: ServicePackageVersionHome not found in context!");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
        if (config == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: GeneralConfig not found in context!");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        ServicePackageVersion oldVersion = null;
        try
        {
            oldVersion = (ServicePackageVersion) home.find(context, newVersion.ID());
        }
        catch (HomeException exception)
        {
            new DebugLogMsg(this, "Exception caught; assuming no old version exists", exception).log(context);
        }

        try
        {
			validAfter(context, oldVersion, newVersion,
			    ServicePackageVersionXInfo.ACTIVATE_DATE,
			    Calendar.DAY_OF_MONTH, 0);
            // protect against really, really off dates.
			validBefore(context, oldVersion, newVersion,
			    ServicePackageVersionXInfo.ACTIVATE_DATE, Calendar.YEAR, 100);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Singleton instance.
     */
    private static ServicePackageVersionDatesValidator instance;
}
