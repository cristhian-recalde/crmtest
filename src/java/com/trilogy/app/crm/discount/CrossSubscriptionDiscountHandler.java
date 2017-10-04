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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


//TODO Manish
//import com.trilogy.app.crm.accountcontract.support.AccountContractSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CancelReasonEnum;
import com.trilogy.app.crm.bean.DiscountEventActivity;
import com.trilogy.app.crm.bean.DiscountEventActivityServiceTypeEnum;
import com.trilogy.app.crm.bean.DiscountEventActivityStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventActivityTypeEnum;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanGroupForContract;
import com.trilogy.app.crm.bean.PricePlanGroupForContractHome;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.Transaction;
//TODO Manish
//import com.trilogy.app.crm.contract.AccountContract;
//import com.trilogy.app.crm.contract.AccountContractHistory;
//import com.trilogy.app.crm.contract.AccountContractXInfo;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.BusinessRule;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleEngineUtility;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleHome;
import com.trilogy.app.crm.core.ruleengine.CrossSubSecondDeviceDiscountOutputHolder;
import com.trilogy.app.crm.core.ruleengine.CrossSubscriptionDiscountCriteriaHolder;
import com.trilogy.app.crm.core.ruleengine.DiscountApplicationCriteriaEnum;
import com.trilogy.app.crm.core.ruleengine.DiscountCriteriaTypeEnum;
import com.trilogy.app.crm.core.ruleengine.DiscountPriority;
import com.trilogy.app.crm.core.ruleengine.DiscountPriorityTypeEnum;
import com.trilogy.app.crm.core.ruleengine.EventTypeEnum;
import com.trilogy.app.crm.core.ruleengine.engine.exception.NoRuleFoundException;
import com.trilogy.app.crm.core.ruleengine.engine.ifc.BusinessRuleIfc;
import com.trilogy.app.crm.core.ruleengine.util.RuleEngineConstants;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.DiscountSupportImpl;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Discount handler for the Cross Subscription discounts
 * @author abhijit.mokashi
 *
 */
public class CrossSubscriptionDiscountHandler implements DiscountHandler {
	//Taking Static Instance as we don't need to pass anything for now
	private Map<Integer,Object> prameterMap_ = new HashMap<Integer, Object>();
	public static String MODULE = CrossSubscriptionDiscountHandler.class.getName();
	@Override
	public boolean init(Context context,
			final Account account,final List<SubscriberIfc> subscriberList) {
		try{

			// checking for Cross Subscription (without contract) Discount
			List<AbstractBean> list = new ArrayList<AbstractBean>();
			CrossSubscriptionDiscountCriteriaHolder holder;
			long ppGroupId = -1;
			for (SubscriberIfc subscriber : subscriberList)
			{
				Map<ServiceFee2ID,SubscriberServices> container=SubscriberServicesSupport.getSubscribersServices(context,subscriber.getId());
				if(null!=container && !container.isEmpty())
				{
					Set<ServiceFee2ID> serviceIDCollection = container.keySet();
					Iterator<ServiceFee2ID> serviceIdIterator = serviceIDCollection.iterator();
					PricePlan plan = PricePlanSupport.getPlan(context, subscriber.getPricePlan());
					if (plan != null) {
						Home home = (Home) context.get(PricePlanGroupForContractHome.class);
						Collection<PricePlanGroupForContract> ppGroupCollection = home.selectAll();
						Iterator ppGroupIterator= ppGroupCollection.iterator();
						while(ppGroupIterator.hasNext())
						{
							PricePlanGroupForContract obj = (PricePlanGroupForContract) ppGroupIterator.next();
							if(obj.getPricePlanList().contains(plan.getId()))
							{
								ppGroupId = obj.getId();
							}
						}
					}

					while(serviceIdIterator.hasNext())
					{
						holder = new CrossSubscriptionDiscountCriteriaHolder();
						holder.setSubscriptionType(subscriber.getSubscriptionType());
						holder.setDunningLevelSubscription(subscriber.getLastDunningLevel());
						holder.setSubscriptionState(subscriber.getState());
						holder.setPricePlanGroupID(ppGroupId);
						holder.setPricePlanID(subscriber.getPricePlan());
						holder.setServiceID(serviceIdIterator.next().getServiceId());
						list.add(holder);
					}
				}
			}

			prameterMap_.put(RuleEngineConstants.DISCOUNT_TYPE, DiscountCriteriaTypeEnum.CROSS_SUBSCRIPTION);
			prameterMap_.put(RuleEngineConstants.SUBSCRIBER_EVENTS,list);
		} catch (Exception e){
			new MinorLogMsg(this, "Failed to initialize the cross subscription discount handler for account '");

			return false;
		}
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
		try{
			
			if(context.get(DiscountSupportImpl.DISCOUNT_GRADE) != null)
			{
				prameterMap_.put(RuleEngineConstants.DISCOUNT_GRADE,
						context.get(DiscountSupportImpl.DISCOUNT_GRADE));
			}
			
			boolean mutuallyExclusive = true;
			if(getDiscountEventActivityType().equals(DiscountEventActivityTypeEnum.BUNDLE_DISCOUNT))
			{
				mutuallyExclusive =checkForMutualExclusiveCrossSubscriptionDiscount(context,discountEventActivityForCreation,
						discountEventActivityContinued);
			}
			if(mutuallyExclusive)
			{
				// hit the discount rule engine to find applicable rule if any
				BusinessRuleIfc output=null;
				boolean outputApplied = false;
				output = BusinessRuleEngineUtility.evaluateRule(context,EventTypeEnum.DISCOUNT,
						account, subscriberList, prameterMap_);
			    if(checkRuleHasContractIdOrNot(output))
			    {
			      outputApplied=processOutput(context,
								account,
								subscriberList,
								existingDiscountActivities,
								trackDiscount, 
								discountEventActivityForCreation, 
								discountEventActivityForUpdate,
								discountEventActivityContinued,
								output);
			    }
			    	
			   
				if(!outputApplied)
				{
					List<BusinessRuleIfc> outputList = BusinessRuleEngineUtility.evaluateAllRule(context,EventTypeEnum.DISCOUNT,
							account, subscriberList, prameterMap_);
					if(outputList!=null && !outputList.isEmpty())
					{
						for(BusinessRuleIfc outputObject : outputList)
						{
							if(outputObject.getRulePriority() > output.getRulePriority() && !outputApplied)
							{
								if (LogSupport.isDebugEnabled(context)) {
									LogSupport.debug(
											context,
											MODULE,
											"evaluate : Previous matched rule could not discount any subscription, so Fetching next rule :"
											+"Old rule is : " + output.getRuleId()
											+"New Rule id is : " + outputObject.getRuleId());
								}
								output = outputObject;
								if(checkRuleHasContractIdOrNot(output))
								{
										
								  outputApplied=processOutput(context,
										account,
										subscriberList,
										existingDiscountActivities,
										trackDiscount, 
										discountEventActivityForCreation, 
										discountEventActivityForUpdate,
										discountEventActivityContinued,
										output);
								}
							}
						}
					}
	
				}
			  }else
			  {
				  new DebugLogMsg(this, "evaluate : Contract Discount Rule for Cross Subscription  is already matched for the BAN:'" + account.getBAN()).log(context);
			  }
			}catch(NoRuleFoundException ex){
				new DebugLogMsg(this, "evaluate : No Discount Rule for Cross Subscription matched for the BAN:'" + account.getBAN()).log(context);
				return false;
			}
		
	
		return true;
	}

