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
package com.trilogy.app.crm.api.queryexecutor.generalprovisioning;

import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.ExecuteResultQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.GenericParametersAdapter;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutorFactory;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_1.types.calldetail.UsageTypeReference;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class ExecuteQueryExecutor<T> extends AbstractQueryExecutor<T>
{
    public ExecuteQueryExecutor()
    {
    }
    
    @Override
    public T execute(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        Object[] extractParameters = getParameters(ctx, parameters);
        
        String apiInterface = (String) extractParameters[PARAM_INTERFACE];
        String apiMethod = (String) extractParameters[PARAM_METHOD];
        Object[] newParameters = new Object[extractParameters.length-2];
        newParameters[0] = extractParameters[0];
        for (int i = 1; i < extractParameters.length - 2; i++)
        {
            newParameters[i] = extractParameters[i+2];
        }

        return (T) QueryExecutorFactory.getInstance().execute(ctx, apiInterface, apiMethod, Object.class, newParameters);
    }

    @Override
    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        Object[] result = null;
        if (isGenericExecution(ctx, parameters))
        {
            result = new Object[4];
            result[0] = parameters[0];
            result[1] = getInterfaceName(ctx, parameters);
            result[2] = getMethodName(ctx, parameters);
            result[3] = parameters;
        }
        else
        {
            result = parameters;
        }
        
        return result;
    }

    private String getInterfaceName(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        String result = null;
        if (isGenericExecution(ctx, parameters))
        {
            GenericParametersAdapter<String> adapter = new GenericParametersAdapter<String>(String.class, PARAM_INTERFACE_NAME);
            try
            {
                result = (String) adapter.unAdapt(ctx, parameters);
            }
            catch (HomeException e)
            {
                RmiApiErrorHandlingSupport.generalException(ctx, e,
                        "Unable to extract argument '" + PARAM_INTERFACE_NAME + "' from generic parameters: " + e.getMessage(), this);
            }
        }
        else
        {
            result = (String) parameters[PARAM_INTERFACE];
        }
        return result;
    }

    private String getMethodName(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        String result = null;
        if (isGenericExecution(ctx, parameters))
        {
            GenericParametersAdapter<String> adapter = new GenericParametersAdapter<String>(String.class, PARAM_METHOD_NAME);
            try
            {
                result = (String) adapter.unAdapt(ctx, parameters);
            }
            catch (HomeException e)
            {
                RmiApiErrorHandlingSupport.generalException(ctx, e,
                        "Unable to extract argument '" + PARAM_METHOD_NAME + "' from generic parameters: " + e.getMessage(), this);
            }
        }
        else
        {
            result = (String) parameters[PARAM_METHOD];
        }
        return result;
    }

    @Override
    public boolean validateParameterTypes(Class[] parameterTypes)
    {
        boolean result = true;
        result = result && (parameterTypes.length>=4);
        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
        result = result && String.class.isAssignableFrom(parameterTypes[PARAM_INTERFACE]);
        result = result && String.class.isAssignableFrom(parameterTypes[PARAM_METHOD]);
        result = result && Object[].class.isAssignableFrom(parameterTypes[PARAM_PARAMETERS]);
        return result;
    }
    
    @Override
    public boolean validateReturnType(Class<?> resultType)
    {
        return Object.class.isAssignableFrom(resultType);
    }

    

    public static final int PARAM_HEADER = 0;
    public static final int PARAM_INTERFACE = 1;
    public static final int PARAM_METHOD = 2;
    public static final int PARAM_PARAMETERS = 3;

    public static final String PARAM_INTERFACE_NAME = "interfaceName";
    public static final String PARAM_METHOD_NAME = "methodName";
}
