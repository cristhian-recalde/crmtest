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

package com.trilogy.app.crm.bas.promotion.home;

import java.util.Calendar;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.HandsetPromotionHistory;
import com.trilogy.app.crm.bean.HandsetPromotionHistoryHome;
import com.trilogy.app.crm.bean.HandsetPromotionHistoryXInfo;
import com.trilogy.app.crm.home.DateValidator;

/**
 * Validates the dates in {@link HandsetPromotionHistory}.
 *
 * @author cindy.wong@redknee.com
 */
public class HandsetPromotionHistoryDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>HandsetPromotionHistoryDatesValidator</code>.
     */
    protected HandsetPromotionHistoryDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>HandsetPromotionHistoryDatesValidator</code>.
     *
     * @return An instance of <code>HandsetPromotionHistoryDatesValidator</code>.
     */
    public static HandsetPromotionHistoryDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new HandsetPromotionHistoryDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final HandsetPromotionHistory newHistory = (HandsetPromotionHistory) object;
        final Home home = (Home) context.get(HandsetPromotionHistoryHome.class);
        if (home == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: Handset Promotion History home not found in context!");
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

        HandsetPromotionHistory oldHistory = null;
        try
        {
            oldHistory = (HandsetPromotionHistory) home.find(newHistory);
        }
        catch (HomeException exception)
        {
            new DebugLogMsg(this, "Problems finding existing handset promotion history, assuming null", exception)
                .log(context);
        }

        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();
        try
        {
			validatePrior(context, oldHistory, newHistory,
			    HandsetPromotionHistoryXInfo.GENERATION, config);
			validBefore(context, oldHistory, newHistory,
			    HandsetPromotionHistoryXInfo.GENERATION, Calendar.YEAR, 100);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            exceptions.thrown(exception);
        }

        try
        {
			validatePrior(context, oldHistory, newHistory,
			    HandsetPromotionHistoryXInfo.START_DATE, config);
			validBefore(context, oldHistory, newHistory,
			    HandsetPromotionHistoryXInfo.START_DATE, Calendar.YEAR, 100);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            exceptions.thrown(exception);
        }

        try
        {
			validatePrior(context, oldHistory, newHistory,
			    HandsetPromotionHistoryXInfo.END_DATE, config);
			validBefore(context, oldHistory, newHistory,
			    HandsetPromotionHistoryXInfo.END_DATE, Calendar.YEAR, 100);
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
    private static HandsetPromotionHistoryDatesValidator instance;
}
