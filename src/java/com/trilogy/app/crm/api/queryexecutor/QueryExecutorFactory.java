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
package com.trilogy.app.crm.api.queryexecutor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.databinding.ADBBean;

import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class QueryExecutorFactory
{
    
    protected QueryExecutorFactory()
    {
        methods_ = new HashMap<String, QueryExecutor>();
    }
    
    public static QueryExecutorFactory getInstance()
    {
        if (instance_==null)
        {
            synchronized(QueryExecutorFactory.class)
            {
                instance_ = new QueryExecutorFactory();
            }
        }
        
        return instance_;
    }
    
    public <T extends Object> T execute(Context ctx, String methodKey, Class<T> expectedClass, Object... parameters) throws CRMExceptionFault
    {
        
        if (methodKey!=null && methodKey.indexOf(".") != -1)
        {
            String apiInterface = getApiInterface(methodKey);
            String methodName = getApiMethod(methodKey);
            
            return execute(ctx, apiInterface, methodName, expectedClass, parameters);
        }
        else
        {
            throw new UnsupportedOperationException("Invalid command");
        }
        
    }
    
    public <T extends Object> T execute(Context ctx, String apiInterface, String methodName, Class<T> expectedClass, Object... parameters) throws CRMExceptionFault
    {
        Object result = null;
        
        QueryExecutor executor = getMethodQueryExecutor(ctx, apiInterface, methodName);

        result = executor.execute(ctx, parameters);
        
        if (expectedClass.isAssignableFrom(result.getClass()))
        {
            return (T) result;
        }
        else if (ExecuteResult.class.isAssignableFrom(result.getClass()) && ADBBean.class.isAssignableFrom(expectedClass))
        {
            try
            {
                return (T) new ExecuteResultAdapter((Class<ADBBean>) expectedClass).unAdapt(ctx, result);
            }
            catch (HomeException t)
            {
                RmiApiErrorHandlingSupport.generalException(ctx, t, t.getMessage(), this);
            }
        }
        else if (ADBBean.class.isAssignableFrom(result.getClass()) && ExecuteResult.class.isAssignableFrom(expectedClass))
        {
            try
            {
                return (T) new ExecuteResultAdapter((Class<ExecuteResult>) expectedClass).adapt(ctx, result);
            }
            catch (HomeException t)
            {
                RmiApiErrorHandlingSupport.generalException(ctx, t, t.getMessage(), this);
            }
        }
        else
        {
            throw new UnsupportedOperationException("Conversion not supported");
        }
        
        return null;
    }
    
    public void validateAllQueryExecutor(Context ctx) throws UnsupportedOperationException, HomeException
    {
        String invalidQueryExecutors = "";
        Collection<ApiInterface> apiInterfaces = HomeSupportHelper.get(ctx).getBeans(ctx, ApiInterface.class);
        for (ApiInterface apiInterface : apiInterfaces)
        {
            Collection<ApiMethod> apiMethods = HomeSupportHelper.get(ctx).getBeans(ctx, ApiMethod.class, new EQ(ApiMethodXInfo.API_INTERFACE, apiInterface.getName()));
            for (ApiMethod apiMethod : apiMethods)
            {
                String apiInterfaceName = apiInterface.getName();
                String methodName = apiMethod.getName();
                try
                {
                    boolean result = validateQueryExecutor(ctx, apiInterfaceName, methodName);
                    if (result == false)
                    {
                        invalidQueryExecutors += "Query Executor '" + apiMethod.getFullName() + "' not valid.\n";
                    }
                }
                catch (UnsupportedOperationException ignored)
                {
                    
                }
            }
        }
        if (!invalidQueryExecutors.isEmpty())
        {
            throw new UnsupportedOperationException(invalidQueryExecutors);
        }
    }

    public boolean validateQueryExecutor(Context ctx, String apiInterfaceName, String methodName) throws UnsupportedOperationException, HomeException
    {
        boolean result = false;
        try
        {
            QueryExecutor executor = QueryExecutorFactory.getInstance().getMethodQueryExecutor(ctx, apiInterfaceName, methodName);
            if (executor!=null)
            {
                ApiInterface apiInterface = HomeSupportHelper.get(ctx).findBean(ctx, ApiInterface.class, apiInterfaceName);
                if (apiInterface!=null)
                {
                    Class clazz = Class.forName("com.redknee.util.crmapi.wsdl.v3_0.api.PinManagementServiceSkeletonInterface");
                    Method method = null;
                    for (Method m: clazz.getMethods())
                    {
                        if (m.getName().equals(methodName))
                        {
                            method = m;
                            break;
                        }
                    }
                    if (method != null)
                    {
                        result = executor.validateParameterTypes(method.getParameterTypes()) && executor.validateReturnType(method.getReturnType());
                    }
                    else
                    {
                        throw new UnsupportedOperationException("Method not supported");
                    }
                }
                else
                {
                    throw new UnsupportedOperationException("Api Interface not supported");
                }
            }
            else
            {
                throw new UnsupportedOperationException("Method not implemented");
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new UnsupportedOperationException("Wrong class defined for interface");
        }
        catch (CRMExceptionFault e)
        {
            throw new HomeException("Unable to retrieve query executor: " + e.getMessage());
        }
        return result;
    }
    
    
    private QueryExecutor getMethodQueryExecutor(Context ctx, String apiInterface, String apiMethod) throws CRMExceptionFault
    {
        String methodKey = getKey(apiInterface, apiMethod);
        QueryExecutor executor = methods_.get(methodKey);
        
        if (executor==null)
        {
            try
            {
                executor = reloadMethodQueryExecutor(ctx, apiInterface, methodKey);

                if (executor == null)
                {
                    throw new UnsupportedOperationException("Method not implemented");
                }
                else
                {
                    executor.setMethodSimpleName(ctx, apiMethod);
                    methods_.put(methodKey, executor);
                }
            }
            catch (HomeException e)
            {
                RmiApiErrorHandlingSupport.generalException(ctx, e,
                        "Unable to look up for query executor for interface '" + apiInterface + "' and method '"
                                + apiMethod + "': " + e.getMessage(), this);
            }

        }

        
        return executor;
    }
    
    private String getKey(String apiInterface, String apiMethod)
    {
        return apiInterface + SEPARATOR + apiMethod;
    }
    
    private String getApiInterface(String methodKey)
    {
        return methodKey.substring(0, methodKey.indexOf("."));
    }

    private String getApiMethod(String methodKey)
    {
        return methodKey.substring(methodKey.indexOf(".")+1);
    }

    public QueryExecutor reloadMethodQueryExecutor(Context ctx, String apiMethodKey) throws HomeException
    {
        return reloadMethodQueryExecutor(ctx, getApiInterface(apiMethodKey), apiMethodKey);
    }
    

    public QueryExecutor reloadMethodQueryExecutor(Context ctx, String apiInterfaceKey, String apiMethodKey) throws HomeException
    {
        QueryExecutor executor = null;
    
        try
        {
            executor = instantiateQueryExecutor(ctx, apiInterfaceKey, apiMethodKey);
        }
        catch (HomeException t)
        {
            throw new HomeException("Method Query Executor could not be reloaded to most recent implementation: " + t.getMessage(), t);
        }
        
        synchronized(methods_)
        {
            if (executor!=null)
            {
                executor.setMethodSimpleName(ctx, getApiMethod(apiMethodKey));
                methods_.put(apiMethodKey, executor);
            }
            else
            {
                methods_.remove(apiMethodKey);
            }
        }
        
        return executor;
    }
    
    private QueryExecutor instantiateQueryExecutor(Context ctx, String apiInterfaceKey, String methodKey) throws HomeException
    {
        ApiMethodQueryExecutor method = HomeSupportHelper.get(ctx).findBean(ctx, ApiMethodQueryExecutor.class,
                new And().add(new EQ(ApiMethodQueryExecutorXInfo.API_INTERFACE, apiInterfaceKey))
                .add(new EQ(ApiMethodQueryExecutorXInfo.API_METHOD, methodKey)));
        
        if (method!=null)
        {
            return method.getQueryExecutor();
        }
        else
        {
            return null;
        }
    }

    private Map<String, QueryExecutor> methods_;
    
    private static QueryExecutorFactory instance_;
    
    private static final String SEPARATOR = ".";
    
}
