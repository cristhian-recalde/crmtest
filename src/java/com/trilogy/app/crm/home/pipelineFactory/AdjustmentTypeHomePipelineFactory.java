/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.home.AdjustmentTypeERLogHome;
import com.trilogy.app.crm.home.AdjustmentTypePermissionSettingHome;
import com.trilogy.app.crm.home.core.CoreAdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.sequenceId.AdjustmentTypeCodeSettingHome;

/**
 * Provides a class from which to create the pipeline of Home decorators that
 * process a Transaction travelling between the application and the given
 * delegate.
 * 
 * @author arturo.medina@redknee.com
 */
public class AdjustmentTypeHomePipelineFactory extends CoreAdjustmentTypeHomePipelineFactory
{

    @Override
    public Home createPipeline(Context ctx, Context serverCtx)
            throws RemoteException, HomeException, IOException
    {
        // Core installed most of this pipeline already.  Need to wrap with CRM specific homes.
        Home systemHome = (Home) ctx.get(ADJUSTMENT_TYPE_SYSTEM_HOME);

        systemHome = new AdjustmentTypePermissionSettingHome(ctx, systemHome);

        systemHome = new AdjustmentTypeCodeSettingHome(ctx, systemHome);
        systemHome = new AdjustmentTypeERLogHome(systemHome);

        ctx.put(ADJUSTMENT_TYPE_SYSTEM_HOME, systemHome);

        // Don't return the system home.  Return the original adjustment type home that was
        // installed by the core.  It redirects to system home that we just installed above.
        return (Home) ctx.get(AdjustmentTypeHome.class);
    }

}
