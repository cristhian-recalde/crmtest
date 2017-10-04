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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.DiscountEventActivity;
import com.trilogy.app.crm.bean.DiscountEventActivityServiceTypeEnum;
import com.trilogy.app.crm.bean.DiscountEventActivityStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventActivityTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleEngineUtility;
import com.trilogy.app.crm.core.ruleengine.DiscountApplicationCriteriaEnum;
import com.trilogy.app.crm.core.ruleengine.DiscountCriteriaTypeEnum;
import com.trilogy.app.crm.core.ruleengine.EventTypeEnum;
import com.trilogy.app.crm.core.ruleengine.MasterPackDiscountCriteriaHolder;
import com.trilogy.app.crm.core.ruleengine.MasterPackDiscountOutputHolder;
import com.trilogy.app.crm.core.ruleengine.engine.exception.NoRuleFoundException;
import com.trilogy.app.crm.core.ruleengine.engine.ifc.BusinessRuleIfc;
import com.trilogy.app.crm.core.ruleengine.util.RuleEngineConstants;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.DiscountSupportImpl;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Discount handler for the Master pack discounts
 * @author abhijit.mokashi
 *
 */
public class MasterPackDiscountHandler implements DiscountHandler{

	// Map to hold the input data to be passed to discount rule engine
	private Map<Integer,Object> prameterMap_ = new HashMap<Integer, Object>();
	public static String MODULE = MasterPackDiscountHandler.class.getName();
	
