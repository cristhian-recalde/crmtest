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

import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryXDBHome;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryXInfo;
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
 * @author ali
 *
 */
public class SubscriberSubscriptionsPipelineFactory implements PipelineFactory
{

    public Home createPipeline(Context ctx, Context serverCtx)
            throws RemoteException, HomeException, IOException, AgentException 
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, SubscriberSubscriptionHistory.class, "SUBSCRIBERSUBSCRIPTIONHIST");
        ctx.put(SubscriberSubscriptionHistoryXDBHome.class, home);
        /*
         * TODO temporarily removing sorting home decorator -- the ID object is not
         * comparable.
         */
        // home = new SortingHome(ctx, home);
        home = new ValidatingHome(home, new SubscriberSubscriptionHistoryValidator());
        
        // Sorting home by timestamp
       //Commenting this as this is resulting into the issue of A column has been specified more than once in the order by list (x:orderBy(timestamp_ DESC))
        //The call to the home from the support class are also adding this predicate. So considering that the caller will
        //Add this predicate
        //home = new WhereHome(ctx, home, (new Order()).add(SubscriberSubscriptionHistoryXInfo.TIMESTAMP_, false));
        
        ctx.put(SubscriberSubscriptionHistoryHome.class, home);
        
        return home;
    }

}
