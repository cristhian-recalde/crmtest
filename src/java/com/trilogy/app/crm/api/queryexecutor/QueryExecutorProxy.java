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

import com.trilogy.framework.xhome.beans.Proxy;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class QueryExecutorProxy<T extends Object> extends AbstractQueryExecutorProxy implements Proxy
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected QueryExecutorProxy()
    {
        delegate_ = null;
    }

    public QueryExecutorProxy(QueryExecutor<T> delegate)
    {
        delegate_ = delegate;
    }

    public T execute(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        if (getDelegate()!=null)
        {
            return getDelegate().execute(ctx, parameters);
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public boolean validateParameterTypes(Class[] parameterTypes)
    {
        if (getDelegate()!=null)
        {
            return getDelegate().validateParameterTypes(parameterTypes);
        }
        else
        {
            return true;
        }
    }

    @Override
    public boolean validateReturnType(Class returnType)
    {
        if (getDelegate()!=null)
        {
            return getDelegate().validateReturnType(returnType);
        }
        else
        {
            return true;
        }
    }

    public QueryExecutor<T> getDelegate()
    {
        return delegate_;
    }

    @Override
    public boolean isGenericExecution(Context ctx, Object... parameters)
    {
        if (getDelegate()!=null)
        {
            return getDelegate().isGenericExecution(ctx, parameters);
        }
        else
        {
            return false;
        }
    }

    @Override
    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        if (getDelegate()!=null)
        {
            return getDelegate().getParameters(ctx, parameters);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setMethodSimpleName(Context ctx, String methodSimpleName)
    {
        if (getDelegate()!=null)
        {
            getDelegate().setMethodSimpleName(ctx, methodSimpleName);
        }
    }

    @Override
    public String getMethodSimpleName(Context ctx)
    {
        if (getDelegate()!=null)
        {
            return getDelegate().getMethodSimpleName(ctx);
        }
        else
        {
            return null;
        }
    }

}
