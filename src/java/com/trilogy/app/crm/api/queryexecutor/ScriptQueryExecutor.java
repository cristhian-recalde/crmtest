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

import javax.script.ScriptException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class ScriptQueryExecutor<T extends Object> extends AbstractScriptQueryExecutor
{
    public ScriptQueryExecutor()
    {
        super();
        queryExecutor_ = null;
    }
    
    protected Object retrieveObjectFromScript(Context ctx) throws ScriptException
    {
        throw new UnsupportedOperationException("Use a super class");
    }
    

    public QueryExecutor<T> parseScript(Context ctx)
    {
        try
        {
            Object obj = retrieveObjectFromScript(ctx);

            if (obj instanceof QueryExecutor)
            {
                return ((QueryExecutor<T>) obj);
            }
            else
            {
                throw new UnsupportedOperationException(
                        "Query Executor BeanShell Script must return object which implements the QueryExecutor interface");
            }
        }
        catch (ScriptException t)
        {
            throw new UnsupportedOperationException("Query Executor BeanShell Script is invalid and cannot be executed");
        }
    }

    
    public T execute(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        return getQueryExecutor(ctx).execute(ctx, parameters);
    }
    
    public QueryExecutor<T> getQueryExecutor(Context ctx)
    {
        if (queryExecutor_==null)
        {
            synchronized(this)
            {
                queryExecutor_ = parseScript(ctx);
            }
        }
        return queryExecutor_;
    }
    
    private QueryExecutor<T> queryExecutor_;

    @Override
    public boolean validateParameterTypes(Class[] parameterTypes)
    {
        return getQueryExecutor(ContextLocator.locate()).validateParameterTypes(parameterTypes);
    }
    
    @Override
    public boolean validateReturnType(Class returnType)
    {
        return getQueryExecutor(ContextLocator.locate()).validateReturnType(returnType);
    }


    @Override
    public boolean isGenericExecution(Context ctx, Object... parameters)
    {
        return getQueryExecutor(ctx).isGenericExecution(ctx, parameters);
    }

    @Override
    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        return getQueryExecutor(ctx).getParameters(ctx, parameters);
    }

    @Override
    public void setMethodSimpleName(Context ctx, String methodSimpleName)
    {
        getQueryExecutor(ctx).setMethodSimpleName(ctx, methodSimpleName);
    }

    @Override
    public String getMethodSimpleName(Context ctx)
    {
        return getQueryExecutor(ctx).getMethodSimpleName(ctx);
    }

    
}
