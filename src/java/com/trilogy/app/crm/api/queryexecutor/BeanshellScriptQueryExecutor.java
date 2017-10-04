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

import com.trilogy.framework.core.scripting.BeanShellExecutor;
import javax.script.ScriptException;
import com.trilogy.framework.xhome.context.Context;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class BeanshellScriptQueryExecutor<T extends Object> extends AbstractBeanshellScriptQueryExecutor
{
    public BeanshellScriptQueryExecutor()
    {
        super();
    }
    
    protected Object retrieveObjectFromScript(Context ctx) throws ScriptException
    {
        return  BeanShellExecutor.instance().retrieveObject(ctx, this.getScript(), "");
    }
    
}
