package com.trilogy.app.crm.validator;

import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorXInfo;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutorProxy;
import com.trilogy.app.crm.api.queryexecutor.ScriptQueryExecutor;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

public class ApiMethodQueryExecutorScriptValidator implements Validator
{
    public void validate(Context ctx, Object obj)
    throws IllegalStateException 
    {
        CompoundIllegalStateException exception = new CompoundIllegalStateException();

        ApiMethodQueryExecutor bean = (ApiMethodQueryExecutor) obj;
        QueryExecutor queryExecutor = bean.getQueryExecutor();
        while (queryExecutor!=null && queryExecutor instanceof QueryExecutorProxy)
        {
            queryExecutor = ((QueryExecutorProxy) queryExecutor).getDelegate();
        }
        
        if (queryExecutor==null)
        {
            exception.thrown(new IllegalPropertyArgumentException(ApiMethodQueryExecutorXInfo.QUERY_EXECUTOR, "No Script Query Executor defined."));
        }
        else if (queryExecutor instanceof ScriptQueryExecutor)
        {
            ScriptQueryExecutor scriptQueryExecutor = (ScriptQueryExecutor) queryExecutor;
            try
            {
                scriptQueryExecutor.parseScript(ctx);
            }
            catch (Throwable t)
            {
                exception.thrown(new IllegalPropertyArgumentException(ApiMethodQueryExecutorXInfo.QUERY_EXECUTOR, t.getMessage()));
            }
        }
        else if (queryExecutor instanceof QueryExecutorProxy || !(queryExecutor instanceof QueryExecutor))
        {
            exception.thrown(new IllegalPropertyArgumentException(ApiMethodQueryExecutorXInfo.QUERY_EXECUTOR, "No Query Executor defined."));
        }
            
        exception.throwAll();
    }
}
