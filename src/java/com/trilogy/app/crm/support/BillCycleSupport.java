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
package com.trilogy.app.crm.support;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bsh.This;

import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.WeekDayEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.core.BillCycle;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.msp.SpidHome;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.app.crm.support.CalendarSupportHelper;

/**
 * Provides utility functions for use with BillCycle.
 *
 * @author gary.anderson@redknee.com
 */
public final class BillCycleSupport
{

    /**
     * Minimum bill cycle day.
     */
    public static final int MIN_BILL_CYCLE_DAY = CoreCrmConstants.MIN_BILL_CYCLE_DAY;

    /**
     * Maximum bill cycle day.
     */
    public static final int MAX_BILL_CYCLE_DAY = CoreCrmConstants.MAX_BILL_CYCLE_DAY;
    
    /**
     * Special bill cycle day.
     */
    public static final int SPECIAL_BILL_CYCLE_DAY = CoreCrmConstants.SPECIAL_BILL_CYCLE_DAY;
    public static final int AUTO_BILL_CYCLE_START_ID = CoreCrmConstants.AUTO_BILL_CYCLE_START_ID;


    /**
     * Prevent instantiation of this utility class.
     */
    private BillCycleSupport()
    {
        // Empty
    }

    /**
     * Returns the start date of the billing week containing the given date.
     *
     * @param date
     *            The date for which to return the start of the week.
     * @param dayOfWeek
     *            The day of the week on which the billing week starts. Must be between
     *            {@link Calendar#SUNDAY} and {@link Calendar#SATURDAY}.
     * @return The start date of the billing week.
     */
    public static Date getStartDayOfBillingWeek(final Date date, final WeekDayEnum dayOfWeek)
    {
        return getCurrentWeekStartDate(date, dayOfWeek.getIndex());
    }


    /**
     * Returns the start date of the billing week containing the given date.
     *
     * @param date
     *            The date for which to return the start of the week.
     * @param dayOfWeek
     *            The day of the week on which the billing week starts. Must be between
     *            {@link Calendar#SUNDAY} and {@link Calendar#SATURDAY}.
     * @return The start date of the billing week.
     */
    public static Date getCurrentWeekStartDate(final Date date, final int dayOfWeek)
    {
        final Calendar calendar = Calendar.getInstance();
        if (dayOfWeek < calendar.getActualMinimum(Calendar.DAY_OF_WEEK)
            || dayOfWeek > calendar.getActualMaximum(Calendar.DAY_OF_WEEK))
        {
            throw new IllegalArgumentException("Invalid day of week");
        }
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        if (calendar.getTime().after(date))
        {
            calendar.add(Calendar.WEEK_OF_YEAR, -1);
        }
        return calendar.getTime();
    }


