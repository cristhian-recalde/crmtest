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
package com.trilogy.app.crm.api.queryexecutor.subscription;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Iterator;


import com.trilogy.framework.xhome.elang.And;
import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.buckethistory.BucketHistorySupport;
import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.ExecuteResultQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.GenericParametersAdapter;
import com.trilogy.app.crm.api.rmi.BalanceHistoryToApiAdapter;
import com.trilogy.app.crm.api.rmi.CallDetailToApiAdapter;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.SubscriberBucketToApiAdapter;
import com.trilogy.app.crm.api.rmi.SubscriptionBalanceApiAdapter;
import com.trilogy.app.crm.api.rmi.impl.SubscribersImpl;
import com.trilogy.app.crm.api.rmi.previewfees.PreviewUpdateFees;
import com.trilogy.app.crm.api.rmi.previewfees.SubscriptionUpdateFeesFactory;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.CallDetailsApiSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.api.rmi.support.TransactionsApiSupport;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.bean.BalanceHistory;
import com.trilogy.app.crm.bean.BucketHistory;
import com.trilogy.app.crm.bean.BucketHistoryHome;
import com.trilogy.app.crm.bean.BucketHistoryXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.ChargedBundlePicker;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.usage.BalanceUsage;
import com.trilogy.app.crm.bean.DeactivatedReasonEnum;
import com.trilogy.app.crm.bundle.Balance;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.bundle.SubscriberBucketXInfo;
import com.trilogy.app.crm.bundle.SubscriberBucketsAndBalances;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.app.crm.bundle.service.adapters.CollectionAdapter;
import com.trilogy.app.crm.bundle.service.adapters.CollectionAdapter.ComponentAdapter;
import com.trilogy.app.crm.defaultvalue.IntValue;
import com.trilogy.app.crm.home.calldetail.CallDetailPrivacyRestrictionHome;
import com.trilogy.app.crm.move.MoveManager;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.ReverseActivationMoveRequest;
import com.trilogy.app.crm.move.support.MoveRequestSupport;
import com.trilogy.app.crm.secondarybalance.CategoryIdBalanceMapper;
import com.trilogy.app.crm.subscriber.state.SubscriberStateTransitionSupport;
import com.trilogy.app.crm.support.BalanceHistorySupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.OcgAdj2CrmAdjSupport;
import com.trilogy.app.crm.util.CollectionsUtils;
import com.trilogy.app.crm.util.TypedPredicate;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReason;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonHome;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonXInfo;
import com.trilogy.app.crm.dunning.DunningConstants;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCode;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCodeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionBundleBalance;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionState;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.ProfileTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.ReadOnlySubscriptionBundle;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.exception.CRMExceptionFactory;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.DetailedCallDetailQueryResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.CreateBucketHistoryRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BucketHistoryCreateResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BucketHistoryQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.DetailedBucketHistoryQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.MergedBalanceHistory;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionBundleBalanceSummary;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContractStatus;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContractStatusQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionSecondaryBalanceQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionSecondaryBalanceReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateFees;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionStateTransitionException;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionStateTransitionResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.MergedBalanceHistoryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateCriteria;
import com.trilogy.util.snippet.log.Logger;
import electric.util.holder.longInOut;

/**
 * 
 * @author Marcio Marques
 * @since 9.2
 *
 */
public class SubscriptionQueryExecutors {

	
	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.1.3
	 *
	 */
	public static abstract class AbstractSubscriptionExecuteSetQueryExecutor extends ExecuteResultQueryExecutor
	{

	    protected Subscriber getSubscription(Context ctx, SubscriptionReference subscriptionRef) throws CRMExceptionFault
	    {
	        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
	        return subscriber;
	    }
	    
