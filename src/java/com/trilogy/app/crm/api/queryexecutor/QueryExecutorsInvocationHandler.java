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
package com.trilogy.app.crm.api.queryexecutor;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.context.ContextLocator;


/**
 * An invocation handler for query executors.
 *
 * @author Marcio Marques
 * @since 9.3
 */
public class QueryExecutorsInvocationHandler<T> extends ContextAwareSupport implements InvocationHandler, Serializable
{
    private static final long serialVersionUID = 1L;
    
    private Class<T> skeletonServiceInterface_;

    public static <T> T newInstance(Context ctx, Class<T> skeletonServiceInterface)
    {
        return (T) java.lang.reflect.Proxy.newProxyInstance(skeletonServiceInterface.getClassLoader(),
                new Class[]{ skeletonServiceInterface }, new QueryExecutorsInvocationHandler<T>(ctx, skeletonServiceInterface));
    }
    

    private QueryExecutorsInvocationHandler(Context ctx, Class<T> skeletonServiceInterface)
    {
        setContext(ctx);
        skeletonServiceInterface_ = skeletonServiceInterface;
    }

    /**
     * {@inheritDoc}
     */
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Class declaringClass = method.getDeclaringClass();

        if (declaringClass == Object.class)
        {
            if ("hashCode".equals(method.getName()))
            {
                return proxyHashCode(proxy);
            }
            else if ("equals".equals(method.getName()))
            {
                return proxyEquals(proxy, args[0]);
            }
            else if ("toString".equals(method.getName()))
            {
                return proxyToString(proxy);
            }
            else
            {
                throw new InternalError("unexpected Object method dispatched: " + method);
            }
        }
        else
        {
            final Context subCtx = getContext().createSubContext();
            ContextLocator.setThreadContext(subCtx);

            QueryExecutorFactory executor = QueryExecutorFactory.getInstance();
            
            return executor.execute(subCtx, skeletonServiceInterface_.getSimpleName(), method.getName(), method.getReturnType(), 
                    args);
        }
    }

    protected Integer proxyHashCode(Object proxy)
    {
        return new Integer(System.identityHashCode(proxy));
    }


    protected Boolean proxyEquals(Object proxy, Object other)
    {
        return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
    }


    protected String proxyToString(Object proxy)
    {
        return proxy.getClass().getName() + '@' + Integer.toHexString(proxyHashCode(proxy)) + "(InvocationHandler=" + this.getClass().getName() + ")";
    }
}
