/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bulkloader.generic;

import com.trilogy.app.crm.agent.StorageInstall;
import com.trilogy.app.crm.bulkloader.generic.bean.SearchableSubscriberAuxiliaryServicePipelineFactory;
import com.trilogy.app.crm.bulkloader.generic.bean.SearchableSubscriberPipelineFactory;
import com.trilogy.app.crm.bulkloader.generic.bean.SearchableSubscriberServicesPipelineFactory;
import com.trilogy.app.crm.config.AppSmsbClientConfig;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredMetaBean;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.CritLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This class will install the Generic Bulkload Module -- the necessary components for the 
 * Generic Bulkload class to work.
 * 
 * @author angie.li@redknee.com
 *
 */
public class GenericBeanBulkloadModuleInstall extends CoreSupport implements ContextAgent
{

    public void execute(Context ctx) throws AgentException 
    {
        try
        {
            Context serverCtx = (Context) ctx.get(StorageInstall.RMI_SERVER_CTX_KEY);
            
            //Install the Generic Bulk loader configuration 
            new GenericBeanBulkloaderPipelineFactory().createPipeline(ctx, serverCtx);
            
            //Thread Pool Configuration
            LogSupport.info(ctx, this, "Installing the Generic Bulkloader Thread Pool Config Bean");
            CoreSupport.bindBean(ctx, GenericBeanBulkloaderThreadPoolConfig.class);
            new RMIClusteredMetaBean(
                  ctx,
                  GenericBeanBulkloaderThreadPoolConfig.class.getName(),
                  GenericBeanBulkloaderThreadPoolConfig.class,
                  true,
                  CoreSupport.getProjectHome(ctx),
                  CoreSupport.getHostname(ctx));
            
            //Install the pipelines for Bean Search/Look-up
            new SearchableSubscriberPipelineFactory().createPipeline(ctx, serverCtx);
            
            //Install the pipelines for SubscriberAuxiliaryService search/look-up
            new SearchableSubscriberAuxiliaryServicePipelineFactory().createPipeline(ctx, serverCtx);
            
            //Install the pipelines for SubscriberPricePlanVersion search/look-up
            new SearchableSubscriberServicesPipelineFactory().createPipeline(ctx, serverCtx);
        }
        catch (final Throwable t)
        {
            new CritLogMsg(this, "Failed to install the Generic Bean Bulkload Module", t).log(ctx);
            throw new AgentException("Fail to complete GenericBeanBulkloadModuleInstall", t);
        }
    }

}
