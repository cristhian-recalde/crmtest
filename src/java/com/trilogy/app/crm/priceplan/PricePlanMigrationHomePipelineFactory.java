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

package com.trilogy.app.crm.priceplan;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.bean.PricePlanMigrationXDBHome;
import com.trilogy.app.crm.home.PipelineFactory;

/**
 * Create the pipeline for {@link PricePlanMigrationHome}. 
 * @author cindy.wong@redknee.com
 */
public class PricePlanMigrationHomePipelineFactory implements PipelineFactory
{
/**
 * Create a new instance of <code>PricePlanMigrationHomePipelineFactory</code>.
 */
protected PricePlanMigrationHomePipelineFactory()
{
    //empty
}

/**
 * Returns an instance of <code>PricePlanMigrationHomePipelineFactory</code>.
 * @return An instance of <code>PricePlanMigrationHomePipelineFactory</code>.
 */
public static PricePlanMigrationHomePipelineFactory instance()
{
    if (instance == null)
    {
        instance = new PricePlanMigrationHomePipelineFactory();
    }
    return instance;
}

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context context, final Context serverContext) throws RemoteException, HomeException, IOException,
        AgentException
    {
        final Home home = new PricePlanMigrationXDBHome(context);
        return addDecorators(home, context, serverContext);
    }

    /**
     * Add decorators to the original home.
     *
     * @param originalHome
     *            The original home.
     * @param context
     *            Operating context.
     * @param serverContext
     *            Server context.
     * @return Decorated home.
     */
    public Home addDecorators(final Home originalHome, final Context context, final Context serverContext)
    {
        Home home = new ValidatingHome(PricePlanMigrationDatesValidator.instance(), originalHome);
        return home;
    }

    /**
     * Singleton instance.
     */
    private static PricePlanMigrationHomePipelineFactory instance;
}
