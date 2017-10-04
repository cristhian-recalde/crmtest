/*
 * Created on Jan 20, 2005
 *
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
package com.trilogy.app.crm.agent;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.config.CacheConfig;
import com.trilogy.app.crm.config.CacheConfigHome;
import com.trilogy.app.crm.home.pipelineFactory.CRMSpidHomePipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.application.RemoteApplication;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.core.platform.Ports;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.CritLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

/**
 * This class contains homes that need to be installed even before the beans (BeanInstall).
 *
 * @author psperneac
 */
public class PreStorageInstall implements ContextAgent
{

    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
      Context serverCtx = ctx.createSubContext("RMI Server Context");
      CoreSupport.bindBean(ctx, Common.BAS_APPNAME, RemoteApplication.class);

      RemoteApplication basApp = StorageSupportHelper.get(ctx).retrieveRemoteBASAppConfig(ctx);
      int basRemotePort = basApp!=null? basApp.getBasePort()+ Ports.RMI_OFFSET : Common.BAS_PORT;
      new InfoLogMsg(this, "BAS is configured to be "+basApp, null).log(ctx);

      try
      {
        // [CW] clustered for all
        ctx.put(CRMSpidHome.class, CRMSpidHomePipelineFactory.instance().createPipeline(ctx, serverCtx));

        // [CW] cache sizes to be used by the LRUCachingHome across the system
        ctx.put(CacheConfigHome.class,
              new RMIClusteredHome(ctx, CacheConfigHome.class.getName(),
                 CoreSupport.bindHome(ctx, CacheConfig.class)));
      }
      catch (Throwable t)
      {
         new CritLogMsg(this, "fail to install", t).log(ctx);
         throw new AgentException("Fail to complete PreStorageInstall", t);
      }
    }
    
}
