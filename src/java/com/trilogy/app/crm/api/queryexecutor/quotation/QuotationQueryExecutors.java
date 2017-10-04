/*
� * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.queryexecutor.quotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.DepositDetail;
import com.trilogy.app.crm.bean.DepositDetailHolder;
import com.trilogy.app.crm.bean.DepositItemReference;
import com.trilogy.app.crm.bean.QuotationActionTypeEnum;
import com.trilogy.app.crm.bean.QuotationOperationRequiredEnum;
import com.trilogy.app.crm.deposit.DetermineDepositSupport;
import com.trilogy.app.crm.discount.quote.DiscountForecastingImpl;
import com.trilogy.app.crm.discount.quote.QuoteDiscountException;
import com.trilogy.app.crm.discount.quote.QuoteDiscountRequest;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CreditCategorySupport;
import com.trilogy.app.crm.support.DefaultAccountTypeSupport;
import com.trilogy.app.crm.support.QuotationSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.quotation.GenerateQuoteResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.quotation.DepositResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.quotation.ItemReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.quotation.ProductDetails;
import com.trilogy.util.crmapi.wsdl.v3_0.types.quotation.QuotationResult;


/**
 * 
 * @author bdhavalshankh
 * @since 9_12
 */
public class QuotationQueryExecutors {
	/**
     * 
     * @author bdhavalshankh
     * @since 9_12
     * 
     * API implementation to get quote
     *
     */
    public static class GetQuoteQueryExecutor extends AbstractQueryExecutor<GenerateQuoteResponse>
    {
    	GenerateQuoteResponse response = new GenerateQuoteResponse();
    	
        public GetQuoteQueryExecutor()
        {
            
        }

        public GenerateQuoteResponse execute(Context mainCtx, Object... parameters) throws CRMExceptionFault
        {
            Context ctx = mainCtx.createSubContext();
            Map<String,Object> parameterMap = new HashMap<String,Object>();
            
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, 
            		PARAM_GENERIC_PARAMETERS_NAME, parameters);
            
          
           
			Integer spid = getParameter(ctx, SPID_PARAM_IDENTIFIER, 
					SPID_PARAM_OPERATION_CODE, Integer.class,
					parameters);
			String ban = getParameter(ctx, BAN_PARAM_IDENTIFIER,
					BAN_PARAM_OPERATION_CODE, String.class,
					parameters);
			int[] operations = getParameter(ctx, OPERATION_PARAM_IDENTIFIER,
					OPERATION_PARAM_OPERATION_CODE, int[].class,
					parameters);
			long accountType = getParameter(ctx, ACCOUNT_TYPE_PARAM_IDENTIFIER,
					ACCOUNT_TYPE_PARAM_OPERATION_CODE, Long.class,
					parameters);
			String discountScope = getParameter(ctx, DISCOUNT_SCOPE_PARAM_IDENTIFIER,
					DISCOUNT_SCOPE_PARAM_OPERATION_CODE, String.class,
					parameters);
			String discountGrade = getParameter(ctx, DISCOUNT_GRADE_PARAM_IDENTIFIER,
					DISCOUNT_GRADE_PARAM_OPERATION_CODE, String.class,
					parameters);
			long creditCategory = getParameter(ctx, CREDITCATEGORY_PARAM_IDENTIFIER,
					CREDITCATEGORY_PARAM_OPERATION_CODE, Long.class,
					parameters);
			ItemReference[] itemReferences = getParameter(ctx, ITEM_REFERENCE_PARAM_IDENTIFIER,
					ITEM_REFERENCE_PARAM_OPERATION_CODE, ItemReference[].class,
					parameters);
			
			RmiApiErrorHandlingSupport.validateMandatoryObject(creditCategory, CREDITCATEGORY_PARAM_OPERATION_CODE);
	        RmiApiErrorHandlingSupport.validateMandatoryObject(spid, SPID_PARAM_OPERATION_CODE);
	        RmiApiErrorHandlingSupport.validateMandatoryObject(ban, BAN_PARAM_OPERATION_CODE);
	        RmiApiErrorHandlingSupport.validateMandatoryObject(operations, OPERATION_PARAM_OPERATION_CODE);
	        RmiApiErrorHandlingSupport.validateMandatoryObject(accountType, ACCOUNT_TYPE_PARAM_OPERATION_CODE);
		    
	        validateItemReferenceMandatoryValues(ctx, itemReferences);
	        
			validateRequestParameters(ctx, spid, ban, accountType, creditCategory);
			
