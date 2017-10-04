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
import com.trilogy.app.crm.bean.PricePlanMigration;
import com.trilogy.app.crm.bean.PricePlanMigrationHome;
import com.trilogy.app.crm.bean.PricePlanMigrationXInfo;
import com.trilogy.app.crm.home.DateValidator;

/**
 * Validates the dates in a {@link PricePlanMigration}.
 *
 * @author cindy.wong@redknee.com
 */
public class PricePlanMigrationDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>PricePlanMigrationDatesValidator</code>.
     */
    protected PricePlanMigrationDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>PricePlanMigrationDatesValidator</code>.
     *
     * @return An instance of <code>PricePlanMigrationDatesValidator</code>.
     */
    public static PricePlanMigrationDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new PricePlanMigrationDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final PricePlanMigration newMigration = (PricePlanMigration) object;
        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
        if (config == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: GeneralConfig not in context");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        final Home home = (Home) context.get(PricePlanMigrationHome.class);
        if (home == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: PricePlanMigrationHome not in context");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        PricePlanMigration oldMigration = null;
        try
        {
            oldMigration = (PricePlanMigration) home.find(context, newMigration.ID());
        }
        catch (HomeException exception)
        {
            new DebugLogMsg(this, "Old PricePlanMigration not found in home; assume null", exception).log(context);
        }

        final ExceptionListener el = (ExceptionListener) context.get(ExceptionListener.class);
        try
        {
			validatePrior(context, oldMigration, newMigration,
			    PricePlanMigrationXInfo.MIGRATION_DATE, config);

            // to guard against really, really off dates
			validBefore(context, oldMigration, newMigration,
			    PricePlanMigrationXInfo.MIGRATION_DATE, Calendar.YEAR, 100);
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

    /**
     * Singleton instance.
     */
    private static PricePlanMigrationDatesValidator instance;
}
