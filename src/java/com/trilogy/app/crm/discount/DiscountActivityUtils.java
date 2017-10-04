package com.trilogy.app.crm.discount;

/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 * 
 * @Author: sapan.modi
 * Since: 10.3.2
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
//TODO Manish
//import com.trilogy.app.crm.accountcontract.support.AccountContractSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CancelReasonEnum;
import com.trilogy.app.crm.bean.DiscountActivityTrigger;
import com.trilogy.app.crm.bean.DiscountActivityTriggerHome;
import com.trilogy.app.crm.bean.DiscountActivityTriggerXInfo;
import com.trilogy.app.crm.bean.DiscountActivityTypeEnum;
import com.trilogy.app.crm.bean.DiscountEvaluationStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventActivity;
import com.trilogy.app.crm.bean.DiscountEventActivityHome;
import com.trilogy.app.crm.bean.DiscountEventActivityServiceTypeEnum;
import com.trilogy.app.crm.bean.DiscountEventActivityStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventActivityTypeEnum;
import com.trilogy.app.crm.bean.DiscountEventActivityXInfo;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanGroupForContract;
import com.trilogy.app.crm.bean.PricePlanGroupForContractHome;
import com.trilogy.app.crm.bean.PricePlanGroupForContractXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
//TODO Manish
//import com.trilogy.app.crm.contract.AccountContract;
//import com.trilogy.app.crm.contract.AccountContractHistory;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.BusinessRule;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleHome;
import com.trilogy.app.crm.core.ruleengine.CombinationDiscountCriteriaHolder;
import com.trilogy.app.crm.core.ruleengine.CrossSubscriptionDiscountCriteriaHolder;
import com.trilogy.app.crm.core.ruleengine.DiscountCriteriaTypeEnum;
import com.trilogy.app.crm.core.ruleengine.FirstDeviceDiscountHolder;
import com.trilogy.app.crm.core.ruleengine.MasterPackDiscountCriteriaHolder;
import com.trilogy.app.crm.core.ruleengine.PairedDiscountCriteriaHolder;
import com.trilogy.app.crm.core.ruleengine.SecondaryDeviceDiscountHolder;
import com.trilogy.app.crm.core.ruleengine.engine.ifc.BusinessRuleIfc;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.DiscountEventActivityComparator;
import com.trilogy.app.crm.support.DiscountSupportImpl;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.account.AccountRelationship;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.account.AccountRelationshipHome;
import com.trilogy.app.crm.bean.DiscountStrategyEnum;
import com.trilogy.app.crm.bean.account.AccountRelationshipID;


public class DiscountActivityUtils {

	public static String MODULE = DiscountActivityUtils.class.getName();

