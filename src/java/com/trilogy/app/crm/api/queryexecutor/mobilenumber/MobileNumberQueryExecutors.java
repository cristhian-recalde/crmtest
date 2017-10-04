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
package com.trilogy.app.crm.api.queryexecutor.mobilenumber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import oracle.net.aso.s;

import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.MsisdnToMobileNumberAdapter;
import com.trilogy.app.crm.api.rmi.MsisdnToMobileNumberReferenceAdapter;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupXInfo;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.elang.PagingXStatement;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidType;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumberStateEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumber;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumberQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumberReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumberState;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.PortingTypeReference;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author Marcio Marques
 * @since 9.3
 *
 */
public class MobileNumberQueryExecutors 
{
    protected static final Adapter msisdnReferenceAdapter_ = new MsisdnToMobileNumberReferenceAdapter();
    protected static final Adapter mobileNumberAdapter_ = new MsisdnToMobileNumberAdapter();

    /**
	 * 
	 * @author Marcio Marques
	 * @since 9.3.0
	 *
	 */
	public static class MobileNumbersListQueryExecutor extends AbstractQueryExecutor<MobileNumberQueryResult>
	{
	    
		public MobileNumbersListQueryExecutor()
		{
			
		}

	    public MobileNumberQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
	    	int limit = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
            long groupID = getParameter(ctx, PARAM_GROUP_ID, PARAM_GROUP_ID_NAME, long.class, parameters);
            MobileNumberState state = getParameter(ctx, PARAM_STATE, PARAM_STATE_NAME, MobileNumberState.class, parameters);
            String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
            PaidType paidType = getParameter(ctx, PARAM_PAID_TYPE, PARAM_PAID_TYPE_NAME, PaidType.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

	        final User user = RmiApiSupport.retrieveUser(ctx, header, this.getClass().getName());
	        
	        MsisdnStateEnum bssState = null;
	        SubscriberTypeEnum bssPaidType = null;
	        int bssGroupID = Long.valueOf(groupID).intValue();
	        
	        MsisdnGroup group = null;
	        try
	        {
	        	group = HomeSupportHelper.get(ctx).findBean(ctx, MsisdnGroup.class, new EQ(MsisdnGroupXInfo.ID, Integer.valueOf(bssGroupID)));
	        }
	        catch (HomeException e)
	        {
	        	RmiApiErrorHandlingSupport.generalException(ctx, e, "Unable to retrieve Mobile Number Group for groupID " + groupID, this);
	        }
	        
            if(!GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
            {
                /*
                 *  We need to skip this validation for shared pool of msisdns, as the intent of list mobile numbers would be 
                 *  to list all msisdns including those with SPID = -1. So spidaware validations can not be done.
                 */
                
                CRMSpid crmSpid = null;
    	        if (group != null)
            	{
            	       	crmSpid = RmiApiSupport.getCrmServiceProvider(ctx, group.getSpid(), this);
            	}
            	else
            	{
            	      	crmSpid = RmiApiSupport.getCrmServiceProvider(ctx, user.getSpid(), this);
            	}
    	        
    	        RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, crmSpid.getMaxGetLimit());
	        }

	      

	        
	        GenericParameterParser parser = new GenericParameterParser(genericParameters);
	        boolean collisionAvoidance = parser.getParameter(COLLISION_AVOIDANCE, Boolean.class, Boolean.FALSE);
	        
	        MobileNumberReference[] numberReferences = new MobileNumberReference[]
	            {};
	        try
	        {

	            
	            if(LogSupport.isDebugEnabled(ctx))
	            {
	                Home home = HomeSupportHelper.get(ctx).getHome(ctx, com.redknee.app.crm.bean.core.Msisdn.class);
                    LogSupport.debug(ctx, this, "Fetching msisdns... from Home: "+ home);
	            }
	            

	            boolean ascending = RmiApiSupport.isSortAscending(isAscending);
	            
	            final And condition = new And();
	            
	            condition.add(new EQ(MsisdnXInfo.GROUP, bssGroupID));
	            
	            if (state != null)
	            {
	                bssState = RmiApiSupport.convertApiMsisdnState2Crm(state);
	                condition.add(new EQ(MsisdnXInfo.STATE, bssState));
	            }
	            
	            
                if (paidType != null)
                {
                    bssPaidType = RmiApiSupport.convertApiPaidType2CrmSubscriberType(paidType);
                    
                    // Share pool condition 
                    Predicate p = GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx) ?
                            new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.HYBRID) :
                                False.instance();
                    final Or or = new Or();
                    or.add(new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, bssPaidType));
                    or.add(p);
                    condition.add(or);
                }

