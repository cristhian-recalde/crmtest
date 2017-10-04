package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceSubTypeEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesID;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bean.paymentgatewayintegration.SubscriptionSupport;
import com.trilogy.app.crm.bundle.BundleAuxiliaryService;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceHome;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bundle.BundleFee;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.service.MultiDayPeriodHandler;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author kabhay, msahoo
 *
 * It executes super.create() first.
 */
public class UpdateNextRecurringChargeDateHome extends HomeProxy 
{

    private static final long serialVersionUID = 1L;


    public UpdateNextRecurringChargeDateHome(Home delegate) 
	{
		super(delegate);

	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{
		Object return_obj = super.create(ctx, obj);
		try
		{
			updateAssociation(ctx, (Transaction) obj);
		}catch (Exception e) {
			LogSupport.major(ctx, this, "Exception occurred while updating services next-recurring-charge-date!!",e);
		}
		
		return return_obj;
	}

	

	private void updateAssociation(Context ctx, Transaction txn) throws HomeException 
	{
		try
		{
			
			// Discount services will have negative/credit charge. Their next recurring charge date should be updated based on service period
			boolean isDiscountService = Boolean.FALSE;
			if(AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, txn.getAdjustmentType(), AdjustmentTypeEnum.Services) )
			{
				Service service = ServiceSupport.getServiceByAdjustment(ctx, txn.getAdjustmentType());
				if(service != null)
				{
					if(service.getServiceSubType().equals(ServiceSubTypeEnum.DISCOUNT))
					{
						isDiscountService = Boolean.TRUE;
					}
				}
			}
			
			/*
			 * The condition is added to skip update of next recurring charge date in case of refund/credit transactions. 
			 * Use case : If it is refund transaction and a $0 refundable bundle is provisioned to subscriber, 
			 * the below code of setting next recurring date for bundles updates the subscriber bean as well.
			 * The subscriber is updated by taking the old subscriber object from context, which results in reverting the new modified subscriber changes.
			 * Hence, added the logic to skip the update of next recurring charge date in case of $0 amount and if it is a refund transaction.
			 */
			
			if((txn.getAmount() < 0 && !isDiscountService) || (txn.getAmount() == 0 && ctx.getBoolean(ChargingConstants.IS_REFUND_TRANSACTION,false)))
			{
				LogSupport.info(ctx, this, "It is a refund/credit transaction, hence skipping update of next recurring charge date.");
				return; /*not for refund or credit*/
			}
			
			Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
			if(sub == null)
			{
				sub = SubscriberSupport.getSubscriber(ctx, txn.getSubscriberID());
			}
			
			
			if(AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, txn.getAdjustmentType(), AdjustmentTypeEnum.Services) )
			{
				/* price plan service */
				Service service = ServiceSupport.getServiceByAdjustment(ctx, txn.getAdjustmentType());
				if(service != null)
				{
					ServicePeriodEnum chargingScheme = service.getChargeScheme();
					if(chargingScheme.equals(ServicePeriodEnum.ONE_TIME))
					{
						return;
					}
					ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(chargingScheme);
					if(handler == null)
					{
						throw new HomeInternalException("Could not update next-recurring-charge-date for service - " + service.getID() +
					          " because handler is not found for charging-period : " + chargingScheme.getDescription()) ;
					}
					
					Account account = sub.getAccount(ctx);
					ctx.put(MultiDayPeriodHandler.CALCULATE_END_DATE_FROM_CYCLE_START, false);
					Date serviceCycleEndDate = handler.calculateCycleEndDate(ctx, txn.getReceiveDate(), 
					        account.getBillCycleDay(ctx), account.getSpid(), sub.getId(), service);
					/*Fix for TT# TTOTST-80 : Next Recurring Charge Date for Multi-Monthly Service and Aux. Service in MRC is Calculated wrong. So Adding check for Multi-Monthly*/
					if((chargingScheme.equals(ServicePeriodEnum.MONTHLY) || chargingScheme.equals(ServicePeriodEnum.MULTIMONTHLY)) && sub.getSubscriberType().getIndex() == SubscriberTypeEnum.POSTPAID_INDEX)
					{
						/*
						 * The condition is added to set next recurring charge date monthly service of postpaid subscriber. 
						 * We observe that at the time of monthly service , next recurring charge date is not set properly after running Monthly Recurring Charges task also.
						 * Use case : If billing date is 1 (first day of month), 
						 * System create the transaction of previous day of billing date i.e. last day of previous month. if billing date is 1 Oct then transaction date is created on 30 Sept.
						 * So System takes transaction month and calculate the last day of that month and add one (billing day) and set next recurring charge date so it is 1 Oct, which is wrong.
						 * Hence, added the logic to take proper month and set proper next recurring charge date.
						 */
						Calendar transactionReceivedDate = Calendar.getInstance();
						transactionReceivedDate.setTime(txn.getReceiveDate());
						transactionReceivedDate.add(Calendar.DAY_OF_YEAR,1);					
						serviceCycleEndDate = handler.calculateCycleEndDate(ctx, transactionReceivedDate.getTime(), 
								account.getBillCycleDay(ctx), account.getSpid(), sub.getId(), service);
					}
					
					
					Calendar cal = Calendar.getInstance();
					cal.setTime(serviceCycleEndDate);
					cal.add(Calendar.DAY_OF_YEAR,1); // Cycle-end-date + 1 = next-recurring-charge-date
						
					Date nextRecurChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(cal.getTime());					
							
					updateSubsriberServicesNextRecurringChargeDate(ctx,sub.getId(),service.getID(),nextRecurChargeDate);
					
					if(sub.getSubscriberType().getIndex() == SubscriberTypeEnum.PREPAID_INDEX){
						updateTopupSchedule(ctx, sub, service.getID(), nextRecurChargeDate);
					}
				}
				/* get the CUG price plan services */
				else if(service == null)
				{
					Subscriber supportedSubscriber = SubscriberSupport.getSubscriber(ctx, txn.getSupportedSubscriberID());
					List<Service> cugServices = new ArrayList<Service>();
					if(supportedSubscriber != null)
					{
    					Set<ServiceFee2ID> services = supportedSubscriber.getServices(ctx);
    					if(!services.isEmpty())
    					{
    						for (Iterator<ServiceFee2ID> iter = services.iterator(); iter.hasNext();)
    						{ 
    							ServiceFee2ID serviceFee2ID = iter.next();
    							Service serviceTemp = ServiceSupport.getService(ctx, serviceFee2ID.getServiceId());
    							if (serviceTemp != null)
    							{
    								if (serviceTemp.getType().equals(ServiceTypeEnum.CALLING_GROUP))
    								{
    									cugServices.add(serviceTemp);
    								}
    							}
    						}
    					}
					}
					if(!cugServices.isEmpty())
					{
						for(Service cugService: cugServices)
						{
							ServicePeriodEnum chargingScheme = cugService.getChargeScheme();
							if(chargingScheme.equals(ServicePeriodEnum.ONE_TIME))
							{
								return;
							}
							ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(chargingScheme);
							if(handler == null)
							{
								throw new HomeInternalException("Could not update next-recurring-charge-date for service - " + cugService.getID() +
							          " because handler is not found for charging-period : " + chargingScheme.getDescription()) ;
							}
							
							Account account = supportedSubscriber.getAccount(ctx);
							ctx.put(MultiDayPeriodHandler.CALCULATE_END_DATE_FROM_CYCLE_START, false);
							Date serviceCycleEndDate = handler.calculateCycleEndDate(ctx, txn.getReceiveDate(), 
							        account.getBillCycleDay(ctx), account.getSpid(), supportedSubscriber.getId(), cugService);
							Calendar cal = Calendar.getInstance();
							cal.setTime(serviceCycleEndDate);
							cal.add(Calendar.DAY_OF_YEAR,1); // Cycle-end-date + 1 = next-recurring-charge-date
							
							Date nextRecurChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(cal.getTime());
							
							updateSubsriberServicesNextRecurringChargeDate(ctx,supportedSubscriber.getId(),cugService.getID(),nextRecurChargeDate);
							
							if(supportedSubscriber.getSubscriberType().getIndex() == SubscriberTypeEnum.PREPAID_INDEX){
								updateTopupSchedule(ctx, supportedSubscriber, cugService.getID(), nextRecurChargeDate);
							}
						}
					}
				}
				
			}else if(AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, txn.getAdjustmentType(), AdjustmentTypeEnum.AuxiliaryServices)  || 
					AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, txn.getAdjustmentType(), AdjustmentTypeEnum.AuxiliaryService) )
			{
				/*
				 * Search in aux-service
				 */
				 AuxiliaryService auxService = AuxiliaryServiceSupport.getAuxiliaryServiceByAdjustmentType(ctx, txn.getAdjustmentType());
				 
				
				if(auxService != null)
				{
					
					ServicePeriodEnum chargingScheme = auxService.getChargingModeType();
					if(chargingScheme.equals(ServicePeriodEnum.ONE_TIME))
					{
						return;
					}
					ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(chargingScheme);
					if(handler == null)
					{
						throw new HomeInternalException("Could not update next-recurring-charge-date for aux-service - " + auxService.getID() + 
						        " because handler is not found for charging-period : " + chargingScheme.getDescription()) ;
					}
					
					if(AuxiliaryServiceTypeEnum.CallingGroup.equals(auxService.getType()) && txn.getSupportedSubscriberID()!=null)
					{
						if(auxService.getAggPPServiceChargesToCUGOwner())
						{
							Subscriber supportedSubscriber = SubscriberSupport.getSubscriber(ctx, txn.getSupportedSubscriberID());
							if(supportedSubscriber != null)
							{
								sub = supportedSubscriber;
							}
						}
					}
					
					Account account = sub.getAccount(ctx);
					
					SubscriberAuxiliaryService subAuxServ = (SubscriberAuxiliaryService) ctx.get(RecurringRechargeSupport.RECURRING_RECHARGE_CHARGED_ITEM);
					
					if(subAuxServ == null)
					{
					    Collection<SubscriberAuxiliaryService> subAuxServColl = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(ctx, 
					            sub.getId(),auxService.getID() );
	                    
	                    if(subAuxServColl.size()<=0)
	                    {
	                        throw new HomeInternalException("Could not update next-recurring-charge-date for aux-service - " + auxService.getID() + 
	                                " because SubscriberAuxiliaryService is not found for sub : " + sub.getId() + ", aux-service : " + auxService.getID() );
	                        
	                    }
	                    
	                    /*
	                     * Not sure how come subscriber can have more than one associations with the same aux-service. TODO: will ask DNA. 
	                     */
	                    subAuxServ = subAuxServColl.iterator().next();
					}				
					
					Date recDate = txn.getReceiveDate();
					Date serviceCycleEndDate = handler.calculateCycleEndDate(ctx, recDate, account.getBillCycleDay(ctx), account.getSpid(), 
					        sub.getId(), subAuxServ);
					/*Fix for TT# TTOTST-80 : Next Recurring Charge Date for Multi-Monthly Service and Aux. Service in MRC is Calculated wrong. So Adding check for Multi-Monthly*/
					if((chargingScheme.equals(ServicePeriodEnum.MONTHLY) || chargingScheme.equals(ServicePeriodEnum.MULTIMONTHLY))&& account.isPostpaid())
                    {
						Calendar transactionReceivedDate = Calendar.getInstance();
						transactionReceivedDate.setTime(txn.getReceiveDate());
						transactionReceivedDate.add(Calendar.DAY_OF_YEAR,1);					
						serviceCycleEndDate = handler.calculateCycleEndDate(ctx, transactionReceivedDate.getTime(), 
								account.getBillCycleDay(ctx), account.getSpid(), sub.getId(), subAuxServ);
                    }
					/*Fix for TT 14110753001 NextRecurring Charge date of Non Refundable aux service of postpaid subscriber is coming wrong when provisioned*/	
					/*Fix for TT 12051834015 (Account Management > Run Recurring Recharge(RRC)) When Running through RRC Next Recurring Charge was setting to new Next Recurring Date.*/
					
					Calendar cal = Calendar.getInstance();
					cal.setTime(serviceCycleEndDate);
					cal.add(Calendar.DAY_OF_YEAR,1); // Cycle-end-date + 1 = next-recurring-charge-date
					Date nextRecurChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(cal.getTime());
					subAuxServ.setNextRecurringChargeDate(nextRecurChargeDate);
					
					//This code is making a recursive call from the current transactionPipeline
					//As this invoking the SubscriberAuxiliaryServicePipeLine again.However the current code is in the same call from the 
					//SubscriberAuxiliaryServicePipeline itself
					//SubscriberAuxiliaryServiceSupport.updateSubscriberAuxiliaryService(ctx, subAuxServ);
					SubscriberAuxiliaryServiceSupport.updateSubscriberAuxiliaryServiceOnXDBHomeDirectly(ctx, subAuxServ);
					
					/*
					 * Refresh subscriber aux services
					 * TT # 12050841046
					 */
					sub.setAuxiliaryServices(null);
					
					/*
					 * Refresh suspended aux service
					 * TT #  12051643012 
					 */
					sub.setSuspendedAuxServices(null);
				}
			}else if(AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, txn.getAdjustmentType(), AdjustmentTypeEnum.Bundles) )
			{
				/*
				 * Bundles
				 */
				
				//BundleProfile bundleProfile = BundleSupportHelper.get(ctx).getBundleByAdjustmentType(ctx, txn.getAdjustmentType());

				BundleProfile bundleProfile = getNonAuxiliaryBundleProfileByAdjustmentType(ctx, txn.getAdjustmentType());
				if(bundleProfile == null)
				{
					return;
				}
				
				updateNextRecurringChargeDate(ctx,  sub, txn, bundleProfile);
								
			}else if(AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, txn.getAdjustmentType(), AdjustmentTypeEnum.AuxiliaryBundles) )
			{
				/*
				 * Aux Bundles
				 */
				BundleProfile bundleProfile = getAuxiliaryBundleProfileByAdjustmentType(ctx, txn.getAdjustmentType());
				if(bundleProfile == null)
				{
					return;
				}
				
				com.redknee.app.crm.bean.AuxiliaryService auxSrv = SubscriberAuxiliaryServiceSupport
                        .findSubscriberProvisionedAuxiliaryServicesByType(ctx, sub,
                                AuxiliaryServiceTypeEnum.CallingGroup);
                if(auxSrv != null )
                {
                    if(auxSrv.getAggPPServiceChargesToCUGOwner())
                    {
                        Subscriber supportedSubscriber = SubscriberSupport.getSubscriber(ctx, txn.getSupportedSubscriberID());
                        if(supportedSubscriber != null)
                        {
                            sub = supportedSubscriber;
                        }
                    }
                }
				
				updateNextRecurringChargeDate(ctx,  sub, txn, bundleProfile);
				
			}
			
			
			
		}catch (Exception e) {
			throw new HomeException(e) ;
		}
	}

	
	
	private void updateNextRecurringChargeDate(Context ctx, Subscriber sub,Transaction txn, BundleProfile bundleProfile) 
	        throws HomeException, CloneNotSupportedException
	{
		ServicePeriodEnum chargingScheme = bundleProfile.getChargingRecurrenceScheme();
		if(chargingScheme.equals(ServicePeriodEnum.ONE_TIME))
		{
			return;
		}
		
		ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(chargingScheme);
		if(handler == null)
		{
			throw new HomeInternalException("Could not update next-recurring-charge-date for Bundle - " + bundleProfile.getBundleId() + 
			        " because handler is not found for charging-period : " + chargingScheme.getDescription()) ;
		}
		
		Account account = sub.getAccount(ctx);
		Date serviceCycleEndDate = handler.calculateCycleEndDate(ctx, txn.getReceiveDate(), account.getBillCycleDay(ctx), 
		        account.getSpid(), sub.getId(), bundleProfile);
		Calendar cal = Calendar.getInstance();
		cal.setTime(serviceCycleEndDate);
		cal.add(Calendar.DAY_OF_YEAR,1); // Cycle-end-date + 1 = next-recurring-charge-date
		Date nextRecurChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(cal.getTime());
		
		Map<Long, BundleFee> bundleFeeMap = sub.getBundles();
		if(bundleFeeMap != null & bundleFeeMap.size() > 0)
		{
			BundleFee bundleFee = bundleFeeMap.get(bundleProfile.getBundleId());
			if(bundleFee != null)
			{
				bundleFee.setNextRecurringChargeDate(nextRecurChargeDate);
				/*
				 * Using directly XDB home for high performance. Dont want to go through entire subscriber home pipeline 
				 * just for updating next-recurring-charge-home.
				 * 
				 */
				Home subXdbHome = (Home)ctx.get(SubscriberXDBHome.class);
				
				
				Subscriber clonedSub = (Subscriber) sub.clone();
				clonedSub.setBundles(bundleFeeMap);
				subXdbHome.store(ctx,clonedSub);
				
				/*
				 * update BundleAuxiliaryService , table - BUNDLEAUXSERV
				 */
				
				
				Home bundleAuxServiceHome = (Home)ctx.get(BundleAuxiliaryServiceHome.class);
				And and = new And();
				and.add(new EQ(BundleAuxiliaryServiceXInfo.SUBSCRIBER_ID,sub.getId()));
				and.add(new EQ(BundleAuxiliaryServiceXInfo.ID,bundleProfile.getBundleId()));
				BundleAuxiliaryService bean = (BundleAuxiliaryService) bundleAuxServiceHome.find(ctx,and);
				if(bean != null)
				{
					bean.setNextRecurringChargeDate(nextRecurChargeDate);
					bundleAuxServiceHome.store(ctx,bean);
				}
			}
			
		}
		
	}

	private BundleProfile getAuxiliaryBundleProfileByAdjustmentType(Context ctx, int adjustmentId) throws HomeException
	{
		
		Home bundleProfileHome = (Home) ctx.get(BundleProfileHome.class);
		
		return (BundleProfile) bundleProfileHome.find(ctx, new EQ(BundleProfileXInfo.AUXILIARY_ADJUSTMENT_TYPE, adjustmentId));
		
	}
	
	private BundleProfile getNonAuxiliaryBundleProfileByAdjustmentType(Context ctx, int adjustmentId) throws HomeException
	{
		
		Home bundleProfileHome = (Home) ctx.get(BundleProfileHome.class);
		
		return (BundleProfile) bundleProfileHome.find(ctx, new EQ(BundleProfileXInfo.ADJUSTMENT_TYPE, adjustmentId));
		
	}
	
	
	private void updateSubsriberServicesNextRecurringChargeDate(Context ctx,
			String subscriberId, long serviceId, Date nextRecurChargeDate) throws HomeInternalException, HomeException 
	{
		Home subscriberServicesHome = (Home) ctx.get(SubscriberServicesHome.class);
		
		if(subscriberServicesHome == null)
		{
			throw new HomeInternalException("Could not update next-recurring-charge-date because SubscriberServicesHome " +
					"not found in context! Sub-id : " + subscriberId + " , service-id : " + serviceId ) ;
		}
		
		/*SubscriberServices subscriberService = (SubscriberServices) subscriberServicesHome.find(ctx, 
		        new SubscriberServicesID(serviceId, subscriberId));*/
		
		And and = new And();
		and.add(new EQ(com.redknee.app.crm.bean.SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId));
		and.add(new EQ(com.redknee.app.crm.bean.SubscriberServicesXInfo.SERVICE_ID, serviceId));
		SubscriberServices subscriberService = HomeSupportHelper.get(ctx).findBean(ctx, SubscriberServices.class, and );

		if(subscriberService == null)
		{
			return; /*it may not be recurring service*/
		}
		
		subscriberService.setNextRecurringChargeDate(nextRecurChargeDate);
		subscriberServicesHome.store(ctx, subscriberService);
	}
	
	private void updateTopupSchedule(Context ctx, Subscriber subscriber, long chargedServiceId, Date nextRecurChargeDate) 
            throws HomeInternalException, HomeException
    {
	    
	    Predicate where = new EQ(TopUpScheduleXInfo.SUBSCRIPTION_ID, subscriber.getId());
        TopUpSchedule schedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, where);
        
        if(schedule != null && schedule.isScheduleUserDefined())
        {
            /**
             * determineNextTopupScheduleDate checks if the current value of nextRecurChargeDate is passed, if yes it sets to new recurring date
             */
            nextRecurChargeDate = SubscriptionSupport.determineNextTopupScheduleDate(ctx, nextRecurChargeDate);
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Current schedule is UserDefined, Updating TopUpSchedule [ID: ");
                sb.append(schedule.getId());
                sb.append("] for Subscriber [ID: ");
                sb.append(subscriber.getId());
                sb.append("] current nextApplication: [");
                sb.append(schedule.getNextApplication());
                sb.append("] with nextApplication: [");
                sb.append(nextRecurChargeDate);
                sb.append("].");
                LogSupport.debug(ctx, this, sb.toString());
            }
            schedule.setNextApplication(nextRecurChargeDate);
            HomeSupportHelper.get(ctx).storeBean(ctx, schedule);
            return;
        }
        
        PricePlanVersion ppv = null;
        try
        {
            ppv = subscriber.getRawPricePlanVersion(ctx);
        }
        catch (HomeException e)
        {
            LogSupport.info(ctx, this, "Cannot obtain PricePlanVersion for subscriber [" + subscriber.getId() +"].", e);
        }
        
        if (ppv == null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("No PricePlanVersion found for Subscriber ID: ");
                sb.append(subscriber.getId());
                sb.append(". Cannot update nextApplication date of TopUpSchedule.");
                LogSupport.debug(ctx, this, sb.toString());
            }
            return;
        }
        
        ServiceFee2 fee = null; 
        //ServiceFee2 fee = ppv.getPrimaryService(ctx);
        // For PickNPay priceplan there can be multiple primary services, 
        // hence in any case choose the one that's attached to subscriber to determine next topup date
        fee = SubscriptionSupport.getProvisionedPrimaryService(ctx, subscriber);
        
        if (fee == null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("No attached Primary service for Subscriber [ID: ");
                sb.append(subscriber.getId());
                sb.append("]. Not updating nextApplication date of TopUpSchedule.");
                LogSupport.debug(ctx, this, sb.toString());
            }
            return;
        }
        
        if (chargedServiceId == fee.getServiceId()) //CreditCardTopUpFrequencyEnum.PRIMARY_SERVICE_RECURRENCE
        {
            if (schedule == null)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("No TopUpSchedule found for Subscriber [ID: ");
                    sb.append(subscriber.getId());
                    sb.append("].");
                    LogSupport.debug(ctx, this, sb.toString());
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Updating TopUpSchedule [ID: ");
                    sb.append(schedule.getId());
                    sb.append("] for Subscriber [ID: ");
                    sb.append(subscriber.getId());
                    sb.append("] with nextApplication: [");
                    sb.append(nextRecurChargeDate);
                    sb.append("].");
                    LogSupport.debug(ctx, this, sb.toString());
                }
                schedule.setNextApplication(nextRecurChargeDate);
                HomeSupportHelper.get(ctx).storeBean(ctx, schedule);
            }
        }
        else
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Charged Service [ID: ");
                sb.append(fee.getServiceId());
                sb.append("] is not a Primary Service for Subscriber [ID: ");
                sb.append(subscriber.getId());
                sb.append("]. Not updating nextApplication date of TopUpSchedule.");
                LogSupport.debug(ctx, this, sb.toString());
            }
        }
    }
}