	@Override
	public boolean generateTransactions(final Context context, final Account account,
			 final List<DiscountEventActivity> discountActivityEventList,
    		 List<DiscountEventActivity> discountEventToBeUpdated)
      {
		for(DiscountEventActivity discountEventActivity : discountActivityEventList){
			if(discountEventActivity.getDiscountType().equals(getDiscountEventActivityType())) 
			{
				if(DiscountActivityUtils.isApplicableForDiscountTransaction(context, discountEventActivity)){
					if (LogSupport.isDebugEnabled(context)) {
						LogSupport.debug(
								context,
								MODULE,
								"generateTransactions : Applying Discount for Account: " + account.getBAN()
								+ "  And discount event "
								+ discountEventActivity.getId());
					}
	
					try {
						Transaction discountTransaction = generateCrossSubDiscountTransaction(context, discountEventActivity);
						if(null!=discountTransaction)
						{
							/*discountEventActivity.setDiscountAppliedFromDate(DiscountActivityUtils.getDiscountAppliedPeriodStartDate(context,discountEventActivity,account));
							discountEventActivity.setDiscountAppliedTillDate(DiscountActivityUtils.getDiscountAppliedPeriodEndDate(context,discountEventActivity,account));
						*/	discountEventToBeUpdated.add(discountEventActivity);
							
						}
						
					} catch (Exception e) {
						LogSupport.minor(context, CrossSubscriptionDiscountHandler.class.getName(), "Error occured while generating discount transaction :" + e.getMessage());
						LogSupport.minor(
								context,
								MODULE,
								"generateTransactions : Exception Occured while creating transaction for " + account.getBAN()
								+ "  And discount event "
								+ discountEventActivity.getId() + " exception is : " + e.getMessage());
						//e.printStackTrace();
					
					}
	
				} else if (DiscountActivityUtils.isApplicableForReverseDiscountTransaction(context, discountEventActivity)){
					if (LogSupport.isDebugEnabled(context)) {
						LogSupport.debug(
								context,
								MODULE,
								"generateTransactions : Reversing Discount for Account: " + account.getBAN()
								+ "  And discount event "
								+ discountEventActivity.getId());
					}
	
					try {
						Transaction reverseDiscountTransaction = generateCrossSubReverseDiscountTransaction(context, discountEventActivity);
						if(null!=reverseDiscountTransaction)
						{
							discountEventActivity.setDiscountAppliedTillDate(discountEventActivity.getDiscountExpirationDate());
							discountEventActivity.setState(DiscountEventActivityStatusEnum.CANCELLED);
							discountEventToBeUpdated.add(discountEventActivity);
							
						}else if((null!=discountEventActivity.getDiscountAppliedTillDate() &&
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
								"generateTransactions : Exception Occured while creating transaction for " + account.getBAN()
								+ "  And discount event "
								+ discountEventActivity.getId() + " exception is : " + e.getMessage());
						//e.printStackTrace();
					}
					
				} else {
					LogSupport
					.debug(context,
							MODULE,
							"generateTransactions : Discount event activity entry not applicable for transaction generation :"
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
	private Transaction generateCrossSubDiscountTransaction(Context ctx,
			DiscountEventActivity discEventActivity)
					throws HomeException {
		
		
		Transaction discountTransaction =  DiscountSupportImpl.generateDiscountTransactions(ctx, discEventActivity);
		
		// update the contract discount count
		if (null != discountTransaction && discEventActivity.getDiscountClass() != 0) {
			//Subscriber sub = SubscriberSupport.getSubscriber(ctx, discEventActivity.getSubId());
			/*if (!sub.getState().equals(SubscriberStateEnum.SUSPENDED) && 
					DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT.equals(discEventActivity.getDiscountType())) {
				
			*/	if(DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT.equals(discEventActivity.getDiscountType()))
			    {
					DiscountSupportImpl.updateContractDiscountCount(ctx, discEventActivity);
			}
		}
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
	private Transaction generateCrossSubReverseDiscountTransaction(Context ctx,
			DiscountEventActivity discEventActivity)
					throws HomeException {
		Transaction discountTransaction =  DiscountSupportImpl.generateReverseDiscountTransactions(ctx, discEventActivity);
		
		// update the contract discount count
		/*if (null != discountTransaction && discEventActivity.getDiscountClass() != 0) {
			//Subscriber sub = SubscriberSupport.getSubscriber(ctx, discEventActivity.getSubId());
			if (!sub.getState().equals(SubscriberStateEnum.SUSPENDED) && 
					DiscountEventTypeEnum.CONTRACT_DISCOUNT.equals(discEventActivity.getDiscountType())) {
				if(DiscountEventTypeEnum.CONTRACT_DISCOUNT.equals(discEventActivity.getDiscountType()))
			    {
					DiscountSupportImpl.updateContractDiscountCount(ctx, discEventActivity);
			    }
		}*/
		return discountTransaction;
		
	}
	
	private DiscountEventActivity getDiscountEventActivityIfAvailable(final Context context,final Collection<DiscountEventActivity> discountEventsActivity,
			final CrossSubSecondDeviceDiscountOutputHolder object,final BusinessRuleIfc output,SubscriberIfc subscriber)
	{
		DiscountEventActivity discountEventActivityAvailable = null;
		Iterator<DiscountEventActivity> iterator = discountEventsActivity.iterator();
		while(iterator.hasNext())
		{
			DiscountEventActivity discEventActivity = iterator.next();
			if(discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.BUNDLE_DISCOUNT)
					|| discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT))
			{
				if(discEventActivity.getServiceId()==object.getServiceID() 
						&& discEventActivity.getDiscountRuleId().equals(output.getRuleId())
						&& discEventActivity.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.SERVICE)
						&& discEventActivity.getSubId().equals(subscriber.getId())
						&& discEventActivity.getDiscountExpirationDate()==null
						&& !subscriber.getState().equals(SubscriberStateEnum.SUSPENDED)
						&& discEventActivity.getDiscountClass() == object.getDiscountClass()
						&& discEventActivity.getContractId() == output.getRuleContractId())
				{
					if(DiscountActivityUtils.isFeePersonalizedAvailableForSubscriptionService(context,discEventActivity))
					{
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									"getDiscountEventActivityIfAvailable: Fee Personalization  : Found matched discount event for Fee Personalization CrossSubscription discount "
									+ discEventActivity.getId());
						}
						break;
					}else{
						discountEventActivityAvailable= discEventActivity;
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									"getDiscountEventActivityIfAvailable: Found matched discount event for CrossSubscription discount "
									+ discEventActivity.getId());
						}
						break;
					}
				}
			}
		}
		return discountEventActivityAvailable;
	}

