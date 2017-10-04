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

package com.trilogy.app.crm.bas.promotion;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.HandsetPromotionGeneration;
import com.trilogy.app.crm.bean.HandsetPromotionGenerationXInfo;
import com.trilogy.app.crm.home.DateValidator;

/**
 * Validates the dates of a handset promotion.
 *
 * @author cindy.wong@redknee.com
 */
public class HandsetPromotionDatesValidator extends DateValidator
{

    /**
     * Create a new instance of <code>HandsetPromotionDatesValidator</code>.
     */
    protected HandsetPromotionDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>HandsetPromotionDatesValidator</code>.
     *
     * @return An instance of <code>HandsetPromotionDatesValidator</code>.
     */
    public static HandsetPromotionDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new HandsetPromotionDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final HandsetPromotionGeneration promotion = (HandsetPromotionGeneration) object;
        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
        try
        {
			validatePrior(context, null, promotion,
			    HandsetPromotionGenerationXInfo.GENERATION_DATE, config);
			validateAfter(context, null, promotion,
			    HandsetPromotionGenerationXInfo.GENERATION_DATE, config);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            final ExceptionListener ell = (ExceptionListener) context.get(ExceptionListener.class);
            if (ell != null)
            {
                ell.thrown(exception);
            }
            else
            {
                // rethrow
                throw exception;
            }
        }
    }

    /**
     * Singleton instance.
     */
    private static HandsetPromotionDatesValidator instance;
}
