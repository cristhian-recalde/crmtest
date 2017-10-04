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
package com.trilogy.app.crm.subscriber.subscription.history;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.service.SubscriptionProvisioningHistory;
import com.trilogy.app.crm.bean.service.SubscriptionProvisioningHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriptionProvisioningHistoryXDBHome;
import com.trilogy.app.crm.bean.service.SubscriptionProvisioningHistoryXInfo;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.Order;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.home.WhereHome;

/**
 * Creates and registers the pipeline for ServiceProvisioningHistory
 * @author kumaran.sivasubramaniam@redknee.com
 *
 */
public class SubscriptionProvisioningHistoryPipelineFactory implements PipelineFactory
{

    public Home createPipeline(Context ctx, Context serverCtx)
            throws RemoteException, HomeException, IOException, AgentException 
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, SubscriptionProvisioningHistory.class, "SUBSCRIPTIONPROVISIONHIST");
        ctx.put(SubscriptionProvisioningHistoryXDBHome.class, home);

        home = new ValidatingHome(home, new SubscriberSubscriptionHistoryValidator());
        
        // Sorting home by timestamp
        home = new WhereHome(ctx, home, (new Order()).add(SubscriptionProvisioningHistoryXInfo.TIMESTAMP_, false));
        
        ctx.put(SubscriptionProvisioningHistoryHome.class, home);
        
        return home;
    }

}
