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

import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
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
public class MandatoryParameterValidatorQueryExecutor<T extends Object> extends AbstractMandatoryParameterValidatorQueryExecutor
{
    public MandatoryParameterValidatorQueryExecutor()
    {
        super();
    }

    public MandatoryParameterValidatorQueryExecutor(int parameter, String parameterName, QueryExecutor<T> delegate)
    {
        super(parameter, parameterName, delegate);
    }
    
    protected void validateMandatoryParameter(Context ctx, Object parameter) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.validateMandatoryObject(parameter, getParameterName());
    }

    public T execute(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        validateMandatoryParameter(ctx, (Object) parameters[getParameter()-1]);
        
        return (T) getDelegate().execute(ctx, parameters);
    }

    
    @Override
    public boolean validateParameterTypes(Class[] parameterTypes)
    {
        return getDelegate().validateParameterTypes(parameterTypes);
    }
    
}
