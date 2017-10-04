package com.trilogy.app.crm.service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;


public class WeeklyPeriodHandler implements ServicePeriodHandler, ChargingCycleHandler
{
    /**
     * Number of days in a week.
     */
    public static final int DAYS_IN_WEEK = 7;
    
    public double calculateRate(final Context context, final Date startDate, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
        double rate;
        Date start = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(startDate);
        Date billing = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(billingDate);

        if (start.before(billing))
        {
            // Calculate rate for first month.
            rate = calculateRate(context, start, billingCycleDay, spid, subscriberId, item);

            // Move start to end of billing cycle.
            start = calculateCycleEndDate(context, start, billingCycleDay, spid, subscriberId, item);

            // While start before billing date, charge for next cycle as well.
            while (start.before(billing))
            {
                start = CalendarSupportHelper.get(context).getDayAfter(start);
                rate += calculateRate(context, start, billingCycleDay, spid, subscriberId, item);
                start = calculateCycleEndDate(context, start, billingCycleDay, spid, subscriberId, item);
            }
        }
        else
        {
            rate = calculateRate(context, billingDate, billingCycleDay, spid, subscriberId, item);
        }
        
        return rate;
        
    }

    public double calculateRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
        double result = 1.0;
        try
        {
            result = calculateRate(context, billingDate, billingCycleDay, spid, null, null);
        }
        catch (Throwable t)
        {
            // Ignored. Can't happen.
        }
        return result;
    }    
    
    public Date calculateCycleStartDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
        Date result = null;
        try
        {
            result = calculateCycleStartDate(context, billingDate, billingCycleDay, spid, null, null);
        }
        catch (Throwable t)
        {
            // Ignored. Can't happen.
        }
        return result;    
    }    
    
    public Date calculateCycleEndDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
        Date result = null;
        try
        {
            result = calculateCycleEndDate(context, billingDate, billingCycleDay, spid, null, null);
        }
        catch (Throwable t)
        {
            // Ignored. Can't happen.
        }
        return result;    
    } 
    
    public double calculateRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
        CRMSpid crmSpid = null;
        try
        {
            crmSpid = SpidSupport.getCRMSpid(context, spid);
        }
        catch (HomeException e)
        {
            throw new IllegalArgumentException("Error retrieving SPID " + spid, e);
        }
        
        final int spidDayOfWeek = crmSpid.getWeeklyRecurChargingDay().getIndex();
        final int curDayOfWeek = computeBillingDayOfWeek(billingDate);
        final double remDays = calculateRemainingDaysInWeek(curDayOfWeek, spidDayOfWeek);
        return remDays / DAYS_IN_WEEK;
    }
    
    public double calculateRefundRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
        return -1.0 * calculateRate(context, billingDate, billingCycleDay, spid, subscriberId, item);
    }
    
    public Date calculateCycleStartDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
        CRMSpid crmSpid;
        try
        {
            crmSpid = SpidSupport.getCRMSpid(context, spid);
        }
        catch (HomeException e)
        {
            throw new IllegalArgumentException("Error retrieving SPID " + spid, e);
        }
        int billingDayOfWeek = crmSpid.getWeeklyRecurChargingDay().getIndex();
        final Calendar calendar = Calendar.getInstance();
        if (billingDayOfWeek < calendar.getActualMinimum(Calendar.DAY_OF_WEEK)
            || billingDayOfWeek > calendar.getActualMaximum(Calendar.DAY_OF_WEEK))
        {
            throw new IllegalArgumentException("Invalid day of week");
        }
        calendar.setTime(billingDate);
        calendar.set(Calendar.DAY_OF_WEEK, billingDayOfWeek);
        if (calendar.getTime().after(billingDate))
        {
            calendar.add(Calendar.WEEK_OF_YEAR, -1);
        }
        return CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(calendar.getTime());
    }
    
    public Date calculateCycleEndDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
        CRMSpid crmSpid;
        try
        {
            crmSpid = SpidSupport.getCRMSpid(context, spid);
        }
        catch (HomeException e)
        {
            throw new IllegalArgumentException("Error retrieving SPID " + spid, e);
        }
        int billingDayOfWeek = crmSpid.getWeeklyRecurChargingDay().getIndex();
        final Calendar calendar = Calendar.getInstance();
        if (billingDayOfWeek < calendar.getActualMinimum(Calendar.DAY_OF_WEEK)
            || billingDayOfWeek > calendar.getActualMaximum(Calendar.DAY_OF_WEEK))
        {
            throw new IllegalArgumentException("Invalid day of week");
        }
        calendar.setTime(billingDate);
        calendar.set(Calendar.DAY_OF_WEEK, billingDayOfWeek);
        calendar.add(Calendar.DAY_OF_WEEK, -1);
        if (calendar.getTime().before(billingDate))
        {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        return CalendarSupportHelper.get(context).getDateWithLastSecondofDay(calendar.getTime());
    }
    
    /**
     * Determines the day of week of the billing date.
     *
     * @param billingDate
     *            Billing date.
     * @return The day of week the billing date falls on.
     */
    private int computeBillingDayOfWeek(final Date billingDate)
    {
        // get billing day of week
        final Calendar billDate = new GregorianCalendar();
        billDate.setTime(billingDate);
        return billDate.get(Calendar.DAY_OF_WEEK);
    }
    /**
     * Calculate how many days remaining in this week.
     *
     * @param curDayOfWeek
     *            Current day of the week.
     * @param spidDayOfWeek
     *            SPID's weekly recurring charge day of week.
     * @return The number of days remaining in this week.
     */
    private int calculateRemainingDaysInWeek(final int curDayOfWeek, final int spidDayOfWeek)
    {
        int remDays = 0;

        remDays = spidDayOfWeek - curDayOfWeek;
        if (remDays <= 0)
        {
            remDays += DAYS_IN_WEEK;
        }
        return remDays;
    }
    
    public static WeeklyPeriodHandler instance()
    {
        if (handler==null)
        {
            handler = new WeeklyPeriodHandler();
        }
        return handler;
    }
    
    private static WeeklyPeriodHandler handler = null;
    
    /**
	 * {@inheritDoc}
	 */
	public double calculateRefundRateBasedOnUsage(Context context,
			Date billingDate, int billingCycleDay, Date startDate, int spid,
			String subscriberId, Object item, int unbilledDays) throws HomeException {
		throw new UnsupportedOperationException("Operation Not Supported.");
	}
}
