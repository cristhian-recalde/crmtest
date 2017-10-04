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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class EventRecordGeneratorQueryExecutor<T extends Object> extends AbstractEventRecordGeneratorQueryExecutor
{
    public EventRecordGeneratorQueryExecutor()
    {
        super();
    }

    public EventRecordGeneratorQueryExecutor(int identifierProperty, int spidProperty, String method, QueryExecutor<T> delegate)
    {
        super(identifierProperty, spidProperty, method, delegate);
    }
    
    protected String getIdentifier(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        return String.valueOf(getParameters(ctx, parameters)[getIdentifierProperty()-1]);
    }
    
    protected int getSpid(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        int result = -1;
        
        if (getSpidProperty()!=DEFAULT_SPIDPROPERTY)
        {
            result = (Integer) getParameters(ctx, parameters)[getSpidProperty()-1];
        }
        
        return result;
    }

    public T execute(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        return (T) getDelegate().execute(ctx, parameters);
    }
    
    public String getMethodFromContext(Context ctx)
    {
        ApiMethodQueryExecutor methodQueryExecutor = (ApiMethodQueryExecutor) ctx.get(ApiMethodQueryExecutor.class);
        if (methodQueryExecutor!=null)
        {
            setMethod(methodQueryExecutor.getApiMethod());
        }

        return super.getMethod();
    }

    public static final String SUCCESS = "SUCCESS";

    
}
