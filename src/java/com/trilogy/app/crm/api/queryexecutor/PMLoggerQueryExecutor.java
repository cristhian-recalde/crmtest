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
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class PMLoggerQueryExecutor<T extends Object> extends AbstractPMLoggerQueryExecutor
{
    public PMLoggerQueryExecutor()
    {
        super();
    }

    public PMLoggerQueryExecutor(QueryExecutor<T> delegate, String name)
    {
        super(delegate, name);
    }
    
    public T execute(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        PMLogMsg logMsg = new PMLogMsg("QueryExecutor", getName());
        try
        {
            return (T) getDelegate().execute(ctx, parameters);
        }
        finally
        {
            logMsg.log(ctx);
        }
    }

    
    public String getMethodFromContext(Context ctx)
    {
        ApiMethodQueryExecutor methodQueryExecutor = (ApiMethodQueryExecutor) ctx.get(ApiMethodQueryExecutor.class);
        if (methodQueryExecutor!=null)
        {
            setName(methodQueryExecutor.getApiMethod());
        }

        return super.getName();
    }

    
}
