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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.PackageBulkTask;
import com.trilogy.app.crm.bean.PackageBulkTaskHome;
import com.trilogy.app.crm.home.BackgroundTaskAwareHome;
import com.trilogy.app.crm.home.FileLoadingTypeAwareHome;
import com.trilogy.app.crm.home.PackageBatchCreateCheckHome;
import com.trilogy.app.crm.home.PackageBatchDependencyCheckHome;
import com.trilogy.app.crm.home.PackageBatchStateMutatorHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.LastModifiedAwareHome;
import com.trilogy.framework.xhome.home.LastModifiedByAwareHome;


/**
 * Creates the home decorators pipeline for the GenericPackage-Bulk-Loader-Task
 * 
 * @author simar.singh@redknee.com
 */
public class PackageBulkLoaderPipelineFactory implements PipelineFactory
{

    public PackageBulkLoaderPipelineFactory()
    {
        super();
    }


    public Home createPipeline(final Context ctx, final Context serverCtx) throws RemoteException, HomeException,
            IOException, AgentException
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, PackageBulkTask.class, "PACKAGEBULKTASK");
        home = new PackageBatchStateMutatorHome(home);
        home = new PackageBatchDependencyCheckHome(home);
        home = new LastModifiedAwareHome(home);
        home = new LastModifiedByAwareHome(home);
        ctx.put(PAKCAGEBULKTASK_DATA_HOME, home);
        home = new FileLoadingTypeAwareHome<PackageBulkTask>(PackageBulkTask.class, home);
        home = new BackgroundTaskAwareHome<PackageBulkTask>(PackageBulkTask.class, home);
        home = new PackageBatchCreateCheckHome(home);
        ctx.put(PackageBulkTaskHome.class, home);
        return home;
    }
    
    public final static String PAKCAGEBULKTASK_DATA_HOME = "PAKCAGEBULKTASK_XDB_HOME";
}