	@Override
	public boolean init(final Context context, Account account,List<SubscriberIfc> subscriberList) {
		try{
			
			 List<AbstractBean> list = new ArrayList<AbstractBean>();
		        MasterPackDiscountCriteriaHolder holder;
		        for (SubscriberIfc subscriber : subscriberList)
		        {     
		        	/*Subscriber subObj = (Subscriber)subscriber;
		        	Collection<SubscriberAuxiliaryService> associationList=SubscriberAuxiliaryServiceSupport.getActiveSubscriberAuxiliaryServices (context,subObj,new Date());
		        */	
		        	holder = new MasterPackDiscountCriteriaHolder();
		        	holder.setSubscriptionType(subscriber.getSubscriptionType());
		        	holder.setDunningLevelSubscription(subscriber.getLastDunningLevel());
		        	holder.setSubscriptionState(subscriber.getState());
		        	holder.setPricePlanID(subscriber.getPricePlan());
		        	list.add(holder);
		        	/*if(null!=associationList && !associationList.isEmpty())
		        	{
		        		for(SubscriberAuxiliaryService association:associationList)
		        		{
				        	holder = new MasterPackDiscountCriteriaHolder();
				        	holder.setSubscriptionType(subscriber.getSubscriptionType());
				        	holder.setDunningLevelSubscription(subscriber.getLastDunningLevel());
				        	holder.setSubscriptionState(subscriber.getState());
		
				        //	holder.setAuxiliaryServiceID(association.getAuxiliaryServiceIdentifier());
				        	list.add(holder);
		        		}
		        	}*/
		        	
		        }
			prameterMap_.put(RuleEngineConstants.DISCOUNT_TYPE, DiscountCriteriaTypeEnum.MASTER_PACK);
			prameterMap_.put(RuleEngineConstants.SUBSCRIBER_EVENTS, list);
		} catch (Exception e){
			new MinorLogMsg(this, "Failed to initialize the master pack discount handler for account '");
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
			// hit the discount rule engine to find applicable rule if any
			List<BusinessRuleIfc> outputList = BusinessRuleEngineUtility.evaluateAllRule(context,EventTypeEnum.DISCOUNT,account, subscriberList, prameterMap_);
			List<Long> associationIdList = new ArrayList<Long>();
			Date effectiveDate = null;
			DiscountEventActivity discountEventActivity=null;
			List<Long> pricePlanIdList = new ArrayList<Long>();
			if(null!=outputList && !outputList.isEmpty())
			{
				for(BusinessRuleIfc outputRule : outputList){
					
					getPricePlanIdListFromInput(context,pricePlanIdList,outputRule.getMasterDiscountInput());
					for(MasterPackDiscountOutputHolder output : outputRule.getMasterPackDiscountOutput())
					{
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									"Starting Processing of MasterPack Discount assignment for Rule ID "
									+ outputRule.getRuleId());
						}
					    if(output.getDiscountApplicationCriteria().equals(DiscountApplicationCriteriaEnum.AGE_ON_NETWORK))
						{
                          List<SubscriberIfc> subPriorityList = DiscountActivityUtils.getSubscriptionByPriority(context,
                        		  subscriberList, output.getSubscriptionType(), -1);
                          SubscriberIfc sub = getSubscriberAuxInstance(context,subPriorityList,outputRule,output,associationIdList);
                          if(null!=sub)
                          {
                        	 Collection<SubscriberAuxiliaryService> subAuxCollection = SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(context, sub.getId());
                        	 List<SubscriberAuxiliaryService> subAuxList = new ArrayList<SubscriberAuxiliaryService>(subAuxCollection);
                        	 Collections.sort(subAuxList, new Comparator<SubscriberAuxiliaryService>() {

                     			@Override
                     			public int compare(SubscriberAuxiliaryService o1, SubscriberAuxiliaryService o2) {
                     				if(o1.getSecondaryIdentifier()<o2.getSecondaryIdentifier())
                     				{
                     					return -1;
                     				}else if(o1.getSecondaryIdentifier() > o2.getSecondaryIdentifier())
                     				{
                     					return 1;
                     				}else
                     				{
                     					return 0;
                     				}
                     				
                     			}
                        	 });
                        	 if (LogSupport.isDebugEnabled(context)) {
									LogSupport.debug(
											context,
											MODULE,
											"Ordered Aux service instance as per Rule's output " + subAuxList);
							  }	
                        	 SubscriberAuxiliaryService subAux =subAuxList.get(0);
                        	  if(null!=existingDiscountActivities && !existingDiscountActivities.isEmpty())
						    	{
							    	 discountEventActivity = getDiscountEventActivityAvailable(context,
							    			existingDiscountActivities,
							    			output,
							    			outputRule,
							    			sub,
							    			subAux,
							    			pricePlanIdList);
							    	if(null!=discountEventActivity)
							    	{
							    		if(!associationIdList.contains(subAux.getIdentifier()))
							    		{
							    			if (LogSupport.isDebugEnabled(context)) {
												LogSupport.debug(
														context,
														MODULE,
														"Discount given to aux service single instance is : " + subAux.getIdentifier());
											}	
							    		  associationIdList.add(subAux.getIdentifier());	
							    		}
							    		if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"Discount Event Activity for Masterpack Age On Network exists for the current criteria");
										}
							    		if(DiscountActivityUtils.checkIfRuleVersionChange(context, discountEventActivity, outputRule))
										{
											discountEventActivity.setDiscountRuleVersion(outputRule.getRuleVersion(context));
											discountEventActivityForUpdate.add(discountEventActivity);
										}else
										{
											discountEventActivityContinued.add(discountEventActivity);
										}
							    	}else 
							    	{
							    		effectiveDate = getEffectiveDate(context,subAux,account,sub,
							    				getEffectiveDateFromContributors(context,account,subscriberList,outputRule.getMasterDiscountInput(),output));
							    		discountEventActivity=DiscountActivityUtils.createDiscounteventActivity(context,
							    				existingDiscountActivities, 
							    				account,
							    				sub.getId(), 
							    				output.getAuxiliaryServiceID(),
							    				DiscountEventActivityServiceTypeEnum.AUXILIARY_SERVICE,
							    				sub.getPricePlan(),
							    				-1,
							    				DiscountEventActivityTypeEnum.MASTER_PACK_DISCOUNT,
							    				outputRule.getRuleId(),
							    				outputRule.getRuleVersion(context),
							    				effectiveDate,
							    				output.getDiscountClass(),
							    				subAux.getSecondaryIdentifier(),
							    				-1);
							    		if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"Creating New Discount Event Activity for MasterPack Age On Network and effective date for event activity is :"
													+ effectiveDate);
										}
							    		validateAndFilterDiscount(context, 
												existingDiscountActivities, 
												discountEventActivity, 
												discountEventActivityForCreation, 
												discountEventActivityForUpdate,
												discountEventActivityContinued);
							    		discountEventActivityForCreation.add(discountEventActivity);
							    		if(!associationIdList.contains(subAux.getIdentifier()))
							    		{
							    			if (LogSupport.isDebugEnabled(context)) {
												LogSupport.debug(
														context,
														MODULE,
														"Discount given to aux service single instance is : " + subAux.getIdentifier());
											}	
							    		  associationIdList.add(subAux.getIdentifier());	
							    		}
							    		
							    	}
						    	}else 
						    	{
						    		//create new Row
						    		effectiveDate = getEffectiveDate(context,subAux,account,sub,
						    				getEffectiveDateFromContributors(context,account,subscriberList,outputRule.getMasterDiscountInput(),output));
						    		discountEventActivity=DiscountActivityUtils.createDiscounteventActivity(context,
						    				existingDiscountActivities, 
						    				account,
						    				sub.getId(), 
						    				output.getAuxiliaryServiceID(),
						    				DiscountEventActivityServiceTypeEnum.AUXILIARY_SERVICE,
						    				sub.getPricePlan(),
						    				-1,
						    				DiscountEventActivityTypeEnum.MASTER_PACK_DISCOUNT,
						    				outputRule.getRuleId(),
						    				outputRule.getRuleVersion(context),
						    				effectiveDate,
						    				output.getDiscountClass(),
						    				subAux.getSecondaryIdentifier(),
						    				-1);
						    		if (LogSupport.isDebugEnabled(context)) {
										LogSupport.debug(
												context,
												MODULE,
												"Creating First Discount Event Activity for Masterpack Age On Network and effective date for event activity is :"
												+ effectiveDate);
									}
						    		
						    		validateAndFilterDiscount(context, 
											existingDiscountActivities, 
											discountEventActivity, 
											discountEventActivityForCreation, 
											discountEventActivityForUpdate,
											discountEventActivityContinued);
											
						    		
						    		if(!associationIdList.contains(subAux.getIdentifier()))
						    		{
						    			if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"Discount given to aux service single instance is : " + subAux.getIdentifier());
										}		
						    		  associationIdList.add(subAux.getIdentifier());	
						    		}
						    		discountEventActivityForCreation.add(discountEventActivity);
						    	}
                          }else
                          {
                        	  new MinorLogMsg(this, "No Subscriber found for the given rule : " + outputRule.getRuleId());
                          }
							
						}else
						{
							for(SubscriberIfc sub:subscriberList)
							{
								List<SubscriberAuxiliaryService> listOfAuxService =getSubscriberAuxServiceList(context,sub,output);	                                                          
								    
								    if(null!=listOfAuxService && !listOfAuxService.isEmpty())
								    {    
								    	 
								    	 for(SubscriberAuxiliaryService subAux : listOfAuxService)
										  {
										    if(!associationIdList.contains(subAux.getIdentifier()) || outputRule.getMultipleDiscount())
										    {
										    	if(null!=existingDiscountActivities && !existingDiscountActivities.isEmpty())
										    	{
											    	 discountEventActivity = getDiscountEventActivityAvailable(context,
											    			existingDiscountActivities,
											    			output,
											    			outputRule,
											    			sub,
											    			subAux,
											    			pricePlanIdList);
											    	if(null!=discountEventActivity)
											    	{
											    		if (LogSupport.isDebugEnabled(context)) {
															LogSupport.debug(
																	context,
																	MODULE,
																	"Discount Event Activity for MasterPack All Subscription criteria exits for the subscriber :"
																	+ sub.getId());
														}
											    		if(!associationIdList.contains(subAux.getAuxiliaryServiceIdentifier()))
											    		{
											    			if (LogSupport.isDebugEnabled(context)) {
																LogSupport.debug(
																		context,
																		MODULE,
																		"Discount given to aux service single instance is : " + subAux.getIdentifier());
															}	
											    		  associationIdList.add(subAux.getIdentifier());	
											    		}
											    	
											    		if(DiscountActivityUtils.checkIfRuleVersionChange(context, discountEventActivity, outputRule))
														{
															discountEventActivity.setDiscountRuleVersion(outputRule.getRuleVersion(context));
															discountEventActivityForUpdate.add(discountEventActivity);
														}else
														{
															discountEventActivityContinued.add(discountEventActivity);
														}
											    	}else if(subAux.getAuxiliaryServiceIdentifier() == output.getAuxiliaryServiceID() 
															&& sub.getSubscriptionType() == output.getSubscriptionType()
															&& !sub.getState().equals(SubscriberStateEnum.SUSPENDED))
											    	{
											    		effectiveDate = getEffectiveDate(context,subAux,account,sub
											    				           ,getEffectiveDateFromContributors(context,account,subscriberList,outputRule.getMasterDiscountInput(),output));
											    		discountEventActivity=DiscountActivityUtils.createDiscounteventActivity(context,
											    				existingDiscountActivities, 
											    				account,
											    				sub.getId(), 
											    				subAux.getAuxiliaryServiceIdentifier(),
											    				DiscountEventActivityServiceTypeEnum.AUXILIARY_SERVICE,
											    				sub.getPricePlan(),
											    				-1,
											    				DiscountEventActivityTypeEnum.MASTER_PACK_DISCOUNT,
											    				outputRule.getRuleId(),
											    				outputRule.getRuleVersion(context),
											    				effectiveDate,
											    				output.getDiscountClass(),
											    				subAux.getSecondaryIdentifier(),
											    				-1);
											    		if (LogSupport.isDebugEnabled(context)) {
															LogSupport.debug(
																	context,
																	MODULE,
																	"Creating new Discount Event Activity for MasterPack All Subscription criteria for subscriber :"
																	+ sub.getId() + " And effective Date is : " +effectiveDate);
														}
											    		if(!associationIdList.contains(subAux.getAuxiliaryServiceIdentifier()))
											    		{
											    			if (LogSupport.isDebugEnabled(context)) {
																LogSupport.debug(
																		context,
																		MODULE,
																		"Discount given to aux service single instance is : " + subAux.getIdentifier());
															}		
											    		  associationIdList.add(subAux.getIdentifier());	
											    		}
											    		validateAndFilterDiscount(context, 
																existingDiscountActivities, 
																discountEventActivity, 
																discountEventActivityForCreation, 
																discountEventActivityForUpdate,
																discountEventActivityContinued);
																
											    		
											    	    discountEventActivityForCreation.add(discountEventActivity);
											    	}
										    	}else if(subAux.getAuxiliaryServiceIdentifier() == output.getAuxiliaryServiceID() 
														&& sub.getSubscriptionType() == output.getSubscriptionType()
														&& !sub.getState().equals(SubscriberStateEnum.SUSPENDED))
										    	{
										    		//create new Row
										    		effectiveDate = getEffectiveDate(context,subAux,account,sub,
										    				getEffectiveDateFromContributors(context,account,subscriberList,outputRule.getMasterDiscountInput(),output));
										    		discountEventActivity=DiscountActivityUtils.createDiscounteventActivity(context,
										    				existingDiscountActivities, 
										    				account,
										    				sub.getId(), 
										    				subAux.getAuxiliaryServiceIdentifier(),
										    				DiscountEventActivityServiceTypeEnum.AUXILIARY_SERVICE,
										    				sub.getPricePlan(),
										    				-1,
										    				DiscountEventActivityTypeEnum.MASTER_PACK_DISCOUNT,
										    				outputRule.getRuleId(),
										    				outputRule.getRuleVersion(context),
										    				effectiveDate,
										    				output.getDiscountClass(),
										    				subAux.getSecondaryIdentifier(),
										    				-1);
										    		if (LogSupport.isDebugEnabled(context)) {
														LogSupport.debug(
																context,
																MODULE,
																"Creating First Discount Event Activity for MasterPack All Subscription criteria for subscriber :"
																+ sub.getId() + " And effective Date is : " +effectiveDate);
													}
										    		if(!associationIdList.contains(subAux.getAuxiliaryServiceIdentifier()))
										    		{
										    			if (LogSupport.isDebugEnabled(context)) {
															LogSupport.debug(
																	context,
																	MODULE,
																	"Discount given to aux service single instance is : " + subAux.getIdentifier());
														}	
										    		  associationIdList.add(subAux.getIdentifier());	
										    		}
										    		validateAndFilterDiscount(context, 
															existingDiscountActivities, 
															discountEventActivity, 
															discountEventActivityForCreation, 
															discountEventActivityForUpdate,
															discountEventActivityContinued);
										    		discountEventActivityForCreation.add(discountEventActivity);
										    	}
										     }
										  }
								    	 
								    }
			     			}

						}
					}
					if (LogSupport.isDebugEnabled(context)) {
						LogSupport.debug(
								context,
								MODULE,
								"Clearing input price plan id list");
					}	
					pricePlanIdList.clear();
				}
				
			}else
			{
				new MinorLogMsg(this, "No Discount Rule for Master Pack matched for the BAN:'" + account.getBAN());
			}
		
				}catch(NoRuleFoundException ex){
			new DebugLogMsg(this, "No Discount Rule for Master Pack matched for the BAN:'" + account.getBAN()).log(context);
		}
		return false;
	}

	@Override
	public boolean generateTransactions(final Context context, final Account account,
			 final List<DiscountEventActivity> discountActivityEventList,
    		 List<DiscountEventActivity> discountEventToBeUpdated) 
	{
		for(DiscountEventActivity discountEventActivity : discountActivityEventList){
			if(discountEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.MASTER_PACK_DISCOUNT))
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
						Transaction discountTransaction = generateMasterPackDiscountTransaction(context, discountEventActivity);
						if(null!=discountTransaction)
						{
							/*discountEventActivity.setDiscountAppliedFromDate(DiscountActivityUtils.getDiscountAppliedPeriodStartDate(context,discountEventActivity,account));
							discountEventActivity.setDiscountAppliedTillDate(DiscountActivityUtils.getDiscountAppliedPeriodEndDate(context,discountEventActivity,account));
					*/		
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
						Transaction reverseDiscountTransaction = generateMasterPackReverseDiscountTransaction(context, discountEventActivity);
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
	private Transaction generateMasterPackDiscountTransaction(Context ctx,
			DiscountEventActivity discEventActivity)
					throws HomeException {
		
		
		Transaction discountTransaction =  DiscountSupportImpl.generateAuxServiceDiscountTransactions(ctx, discEventActivity);
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
	private Transaction generateMasterPackReverseDiscountTransaction(Context ctx,
			DiscountEventActivity discEventActivity)
					throws HomeException {
		Transaction discountTransaction =  DiscountSupportImpl.generateReverseDiscountTransactionsForAux(ctx, discEventActivity);
		return discountTransaction;
		
	}
	
	private List<SubscriberAuxiliaryService> getSubscriberAuxServiceList(final Context context,final SubscriberIfc sub,final MasterPackDiscountOutputHolder output)
	{
		Subscriber subObj = (Subscriber)sub;
		List<SubscriberAuxiliaryService> listOfSubAux = new ArrayList<SubscriberAuxiliaryService>();
		try {
			Collection<SubscriberAuxiliaryService> auxServiceCollection =SubscriberAuxiliaryServiceSupport.
					                                                        getActiveSubscriberAuxiliaryServices(context, subObj, new Date());
			java.util.Iterator<SubscriberAuxiliaryService> iterator =  auxServiceCollection.iterator();
			
			while(iterator.hasNext())
			{
				SubscriberAuxiliaryService subAux = (SubscriberAuxiliaryService)iterator.next();
				if(output.getAuxiliaryServiceID() == subAux.getAuxiliaryServiceIdentifier())
				{
					listOfSubAux.add(subAux);
					if (LogSupport.isDebugEnabled(context)) {
						LogSupport.debug(
								context,
								MODULE,
								"Adding Aux service to the list matching with the output of rule " + subAux.getIdentifier());
					}
				}
			}
			return listOfSubAux;
		} catch (HomeException e) {
			new MinorLogMsg(this, "Exception while finding Association for Aux service with Subscriber");
			return null;
		}
		
	}
	
	private DiscountEventActivity getDiscountEventActivityAvailable(final Context context,final Collection<DiscountEventActivity> discountEventsActivity,
			final MasterPackDiscountOutputHolder object,final BusinessRuleIfc output,SubscriberIfc subscriber, final SubscriberAuxiliaryService subAux
			,final List<Long> pricePlanIdList)
	{
		DiscountEventActivity discountEventActivityAvailable = null;
		java.util.Iterator<DiscountEventActivity> iterator = discountEventsActivity.iterator();
		while(iterator.hasNext())
		{
			DiscountEventActivity discEventActivity = iterator.next();
			if(discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.MASTER_PACK_DISCOUNT))
			{
				if(discEventActivity.getServiceId()==object.getAuxiliaryServiceID() 
						&& discEventActivity.getDiscountRuleId().equals(output.getRuleId())
						&& discEventActivity.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.AUXILIARY_SERVICE)
						&& discEventActivity.getSubId().equals(subscriber.getId())
						&& discEventActivity.getState().equals(DiscountEventActivityStatusEnum.ACTIVE)
						&& !subscriber.getState().equals(SubscriberStateEnum.SUSPENDED)
						&& discEventActivity.getDiscountClass() == object.getDiscountClass()
						&& discEventActivity.getServiceInstance() == subAux.getSecondaryIdentifier()
						&& pricePlanIdList.contains(subscriber.getPricePlan()))
				    {
					if(DiscountActivityUtils.isFeePersonalizedAvailableForSubscriptionAuxService(context,discEventActivity))
					{
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									"getDiscountEventActivityAvailable : Fee Personalization  : Found matched discount event for Fee Personalization MasterPack discount "
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
										"Found matched discount event for MasterPack discount "
										+ discEventActivity.getId());
							}
						  break;
						}
				    }
			}
		}
		return discountEventActivityAvailable;
	}
	
	private SubscriberIfc getSubscriberAuxInstance(final Context context,
			final List<SubscriberIfc> subList,
			final BusinessRuleIfc outputRule,
			final MasterPackDiscountOutputHolder output,
			final List<Long> subAuxMappingId)
	{
		SubscriberIfc subscriber = null;
		for(SubscriberIfc sub : subList)
		{
			
			List<SubscriberAuxiliaryService> listOfAuxService =getSubscriberAuxServiceList(context,sub,output);	
			if(null!=listOfAuxService && !listOfAuxService.isEmpty())
			{
				for(SubscriberAuxiliaryService subAux : listOfAuxService)
				{
					if(subAux.getAuxiliaryServiceIdentifier() == output.getAuxiliaryServiceID()&& 
							(outputRule.getMultipleDiscount()|| !subAuxMappingId.contains(subAux.getIdentifier())))
					{
						subscriber = sub;
						return subscriber;
					}
							
				}
			}
			
		}
	 return null;	
	}
	
	private  Date getEffectiveDate(final Context ctx
			,final SubscriberAuxiliaryService subscriberAuxService
			,final Account account
			,final SubscriberIfc sub
			,final Date effectiveDatefromContribuitors)
	{
		/*
		 * Check here is to compare date of service's bill start date with
		 * respect to discount 1. If Service is provisioned two months back then
		 * discount effective date should be current date(it task runs daily) 2.
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
				.getDateWithNoTimeOfDay(subscriberAuxService.getStartDate())
				.before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
						new Date()))) {
			Date currentBCD;
			
			try {
				currentBCD = InvoiceSupport.getCurrentBillingDate(account
						.getBillCycleDay(ctx));

				if (CalendarSupportHelper
						.get(ctx)
						.getDateWithNoTimeOfDay(
								subscriberAuxService.getStartDate())
						.before(CalendarSupportHelper.get(ctx)
								.getDateWithNoTimeOfDay(currentBCD))) {
					effectiveDate=CalendarSupportHelper.get(ctx)
							.getDateWithNoTimeOfDay(currentBCD);
					
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,"getEffectiveDate : Effective date is current BCD"
										+ effectiveDate);
				    }
				} else if (((CalendarSupportHelper
						.get(ctx)
						.getDateWithNoTimeOfDay(
								subscriberAuxService.getStartDate())
						.after(CalendarSupportHelper.get(ctx)
								.getDateWithNoTimeOfDay(currentBCD)))|| 
								(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
										subscriberAuxService.getStartDate()).getTime()== CalendarSupportHelper.
										get(ctx).getDateWithNoTimeOfDay(currentBCD).getTime()))
						&& CalendarSupportHelper
								.get(ctx)
								.getDateWithNoTimeOfDay(
										subscriberAuxService.getStartDate())
								.before(CalendarSupportHelper.get(ctx)
										.getDateWithNoTimeOfDay(new Date()))){
					effectiveDate=CalendarSupportHelper
									.get(ctx).getDateWithNoTimeOfDay(
											subscriberAuxService
													.getStartDate());
					
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,"getEffectiveDate : Effective date is Service's bill start date"
										+ effectiveDate);
				    }
				} else {
					effectiveDate=CalendarSupportHelper
									.get(ctx)
									.getDateWithNoTimeOfDay(new Date());
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,"getEffectiveDate : Effective date is Current date"
										+ effectiveDate);
				    }
				}
			} catch (HomeException e) {
                 LogSupport.minor(ctx,MODULE,"Exception occured while calculating effective date for Subscriber : " + sub);
                 effectiveDate=CalendarSupportHelper
							.get(ctx)
							.getDateWithNoTimeOfDay(new Date());
			}
		} else {
			effectiveDate=CalendarSupportHelper.get(ctx)
							.getDateWithNoTimeOfDay(
									subscriberAuxService.getStartDate());
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(
						ctx,
						MODULE,"getEffectiveDate : Effective date is Bill start date as service is future dated"
								+ effectiveDate);
		    }

		}
		if(null!=ctx.get(DiscountClassContextAgent.RESUMETRIGGER) && ctx.getBoolean(DiscountClassContextAgent.RESUMETRIGGER))
		{
			try {
				AuxiliaryService auxServiceObj = AuxiliaryServiceSupport.getAuxiliaryService(ctx, subscriberAuxService.getAuxiliaryServiceIdentifier());
				Subscriber subObj = (Subscriber)sub;
				if(null!=auxServiceObj && null!=subObj && null!=subObj.getResumedDate())
				{
					if(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subObj.getResumedDate())
							.after(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiveDate)))
					{
						effectiveDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subObj.getResumedDate());
					}
				}
		    } catch (HomeException e) {
			LogSupport.minor(ctx,DiscountActivityUtils.class, "Unable to retrive service from service id" + subscriberAuxService.getAuxiliaryServiceIdentifier());
		   }
		}
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(
					ctx,
					MODULE,"Effective date from input of rule is"
							+ effectiveDatefromContribuitors);
	    }
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(
					ctx,
					MODULE,"Effective date from output of rule is"
							+ effectiveDate);
	    }
		if(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiveDatefromContribuitors).before(effectiveDate))
		{
			return effectiveDate;
		}else
		{
			return CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiveDatefromContribuitors);
		}
		
		
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
				"Start validating Master Discount event activity created with existing snapshot");
		/*// need to check if the new version of rule applied is present
		DiscountActivityUtils.checkRuleVersionChange(context,
				discountEventActivity,
				existingDiscountActivities,
				discountEventActivityForCreation,
				discountEventActivityForUpdate);*/
		checkForApplicableExpiredMasterPackDiscount(context,
				discountEventActivity,
				existingDiscountActivities,
				discountEventActivityForCreation,
				discountEventActivityForUpdate);
		checkForPriceplanChangeForExistingDiscountedSub(context,
				discountEventActivity,
				existingDiscountActivities,
				discountEventActivityForCreation,
				discountEventActivityForUpdate);
		
		if(DiscountActivityUtils.isFeePersonalizedAvailableForSubscriptionAuxService(context, discountEventActivity))
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
								&& currDisc.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.AUXILIARY_SERVICE)
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
							sub.getPricePlan() != currDisc.getPricePlanId()&&
							currDisc.getServiceInstance() == discountEventActivity.getServiceInstance()
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
	
	private void checkForApplicableExpiredMasterPackDiscount(Context context,
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
					  //add check for secondary identifier
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
									 "Got old Masterpack Discount event activity for ban" + currDisc.getBan()
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
			   ,final List<MasterPackDiscountCriteriaHolder> inputObject
			   ,final MasterPackDiscountOutputHolder outputObject)
	   {
		   Map<String,Long> subServiceMap = new HashMap<String, Long>();
		   Iterator<MasterPackDiscountCriteriaHolder> iterator = inputObject.iterator();
		   Date effectiveDate = null;
		   while(iterator.hasNext())
		   {
			   MasterPackDiscountCriteriaHolder input = iterator.next();
			  SubscriberIfc sub = getSubscriberForMasterInput(ctx,input.getSubscriptionType(),
					  input.getPricePlanID(),subscriberList,-1);  
			  Collection<Long> subServiceId = SubscriberServicesSupport.getMandatoryServicesId(ctx, sub.getId());
			  if(null!=subServiceId && !subServiceId.isEmpty())
			  {
				  if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,"Filling the map for calculating the effective date"
										+ "Aux Service Id :" + (Long)new ArrayList(subServiceId).get(0)
										+ "Subscriber Id : " + sub.getId());
				    } 	  
			   subServiceMap.put(sub.getId(),(Long)new ArrayList(subServiceId).get(0));
			  }
		   }
		   effectiveDate = DiscountActivityUtils.getLatestEffectiveDate(subServiceMap,ctx,account);
		   return effectiveDate;
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
		public static SubscriberIfc getOldestSubscriberForMasterInput(final Context ctx,
				final long subScriptionType, final long pricePlanId,
				List<SubscriberIfc> subScriberList,
				final long serviceId) {

			Iterator<SubscriberIfc> itr = subScriberList.iterator();
			List<SubscriberIfc> subList = new ArrayList<SubscriberIfc>();
			while (itr.hasNext()) {
				SubscriberIfc sub = itr.next();
				if (sub.getSubscriptionType() == subScriptionType
						&& sub.getPricePlan() == pricePlanId) {
					
						subList.add(sub);
					
				}
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
			/*		if (subFirst.getStartDate() != null
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
						MODULE,"Fetching Oldest Subscriber for price plan" + pricePlanId
						+"and subscriber is : " + subList.get(0) );
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
		public static SubscriberIfc getSubscriberForMasterInput(final Context ctx,
				final long subScriptionType, final long pricePlanId,
				List<SubscriberIfc> subScriberList,
				final long serviceId) {

			Iterator<SubscriberIfc> itr = subScriberList.iterator();
			List<SubscriberIfc> subList = new ArrayList<SubscriberIfc>();
			while (itr.hasNext()) {
				SubscriberIfc sub = itr.next();
				if (sub.getSubscriptionType() == subScriptionType
						&& sub.getPricePlan() == pricePlanId
						&& !sub.getState().equals(SubscriberStateEnum.SUSPENDED)) {
					
						subList.add(sub);
					
				}
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
			/*		if (subFirst.getStartDate() != null
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
						MODULE,"Fetching Oldest Subscriber for price plan" + pricePlanId
						+"and subscriber is : " + subList.get(0) );
		    } 
			return subList.get(0);
		}	
		
    private void getPricePlanIdListFromInput(final Context ctx,List<Long> pricePlanIdList,List<MasterPackDiscountCriteriaHolder> listOfInput)
    {
    	for(MasterPackDiscountCriteriaHolder input : listOfInput)
    	{
    		if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(
						ctx,
						MODULE,
						"Adding price plan id to the list from input "
						);
			}
    		pricePlanIdList.add(input.getPricePlanID());
    	}
    }
}