	private DiscountEventActivity getDiscountEventActivityFromSub(final Collection<DiscountEventActivity> discountEventsActivity,final long serviceID,
			final String subId, final String ruleId)
	{
		DiscountEventActivity discountEventActivityAvailable = null;
		Iterator<DiscountEventActivity> iterator = discountEventsActivity.iterator();
		while(iterator.hasNext())
		{
			DiscountEventActivity discEventActivity = iterator.next();
			if(discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.BUNDLE_DISCOUNT)
					|| discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT))
			{
				if(discEventActivity.getServiceId()==serviceID 
						&& discEventActivity.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.SERVICE)
						&& discEventActivity.getSubId().equals(subId)
						&& discEventActivity.getState().equals(DiscountEventActivityStatusEnum.ACTIVE)
						&& discEventActivity.getDiscountRuleId().equals(ruleId)
						)
				{
					discountEventActivityAvailable= discEventActivity;
					break;
				}
			}
		}
		return discountEventActivityAvailable;	   
	}
	private DiscountEventActivity getDiscountEventActivityAvailableForPricePlanGroup(final Context context,final Collection<DiscountEventActivity> discountEventsActivity,
			final CrossSubSecondDeviceDiscountOutputHolder object,final BusinessRuleIfc output,SubscriberIfc subscriber,final long serviceId)
	{
		DiscountEventActivity discountEventActivityAvailable = null;
		Iterator<DiscountEventActivity> iterator = discountEventsActivity.iterator();
		while(iterator.hasNext())
		{
			DiscountEventActivity discEventActivity = iterator.next();
			if(discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.BUNDLE_DISCOUNT)
					|| discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT))
			{

				if(discEventActivity.getServiceId()==serviceId 
						&& discEventActivity.getDiscountRuleId().equals(output.getRuleId())
						&& discEventActivity.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.SERVICE)
						&& discEventActivity.getSubId().equals(subscriber.getId())
						&& discEventActivity.getPpGroupId() == object.getPricePlanGroupID()
						&& discEventActivity.getState().equals(DiscountEventActivityStatusEnum.ACTIVE)
						&& !subscriber.getState().equals(SubscriberStateEnum.SUSPENDED)
						&& discEventActivity.getDiscountClass() == object.getDiscountClass()
						&& discEventActivity.getContractId() == output.getRuleContractId())
				{
					if(DiscountActivityUtils.isFeePersonalizedAvailableForSubscriptionService(context,discEventActivity))
					{
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									"getDiscountEventActivityAvailableForPricePlanGroup : Fee Personalization  : Found matched discount event for Fee Personalization CrossSubscription discount "
									+ discEventActivity.getId());
						}
						break;
					}else
					{
						discountEventActivityAvailable= discEventActivity;
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									"getDiscountEventActivityAvailableForPricePlanGroup : Found matched discount event for CrossSubscription discount and Priceplan group :"
									+ discEventActivity.getId());
						}
						break;
					}
				}
			}
		}
		return discountEventActivityAvailable;
	}

	private boolean processOutput(final Context context, final Account account,
			final List<SubscriberIfc> subscriberList,
			final Collection<DiscountEventActivity> existingDiscountActivities,
			Map<String, Boolean> trackDiscount,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate,
			Collection<DiscountEventActivity> discountEventActivityContinued,
			final BusinessRuleIfc output)
	{
		boolean outputApplied = false;
		Date effectiveDate = null;
		long contractId=-1;
		if(null!=output)
		{
			
			if(this instanceof ContractDiscountHandler){
				contractId = account.getContract(context);
			}
			if (LogSupport.isDebugEnabled(context)) {
				LogSupport.debug(
						context,
						MODULE,
						"processOutput : Starting Processing of CrossSubscription Discount for Rule ID "
						+ output.getRuleId());
			}
			for(CrossSubSecondDeviceDiscountOutputHolder object : output.getCrossSubDiscountOutput())
			{
				if(object.getPricePlanGroupID()==-1)
				{
					if (LogSupport.isDebugEnabled(context)) {
						LogSupport.debug(
								context,
								MODULE,
								"processOutput : Matched Rule doesn't contain price plan group "
								+ output.getRuleId());
					}
					if(object.getDiscountApplicationCriteria().equals(DiscountApplicationCriteriaEnum.AGE_ON_NETWORK))
					{
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,"processOutput : Matched Rule has AGE_ON_NETWORK = true and MultipleDiscount set as " + output.getMultipleDiscount()+ "Ruleid:" + output.getRuleId());
						}
						SubscriberIfc sub=DiscountActivityUtils.getOldestSubscriber(context,object.getSubscriptionType(), object.getPricePlanID()
								, subscriberList, trackDiscount, output.getMultipleDiscount(),object.getServiceID());
						if(null!=sub)
						{
                         	if(null!=existingDiscountActivities && !existingDiscountActivities.isEmpty())
								{
									DiscountEventActivity discountEventActivity = getDiscountEventActivityIfAvailable(context,existingDiscountActivities
											,object,output,sub);
									if(null!=discountEventActivity)	{
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
										
										trackDiscount.put(sub.getId(),true);
										outputApplied=true;
										if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"processOutput : Discount Event Activity for CrossSubscription Age On Network exists for the current criteria");
										}
										continue;

									} else {
										effectiveDate = getEffectiveDateFromAllCriteria(context, account, subscriberList, output.getCrossSubDiscountInput()
												,sub,object.getServiceID());
										if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"processOutput : Creating New Discount Event Activity for CrossSubscription Age On Network and effective date for event activity is :"
													+ effectiveDate);
										}
										discountEventActivity = DiscountActivityUtils.createDiscounteventActivity(context, existingDiscountActivities, account, 
												      sub.getId(), object.getServiceID(), DiscountEventActivityServiceTypeEnum.SERVICE, object.getPricePlanID(),
												        -1, getDiscountEventActivityType(), output.getRuleId(),output.getRuleVersion(context),effectiveDate,
												        object.getDiscountClass(),-1,contractId);

							            outputApplied=true;
							            trackDiscount.put(sub.getId(),true);
							            
							            validateAndFilterDiscount(context, 
												existingDiscountActivities, 
												discountEventActivity, 
												discountEventActivityForCreation, 
												discountEventActivityForUpdate,
												discountEventActivityContinued,
												subscriberList);
							            discountEventActivityForCreation.add(discountEventActivity);
									}

								} else {
									effectiveDate = getEffectiveDateFromAllCriteria(context, account, subscriberList, output.getCrossSubDiscountInput()
											,sub,object.getServiceID());
									
									discountEventActivityForCreation.
			                          add(DiscountActivityUtils.createDiscounteventActivity(context, null, account, 
								      sub.getId(), object.getServiceID(), DiscountEventActivityServiceTypeEnum.SERVICE, object.getPricePlanID(),
								        -1, getDiscountEventActivityType(), output.getRuleId(), output.getRuleVersion(context),
								        effectiveDate,object.getDiscountClass(),-1,contractId));
									if (LogSupport.isDebugEnabled(context)) {
										LogSupport.debug(
												context,
												MODULE,
												"processOutput : Creating First Discount Event Activity for CrossSubscription Age On Network and effective date for event activity is :"
												+ effectiveDate );
									}

									outputApplied=true;
									trackDiscount.put(sub.getId(),true);
								}
						     }
									
						else
						{
							//Check if sub is getting other discount higher in priority and if yes then expire current discount rows
							checkForCrossOrContractDiscount(context, 
									existingDiscountActivities, 
									discountEventActivityForCreation, 
									discountEventActivityForUpdate,
									discountEventActivityContinued,
									account,
									null,
									object.getServiceID(),
									output.getRuleId()
									);
						}
					}
					else
					{

						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									"processOutput : Matched Rule has DISCOUNT_ALL_SUBSCRIPTIONS true and MultipleDiscount set as " + output.getMultipleDiscount()
									+ "Ruleid:" + output.getRuleId());
						}
						
						for(SubscriberIfc sub:subscriberList)
						{
							Map<ServiceFee2ID,SubscriberServices> serviceMap =SubscriberServicesSupport.getSubscribersServices(context, sub.getId());
							if(null==trackDiscount.get(sub.getId()) || output.getMultipleDiscount())
							{
								if(null!=existingDiscountActivities && !existingDiscountActivities.isEmpty())
								{
									DiscountEventActivity discountEventActivity = getDiscountEventActivityIfAvailable(context,existingDiscountActivities
											,object,output,sub);
									if(null!=discountEventActivity)
									{
										if(DiscountActivityUtils.checkIfRuleVersionChange(context, discountEventActivity, output))
										{
											discountEventActivity.setDiscountRuleVersion(output.getRuleVersion(context));
											discountEventActivityForUpdate.add(discountEventActivity);
										}else
										{
											discountEventActivityContinued.add(discountEventActivity);
										}
										trackDiscount.put(sub.getId(),true);
										outputApplied=true;
										if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"processOutput : Discount Event Activity for CrossSubscription All Subscription criteria exits for the subscriber :"
													+ sub.getId());
										}
										continue;
									}else //if(serviceMap.containsKey(object.getServiceID()) 
										    if(checkServiceId(context, serviceMap, object.getServiceID())
											&& sub.getSubscriptionType() == object.getSubscriptionType()
											&& sub.getPricePlan() == object.getPricePlanID()
											&& SubscriberServicesUtil.containsServiceId(sub.getServices(), object.getServiceID())
											&& !sub.getState().equals(SubscriberStateEnum.SUSPENDED))
									{


                                        effectiveDate = getEffectiveDateFromAllCriteria(context, account, subscriberList, output.getCrossSubDiscountInput()
												,sub,object.getServiceID());

										discountEventActivity =	DiscountActivityUtils.createDiscounteventActivity(context, existingDiscountActivities, account, 
												sub.getId(), object.getServiceID(), DiscountEventActivityServiceTypeEnum.SERVICE, object.getPricePlanID(),
												-1, getDiscountEventActivityType(), output.getRuleId(), output.getRuleVersion(context),
												effectiveDate,object.getDiscountClass(),-1,contractId);
										if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"processOutput : Creating new Discount Event Activity for CrossSubscription All Subscription criteria for subscriber :"
													+ sub.getId() + " And effective Date is : " +effectiveDate);
										}

										 outputApplied=true;
										 trackDiscount.put(sub.getId(),true);
										 validateAndFilterDiscount(context, 
													existingDiscountActivities, 
													discountEventActivity, 
													discountEventActivityForCreation, 
													discountEventActivityForUpdate,
													discountEventActivityContinued,
													subscriberList);
										 discountEventActivityForCreation.add(discountEventActivity);

									}

								}else
								{
									//Check of the serviceMap contains the serviceID
									if(checkServiceId(context, serviceMap, object.getServiceID())  
								    //if(serviceMap.containsKey(object.getServiceID()) 
											&& sub.getSubscriptionType() == object.getSubscriptionType()
											&& sub.getPricePlan() == object.getPricePlanID()
											&& SubscriberServicesUtil.containsServiceId(sub.getServices(), object.getServiceID())
											&& !sub.getState().equals(SubscriberStateEnum.SUSPENDED))
									{


                                        effectiveDate = getEffectiveDateFromAllCriteria(context, account, subscriberList, output.getCrossSubDiscountInput()
												,sub,object.getServiceID());
										

										DiscountEventActivity discountEventActivity = DiscountActivityUtils.createDiscounteventActivity(context, existingDiscountActivities, account, 
												sub.getId(), object.getServiceID(), DiscountEventActivityServiceTypeEnum.SERVICE, object.getPricePlanID(),
												-1, getDiscountEventActivityType(), output.getRuleId(), output.getRuleVersion(context),
												effectiveDate,object.getDiscountClass(),-1,contractId);

										if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"processOutput : Creating First Discount Event Activity for CrossSubscription All Subscription criteria for subscriber :"
													+ sub.getId() + " And effective Date is : " +effectiveDate);
										}

										 outputApplied=true;
										 trackDiscount.put(sub.getId(),true);
										 validateAndFilterDiscount(context, 
													existingDiscountActivities, 
													discountEventActivity, 
													discountEventActivityForCreation, 
													discountEventActivityForUpdate,
													discountEventActivityContinued,
													subscriberList);
										 discountEventActivityForCreation.add(discountEventActivity);

									}
								}
							}else
							{
								//Check if sub is getting other discount higher in priority and if yes then expire current discount rows
								checkForCrossOrContractDiscount(context, 
										existingDiscountActivities, 
										discountEventActivityForCreation, 
										discountEventActivityForUpdate,
										discountEventActivityContinued,
										account,
										sub.getId(),
										object.getServiceID(),
										output.getRuleId()
										);
							}
						}
					}
				}else
				{
					if (LogSupport.isDebugEnabled(context)) {
						LogSupport.debug(
								context,
								MODULE,
								"processOutput : Rule Contains the price group in the output : " + output.getRuleId());
					}

					outputApplied = processPricePlanGroupOutput(context,
							account,existingDiscountActivities,
							subscriberList,object,output,
							discountEventActivityForCreation, 
							discountEventActivityForUpdate,
							discountEventActivityContinued,trackDiscount,
							contractId);
				}		
			}
		}
		return outputApplied;
	}


	
	/*private boolean checkServiceId(Map<ServiceFee2ID,SubscriberServices> serviceMap, long serviceId){
		List<Long> list = new ArrayList<>();
		Set temp = serviceMap.keySet();
		Iterator<ServiceFee2ID> tempIt = temp.iterator();
		while(tempIt.hasNext()){
			ServiceFee2ID serFeeInst = tempIt.next();
			Long tempId = serFeeInst.getId();
			list.add(tempId);
		}
		Long serviceID = serviceId;
		boolean flag = list.contains(serviceID);
		if (LogSupport.isDebugEnabled(context)) {
			LogSupport.debug(
					context,
					MODULE,
					"checkServiceId :  list contains serviceIDs [ " + list + " ] serviceID to check [ " + serviceID + " ] result is [" + flag + "]");
		}
		
		return flag;
	}*/
	
	private boolean checkServiceId(Context context, Map<ServiceFee2ID,SubscriberServices> serviceMap, long serviceId){
		boolean flag = SubscriberServicesUtil.containsServiceId(serviceMap.keySet(), serviceId);
		if (LogSupport.isDebugEnabled(context)) {
			LogSupport.debug(
					context,
					MODULE,
					"checkServiceId :  serviceID to check [ " + serviceId + " ] result is [" + flag + "]");
		}
		
		return flag;
	}


	private boolean processPricePlanGroupOutput(final Context context
			   ,final Account account
			   ,final Collection<DiscountEventActivity> existingDiscountActivities
			   ,final List<SubscriberIfc> subscriberList
			   ,final CrossSubSecondDeviceDiscountOutputHolder object
			   ,final BusinessRuleIfc output
			   ,final Collection<DiscountEventActivity> discountEventActivityForCreation
			   ,final Collection<DiscountEventActivity> discountEventActivityForUpdate
			   ,final Collection<DiscountEventActivity> discountEventActivityContinued
			   ,final Map<String,Boolean> trackDiscount
			   ,final long contractId)
	   {
		   boolean outputApplied= false;
		   Date effectiveDate=null;
			List<SubscriberIfc> subPriorityList=DiscountActivityUtils.getSubscriptionByPriority(context, subscriberList,object.getSubscriptionType(),object.getPricePlanGroupID());
			if (LogSupport.isDebugEnabled(context)) {
				LogSupport.debug(
						context,
						MODULE,
						"processPricePlanGroupOutput : Fetched the Subscription by priority for the price plans which have "
						+ "the price plan group same as given in rule's output and size is :" +subPriorityList.size());
			}
	    	if(null!=subPriorityList && !subPriorityList.isEmpty())
	    	{
	    		for(SubscriberIfc subObj : subPriorityList)
	    		{
	    		  if((null==trackDiscount.get(subObj.getId()) || output.getMultipleDiscount()) && !subObj.getState().equals(SubscriberStateEnum.SUSPENDED))
	    		  {
		    	      Collection<SubscriberServices> collectionOfMandatoryService=SubscriberServicesSupport.getMandatorySubscriberServices(context, subObj.getId());
		    	      
		    	      if(null!=collectionOfMandatoryService && !collectionOfMandatoryService.isEmpty())
		    	      {
		    	    	  SubscriberServices serviceObj = new ArrayList<SubscriberServices>(collectionOfMandatoryService).get(0);
		    	    	  long serviceId = serviceObj.getServiceId();
		    	    	  if (LogSupport.isDebugEnabled(context)) {
		    					LogSupport.debug(
		    							context,
		    							MODULE,
		    							"processPricePlanGroupOutput : Fetched the Subscription's mandatory service :" +serviceId);
		    				}
		    	    	  if(null!=existingDiscountActivities && !existingDiscountActivities.isEmpty())

		    	    	  {
			    	    	  DiscountEventActivity discountEventActivity = getDiscountEventActivityAvailableForPricePlanGroup(context,existingDiscountActivities
										,object,output,subObj,serviceId);
			    	    	  if(null!=discountEventActivity)
			    	    	  {
			    	    		  if(DiscountActivityUtils.checkIfRuleVersionChange(context, discountEventActivity, output))
									{
										discountEventActivity.setDiscountRuleVersion(output.getRuleVersion(context));
										discountEventActivityForUpdate.add(discountEventActivity);
									}else
									{
										discountEventActivityContinued.add(discountEventActivity);
									}
								  trackDiscount.put(subObj.getId(),true);
								  outputApplied=true;
								  if(object.getDiscountApplicationCriteria().equals(DiscountApplicationCriteriaEnum.AGE_ON_NETWORK))
								  {
									  if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"processPricePlanGroupOutput : Discount Event Activity for CrossSubscription Age On Network criteria exists for the subscriber :"
													+ subObj.getId());
										}  
								    break;
								  }
								  if (LogSupport.isDebugEnabled(context)) {
										LogSupport.debug(
												context,
												MODULE,
												"processPricePlanGroupOutput : Discount Event Activity for CrossSubscription All Subscription criteria exists for the subscriber :"
												+ subObj.getId());
								 } 
			    	    	  }else
			    	    	  {
			    	    		  effectiveDate = getEffectiveDateFromAllCriteriaForPPGroup(context, account, subscriberList, output.getCrossSubDiscountInput()
											,subObj,object);
									
			    	    		  discountEventActivity  = DiscountActivityUtils.createDiscounteventActivity(context, existingDiscountActivities, account, 
		                        		  subObj.getId(), serviceId, DiscountEventActivityServiceTypeEnum.SERVICE, subObj.getPricePlan(),
							              object.getPricePlanGroupID(), getDiscountEventActivityType(), output.getRuleId(),output.getRuleVersion(context),
							              effectiveDate,object.getDiscountClass(),-1,contractId);
		                          outputApplied=true;
		                          trackDiscount.put(subObj.getId(),true);
		                          
		                          validateAndFilterDiscount(context, 
											existingDiscountActivities, 
											discountEventActivity, 
											discountEventActivityForCreation, 
											discountEventActivityForUpdate,
											discountEventActivityContinued,
											subscriberList);
		                          discountEventActivityForCreation.add(discountEventActivity);
		                          
		                          if(object.getDiscountApplicationCriteria().equals(DiscountApplicationCriteriaEnum.AGE_ON_NETWORK))
								  {
		                        	  if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"processPricePlanGroupOutput : Creating new Discount Event Activity for CrossSubscription Age On Network criteria for the subscriber :"
													+ subObj.getId()+" and effective date is : " +effectiveDate);
									 }  
								    break;
								  }
		                          if (LogSupport.isDebugEnabled(context)) {
										LogSupport.debug(
												context,
												MODULE,
												"processPricePlanGroupOutput : Creating new Discount Event Activity for CrossSubscription All Subscription criteria for the subscriber :"
												+ subObj.getId() + "and effective date is : " + effectiveDate);
								 } 
			    	    	  }
		    	    	  }else
		    	    	  {

                           effectiveDate = getEffectiveDateFromAllCriteriaForPPGroup(context, account, subscriberList, output.getCrossSubDiscountInput()
										,subObj,object);
		    	    		  discountEventActivityForCreation.
	                          add(DiscountActivityUtils.createDiscounteventActivity(context, existingDiscountActivities, account, 
	                        		  subObj.getId(), serviceId, DiscountEventActivityServiceTypeEnum.SERVICE, subObj.getPricePlan(),
						              object.getPricePlanGroupID(), getDiscountEventActivityType(), output.getRuleId(),output.getRuleVersion(context),
						              effectiveDate,object.getDiscountClass(),-1,contractId));
                              outputApplied=true;
	                          trackDiscount.put(subObj.getId(),true);
	                          if(object.getDiscountApplicationCriteria().equals(DiscountApplicationCriteriaEnum.AGE_ON_NETWORK))
							  {
	                        	  if (LogSupport.isDebugEnabled(context)) {
										LogSupport.debug(
												context,
												MODULE,
												"processPricePlanGroupOutput : Creating First Discount Event Activity for CrossSubscription Age On Network criteria for the subscriber :"
												+ subObj.getId()+" and effective date is : " +effectiveDate);
								 }   
							    break;
							  }
	                          if (LogSupport.isDebugEnabled(context)) {
									LogSupport.debug(
											context,
											MODULE,
											"processPricePlanGroupOutput : Creating First Discount Event Activity for CrossSubscription All Subscription criteria for the subscriber :"
											+ subObj.getId() + "and effective date is : " + effectiveDate);
							 } 
		    	    	  }

		    	      }
	    		  }else
	    		  {
	    			//Check if sub is getting other discount higher in priority and if yes then expire current discount rows
	    			  checkForCrossOrContractDiscount(context, 
								existingDiscountActivities, 
								discountEventActivityForCreation, 
								discountEventActivityForUpdate,
								discountEventActivityContinued,
								account,
								subObj.getId(),
								object.getServiceID(),
								output.getRuleId()
								);
	    		  }
	    	}
		
	    }
	    return outputApplied;	
	   }
	   
		private void validateAndFilterDiscount(Context context,
				Collection<DiscountEventActivity> existingDiscountActivities,
				DiscountEventActivity discountEventActivity,
				Collection<DiscountEventActivity> discountEventActivityForCreation,
				Collection<DiscountEventActivity> discountEventActivityForUpdate,
				Collection<DiscountEventActivity> discountEventActivityContinued,
				final List<SubscriberIfc> subscriberList) {

			/*// need to check if the new version of rule applied is present
			DiscountActivityUtils.checkRuleVersionChange(context,
					discountEventActivity,
					existingDiscountActivities,
					discountEventActivityForCreation,
					discountEventActivityForUpdate);
*/
			
			if( this instanceof ContractDiscountHandler){
				// need to check if the there is a contract hand over case
				checkForContractHandoverCase(context,
						discountEventActivity,
						existingDiscountActivities,
						discountEventActivityForCreation,
						discountEventActivityForUpdate);
			}
			
			checkForMutualNotApplicableCrossOrContractDiscount(context,
					discountEventActivity,
					existingDiscountActivities,
					discountEventActivityForCreation,
					discountEventActivityForUpdate);
			
			// need to check if any rule are now not applicable
		/*	DiscountActivityUtils.checkNotApplicableDiscounts(context,
					discountEventActivity,
					existingDiscountActivities,
					discountEventActivityForCreation,
					discountEventActivityForUpdate,
					discountEventActivityContinued);*/

			checkForOtherNotApplicableCrossOrContractDiscount(context,
					discountEventActivity,
					existingDiscountActivities,
					discountEventActivityForCreation,
					discountEventActivityForUpdate,
					subscriberList);
			
			checkForApplicableExpiredCrossOrContractDiscount(context,
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
										 "checkForPriceplanChangeForExistingDiscountedSub : Got the Discount event activity for ban" + currDisc.getBan()
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

		private void checkForMutualNotApplicableCrossOrContractDiscount(Context context,
				DiscountEventActivity discountEventActivity,
				Collection<DiscountEventActivity> existingDiscountActivities,
				Collection<DiscountEventActivity> discountEventActivityForCreation,
				Collection<DiscountEventActivity> discountEventActivityForUpdate) {
			DiscountEventActivityTypeEnum discountTypeToBeChecked = getDiscountEventActivityType().equals(DiscountEventActivityTypeEnum.BUNDLE_DISCOUNT)?
					DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT : DiscountEventActivityTypeEnum.BUNDLE_DISCOUNT;
			
			if(null!=existingDiscountActivities && !existingDiscountActivities.isEmpty())
			{
				//find if the same rule for same service with different subscriber id is already applied
				for(DiscountEventActivity currDisc : existingDiscountActivities){
					// condition for same rule with different version comparison
					if(currDisc.getBan().equals(discountEventActivity.getBan()) &&
							currDisc.getDiscountServiceType().equals(discountEventActivity.getDiscountServiceType()) &&
							currDisc.getState().equals(discountEventActivity.getState()) &&
							// discount type check
							currDisc.getDiscountType().equals(discountTypeToBeChecked) 
							){
						if(currDisc.getDiscountType().equals(DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT))
						{
							try
							{
							 Account account = AccountSupport.getAccount(context,currDisc.getBan());
							 long accContractId = account.getContract(context);
							 if(accContractId == -1)
							 {
								 //Todo Manish: This class is not present AccountContractSupport
								 //AccountContractHistory accountContractHistoryBean = AccountContractSupport.getAccountContractHistory(context,account.getBAN(), currDisc.getContractId(),CancelReasonEnum.CANCELLED);
								 /*if(null!=accountContractHistoryBean)
								 {
								  currDisc.setDiscountExpirationDate(CalendarSupportHelper.get(context).
                                         getDateWithNoTimeOfDay(accountContractHistoryBean.getContractEndDate()));
								 if (LogSupport.isDebugEnabled(context)) {
										LogSupport.debug(
												context,
												MODULE,
												"checkForMutualNotApplicableCrossOrContractDiscount : Setting contract end date as expiry date for the discount : " + currDisc.getId());
								    }
								 }else*/
								 {
									 currDisc.setDiscountExpirationDate(CalendarSupportHelper.get(context).
	                                         getDateWithNoTimeOfDay(calculateExpiryDate(context,discountEventActivity.getDiscountEffectiveDate())));
									 if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"checkForMutualNotApplicableCrossOrContractDiscount : Setting current date as expiry date for the discount : " + currDisc.getId());
									    }
								 }
							 }else
							 {
								 currDisc.setDiscountExpirationDate(CalendarSupportHelper.get(context).
                                         getDateWithNoTimeOfDay(calculateExpiryDate(context,discountEventActivity.getDiscountEffectiveDate())));
								 if (LogSupport.isDebugEnabled(context)) {
										LogSupport.debug(
												context,
												MODULE,
												"checkForMutualNotApplicableCrossOrContractDiscount : Setting current date as expiry date for the discount : " + currDisc.getId());
								    }
							 }
							 
							}catch(Exception e)
							{
								LogSupport.info(context,MODULE,"Error while getting account or account contract");
							}
							
						}else
						{
							currDisc.setDiscountExpirationDate(CalendarSupportHelper.get(context).
								                                          getDateWithNoTimeOfDay(calculateExpiryDate(context,discountEventActivity.getDiscountEffectiveDate())));
						}
						currDisc.setState(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);
						discountEventActivityForUpdate.add(currDisc);
						
					}
				}
			}
			
			
		}

		private Date calculateExpiryDate(final Context ctx,final Date discountEffectiverDate) {
			Date expiryDate = new Date();
			if(expiryDate.before(discountEffectiverDate))
			{
				return CalendarSupportHelper.get(ctx).getDayBefore(discountEffectiverDate);
			}else if(!expiryDate.after(discountEffectiverDate))
			{
				return CalendarSupportHelper.get(ctx).getDayBefore(expiryDate);
			}
			return expiryDate;
		}

		private void checkForContractHandoverCase(Context context,
				DiscountEventActivity discountEventActivity,
				Collection<DiscountEventActivity> existingDiscountActivities,
				Collection<DiscountEventActivity> discountEventActivityForCreation,
				Collection<DiscountEventActivity> discountEventActivityForUpdate) {
			// In case the subscriber having a contract is suspended the contract is handed over to 
			// the other eligible subscriber
			
			if (LogSupport.isDebugEnabled(context)) {
				LogSupport.debug(
						context,
						MODULE,
						"checkForContractHandoverCase : Checking for Contract Handover case for discountEventActivity :" + discountEventActivity);
		    } 
			//find if the same rule for same service with different subscriber id is already applied
			if(null!=existingDiscountActivities && !existingDiscountActivities.isEmpty())
			{
				for(DiscountEventActivity currDisc : existingDiscountActivities){
					// condition for same rule with different version comparison
					if(currDisc.getBan().equals(discountEventActivity.getBan()) &&
							currDisc.getDiscountType().equals(discountEventActivity.getDiscountType()) &&
							currDisc.getDiscountRuleId().equals(discountEventActivity.getDiscountRuleId()) &&
							currDisc.getContractId() == discountEventActivity.getContractId() &&
							currDisc.getServiceId() == discountEventActivity.getServiceId() &&
							currDisc.getDiscountServiceType().equals(discountEventActivity.getDiscountServiceType()) &&
							currDisc.getState().equals(discountEventActivity.getState()) &&
							// subscriber id should be different
							!currDisc.getSubId().equals(discountEventActivity.getSubId())
							){
						// In case of contract hand-over we need to expire old entry and create new entry
						currDisc.setDiscountExpirationDate(
								CalendarSupportHelper.get(context).
								getDateWithNoTimeOfDay(calculateExpiryDateForContractHandover(context,currDisc.getDiscountEffectiveDate())));
						currDisc.setState(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);
						discountEventActivityForUpdate.add(currDisc);
						//Setting Discount effective date is set as today's date
						discountEventActivity.setDiscountEffectiveDate(CalendarSupportHelper.get(context).
								getDateWithNoTimeOfDay(new Date()));
												
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									 "checkForContractHandoverCase : Got the old Contract Discount event activity for ban" + currDisc.getBan()
									+"and eventId is : " + currDisc.getId() + "and subscriber is : " +currDisc.getSubId()
									+"and Expiry Date is current date-1 : " + currDisc.getDiscountExpirationDate()
									+"Contract Discount is moving to subscriber : " + discountEventActivity.getSubId()
									+"effective date for the new entry is current date: " + discountEventActivity.getDiscountEffectiveDate());
					    }
						break;
					}
				}
			}
		}

		private Date calculateExpiryDateForContractHandover(final Context ctx,final Date effectiveDate) {
			    Date expiryDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(CalendarSupportHelper.get(ctx).getDayBefore(new Date()));
			    if(effectiveDate.getTime()>=expiryDate.getTime())
			    {
			    	expiryDate = effectiveDate;
			    }
				return expiryDate;
		
		}
		
		private DiscountEventActivityTypeEnum getDiscountEventActivityType(){
			if(this instanceof ContractDiscountHandler){
				return DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT;
			}
			return DiscountEventActivityTypeEnum.BUNDLE_DISCOUNT;
		}

		private void checkForOtherNotApplicableCrossOrContractDiscount(final Context context,
				DiscountEventActivity discountEventActivity,
				Collection<DiscountEventActivity> existingDiscountActivities,
				Collection<DiscountEventActivity> discountEventActivityForCreation,
				Collection<DiscountEventActivity> discountEventActivityForUpdate,
				final List<SubscriberIfc> subscriberList
				) {
			
			if(existingDiscountActivities!=null && !existingDiscountActivities.isEmpty())
			{
				//find if the same rule for same service with different subscriber id is already applied
				for(DiscountEventActivity currDisc : existingDiscountActivities){
					// condition for same rule with different version comparison
					if(currDisc.getBan().equals(discountEventActivity.getBan()) &&
							currDisc.getDiscountType().equals(discountEventActivity.getDiscountType()) &&
							!currDisc.getDiscountRuleId().equals(discountEventActivity.getDiscountRuleId()) &&
							currDisc.getDiscountServiceType().equals(discountEventActivity.getDiscountServiceType()) &&
							currDisc.getState().equals(discountEventActivity.getState())
							){
						
						currDisc.setDiscountExpirationDate(
								CalendarSupportHelper.get(context).
								getDateWithNoTimeOfDay(getExpiryDateFromContributors(context,currDisc.getDiscountRuleId()
										,currDisc.getServiceId()
										,currDisc.getSubId()
										,discountEventActivity.getDiscountEffectiveDate()
										,subscriberList)));
						currDisc.setState(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);
						
						if(currDisc.getDiscountExpirationDate().after(discountEventActivity.getDiscountEffectiveDate()))
						{
							discountEventActivity.setDiscountEffectiveDate(
									CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(
											CalendarSupportHelper.get(context).getDayAfter(currDisc.getDiscountExpirationDate())));
						}
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									 "checkForOtherNotApplicableCrossOrContractDiscount : Got old CrossSubscription Discount event activity for ban" + currDisc.getBan()
									+"and eventId is : " + currDisc.getId() + "and subscriber is : " +currDisc.getSubId()
									+"and Expiry Date is : " + currDisc.getDiscountExpirationDate()
									+"New CrossSubscription discount is getting applied to the subscriber : " + discountEventActivity.getSubId()
									+"effective date for the new entry is : " + discountEventActivity.getDiscountEffectiveDate()
									+"for the rule id is : " + discountEventActivity.getDiscountRuleId());
					    }
						discountEventActivityForUpdate.add(currDisc);
						
					}
				}
			}
			
			
		}
		
		private Date calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount(final Context ctx
				,final long serviceId
				,final String subId
				,final Date discountEffecttiveDate, String path) {
	
			Date expiryDate=null;
			try {
			    Subscriber sub = SubscriberSupport.getSubscriber(ctx, subId);
			    if((null !=ctx.get(DiscountClassContextAgent.SUSPENSIONTRIGGER) 
			    		&& ctx.getBoolean(DiscountClassContextAgent.SUSPENSIONTRIGGER))
			    		|| sub.getState().equals(SubscriberStateEnum.SUSPENDED))
			    {
			    	
			    	if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,"calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount:Expiry date calculation due to suspension of subscriber contrbuting to discount ");
										
				    }	
						Service service = ServiceSupport.getService(ctx, serviceId);
						
			            if(service.isRefundable())
			            {
			            	if(service.isSmartSuspension())
			            	{
			            		SubscriberServices subscriberService = SubscriberServicesSupport
										.getSubscriberServiceRecord(ctx,
												subId,
												serviceId, path);
			            		if(null!=subscriberService)
			            		{
				            	expiryDate=CalendarSupportHelper
										.get(ctx)
										.getDateWithNoTimeOfDay(
												CalendarSupportHelper
												.get(ctx)
												.findDateDaysBefore(
														1,
														subscriberService
														.getNextRecurringChargeDate()));
			            		}else
			            		{
			            			expiryDate=CalendarSupportHelper
											.get(ctx).getDateWithNoTimeOfDay(
													new Date());
			            		}
				            	if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,"calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount:Expiry date for service with smart suspension on and refundable is :  " +expiryDate);
													
							    }
			            	}else
			            	{
			            		expiryDate=CalendarSupportHelper
										.get(ctx).getDateWithNoTimeOfDay(
												new Date());
			            		if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,"calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount:Expiry date for service with smart suspension on and non-refundable is :  " +expiryDate);
													
							    }
			            	}
			            	
			            	
			            }else{
			            	SubscriberServices subscriberService = SubscriberServicesSupport
									.getSubscriberServiceRecord(ctx,
											subId,
											serviceId, path);
			            	expiryDate=CalendarSupportHelper
									.get(ctx)
									.getDateWithNoTimeOfDay(
											CalendarSupportHelper
											.get(ctx)
											.findDateDaysBefore(
													1,
													subscriberService
													.getNextRecurringChargeDate()));
			            	if (LogSupport.isDebugEnabled(ctx)) {
								LogSupport.debug(
										ctx,
										MODULE,"calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount:Expiry date for service with smart suspension off is :  " +expiryDate);
												
						    }
			            }
						
					
			    }else if(sub.getState().equals(SubscriberStateEnum.INACTIVE))
			    {
			    	expiryDate = CalendarSupportHelper
							.get(ctx).getDateWithNoTimeOfDay(
									new Date());
			    	if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,"calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount:Expiry date is :  " +expiryDate);
										
				    }
			    	
			    }else
			    {
			    	expiryDate = discountEffecttiveDate;
			    	if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,"calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount:Expiry date is :  " +expiryDate);
										
				    }
			    }
			} catch (HomeException e) {
			     LogSupport.info(ctx, this,"calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount : Error while Calculating Expiry Date");
			}
			
				return expiryDate;
		
		}
		
		private void checkForCrossOrContractDiscount(final Context context, 
				 Collection<DiscountEventActivity> existingDiscountActivities, 
				 Collection<DiscountEventActivity> discountEventActivityForCreation, 
				 Collection<DiscountEventActivity> discountEventActivityForUpdate,
				 Collection<DiscountEventActivity> discountEventActivityContinued,
				 final Account ban,
				 final String subId,
				 final long serviceId,
				 final String ruleId)
		{
		
			List<DiscountPriority> priorityList=DiscountClassContextAgent.discountPriorityList;
			if(priorityList == null 
					|| priorityList.isEmpty())
			{
				priorityList = DiscountSupportImpl.getDiscountPriorityList(context, ban);
			}
			List<DiscountEventActivityTypeEnum> higherPriorityToBeChecked = new ArrayList<DiscountEventActivityTypeEnum>();
			if(this instanceof ContractDiscountHandler){
			      for(DiscountPriority obj:priorityList)
			      {
			    	  if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.PAIRED_DISCOUNT))
			    		{
			    			higherPriorityToBeChecked.add(DiscountEventActivityTypeEnum.PAIRED_DISCOUNT);
			    		}else if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.SECONDARY_DEVICE_DISCOUNT))
			    		{
			    			higherPriorityToBeChecked.add(DiscountEventActivityTypeEnum.SECOND_DEVICE_DISCOUNT);
			    		}else if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.COMBINATION_DISCOUNT))
			    		{
			    			higherPriorityToBeChecked.add(DiscountEventActivityTypeEnum.COMBINATION_DISCOUNT);
			    		}else if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.CONTRACT_DISCOUNT))
			    		{
			    			break;
			    		}
			      }
			}else
			{
				  for(DiscountPriority obj:priorityList)
			      {
					  if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.PAIRED_DISCOUNT))
			    		{
			    			higherPriorityToBeChecked.add(DiscountEventActivityTypeEnum.PAIRED_DISCOUNT);
			    		}else if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.SECONDARY_DEVICE_DISCOUNT))
			    		{
			    			higherPriorityToBeChecked.add(DiscountEventActivityTypeEnum.SECOND_DEVICE_DISCOUNT);
			    		}else if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.COMBINATION_DISCOUNT))
			    		{
			    			higherPriorityToBeChecked.add(DiscountEventActivityTypeEnum.COMBINATION_DISCOUNT);
			    		}else if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.CROSS_SUBSCRIPTION_DISCOUNT))
			    		{
			    			break;
			    		}
			      }
			}
			
	
			for(DiscountEventActivity currDisc : discountEventActivityForCreation){
				
				if(currDisc.getBan().equals(ban) &&
						higherPriorityToBeChecked.contains(currDisc.getDiscountType()) &&
						currDisc.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.SERVICE) &&
						currDisc.getState().equals(DiscountEventActivityStatusEnum.ACTIVE) &&
						currDisc.getServiceId() == serviceId)
						{
					      DiscountEventActivity discEventActivity = getDiscountEventActivityFromSub(existingDiscountActivities,
					    		  serviceId,
					    		  currDisc.getSubId(),
					    		  ruleId);
					      if(null!=discEventActivity)
					      {
					    	  
					    	  discEventActivity.setDiscountExpirationDate(CalendarSupportHelper.get(context).
					    			  getDateWithNoTimeOfDay(calculateExpiryDate(context,currDisc.getDiscountEffectiveDate())));
					    	  discEventActivity.setState(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);
					    	  discountEventActivityForUpdate.add(discEventActivity);
					    	  if (LogSupport.isDebugEnabled(context)) {
									LogSupport.debug(
											context,
											MODULE,
											 "checkForCrossOrContractDiscount : Deactivating old CrossSubscription Discount event activity for ban" + discEventActivity.getBan()
											+"and eventId is : " + discEventActivity.getId() + "and subscriber is : " +discEventActivity.getSubId()
											+"and Expiry Date is : " + discEventActivity.getDiscountExpirationDate()
											+" due to application of highest priority discount for subscriber : " + currDisc.getSubId()
											+"effective date for the new entry is : " + currDisc.getDiscountEffectiveDate()
											+"for the rule id is : " + currDisc.getDiscountRuleId()
											+"discount Type is : " +currDisc.getDiscountType());
							    }
					    	  
					      }
					      		  
				}
			}
			
		}
		
		private void checkForApplicableExpiredCrossOrContractDiscount(Context context,
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
										 "checkForApplicableExpiredCrossOrContractDiscount : Got old CrossSubscription Discount event activity for ban" + currDisc.getBan()
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
		

	private Date getEffectiveDateFromContributors(final Context ctx
			   ,final Account account
			   ,final List<SubscriberIfc> subscriberList
			   ,final List<CrossSubscriptionDiscountCriteriaHolder> inputObject)
	{
		   Map<String,Long> subServiceMap = new HashMap<String, Long>();
		   Iterator<CrossSubscriptionDiscountCriteriaHolder> iterator = inputObject.iterator();
		   Date effectiveDate = null;
		   while(iterator.hasNext())
		   {
			  CrossSubscriptionDiscountCriteriaHolder input = iterator.next();
			  SubscriberIfc sub = DiscountActivityUtils.getOldestSubscriberForInput(ctx,input.getSubscriptionType(),
					  input.getPricePlanID(),subscriberList,input.getServiceID());  
			  subServiceMap.put(sub.getId(),input.getServiceID());
			  if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,"getEffectiveDateFromContributors : Filling the map for calculating the effective date"
									+ "Service Id :" + input.getServiceID()
									+ "Subscriber Id : " + sub.getId());
			    }
		   }
		   effectiveDate = DiscountActivityUtils.getLatestEffectiveDate(subServiceMap,ctx,account);
		   return effectiveDate;
	}

	private Date getEffectiveDateFromContributorsForPPGroup(final Context ctx
			   ,final Account account
			   ,final List<SubscriberIfc> subscriberList
			   ,final List<CrossSubscriptionDiscountCriteriaHolder> inputObject)
	{
		   Map<String,Long> subServiceMap = new HashMap<String, Long>();
		   Iterator<CrossSubscriptionDiscountCriteriaHolder> iterator = inputObject.iterator();
		   Date effectiveDate = null;
		   while(iterator.hasNext())
		   {
			  CrossSubscriptionDiscountCriteriaHolder input = iterator.next();
			  SubscriberIfc sub = DiscountActivityUtils.getSubscriptionByPriority(ctx,subscriberList,input.getSubscriptionType(),
					  input.getPricePlanGroupID()).get(0); 
			  if(null!=sub)
			  {
				  Collection<SubscriberServices> collectionOfMandatoryService=SubscriberServicesSupport.getMandatorySubscriberServices(ctx, sub.getId());
			      if(null!=collectionOfMandatoryService && !collectionOfMandatoryService.isEmpty())
			      {
			    	  SubscriberServices serviceObj = new ArrayList<SubscriberServices>(collectionOfMandatoryService).get(0);
			    	  long serviceId = serviceObj.getServiceId();
			    	  subServiceMap.put(sub.getId(),serviceId);
			    	  if (LogSupport.isDebugEnabled(ctx)) {
							LogSupport.debug(
									ctx,
									MODULE,"getEffectiveDateFromContributorsForPPGroup : Filling the map for calculating the effective date for PricePlan group rule"
											+ "Service Id :" + input.getServiceID()
											+ "Subscriber Id : " + sub.getId());
					    }
			      }
			  }
			  
		   }
		   effectiveDate = DiscountActivityUtils.getLatestEffectiveDate(subServiceMap,ctx,account);
		   return effectiveDate;
	}

    private Date getEffectiveDateFromAllCriteria(final Context ctx
		   ,final Account account
		   ,final List<SubscriberIfc> subscriberList
		   ,final List<CrossSubscriptionDiscountCriteriaHolder> inputObject
		   ,final SubscriberIfc sub
		   ,final long serviceId)
		   {
	        Date effectiveDate = null;
	        Date effectiverDateFromInput = getEffectiveDateFromContributors(ctx,account,subscriberList,inputObject);
	        if(null!=effectiverDateFromInput)
	        {
	        	if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,"getEffectiveDateFromAllCriteria : Effective date from input of rule is"
									+ effectiverDateFromInput);
			    }
	        	Map<String,Long> subServiceMap = new HashMap<String, Long>();
	        	subServiceMap.put(sub.getId(),serviceId);
	        	if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,"getEffectiveDateFromAllCriteria : Filling the map for calculating the effective date with output of rule"
									+ "Service Id :" + serviceId
									+ "Subscriber Id : " + sub.getId());
			    }
	        	effectiveDate = DiscountActivityUtils.getLatestEffectiveDate(subServiceMap,ctx,account);
	        	if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,"getEffectiveDateFromAllCriteria : Effective date from output of rule is"
									+ effectiveDate);
			    }
	        	if(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiveDate).
	        			before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiverDateFromInput)))
	        	{
	        		effectiveDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiverDateFromInput);
	        	}
	        }
	        return effectiveDate;
		   }

    private Date getEffectiveDateFromAllCriteriaForPPGroup(final Context ctx
		   ,final Account account
		   ,final List<SubscriberIfc> subscriberList
		   ,final List<CrossSubscriptionDiscountCriteriaHolder> inputObject
		   ,final SubscriberIfc sub
	  	   ,final CrossSubSecondDeviceDiscountOutputHolder object
		   )
		   {
		        Date effectiveDate = null;
		        Date effectiverDateFromInput = getEffectiveDateFromContributorsForPPGroup(ctx,account,subscriberList,inputObject);
		        if(null!=effectiverDateFromInput)
		        {
		        	if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,"getEffectiveDateFromAllCriteriaForPPGroup : Effective date from input of price plan group rule is"
										+ effectiverDateFromInput);
				    }
		        	Map<String,Long> subServiceMap = new HashMap<String, Long>();
		        	
		  		  if(null!=sub)
		  		  {
		  			  Collection<SubscriberServices> collectionOfMandatoryService=SubscriberServicesSupport.getMandatorySubscriberServices(ctx, sub.getId());
		  		      if(null!=collectionOfMandatoryService && !collectionOfMandatoryService.isEmpty())
		  		      {
		  		    	  SubscriberServices serviceObj = new ArrayList<SubscriberServices>(collectionOfMandatoryService).get(0);
		  		    	  long serviceId = serviceObj.getServiceId();
		  		    	if (LogSupport.isDebugEnabled(ctx)) {
							LogSupport.debug(
									ctx,
									MODULE,"getEffectiveDateFromAllCriteriaForPPGroup : Filling the map for calculating the effective date with output of price plan group rule"
											+ "Service Id :" + serviceId
											+ "Subscriber Id : " + sub.getId());
					    }
		  		    	  subServiceMap.put(sub.getId(),serviceId);
		  		      }
		  		  }
		         effectiveDate = DiscountActivityUtils.getLatestEffectiveDate(subServiceMap,ctx,account);
		         if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,"getEffectiveDateFromAllCriteriaForPPGroup : Effective date from output of price plan group rule is"
										+ effectiveDate);
				    }
		         if(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiveDate).
		        			before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiverDateFromInput)))
		        	{
		        		effectiveDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiverDateFromInput);
		        	}
		        }
		        return effectiveDate;
		   }
  
	  private Date getExpiryDateFromContributors(final Context ctx,final String ruleId
	 		 ,final long serviceId
	 		 ,final String subId
	 		 ,final Date effectiveDate
	 		 ,final List<SubscriberIfc> subscriberList)
	  {
	 	 Home home = (Home) ctx.get(BusinessRuleHome.class);
	 	 List<Date> effectiveDateList = new ArrayList<Date>();
	 	 try {
				BusinessRule rule = (BusinessRule)home.find(ctx,ruleId);
				if(null!=rule && rule.getDiscountType().equals(DiscountCriteriaTypeEnum.CROSS_SUBSCRIPTION))
				{
					List<CrossSubscriptionDiscountCriteriaHolder> inputList = rule.getCrossSubscriptionDiscountingCriteria();
					if(null!=inputList && !inputList.isEmpty())
					{
						Iterator<CrossSubscriptionDiscountCriteriaHolder> iterator = inputList.iterator();
						 
						   while(iterator.hasNext())
						   {
							  CrossSubscriptionDiscountCriteriaHolder input = iterator.next();
							  SubscriberIfc sub = DiscountActivityUtils.getSubscriptionByPriority(ctx,subscriberList,input.getSubscriptionType(),
									  input.getPricePlanGroupID()).get(0); 
							  if(input.getPricePlanGroupID()!=-1)
							  {
								  Collection<SubscriberServices> collectionOfMandatoryService=SubscriberServicesSupport.getMandatorySubscriberServices(ctx, sub.getId());
					    	      if(null!=collectionOfMandatoryService && !collectionOfMandatoryService.isEmpty())
					    	      {
					    	    	  if (LogSupport.isDebugEnabled(ctx)) {
											LogSupport.debug(
													ctx,
													MODULE,"getExpiryDateFromContributors : Expiry date calculation from input of price plan group rule");
															
									    }
					    	    	  SubscriberServices serviceObj = new ArrayList<SubscriberServices>(collectionOfMandatoryService).get(0);
					    	    	  effectiveDateList.add(calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount(ctx,
					    	    			  serviceObj.getServiceId(),
											 	sub.getId(),
												 effectiveDate, serviceObj.getPath()));
					    	      }
								  
							  }else
							  {
								  if (LogSupport.isDebugEnabled(ctx)) {
										LogSupport.debug(
												ctx,
												MODULE,"getExpiryDateFromContributors : Expiry date calculation from input of rule");
								    }
								  effectiveDateList.add(calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount(ctx,
										 	input.getServiceID(),
										 	sub.getId(),
											 effectiveDate, SubscriberServicesUtil.DEFAULT_PATH));
							  }
							  
						   }
					}
					 if (LogSupport.isDebugEnabled(ctx)) {
							LogSupport.debug(
									ctx,
									MODULE,"getExpiryDateFromContributors : Expiry date calculation from output of rule ");
											
					    }
					 effectiveDateList.add(calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount(ctx,
							     serviceId,
								 subId,
								 effectiveDate,null));
					 Collections.sort(effectiveDateList);
					 return effectiveDateList.get(effectiveDateList.size()-1);
				}
				return CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
			} catch (HomeInternalException e) {
				LogSupport.minor(ctx, MODULE,"getExpiryDateFromContributors : Expiry date is : " + CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
				return CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
				
			} catch (HomeException e) {
				LogSupport.minor(ctx, MODULE,"getExpiryDateFromContributors : Expiry date is : " + CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
				return CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
			}	    
	  }
  
     private boolean checkRuleHasContractIdOrNot(final BusinessRuleIfc output)
     {
    	 if(getDiscountEventActivityType().equals(DiscountEventActivityTypeEnum.BUNDLE_DISCOUNT))
    	 {
    		 if(output.getRuleContractId() == -1)
    		 {
    			 return true;
    		 }else
    		 {
    			 return false;
    		 }
    	 }else if(getDiscountEventActivityType().equals(DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT))
    	 {
    		 if(output.getRuleContractId() != -1)
    		 {
    			 return true;
    		 }else
    		 {
    			 return false;
    		 }
    	 }
    	 return true;
     }
  
     private boolean checkForMutualExclusiveCrossSubscriptionDiscount(Context context,
				Collection<DiscountEventActivity> discountEventActivityForCreation,
				Collection<DiscountEventActivity> discountEventActivityContinued) {
			if(null!=discountEventActivityContinued && !discountEventActivityContinued.isEmpty())
			{
				
				for(DiscountEventActivity currDisc : discountEventActivityContinued){
					if(currDisc.getDiscountType().equals(DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT))
					{
						return false;
					}
				}
			}
			if(null!=discountEventActivityForCreation && !discountEventActivityForCreation.isEmpty())
			{
				for(DiscountEventActivity currDisc : discountEventActivityForCreation){
					if(currDisc.getDiscountType().equals(DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT))
					{
						return false;
					}
				}
			}
			
			return true;
		}
}