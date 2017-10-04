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

import com.trilogy.framework.xhome.beans.XDeepCloneable;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public interface QueryExecutor<T> extends XDeepCloneable
{
    public T execute(Context ctx, Object... parameters) throws CRMExceptionFault;

    public boolean validateParameterTypes(Class<?>[] parameterTypes);

    public boolean validateReturnType(Class<?> returnType);

    public boolean isGenericExecution(Context ctx, Object... parameters);
    
    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault;
    
    public void setMethodSimpleName(Context ctx, String methodSimpleName);
    
    public String getMethodSimpleName(Context ctx);
}
