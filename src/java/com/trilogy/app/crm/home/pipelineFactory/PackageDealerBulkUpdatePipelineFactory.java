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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.LastModifiedAwareHome;
import com.trilogy.framework.xhome.home.LastModifiedByAwareHome;

import com.trilogy.app.crm.bean.PackageDealerBulkUpdateTask;
import com.trilogy.app.crm.bean.PackageDealerBulkUpdateTaskHome;
import com.trilogy.app.crm.home.BackgroundTaskAwareHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.home.FileLoadingTypeAwareHome;

/**
 * Creates the home decorators pipeline for the Package-Dealer-Bulk-Update-Task
 * 
 * @author simar.singh@redknee.com
 */
public class PackageDealerBulkUpdatePipelineFactory implements PipelineFactory {

    public PackageDealerBulkUpdatePipelineFactory() {
        super();
    }

    public Home createPipeline(final Context ctx, final Context serverCtx)
            throws RemoteException, HomeException, IOException, AgentException {

        Home home = StorageSupportHelper.get(ctx).createHome(ctx,
                PackageDealerBulkUpdateTask.class, "PACKAGEDEALERBULKTASK");
        home = new LastModifiedAwareHome(home);
        home = new LastModifiedByAwareHome(home);
        home = new FileLoadingTypeAwareHome<PackageDealerBulkUpdateTask>(PackageDealerBulkUpdateTask.class, home);
        home = new BackgroundTaskAwareHome<PackageDealerBulkUpdateTask>(PackageDealerBulkUpdateTask.class,home);

        ctx.put(PackageDealerBulkUpdateTaskHome.class, home);
        return home;

    }
}
