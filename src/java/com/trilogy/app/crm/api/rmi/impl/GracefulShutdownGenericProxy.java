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
package com.trilogy.app.crm.api.rmi.impl;

import java.lang.reflect.Method;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.support.GracefulShutdownSupport;
import com.trilogy.util.crmapi.wsdl.AbstractGenericProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class GracefulShutdownGenericProxy extends AbstractGenericProxy
{
    public static Object newInstance(Context ctx, Object delegate)
    {
        return java.lang.reflect.Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                delegate.getClass().getInterfaces(), new GracefulShutdownGenericProxy(ctx, delegate));
    }
    
    protected GracefulShutdownGenericProxy(Context ctx, Object delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object delegate(Context ctx, Object dynamicProxy, Object delegate, Method method, Object[] args)
            throws Throwable
    {
        boolean entered = false;
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes != null)
        {
            for (Class exceptionType : exceptionTypes)
            {
                if (CRMExceptionFault.class.getName().equals(exceptionType.getName()))
                {
                    GracefulShutdownSupport.enter(getContext(), exceptionType);
                    entered = true;
                    break;
                }
            }
        }
        if (!entered)
        {
            GracefulShutdownSupport.enter(getContext(), CRMExceptionFault.class);
        }
        
        try
        {
            return super.delegate(ctx, dynamicProxy, delegate, method, args);
        }
        finally
        {
            GracefulShutdownSupport.exit(getContext());
        }
    }

}
