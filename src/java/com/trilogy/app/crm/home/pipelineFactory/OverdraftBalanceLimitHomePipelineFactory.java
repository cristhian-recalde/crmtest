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

import com.trilogy.app.crm.bean.OverdraftBalanceLimit;
import com.trilogy.app.crm.bean.OverdraftBalanceLimitHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.LRUCachingHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

/**
 * Creates the pipeline for {@link OverdraftBalanceLimitHome}.
 *
 * @author Marcio Marques
 * @since 9.1.1
 */
public class OverdraftBalanceLimitHomePipelineFactory implements PipelineFactory
{

    /**
     * Create a new instance of <code>BearerTypeHomePipelineFactory</code>.
     */
    protected OverdraftBalanceLimitHomePipelineFactory()
    {
        // do nothing
    }


    /**
     * Returns an instance of <code>BearerTypeHomePipelineFactory</code>.
     *
     * @return An instance of <code>BearerTypeHomePipelineFactory</code>.
     */
    public static OverdraftBalanceLimitHomePipelineFactory instance()
    {
        if (instance == null)
        {
            instance = new OverdraftBalanceLimitHomePipelineFactory();
        }
        return instance;
    }


    /**
     * Decorates the provided home with the proper pipeline.
     *
     * @param context
     *            The operating context.
     * @param serverContext
     *            The server operating context.
     * @param home
     *            The home being decorated.
     * @return The decorated home pipeline.
     */
    public Home decorateHome(final Context context, final Context serverContext, final Home home)
    {
        // empty pipeline
        Home localHome = new LRUCachingHome(context, OverdraftBalanceLimit.class, true, home);
        localHome = new SortingHome(context, localHome);
        localHome = new SpidAwareHome(context, localHome);
        return localHome;
    }


    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context context, final Context serverContext)
    {
        Home home = StorageSupportHelper.get(context).createHome(context, OverdraftBalanceLimit.class, "OVERDRAFTBALANCELIMIT");
        return decorateHome(context, serverContext, home);
    }

    /**
     * Singleton instance.
     */
    private static OverdraftBalanceLimitHomePipelineFactory instance;
}
