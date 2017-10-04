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
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleEngineUtility;
import com.trilogy.app.crm.core.ruleengine.DiscountCriteriaTypeEnum;
import com.trilogy.app.crm.core.ruleengine.DiscountPriority;
import com.trilogy.app.crm.core.ruleengine.DiscountPriorityTypeEnum;
import com.trilogy.app.crm.core.ruleengine.EventTypeEnum;
import com.trilogy.app.crm.core.ruleengine.PairedDiscountCriteriaHolder;
import com.trilogy.app.crm.core.ruleengine.PairedDiscountOutputHolder;
import com.trilogy.app.crm.core.ruleengine.engine.exception.NoRuleFoundException;
import com.trilogy.app.crm.core.ruleengine.engine.ifc.BusinessRuleIfc;
import com.trilogy.app.crm.core.ruleengine.util.RuleEngineConstants;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.DiscountSupportImpl;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Discount handler for the Paired discounts
 * @author abhijit.mokashi
 *
 */
public class PairedDiscountHandler implements DiscountHandler{

	// Map to hold the input data to be passed to discount rule engine
	private Map<Integer,Object> prameterMap_ = new HashMap<Integer, Object>();
	public static String MODULE = PairedDiscountHandler.class.getName();
	@Override
	public boolean init(Context context,
			Account account,List<SubscriberIfc> subscriberList) {
		try	{
			// checking for Paired Discount 
			List<AbstractBean> list = new ArrayList<AbstractBean>();
	        PairedDiscountCriteriaHolder holder;
	       
	        for (SubscriberIfc subscriber : subscriberList)
	        {
	        	Map<ServiceFee2ID,SubscriberServices> container=SubscriberServicesSupport.getSubscribersServices(context,subscriber.getId());
	        	if(null!=container && !container.isEmpty())
	        	{
	        		Set<ServiceFee2ID> serviceIDCollection = container.keySet();
	        		Iterator<ServiceFee2ID> serviceIdIterator = serviceIDCollection.iterator();
	        		
	        		while(serviceIdIterator.hasNext())
	        		{
	        			holder = new PairedDiscountCriteriaHolder();
	    	            holder.setSubscriptionType(subscriber.getSubscriptionType());
	    	            holder.setDunningLevelSubscription(subscriber.getLastDunningLevel());
	    	            holder.setSubscriptionState(subscriber.getState());
	    	            holder.setPricePlanID(subscriber.getPricePlan());
	    	            holder.setServiceID(serviceIdIterator.next().getServiceId());
	    	            list.add(holder);
	        		}
	        	}
	        }
	 		prameterMap_.put(RuleEngineConstants.DISCOUNT_TYPE, DiscountCriteriaTypeEnum.PAIRED_DISCOUNT);
			prameterMap_.put(RuleEngineConstants.SUBSCRIBER_EVENTS, list);
		} catch (Exception e){
			new MinorLogMsg(this, "Failed to initialize the paired discount handler for account '");
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
			BusinessRuleIfc output = BusinessRuleEngineUtility.evaluateRule(context,EventTypeEnum.DISCOUNT,
					account, subscriberList, prameterMap_);
			Date effectiveDate=null;
            Map<String,SubscriberIfc> pairedMap = new  HashMap<String,SubscriberIfc>();
            for(PairedDiscountOutputHolder holderObject : output.getPairedDiscountOutput()){
		    		
				boolean pairedSearchSuccessful = getPairedMap(context,output.getPairedDiscountInput(),subscriberList,
						                         holderObject,output.getMultipleDiscount(),pairedMap,trackDiscount);
				
				if(!pairedMap.isEmpty()&& pairedSearchSuccessful)
				{
				 for(SubscriberIfc sub : subscriberList)
				 {
					 if(pairedMap.containsKey(sub.getId()) )
					 {
						 if((null==trackDiscount.get(sub.getId())||output.getMultipleDiscount()))
						 {
						   DiscountEventActivity discountEventActivity=getDiscountEventActivityAvailableForPaired(context
									,existingDiscountActivities, holderObject, output, sub);	
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
						    }else
						    {
						    	new MinorLogMsg(this, "Discount Rule for Paired Discount matched for the BAN:'" + account.getBAN() + "Rule Id is :" +  output.getRuleId()).log(context);
	
	                            effectiveDate = getEffectiveDateFromAllCriteria(context, account, subscriberList, output.getPairedDiscountInput(),sub,holderObject.getServiceID());
						    	discountEventActivity = DiscountActivityUtils.createDiscounteventActivity(context, null, account, 
										 sub.getId(), holderObject.getServiceID(), DiscountEventActivityServiceTypeEnum.SERVICE, holderObject.getPricePlanID(),
										        -1, DiscountEventActivityTypeEnum.PAIRED_DISCOUNT, output.getRuleId(), output.getRuleVersion(context),
										        effectiveDate,holderObject.getDiscountClass(),-1,-1);
	
				
						    	
						    	validateAndFilterDiscount(context, existingDiscountActivities, 
						    			discountEventActivity, discountEventActivityForCreation, 
						    			discountEventActivityForUpdate, discountEventActivityContinued);
						    	discountEventActivityForCreation.add(discountEventActivity);
						    	trackDiscount.put(sub.getId(),true);
						    }
						 }else
						 {
							 checkForPairedDiscount(context, 
										existingDiscountActivities, 
										discountEventActivityForCreation, 
										discountEventActivityForUpdate,
										discountEventActivityContinued,
										account,
										sub.getId(),
										holderObject.getServiceID(),
										output.getRuleId()
										);
						 }
					 }
				  }
					
				}
				
			}
			}catch(NoRuleFoundException ex){
			new DebugLogMsg(this, "No Discount Rule for Paired Discount matched for the BAN:'" + account.getBAN()).log(context);
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
			if(discountEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.PAIRED_DISCOUNT))
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
						Transaction discountTransaction = generatePairedDiscountTransaction(context, discountEventActivity);
						if(null!=discountTransaction)
						{
							/*discountEventActivity.setDiscountAppliedFromDate(DiscountActivityUtils.getDiscountAppliedPeriodStartDate(context,discountEventActivity,account));
							discountEventActivity.setDiscountAppliedTillDate(DiscountActivityUtils.getDiscountAppliedPeriodEndDate(context,discountEventActivity,account));
							*/discountEventToBeUpdated.add(discountEventActivity);
							
						}
						
					} catch (Exception e) {
						LogSupport.minor(
								context,
								MODULE,
								"Exception Occured while creating transaction for " + account.getBAN()
								+ "  And discount event "
								+ discountEventActivity.getId() + " exception is : " + e.getStackTrace());
							
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
						Transaction reverseDiscountTransaction = generatePairedReverseDiscountTransaction(context, discountEventActivity);
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
	private Transaction generatePairedDiscountTransaction(Context ctx,
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
	private Transaction generatePairedReverseDiscountTransaction(Context ctx,
			DiscountEventActivity discEventActivity)
					throws HomeException {
		Transaction discountTransaction =  DiscountSupportImpl.generateReverseDiscountTransactions(ctx, discEventActivity);
		return discountTransaction;
		
	}
	private boolean getPairedMap(final Context ctx,final List<PairedDiscountCriteriaHolder> pairedDiscountCriteriaList
			                      ,final List<SubscriberIfc> subscriberList,final PairedDiscountOutputHolder output
			                      ,final boolean multipleDiscount, Map<String,SubscriberIfc> pairedMap
			                      ,Map<String, Boolean> trackDiscount)
	{
		int uniquePair=getPairedSubscriptionCount(ctx,pairedDiscountCriteriaList,subscriberList);
		List<SubscriberIfc> sortedSubList=DiscountActivityUtils.
				               getSubscriptionByPriority(ctx, subscriberList, output.getSubscriptionType(),-1);
		if(sortedSubList!=null && !sortedSubList.isEmpty())
		{
			if(multipleDiscount){
				int instanceCount=0;
				for(SubscriberIfc sub : sortedSubList)
				{
                    if(!sub.getState().equals(SubscriberStateEnum.SUSPENDED)){  
						PricePlanVersion version=null;
						try {
							version = PricePlanSupport.getCurrentVersion(ctx, sub.getPricePlan());
						} catch (HomeException e) {
						    LogSupport.info(ctx,MODULE,"Unable to find version for the paired subscription "
						    		+ "price plan");
						}
						// Assumption there will be only 1 mandatory service in PP
						if(version!=null)
						{
							ServiceFee2 serviceFee =  version.getMandatoryService(ctx);
							if(serviceFee.getServiceId() == output.getServiceID() && instanceCount<uniquePair)
							{
								instanceCount++;
								pairedMap.put(sub.getId(),sub);
								 if (LogSupport.isDebugEnabled(ctx)) {
										LogSupport.debug(
												ctx,
												MODULE,
												"Finding Paired Discount's output instance when mulitple discount allowed" + pairedMap );
									}
							}
						}
                    }
					
				}
				
			}else
			{
				int instanceCount=0;
				for(SubscriberIfc sub : sortedSubList)
				{
					if(!trackDiscount.containsKey(sub.getId()) && instanceCount<uniquePair
							&& !sub.getState().equals(SubscriberStateEnum.SUSPENDED))
					{
						PricePlanVersion version=null;
						try {
							version = PricePlanSupport.getCurrentVersion(ctx, sub.getPricePlan());
						} catch (HomeException e) {
						    LogSupport.info(ctx,MODULE,"Unable to find version for the paired subscription "
						    		+ "price plan");
						}
						// Assumption there will be only 1 mandatory service in PP
						if(version!=null)
						{
							ServiceFee2 serviceFee =  version.getMandatoryService(ctx);
							if(serviceFee.getServiceId() == output.getServiceID() && instanceCount<uniquePair)
							{
								instanceCount++;
								pairedMap.put(sub.getId(),sub);
								 if (LogSupport.isDebugEnabled(ctx)) {
										LogSupport.debug(
												ctx,
												MODULE,
												"Finding Paired Discount's output instance when mulitple discount not allowed" + pairedMap );
									}
							}
						}
						
					}
				}
			}
		}
		return true;
	}
	
	/*
	 * This method is used to find unique pair for the given criteria
	 * @param : List<PairedDiscountCriteriaHolder>
	 * @param : List<SubscriberIfc>
	 */
	 private static int getPairedSubscriptionCount(final Context ctx,final List<PairedDiscountCriteriaHolder> listPairedDiscountCriteria,final List<SubscriberIfc> subscriberList)
		{
			if(listPairedDiscountCriteria!=null && !listPairedDiscountCriteria.isEmpty())
			{
				List<Long> pricePlanId = new ArrayList<Long>();
				List<SubscriberIfc> pairedSubscriberList = new ArrayList<SubscriberIfc>();
				Map<Integer,List<SubscriberIfc>> uniquePaired = new HashMap<Integer,List<SubscriberIfc>>();
				for(PairedDiscountCriteriaHolder pairedDiscountObject: listPairedDiscountCriteria)
				{
					pricePlanId.add(pairedDiscountObject.getPricePlanID());	
				}
				
				int count=0;
				List<Long> pairedPpId = new ArrayList<Long>();
			    for(SubscriberIfc sub: subscriberList)
				{
					  for(Long ppId: pricePlanId)
					  {
						  if(sub.getPricePlan()==ppId && !pairedPpId.contains(sub.getPricePlan())
								  && !sub.getState().equals(SubscriberStateEnum.SUSPENDED))
						  {
							  pairedSubscriberList.add(sub);
							  pairedPpId.add(ppId);
						  }
					  }
					if(pairedSubscriberList.size()==pricePlanId.size())
					{
						count++;
						 if (LogSupport.isDebugEnabled(ctx)) {
								LogSupport.debug(
										ctx,
										MODULE,
										"Pair No: " + count
										+"Paired Subscriber :" + pairedSubscriberList);
							}
						uniquePaired.put(count,pairedSubscriberList);
						pairedSubscriberList.clear();
						pairedPpId.clear();
					}
				}
				if(uniquePaired.isEmpty())
				{
					return 0;
				}else
				{
					 if (LogSupport.isDebugEnabled(ctx)) {
							LogSupport.debug(
									ctx,
									MODULE,
									"No Of pair detected for Paired Discount: " + uniquePaired.size());
						}
					return uniquePaired.size();
				}
		       
			}
			return 0;
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
					discountEventActivityForUpdate);
*/
			/*// need to check if any rule are now not applicable
			DiscountActivityUtils.checkNotApplicableDiscounts(context,
					discountEventActivity,
					existingDiscountActivities,
					discountEventActivityForCreation,
					discountEventActivityForUpdate,
					discountEventActivityContinued);*/
			
			checkForOtherNotApplicablePairedDiscount(context,
					discountEventActivity,
					existingDiscountActivities,
					discountEventActivityForCreation,
					discountEventActivityForUpdate);
			
			checkForApplicableExpiredPairedDiscount(context,
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
		
		
			
	 private DiscountEventActivity getDiscountEventActivityAvailableForPaired(final Context context,final Collection<DiscountEventActivity> discountEventsActivity,
				final PairedDiscountOutputHolder object,final BusinessRuleIfc output,SubscriberIfc subscriber)
		{
			DiscountEventActivity discountEventActivityAvailable = null;
			Iterator<DiscountEventActivity> iterator = discountEventsActivity.iterator();
			while(iterator.hasNext())
			{
				DiscountEventActivity discEventActivity = iterator.next();
				if(discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.PAIRED_DISCOUNT))
				{
					      
					if(discEventActivity.getServiceId()==object.getServiceID() 
							&& discEventActivity.getDiscountRuleId().equals(output.getRuleId())
							&& discEventActivity.getDiscountServiceType().equals(DiscountEventActivityServiceTypeEnum.SERVICE)
							&& discEventActivity.getSubId().equals(subscriber.getId())
							&& !subscriber.getState().equals(SubscriberStateEnum.SUSPENDED)
							&& discEventActivity.getDiscountClass() == object.getDiscountClass())
					    {
							if(DiscountActivityUtils.isFeePersonalizedAvailableForSubscriptionService(context,discEventActivity))
							{
								if (LogSupport.isDebugEnabled(context)) {
									LogSupport.debug(
											context,
											MODULE,
											"getDiscountEventActivityAvailableForPaired : Fee Personalization  : Found matched discount event for Fee Personalization Paired discount "
											+ discEventActivity.getId());
								}
								break;
							}else{
							  discountEventActivityAvailable= discEventActivity;
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
			   ,final List<PairedDiscountCriteriaHolder> inputObject)
	   {
		   Map<String,Long> subServiceMap = new HashMap<String, Long>();
		   Iterator<PairedDiscountCriteriaHolder> iterator = inputObject.iterator();
		   Date effectiveDate = null;
		   while(iterator.hasNext())
		   {
			   PairedDiscountCriteriaHolder input = iterator.next();
			  SubscriberIfc sub = DiscountActivityUtils.getOldestSubscriberForInput(ctx,input.getSubscriptionType(),
					  input.getPricePlanID(),subscriberList,input.getServiceID());  
			  subServiceMap.put(sub.getId(),input.getServiceID());
		   }
		   effectiveDate = DiscountActivityUtils.getLatestEffectiveDate(subServiceMap,ctx,account);
		   return effectiveDate;
	   }
	 
		private void checkForOtherNotApplicablePairedDiscount(Context context,
				DiscountEventActivity discountEventActivity,
				Collection<DiscountEventActivity> existingDiscountActivities,
				Collection<DiscountEventActivity> discountEventActivityForCreation,
				Collection<DiscountEventActivity> discountEventActivityForUpdate) {
			
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
							getDateWithNoTimeOfDay(calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount(context
									,currDisc.getServiceId()
									,currDisc.getSubId()
									,discountEventActivity.getDiscountEffectiveDate())));
					currDisc.setState(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);
					if(currDisc.getDiscountExpirationDate().after(discountEventActivity.getDiscountEffectiveDate()))
					{
						discountEventActivity.setDiscountEffectiveDate(
								CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(
										CalendarSupportHelper.get(context).getDayAfter(currDisc.getDiscountExpirationDate())));
					}
					discountEventActivityForUpdate.add(currDisc);
					
				}
			}
			
			
		}
		
		private Date calculateExpiryDateForSmartSuspensionOrOtherPriorityDiscount(final Context ctx
				,final long serviceId
				,final String subId
				,final Date discountEffecttiveDate) {
	
			Date expiryDate=null;
			try {
			    Subscriber sub = SubscriberSupport.getSubscriber(ctx, subId);
			    if((null !=ctx.get(DiscountClassContextAgent.SUSPENSIONTRIGGER) 
			    		&& ctx.getBoolean(DiscountClassContextAgent.SUSPENSIONTRIGGER))
			    		|| sub.getState().equals(SubscriberStateEnum.SUSPENDED))
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
			            	}else
			            	{
			            		expiryDate=CalendarSupportHelper
										.get(ctx).getDateWithNoTimeOfDay(
												new Date());
			            		
			            	}
			            	
			            	
			            }else{
			            	SubscriberServices subscriberService = SubscriberServicesSupport
									.getSubscriberServiceRecord(ctx,
											subId,
											serviceId, SubscriberServicesUtil.DEFAULT_PATH);
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
			            		expiryDate = CalendarSupportHelper
										.get(ctx).getDateWithNoTimeOfDay(
												new Date());
			            	}
			            }
						
					
			    }else if(sub.getState().equals(SubscriberStateEnum.INACTIVE))
			    {
			    	expiryDate = CalendarSupportHelper
							.get(ctx).getDateWithNoTimeOfDay(
									new Date());
			    	
			    }else
			    {
			    	expiryDate = CalendarSupportHelper.get(ctx).getDayBefore(CalendarSupportHelper
							.get(ctx).getDateWithNoTimeOfDay(
									discountEffecttiveDate));
			    }
			} catch (HomeException e) {
			     LogSupport.info(ctx, this,"Error while Calculating Expiry Date");
			}
			
				return expiryDate;
		
		}
		
		 private void checkForPairedDiscount(final Context context, 
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
			    			break;
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
			    			higherPriorityToBeChecked.add(DiscountEventActivityTypeEnum.COMBINATION_DISCOUNT);
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
					if(discEventActivity.getDiscountType().equals(DiscountEventActivityTypeEnum.PAIRED_DISCOUNT))
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
		
		private void checkForApplicableExpiredPairedDiscount(Context context,
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
						}
					
					}
				}
			}
			
			
		}
		
		private Date getEffectiveDateFromAllCriteria(final Context ctx
				   ,final Account account
				   ,final List<SubscriberIfc> subscriberList
				   ,final List<PairedDiscountCriteriaHolder> inputObject
				   ,final SubscriberIfc sub
				   ,final long serviceId)
				   {
			        Date effectiveDate = null;
			        Date effectiverDateFromInput = getEffectiveDateFromContributors(ctx,account,subscriberList,inputObject);
			        if(null!=effectiverDateFromInput)
			        {
			        	Map<String,Long> subServiceMap = new HashMap<String, Long>();
			        	subServiceMap.put(sub.getId(),serviceId);
			        	effectiveDate = DiscountActivityUtils.getLatestEffectiveDate(subServiceMap,ctx,account);
			        	if(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiveDate).
			        			before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiverDateFromInput)))
			        	{
			        		effectiveDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(effectiverDateFromInput);
			        	}
			        }
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

