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

package com.trilogy.app.crm.deposit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.DepositDetail;
import com.trilogy.app.crm.bean.DepositDetailHolder;
import com.trilogy.app.crm.bean.DepositItemReference;
import com.trilogy.app.crm.bean.ProductsListRow;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleEngineUtility;
import com.trilogy.app.crm.core.ruleengine.DepositCriteriaHolder;
import com.trilogy.app.crm.core.ruleengine.DepositDetails;
import com.trilogy.app.crm.core.ruleengine.DepositLevelEnum;
import com.trilogy.app.crm.core.ruleengine.EventTypeEnum;
import com.trilogy.app.crm.core.ruleengine.engine.exception.NoRuleFoundException;
import com.trilogy.app.crm.core.ruleengine.engine.ifc.BusinessRuleIfc;
import com.trilogy.app.crm.core.ruleengine.util.RuleEngineConstants;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class DetermineDepositSupport 
{

	/**
	 * 
	 * @param ctx
	 * @param spid
	 * @param accountId
	 * @param creditClass
	 * @param accountType
	 * @param itemReference
	 * @return
	 * @throws HomeException
	 * 
	 * Before allocating/creating deposit to subscription , need to determine the deposit amount based on subscription & allocated product/service.
	 * Deposit determination is bases on two condition
	 * 
	 * Condition 1: For New Subscription
	 * 				For new subscription, deposit determination is calculate on Subscription level & Product Level
	 * 				A) Subscription Level : Subscription level input parameter send to Rule Engine & get best match rule to get deposit amount
	 * 				B) Product Level      : Send assigned product Id/service Id List. Rule Engine will create request for each product Id and give best matching rule output to get deposit amount.
	 * 				
	 * 				Combining above condition's result, output will be generated.
	 * 
	 * Condition 2 : For Existing Subscription 
	 * 				A) Product Level      : Send assigned product Id/service Id List. Rule Engine will create request for each product Id and give best matching rule output to get deposit amount.
	 * @throws NoRuleFoundException 
	 * 
	 * Assumption 
	 * -----------
	 * To send account type as blank then value must be accountType <= 0 
	 * To send credit category as blank then send creditClass < 0
	 */
	public static DepositDetailHolder determineDeposit(Context ctx,int spid,String accountId,int creditClass,long accountType, DepositItemReference itemReference) throws HomeException
	{
		
		// COMMON VARIABLE
		Set<Long> subscriptionTypes 		= new HashSet<Long>();
		String correlationID 				= "";
		String depositCategory 				= "";
		String subscriberId 				= "";
		List<ProductsListRow> productIdList 			= new ArrayList<ProductsListRow>();
		BusinessRuleIfc output				= null;
		List<DepositDetail> depositDetails	= null;
		Account account 					= null;
		DepositDetailHolder depositDetailOutputHolder = new DepositDetailHolder();
		
		
		correlationID 		= itemReference.getCorrelationID();			
		subscriptionTypes 	= itemReference.getSubscriptionTypeSet();
		depositCategory 	= itemReference.getDepositCategory();
		productIdList 		= itemReference.getProductsListRow();
		subscriberId		= itemReference.getSubscriptionID();
		long contractID		= itemReference.getContractID();
		//----------------------------------------------------------------------------------------
		// FOR NEW SUBSCRIPTION
		//----------------------------------------------------------------------------------------
		
		if(subscriberId == null || subscriberId.isEmpty())
		{

			if(accountId != null && !accountId.isEmpty())
			{
				account = AccountSupport.getAccount(ctx, accountId);
				
				if(account == null)
				{
					throw new HomeException("Account not found for iddentifier:"+accountId);
				}
				
				//if creditClass not provided as input parameter
				if(creditClass < 0)
				{
					creditClass = account.getCreditCategory();
				}
				
				//if account Type not provided as input parameter
				if(accountType <= 0)
				{
					accountType = account.getType();
				}
				
			}else
			{
				// Account Credit Category is mandatory for new subscription is accountId not provided.
				if(creditClass < 0)
				{
					throw new HomeException("Credit category is mandatory if account Id is null");
				}
				
				// Account Type is mandatory for new subscription is accountId not provided.
				if(accountType <= 0)
				{
					throw new HomeException("accountType is mandatory if account Id is null");
				}
			}
			
			//get deposit determination details
			List<DepositDetail> depositDetailListResult = getDetermineDepositDetail(ctx, spid,account, creditClass, accountType,null, depositCategory, subscriptionTypes,productIdList,contractID);
			
			depositDetailOutputHolder.setCorrelationID(correlationID);
			depositDetailOutputHolder.setDepositDetails(depositDetailListResult);
			
			return depositDetailOutputHolder;
			
		}else
		{
		
		//----------------------------------------------------------------------------------------
				// FOR EXISTING SUBSCRIPTION
		//----------------------------------------------------------------------------------------
			Subscriber subscriber = SubscriberSupport.getSubscriber(ctx, subscriberId);
			
			if(subscriber == null)
			{
				throw new HomeException("Subscriber not found for Iddentifier:"+subscriberId);
			}
			
			if(accountId != null && !accountId.isEmpty())
			{
				account = AccountSupport.getAccount(ctx, accountId);
				 
				if(account == null)
				{
					throw new HomeException("Account not found for iddentifier:"+accountId);
				}
				
				//if creditClass not provided as input parameter
				if(creditClass < 0)
				{
					creditClass = account.getCreditCategory();
				}
				
				//if account Type not provided as input parameter
				if(accountType <= 0)
				{
					accountType = account.getType();
				}
				
				
			}else
			{
				
				account = subscriber.getAccount(ctx);
				
				if(creditClass < 0)
				{
					creditClass = account.getCreditCategory();
				}
				
				if(accountType <= 0)
				{
					accountType = account.getType();
				}
				
			}
			
			List<DepositDetail> depositDetailListResult = getDetermineDepositDetail(ctx, spid,account, creditClass, accountType,subscriberId, depositCategory, subscriptionTypes,productIdList,contractID);
		
			depositDetailOutputHolder.setCorrelationID(correlationID);
			depositDetailOutputHolder.setDepositDetails(depositDetailListResult);
			
			return depositDetailOutputHolder;
			
			
		}//end of else for existing subscription
		
	}

/**
 * 
 * @param ctx
 * @param spid
 * @param accountId
 * @param creditClass
 * @param accountType
 * @param subscriberId
 * @param depositCategory
 * @param subscriptionType
 * @param productIdList
 * @param contractID 
 * @return
 * @throws HomeException
 * 
 * Get the deposit determination for subscription as well as Product level.
 * @throws NoRuleFoundException 
 * 
 */
	public static List<DepositDetail> getDetermineDepositDetail(Context ctx, int spid,
			Account account, int creditClass, long accountType,
			String subscriberId, String depositCategory, Set subscriptionTypes,
			List<ProductsListRow> productIdList, long contractID) throws HomeException 
	{
			
		List<DepositDetail> depositDetailListResult = new ArrayList<DepositDetail>() ;
		
		//DEPOSIT DETERMINATION FOR NEW SUBSCRIBER
		if(subscriberId == null || subscriberId.isEmpty())
		{
			List<DepositDetail> depositSubscriptionLevelDetails= getSubscriptionLevelDepositDetermination(ctx,spid,account,
					creditClass,accountType,depositCategory,subscriptionTypes,true,contractID);

			List<DepositDetail> depositProductLevelDetails = getProductLevelDepositDetermination(ctx,spid,account,creditClass,
					accountType,subscriberId,depositCategory,subscriptionTypes,productIdList,true,contractID);
			
			if(depositSubscriptionLevelDetails != null && !depositSubscriptionLevelDetails.isEmpty())
				depositDetailListResult.addAll(depositSubscriptionLevelDetails);
			
			if(depositProductLevelDetails != null && !depositProductLevelDetails.isEmpty())				
				depositDetailListResult.addAll(depositProductLevelDetails);
			
		}else
		{
			//DEPOSIT DETERMINATION FOR EXISTING SUBSCRIPTION
			List<DepositDetail> depositProductLevelDetails = getProductLevelDepositDetermination(ctx,spid,account,creditClass,accountType,subscriberId,depositCategory,subscriptionTypes,productIdList,false,contractID);
			depositDetailListResult = depositProductLevelDetails;
		}
		
		return depositDetailListResult;
		
	}

/**
 * 
 * @param ctx
 * @param spid
 * @param accountId
 * @param creditClass
 * @param accountType
 * @param depositCategory
 * @param subscriptionType
 * @param isnewSubscription
 * @param contractID 
 * @return List<DepositDetail>
 * 
 *  This method return the subscription level deposit determination.
 * @throws HomeException 
 * @throws NoRuleFoundException 
 *  
 */
	private static List<DepositDetail> getSubscriptionLevelDepositDetermination(
			Context ctx, int spid,Account account, int creditClass,
			long accountType, String depositCategory, Set subscriptionTypes,boolean isnewSubscription, long contractID) throws HomeException 
	{
		
		BusinessRuleIfc output						= null;
		List<DepositDetail> depositDetails	 		= null;
		List<SubscriberIfc> subscriberList 			= null;
		List<DepositDetails> depositOutputHolder	= null;
		long amount 								= 0;
		long depositType 							= 0;		
		Map<Integer,Object> prameterMap_ 			= new HashMap<Integer, Object>();
		boolean isRuleFound							= true;
		
		Set<Integer> creditCategorySet 					= new HashSet<Integer>();
		creditCategorySet.add(creditClass);		
		
		depositDetails = new ArrayList<DepositDetail>();
		
		//Set 
		prameterMap_.put(RuleEngineConstants.DEPOSIT_LEVEL, DepositLevelEnum.SUBCRIPTION_LEVEL.getIndex());
		prameterMap_.put(RuleEngineConstants.ACCOUNT_TYPE_LIST, accountType);
		prameterMap_.put(RuleEngineConstants.CREDIT_CATEGORY, creditCategorySet);
		
		prameterMap_.put(RuleEngineConstants.DEPOSIT_CATEGORY, depositCategory);
		prameterMap_.put(RuleEngineConstants.SUBSCRIPTION_TYPE, subscriptionTypes);
		//prameterMap_.put(RuleEngineConstants.PRODUCT_LIST, productIdList);
		if(contractID >= 0)
			prameterMap_.put(RuleEngineConstants.CONTRACT_ID, contractID); 
		
		try {
			output = BusinessRuleEngineUtility.evaluateRule(ctx,EventTypeEnum.DEPOSIT_DETERMINATION,
					account, subscriberList, prameterMap_);
		} catch (NoRuleFoundException e) {
			LogSupport.info(ctx, DetermineDepositSupport.class.getName(), "No Rule Found for subscription Types: "+subscriptionTypes);
			isRuleFound = false;
		}
		
		if(!isRuleFound)
		{
			return depositDetails;
		}
		
		
		if(null!=output)
		{
			// get deposit details from Rule Engine
			List<DepositCriteriaHolder> depositCriteriaList=output.getDepositList();
			
			for(DepositCriteriaHolder depositCriteriaHolder:depositCriteriaList)
			{
				long subscriptionType = depositCriteriaHolder.getSubscriptionType();
				
				if(!subscriptionTypes.contains(subscriptionType))
					continue;
				
				depositOutputHolder = depositCriteriaHolder.getDepositDetails();
				
				for(DepositDetails depositDeails : depositOutputHolder)
				{
					amount 		= depositDeails.getAmount();
					depositType = depositDeails.getDepositType();
					
					DepositDetail depositDetail = new DepositDetail();
					
					depositDetail.setDepositTypeId(depositType);
					depositDetail.setSubscriptionType(subscriptionType);					
					depositDetail.setAmount(amount);
					//depositDetail.setProductId(productId);
					//depositDetail.setSubscriptionId(subscriptionId);
					depositDetails.add(depositDetail); 
				}
			}
		}
		return depositDetails;
	}	

/**
 * 
 * @param ctx
 * @param spid
 * @param accountId
 * @param creditClass
 * @param accountType
 * @param subscriberId
 * @param depositCategory
 * @param subscriptionType
 * @param productIdList
 * @param isnewSubscription
 * @param contractID 
 * @return List<DepositDetail>
 * 
 * This method return Product level deposit determination.
 * @throws NoRuleFoundException 
 * @throws HomeException 
 */
	private static List<DepositDetail> getProductLevelDepositDetermination(
			Context ctx, int spid, Account account, int creditClass,
			long accountType, String subscriberId, String depositCategory,
			Set subscriptionTypes, List<ProductsListRow> productIdList,boolean isnewSubscription, long contractID) throws HomeException 
	{
		BusinessRuleIfc output							= null;
		List<DepositDetail> depositDetails	 			= null;
		List<SubscriberIfc> subscriberList 				= new ArrayList<SubscriberIfc>();
		List<DepositDetails> depositOutputHolder		= null;
		List<DepositCriteriaHolder> depositCriteriaList	= null;
		List<Long> productIds 							= new ArrayList<Long>();
		Map<Long, String>	productIdToPath				= new HashMap<Long, String>();
		long amount 									= 0;
		long depositType 								= 0;		
		Map<Integer,Object> prameterMap_ 				= new HashMap<Integer, Object>();
		boolean isRuleFound								= true;
		
		Set<Integer> creditCategorySet 					= new HashSet<Integer>();
		creditCategorySet.add(creditClass);	
		depositDetails = new ArrayList<DepositDetail>();
		
			
		if(productIdList != null && !productIdList.isEmpty() && productIdList.size()>0)
		{
			for (ProductsListRow row : productIdList) 
			{
				productIds.add(row.getProductID());
				productIdToPath.put(row.getProductID(), row.getProductPath());
			}
			if(!isnewSubscription)
			{
				Subscriber subscriber = SubscriberSupport.getSubscriber(ctx, subscriberId);
				subscriberList.add(subscriber);
			}	
			
			prameterMap_.put(RuleEngineConstants.DEPOSIT_LEVEL, DepositLevelEnum.PRODUCT_LEVEL.getIndex());
			prameterMap_.put(RuleEngineConstants.ACCOUNT_TYPE_LIST, accountType);
			//prameterMap_.put(RuleEngineConstants.CREDIT_CATEGORY, creditClass);
			prameterMap_.put(RuleEngineConstants.CREDIT_CATEGORY, creditCategorySet);
			prameterMap_.put(RuleEngineConstants.DEPOSIT_CATEGORY, depositCategory);
			prameterMap_.put(RuleEngineConstants.SUBSCRIPTION_TYPE, subscriptionTypes);
			prameterMap_.put(RuleEngineConstants.PRODUCT_ID, productIds); 
			if(contractID >= 0)
				prameterMap_.put(RuleEngineConstants.CONTRACT_ID, contractID); 
			/*
			 * Send Product list to Rule Engine so that Rule Engine will create input for each product Id
			 * As a output we get all matched rules for each product Id.
			 */
			
			List<BusinessRuleIfc> outputList = null;
			try {
				outputList = BusinessRuleEngineUtility.evaluateAllRule(ctx,EventTypeEnum.DEPOSIT_DETERMINATION,
						account, subscriberList, prameterMap_);
			} catch (NoRuleFoundException e) {
				LogSupport.info(ctx, DetermineDepositSupport.class.getName(), "No Rule Found for subscription Types: "+subscriptionTypes);
				isRuleFound = false;
			}
			
			if(!isRuleFound)
			{
				return depositDetails;
			}
			
			
			if(outputList!=null && !outputList.isEmpty())
			{
				//Iterate all evaluated rules to get output
				for(BusinessRuleIfc outputObject : outputList)
				{
					
						output = outputObject;
						long productId = 0;
						depositOutputHolder = null;
						if(null!=output)
						{
							depositCriteriaList	= output.getDepositList();
							productId 			= output.getProductId();
							
							
							for(DepositCriteriaHolder depositCriteriaHolder:depositCriteriaList)
							{
								long subscriptionType = depositCriteriaHolder.getSubscriptionType();
							 
								if(!subscriptionTypes.contains(subscriptionType))
									continue;
							
								depositOutputHolder   = depositCriteriaHolder.getDepositDetails();
								
								
								for(DepositDetails depositDeails : depositOutputHolder)
								{
									amount 		= depositDeails.getAmount();
									depositType = depositDeails.getDepositType();
									
									DepositDetail depositDetail = new DepositDetail();
									
									depositDetail.setDepositTypeId(depositType);
									depositDetail.setSubscriptionType(subscriptionType);					
									depositDetail.setAmount(amount);
									if(!isnewSubscription)
									{
										depositDetail.setSubscriptionId(subscriberId);
									}
									depositDetail.setProductId(productId);
									if(null != productIdToPath.get(productId))
									{
										depositDetail.setProductPath(productIdToPath.get(productId));
									}
									
									depositDetails.add(depositDetail); 
								}
							}
						}
						
						
				}//end of for BusinessRuleIfc
			}
		}
		
		return depositDetails;
	}

	
	
	
}
