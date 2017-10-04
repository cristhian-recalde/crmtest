package com.trilogy.app.crm.home;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
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
 * Updates next-recurring-charge-date for future dated services and services whose refund was not done ( i.e. isRefundable Flag is false ).
 */
public class SubscriberAuxServiceNextRecurChargeDateUpdateHome extends
		HomeProxy 
		
{

	public SubscriberAuxServiceNextRecurChargeDateUpdateHome(Context ctx,
			Home delegate) {
		super(ctx, delegate);
		
	}

	@Override
	public Object store(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		updateNextRecurringChargeDate(ctx,obj, false);
		return super.store(ctx, obj);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {

		updateNextRecurringChargeDate(ctx,obj, true);
		
		return super.create(ctx, obj);
	}
	

	private void updateNextRecurringChargeDate(Context ctx, Object obj, boolean creation ) throws HomeException 
	{
		SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;
		
		Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
		if(sub == null)
		{
			sub = SubscriberSupport.getSubscriber(ctx, association.getSubscriberIdentifier());
		}
		
		if(sub.getState().equals(SubscriberStateEnum.AVAILABLE) || sub.getState().equals(SubscriberStateEnum.PENDING))
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this, "Not setting next-recurring-charge-date of Aux-Service-" + association.getAuxiliaryServiceIdentifier() + " since subscriber is either in Available or in Pending state!!");
			}
			return;
		}
		
		if(association.getStartDate().after(new Date()))
		{
			/*
			 * Future dated service
			 */
			Date startDateWithNoTimeofDay = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(association.getStartDate());
			association.setNextRecurringChargeDate(startDateWithNoTimeofDay);
			return;
		}
		
		/*
		 * if back dating future dated service to current date or earlier (predating ) then reset the next-recurring-charge-date to null .
		 * It will be set during transaction creation
		 */
		
		SubscriberAuxiliaryService oldAssociation = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServicesWithIdentifiers(ctx, sub.getId(), association.getAuxiliaryServiceIdentifier(),association.getSecondaryIdentifier());
		if(oldAssociation != null)
		{
			if(oldAssociation.getStartDate().after(association.getStartDate()))
			{
				association.setNextRecurringChargeDate(null);
				return;
			}
		}

		
		AuxiliaryService auxService  = association.getAuxiliaryService(ctx);
		if(auxService != null)
		{
			if(!auxService.isRefundable() && association.getNextRecurringChargeDate() ==  null)
			{
				ServicePeriodEnum chargingScheme = auxService.getChargingModeType();
				if(chargingScheme.equals(ServicePeriodEnum.ONE_TIME))
				{
					/*
					 * Do not set next-recurring-charge-date for one time service.
					 */
					return;
				}
				
				/*
				 * If it is not refundable then service was not refunded the time it was unprovisioned and hence charge will not be applied if within the bill cycle.
				 * 
				 * Hence need to set next-recurring-charge-date here.
				 */
				
				
				
				ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(auxService.getChargingModeType());
				if(handler != null)
				{
					/*
					 * Service start and end date needed by Multi-Monthly Handler 
					 * 
					 * TT# 12050845016
					 */
					auxService.setStartDate(association.getStartDate());
					auxService.setEndDate(association.getEndDate());
					Date cycleEndDate = handler.calculateCycleEndDate(ctx, new Date(), sub.getAccount(ctx).getBillCycleDay(ctx), sub.getSpid(), association.getSubscriberIdentifier(), auxService);
					
					Calendar cal = Calendar.getInstance();
					cal.setTime(cycleEndDate);
					cal.add(Calendar.DAY_OF_YEAR, 1);
					Date nextRecur = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(cal.getTime());
					association.setNextRecurringChargeDate(nextRecur);
				}
				
			}
		}
		
		
		
	}

	

}
