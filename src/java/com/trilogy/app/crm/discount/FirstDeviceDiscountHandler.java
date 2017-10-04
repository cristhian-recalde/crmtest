/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.discount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.DiscountEventActivity;
import com.trilogy.app.crm.bean.DiscountEventActivityServiceTypeEnum;
import com.trilogy.app.crm.bean.DiscountEventActivityStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventActivityTypeEnum;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleEngineUtility;
import com.trilogy.app.crm.core.ruleengine.DiscountCriteriaTypeEnum;
import com.trilogy.app.crm.core.ruleengine.EventTypeEnum;
import com.trilogy.app.crm.core.ruleengine.FirstDeviceDiscountHolder;
import com.trilogy.app.crm.core.ruleengine.FirstDeviceDiscountOutputHolder;
import com.trilogy.app.crm.core.ruleengine.engine.exception.NoRuleFoundException;
import com.trilogy.app.crm.core.ruleengine.engine.ifc.BusinessRuleIfc;
import com.trilogy.app.crm.core.ruleengine.util.RuleEngineConstants;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.DiscountSupportImpl;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Discount handler for the First Device discounts
 * @author abhijit.mokashi
 *
 */
public class FirstDeviceDiscountHandler implements DiscountHandler{
	private Map<Integer,Object> prameterMap_ = new HashMap<Integer, Object>();
	public static String MODULE = FirstDeviceDiscountHandler.class.getName();
	@Override
	public boolean init(Context context,
			Account account,List<SubscriberIfc> subscriberList) {
		return true;
	}

