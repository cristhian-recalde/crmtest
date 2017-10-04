package com.trilogy.app.crm.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;


public class MultiMonthlyPeriodHandler implements ServicePeriodHandler
{
    
    public double calculateRate(final Context context, final Date startDate, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item) throws HomeException
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

    public double calculateRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item) throws HomeException
    {
        Date itemStartDate = calculateItemStartDate(context, billingCycleDay, subscriberId, item);
        int recurrenceInterval = retrieveRecurrenceInterval(context, item);
        Date currentBillCycle = calculateCycleStartDate(billingDate, billingCycleDay, itemStartDate, recurrenceInterval);
        return calculateRate(context, billingDate, currentBillCycle, recurrenceInterval);
    }
    
    public double calculateRefundRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item) throws HomeException
    {
        return -1.0 * calculateRate(context, billingDate, billingCycleDay, spid, subscriberId, item);
    }

    public Date calculateCycleStartDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item) throws HomeException
    {
        Date itemStartDate = calculateItemStartDate(context, billingCycleDay, subscriberId, item);
        int recurrenceInterval = retrieveRecurrenceInterval(context, item);

        return calculateCycleStartDate(billingDate, billingCycleDay, itemStartDate, recurrenceInterval);
    }

    public Date calculateCycleEndDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item) throws HomeException
    {
        if (billingCycleDay < BillCycleSupport.MIN_BILL_CYCLE_DAY || billingCycleDay > BillCycleSupport.MAX_BILL_CYCLE_DAY)
        {
            throw new IllegalStateException("BillCycle day must be between 1 and 28");
        }

        Date cycleStartDate = calculateCycleStartDate(context, billingDate, billingCycleDay, spid, subscriberId, item);
        int recurrenceInterval = retrieveRecurrenceInterval(context, item);
        
        final Calendar cal = Calendar.getInstance();
        cal.setTime(cycleStartDate);

        final int dayOfBillingDate = cal.get(Calendar.DAY_OF_MONTH);
        if (dayOfBillingDate >= billingCycleDay)
        {
            cal.add(Calendar.MONTH, recurrenceInterval);
        }
        else if (recurrenceInterval>1)
        {
            cal.add(Calendar.MONTH, recurrenceInterval-1);
        }
        cal.set(Calendar.DAY_OF_MONTH, billingCycleDay);
        cal.add(Calendar.DAY_OF_MONTH, -1);

        return CalendarSupportHelper.get(context).getDateWithLastSecondofDay(cal.getTime());        
    }
    
    private Date calculateCycleStartDate(final Date billingDate, final int day, 
            final Date itemStartDate, final int recurrenceInterval)
    {
        Date itemStartDateCycle = getFirstMonthChargeStartDate(itemStartDate, day);
        Date currentDate = CalendarSupportHelper.get().getDateWithNoTimeOfDay(billingDate);
        Calendar itemBillingCycleCalendar = Calendar.getInstance();
        itemBillingCycleCalendar.setTime(itemStartDateCycle);
        
        if (CalendarSupportHelper.get().getDayOfMonth(itemStartDate) != day)
        {
            itemBillingCycleCalendar.add(Calendar.MONTH, (-recurrenceInterval+1));
        }
        
        while (!currentDate.before(itemBillingCycleCalendar.getTime()))
        {
            itemBillingCycleCalendar.add(Calendar.MONTH, recurrenceInterval);
        }
        
        itemBillingCycleCalendar.add(Calendar.MONTH, -recurrenceInterval);
        
        return itemBillingCycleCalendar.getTime();
    }    

    
    private int retrieveRecurrenceInterval(Context context, Object item) throws HomeException
    {
    	if (item instanceof AuxiliaryService)
    	{
    		return ((AuxiliaryService) item).getRecurrenceInterval();
    	}else if (item instanceof ServiceFee2)
        {
            return ((ServiceFee2) item).getRecurrenceInterval();
        }else if (item instanceof SubscriberServices)
        {
        	return ((SubscriberServices) item).getService(context).getRecurrenceInterval();
        }else if(item instanceof Service)
        {
        	Service service = (Service) item;
        	return service.getRecurrenceInterval();
        }
        else if (item instanceof SubscriberAuxiliaryService)
        {
            return ((SubscriberAuxiliaryService) item).getAuxiliaryService(context).getRecurrenceInterval();
        }        
        else if (item instanceof BundleFee)
        {
        	// @TODO: method name should be more readble.
        	try {
				return ((BundleFee)item).getBundleProfile(context).getRecurringStartValidity();
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
        }
        else if(item instanceof BundleProfile)
        {
        	BundleProfile bundleProfile = (BundleProfile) item;
        	return bundleProfile.getRecurringStartValidity();
        	
        }
        else if (item instanceof ServicePackageFee)
        {
            throw new IllegalArgumentException("Packages do not support by multimonthly fee.");
        }
        else
        {
            throw new IllegalArgumentException("Developers error: item should be instance of a subscriber fee.");
        }

    }
    
    private Date getFirstMonthChargeStartDate(final Date date, final int day)
    {
        if (day < BillCycleSupport.MIN_BILL_CYCLE_DAY || day > BillCycleSupport.MAX_BILL_CYCLE_DAY)
        {
            throw new IllegalArgumentException("BillCycle day must be between 1 and 28");
        }

        final Calendar cycleStartCalendar = Calendar.getInstance();
        cycleStartCalendar.setTime(CalendarSupportHelper.get().getDateWithNoTimeOfDay(date));
        cycleStartCalendar.set(Calendar.DAY_OF_MONTH, day);

        if (cycleStartCalendar.getTime().after(date))
        {
            cycleStartCalendar.add(Calendar.MONTH, -1);
        }

        return cycleStartCalendar.getTime();
    }
    
    private Date getAproximatedItemStartDate(Context ctx, String subscriberId, Object item, int recurrenceInterval, int billCycleDay) throws HomeException
    {
        Transaction lastTransaction;
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -recurrenceInterval);

        Date currentBillCycleDate = startDate.getTime();
        
        Subscriber subscriber = SubscriberSupport.getSubscriber(ctx, subscriberId);
        SubscriberSubscriptionHistory subHistory = SubscriberSubscriptionHistorySupport.getLastChargingEventSince(ctx, subscriberId, ChargedItemTypeEnum.SERVICE, item,  startDate.getTime());
        
        if (subHistory!=null)
        {
            currentBillCycleDate = getCurrentCycleStartDate(new Date(), subHistory.getLastBillCycleDate(), recurrenceInterval);
        }
                
        return currentBillCycleDate;
    }
    
    private Date getCurrentCycleStartDate(final Date date, final Date previousCycleDate, final int recurrenceInterval)
    {
        Date currentDate = CalendarSupportHelper.get().getDateWithNoTimeOfDay(date);
        Calendar itemBillingCycleCalendar = Calendar.getInstance();
        itemBillingCycleCalendar.setTime(previousCycleDate);
        
        while (!currentDate.before(itemBillingCycleCalendar.getTime()))
        {
            itemBillingCycleCalendar.add(Calendar.MONTH, recurrenceInterval);
        }
        
        itemBillingCycleCalendar.add(Calendar.MONTH, -recurrenceInterval);
        
        return itemBillingCycleCalendar.getTime();
    }
    
    private Date calculateItemStartDate(final Context context, final int billCycleDay, final String subscriberId, final Object item) throws HomeException
    {
        if (item instanceof ServiceFee2 || item instanceof Service)
        {
            long serviceId = 0;
            int recurrenceInterval = 1;
            String path = SubscriberServicesUtil.DEFAULT_PATH;
            if (item instanceof ServiceFee2)
            {
                ServiceFee2 serviceFee =  ((ServiceFee2) item);
                serviceId = serviceFee.getServiceId();
                recurrenceInterval = serviceFee.getRecurrenceInterval();
                path = serviceFee.getPath();
            }
            else
            {
                Service service = ((Service) item);
                serviceId = service.getID();
                recurrenceInterval = service.getRecurrenceInterval();
            }
            
            SubscriberServices subscriberService = SubscriberServicesSupport.getSubscriberServiceRecord(context, subscriberId, serviceId, path);
            if (subscriberService!=null)
            {
                return subscriberService.getStartDate();
            }
            else
            {
                return getAproximatedItemStartDate(context, subscriberId, item, recurrenceInterval, billCycleDay);
            }
        }else if (item instanceof SubscriberServices)
        {
        	return ((SubscriberServices) item).getStartDate();
        }
        else if (item instanceof SubscriberAuxiliaryService)
        {
            return ((SubscriberAuxiliaryService) item).getStartDate();
        }else if (item instanceof AuxiliaryService)
        {
        	return ((AuxiliaryService) item).getStartDate();
        }
        else if (item instanceof BundleFee)
        {
            return ((BundleFee)item).getStartDate();
        }
        else if(item instanceof BundleProfile)
        {
        	BundleProfile bundleProfile = (BundleProfile) item;
        	Subscriber sub = (Subscriber) context.get(Subscriber.class);
        	if(sub == null )
        	{
        		try {
					sub = SubscriberSupport.getSubscriber(context, subscriberId);
				} catch (HomeException e) {
					throw new IllegalArgumentException(e);
				}
        	}
        	
        	Map<Long, BundleFee> bundleIdToBundleFeeMap  = sub.getBundles();
        	if(bundleIdToBundleFeeMap == null || bundleIdToBundleFeeMap.size() <=0 )
        	{
        		/*
        		 * cached subscriber might not have bundle-fee initialized.
        		 */
        		try {
					sub = SubscriberSupport.getSubscriber(context, subscriberId);
				} catch (HomeException e) {
					throw new IllegalArgumentException(e);
				}
        	}
        	
        	if(bundleIdToBundleFeeMap != null && bundleIdToBundleFeeMap.size() >0 )
        	{
        		BundleFee bundleFee = bundleIdToBundleFeeMap.get(bundleProfile.getBundleId());
        		if(bundleFee != null)
        		{
        			return bundleFee.getStartDate();
        		}
        	}
        	throw new IllegalArgumentException("Unable to find BundleFee associated with Bundle");
        	
        }
        else if (item instanceof ServicePackageFee)
        {
            throw new IllegalArgumentException("Packages do not support by multimonthly fee.");
        }
        else
        {
            throw new IllegalArgumentException("Developers error: item should be instance of a subscriber fee.");
        }
    }

    private double calculateRate(final Context context, final Date billingDate,
            final Date currentBillCycle, final int recurrenceInterval)
    {
        int daysInBillingCycle = 0;
        int remainingDays = 0;

        Calendar billingDateWithNoTimeOfDay = Calendar.getInstance();
        billingDateWithNoTimeOfDay.setTime(CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(billingDate));
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentBillCycle);
        
        for (int i=0; i<recurrenceInterval; i++)
        {
            int daysInMonth = CalendarSupportHelper.get(context).getNumberOfDaysInMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
            daysInBillingCycle += daysInMonth;
            
            // if billing date is after this bill cycle, we need to calculate if any day in this month is remaining
            if (billingDateWithNoTimeOfDay.getTime().after(calendar.getTime()))
            {
                calendar.add(Calendar.MONTH, 1);
                // if billing date is before next bill cycle, we need to calculate the amount of days remaining.
                if (billingDateWithNoTimeOfDay.getTime().before(calendar.getTime()))
                {
                    if (billingDateWithNoTimeOfDay.get(Calendar.DAY_OF_MONTH) < calendar.get(Calendar.DAY_OF_MONTH))
                    {
                        remainingDays = calendar.get(Calendar.DAY_OF_MONTH) - billingDateWithNoTimeOfDay.get(Calendar.DAY_OF_MONTH);
                    }
                    else
                    {
                        remainingDays = daysInMonth - billingDateWithNoTimeOfDay.get(Calendar.DAY_OF_MONTH) + calendar.get(Calendar.DAY_OF_MONTH);
                    }
                }
                
            }
            // if billing date is before this bill cycle, all days in month are remaining.
            else
            {
                calendar.add(Calendar.MONTH, 1);
                remainingDays += daysInMonth;
            }
        }
        
        return remainingDays * 1.0 / daysInBillingCycle;
    }   
    
    public double calculateRefundRateBasedOnUsage(Context context,
			Date billingDate, int billingCycleDay, Date startDate, int spid,
			String subscriberId, Object item, int unbilledDays) throws HomeException 
	{
    	Date itemStartDate = calculateItemStartDate(context, billingCycleDay, subscriberId, item);
        int recurrenceInterval = retrieveRecurrenceInterval(context, item);
        Date currentBillCycle = calculateCycleStartDate(billingDate, billingCycleDay, itemStartDate, recurrenceInterval);
        
        int usageDays = 0;
    	int daysInBillingCycle = 0;

        Calendar billingDateWithNoTimeOfDay = Calendar.getInstance();
        billingDateWithNoTimeOfDay.setTime(CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(billingDate));
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentBillCycle);
        
        Calendar startDateCalender = Calendar.getInstance();
        startDateCalender.setTime(startDate);
        
        for (int i=0; i<recurrenceInterval; i++)
        {
        	int daysInMonth = CalendarSupportHelper.get(context).getNumberOfDaysInMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
            daysInBillingCycle += daysInMonth;
            
         // if billing date is after this bill cycle
            if (billingDateWithNoTimeOfDay.getTime().after(calendar.getTime()))
            {
                calendar.add(Calendar.MONTH, 1);
                // if billing date is before next bill cycle, usage days will be partial
                if (billingDateWithNoTimeOfDay.getTime().before(calendar.getTime()))
                {
                	if (billingDateWithNoTimeOfDay.get(Calendar.DAY_OF_MONTH) < calendar.get(Calendar.DAY_OF_MONTH))
                    {
                		usageDays += daysInMonth - (calendar.get(Calendar.DAY_OF_MONTH) - billingDateWithNoTimeOfDay.get(Calendar.DAY_OF_MONTH));
                    }
                	else
                	{
                		usageDays += billingDateWithNoTimeOfDay.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH);
                	}
                } 
                else 
                {
                	usageDays += daysInMonth;
                }
            }
         // if billing date is before this bill cycle, no usage days in this month
            else
            {
                calendar.add(Calendar.MONTH, 1);
                usageDays += 0;
            }
        }
        
     usageDays+=unbilledDays;
     
	     // BSS-3358: In case of bill end date is a day before BSD full charge was getting refund
	     // Also, now we need to exclude current day from getting refund so increasing 1Day from remainingDays
	     if(usageDays < daysInBillingCycle){
	    	 usageDays++;
	     }
        
        if(LogSupport.isDebugEnabled(context))
		{
			LogSupport.debug(context, this, "Calculating Refund rate based on UsageDays = "+usageDays);
		}
    	
		return -1.0 * usageDays  * 1.0 / daysInBillingCycle;
	}

    public static MultiMonthlyPeriodHandler instance()
    {
        if (handler==null)
        {
            handler = new MultiMonthlyPeriodHandler();
        }
        return handler;
    }
    
    private static MultiMonthlyPeriodHandler handler = null;
}
