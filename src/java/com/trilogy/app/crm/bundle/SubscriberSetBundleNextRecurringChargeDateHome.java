package com.trilogy.app.crm.bundle;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;


import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author kabhay Sets next-recurring-charge-date of only future dates bundles
 *         . Check UpdateNextRecurringChargeDateHome for other scenario.
 */
public class SubscriberSetBundleNextRecurringChargeDateHome extends HomeProxy {

	public SubscriberSetBundleNextRecurringChargeDateHome(Home delegate) {
		super(delegate);

	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {

		setNextRecurringChargeDateForFutureDatedBundles(ctx, obj,true);
		return super.create(ctx, obj);
	}

	@Override
	public Object store(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		setNextRecurringChargeDateForFutureDatedBundles(ctx, obj,false);
		return super.store(ctx, obj);
	}

	private void setNextRecurringChargeDateForFutureDatedBundles(Context ctx,
			Object obj, boolean creaton) throws HomeException {

		if (obj instanceof Subscriber) 
		{
			Subscriber sub = (Subscriber) obj;
			if(sub.getState().equals(SubscriberStateEnum.AVAILABLE) || sub.getState().equals(SubscriberStateEnum.PENDING))
			{
				if(LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this, "Not setting next-recurring-charge-date of Bundle(s) since subscriber is either in Available or in Pending state!!");
				}
				return;
			}
			
			MSP.setBeanSpid(ctx, sub.getSpid());
			
			Collection<BundleFee> bundleFees = sub.getBundles().values();
			if(bundleFees == null) return;
			for(BundleFee bundleFee : bundleFees)
			{
				Date startDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(bundleFee.getStartDate());
				Date today = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
				if(startDate.after(today))
				{
					/*
					 * if future dated bundle then set next-recurring-charge-date
					 */
					
					bundleFee.setNextRecurringChargeDate(startDate);
					
				}else
				{
					try {
					/*
					 * if back dating future dated bundle to current date or earlier (predating ) then reset the next-recurring-charge-date to null .
					 * It will be set during transaction creation
					 */
					
					Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
					if(oldSub == null)
					{
						oldSub = SubscriberSupport.getSubscriber(ctx, sub.getId());
					}
					if(oldSub.getBundles() != null)
					{
						BundleFee oldBundleFee = (BundleFee) oldSub.getBundles().get(bundleFee.getId());
						if(oldBundleFee != null)
						{
							if(oldBundleFee.getStartDate().after(bundleFee.getStartDate()))
							{
								bundleFee.setNextRecurringChargeDate(null);
								return;
							}
						}
					}
					
					
					
						BundleProfile bundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundleFee.getId());
						if(!bundleProfile.isRefundable()  && bundleFee.getNextRecurringChargeDate() == null)
						{
							/*
							 * If it is not refundable then bundle was not refunded the time it was unprovisioned and hence charge will not be applied if within the bill cycle.
							 * 
							 * Hence need to set next-recurring-charge-date here.
							 */
							
							ServicePeriodEnum chargingScheme = bundleProfile.getChargingRecurrenceScheme();
							if(chargingScheme.equals(ServicePeriodEnum.ONE_TIME))
							{
								/*
								 * Do not set next-recurring-charge-date for one time bundle.
								 */
								return;
							}
							
							ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(bundleProfile.getChargingRecurrenceScheme());
							if(handler != null)
							{
	
								Date cycleEndDate = handler.calculateCycleEndDate(ctx, new Date(), sub.getAccount(ctx).getBillCycleDay(ctx), sub.getSpid(), sub.getId(), bundleProfile);
								
								Calendar cal = Calendar.getInstance();
								cal.setTime(cycleEndDate);
								cal.add(Calendar.DAY_OF_YEAR, 1);
								Date nextRecur = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(cal.getTime());
								bundleFee.setNextRecurringChargeDate(nextRecur);
							}
							
						}
					} catch (InvalidBundleApiException e) {
						throw new HomeException(e);
					} catch (HomeException e) {
						throw e;
					}
					
				}
				
				
			}
			
			
		}

	}

}
