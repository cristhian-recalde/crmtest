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
package com.trilogy.app.crm.api.queryexecutor.calldetail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import org.hsqldb.lib.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;

import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.CallDetailToApiAdapter;
import com.trilogy.app.crm.api.rmi.CallTypeToApiAdapter;
import com.trilogy.app.crm.api.rmi.ChargedBundleInfoToApiAdapter;
import com.trilogy.app.crm.api.rmi.UsageTypeToApiAdapter;
import com.trilogy.app.crm.api.rmi.support.CallDetailsApiSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CallTypeXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.UsageTypeXInfo;
import com.trilogy.app.crm.bean.calldetail.ChargedBundlePicker;
import com.trilogy.app.crm.home.calldetail.CallDetailPrivacyRestrictionHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_1.types.calldetail.CallType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.calldetail.CallTypeReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.calldetail.UsageType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.calldetail.UsageTypeReference;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.CallDetail;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.CallDetailQueryResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.CallDetailReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.CallDetailWithBundles;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.ChargedBundleInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.DetailedCallDetailQueryResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.DetailedCallDetailWithBundlesQueryResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;

/**
 * 
 * @author Marcio Marques
 * @since 9.3
 *
 */
public class CallDetailQueryExecutors 
{
    
    /**
     * CRM call type to API call type adapter.
     */
    private static final CallTypeToApiAdapter callTypeToApiAdapter_ = new CallTypeToApiAdapter();

    /**
     * CRM usage type to API usage type adapter.
     */
    private static final UsageTypeToApiAdapter usageTypeToApiAdapter_ = new UsageTypeToApiAdapter();