	@Override
	public boolean evaluate(Context context, Account account,
			List<SubscriberIfc> subscriberList,
			Collection<DiscountEventActivity> existingDiscountActivities,
			Map<String, Boolean> trackDiscount,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate,
			Collection<DiscountEventActivity> discountEventActivityContinued) {
		boolean discountGiveToFirstDevice=false;
		Map<Long, List<SubscriberIfc>> subscriberMap = DiscountUtils.getSubscriptionMapByType(context, subscriberList);
		Iterator<Long> itr = subscriberMap.keySet().iterator();

		while(itr.hasNext()){
			discountGiveToFirstDevice=false;
			List<SubscriberIfc> sortedSubscriberList = subscriberMap.get(itr.next());
			if(sortedSubscriberList.size() > 1){
				List<SubscriberIfc> secondarySubscriberList = sortedSubscriberList.subList(1, sortedSubscriberList.size());
				
				  for(SubscriberIfc secondaySubscriber : secondarySubscriberList){
					  if(!discountGiveToFirstDevice)
						{
						try{
							List<SubscriberIfc> subsListForRuleEvaluation = new ArrayList<SubscriberIfc>();
							subsListForRuleEvaluation.add(sortedSubscriberList.get(0));
							subsListForRuleEvaluation.add(secondaySubscriber);
	
							try	{
								// checking for Primary Subscription 
								List<AbstractBean> list = new ArrayList<AbstractBean>();
								FirstDeviceDiscountHolder holder;
								for (SubscriberIfc subscriber : subsListForRuleEvaluation)
								{
									Map<ServiceFee2ID,SubscriberServices> container=SubscriberServicesSupport.getSubscribersServices(context,subscriber.getId());
									if(null!=container && !container.isEmpty())
									{
										Set<ServiceFee2ID> serviceIDCollection = container.keySet();
										Iterator<ServiceFee2ID> serviceIdIterator = serviceIDCollection.iterator();
	
										while(serviceIdIterator.hasNext())
										{
											holder  = new FirstDeviceDiscountHolder();
											holder.setSubscriptionType(subscriber.getSubscriptionType());
											holder.setPricePlanID(subscriber.getPricePlan());
											holder.setServiceID(serviceIdIterator.next().getServiceId());
											list.add(holder);
										}
									}
								}
								prameterMap_.put(RuleEngineConstants.DISCOUNT_TYPE, DiscountCriteriaTypeEnum.FIRST_DEVICE);
								prameterMap_.put(RuleEngineConstants.SUBSCRIBER_EVENTS, list);
								
								if(context.get(DiscountSupportImpl.DISCOUNT_GRADE) != null)
								{
									prameterMap_.put(RuleEngineConstants.DISCOUNT_GRADE,
											context.get(DiscountSupportImpl.DISCOUNT_GRADE));
								}
	
							} catch (Exception e){
								new MinorLogMsg(this, "Failed to initialize the First Device discount handler for account '");
								return false;
							}
	
							BusinessRuleIfc output = BusinessRuleEngineUtility.evaluateRule(context,EventTypeEnum.DISCOUNT,
									account, subsListForRuleEvaluation, prameterMap_);
	
							// process the output
							if(null != output.getFirstDeviceOutput() && output.getFirstDeviceOutput().size()>0 && 
									output.getFirstDeviceOutput().get(0) instanceof FirstDeviceDiscountOutputHolder){ //In case of second device discount there will be single output
	
								
							    LogSupport.info(
											context,
											MODULE,
											"Starting processing of First Device Discount");
								
								DiscountEventActivity discountEventActivity = getDiscountEventActivityIfAvailable(context, existingDiscountActivities
										,output.getFirstDeviceOutput().get(0),output,subsListForRuleEvaluation.get(0));
	
								if(null == discountEventActivity){
									discountEventActivity = DiscountActivityUtils.createDiscountEventActivityForFirstDeviceDiscount(context, 
											existingDiscountActivities, 
											output, 
											subsListForRuleEvaluation.get(0), 
											account);
									if (LogSupport.isDebugEnabled(context)) {
										LogSupport.debug(
												context,
												MODULE,
												"Creating new Discount Event Activity for First Device for subscriber :"
												+ discountEventActivity.getSubId() + " And effective Date is : " +discountEventActivity.getDiscountEffectiveDate());
									}
									validateAndFilterDiscount(context, 
											existingDiscountActivities, 
											discountEventActivity, 
											discountEventActivityForCreation, 
											discountEventActivityForUpdate,
											discountEventActivityContinued);
									new DebugLogMsg(this, "Rule has been matched for First device discount and processing discount for ban " + account.getBAN()).log(context);
									discountEventActivityForCreation.add(discountEventActivity);
									trackDiscount.put(subsListForRuleEvaluation.get(0).getId(), true);
									discountGiveToFirstDevice=true;
								}else{
									// in case of discount event entry already present in old list, we don't need to validate and filter again
									// as it was already in sync with all other discount
									if(DiscountActivityUtils.checkIfRuleVersionChange(context, discountEventActivity, output))
									{
										discountEventActivity.setDiscountRuleVersion(output.getRuleVersion(context));
										discountEventActivityForUpdate.add(discountEventActivity);
									}else
									{
										discountEventActivityContinued.add(discountEventActivity);
									}
									if (LogSupport.isDebugEnabled(context)) {
										LogSupport.debug(
												context,
												MODULE,
												"Discount Event Activity for First Device exists for subscriber :"
												+ discountEventActivity.getSubId() + " And effective Date is : " +discountEventActivity.getDiscountEffectiveDate());
									}
									trackDiscount.put(subsListForRuleEvaluation.get(0).getId(), true);
									discountGiveToFirstDevice=true;
								}
							}
						}catch(NoRuleFoundException ex)
						{
							new DebugLogMsg(this, "No First Device Discount Rule matched for the BAN:'" + account.getBAN()).log(context);
						}
				   }
				}
			}
		}

		// check if the new rule version is effective


		return true;
	}

	private void validateAndFilterDiscount(Context context,
			Collection<DiscountEventActivity> existingDiscountActivities,
			DiscountEventActivity discountEventActivity,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate,
			Collection<DiscountEventActivity> discountEventActivityContinued) {

		LogSupport.info(
				context,
				MODULE,
				"Start validating First Device Discount event activity created with existing snapshot");
		/*// need to check if the new version of rule applied is present
		DiscountActivityUtils.checkRuleVersionChange(context,
				discountEventActivity,
				existingDiscountActivities,
				discountEventActivityForCreation,
				discountEventActivityForUpdate);*/

		/*DiscountActivityUtils.checkNotApplicableDiscounts(context,
				discountEventActivity,
				existingDiscountActivities,
				discountEventActivityForCreation,
				discountEventActivityForUpdate,
				discountEventActivityContinued);*/
		checkForApplicableExpiredFDDDiscount(context,
				discountEventActivity,
				existingDiscountActivities,
				discountEventActivityForCreation,
				discountEventActivityForUpdate);
		
		checkForPriceplanChangeForExistingDiscountedSub(context,
				discountEventActivity,
				existingDiscountActivities,
				discountEventActivityForCreation,
				discountEventActivityForUpdate);
		if(DiscountActivityUtils.isFeePersonalizedAvailableForSubscriptionService(context, discountEventActivity))
		{
			checkForFeePersonalizationEventForExistingDiscount(context,
					discountEventActivity,
					existingDiscountActivities,
					discountEventActivityForCreation,
					discountEventActivityForUpdate);
		}
	}
	