    /**
     * Returns the start date of the next billing week after the given date.
     *
     * @param date
     *            The date for which to return the start of the next billing week.
     * @param dayOfWeek
     *            The day of the week on which the billing week starts. Must be between
     *            {@link Calendar#SUNDAY} and {@link Calendar#SATURDAY}.
     * @return The start date of the next billing week.
     */
    public static Date getNextWeekStartDate(final Date date, final int dayOfWeek)
    {
        final Calendar calendar = Calendar.getInstance();
        if (dayOfWeek < calendar.getActualMinimum(Calendar.DAY_OF_WEEK)
            || dayOfWeek > calendar.getActualMaximum(Calendar.DAY_OF_WEEK))
        {
            throw new IllegalArgumentException("Invalid day of week");
        }
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        if (!calendar.getTime().after(date))
        {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        return calendar.getTime();
    }


    /**
     * Gets the start date of the current bill cycle of the given date.
     *
     * @param date
     *            The date for which to return the start of bill cycle.
     * @param day
     *            The day-of-the-month on which the bill cycle starts. Must be in [1, 28]
     *            interval.
     * @return The start date of the current bill cycle of the given date.
     * @throws IllegalArgumentException
     *             if day is not in the [1, 28] interval
     */
    public static Date getCurrentCycleStartDate(final Context ctx, final Date date, final int day)
    {
        if (day < MIN_BILL_CYCLE_DAY || day > MAX_BILL_CYCLE_DAY)
        {
            throw new IllegalArgumentException("BillCycle day must be between 1 and 28");
        }

        final Calendar cycleStartCalendar = Calendar.getInstance();
        cycleStartCalendar.setTime(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(date));
        cycleStartCalendar.set(Calendar.DAY_OF_MONTH, day);

        if (cycleStartCalendar.getTime().after(date))
        {
            cycleStartCalendar.add(Calendar.MONTH, -1);
        }

        return cycleStartCalendar.getTime();
    }

    /**
     * Gets the start date of the current bill cycle of the given date.
     *
     * @param date
     *            The date for which to return the start of bill cycle.
     * @param cycle
     *            The BillCycle with the day-of-the-month on which the bill cycle starts.
     * @return The start date of the current bill cycle of the given date.
     */
    public static Date getCurrentCycleStartDate(final Context ctx, final Date date, final BillCycle cycle)
    {
        return getCurrentCycleStartDate(ctx, date, cycle.getDayOfMonth());
    }


    /**
     * Gets the start date of the next bill cycle of the given date.
     *
     * @param date
     *            The date for which to return the start of bill cycle.
     * @param day
     *            The day-of-the-month on which the bill cycle starts.
     * @return The start date of the next bill cycle of the given date.
     */
    public static Date getNextCycleStartDate(final Context ctx, final Date date, final int day)
    {
        final Date currentCycleStartDate = getCurrentCycleStartDate(ctx, date, day);

        final Calendar cycleStartCalendar = Calendar.getInstance();
        cycleStartCalendar.setTime(currentCycleStartDate);
        cycleStartCalendar.add(Calendar.MONTH, 1);

        return cycleStartCalendar.getTime();
    }


    /**
     * Gets the start date of the next bill cycle of the given date.
     *
     * @param date
     *            The date for which to return the start of bill cycle.
     * @param cycle
     *            The BillCycle with the day-of-the-month on which the bill cycle starts.
     * @return The start date of the next bill cycle of the given date.
     */
    public static Date getNextCycleStartDate(final Context ctx, final Date date, final BillCycle cycle)
    {
        return getNextCycleStartDate(ctx, date, cycle.getDayOfMonth());
    }


    /**
     * Gets the start date of the previous bill cycle of the given date.
     *
     * @param date
     *            The date for which to return the start of the previous bill cycle.
     * @param day
     *            The day-of-the-month on which the bill cycle starts.
     * @return The start date of the previous bill cycle of the given date.
     */
    public static Date getPreviousCycleStartDate(final Context ctx, final Date date, final int day)
    {
        final Calendar cycleStartCalendar = Calendar.getInstance();
        cycleStartCalendar.setTime(getCurrentCycleStartDate(ctx, date, day));
        cycleStartCalendar.add(Calendar.MONTH, -1);

        return cycleStartCalendar.getTime();
    }
    
    /**
     * Gets the start date of the previous bill cycle of the given date.
     *
     * @param date
     *            The date for which to return the start of the previous bill cycle.
     * @param cycle
     *            The BillCycle with the day-of-the-month on which the bill cycle starts.
     * @return The start date of the previous bill cycle of the given date.
     */
    public static Date getPreviousCycleStartDate(final Context ctx, final Date date, final BillCycle cycle)
    {
        if (cycle != null)
        {
            return getPreviousCycleStartDate(ctx, date, cycle.getDayOfMonth());
        }

        return date;
    }


    /**
     * Gets the BillCycle used by the given account.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            The account for which to return the BillCycle.
     * @return The BillCycle used by the given account.
     * @throws HomeException
     *             Thrown if there is a problem accessing Home data in the context.
     */
    public static BillCycle getBillCycleForAccount(final Context ctx, final Account account) throws HomeException
    {
        final Home home = (Home) ctx.get(BillCycleHome.class);
        final BillCycle cycle = (BillCycle) home.find(ctx, new Integer(account.getBillCycleID()));
        return cycle;
    }


    /**
     * Gets the BillCycle used by the given subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to return the BillCycle.
     * @return The BillCycle used by the given subscriber.
     * @throws HomeException
     *             Thrown if there is a problem accessing Home data in the context.
     */
    public static BillCycle getBillCycleForSubscriber(final Context context, final Subscriber subscriber)
        throws HomeException
    {
        return getBillCycleForBan(context, subscriber.getBAN());
    }


    /**
     * Gets the BillCycle used by the given service provider.
     *
     * @param context
     *            The operating context.
     * @param provider
     *            The service provider for which to return the BillCycle.
     * @return The BillCycle used by the given service provider.
     * @throws HomeException
     *             Thrown if there is a problem accessing Home data in the context.
     */
    public static BillCycle getBillCycleForServiceProvider(final Context context, final CRMSpid provider)
        throws HomeException
    {
        final Home home = (Home) context.get(BillCycleHome.class);
        final BillCycle cycle = (BillCycle) home.find(context, new Integer(provider.getBillCycle()));
        return cycle;
    }


    /**
     * Gets the BillCycle used by the given service provider.
     *
     * @param context
     *            The operating context.
     * @param identifier
     *            The identifier of the service provider for which to return the
     *            BillCycle.
     * @return The BillCycle used by the given service provider.
     * @throws HomeException
     *             Thrown if there is a problem accessing Home data in the context.
     */
    public static BillCycle getBillCycleForSPID(final Context context, final int identifier) throws HomeException
    {
        final Home home = (Home) context.get(CRMSpidHome.class);
        final CRMSpid provider = (CRMSpid) home.find(context, new Integer(identifier));
        return getBillCycleForServiceProvider(context, provider);
    }


    /**
     * Gets the BillCycle used by the given account.
     *
     * @param context
     *            The operating context.
     * @param identifier
     *            The identifier of the account for which to return the BillCycle.
     * @return The BillCycle used by the given account.
     * @throws HomeException
     *             Thrown if there is a problem accessing Home data in the context.
     */
    public static BillCycle getBillCycleForBan(final Context context, final String identifier) throws HomeException
    {
        final Account account = HomeSupportHelper.get(context).findBean(context, Account.class, identifier);
        return account.getBillCycle(context);
    }


    /**
     * Returns the bill cycle with the provided identifier.
     *
     * @param context
     *            The operating context.
     * @param billCycleId
     *            Bill cycle identifier.
     * @return The bill cycle with the provided identifier.
     * @throws HomeException
     *             Thrown if there is a problem looking up the home.
     */
    public static BillCycle getBillCycle(final Context context, final int billCycleId) throws HomeException
    {
        final BillCycle billCycle = HomeSupportHelper.get(context).findBean(context, BillCycle.class, billCycleId);
        return billCycle;
    }


    /**
     * Indicates whether or not the given date occurs on the start day of the bill cycle
     * of the given account.
     *
     * @param context
     *            The operating context.
     * @param date
     *            The date of interest.
     * @param identifier
     *            The identifier of the account for which to return the BillCycle.
     * @return True if the given date occurs on the start day of the bill cycle; false
     *         otherwise.
     * @throws HomeException
     *             Thrown if there is a problem accessing Home data in the context.
     * @see This method unnecessarily causes an extra DB query.
     * Use isCycleStartOfAccount(Context, Date, Account) wherever you can.
     */
    public static boolean isCycleStartOfAccount(final Context context, final Date date, final String identifier)
            throws HomeException
    {
        final BillCycle cycle = getBillCycleForBan(context, identifier);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.DAY_OF_MONTH) == cycle.getDayOfMonth();
    }
    