    /**
     * CRM call detail to API call detail adapter.
     */
    private static final CallDetailToApiAdapter callDetailToApiAdapter_ = new CallDetailToApiAdapter();
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class CallTypeQueryExecutor extends AbstractQueryExecutor<CallType>
    {
        public CallTypeQueryExecutor()
        {
            
        }

        public CallType execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            long identifier = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, long.class, parameters);
            int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);

            com.redknee.app.crm.bean.CallType callType = null;
            try
            {
                // TODO change Call Type ID to long
                final And condition = new And();
                condition.add(new EQ(CallTypeXInfo.ID, Long.valueOf(identifier).intValue()));
                condition.add(new EQ(CallTypeXInfo.SPID, Integer.valueOf(spid)));
                callType = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.CallType.class, condition);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Call Type " + identifier;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            
            if (callType == null)
            {
                final String msg = "Call Type " + identifier;
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
            }
            
            final CallType result = CallTypeToApiAdapter.adaptCallTypeToApi(callType);
            
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
               result[1] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
               result[2] = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, long.class, parameters);
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
           result = result && int.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
           result = result && long.class.isAssignableFrom(parameterTypes[PARAM_IDENTIFIER]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }

       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return CallType.class.isAssignableFrom(resultType);
           
       }

       public static final int PARAM_HEADER = 0;
       public static final int PARAM_SPID = 1;
       public static final int PARAM_IDENTIFIER = 2;
       public static final int PARAM_GENERIC_PARAMETERS = 3;
       
       public static final String PARAM_SPID_NAME = "spid";
       public static final String PARAM_IDENTIFIER_NAME = "identifier";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class CallTypesListQueryExecutor extends AbstractQueryExecutor<CallTypeReference[]>
    {
        public CallTypesListQueryExecutor()
        {
            
        }

        public CallTypeReference[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);

            RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
            CallTypeReference[] callTypeReferences = new CallTypeReference[]
                {};
            try
            {
            	
                final EQ condition = new EQ(CallTypeXInfo.SPID, spid);
                final Collection<com.redknee.app.crm.bean.CallType> collection = HomeSupportHelper.get(ctx).getBeans(ctx,
                        com.redknee.app.crm.bean.CallType.class, condition, isAscending, CallTypeXInfo.ID); /** TT 13073023023 **/
                callTypeReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                        callTypeToApiAdapter_, callTypeReferences);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Call Types";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return callTypeReferences;
        }
        
        @Override
       public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           Object[] result = null;
           if (isGenericExecution(ctx, parameters))
           {
               result = new Object[4];
               result[0] = parameters[0];
               result[1] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
               result[2] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
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
           result = result && int.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
           result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }

       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return CallTypeReference[].class.isAssignableFrom(resultType);
           
       }

       public static final int PARAM_HEADER = 0;
       public static final int PARAM_SPID = 1;
       public static final int PARAM_IS_ASCENDING = 2;
       public static final int PARAM_GENERIC_PARAMETERS = 3;
       
       public static final String PARAM_SPID_NAME = "spid";
       public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class UsageTypeQueryExecutor extends AbstractQueryExecutor<UsageType>
    {
        public UsageTypeQueryExecutor()
        {
            
        }

        public UsageType execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            long identifier = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, long.class, parameters);

            com.redknee.app.crm.bean.UsageType usageType = null;
            try
            {
                final Object condition = new EQ(UsageTypeXInfo.ID, identifier);
                usageType = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.UsageType.class, condition);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Usage Type " + identifier;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            if (usageType == null)
            {
                final String msg = "Usage Type " + identifier;
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
            }
            final UsageType result = UsageTypeToApiAdapter.adaptUsageTypeToApi(usageType);
            
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
               result[1] = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, long.class, parameters);
               result[2] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
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
           result = result && long.class.isAssignableFrom(parameterTypes[PARAM_IDENTIFIER]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }

       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return UsageType.class.isAssignableFrom(resultType);
       }

       public static final int PARAM_HEADER = 0;
       public static final int PARAM_IDENTIFIER = 1;
       public static final int PARAM_GENERIC_PARAMETERS = 2;
       
       public static final String PARAM_IDENTIFIER_NAME = "identifier";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class UsageTypesListQueryExecutor extends AbstractQueryExecutor<UsageTypeReference[]>
    {
        public UsageTypesListQueryExecutor()
        {
            
        }

        public UsageTypeReference[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);

            RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
            UsageTypeReference[] usageTypeReferences = new UsageTypeReference[]
                {};
            try
            {
                // fix when UsageType becomes SPid aware
                // final Object condition = new EQ(UsageTypeXInfo.SPID,
                // Integer.valueOf(spid));
                final Object condition = True.instance();
                Collection<com.redknee.app.crm.bean.UsageType> collection = HomeSupportHelper.get(ctx).getBeans(ctx,
                        com.redknee.app.crm.bean.UsageType.class, condition, isAscending);
                usageTypeReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                        usageTypeToApiAdapter_, usageTypeReferences);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Usage Types";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return usageTypeReferences;
        }
        
        @Override
       public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           Object[] result = null;
           if (isGenericExecution(ctx, parameters))
           {
               result = new Object[4];
               result[0] = parameters[0];
               result[1] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
               result[2] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
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
           result = result && int.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
           result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }

       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return UsageTypeReference[].class.isAssignableFrom(resultType);
       }

       public static final int PARAM_HEADER = 0;
       public static final int PARAM_SPID = 1;
       public static final int PARAM_IS_ASCENDING = 2;
       public static final int PARAM_GENERIC_PARAMETERS = 3;
       
       public static final String PARAM_SPID_NAME = "spid";
       public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class CallDetailQueryExecutor extends AbstractQueryExecutor<CallDetail>
    {
        public CallDetailQueryExecutor()
        {
            
        }

        public CallDetail execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            long identifier = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, long.class, parameters);

            com.redknee.app.crm.bean.calldetail.CallDetail callDetail = null;
            
            try
            {
                callDetail = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.calldetail.CallDetail.class,
                        identifier);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Call Detail " + identifier;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            
            if (callDetail == null)
            {
                final String msg = "Call Detail " + identifier;
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
            }
            else
            {
                CallDetailPrivacyRestrictionHome.censor(ctx, callDetail);
            }
            
            final CallDetailWithBundles result = new CallDetailWithBundles();
            CallDetailToApiAdapter.adaptCallDetailToApi(ctx, callDetail, result);
            try{
            	ArrayList<com.redknee.app.crm.bean.ChargedBundleInfo> list = (ArrayList)ChargedBundlePicker.getChargedBundlesForCallDetail(ctx, identifier);
            	ChargedBundleInfo[] chargedBundleInfo = new ChargedBundleInfo[list.size()];
            	int count = 0;
            	for(com.redknee.app.crm.bean.ChargedBundleInfo chargedBundleInfo1 : list){
            		chargedBundleInfo[count] = ChargedBundleInfoToApiAdapter.adaptChargedBundleInfoToApi(ctx, chargedBundleInfo1);
            		count++;
            	}
            	result.setBundleInfo(chargedBundleInfo);
            }catch (final Exception e)
            {
                final String msg = "Unable to retrieve bundle info for :" + identifier;
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
               result[1] = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, long.class, parameters);
               result[2] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
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
           result = result && long.class.isAssignableFrom(parameterTypes[PARAM_IDENTIFIER]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return CallDetail.class.isAssignableFrom(resultType);
           
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_IDENTIFIER = 1;
       public static final int PARAM_GENERIC_PARAMETERS = 2;
       
       public static final String PARAM_IDENTIFIER_NAME = "identifier";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class CallDetailsListQueryExecutor extends AbstractQueryExecutor<CallDetailQueryResponse>
    {
        public CallDetailsListQueryExecutor()
        {
            
        }

        public CallDetailQueryResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            SubscriptionReference subscriptionRef = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
            Calendar start = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
            Calendar end = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
            String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
            int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef,PARAM_SUBSCRIPTION_REFERENCE_NAME);
            
            final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);

            final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, subscriber.getSpid(), this);

            final int maxLimitSize = spid.getMaxGetLimit();

            RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, maxLimitSize);
            final Long longPageKey = RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pageKey);

            CallDetailReference[] callDetailReferences = new CallDetailReference[] {};
            try
            {
                Collection<com.redknee.app.crm.bean.calldetail.CallDetail> collection = CallDetailsApiSupport.getCallDetailsUsingGivenParameters(
                        ctx, 
                        subscriber,
                        start, 
                        end, 
                        longPageKey, 
                        limit, 
                        isAscending);
                
                //collection = CallDetailsApiSupport.filterCallDetailsByPageKey(ctx, collection, longPageKey, isAscending, limit);
                
                for(com.redknee.app.crm.bean.calldetail.CallDetail obj: collection)
                {
                    CallDetailPrivacyRestrictionHome.censor(ctx, obj);
                }

                callDetailReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        collection, 
                        callDetailToApiAdapter_, 
                        callDetailReferences);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Call Details for start date " + start + ", end date " + end
                + ", pageKey " + pageKey + " and limit " + limit;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }

            final CallDetailQueryResponse result = new CallDetailQueryResponse();
            result.setReferences(callDetailReferences);
            if (callDetailReferences != null && callDetailReferences.length > 0)
            {
                result.setPageKey(String.valueOf(callDetailReferences[callDetailReferences.length - 1].getIdentifier()));
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
               result[1] = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
               result[2] = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
               result[3] = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
               result[4] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
               result[5] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
               result[6] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
               result[7] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
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
           result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
           result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START]);
           result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END]);
           result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
           result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
           result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }

       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return CallDetailQueryResponse.class.isAssignableFrom(resultType);
           
       }

       public static final int PARAM_HEADER = 0;
       public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
       public static final int PARAM_START = 2;
       public static final int PARAM_END = 3;
       public static final int PARAM_PAGE_KEY = 4;
       public static final int PARAM_LIMIT = 5;
       public static final int PARAM_IS_ASCENDING = 6;
       public static final int PARAM_GENERIC_PARAMETERS = 7;
       
       public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
       public static final String PARAM_START_NAME = "start";
       public static final String PARAM_END_NAME = "end";
       public static final String PARAM_PAGE_KEY_NAME = "pageKey";
       public static final String PARAM_LIMIT_NAME = "limit";
       public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class DetailedCallDetailsListQueryExecutor extends AbstractQueryExecutor<DetailedCallDetailQueryResponse>
    {
        public DetailedCallDetailsListQueryExecutor()
        {
            
        }

        public DetailedCallDetailQueryResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            SubscriptionReference subscriptionRef = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
            Calendar start = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
            Calendar end = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
            String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
            int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef,PARAM_SUBSCRIPTION_REFERENCE_NAME);
            
            final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);

            final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, subscriber.getSpid(), this);
            final int maxLimitSize = spid.getMaxGetLimit();

            RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, maxLimitSize);
            final Long longPageKey = RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pageKey);

            CallDetail[] callDetails = new CallDetail[] {};
            try
            {
                Collection<com.redknee.app.crm.bean.calldetail.CallDetail> collection = 
                    CallDetailsApiSupport.getCallDetailsUsingGivenParameters(
                            ctx, 
                            subscriber,
                            start, 
                            end, 
                            longPageKey, 
                            limit, 
                            isAscending);
                
                //collection = CallDetailsApiSupport.filterCallDetailsByPageKey(ctx, collection, longPageKey, isAscending, limit);

                callDetails = new CallDetail[collection.size()];

                int i = 0;
                for (com.redknee.app.crm.bean.calldetail.CallDetail callDetail : collection)
                {
                    CallDetailPrivacyRestrictionHome.censor(ctx, callDetail);
                    CallDetail apiCallDetail = new CallDetail();
                    CallDetailToApiAdapter.adaptCallDetailToApi(ctx, callDetail, apiCallDetail);
                    callDetails[i++] = apiCallDetail;
                }
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve all Call Details for start date " + start + ", end date " + end
                + ", pageKey " + pageKey + ", limit " + limit;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }

            final DetailedCallDetailQueryResponse result = new DetailedCallDetailQueryResponse();

            result.setResults(callDetails);
            if (callDetails != null && callDetails.length > 0)
            {
                result.setPageKey(String.valueOf(callDetails[callDetails.length - 1].getIdentifier()));
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
               result[1] = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
               result[2] = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
               result[3] = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
               result[4] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
               result[5] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
               result[6] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
               result[7] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
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
           result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
           result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START]);
           result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END]);
           result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
           result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
           result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return DetailedCallDetailQueryResponse.class.isAssignableFrom(resultType);
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
       public static final int PARAM_START = 2;
       public static final int PARAM_END = 3;
       public static final int PARAM_PAGE_KEY = 4;
       public static final int PARAM_LIMIT = 5;
       public static final int PARAM_IS_ASCENDING = 6;
       public static final int PARAM_GENERIC_PARAMETERS = 7;
       
       public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
       public static final String PARAM_START_NAME = "start";
       public static final String PARAM_END_NAME = "end";
       public static final String PARAM_PAGE_KEY_NAME = "pageKey";
       public static final String PARAM_LIMIT_NAME = "limit";
       public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Mangaraj Sahoo
     * @since 9.3.2
     *
     */
    public static class DetailedCallDetailsWithBundlesListQueryExecutor extends AbstractQueryExecutor<DetailedCallDetailWithBundlesQueryResponse>
    {
        public DetailedCallDetailsWithBundlesListQueryExecutor()
        {
            
        }

        public DetailedCallDetailWithBundlesQueryResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            SubscriptionReference subscriptionRef = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
            Calendar start = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
            Calendar end = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
            String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
            int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef,PARAM_SUBSCRIPTION_REFERENCE_NAME);
            
            final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);

            final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, subscriber.getSpid(), this);
            final int maxLimitSize = spid.getMaxGetLimit();

            RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, maxLimitSize);
            final Long longPageKey = RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pageKey);

            CallDetailWithBundles[] callDetails = new CallDetailWithBundles[] {};
            try
            {
                Collection<com.redknee.app.crm.bean.calldetail.CallDetail> collection = CallDetailsApiSupport
                        .getCallDetailsUsingGivenParameters(ctx, subscriber, start, end, longPageKey, limit, isAscending);
                
                //collection = CallDetailsApiSupport.filterCallDetailsByPageKey(ctx, collection, longPageKey, isAscending, limit);
                
                final ChargedBundlePicker bundlePicker = new ChargedBundlePicker(ctx, subscriber.getId(), 
                        CalendarSupportHelper.get(ctx).calendarToDate(start), CalendarSupportHelper.get(ctx).calendarToDate(end));

                callDetails = new CallDetailWithBundles[collection.size()];

                int cdCounter = 0;
                for (com.redknee.app.crm.bean.calldetail.CallDetail crmCallDetail : collection)
                {
                    CallDetailPrivacyRestrictionHome.censor(ctx, crmCallDetail);
                    CallDetailWithBundles apiCallDetail = new CallDetailWithBundles();
                    CallDetailToApiAdapter.adaptCallDetailToApi(ctx, crmCallDetail, apiCallDetail);
                    
                    Collection<com.redknee.app.crm.bean.ChargedBundleInfo> chargedBundles = bundlePicker.getChargedBundles(ctx, crmCallDetail);
                    ChargedBundleInfo[] apiChargedBundles = new ChargedBundleInfo[chargedBundles.size()];
                    
                    int bundleCounter = 0;
                    for (com.redknee.app.crm.bean.ChargedBundleInfo chargedBundle : chargedBundles)
                    {
                        apiChargedBundles[bundleCounter++] = ChargedBundleInfoToApiAdapter.adaptChargedBundleInfoToApi(ctx, chargedBundle);
                    }
                    apiCallDetail.setBundleInfo(apiChargedBundles);
                    
                    callDetails[cdCounter++] = apiCallDetail;
                }
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve all Call Details for start date " + start + ", end date " + end
                + ", pageKey " + pageKey + ", limit " + limit;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }

            final DetailedCallDetailWithBundlesQueryResponse result = new DetailedCallDetailWithBundlesQueryResponse();

            result.setResults(callDetails);
            if (callDetails != null && callDetails.length > 0)
            {
                result.setPageKey(String.valueOf(callDetails[callDetails.length - 1].getIdentifier()));
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
               result[1] = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
               result[2] = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
               result[3] = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
               result[4] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
               result[5] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
               result[6] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
               result[7] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
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
           result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
           result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START]);
           result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END]);
           result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
           result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
           result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return DetailedCallDetailQueryResponse.class.isAssignableFrom(resultType);
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
       public static final int PARAM_START = 2;
       public static final int PARAM_END = 3;
       public static final int PARAM_PAGE_KEY = 4;
       public static final int PARAM_LIMIT = 5;
       public static final int PARAM_IS_ASCENDING = 6;
       public static final int PARAM_GENERIC_PARAMETERS = 7;
       
       public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
       public static final String PARAM_START_NAME = "start";
       public static final String PARAM_END_NAME = "end";
       public static final String PARAM_PAGE_KEY_NAME = "pageKey";
       public static final String PARAM_LIMIT_NAME = "limit";
       public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

}
