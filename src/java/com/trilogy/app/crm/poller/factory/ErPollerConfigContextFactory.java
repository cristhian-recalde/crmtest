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
package com.trilogy.app.crm.poller.factory;

import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.bean.ErPollerConfigHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;


/**
 * 
 * @author Aaron Gourley
 * @since 7.5 
 *
 */
public class ErPollerConfigContextFactory implements ContextFactory
{
    private ErPollerConfig config_ = null;

    public ErPollerConfigContextFactory(ErPollerConfig defaultConfig)
    {
        config_  = defaultConfig;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.context.ContextFactory#create(com.redknee.framework.xhome.context.Context)
     */
    public Object create(Context ctx)
    {
        ErPollerConfig config = null;
        
        Home configHome = (Home)ctx.get(ErPollerConfigHome.class);
        if( configHome != null )
        {
            try
            {
                config = (ErPollerConfig) configHome.find(config_.ID());
            }
            catch (HomeException e)
            {
                new DebugLogMsg(this, e.getClass().getSimpleName() + " occurred looking up ErPollerConfig in home.", e).log(ctx);
            }
        }
        else
        {
            new DebugLogMsg(this, "ErPollerConfigHome not found in context.", null).log(ctx);
        }
        
        if( config != null )
        {
            config_ = config;
        }
        else
        {
            new DebugLogMsg(this, "ErPollerConfig not found in home.  Using previous config.", null).log(ctx);
        }
        
        return config_;
    }

}