	@Override
	public boolean generateTransactions(final Context context, final Account account,
			 final List<DiscountEventActivity> discountActivityEventList,
    		 List<DiscountEventActivity> discountEventToBeUpdated) 
	{
	   	for(DiscountEventActivity discountEventActivity : discountActivityEventList){
			if(discountEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.FIRST_DEVICE))
			{
				if(DiscountActivityUtils.isApplicableForDiscountTransaction(context, discountEventActivity)){
					if (LogSupport.isDebugEnabled(context)) {
						LogSupport.debug(
								context,
								MODULE,
								"Applying Discount for Account: " + account.getBAN()
								+ "  And discount event "
								+ discountEventActivity.getId());
					}
	
					try {
						Transaction discountTransaction = generateFirstDeviceDiscountTransaction(context, discountEventActivity);
						if(null!=discountTransaction)
						{
							/*discountEventActivity.setDiscountAppliedFromDate(DiscountActivityUtils.getDiscountAppliedPeriodStartDate(context,discountEventActivity,account));
							discountEventActivity.setDiscountAppliedTillDate(DiscountActivityUtils.getDiscountAppliedPeriodEndDate(context,discountEventActivity,account));
						*/	discountEventToBeUpdated.add(discountEventActivity);
							
						}
						
					} catch (Exception e) {
						LogSupport.minor(
								context,
								MODULE,
								"Exception Occured while creating transaction for " + account.getBAN()
								+ "  And discount event "
								+ discountEventActivity.getId() + " exception is : " + e.getMessage());
						//e.printStackTrace();	
					}
	
				} else if (DiscountActivityUtils.isApplicableForReverseDiscountTransaction(context, discountEventActivity)){
					if (LogSupport.isDebugEnabled(context)) {
						LogSupport.debug(
								context,
								MODULE,
								"Reversing Discount for Account: " + account.getBAN()
								+ "  And discount event "
								+ discountEventActivity.getId());
					}
	
					try {
						Transaction reverseDiscountTransaction = generateFirstDeviceReverseDiscountTransaction(context, discountEventActivity);
						if(null!=reverseDiscountTransaction)
						{
							
							discountEventActivity.setDiscountAppliedTillDate(discountEventActivity.getDiscountExpirationDate());
							discountEventActivity.setState(DiscountEventActivityStatusEnum.CANCELLED);
							discountEventToBeUpdated.add(discountEventActivity);
							
						}else if((null!=discountEventActivity.getDiscountAppliedTillDate() ||
								0<discountEventActivity.getDiscountAppliedTillDate().getTime()) 
								&& discountEventActivity.getDiscountExpirationDate().getTime() == discountEventActivity.getDiscountAppliedTillDate().getTime())
						{
							discountEventActivity.setState(DiscountEventActivityStatusEnum.CANCELLED);
							discountEventToBeUpdated.add(discountEventActivity);
						}
					} catch (Exception e) {
						LogSupport.minor(
								context,
								MODULE,
								"Exception Occured while creating transaction for " + account.getBAN()
								+ "  And discount event "
								+ discountEventActivity.getId() + " exception is : " + e.getMessage());
						//e.printStackTrace();
					}
					
				} else {
					LogSupport
					.debug(context,
							MODULE,
							"Discount event activity entry not applicable for transaction generation :"
									+ discountEventActivity.toString());
				}
		  }
		}
		return true;
	}

	/**
	 * This method will generate transaction for discount and update the
	 * contract table accordingly
	 * 
	 * @param ctx
	 * @param discEventActivity
	 * @throws HomeException
	 */
	private Transaction generateFirstDeviceDiscountTransaction(Context ctx,
			DiscountEventActivity discEventActivity)
					throws HomeException {
		
		
		Transaction discountTransaction =  DiscountSupportImpl.generateDiscountTransactions(ctx, discEventActivity);
		return discountTransaction;
		
	}
	
	/**
	 * This method will generate transaction for discount and update the
	 * contract table accordingly
	 * 
	 * @param ctx
	 * @param discEventActivity
	 * @throws HomeException
	 */
	private Transaction generateFirstDeviceReverseDiscountTransaction(Context ctx,
			DiscountEventActivity discEventActivity)
					throws HomeException {
		Transaction discountTransaction =  DiscountSupportImpl.generateReverseDiscountTransactions(ctx, discEventActivity);
		return discountTransaction;
		
	}
	
	private DiscountEventActivity getDiscountEventActivityIfAvailable(final Context context,final Collection<DiscountEventActivity> discountEventsActivity,
			final FirstDeviceDiscountOutputHolder object,final BusinessRuleIfc output,SubscriberIfc subscriber)
	{
		DiscountEventActivity discountEventActivityAvailable = null;
		Iterator<DiscountEventActivity> iterator = discountEventsActivity.iterator();
		while(iterator.hasNext())
		{
			DiscountEventActivity discEventActivity = iterator.next();
			if(discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.FIRST_DEVICE))
			{
				if(discEventActivity.getServiceId()==object.getServiceID() 
						&& discEventActivity.getDiscountRuleId().equals(output.getRuleId())
						&& discEventActivity.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.SERVICE)
						&& discEventActivity.getSubId().equals(subscriber.getId())
						&& discEventActivity.getState().equals(DiscountEventActivityStatusEnum.ACTIVE)
						&& !subscriber.getState().equals(SubscriberStateEnum.SUSPENDED)
						&& discEventActivity.getDiscountClass() == object.getDiscountClass())
				{
					if(DiscountActivityUtils.isFeePersonalizedAvailableForSubscriptionService(context,discEventActivity))
					{
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									"getDiscountEventActivityIfAvailable : Fee Personalization  : Found matched discount event for Fee Personalization FirstDevice discount "
									+ discEventActivity.getId());
						}
						break;
					}else{
						discountEventActivityAvailable = discEventActivity;
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									"Found Matched discount event activity for First Device Discount"+ discEventActivity.getId());
						}
						break;
					}
				}
			}
		}
		return discountEventActivityAvailable;
	}
	
	private void checkForApplicableExpiredFDDDiscount(Context context,
			DiscountEventActivity discountEventActivity,
			Collection<DiscountEventActivity> existingDiscountActivities,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate) {
		
		if(existingDiscountActivities!=null && !existingDiscountActivities.isEmpty())
		{
			//find if the same rule for same service with different subscriber id is already applied
			for(DiscountEventActivity currDisc : existingDiscountActivities){
				// condition for same rule with different version comparison
				if(currDisc.getBan().equals(discountEventActivity.getBan()) &&
						currDisc.getDiscountType().equals(discountEventActivity.getDiscountType()) &&
						currDisc.getDiscountRuleId().equals(discountEventActivity.getDiscountRuleId()) &&
						currDisc.getDiscountServiceType().equals(discountEventActivity.getDiscountServiceType()) &&
						(currDisc.getState().equals(DiscountEventActivityStatusEnum.CANCELLATION_PENDING)
								|| currDisc.getState().equals(DiscountEventActivityStatusEnum.CANCELLED))&&
					    currDisc.getServiceId() == discountEventActivity.getServiceId()		
						){
					
					if(currDisc.getDiscountExpirationDate().after(discountEventActivity.getDiscountEffectiveDate()))
					{
						discountEventActivity.setDiscountEffectiveDate(
								CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(
										CalendarSupportHelper.get(context).getDayAfter(currDisc.getDiscountExpirationDate())));
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									 "Got old Combination Discount event activity for ban" + currDisc.getBan()
									+"and eventId is : " + currDisc.getId() + "and subscriber is : " +currDisc.getSubId()
									+"and Expiry Date is : " + currDisc.getDiscountExpirationDate()
									+" Creating New DiscountEventACtivity for subscriber : " + discountEventActivity.getSubId()
									+"effective date for the new entry is : " + discountEventActivity.getDiscountEffectiveDate()
									+"for the rule id is : " + discountEventActivity.getDiscountRuleId());
					    }
					}
				
				}
			}
		}
		
		
	}
	
	private void checkForPriceplanChangeForExistingDiscountedSub(Context context,
			DiscountEventActivity discountEventActivity,
			Collection<DiscountEventActivity> existingDiscountActivities,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate) {
		
		if(null!=existingDiscountActivities && !existingDiscountActivities.isEmpty())
		{
			//find if the same rule for same service with different subscriber id is already applied
			for(DiscountEventActivity currDisc : existingDiscountActivities){
				// condition for same rule with different version comparison
				try
				{
					Subscriber sub  = SubscriberSupport.getSubscriber(context, currDisc.getSubId());
					if(currDisc.getBan().equals(discountEventActivity.getBan()) &&
							currDisc.getDiscountServiceType().equals(discountEventActivity.getDiscountServiceType()) &&
							currDisc.getState().equals(discountEventActivity.getState()) &&
							// discount type check
							currDisc.getDiscountType().equals(discountEventActivity.getDiscountType())&&
							currDisc.getDiscountRuleId().equals(discountEventActivity.getDiscountRuleId())&&
							currDisc.getPricePlanId() == discountEventActivity.getPricePlanId() &&
							sub.getPricePlan() != currDisc.getPricePlanId()
							){
						
						discountEventActivity.setDiscountEffectiveDate(CalendarSupportHelper.get(context).
                             getDateWithNoTimeOfDay(new Date()));
						Date expiryDate=CalendarSupportHelper.get(context).
			                    getDateWithNoTimeOfDay(CalendarSupportHelper.get(context).getDayBefore(new Date()));
						if(expiryDate.before(currDisc.getDiscountEffectiveDate()))
						{
							currDisc.setDiscountExpirationDate(currDisc.getDiscountEffectiveDate());
						}
						currDisc.setState(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);
						discountEventActivityForUpdate.add(currDisc);
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									 "Got the Discount event activity for ban" + currDisc.getBan()
									+"and eventId is : " + currDisc.getId() + "and subscriber is : " +currDisc.getSubId()
									+"and Expiry Date is current date-1 : " + currDisc.getDiscountExpirationDate()
									+"Discount is moving to subscriber : " + discountEventActivity.getSubId()
									+"effective date for the new entry is current date: " + discountEventActivity.getDiscountEffectiveDate());
					    }
						
				}
				}catch(Exception ex)
				{
					
						LogSupport.info(
								context,
								MODULE,"Exception occured" + ex.getMessage());
				
			}
		 }
		
		}
	}
	
	private void checkForFeePersonalizationEventForExistingDiscount(Context context,
			DiscountEventActivity discountEventActivity,
			Collection<DiscountEventActivity> existingDiscountActivities,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate)
		{
			if(null!=existingDiscountActivities && !existingDiscountActivities.isEmpty())
			{
				//find if the same rule for same service with different subscriber id is already applied
				for(DiscountEventActivity currDisc : existingDiscountActivities){
					// condition for same rule with different version comparison
					try
					{
						Subscriber sub  = SubscriberSupport.getSubscriber(context, currDisc.getSubId());
						if(currDisc.getServiceId()==discountEventActivity.getServiceId() 
								&& currDisc.getDiscountRuleId().equals(discountEventActivity.getDiscountRuleId())
								&& currDisc.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.SERVICE)
								&& currDisc.getSubId().equals(discountEventActivity.getSubId())
								&& currDisc.getDiscountExpirationDate()==null
								&& currDisc.getDiscountClass() == discountEventActivity.getDiscountClass()
								&& currDisc.getContractId() == discountEventActivity.getContractId()
								&& !sub.getState().equals(SubscriberStateEnum.SUSPENDED))
						{
							discountEventActivity.setDiscountEffectiveDate(CalendarSupportHelper.get(context).
                                    getDateWithNoTimeOfDay(new Date()));
							currDisc.setDiscountExpirationDate(currDisc.getDiscountEffectiveDate());
							currDisc.setState(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);
							discountEventActivityForUpdate.add(currDisc);
							if (LogSupport.isDebugEnabled(context)) {
								LogSupport.debug(
										context,
										MODULE,
										 "checkForFeePersonalizationEventForExistingDiscount : Got the Discount event activity for ban" + currDisc.getBan()
										+"and eventId is : " + currDisc.getId() + "and subscriber is : " +currDisc.getSubId()
										+"and Expiry Date is current date : " + currDisc.getDiscountExpirationDate()
										+" As Fee is Personalized  so Discount Event is getting created for the new fee change event : " 
										+"effective date for the new entry is current date: " + discountEventActivity.getDiscountEffectiveDate());
						    }
						}
					
					}catch(Exception ex)
					{
						LogSupport.info(
								context,
								MODULE,"Exception occured" + ex.getMessage());
					}
				}
			}
		}

}
