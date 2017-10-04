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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.AutoDepositReleaseForm;
import com.trilogy.app.crm.bean.AutoDepositReleaseFormXInfo;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.home.DateValidator;

/**
 * Validates the dates from a {@link AutoDepositReleaseForm}.
 *
 * @author cindy.wong@redknee.com
 */
public class AutoDepositReleaseFormDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>AutoDepositReleaseFormDatesValidator</code>.
     */
    protected AutoDepositReleaseFormDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>AutoDepositReleaseFormDatesValidator</code>.
     *
     * @return An instance of <code>AutoDepositReleaseFormDatesValidator</code>.
     */
    public static AutoDepositReleaseFormDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new AutoDepositReleaseFormDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final AutoDepositReleaseForm form = (AutoDepositReleaseForm) object;
        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
        if (config == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: GeneralConfig does not exist in context");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

		validatePrior(context, null, form,
		    AutoDepositReleaseFormXInfo.ACTIVE_DATE, config);

        validBefore(context, null, form,
		    AutoDepositReleaseFormXInfo.ACTIVE_DATE, Calendar.DAY_OF_MONTH, 0);
    }

    /**
     * Singleton instance.
     */
    private static AutoDepositReleaseFormDatesValidator instance;
}
