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

import com.trilogy.app.crm.bean.BearerType;
import com.trilogy.app.crm.bean.BearerTypeHome;
import com.trilogy.app.crm.home.BearerTypeRemovalValidator;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.validator.RemovalValidatingHome;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.LRUCachingHome;

/**
 * Creates the pipeline for {@link BearerTypeHome}.
 *
 * @author cindy.wong@redknee.com
 * @since Jul 19, 2007
 */
public class BearerTypeHomePipelineFactory implements PipelineFactory
{

    /**
     * Create a new instance of <code>BearerTypeHomePipelineFactory</code>.
     */
    protected BearerTypeHomePipelineFactory()
    {
        // do nothing
    }


    /**
     * Returns an instance of <code>BearerTypeHomePipelineFactory</code>.
     *
     * @return An instance of <code>BearerTypeHomePipelineFactory</code>.
     */
    public static BearerTypeHomePipelineFactory instance()
    {
        if (instance == null)
        {
            instance = new BearerTypeHomePipelineFactory();
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
        Home localHome = new LRUCachingHome(context, BearerType.class, true, home);
        localHome = new RemovalValidatingHome(new BearerTypeRemovalValidator(), localHome);
        return localHome;
    }


    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context context, final Context serverContext)
    {
        Home home = CoreSupport.bindHome(context, BearerType.class);
        return decorateHome(context, serverContext, home);
    }

    /**
     * Singleton instance.
     */
    private static BearerTypeHomePipelineFactory instance;
}