			boolean quoteDeposit = Boolean.FALSE;
			boolean quoteDiscount = Boolean.FALSE;
			for (int operation : operations) 
			{
				if(operation == QuotationOperationRequiredEnum.CALCULATE_DEPOSIT_INDEX)
				{
					quoteDeposit = Boolean.TRUE;
				}
				else if(operation == QuotationOperationRequiredEnum.CALCULATE_DISCOUNT_INDEX)
				{
					quoteDiscount = Boolean.TRUE;
				}
			}

			QuotationResult[] quotationResult = null; 
	    	List<QuotationResult> quotationDepositResults = new ArrayList<QuotationResult>();
	    	List<QuotationResult> quotationDiscountResults = new ArrayList<QuotationResult>();
	    	List<QuotationResult> allQuoteResults = new ArrayList<QuotationResult>();
	    	
	    	if(quoteDeposit)
	    	{
	    		for (ItemReference itemReference : itemReferences) 
				{
					LogSupport.debug(ctx, this, "Processing request for item reference correlation id : "+itemReference.getCorrelationID());
					if(itemReference.getActionType() == QuotationActionTypeEnum.ADD_INDEX)
					{
						DepositItemReference depositItemReference = QuotationSupport.adaptApiItemToDepositReference(ctx, itemReference);
						DepositDetailHolder depositDetailHolder;
						try 
						{
							depositDetailHolder = DetermineDepositSupport.determineDeposit(ctx, spid, ban, 
								(int)creditCategory, accountType, depositItemReference);
						} 
						catch (HomeException e) 
						{
							throw new CRMExceptionFault(e);
						}
						
						if(null != depositDetailHolder
								&& !depositDetailHolder.getDepositDetails().isEmpty()
								&& depositDetailHolder.getDepositDetails().size() > 0)
						{
							List<DepositDetail> depositDetails = depositDetailHolder.getDepositDetails();
							if(null != depositDetails 
									&& !depositDetails.isEmpty())
							{
								List<DepositResult> depositResultList = new ArrayList<DepositResult>();
								for (DepositDetail depositDetail : depositDetails) 
								{
									DepositResult apiDepositResult = new DepositResult();  
									QuotationSupport.adaptCrmToApiDepositResult(ctx, depositDetail, apiDepositResult);
									depositResultList.add(apiDepositResult);
								}
								DepositResult[] depositResult = new DepositResult[] {};
								depositResult = depositResultList.toArray(new DepositResult[0]);
								
								QuotationResult quoteResult = new QuotationResult();
								quoteResult.setCorrelationID(itemReference.getCorrelationID());
								quoteResult.setDepositResult(depositResult);
								quoteResult.setParameters(itemReference.getParameters());
								
								quotationDepositResults.add(quoteResult);						
							}
						}
					}
				}
	    	}
			
			if(quoteDiscount)
			{
					QuoteDiscountRequest request = QuotationSupport.adaptApiItemToDiscountReference(ctx, spid,ban,operations,accountType,discountScope,
							                        discountGrade,creditCategory,itemReferences);
					
					try
					{
						DiscountForecastingImpl impl = new DiscountForecastingImpl();
						impl.foreCastDiscount(ctx, request);
						quotationDiscountResults = impl.getDiscountQuoteResult(ctx);
					}
					catch(QuoteDiscountException e)
					{
						if (LogSupport.isDebugEnabled(ctx)){
					    	LogSupport.debug(ctx, this, e.getMessage());
					    }
						throw new CRMExceptionFault(e.getMessage());
					}
					
			}
			
			if(!quotationDiscountResults.isEmpty()
					&& quotationDiscountResults.size() > 0)
			{
				for (QuotationResult discountResult : quotationDiscountResults)
				{
					QuotationResult quoteResult = new QuotationResult();
					quoteResult.setCorrelationID(discountResult.getCorrelationID());
					quoteResult.setDiscountResult(discountResult.getDiscountResult());
					
					for (Iterator iterator = quotationDepositResults.iterator(); iterator
							.hasNext();) 
					{
						QuotationResult depositResult = (QuotationResult) iterator
								.next();
						
						if(discountResult.getCorrelationID()
								.equals(depositResult.getCorrelationID()))
						{
							quoteResult.setDepositResult(depositResult.getDepositResult());
							iterator.remove();
							break;
						}
					}
					allQuoteResults.add(quoteResult);
				}
			}
			
			allQuoteResults.addAll(quotationDepositResults);
			
			quotationResult = new QuotationResult[]{};
			quotationResult = allQuoteResults.toArray(
			new QuotationResult[0]);
			
			response.setQuotationResult(quotationResult);
			
            return response;
        }

