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
package com.trilogy.app.crm.api.queryexecutor.deposit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.DepositToDepositReferenceAdapter;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.Deposit;
import com.trilogy.app.crm.bean.DepositStatusEnum;
import com.trilogy.app.crm.support.DepositSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.depositmanagement.DepositHistoryQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.depositmanagement.DepositHistoryReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;


/**
 * 
 * @author unmesh sonawane
 * @since 10x
 *
 */
public class DepositQueryExecutors {
	protected static final Adapter depositeReferenceAdapter_ = new DepositToDepositReferenceAdapter();
	/**
     * 
     * @author unmesh.sonawane
     * @since 10x
     * 
     * API implementation to update Deposit
     *
     */
    public static class UpdateDepositQueryExecutor extends AbstractQueryExecutor<Integer>
    {
        public UpdateDepositQueryExecutor()
        {
            
        }


        public Integer execute(Context mainCtx, Object... parameters) throws CRMExceptionFault
        {
            Context ctx = mainCtx.createSubContext();
            
            Map<String,Object> parameterMap = new HashMap<String,Object>();
            
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            
            int opCode = getParameter(ctx, PARAM_IDENTIFIER, PARAM_OPERATION_CODE, int.class, parameters);
            
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            
            GenericParameterParser parser = new GenericParameterParser(genericParameters);
            
            String ban 					= null;
            long deposit_id 			= -1;            
            String subscription_id 		= null;
            boolean calculate_intrest	= false;
            int reason_code 			= -1;
            long product_id 			= -1;
            
            List<DepositStatusEnum> depositStatusList = new ArrayList<DepositStatusEnum>();
            depositStatusList.add(DepositStatusEnum.ACTIVE);
            depositStatusList.add(DepositStatusEnum.ONHOLD);
            
            if (parser.containsParam(BAN))
            {
            	ban = parser.getParameter(BAN, String.class);
            	  parameterMap.put(BAN, ban);
            }
            
            if (parser.containsParam(DEPOSIT_ID))
            {
            	deposit_id = parser.getParameter(DEPOSIT_ID, Long.class);
            	 parameterMap.put(DEPOSIT_ID, deposit_id);
            }
            
            if (parser.containsParam(SUBSCRIPTION_ID))
            {
            	subscription_id = parser.getParameter(SUBSCRIPTION_ID, String.class);
            	parameterMap.put(SUBSCRIPTION_ID, subscription_id);
            }
            
            if (parser.containsParam(CALCULATE_INTREST))
            {
            	calculate_intrest = parser.getParameter(CALCULATE_INTREST, Boolean.class);
            	 parameterMap.put(CALCULATE_INTREST, calculate_intrest);
            }
            
            if (parser.containsParam(REASON_CODE))
            {
            	reason_code = parser.getParameter(REASON_CODE, Integer.class);
            	parameterMap.put(REASON_CODE, reason_code);
            }
            
            if (parser.containsParam(PRODUCT_ID))
            {
            	product_id = parser.getParameter(PRODUCT_ID, Long.class);
            	 parameterMap.put(PRODUCT_ID, product_id);
            }
            
            if (LogSupport.isDebugEnabled(ctx))
            {   
                LogSupport.debug(ctx, this, "parameterMap:"+parameterMap);
            }
            
            int result = 1;
           
            try
            {
            	
            	switch (opCode) {
        		case 2: case 3:{
        			List<Deposit> depositList = com.redknee.app.crm.support.DepositSupport.getDeposits(ctx, parameterMap);
        			com.redknee.app.crm.support.ReleaseDeposit deposit = com.redknee.app.crm.support.DepositReleaseFactory.releaseDeposit(opCode);
        			result = deposit.releaseDeposit(ctx, parameterMap, depositStatusList, depositList);
        			break;
        			}
        		case 1:
        			result = DepositSupport.updateSubscriptionId(ctx, parameterMap);
        			break;
        		default:
        			result = 1;
        			break;
        		}
        		
            }
            catch (final Exception e)
            {
                final String msg = "Unable to update Deposit::"+e.getMessage();         
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }
            
            return result;
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_IDENTIFIER = 1;
        
        public static final String PARAM_OPERATION_CODE = "opCode";        
        public static final int PARAM_GENERIC_PARAMETERS=2;        
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        private static final String BAN = "BAN";
        private static final String DEPOSIT_ID = "DEPOSIT_ID";
        private static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
        private static final String CALCULATE_INTREST = "CALCULATE_INTREST";
        private static final String REASON_CODE = "REASON_CODE";
        private static final String PRODUCT_ID = "PRODUCT_ID";
        
        
        @Override
	    public boolean validateParameterTypes(Class<?>[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=2);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && int.class.isAssignableFrom(parameterTypes[PARAM_IDENTIFIER]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }


        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return Integer.class.isAssignableFrom(resultType);
        }


        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_IDENTIFIER, PARAM_OPERATION_CODE, Integer.class, parameters);               
                result[2] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
                
            }
            else
            {
                result = parameters;
            }
            return result;
        }
    }
    
    /**
	 * 
	 * @author rohit.muneshwar
	 * @since 10x
	 *
	 */
	public static class DepositHistoryQueryExecutor extends AbstractQueryExecutor<DepositHistoryQueryResult> {
		public DepositHistoryQueryExecutor() {

		}

		public DepositHistoryQueryResult execute(Context mainCtx, Object... parameters) throws CRMExceptionFault {
			Context ctx = mainCtx.createSubContext();
			Integer spid = DepositSupport.getValidValue(getParameter(ctx, FIRST_PARAM_IDENTIFIER, FIRST_PARAM_OPERATION_CODE, Integer.class,
					parameters));
			String ban = DepositSupport.getValidValue(getParameter(ctx, SECOND_PARAM_IDENTIFIER, SECOND_PARAM_OPERATION_CODE, String.class,
					parameters));
			String subscriptionId = DepositSupport.getValidValue(getParameter(ctx, THIRD_PARAM_IDENTIFIER, THIRD_PARAM_OPERATION_CODE, String.class,
					parameters));
			Integer status = DepositSupport.getValidValue(getParameter(ctx, FOURTH_PARAM_IDENTIFIER, FOURTH_PARAM_OPERATION_CODE, Integer.class,
					parameters));
			
			DepositHistoryReference[] depositHistoryReferences = new DepositHistoryReference[]
	            {};
				
			try
	        {
				List<Deposit> depositList = DepositSupport.getDepositHistory(ctx, spid, ban, subscriptionId, status);
				depositHistoryReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx,
					depositList, depositeReferenceAdapter_, depositHistoryReferences);
			}catch(CRMExceptionFault e){
	            RmiApiErrorHandlingSupport.generalException(ctx, e, e.getMessage(), this);
			}
			catch	(final Exception e)
	        {
	            final String msg = "Exception while fetching deposit history";
	            RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
	        }
			DepositHistoryQueryResult depositHistoryQueryResult = new DepositHistoryQueryResult();
			depositHistoryQueryResult.setReferences(depositHistoryReferences);
			return depositHistoryQueryResult;
		}

		public static final int PARAM_HEADER = 0;
		public static final int FIRST_PARAM_IDENTIFIER = 1;
		public static final int SECOND_PARAM_IDENTIFIER = 2;
		public static final int THIRD_PARAM_IDENTIFIER = 3;
		public static final int FOURTH_PARAM_IDENTIFIER = 4;

		public static final String FIRST_PARAM_OPERATION_CODE = "spid";
		public static final String SECOND_PARAM_OPERATION_CODE = "ban";
		public static final String THIRD_PARAM_OPERATION_CODE = "subscriptionId";
		public static final String FOURTH_PARAM_OPERATION_CODE = "status";

		public boolean validateParameterTypes(Class<?>[] parameterTypes) {
			boolean result = true;
			result = result && (parameterTypes.length >= 2);
			result = result && int.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
			result = result && int.class.isAssignableFrom(parameterTypes[FIRST_PARAM_IDENTIFIER]);
			result = result && String.class.isAssignableFrom(parameterTypes[SECOND_PARAM_IDENTIFIER]);
			result = result && String.class.isAssignableFrom(parameterTypes[THIRD_PARAM_IDENTIFIER]);
			result = result && int.class.isAssignableFrom(parameterTypes[FOURTH_PARAM_IDENTIFIER]);

			return result;
		}

		public boolean validateReturnType(Class<?> resultType) {
			return DepositHistoryQueryResult.class.isAssignableFrom(resultType);
		}

		public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault {
			Object[] result = null;
			result = new Object[5];
			result[0] = parameters[0];
			result[1] = getParameter(ctx, FIRST_PARAM_IDENTIFIER, FIRST_PARAM_OPERATION_CODE, int.class, parameters);
			result[2] = getParameter(ctx, SECOND_PARAM_IDENTIFIER, SECOND_PARAM_OPERATION_CODE, String.class, parameters);
			result[3] = getParameter(ctx, THIRD_PARAM_IDENTIFIER, THIRD_PARAM_OPERATION_CODE, String.class, parameters);
			result[4] = getParameter(ctx, FOURTH_PARAM_IDENTIFIER, FOURTH_PARAM_OPERATION_CODE, int.class, parameters);
			return result;
		}

	}
}