                if (pageKey != null && pageKey.length() > 0)
	            {
	                condition.add(new PagingXStatement(MsisdnXInfo.MSISDN, pageKey, ascending));
	            }

                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, this, "Msisdn Filter Condition: "+ condition);
                
                Collection<Msisdn> collection = null;

                if (!collisionAvoidance)
                {
                    collection = HomeSupportHelper.get(ctx).getBeans(ctx, Msisdn.class, condition, limit,
	                    ascending);
                }
                else if (bssPaidType==null || bssState==null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation(PARAM_GENERIC_PARAMETERS_NAME, "'"
                            + COLLISION_AVOIDANCE + "' generic parameter only supported if '" + PARAM_PAID_TYPE_NAME
                            + "' and '" + PARAM_STATE_NAME + "' parameters are provided.");
                }
                else if (bssState.getIndex() != MsisdnStateEnum.AVAILABLE_INDEX)
                {
                    RmiApiErrorHandlingSupport.simpleValidation(PARAM_GENERIC_PARAMETERS_NAME, "'"
                            + COLLISION_AVOIDANCE + "' generic parameter only supported if '" + PARAM_STATE_NAME
                            + "' parameter has value equals AVAILABLE (0).");
                }
                else
                {
             
                    if (group==null)
                    {
                        // Return empty collection for invalid group
                        collection = new ArrayList<Msisdn>();
                    }
                    else
                    {
                        int fetchingFactor = group.getFetchingFactor();
                        Long fetchingSize = (long) limit*fetchingFactor;
                        
                        if (fetchingSize>Integer.MAX_VALUE)
                        {
                            fetchingSize = (long) Integer.MAX_VALUE;
                        }
                        
                        Collection<Msisdn> fullCollection = HomeSupportHelper.get(ctx).getBeans(ctx, Msisdn.class, condition, fetchingSize.intValue(),
                                ascending);
                        int fullCollectionSize = fullCollection.size();
                        
                        if (fullCollectionSize<=limit)
                        {
                            collection = fullCollection;
                        }
                        else
                        {
                            collection = new ArrayList<Msisdn>();
                            Set<Integer> selectedRows = new HashSet<Integer>();
                            Random randomizer = new Random();
                            
                            for (int i=0; i<limit; i++)
                            {
                                int next;
                                do
                                {
                                    next = randomizer.nextInt(fullCollectionSize);
                                }
                                while (selectedRows.contains(next));

                                collection.add(((ArrayList<Msisdn>)fullCollection).get(next));
                                selectedRows.add(next);
                            }
                        }

                    }
                        
                }
	            
	            numberReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection, msisdnReferenceAdapter_,
	                    numberReferences);
	        }
	        catch (final Exception e)
	        {
	            final String msg = "Unable to retrieve Mobile Numbers for groupID " + groupID + " state " + state
	                    + " pageKey " + pageKey + " limit " + limit + " isAscending " + isAscending;
	            RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
	        }
	        
	        final MobileNumberQueryResult result = new MobileNumberQueryResult();
	        result.setReferences(numberReferences);

	        if (numberReferences != null && numberReferences.length > 0)
	        {
	            result.setPageKey(numberReferences[numberReferences.length - 1].getIdentifier());
	        }

	        return result;
	    }
	    
        @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[8];
	            result[0] = parameters[0];
	            result[1] = getParameter(ctx, PARAM_GROUP_ID, PARAM_GROUP_ID_NAME, long.class, parameters);
                result[2] = getParameter(ctx, PARAM_STATE, PARAM_STATE_NAME, MobileNumberState.class, parameters);
                result[3] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
                result[4] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
                result[5] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
                result[6] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	            result[7] = getParameter(ctx, PARAM_PAID_TYPE, PARAM_PAID_TYPE_NAME, PaidType.class, parameters);
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
	        result = result && (parameterTypes.length>=8);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && long.class.isAssignableFrom(parameterTypes[PARAM_GROUP_ID]);
            result = result && MobileNumberState.class.isAssignableFrom(parameterTypes[PARAM_STATE]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
            result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
            result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        result = result && PaidType.class.isAssignableFrom(parameterTypes[PARAM_PAID_TYPE]);
	        return result;
	    }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return MobileNumberQueryResult.class.isAssignableFrom(resultType);
        }

        protected static final String COLLISION_AVOIDANCE = "COLLISION_AVOIDANCE";
	    
	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_GROUP_ID = 1;
        public static final int PARAM_STATE = 2;
        public static final int PARAM_PAGE_KEY = 3;
        public static final int PARAM_LIMIT = 4;
        public static final int PARAM_IS_ASCENDING = 5;
        public static final int PARAM_GENERIC_PARAMETERS = 6;
	    public static final int PARAM_PAID_TYPE = 7;
	    
        public static final String PARAM_GROUP_ID_NAME = "groupID";
        public static final String PARAM_STATE_NAME = "state";
        public static final String PARAM_PAGE_KEY_NAME = "pageKey";
        public static final String PARAM_LIMIT_NAME = "limit";
        public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        public static final String PARAM_PAID_TYPE_NAME = "paidType";
	}
	
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class MobileNumberQueryExecutor extends AbstractQueryExecutor<MobileNumber>
    {
        public MobileNumberQueryExecutor()
        {
            
        }

        public MobileNumber execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            String identifier = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, String.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(identifier, PARAM_IDENTIFIER_NAME);

            MobileNumber result = null;
            try
            {
                Msisdn msisdn  = MsisdnSupport.getMsisdn(ctx, identifier);

                if (msisdn!=null)
                {
                    result = (MobileNumber) mobileNumberAdapter_.adapt(ctx, msisdn);
                }
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Mobile Number with identifier '" + identifier + "'";
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }
            
            if (result == null)
            {
                final String msg = "Mobile Number " + identifier + " cannot be found!";
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
            }

            return result;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[2];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, String.class, parameters);
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
            result = result && (parameterTypes.length>=2);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_IDENTIFIER]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return MobileNumber.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_IDENTIFIER = 1;
        
        public static final String PARAM_IDENTIFIER_NAME = "identifier";
    }
	
    /**
     * 
     * @author Kumaran Sivasubramaniam
     * @since 9.3.0
     *
     */
    public static class MobileNumberUpdateStateQueryExecutor extends AbstractQueryExecutor<MobileNumber>
    {
        public MobileNumberUpdateStateQueryExecutor()
        {
            
        }


        public MobileNumber execute(Context mainCtx, Object... parameters) throws CRMExceptionFault
        {
            Context ctx = mainCtx.createSubContext();
            String identifier = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, String.class, parameters);
            MobileNumberState state = getParameter(ctx, PARAM_STATE, PARAM_STATE_NAME, MobileNumberState.class,
                    parameters);
            RmiApiErrorHandlingSupport.validateMandatoryObject(identifier, PARAM_IDENTIFIER_NAME);
            
            boolean snapBack;
            {
                GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
                if(null == genericParameters)
                {
                    snapBack = false;
                }
                else
                {
                    GenericParameterParser parser = new GenericParameterParser(genericParameters);
                    snapBack = Boolean.TRUE.equals(parser.getParameter(APIGenericParameterSupport.SNAP_BACK_IN, Boolean.class));
                }
                
            }
            
            
            MobileNumber result = null;
            try
            {
                Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, identifier);
                MsisdnStateEnum oldState = msisdn.getState();
                MsisdnStateEnum newState = getBSStateFromApi(ctx, state);
                msisdn.setState(newState);
                if(snapBack)
                {
                    if(PortingTypeEnum.IN == msisdn.getPortingType())
                    {
                        throw new Exception("Cannot snap-back MSISDN [ " + msisdn.getMsisdn() + "]. It is not owned by the system but ported-in");
                    }
                    msisdn.setPortingType(PortingTypeEnum.NONE);
                    ERLogger.logSnapBackER(ctx, msisdn);
                } else if(PortingTypeEnum.NONE != msisdn.getPortingType())
                {
                    throw new Exception("State of a ported number cannot be changed without a valid SnapBackIn or PortIn request.");
                }
                
                Msisdn newMsisdn = HomeSupportHelper.get(ctx).storeBean(ctx, msisdn);
                if (newMsisdn != null)
                {
                    result = (MobileNumber) mobileNumberAdapter_.adapt(ctx, msisdn);
                }
            }
            catch (final Exception e)
            {
                final String msg = "Unable to set Mobile Number with identifier '" + identifier + "' to state " + state.getValue();
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }
            if (result == null)
            {
                final String msg = "Mobile Number " + identifier + " cannot be found!";
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
            }
            return result;
        }


        private MsisdnStateEnum getBSStateFromApi(final Context ctx, MobileNumberState state) throws CRMExceptionFault
        {
            if (state.getValue() == MobileNumberStateEnum.AVAILABLE.getValue().getValue())
            {
                return MsisdnStateEnum.AVAILABLE;
            }
            else if (state.getValue() == MobileNumberStateEnum.IN_USE.getValue().getValue())
            {
                return MsisdnStateEnum.IN_USE;
            }
            else if (state.getValue() == MobileNumberStateEnum.HELD.getValue().getValue())
            {
                return MsisdnStateEnum.HELD;
            }
            
            RmiApiErrorHandlingSupport.generalException(ctx, null,
                    "Cannot set Msisdn state to " + state.getValue(), this);
            return null;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[4];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, String.class, parameters);
                result[2] = getParameter(ctx, PARAM_STATE, PARAM_STATE_NAME, String.class, parameters);
                result[3] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
                
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
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_IDENTIFIER]);
            result = result && MobileNumberState.class.isAssignableFrom(parameterTypes[PARAM_IDENTIFIER]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return MobileNumber.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_IDENTIFIER = 1;
        public static final int PARAM_STATE =2;
        public static final int PARAM_GENERIC_PARAMETERS=3;
        
        public static final String PARAM_IDENTIFIER_NAME = "identifier";
        public static final String PARAM_STATE_NAME = "newState";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        
    }
    
    
    
    public static class MobileNumberPortingTypeQueryExecutor extends AbstractQueryExecutor<PortingTypeReference>
    {
        public MobileNumberPortingTypeQueryExecutor()
        {
            
        }

        public PortingTypeReference execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            String identifier = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, String.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(identifier, PARAM_IDENTIFIER_NAME);

            PortingTypeReference protingTypeReference = new PortingTypeReference();
            
            try
            {
                Msisdn msisdn  = MsisdnSupport.getMsisdn(ctx, identifier);

                if (msisdn==null)
                {
                	throw new Exception();
                }
                protingTypeReference.setPortingType(com.redknee.util.crmapi.wsdl.v3_0.types.mobilenumber.PortingTypeEnum.valueOf(msisdn.getPortingType().getIndex()));
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Mobile Number with identifier '" + identifier + "'";
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }
            
            return protingTypeReference;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[2];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, String.class, parameters);
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
            result = result && (parameterTypes.length>=2);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_IDENTIFIER]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return MobileNumber.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_IDENTIFIER = 1;
        
        public static final String PARAM_IDENTIFIER_NAME = "identifier";
    }
    
    
}
