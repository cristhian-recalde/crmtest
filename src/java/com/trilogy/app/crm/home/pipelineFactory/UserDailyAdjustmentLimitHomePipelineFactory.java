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

import com.trilogy.app.crm.bean.UserDailyAdjustmentLimit;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.xhome.home.CreateOrStoreHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.LRUCachingHome;

/**
 * Creates the service home decorators and put is in the context.
 * @author Marcio Marques
 * @since 9.1.1
 *
 */
public class UserDailyAdjustmentLimitHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx)
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, UserDailyAdjustmentLimit.class,
                "USERDAILYADJUSTMENTLIMIT");
        home = new LRUCachingHome(ctx, UserDailyAdjustmentLimit.class, true, home);

        return home;
    }

}