	    protected SubscriptionReference getSubscriptionReference(Context ctx, int paramSubscriptionReference, String paramSubscriptionReferenceName, Object... parameters) throws CRMExceptionFault
	    {
	        SubscriptionReference result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            GenericParametersAdapter<SubscriptionReference> adapter = new GenericParametersAdapter<SubscriptionReference>(SubscriptionReference.class, paramSubscriptionReferenceName);
	            try
	            {
	                result = (SubscriptionReference) adapter.unAdapt(ctx, parameters);
	            }
	            catch (HomeException e)
	            {
	                RmiApiErrorHandlingSupport.generalException(ctx, e,
	                        "Unable to extract argument '" + paramSubscriptionReferenceName + "' from generic parameters: " + e.getMessage(), this);
	            }
	        }
	        else
	        {
	            result = (SubscriptionReference) parameters[paramSubscriptionReference];
	        }
	        return result;
	    }

	    
	}
	
	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.1.3
	 *
	 */
	public static abstract class AbstractSubscriptionQueryExecutor<T> extends AbstractQueryExecutor<T>
	{

	    protected Subscriber getSubscription(Context ctx, SubscriptionReference subscriptionRef) throws CRMExceptionFault
	    {
	        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
	        return subscriber;
	    }
	    
	    protected Home getSubscriberHome(final Context ctx) throws CRMExceptionFault
	    {
	        return RmiApiSupport.getCrmHome(ctx, SubscriberHome.class, SubscribersImpl.class);
	    }
	    
	    protected SubscriptionReference getSubscriptionReference(Context ctx, int paramSubscriptionReference, String paramSubscriptionReferenceName, Object... parameters) throws CRMExceptionFault
	    {
	    	return getParameter(ctx, paramSubscriptionReference, paramSubscriptionReferenceName, SubscriptionReference.class, parameters);
	    }
	    
	    
	}
	
	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.2
	 *
	 */
	public static class BucketHistoryQueryExecutor extends AbstractSubscriptionQueryExecutor<DetailedBucketHistoryQueryResult>
	{
	    public BucketHistoryQueryExecutor()
	    {
	    }
	    
	    public DetailedBucketHistoryQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Long buckedHistoryID = getParameter(ctx, PARAM_BUCKET_HISTORY_ID, PARAM_BUCKET_HISTORY_ID_NAME, Long.class, parameters);
	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        
	    	DetailedBucketHistoryQueryResult result = BucketHistorySupport.createDetailedBucketHistoryQueryResult("");
	    	
	    	try
	    	{
		    	BucketHistory bucketHistory = HomeSupportHelper.get(ctx).findBean(ctx, BucketHistory.class, new EQ(BucketHistoryXInfo.IDENTIFIER, buckedHistoryID));
	    		BucketHistorySupport.addBucketHistoryToDetailedResult(result, bucketHistory);
	    	}
	    	catch (HomeException e)
	    	{
	    		RmiApiErrorHandlingSupport.generalException(ctx, e, "Unable to retrieve bucket history", this);
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
	            result[1] = getParameter(ctx, PARAM_BUCKET_HISTORY_ID, PARAM_BUCKET_HISTORY_ID_NAME, Long.class, parameters);
	            result[2] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }
	        
	        return result;
	    }
	    @Override
	    public boolean validateParameterTypes(Class[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=3);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && long.class.isAssignableFrom(parameterTypes[PARAM_BUCKET_HISTORY_ID]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return DetailedBucketHistoryQueryResult.class.isAssignableFrom(resultType);
        }

	    
	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_BUCKET_HISTORY_ID = 1;
	    public static final int PARAM_GENERIC_PARAMETERS = 2;
	    
	    public static final String PARAM_BUCKET_HISTORY_ID_NAME = "bucketHistoryID";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";

	}

	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.2
	 *
	 */
	public static class SubscriptionBucketHistoryListQueryExecutor extends AbstractSubscriptionQueryExecutor<BucketHistoryQueryResult>
	{
	    public SubscriptionBucketHistoryListQueryExecutor()
	    {
	    }
	    
	    public BucketHistoryQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        SubscriptionReference subscriptionRef = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
            Calendar startTime = getParameter(ctx, PARAM_START_TIME, PARAM_START_TIME_NAME, Calendar.class, parameters);
            Calendar endTime = getParameter(ctx, PARAM_END_TIME, PARAM_END_TIME_NAME, Calendar.class, parameters);
            Long category = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
            String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
            int limit = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, Integer.class, parameters);
            boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        
	        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, PARAM_SUBSCRIPTION_REFERENCE_NAME);

	        Subscriber subscription = getSubscription(ctx, subscriptionRef);

	    	BucketHistoryQueryResult result = BucketHistorySupport.createBucketHistoryQueryResult(pageKey);
	    	
	    	Collection<BucketHistory> bucketHistoryCollection = BucketHistorySupport.getBucketHistoryCollection(ctx, subscription.getId(), startTime, endTime, category, pageKey, limit, isAscending);
	    	
	    	for (BucketHistory history : bucketHistoryCollection) {
	    		
	    		BucketHistorySupport.addBucketHistoryReferenceToResult(result, history);
	    	}
	    	
	    	   	
	    	return result;
	    }

	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[9];
	            result[0] = parameters[0];
	            result[1] = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
	            result[2] = getParameter(ctx, PARAM_START_TIME, PARAM_START_TIME_NAME, Calendar.class, parameters);
	            result[3] = getParameter(ctx, PARAM_END_TIME, PARAM_END_TIME_NAME, Calendar.class, parameters);
	            result[4] = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
	            result[5] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
	            result[6] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, Integer.class, parameters);
	            result[7] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
	            result[8] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }
	        
	        return result;
	    }
	    @Override
	    public boolean validateParameterTypes(Class[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=9);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
	        result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START_TIME]);
	        result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END_TIME]);
	        result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_CATEGORY]);
	        result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
	        result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
	        result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return BucketHistoryQueryResult.class.isAssignableFrom(resultType);
        }

	    
	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
	    public static final int PARAM_START_TIME = 2;
	    public static final int PARAM_END_TIME = 3;
	    public static final int PARAM_CATEGORY = 4;
	    public static final int PARAM_PAGE_KEY = 5;
	    public static final int PARAM_LIMIT = 6;
	    public static final int PARAM_IS_ASCENDING = 7;
	    public static final int PARAM_GENERIC_PARAMETERS = 8;
	    
	    public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
	    public static final String PARAM_START_TIME_NAME = "startTime";
	    public static final String PARAM_END_TIME_NAME = "endTime";
	    public static final String PARAM_CATEGORY_NAME = "category";
	    public static final String PARAM_PAGE_KEY_NAME = "pageKey";
	    public static final String PARAM_LIMIT_NAME = "limit";
	    public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}
	
	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.2
	 *
	 */	public static class SubscriptionBucketHistoryDetailedListQueryExecutor extends AbstractSubscriptionQueryExecutor<DetailedBucketHistoryQueryResult>
	{
	    public SubscriptionBucketHistoryDetailedListQueryExecutor()
	    {
	    }
	    
	    public DetailedBucketHistoryQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        SubscriptionReference subscriptionRef = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
            Calendar startTime = getParameter(ctx, PARAM_START_TIME, PARAM_START_TIME_NAME, Calendar.class, parameters);
            Calendar endTime = getParameter(ctx, PARAM_END_TIME, PARAM_END_TIME_NAME, Calendar.class, parameters);
            Long category = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
            String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
            int limit = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, Integer.class, parameters);
            boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        
	        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, PARAM_SUBSCRIPTION_REFERENCE_NAME);

	        Subscriber subscription = getSubscription(ctx, subscriptionRef);

	    	DetailedBucketHistoryQueryResult result = BucketHistorySupport.createDetailedBucketHistoryQueryResult(pageKey);

	    	Collection<BucketHistory> bucketHistoryCollection = BucketHistorySupport.getBucketHistoryCollection(ctx, subscription.getId(), startTime, endTime, category, pageKey, limit, isAscending);
	    	
	    	for (BucketHistory history : bucketHistoryCollection) {
	    		
	    		BucketHistorySupport.addBucketHistoryToDetailedResult(result, history);
	    	}    	
	    	   	
	    	return result;
	    }

	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[9];
	            result[0] = parameters[0];
	            result[1] = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
	            result[2] = getParameter(ctx, PARAM_START_TIME, PARAM_START_TIME_NAME, Calendar.class, parameters);
	            result[3] = getParameter(ctx, PARAM_END_TIME, PARAM_END_TIME_NAME, Calendar.class, parameters);
	            result[4] = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
	            result[5] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
	            result[6] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, Integer.class, parameters);
	            result[7] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
	            result[8] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }
	        
	        return result;
	    }
	    @Override
	    public boolean validateParameterTypes(Class[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=9);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
	        result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START_TIME]);
	        result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END_TIME]);
	        result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_CATEGORY]);
	        result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
	        result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
	        result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return DetailedBucketHistoryQueryResult.class.isAssignableFrom(resultType);
        }

	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
	    public static final int PARAM_START_TIME = 2;
	    public static final int PARAM_END_TIME = 3;
	    public static final int PARAM_CATEGORY = 4;
	    public static final int PARAM_PAGE_KEY = 5;
	    public static final int PARAM_LIMIT = 6;
	    public static final int PARAM_IS_ASCENDING = 7;
	    public static final int PARAM_GENERIC_PARAMETERS = 8;
	    
	    public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
	    public static final String PARAM_START_TIME_NAME = "startTime";
	    public static final String PARAM_END_TIME_NAME = "endTime";
	    public static final String PARAM_CATEGORY_NAME = "category";
	    public static final String PARAM_PAGE_KEY_NAME = "pageKey";
	    public static final String PARAM_LIMIT_NAME = "limit";
	    public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}

	/**
	 * 
	 * @author abhurke
	 * @since 9.2
	 *
	 */
	public static class SubscriptionBucketHistoryCreationQueryExecutor extends AbstractSubscriptionQueryExecutor<BucketHistoryCreateResult>
	{
		
	    public SubscriptionBucketHistoryCreationQueryExecutor()
	    {
	    }
	    
	    public BucketHistoryCreateResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        SubscriptionReference subscriptionRef = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
	        CreateBucketHistoryRequest createBucketHistoryRequest = getParameter(ctx, PARAM_CREATE_BUCKET_HISTORY_REQUEST, PARAM_CREATE_BUCKET_HISTORY_REQUEST_NAME, CreateBucketHistoryRequest.class, parameters);
	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        
	        GenericParameterParser parser = new GenericParameterParser(genericParameters);
	        
	        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, PARAM_SUBSCRIPTION_REFERENCE_NAME);
	        RmiApiErrorHandlingSupport.validateMandatoryObject(createBucketHistoryRequest, PARAM_CREATE_BUCKET_HISTORY_REQUEST_NAME);
	        
	        Subscriber subscription = getSubscription(ctx, subscriptionRef);
	        
	        BucketHistory bucketHistory = new BucketHistory();
	        bucketHistory.setAmount(createBucketHistoryRequest.getAdjustmentAmount());
	        bucketHistory.setBalance(createBucketHistoryRequest.getBalance());
	        bucketHistory.setBan(subscription.getBAN());
	        bucketHistory.setBucketID(createBucketHistoryRequest.getBucketID());
	        bucketHistory.setBundleID(createBucketHistoryRequest.getBundleID());
	        bucketHistory.setCategoryID(createBucketHistoryRequest.getCategoryID());
	        bucketHistory.setAdjustmentDate(createBucketHistoryRequest.getEventDate());
	        bucketHistory.setExpiryDate(createBucketHistoryRequest.getExpiryDate());
	        bucketHistory.setExpiryExtension(createBucketHistoryRequest.getExpiryExtension());
	        bucketHistory.setExternalReference(createBucketHistoryRequest.getExternalReferenceID());
	        bucketHistory.setSpid(createBucketHistoryRequest.getSpid());
	        bucketHistory.setSubscriptionID(subscription.getId());
	        
        	// Workaround for kilobyte unit, since it's not supported on API 3.0.
        	if (parser.getParameter(KILOBYTES, Boolean.class, Boolean.FALSE))
        	{
        		bucketHistory.setUnitType(UnitTypeEnum.VOLUME_KILOBYTES);
        	}
        	else if (createBucketHistoryRequest.getAmountUnit() != null) 
	        {
        		bucketHistory.setUnitType(UnitTypeEnum.get((short)createBucketHistoryRequest.getAmountUnit().getValue()));
	        }
        	
        	bucketHistory.setAgentId(parser.getParameter(AGENT_FIELD_NAME, String.class, "System"));
        	
        	// TT# 12060653029 
        	String ocgAdjustmentType = parser.getParameter(ADJUSTMENT_TYPE_FIELD_NAME, String.class);
        	
        	if (ocgAdjustmentType != null && !"".equals(ocgAdjustmentType))
            {
                AdjustmentType adjType;
				try 
				{
					adjType = OcgAdj2CrmAdjSupport.mapOcgAdj2CrmAdjType(ctx, createBucketHistoryRequest.getSpid(), ocgAdjustmentType);
				} catch (HomeException e) {
					throw new CRMExceptionFault(e);
				}
                RmiApiSupport.setOptional(bucketHistory, BucketHistoryXInfo.ADJUSTMENT_TYPE, adjType.getCode());                           	 
            }
        	
        	       	
	        /* 
	        Home bundleProfileHome = (Home)ctx.get(BundleProfileHome.class);
	        BundleProfile bundleProfile = null;
	        try
	        {
	        	bundleProfile = (BundleProfile)bundleProfileHome.find(ctx, createBucketHistoryRequest.getBundleID());
	        } 
	        catch (HomeException e) 
	        {
	        	throw new CRMExceptionFault(e);
	        }
	        
	       	bucketHistory.setAdjustmentType(bundleProfile.getAdjustmentType());
	        */
        	
	        try
	        {
	        	bucketHistory = (BucketHistory)((Home)ctx.get(BucketHistoryHome.class)).create(ctx, bucketHistory);
	        } 
	        catch (HomeException e) 
	        {
	        	throw new CRMExceptionFault(e);
	        }
	       
	        BucketHistoryCreateResult result = new BucketHistoryCreateResult();
	        result.setIdentifier(bucketHistory.getIdentifier());
	        result.setStatus(true);
	        
	        if(LogSupport.isDebugEnabled(ctx))
        	{
        		LogSupport.debug(ctx, this, "Bucket History created = "+result);
        		
        	}
	        
	        return result;
	    }
	    
	    
	    
	    
	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[5];
	            result[0] = parameters[0];
	            result[1] = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);            
	            result[2] = getParameter(ctx, PARAM_CREATE_BUCKET_HISTORY_REQUEST, PARAM_CREATE_BUCKET_HISTORY_REQUEST_NAME, CreateBucketHistoryRequest.class, parameters);
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
	        result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
	        result = result && CreateBucketHistoryRequest.class.isAssignableFrom(parameterTypes[PARAM_CREATE_BUCKET_HISTORY_REQUEST]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return BucketHistoryCreateResult.class.isAssignableFrom(resultType);
        }


	    private static final String KILOBYTES = "Kilobytes";
	    private static final String AGENT_FIELD_NAME = "AGENT_ID";
	    private static final String ADJUSTMENT_TYPE_FIELD_NAME = "ADJUSTMENT_TYPE";

	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
	    public static final int PARAM_CREATE_BUCKET_HISTORY_REQUEST = 2;
	    public static final int PARAM_GENERIC_PARAMETERS = 3;
	    
	    public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
	    public static final String PARAM_CREATE_BUCKET_HISTORY_REQUEST_NAME = "createBucketHistoryRequest";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}

	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.1.3
	 *
	 */
	public static class SubscriptionBalanceQueryExecutor extends AbstractSubscriptionExecuteSetQueryExecutor
	{

	    public SubscriptionBalanceQueryExecutor()
	    {
	    }
	    
	    public ExecuteResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        SubscriptionReference subscriptionRef = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
	        String[] balanceTypesArray = getParameter(ctx, PARAM_BALANCE_TYPES, PARAM_BALANCE_TYPES_NAME, String[].class, parameters);
	        GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        
	        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, PARAM_SUBSCRIPTION_REFERENCE_NAME);
	        RmiApiErrorHandlingSupport.validateMandatoryObject(balanceTypesArray, PARAM_BALANCE_TYPES_NAME);

	        Subscriber subscription = getSubscription(ctx, subscriptionRef);

	        Set<String> balanceTypes = extractBalanceTypes(balanceTypesArray);
	        BalanceUsage balanceUsage = null;
	        boolean isPoolMember = subscription.isPooledMemberSubscriber(ctx);
	        
	        ExecuteResult result = new ExecuteResult();
	        
	        for (String balanceType : BALANCE_TYPES)
	        {
	            if (isBalanceTypeRequired(balanceTypes, balanceType))
	            {
	                if (balanceUsage == null && isBalanceUsageRequired(balanceType))
	                {
	                    balanceUsage = subscription.getBalanceUsage(ctx);
	                }
	                
	                Object value = getBalanceTypeValue(ctx, balanceType, subscription, balanceUsage, isPoolMember);
	                addParameterToResult(result, balanceType, value);
	            }
	        }
	        
	        return result;
	    }
	    
	    private Object getBalanceTypeValue(Context ctx, String balanceType, Subscriber subscription, BalanceUsage balanceUsage, boolean isPoolMember)
	    {
	        if (AMOUNT.equals(balanceType))
	        {
	            if (subscription.isPostpaid())
	            {
	                if (balanceUsage!=null)
	                {
	                    return balanceUsage.getAmountOwing();
	                }
	                else
	                {
	                    return subscription.getAmountOwing(ctx);
	                }
	            }
	            else if (subscription.isPrepaid())
	            {
	                if (balanceUsage!=null)
	                {
	                    return balanceUsage.getBalanceRemaining();
	                }
	                else
	                {
	                    return subscription.getBalanceRemaining(ctx);
	                }
	            }
	        }
	        else if (PRIMARY_MOBILE_NUMBER.equals(balanceType))
	        {
	            return subscription.getMSISDN();
	            
	        }
	        else if (CURRENCY.equals(balanceType))
	        {
	            return subscription.getCurrency(ctx);
	        }
	        else if (BLOCKED_BALANCE.equals(balanceType))
	        {
	            if (balanceUsage!=null)
	            {
	                return balanceUsage.getBlockedBalance();
	            }
	            else
	            {
	                return subscription.getBlockedBalance(ctx);
	            }
	        }
	        else if (POOL_QUOTA.equals(balanceType))
	        {
	            if (isPoolMember)
	            {
	                return balanceUsage.getGroupUsageQuota();
	            }
	        }
	        else if (POOL_USAGE.equals(balanceType))
	        {
	            if (isPoolMember)
	            {
	                return balanceUsage.getGroupUsage();
	            }
	        }
	        else if (CREDIT_LIMIT.equals(balanceType))
	        {
	            if (subscription.isPostpaid())
	            {
	                return balanceUsage.getCreditLimit();
	            }
	        }
	        else if (REALTIME_BALANCE.equals(balanceType))
	        {
	            if (subscription.isPostpaid())
	            {
	                return balanceUsage.getRealTimeBalance();
	            }
	        }
	        else if (LAST_INVOICE_AMOUNT.equals(balanceType))
	        {
	            if (subscription.isPostpaid())
	            {
	                if (balanceUsage!=null)
	                {
	                    return balanceUsage.getLastInvoiceAmount();
	                }
	                else
	                {
	                    return subscription.getLastInvoiceAmount(ctx);
	                }
	                
	            }
	        }
	        else if (PAYMENTS_SINCE_LAST_INVOICE.equals(balanceType))
	        {
	            if (subscription.isPostpaid())
	            {
	                if (balanceUsage!=null)
	                {
	                    return balanceUsage.getPaymentSinceLastInvoice();
	                }
	                else
	                {
	                    return subscription.getPaymentSinceLastInvoice(ctx);
	                }
	            }
	        }
	        else if (ADJUSTMENTS_SINCE_LAST_INVOICE.equals(balanceType))
	        {
	            if (subscription.isPostpaid())
	            {
	                if (balanceUsage!=null)
	                {
	                    return balanceUsage.getAdjustmentsSinceLastInvoice();
	                }
	                else
	                {
	                    return subscription.getAdjustmentsSinceLastInvoice(ctx);
	                }
	            }
	        }
	        else if (WRITTEN_OFF_BALANCE.equals(balanceType))
	        {
	            if (subscription.isPostpaid())
	            {
	                /*
	                 * TODO: support write-off balance
	                 */
	                // return balanceUsage.getWriteOffBalance();
	            }
	        }
	        else if (MONTHLY_SPEND_AMOUNT.equals(balanceType))
	        {
	            if (subscription.isPostpaid())
	            {
	                return balanceUsage.getMonthlySpendAmount();
	            }
	        }
	        else if (MONTHLY_SPEND_LIMIT.equals(balanceType))
	        {
	            if (subscription.isPostpaid())
	            {
	                return subscription.getMonthlySpendLimit();
	            }
	        }
	        else if (EXPIRY_DATE.equals(balanceType))
	        {
	            if (subscription.isPrepaid())
	            {
	                return subscription.getExpiryDate();
	            }
	        }
	        else if (OVERDRAFT_BALANCE.equals(balanceType))
	        {
	            if (subscription.isPrepaid())
	            {
	                if (balanceUsage!=null)
	                {
	                    return balanceUsage.getOverdraftBalance();
	                }
	                else
	                {
	                    return subscription.getOverdraftBalance(ctx);
	                }
	            }
	        }
	        else if (OVERDRAFT_DATE.equals(balanceType))
	        {
	            if (subscription.isPrepaid())
	            {
	                if (balanceUsage!=null)
	                {
	                    return new Date(balanceUsage.getOverdraftDate());
	                }
	                else
	                {
	                    return new Date(subscription.getOverdraftDate(ctx));
	                }
	            }
	        }
	        
	        return null;
	    }
	    
	    private boolean isBalanceUsageRequired(String balanceType)
	    {
	        return BALANCE_USAGE_REQUIRED_BALANCE_TYPES.contains(balanceType);
	               
	    }
	    
	    private Set<String> extractBalanceTypes(String[] balanceTypes)
	    {
	        Set<String> result = new HashSet<String>();
	        for (String balanceType : balanceTypes)
	        {
	            result.add(balanceType.toUpperCase());
	        }
	        return result;
	    }
	    
	    private boolean isBalanceTypeRequired(Set<String> balanceTypes, String balanceType)
	    {
	        boolean result = false;
	        
	        if (balanceTypes.contains(balanceType.toUpperCase()))
	        {
	            result = true;
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
	            result[1] = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
	            result[2] = getParameter(ctx, PARAM_BALANCE_TYPES, PARAM_BALANCE_TYPES_NAME, String[].class, parameters);
	            result[3] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }
	        
	        return result;
	    }
	    @Override
	    public boolean validateParameterTypes(Class[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=4);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
	        result = result && String[].class.isAssignableFrom(parameterTypes[PARAM_BALANCE_TYPES]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }

        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return ExecuteResult.class.isAssignableFrom(resultType);
        }


	    private static final String AMOUNT = "AMOUNT";
	    private static final String PRIMARY_MOBILE_NUMBER = "PRIMARY_MOBILE_NUMBER";
	    private static final String CURRENCY = "CURRENCY";
	    private static final String BLOCKED_BALANCE = "BLOCKED_BALANCE";
	    private static final String POOL_QUOTA = "POOL_QUOTA";
	    private static final String POOL_USAGE = "POOL_USAGE";
	    private static final String CREDIT_LIMIT = "CREDIT_LIMIT";
	    private static final String REALTIME_BALANCE = "REALTIME_BALANCE";
	    private static final String LAST_INVOICE_AMOUNT = "LAST_INVOICE_AMOUNT";
	    private static final String PAYMENTS_SINCE_LAST_INVOICE = "PAYMENTS_SINCE_LAST_INVOICE";
	    private static final String ADJUSTMENTS_SINCE_LAST_INVOICE = "ADJUSTMENTS_SINCE_LAST_INVOICE";
	    private static final String WRITTEN_OFF_BALANCE = "WRITTEN_OFF_BALANCE";
	    private static final String MONTHLY_SPEND_LIMIT = "MONTHLY_SPEND_LIMIT";
	    private static final String MONTHLY_SPEND_AMOUNT = "MONTHLY_SPEND_AMOUNT";
	    private static final String EXPIRY_DATE = "EXPIRY_DATE";
	    private static final String OVERDRAFT_BALANCE = "OVERDRAFT_BALANCE";
	    private static final String OVERDRAFT_DATE = "OVERDRAFT_DATE";
	    
	    private final String[] BALANCE_TYPES = new String[]
	    {
	        POOL_QUOTA,
	        POOL_USAGE,
	        CREDIT_LIMIT,
	        REALTIME_BALANCE,
	        MONTHLY_SPEND_AMOUNT,
	        AMOUNT, 
	        WRITTEN_OFF_BALANCE,
	        PRIMARY_MOBILE_NUMBER, 
	        CURRENCY,
	        LAST_INVOICE_AMOUNT,
	        PAYMENTS_SINCE_LAST_INVOICE,
	        ADJUSTMENTS_SINCE_LAST_INVOICE,
	        MONTHLY_SPEND_LIMIT,
	        EXPIRY_DATE,
	        BLOCKED_BALANCE,
	        OVERDRAFT_BALANCE,
	        OVERDRAFT_DATE
	   };

	    private final Set<String> BALANCE_USAGE_REQUIRED_BALANCE_TYPES = new HashSet<String>(Arrays.asList(new String[] {
	            POOL_QUOTA,
	            POOL_USAGE,
	            CREDIT_LIMIT,
	            REALTIME_BALANCE,
	            MONTHLY_SPEND_AMOUNT
	            }));
	        
	    public static final String METHOD_SIMPLE_NAME = "executeSubscriptionBalanceQuery";
	    public static final String METHOD_NAME = "SubscriptionService." + METHOD_SIMPLE_NAME;

	    public static final String PERMISSION = Constants.PERMISSION_SUBSCRIBERS_READ_EXECUTESUBSCRIPTIONBALANCEQUERY;
	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
	    public static final int PARAM_BALANCE_TYPES = 2;
	    public static final int PARAM_GENERIC_PARAMETERS = 3;
	    
	    public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
	    public static final String PARAM_BALANCE_TYPES_NAME = "balanceTypes";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}

	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.1.3
	 *
	 */
	public static class SubscriptionStateUpdateQueryExecutor extends AbstractQueryExecutor<SuccessCode> 
	{
	    public SubscriptionStateUpdateQueryExecutor()
	    {
	        delegate_  = new SubscriptionUpdateWithStateTransitionQueryExecutor();
	    }

	    @Override
	    public SuccessCode execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        try
	        {
	            if (isGenericExecution(ctx, parameters))
	            {
	                delegate_.execute(ctx, parameters);
	            }
	            else
	            {
	                RmiApiErrorHandlingSupport.validateMandatoryObject(parameters[PARAM_SUBSCRIPTION_REFERENCE], PARAM_SUBSCRIPTION_REFERENCE_NAME);
	                RmiApiErrorHandlingSupport.validateMandatoryObject(parameters[PARAM_NEW_STATE], PARAM_NEW_STATE_NAME);
	    
	                Object[] newParameters = new Object[5];
	                newParameters[SubscriptionUpdateWithStateTransitionQueryExecutor.PARAM_HEADER] = parameters[PARAM_HEADER];
	                newParameters[SubscriptionUpdateWithStateTransitionQueryExecutor.PARAM_SUBSCRIPTION_REFERENCE] = parameters[PARAM_SUBSCRIPTION_REFERENCE];
	                newParameters[SubscriptionUpdateWithStateTransitionQueryExecutor.PARAM_CURRENT_STATES] = null;
	                newParameters[SubscriptionUpdateWithStateTransitionQueryExecutor.PARAM_NEW_STATE] = parameters[PARAM_NEW_STATE];
	                newParameters[SubscriptionUpdateWithStateTransitionQueryExecutor.PARAM_GENERIC_PARAMETERS] = parameters[PARAM_GENERIC_PARAMETERS];
	    
	                delegate_.execute(ctx, newParameters);
	            }
	        }
	        catch (CRMExceptionFault e)
	        {
	            if (e.getFaultMessage().getCRMException() instanceof SubscriptionStateTransitionException)
	            {
	                RmiApiErrorHandlingSupport.generalException(ctx, (Exception) e.getCause(), e.getFaultMessage().getCRMException().getMessage(), e.getFaultMessage().getCRMException().getCode(), this);
	            }
	            else
	            {
	                throw e;
	            }
	        }
	        
	        return SuccessCodeEnum.SUCCESS.getValue();
	    }

	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            Object[] delegateParameters = delegate_.getParameters(ctx, parameters);
	            result = new Object[4];
	            result[PARAM_HEADER] = delegateParameters[SubscriptionUpdateWithStateTransitionQueryExecutor.PARAM_HEADER];
	            result[PARAM_SUBSCRIPTION_REFERENCE] = delegateParameters[SubscriptionUpdateWithStateTransitionQueryExecutor.PARAM_SUBSCRIPTION_REFERENCE];
	            result[PARAM_NEW_STATE] = delegateParameters[SubscriptionUpdateWithStateTransitionQueryExecutor.PARAM_NEW_STATE];
	            result[PARAM_GENERIC_PARAMETERS] = delegateParameters[SubscriptionUpdateWithStateTransitionQueryExecutor.PARAM_GENERIC_PARAMETERS];
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
	        result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
	        result = result && SubscriptionState.class.isAssignableFrom(parameterTypes[PARAM_NEW_STATE]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return SuccessCode.class.isAssignableFrom(resultType);
        }

	    
	    private SubscriptionUpdateWithStateTransitionQueryExecutor delegate_;

	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
	    public static final int PARAM_NEW_STATE = 2;
	    public static final int PARAM_GENERIC_PARAMETERS = 3;
	    
	    public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
	    public static final String PARAM_NEW_STATE_NAME = "newState";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";

	}
	
	/**
	 * 
	 * @author Marcio Marques
	 * @since 9.1.3
	 *
	 */
	public static class SubscriptionUpdateWithStateTransitionQueryExecutor extends AbstractSubscriptionQueryExecutor<SubscriptionStateTransitionResult>
	{
		public static String PORTOUT_AND_INACTIVE_STATE = "PortOutAndInactiveState";
	    public SubscriptionUpdateWithStateTransitionQueryExecutor()
	    {
	    }
	    
	    public SubscriptionStateTransitionResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        SubscriptionReference subscriptionRef = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
	        SubscriptionState[] currentStates =  getParameter(ctx, PARAM_CURRENT_STATES, PARAM_CURRENT_STATES_NAME, SubscriptionState[].class, parameters);
	        SubscriptionState newState = getParameter(ctx, PARAM_NEW_STATE, PARAM_NEW_STATE_NAME, SubscriptionState.class, parameters);
	        
	        GenericParameterParser parser = null;
	        if (parameters!=null)
	        {
	            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
	            parser = new GenericParameterParser(genericParameters);
	        }
	        
	            
	        if (LogSupport.isDebugEnabled(ctx))
	        {
	            StringBuilder msg = new StringBuilder();
	            msg.append("Subscription State Transition request parameters -> ");
	            msg.append(" subscriptionRef: ");
	            msg.append(subscriptionRef);
	            msg.append(", currentStates: ");
	            msg.append(currentStates);
	            msg.append(", newState: ");
	            msg.append(newState);
	            msg.append(", genericParameters: ");
	            msg.append(Arrays.toString(parameters));
	            LogSupport.debug(ctx, this, msg.toString());
	        }
	        
	        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, PARAM_SUBSCRIPTION_REFERENCE_NAME);
	        RmiApiErrorHandlingSupport.validateMandatoryObject(newState, PARAM_NEW_STATE_NAME);

	        Subscriber subscription = getSubscription(ctx, subscriptionRef);
	        
	        final SubscriptionState oldState = RmiApiSupport.convertCrmSubscriberState2Api(subscription.getState());
	        
	        final Home home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, getSubscriberHome(ctx), getMethodSimpleName(ctx));

	        final SubscriberStateEnum crmNewState = RmiApiSupport.convertApiSubscriberState2Crm(newState);
	        final SubscriberStateEnum[] crmCurrentStates = RmiApiSupport.convertApiSubscriberState2Crm(currentStates);

	        validateCurrentState(ctx, subscription, crmNewState, crmCurrentStates);
	        
	        Subscriber resultSubscription = null;
	        
	        if (SubscriberStateEnum.ACTIVE.equals(subscription.getState()) && SubscriberStateEnum.AVAILABLE.equals(crmNewState))
	        {
	            MoveRequest request = null;
	            try
	            {
	                request = MoveRequestSupport.getMoveRequest(ctx, subscription, ReverseActivationMoveRequest.class);
	                if (request instanceof ReverseActivationMoveRequest)
	                {
	                    Context subCtx = ctx.createSubContext();
	                    new MoveManager().move(subCtx, request);
	                    resultSubscription = ((ReverseActivationMoveRequest) request).getNewSubscription(subCtx);
	                }
	                else
	                {
	                    StringBuilder msg = new StringBuilder();
	                    msg.append("Expected ");
	                    msg.append(ReverseActivationMoveRequest.class.getSimpleName());
	                    msg.append(", but got a ");
	                    msg.append(request.getClass().getSimpleName());
	                    msg.append(".");
	                    if (LogSupport.isDebugEnabled(ctx))
	                    {
	                        LogSupport.debug(ctx, this, msg.toString()+ " MoveRequest -> " + request);
	                    }
	                    throw new IllegalStateException(msg.toString());
	                }
	            }
	            catch (final Exception e)
	            {
	                final String msg = "Failed to move subscription " + subscription.getId();
	                Subscriber currentSubscription = getSubscription(ctx, getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters));
	                RmiApiErrorHandlingSupport.subscriptionStateTransitionException(ctx, e, msg, oldState, newState,
	                        RmiApiSupport.convertCrmSubscriberState2Api(currentSubscription.getState()), this);
	            }
	        }
	        else
	        {
	            validateStateTransition(ctx, subscription, crmNewState);
	            boolean portOut = Boolean.TRUE.equals(parser.getParameter(APIGenericParameterSupport.PORT_OUT_FLAG,Boolean.class));
	            boolean isPortOutandInactiveState = portOut && (SubscriberStateEnum.INACTIVE == crmNewState);
	            
	            if(isPortOutandInactiveState)
	            	ctx.put(PORTOUT_AND_INACTIVE_STATE, true);
	            
	            if(isPortOutandInactiveState && (crmNewState == subscription.getState()))
	            {
	                String msg = "Cannot port-out a Subscription that is in-active";
	                Subscriber currentSubscription = getSubscription(ctx, getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters));
	                RmiApiErrorHandlingSupport.subscriptionStateTransitionException(ctx, null, msg, oldState, newState,
                            RmiApiSupport.convertCrmSubscriberState2Api(currentSubscription.getState()), this);
	            }
	                
	            try
	            {
	            	boolean bIsStateSuspendedToActivate = false;
	                boolean bIsStatePendingToActivate = false;
	                //Sprint#7 : If subscriber is going to ACTIVE state from SUSPENDED state, set the resumed date
	                //capturing the current and previous states before setting in the new state.
	                if(subscription.getState().equals(SubscriberStateEnum.SUSPENDED) &&
	                        crmNewState.equals(SubscriberStateEnum.ACTIVE))
	                {
	                    bIsStateSuspendedToActivate = true;
	                }
	                if(subscription.getState().equals(SubscriberStateEnum.PENDING) &&
	                        crmNewState.equals(SubscriberStateEnum.ACTIVE))
	                {
	                	bIsStatePendingToActivate = true;
	                }
	            	subscription.setState(crmNewState);
	            	
	            	 //new generic parameter to catch the suspension reason code for the subscription                    
                    //if the new state of the subscriber is going to suspended, we need to set the suspension reason and the date of suspension
                    if(subscription.getState().equals(SubscriberStateEnum.SUSPENDED))
                    {
                    	if (parser.containsParam(APIGenericParameterSupport.SUBSCRIBER_SUSPENSION_REASON))
	        			{
                    		if (LogSupport.isDebugEnabled(ctx))
							{
								LogSupport.debug(ctx,this,
										"Suspension reason provided in request: "+parser.getParameter(APIGenericParameterSupport.SUBSCRIBER_SUSPENSION_REASON, String.class));
							}
                    		  
                    		int spid = subscription.getSpid();
                    		Home subSuspensionReasonHome = (Home)ctx.get(SubscriptionSuspensionReasonHome.class);;

  	        				And where = new And();
  	        				where.add(new EQ(SubscriptionSuspensionReasonXInfo.SPID, spid));
  	        				where.add(new EQ(SubscriptionSuspensionReasonXInfo.REASONCODE, parser.getParameter(APIGenericParameterSupport.SUBSCRIBER_SUSPENSION_REASON, String.class)));
  	        				if(subSuspensionReasonHome != null)
  	        				{
  	        					Collection<SubscriptionSuspensionReason> subSuspensionReasonColl = subSuspensionReasonHome.select(ctx, where);

  	        					if (subSuspensionReasonColl != null && !subSuspensionReasonColl.isEmpty())
  	        					 {
  	        						if (LogSupport.isDebugEnabled(ctx))
  	    							{
  	    								LogSupport.debug(ctx,this,
  	    										"Suspension reason matched in db");
  	    							}
  	        						subscription.setSuspensionReason(parser.getParameter(APIGenericParameterSupport.SUBSCRIBER_SUSPENSION_REASON, String.class));
  	        					 }
  	        					else
  	        					 {
  	        						if (LogSupport.isDebugEnabled(ctx))
  	    							{
  	    								LogSupport.debug(ctx,this,
  	    										"Suspension reason not matched in db");
  	    							}
  	        						throw new HomeException("Following Suspension reason does not exist : "+parser.getParameter(APIGenericParameterSupport.SUBSCRIBER_SUSPENSION_REASON, String.class));
  	        					 }
  	        					}
                    		subscription.setSuspensionReason(parser.getParameter(APIGenericParameterSupport.SUBSCRIBER_SUSPENSION_REASON, String.class));
	            			 
	        			}
                    	else
                    	{ 
                    		if (LogSupport.isDebugEnabled(ctx))
							{
								LogSupport.debug(ctx,this,
										"Suspension reason not provided in request");
							}
                    	}
                    	
	            		/*else
	            		{
	            			//Any Suspension Reason which is not thru dunning is "Other" and index '1'
	        				String subSusReasonCode = null;
	        				int spid = subscription.getSpid();
	        				Home subSuspensionReasonHome = (Home)ctx.get(SubscriptionSuspensionReasonHome.class);;
	        				
	        				And where = new And();
	        				where.add(new EQ(SubscriptionSuspensionReasonXInfo.SPID, spid));
	        				where.add(new EQ(SubscriptionSuspensionReasonXInfo.NAME, DunningConstants.DUNNING_SUSPENSION_REASON_OTHER));
	        				
	        				if(subSuspensionReasonHome != null)
	        				{
	        					Collection<SubscriptionSuspensionReason> subSuspensionReasonColl = subSuspensionReasonHome.select(ctx, where);

	        					if (subSuspensionReasonColl != null && !subSuspensionReasonColl.isEmpty())
	        					{
	        						for (Object subSuspensionReasonObj : subSuspensionReasonColl) 
	        						{
	        							if (subSuspensionReasonObj instanceof SubscriptionSuspensionReason) 
	        							{
	        								SubscriptionSuspensionReason subSusReason = (SubscriptionSuspensionReason) subSuspensionReasonObj;
	        								subSusReasonCode = subSusReason.getReasoncode();
	        								
	        								if (LogSupport.isDebugEnabled(ctx))
	        								{
	        									LogSupport.debug(ctx,this,
	        											"SubscriptionQueryExecutor: "+ subSusReason);
	        								}
	        							}
	        						}
	        					}
	        					else
	        					{
	        						LogSupport.minor(ctx, this, "The mapping for the reason code Unpaid" +
	        								" does not exists for SPID["+ subscription.getSpid() + "]");
	        					}
	        				}
	            			subscription.setSuspensionReason(subSusReasonCode); // OTHER REASON
	            		}	*/
                        subscription.setSuspensionDate(new Date());             
                    }
                    
                    //if manually suspended subscription is going to active state manually then set suspensionreason as default(null)
                    
                    if(subscription.getState().equals(SubscriberStateEnum.ACTIVE))
                    {
                    	String reason= null;
                    	subscription.setSuspensionReason(reason);
                    }
                    
                    if(subscription.getState().equals(SubscriberStateEnum.INACTIVE))
                    {
                    	 if (parser.containsParam(APIGenericParameterSupport.SUBSCRIBER_DEACTIVATION_REASON_CODE))
                    	 {
                    		 Short reasonCodeParam = parser.getParameter(APIGenericParameterSupport.SUBSCRIBER_DEACTIVATION_REASON_CODE, Short.class);
                    		 subscription.setDeactivatedReason(DeactivatedReasonEnum.get(reasonCodeParam));
                    	 }
                    	
                    }
                    
                    if(bIsStateSuspendedToActivate)
                    {
                        //set the resumed date in the subscriber database
                        subscription.setResumedDate(new Date());
                    }
                    if(bIsStatePendingToActivate)
                    {
                        //set the Start date when pending to activate.
                    	 if (parser.containsParam(APIGenericParameterSupport.ACTIVATION_DATE))
                    	 {
                    		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                         	format.setTimeZone(TimeZone.getTimeZone("UTC"));
                         	String dateString=parser.getParameter(APIGenericParameterSupport.ACTIVATION_DATE, String.class);
                            subscription.setStartDate(format.parse(dateString));
                           
                    	 }else
                    	 {
                    		 subscription.setStartDate(new Date());
                    	 }
                    }
                    
	                resultSubscription = (Subscriber) home.store(ctx, subscription);
	                
	                if(resultSubscription.getState() == SubscriberStateEnum.INACTIVE )
	                {   
	                	Msisdn msisdnBean;
	                    {
	                        msisdnBean = MsisdnSupport.getMsisdn(ctx, subscription.getMsisdn());
	                        if (null != msisdnBean && portOut)
	                        {
		                        SubscribersApiSupport.handleSubscriptionPortOut(ctx, subscription);
	                        }
	                    }
	                }
	            }
	            catch (final Exception e)
	            {
	                final String msg = "Unable to update State for subscription " + subscription.getId();
	                Subscriber currentSubscription = getSubscription(ctx, getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters));
	                RmiApiErrorHandlingSupport.subscriptionStateTransitionException(ctx, e, msg, oldState, newState,
	                        RmiApiSupport.convertCrmSubscriberState2Api(currentSubscription.getState()), this);
	            }
	        }
	        
	        if (resultSubscription != null && resultSubscription.getLastExp() != null)
	        {
	            final String msg = "Unable to update State for subscription " + subscription.getId();
	            RmiApiErrorHandlingSupport.subscriptionStateTransitionException(ctx, resultSubscription.getLastExp(), msg, oldState, newState,
	                    RmiApiSupport.convertCrmSubscriberState2Api(resultSubscription.getState()), this);
	        }

	        SubscriptionStateTransitionResult result = new SubscriptionStateTransitionResult();
	        result.setOldState(oldState);
	        
	        GenericParameter[] params = APIGenericParameterSupport.getSubscriptionStateTransitionGenericParameters(ctx, resultSubscription, oldState);

	        if (params != null )
            {
            	if(LogSupport.isDebugEnabled(ctx))
            	{
            		LogSupport.debug(ctx, this, " Setting Subscriber Activation Date in response as : "+params[0]);
            	}
            	result.setParameters(params);
            }
	        
	        return result;
	    }
	    
	    

        private void validateCurrentState(Context ctx, Subscriber subscription, SubscriberStateEnum newState, 
	            SubscriberStateEnum[] currentStates) throws CRMExceptionFault
	    {
	        if (LogSupport.isDebugEnabled(ctx))
	        {
	            LogSupport.debug(ctx, this, "validateCurrentState(...) : Start");
	        }
	        if (subscription.isInFinalState())
	        {
	            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
	                "Subscription is in a closed state. Cannot update subscriber.");
	        }
	        else if (currentStates!=null && currentStates.length > 0 
	                && !EnumStateSupportHelper.get(ctx).isOneOfStates(subscription.getState(), currentStates))
	        {
	            StringBuilder sb = new StringBuilder();
	            sb.append("Subscription current state is ");
	            sb.append(subscription.getState().getDescription());
	            sb.append(".");

	            RmiApiErrorHandlingSupport.subscriptionStateTransitionException(ctx, null, sb.toString(),
	                    RmiApiSupport.convertCrmSubscriberState2Api(subscription.getState()),
	                    RmiApiSupport.convertCrmSubscriberState2Api(newState),
	                    RmiApiSupport.convertCrmSubscriberState2Api(subscription.getState()), this);
	        }
	        if (LogSupport.isDebugEnabled(ctx))
	        {
	            LogSupport.debug(ctx, this, "validateCurrentState(...) : Finish - passed");
	        }
	    }
	    
	    
	    private void validateStateTransition(Context ctx, Subscriber subscription, SubscriberStateEnum newState) 
	            throws CRMExceptionFault
	    {
	        if (LogSupport.isDebugEnabled(ctx))
	        {
	            LogSupport.debug(ctx, this, "validateStateTransition(...) : Start");
	        }
	        if (subscription.getStateWithExpired() == newState)
	        {
	            RmiApiErrorHandlingSupport.simpleValidation("newState",
	                "Subscription is currently in the given State. Cannot change state to the same State.");
	        }
	        else if (!SubscriberStateTransitionSupport.instance(ctx, 
	                subscription).isManualStateTransitionAllowed(ctx, subscription, newState))
	        {
	            StringBuilder sb = new StringBuilder();
	            sb.append("Subscription transition from ");
	            sb.append(subscription.getStateWithExpired().getDescription());
	            sb.append(" State to ");
	            sb.append(newState.getDescription());
	            sb.append(" State is not allowed");
	            RmiApiErrorHandlingSupport.subscriptionStateTransitionException(ctx, null, sb.toString(),
	                    RmiApiSupport.convertCrmSubscriberState2Api(subscription.getState()),
	                    RmiApiSupport.convertCrmSubscriberState2Api(newState),
	                    RmiApiSupport.convertCrmSubscriberState2Api(subscription.getState()), this);
	        }
	        if (LogSupport.isDebugEnabled(ctx))
	        {
	            LogSupport.debug(ctx, this, "validateStateTransition(...) : Finish - passed");
	        }
	    }
	    
	    
	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[5];
	            result[0] = parameters[0];
	            result[1] = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
	            result[2] = getParameter(ctx, PARAM_CURRENT_STATES, PARAM_CURRENT_STATES_NAME, SubscriptionState[].class, parameters);
	            result[3] = getParameter(ctx, PARAM_NEW_STATE, PARAM_NEW_STATE_NAME, SubscriptionState.class, parameters);
	            result[4] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
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
	        result = result && (parameterTypes.length>=5);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
	        result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
	        result = result && SubscriptionState[].class.isAssignableFrom(parameterTypes[PARAM_CURRENT_STATES]);
	        result = result && SubscriptionState.class.isAssignableFrom(parameterTypes[PARAM_NEW_STATE]);
	        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return SubscriptionStateTransitionResult.class.isAssignableFrom(resultType);
        }


	    public static final int PARAM_HEADER = 0;
	    public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
	    public static final int PARAM_CURRENT_STATES = 2;
	    public static final int PARAM_NEW_STATE = 3;
	    public static final int PARAM_GENERIC_PARAMETERS = APIGenericParameterSupport.PARAM_GENERIC_PARAMETERS;
	    
	    public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
	    public static final String PARAM_CURRENT_STATES_NAME = "currentStates";
	    public static final String PARAM_NEW_STATE_NAME = "newState";
	    public static final String PARAM_GENERIC_PARAMETERS_NAME = APIGenericParameterSupport.PARAM_GENERIC_PARAMETERS_NAME;
	}

	 /**
     * 
     * @author Marcio Marques
     * @since 9.3
     *
     */
    public static class SubscriptionBundleBalancesWithSummaryQueryExecutor extends AbstractSubscriptionQueryExecutor<SubscriptionBundleBalanceSummary>
    {
        
        public SubscriptionBundleBalancesWithSummaryQueryExecutor()
        {
        }
        
        public SubscriptionBundleBalanceSummary execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            SubscriptionReference subscriptionRef = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
            Long bucketID = getParameter(ctx, PARAM_BUCKED_IDENTIFIER, PARAM_BUCKED_IDENTIFIER_NAME, Long.class, parameters);
            Long bundleID = getParameter(ctx, PARAM_BUNDLE_IDENTIFIER, PARAM_BUNDLE_IDENTIFIER_NAME, Long.class, parameters);
            Long bundleCategory = getParameter(ctx, PARAM_BUNDLE_CATEGORY, PARAM_BUNDLE_CATEGORY_NAME, Long.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            
            return getSubscriptionBundleBalancesWithSummary(ctx, subscriptionRef, bucketID, bundleID, bundleCategory, genericParameters);
        }

        public SubscriptionBundleBalanceSummary getSubscriptionBundleBalancesWithSummary(Context ctx,
                SubscriptionReference subscriptionRef, Long bucketID, Long bundleID, Long bundleCategory, GenericParameter[] parameters)
                throws CRMExceptionFault
        {
            ctx = ctx.createSubContext();
            RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, "subscriptionRef");
            SubscriptionBundleBalanceSummary ret = new SubscriptionBundleBalanceSummary();
            final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
            com.redknee.framework.xhome.msp.MSP.setBeanSpid(ctx, subscriber.getSpid());
            try
            {
                SubscriberBucketsAndBalances bucketsWithSummary = getBucketsWithCategorySummary(ctx, subscriber, bucketID, bundleID, bundleCategory, parameters);
                ret.setBundleBalances(adaptBucketsToApi(ctx, bucketsWithSummary));
                List<SubscriptionBundleBalance> sucscriptionBalances = adaptBalancesToApi(ctx, bucketsWithSummary);
                ret.setCategorySummaries(sucscriptionBalances.toArray(new SubscriptionBundleBalance[]{}));
                GenericParameter[] params = com.redknee.app.crm.api.rmi.support.APIGenericParameterSupport.getBundleProfileGenericParameters(ctx, bucketsWithSummary.getBuckets(), new GenericParameter[4], 0);
                if (params != null )
                {
                    ret.setParameters(params);
                }
            }
            catch (Exception e)
            {
                final String msg = "Error retrieving bundles for subscriber with MSISDN " + subscriber.getMSISDN() + ".";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            
            return ret;
        }

        private static List<SubscriptionBundleBalance> adaptBalancesToApi(Context ctx,
                SubscriberBucketsAndBalances bucketsWithSummary)
        {
            CollectionAdapter<Balance, SubscriptionBundleBalance, List<Balance>, List<SubscriptionBundleBalance>> adapter = new 
                CollectionAdapter<Balance, SubscriptionBundleBalance, List<Balance>, List<SubscriptionBundleBalance>>(
                        new ComponentAdapter<Balance, SubscriptionBundleBalance, List<Balance>, List<SubscriptionBundleBalance>>()
                        {
                            @Override
                            public SubscriptionBundleBalance newInstance()
                            {
                                return new SubscriptionBundleBalance();
                            }

                            @Override
                            public List<SubscriptionBundleBalance> newCollection(
                                    int length)
                            {
                                return new ArrayList<SubscriptionBundleBalance>();
                            }

                            @Override
                            public void adapt(Balance fromComponent,
                                    SubscriptionBundleBalance toComponent)
                            {
                                SubscriptionBalanceApiAdapter.adapt(fromComponent, toComponent);
                            }
                        }
                    );
            
            return adapter.adapt(bucketsWithSummary.getSummaryByCategory());
        }

        /**
         * @param ctx
         * @param bucketsWithSummary
         * @return
         * @throws HomeException
         */
        private static ReadOnlySubscriptionBundle[] adaptBucketsToApi(Context ctx,
                SubscriberBucketsAndBalances bucketsWithSummary)
                throws HomeException
        {
            return CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        bucketsWithSummary.getBuckets(), 
                        SubscriberBucketToApiAdapter.instance(), 
                        new ReadOnlySubscriptionBundle[] {});
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[6];
                result[PARAM_HEADER] = parameters[PARAM_HEADER];
                result[PARAM_SUBSCRIPTION_REFERENCE] = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);            
                result[PARAM_BUCKED_IDENTIFIER] = getParameter(ctx, PARAM_BUCKED_IDENTIFIER, PARAM_BUCKED_IDENTIFIER_NAME, Long.class, parameters);
                result[PARAM_BUNDLE_IDENTIFIER] = getParameter(ctx, PARAM_BUNDLE_IDENTIFIER, PARAM_BUNDLE_IDENTIFIER_NAME, Long.class, parameters);
                result[PARAM_BUNDLE_CATEGORY] = getParameter(ctx, PARAM_BUNDLE_CATEGORY, PARAM_BUNDLE_CATEGORY_NAME, Long.class, parameters);
                result[PARAM_GENERIC_PARAMETERS] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
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
            result = result && (parameterTypes.length>=6);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
            result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_BUCKED_IDENTIFIER]);
            result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_BUNDLE_IDENTIFIER]);
            result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_BUNDLE_CATEGORY]);
            result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return SubscriptionBundleBalanceSummary.class.isAssignableFrom(resultType);
        }

        public static SubscriberBucketsAndBalances getBucketsWithCategorySummary(Context ctx, final Subscriber subscriber, final Long bucketID,
                final Long bundleID, final Long bundleCategory, GenericParameter[] parameters) throws HomeException
        {
            ctx = ctx.createSubContext();
            try
            {
                final CRMSubscriberBucketProfile service = (CRMSubscriberBucketProfile) ctx
                        .get(CRMSubscriberBucketProfile.class);
                SubscriberBucketsAndBalances bnbSource = service.getBucketsWithCategorySummary(ctx, subscriber.getMSISDN());
                
                Collection<SubscriberBucket> bucketsSource = bnbSource.getBuckets();
                Collection<SubscriberBucket> bucketsDest = filterSubscriberBuckets(
                        ctx, bucketID, bundleID, bundleCategory, bucketsSource);
                
                List<Balance> balancesSource = bnbSource.getSummaryByCategory();
                List<Balance> balancesDest = filterSubscriberBalances(
                        ctx, bucketsDest, bucketID, bundleID, bundleCategory, balancesSource);
                
                bnbSource.setBuckets(bucketsDest);
                bnbSource.setSummaryByCategory(balancesDest);
                
                return bnbSource;
                    
            }
            catch (BundleManagerException bme)
            {
                new DebugLogMsg(SubscribersImpl.class.getName(), bme.getMessage(), bme).log(ctx);
                throw new HomeException(bme.getMessage(), bme);
            }
        }

        /**
         * 
         * @param ctx
         * @param bucketsDest (already) Filtered Buckets
         * @param bucketID
         * @param bundleID
         * @param bundleCategory
         * @param balancesSource
         * @return
         */
        private static List<Balance> filterSubscriberBalances(
                Context ctx, Collection<SubscriberBucket> bucketsDest, final Long bucketID, final Long bundleID,
                final Long bundleCategory,
                List<Balance> balancesSource)
        {
            List<Balance> balancesDest = new ArrayList<Balance>();
            CollectionsUtils.filter(ctx, balancesDest, balancesSource, new TypedPredicate<Balance>()
            {

                private static final long serialVersionUID = 1L;

                public boolean f(Context context, Balance bal) throws AbortVisitException
                {
                    /*
                     * The bundle info are already amalgamated based on category. We can only filter by Bundle-Category-Id.
                     */
                    boolean p  = bundleCategory!=null? bundleCategory.equals(Long.valueOf(bal.getApplicationId())) : true;
                    return p;
                }
            });
            return balancesDest;
        }

        /**
         * @param ctx
         * @param bucketID
         * @param bundleID
         * @param bundleCategory
         * @param bucketsSource
         * @return
         */
        private static Collection<SubscriberBucket> filterSubscriberBuckets(
                Context ctx, final Long bucketID, final Long bundleID,
                final Long bundleCategory,
                Collection<SubscriberBucket> bucketsSource)
        {
            Collection<SubscriberBucket> bucketsDest = new ArrayList<SubscriberBucket>();
            CollectionsUtils.filter(ctx, bucketsDest, bucketsSource, new TypedPredicate<SubscriberBucket>()
            {

                private static final long serialVersionUID = 1L;

                public boolean f(Context context, SubscriberBucket bucket) throws AbortVisitException
                {
                    /*
                     * Predicates: P0, P1, P2
                     */
                    boolean p0 = false;
                    /*
                     * TODO refine this predicate.
                     */
                    if (bundleCategory!=null)
                    {
                        if (!UnitTypeEnum.CURRENCY.equals(bucket.getUnitType()))
                        {
                            p0 = SafetyUtil.safeEquals(bundleCategory
                                    .longValue(), bucket.getRegularBal()
                                    .getApplicationId());
                        }
                        else
                        {
                            try
                            {
                                BundleProfile profile = BundleSupportHelper.get(
                                        context)
                                        .getBundleProfile(context, bundleID);
                                for (BundleCategoryAssociation assoc : (Collection<BundleCategoryAssociation>) profile
                                        .getBundleCategoryIds().values())
                                {
                                    if (bundleCategory.longValue() == assoc
                                            .getCategoryId())
                                    {
                                        p0 = true;
                                        break;
                                    }
                                }

                            } catch (Exception e)
                            {
                                p0 = false;
                            }
                        }
                    }
                    else
                    {
                        p0 = true;
                    }
                    boolean p1 = bucketID!=null? bucketID.equals(SubscriberBucketXInfo.BUCKET_ID.get(bucket)) : true;
                    boolean p2 = bundleID!=null? bundleID.equals(SubscriberBucketXInfo.BUNDLE_ID.get(bucket)) : true;
                    return p0 && p1 && p2;
                }
            });
            return bucketsDest;
        }

        public static final int PARAM_HEADER = 0;
        public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
        public static final int PARAM_BUCKED_IDENTIFIER = 2;
        public static final int PARAM_BUNDLE_IDENTIFIER = 3;
        public static final int PARAM_BUNDLE_CATEGORY = 4;
        public static final int PARAM_GENERIC_PARAMETERS = 5;
        
        public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
        public static final String PARAM_BUCKED_IDENTIFIER_NAME = "bucketID";
        public static final String PARAM_BUNDLE_IDENTIFIER_NAME = "bundleID";
        public static final String PARAM_BUNDLE_CATEGORY_NAME = "bundleCategory";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.2
     *
     */
    public static class SubscriptionUpdateFeesQueryExecutor extends AbstractSubscriptionQueryExecutor<SubscriptionUpdateFees>
    {
        public SubscriptionUpdateFeesQueryExecutor()
        {
        }
        
        public SubscriptionUpdateFees execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            SubscriptionUpdateCriteria[] criteria = getParameter(ctx, PARAM_CRITERIA, PARAM_CRITERIA_NAME, SubscriptionUpdateCriteria[].class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            
            PreviewUpdateFees previewUpdateFees = SubscriptionUpdateFeesFactory.getPreviewUpdatesFees(ctx, criteria,
                    genericParameters);
            return previewUpdateFees.getUpdateFees(ctx, criteria, genericParameters);
        }


        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[PARAM_HEADER] = parameters[PARAM_HEADER];
                result[PARAM_CRITERIA] = getParameter(ctx, PARAM_CRITERIA, PARAM_CRITERIA_NAME, SubscriptionUpdateCriteria[].class, parameters);
                result[PARAM_GENERIC_PARAMETERS] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }
        @Override
        public boolean validateParameterTypes(Class[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && SubscriptionUpdateCriteria[].class.isAssignableFrom(parameterTypes[PARAM_CRITERIA]);
            result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return DetailedBucketHistoryQueryResult.class.isAssignableFrom(resultType);
        }

        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_CRITERIA = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_CRITERIA_NAME = "criteria";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";

    }

    /**
     * 
     * @author Bhagyashree Dhavalshankh
     * @since 9.5.0
     *
     */
    public static class SubscriptionContractStatusQueryExecutor extends AbstractSubscriptionQueryExecutor<SubscriptionContractStatusQueryResult>
    {
        public SubscriptionContractStatusQueryExecutor()
        {
        }
        
        public SubscriptionContractStatusQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            SubscriptionReference subscriptionReference = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            
            List<SubscriptionContractStatus> contracts = SubscribersApiSupport.getSubscriptionContracts(ctx, subscriptionReference, genericParameters);
            
            SubscriptionContractStatusQueryResult result = new SubscriptionContractStatusQueryResult();
            result.setContractStatus(contracts.toArray(new SubscriptionContractStatus[]{}));
            
            return result;
        }


        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[PARAM_HEADER] = parameters[PARAM_HEADER];
                result[PARAM_SUBSCRIPTION_REFERENCE] = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
                result[PARAM_GENERIC_PARAMETERS] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }
        
        @Override
        public boolean validateParameterTypes(Class[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=2);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return SubscriptionContractStatusQueryResult.class.isAssignableFrom(resultType);
        }
        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";

    }
    
    
    /**
     * 
     * Fetch Subscription Secondary Balance for an array of category ids.
     * 
     * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
     * @since 9.6
     *
     */
    public static class SubscriptionSecondaryBalanceQueryExecutor extends AbstractSubscriptionQueryExecutor<SubscriptionSecondaryBalanceQueryResult>
    {
        public SubscriptionSecondaryBalanceQueryExecutor()
        {
        }
        
        public SubscriptionSecondaryBalanceQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            SubscriptionReference subscriptionReference = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            Integer[] categoryId = getParameter(ctx, PARAM_CATEGORY_ID, PARAM_CATEGORY_ID_NAME, Integer[].class, parameters);
            
            SubscriptionSecondaryBalanceQueryResult result = null;
            
            try
            {
            	
            	Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionReference, this);
            	
            	List<CategoryIdBalanceMapper> categoryIdBalanceMapperList = null;
            	
            	if(categoryId == null || categoryId.length == 0)
            	{
            		categoryIdBalanceMapperList = SubscribersApiSupport.getSubscriptionSecondaryBalanceList(ctx, subscriber);
            	}
            	else
            	{
            		categoryIdBalanceMapperList = SubscribersApiSupport.getSubscriptionSecondaryBalanceList(ctx, 
            					subscriber, Arrays.asList(categoryId));
            	}
            	
            	result = adaptCategoryIdBalanceMapperToSecondaryBalanceReference(categoryIdBalanceMapperList);
            }
            catch (Exception e)
            {
            	throw CRMExceptionFactory.create(e);
            }
            
            return result;
        }
        

        
        private SubscriptionSecondaryBalanceQueryResult adaptCategoryIdBalanceMapperToSecondaryBalanceReference(List<CategoryIdBalanceMapper> secondaryBalanceList)
        {
        	SubscriptionSecondaryBalanceQueryResult result = new SubscriptionSecondaryBalanceQueryResult();
        	
        	StringBuilder statusBuilder = new StringBuilder("Successfully retrieved subscription secondary balance. Could not retrieve balance for categories - ");
        	        	
        	
        	
        	for(CategoryIdBalanceMapper categoryIdBalanceMapper : secondaryBalanceList)
        	{
        		OcgTransactionException exception = categoryIdBalanceMapper.getException(); 
        		
        		if(exception == null)
        		{	
	        		SubscriptionSecondaryBalanceReference reference = new SubscriptionSecondaryBalanceReference();
	        		reference.setAmountInSecondaryBalanceBucket(categoryIdBalanceMapper.getBalance());
	        		reference.setCategoryId(categoryIdBalanceMapper.getCategoryId());
	        		result.addSubscriptionSecondaryBalanceReference(reference);
        		}
        		else
        		{
        			statusBuilder.append("categoryId[");
        			statusBuilder.append(categoryIdBalanceMapper.getCategoryId());
        			statusBuilder.append("]");
        			statusBuilder.append("error[code:");
        			statusBuilder.append(exception.getErrorCode());
        			statusBuilder.append(" -- message:");
        			statusBuilder.append(exception.getMessage());
        			statusBuilder.append("]  ");
        		}
        	}
        	
        	result.setStatus(IntValue.ZERO);
        	result.setStatusMessage(statusBuilder.toString());
        		
        	return result;
        }


        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[PARAM_HEADER] = parameters[PARAM_HEADER];
                result[PARAM_SUBSCRIPTION_REFERENCE] = getSubscriptionReference(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, parameters);
                result[PARAM_CATEGORY_ID] = getParameter(ctx, PARAM_CATEGORY_ID, PARAM_CATEGORY_ID_NAME, int[].class, parameters);
                result[PARAM_GENERIC_PARAMETERS] = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }
        
        @Override
        public boolean validateParameterTypes(Class[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=2);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
            result = result && int[].class.isAssignableFrom(parameterTypes[PARAM_CATEGORY_ID]);
            result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return SubscriptionSecondaryBalanceQueryResult.class.isAssignableFrom(resultType);
        }
        
        public static final int PARAM_HEADER = 0;
        public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
        public static final int PARAM_CATEGORY_ID = 2;
        public static final int PARAM_GENERIC_PARAMETERS = 3;
        
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
        public static final String PARAM_CATEGORY_ID_NAME = "categoryId";
    }    
    
    public static class  DetailedMergedBalanceQueryExecutor extends AbstractSubscriptionQueryExecutor<MergedBalanceHistoryResult>
    {
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
        
    	public MergedBalanceHistoryResult execute(Context ctx,
				Object... parameters) throws CRMExceptionFault 
		{
			SubscriptionReference subscriptionRef = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
			Calendar start = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
			Calendar end = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
			Calendar pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, Calendar.class, parameters);
			int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
			Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
			final MergedBalanceHistoryResult response = new MergedBalanceHistoryResult();
			RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef,PARAM_SUBSCRIPTION_REFERENCE_NAME);
			final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
			final List<BalanceHistory> result = new ArrayList<BalanceHistory>(limit);
			final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, subscriber.getSpid(), this);
            final int maxLimitSize = spid.getMaxGetLimit();
			Boolean recordLimitReached	=	false;
			if(limit <= 0 )
			{
				RmiApiErrorHandlingSupport.simpleValidation("limit", "The input limit value is smaller than or equal to 0");
			}
			else
			{
				if(limit>maxLimitSize)
				{
					limit = maxLimitSize;
				}
			}
			
			if(pageKey != null)
			{
				if(Logger.isDebugEnabled())
				{
					LogSupport.debug(ctx, this, "value of page key is--->>"+ pageKey.toString());
					LogSupport.debug(ctx, this, "here we are handling pagekey as per the sorting order");
				
				}
				if(isAscending)
				{
					start	=	pageKey;
				}
				else
				{
					end		=	pageKey;
				}
			}else{
				pageKey		=	start;
			}
			
			MergedBalanceHistory[] mergedHistoryRecords	=	new MergedBalanceHistory[]{};
			try{
				
				final ChargedBundlePicker bundlePicker = new ChargedBundlePicker(ctx, subscriber.getId(), 
                    CalendarSupportHelper.get(ctx).calendarToDate(start), CalendarSupportHelper.get(ctx).calendarToDate(end));

				Collection<com.redknee.app.crm.bean.calldetail.CallDetail> collectionCallDetails = 
	                    CallDetailsApiSupport.getCallDetailsUsingGivenParametersOrderByDateTime(
	                            ctx, 
	                            subscriber,
	                            start, 
	                            end, 
	                            null, 
	                            limit, 
	                            isAscending);
	           Collection<com.redknee.app.crm.bean.Transaction> transactionsDetails = 
	        		   TransactionsApiSupport.getTransactionsUsingGivenParametersOrderByDateTime(
	                            ctx,
	                            subscriber,
                                null, 
                                start, 
                                end, 
                                null,
                                limit, 
                                isAscending,
                                null,
                                null,
                                null);                
                final Iterator<CallDetail> cdIt = collectionCallDetails.iterator();
                final Iterator<Transaction> trIt = transactionsDetails.iterator();
                BalanceHistory cdHist = null;
                BalanceHistory trHist = null;
                for (int i = 0; i < limit && (cdIt.hasNext() || trIt.hasNext()); i++)
                {
                    if (cdHist == null && cdIt.hasNext())
                    {	
                    	CallDetail cd	=	cdIt.next();
                        cdHist = BalanceHistorySupport.convertCallDetailToHistory(ctx,cd );
                        Collection<com.redknee.app.crm.bean.ChargedBundleInfo> chargedBundles = bundlePicker.getChargedBundles(ctx, cd);
                        cdHist.setChargedBundles((ArrayList<com.redknee.app.crm.bean.ChargedBundleInfo>)chargedBundles);
                        cdHist.setRecordType(Long.parseLong("0"));
                    }

                    if (trHist == null && trIt.hasNext())
                    {
                        trHist = BalanceHistorySupport.convertTransactionToHistory(ctx, trIt.next());
                        trHist.setRecordType(Long.parseLong("1"));
                    }
                    if (cdHist == null && trHist != null)
                    {
                        result.add(trHist);
                        trHist = null;
                    }
                    else if ( trHist == null || (isAscending && trHist.getSortDate().after(cdHist.getSortDate())))
                    {
                        result.add(cdHist);
                        cdHist = null;
                    }
                    else if(trHist == null || (!isAscending && trHist.getSortDate().before(cdHist.getSortDate())) )
                    {
                    	result.add(cdHist);
                        cdHist = null;
                    }
                    else
                    {
                        result.add(trHist);
                        trHist = null;
                    }
                    if(!(cdIt.hasNext() || trIt.hasNext()) && ( i < limit-1) )
                    {
                    	if(cdHist !=null)
                    	{
                    		result.add(cdHist);
                    	}
                    	else if(trHist != null)
                    	{
                    		result.add(trHist);
                    	}
                    }
                    if((i==limit-1))
                    {
                    	recordLimitReached	=	true;
                	}
                    else
                    {
                		pageKey	=	null;
                		recordLimitReached	=	false;
                    }
                }
                if(Logger.isDebugEnabled() && result != null)
				{
                	LogSupport.debug(ctx, this, "number of record in the result--->>"+ result.size());
				}
                if (result != null && result.size() > 0)
                {
                	LogSupport.info(ctx, this, "creating PageKey for next request"+ ((BalanceHistory)result.get(result.size()-1)).getKeyDate().toString() +"1 millisecond");
                	pageKey	=	Calendar.getInstance();
                	pageKey.setTime(((BalanceHistory)result.get(result.size()-1)).getKeyDate());
                	if(isAscending)
                	{
                		pageKey.setTimeInMillis(pageKey.getTimeInMillis()+1);
                	}
                	else
                	{
                		pageKey.setTimeInMillis(pageKey.getTimeInMillis()-1);
                	}
                }
                
                mergedHistoryRecords	=	new MergedBalanceHistory[result.size()];
                int j = 0;
                for (com.redknee.app.crm.bean.BalanceHistory balanceHistory : result)
                {
                    MergedBalanceHistory apiMergedBalanceHistory = new MergedBalanceHistory();
                    BalanceHistoryToApiAdapter.adaptBalanceHistoryToAPI(ctx, balanceHistory, apiMergedBalanceHistory);
                    mergedHistoryRecords[j++] = apiMergedBalanceHistory;
                }
			}
    		catch(Exception e){
    			
    			final String msg = "Unable to retrieve all Call Details for start date " + start + ", end date " + end
    	                + ", pageKey " + pageKey + ", limit " + limit;
    			RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
    		}
			response.setResults(mergedHistoryRecords);
			response.setPageKey(pageKey);
			response.setRecordLimitReached(recordLimitReached);
			return response;
    	}
			
		

		@Override
		public boolean validateParameterTypes(Class<?>[] parameterTypes) {
			boolean result = true;
           result = result && (parameterTypes.length>=8);
           result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
           result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
           result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START]);
           result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END]);
           result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
           result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
           result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
		}

		@Override
		public boolean validateReturnType(Class<?> returnType) {
			return MergedBalanceHistoryResult.class.isAssignableFrom(returnType);
		}

		@Override
		public Object[] getParameters(Context ctx, Object... parameters)
				throws CRMExceptionFault {
			  Object[] result = null;
           if (isGenericExecution(ctx, parameters))
           {
               result = new Object[8];
               result[0] = parameters[0];
               result[1] = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
               result[2] = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
               result[3] = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
               result[4] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, Calendar.class, parameters);
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
    	
    }
}