        private void validateItemReferenceMandatoryValues(Context ctx,
				ItemReference[] itemReferences) throws CRMExceptionFault {
        	
        	for (ItemReference itemReference : itemReferences) 
        	{
        		String id = itemReference.getCorrelationID();
        		RmiApiErrorHandlingSupport.validateMandatoryObject(id, ITEM_REFERENCE_CORRELATION_ID);
        		
        		RmiApiErrorHandlingSupport.validateMandatoryObject(itemReference.getActionType(), ITEM_REFERENCE_CORRELATION_ID);
        		RmiApiErrorHandlingSupport.validateMandatoryObject(itemReference.getSubscriptionType(), ITEM_REFERENCE_SUBSCRIPTION_TYPE);
        		RmiApiErrorHandlingSupport.validateMandatoryObject(itemReference.getDepositCategory(), ITEM_REFERENCE_DEPOSIT_CATEGORY);
        		RmiApiErrorHandlingSupport.validateMandatoryObject(itemReference.getOfferId(), ITEM_REFERENCE_OFFER_ID);

        		
        		ProductDetails[] details =  itemReference.getProductReference();
        		for (ProductDetails productDetails : details) 
        		{
        			RmiApiErrorHandlingSupport.validateMandatoryObject(productDetails.getProductId(), PRODUCT_REFERENCE_PRODUCT_ID);
            		RmiApiErrorHandlingSupport.validateMandatoryObject(productDetails.getCharge(), PRODUCT_REFERENCE_PRODUCT_CHARGE);
            		RmiApiErrorHandlingSupport.validateMandatoryObject(productDetails.getProductPath(), PRODUCT_REFERENCE_PRODUCT_PATH);
				}

			}
		}

		private void validateRequestParameters(Context ctx, Integer spid, String ban,
				long accountType, long creditCategory) throws CRMExceptionFault
        {
        	Account account = null;
        	CRMSpid crmSpid = null;
        	AccountCategory accntType = null;
        	CreditCategory cc = null;
        	/**
        	 * Validate Account
        	 */
        	try{
        		 account = AccountSupport.getAccount(ctx, ban);
        	}catch(HomeException e){
        		throw new CRMExceptionFault("Problem occurred while fetching Account for ban : "+ban);
        	}
        	if(null == account){
        		throw new CRMExceptionFault("Account does not exist with ban : "+ban);
        	}
        	else
        	{
        		if(account.getState() == AccountStateEnum.INACTIVE)
        		{
        			throw new CRMExceptionFault("Account with ban : "+ban+" is inactive in the system");	
        		}
        	}
        	
        	/**
        	 * Validate Spid 
        	 */
        	try{
        	crmSpid = SpidSupport.getCRMSpid(ctx, spid);
        	}catch(HomeException e){
        		throw new CRMExceptionFault("Problem occurred while fetching spid for id : "+spid);
        	}
        	if(null == crmSpid){
        		throw new CRMExceptionFault("Spid does not exist with id : "+spid);
        	}

        	/**
        	 * Validate Account category
        	 */
        	accntType = DefaultAccountTypeSupport.instance().getTypedAccountType(ctx, accountType);
        	if(null == accntType){
        		throw new CRMExceptionFault("Account Type does not exist with id : "+accountType);
        	}
        	
        	/**
        	 * Validate Credit category
        	 */
        	try{
        	cc = CreditCategorySupport.findCreditCategory(ctx, (int)creditCategory);
        	}catch(HomeException e){
        		throw new CRMExceptionFault("Problem occurred while fetching credit category for id : "+creditCategory);
        	}
        	if(null == cc){
        		throw new CRMExceptionFault("Credit category does not exist with id : "+creditCategory);
        	}
        		
		}

		public static final int PARAM_HEADER = 0;
		public static final int SPID_PARAM_IDENTIFIER = 1;
		public static final int BAN_PARAM_IDENTIFIER = 2;
		public static final int OPERATION_PARAM_IDENTIFIER = 3;
		public static final int ACCOUNT_TYPE_PARAM_IDENTIFIER = 4;
		public static final int DISCOUNT_SCOPE_PARAM_IDENTIFIER = 5;
		public static final int DISCOUNT_GRADE_PARAM_IDENTIFIER = 6;
		public static final int CREDITCATEGORY_PARAM_IDENTIFIER = 7;
		public static final int ITEM_REFERENCE_PARAM_IDENTIFIER = 8;
	    public static final int PARAM_GENERIC_PARAMETERS = 9;


