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

import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.framework.core.scripting.BeanShellExecutor;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class StubImplementationQueryExecutor<T extends Object> extends AbstractStubImplementationQueryExecutor
{

    @Override
    public Object execute(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        if (this.getImplementationScript()!=null && this.getImplementationScript().trim().length()>0)
        {
            try
            {
                Context subContext = ctx.createSubContext();
                subContext.put(this.getParametersKey(), parameters);
                return BeanShellExecutor.instance().retrieveObject(subContext, this.getImplementationScript(), "");
            }
            catch (Throwable t)
            {
                LogSupport.minor(ctx, this, "Unable to instantiate result object from beanshell: " + t.getMessage());
                return null;
            }
        }
        else
        {
            try
            {
                return getDefaultResult();
            }
            catch (Throwable t)
            {
                LogSupport.minor(ctx, this, "Unable to instantiate result default object: " + t.getMessage());
                return null;
            }
        }
    }

    @Override
    public boolean validateParameterTypes(Class[] parameterTypes)
    {
        return true;
    }

    @Override
    public boolean validateReturnType(Class returnType)
    {
        try
        {
            return getReturnTypeClass().isAssignableFrom(returnType);
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    @Override
    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        return parameters;
    }
    
    public Object getDefaultResult() throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        return getReturnTypeClass().newInstance();
    }

    public Class<?> getReturnTypeClass() throws ClassNotFoundException
    {
        if (returnTypeClass_==null)
        {
            returnTypeClass_ = Class.forName(getReturnType());
        }
        return returnTypeClass_;
    }
    
    public boolean isGenericExecution(Context ctx, Object... parameters)
    {
        return (parameters.length == 2  && parameters[1] instanceof GenericParameter[]);
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        StubImplementationQueryExecutor cln = (StubImplementationQueryExecutor) super.clone();
        return cln;
    }
    

    public Object deepClone() throws CloneNotSupportedException
    {
        return clone();
    }
    

    public void setMethodSimpleName(Context ctx, String methodSimpleName)
    {
        methodSimpleName_ = methodSimpleName;
    }


    public String getMethodSimpleName(Context ctx)
    {
        return methodSimpleName_;
    }

    protected GenericParameter[] getGenericParameters(Context ctx, int paramGenericParameters, String paramGenericParametersName, Object... parameters) throws CRMExceptionFault
    {
        return getParameter(ctx, paramGenericParameters, paramGenericParametersName, GenericParameter[].class, parameters);
    }
    
    protected <Y extends Object> Y getParameter(Context ctx, int paramIdentifier, String paramName, Class<Y> resultClass, Object... parameters) throws CRMExceptionFault
    {
        Y result = null;
        if (isGenericExecution(ctx, parameters))
        {
            GenericParametersAdapter<Y> adapter = new GenericParametersAdapter<Y>(resultClass, paramName);
            try
            {
                result = (Y) adapter.unAdapt(ctx, parameters);
            }
            catch (HomeException e)
            {
                RmiApiErrorHandlingSupport.generalException(ctx, e,
                        "Unable to extract argument '" + paramName + "' from generic parameters: " + e.getMessage(), this);
            }
        }
        else
        {
            result = (Y) parameters[paramIdentifier];
        }
        return result;
    }


    private String methodSimpleName_ = "";
    
    private Class<?> returnTypeClass_;
}
