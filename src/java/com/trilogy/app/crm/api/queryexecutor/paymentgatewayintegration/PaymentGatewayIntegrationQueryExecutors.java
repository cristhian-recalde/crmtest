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
package com.trilogy.app.crm.api.queryexecutor.paymentgatewayintegration;

import static com.redknee.app.crm.bean.paymentgatewayintegration.PaymentGatewayResponseCodes.*;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import billsoft.eztax.ZipAddress;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillingOptionEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChannelTypeEnum;
import com.trilogy.app.crm.bean.CreditCardToken;
import com.trilogy.app.crm.bean.CreditCardTokenHome;
import com.trilogy.app.crm.bean.CreditCardTokenXInfo;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PGILogSupport;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PGINoteSupport;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayEntitiesAdapter;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayException;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayIntegrationConstants;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayResponseCodes;
import com.trilogy.app.crm.bean.paymentgatewayintegration.SubscriptionSupport;
import com.trilogy.app.crm.bean.paymentgatewayintegration.TaxSupport;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.paymentgatewayintegration.PaymentGatewaySupportHelper;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CurrencyPrecisionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.support.UserGroupSupport;
import com.trilogy.app.crm.validator.UserDailyAdjustmentLimitTransactionValidator;
import com.trilogy.product.s2100.ErrorCode;
import com.trilogy.product.s2100.oasis.param.Parameter;
import com.trilogy.product.s2100.oasis.param.ParameterID;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.exception.CRMExceptionFactory;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.ApplyPaymentChargeRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.ApplyPaymentChargeResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.BillingSystemResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.CalculatePaymentTaxRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.CalculatePaymentTaxResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.ChargingGatewayResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.CreateScheduleRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.CreateScheduleResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.CreateTokenRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.CreateTokenResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.DeleteScheduleRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.DeleteScheduleResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.DeleteTokenRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.DeleteTokenResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.PaymentGatewayResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.ReadSchedulesRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.ReadSchedulesResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.ReadTokensCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.ReadTokensRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.ReadTokensResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.Schedule;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.Token;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.UpdateScheduleRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.UpdateScheduleResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.UpdateTokenRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.UpdateTokenResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;

/**
 * 
 * @author Marcio Marques
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 * @author Mangaraj Sahoo
 * @since 9.3
 *
 */
public class PaymentGatewayIntegrationQueryExecutors 
{
	
	
    /**
	 * Implements method createToken
	 * @author Marcio Marques
	 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
	 * @since 9.3
	 *
	 */
	public static class TokenCreationQueryExecutor extends AbstractQueryExecutor<CreateTokenResponse>
	{
		public TokenCreationQueryExecutor()
		{
			
		}
		
		private CreateTokenResponse createResponse(int code , String message , Token token)
		{
			CreateTokenResponse response = new CreateTokenResponse();
			response.setStatusCode(Long.valueOf(code));
			response.setStatusMessage(message);
			
			if(code == PaymentGatewayResponseCodes.SUCCESS)
			{
				response.setToken(token);
			}
			
			return response;
		}
		
		private CreateTokenResponse createResponse(PaymentGatewayException e)
		{
			CreateTokenResponse response = new CreateTokenResponse();
			response.setStatusCode(Long.valueOf(e.getErrorCode()));
			response.setStatusMessage(e.getMessage());
			response.setToken(null);
			
			return response;
		}		

	    public CreateTokenResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
	    	CreateTokenRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, CreateTokenRequest.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

	        RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
	        CreateTokenResponse response = null;

	        PGILogSupport.logApiStart(ctx, this.getClass(), "createToken", "BAN: " + request.getAccountID() + " Token Value: " + request.getTokenValue());
	        try
	        {
	        	Home tokenHome = (Home)ctx.get(PaymentGatewayIntegrationConstants.CREATE_TOKEN_HOME_PIPELINE_KEY);
	        	CreditCardToken token = (CreditCardToken)tokenHome.create(ctx, request);
	        	
                Token apiToken = new Token();              
                PaymentGatewayEntitiesAdapter.adapt(apiToken, token);

                response = createResponse(PaymentGatewayResponseCodes.SUCCESS,
	        			"Token successfully created for Account with id:" + request.getAccountID(),
	        			apiToken);
                //PGINoteSupport.addAccountNote(ctx, request.getAccountID(), "Account:createToken: Credit Card Token[Id:" + apiToken.getTokenID()
                	//	+ " , Credit Card Number:" + apiToken.getMaskedCardNumber() + "] successfully associated.");
                PGINoteSupport.addAccountNoteForCreateToken(ctx, token.getBan(), token);
            }
	        catch (HomeException e)
	        {
	        	Throwable t = e.getCause();
	        	
	        	if(t instanceof PaymentGatewayException)
	        	{
	        		PaymentGatewayException pge = (PaymentGatewayException)t;
	        		
	        		PGILogSupport.logApiEnd(ctx, getClass(), "createToken", pge.getErrorCode() , pge.getMessage());
	        		
	        		PGINoteSupport.addAccountNoteForCreateTokenFailure(ctx, request.getAccountID(), pge);
	        			
	        		return createResponse(pge);
	        	} 
	        	else 
	        	{
	        		PGILogSupport.logApiEnd(ctx, getClass(), "createToken", null , "Exception: " + e.getMessage());
	        		PGINoteSupport.addAccountNoteForCreateTokenFailure(ctx, request.getAccountID(), e);
	        		throw CRMExceptionFactory.create(e);
	        	}
	        }
	        catch (Exception e)
	        {
	        	PGILogSupport.logApiEnd(ctx, getClass(), "createToken", null , "Exception: " + e.getMessage());
	        	PGINoteSupport.addAccountNoteForCreateTokenFailure(ctx, request.getAccountID(), e);
	        	throw CRMExceptionFactory.create(e);
	        }
	        