    public static Date getDateForBillCycleStart(Context ctx, Date dateToProcess, int billCycleDay)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateToProcess);
        int month = calendar.get(Calendar.MONTH);
        
        if(billCycleDay > calendar.getTime().getDate())
        {
            month--;
        }

        GregorianCalendar billingStartDate = new GregorianCalendar(calendar.get(Calendar.YEAR), 
                month, billCycleDay);

        return CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(billingStartDate.getTime());
    }

    
    public static Date getDateForBillCycleEnd(Context ctx, Date dateToProcess, int billCycleDay)
    {
        Calendar nextBillingCal = Calendar.getInstance();
        nextBillingCal.setTime(getDateForBillCycleStart(ctx, dateToProcess, billCycleDay));
        nextBillingCal.add(Calendar.MONTH, 1);
        nextBillingCal.add(Calendar.SECOND, -1);

        return nextBillingCal.getTime();
    }


    /**
     * Determines whether the provided date is a start day of the provided bill cycle.
     *
     * @param billCycle
     *            Bill cycle of interest.
     * @param date
     *            Date of interest.
     * @return Whether the provided date is a start day of the provided bill cycle.
     */
    public static boolean isBillCycleStartDay(final BillCycle billCycle, final Date date)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH) == billCycle.getDayOfMonth();
    }

    
    /**
     * Determines the day of week of the billing date.
     *
     * @param billingDate
     *            Billing date.
     * @return The day of week the billing date falls on.
     */
    public static int computeBillingDayOfWeek(final Date billingDate)
    {
        // get billing day of week
        final Calendar billDate = new GregorianCalendar();
        billDate.setTime(billingDate);
        return billDate.get(Calendar.DAY_OF_WEEK);
    }
    
    
    /**
     * Return the bill cycle day for activation day.
     *
     * @param activationDay
     *            Activation day of month.
     * @return The bill cycle day for activation day.
     */
    public static int getBillCycleDayForActivationDay(final int activationDay)
        {
        return Math.min(activationDay, MAX_BILL_CYCLE_DAY);
        }


    /**
     * Returns a collection of all bill cycles belonging to the service provider.
     *
     * @param context
     *            The operating context.
     * @param spid
     *            Service provider identifier.
     * @return A collection of all bill cycles belonging to the service provider.
     * @throws HomeException
     *             Thrown if there are problems looking up the bill cycles.
     */
    public static Collection getAllBillCyclesForSpid(final Context context, final int spid) throws HomeException
        {
        final Home home = (Home) context.get(BillCycleHome.class);
        if (home == null)
        {
            throw new HomeException("Bill cycle home not found in context!");
        }
        return home.select(context, new EQ(BillCycleXInfo.SPID, Integer.valueOf(spid)));
    }


    /**
     * Returns a collection of all bill cycles with the provided bill cycle day and
     * belonging to the service provider.
     *
     * @param context
     *            The operating context.
     * @param spid
     *            Service provider identifier.
     * @param dayOfMonth
     *            Day of month of the bill cycle.
     * @return A collection of all bill cycles belonging to the service provider.
     * @throws HomeException
     *             Thrown if there are problems looking up the bill cycles.
     */
    public static Collection getAllBillCyclesForSpid(final Context context, final int spid, final int dayOfMonth)
        throws HomeException
    {
        final Home home = (Home) context.get(BillCycleHome.class);
        if (home == null)
        {
            throw new HomeException("Bill cycle home not found in context!");
    }
        final And and = new And();
        and.add(new EQ(BillCycleXInfo.DAY_OF_MONTH, Integer.valueOf(dayOfMonth)));
        and.add(new EQ(BillCycleXInfo.SPID, Integer.valueOf(spid)));
        return home.select(context, and);
    }
    

    /**
     * Gets the last day date of the billing cycle with the given BillCycle day.
     *
     * @param billCycleDay
     *            The given BillCycle day.
     * @return Date The last day date of the billing cycle to be returned.
     * @throws IllegalStateException
     *             Thrown if the given BillCycle day is invalid.
     */
    public static Date getDateOfBillCycleLastDay(final int billCycleDay, final Date billingDate)
        throws IllegalStateException
    {
        return getDateOfMultiMonthlyBillCycleLastDay(billCycleDay, billingDate, 1);
    }

    public static Date getDateOfMultiMonthlyBillCycleLastDay(final int billCycleDay, final Date cycleStartDate, int recurrenceInterval)
    throws IllegalStateException
{
    if (billCycleDay < MIN_BILL_CYCLE_DAY || billCycleDay > MAX_BILL_CYCLE_DAY)
    {
        throw new IllegalStateException("BillCycle day must be between 1 and 28");
    }
    
    if (recurrenceInterval<1)
    {
        throw new IllegalStateException("Recurrence interval must be greater than 0");
    }

    final Calendar cal = Calendar.getInstance();
    cal.setTime(cycleStartDate);

    final int dayOfBillingDate = cal.get(Calendar.DAY_OF_MONTH);
    if (dayOfBillingDate >= billCycleDay)
    {
        cal.add(Calendar.MONTH, recurrenceInterval);
    }
    else if (recurrenceInterval>1)
    {
        cal.add(Calendar.MONTH, recurrenceInterval-1);
    }
    cal.set(Calendar.DAY_OF_MONTH, billCycleDay);
    cal.add(Calendar.DAY_OF_MONTH, -1);

    return cal.getTime();
}

    
    /**
     * Gets the last day of the billing week containing the provided date.
     *
     * @param date
     *            The date to determine the billing week.
     * @param dayOfWeek
     *            The day of the week to be considered the start day of the billing week.
     *            The value must be between {@link Calendar#SUNDAY} and
     *            {@link Calendar#SATURDAY}.
     * @return The last day of the billing week containing the provided date.
     */
    public static Date getLastDayOfBillingWeek(final Date date, final WeekDayEnum dayOfWeek)
    {
        return getLastDayOfBillingWeek(date, dayOfWeek.getIndex());
    }


    /**
     * Gets the last day of the billing week containing the provided date.
     *
     * @param date
     *            The date to determine the billing week.
     * @param dayOfWeek
     *            The day of the week to be considered the start day of the billing week.
     *            The value must be between {@link Calendar#SUNDAY} and
     *            {@link Calendar#SATURDAY}.
     * @return The last day of the billing week containing the provided date.
     */
    public static Date getLastDayOfBillingWeek(final Date date, final int dayOfWeek)
    {
        final Calendar calendar = Calendar.getInstance();
        if (dayOfWeek < calendar.getActualMinimum(Calendar.DAY_OF_WEEK)
            || dayOfWeek > calendar.getActualMaximum(Calendar.DAY_OF_WEEK))
        {
            throw new IllegalArgumentException("Invalid day of week");
        }
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.add(Calendar.DAY_OF_WEEK, -1);
        if (calendar.getTime().before(date))
        {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        return calendar.getTime();
    }


    /**
     * Gets the BillCycleID based on billcycleDay .
     *
     * @param context
     *            The operating context.
     * @param billCycleDay
     *            day of month for which billCycle is to get
     * @param spid
     *            Service provider ID.
     * @return The BillCycleID to be returned. -1 indicates failure
     * @exception HomeException
     *                Thrown if failed to look for a BillCycle in the BillCycleHome.
     */
    public static int getAutoBillCycleID(final Context context, final int billCycleDay, final int spid)
        throws HomeException
    {
        final Home billCycleHome = (Home) context.get(BillCycleHome.class);
        if (billCycleHome == null)
        {
            throw new IllegalStateException("System Error: BillCycleHome does not exist in context");
        }

        Home home = billCycleHome.where(
                        context, 
                        new And()
                            .add(new EQ(BillCycleXInfo.SPID, spid))
                            .add(new EQ(BillCycleXInfo.DAY_OF_MONTH, billCycleDay)));
        
        XStatement where = new GTE(BillCycleXInfo.BILL_CYCLE_ID, AUTO_BILL_CYCLE_START_ID);
        Collection<BillCycle> subscriberBillCycleDays = home.select(context, where);
        if( subscriberBillCycleDays != null && subscriberBillCycleDays.size() > 0 )
        {
            return subscriberBillCycleDays.iterator().next().getBillCycleID();
        }
        
        // Didn't find an appropriate auto-bill cycle.  Someone must have deleted it or it hasn't been created yet.
        // Try to reprovision the auto-bill cycles and retry.
        createAutoBillCycles(context);
        subscriberBillCycleDays = home.select(context, where);
        if( subscriberBillCycleDays != null && subscriberBillCycleDays.size() > 0 )
        {
            return subscriberBillCycleDays.iterator().next().getBillCycleID();
        }

        // Still none.  Reprovisioning must have failed.  
        // Check for non-auto-bill cycles with this billing date since failure must be avoided at all costs!
        where = new LT(BillCycleXInfo.BILL_CYCLE_ID, AUTO_BILL_CYCLE_START_ID);
        subscriberBillCycleDays = home.select(context, where);
        if( subscriberBillCycleDays != null && subscriberBillCycleDays.size() > 0 )
        {
            return subscriberBillCycleDays.iterator().next().getBillCycleID();
        }
        
        return -1;
    }

    /**
     * REFERENCE: HLD section 5.2.14.2 Subscriber Auto-Bill Cycle Assignment
     * 
     * @param ctx Application Context
     * @return A Map of Bill Cycles to creation status.  Value "OK" indicates successful creation of a bill cycle.  Error message otherwise.  No entry will be added to the map for existing bill cycles.
     */
    private static Map<BillCycle, String> createAutoBillCycles(Context ctx)
    {
        PMLogMsg pm = new PMLogMsg(BillCycleSupport.class.getName(), "createAutoBillCycles()");
        try
        {
            Map<BillCycle, String> resultSet = new HashMap<BillCycle, String>();

            int startID = AUTO_BILL_CYCLE_START_ID;

            // Build an index of the existing auto bill cycle days per spid
            Map<Integer/*SPID*/, Set<Integer/*DayOfMonth*/>> billCycleIndex = new HashMap<Integer/*SPID*/, Set<Integer/*DayOfMonth*/>>();
            try
            {
                Home bcHome = (Home)ctx.get(BillCycleHome.class);
                Collection<BillCycle> existingBillCycles = bcHome.select(ctx, new GTE(BillCycleXInfo.BILL_CYCLE_ID, startID));
                if( existingBillCycles != null )
                {
                    for( BillCycle bc : existingBillCycles )
                    {
                        if( bc.getBillCycleID() >= startID )
                        {
                            startID = bc.getBillCycleID()+1;
                        }
                        
                        Set<Integer> daysOfMonth = billCycleIndex.get(bc.getSpid());
                        if( daysOfMonth == null )
                        {
                            daysOfMonth = new HashSet<Integer>();
                            billCycleIndex.put(bc.getSpid(), daysOfMonth);
                        }
                        
                        daysOfMonth.add(bc.getDayOfMonth());
                    }
                }
            }
            catch (HomeException e)
            {
                new MajorLogMsg(BillCycleSupport.class, "Unable to create auto-bill cycles due to error building index of existing auto bill cycles: " + e.getMessage(), null).log(ctx);
                new DebugLogMsg(BillCycleSupport.class, "Error building index of existing auto bill cycles: " + e.getMessage(), e).log(ctx);
                return resultSet;
            }

            Collection<SpidAware> spids = null;
            try
            {
                // Per Spid install the Auto-Bill Cycles
                Home spidHome = (Home)ctx.get(SpidHome.class);
                if( spidHome == null )
                {
                    new MajorLogMsg(BillCycleSupport.class, "Unable to create auto-bill cycles because no SPID home was found.", null).log(ctx);
                    return resultSet;
                }
                spids = spidHome.selectAll();
            }
            catch (HomeException e)
            {
                new MajorLogMsg(BillCycleSupport.class, "Unable to create auto-bill cycles due to error retrieving SPIDs: " + e.getMessage(), null).log(ctx);
                new DebugLogMsg(BillCycleSupport.class, "Error retrieving SPIDs: " + e.getMessage(), e).log(ctx);
                return resultSet;
            }
            
            if( spids != null )
            {
                for (SpidAware sProvider : spids)
                {            
                    int spid = sProvider.getSpid();
                    int nextID = startID+resultSet.size();
                    Set<Integer> existingDaysOfMonth = billCycleIndex.get(spid);
                    if( existingDaysOfMonth == null )
                    {
                        existingDaysOfMonth = new HashSet<Integer>();
                    }
                    installBillCyclesForSpid(ctx, spid, nextID, existingDaysOfMonth, resultSet);
                }
            }

            return resultSet;
        }
        finally
        {
            pm.log(ctx);
        }
    }


    private static int installBillCyclesForSpid(final Context ctx, final int spid, final int startID, final Set<Integer> existingDaysOfMonth, final Map<BillCycle, String> resultSet)
    {
        Home bcHome = (Home)ctx.get(BillCycleHome.class);
        
        int nextID = startID;

        // Provision the special bill cycle for this SPID
        BillCycle bc = new BillCycle();
        bc.setBillCycleID(nextID);
        bc.setSpid(spid);
        bc.setDescription("Special Auto-Bill Cycle");
        bc.setDayOfMonth(SPECIAL_BILL_CYCLE_DAY);
        if( !existingDaysOfMonth.contains(bc.getDayOfMonth()) )
        {
            // No existing bill cycle for this SPID/day of week.  Create it.
            try
            {
                resultSet.put((BillCycle)bcHome.create(bc), "OK");
                nextID++;
            }
            catch (HomeException e)
            {
                resultSet.put(bc, e.getMessage());
            }
        }
        else
        {
            new DebugLogMsg(BillCycleSupport.class,
                    "Auto-bill cycle with SPID " + spid + " & day of month " + bc.getDayOfMonth() + " already exists!  No need to create it again.  This is not an error.",
                    null).log(ctx);
        }

        for (int j=1; j<=28; j++)
        {
            bc = new BillCycle();
            bc.setBillCycleID(nextID);
            bc.setSpid(spid);
            bc.setDescription("Auto-Bill Cycle for day " + j);
            bc.setDayOfMonth(j);
            if( !existingDaysOfMonth.contains(bc.getDayOfMonth()) )
            {
                // No existing bill cycle for this SPID/day of week.  Create it.
                try
                {
                    resultSet.put((BillCycle)bcHome.create(bc), "OK");
                    nextID++;
                }
                catch (HomeException e)
                {
                    resultSet.put(bc, e.getMessage());
                }
            }
            else
            {
                new DebugLogMsg(BillCycleSupport.class,
                        "Auto-bill cycle with SPID " + spid + " & day of month " + bc.getDayOfMonth() + " already exists!  No need to create it again.  This is not an error.",
                        null).log(ctx);
            }
        }

        return nextID;
    }
    
    
    public static boolean isCycleStartOfAccount(final Context context, final Date date, Account account)
        throws HomeException
    {
        /*
         * This is just to log we are saving a DB query by not using the other variant of the 
         * method 'isCycleStartOfAccount'
         */
        if(LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(BillCycleSupport.class, 
                    MessageFormat.format("Skipping DB query for BAN {0} while fetching bill-cycle", 
                            new Object[]{account.getBAN()}), 
                    null);
        }
        
        final BillCycle cycle = account.getBillCycle(context);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.DAY_OF_MONTH) == cycle.getDayOfMonth();
    }

    

    public static boolean isCurrentDateInBillDelayPeriod(Context ctx, CRMSpid spid, int billCycleDay)
    {
       // int billDelayPeriod = spid.getBillDelayPeriod();
        Calendar currentDate = GregorianCalendar.getInstance();
        
        
        Date bilCycleStartDate = getDateForBillCycleStart(ctx, new Date(), billCycleDay);
        Calendar billingStartCalendar = Calendar.getInstance();
        billingStartCalendar.setTime(bilCycleStartDate);
        
        Calendar dateAfterbillDelayPeriod = Calendar.getInstance();
        dateAfterbillDelayPeriod.setTime(bilCycleStartDate);
        //dateAfterbillDelayPeriod.add(Calendar.DAY_OF_MONTH, billDelayPeriod);
        
        if(currentDate.after(billingStartCalendar) && currentDate.before(dateAfterbillDelayPeriod))
        {
            return Boolean.TRUE;
        }
        
        return Boolean.FALSE;
    }
    
    
} // class
