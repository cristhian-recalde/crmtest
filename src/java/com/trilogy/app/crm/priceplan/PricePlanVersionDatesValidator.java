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

package com.trilogy.app.crm.priceplan;

import java.util.Calendar;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.home.DateValidator;
import com.trilogy.app.crm.priceplan.task.PricePlanVersionModificationLifecycleAgent;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * Date validator for {@link PricePlanVersion}.
 *
 * @author cindy.wong@redknee.com
 */
public class PricePlanVersionDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>PricePlanVersionDatesValidator</code>.
     */
    protected PricePlanVersionDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>PricePlanVersionDatesValidator</code>.
     *
     * @return An instance of <code>PricePlanVersionDatesValidator</code>.
     */
    public static PricePlanVersionDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new PricePlanVersionDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        // Validation is only done if it's not the price plan version modification agent that is trying to update the version.
        if (!context.getBoolean(PricePlanVersionModificationLifecycleAgent.PRICE_PLAN_VERSION_MODIFICATION_AGENT, Boolean.FALSE))
        {
            final PricePlanVersion newVersion = (PricePlanVersion) object;
            final Home home = (Home) context.get(PricePlanVersionHome.class);
            if (home == null)
            {
                final IllegalStateException exception = new IllegalStateException(
                    "System Error: Price Plan Version home not found in context!");
                new DebugLogMsg(this, exception.getMessage(), exception).log(context);
                throw exception;
            }
    
            final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
            if (config == null)
            {
                final IllegalStateException exception = new IllegalStateException(
                    "System Error: General Config not found in context!");
                new DebugLogMsg(this, exception.getMessage(), exception).log(context);
                throw exception;
            }
    
            final ExceptionListener el = (ExceptionListener) context.get(ExceptionListener.class);
    
            // we really should need to find the old PPV since they *should* not be editable after creation, but just in
            // case.
            PricePlanVersion oldVersion = null;
            try
            {
                oldVersion = PricePlanSupport.getCurrentVersion(context, newVersion.getId());
            }
            catch (HomeException exception)
            {
                new DebugLogMsg(this, "Cannot find old price plan version; assume null", exception).log(context);
            }
    
            try
            {
    			validAfter(context, oldVersion, newVersion,
    			    PricePlanVersionXInfo.ACTIVATE_DATE, Calendar.DAY_OF_MONTH, 0);
    
                // to guard against really, really off dates
    			validBefore(context, oldVersion, newVersion,
    			    PricePlanVersionXInfo.ACTIVATE_DATE, Calendar.YEAR, 100);
            }
            catch (IllegalPropertyArgumentException exception)
            {
                new DebugLogMsg(this, exception.getMessage(), exception).log(context);
                if (el != null)
                {
                    el.thrown(exception);
                }
                else
                {
                    throw new IllegalStateException(exception);
                }
            }
        }

    }

    /**
     * Singleton instance.
     */
    private static PricePlanVersionDatesValidator instance;

}