	        return response;
	    }
	    
	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[3];
	            result[0] = parameters[0];
	            result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, CreateTokenRequest.class, parameters);
                result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }
	        
	        return result;
	    }

	    @Override
	    public boolean validateParameterTypes(Class<?>[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=3);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && CreateTokenRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return CreateTokenResponse.class.isAssignableFrom(resultType);
        }

	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
	    
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}

	/**
     * Implements method readTokens
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class TokensQueryExecutor extends AbstractQueryExecutor<ReadTokensResponse>
    {
    	
    	private ReadTokensResponse createResponse(int errorCode , String message)
    	{
    		ReadTokensResponse response  =  new ReadTokensResponse();
    		
    		response.setStatusCode(Long.valueOf(errorCode));
    		response.setStatusMessage(message);
    		
    		return response;
    	}
    	
        public TokensQueryExecutor()
        {
            
        }

        private ReadTokensResponse validateAccount(Context ctx, Account account, String accountId)
        {
    		if(account == null)
    		{
    			String message = "Account with id:" + accountId + " does not exist in the system";
    			PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", NO_SUCH_BAN, message);
    			return createResponse(NO_SUCH_BAN, message);
    		}
    		
    		/* TT#12072518010
    		 * if(!AccountStateEnum.ACTIVE.equals(account.getState()) && account.isPrepaid())
    		{
    			String message = "Account[State:" + account.getState().getDescription() + "] with id:" + account.getBAN() + " is not Active.";
    			PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", INVALID_PARAMETER, message);
    			return createResponse(INVALID_PARAMETER, message);            			
    		}
    		*/
    		
    		if(!account.isResponsible())
    		{
    			String message = "Account with id:" + account.getBAN() + " is not Responsible.";
    			PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", INVALID_PARAMETER, message);
    			return createResponse(INVALID_PARAMETER, message);
    		}
    		
    		return null;
        }
        
        public ReadTokensResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            ReadTokensRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, ReadTokensRequest.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
            
            Context subCtx = ctx.createSubContext();
            SubscriptionReference subRef = request.getCriteria().getSubscriptionRef();
            ReadTokensResponse response = null;
            Subscriber subscriber = null;
            Home tokenHome = (Home)ctx.get(CreditCardTokenHome.class);

    		ReadTokensCriteria criteria = request.getCriteria();
    		
    		String accountId = criteria.getAccountID();
    		Long tokenId = criteria.getTokenID();
    		String tokenValue = criteria.getTokenValue();
    		
    		PGILogSupport.logApiStart(ctx, getClass(), "readTokens", "Account Id: " + accountId + " Token Id: " + tokenId + " Token Value: " + tokenValue);
    		
    		if( (accountId == null || "".equals(accountId))
    			&& (tokenId == null)
    			&& (tokenValue == null || "".equals(tokenValue) )
    			&& (subRef == null)
    			)
    		{
    			String message = "Atleast one of the parameters in the request should have a value.";
    			PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", INVALID_PARAMETER, message);
    			return createResponse(INVALID_PARAMETER, message);
    		}            

            if(subRef != null)
            {
            	try
            	{
            		subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subRef, this);
            	}
            	catch (CRMExceptionFault e)
            	{
            		return createResponse(NO_SUCH_SUBSCRIPTION, e.getFaultMessage().getCRMException().getMessage());
            	}
            	
            	if(subscriber == null)
            	{
            		return createResponse(NO_SUCH_SUBSCRIPTION, "Subscription [" + subRef.getIdentifier() + " / " + subRef.getMobileNumber() + " does not exist.");
            	}
            }
            
            And where = new And();
            
            if(verifyStringHasContent(accountId))
            {
            	try
            	{
            		Account account = AccountSupport.getAccount(ctx, accountId);
            		

            		
            		ReadTokensResponse readResponse = validateAccount(ctx, account, accountId);
            		
            		if(readResponse != null)
            		{
            			return readResponse;
            		}
            	}
            	catch (HomeException e)
            	{
            		PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", null, e.getMessage());
            		throw CRMExceptionFactory.create(e);
            	}
            	where.add(new EQ(CreditCardTokenXInfo.BAN, accountId));            	
            }
            if(verifyStringHasContent(tokenValue))
            {
            	try
            	{
            		Collection<CreditCardToken> tokens = HomeSupportHelper.get(ctx).getBeans(
            				ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.VALUE , tokenValue));
            		if(tokens.isEmpty())
            		{
            			String message = "No token with value:" + tokenValue + " exists in the system.";
            			PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", NO_SUCH_TOKEN_VALUE, message);
            			return createResponse(NO_SUCH_TOKEN_VALUE, message);
            		}
            	}
            	catch (HomeException e)
            	{
            		PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", null, e.getMessage());
            		throw CRMExceptionFactory.create(e);
            	}
            	where.add(new EQ(CreditCardTokenXInfo.VALUE, tokenValue));
            }
            if(tokenId != null)
            {
            	try
            	{
            		Collection<CreditCardToken> tokens = HomeSupportHelper.get(ctx).getBeans(
            				ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID , tokenId));
            		if(tokens.isEmpty())
            		{
            			String message = "No token with id:" + tokenId + " exists in the system.";
            			PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", NO_SUCH_TOKEN_ID, message);
            			return createResponse(NO_SUCH_TOKEN_ID, message);
            		}
            	}
            	catch (HomeException e)
            	{
            		PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", null, e.getMessage());
            		throw CRMExceptionFactory.create(e);
            	}
            	where.add(new EQ(CreditCardTokenXInfo.ID , tokenId));
            }
            if(accountId == null && subscriber != null)
            {
            	try {
					Account account = AccountSupport.getAccount(ctx, subscriber.getBAN());
					
					ReadTokensResponse readResponse = validateAccount(ctx, account, accountId);
					
					if(readResponse != null)
					{
						return readResponse;
					}
					
				} catch (HomeException e) {

            		PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", null, e.getMessage());
            		throw CRMExceptionFactory.create(e);
				}
            	where.add(new EQ(CreditCardTokenXInfo.BAN, subscriber.getBAN()));
            }
            
            try
            {
            	Collection<CreditCardToken> tokens = HomeSupportHelper.get(ctx).getBeans(ctx, CreditCardToken.class, where);
            	
            	response = createResponse(SUCCESS, "ReadToken successful.");
            	for (CreditCardToken token : tokens)
            	{
            		Token responseToken = new Token();
            		
            		PaymentGatewayEntitiesAdapter.adapt(responseToken, token);
            		
            		response.addTokens(responseToken);
            	}
            }
            catch(HomeException e)
            {
            	PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", null, e.getMessage());
            	throw CRMExceptionFactory.create(e);
            }
            
            PGILogSupport.logApiEnd(ctx, getClass(), "readTokens", SUCCESS, "Tokens successfully read");
            
            return response;
            //TODO: Implement method.
        }
        
        private boolean verifyStringHasContent(String value)
        {
        	return ( value != null && !"".equals(value) );
        			
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, ReadTokensRequest.class, parameters);
                result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && ReadTokensRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return ReadTokensResponse.class.isAssignableFrom(resultType);
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    /**
     * Implements method updateToken
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class TokenUpdateQueryExecutor extends AbstractQueryExecutor<UpdateTokenResponse>
    {
        public TokenUpdateQueryExecutor()
        {
            
        }

    	private UpdateTokenResponse createResponse(int errorCode , String message)
    	{
    		UpdateTokenResponse response  =  new UpdateTokenResponse();
    		
    		response.setStatusCode(Long.valueOf(errorCode));
    		response.setStatusMessage(message);
    		
    		return response;
    	}
    	
        public UpdateTokenResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            UpdateTokenRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, UpdateTokenRequest.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
            
            GenericParameterParser parser = new GenericParameterParser(request.getTokenFields());
            
            UpdateTokenResponse response = null;
            Long tokenId = request.getTokenID();
            
            try
            {
            	PGILogSupport.logApiStart(ctx, getClass(), "updateToken", "Update Token with id: "  + tokenId);
            	CreditCardToken token = (CreditCardToken)HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID , tokenId));
            	if(token == null)
            	{
            		String message = "Token with id:" + tokenId + " is not present in the System.";
            		PGILogSupport.logApiEnd(ctx, this.getClass(), "updateToken" , NO_SUCH_TOKEN_ID,  message);
            		return createResponse(NO_SUCH_TOKEN_ID, message);
            	}
            	
            	String newTokenValue = parser.getParameter(TOKEN_VALUE, String.class);
            	String newExpiryDate = parser.getParameter(EXPIRY_DATE, String.class);
            	String newMaskedCardNumber = parser.getParameter(MASKED_CARD_NUMBER, String.class);
            	
            	if(newTokenValue == null && newExpiryDate == null && newMaskedCardNumber == null)
            	{
            		return createResponse(INVALID_PARAMETER, "Atleast one parameter from [" 
            				+ TOKEN_VALUE + "," + EXPIRY_DATE + "," + MASKED_CARD_NUMBER 
            				+ " should be present so that the token can be updated.");
            	}
            	
            	if(newTokenValue != null)
            	{
            		token.setValue(newTokenValue);
            	}
            	
            	if ( newExpiryDate != null )
            	{
            		token.setExpiryDate(newExpiryDate);
            	}
            	
            	if( newMaskedCardNumber != null )
            	{
            		token.setMaskedCreditCardNumber(newMaskedCardNumber);
            	}
            	
            	HomeSupportHelper.get(ctx).storeBean(ctx, token);
            	
            	response = new UpdateTokenResponse();
            	response.setStatusCode(Long.valueOf(SUCCESS));
            	response.setStatusMessage("Token with id:" + tokenId + " updated successfully.");
            	PGINoteSupport.addAccountNoteForUpdateToken(ctx, token.getBan(), token);
            	Token responseToken = new Token();
            	PaymentGatewayEntitiesAdapter.adapt(responseToken, token);
            	response.setToken(responseToken);
            	
            }
            catch ( HomeException e) 
            {
            	throw CRMExceptionFactory.create(e);
            }
            
            return response;
        }
        
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, UpdateTokenRequest.class, parameters);
                result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && UpdateTokenRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return UpdateTokenResponse.class.isAssignableFrom(resultType);
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        
        public static final String TOKEN_VALUE = "TOKEN_VALUE";
        public static final String MASKED_CARD_NUMBER = "MASKED_CARD_NUMBER";
        public static final String EXPIRY_DATE = "EXPIRY_DATE";

    }

    /**
     * Implements method deleteToken
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class TokenRemovalQueryExecutor extends AbstractQueryExecutor<DeleteTokenResponse>
    {
        public TokenRemovalQueryExecutor()
        {
            
        }

        private DeleteTokenResponse createResponse(long code, String message)
        {
        	DeleteTokenResponse response = new DeleteTokenResponse();
        	response.setStatusCode(code);
        	response.setStatusMessage(message);
        	return response;
        }
        
        public DeleteTokenResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            DeleteTokenRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, DeleteTokenRequest.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
            
            Long tokenId = request.getTokenID();
            DeleteTokenResponse response = null;
            
            try
            {
            	if(tokenId == null)
            	{
            		return createResponse(INVALID_PARAMETER, "Token ID is mandatory. Null token id received.");
            	}
            	
            	CreditCardToken token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID, tokenId));
            	
            	if(token == null)
            	{
            		LogSupport.info(ctx, this, "Could not add Account Note as Token for id:" + tokenId + " is either already deleted or does not exist.");
            		return createResponse(SUCCESS , "Token [" + tokenId + "] successfully deleted.");
            	}
            	
            	HomeSupportHelper.get(ctx).removeBean(ctx, token);
            	PGINoteSupport.addAccountNoteForDeleteToken(ctx, token.getBan(), token);
            	
            	Collection<TopUpSchedule> schedules = HomeSupportHelper.get(ctx).getBeans(ctx, TopUpSchedule.class, new EQ(TopUpScheduleXInfo.TOKEN_ID, tokenId));
            	
            	StringBuilder scheduleIds = new StringBuilder();
            	for(TopUpSchedule bean : schedules)
            	{            		
            		scheduleIds.append(bean.getId()).append(',');            		
            		HomeSupportHelper.get(ctx).removeBean(ctx, bean);
            		PGINoteSupport.addAccountNoteForDeleteSchedule(ctx, token.getBan(), bean);
            	}
            	
            	response = createResponse(SUCCESS , "Token [" + tokenId + "] successfully deleted. Associated Schedules[" + scheduleIds + "] also deleted from the System.");
            	Token apiToken = new Token();
            	PaymentGatewayEntitiesAdapter.adapt(apiToken, token);
            	response.setToken(apiToken);
            	
            	
            }
            catch ( HomeException e )
            {
            	LogSupport.major(ctx, this, "Could not add Account Note due to exception", e);
            	throw CRMExceptionFactory.create(e);
            }
            catch (Exception e)
            {
            	LogSupport.major(ctx, this, "Could not add Account Note due to exception", e);
            	throw CRMExceptionFactory.create(e);
            }
            
            return response;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, DeleteTokenRequest.class, parameters);
                result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && DeleteTokenRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return DeleteTokenResponse.class.isAssignableFrom(resultType);
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    /**
     * Implements method createSchedule
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class ScheduleCreationQueryExecutor extends AbstractQueryExecutor<CreateScheduleResponse>
    {
        public ScheduleCreationQueryExecutor()
        {
            
        }

        private CreateScheduleResponse createResponse(long code , String message)
        {
        	CreateScheduleResponse response = new CreateScheduleResponse();
        	response.setStatusCode(code);
        	response.setStatusMessage(message);
        	return response;
        }
        
        public CreateScheduleResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            CreateScheduleRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, CreateScheduleRequest.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
            
            CreateScheduleResponse response = null;
            
            Account account = null;
            Subscriber subscriber = null;
            String accountID = null;
            try
            {
            	subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, request.getSubscriptionRef(), this);
            }
            catch (CRMExceptionFault e)
            {
            	SubscriptionReference subscriptionReference = request.getSubscriptionRef();
            	if (subscriptionReference != null)
                {
                	final String identifier = subscriptionReference.getIdentifier();
                    final String mobileNumber = subscriptionReference.getMobileNumber();
                    accountID = subscriptionReference.getAccountID();
                    if ((identifier == null || identifier.length() == 0) && (mobileNumber == null || mobileNumber.length() == 0))
                    {
                        if (accountID == null || accountID.length() == 0)
                        {
                        	final String msg = "Either provide identifier or mobileNumber or accountID in Subscription Reference.";
    	        			LogSupport.minor(ctx, this, msg);
                        	return createResponse(INVALID_PARAMETER, msg);
                        }
                        else
                        {
                        	try
                    		{
                    			account = AccountSupport.getAccount(ctx, accountID);
                    		}
                    		catch (HomeException he)
                        	{
                    			final String msg = "Account with accountID : [" + accountID + "], does not exist in the system.";
        	        			LogSupport.minor(ctx, this, msg);
                    			return createResponse(NO_SUCH_BAN, msg);
                        	}
                        }
                    }
                    else
                    {
                    	final String msg = e.getFaultMessage().getCRMException().getMessage();
	        			LogSupport.minor(ctx, this, msg);
                    	return createResponse(NO_SUCH_SUBSCRIPTION, msg);
                    }
                } 
            	else
            	{
            		final String msg = e.getFaultMessage().getCRMException().getMessage();
        			LogSupport.minor(ctx, this, msg);
            		return createResponse(NO_SUCH_SUBSCRIPTION, msg);
            	}
            }

            if (subscriber != null)
            {
            	if (subscriber.isPrepaid())
            	{
            	    if(SubscriberSupport.isCCAtuScheduleAllowed(ctx, subscriber))
            	    {
            	        return createScheduleForPrepaidSubscriberAccount(ctx, request, subscriber);
            	    }
            	    else
            	    {
            	        final String msg = "CC ATU schedule creation not allowed by Service Provider.";
                        LogSupport.major(ctx, this, msg);
                        return createResponse(NO_SCHDEULE_CREATION_ALLOWED, msg);
            	    }
            	}
            	if (subscriber.isPostpaid())
                {
            		return createScheduleForPostpaidSubscriberAccount(ctx, request, subscriber);
                }
            } 
            else if (account != null)
            {
            	if(account.isResponsible())
            	{
            		Subscriber sub = null;
            		if(account.isIndividual(ctx)){
            			try{
            				sub = HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class, new And().add(new EQ(SubscriberXInfo.BAN, account.getBAN())).add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE)));
            				account.setOwnerMSISDN(sub.getMsisdn());

            			}catch (Exception e){
            				final String msg = "createSchedule: Responsible Account with accountID : [" + accountID + "] does not have a valid subscription.";
            				LogSupport.minor(ctx, this, msg);
            				return createResponse(INVALID_PARAMETER, msg);
            			}
            		}
            		return createScheduleForPostpaidGroupAccount(ctx,request,account);
            	}
              	else
              	{
              		final String msg = "createSchedule for Non Responsible Account with accountID : [" + accountID + "] and without identifier or mobileNumber in Subscription Reference is not suppported.";
        			LogSupport.minor(ctx, this, msg);
              		return createResponse(INVALID_PARAMETER, msg);
              	}

            } 
            else
            {
          		final String msg = "Cannot retrieve Account / Subscription from system using Identifier or mobileNumber or accountID passed in Subscription Reference.";
    			LogSupport.minor(ctx, this, msg);
            	return createResponse(INVALID_PARAMETER, msg);
            }
            return response;
        }
        
        private CreateScheduleResponse createScheduleForPrepaidSubscriberAccount(Context ctx, CreateScheduleRequest request, Subscriber subscriber) throws CRMExceptionFault
        {
        	if (subscriber == null)
        	{
        		final String msg = "Subscription does not exist in the system.";
    			LogSupport.minor(ctx, this, msg);
        		return createResponse(NO_SUCH_SUBSCRIPTION, msg);
        	}
        	/* TT#12072518010
        	 * if(!SubscriberStateEnum.ACTIVE.equals(subscriber.getState()))
            {
            	String message = "Subscription State[" + subscriber.getState() + "] is not Active.";
            	PGINoteSupport.addAccountNoteForCreateScheduleFailure(ctx, subscriber.getBAN() , message);
            	PGILogSupport.logApiEnd(ctx, this.getClass(), "createSchedule", INVALID_PARAMETER, message);
            	return createResponse(INVALID_PARAMETER, message);
            }*/
            
            CreateScheduleResponse response = null;
            try
            {
            	Long tokenId = request.getTokenID();
            	if(tokenId == null || tokenId < 1)
            	{
            		String message = "Invalid/NULL Token ID.";
            		PGINoteSupport.addAccountNoteForCreateScheduleFailure(ctx, subscriber.getBAN(), message);
                	PGILogSupport.logApiEnd(ctx, this.getClass(), "createSchedule", INVALID_PARAMETER, message);
            		return createResponse(INVALID_PARAMETER, message);           		
            	}
            	
            	CreditCardToken token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID, request.getTokenID()));
            	
            	if(token == null)
            	{
            		String message = "Token [" + request.getTokenID() + "] does not exist in the system.";
            		PGINoteSupport.addAccountNoteForCreateScheduleFailure(ctx, subscriber.getBAN(), message);
                	PGILogSupport.logApiEnd(ctx, this.getClass(), "createSchedule", NO_SUCH_TOKEN_ID, message);
            		return createResponse(NO_SUCH_TOKEN_ID, message);
            	}
            	
            	if(request.getAmount() == null)
            	{
            		String message = "Amount is not passed in the request.";
            		PGINoteSupport.addAccountNoteForCreateScheduleFailure(ctx, subscriber.getBAN(), message);
                	PGILogSupport.logApiEnd(ctx, this.getClass(), "createSchedule", INVALID_PARAMETER, message);
            		return createResponse(INVALID_PARAMETER, message);            		
            	}
            	
            	Date nextApplicationDate = null;
            	TopUpSchedule schedule = new TopUpSchedule();
            	Date determineNextAppDate = SubscriptionSupport.determineNextTopUpDate(ctx, subscriber);
                Calendar nextApplicationDateInRequest = request.getNextApplication();
                
                if (nextApplicationDateInRequest != null)
                {
                    if(nextApplicationDateInRequest.before(Calendar.getInstance()))
                    {
                        nextApplicationDate = new GregorianCalendar(nextApplicationDateInRequest.get(Calendar.YEAR), 
                                nextApplicationDateInRequest.get(Calendar.MONTH) + 1, nextApplicationDateInRequest.get(Calendar.DAY_OF_MONTH)).getTime();
                        
                        /*
                         *  Issue : Next Application Date Incorrectly Populated on PAP set Up from WSC for Prepaid Brands
                         *  Solution: if nextApplicationDateInRequest.before(Calendar.getInstance()) then the NextApplication date is incorrectly set to Current Date+30 days 
                         *  when done from WSC instead of aligning with the actual expiry date of the subscription so below code done to check if nextApplicationDate.after(determineNextAppDate) 
                         *  condition true then override the  nextApplicationDateInRequest with subscriber expiry date.
                         */
                      
                        if(determineNextAppDate != null)
                        {
                        	nextApplicationDate = determineNextAppDate;
                        }
                        
                    }
                    
                    else if(determineNextAppDate != null )
                    {
                    	nextApplicationDate = determineNextAppDate;
                    }
                    else
                    {
                    	 nextApplicationDate = nextApplicationDateInRequest.getTime();
                    }
                    
                    schedule.setScheduleUserDefined(true);
                }
                else
                {
                	nextApplicationDate = determineNextAppDate;
                	 
                    if (nextApplicationDate == null)
                    {
                        String message = "Next Application Date was calculated as NULL. Please verify Subscriber ["
                                + subscriber.getId() + "]'s Price Plan.";
                        PGINoteSupport.addAccountNoteForCreateScheduleFailure(ctx, subscriber.getBAN(), message);
                        PGILogSupport.logApiEnd(ctx, this.getClass(), "createSchedule", INVALID_PARAMETER, message);
                        return createResponse(INVALID_PARAMETER, message);
                    }
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "The nextApplication calculated is : " + nextApplicationDate);
                    }
                }
                
                GenericParameterParser parser = new GenericParameterParser(request.getParameters());
                
                Boolean isPlanChangeScheduled = parser.getParameter(APIGenericParameterSupport.IS_PLAN_CHANGE_SCHEDULED, Boolean.class);
            	Boolean usePlanFees = parser.getParameter(APIGenericParameterSupport.IS_USE_PLAN_FEE, Boolean.class);
                RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
            	
            	
            	schedule.setAmount(request.getAmount());
            	schedule.setBan(subscriber.getBAN());
            	schedule.setMsisdn(subscriber.getMsisdn());
            	schedule.setSubscriptionId(subscriber.getId());
            	schedule.setTokenId(token.getId());
            	schedule.setNextApplication(nextApplicationDate);
            	schedule.setInvoiceId("");
            	schedule.setInvoiceDueDate(null);
            	schedule.setSystemType(SubscriberTypeEnum.PREPAID);
            	
            	if(isPlanChangeScheduled != null)
	        	{
	        		schedule.setPlanChangeScheduled(isPlanChangeScheduled);
	        	}
	        	if(usePlanFees != null)
	        	{
	        		schedule.setUsePlanFees(usePlanFees);
	        	}
            	
            	TopUpSchedule existingSchedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, new EQ(TopUpScheduleXInfo.SUBSCRIPTION_ID, subscriber.getId()));
            	
            	if(existingSchedule != null)
            	{
            		PGILogSupport.logApiInExecution(ctx, getClass(), "createSchedule", "Overwriting existing Schedule with id: " + existingSchedule.getId());
            		HomeSupportHelper.get(ctx).removeBean(ctx, existingSchedule);
            	}
            	
            	schedule = HomeSupportHelper.get(ctx).createBean(ctx, schedule);
            	
            	PGINoteSupport.addAccountNoteForCreateSchedule(ctx, token.getBan(), schedule);
            	
            	try
            	{
            		ERLogger.createATUReistrationDeregistrationER(ctx, subscriber, ERLogger.ATU_REGISTRATION_ACTION);
            	}
            	catch(HomeException he)
            	{
            		LogSupport.minor(ctx, this, "Unable to log ER for ATU registration due to exception", he);
            	}
            	
            	Schedule apiSchedule = new Schedule();            	
            	PaymentGatewayEntitiesAdapter.adapt(apiSchedule, schedule);            	
            	apiSchedule.setSubscriptionRef(request.getSubscriptionRef());
            	
            	response = createResponse(SUCCESS, "Schedule created successfully.");
                response.setSchedule(apiSchedule);             
            	
            	
            }
            catch ( HomeException e )
            {
            	PGILogSupport.logApiEnd(ctx, getClass(), "createSchedule", null, "Exception:" + e.getMessage());
            	throw CRMExceptionFactory.create(e);
            } 
            catch (Exception e) 
            {
            	PGILogSupport.logApiEnd(ctx, getClass(), "createSchedule", null, "Exception:" + e.getMessage());
            	throw CRMExceptionFactory.create(e);
            }
            
            return response;
        }
        
        private CreateScheduleResponse createScheduleForPostpaidSubscriberAccount(Context ctx, CreateScheduleRequest request, Subscriber subscriber) throws CRMExceptionFault
        {
        	if (subscriber == null)
        	{
        		final String msg = "Subscription does not exist in the system.";
    			LogSupport.minor(ctx, this, msg);
        		return createResponse(NO_SUCH_SUBSCRIPTION, msg);
        	}
            
            CreateScheduleResponse response = null;
            try
            {
            	Long tokenId = request.getTokenID();
            	if(tokenId == null || tokenId < 1)
            	{
            		String message = "Invalid / NULL Token ID.";
            		PGINoteSupport.addAccountNoteForCreateScheduleFailure(ctx, subscriber.getBAN(), message);
                	PGILogSupport.logApiEnd(ctx, this.getClass(), "createSchedule", INVALID_PARAMETER, message);
            		return createResponse(INVALID_PARAMETER, message);           		
            	}
            	
            	CreditCardToken token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID, request.getTokenID()));
            	
            	if(token == null)
            	{
            		String message = "Token : [" + request.getTokenID() + "] does not exist in the system.";
            		PGINoteSupport.addAccountNoteForCreateScheduleFailure(ctx, subscriber.getBAN(), message);
                	PGILogSupport.logApiEnd(ctx, this.getClass(), "createSchedule", NO_SUCH_TOKEN_ID, message);
            		return createResponse(NO_SUCH_TOKEN_ID, message);
            	}
            	
            	GenericParameterParser parser = new GenericParameterParser(request.getParameters());
                
                Boolean isPlanChangeScheduled = parser.getParameter(APIGenericParameterSupport.IS_PLAN_CHANGE_SCHEDULED, Boolean.class);
            	Boolean usePlanFees = parser.getParameter(APIGenericParameterSupport.IS_USE_PLAN_FEE, Boolean.class);
                RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
            	
            	TopUpSchedule schedule = new TopUpSchedule();
            	schedule.setAmount(0L);
            	schedule.setBan(subscriber.getBAN());
            	schedule.setMsisdn(subscriber.getMsisdn());
            	schedule.setSubscriptionId(subscriber.getId());
            	schedule.setTokenId(token.getId());
            	schedule.setNextApplication(null);
            	schedule.setInvoiceId("");
            	schedule.setInvoiceDueDate(null);
            	schedule.setSystemType(SubscriberTypeEnum.POSTPAID);
            	
            	if(isPlanChangeScheduled != null)
	        	{
	        		schedule.setPlanChangeScheduled(isPlanChangeScheduled);
	        	}
	        	if(usePlanFees != null)
	        	{
	        		schedule.setUsePlanFees(usePlanFees);
	        	}
            	
            	TopUpSchedule existingSchedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, new EQ(TopUpScheduleXInfo.SUBSCRIPTION_ID, subscriber.getId()));
            	
            	if(existingSchedule != null)
            	{
            		PGILogSupport.logApiInExecution(ctx, getClass(), "createSchedule", "Overwriting existing Schedule with id: " + existingSchedule.getId());
            		HomeSupportHelper.get(ctx).removeBean(ctx, existingSchedule);
            	}
            	
            	schedule = HomeSupportHelper.get(ctx).createBean(ctx, schedule);
            	
            	PGINoteSupport.addAccountNoteForCreateSchedule(ctx, token.getBan(), schedule);
            	
            	Schedule apiSchedule = new Schedule();            	
            	PaymentGatewayEntitiesAdapter.adapt(apiSchedule, schedule);            	
            	apiSchedule.setSubscriptionRef(request.getSubscriptionRef());
            	
            	response = createResponse(SUCCESS, "Schedule created successfully.");
                response.setSchedule(apiSchedule);             
            	
            	
            } 
            catch ( HomeException e )
            {
            	PGILogSupport.logApiEnd(ctx, getClass(), "createSchedule", null, "Exception:" + e.getMessage());
            	throw CRMExceptionFactory.create(e);
            } 
            catch (Exception e) 
            {
            	PGILogSupport.logApiEnd(ctx, getClass(), "createSchedule", null, "Exception:" + e.getMessage());
            	throw CRMExceptionFactory.create(e);
            }
            
            return response;
        }
        
        private CreateScheduleResponse createScheduleForPostpaidGroupAccount(Context ctx, CreateScheduleRequest request, Account account) throws CRMExceptionFault
        {
        	if (account == null)
        	{
        		final String msg = "Account does not exist in the system.";
    			LogSupport.minor(ctx, this, msg);
        		return createResponse(NO_SUCH_BAN, msg);
        	}
        	
        	else if (account != null && account.getOwnerMSISDN() != null && account.getOwnerMSISDN().trim().equals(""))
        	{
        		final String msg = "Account with accountID : [" + account.getBAN() + "] and Account Group Type : [" + account.getGroupType() + "], does not contain OwnerMSISDN, cannot create Schedule.";
    			LogSupport.minor(ctx, this, msg);
        		return createResponse(INVALID_PARAMETER, msg);
        	}
        	
        	CreateScheduleResponse response = null;
            try
            {
            	Long tokenId = request.getTokenID();
            	if(tokenId == null || tokenId < 1)
            	{
            		String message = "Invalid / NULL Token ID.";
            		PGINoteSupport.addAccountNoteForCreateScheduleFailure(ctx, account.getBAN(), message);
                	PGILogSupport.logApiEnd(ctx, this.getClass(), "createSchedule", INVALID_PARAMETER, message);
            		return createResponse(INVALID_PARAMETER, message);           		
            	}
            	
            	CreditCardToken token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID, request.getTokenID()));
            	
            	if(token == null)
            	{
            		String message = "Token : [" + request.getTokenID() + "] does not exist in the system.";
            		PGINoteSupport.addAccountNoteForCreateScheduleFailure(ctx, account.getBAN(), message);
                	PGILogSupport.logApiEnd(ctx, this.getClass(), "createSchedule", NO_SUCH_TOKEN_ID, message);
            		return createResponse(NO_SUCH_TOKEN_ID, message);
            	}
            	
            	TopUpSchedule schedule = new TopUpSchedule();
            	schedule.setAmount(0L);
            	schedule.setBan(account.getBAN());
            	schedule.setMsisdn(account.getOwnerMSISDN());
            	schedule.setSubscriptionId("");
            	schedule.setTokenId(token.getId());
            	schedule.setNextApplication(null);
            	schedule.setInvoiceId("");
            	schedule.setInvoiceDueDate(null);
            	schedule.setSystemType(SubscriberTypeEnum.POSTPAID);
            	
            	TopUpSchedule existingSchedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, new EQ(TopUpScheduleXInfo.BAN, account.getBAN()));
            	
            	if(existingSchedule != null)
            	{
            		PGILogSupport.logApiInExecution(ctx, getClass(), "createSchedule", "Overwriting existing Schedule with id: " + existingSchedule.getId());
            		HomeSupportHelper.get(ctx).removeBean(ctx, existingSchedule);
            	}
            	
            	schedule = HomeSupportHelper.get(ctx).createBean(ctx, schedule);
            	
            	PGINoteSupport.addAccountNoteForCreateSchedule(ctx, token.getBan(), schedule);
            	
            	Schedule apiSchedule = new Schedule();            	
            	PaymentGatewayEntitiesAdapter.adapt(apiSchedule, schedule);            	
            	apiSchedule.setSubscriptionRef(request.getSubscriptionRef());
            	
            	response = createResponse(SUCCESS, "Schedule created successfully.");
                response.setSchedule(apiSchedule);             
            	
            	
            } 
            catch ( HomeException e )
            {
            	PGILogSupport.logApiEnd(ctx, getClass(), "createSchedule", null, "Exception:" + e.getMessage());
            	throw CRMExceptionFactory.create(e);
            } 
            catch (Exception e) 
            {
            	PGILogSupport.logApiEnd(ctx, getClass(), "createSchedule", null, "Exception:" + e.getMessage());
            	throw CRMExceptionFactory.create(e);
            }
            
            return response;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, CreateScheduleRequest.class, parameters);
                result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && CreateScheduleRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return CreateScheduleResponse.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        public static final String PARAM_GENERIC_PARAMETERS_USER_CHOICE_TOPUP_DATE = "UserChoiceTopUpDate";
    }

    /**
     * Implements method readSchedules
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class SchedulesQueryExecutor extends AbstractQueryExecutor<ReadSchedulesResponse>
    {
        public SchedulesQueryExecutor()
        {
            
        }

        private ReadSchedulesResponse createResponse(long code, String message)
        {
        	ReadSchedulesResponse response = new ReadSchedulesResponse();
        	response.setStatusCode(code);
        	response.setStatusMessage(message);
        	return response;
        }
        
        public ReadSchedulesResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            ReadSchedulesRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, ReadSchedulesRequest.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);

            ReadSchedulesResponse response = null;
            try
            {
            	SubscriptionReference reference = request.getCriteria().getSubscriptionRef();
            	Long tokenId = request.getCriteria().getTokenID();
            	Long scheduleId = request.getCriteria().getScheduleID();
            	
            	if(reference == null && tokenId == null && scheduleId == null)
            	{
            		return createResponse(INVALID_PARAMETER, "Atleast one parameter in the request should be present");
            	}
            	
            	And predicate = new And();
            	
            	
            	if(reference != null)
            	{
            		Subscriber subscriber = null;
                    Account account = null;
            	    SubscriptionReference subRef = request.getCriteria().getSubscriptionRef();
            		try
            		{
            			subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subRef, this);
            		}
            		catch(CRMExceptionFault e)
            		{
            			
            		}
            		
                
	                if(subscriber == null)
	                {
	                	if ( subRef.getAccountID() != null  )
	                    {
	                        try
	                        {
	                         account = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.Account.class, new EQ(AccountXInfo.BAN, subRef.getAccountID()));
	                    
	                        }
	                        catch(HomeException ex)
	                        {
	                            
	                        }
	                        if ( account != null)
	                        {
	                            predicate.add(new EQ(TopUpScheduleXInfo.BAN, account.getBAN()));
	                        }
	                    }
	                    else
	                    {
	                        return createResponse(NO_SUCH_SUBSCRIPTION , "Subscription/Account does not exist in the system.");	                        
	                    }
	                }
	                else
	                {
	                    predicate.add(new EQ(TopUpScheduleXInfo.SUBSCRIPTION_ID, subscriber.getId()));
	                }
	                }
	             
            	if(scheduleId != null)
            	{
            		TopUpSchedule schedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, new EQ(TopUpScheduleXInfo.ID, request.getCriteria().getScheduleID()));
            		
            		if(schedule == null)
            		{
            			return createResponse(NO_SUCH_SCHEDULE_ID, "Schedule [" + request.getCriteria().getScheduleID() + "] does not exist in the system.");
            		}
            		
            		predicate.add(new EQ(TopUpScheduleXInfo.ID, scheduleId));
            	}            	
            	
            	CreditCardToken token = null; 
            	if(tokenId != null)
            	{
	            	token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID, request.getCriteria().getTokenID()));
	            	
	            	if(token == null)
	            	{
	            		return createResponse(NO_SUCH_TOKEN_ID, "Token [" + request.getCriteria().getTokenID() + "] does not exist in the system.");
	            	}
	            	predicate.add(new EQ(TopUpScheduleXInfo.TOKEN_ID, token.getId()));
            	}
            	
            	Collection<TopUpSchedule> scheduleCollection = HomeSupportHelper.get(ctx).getBeans(ctx, TopUpSchedule.class, predicate);
            	
            	/*if(scheduleCollection.isEmpty())
            	{
            		return createResponse(INVALID_PARAMETER, "No Schedule found for Subscription Reference [ID/MSISDN : "
            				+ ( reference == null ? null : reference.getIdentifier() ) 
            				+ "/" 
            				+ ( reference == null ? null : reference.getMobileNumber() + "]" )
            				+ ", tokenID:" + tokenId 
            				+ ", scheduleID:" + scheduleId);
            	}*/
            	response = createResponse(SUCCESS, "ReadSchedules successfull.");
            	
            	for(TopUpSchedule schedule : scheduleCollection)
            	{            		
            		Schedule apiSchedule = new Schedule();
            		PaymentGatewayEntitiesAdapter.adapt(apiSchedule, schedule);
            		apiSchedule.setSubscriptionRef(reference);
            		response.addSchedules(apiSchedule);
            	}
            	
            	
            } catch ( HomeException e )
            {
            	throw CRMExceptionFactory.create(e);
            } 
            catch (Exception e) 
            {
            	throw CRMExceptionFactory.create(e);
            }
            return response;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, ReadSchedulesRequest.class, parameters);
                result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && ReadSchedulesRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return ReadSchedulesResponse.class.isAssignableFrom(resultType);
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    /**
     * Implements method updateSchedule
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class ScheduleUpdateQueryExecutor extends AbstractQueryExecutor<UpdateScheduleResponse>
    {
        public ScheduleUpdateQueryExecutor()
        {
            
        }
        
        private UpdateScheduleResponse createResponse(long code , String message)
        {
        	UpdateScheduleResponse response = new UpdateScheduleResponse();
        	response.setStatusCode(code);
        	response.setStatusMessage(message);
        	return response;
        }

        public UpdateScheduleResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            UpdateScheduleRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, UpdateScheduleRequest.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
            
            UpdateScheduleResponse response = null;
            
            try
            {
            	TopUpSchedule schedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, new EQ(TopUpScheduleXInfo.ID, request.getScheduleID()));
            	
            	if(schedule == null)
            	{
            		final String msg = "Schedule [" + request.getScheduleID() + "] not present in the system.";
        			LogSupport.minor(ctx, this, msg);
            		return createResponse(NO_SUCH_SCHEDULE_ID, msg);
            	}
            	
            	GenericParameterParser parser = new GenericParameterParser(request.getScheduleFields());
            	
            	final String accountID = schedule.getBan();
            	final String subscriptionID = schedule.getSubscriptionId();
            	
            	final Subscriber subscriber = SubscriberSupport.getSubscriber(ctx, subscriptionID);
            	final Account account = AccountSupport.getAccount(ctx, accountID);
            	
            	if (subscriber != null)
                {
                	if (subscriber.isPrepaid())
                	{
                		return updateScheduleForPrepaidSubscriberAccount(ctx, request, parser, schedule, subscriber);
                	}
                	if (subscriber.isPostpaid())
                    {
                		return updateScheduleForPostpaidSubscriberAccount(ctx, request, parser, schedule, subscriber);
                    }
                } 
                else if (account != null)
                {
                	if (GroupTypeEnum.GROUP.equals(account.getGroupType()))
                   	{
                   		return updateScheduleForPostpaidGroupAccount(ctx, request, parser, schedule, account);
                   	}
                  	else
                  	{
                  		return createResponse(INVALID_PARAMETER, "updateSchedule for Account with accountID : [" + accountID + "], Account Group Type : " + account.getGroupType() + " not supported.");
                  	}
                } 
                else
                {
                	return createResponse(INVALID_PARAMETER, "BAN / Subscription ID in the Schedule cannot retrieve Account / Subscriber from the system.");
                }
            }
            catch (HomeException e)
            {
            	CRMExceptionFactory.create(e);
            }
            catch (Exception e)
            {
            	CRMExceptionFactory.create(e);
            }
            
            return response;
            
        }
        
        private UpdateScheduleResponse updateScheduleForPrepaidSubscriberAccount(Context ctx, UpdateScheduleRequest request, GenericParameterParser parser, TopUpSchedule schedule, Subscriber subscriber) throws CRMExceptionFault
        {

        	UpdateScheduleResponse response = null;
        	
        	String subscriptionId = parser.getParameter("SUBSCRIPTION_ID", String.class);
        	String msisdn = parser.getParameter("MSISDN", String.class);
        	Long amount = parser.getParameter("AMOUNT", Long.class);
        	Long tokenId = parser.getParameter("TOKEN_ID", Long.class);
        	
        	GenericParameterParser genericParser = new GenericParameterParser(request.getParameters());
            
            Boolean isPlanChangeScheduled = genericParser.getParameter(APIGenericParameterSupport.IS_PLAN_CHANGE_SCHEDULED, Boolean.class);
        	Boolean usePlanFees = genericParser.getParameter(APIGenericParameterSupport.IS_USE_PLAN_FEE, Boolean.class);        	
        	
//        	GTAC TT#12060755041
//        	Date nextApplication = parser.getParameter("NEXT_APPLICATION", Date.class);
        	
        	if ( subscriptionId == null && msisdn == null && amount == null && tokenId == null && isPlanChangeScheduled==null && usePlanFees==null/*&& nextApplication == null*/)
        	{
        		return createResponse(INVALID_PARAMETER, "Atleast one of the ScheduleFields should be present to be updated.");
        	}
        	try
        	{
	        	if(subscriptionId != null)
	        	{
	        		if( SubscriberSupport.getSubscriber(ctx, subscriptionId) == null)
	        		{
                    	return createResponse(NO_SUCH_SUBSCRIPTION , "Subscription [" + subscriptionId + "] does not exist in the system.");
	        		}
	        		schedule.setSubscriptionId(subscriptionId);
	        	}
	        	if(msisdn != null)
	        	{
	        		if( SubscriberSupport.getSubscriptionIdsByMSISDN(ctx, msisdn, new Date() ).isEmpty() )
	        		{
	        			return createResponse(INVALID_PARAMETER, "MSISDN [" + msisdn + "], not assigned to any Subscription.");
	        		}
	        		schedule.setMsisdn(msisdn);
	        	}
	        	if(amount != null)
	        	{
	        		schedule.setAmount(amount);
	        	}
	        	if(tokenId != null)
	        	{
	        		CreditCardToken token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID, tokenId));
	        		
	        		if( token == null )
	        		{
	        			return createResponse(NO_SUCH_TOKEN_ID, "Token [" + tokenId + "] does not exist in the system.");
	        		}
	        		
	        		schedule.setTokenId(tokenId);
	        	}
	        	
	        	if(isPlanChangeScheduled != null)
	        	{
	        		schedule.setPlanChangeScheduled(isPlanChangeScheduled);
	        	}
	        	if(usePlanFees != null)
	        	{
	        		schedule.setUsePlanFees(usePlanFees);
	        	}
	        	
	//        	GTAC TT#12060755041
	//        	if(nextApplication != null)
	//        	{
	//        		schedule.setNextApplication(nextApplication);
	//        	}
	        	
	        	HomeSupportHelper.get(ctx).storeBean(ctx, schedule);
	        	
	        	PGINoteSupport.addAccountNoteForUpdateSchedule(ctx, schedule.getBan(), schedule);
	        	
	        	response = createResponse(SUCCESS, "Schedule [" + schedule.getId() + "] updated successfully.");
	        	Schedule apiSchedule = new Schedule();
	        	PaymentGatewayEntitiesAdapter.adapt(apiSchedule, schedule);
	        	response.setSchedule(apiSchedule);
	        	
	        }
        	catch (HomeException e)
        	{
        		CRMExceptionFactory.create(e);
        	}
        	catch (Exception e)
        	{
        		CRMExceptionFactory.create(e);
        	}
        
        	return response;
        }
        
        private UpdateScheduleResponse updateScheduleForPostpaidSubscriberAccount(Context ctx, UpdateScheduleRequest request, GenericParameterParser parser, TopUpSchedule schedule, Subscriber subscriber) throws CRMExceptionFault
        {
        
        	UpdateScheduleResponse response = null;
        	
        	String subscriptionId = parser.getParameter("SUBSCRIPTION_ID", String.class);
        	String msisdn = parser.getParameter("MSISDN", String.class);
        	Long tokenId = parser.getParameter("TOKEN_ID", Long.class);
        	
        	GenericParameterParser genericParser = new GenericParameterParser(request.getParameters());
            
            Boolean isPlanChangeScheduled = genericParser.getParameter(APIGenericParameterSupport.IS_PLAN_CHANGE_SCHEDULED, Boolean.class);
        	Boolean usePlanFees = genericParser.getParameter(APIGenericParameterSupport.IS_USE_PLAN_FEE, Boolean.class);   
        	
        	if (subscriptionId == null && msisdn == null && tokenId == null && isPlanChangeScheduled==null && usePlanFees==null)
        	{
        		return createResponse(INVALID_PARAMETER, "Atleast one of the ScheduleFields should be present to be updated.");
        	}
        	
        	try
        	{
        		if(subscriptionId != null)
	        	{
	        		if( SubscriberSupport.getSubscriber(ctx, subscriptionId) == null)
	        		{
	        			String msg = "Subscription [" + subscriptionId + "] does not exist in the system.";
	        			LogSupport.minor(ctx, this, msg);
	        			return createResponse(NO_SUCH_SUBSCRIPTION , msg);
	        		}
	        		schedule.setSubscriptionId(subscriptionId);
	        	}
	        	if(msisdn != null)
	        	{
	        		if( SubscriberSupport.getSubscriptionIdsByMSISDN(ctx, msisdn, new Date() ).isEmpty() )
	        		{
	        			String msg = "MSISDN [" + msisdn + "], not assigned to any Subscription.";
	        			LogSupport.minor(ctx, this, msg);
	        			return createResponse(INVALID_PARAMETER, msg);
	        		}
	        		schedule.setMsisdn(msisdn);
	        	}
	        	if(tokenId != null)
	        	{
	        		CreditCardToken token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID, tokenId));
	        		
	        		if( token == null )
	        		{
	        			String msg = "Token [" + tokenId + "] does not exist in the system.";
	        			LogSupport.minor(ctx, this, msg);
	        			return createResponse(NO_SUCH_TOKEN_ID, msg);
	        		}
	        		
	        		schedule.setTokenId(tokenId);
	        	}
	        	
	        	if(isPlanChangeScheduled != null)
	        	{
	        		schedule.setPlanChangeScheduled(isPlanChangeScheduled);
	        	}
	        	if(usePlanFees != null)
	        	{
	        		schedule.setUsePlanFees(usePlanFees);
	        	}
	        	
	        	HomeSupportHelper.get(ctx).storeBean(ctx, schedule);
        	
	        	PGINoteSupport.addAccountNoteForUpdateSchedule(ctx, schedule.getBan(), schedule);
	        	
	        	response = createResponse(SUCCESS, "Schedule [" + schedule.getId() + "] updated successfully.");
	        	Schedule apiSchedule = new Schedule();
	        	PaymentGatewayEntitiesAdapter.adapt(apiSchedule, schedule);
	        	response.setSchedule(apiSchedule);
	        	
	        }
        	catch (HomeException e)
        	{
        		CRMExceptionFactory.create(e);
        	}
        	catch (Exception e)
        	{
        		CRMExceptionFactory.create(e);
        	}
        
        	return response;
        }
        
        private UpdateScheduleResponse updateScheduleForPostpaidGroupAccount(Context ctx, UpdateScheduleRequest request, GenericParameterParser parser, TopUpSchedule schedule, Account account) throws CRMExceptionFault
        {
        	
        	UpdateScheduleResponse response = null;
        	
        	Long tokenId = parser.getParameter("TOKEN_ID", Long.class);
        	
        	if (tokenId == null)
        	{
        		String msg = "TokenId in ScheduleFields should be present to be updated.";
    			LogSupport.minor(ctx, this, msg);
        		return createResponse(INVALID_PARAMETER, msg);
        	}
        	
        	try
        	{
        		if(tokenId != null)
	        	{
	        		CreditCardToken token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID, tokenId));
	        		
	        		if( token == null )
	        		{
	        			String msg = "Token [" + tokenId + "] does not exist in the system.";
	        			LogSupport.minor(ctx, this, msg);
	        			return createResponse(NO_SUCH_TOKEN_ID, msg);
	        		}
	        		
	        		schedule.setTokenId(tokenId);
	        	}
	        	
	        	HomeSupportHelper.get(ctx).storeBean(ctx, schedule);
        	
	        	PGINoteSupport.addAccountNoteForUpdateSchedule(ctx, schedule.getBan(), schedule);
	        	
	        	response = createResponse(SUCCESS, "Schedule [" + schedule.getId() + "] updated successfully.");
	        	Schedule apiSchedule = new Schedule();
	        	PaymentGatewayEntitiesAdapter.adapt(apiSchedule, schedule);
	        	response.setSchedule(apiSchedule);
	        	
	        }
        	catch (HomeException e)
        	{
        		CRMExceptionFactory.create(e);
        	}
        	catch (Exception e)
        	{
        		CRMExceptionFactory.create(e);
        	}
        
        	return response;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, UpdateScheduleRequest.class, parameters);
                result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && UpdateScheduleRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return UpdateScheduleResponse.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    /**
     * Implements method deleteSchedule
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class ScheduleRemovalQueryExecutor extends AbstractQueryExecutor<DeleteScheduleResponse>
    {
        public ScheduleRemovalQueryExecutor()
        {
            
        }

        private DeleteScheduleResponse createResponse(long code, String message)
        {
        	DeleteScheduleResponse response = new DeleteScheduleResponse();
        	response.setStatusCode(code);
        	response.setStatusMessage(message);
        	return response;
        }
        
        public DeleteScheduleResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            DeleteScheduleRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, DeleteScheduleRequest.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
            
            DeleteScheduleResponse response = null;
            try
            {
            	PGILogSupport.logApiStart(ctx, getClass(), "deleteSchedule", " Delete Schedule with id:" + request.getScheduleID());
            	TopUpSchedule schedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, new EQ(TopUpScheduleXInfo.ID, request.getScheduleID()));
            	
            	if(schedule == null)
            	{
            		PGILogSupport.logApiEnd(ctx, getClass(), "deleteSchedule", null, "Schedule[" + request.getScheduleID() + "] not found.");
            		return createResponse(NO_SUCH_SCHEDULE_ID, "Schedule [" + request.getScheduleID() + "] does not exist in the system." );
            	}
            	
            	final String accountID = schedule.getBan();
            	final Account account = AccountSupport.getAccount(ctx, accountID);
            	
            	Subscriber subscriber = SubscriberSupport.getSubscriber(ctx, schedule.getSubscriptionId());
            	
            	if (subscriber != null)
                {
            		/* TT#12072518010
            		 * if(!SubscriberStateEnum.ACTIVE.equals(subscriber.getState()) && !(subscriber.isPostpaid()))
                	{
            			String message = "Subscription is not Active";
                		PGILogSupport.logApiEnd(ctx, getClass(), "deleteSchedule", INVALID_PARAMETER, message);
                		return createResponse(INVALID_PARAMETER, message);
                    }
                    */
                } 
                else if (account != null)
                {
                	if (GroupTypeEnum.GROUP.equals(account.getGroupType()))
                   	{
                		/* TT#12072518010
                		 * if (!AccountStateEnum.ACTIVE.equals(account.getState()) && !(account.isPostpaid()))
                		{
                			String message = "Account is not Active";
                			PGILogSupport.logApiEnd(ctx, getClass(), "deleteSchedule", INVALID_PARAMETER, message);
                			return createResponse(INVALID_PARAMETER, message);
                		}*/
                   	}
                  	else if(account.isPostpaid() && (!account.isResponsible() && !account.isIndividual(ctx)))
                  	{
                  		String message = "deleteSchedule for Account with BAN : [" + accountID + "], Account Group Type : " + account.getGroupType() + " is not supported.";
            			PGILogSupport.logApiEnd(ctx, getClass(), "deleteSchedule", INVALID_PARAMETER, message);
                  		return createResponse(INVALID_PARAMETER, message);
                  	}
                } 
                else
                {
                	String message = "BAN / SubscriptionID in the Schedule cannot retrieve Account / Subscriber from the system.";
                	PGILogSupport.logApiEnd(ctx, getClass(), "deleteSchedule", INVALID_PARAMETER, message);                			
                	return createResponse(INVALID_PARAMETER, message);
                }
            	
            	HomeSupportHelper.get(ctx).removeBean(ctx, schedule);
            	
            	response = createResponse(SUCCESS, "Schedule [" + request.getScheduleID() + "] deleted successfully");
            	
            	PGINoteSupport.addAccountNoteForDeleteSchedule(ctx, schedule.getBan(), schedule);
            	try
            	{
            		ERLogger.createATUReistrationDeregistrationER(ctx, subscriber, ERLogger.ATU_DEREGISTRATION_ACTION);
            	}
            	catch(HomeException he)
            	{
            		LogSupport.minor(ctx, this, "Unable to log ER for ATU registration due to exception", he);
            	}
            	
            	Schedule apiSchedule = new Schedule();            	
            	PaymentGatewayEntitiesAdapter.adapt(apiSchedule, schedule);
            	response.setSchedule(apiSchedule);
            	PGILogSupport.logApiEnd(ctx, getClass(), "deleteSchedule", SUCCESS, response.getStatusMessage());
            	
            }
            catch (HomeException e)
            {
            	PGILogSupport.logApiEnd(ctx, getClass(), "deleteSchedule", null, "Exception : " + e.getMessage());
            	CRMExceptionFactory.create(e);
            }
            catch (Exception e)
            {
            	PGILogSupport.logApiEnd(ctx, getClass(), "deleteSchedule", null, "Exception : " + e.getMessage());
            	CRMExceptionFactory.create(e);
            }
            
            return response;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, DeleteScheduleRequest.class, parameters);
                result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && DeleteScheduleRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return DeleteScheduleResponse.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    /**
     * Implements method calculatePaymentTax
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class PaymentTaxCalculationQueryExecutor extends AbstractQueryExecutor<CalculatePaymentTaxResponse>
    {
    	public static final String COUNTRY_ISO =  "countryISO"; 
        public static final String STATE = "state";
        public static final String COUNTY = "county";
        public static final String LOCALITY = "locality";
        public static final String ZIPCODE = "zipCode";
        ZipAddress zipAddress  = null;
        public static final String ACCOUNT_PAYMENT_BAN = "AccountPaymentBAN";
        public PaymentTaxCalculationQueryExecutor()
        {
            
        }

        
        public CalculatePaymentTaxResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
        	CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
        	CalculatePaymentTaxRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, CalculatePaymentTaxRequest.class, parameters);
        	GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
        	RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
        	
        	if( request.getAmount() == null )
    		{
    			CalculatePaymentTaxResponse response = new CalculatePaymentTaxResponse();
    			response.setStatusCode(Long.valueOf(INVALID_PARAMETER));
    			response.setStatusMessage("Amount is invalid.Please specify the correct amount for which the tax should be calculated.");
    			return response;

    		}
        	
        	SubscriptionReference subReference = request.getSubscriptionRef();
        	zipAddress = null;
        	int spid = 0;
        	String msisdn = null;
        	String ban = null;
        	GenericParameter[] reqgenericParameters = request.getParameters();
        	if(reqgenericParameters != null)
        	{
        		GenericParameterParser parser = new GenericParameterParser(reqgenericParameters);
        		if(parser.containsParam(COUNTY)&& parser.containsParam(LOCALITY) && parser.containsParam(STATE) && parser.containsParam(ZIPCODE))
       		 	{
           			 zipAddress = new ZipAddress();
           			 
           			 zipAddress.setCountryISO("USA") ;
           			 zipAddress.setCounty(parser.getParameter(COUNTY, String.class)) ;
           			 zipAddress.setLocality(parser.getParameter(LOCALITY, String.class)) ;
           			 zipAddress.setState(parser.getParameter(STATE, String.class)) ;
           			 zipAddress.setZipCode(parser.getParameter(ZIPCODE, String.class)) ;
           			 
       		 	}
        		ban = parser.getParameter(ACCOUNT_PAYMENT_BAN , String.class);
        	}

        	if(validateSubscriberIdBasedQuery(ctx,subReference))
        	{
        		Subscriber subscriber = null;
        		try
        		{
        			subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subReference, this);
        		}
        		catch ( CRMExceptionFault e)
        		{
        			CalculatePaymentTaxResponse response = new CalculatePaymentTaxResponse();
        			response.setStatusCode(Long.valueOf(NO_SUCH_SUBSCRIPTION));
        			response.setStatusMessage(e.getFaultMessage().getCRMException().getMessage());
        			return response;
        		}

        		if ( subscriber == null )
        		{
        			CalculatePaymentTaxResponse response = new CalculatePaymentTaxResponse();
        			response.setStatusCode(Long.valueOf(NO_SUCH_SUBSCRIPTION));
        			response.setStatusMessage("Subscriber does not exist in the system");
        			return response;
        		}

        		if( !SubscriberStateEnum.ACTIVE.equals(subscriber.getState()) && !(subscriber.isPostpaid()))
        		{
        			CalculatePaymentTaxResponse response = new CalculatePaymentTaxResponse();
        			response.setStatusCode(Long.valueOf(INVALID_PARAMETER));
        			response.setStatusMessage("Subscriber is not Active.");
        			return response;            	
        		}
        		
        		spid = subscriber.getSpid();
        		msisdn = subscriber.getMsisdn();
        	}
        	else
        	{
        		if(subReference != null && subReference.getMobileNumber() != null && subReference.getMobileNumber().length() > 0  )
        		{
        			msisdn = subReference.getMobileNumber();
        			try 
                    {
                        Account account = null;
                        ban = AccountSupport.getBAN(ctx, msisdn);
                        account = AccountSupport.getAccount(ctx, ban);
                        if(account == null)
                        {
                            String errorMsg = "Account could not be found for ban: "+ban;
                            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, new HomeException(errorMsg),
                                    errorMsg, this);
                        }
                        CRMSpid spidObj = SpidSupport.getCRMSpid(ctx, account.getSpid());
                        if(spidObj != null && spid == 0)
                        {
                            spid = spidObj.getSpid();
                        }
                    }
                    catch(HomeException e)
                    {
                        LogSupport.major(ctx, this, "HomeException occurred in API PaymentTaxCalculationQueryExecutor() : ",e);
                    }
        		}
        		else if(zipAddress != null)
        		{ 
        			try 
        			{
	        			Account account = null;
	        			if(ban != null && !ban.isEmpty())
	        			{
	                		account = AccountSupport.getAccount(ctx, ban);
	        			}
	        			else
	        			{
	        				if(subReference != null)
	        				{
	        					ban = subReference.getAccountID();
	        					account = AccountSupport.getAccount(ctx, ban);
	        				}
	        			}
	        			if(account == null)
	            		{
	        				RmiApiErrorHandlingSupport.validateMandatoryObject(msisdn, "ban or mobile number");
	                    }
	        			CRMSpid spidObj = SpidSupport.getCRMSpid(ctx, account.getSpid());
                        if(spidObj != null && spid == 0)
                        {
                       		spid = spidObj.getSpid();
                        }
        			}
        			catch(HomeException e)
        			{
        				LogSupport.major(ctx, this, "HomeExcpetion occurred in API PaymentTaxCalculationQueryExecutor() : ",e);
        			}
        		}
        		else
        		{
        			RmiApiErrorHandlingSupport.validateMandatoryObject(msisdn, "MobileNumber or ZipAddress");
        		}
        		
        		//Exception is not thrown on SPID, because for this release, SPID validation needs to be skipped, as it is an Optional 
        		//Parameter in SubscriptionReference.
        		if(subReference != null && subReference.getSpid() != null )
        		{
        			spid  = subReference.getSpid();
        		}
        	}
        	
        	try
    		{
    			long taxAmount = TaxSupport.getTaxAmount(ctx, spid, msisdn, request.getAmount(), zipAddress);
    			CalculatePaymentTaxResponse response = new CalculatePaymentTaxResponse();
    			response.setStatusCode(Long.valueOf(PaymentGatewayResponseCodes.SUCCESS));
    			response.setStatusMessage("Tax amount calculated successfully");
    			response.setTaxAmount(taxAmount);
    			return response;
    		}
    		catch (HomeException e)
    		{
    			throw CRMExceptionFactory.create(e);
    		}
        }
        
        private boolean validateSubscriberIdBasedQuery(Context ctx,
				SubscriptionReference subscriptionRef) throws CRMExceptionFault 
        {
        	if(subscriptionRef != null && subscriptionRef.getIdentifier() != null )
        	{
        		return true;
        	}
        	return false;
        }
        

		@Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, CalculatePaymentTaxRequest.class, parameters);
                result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && CalculatePaymentTaxRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return CalculatePaymentTaxResponse.class.isAssignableFrom(resultType);
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    public static class ApplyPaymentChargeQueryExecutor extends AbstractQueryExecutor<ApplyPaymentChargeResponse>
    {

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        public static final String FRAUD_SESSION_ID = "FraudSessionId";
        public static final String MERCHANT_ID = "merchantID";
        public static final String SPID = "Spid";
        public static final String ACCOUNT_PAYMENT_BAN = "AccountPaymentBAN";
       
        public static final String COUNTRY_ISO =  "countryISO"; 
        public static final String STATE = "state";
        public static final String COUNTY = "county";
        public static final String LOCALITY = "locality";
        public static final String ZIPCODE = "zipCode";
        public static final String INTERFACEID = "interfaceid";
        
        ZipAddress zipAddress  = null;
       
        public static final String MASKED_CREDIT_CARD_NUMBER = "MaskedCreditCardNumber";
        public static final String CREDIT_CARD_EXPIRY_DATE = "CreditCardExpiryDate";
        
		@Override
		public ApplyPaymentChargeResponse execute(Context ctx,
				Object... parameters) throws CRMExceptionFault 
		{
        	CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
        	ApplyPaymentChargeRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, ApplyPaymentChargeRequest.class, parameters);
        	GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
        	
        	String fraudSessionId = null;
        	Integer spid = null;
        	String ban = null, merchantId = null;
        	zipAddress = null;
        	long interfaceId = ChannelTypeEnum.BSS.getIndex();
        	GenericParameter[] reqgenericParameters = request.getParameters();
        	if(reqgenericParameters != null)
        	{
        		 GenericParameterParser parser = new GenericParameterParser(reqgenericParameters);
        		 fraudSessionId = parser.getParameter(FRAUD_SESSION_ID, String.class);
        		 spid = parser.getParameter(SPID, Integer.class);
        		 ban = parser.getParameter(ACCOUNT_PAYMENT_BAN , String.class);
        		
        		 if(parser.containsParam(COUNTY)&& parser.containsParam(LOCALITY) && parser.containsParam(STATE) && parser.containsParam(ZIPCODE))
        		 {
        			 zipAddress = new ZipAddress();
        			 zipAddress.setCountryISO("USA") ;
        			 zipAddress.setCounty(parser.getParameter(COUNTY, String.class)) ;
        			 zipAddress.setLocality(parser.getParameter(LOCALITY, String.class)) ;
        			 zipAddress.setState(parser.getParameter(STATE, String.class)) ;
        			 zipAddress.setZipCode(parser.getParameter(ZIPCODE, String.class)) ;
        		 }
        		 
        		 if(parser.containsParam(MERCHANT_ID))
        		 {
        		     merchantId =  parser.getParameter(MERCHANT_ID , String.class);
        		 }
        		 if(parser.containsParam(INTERFACEID))
        		 {
        			 interfaceId = parser.getParameter(INTERFACEID , Long.class);
        		 }
        	}
        	
        	ApplyPaymentChargeResponse response  = new ApplyPaymentChargeResponse();
        	BillingSystemResult billingResult = new BillingSystemResult();
			ChargingGatewayResult chargingGtResult = new ChargingGatewayResult();
			PaymentGatewayResult paymentGatewayResult =  new PaymentGatewayResult();
			
			
        	RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);


        	try
        	{
            	StringBuilder errorMessage = new StringBuilder();
    			
            	if((ban == null || ban ==  "") && request.getMobileNumber() == "-1"){
            		errorMessage.append("Account number is not present in request");
            		billingResult.setBillingSystemResultCode((long) PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
        			billingResult.setBillingSystemResultMessage(errorMessage.toString());
        			return response;
            		
            	}
            	
            	int subscriptionType = -1;
            	if(ban != null && !ban.isEmpty()){
            		Account account = AccountSupport.getAccount(ctx, ban);
            		if(account == null)
            		{
            			errorMessage.append("Account number is not valid.");
            			billingResult.setBillingSystemResultCode((long) PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
            			billingResult.setBillingSystemResultMessage(errorMessage.toString());
            			return response;
            		}
            		
            		CRMSpid spidObj = SpidSupport.getCRMSpid(ctx, account.getSpid());
                    if(spidObj != null)
                    {
                    	subscriptionType =  Long.valueOf(spidObj.getDefaultSubscriptionType()).intValue();
                   		spid = spidObj.getSpid();
                    }
                    else
                    {
            			errorMessage.append("Spid not found :" + account.getSpid());
            			billingResult.setBillingSystemResultCode((long) PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
            			billingResult.setBillingSystemResultMessage(errorMessage.toString());
            			return response;
                    }
            	}
            	
            	String banOrMsisdn = "";
                
                if (ban != null && ban != "")
                {
                    request.setMobileNumber("-1");
                    request.setSubscriptionType(subscriptionType);
                    banOrMsisdn = ban;
                }
                else
                {
                    banOrMsisdn = request.getMobileNumber();
                    Integer reqSubscriptionType = request.getSubscriptionType();
                    
                    // validation of prepaid subscription to check if it is in not in DEACTIVATED state. If yes do not
                    // charge the subscription.
                    if (banOrMsisdn != null && !banOrMsisdn.isEmpty())
                    {
                    	if (LogSupport.isDebugEnabled(ctx))
                        {
                    		LogSupport.debug(ctx, this, "Validating subscriber if active");
                        }
                        Subscriber subscriber = null;
                        try
                        {
                        	String subscriberId = SubscriberSupport.lookupSubscriberIdForMSISDN(ctx, banOrMsisdn, reqSubscriptionType, null);

                        	if (LogSupport.isDebugEnabled(ctx))
                            {
                                LogSupport.debug(ctx, PaymentGatewayIntegrationQueryExecutors.class.getName(), "Found Subscriber id from MSISDN:"+subscriberId);
                            }
                        	
                        	if(subscriberId != null)
                        	{
                        		subscriber = SubscriberSupport.lookupSubscriberLimited(ctx, subscriberId);	
                        	}
                        }
                        catch(Throwable e)
                        {
                                LogSupport.minor(ctx, PaymentGatewayIntegrationQueryExecutors.class.getName(), "Continuing with top-up process assuming this is a special case of MSISDN based Top-up without Subscriber.");
                        }
                        if (subscriber != null
                                && (subscriber.getSubscriberType().getIndex() == SubscriberTypeEnum.PREPAID_INDEX)
                                && subscriber.getState().getIndex() == SubscriberStateEnum.INACTIVE_INDEX)
                        {
                            errorMessage.append("Subscriber in INACTIVE state, cannot apply payment charge.");
                            billingResult
                                    .setBillingSystemResultCode((long) PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
                            billingResult.setBillingSystemResultMessage(errorMessage.toString());
                            return response;
                        }
                    }
                }
            	
            	boolean valid = validateRequestParameter(request,errorMessage , zipAddress);

            	if(!valid)
            	{
            		billingResult.setBillingSystemResultCode((long) PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
        			billingResult.setBillingSystemResultMessage(errorMessage.toString());
        			return response;
            	}
            	
        	
        		long paymentChargeAmount = request.getPreTaxAmount() ;
            	long paymentChargeTaxAmount = request.getTaxAmount();

            	boolean recurring = false; 

            	if(request.getMobileNumber() != "-1"  || zipAddress != null){
            		if(request.getRecalculateTax() != null && request.getRecalculateTax())
            		{
            			Msisdn msisdnObj = MsisdnSupport.getMsisdn(ctx, request.getMobileNumber());
            			if(msisdnObj != null )
            			{
            				paymentChargeTaxAmount = TaxSupport.getTaxAmount(ctx, msisdnObj.getSpid(), request.getMobileNumber(), request.getPreTaxAmount(), null);
            			}else if(zipAddress != null &&(ban != null && ban != ""))
            			{
            				paymentChargeTaxAmount = TaxSupport.getTaxAmount(ctx, spid, null, request.getPreTaxAmount(), zipAddress);
            			}
            			else 
            			{
            				billingResult.setBillingSystemResultCode((long) PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
            				billingResult.setBillingSystemResultMessage("MSISDN " +  request.getMobileNumber() + " not found in Billing system");
            				return response;
            			}
            		}
            	}
            	
            	/**
            	 * 
            	 * Every time an Account's Credit Card is charged (Debit Task) a complementary Credit Transaction is created in BSS.
            	 * If the amount of this transaction if greater than the User Adjustment Limit then transaction will not be created.
            	 * In such cases we should not charge the Credit Card.
            	 * 
            	 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
            	 * 
            	 */
            	
            	long userAdjustmentLimit = UserGroupSupport.getAdjustmentLimit(ctx);
            	
            	if(userAdjustmentLimit < paymentChargeAmount)
            	{
            		billingResult.setBillingSystemResultCode((long) PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
            		billingResult.setBillingSystemResultMessage("Payment Charge (Gateway) Amount is greater than allowed User's Adjustment Limit of " + CurrencyPrecisionSupportHelper.get(ctx).formatStorageCurrencyValue(ctx,
                            (Currency) ctx.get(Currency.class, Currency.DEFAULT), userAdjustmentLimit));
            		response.setBillingSystemResult(billingResult);
            		return response;
            	}
            	
            	long dailyLimit = UserDailyAdjustmentLimitTransactionValidator.getUserTransactionLimit(ctx, SystemSupport.getAgent(ctx), paymentChargeAmount);
            	
            	if(dailyLimit < paymentChargeAmount)
            	{
            		billingResult.setBillingSystemResultCode((long) PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
            		billingResult.setBillingSystemResultMessage("Payment Charge (Gateway) Amount is greater than allowed User's Daily Adjustment Limit of " + CurrencyPrecisionSupportHelper.get(ctx).formatStorageCurrencyValue(ctx,
                            (Currency) ctx.get(Currency.class, Currency.DEFAULT), dailyLimit));
            		response.setBillingSystemResult(billingResult);
            		return response;            		
            	}
            	Map <Object,Object> map = new HashMap<Object,Object>();
            	map.put(ParameterID.INTERFACE_ID, interfaceId);
    			Map<Short, Parameter> outParams = new HashMap<Short, Parameter>();
				int resultCode = PaymentGatewaySupportHelper.get(ctx).chargePaymentGateway(ctx, paymentChargeAmount+paymentChargeTaxAmount , paymentChargeTaxAmount, banOrMsisdn,
    					request.getSubscriptionType(), null, recurring, request.getToken().getMaskedCardNumber(), request.getToken().getTokenValue(),  fraudSessionId , spid, outParams, merchantId, map);
            	
    			StringBuilder billingResultMessage = new StringBuilder();
    			long billingResultCode = resolveBillingMessage(resultCode, billingResultMessage);
    			billingResult.setBillingSystemResultCode(billingResultCode);
    			billingResult.setBillingSystemResultMessage(billingResultMessage.toString());
    			
    			chargingGtResult.setChargingGatewayResultCode(String.valueOf(resultCode));
    			chargingGtResult.setChargingGatewayResultMessage(PaymentGatewayResponseCodes.resolveOcgMessage((short) resultCode));
    			
    			Parameter maskedCCNumberOutParam = outParams.get(ParameterID.PG_CREDIT_CARD_NUMBER);
    			if(maskedCCNumberOutParam != null)
    			{
    			    GenericParameter maskedCCardNumberResponseParam = new GenericParameter();
    			    maskedCCardNumberResponseParam.setName(MASKED_CREDIT_CARD_NUMBER);
    			    maskedCCardNumberResponseParam.setValue(maskedCCNumberOutParam.value.stringValue());
    			    chargingGtResult.addParameters(maskedCCardNumberResponseParam);
    			}
    			
    			Parameter creditCardExpiryOutParam = outParams.get(ParameterID.PG_EXPIRY_DATE);
                if(creditCardExpiryOutParam != null)
                {
                    GenericParameter creditCardExpiryResponseParam = new GenericParameter();
                    creditCardExpiryResponseParam.setName(CREDIT_CARD_EXPIRY_DATE);
                    creditCardExpiryResponseParam.setValue(creditCardExpiryOutParam.value.stringValue());
                    chargingGtResult.addParameters(creditCardExpiryResponseParam);
                }
                
    			Parameter pgReasonCodeParam = outParams.get(ParameterID.PG_REASON_CODE);
    			Parameter pgReasonTextParam = outParams.get(ParameterID.PG_REASON_TEXT);
    			
    			paymentGatewayResult.setPaymentGatewayResultCode( (pgReasonCodeParam!=null)?pgReasonCodeParam.value.stringValue():null);
    			paymentGatewayResult.setPaymentGatewayResultMessage( (pgReasonTextParam!=null)?pgReasonTextParam.value.stringValue():null);
    			
         	}catch (PaymentGatewayException e) 
        	{
         		LogSupport.major(ctx, this, "Excpetion occurred in API ApplyPaymentChargeQueryExecutor() : ",e);
         		billingResult.setBillingSystemResultCode((long) PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
    			billingResult.setBillingSystemResultMessage(e.getLocalizedMessage());
    			return response;
        	}catch (Exception e) 
        	{
        		LogSupport.major(ctx, this, "Excpetion occurred in API ApplyPaymentChargeQueryExecutor() : ",e);
        		billingResult.setBillingSystemResultCode((long) PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
    			billingResult.setBillingSystemResultMessage("Billing System internal error");
    			return response;
        	}finally
        	{
        		response.setBillingSystemResult(billingResult);
				response.setChargingGatewayResult(chargingGtResult );
				response.setPaymentGatewayResult(paymentGatewayResult );
        	}
        	
        	return response;
		}

		private boolean validateRequestParameter(
				ApplyPaymentChargeRequest request, StringBuilder errorMessage, ZipAddress zipAddress) 
		{

			if((request.getMobileNumber() == null || request.getMobileNumber().trim().isEmpty()) && zipAddress != null)
			{
				errorMessage.append("Mobile number is not present in request!");
				return false;
			}
			
			if(request.getPreTaxAmount() ==  null)
			{
				errorMessage.append("PreTax amount is not present in request!");
				return false;
			}
			
			if(request.getSubscriptionType() ==  null  && zipAddress != null)
			{
				errorMessage.append("Subscription Type is not present in request!");
				return false;
			}
			
			if(request.getTaxAmount() ==  null)
			{
				errorMessage.append("Tax Amount is not present in request!");
				return false;
			}
			
			if(request.getToken() ==  null)
			{
				errorMessage.append("Token object is not present in request!");
				return false;
			}
			else
			{
				if(request.getToken().getTokenValue() == null)
				{
					errorMessage.append("Token value is not present in request!");
					return false;
				}
			}
			
			return true;
		}

		private long resolveBillingMessage(int ocgResultCode, StringBuilder billingResultMessage) 
		{
			switch(ocgResultCode)
			{
				case ErrorCode.NO_ERROR : billingResultMessage.append("Success");return PaymentGatewayResponseCodes.PGW_CHARGE_SUCCEEDED;
				case ErrorCode.SERVICE_NOT_AVAILABLE : billingResultMessage.append("Charging Gateway is down !!");return PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR ; 
				case ErrorCode.TRANSACTION_TIMEDOUT : billingResultMessage.append("Charging Gatway Internal Timeout");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.INTERNAL_ERROR  : billingResultMessage.append("Charging Gatway Internal Error");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.BAD_DATA  : billingResultMessage.append("Payment gateway bad response data");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_INVALID_TRANSACTION_KEY  : billingResultMessage.append("Payment gateway - Invalid transaction key");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_INVALID_MERCHANT_NAME  : billingResultMessage.append("Payment gateway - Invalid merchant name.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_AUTHENTICATION_FAILED  : billingResultMessage.append("Payment gateway - Authentication failed.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_ACCOUNT_INACTIVE  : billingResultMessage.append("Payment gateway - Account inactive.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_INVALID_ACCOUNT_MODE       : billingResultMessage.append("Payment gateway - Invalid account mode.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_INAPPROPRIATE_PERMISSIONS  : billingResultMessage.append("Payment gateway - Inappropriate permissions.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_ACCESS_DENIED      : billingResultMessage.append("Payment gateway - Access denied.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_INVALID_FIELD_VALUE        : billingResultMessage.append("Payment gateway - Invalid field value.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_MISSING_MANDATORY_FIELD  : billingResultMessage.append("Payment gateway - Missing mandatory field.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_INVALID_FIELD_LENGTH   : billingResultMessage.append("Payment gateway - Invalid field length.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_INVALID_FIELD_TYPE  : billingResultMessage.append("Payment gateway - Invalid field type.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_RECORD_NOT_FOUND  : billingResultMessage.append("Payment gateway bad - Record not found.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_CIM_INTERFACE_DISABLED  : billingResultMessage.append("Payment gateway - CIM interface disable.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.PG_TRANSACTION_UNSUCCESSFUL   : billingResultMessage.append("Payment gateway - Transaction unsuccessful.");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				case ErrorCode.UNKNOWN_ERROR  : billingResultMessage.append("Unknown error");return PaymentGatewayResponseCodes.OCG_INTERNAL_ERROR ;
				default : billingResultMessage.append("Charge Failed");return PaymentGatewayResponseCodes.PGW_CHARGE_FAILED;
			}

		}

		@Override
		public boolean validateParameterTypes(Class<?>[] parameterTypes) 
		{
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && ApplyPaymentChargeRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
		}

		@Override
		public boolean validateReturnType(Class<?> returnType) 
		{
			return ApplyPaymentChargeResponse.class.isAssignableFrom(returnType);
		}

		@Override
		public Object[] getParameters(Context ctx, Object... parameters)
				throws CRMExceptionFault 
		{
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, ApplyPaymentChargeRequest.class, parameters);
                result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }
    	
    }

}
