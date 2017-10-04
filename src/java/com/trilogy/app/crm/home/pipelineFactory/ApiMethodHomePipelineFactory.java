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
package com.trilogy.app.crm.home.pipelineFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import com.trilogy.app.crm.api.queryexecutor.ApiInterface;
import com.trilogy.app.crm.api.queryexecutor.ApiInterfaceHome;
import com.trilogy.app.crm.api.queryexecutor.ApiMethod;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorHome;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorXInfo;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodTransientHome;
import com.trilogy.app.crm.home.ApiMethodIdentifierSettingHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Creates the service home decorators and put is in the context.
 * @author Marcio Marques
 * @since 9.1.1
 *
 */
public class ApiMethodHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx)
    {
        Home home = new ApiMethodTransientHome(ctx);
        home = new ApiMethodIdentifierSettingHome(ctx, home);
        home = new SortingHome(ctx, home, new Comparator() {

            @Override
            public int compare(Object o1, Object o2)
            {
                ApiMethod method1 = (ApiMethod) o1;
                ApiMethod method2 = (ApiMethod) o2;
                int interfaceCompare = method1.getApiInterface().compareTo(method2.getApiInterface());
                if (interfaceCompare==0)
                {
                    return method1.getName().compareTo(method2.getName());
                }
                else
                {
                    return interfaceCompare;
                }
            }
        });
        

        Home apiInterfaceHome = (Home) ctx.get(ApiInterfaceHome.class);
        try
        {
            
            for (ApiInterface apiInterface : (Collection<ApiInterface>) apiInterfaceHome.selectAll(ctx))
            {
                Collection<String> methodsAlreadyCreated = createApiMethods(ctx, apiInterface, false, home);
                Home queryExecutorHome = (Home) ctx.get(ApiMethodQueryExecutorHome.class);
                
                queryExecutorHome = queryExecutorHome.where(ctx, new EQ(ApiMethodQueryExecutorXInfo.API_INTERFACE, apiInterface.getName()));
                for (String methodCreated : methodsAlreadyCreated)
                {
                    queryExecutorHome = queryExecutorHome.where(ctx, new NEQ(ApiMethodQueryExecutorXInfo.API_METHOD, methodCreated));
                }
                
                for (ApiMethodQueryExecutor methodExecutor : (Collection<ApiMethodQueryExecutor>) queryExecutorHome.selectAll(ctx))
                {
                    createApiMethod(ctx, apiInterface, methodExecutor.getApiMethod(), true, home);
                }
            }
            
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to add api methods to ApiMethod transient home: " + e.getMessage(), e);
        }
        
        return home;
    }
    
    private ApiMethod createApiMethod(Context ctx, ApiInterface apiInterface, String methodName, boolean custom, Home home)
    {
        ApiMethod apiMethod = new ApiMethod();
        apiMethod.setFullName(methodName);
        apiMethod.setName(methodName.substring(methodName.indexOf(".") + 1));
        apiMethod.setApiInterface(apiInterface.getName());
        apiMethod.setApiInterfaceFullName(apiInterface.getFullName());
        apiMethod.setCustom(custom);
        try
        {
            home.create(ctx, apiMethod);
            return apiMethod;
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, ApiMethodHomePipelineFactory.class.getName(), "Unable to add api method '" + apiMethod.getName() + "' of interface '"
                    + apiInterface.getFullName() + "' to ApiMethod transient home: " + e.getMessage(), e);
        }
        return null;
    }
    
    public Collection<String> createApiMethods(Context ctx, ApiInterface apiInterface, boolean custom, Home home)
    {
        String interfaceClassName = apiInterface.getFullName();
        Collection<String> result = new ArrayList<String>();
        try
        {
            Class interfaceClass = Class.forName(interfaceClassName);

            for (Method method : interfaceClass.getMethods())
            {
                ApiMethod apiMethod = createApiMethod(ctx, apiInterface, method.getName(), custom, home);
                if (apiMethod!=null)
                {
                    result.add(apiMethod.getFullName());
                }
            }
        }
        catch (ClassNotFoundException e)
        {
            LogSupport.minor(ctx, ApiMethodHomePipelineFactory.class.getName(), "Unable to add api methods of interface '"
                    + interfaceClassName + "' to ApiMethod transient home. Interface class was not found: " + e.getMessage(), e);
        }
        return result;
    }

}
