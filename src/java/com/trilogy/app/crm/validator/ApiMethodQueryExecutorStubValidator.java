package com.trilogy.app.crm.validator;

import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorXInfo;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutorProxy;
import com.trilogy.app.crm.api.queryexecutor.StubImplementationQueryExecutor;
import com.trilogy.framework.core.scripting.BeanShellExecutor;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

public class ApiMethodQueryExecutorStubValidator implements Validator
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
        //ClassNotFoundException, InstantiationException, IllegalAccessException
        if (queryExecutor != null && queryExecutor instanceof StubImplementationQueryExecutor)
        {
            StubImplementationQueryExecutor stubQueryExecutor = (StubImplementationQueryExecutor) queryExecutor;
            try
            {
                Object defaultResult = stubQueryExecutor.getDefaultResult();
                if (stubQueryExecutor.getImplementationScript()!=null && stubQueryExecutor.getImplementationScript().trim().length()>0)
                {
                    try
                    {
                        Object scriptResult = BeanShellExecutor.instance().retrieveObject(ctx, stubQueryExecutor.getImplementationScript(), "");
                        if (scriptResult==null)
                        {
                            exception.thrown(new IllegalPropertyArgumentException(ApiMethodQueryExecutorXInfo.QUERY_EXECUTOR, "Implementation script returns null."));
                        }
                        else if (!defaultResult.getClass().isAssignableFrom(scriptResult.getClass()))
                        {
                            exception.thrown(new IllegalPropertyArgumentException(ApiMethodQueryExecutorXInfo.QUERY_EXECUTOR, "Implementation script returns object that cannot be cast to the return type: '" + scriptResult.getClass() + "'."));
                        }
                    }
                    catch (Throwable t)
                    {
                        exception.thrown(new IllegalPropertyArgumentException(ApiMethodQueryExecutorXInfo.QUERY_EXECUTOR, "Unable to parse implementation script."));
                    }
                    
                }
            }
            catch (ClassNotFoundException t)
            {
                exception.thrown(new IllegalPropertyArgumentException(ApiMethodQueryExecutorXInfo.QUERY_EXECUTOR, "Class '" + stubQueryExecutor.getReturnType() + "' not found."));
            }
            catch (Throwable t)
            {
                exception.thrown(new IllegalPropertyArgumentException(ApiMethodQueryExecutorXInfo.QUERY_EXECUTOR, "Unable to instantiate an object instance for class '" + stubQueryExecutor.getReturnType() + "'."));
            }
            
        }
            
        exception.throwAll();
    }
}
