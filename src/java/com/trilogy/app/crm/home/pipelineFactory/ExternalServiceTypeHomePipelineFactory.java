package com.trilogy.app.crm.home.pipelineFactory;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.LRUCachingHome;

import com.trilogy.app.crm.bean.ExternalServiceType;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;

/***
 *
 * @author chandrachud.ingale
 * @since  9.7
 */
public class ExternalServiceTypeHomePipelineFactory implements PipelineFactory
{
    
    private static ExternalServiceTypeHomePipelineFactory instance = new ExternalServiceTypeHomePipelineFactory();
    
    private ExternalServiceTypeHomePipelineFactory()
    {
        
    }
    
    public static ExternalServiceTypeHomePipelineFactory getInstance()
    {
        return instance;
    }
    
    /**
     * {@inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx)
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, ExternalServiceType.class, "EXTERNALSERVICETYPE");
        home = new LRUCachingHome(ctx, ExternalServiceType.class, true, home);      
        
        return home;
    }

}