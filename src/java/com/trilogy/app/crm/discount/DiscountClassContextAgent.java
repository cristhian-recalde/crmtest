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
 */
package com.trilogy.app.crm.discount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.DiscountActivityTrigger;
import com.trilogy.app.crm.bean.DiscountActivityTriggerHome;
import com.trilogy.app.crm.bean.DiscountActivityTypeEnum;
import com.trilogy.app.crm.bean.DiscountEvaluationStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventActivity;
import com.trilogy.app.crm.bean.DiscountEventActivityHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.DiscountPriority;
import com.trilogy.app.crm.support.DiscountSupportImpl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Assigns the discount to the subscribers.
 * This class executes all the discount evaluation handlers those are configured on spid level
 * @author abhijit.mokashi
 *
 */
public class DiscountClassContextAgent implements ContextAgent {


	public static String SUSPENSIONTRIGGER = "SuspensionTrigger";
	public static String RESUMETRIGGER = "ResumeTrigger";
	public static List<DiscountPriority> discountPriorityList=null;

	public DiscountClassContextAgent(final DiscountClassAssignmentVisitor discountClassAssignmentVisitor){
	}

	public void execute(final Context context){
		final Account account = (Account) context.get(DISCOUNT_ELIGIBILITY_CHECK_ACCOUNT);
		boolean ruleVersioningCheck = (Boolean)context.getBoolean(DiscountClassContextAgent.DISCOUNT_RULE_VERSIONING_CHECK,false);
		try{
		
		//This is to fetch the scope accounts from the discountActivityTrigger table
		//Here, the target ban could be the ban itself or the parent/root ban fetched from the new table accountrelationship
		SimpleXStatement filter = new SimpleXStatement(" TARGETBAN =" + account.getBAN() + " and discountEvaluationStatus=" + 
		DiscountEvaluationStatusEnum.PENDING_INDEX +" "
					+ " and discountactivitytype not in (" + DiscountActivityTypeEnum.SERVICE_FEE_PERSONALIZE_EVENT_INDEX + "," 
		+ DiscountActivityTypeEnum.AUX_SERVICE_FEE_PERSONALIZE_EVENT_INDEX +")"); 
		
	    /*SimpleXStatement filter = new SimpleXStatement(" BAN=" + account.getBAN() + " and discountEvaluationStatus=" + DiscountEvaluationStatusEnum.PENDING_INDEX +" "
				+ " and discountactivitytype not in (" + DiscountActivityTypeEnum.SERVICE_FEE_PERSONALIZE_EVENT_INDEX + "," + DiscountActivityTypeEnum.AUX_SERVICE_FEE_PERSONALIZE_EVENT_INDEX +")"); */
		
		final Home discountActivityTriggerHome =  (Home) context.get(DiscountActivityTriggerHome.class);
		Collection<DiscountActivityTrigger> discountActivityTrigger = discountActivityTriggerHome.where(context, filter).selectAll(context);
		List<DiscountHandler> discountEvaluationOrder = new ArrayList<DiscountHandler>();
		if (account!=null && DiscountSupportImpl.initializeDiscountEvaluationOrder(context, account, 
				discountEvaluationOrder, discountPriorityList)&& ((null!=discountActivityTrigger
				&& !discountActivityTrigger.isEmpty())|| ruleVersioningCheck)) {
			try	{
				if(null!=discountActivityTrigger && !discountActivityTrigger.isEmpty())
				{
					for(DiscountActivityTrigger obj : discountActivityTrigger)
					{
						if(obj.getDiscountActivityType().equals(DiscountActivityTypeEnum.SUBSCRIBER_SUSPENSION_EVENT))
						{
							context.put(SUSPENSIONTRIGGER,true);
						}
						if(obj.getDiscountActivityType().equals(DiscountActivityTypeEnum.SUBSCRIBER_RESUME_EVENT))
						{
							context.put(RESUMETRIGGER,true);
						}
					}
				}
				
				
				
				//Collection<Subscriber> subList= account.getSubscribers(context);
				//To get all the subscribers from the root ban or the current ban
				Collection<Subscriber> subList = account.getAllSubscribers(context);
				new DebugLogMsg(this, "DiscountClassAssignment number of the fetched subscribers is :[ " + subList.size()+ " ] for the scope account id [ " +  account.getBAN() + " ]").log(context);
				
				List<SubscriberIfc> subscriberList = new ArrayList<SubscriberIfc>();
				for(Subscriber sub:subList) {
					if(!SubscriberStateEnum.INACTIVE.equals(sub.getState())){
						subscriberList.add(sub);
					}else{
						new DebugLogMsg(this, "DiscountClassAssignment process skipped subscriber:'" + sub.getId()+ " as its in " +  sub.getState() + " state.").log(context);
					}
				}

				// Retrieve the DiscountEvent record for the ban
				//Retrieve the discounts given to the current ban in the  DiscountEventActivity table depending on the SPID level strategy
				Collection<DiscountEventActivity> existingDiscountEventActivity = null;
				final Home discountEventActivityHome =  (Home) context.get(DiscountEventActivityHome.class);
				try{
					existingDiscountEventActivity= DiscountSupportImpl.findExistingDiscountEventActivity(context, account);
				}catch(Exception ex){
					new MinorLogMsg(this, "DiscountClassContextAgent:: findExistingDiscountEventActivity() failed for account '" + account.getBAN() + "': " +  ex.getMessage(), ex).log(context);
				}
				new DebugLogMsg(this, "DiscountClassContextAgent: findExistingDiscountEventActivity: existingDiscountEventActivity =  [" + existingDiscountEventActivity + "]").log(context);
				
				Map<String, Boolean> trackDiscount= new HashMap<String, Boolean>();
				
				// this will hold the discount event activity that need to be created 
				Collection<DiscountEventActivity> discountEventActivityForCreation = new ArrayList<DiscountEventActivity>();
				// this will hold the discount event activity that need to be updated, specially the expired entries
				Collection<DiscountEventActivity> discountEventActivityForUpdate = new ArrayList<DiscountEventActivity>();
				// this will hold the discount event activity that need to be continued, 
				// specially the entries which were applicable in past and in present also
				Collection<DiscountEventActivity> discountEventActivityContinued = new ArrayList<DiscountEventActivity>();


				// Execute each handler for the discount evaluation
				for(DiscountHandler handler : discountEvaluationOrder){
					// if initialization fails no need to evaluate the discount
					if(handler.init(context, account,subscriberList)){
						if (LogSupport.isDebugEnabled(context)) {
							LogSupport.debug(
									context,
									this,"Discount Handler : " + handler.getClass().getName() +" for processing account is called");
						}
		
          				handler.evaluate(context, account, subscriberList,existingDiscountEventActivity, 
          						trackDiscount,discountEventActivityForCreation, discountEventActivityForUpdate, 
          						discountEventActivityContinued);
					}
				}
               
				//DiscountActivityUtils.validateAndFilterDiscountEventActivity(context, existingDiscountEventActivity, newdiscountEventsActivity);
				// need to check if any rule are now not applicable
				DiscountActivityUtils.checkNotApplicableDiscounts(context,
						existingDiscountEventActivity,
						discountEventActivityForCreation,
						discountEventActivityForUpdate,
						discountEventActivityContinued,
						subscriberList);
				DiscountActivityUtils.saveDiscountEventActivity(context, discountEventActivityHome, discountEventActivityForCreation, discountEventActivityForUpdate);
				
				//updating the evaluation status of DiscountActivityTrigger
				if(null!=discountActivityTrigger && !discountActivityTrigger.isEmpty())
				{	
					for(DiscountActivityTrigger obj : discountActivityTrigger)
					{
						obj.setDiscountEvaluationStatus(DiscountEvaluationStatusEnum.PROCESSED);
						discountActivityTriggerHome.store(context,obj);
					}
					DiscountActivityUtils.saveDiscountActivityTriggerForServiceFee(context,account.getBAN());
					DiscountActivityUtils.saveDiscountActivityTriggerForAuxServiceFee(context,account.getBAN());
				}else if(ruleVersioningCheck)
				{
					SimpleXStatement condition = new SimpleXStatement("discountEvaluationStatus=" + DiscountEvaluationStatusEnum.PENDING_INDEX +" and discountActivityType= " + DiscountActivityTypeEnum.RULE_VERSIONING_CHANGE_EVENT_INDEX); 
					Collection<DiscountActivityTrigger> discountActivityTriggerForRuleVersioning = discountActivityTriggerHome.where(context, condition).selectAll(context);
					if(null!=discountActivityTriggerForRuleVersioning && !discountActivityTriggerForRuleVersioning.isEmpty())
					{	
						for(DiscountActivityTrigger obj : discountActivityTriggerForRuleVersioning)
						{
							obj.setDiscountEvaluationStatus(DiscountEvaluationStatusEnum.PROCESSED);
							discountActivityTriggerHome.store(context,obj);
						}
					}
				}
				
			}catch (Exception e)	{
				new MinorLogMsg(this, "DiscountClassAssignment process failed for account '" + account.getBAN() + "': " +  e.getMessage(), e).log(context);
			}

		  } else {
	
				new MinorLogMsg(this, "Discount agent initialization failed or No Pending triggers are left").log(context);
		  }
		}catch(Exception e)
		{
			new MinorLogMsg(this, "Unable to fecth records from triggers").log(context);
		}
	}

	
	
	// Name of the discount context agent
	public static final String DISCOUNT_ELIGIBILITY_CHECK_ACCOUNT = "DiscountEligibilityCheckAccount";
	public static final String DISCOUNT_RULE_VERSIONING_CHECK = "DiscountRuleVersioningCheck";

}
