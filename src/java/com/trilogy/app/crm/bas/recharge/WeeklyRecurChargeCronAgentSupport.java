/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.bas.recharge;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CronTaskSupportHelper;

import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Process the input date
 * Moved from ApplyWeeklyRecurRechargeCronAgent
 * 
 * @author rchen
 *
 */
final class WeeklyRecurChargeCronAgentSupport
{
    /**
     * Minimum value accepted for day-of-week.
     */
    private static final int MIN_DAY_IN_WEEK = 1;

    /**
     * Maximum value accepted for day-of-week.
     */
    private static final int MAX_DAY_IN_WEEK = 7;

    /**
     * Invalid day-of-week parameter error message.
     */
    private static final String INVALID_MSG = "Invalid Parameter : Day Of Week must be Entered 1->Sunday,"
        + "2->Monday....7->Saturday";

    public static Date getRunningDate(final Context ctx)
    {
        final String billingDateString = CronTaskSupportHelper.get(ctx).getParameter1(ctx);
        final Calendar calendar = new GregorianCalendar();
        if (billingDateString != null && billingDateString.trim().length() > 0)
        {
            try
            {
                final int paramDayOfWeek = Integer.parseInt(billingDateString);
                final int currentDayOfWeek = BillCycleSupport.computeBillingDayOfWeek(calendar.getTime());
                if (paramDayOfWeek >= MIN_DAY_IN_WEEK && paramDayOfWeek <= MAX_DAY_IN_WEEK)
                {
                    if (paramDayOfWeek < currentDayOfWeek)
                    {
                        calendar.add(Calendar.DAY_OF_MONTH, paramDayOfWeek - currentDayOfWeek);
                    }
                    else if (paramDayOfWeek > currentDayOfWeek)
                    {
                        calendar.add(Calendar.DAY_OF_MONTH, paramDayOfWeek - currentDayOfWeek - MAX_DAY_IN_WEEK);
                    }
                }
                else
                {
                    throw new IllegalArgumentException();
                }
            }
            catch (final NumberFormatException e)
            {
                LogSupport.minor(ctx, WeeklyRecurChargeCronAgentSupport.class.getName(), INVALID_MSG);
            }
        }

        // remove time of day
        final Date currentDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(calendar.getTime());

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, WeeklyRecurChargeCronAgentSupport.class.getName(), "Date considered is " + currentDate);
        }
        
        return currentDate;
    }

}
