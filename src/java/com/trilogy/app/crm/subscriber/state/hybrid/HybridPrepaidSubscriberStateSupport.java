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
package com.trilogy.app.crm.subscriber.state.hybrid;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;

import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.state.AbstractEnumStateAware;
import com.trilogy.app.crm.state.EnumState;


/**
 * Provides a number of utility functions for use with subscriber states.
 *
 * @author gary.anderson@redknee.com
 */
public final class HybridPrepaidSubscriberStateSupport extends AbstractEnumStateAware
{

    /**
     * Create a new instance of <code>HybridPrepaidSubscriberStateSupport</code>.
     */
    protected HybridPrepaidSubscriberStateSupport()
    {
        // empty
    }


    /**
     * Returns an instance of <code>HybridPrepaidSubscriberStateSupport</code>.
     *
     * @return An instance of <code>HybridPrepaidSubscriberStateSupport</code>.
     */
    public static HybridPrepaidSubscriberStateSupport instance()
    {
        if (instance == null)
        {
            instance = new HybridPrepaidSubscriberStateSupport();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected EnumCollection getEnumCollection()
    {
        return SubscriberStateEnum.COLLECTION;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public EnumState getState(final Context context, final Enum type)
    {
        if (type == null)
        {
            return HybridPrepaidNullSubscriberState.instance();
        }

        return findStateInstance(type, HYBRID_SUBSCRIBER_STATES);
    }

    /**
     * Valid states for hybrid subscribers.
     */
    private static final Map HYBRID_SUBSCRIBER_STATES = new HashMap();

    /*
     * Java refresh course: the following is a class initialization block, which gets
     * executed once when the class is loaded.
     */
    {
        HYBRID_SUBSCRIBER_STATES.put(SubscriberStateEnum.PENDING, HybridPrepaidPendingSubscriberState.instance());
        HYBRID_SUBSCRIBER_STATES.put(SubscriberStateEnum.ACTIVE, HybridPrepaidActiveSubscriberState.instance());
        HYBRID_SUBSCRIBER_STATES.put(SubscriberStateEnum.SUSPENDED, HybridPrepaidSuspendedSubscriberState.instance());
        HYBRID_SUBSCRIBER_STATES.put(SubscriberStateEnum.INACTIVE, HybridPrepaidInactiveSubscriberState.instance());
    }

    /**
     * Singleton instance.
     */
    private static HybridPrepaidSubscriberStateSupport instance;
}
