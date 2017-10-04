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

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.relationship.NoRelationshipRemoveHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.priceplan.SubscriptionLevel;
import com.trilogy.app.crm.bean.priceplan.SubscriptionLevelHome;
import com.trilogy.app.crm.bean.priceplan.SubscriptionLevelXInfo;
import com.trilogy.app.crm.home.PipelineFactory;

public class SubscriptionLevelHomePipelineFactory implements PipelineFactory
{
    /**
     * Singleton instance.
     */
    private static SubscriptionLevelHomePipelineFactory instance_;

    /**
     * Create a new instance of <code>SubscriptionTypeHomePipelineFactory</code>.
     */
    protected SubscriptionLevelHomePipelineFactory()
    {
        // empty
    }

    /**
     * Returns an instance of <code>SubscriptionTypeHomePipelineFactory</code>.
     *
     * @return An instance of <code>SubscriptionTypeHomePipelineFactory</code>.
     */
    public static SubscriptionLevelHomePipelineFactory instance()
    {
        if (instance_ == null)
        {
            instance_ = new SubscriptionLevelHomePipelineFactory();
        }
        return instance_;
    }

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
    AgentException
    {
        Home home = CoreSupport.bindHome(ctx, SubscriptionLevel.class);
        home = new SortingHome(ctx, home);
        home = new RMIClusteredHome(ctx, SubscriptionLevelHome.class.getName(), home);
        home = new SpidAwareHome(ctx, home);
        home = new NoRelationshipRemoveHome(ctx, SubscriptionLevelXInfo.ID,
                PricePlanXInfo.SUBSCRIPTION_LEVEL, PricePlanHome.class,
                "This Subscription Level is in use.  Cannot delete this Subscription Level.", home);

        return home;
    }

}