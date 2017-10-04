package com.trilogy.app.crm.home;

import java.util.Calendar;
import java.util.Date;


import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.service.ServiceProvisionActionEnum;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author kabhay
 * update next-recurring-charge-date for future dated service only
 */
public class SubscriberServicesNextRecurChargeDateUpdateHome extends HomeProxy 
{

	public SubscriberServicesNextRecurChargeDateUpdateHome(Context ctx,
			Home delegate) {
		super(ctx, delegate);
		
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		updateNextRecurringChargeDate(ctx, obj, true);
		return super.create(ctx, obj);
	}

	@Override
	public Object store(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		updateNextRecurringChargeDate(ctx, obj, false);
		return super.store(ctx, obj);
	}
	
	private void updateNextRecurringChargeDate(Context ctx, Object obj, boolean creation ) throws HomeException 
	{
		boolean isDebugEnabled = LogSupport.isDebugEnabled(ctx); 
		if(isDebugEnabled)
		{
			LogSupport.debug(ctx, this, "[updateNextRecurringChargeDate] [Start] [Recived Object:" + obj + " [Creation:" + creation + "]");
		}
		
		SubscriberServices association = (SubscriberServices) obj;
		Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
		if(sub == null)
		{
			sub = SubscriberSupport.getSubscriber(ctx, association.getSubscriberId());
		}

		if(sub.getState().equals(SubscriberStateEnum.AVAILABLE) || sub.getState().equals(SubscriberStateEnum.PENDING))
		{
			if(isDebugEnabled)
			{
				LogSupport.debug(ctx, this, "Not setting next-recurring-charge-date of Service-" + association.getServiceId() + " since subscriber is either in Available or in Pending state!!");
			}
			return;
		}
		
		if(association.getStartDate().after(new Date()))
		{
			/*
			 * Future dated service
			 */
			Date startDateWithNoTimeofDay = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(association.getStartDate());
			if(isDebugEnabled)
			{
				LogSupport.debug(ctx, this, "[updateNextRecurringChargeDate] [Start] [Future dated service setting NextRecurringChargeDate:" + startDateWithNoTimeofDay +"]");
			}
			association.setNextRecurringChargeDate(startDateWithNoTimeofDay);
			return;
		}
		
		/*
		 * if back dating future dated service to current date or earlier (predating ) then reset the next-recurring-charge-date to null .
		 * It will be set during transaction creation
		 */
		
		SubscriberServices oldAssociation = SubscriberServicesSupport.getSubscriberServiceRecord(ctx, sub.getId(), association.getServiceId(), association.getPath());
		if(oldAssociation != null)
		{
			if(isDebugEnabled)
			{
				LogSupport.debug(ctx, this, "[updateNextRecurringChargeDate] [Start] [old SubscriberServices  is presant:" + oldAssociation  +"]");
			}
			if(oldAssociation.getStartDate().after(association.getStartDate()))
			{
				if(isDebugEnabled)
				{
					LogSupport.debug(ctx, this, "[updateNextRecurringChargeDate] [Start] [Setting NextRecurringChargeDate to null"  + "]");
				}
				association.setNextRecurringChargeDate(null);
				return;
			}
		}
		
		Service service = association.getService();
		if(service != null)
		{
			if(!service.isRefundable() && association.getNextRecurringChargeDate() ==  null)
			{
				if(isDebugEnabled)
				{
					LogSupport.debug(ctx, this, "[updateNextRecurringChargeDate] [Start] [service is not Refundable And NextRecurringChargeDate date not presant.]");
				}
				ServicePeriodEnum chargingScheme = service.getChargeScheme();
				if(chargingScheme.equals(ServicePeriodEnum.ONE_TIME))
				{
					if(isDebugEnabled)
					{
						LogSupport.debug(ctx, this, "[updateNextRecurringChargeDate] [Start] [One Time Service.]");
					}
					/*
					 * Do not set next-recurring-charge-date for one time service.
					 */
					return;
				}
				
				/*
				 * If it is not refundable then service was not refunded the time it was unprovisioned and hence charge will not be applied if within the bill cycle.
				 * 
				 * Hence need to set next-recurring-charge-date here
				 */
				
				ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(service.getChargeScheme());
				if(handler != null)
				{
					/*
					 * Service start and end date needed by Multi-Monthly Handler 
					 * 
					 * TT# 12050845016
					 */
					service.setStartDate(association.getStartDate());
					service.setEndDate(association.getEndDate());
					Date cycleEndDate = handler.calculateCycleEndDate(ctx, new Date(), sub.getAccount(ctx).getBillCycleDay(ctx), sub.getSpid(), association.getSubscriberId(), service);
					
					Calendar cal = Calendar.getInstance();
					cal.setTime(cycleEndDate);
					cal.add(Calendar.DAY_OF_YEAR, 1);
					Date nextRecur = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(cal.getTime());
					if(isDebugEnabled)
					{
						LogSupport.debug(ctx, this, "[updateNextRecurringChargeDate] [Start] [NextRecurringChargeDate from calculateCycled EndDate:" + nextRecur + "]");
					}
					association.setNextRecurringChargeDate(nextRecur);
				}
				else
				{
					if(isDebugEnabled)
					{
						LogSupport.debug(ctx, this, "[updateNextRecurringChargeDate] [Start] [Error, Could not fond Handler for calculateCycleEndDate.]");
					}
				}
				
			}
		}
		else
		{
			if(isDebugEnabled)
			{
				LogSupport.debug(ctx, this, "[updateNextRecurringChargeDate] [Start] [Service Not found in SubscriberServices..."  + "]");
			}
		}
		
	}
}
