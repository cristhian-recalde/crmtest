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
package com.trilogy.app.crm.bas.recharge;

import java.util.Collection;
import java.util.Map;

import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * @author sbanerjee
 *
 */
public class PrepaidRetryRecurRechargeVisitor 
    extends RetryRecurRechargeVisitor
        implements Visitor
{
    /**
     * @param ctx
     * @param subscriber
     * @return
     */
    @Override
    protected Map<Long, Map<Long, SubscriberAuxiliaryService>> getSubscriberSuspendedAuxiliaryServices(
            final Context ctx, final Subscriber subscriber)
    {
        return subscriber.getSuspendedAuxServices(ctx);
    }


    /**
     * @param ctx
     * @param subscriber
     * @return
     */
    protected Collection<BundleFee> getSubscriberSuspendedBundles(final Context ctx,
            final Subscriber subscriber)
    {
        return subscriber
                .getSuspendedBundles(ctx).values();
    }


    /**
     * @param ctx
     * @param subscriber
     * @return
     */
    protected Map<ServiceFee2ID, ServiceFee2> getSubscriberSuspendedServices(final Context ctx,
            final Subscriber subscriber)
    {
        return subscriber.getSuspendedServices(ctx);
    }


    /**
     * @param ctx
     * @param subscriber
     * @return
     */
    protected Collection<ServicePackage> getSubscriberSuspendedPackages(final Context ctx,
            final Subscriber subscriber)
    {
        return subscriber.getSuspendedPackages(ctx).values();
    }
}