	public static boolean createTrigger(DiscountActivityTypeEnum activityType,
			Context ctx, int spid, String ban) throws HomeException,
		HomeInternalException {
		Home home = (Home) ctx.get(DiscountActivityTriggerHome.class);
		//Home acctHome = (Home) ctx.get(AccountHome.class);
		Home acctRelHome = (Home) ctx.get(AccountRelationshipHome.class);
		
		String targetBanId = null;
		
		
		try{
			// Fetching the target ban from the AccountRelationship table using
			// the SPID and current BANID
			// Further setting the responsible BAN in the Account trigger table
			AccountRelationshipID acctRelbeanId = new AccountRelationshipID(spid, ban);
			AccountRelationship accRelBean = (AccountRelationship) acctRelHome.find(ctx, acctRelbeanId);
			if(accRelBean!=null){
			targetBanId = accRelBean.getTargetBAN();
			LogSupport.info(ctx, MODULE, "createTrigger : DiscountActivityTrigger for trigger: "+ activityType.getName() + " Fetched the target ban [ " + targetBanId + " ]");
			}else{
				targetBanId = ban;	
			LogSupport.info(ctx, MODULE, "createTrigger : DiscountActivityTrigger for trigger: "+ activityType.getName() + "No entry found in the AccountRelation table for the ban [ " + ban + " ] setting the target to itself");
			}
		}catch (HomeInternalException e) {
			LogSupport.info(ctx, MODULE,
					"createTrigger : Unable to add trigger to DiscountActivityTrigger for trigger: "
							+ activityType.getName()+ "While fetching the responsible ban from the accountrelationship so throwing it");
			throw e;
		} catch (HomeException e) {
			LogSupport.info(ctx, MODULE,
					"createTrigger : Unable to add trigger to DiscountActivityTrigger for trigger: "
							+ activityType.getName() + "While fetching the responsible ban from the accountrelationship so throwing it");
			throw e;
		}
		
	    DiscountActivityTrigger discountActivity = new DiscountActivityTrigger();
		discountActivity.setSpid(spid);
		discountActivity.setBan(ban);
		discountActivity.setTargetBan(targetBanId);
		
		discountActivity.setDiscountActivityType(activityType);
		discountActivity
				.setDiscountEvaluationStatus(DiscountEvaluationStatusEnum.PENDING);
		discountActivity.setActivityTimeStamp(new Date().getTime());
        discountActivity.setSubscriptionId("");
        discountActivity.setServiceId(-1);
		if (home != null) {
			try {
				home.create(discountActivity);
				LogSupport.info(ctx, MODULE, "createTrigger : DiscountActivityTrigger for trigger: "+ activityType.getName() + " Saved the entity");
				return true;
			} catch (HomeInternalException e) {
				LogSupport.info(ctx, MODULE,
						"createTrigger : Unable to add trigger to DiscountActivityTrigger for trigger: "
								+ activityType.getName() + "so throwing it");
				throw e;
			} catch (HomeException e) {
				LogSupport.info(ctx, MODULE,
						"createTrigger : Unable to add trigger to DiscountActivityTrigger for trigger: "
								+ activityType.getName() + "so throwing it");
				throw e;
			}

		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE,
					"createTrigger : Unable to find home for DiscountActivityTrigger");

		}
		return false;
	}

	/**
	 * This Method will find the oldest subscriber in given subscriber list for
	 * given subscription type
	 * 
	 * @param subScriptionType
	 * @param subList
	 * @param pricePlanId
	 * @param trackDiscount
	 * @param getMultipleDiscount
	 * @param serviceId
	 * @return
	 */
	public static SubscriberIfc getOldestSubscriber(final Context ctx,
			final long subScriptionType, final long pricePlanId,
			List<SubscriberIfc> subScriberList,
			Map<String, Boolean> trackDiscount,
			final boolean getMultipleDiscount, final long serviceId) {

		Iterator<SubscriberIfc> itr = subScriberList.iterator();
		List<SubscriberIfc> subList = new ArrayList<SubscriberIfc>();
		while (itr.hasNext()) {
			SubscriberIfc sub = itr.next();
			if (sub.getSubscriptionType() == subScriptionType
					&& sub.getPricePlan() == pricePlanId
					&& (null == trackDiscount.get(sub.getId()) || (getMultipleDiscount))
					&& !sub.getState().equals(SubscriberStateEnum.SUSPENDED)) {
				SubscriberServices subscriberService = SubscriberServicesSupport
						.getSubscriberServiceRecord(ctx, sub.getId(), serviceId, SubscriberServicesUtil.DEFAULT_PATH);
				if (null != subscriberService) {
					subList.add(sub);
				}
			}
		}

		if (subList.isEmpty()) {
			return null;
		}

		if (subList.size() == 1) {
			return subList.get(0);
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(
					ctx,
					MODULE,
					"getOldestSubscriber : Subscriber List with out suspended subscription for the given input i.e. PricePlan : " + pricePlanId
					+" service Id : " + serviceId + " is : " + subList);
		}
		Collections.sort(subList, new Comparator<SubscriberIfc>() {

			@Override
			public int compare(SubscriberIfc o1, SubscriberIfc o2) {
				Subscriber subFirst = (Subscriber) o1;
				Subscriber subSecond = (Subscriber) o2;

				/**
				 * prioritize based on subscriber activation date
				 */
				/*if (subFirst.getStartDate() != null
						&& subSecond.getStartDate() != null
						&& subFirst.getStartDate().before(
								subSecond.getStartDate())) {
					return -1;
				} else if (subFirst.getStartDate() != null
						&& subSecond.getStartDate() != null
						&& subFirst.getStartDate().after(
								subSecond.getStartDate())) {
					return 1;
				}*/
				/**
				 * Priority based on subscriber creation date
				 */
				if (subFirst.getDateCreated().before(
						subSecond.getDateCreated())) {
					return -1;
				} else {
					return 1;
				}

			}
		});

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(
					ctx,
					MODULE,
					"getOldestSubscriber : Oldest Subscription for the given input i.e. PricePlan : " + pricePlanId
					+" service Id : " + serviceId + " is : " + subList.get(0));
		}
		return subList.get(0);
	}

	public static DiscountEventActivity createDiscountEventActivityForSecondDeviceDiscount(
			Context ctx,
			Collection<DiscountEventActivity> discountEventActivityList,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate,
			Collection<DiscountEventActivity> discountEventActivityContinued,
			BusinessRuleIfc output, SubscriberIfc subscriber, Account account) {
		
			SubscriberServices subService = SubscriberServicesSupport
					.getSubscriberServiceRecord(ctx, subscriber.getId(),output.getSecondDeviceOutput().get(0).getServiceID(), SubscriberServicesUtil.DEFAULT_PATH);
			Date discountEffectiveDate = getEffectiveDate(ctx,subService,account);
			DiscountEventActivity DEA = createDiscounteventActivity(ctx,
					discountEventActivityList, account, subscriber.getId(),
					output.getSecondDeviceOutput().get(0).getServiceID(),
					DiscountEventActivityServiceTypeEnum.SERVICE, output
							.getSecondDeviceOutput().get(0).getPricePlanID(),
					-1, DiscountEventActivityTypeEnum.SECOND_DEVICE_DISCOUNT,
                	output.getRuleId(), output.getRuleVersion(ctx),discountEffectiveDate,
                	output.getSecondDeviceOutput().get(0).getDiscountClass(),
                	-1,-1);
			return DEA;

	}
	
	public static DiscountEventActivity createDiscountEventActivityForFirstDeviceDiscount(
			Context ctx,
			Collection<DiscountEventActivity> discountEventActivityList,
			BusinessRuleIfc output, SubscriberIfc subscriber, Account account) {
		
			SubscriberServices subService = SubscriberServicesSupport
					.getSubscriberServiceRecord(ctx, subscriber.getId(),output.getFirstDeviceOutput().get(0).getServiceID(), SubscriberServicesUtil.DEFAULT_PATH);
			Date discountEffectiveDate = getEffectiveDate(ctx,subService,account);
			DiscountEventActivity DEA = createDiscounteventActivity(ctx,
					discountEventActivityList, account, subscriber.getId(),
					output.getFirstDeviceOutput().get(0).getServiceID(),
					DiscountEventActivityServiceTypeEnum.SERVICE, output
							.getFirstDeviceOutput().get(0).getPricePlanID(),
					-1, DiscountEventActivityTypeEnum.FIRST_DEVICE,

					output.getRuleId(), output.getRuleVersion(ctx),discountEffectiveDate,
					output.getFirstDeviceOutput().get(0).getDiscountClass(),
					-1,-1);
			return DEA;

	}

	public static DiscountEventActivity createDiscounteventActivity(
			Context ctx,
			Collection<DiscountEventActivity> discountEventActivityList,
			Account account, String subscriberId, long serviceID,
			DiscountEventActivityServiceTypeEnum serviceType, long pricePlanID,
			long ppGroupId, DiscountEventActivityTypeEnum discountType,
			String ruleId, int discountRuleVersion,Date discountEffectiveDate,
			int discountClass, long serviceInstance,long contractId) {
		SubscriberServices subscriberService = SubscriberServicesSupport
				.getSubscriberServiceRecord(ctx, subscriberId, serviceID, SubscriberServicesUtil.DEFAULT_PATH);
		DiscountEventActivity discountEventActivity = new DiscountEventActivity();
		
		//Fetching the subscriber instance from the current subscriberId, this is because we are sending the target/root ban for discount scope
		//So to make the existing behavior in sync, we need to put the subscriber's immediate BAN to the DiscountEventActivity table
		//Not using the passed account BAN replacing it from the current Subscriber's Account BAN  
		Subscriber subscriberInst = null;
		//The default ban which got processed
		//Incase of the Root strategy the BAN will be the root ban 
		//Incase of the self strategy the BAN will be itself
		String ban = account.getBAN();
		try{
			subscriberInst = SubscriberSupport.getSubscriber(ctx, subscriberId);
		    ban = subscriberInst.getBAN();	
		    LogSupport.info(ctx, MODULE,
					"createDiscounteventActivity : BAN from subscriberInst" + ban);
			}catch(NullPointerException ex){
				LogSupport.info(ctx, MODULE,
						"createDiscounteventActivity :Exception while getting the ban from subscriberInst" + ex.getMessage());
			}
			catch(HomeException ex){
			LogSupport.info(ctx, MODULE,
					"createDiscounteventActivity : Unable to create discountEventActivity: "
							+ discountType.getName());
			}
			catch(Exception ex){
					LogSupport.info(ctx, MODULE,
						"createDiscounteventActivity : Unable to get ban : subscriberInst");
			}	
		discountEventActivity.setBan(ban);
		discountEventActivity.setSpid(account.getSpid());
		discountEventActivity.setSubId(subscriberId);
		discountEventActivity.setServiceId(serviceID);
		discountEventActivity.setDiscountServiceType(serviceType);
		discountEventActivity.setPpGroupId(ppGroupId);
		discountEventActivity.setPricePlanId(pricePlanID);
        discountEventActivity.setServiceInstance(serviceInstance);
		discountEventActivity.setDiscountType(discountType);
        discountEventActivity.setDiscountClass(discountClass);
		discountEventActivity.setState(DiscountEventActivityStatusEnum.ACTIVE);
		discountEventActivity.setDiscountRuleId(ruleId);
		discountEventActivity.setDiscountRuleVersion(discountRuleVersion);
		discountEventActivity.setDiscountEffectiveDate(discountEffectiveDate);
        discountEventActivity.setContractId(contractId);
		return discountEventActivity;
	}

	/**
	 * Returns the sorted list of subscription based on PP priority. In case of
	 * same PP priority, subscription activated first will get higher priority.
	 * In case of same activation date, subscription created first will get
	 * higher priority. Price plan Priority: 1 is highest priority.
	 * 
	 * @param ctx
	 * @param subList
	 * @return
	 */
	public static List<SubscriberIfc> getSubscriptionByPriority(
			final Context ctx, final List<SubscriberIfc> subList,
			final long subscriptionType, final long pricePlanGroupId) {

		List<SubscriberIfc> subObjList = new ArrayList<SubscriberIfc>();
		;
		Iterator<SubscriberIfc> itr = subList.iterator();
		List<SubscriberIfc> subscriberList = new ArrayList<SubscriberIfc>();
		while (itr.hasNext()) {
			SubscriberIfc sub = itr.next();
			if(subscriptionType==-1  && !sub.getState().equals(SubscriberStateEnum.SUSPENDED))
			{
				subscriberList.add(sub);
			}
			else if (sub.getSubscriptionType() == subscriptionType && !sub.getState().equals(SubscriberStateEnum.SUSPENDED))
			{
				subscriberList.add(sub);
			}
		}

		if (subscriberList.isEmpty()) {
			return null;
		}

		if (subscriberList.size() == 1) {
			return subscriberList;
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(
					ctx,
					MODULE,
					"getSubscriptionByPriority : Subscriber List with out suspended subscription for the given input i.e. subscriptionType is : " + subscriptionType
					 + subList);
		}
		Collections.sort(subscriberList, new Comparator<SubscriberIfc>() {

			@Override
			public int compare(SubscriberIfc o1, SubscriberIfc o2) {
				Subscriber subFirst = (Subscriber) o1;
				Subscriber subSecond = (Subscriber) o2;

				long ppidFirst = subFirst.getPricePlan();
				long ppidSecond = subSecond.getPricePlan();

				PricePlan plan;
				try {

					plan = PricePlanSupport.getPlan(ctx, ppidFirst);

					long priorityFirst = plan != null ? plan
							.getPricePlanPriority() : -1;

					plan = PricePlanSupport.getPlan(ctx, ppidSecond);
					long prioritySecond = plan != null ? plan
							.getPricePlanPriority() : -1;
					if (priorityFirst == prioritySecond) {
						if (subFirst.getDateCreated().before(
								subSecond.getDateCreated())) {
							return -1;
						} else {
							return 1;
						}
					} else {
						return (int) (priorityFirst - prioritySecond);
					}
				} catch (HomeException e) {

				}
				return 0;
			}
		});

		Set pricePlanSet = null;
		try {
			if (pricePlanGroupId != -1) {
				Home home = (Home) ctx.get(PricePlanGroupForContractHome.class);
				PricePlanGroupForContract ppGroup = (PricePlanGroupForContract) home
						.find(ctx, new EQ(PricePlanGroupForContractXInfo.ID,
								pricePlanGroupId));

				if (ppGroup != null) {
					pricePlanSet = ppGroup.getPricePlanList();
					if (null != pricePlanSet) {
						Iterator subIterator = subscriberList.iterator();
						while (subIterator.hasNext()) {
							SubscriberIfc subObj = (SubscriberIfc) subIterator
									.next();
							if (pricePlanSet.contains(subObj.getPricePlan())) {
								
								subObjList.add(subObj);
							}
						}
						if (LogSupport.isDebugEnabled(ctx)) {
							LogSupport.debug(
									ctx,
									MODULE,
									"getSubscriptionByPriority : Subscriber List with out suspended subscription for the given input i.e. price plan group is : " + pricePlanGroupId
									 + subList);
						}
					}

				} else {
					return subscriberList;
				}
			} else {
				return subscriberList;
			}
		} catch (HomeException e) {

		}
		return subObjList;
	}

	

	
	public static void saveDiscountEventActivity(final Context ctx, 
			final Home discountActivityHome, 
			final Collection<DiscountEventActivity> discountEventActivitiesToBeCreated, 
			final Collection<DiscountEventActivity> discountEventActivitiesToBeUpdated){
		// loop to create new discount event activity rows
		for (DiscountEventActivity disc : discountEventActivitiesToBeCreated) {
			try {
				discountActivityHome.create(ctx, disc);
			} catch (Exception e) {
				LogSupport.debug(
						ctx,
						MODULE,
						"saveDiscountEventActivity : Can not create discounteventActivity for BAN"
								+ disc.getBan());
			}
		}
		
		// loop to update new discount event activity rows
		for (DiscountEventActivity disc : discountEventActivitiesToBeUpdated) {
			try {
				if (disc.getState().equals(
						DiscountEventActivityStatusEnum.CANCELLATION_PENDING)) {
					discountActivityHome.store(ctx, disc);
				}
			} catch (Exception e) {
				LogSupport.debug(
						ctx,
						MODULE,
						"saveDiscountEventActivity : Can not update discounteventActivity for BAN"
								+ disc.getBan());
			}
		}
	}
	
	
	public static void saveDiscountEventActivity(final Context ctx, 
				final Collection<DiscountEventActivity> discountEventActivitiesToBeUpdated)
			{
		        final Home discountEventActivityHome =  (Home) ctx.get(DiscountEventActivityHome.class);
				for (DiscountEventActivity disc : discountEventActivitiesToBeUpdated) {
					try {
						if (disc.getState().equals(
								DiscountEventActivityStatusEnum.CANCELLATION_PENDING)|| disc.getState().equals(
										DiscountEventActivityStatusEnum.ACTIVE) ||disc.getState().equals(
												DiscountEventActivityStatusEnum.CANCELLED)) {
							if (LogSupport.isDebugEnabled(ctx)) {
								LogSupport.debug(
										ctx,
										MODULE,
										"saveDiscountEventActivity : Storing Discount Event Activity" + disc);
							}
							discountEventActivityHome.store(ctx, disc);
						}
					} catch (Exception e) {
						LogSupport.debug(
								ctx,
								MODULE,
								"saveDiscountEventActivity : Can not update discounteventActivity for BAN"
										+ disc.getBan());
					}
				}
			}
	
	public static void saveDiscountEventActivity(final Context ctx,
			final DiscountEventActivity discountEventActivitiyToBeUpdated) {
		final Home discountEventActivityHome = (Home) ctx
				.get(DiscountEventActivityHome.class);
		try {
			discountEventActivityHome.store(ctx,
					discountEventActivitiyToBeUpdated);
		} catch (Exception e) {
			LogSupport.debug(ctx, MODULE,
					"saveDiscountEventActivity : Can not update discounteventActivity for BAN"
							+ discountEventActivitiyToBeUpdated.getBan());
		}
	}
	
	
	
	public static void checkNotApplicableDiscounts(Context context,
			Collection<DiscountEventActivity> existingDiscountActivities,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate,
			Collection<DiscountEventActivity> discountEventActivityContinued,
			List<SubscriberIfc> subscriberList) {
		if (LogSupport.isDebugEnabled(context)) {
			LogSupport.debug(
					context,
					MODULE,
					"checkNotApplicableDiscounts : Expiring Not Applicable discounts");
		}
		// find all such entries which are not already process and not applicable now
		for(DiscountEventActivity currDisc : existingDiscountActivities){
			if(!discountEventActivityForCreation.contains(currDisc)
				&& !discountEventActivityForUpdate.contains(currDisc)
				&& !discountEventActivityContinued.contains(currDisc)
				&& !currDisc.getState().equals(DiscountEventActivityStatusEnum.CANCELLATION_PENDING)
				&& !currDisc.getState().equals(DiscountEventActivityStatusEnum.CANCELLED)){
				if (LogSupport.isDebugEnabled(context)) {
					LogSupport.debug(
							context,
							MODULE,
							"checkNotApplicableDiscounts : Setting Expiration date for discount event activiyt " + currDisc);
				}
				// if the entry doesn't exist in newly created list and update list
				if(currDisc.getDiscountType().equals(DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT))
				{
					try
					{
					 Account account = AccountSupport.getAccount(context,currDisc.getBan());
					 long accContractId = account.getContract(context);
					 if(accContractId == -1)
					 {
						 //TODO Manish this class need to taken care
						 /*AccountContractHistory accountContractHistoryBean = AccountContractSupport.getAccountContractHistory(context,account.getBAN(), currDisc.getContractId(),CancelReasonEnum.CANCELLED);
							
						 if(null!=accountContractHistoryBean)
						 {
						   currDisc.setDiscountExpirationDate(CalendarSupportHelper.get(context).
                                 getDateWithNoTimeOfDay(accountContractHistoryBean.getContractEndDate()));
						  if (LogSupport.isDebugEnabled(context)) {
								LogSupport.debug(
										context,
										MODULE,
										"checkNotApplicableDiscounts : Setting contract end date as expiry date for the discount : " + currDisc.getId());
						    }
						 }else*/
						 {
							 currDisc.setDiscountExpirationDate(getExpiryDateFromAllContributors(context,currDisc.getDiscountRuleId(),
										currDisc.getServiceId(),currDisc.getSubId(),subscriberList));
							 if (LogSupport.isDebugEnabled(context)) {
									LogSupport.debug(
											context,
											MODULE,
											"checkNotApplicableDiscounts : Setting current date as expiry date for the discount : " + currDisc.getId());
							    }
						 }
					 }else
					 {
						 currDisc.setDiscountExpirationDate(getExpiryDateFromAllContributors(context,currDisc.getDiscountRuleId(),
									currDisc.getServiceId(),currDisc.getSubId(),subscriberList));
						 if (LogSupport.isDebugEnabled(context)) {
								LogSupport.debug(
										context,
										MODULE,
										"checkNotApplicableDiscounts : Setting current date as expiry date for the discount : " + currDisc.getId());
						    }
					 }
					 
					}catch(Exception e)
					{
						LogSupport.info(context,MODULE,"Error while getting account or account contract");
					}
					
				}else
				{
				   currDisc.setDiscountExpirationDate(getExpiryDateFromAllContributors(context,currDisc.getDiscountRuleId(),
						currDisc.getServiceId(),currDisc.getSubId(),subscriberList));
				}
				currDisc.setState(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);
				discountEventActivityForUpdate.add(currDisc);
			}
		}
	}

	public static Date getExpiryDateFromContributors(final Context ctx,
			final String subId,
			final long serviceId,
			final DiscountEventActivityServiceTypeEnum serviceType,
			final long secondaryId)
	{
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
							MODULE,
							"getExpiryDateFromContributors : Current Subscriber is Suspended or contributor for the current discount is suspended " +sub);
				}
		    	if(serviceType.equals(DiscountEventActivityServiceTypeEnum.SERVICE))
		    	{
					Service service = ServiceSupport.getService(ctx, serviceId);
					
		            if(service.isRefundable())
		            {
		            	if(service.isSmartSuspension())
		            	{
		            		SubscriberServices subscriberService = SubscriberServicesSupport
									.getSubscriberServiceRecord(ctx,
											subId,
											serviceId, SubscriberServicesUtil.DEFAULT_PATH);
		            		if(null!=subscriberService)
		            		{
		            			if (LogSupport.isDebugEnabled(ctx)) {
		        					LogSupport.debug(
		        							ctx,
		        							MODULE,
		        							"getExpiryDateFromContributors : Current Subscriber service is refundable and smart suspension is ON " +subscriberService);
		        				}
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
		        							MODULE,
		        							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is cycle end date as sub scriber service is found :  "+expiryDate);
		        				}
		            		}else
		            		{
		            			
		            			expiryDate=CalendarSupportHelper
										.get(ctx).getDateWithNoTimeOfDay(
												new Date());
		            			if (LogSupport.isDebugEnabled(ctx)) {
		        					LogSupport.debug(
		        							ctx,
		        							MODULE,
		        							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is current date as sub scriber service is not found :  "+expiryDate);
		        				}
		            		}
		            	}else
		            	{
		            		if (LogSupport.isDebugEnabled(ctx)) {
	        					LogSupport.debug(
	        							ctx,
	        							MODULE,
	        							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is current date as sub scriber service's smart suspension is OFF :  "+expiryDate);
	        				}
		            		expiryDate=CalendarSupportHelper
									.get(ctx).getDateWithNoTimeOfDay(
											new Date());
		            		
		            	}
		            	
		            	
		            }else{
		            	
		            	
		            	SubscriberServices subscriberService = SubscriberServicesSupport
								.getSubscriberServiceRecord(ctx,
										subId,
										serviceId, SubscriberServicesUtil.DEFAULT_PATH);

            			if (LogSupport.isDebugEnabled(ctx)) {
        					LogSupport.debug(
        							ctx,
        							MODULE,
        							"getExpiryDateFromContributors : Current Subscriber service is non-refundable ");
        				}
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
		            	 if (LogSupport.isDebugEnabled(ctx)) {
	        					LogSupport.debug(
	        							ctx,
	        							MODULE,
	        							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is cycle end date as sub scriber service is found :  "+expiryDate);
	        				}
	            		}else
	            		{
	            			expiryDate=CalendarSupportHelper
									.get(ctx).getDateWithNoTimeOfDay(
											new Date());
	            			if (LogSupport.isDebugEnabled(ctx)) {
	        					LogSupport.debug(
	        							ctx,
	        							MODULE,
	        							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is current date as sub scriber service is not found :  "+expiryDate);
	        				}
	            		}
		            }
		    	}else if(serviceType.equals(DiscountEventActivityServiceTypeEnum.AUXILIARY_SERVICE))
		    	{
		    		AuxiliaryService auxService = AuxiliaryServiceSupport.getAuxiliaryService(ctx, serviceId);
		    		 if(auxService.isRefundable())
			            {
			            	if(auxService.isSmartSuspension())
			            	{
			            		if (LogSupport.isDebugEnabled(ctx)) {
		        					LogSupport.debug(
		        							ctx,
		        							MODULE,
		        							"getExpiryDateFromContributors : Current Subscriber Aux service is refundable and smart suspension is ON ");
		        				}
			            		SubscriberAuxiliaryService subscriberAuxService = SubscriberAuxiliaryServiceSupport
										.getSubAuxServBySubIdAuxIdAndSecondaryId(ctx,
												subId,
												serviceId,
												secondaryId);
			            		if(null!=subscriberAuxService)
			            		{
				            	 expiryDate=CalendarSupportHelper
										.get(ctx)
										.getDateWithNoTimeOfDay(
												CalendarSupportHelper
												.get(ctx)
												.findDateDaysBefore(
														1,
														subscriberAuxService
														.getNextRecurringChargeDate()));
				            	 if (LogSupport.isDebugEnabled(ctx)) {
			        					LogSupport.debug(
			        							ctx,
			        							MODULE,
			        							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is cycle end date as subscriber Auxservice is found :  "+expiryDate);
			        				}
			            		}else
			            		{
			            			expiryDate=CalendarSupportHelper
											.get(ctx).getDateWithNoTimeOfDay(
													new Date());
			            			if (LogSupport.isDebugEnabled(ctx)) {
			        					LogSupport.debug(
			        							ctx,
			        							MODULE,
			        							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is current date as subscriber Auxservice is not found :  "+expiryDate);
			        				}
			            		}
			            	}else
			            	{
			            		if (LogSupport.isDebugEnabled(ctx)) {
		        					LogSupport.debug(
		        							ctx,
		        							MODULE,
		        							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is current date as Smart suspension is OFF:  "+expiryDate);
		        				}
			            		expiryDate=CalendarSupportHelper
										.get(ctx).getDateWithNoTimeOfDay(
												new Date());
			            		
			            	}
			            	
			            	
			            }else{
			            	
			            	if (LogSupport.isDebugEnabled(ctx)) {
	        					LogSupport.debug(
	        							ctx,
	        							MODULE,
	        							"getExpiryDateFromContributors : Current Subscriber Auxservice is non-refundable ");
	        				}
			            	SubscriberAuxiliaryService subscriberAuxService = SubscriberAuxiliaryServiceSupport
									.getSubAuxServBySubIdAuxIdAndSecondaryId(ctx,
											subId,
											serviceId,
											secondaryId);
			            	if(null!=subscriberAuxService)
			            	{
			            	expiryDate=CalendarSupportHelper
									.get(ctx)
									.getDateWithNoTimeOfDay(
											CalendarSupportHelper
											.get(ctx)
											.findDateDaysBefore(
													1,
													subscriberAuxService
													.getNextRecurringChargeDate()));
			            	 if (LogSupport.isDebugEnabled(ctx)) {
		        					LogSupport.debug(
		        							ctx,
		        							MODULE,
		        							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is cycle end date as subscriber Auxservice is found :  "+expiryDate);
		        				}
			            	}else
			            	{
			            		expiryDate=CalendarSupportHelper
										.get(ctx).getDateWithNoTimeOfDay(
												new Date());
			            		if (LogSupport.isDebugEnabled(ctx)) {
		        					LogSupport.debug(
		        							ctx,
		        							MODULE,
		        							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is current date as subscriber Auxservice is not found :  "+expiryDate);
		        				}
			            	}
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
							MODULE,
							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is current date as subscriber  state is Inactive : "+expiryDate);
				}
		    }else
		    {
		    	expiryDate = CalendarSupportHelper
						.get(ctx).getDateWithNoTimeOfDay(
								new Date());
		    	if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromContributors : Setting Expiry date for the sub" + sub + "is current date  : "+expiryDate);
				}
		    }
		} catch (Exception e) {
			 expiryDate = CalendarSupportHelper
					.get(ctx).getDateWithNoTimeOfDay(
							new Date());
		     LogSupport.info(ctx, MODULE,"getExpiryDateFromContributors : Error while Calculating Expiry Date");
		}
		
			return expiryDate;
	}
	/*public static void checkRuleVersionChange(Context context,
			DiscountEventActivity discountEventActivity,
			Collection<DiscountEventActivity> existingDiscountActivities,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate) {
		
		if(existingDiscountActivities!=null && !existingDiscountActivities.isEmpty())
		{
			//find if the same rule with older version is already applied
			for(DiscountEventActivity currDisc : existingDiscountActivities){
				// condition for same rule with different version comparison
				if(currDisc.getBan().equals(discountEventActivity.getBan()) &&
						currDisc.getSubId().equals(discountEventActivity.getSubId()) &&
						currDisc.getDiscountType().equals(discountEventActivity.getDiscountType()) &&
						currDisc.getDiscountRuleId().equals(discountEventActivity.getDiscountRuleId()) &&
						currDisc.getContractId() == discountEventActivity.getContractId() &&
						currDisc.getServiceId() == discountEventActivity.getServiceId() &&
						currDisc.getDiscountServiceType().equals(discountEventActivity.getDiscountServiceType()) &&
						currDisc.getState().equals(discountEventActivity.getState()) &&
						//version should be different
						currDisc.getDiscountRuleVersion() != discountEventActivity.getDiscountRuleVersion()
						){
					// In case of version change need to expire old entry and create new entry
					currDisc.setDiscountExpirationDate(calculateExpiryDate(context));
					currDisc.setState(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);
					discountEventActivityForUpdate.add(currDisc);
					//Setting Effective date as next day of the expired discount 
					discountEventActivity.setDiscountEffectiveDate(CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(new Date()));
					//discountEventActivityForCreation.add(discountEventActivity);
					break;
				}
			}
		}
	}*/
	
	public static boolean checkIfRuleVersionChange(Context context,
			final DiscountEventActivity discountEventActivity,
			final BusinessRuleIfc output) {
		
		 if(discountEventActivity.getDiscountRuleVersion()!=output.getRuleVersion(context))
		 {
			 return true;
		 }
		return false;
	}
	
	
	public static Date calculateExpiryDate(final Context ctx) {
		Date expiryDate = CalendarSupportHelper.get(ctx).getDayBefore(new Date());
		
		return expiryDate;
	}
	
	public  static Date getEffectiveDate(final Context ctx
			,final SubscriberServices subscriberService
			,final Account account)
	{
		/*
		 * Check here is to compare date of service's bill start date with
		 * respect to discount 1. If Service is provisioned two months back then
		 * discount effective date should be currentBCD(it task runs daily) 2.
		 * If Task runs on last day of month and service is provisioned in the
		 * previous month then current bcd is set as discount effective date 3.
		 * If task runs on last day of month and service is provisioned in the
		 * current month then Service's bill start date should be set as
		 * discount effective date 4. If future dated service is provisioned
		 * then Service's bill start date should set as discount effective date
		 */
		Date effectiveDate = null;
				
		if (CalendarSupportHelper
				.get(ctx)
				.getDateWithNoTimeOfDay(subscriberService.getStartDate())
				.before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
						new Date()))) {
			Date currentBCD;
			
			try {
				currentBCD = InvoiceSupport.getCurrentBillingDate(account
						.getBillCycleDay(ctx));

				if (CalendarSupportHelper
						.get(ctx)
						.getDateWithNoTimeOfDay(
								subscriberService.getStartDate())
						.before(CalendarSupportHelper.get(ctx)
								.getDateWithNoTimeOfDay(currentBCD))) {
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,
								"getEffectiveDate : Effective date as Current BCD for Subscriber : " 
								+ subscriberService.getSubscriberId()
								+" For Service : " + subscriberService.getServiceId()
								+" is : " + currentBCD);
					}
					effectiveDate=CalendarSupportHelper.get(ctx)
							.getDateWithNoTimeOfDay(currentBCD);
				} else if (((CalendarSupportHelper
						.get(ctx)
						.getDateWithNoTimeOfDay(
								subscriberService.getStartDate())
						.after(CalendarSupportHelper.get(ctx)
								.getDateWithNoTimeOfDay(currentBCD)))|| 
								(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
										subscriberService.getStartDate()).getTime()== CalendarSupportHelper.
										get(ctx).getDateWithNoTimeOfDay(currentBCD).getTime()))
						&& CalendarSupportHelper
								.get(ctx)
								.getDateWithNoTimeOfDay(
										subscriberService.getStartDate())
								.before(CalendarSupportHelper.get(ctx)
										.getDateWithNoTimeOfDay(new Date()))) {
					effectiveDate=CalendarSupportHelper
									.get(ctx).getDateWithNoTimeOfDay(
											subscriberService
													.getStartDate());
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,
								"getEffectiveDate : Effective date as bill start for Subscriber : " 
								+ subscriberService.getSubscriberId()
								+" For Service : " + subscriberService.getServiceId()
								+" is : " + effectiveDate);
					}
				} else {
					effectiveDate=CalendarSupportHelper
									.get(ctx)
									.getDateWithNoTimeOfDay(new Date());
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,
								"getEffectiveDate : Effective date as current date for Subscriber : " 
								+ subscriberService.getSubscriberId()
								+" For Service : " + subscriberService.getServiceId()
								+" is : " + effectiveDate);
					}
				}
			} catch (Exception e) {
				LogSupport.info(ctx,MODULE, "getEffectiveDate : Exception Occured while calculating effective date" + e.getMessage());
				effectiveDate=CalendarSupportHelper
						.get(ctx)
						.getDateWithNoTimeOfDay(new Date());
			}
		} else {
			try
			{
			effectiveDate=CalendarSupportHelper.get(ctx)
							.getDateWithNoTimeOfDay(
									subscriberService.getStartDate());
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(
						ctx,
						MODULE,
						"getEffectiveDate : Effective date as bill start for Subscriber : " 
						+ subscriberService.getSubscriberId()
						+" For Service : " + subscriberService.getServiceId()
						+" is : " + effectiveDate);
			}
			}catch(Exception e)
			{
				LogSupport.info(ctx,MODULE, "getEffectiveDate : Exception Occured while calculating effective date" + e.getMessage());
				effectiveDate=CalendarSupportHelper
						.get(ctx)
						.getDateWithNoTimeOfDay(new Date());
			}

		}
		if(null!=ctx.get(DiscountClassContextAgent.RESUMETRIGGER) && ctx.getBoolean(DiscountClassContextAgent.RESUMETRIGGER))
		{
			try {
				Service serviceObj = ServiceSupport.getService(ctx, subscriberService.getServiceId());
				Subscriber sub = SubscriberSupport.getSubscriber(ctx,subscriberService.getSubscriberId());
				if(null!=serviceObj && null!=sub && null!=sub.getResumedDate())
				{
					if(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(sub.getResumedDate())
							.after(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiveDate)))
					{
						effectiveDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(sub.getResumedDate());
						if (LogSupport.isDebugEnabled(ctx)) {
							LogSupport.debug(
									ctx,
									MODULE,
									"getEffectiveDate : Effective date as resume date for Subscriber : " 
									+ subscriberService.getSubscriberId()
									+" For Service : " + subscriberService.getServiceId()
									+" is : " + effectiveDate);
						}
					}
				}
		    } catch (Exception e) {
			LogSupport.info(ctx,MODULE, "getEffectiveDate : Unable to retrive service from service id" + subscriberService.getServiceId());
		   }
		}
		
		
		return effectiveDate;
	}
	
	public static Date getLatestEffectiveDate(final Map<String,Long> subscriberServiceMap
			                           ,final Context ctx
			                           ,final Account account)
	{
	   Set<String> subscriberSet = subscriberServiceMap.keySet();
	   List<Date> effectiveDateList = new ArrayList<Date>();
	   for(String subId : subscriberSet)
	   {
		   SubscriberServices subService = SubscriberServicesSupport
					.getSubscriberServiceRecord(ctx, subId, subscriberServiceMap.get(subId), SubscriberServicesUtil.DEFAULT_PATH);
		   if(null!=subService)
		   {
			   effectiveDateList.add(getEffectiveDate(ctx, subService, account));
			   if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getLatestEffectiveDate : Effective date added to the List of contributors for Subscriber" +
									subService.getSubscriberId() + " and Service is : " + subService.getServiceId());
				}
		   }
	   }
	   Collections.sort(effectiveDateList);
	   
	   return effectiveDateList.get(effectiveDateList.size()-1);	
	}
		
	
	/**
	 * This Method will find the oldest subscriber in given subscriber list for
	 * given subscription type
	 * 
	 * @param subScriptionType
	 * @param subList
	 * @param pricePlanId
	 * @param serviceId
	 * @return
	 */
	public static SubscriberIfc getOldestSubscriberForInput(final Context ctx,
			final long subScriptionType, final long pricePlanId,
			List<SubscriberIfc> subScriberList,
			final long serviceId) {

		Iterator<SubscriberIfc> itr = subScriberList.iterator();
		List<SubscriberIfc> subList = new ArrayList<SubscriberIfc>();
		while (itr.hasNext()) {
			SubscriberIfc sub = itr.next();
			if (sub.getSubscriptionType() == subScriptionType
					&& sub.getPricePlan() == pricePlanId
					&& !sub.getServices().equals(SubscriberStateEnum.SUSPENDED)) {
				SubscriberServices subscriberService = SubscriberServicesSupport
						.getSubscriberServiceRecord(ctx, sub.getId(), serviceId, SubscriberServicesUtil.DEFAULT_PATH);
				if (null != subscriberService) {
					subList.add(sub);
				}
			}
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(
					ctx,
					MODULE,
					"getOldestSubscriberForInput : Subscription List for the given input i.e. PricePlan : " + pricePlanId
					+" service Id : " + serviceId + " is : " + subList);
		}
		if (subList.isEmpty()) {
			return null;
		}

		if (subList.size() == 1) {
			return subList.get(0);
		}

		Collections.sort(subList, new Comparator<SubscriberIfc>() {

			@Override
			public int compare(SubscriberIfc o1, SubscriberIfc o2) {
				Subscriber subFirst = (Subscriber) o1;
				Subscriber subSecond = (Subscriber) o2;

				/**
				 * prioritize based on subscriber activation date
				 */
			/*	if (subFirst.getStartDate() != null
						&& subSecond.getStartDate() != null
						&& subFirst.getStartDate().before(
								subSecond.getStartDate())) {
					return -1;
				} else if (subFirst.getStartDate() != null
						&& subSecond.getStartDate() != null
						&& subFirst.getStartDate().after(
								subSecond.getStartDate())) {
					return 1;
				}*/
				/**
				 * Priority based on subscriber creation date
				 */
				if (subFirst.getDateCreated().before(
						subSecond.getDateCreated())) {
					return -1;
				} else {
					return 1;
				}

			}
		});

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(
					ctx,
					MODULE,
					"getOldestSubscriberForInput : Oldest Subscription for the given input i.e. PricePlan : " + pricePlanId
					+" service Id : " + serviceId + " is : " + subList.get(0));
		}
		return subList.get(0);
	}

	/**
	 * This Method will find the oldest subscriber in given subscriber list for
	 * given subscription type
	 * 
	 * @param subScriptionType
	 * @param subList
	 * @param pricePlanId
	 * @param serviceId
	 * @return
	 */
	public static SubscriberIfc getOldestSubscriberForInputIncludingSuspendedState(final Context ctx,
			final long subScriptionType, final long pricePlanId,
			List<SubscriberIfc> subScriberList,
			final long serviceId) {

		Iterator<SubscriberIfc> itr = subScriberList.iterator();
		List<SubscriberIfc> subList = new ArrayList<SubscriberIfc>();
		while (itr.hasNext()) {
			SubscriberIfc sub = itr.next();
			if (sub.getSubscriptionType() == subScriptionType
					&& sub.getPricePlan() == pricePlanId) {
				SubscriberServices subscriberService = SubscriberServicesSupport
						.getSubscriberServiceRecord(ctx, sub.getId(), serviceId, SubscriberServicesUtil.DEFAULT_PATH);
				if (null != subscriberService) {
					subList.add(sub);
				}
			}
		}

		if (subList.isEmpty()) {
			return null;
		}

		if (subList.size() == 1) {
			return subList.get(0);
		}
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(
					ctx,
					MODULE,
					"getOldestSubscriberForInputIncludingSuspendedState : Subscriber List with suspended subscription for the given input i.e. PricePlan : " + pricePlanId
					+" service Id : " + serviceId + " is : " + subList);
		}

		Collections.sort(subList, new Comparator<SubscriberIfc>() {

			@Override
			public int compare(SubscriberIfc o1, SubscriberIfc o2) {
				Subscriber subFirst = (Subscriber) o1;
				Subscriber subSecond = (Subscriber) o2;

				/**
				 * prioritize based on subscriber activation date
				 */
			/*	if (subFirst.getStartDate() != null
						&& subSecond.getStartDate() != null
						&& subFirst.getStartDate().before(
								subSecond.getStartDate())) {
					return -1;
				} else if (subFirst.getStartDate() != null
						&& subSecond.getStartDate() != null
						&& subFirst.getStartDate().after(
								subSecond.getStartDate())) {
					return 1;
				}*/
				/**
				 * Priority based on subscriber creation date
				 */
				if (subFirst.getDateCreated().before(
						subSecond.getDateCreated())) {
					return -1;
				} else {
					return 1;
				}

			}
		});

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(
					ctx,
					MODULE,
					"getOldestSubscriberForInputIncludingSuspendedState : Oldest Subscription for the given input i.e. PricePlan : " + pricePlanId
					+" service Id : " + serviceId + " is : " + subList.get(0));
		}
		return subList.get(0);
	}

	
	 private static Date getExpiryDateFromAllContributors(final Context ctx,final String ruleId
    		 ,final long serviceId
    		 ,final String subId
    		 ,final List<SubscriberIfc> subscriberList)
     {
    	 Home home = (Home) ctx.get(BusinessRuleHome.class);
    	 List<Date> expiryDateList = new ArrayList<Date>();
    	 try {
			BusinessRule rule = (BusinessRule)home.find(ctx,ruleId);
			if(null!=rule && rule.getDiscountType().equals(DiscountCriteriaTypeEnum.CROSS_SUBSCRIPTION))
			{
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : Setting Expiration date for discount event activity of type Cross Subscription ");
				}
				List<CrossSubscriptionDiscountCriteriaHolder> inputList = rule.getCrossSubscriptionDiscountingCriteria();
				if(null!=inputList && !inputList.isEmpty())
				{
					Iterator<CrossSubscriptionDiscountCriteriaHolder> iterator = inputList.iterator();
					 
					   while(iterator.hasNext())
					   {
						  CrossSubscriptionDiscountCriteriaHolder input = iterator.next();
						  
						  if(input.getPricePlanGroupID()!=-1)
						  {
							  SubscriberIfc sub = DiscountActivityUtils.getSubscriptionByPriority(ctx,subscriberList,input.getSubscriptionType(),
									  input.getPricePlanGroupID()).get(0); 
			    	    	
							  if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,
											"getExpiryDateFromAllContributors : Subscriber for the Input containing price plan group is : " + sub );
									
									
								}
							  if(null!=sub)
							  {
								  Collection<SubscriberServices> collectionOfMandatoryService=SubscriberServicesSupport.getMandatorySubscriberServices(ctx, sub.getId());
					    	      if(null!=collectionOfMandatoryService && !collectionOfMandatoryService.isEmpty())
					    	      {
					    	    	   SubscriberServices serviceObj = new ArrayList<SubscriberServices>(collectionOfMandatoryService).get(0);
					    	    	   if (LogSupport.isDebugEnabled(ctx)) {
											LogSupport.debug(
													ctx,
													MODULE,
													"getExpiryDateFromAllContributors : fetching expiry date for the servic : " + serviceObj);
											
											
										}
					    	    	   expiryDateList.add(getExpiryDateFromContributors(ctx,
											    sub.getId(),
											 	input.getServiceID(),
											 	DiscountEventActivityServiceTypeEnum.SERVICE,
											 	-1
												));
					    	      }else
					    	      {
					    	    	  if (LogSupport.isDebugEnabled(ctx)) {
											LogSupport.debug(
													ctx,
													MODULE,
													"getExpiryDateFromAllContributors : Adding expiry date to the list as current date as SubscriberService is not found ");
											
											
										}
					    	    	  expiryDateList.add(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
					    	      }
							  }else
							  {
								  if (LogSupport.isDebugEnabled(ctx)) {
										LogSupport.debug(
												ctx,
												MODULE,
												"getExpiryDateFromAllContributors : Adding expiry date to the list as current date as Subscriber is not found ");
										
										
									}
								  expiryDateList.add(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
							  }
							  
						  }else
						  {
							  SubscriberIfc sub = DiscountActivityUtils.getOldestSubscriberForInputIncludingSuspendedState(ctx,
							    		input.getSubscriptionType(), input.getPricePlanID(), subscriberList, input.getServiceID());
							 if(null!=sub)
							 {
								 if (LogSupport.isDebugEnabled(ctx)) {
										LogSupport.debug(
												ctx,
												MODULE,
												"getExpiryDateFromAllContributors : fetching expiry date for the service : " + input.getServiceID()
												+" and subscriber : " + sub);
										
										
									}
							  expiryDateList.add(getExpiryDateFromContributors(ctx,
									    sub.getId(),
									 	input.getServiceID(),
									 	DiscountEventActivityServiceTypeEnum.SERVICE,
									 	-1
										));
							 }else
							    {
								 if (LogSupport.isDebugEnabled(ctx)) {
										LogSupport.debug(
												ctx,
												MODULE,
												"getExpiryDateFromAllContributors : Adding expiry date to the list as current date as Subscriber is not found ");
										
										
									}
							    	expiryDateList.add(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
							    }
						  }
						  
					   }
				}

				 if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,
								"getExpiryDateFromAllContributors : fetching expiry date for the current discount event activity's service : " + serviceId
								+" and subscriber : " + subId);
						
						
					}
				expiryDateList.add(getExpiryDateFromContributors(ctx,
						     subId,
						     serviceId,
						 	DiscountEventActivityServiceTypeEnum.SERVICE,
						 	-1
							));
				 Collections.sort(expiryDateList);
				 if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,
								"getExpiryDateFromAllContributors : Expiration date for discount event activity of type Cross Subscription discount is " + expiryDateList.get(expiryDateList.size()-1));
					}
				 return expiryDateList.get(expiryDateList.size()-1);
			}else if(null!=rule && rule.getDiscountType().equals(DiscountCriteriaTypeEnum.PAIRED_DISCOUNT))
			{
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : Setting Expiration date for discount event activity of type Paired discount ");
				}
				List<PairedDiscountCriteriaHolder> inputList = rule.getPairedDiscountingCriteria();
				if(null!=inputList && !inputList.isEmpty())
				{
					Iterator<PairedDiscountCriteriaHolder> iterator = inputList.iterator();
					 
					   while(iterator.hasNext())
					   {
						   PairedDiscountCriteriaHolder input = iterator.next();
						   SubscriberIfc sub = DiscountActivityUtils.getOldestSubscriberForInputIncludingSuspendedState(ctx,
						    		input.getSubscriptionType(), input.getPricePlanID(), subscriberList, input.getServiceID());
						   if(null!=sub)
						   {
							   if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,
											"getExpiryDateFromAllContributors : fetching expiry date for the service : " + input.getServiceID()
											+" and subscriber : " + sub);
									
									
								}
						   expiryDateList.add(getExpiryDateFromContributors(ctx,
								    sub.getId(),
								 	input.getServiceID(),
								 	DiscountEventActivityServiceTypeEnum.SERVICE,
								 	-1
									));
						   }else
						    {
							   if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,
											"getExpiryDateFromAllContributors : Adding expiry date to the list as current date as Subscriber is not found ");
									
									
								}
						    	expiryDateList.add(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
						    }
					   }
				}
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : fetching expiry date for the current discount event activity's service : " + serviceId
							+" and subscriber : " + subId);
					
					
				}
				expiryDateList.add(getExpiryDateFromContributors(ctx,
					     subId,
					     serviceId,
					 	DiscountEventActivityServiceTypeEnum.SERVICE,
					 	-1
						));
			   Collections.sort(expiryDateList);
			   if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : Expiration date for discount event activity of type PairedDevice discount is " + expiryDateList.get(expiryDateList.size()-1));
				}
			   return expiryDateList.get(expiryDateList.size()-1);
			}else if(null!=rule && rule.getDiscountType().equals(DiscountCriteriaTypeEnum.COMBINATION_DISCOUNT))
			{
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : Setting Expiration date for discount event activity of type Combination discount ");
				}
				List<CombinationDiscountCriteriaHolder> inputList = rule.getCombinationDiscountingCriteria();
				if(null!=inputList && !inputList.isEmpty())
				{
					Iterator<CombinationDiscountCriteriaHolder> iterator = inputList.iterator();
					 
					   while(iterator.hasNext())
					   {
						   CombinationDiscountCriteriaHolder input = iterator.next();
						    SubscriberIfc sub = DiscountActivityUtils.getOldestSubscriberForInputIncludingSuspendedState(ctx,
						    		input.getSubscriptionType(), input.getPricePlanID(), subscriberList, input.getServiceID());
						    if(null!=sub)
						    {
						    	if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,
											"getExpiryDateFromAllContributors : fetching expiry date for the service : " + input.getServiceID()
											+" and subscriber : " + sub);
									
									
								}
						      expiryDateList.add(getExpiryDateFromContributors(ctx,
								    sub.getId(),
								 	input.getServiceID(),
								 	DiscountEventActivityServiceTypeEnum.SERVICE,
								 	-1
									));
						    }else
						    {
						    	if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,
											"getExpiryDateFromAllContributors : Adding expiry date to the list as current date as Subscriber is not found ");
									
									
								}
						    	expiryDateList.add(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
						    }
					   }
				}
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : fetching expiry date for the current discount event activity's service : " + serviceId
							+" and subscriber : " + subId);
					
					
				}
				expiryDateList.add(getExpiryDateFromContributors(ctx,
					     subId,
					     serviceId,
					 	DiscountEventActivityServiceTypeEnum.SERVICE,
					 	-1
						));
			   Collections.sort(expiryDateList);
			   if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : Expiration date for discount event activity of type Combination discount is " + expiryDateList.get(expiryDateList.size()-1));
				}
			   return expiryDateList.get(expiryDateList.size()-1);
			}else if(null!=rule && rule.getDiscountType().equals(DiscountCriteriaTypeEnum.MASTER_PACK))
			{
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : Setting Expiration date for discount event activity of type MasterPack discount ");
				}
				List<MasterPackDiscountCriteriaHolder> inputList = rule.getMasterPackDiscountingCriteria();
				if(null!=inputList && !inputList.isEmpty())
				{
					Iterator<MasterPackDiscountCriteriaHolder> iterator = inputList.iterator();
					 
					   while(iterator.hasNext())
					   {
						  MasterPackDiscountCriteriaHolder input = iterator.next();
						  SubscriberIfc sub = MasterPackDiscountHandler.getOldestSubscriberForMasterInput(ctx,input.getSubscriptionType(),
								  input.getPricePlanID(),subscriberList,-1); 
						  if(null!=sub)
						  {
							  Collection<Long> subServiceIdCollection = SubscriberServicesSupport.getMandatoryServicesId(ctx, sub.getId());
							  if(null!=subServiceIdCollection && !subServiceIdCollection.isEmpty())
							  {
								  if (LogSupport.isDebugEnabled(ctx)) {
										LogSupport.debug(
												ctx,
												MODULE,
												"getExpiryDateFromAllContributors : fetching expiry date for the service : " + input.getPricePlanID()
												+" and subscriber : " + sub);
										
										
									}
								  expiryDateList.add(getExpiryDateFromContributors(ctx,
										    sub.getId(),
										 	new ArrayList<Long>(subServiceIdCollection).get(0),
										 	DiscountEventActivityServiceTypeEnum.SERVICE,
										 	-1
											));
							  }else
							  {
								  if (LogSupport.isDebugEnabled(ctx)) {
										LogSupport.debug(
												ctx,
												MODULE,
												"getExpiryDateFromAllContributors : Adding expiry date to the list as current date as SubscriberService is not found ");
										
										
									}
								  expiryDateList.add(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date())); 
							  }
						  }else
						    {
							  if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,
											"getExpiryDateFromAllContributors : Adding expiry date to the list as current date as Subscriber is not found ");
									
									
								}
						    	expiryDateList.add(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
						    }
					   }
				}
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : fetching expiry date for the current discount event activity's service : " + serviceId
							+" and subscriber : " + subId);
					
					
				}
				expiryDateList.add(getExpiryDateFromContributors(ctx,
					     subId,
					     serviceId,
					 	DiscountEventActivityServiceTypeEnum.AUXILIARY_SERVICE,
					 	-1
						));
			   Collections.sort(expiryDateList);
			   if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : Expiration date for discount event activity of type MasterPack discount is " + expiryDateList.get(expiryDateList.size()-1));
				}
			   return expiryDateList.get(expiryDateList.size()-1);
			}else if(null!=rule && rule.getDiscountType().equals(DiscountCriteriaTypeEnum.FIRST_DEVICE))
			{
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : Setting Expiration date for discount event activity of type FirstDevice discount ");
				}
				List<FirstDeviceDiscountHolder> inputList = rule.getFirstDeviceDiscountingCriteria();
				if(null!=inputList && !inputList.isEmpty())
				{
					Iterator<FirstDeviceDiscountHolder> iterator = inputList.iterator();
					 
					   while(iterator.hasNext())
					   {
						   FirstDeviceDiscountHolder input = iterator.next();
						    SubscriberIfc sub = DiscountActivityUtils.getOldestSubscriberForInputIncludingSuspendedState(ctx,
						    		input.getSubscriptionType(), input.getPricePlanID(), subscriberList, input.getServiceID());
						    if(null!=sub)
						    {
						    	if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,
											"getExpiryDateFromAllContributors : fetching expiry date for the service : " + input.getServiceID()
											+" and subscriber : " + sub);
									
									
								}
						        expiryDateList.add(getExpiryDateFromContributors(ctx,
								    sub.getId(),
								 	input.getServiceID(),
								 	DiscountEventActivityServiceTypeEnum.SERVICE,
								 	-1
									));
						    }else
						    {
						    	if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,
											"getExpiryDateFromAllContributors : Adding expiry date to the list as current date as Subscriber is not found ");
									
									
								}
						    	expiryDateList.add(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
						    }
					   }
				}
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : fetching expiry date for the current discount event activity's service : " + serviceId
							+" and subscriber : " + subId);
					
					
				}
				expiryDateList.add(getExpiryDateFromContributors(ctx,
					     subId,
					     serviceId,
					 	DiscountEventActivityServiceTypeEnum.SERVICE,
					 	-1
						));
			   Collections.sort(expiryDateList);
			   if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : Expiration date for discount event activity of type FirstDevice discount is " + expiryDateList.get(expiryDateList.size()-1));
				}
			   return expiryDateList.get(expiryDateList.size()-1);
			}else if(null!=rule && rule.getDiscountType().equals(DiscountCriteriaTypeEnum.SECONDARY_DEVICE))
			{
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : Setting Expiration date for discount event activity of type SecondDevice discount ");
				}
				List<SecondaryDeviceDiscountHolder> inputList = rule.getSecondaryDeviceDiscountingCriteria();
				if(null!=inputList && !inputList.isEmpty())
				{
					Iterator<SecondaryDeviceDiscountHolder> iterator = inputList.iterator();
					 
					   while(iterator.hasNext())
					   {
						   SecondaryDeviceDiscountHolder input = iterator.next();
						    SubscriberIfc sub = DiscountActivityUtils.getOldestSubscriberForInputIncludingSuspendedState(ctx,
						    		input.getSubscriptionType(), input.getPricePlanID(), subscriberList, input.getServiceID());
						    if(null!=sub)
						    {
						    	if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,
											"getExpiryDateFromAllContributors : fetching expiry date for the service : " + input.getServiceID()
											+" and subscriber : " + sub);
									
									
								}
						    expiryDateList.add(getExpiryDateFromContributors(ctx,
								    sub.getId(),
								 	input.getServiceID(),
								 	DiscountEventActivityServiceTypeEnum.SERVICE,
								 	-1
									));
						    }else
						    {
						    	if (LogSupport.isDebugEnabled(ctx)) {
									LogSupport.debug(
											ctx,
											MODULE,
											"getExpiryDateFromAllContributors : Adding expiry date to the list as current date as Subscriber is not found ");
									
									
								}
						    	expiryDateList.add(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
						    }
					   }
				}
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : fetching expiry date for the current discount event activity's service : " + serviceId
							+" and subscriber : " + subId);
					
					
				}
				expiryDateList.add(getExpiryDateFromContributors(ctx,
					     subId,
					     serviceId,
					 	DiscountEventActivityServiceTypeEnum.SERVICE,
					 	-1
						));
			   Collections.sort(expiryDateList);
			   if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getExpiryDateFromAllContributors : Expiration date for discount event activity of type SecondDevice discount is " + expiryDateList.get(expiryDateList.size()-1));
				}
			   return expiryDateList.get(expiryDateList.size()-1);
			}
				
			
			return CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
		} catch (HomeInternalException e) {
			
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(
						ctx,
						MODULE,
						"getExpiryDateFromAllContributors : Execption occured while calculating expiry date. So setting expirydate as current date");
			}
			return CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
			
		} catch (Exception e) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(
						ctx,
						MODULE,
						"getExpiryDateFromAllContributors : Execption occured while calculating expiry date. So setting expirydate as current date");
			}
			return CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
		}	    
     }


	public static void generateDiscountTransctionForAccount(Context ctx,
			String ban) throws HomeException {
		
		// fetch all the discount event activity row with status as active, cancellation pending
		Set<DiscountEventActivityStatusEnum> enumValues = new HashSet<DiscountEventActivityStatusEnum>();
		enumValues.add(DiscountEventActivityStatusEnum.ACTIVE);
		enumValues.add(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);
		
		ArrayList<DiscountEventActivity> discountEventList = (ArrayList<DiscountEventActivity>) filterDiscountEventActivitybyState(ctx, enumValues, ban);
		
		String subcriberID = null;
		Long discountGiven = (long) 0;
		
		
	}
	
	
	/**
	 * This method returns collection of DiscountEvent Beans for specified
	 * states
	 * 
	 * @param ctx
	 * @param state
	 * @return
	 * @throws HomeException
	 */
	public static Collection<DiscountEventActivity> filterDiscountEventActivitybyState(
			final Context ctx, final Collection<DiscountEventActivityStatusEnum> state,
			String ban) throws HomeException {
		final And condition = new And();
		if (state instanceof Set) {
			condition.add(new In(DiscountEventActivityXInfo.STATE, (Set) state));
		} else {
			condition.add(new In(DiscountEventActivityXInfo.STATE, new HashSet(state)));
		}
		condition.add(new EQ(DiscountEventActivityXInfo.BAN, ban));
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE, "filterDiscountEventActivitybyState : Discount event activities fetched for "
					+ state.toString());
		}

		ArrayList<DiscountEventActivity> discountEventList = (ArrayList<DiscountEventActivity>) HomeSupportHelper
				.get(ctx).getBeans(ctx, DiscountEventActivity.class, condition);
		Collections.sort(discountEventList, new DiscountEventActivityComparator());

		return discountEventList;
	}

	public static boolean isApplicableForDiscountTransaction(final Context context, final DiscountEventActivity discountEventActivity) {
		if (LogSupport.isDebugEnabled(context)) {
			LogSupport
			.debug(context,
					MODULE,
					"isApplicableForDiscountTransaction : Verfying if Discount Already given for discounEventActivity bean : "
							+ discountEventActivity.toString());
		}

		try{
			long billCycleDate = DiscountSupportImpl.getBillCycleDate(context,
					discountEventActivity.getBan());
			
			/**
			 * Discount event should not be null and should be Active
			 * Discount Till date should not be after Current Bill Cycle Start Date
			 */
			if ( null != discountEventActivity && 
					DiscountEventActivityStatusEnum.ACTIVE.equals(discountEventActivity.getState()) &&
					(discountEventActivity.getDiscountAppliedTillDate() == null || discountEventActivity.getDiscountAppliedTillDate().getTime() == 0
					|| billCycleDate > discountEventActivity.getDiscountAppliedTillDate().getTime())) {

				return true;
			}
		}catch(Exception ex){
			LogSupport
			.debug(context,
					MODULE,
					"isApplicableForDiscountTransaction : Unable to validate the discount event entity for discount transction, processing for the current row will be skipped: "
							+ discountEventActivity.toString());
		}

		return false;
	}
	
	public static boolean isApplicableForReverseDiscountTransaction(final Context context, final DiscountEventActivity discountEventActivity) {
		if (LogSupport.isDebugEnabled(context)) {
			LogSupport
			.debug(context,
					MODULE,
					"isApplicableForReverseDiscountTransaction : Verfying if Discount Event Activity is valid for reverse discount : "
							+ discountEventActivity.toString());
		}
		try{
			/**
			 * Discount event should not be null and should be Cancellation Pending
			 * Discount Expiration date should not be null or empty
			 */
			if ( null != discountEventActivity && 
					DiscountEventActivityStatusEnum.CANCELLATION_PENDING.equals(discountEventActivity.getState()) &&
					discountEventActivity.getDiscountExpirationDate() != null && 
					discountEventActivity.getDiscountExpirationDate().getTime() != 0) {
				return true;
			}
		}catch(Exception ex){
			LogSupport
			.debug(context,
					MODULE,
					"isApplicableForReverseDiscountTransaction : Unable to validate the discount event entity for reverse discount transction, processing for the current row will be skipped: "
							+ discountEventActivity.toString());
		}
		return false;
	}
	
	 public static Date getDiscountAppliedPeriodEndDate(final Context context,
			  final DiscountEventActivity discountEventActivity,final Account account, final Date chargeHistDate) throws HomeException
	  {
		  Date discountPeriodEndDate=null;
		  Date discountStartDate = CalendarSupportHelper.get(context).getStartOfMonth(discountEventActivity.getDiscountAppliedFromDate());
		  if(discountEventActivity.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.SERVICE))
		  {
		    Service service = ServiceSupport.getService(context, discountEventActivity.getServiceId());
		    SubscriberServices subService = SubscriberServicesSupport.getSubscriberServiceRecord(context, discountEventActivity.getSubId(), discountEventActivity.getServiceId(), SubscriberServicesUtil.DEFAULT_PATH);
			if(service != null)
			{
				if (LogSupport.isDebugEnabled(context)) {
					LogSupport.debug(
							context,
							MODULE,
							"getDiscountAppliedPeriodEndDate : Calculating the Discount Applied Till Date");
				}
				ServicePeriodEnum chargingScheme = service.getChargeScheme();
				if(chargingScheme.equals(ServicePeriodEnum.ONE_TIME))
				{
					
				}
				ServicePeriodHandler handler = ServicePeriodSupportHelper.get(context).getHandler(chargingScheme);
				if(handler == null)
				{
					throw new HomeInternalException("getDiscountAppliedPeriodEndDate : Could not update DiscountAppliedTillDate for Discount - " + discountEventActivity.getId() +
				          " because handler is not found for charging-period : " + chargingScheme.getDescription()) ;
				}
				discountPeriodEndDate=handler.calculateCycleEndDate(context, discountStartDate, 
				        account.getBillCycleDay(context), account.getSpid(),discountEventActivity.getSubId(),subService);
				if(discountPeriodEndDate!=null)
				{
					discountPeriodEndDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(discountPeriodEndDate);
				}
			}
		  }else if(discountEventActivity.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.AUXILIARY_SERVICE))
		  {
			  AuxiliaryService auxService = AuxiliaryServiceSupport.getAuxiliaryService(context,discountEventActivity.getServiceId());
			    if(auxService !=null)
			  {
			      SubscriberAuxiliaryService subAuxService = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServicesBySubIdAndSvcId(context,
                            discountEventActivity.getSubId(), discountEventActivity.getServiceId());
	
				  if (LogSupport.isDebugEnabled(context)) {
						LogSupport.debug(
								context,
								MODULE,
								"getDiscountAppliedPeriodEndDate : Calculating the Discount Applied Till Date");
					}
					ServicePeriodEnum chargingScheme = auxService.getChargingModeType();
					if(chargingScheme.equals(ServicePeriodEnum.ONE_TIME))
					{
						
					}
					ServicePeriodHandler handler = ServicePeriodSupportHelper.get(context).getHandler(chargingScheme);
					if(handler == null)
					{
						throw new HomeInternalException("getDiscountAppliedPeriodEndDate : Could not update DiscountAppliedTillDate for Discount - " + discountEventActivity.getId() +
					          " because handler is not found for charging-period : " + chargingScheme.getDescription()) ;
					}
					discountPeriodEndDate=handler.calculateCycleEndDate(context, discountStartDate, 
					        account.getBillCycleDay(context), account.getSpid(),discountEventActivity.getSubId(),subAuxService);
					if(discountPeriodEndDate!=null)
					{
						discountPeriodEndDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(discountPeriodEndDate);
					}
			  }
			  
		  }
			if (LogSupport.isDebugEnabled(context)) {
				LogSupport.debug(
						context,
						MODULE,
						"getDiscountAppliedPeriodEndDate : Discount Applied Till date is : " + discountPeriodEndDate);
			}

		 return discountPeriodEndDate;
	  }
	  
	  
	  
	  public static Date getDiscountAppliedPeriodStartDate(final Context context,
			  final DiscountEventActivity discountEventActivity,final Account account)
	  {
		 return getDiscountAppliedPeriodStartDate(context,discountEventActivity,account,null);
	  }	

	  public static Date getDiscountAppliedPeriodStartDate(final Context context,
			  final DiscountEventActivity discountEventActivity,final Account account,final Date chargeHistTime)
	  {
		  Date discountStartDate =null;
	      try
	      {
		  int currentBcd = account.getBillCycleDay(context);
		
		  Date currentMonthBillDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(InvoiceSupport
					.getCurrentBillingDate(currentBcd));
		  Date effectiveDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(discountEventActivity.getDiscountEffectiveDate());
			
		  if(null!=chargeHistTime)
		  {
				  Date chargeHistDateTime = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(chargeHistTime);
				   if(null!=discountEventActivity.getDiscountAppliedTillDate())
				  {
					  discountStartDate=CalendarSupportHelper.get(context).getDayAfter(discountEventActivity.getDiscountAppliedTillDate());
				  }else
				  {
					  if(CalendarSupportHelper.get(context).getStartOfMonth(chargeHistDateTime).getTime()
							  == CalendarSupportHelper.get(context).getStartOfMonth(effectiveDate).getTime())
							  
					  {
						  if(CalendarSupportHelper.get(context).getStartOfMonth(currentMonthBillDate).getTime()
								  > CalendarSupportHelper.get(context).getStartOfMonth(effectiveDate).getTime())
						  {
							  discountStartDate = currentMonthBillDate;
						  }else
						  {
							  discountStartDate = effectiveDate;
						  }
						  
					  }else if(CalendarSupportHelper.get(context).getStartOfMonth(chargeHistDateTime).getTime()
							  < CalendarSupportHelper.get(context).getStartOfMonth(effectiveDate).getTime())
					  {
						  discountStartDate=getStartDate(context,currentMonthBillDate,effectiveDate);
					  }
				  }
			 
		  }else
		  {
			  if(null!=discountEventActivity.getDiscountAppliedTillDate())
			  {
				  discountStartDate=CalendarSupportHelper.get(context).getDayAfter(discountEventActivity.getDiscountAppliedTillDate());
			  }else
			  {
			      discountStartDate=getStartDate(context,currentMonthBillDate,effectiveDate);
			  }
		  }
		  if (LogSupport.isDebugEnabled(context)) {
				LogSupport.debug(
						context,
						MODULE,
						"getDiscountAppliedPeriodStartDate : Discount Applied Start date is : " + discountStartDate);
			}
	      }catch(Exception e)
	      {
	    	  LogSupport.info(context,MODULE, "Exception occured while calculating Discount applied start date");
	    	  discountStartDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(new Date());
	      }

		  return discountStartDate;
	  }
	  
	  private static Date getStartDate(final Context context,final Date currentMonthBillDate,final Date effectiveDate)
	  {
		  Date discountStartDate =null;
		  try
		  {
		   
		   if (LogSupport.isDebugEnabled(context)) {
				LogSupport.debug(
						context,
						MODULE,
						"getStartDate : Calculating the Discount Start Date");
			}

		   if(currentMonthBillDate.before(effectiveDate))
		   {
			  discountStartDate = effectiveDate;   
		   }else
		   {
			   discountStartDate = currentMonthBillDate;
		   }
		   
		  }catch(Exception ex)
		  {
			  discountStartDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(new Date());
		  }
		  return discountStartDate;
	  }
	  
	  public static boolean isFeePersonalizedAvailableForSubscriptionAuxService(final Context ctx,final DiscountEventActivity discEventActivity)
		{
			try
			{
				And and = new And();
				and.add(new EQ(DiscountActivityTriggerXInfo.BAN,discEventActivity.getBan()));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_ACTIVITY_TYPE,DiscountActivityTypeEnum.AUX_SERVICE_FEE_PERSONALIZE_EVENT_INDEX));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_EVALUATION_STATUS,DiscountEvaluationStatusEnum.PENDING_INDEX));
				and.add(new EQ(DiscountActivityTriggerXInfo.SUBSCRIPTION_ID,discEventActivity.getSubId()));
				and.add(new EQ(DiscountActivityTriggerXInfo.SERVICE_ID,discEventActivity.getServiceId()));
				
				Home home = (Home) ctx.get(DiscountActivityTriggerHome.class);
				if(null!=home)
				{
					DiscountActivityTrigger discActivityTrigger= (DiscountActivityTrigger)home.find(ctx,and);
					if(null!=discActivityTrigger)
					{
						return true;
					}else
					{
						return false;
					}
				}
			}catch(Exception e)
			{
				LogSupport.info(ctx,MODULE,"Exception occured while fetching record for discountactivity trigger");
			}
			
			return true;
		}
	  
	  public static boolean isFeePersonalizedAvailableForSubscriptionService(final Context ctx,final DiscountEventActivity discEventActivity)
		{
			try
			{
				And and = new And();
				and.add(new EQ(DiscountActivityTriggerXInfo.BAN,discEventActivity.getBan()));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_ACTIVITY_TYPE,DiscountActivityTypeEnum.SERVICE_FEE_PERSONALIZE_EVENT_INDEX));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_EVALUATION_STATUS,DiscountEvaluationStatusEnum.PENDING_INDEX));
				and.add(new EQ(DiscountActivityTriggerXInfo.SUBSCRIPTION_ID,discEventActivity.getSubId()));
				and.add(new EQ(DiscountActivityTriggerXInfo.SERVICE_ID,discEventActivity.getServiceId()));
				
				Home home = (Home) ctx.get(DiscountActivityTriggerHome.class);
				if(null!=home)
				{
					DiscountActivityTrigger discActivityTrigger= (DiscountActivityTrigger)home.find(ctx,and);
					if(null!=discActivityTrigger)
					{
						return true;
					}else
					{
						return false;
					}
				}
			}catch(Exception e)
			{
				LogSupport.info(ctx,MODULE,"Exception occured while fetching record for discountactivity trigger");
			}
			
			return true;
		}
	  
	  public static void saveDiscountActivityTriggerForServiceFee(final Context ctx,final String banId)
		{
			try
			{
				And and = new And();
				and.add(new EQ(DiscountActivityTriggerXInfo.BAN,banId));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_ACTIVITY_TYPE,DiscountActivityTypeEnum.SERVICE_FEE_PERSONALIZE_EVENT_INDEX));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_EVALUATION_STATUS,DiscountEvaluationStatusEnum.PENDING_INDEX));
								
				Home home = (Home) ctx.get(DiscountActivityTriggerHome.class);
				if(null!=home)
				{
					DiscountActivityTrigger discActivityTrigger= (DiscountActivityTrigger)home.find(ctx,and);
					if(null!=discActivityTrigger)
					{
						discActivityTrigger.setDiscountEvaluationStatus(DiscountEvaluationStatusEnum.PROCESSED);
						home.store(discActivityTrigger);
					}else
					{
						LogSupport.info(ctx,MODULE,"No entry present for Service Fee Personalization trigger");
					}
				}
			}catch(Exception e)
			{
				LogSupport.info(ctx,MODULE,"Exception occured while fetching record for discountactivity trigger");
			}
			
			
		}
	  
	  public static void saveDiscountActivityTriggerForAuxServiceFee(final Context ctx,final String banId)
		{
			try
			{
				And and = new And();
				and.add(new EQ(DiscountActivityTriggerXInfo.BAN,banId));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_ACTIVITY_TYPE,DiscountActivityTypeEnum.AUX_SERVICE_FEE_PERSONALIZE_EVENT_INDEX));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_EVALUATION_STATUS,DiscountEvaluationStatusEnum.PENDING_INDEX));
								
				Home home = (Home) ctx.get(DiscountActivityTriggerHome.class);
				if(null!=home)
				{
					DiscountActivityTrigger discActivityTrigger= (DiscountActivityTrigger)home.find(ctx,and);
					if(null!=discActivityTrigger)
					{
						discActivityTrigger.setDiscountEvaluationStatus(DiscountEvaluationStatusEnum.PROCESSED);
						home.store(discActivityTrigger);
					}else
					{
						LogSupport.info(ctx,MODULE,"No entry present for Aux Service Fee Personalization trigger");
					}
				}
			}catch(Exception e)
			{
				LogSupport.info(ctx,MODULE,"Exception occured while fetching record for discountactivity trigger");
			}
			
			
		}
		
}