		public static final String SPID_PARAM_OPERATION_CODE = "spid";
		public static final String BAN_PARAM_OPERATION_CODE = "ban";
		public static final String OPERATION_PARAM_OPERATION_CODE = "operation";
		public static final String ACCOUNT_TYPE_PARAM_OPERATION_CODE = "accountType";
		public static final String DISCOUNT_SCOPE_PARAM_OPERATION_CODE = "discountScope";
		public static final String DISCOUNT_GRADE_PARAM_OPERATION_CODE = "discountGrade";
		public static final String CREDITCATEGORY_PARAM_OPERATION_CODE = "creditCategory";
		public static final String ITEM_REFERENCE_PARAM_OPERATION_CODE = "itemReference";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	    
	    public static final String ITEM_REFERENCE_CORRELATION_ID= "correlationID";
	    public static final String ITEM_REFERENCE_ACTION_TYPE= "actionType";
	    public static final String ITEM_REFERENCE_SUBSCRIPTION_TYPE= "subscriptionType";
	    public static final String ITEM_REFERENCE_DEPOSIT_CATEGORY= "depositCategory";
	    public static final String ITEM_REFERENCE_OFFER_ID= "offerId";
	    public static final String PRODUCT_REFERENCE_PRODUCT_ID= "productId";
	    public static final String PRODUCT_REFERENCE_PRODUCT_CHARGE= "charge";
	    public static final String PRODUCT_REFERENCE_PRODUCT_PATH= "productPath";
	   
	    public boolean validateParameterTypes(Class<?>[] parameterTypes) {
			boolean result = true;
			result = result && (parameterTypes.length >= 10);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
			result = result && int.class.isAssignableFrom(parameterTypes[SPID_PARAM_IDENTIFIER]);	
			result = result && String.class.isAssignableFrom(parameterTypes[BAN_PARAM_IDENTIFIER]);	
			result = result && int[].class.isAssignableFrom(parameterTypes[OPERATION_PARAM_IDENTIFIER]);	
			result = result && long.class.isAssignableFrom(parameterTypes[ACCOUNT_TYPE_PARAM_IDENTIFIER]);	
			result = result && String.class.isAssignableFrom(parameterTypes[DISCOUNT_SCOPE_PARAM_IDENTIFIER]);	
			result = result && String.class.isAssignableFrom(parameterTypes[DISCOUNT_GRADE_PARAM_IDENTIFIER]);	
			result = result && long.class.isAssignableFrom(parameterTypes[CREDITCATEGORY_PARAM_IDENTIFIER]);	
			result = result && ItemReference[].class.isAssignableFrom(parameterTypes[ITEM_REFERENCE_PARAM_IDENTIFIER]);	
			result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);	
			                                

			return result;
		}

		public boolean validateReturnType(Class<?> resultType) {
			return GenerateQuoteResponse.class.isAssignableFrom(resultType);
		}

		@Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[10];
	            result[PARAM_HEADER] = parameters[0];
	            
	            result[SPID_PARAM_IDENTIFIER] = getParameter(ctx, SPID_PARAM_IDENTIFIER,SPID_PARAM_OPERATION_CODE, Integer.class,                   
	            					parameters);                                                
	            result[BAN_PARAM_IDENTIFIER] = getParameter(ctx, BAN_PARAM_IDENTIFIER, BAN_PARAM_OPERATION_CODE, String.class,                     
	            				parameters);                                                
	            result[OPERATION_PARAM_IDENTIFIER] = getParameter(ctx, OPERATION_PARAM_IDENTIFIER, OPERATION_PARAM_OPERATION_CODE, Integer[].class,            
	            				parameters);                                                
	            result[ACCOUNT_TYPE_PARAM_IDENTIFIER] = getParameter(ctx, ACCOUNT_TYPE_PARAM_IDENTIFIER, ACCOUNT_TYPE_PARAM_OPERATION_CODE, Long.class,              
	            				parameters);                                                
	            result[DISCOUNT_SCOPE_PARAM_IDENTIFIER] = getParameter(ctx, DISCOUNT_SCOPE_PARAM_IDENTIFIER, DISCOUNT_SCOPE_PARAM_OPERATION_CODE, String.class,          
	            				parameters);                                                
	            result[DISCOUNT_GRADE_PARAM_IDENTIFIER] = getParameter(ctx, DISCOUNT_GRADE_PARAM_IDENTIFIER, DISCOUNT_GRADE_PARAM_OPERATION_CODE, String.class,          
	            				parameters);                                                
	            result[CREDITCATEGORY_PARAM_IDENTIFIER] = getParameter(ctx, CREDITCATEGORY_PARAM_IDENTIFIER, CREDITCATEGORY_PARAM_OPERATION_CODE, Long.class,            
	            				parameters);                                                
	            result[ITEM_REFERENCE_PARAM_IDENTIFIER] = getParameter(ctx, ITEM_REFERENCE_PARAM_IDENTIFIER, ITEM_REFERENCE_PARAM_OPERATION_CODE, ItemReference[].class, 
	            				parameters);                                                
	            result[PARAM_GENERIC_PARAMETERS] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }
	        
	        return result;
	    }

    }
    
}
