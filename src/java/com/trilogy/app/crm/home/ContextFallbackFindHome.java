/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * This home will look in the context for a bean that does not exist in the delegate
 * on a call to find.  It is useful for move where we want to perform complex validation
 * on beans that do not exist yet (i.e. new copies of accounts or subscriptions).
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class ContextFallbackFindHome extends HomeProxy
{
    public ContextFallbackFindHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public Object find(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Object bean = super.find(ctx, obj);
        if (bean == null)
        {
            new DebugLogMsg(this, "Record not found for " + obj + ".  Looking in context for a transient value.", null).log(ctx);
            bean = ctx.get(obj.getClass().getName() + "_" + obj);
            if (bean != null)
            {
                new InfoLogMsg(this, "Returning transient bean for " + obj + " query.  bean=[" + bean + "]", null).log(ctx);
            }
        }
        return bean;
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Object result = null;
        
        if (obj instanceof Identifiable
                && super.find(ctx, ((Identifiable)obj).ID()) == null
                && ctx.has(obj.getClass().getName() + "_" + ((Identifiable)obj).ID()))
        {
            // Somebody is doing a store based on the value that they retrieved using
            // this class's find method.  Ignore it because whoever put this in the pipeline
            // didn't want to create it yet (e.g. move logic).  Any updates that the caller
            // made to the bean would have been made on the instance in the context, so trust
            // that if this bean makes its way to the DB, the caller's updates will too.
            new InfoLogMsg(this, "Ignoring store() call for transient bean=[" + obj + "]", null).log(ctx);
            result = obj;
        }
        else
        {
            result = super.store(ctx, obj);
        }
        
        return result;
    }    
}
