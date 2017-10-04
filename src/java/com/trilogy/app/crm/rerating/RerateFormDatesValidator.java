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

package com.trilogy.app.crm.rerating;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.RerateForm;
import com.trilogy.app.crm.bean.RerateFormXInfo;
import com.trilogy.app.crm.home.DateValidator;

/**
 * Validate the dates in a {@link RerateForm}.
 *
 * @author cindy.wong@redknee.com
 */
public class RerateFormDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>RerateFormDatesValidator</code>.
     */
    protected RerateFormDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>RerateFormDatesValidator</code>.
     *
     * @return An instance of <code>RerateFormDatesValidator</code>.
     */
    public static RerateFormDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new RerateFormDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final RerateForm form = (RerateForm) object;
        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
        if (config == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: GeneralConfig does not exist in context");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();
        final ExceptionListener el = (ExceptionListener) context.get(ExceptionListener.class);

        try
        {
			validatePrior(context, null, form, RerateFormXInfo.START_DATE,
			    config);
			validateAfter(context, null, form, RerateFormXInfo.START_DATE,
			    config);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            if (el != null)
            {
                el.thrown(exception);
            }
            else
            {
                exceptions.thrown(exception);
            }
        }

        try
        {
			validatePrior(context, null, form, RerateFormXInfo.END_DATE, config);
			validateAfter(context, null, form, RerateFormXInfo.END_DATE, config);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            if (el != null)
            {
                el.thrown(exception);
            }
            else
            {
                exceptions.thrown(exception);
            }
        }

        exceptions.throwAll();
    }

    /**
     * Singleton instance.
     */
    private static RerateFormDatesValidator instance;
}
