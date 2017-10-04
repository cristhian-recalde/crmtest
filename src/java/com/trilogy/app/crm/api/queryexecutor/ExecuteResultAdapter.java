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
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class ExecuteResultAdapter<T extends Object> implements Adapter
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ExecuteResultAdapter(Class<T> clazz)
    {
        adapter_ = new GenericParametersAdapter<T>(clazz);
    }

    @Override
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        ExecuteResult result = new ExecuteResult();
        GenericParameter[] parameters = (GenericParameter[]) adapter_.adapt(ctx, obj);
        result.setParameters(parameters);
        return result;
    }
    
    @Override
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        return adapter_.unAdapt(ctx, ((ExecuteResult) obj).getParameters());
    }
    
    private GenericParametersAdapter<T> adapter_;

}
