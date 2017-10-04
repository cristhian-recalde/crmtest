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

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.home.pipelineFactory.ApiMethodQueryExecutorHomePipelineFactory;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class TestValidateQueryExecutorsImplementation extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestValidateQueryExecutorsImplementation(final String name)
    {
        super(name);
    }
    
    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }   
    
    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestValidateQueryExecutorsImplementation.class);

        return suite;
    }
    
    // INHERIT
    public void setUp()
    {
        super.setUp();
    }


    // INHERIT
    public void tearDown()
    {
        super.tearDown();
    }
    
    public void testValidateInstalledQueryExecutors()
    {
        Context ctx = getContext().createSubContext();
        try
        {
            Home home = (Home) ctx.get(ApiMethodQueryExecutorHome.class);
            Home apiInterfaceHome = (Home) ctx.get(ApiInterfaceHome.class);
            Home apiMethodHome = (Home) ctx.get(ApiMethodHome.class);
    
            assertNotNull("ApiMethodQueryExecutorHome not in the context", home);
            assertNotNull("ApiInterfaceHome not in the context", apiInterfaceHome);
            assertNotNull("ApiMethodHome not in the context", apiMethodHome);
        
            for (ApiMethodQueryExecutor methodQueryExecutor : (Collection<ApiMethodQueryExecutor>) home.selectAll(ctx))
            {
                String apiInterfaceId = methodQueryExecutor.getApiInterface();
                String apiMethodId = methodQueryExecutor.getApiMethod();
                ApiInterface apiInterface = getApiInterface(ctx, apiInterfaceHome, apiInterfaceId);
                ApiMethod apiMethod = getApiMethod(ctx, apiMethodHome, apiMethodId);
                Class<?> clazz = getClass(apiInterface);
                Method method = getMethod(clazz, apiMethod.getName());
                if (method!=null)
                {
                    assertTrue("Method '" + apiMethod.getName() + "' on interface '" + apiInterfaceId + " expecting different parameters", methodQueryExecutor.getQueryExecutor().validateParameterTypes(method.getParameterTypes()));
                    assertTrue("Method '" + apiMethod.getName() + "' on interface '" + apiInterfaceId + " expecting different return type", methodQueryExecutor.getQueryExecutor().validateReturnType(method.getReturnType()));
                }
            }
        }
        catch (Throwable t)
        {
            fail("Unable to retrieve Api Method Query Executors: " + t.getMessage());
        }
    }
    
    private ApiInterface getApiInterface(Context ctx, Home home, String id)
    {
        ApiInterface result = null;
        try
        {
            result = (ApiInterface) home.find(ctx, id);
            assertNotNull("Api Interface '" + id + "' not found", result);
        }
        catch (Throwable t)
        {
            fail("Unable to retrieve Api Interface '" + id + "': " + t.getMessage());
        }
        
        return result;
    }

    private ApiMethod getApiMethod(Context ctx, Home home, String id)
    {
        ApiMethod result = null;
        try
        {
            result = (ApiMethod) home.find(ctx, id);
            assertNotNull("Api Method '" + id + "' not found", result);
        }
        catch (Throwable t)
        {
            fail("Unable to retrieve Api Method '" + id + "': " + t.getMessage());
        }
        
        return result;
    }
    
    private Class<?> getClass(ApiInterface apiInterface)
    {
        Class<?> clazz = null;
        try
        {
            clazz = Class.forName(apiInterface.getFullName());
            assertNotNull("Class '" + apiInterface.getFullName() + "' defined for '" + apiInterface.ID() + " not found", clazz);
        }
        catch (ClassNotFoundException t)
        {
            fail("Class '" + apiInterface.getFullName() + "' defined for '" + apiInterface.ID() + "not found");
        }
        return clazz;
    }
    
    private Method getMethod(Class clazz, String name)
    {
        Method[] methods = clazz.getMethods();
        Method result = null;
        for (Method method : methods)
        {
            if (method.getName().equals(name))
            {
                result = method;
                break;
            }
        }
        assertNotNull("Method '" + name + "' on interface '" + clazz.getSimpleName() + " not found", clazz);
        return result;
    }

}
