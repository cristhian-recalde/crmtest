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

import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class HeaderAuthenticatorQueryExecutor<T extends Object> extends AbstractHeaderAuthenticatorQueryExecutor
{
    public HeaderAuthenticatorQueryExecutor()
    {
        super();
    }

    public HeaderAuthenticatorQueryExecutor(int headerProperty, String permission, String method, QueryExecutor<T> delegate)
    {
        super(headerProperty, permission, method, delegate);
    }
    
    protected void validateHeader(Context ctx, CRMRequestHeader header) throws CRMExceptionFault
    {
        RmiApiSupport.authenticateUser(ctx, header, method_, permission_);
    }

    public T execute(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        validateHeader(ctx, (CRMRequestHeader) parameters[getHeaderProperty()-1]);
        
        return (T) getDelegate().execute(ctx, parameters);
    }

    
    public String getMethodFromContext(Context ctx)
    {
        ApiMethodQueryExecutor methodQueryExecutor = (ApiMethodQueryExecutor) ctx.get(ApiMethodQueryExecutor.class);
        if (methodQueryExecutor!=null)
        {
            setMethod(methodQueryExecutor.getApiMethod().substring(methodQueryExecutor.getApiMethod().indexOf(".") + 1));
        }

        return super.getMethod();
    }

    @Override
    public boolean validateParameterTypes(Class[] parameterTypes)
    {
        boolean result = true;
        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[getHeaderProperty()-1]);
        return result && getDelegate().validateParameterTypes(parameterTypes);
    }
    
}
