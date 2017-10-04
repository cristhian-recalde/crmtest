package com.trilogy.app.crm.service;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bundle.BundleFee;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.InvalidBundleApiException;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * @author kabhay
 *
 */
public class MultiDayPeriodHandler implements ServicePeriodHandler, ChargingCycleHandler
{
	public static final String CALCULATE_END_DATE_FROM_CYCLE_START = "CALCULATE_END_DATE_FROM_CYCLE_START";
	
    public double calculateRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
    	/*
    	 * For MULTI-DAY service, initial charge would always be FULL.
    	 */
        return 1.0;
    }

    public Date calculateCycleStartDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
    	/*
    	 * BillingDate is actually start date which in turn is actually taken from nextRecurringChargeDate of SubscriberServices/SubscriberAuxiliaryServices
    	 */
    	return CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(billingDate);
    }    
    
    public Date calculateCycleEndDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
    	return null;
    } 

    public double calculateRate(final Context context, final Date startDate, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
    	/*
    	 * For MULTI-DAY service, initial charge would always be FULL.
    	 */
        
        return 1.0;
    }
    
    public double calculateRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
    	/*
    	 * For MULTI-DAY service, initial charge would always be FULL.
    	 */
        
        return 1.0;
    }
    

    public double calculateRefundRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
    	Context subContext = context.createSubContext();
    	subContext.put(CALCULATE_END_DATE_FROM_CYCLE_START, false);
    	int numberOfDays = 0;	
    	Date nextRecurringChargeDate = null;
    	if (item instanceof ServiceFee2)
    	{	
    		ServiceFee2 serviceFee =  ((ServiceFee2) item);
    		numberOfDays = serviceFee.getRecurrenceInterval();
    		
    		SubscriberServices ss = SubscriberServicesSupport.getSubscriberServiceRecord(context, subscriberId, serviceFee.getServiceId(), serviceFee.getPath());
    		if(ss != null)
    		{
    			nextRecurringChargeDate = ss.getNextRecurringChargeDate();
    		}
    		
    		if(nextRecurringChargeDate ==null)
    		{
    			/*
    			 * This means that association has been deleted before charging.
    			 * Calculate nextRecurringChargeDatefrom from cycle start date 
    			 */
    			
    			Date cycleStartDate  = calculateCycleStartDate(context, billingDate, billingCycleDay, spid, subscriberId, item);
    			Date cycleEndDate = calculateCycleEndDate(subContext, cycleStartDate, billingCycleDay, spid, subscriberId, item);
    			
    			Calendar cal = Calendar.getInstance();
    			cal.setTime(cycleEndDate);
    			cal.add(Calendar.DAY_OF_YEAR, 1);
    			nextRecurringChargeDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime());
    			
    		}
    	}else if(item instanceof Service)
        {
    		Service service =  ((Service) item);
    		numberOfDays = service.getRecurrenceInterval();
    		
    		SubscriberServices  subService = SubscriberServicesSupport.getSubscriberServiceRecord(context, subscriberId, service.getID(), SubscriberServicesUtil.DEFAULT_PATH);
    		if(subService != null)
    		{
    			nextRecurringChargeDate = subService.getNextRecurringChargeDate();
    		}
    		
    		if(nextRecurringChargeDate ==null)
    		{
    			/*
    			 * This means that association has been deleted before charging.
    			 * Calculate nextRecurringChargeDatefrom from cycle start date 
    			 */
    			
    			Date cycleStartDate  = calculateCycleStartDate(context, billingDate, billingCycleDay, spid, subscriberId, item);
    			Date cycleEndDate = calculateCycleEndDate(subContext, cycleStartDate, billingCycleDay, spid, subscriberId, item);
    			
    			Calendar cal = Calendar.getInstance();
    			cal.setTime(cycleEndDate);
    			cal.add(Calendar.DAY_OF_YEAR, 1);
    			nextRecurringChargeDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime());
    			
    		}
        }else if (item instanceof AuxiliaryService)
        {
        	AuxiliaryService auxService =  ((AuxiliaryService) item);
        	numberOfDays = auxService.getRecurrenceInterval();
        	
        	Collection<SubscriberAuxiliaryService> coll = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(context, subscriberId, auxService.getID());
        	if(coll != null && coll.size() >0 )
        	{
        		SubscriberAuxiliaryService sas = coll.iterator().next();
        		nextRecurringChargeDate = sas.getNextRecurringChargeDate();
        	}
        	
        	if(nextRecurringChargeDate ==null)
    		{
    			/*
    			 * This means that association has been deleted before charging.
    			 * Calculate nextRecurringChargeDatefrom from cycle start date 
    			 */
    			
    			Date cycleStartDate  = calculateCycleStartDate(context, billingDate, billingCycleDay, spid, subscriberId, item);
    			Date cycleEndDate = calculateCycleEndDate(subContext, cycleStartDate, billingCycleDay, spid, subscriberId, item);
    			
    			Calendar cal = Calendar.getInstance();
    			cal.setTime(cycleEndDate);
    			cal.add(Calendar.DAY_OF_YEAR, 1);
    			nextRecurringChargeDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime());
    			
    		}
        	
        }else if(item instanceof SubscriberAuxiliaryService)
        {
        	SubscriberAuxiliaryService sas =  ((SubscriberAuxiliaryService) item);
        	nextRecurringChargeDate = sas.getNextRecurringChargeDate();
        	AuxiliaryService auxService;
			try {
				auxService = sas.getAuxiliaryService(context);
				numberOfDays = auxService.getRecurrenceInterval();
			} catch (HomeException e) {
				 throw new IllegalArgumentException(e);
			}
			
			if(nextRecurringChargeDate ==null)
    		{

    			
    			Date cycleStartDate  = calculateCycleStartDate(context, billingDate, billingCycleDay, spid, subscriberId, item);
    			Date cycleEndDate = calculateCycleEndDate(subContext, cycleStartDate, billingCycleDay, spid, subscriberId, item);
    			
    			Calendar cal = Calendar.getInstance();
    			cal.setTime(cycleEndDate);
    			cal.add(Calendar.DAY_OF_YEAR, 1);
    			nextRecurringChargeDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime());
    			
    		}
    		
    		
        }
        else if (item instanceof BundleFee)
        {
            BundleFee bundleFee = (BundleFee) item;
            nextRecurringChargeDate = bundleFee.getNextRecurringChargeDate();
        	try {
				BundleProfile bundleProfile = BundleSupportHelper.get(context).getBundleProfile(context, bundleFee.getId());
				numberOfDays = bundleProfile.getRecurringStartValidity();

			} catch (InvalidBundleApiException e) {
				throw new IllegalArgumentException(e);
			} catch (HomeException e) {
				throw new IllegalArgumentException(e);
			}
        	
        	
        	if(nextRecurringChargeDate ==null)
    		{

    			
    			Date cycleStartDate  = calculateCycleStartDate(context, billingDate, billingCycleDay, spid, subscriberId, item);
    			Date cycleEndDate = calculateCycleEndDate(subContext, cycleStartDate, billingCycleDay, spid, subscriberId, item);
    			
    			Calendar cal = Calendar.getInstance();
    			cal.setTime(cycleEndDate);
    			cal.add(Calendar.DAY_OF_YEAR, 1);
    			nextRecurringChargeDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime());
    			
    		}
        	
        	
        }else
        {
        	 throw new IllegalArgumentException("Developers error: item should be instance of ServiceFee2,Service, AuxiliaryService, SubscriberAuxiliaryService, BundleProfile or BundleFee!!");
        }
    	
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(nextRecurringChargeDate);
    	cal.add(Calendar.DAY_OF_YEAR, -1);//cycle end date 
    	Date cycleEndDate =  cal.getTime();
    	
    	long diffInMillis = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cycleEndDate).getTime() - CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(billingDate).getTime();
    	long numberOfDaysRemaining = diffInMillis/com.redknee.app.crm.CommonTime.MILLIS_IN_DAY + 1; // both days should be inclusive 
    	
    	return (-1.0*numberOfDaysRemaining/numberOfDays);
        
    }
    
    
    public Date calculateCycleStartDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
    	/*
    	 * There are two places from where this method would be called :
    	 * 
    	 * 1. During charging for the service/aux-service/bundle. 
    	 * 	  Charging module will call this method as part of validation whether charge has already been applied during the span of multi day service period or not.
    	 * 	  In this case, association's next-recurring-charge-date (SubscriberServices, SubscriberAuxiliaryServices and BundleFee ) will help determining the cycle start date.
    	 * 
    	 *  2. While associating non-refundable service/Aux-Service/Bundle this method would be called to get the start-date for calculation of next-recurring-charge-date.   
    	 *  	This is important because if charge has already been applied and was not refunded while unprovisioning ( isRefundbale = false of service/aux-service/bundle) then no transaction would be create during provisioning the same service again within the billing period
    	 *  	and if no transaction is created next-recurring-charge-date will not be set. charging history would help in determining the cycle-start-date and consequently next-recurring-charge-date.
    	 *  	
    	 * 		If there is a refund already ( i.e. isRefundable = true ), then cycle star date would be billing-date ( which is coming as an input to this method )
    	 * 
    	 */
    	Date billingDayWithNoTimeOfDay =  CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(billingDate);
    	
    	int numberOfDays = 0;
    	Date nextRecurringChargeDate = null;
    	SubscriberSubscriptionHistory chargingHist = null;
    	
        if (item instanceof ServiceFee2)
        {
        	ServiceFee2 serviceFee =  ((ServiceFee2) item);
            numberOfDays = serviceFee.getRecurrenceInterval();
            SubscriberServices ss = SubscriberServicesSupport.getSubscriberServiceRecord(context, subscriberId, serviceFee.getServiceId(), serviceFee.getPath());
        	
            if(ss != null)
        	{
        		nextRecurringChargeDate = ss.getNextRecurringChargeDate();
        	}
            
            if(nextRecurringChargeDate == null)
        	{

                /*
                 * If next-recurring-charge-date is null then look into charging history 
                 * 
                 */
        		
        		
        		Calendar cal  = Calendar.getInstance();
        		cal.setTime(new Date());
        		cal.add(Calendar.DAY_OF_YEAR, -numberOfDays); // if service is for 20 days , check from 20 days ago
        		Date since = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime()); 
        		try {

        	        chargingHist = SubscriberSubscriptionHistorySupport.getLastChargingEventSince(context, subscriberId, ChargedItemTypeEnum.SERVICE, item, since);

				} catch (HomeException e) {
					throw new IllegalArgumentException(e);
				}
        	}
            


			
			
        }else if(item instanceof Service)
        {
        	Service service = (Service) item;
        	numberOfDays = service.getRecurrenceInterval();
        	SubscriberServices ss = SubscriberServicesSupport.getSubscriberServiceRecord(context, subscriberId, service.getID(), SubscriberServicesUtil.DEFAULT_PATH);
        	
        	if(ss != null)
        	{
        		nextRecurringChargeDate = ss.getNextRecurringChargeDate();
        	}
        	
        	if(nextRecurringChargeDate == null)
        	{

                /*
                 * If next-recurring-charge-date is null then one of the reasons is refund has happened. ( service is unprovisioned and its association is removed from subscriberservices)
                 * Check in charging history then 
                 */
        		
        		
        		Calendar cal  = Calendar.getInstance();
        		cal.setTime(new Date());
        		cal.add(Calendar.DAY_OF_YEAR, -numberOfDays);
        		Date since = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime()); 
        		try {

        	        chargingHist = SubscriberSubscriptionHistorySupport.getLastChargingEventSince(context, subscriberId, ChargedItemTypeEnum.SERVICE, item, since);
				} catch (HomeException e) {
					throw new IllegalArgumentException(e);
				}
        	}
        	

        }
        else if (item instanceof AuxiliaryService)
        {
        	AuxiliaryService auxService = (AuxiliaryService) item;
        	numberOfDays = auxService.getRecurrenceInterval();
        	Collection<SubscriberAuxiliaryService> coll = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(context, subscriberId, auxService.getID());
        	
        	if( coll != null && coll.size()>0)
        	{
        		nextRecurringChargeDate = coll.iterator().next().getNextRecurringChargeDate();
        	}
        	
        	
        	if(nextRecurringChargeDate == null)
        	{

                /*
                 * If next-recurring-charge-date is null then one of the reasons is refund has happened.
                 * Check in charging history then 
                 */
        		
        		
        		Calendar cal  = Calendar.getInstance();
        		cal.setTime(new Date());
        		cal.add(Calendar.DAY_OF_YEAR, -numberOfDays);
        		Date since = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime()); 
        		try {
        			chargingHist = SubscriberSubscriptionHistorySupport.getLastChargingEventSince(context, subscriberId, ChargedItemTypeEnum.AUXSERVICE, item, since);
        	        
				} catch (HomeException e) {
					throw new IllegalArgumentException(e);
				}
        	}
        	

        }else if(item instanceof SubscriberAuxiliaryService)
        {
        	SubscriberAuxiliaryService sas = (SubscriberAuxiliaryService) item;
        	try {
        		nextRecurringChargeDate = sas.getNextRecurringChargeDate();
				AuxiliaryService auxService = sas.getAuxiliaryService(context);
	        	numberOfDays = auxService.getRecurrenceInterval();
	        	
	        	
	        	if(nextRecurringChargeDate == null)
	        	{

	                /*
	                 * If next-recurring-charge-date is null then one of the reasons is refund has happened.
	                 * Check in charging history then 
	                 */
	        		
	        		
	        		Calendar cal  = Calendar.getInstance();
	        		cal.setTime(new Date());
	        		cal.add(Calendar.DAY_OF_YEAR, -numberOfDays);
	        		Date since = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime()); 
	        		try {
	        			chargingHist = SubscriberSubscriptionHistorySupport.getLastChargingEventSince(context, subscriberId, ChargedItemTypeEnum.AUXSERVICE, item, since);
	        	        
					} catch (HomeException e) {
						throw new IllegalArgumentException(e);
					}
	        	}

			} catch (HomeException e) {

				throw new IllegalArgumentException("Exception occurred while retrieving Aux service for ID : " + sas.getAuxiliaryServiceIdentifier());
			}
        	
        }else if(item instanceof BundleProfile)
        {
        	BundleProfile bundleProfile = (BundleProfile) item;
        	numberOfDays = bundleProfile.getRecurringStartValidity();
        	
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
        			nextRecurringChargeDate = bundleFee.getNextRecurringChargeDate();
        		}
        	}
        	
        	if(nextRecurringChargeDate == null)
        	{

                /*
                 * If next-recurring-charge-date is null then one of the reasons is, refund has happened.
                 * Check in charging history then 
                 */
        		
        		
        		Calendar cal  = Calendar.getInstance();
        		cal.setTime(new Date());
        		cal.add(Calendar.DAY_OF_YEAR, -numberOfDays);
        		Date since = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime()); 
        		try {
        			ChargedItemTypeEnum chargedItemType = bundleProfile.isAuxiliary() ?ChargedItemTypeEnum.AUXBUNDLE:ChargedItemTypeEnum.BUNDLE;
        	        chargingHist = SubscriberSubscriptionHistorySupport.getLastChargingEventSince(context, subscriberId, chargedItemType, item, since);
        	        
				} catch (HomeException e) {
					throw new IllegalArgumentException(e);
				}
        	}
        	

        }
        else if (item instanceof BundleFee)
        {
            BundleFee bundleFee = (BundleFee) item;
        	try {
        		nextRecurringChargeDate  = bundleFee.getNextRecurringChargeDate();
				BundleProfile bundleProfile = BundleSupportHelper.get(context).getBundleProfile(context, bundleFee.getId());
				numberOfDays = bundleProfile.getRecurringStartValidity();

				
				if(nextRecurringChargeDate == null)
	        	{

	                /*
	                 * If next-recurring-charge-date is null then one of the reasons is refund has happened.
	                 * Check in charging history then 
	                 */
	        		
	        		
	        		Calendar cal  = Calendar.getInstance();
	        		cal.setTime(new Date());
	        		cal.add(Calendar.DAY_OF_YEAR, -numberOfDays);
	        		Date since = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime()); 
	        		try {
	        			ChargedItemTypeEnum chargedItemType = bundleProfile.isAuxiliary() ?ChargedItemTypeEnum.AUXBUNDLE:ChargedItemTypeEnum.BUNDLE;
	        	        chargingHist = SubscriberSubscriptionHistorySupport.getLastChargingEventSince(context, subscriberId, chargedItemType, item, since);
					} catch (HomeException e) {
						throw new IllegalArgumentException(e);
					}
	        	}
				
				
			} catch (InvalidBundleApiException e) {
				throw new IllegalArgumentException(e);
			} catch (HomeException e) {
				throw new IllegalArgumentException(e);
			}
        }
        
        // TT#13042934033 fixed. While subscriber state changes from barred/locked to active in the same charging cycle,
        // Services were getting charged again and expiry date was getting extended.
        // Fix : Removing the code introduced as part of TT#12090545032.
        if(nextRecurringChargeDate != null)
        
        {
        	/*
        	 * This means service/aux-service/bundle has already been provisioned 
        	 */
        	int result = billingDayWithNoTimeOfDay.compareTo(nextRecurringChargeDate);
        	
        	if(result >=0 )
        	{
        		/*
        		 * if billingDayWithNoTimeOfDay is equal or after the next-recurring-charge-date, then return billingDayWithNoTimeOfDay itself as a start date.
        		 * TT # 12051008023
        		 */
        		return billingDayWithNoTimeOfDay;
        		
        	}
        	Calendar cal = Calendar.getInstance();
        	cal.setTime(nextRecurringChargeDate);
        	cal.add(Calendar.DAY_OF_YEAR, -numberOfDays);
        	return CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cal.getTime());
        	
        }
       
        /*
         * If next-recurring-charge-date is null then one of the reasons is refund has happened.
         * Check in charging history then 
         */
        
        if(chargingHist != null)
        {
        	/*
        	 * if refund has happened , start date would be today .
        	 *  
        	 * else return last charging history date as a start-date - only if within the  service-cycle-start and service-cycle-end-date 
        	 * i.e. if start-date = 1st Jan , and recurrence = 10 days then cycle-start-date = 1st Jan and cycle-end-date = 10th Jan ( next-recurring-charge-date = 11th Jan )  
        	 *  1. if  billing-date is within 1st jan and 10th Jan then cycle-start-date would be 1st Jan
        	 *  2. If billing-date is after 10th Jan then cycle-start-date would be billing-Date-Itself. 
        	 * 
        	 * Note : there will NOT be REFUND entry under charging history if "isRefundable" flag is disabled at service level
        	 */
        	
	        if(!chargingHist.getEventType().equals(HistoryEventTypeEnum.REFUND))
	        {
	        	if(chargingHist.getTimestamp_() != null)
	        	{
	        		Date startTimStampWithNoTimeOfDay = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(chargingHist.getTimestamp_());
	        		Calendar cal = Calendar.getInstance();
	            	cal.setTime(startTimStampWithNoTimeOfDay);
	            	cal.add(Calendar.DAY_OF_YEAR, numberOfDays);
	            	Date estimatedNextRecurringChargeDate = cal.getTime();
	        		
	            	int result = billingDayWithNoTimeOfDay.compareTo(estimatedNextRecurringChargeDate);
	            	if(result >= 0)
	            	{
	            		/*
	            		 * if billingDayWithNoTimeOfDay is equal or after the next-recurring-charge-date, then return billingDayWithNoTimeOfDay itself as a start date.
	            		 * TT # 12051008023
	            		 */
	            		return billingDayWithNoTimeOfDay;
	            	}else
	            	{
	            		return startTimStampWithNoTimeOfDay;
	            	}
	        	}
	        	
	        }
	        
        	
        }
        
        return billingDayWithNoTimeOfDay;
    }    

    public Date calculateCycleEndDate(final Context context,  Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {

    	if(context.getBoolean(CALCULATE_END_DATE_FROM_CYCLE_START, true))
    	{
    		billingDate = calculateCycleStartDate(context, billingDate, billingCycleDay, spid, subscriberId, item);
    	}

    	int numberOfDays = 0;
        if (item instanceof ServiceFee2)
        {
            ServiceFee2 serviceFee =  ((ServiceFee2) item);
            
            numberOfDays = serviceFee.getRecurrenceInterval();
            Calendar cal = Calendar.getInstance();
			cal.setTime(billingDate);
			cal.add(Calendar.DAY_OF_YEAR, numberOfDays-1); // minus 1 , because both days are inclusive. 
			
			return  CalendarSupportHelper.get(context).getDateWithLastSecondofDay(cal.getTime());
			
			
        }else if(item instanceof Service)
        {
        	Service service = (Service) item;
        	numberOfDays = service.getRecurrenceInterval();
            Calendar cal = Calendar.getInstance();
			cal.setTime(billingDate);
			cal.add(Calendar.DAY_OF_YEAR, numberOfDays-1); // minus 1 , because both days are inclusive. 
			
			return  CalendarSupportHelper.get(context).getDateWithLastSecondofDay(cal.getTime());
        }
        else if (item instanceof AuxiliaryService)
        {
        	numberOfDays = ((AuxiliaryService) item).getRecurrenceInterval();
        	Calendar cal = Calendar.getInstance();
			cal.setTime(billingDate);
			cal.add(Calendar.DAY_OF_YEAR, numberOfDays-1); // minus 1 , because both days are inclusive. 
			
			return  CalendarSupportHelper.get(context).getDateWithLastSecondofDay(cal.getTime());
        }else if(item instanceof SubscriberAuxiliaryService)
        {
        	SubscriberAuxiliaryService sas = (SubscriberAuxiliaryService) item;
        	try {
				AuxiliaryService auxService = sas.getAuxiliaryService(context);
	        	numberOfDays = auxService.getRecurrenceInterval();
	        	Calendar cal = Calendar.getInstance();
				cal.setTime(billingDate);
				cal.add(Calendar.DAY_OF_YEAR, numberOfDays-1); // minus 1 , because both days are inclusive. 
				
				return  CalendarSupportHelper.get(context).getDateWithLastSecondofDay(cal.getTime());
			} catch (HomeException e) {

				throw new IllegalArgumentException("Exception occurred while retrieving Aux service for ID : " + sas.getAuxiliaryServiceIdentifier());
			}
        	
        }else if(item instanceof BundleProfile)
        {
        	numberOfDays = ((BundleProfile) item).getRecurringStartValidity();
        	Calendar cal = Calendar.getInstance();
			cal.setTime(billingDate);
			cal.add(Calendar.DAY_OF_YEAR, numberOfDays-1); // minus 1 , because both days are inclusive. 
			
			return  CalendarSupportHelper.get(context).getDateWithLastSecondofDay(cal.getTime());
        }
        else if (item instanceof BundleFee)
        {
            BundleFee bundleFee = (BundleFee) item;
        	try {
				BundleProfile bundleProfile = BundleSupportHelper.get(context).getBundleProfile(context, bundleFee.getId());
				numberOfDays = bundleProfile.getRecurringStartValidity();
	        	Calendar cal = Calendar.getInstance();
				cal.setTime(billingDate);
				cal.add(Calendar.DAY_OF_YEAR, numberOfDays-1); // minus 1 , because both days are inclusive. 
				
				return  CalendarSupportHelper.get(context).getDateWithLastSecondofDay(cal.getTime());
			} catch (InvalidBundleApiException e) {
				throw new IllegalArgumentException(e);
			} catch (HomeException e) {
				throw new IllegalArgumentException(e);
			}
        }
        else if (item instanceof ServicePackageFee)
        {
            throw new IllegalArgumentException("Packages do not support by multi-day fee.");
        }
        else
        {
            throw new IllegalArgumentException("Developers error: item should be instance of a subscriber fee.");
        }    
     }    

    public static MultiDayPeriodHandler instance()
    {
        if (handler==null)
        {
            handler = new MultiDayPeriodHandler();
        }
        return handler;
    }
    
    private static MultiDayPeriodHandler handler = null;
    
    /**
	 * {@inheritDoc}
	 */
	public double calculateRefundRateBasedOnUsage(Context context,
			Date billingDate, int billingCycleDay, Date startDate, int spid,
			String subscriberId, Object item, int unbilledDays) throws HomeException {
		throw new UnsupportedOperationException("Operation Not Supported.");
	}
}
