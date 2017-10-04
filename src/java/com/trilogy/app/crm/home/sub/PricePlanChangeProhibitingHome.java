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
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSupport;


/**
 * Provides a way of prohibiting a Home.store() if the Subscriber's price plan
 * related information has changed while the Subscriber is in the queue to have
 * their price plan updated automatically.  This decorator provides a context
 * switch, ENABLED, for enabling (default) or disabling processing.
 *
 * @author gary.anderson@redknee.com
 */
public class PricePlanChangeProhibitingHome extends HomeProxy
{
    /**
     * This key is used to explicitly enable or disable this home.  The
     * automated task that updates Subscribers will need to disable this (so
     * that it does not conflict with itself).
     */
    public final static String ENABLED =
        PricePlanChangeProhibitingHome.class.getName() + ".ACTIVATION";


    /**
     * Creates a new PricePlanChangeProhibitingHome.
     *
     * @param delegate The Home to which we delegate.
     */
    public PricePlanChangeProhibitingHome(final Home delegate)
    {
        super(delegate);
    }

    // INHERIT
    public Object store(Context ctx,final Object object)
        throws HomeException
    {
        if (ctx.getBoolean(ENABLED, true))
        {
            ensureNoConflictingPricePlanChanges(ctx,(Subscriber)object);
        }

        return super.store(ctx,object);
    }

    /**
     * Ensures that no conflicting changes have been made to the Subscriber's
     * price plan information while there are queued changes in progress.
     *
     * @param subscriber The Subscriber to check.
     *
     * @exception HomeException Thrown if a conflict is detected.
     */
    private void ensureNoConflictingPricePlanChanges(Context ctx,final Subscriber subscriber)
        throws HomeException
    {
        if (!PricePlanSupport.isSubscriberPricePlanUpdating(ctx, subscriber))
        {
            return;
        }

        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        if (subscriber.getPricePlan() != oldSubscriber.getPricePlan())
        {
            throw new HomeException(
                "Update to subscriber's price plan conflicts with Price Plan Version update currently in progress.");
        }

        if (subscriber.getPricePlanVersion() != oldSubscriber.getPricePlanVersion())
        {
            throw new HomeException(
                "Update to subscriber's price plan version conflicts with Price Plan Version update currently in progress.");
        }

        if (!subscriber.getServices().equals(oldSubscriber.getServices()))
        {
            throw new HomeException(
                "Update to subscriber's services information conflicts with Price Plan Version update currently in progress.");
        }

        // TODO 2007-04-19 add check so that the price plan bundles selection cannot be changed as well 
    }
} // class
