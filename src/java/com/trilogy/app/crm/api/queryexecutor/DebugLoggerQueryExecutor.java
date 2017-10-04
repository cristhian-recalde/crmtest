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
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class DebugLoggerQueryExecutor<T extends Object> extends AbstractDebugLoggerQueryExecutor
{
    public DebugLoggerQueryExecutor()
    {
        super();
    }

    public DebugLoggerQueryExecutor(QueryExecutor<T> delegate, String name)
    {
        super(delegate, name);
    }
    
    public T execute(Context ctx, Object... parameters) throws CRMExceptionFault
    {
    	StringBuilder message = new StringBuilder("Parameters: ");
    	int i = 0;
    	
    	for (Object parameter : parameters)
    	{
    		message.append("("+ i + ") ");
    		message.append(parameter.toString());
    		message.append("; ");
    		i++;
    	}
    	
    	LogSupport.debug(ctx, getName(), message.toString());
        try
        {
            T result = (T) getDelegate().execute(ctx, parameters);
        	LogSupport.debug(ctx, getName(), "Result: " + result.toString());
        	return result;
        }
        catch (RuntimeException t)
        {
        	LogSupport.debug(ctx, getName(), "Exception: " + t.getMessage(), t);
        	throw t;
        }
        catch (CRMExceptionFault t)
        {
        	LogSupport.debug(ctx, getName(), "CRM Exception Fault: " + t.getMessage(), t);
        	throw t;
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
