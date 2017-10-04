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
package com.trilogy.app.crm.api.queryexecutor.loyalty;

import java.util.Map;

import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.LoyaltyToApiAdapter;
import com.trilogy.app.crm.api.rmi.impl.AccountsImpl;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.LoyaltyCard;
import com.trilogy.app.crm.bean.LoyaltyCardHome;
import com.trilogy.app.crm.client.urcs.LoyaltyOperationClient;
import com.trilogy.app.crm.client.urcs.LoyaltyParameters;
import com.trilogy.app.crm.client.urcs.LoyaltyProvisionClient;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.product.bundle.manager.provision.v4_0.loyalty.LoyaltyCardProfile;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyCardProfileReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyCardProfileReferenceByAccount;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyCardProfileReferenceByCard;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyPointsConversionRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyPointsConversionResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyProfileAssociationCreateRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyProfileAssociationResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyProfileBalanceResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyProfileBalanceUpdateRequest;

/**
 * 
 * @author Marcio Marques
 * @since 9.3
 *
 */
public class LoyaltyQueryExecutors 
{
	
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class LoyaltyProfileAssociationCreationQueryExecutor extends AbstractQueryExecutor<LoyaltyProfileAssociationResponse>
    {
        public LoyaltyProfileAssociationCreationQueryExecutor()
        {
            
        }

        /**
         *  Create a Loyalty Profile association in BSS and URCS for given subscriber account.
         *  
            <pre>Preconditions:
                - Account exists
                - Account does not have LCID already associated
                - Account not In-active
                - Account is a Subscriber account (isIndividual)
                - Account is Postpaid or Hybrid (not Prepaid)
            
            Postconditions:
                - LoyaltyCard association is created in BSS
                - LoyaltyCard profile is created in URCS
                
                Exceptions:
                    - For any exception, changes will be rolled back.
            </pre>

         *  
         *  @param header
         *  @param request  Contains the required fields for Loyalty Profile
         *  @return LoyaltyProfile 
         */
        public LoyaltyProfileAssociationResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            LoyaltyProfileAssociationCreateRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, LoyaltyProfileAssociationCreateRequest.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
            
            LoyaltyCard card = null;
            //1. Create Loyalty Card in BSS
            try
            {
                Account account = AccountsImpl.getCrmAccount(ctx, request.getAccountID(), this);
                if (!account.isIndividual(ctx))
                {
                    final String msg = "LoyaltyCard cannot be associated with Non-Subscriber Account";
                    RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, false, LoyaltyCard.class, null, this);
                }
                else if (AccountStateEnum.INACTIVE.equals(account.getState()))
                {
                    final String msg = "LoyaltyCard cannot be associated with Inactive Account";
                    RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, false, LoyaltyCard.class, null, this);
                }
                else if (account.isPrepaid())
                {
                    final String msg = "LoyaltyCard cannot be associated with Prepaid Account";
                    RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, false, LoyaltyCard.class, null, this);
                }
                LoyaltyCard newCard = LoyaltyToApiAdapter.adaptLoyaltyRequestToLoyaltyCard(ctx, request, account);
                ctx.put(LoyaltyCardHome.class, ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, (Home) ctx.get(LoyaltyCardHome.class)));            
                card = HomeSupportHelper.get(ctx).createBean(ctx, newCard);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to create LoyaltyCard for Account=" + request.getAccountID() + ".";
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, LoyaltyCard.class, null, this);
            }

            //2. Provision to URCS next
            LoyaltyProvisionClient client =  UrcsClientInstall.getClient(ctx, LoyaltyProvisionClient.class);
            try
            {
                LoyaltyCardProfile profile = LoyaltyToApiAdapter.adaptLoyaltyRequestToLoyaltyCardProfile(ctx, request);
                //call URCS client
                client.createLoyaltyProfile(ctx, profile);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to provision LoyaltyCard in URCS for Account " + request.getAccountID() + ".";
                rollbackLoyaltyCard(ctx, card, e, msg);
                
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, LoyaltyCard.class, null, this);
            }
            
            //3. Get profile from URCS
            LoyaltyProfileAssociationResponse result = null;
            try
            {
                //adapt result
                LoyaltyCardProfile cardProfile = client.getLoyaltyProfile(ctx, "", request.getLoyaltyCardID(), request.getProgramID());
                result = LoyaltyToApiAdapter.adaptLoyaltyCardProfileToResponse(ctx, cardProfile);
            }
            catch (final Exception e)
            {
                final String msg = "LoyaltyCard provisioned in URCS but unable to retrieve it, for Account " + request.getAccountID() + ".";
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, true, LoyaltyCard.class, null, this);
            }        
            
            return result;
        }

        private void rollbackLoyaltyCard(final Context ctx, LoyaltyCard card, final Exception e, final String msg)
        {
            if (card != null)
            {
                //Rollback
                try
                {
                    new InfoLogMsg(this, msg + ", attempting to rollback locally", e).log(ctx);
                    HomeSupportHelper.get(ctx).removeBean(ctx, card);
                }
                catch (Exception e2)
                {
                    new MinorLogMsg(this, msg + ", failed to rollback locally", e2).log(ctx);
                }
            }
        }

        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, LoyaltyProfileAssociationCreateRequest.class, parameters);
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
            result = result && LoyaltyProfileAssociationCreateRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return LoyaltyProfileAssociationResponse.class.isAssignableFrom(resultType);
        }

        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class LoyaltyPointsConversionQueryExecutor extends AbstractQueryExecutor<LoyaltyPointsConversionResponse>
    {
        public LoyaltyPointsConversionQueryExecutor()
        {
            
        }

        public LoyaltyPointsConversionResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            LoyaltyPointsConversionRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, LoyaltyPointsConversionRequest.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);

            //Handle input parameters
            String programId = request.getProfileReference().getProgramID();
            String ban = "";
            String cardId = "";
            LoyaltyCardProfileReference profile = request.getProfileReference();
            if (profile instanceof LoyaltyCardProfileReferenceByAccount)
            {
                ban = ((LoyaltyCardProfileReferenceByAccount)profile).getAccountID();
            }
            else if (profile instanceof LoyaltyCardProfileReferenceByCard)
            {
                cardId = ((LoyaltyCardProfileReferenceByCard)profile).getLoyaltyCardID();
            }
            else
            {
                RmiApiErrorHandlingSupport.simpleValidation("request.profileReference", 
                        "profile must be of type LoyaltyCardProfileReferenceByAccount or LoyaltyCardProfileReferenceByCard");
            }

            LoyaltyPointsConversionResponse result = null;
            try
            {
                //update profile
                LoyaltyOperationClient client =  UrcsClientInstall.getClient(ctx, LoyaltyOperationClient.class);
                
                LoyaltyParameters params = client.convertLoyaltyPoints(ctx, ban, cardId, programId, 
                        request.getRedemptionType().getValue(), 
                        request.getPartnerID(), 
                        (int)request.getVoucherType(), 
                        request.getPoints(), 
                        request.getAmount());

                result = LoyaltyToApiAdapter.adaptLoyaltyCardProfileToConversionResponse(ctx, params);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to convert loyalty points for Account=" + ban + 
                    ", Loyalty Card ID=" + cardId + ", programId=" + programId + ".";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return result;
        }


        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, LoyaltyPointsConversionRequest.class, parameters);
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
            result = result && LoyaltyPointsConversionRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return LoyaltyPointsConversionResponse.class.isAssignableFrom(resultType);
        }


        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class LoyaltyProfileBalanceUpdateQueryExecutor extends AbstractQueryExecutor<LoyaltyProfileBalanceResponse>
    {
        public LoyaltyProfileBalanceUpdateQueryExecutor()
        {
            
        }

        public LoyaltyProfileBalanceResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            LoyaltyProfileBalanceUpdateRequest request = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, LoyaltyProfileBalanceUpdateRequest.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_REQUEST_NAME);
            
            //Handle input parameters
            String programId = request.getProfileReference().getProgramID();
            String ban = "";
            String cardId = "";
            LoyaltyCardProfileReference profile = request.getProfileReference();
            if (profile instanceof LoyaltyCardProfileReferenceByAccount)
            {
                ban = ((LoyaltyCardProfileReferenceByAccount)profile).getAccountID();
            }
            else if (profile instanceof LoyaltyCardProfileReferenceByCard)
            {
                cardId = ((LoyaltyCardProfileReferenceByCard)profile).getLoyaltyCardID();
            }
            else
            {
                RmiApiErrorHandlingSupport.simpleValidation("request.profileReference", 
                        "profile must be of type LoyaltyCardProfileReferenceByAccount or LoyaltyCardProfileReferenceByCard");
            }

            LoyaltyProfileBalanceResponse result = null;
            try
            {
                //update profile
                LoyaltyOperationClient client =  UrcsClientInstall.getClient(ctx, LoyaltyOperationClient.class);
                LoyaltyParameters params = null;
                
                if (request.getType().getValue() == 0)
                {
                    params = client.accumulateLoyaltyPoints(ctx, ban, cardId, programId, request.getTransactionID(), (int) request.getSourceType().getValue(), request.getAmount(), request.getUserID(), request.getUserNote(), request.getUserLocation());
                }
                else if (request.getType().getValue() == 1)
                {
                    params = client.redeemLoyaltyPoints(ctx, ban, cardId, programId, request.getTransactionID(), (int) request.getSourceType().getValue(), request.getAmount(), request.getUserID(), request.getUserNote(), request.getUserLocation());
                }
                else if (request.getType().getValue() == 2)
                {
                    params = client.adjustLoyaltyPoints(ctx, ban, cardId, programId, request.getTransactionID(), (int) request.getSourceType().getValue(), request.getAmount(), request.getUserID(), request.getUserNote(), request.getUserLocation());
                }
                else
                {
                    RmiApiErrorHandlingSupport.simpleValidation("request.type", 
                        "type must of Accumulation(0), Redemption(1) or Adjustment(2)");
                }

                result = LoyaltyToApiAdapter.adaptLoyaltyParametersToResponse(ctx, params);
            }
            catch (final Exception e)
            {
                final String msg = "Unable update loyalty points balance for Account=" + ban + 
                    ", Loyalty Card ID=" + cardId + ", programId=" + programId + ".";
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }
            
            return result;
        }


        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_REQUEST, PARAM_REQUEST_NAME, LoyaltyProfileBalanceUpdateRequest.class, parameters);
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
            result = result && LoyaltyProfileBalanceUpdateRequest.class.isAssignableFrom(parameterTypes[PARAM_REQUEST]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return LoyaltyProfileBalanceResponse.class.isAssignableFrom(resultType);
        }


        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_REQUEST = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_REQUEST_NAME = "request";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class LoyaltyProfileBalanceQueryExecutor extends AbstractQueryExecutor<LoyaltyProfileBalanceResponse>
    {
        public LoyaltyProfileBalanceQueryExecutor()
        {
            
        }

        public LoyaltyProfileBalanceResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            LoyaltyCardProfileReference profile = getParameter(ctx, PARAM_PROFILE, PARAM_PROFILE_NAME, LoyaltyCardProfileReference.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(profile, PARAM_PROFILE_NAME);
            
            //Handle input parameters
            String programId = profile.getProgramID();
            String ban = "";
            String cardId = "";
            if (profile instanceof LoyaltyCardProfileReferenceByAccount)
            {
                ban = ((LoyaltyCardProfileReferenceByAccount)profile).getAccountID();
            }
            else if (profile instanceof LoyaltyCardProfileReferenceByCard)
            {
                cardId = ((LoyaltyCardProfileReferenceByCard)profile).getLoyaltyCardID();
            }
            else
            {
                RmiApiErrorHandlingSupport.simpleValidation("profile", 
                        "profile must be of type LoyaltyCardProfileReferenceByAccount or LoyaltyCardProfileReferenceByCard");
            }

            LoyaltyProfileBalanceResponse result = null;
            try
            {
                //update profile
                LoyaltyOperationClient client =  UrcsClientInstall.getClient(ctx, LoyaltyOperationClient.class);
                long points = client.queryLoyaltyPoints(ctx, ban, cardId, programId);
               
                
                //get updated profile
                result = LoyaltyToApiAdapter.adaptLoyaltyCardProfileToResponse(ctx, points);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to query loyalty points balance for Account=" + ban + 
                    ", Loyalty Card ID=" + cardId + ", programId=" + programId + ".";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return result;


        }


        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_PROFILE, PARAM_PROFILE_NAME, LoyaltyCardProfileReference.class, parameters);
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
            result = result && LoyaltyCardProfileReference.class.isAssignableFrom(parameterTypes[PARAM_PROFILE]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return LoyaltyProfileBalanceResponse.class.isAssignableFrom(resultType);
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_PROFILE = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_PROFILE_NAME = "profile";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class LoyaltyProfileAssociationQueryExecutor extends AbstractQueryExecutor<LoyaltyProfileAssociationResponse>
    {
        public LoyaltyProfileAssociationQueryExecutor()
        {
            
        }

        public LoyaltyProfileAssociationResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            LoyaltyCardProfileReference profile = getParameter(ctx, PARAM_PROFILE, PARAM_PROFILE_NAME, LoyaltyCardProfileReference.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(profile, PARAM_PROFILE_NAME);
            
            String programId = profile.getProgramID();
            String ban = "";
            String cardId = "";
            
            if (profile instanceof LoyaltyCardProfileReferenceByAccount)
            {
                ban = ((LoyaltyCardProfileReferenceByAccount)profile).getAccountID();
            }
            else if (profile instanceof LoyaltyCardProfileReferenceByCard)
            {
                cardId = ((LoyaltyCardProfileReferenceByCard)profile).getLoyaltyCardID();
            }
            else
            {
                RmiApiErrorHandlingSupport.simpleValidation("profile", 
                        "profile must be of type LoyaltyCardProfileReferenceByAccount or LoyaltyCardProfileReferenceByCard");
            }
            
            LoyaltyProfileAssociationResponse result = null;
            try
            {
                LoyaltyProvisionClient client =  UrcsClientInstall.getClient(ctx, LoyaltyProvisionClient.class);
                LoyaltyCardProfile cardProfile = client.getLoyaltyProfile(ctx, ban, cardId, programId);
                result = LoyaltyToApiAdapter.adaptLoyaltyCardProfileToResponse(ctx, cardProfile);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to query loyalty profile for Account=" + ban + 
                    ", Loyalty Card ID=" + cardId + ", programId=" + programId + ".";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            
            return result;
        }


        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_PROFILE, PARAM_PROFILE_NAME, LoyaltyCardProfileReference.class, parameters);
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
            result = result && LoyaltyCardProfileReference.class.isAssignableFrom(parameterTypes[PARAM_PROFILE]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return LoyaltyProfileAssociationResponse.class.isAssignableFrom(resultType);
        }

        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_PROFILE = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_PROFILE_NAME = "profile";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class LoyaltyProfileAssociationUpdateQueryExecutor extends AbstractQueryExecutor<LoyaltyProfileAssociationResponse>
    {
        public LoyaltyProfileAssociationUpdateQueryExecutor()
        {
            
        }

        public LoyaltyProfileAssociationResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            LoyaltyCardProfileReference profile = getParameter(ctx, PARAM_PROFILE, PARAM_PROFILE_NAME, LoyaltyCardProfileReference.class, parameters);
            GenericParameter[] profileParameters = getGenericParameters(ctx, PARAM_PROFILE_PARAMETERS, PARAM_PROFILE_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(profileParameters, PARAM_PROFILE_PARAMETERS_NAME);

            //Handle input parameters
            String programId = profile.getProgramID();
            String ban = "";
            String cardId = "";
            if (profile instanceof LoyaltyCardProfileReferenceByAccount)
            {
                ban = ((LoyaltyCardProfileReferenceByAccount)profile).getAccountID();
            }
            else if (profile instanceof LoyaltyCardProfileReferenceByCard)
            {
                cardId = ((LoyaltyCardProfileReferenceByCard)profile).getLoyaltyCardID();
            }
            else
            {
                RmiApiErrorHandlingSupport.simpleValidation("profile", 
                        "profile must be of type LoyaltyCardProfileReferenceByAccount or LoyaltyCardProfileReferenceByCard");
            }

            final Map<String, Object> map = APIGenericParameterSupport.createGenericParameterMap(profileParameters);
            Boolean enableAccumulation = 
                //APIGenericParameterSupport.getBoolean(map.get(LoyaltyCardProfileParamIDEnum.ACCUMULATION_ENABLED.ordinal()));
                APIGenericParameterSupport.getBooleanOrNull(map.get("2"));
            Boolean enableRedemption = 
                //APIGenericParameterSupport.getBoolean(map.get(LoyaltyCardProfileParamIDEnum.REDEMPTION_ENABLED.ordinal()));
                APIGenericParameterSupport.getBooleanOrNull(map.get("1"));
            
            LoyaltyProfileAssociationResponse result = null;
            try
            {
                //update profile
                LoyaltyProvisionClient client =  UrcsClientInstall.getClient(ctx, LoyaltyProvisionClient.class);
                client.updateLoyaltyProfile(ctx, ban, cardId, programId, enableAccumulation, enableRedemption);
        
                //get updated profile
                LoyaltyCardProfile cardProfile = client.getLoyaltyProfile(ctx, ban, cardId, programId);
                
                result = LoyaltyToApiAdapter.adaptLoyaltyCardProfileToResponse(ctx, cardProfile);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to update loyalty card profile in URCS for Account=" + ban + 
                    ", Loyalty Card ID=" + cardId + ", programId=" + programId + ".";
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }
            return result;

        }


        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[4];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_PROFILE, PARAM_PROFILE_NAME, LoyaltyCardProfileReference.class, parameters);
                result[2] = getGenericParameters(ctx, PARAM_PROFILE_PARAMETERS, PARAM_PROFILE_PARAMETERS_NAME, parameters);
                result[3] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
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
            result = result && (parameterTypes.length>=4);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && LoyaltyCardProfileReference.class.isAssignableFrom(parameterTypes[PARAM_PROFILE]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_PROFILE_PARAMETERS]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return LoyaltyProfileAssociationResponse.class.isAssignableFrom(resultType);
        }


        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_PROFILE = 1;
        public static final int PARAM_PROFILE_PARAMETERS = 2;
        public static final int PARAM_GENERIC_PARAMETERS = 3;
        
        public static final String PARAM_PROFILE_NAME = "profile";
        public static final String PARAM_PROFILE_PARAMETERS_NAME = "profileParameters";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "otherParameters";
    }
}
