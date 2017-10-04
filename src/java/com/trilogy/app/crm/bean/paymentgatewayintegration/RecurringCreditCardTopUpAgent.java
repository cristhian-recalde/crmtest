package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.bas.recharge.RechargeSubscriberVisitor;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.CreditCardToken;
import com.trilogy.app.crm.bean.CreditCardTokenXInfo;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.SubModificationSchedule;
import com.trilogy.app.crm.bean.SubModificationScheduleXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.TaxableTopUp;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.paymentgatewayintegration.PaymentGatewaySupport;
import com.trilogy.app.crm.paymentgatewayintegration.PaymentGatewaySupportHelper;
import com.trilogy.app.crm.priceplan.ScheduledPriceplanChangeExecutor;
import com.trilogy.app.crm.subscriber.charge.support.BundleChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.taxation.LocalTaxAdapter;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.product.s2100.oasis.param.Parameter;

/**
 * 
 * Recurring top up agent.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class RecurringCreditCardTopUpAgent implements ContextAgent
{

	@Override
	public void execute(Context ctx) throws AgentException 
	{
		TopUpSchedule schedule = (TopUpSchedule)ctx.get(TopUpSchedule.class);
		boolean recurring = true;
		TaxableTopUp topUp = null;
		
		if(schedule == null)
		{
			throw new AgentException("Schedule is null");
		}
		
		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "STARTED Applying recurring top up Schedule :"  + schedule);
		}
		
		Map<Short, Parameter> outParams = new HashMap<Short, Parameter>();
		Subscriber subscriber = null;
		LongHolder balance = new LongHolder();
		try 
		{
			CreditCardToken token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID, schedule.getTokenId()));
			subscriber = SubscriberSupport.getSubscriber(ctx, schedule.getSubscriptionId());
			
			if(token == null)
			{
				throw new AgentException("Token[id:" + schedule.getTokenId() + "] does not exist in the system.");
			}
			
			if(subscriber == null)
			{
				throw new AgentException("Cannot find subscriber with id:" + schedule.getSubscriptionId());
			}
			
			if(SubscriberStateEnum.ACTIVE.equals(subscriber.getState()) || SubscriberStateEnum.EXPIRED.equals(subscriber.getState()))
			{
				
			}
			else
			{
				throw new AgentException("Invalid subscriber state:" + subscriber.getState().getDescription() + 
						". Subscriber state should be one of :" + SubscriberStateEnum.ACTIVE.getDescription() + " or " + SubscriberStateEnum.EXPIRED.getDescription());
			}
			
			CRMSpid spid = SubscriberSupport.getServiceProvider(ctx, subscriber);
			long totalPGChargableAmount = 0;
			
			if(schedule.getUsePlanFees() && !schedule.isPlanChangeScheduled() && schedule.getAmount() == 0)
			{
			    if(LogSupport.isDebugEnabled(ctx))
			    {
			        LogSupport.debug(ctx, this, "Calculating PricePlanFee on TOPUP for subscriber : " + subscriber.getId());
			    }
			    
				long pricePlanFees = calculatePricplanFeeOnTopupCharge(ctx, subscriber);
				
				if(spid.getDynamicAutoTopUp())
				{
					pricePlanFees = calculateDynamicAutoTopupAmount(
							ctx, pricePlanFees, subscriber, spid);
				}
				
				topUp = LocalTaxAdapter.getTotalTopUp(ctx, subscriber, pricePlanFees);
				totalPGChargableAmount = topUp.getPGChargeableAmount();
				
			}
			else if(schedule.getUsePlanFees() && schedule.isPlanChangeScheduled() && schedule.getAmount() == 0)
			{
			    if(LogSupport.isDebugEnabled(ctx))
                {
			        LogSupport.debug(ctx, this, "Calculating scheduled PricePlanFee on TOPUP for subscriber : " + subscriber.getId());
                }
				long pricePlanFees = calculateScheduledPricplanFee(ctx, subscriber);
				
				if(spid.getDynamicAutoTopUp())
				{
					pricePlanFees = calculateDynamicAutoTopupAmount(
							ctx, pricePlanFees, subscriber, spid);
				}
				
				topUp = LocalTaxAdapter.getTotalTopUp(ctx, subscriber, pricePlanFees);
				totalPGChargableAmount = topUp.getPGChargeableAmount();
			}
			else
			{
			    if(LogSupport.isDebugEnabled(ctx))
                {
			        LogSupport.debug(ctx, this, "NOT Calculating PricePlanFee on TOPUP for subscriber : " + subscriber.getId() 
                            + " going with schedule amount : " + schedule.getAmount());
                }
				topUp = LocalTaxAdapter.getTotalTopUp(ctx, subscriber, schedule.getAmount());
				totalPGChargableAmount = topUp.getPGChargeableAmount();
			}
			
			int result = -1;
			 
			if(totalPGChargableAmount > 0) 
			{
				if(LogSupport.isDebugEnabled(ctx))
	             {
				     LogSupport.debug(ctx, this, "Sending request to payment gateway for TOPUP for subscriber : " + subscriber.getId() 
	                         + " with amount : " + totalPGChargableAmount);
	             }
				 
				result = PaymentGatewaySupportHelper.get(ctx).chargePaymentGateway(ctx, totalPGChargableAmount, topUp.getTaxAmount(), subscriber, recurring, 
					token.getMaskedCreditCardNumber(), token.getValue(),  outParams);
			
				if(result != PaymentGatewaySupport.DEFAULT_SUCCESS)
				{
					throw PaymentGatewayExceptionFactory.createNestedAgentException( result , "PaymentGateway charging failed for msisdn:" + subscriber.getMsisdn() + ". An account note will be added with details of error shortly." );
				}
				
				LogSupport.info(ctx, this, "Payment Gateway successfully charged for subscriber:" + subscriber.getId()+ " and amount:" + totalPGChargableAmount);
				
				AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.RecurringCreditCardTopUp);
				
				TransactionSupport.createTransaction(ctx, subscriber, topUp.getSubscriberBalanceCrAmount(), adjustmentType);
				
				LogSupport.info(ctx, this, "Subscriber:" + subscriber.getId() + " successfully credited. Balance:" + balance.getValue());
			}
			else
			{
				LogSupport.info(ctx, this, "Not sending payment gateway call for TOPUP for subscriber : " + subscriber.getId() 
                        + " since topup amount calculated is : " + totalPGChargableAmount);
				result = PaymentGatewaySupport.DEFAULT_SUCCESS;
			}
			if(LogSupport.isDebugEnabled(ctx))
			{
			    LogSupport.debug(ctx, this, "Setting schedule nextApplicationDate, date is currently user defined (true/false) : " + schedule.isScheduleUserDefined());
			}
			if(schedule.isScheduleUserDefined())
			{
			   Date nextScheduleApplicationDate = SubscriptionSupport.determineNextTopupScheduleDate(ctx, schedule.getNextApplication());
			   if(LogSupport.isDebugEnabled(ctx))
               {
                   LogSupport.debug(ctx, this, "Calculated currentApplication date : " + schedule.getNextApplication() + " and nextApplicationDate is : " + nextScheduleApplicationDate);
               }
			   
			   schedule.setNextApplication(nextScheduleApplicationDate);
			   schedule = HomeSupportHelper.get(ctx).storeBean(ctx, schedule);
			}
            else
            {
               ServiceFee2 fee = null;
                // After introduction of PickNPay there can be multiple Primary services in a priceplan
                // but only single primary service as membership svc will be attached to subscriber
                // next recurring charge date / next top up date will be based on the primary service that the subscriber has picked up
                
                fee = SubscriptionSupport.getProvisionedPrimaryService(ctx, subscriber);
                
                if (fee == null) // CreditCardTopUpFrequencyEnum.BILL_CYCLE
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Subscriber [ID: ");
                        sb.append(subscriber.getId());
                        sb.append("] has no Primary service. Setting schedule's nextApplication to next BillCycle date.");
                        LogSupport.debug(ctx, this, sb.toString());
                    }
                    schedule.setNextApplication(SubscriptionSupport.determineNextBillCycleDate(ctx, subscriber));
                    schedule = HomeSupportHelper.get(ctx).storeBean(ctx, schedule);
                }
                else
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Subscriber [ID: ");
                        sb.append(subscriber.getId());
                        sb.append("] has a Primary service [ID: ");
                        sb.append(fee.getServiceId());
                        sb.append(", Is Primary: ");
                        sb.append(fee.isPrimary());
                        sb.append("]. The nextApplication will be Primary service's next recurrence date.");
                        LogSupport.debug(ctx, this, sb.toString());
                    }

                    if (fee.getServicePeriod().equals(ServicePeriodEnum.MULTIDAY))
                    {
                        And filter = new And();
                        filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriber.getId()));
                        filter.add(new EQ(SubscriberServicesXInfo.SERVICE_ID, fee.getServiceId()));

                        SubscriberServices association = HomeSupportHelper.get(ctx).findBean(ctx,
                                SubscriberServices.class, filter);

                        Date currDate = new Date();
                        if (currDate.before(association.getNextRecurringChargeDate()))
                        {
                            // if Top Up task runs manually
                            currDate = association.getNextRecurringChargeDate();
                            schedule.setNextApplication(currDate);
                            schedule = HomeSupportHelper.get(ctx).storeBean(ctx, schedule);
                        }
                        else
                        {
                            int numberOfDays = fee.getRecurrenceInterval();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(new Date());
                            cal.add(Calendar.DAY_OF_YEAR, numberOfDays); // minus 1 , because both days are inclusive.
                            Date nextRecurChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
                                    cal.getTime());
                            schedule.setNextApplication(nextRecurChargeDate);
                            schedule = HomeSupportHelper.get(ctx).storeBean(ctx, schedule);
                        }
                    }
                }
            }
			
			
			if(result == PaymentGatewaySupport.DEFAULT_SUCCESS && spid.getAvoidBalanceExposure())
			{
				if(schedule.isPlanChangeScheduled())
				{ 
					ScheduledPriceplanChangeExecutor planChangeExecutor = new ScheduledPriceplanChangeExecutor();
					And predicate = new And();
					predicate.add(new EQ(SubModificationScheduleXInfo.SUBSCRIPTION_ID, schedule.getSubscriptionId()));
					predicate.add(new EQ(SubModificationScheduleXInfo.TYPE, 0)); // 0 Indicates Price Plan change schedule.
					predicate.add(new EQ(SubModificationScheduleXInfo.STATUS, CronConstant.SCHEDULED_PENDING));

					Collection<SubModificationSchedule> schedules = 
							HomeSupportHelper.get(ctx).getBeans(ctx, SubModificationSchedule.class, predicate);
					
					if(LogSupport.isDebugEnabled(ctx))
					{
						LogSupport.debug(ctx, this, "SubModificationSchedule look up predicate: "+ predicate);
						LogSupport.debug(ctx, this, "SubModificationSchedule result set: "+ schedules);
					}
					
					for(SubModificationSchedule subModificationSchedule : schedules)
					{
						planChangeExecutor.execute(ctx, subModificationSchedule);
					}
				}
				else
				{
					final Date billingDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(CalendarSupportHelper.get(ctx).getRunningDate(ctx));

					RechargeSubscriberVisitor rechargeSubscriberVisitor = new RechargeSubscriberVisitor(billingDate, null, ChargingCycleEnum.MULTIDAY, billingDate, billingDate, true, false, false);
					rechargeSubscriberVisitor.visit(ctx, subscriber);
				}
			}
			
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this, "FINISHED Applying recurring top up Schedule :"  + schedule);
			}
		}
		catch (HomeException e) 
		{			
			throw new AgentException(e);
		}
		catch( PaymentGatewayException e)
		{
			throw new AgentException(e);
		}
	}

	/**
	 * @param ctx
	 * @param topUp
	 * @param subscriber
	 * @param spid
	 * @return
	 */
	private long calculateDynamicAutoTopupAmount(Context ctx,
			long fees, Subscriber subscriber, CRMSpid spid) {
		long dynamicTopUpAmount;
		long subBalance = subscriber.getBalanceRemaining(ctx);
		
		if(subBalance <= fees)
		{
			dynamicTopUpAmount = fees - subBalance;
		}
		else
		{
			dynamicTopUpAmount = 0l;
		}
		
		if(LogSupport.isDebugEnabled(ctx))
		{
		    LogSupport.debug(ctx, this, "Dynamic AutoTopup is enabled for service provider " + spid.getId()
		    		+". After considering Subscriber Balance amount = "+subBalance+" for subscriber "+subscriber.getId()+", total chargable amount is = "+dynamicTopUpAmount);
		}
		return dynamicTopUpAmount;
	}
	
	private long calculatePricplanFeeOnTopupCharge(Context ctx, Subscriber subscriber)
	{
		long pricePlanFee = 0;
		Collection<SubscriberServices> servicesToBeCharged = new  HashSet<SubscriberServices>();
		Collection<SubscriberAuxiliaryService> auxServicesToBeCharged= new  HashSet<SubscriberAuxiliaryService>(); 
		Collection<Long> bundlesToBeCharged= new  HashSet<Long>();

		servicesToBeCharged.addAll(subscriber.getProvisionedSubscriberServices(ctx)); 
		bundlesToBeCharged.addAll(BundleChargingSupport.getSubscribedBundles(ctx, subscriber).keySet()); 
		auxServicesToBeCharged.addAll(SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, subscriber.getId())); 
		try
		{
			Date runningDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());

			Map<com.redknee.app.crm.bean.core.ServiceFee2, SubscriberServices> serviceFees = ServiceChargingSupport.getProvisionedServices(ctx,
					subscriber.getPricePlan(ctx).getServiceFees(ctx).values(), servicesToBeCharged);
			for (Iterator<com.redknee.app.crm.bean.core.ServiceFee2> i = serviceFees.keySet().iterator(); i.hasNext();)
			{
				ServiceFee2 fee = i.next();
				final SubscriberServices subService = (SubscriberServices) serviceFees.get(fee);
				
				
				if(ServicePeriodEnum.ONE_TIME.equals(fee.getServicePeriod()))
				{
					continue;
				}		
				
				Date nextRecurringChargeDate = null;
				
				if(subService.getNextRecurringChargeDate() != null)
				{
					nextRecurringChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subService.getNextRecurringChargeDate());
					
		            if(LogSupport.isDebugEnabled(ctx))
		            {
		                LogSupport.debug(ctx, this, "Service fees for subscriber : " + subscriber.getId() + " serviceID : " + fee.getServiceId()
		                        + " nextRecurDate : "  + nextRecurringChargeDate + " running date : " + runningDate);
		            }
				}else
				{
					if(LogSupport.isDebugEnabled(ctx))
		            {
						LogSupport.debug(ctx, this, "NextRecurringChargeDate FOUND NULL for Service:"+fee);
		            }
				}
	            
				if (nextRecurringChargeDate != null && nextRecurringChargeDate.equals(runningDate))
				{
					pricePlanFee  = pricePlanFee + fee.getFee();
				}
			}
			
			if(LogSupport.isDebugEnabled(ctx))
			{
			    LogSupport.debug(ctx, this, "Calculated service fees for subscriber : " + subscriber.getId() + " is : " + pricePlanFee);
			}

			long cumulativeBundleFees = 0;
			Collection<BundleFee> bundleFees = BundleChargingSupport.getProvisionedBundles(ctx, SubscriberBundleSupport.getSubscribedBundles(ctx, subscriber).values(), 
					bundlesToBeCharged);
			
			for(BundleFee fee : bundleFees)
			{
				Date nextRecurringChargeDate = null;
				
				if(fee.getNextRecurringChargeDate() != null)
				{
					nextRecurringChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(fee.getNextRecurringChargeDate());
					
					if(LogSupport.isDebugEnabled(ctx))
	                {
	                    LogSupport.debug(ctx, this, "Bundle fees for subscriber : " + subscriber.getId() + " serviceID : " + fee.getId()
	                            + " nextRecurDate : "  + nextRecurringChargeDate + " running date : " + runningDate);
	                }
				}
				
				
				if(nextRecurringChargeDate != null && nextRecurringChargeDate.equals(runningDate))
				{
				    cumulativeBundleFees = cumulativeBundleFees + fee.getFee();
				} 
			}        
			
			pricePlanFee = pricePlanFee + cumulativeBundleFees;

			if(LogSupport.isDebugEnabled(ctx))
            {
			    LogSupport.debug(ctx, this, "Calculated bundle fees for subscriber : " + subscriber.getId() + " is : " + cumulativeBundleFees);
            }
			
			long cumulativeAuxServiceFees = 0; 
			for (SubscriberAuxiliaryService subAuxService: auxServicesToBeCharged)
			{
			    if(subAuxService == null)
			        continue;
			    
				AuxiliaryService service = subAuxService.getAuxiliaryService(ctx);
				 
				Date nextRecurringChargeDate =  null;
				 
				if(subAuxService.getNextRecurringChargeDate() != null)
				{
					nextRecurringChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subAuxService.getNextRecurringChargeDate());
			      
	                if(LogSupport.isDebugEnabled(ctx))
	                {
	                    LogSupport.debug(ctx, this, "Service fees for subscriber : " + subscriber.getId() + " serviceID : " + service.getID()
	                            + " nextRecurDate : "  + nextRecurringChargeDate + " running date : " + runningDate);
	                }
				}
				
	            if (nextRecurringChargeDate != null && nextRecurringChargeDate.equals(runningDate))
				{
				    cumulativeAuxServiceFees = cumulativeAuxServiceFees + service.getCharge();
				}
			}
			
			pricePlanFee = pricePlanFee + cumulativeAuxServiceFees;
			
			if(LogSupport.isDebugEnabled(ctx))
            {
			    LogSupport.debug(ctx, this, "Calculated auxiliary service fees subscriber : " + subscriber.getId() + " is : " + cumulativeAuxServiceFees);
			    LogSupport.debug(ctx, this, "Total charges for subscriber : " + subscriber.getId() + " is : " + pricePlanFee);
            }
            
		}
		catch (Throwable t)
		{
		    LogSupport.major(ctx, this, "Error calculating PricePlan charging fee for top up for Subscriber"+subscriber.getId()+"::"+ t.getMessage()+":StackTrace:"+t.getStackTrace(), t);
		}
		return pricePlanFee;
	}
	
	private long calculateScheduledPricplanFee(Context ctx, Subscriber subscriber) throws HomeInternalException, HomeException
	{
		long pricePlanFee = 0;
		Collection<SubscriberAuxiliaryService> auxServicesToBeCharged= new  HashSet<SubscriberAuxiliaryService>(); 
		Collection<Service> servicesToBeCharged = new  HashSet<Service>();
		
		And andSubModificationSchedule = new And();
		andSubModificationSchedule.add(new EQ(SubModificationScheduleXInfo.SUBSCRIPTION_ID, subscriber.getId()));
		andSubModificationSchedule.add(new EQ(SubModificationScheduleXInfo.STATUS, CronConstant.SCHEDULED_PENDING));
		SubModificationSchedule subModificationSchedule = HomeSupportHelper.get(ctx).findBean(ctx, SubModificationSchedule.class, andSubModificationSchedule);
        		
		try
		{
			List snapshotList = subModificationSchedule.getSnapshot();
			List supportingList = subModificationSchedule.getSupportingInformation();
			Date runningDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
			
			String[] requestSnapshotArr = null;
			String[] requestSupportingInfoArr = null;
			String message = null;
			
			if(snapshotList == null || snapshotList.size() != 1 || snapshotList.size() ==0){
				message = "Scheduled priceplan data improper for subscriber: [" + subModificationSchedule.getSubscriptionId()
						+ "] Proceeding to next entry.";
				if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
					LogSupport.debug(ctx,this, message);
			}		
			else
			{
				requestSnapshotArr = ((StringHolder)snapshotList.get(0)).getString().split(",");
			}
			
			if(supportingList != null && supportingList.size() > 0)
			{
				requestSupportingInfoArr = ((StringHolder)supportingList.get(0)).getString().split(",");
			}
			
			Map<String, String> requestSnapshotMap = new HashMap<String, String>(requestSnapshotArr.length);
			for(String requestSnapshot: requestSnapshotArr){
				requestSnapshotMap.put(requestSnapshot.split("=")[0], requestSnapshot.split("=")[1]);
			}
			
			Map<String, String> requestSupportSnapshotMap = null;
			if(requestSupportingInfoArr != null){
				requestSupportSnapshotMap = new HashMap<String, String>(requestSupportingInfoArr.length);
				for(String requestSupport: requestSupportingInfoArr){
					requestSupportSnapshotMap.put(requestSupport.split("=")[0], requestSupport.split("=")[1]);
	    		}
			}
			
			List<String> requestedServicesList = new ArrayList<String>();
	    	List<String> requestedBundlesList = new ArrayList<String>();
	    	
	    	if(requestSnapshotMap.get(CronConstant.SERVICES) != null && !requestSnapshotMap.get(CronConstant.SERVICES).equals("")){
	    		requestedServicesList = Arrays.asList(requestSnapshotMap.get(CronConstant.SERVICES).split("\\|"));
	    	}
	    	
	    	if(requestSnapshotMap.get(CronConstant.BUNDLES) != null && !requestSnapshotMap.get(CronConstant.BUNDLES).equals("")){
	    		requestedBundlesList = Arrays.asList(requestSnapshotMap.get(CronConstant.BUNDLES).split("\\|"));
	    	}
			
	    	if (requestSnapshotMap.get(CronConstant.NEW_PRICEPLAN_ID) != null)
		    {
		    	long toProvisionPriceplan =  Long.parseLong(requestSnapshotMap.get(CronConstant.NEW_PRICEPLAN_ID));
		    	Predicate wherePricePlan = new EQ(PricePlanXInfo.ID, toProvisionPriceplan);
		    	PricePlan pricePlan = HomeSupportHelper.get(ctx).findBean(ctx, PricePlan.class, wherePricePlan);
		    	
		    	And and = new And();
		    	and.add(new EQ(PricePlanVersionXInfo.ID, toProvisionPriceplan));
		    	and.add(new EQ(PricePlanVersionXInfo.VERSION, pricePlan.getCurrentVersion()));
		    	PricePlanVersion pricePlanVersion = HomeSupportHelper.get(ctx).findBean(ctx, PricePlanVersion.class, and);
		    	
		    	for (String serviceId : requestedServicesList)
		    	{
		    		Service service = ServiceChargingSupport.getServiceById(ctx, Long.parseLong(serviceId));
		    		servicesToBeCharged.add(service);
		    	}
		    		
		    	Map<com.redknee.app.crm.bean.core.ServiceFee2, Service> serviceFees = ServiceChargingSupport.getFutureServices(ctx, 
		    			pricePlanVersion.getServiceFees(ctx).values(), servicesToBeCharged);
		    	for (Iterator<com.redknee.app.crm.bean.core.ServiceFee2> i = serviceFees.keySet().iterator(); i.hasNext();)
				{
					ServiceFee2 fee = i.next();
					pricePlanFee  = pricePlanFee + fee.getFee();
				}
		    	if(LogSupport.isDebugEnabled(ctx))
	            {
				    LogSupport.debug(ctx, this, "Future services Total:" + pricePlanFee+":for Subscriber"+subscriber.getId());
	            }
		    	for (String bundleId : requestedBundlesList)
		    	{
		    		Map<Long, BundleFee> bundleFee = SubscriberBundleSupport.getPricePlanBundles(ctx, pricePlan, pricePlanVersion);
		    		BundleFee bundleFees = bundleFee.get(Long.parseLong(bundleId));
		    		if (bundleFees.getId() == Long.parseLong(bundleId))
		    		{
		    			pricePlanFee  = pricePlanFee + bundleFees.getFee();
		    		}
		    		
		    	}
		    	if(LogSupport.isDebugEnabled(ctx))
	            {
				    LogSupport.debug(ctx, this, "After adding future Bundle Total:" + pricePlanFee+":for Subscriber"+subscriber.getId());
	            }
		    	
		    }
	    	
	    	auxServicesToBeCharged.addAll(SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, subscriber.getId())); 
	    	
			for (SubscriberAuxiliaryService subAuxService: auxServicesToBeCharged)
			{
				AuxiliaryService service = subAuxService.getAuxiliaryService(ctx);
				Date nextRecurringChargeDate = null;
				if(subAuxService.getNextRecurringChargeDate() != null)
				{
					nextRecurringChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subAuxService.getNextRecurringChargeDate());
				}
				if (nextRecurringChargeDate != null && nextRecurringChargeDate.equals(runningDate))
				{
					pricePlanFee = pricePlanFee + service.getCharge();
				}
			}
			if(LogSupport.isDebugEnabled(ctx))
            {
			    LogSupport.debug(ctx, this, "After adding future AuxiliaryService Total:" + pricePlanFee+":for Subscriber"+subscriber.getId());
            }
		}
		catch (Throwable t)
		{
			new MinorLogMsg(this, "Error calculating PricePlan charging fee for top up for Subscriber"+subscriber.getId()+"::"+ t.getMessage(), t).log(ctx);
		}

		return pricePlanFee;
	}
}
