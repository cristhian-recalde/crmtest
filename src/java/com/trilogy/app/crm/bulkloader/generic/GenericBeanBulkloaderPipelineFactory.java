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

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.home.PipelineFactory;

/**
 * Factory creates the GenericBeanBulkloader pipeline.
 * @author angie.li@redknee.com
 *
 */
public class GenericBeanBulkloaderPipelineFactory implements PipelineFactory
{


    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException, AgentException
    {
        LogSupport.info(ctx, this, "Installing the Generic Bulkloader Config home ");
        
        Home home = CoreSupport.bindHome(ctx, GenericBeanBulkloader.class);
        home = new AuditJournalHome(ctx, home);
        home = new SortingHome(home);
        home = new RMIClusteredHome(ctx, GenericBeanBulkloaderHome.class.getName(), home);
        home = new ValidatingHome(home, new BulkloaderSearchValidator());

        ctx.put(GenericBeanBulkloaderHome.class, home);
        
        return home;
    }

}
