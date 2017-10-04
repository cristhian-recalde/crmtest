package com.trilogy.app.crm.service;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;


public class OneTimePeriodHandler implements ServicePeriodHandler, ChargingCycleHandler
{
    public double calculateRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
        return 1.0;
    }

    public Date calculateCycleStartDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
        return null;    
    }    
    
    public Date calculateCycleEndDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
        return null;   
    } 

    public double calculateRate(final Context context, final Date startDate, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
        return 1.0;
    }
    
    public double calculateRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
        return 1.0;
    }
    
    public double calculateRefundRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
        return 0;
    }
    
    
    public Date calculateCycleStartDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
        if (item instanceof ServiceFee2)
        {
            ServiceFee2 serviceFee =  ((ServiceFee2) item);
            SubscriberServices subscriberService = SubscriberServicesSupport.getSubscriberServiceRecord(context, subscriberId, serviceFee.getServiceId(), serviceFee.getPath());
            if (subscriberService!=null)
            {
                return subscriberService.getStartDate();
            }
            else
            {
                return new Date(0);
            }
        }
        else if (item instanceof Service)
        {
            Service service =  ((Service) item);
            SubscriberServices subscriberService = SubscriberServicesSupport.getSubscriberServiceRecord(context, subscriberId, service.getID(), SubscriberServicesUtil.DEFAULT_PATH);
            if (subscriberService != null)
            {
                return subscriberService.getStartDate();
            }
            else
            {
                return new Date(0);
            }
        }
        else if (item instanceof SubscriberAuxiliaryService)
        {
            return ((SubscriberAuxiliaryService) item).getStartDate();
        }        
        else if (item instanceof AuxiliaryService)
        {
            return ((AuxiliaryService) item).getStartDate();
        }
        else if (item instanceof BundleFee)
        {
            return ((BundleFee) item).getStartDate();
        }
        else if (item instanceof ServicePackageFee)
        {
            throw new IllegalArgumentException("Packages do not support by one-time fee.");
        }
        else
        {
            throw new IllegalArgumentException("Developers error: item should be instance of a subscriber fee.");
        }    }    

    public Date calculateCycleEndDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
        if (item instanceof ServiceFee2)
        {
            ServiceFee2 serviceFee =  ((ServiceFee2) item);
            SubscriberServices subscriberService = SubscriberServicesSupport.getSubscriberServiceRecord(context, subscriberId, serviceFee.getServiceId(), serviceFee.getPath());
            if (subscriberService!=null)
            {
                return subscriberService.getEndDate();
            }
            else
            {
                return new Date();
            }
        }
        else if (item instanceof Service)
        {
            Service service =  ((Service) item);
            SubscriberServices subscriberService = SubscriberServicesSupport.getSubscriberServiceRecord(context, subscriberId, service.getID(), SubscriberServicesUtil.DEFAULT_PATH);
            if (subscriberService != null)
            {
                return subscriberService.getEndDate();
            }
            else
            {
                return new Date(0);
            }
        }
        else if (item instanceof SubscriberAuxiliaryService)
        {
        	/*  TT#13041756009 - Service Period shows incorrectly in Invoice for One-time service with 12 months validity
        	 *  In subscriber auxiliary services table the serviceEndDate is stored with noTimeOfDay always.
        	 *  For auxiliary services with ONE_TIME charge, this date is +1 day after the periodInterval
        	 *  Hence reducing one second from this date gives us correct date for end of Seervice charge in transaction table.
        	 *  ServiceEndDate in subscriberAuxService table is untouched (as it might be used for unprovisioning of this OT service later)
        	 */
        	
            Date otAuxServiceEndDate = ((SubscriberAuxiliaryService) item).getEndDate();
            Date txnEndDate = CalendarSupportHelper.get(context).getDayBefore(otAuxServiceEndDate);
            txnEndDate = CalendarSupportHelper.get(context).getDateWithLastSecondofDay(txnEndDate);
            
            return txnEndDate;
        }        
        else if (item instanceof BundleFee)
        {
            return ((BundleFee) item).getEndDate();
        }
        else if (item instanceof AuxiliaryService)
        {
        	return ((AuxiliaryService) item).getEndDate();
        }
        else if (item instanceof ServicePackageFee)
        {
            throw new IllegalArgumentException("Packages do not support by one-time fee.");
        }
        else
        {
            throw new IllegalArgumentException("Developers error: item should be instance of a subscriber fee.");
        }    }    

    public static OneTimePeriodHandler instance()
    {
        if (handler==null)
        {
            handler = new OneTimePeriodHandler();
        }
        return handler;
    }
    
    private static OneTimePeriodHandler handler = null;
    
    /**
	 * {@inheritDoc}
	 */
	public double calculateRefundRateBasedOnUsage(Context context,
			Date billingDate, int billingCycleDay, Date startDate, int spid,
			String subscriberId, Object item, int unbilledDays) throws HomeException {
		throw new UnsupportedOperationException("Operation Not Supported.");
	}
}
