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
import com.trilogy.app.crm.core.ruleengine.CombinationDiscountCriteriaHolder;
import com.trilogy.app.crm.core.ruleengine.CombinationDiscountOutputHolder;
import com.trilogy.app.crm.core.ruleengine.DiscountApplicationCriteriaEnum;
import com.trilogy.app.crm.core.ruleengine.DiscountCriteriaTypeEnum;
import com.trilogy.app.crm.core.ruleengine.DiscountPriority;
import com.trilogy.app.crm.core.ruleengine.DiscountPriorityTypeEnum;
import com.trilogy.app.crm.core.ruleengine.EventTypeEnum;
import com.trilogy.app.crm.core.ruleengine.RuleInfoConfig;
import com.trilogy.app.crm.core.ruleengine.engine.exception.NoRuleFoundException;
import com.trilogy.app.crm.core.ruleengine.engine.ifc.BusinessRuleIfc;
import com.trilogy.app.crm.core.ruleengine.util.RuleEngineConstants;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.DiscountSupportImpl;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class CombinationDiscountHandler implements DiscountHandler {

	//Taking Static Instance as we don't need to pass anything for now
	private Map<Integer,Object> prameterMap_ = new HashMap<Integer, Object>();
	public static String MODULE = CombinationDiscountHandler.class.getName();
	@Override
	public boolean init(Context context, Account account,
			List<SubscriberIfc> subscriberList) {
		
         try{
			// checking for Cross Subscription (without contract) Discount
			List<AbstractBean> list = new ArrayList<AbstractBean>();
	        CombinationDiscountCriteriaHolder holder;
	        for (SubscriberIfc subscriber : subscriberList)
	        {
	        	Map<ServiceFee2ID,SubscriberServices> container=SubscriberServicesSupport.getSubscribersServices(context,subscriber.getId());
	        	if(null!=container && !container.isEmpty())
	        	{
	        		Set<ServiceFee2ID> serviceFee2IDs = container.keySet();
	        		Iterator<ServiceFee2ID> serviceFee2IDIterator = serviceFee2IDs.iterator();
	        	
	        		while(serviceFee2IDIterator.hasNext())
	        		{
	        			holder = new CombinationDiscountCriteriaHolder();
	    	            holder.setSubscriptionType(subscriber.getSubscriptionType());
	    	            holder.setDunningLevelSubscription(subscriber.getLastDunningLevel());
	    	            holder.setSubscriptionState(subscriber.getState());
	    	            holder.setPricePlanID(subscriber.getPricePlan());
	    	            holder.setServiceID(serviceFee2IDIterator.next().getServiceId());
	    	            list.add(holder);
	        		}
	        	}
	            
	        }
	        
			prameterMap_.put(RuleEngineConstants.DISCOUNT_TYPE, DiscountCriteriaTypeEnum.COMBINATION_DISCOUNT);
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
			// hit the discount rule engine to find applicable rule if any
			List<BusinessRuleIfc> outputList = BusinessRuleEngineUtility.evaluateAllRule(context,EventTypeEnum.DISCOUNT,account, subscriberList, prameterMap_);
			
			Map<String, SubscriberIfc> subscribers = new HashMap<String, SubscriberIfc>();
			for(SubscriberIfc sub : subscriberList){
				subscribers.put(sub.getId(), sub);
			}
			Date effectiveDate = null;
			if(null != outputList && !outputList.isEmpty()){
				
				for(BusinessRuleIfc outputRule : outputList){
					for(CombinationDiscountOutputHolder output : outputRule.getCombinationDiscountOutput())
					{
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									MODULE,
									"Starting Processing of Combination Discount assignment for Rule ID "
									+ outputRule.getRuleId());
						}
						RuleInfoConfig cfg = (RuleInfoConfig)context.get(RuleInfoConfig.class);
						int version = cfg.getCurrentRuleVersion();


						if(output.getDiscountApplicationCriteria().equals(DiscountApplicationCriteriaEnum.AGE_ON_NETWORK))
						{

							SubscriberIfc sub=DiscountActivityUtils.getOldestSubscriber(context, output.getSubscriptionType(), output.getPricePlanID()
									, subscriberList, trackDiscount, outputRule.getMultipleDiscount(), output.getServiceID());
							if(null != sub){
								if(null!=existingDiscountActivities && !existingDiscountActivities.isEmpty())
								{
									DiscountEventActivity discountEventActivity = getDiscountEventActivityAvailable(context,existingDiscountActivities
											,output,outputRule,sub);
									if(null!=discountEventActivity)
									{
										if(DiscountActivityUtils.checkIfRuleVersionChange(context, discountEventActivity, outputRule))
										{
											discountEventActivity.setDiscountRuleVersion(outputRule.getRuleVersion(context));
											discountEventActivityForUpdate.add(discountEventActivity);
										}else
										{
											discountEventActivityContinued.add(discountEventActivity);
										}
										if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"Discount Event Activity for Combination Age On Network exists for the current criteria");
										}
										trackDiscount.put(sub.getId(),true);
										continue;
									}else
									{
                                        effectiveDate = getEffectiveDateFromAllCriteria(context, account, subscriberList,outputRule.getCombinationDiscountInput(),sub,output.getServiceID());
										discountEventActivity = DiscountActivityUtils.createDiscounteventActivity(context, existingDiscountActivities, account, 
												sub.getId(), output.getServiceID(), DiscountEventActivityServiceTypeEnum.SERVICE, output.getPricePlanID(),
												-1, DiscountEventActivityTypeEnum.COMBINATION_DISCOUNT, outputRule.getRuleId(), version,
												effectiveDate,output.getDiscountClass(),-1,-1);
										if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(
													context,
													MODULE,
													"Creating New Discount Event Activity for Combination Age On Network and effective date for event activity is :"
													+ effectiveDate);
										}
										trackDiscount.put(sub.getId(),true);
										
										validateAndFilterDiscount(context, 
												existingDiscountActivities, 
												discountEventActivity, 
												discountEventActivityForCreation, 
												discountEventActivityForUpdate,
												discountEventActivityContinued);
										discountEventActivityForCreation.add(discountEventActivity);
									}
								}else
								{
									effectiveDate = getEffectiveDateFromAllCriteria(context, account, subscriberList,outputRule.getCombinationDiscountInput(),sub,output.getServiceID());
                                    discountEventActivityForCreation.
									add(DiscountActivityUtils.createDiscounteventActivity(context, null, account, 
											sub.getId(), output.getServiceID(), DiscountEventActivityServiceTypeEnum.SERVICE, output.getPricePlanID(),
											-1, DiscountEventActivityTypeEnum.COMBINATION_DISCOUNT, outputRule.getRuleId(), version,
											effectiveDate,output.getDiscountClass(),-1,-1));
                                    if (LogSupport.isDebugEnabled(context)) {
										LogSupport.debug(
												context,
												MODULE,
												"Creating First Discount Event Activity for Combination Age On Network and effective date for event activity is :"
												+ effectiveDate);
									}
									trackDiscount.put(sub.getId(),true);

								}
							}else
							{
								checkForCombinationDiscount(context, 
										existingDiscountActivities, 
										discountEventActivityForCreation, 
										discountEventActivityForUpdate,
										discountEventActivityContinued,
										account,
										null,
										output.getServiceID(),
										outputRule.getRuleId()
										);
							}
						}else
						{
							for(SubscriberIfc sub:subscriberList)
							{
								Map<ServiceFee2ID,SubscriberServices> serviceMap =SubscriberServicesSupport.getSubscribersServices(context, sub.getId());
								if(null==trackDiscount.get(sub.getId()) || outputRule.getMultipleDiscount())
								{
									if(null!=existingDiscountActivities && !existingDiscountActivities.isEmpty())
									{
										DiscountEventActivity discountEventActivity = getDiscountEventActivityAvailable(context,existingDiscountActivities
												,output,outputRule,sub);
										if(null!=discountEventActivity)
										{
											if(DiscountActivityUtils.checkIfRuleVersionChange(context, discountEventActivity, outputRule))
											{
												discountEventActivity.setDiscountRuleVersion(outputRule.getRuleVersion(context));
												discountEventActivityForUpdate.add(discountEventActivity);
											}else
											{
												discountEventActivityContinued.add(discountEventActivity);
											}
											trackDiscount.put(sub.getId(),true);
											if (LogSupport.isDebugEnabled(context)) {
												LogSupport.debug(
														context,
														MODULE,
														"Discount Event Activity for Combination All Subscription criteria exits for the subscriber :"
														+ sub.getId());
											}
											continue;
										}else if(serviceMap.containsKey(output.getServiceID()) 
												&& sub.getSubscriptionType() == output.getSubscriptionType()
												&& sub.getPricePlan() == output.getPricePlanID()
												&& SubscriberServicesUtil.containsServiceId(sub.getServices(), output.getServiceID())
												&& !sub.getState().equals(SubscriberStateEnum.SUSPENDED))
										{
                                            effectiveDate = getEffectiveDateFromAllCriteria(context, account, subscriberList,outputRule.getCombinationDiscountInput(),sub,output.getServiceID());
											discountEventActivity = DiscountActivityUtils.createDiscounteventActivity(context, existingDiscountActivities, account, 
															sub.getId(), output.getServiceID(), DiscountEventActivityServiceTypeEnum.SERVICE, output.getPricePlanID(),
															-1, DiscountEventActivityTypeEnum.COMBINATION_DISCOUNT, outputRule.getRuleId(), version,
															effectiveDate,output.getDiscountClass(),-1,-1);
											if (LogSupport.isDebugEnabled(context)) {
												LogSupport.debug(
														context,
														MODULE,
														"Creating new Discount Event Activity for Combination All Subscription criteria for subscriber :"
														+ sub.getId() + " And effective Date is : " +effectiveDate);
											}
											trackDiscount.put(sub.getId(),true);

											validateAndFilterDiscount(context, 
													existingDiscountActivities, 
													discountEventActivity, 
													discountEventActivityForCreation, 
													discountEventActivityForUpdate,
													discountEventActivityContinued);
											discountEventActivityForCreation.add(discountEventActivity);
										}

									}else
									{
										if(serviceMap.containsKey(output.getServiceID()) 
												&& sub.getSubscriptionType() == output.getSubscriptionType()
												&& sub.getPricePlan() == output.getPricePlanID()
												&& SubscriberServicesUtil.containsServiceId(sub.getServices(), output.getServiceID())
												&& !sub.getState().equals(SubscriberStateEnum.SUSPENDED))
										{

                                            effectiveDate = getEffectiveDateFromAllCriteria(context, account, subscriberList,outputRule.getCombinationDiscountInput(),sub,output.getServiceID());
											discountEventActivityForCreation.add(
													DiscountActivityUtils.createDiscounteventActivity(context, existingDiscountActivities, account, 
															sub.getId(), output.getServiceID(), DiscountEventActivityServiceTypeEnum.SERVICE, output.getPricePlanID(),
															-1, DiscountEventActivityTypeEnum.COMBINATION_DISCOUNT, outputRule.getRuleId(), version,effectiveDate,
															output.getDiscountClass(),-1,-1));
											if (LogSupport.isDebugEnabled(context)) {
												LogSupport.debug(
														context,
														MODULE,
														"Creating first Discount Event Activity for Combination All Subscription criteria for subscriber :"
														+ sub.getId() + " And effective Date is : " +effectiveDate);
											}
											trackDiscount.put(sub.getId(),true);
										}
									}
								}else
								{
									checkForCombinationDiscount(context, 
												existingDiscountActivities, 
												discountEventActivityForCreation, 
												discountEventActivityForUpdate,
												discountEventActivityContinued,
												account,
												sub.getId(),
												output.getServiceID(),
												outputRule.getRuleId()
												);
								}

							}

						}
					}
				}
			}
			
		}catch(NoRuleFoundException ex){
			new DebugLogMsg(this, "No Discount Rule for Cross Subscription matched for the BAN:'" + account.getBAN()).log(context);
			return false;
		}
		return true;

	}

	private void validateAndFilterDiscount(Context context,
			Collection<DiscountEventActivity> existingDiscountActivities,
			DiscountEventActivity discountEventActivity,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate,
			Collection<DiscountEventActivity> discountEventActivityContinued) {
		/*// need to check if the new version of rule applied is present
		DiscountActivityUtils.checkRuleVersionChange(context,
				discountEventActivity,
				existingDiscountActivities,
				discountEventActivityForCreation,
				discountEventActivityForUpdate);*/

	/*	// need to check if any rule are now not applicable
		DiscountActivityUtils.checkNotApplicableDiscounts(context,
				discountEventActivity,
				existingDiscountActivities,
				discountEventActivityForCreation,
				discountEventActivityForUpdate,
				discountEventActivityContinued);*/
		
		checkForApplicableExpiredCombinationDiscount(context,
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
    		 List<DiscountEventActivity> discountEventToBeUpdated
    	) {
		for(DiscountEventActivity discountEventActivity : discountActivityEventList){
			if(discountEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.COMBINATION_DISCOUNT))
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
						Transaction discountTransaction = generateCombinationDiscountTransaction(context, discountEventActivity);
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
						Transaction reverseDiscountTransaction = generateCombinationReverseDiscountTransaction(context, discountEventActivity);
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
	private Transaction generateCombinationDiscountTransaction(Context ctx,
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
	private Transaction generateCombinationReverseDiscountTransaction(Context ctx,
			DiscountEventActivity discEventActivity)
					throws HomeException {
		Transaction discountTransaction =  DiscountSupportImpl.generateReverseDiscountTransactions(ctx, discEventActivity);
		return discountTransaction;
		
	}
	
	private DiscountEventActivity getDiscountEventActivityAvailable(final Context context,final Collection<DiscountEventActivity> discountEventsActivity,
			final CombinationDiscountOutputHolder object,final BusinessRuleIfc output,SubscriberIfc subscriber)
	{
		DiscountEventActivity discountEventActivityAvailable = null;
		Iterator<DiscountEventActivity> iterator = discountEventsActivity.iterator();
		while(iterator.hasNext())
		{
			DiscountEventActivity discEventActivity = iterator.next();
			if(discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.COMBINATION_DISCOUNT))
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
									"getDiscountEventActivityAvailable : Fee Personalization  : Found matched discount event for Fee Personalization Combination discount "
									+ discEventActivity.getId());
						}
						break;
					}else{
						  discountEventActivityAvailable= discEventActivity;
						  if (LogSupport.isDebugEnabled(context)) {
								LogSupport.debug(
										context,
										MODULE,
										"Found matched discount event for Combination discount "
										+ discEventActivity.getId());
							}
						  break;
						}
				    }
			}
		}
		return discountEventActivityAvailable;
	}

	
	 private Date getEffectiveDateFromContributors(final Context ctx
			   ,final Account account
			   ,final List<SubscriberIfc> subscriberList
			   ,final List<CombinationDiscountCriteriaHolder> inputObject)
	   {
		   Map<String,Long> subServiceMap = new HashMap<String, Long>();
		   Iterator<CombinationDiscountCriteriaHolder> iterator = inputObject.iterator();
		   Date effectiveDate = null;
		   while(iterator.hasNext())
		   {
			  CombinationDiscountCriteriaHolder input = iterator.next();
			  SubscriberIfc sub = DiscountActivityUtils.getOldestSubscriberForInput(ctx,input.getSubscriptionType(),
					  input.getPricePlanID(),subscriberList,input.getServiceID());  
			  subServiceMap.put(sub.getId(),input.getServiceID());
			  if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,"Filling the map for calculating the effective date"
									+ "Service Id :" + input.getServiceID()
									+ "Subscriber Id : " + sub.getId());
			    }
		   }
		   effectiveDate = DiscountActivityUtils.getLatestEffectiveDate(subServiceMap,ctx,account);
		   return effectiveDate;
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

	 private void checkForCombinationDiscount(final Context context, 
			 Collection<DiscountEventActivity> existingDiscountActivities, 
			 Collection<DiscountEventActivity> discountEventActivityForCreation, 
			 Collection<DiscountEventActivity> discountEventActivityForUpdate,
			 Collection<DiscountEventActivity> discountEventActivityContinued,
			 final Account ban,
			 final String subId,
			 final long serviceId,
			 final String ruleId)
	{
	
		List<DiscountPriority> priorityList= DiscountClassContextAgent.discountPriorityList;
		List<DiscountEventActivityTypeEnum> higherPriorityToBeChecked = new ArrayList<DiscountEventActivityTypeEnum>();
		
		      for(DiscountPriority obj:priorityList)
		      {
		    	  if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.PAIRED_DISCOUNT))
		    		{
		    			higherPriorityToBeChecked.add(DiscountEventActivityTypeEnum.PAIRED_DISCOUNT);
		    		}else if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.SECONDARY_DEVICE_DISCOUNT))
		    		{
		    			higherPriorityToBeChecked.add(DiscountEventActivityTypeEnum.SECOND_DEVICE_DISCOUNT);
		    		}else if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.CONTRACT_DISCOUNT))
		    		{
		    			higherPriorityToBeChecked.add(DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT);
		    		}else if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.CROSS_SUBSCRIPTION_DISCOUNT))
		    		{
		    			higherPriorityToBeChecked.add(DiscountEventActivityTypeEnum.BUNDLE_DISCOUNT);
		    		}else if(obj.getDiscountPriority().equals(DiscountPriorityTypeEnum.COMBINATION_DISCOUNT))
		    		{
		    			break;
		    		}
		      }
		
		
		//find if the same rule for same service with different subscriber id is already applied
		for(DiscountEventActivity currDisc : discountEventActivityForCreation){
			// condition for same rule with different version comparison
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
										 "Deactivating old Combination Discount event activity for ban" + discEventActivity.getBan()
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
		
	private DiscountEventActivity getDiscountEventActivityFromSub(final Collection<DiscountEventActivity> discountEventsActivity,final long serviceID,
				final String subId,final String ruleId)
		{
			DiscountEventActivity discountEventActivityAvailable = null;
			Iterator<DiscountEventActivity> iterator = discountEventsActivity.iterator();
			while(iterator.hasNext())
			{
				DiscountEventActivity discEventActivity = iterator.next();
				if(discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.COMBINATION_DISCOUNT))
				{
					if(discEventActivity.getServiceId()==serviceID 
							&& discEventActivity.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.SERVICE)
							&& discEventActivity.getSubId().equals(subId)
							&& discEventActivity.getState().equals(DiscountEventActivityStatusEnum.ACTIVE)
							&& discEventActivity.getDiscountRuleId().equals(ruleId))
					    {
						  discountEventActivityAvailable= discEventActivity;
						  break;
						}
				}
			}
			return discountEventActivityAvailable;	   
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
	
	private void checkForApplicableExpiredCombinationDiscount(Context context,
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
	
	private Date getEffectiveDateFromAllCriteria(final Context ctx
			   ,final Account account
			   ,final List<SubscriberIfc> subscriberList
			   ,final List<CombinationDiscountCriteriaHolder> inputObject
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
								MODULE,"Effective date from input of rule is"
										+ effectiverDateFromInput);
				    }
		        	Map<String,Long> subServiceMap = new HashMap<String, Long>();
		        	subServiceMap.put(sub.getId(),serviceId);
		        	if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,"Filling the map for calculating the effective date with output of rule"
										+ "Service Id :" + serviceId
										+ "Subscriber Id : " + sub.getId());
				    }
		        	effectiveDate = DiscountActivityUtils.getLatestEffectiveDate(subServiceMap,ctx,account);
		        	if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,"Effective date from output of rule is"
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

}
