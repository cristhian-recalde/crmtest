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

import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public abstract class ExecuteResultQueryExecutor extends AbstractQueryExecutor<ExecuteResult> 
{
    
    public ExecuteResult addParameterToResult(ExecuteResult result, String name, Object value)
    {
        if (result==null)
        {
            result = new ExecuteResult();
        }

        GenericParameter parameter = new GenericParameter();
        parameter.setName(name);
        parameter.setValue(value);
        
        result.addParameters(parameter);
        
        return result;
    }
    
}
