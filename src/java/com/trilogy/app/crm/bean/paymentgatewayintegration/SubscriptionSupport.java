package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Support class for Subscription behavior around Payment Gateway Integration and Credit Card Top-up use-cases.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 * @author mangaraj.sahoo@redknee.com
 */
public class SubscriptionSupport 
{
    
    private static final String MODULE = SubscriptionSupport.class.getName();

	/**
	 * 
	 * Checks if this subscription is scheduled for credit card top up.
	 * 
	 * @param ctx
	 * @return <code>true</code> if scheduled, else <code>false</code>
	 */
	public static boolean isTopUpScheduledForSubscription(Context ctx , Subscriber subscriber)
	{
		boolean result = Boolean.FALSE;
		
		try
		{
			long entries = HomeSupportHelper.get(ctx).getBeanCount(ctx, TopUpSchedule.class, new EQ(TopUpScheduleXInfo.SUBSCRIPTION_ID, subscriber.getId()));
			
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, MODULE, "Found " + entries + " schedules(s) for Subscriber:" + subscriber.getId());
			}				
			
			if(entries >= 1)
			{
				return Boolean.TRUE;
			}
		}
		catch ( HomeException e )
		{
			LogSupport.minor(ctx, MODULE, "Exception validating isTopUpScheduledForSubscription for subscriber:" + subscriber.getId()
					+ ". Message:" + e.getMessage());
		}
		
		return result;
	}
	
    
    public static Date determineNextTopUpDate(Context ctx, Subscriber subscriber) throws HomeException
    {
        ServiceFee2 primarySvcFee = null; 
        //ServiceFee2 fee = ppv.getPrimaryService(ctx);
        // For PickNPay priceplan there can be multiple primary services, 
        // hence in any case choose the one that's attached to subscriber to determine next topup date
        
        primarySvcFee = getProvisionedPrimaryService(ctx, subscriber);
		
        if (primarySvcFee == null) //CreditCardTopUpFrequencyEnum.BILL_CYCLE
        {
            /**
             * If primary service is not available to the user then the user can be PAYGO enabled user 
             */
        	
        	PricePlanVersion ppv = null;
            try
            {
                ppv = subscriber.getRawPricePlanVersion(ctx);
            }
            catch (HomeException e)
            {
                LogSupport.info(ctx, MODULE, "Cannot obtain PricePlanVersion for subscriber [" + subscriber.getId() +"].", e);
            }
            
            if (ppv == null)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("No PricePlanVersion found for Subscriber ID: ");
                    sb.append(subscriber.getId());
                    sb.append(". Cannot determine nextApplication date of TopUpSchedule.");
                    LogSupport.debug(ctx, MODULE, sb.toString());
                }
                return null;
            }
            
            if (ppv.getPricePlan(ctx).getPricePlanSubType().getIndex() == PricePlanSubTypeEnum.PAYGO_INDEX)
            {
                CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, subscriber.getSpid());

                int oneDayPriorSchedule = crmSpid.isScheduleCCAtuOneDayPriorExpiry() ? -1 : 0;

                Calendar expiryCalendar = Calendar.getInstance();
                expiryCalendar.setTime(subscriber.getExpiryDate());
                expiryCalendar.add(Calendar.DAY_OF_YEAR, oneDayPriorSchedule);

                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, MODULE, "The nextApplication Date : " + expiryCalendar.getTime()
                            + " Calculated from subscriber expiry of : " + subscriber.getExpiryDate());
                }

                return expiryCalendar.getTime();
            }
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("PricePlanVersion has no Primary service Subscriber [ID: ");
                sb.append(subscriber.getId());
                sb.append("]. The nextApplication will be the next BillCycle Date.");
                LogSupport.debug(ctx, MODULE, sb.toString());
            }
            
            Date nextBillCycleDate = determineNextBillCycleDate(ctx, subscriber);
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, MODULE, "The nextApplication Date: " + nextBillCycleDate);
            }
            return nextBillCycleDate;
        }
        else // CreditCardTopUpFrequencyEnum.PRIMARY_SERVICE_RECURRENCE
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Subscriber [ID: ");
                sb.append(subscriber.getId());
                sb.append("] has a Primary service [ID: ");
                sb.append(primarySvcFee.getServiceId());
                sb.append(", Is Primary: ");
                sb.append(primarySvcFee.isPrimary());
                sb.append("]. The nextApplication will be Primary service's next recurrence date." );
                LogSupport.debug(ctx, MODULE, sb.toString());
            }
            
            And filter = new And();
            filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriber.getId()));
            filter.add(new EQ(SubscriberServicesXInfo.SERVICE_ID, primarySvcFee.getServiceId()));
            
            SubscriberServices association = HomeSupportHelper.get(ctx).findBean(ctx, SubscriberServices.class, filter);
            if (association == null)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("No Subscriber Service association found for Subscriber ID: ");
                    sb.append(subscriber.getId());
                    sb.append(" and Service ID: ");
                    sb.append(primarySvcFee.getServiceId());
                    sb.append(". Cannot determine Next Application Date of TopUpSchedule.");
                    LogSupport.debug(ctx, MODULE, sb.toString());
                }
                return null;
            }
            else
            {
                return association.getNextRecurringChargeDate();
            }
        }
    }


	/**
	 * Method to obtain a primary service thats attached to subscriber
	 * @param ctx
	 * @param subscriber
	 * @return
	 * @throws HomeException
	 */
	public static ServiceFee2 getProvisionedPrimaryService(Context ctx,
			Subscriber subscriber) throws HomeException 
	{
		ServiceFee2 primarySvcFee = null;
		
		Map<ServiceFee2ID, SubscriberServices> subServicesMap = SubscriberServicesSupport.getSubscribersServices(ctx, subscriber.getId());
        Collection<SubscriberServices> associatedServices =  subServicesMap.values();
        
        Map<com.redknee.app.crm.bean.core.ServiceFee2, SubscriberServices> serviceFees = ServiceChargingSupport.getProvisionedServices(ctx,
				subscriber.getPricePlan(ctx).getServiceFees(ctx).values(), associatedServices);
		
		for (Iterator<com.redknee.app.crm.bean.core.ServiceFee2> i = serviceFees.keySet().iterator(); i.hasNext();)
		{
			ServiceFee2 fee = i.next();
			if(fee.isPrimary())
			{
				primarySvcFee = fee;
				break;
			}
		}
		return primarySvcFee;
	}
    
    
    public static Date determineNextBillCycleDate(Context ctx, Subscriber subscriber) throws HomeException
    {
        if (subscriber != null)
        {
            int dayOfMonth = subscriber.getAccount(ctx).getBillCycleDay(ctx);
            
            Calendar calendar = Calendar.getInstance();
            GregorianCalendar nextBillCycleDate = new GregorianCalendar(calendar.get(Calendar.YEAR), 
                    calendar.get(Calendar.MONTH), dayOfMonth);
            
            if(nextBillCycleDate.before(Calendar.getInstance()))
            {
                nextBillCycleDate = new GregorianCalendar(calendar.get(Calendar.YEAR), 
                        calendar.get(Calendar.MONTH) + 1, dayOfMonth);
            }
            return nextBillCycleDate.getTime();
        }
        
        return null;
    }
    
    public static Date determineNextTopupScheduleDate(Context ctx, Date currentDate)
    {
        if (currentDate != null)
        {
            Calendar currentScheuleDate = Calendar.getInstance();
            currentScheuleDate.setTime(currentDate);
            
            Calendar calendarNow = Calendar.getInstance();
            if(currentScheuleDate.before(calendarNow))// 1st pass try to set schedule date to current month.
            {
                currentScheuleDate = new GregorianCalendar(calendarNow.get(Calendar.YEAR), 
                        calendarNow.get(Calendar.MONTH), currentScheuleDate.get(Calendar.DAY_OF_MONTH));
            }
            
            if(currentScheuleDate.before(calendarNow))//If current month date has also passed, schedule for next month
            {
                currentScheuleDate = new GregorianCalendar(calendarNow.get(Calendar.YEAR), 
                        calendarNow.get(Calendar.MONTH) + 1, currentScheuleDate.get(Calendar.DAY_OF_MONTH));
            }
            return currentScheuleDate.getTime();
        }
        
        return null;
    }
    
}
