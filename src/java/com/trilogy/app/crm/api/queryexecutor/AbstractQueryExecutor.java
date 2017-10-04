package com.trilogy.app.crm.api.queryexecutor;

import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

public abstract class AbstractQueryExecutor<T> implements QueryExecutor<T> 
{
    public boolean isGenericExecution(Context ctx, Object... parameters)
    {
        return (parameters.length == 2  && parameters[1] instanceof GenericParameter[]);
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        AbstractQueryExecutor cln = (AbstractQueryExecutor) super.clone();
        return cln;
    }
    

    public Object deepClone() throws CloneNotSupportedException
    {
        return clone();
    }
    

    public void setMethodSimpleName(Context ctx, String methodSimpleName)
    {
        methodSimpleName_ = methodSimpleName;
    }


    public String getMethodSimpleName(Context ctx)
    {
        return methodSimpleName_;
    }

    protected GenericParameter[] getGenericParameters(Context ctx, int paramGenericParameters, String paramGenericParametersName, Object... parameters) throws CRMExceptionFault
    {
    	return getParameter(ctx, paramGenericParameters, paramGenericParametersName, GenericParameter[].class, parameters);
    }
    
    protected <Y extends Object> Y getParameter(Context ctx, int paramIdentifier, String paramName, Class<Y> resultClass, Object... parameters) throws CRMExceptionFault
    {
        Y result = null;
        if (isGenericExecution(ctx, parameters))
        {
            GenericParametersAdapter<Y> adapter = new GenericParametersAdapter<Y>(resultClass, paramName);
            try
            {
                result = (Y) adapter.unAdapt(ctx, parameters);
            }
            catch (HomeException e)
            {
                RmiApiErrorHandlingSupport.generalException(ctx, e,
                        "Unable to extract argument '" + paramName + "' from generic parameters: " + e.getMessage(), this);
            }
        }
        else
        {
            result = (Y) parameters[paramIdentifier];
        }
        return result;
    }


    private String methodSimpleName_ = "";
}
