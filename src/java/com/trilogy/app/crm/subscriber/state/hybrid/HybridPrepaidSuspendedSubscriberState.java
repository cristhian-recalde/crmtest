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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.subscriber.state.AbstractSubscriberState;


/**
 * Handles processing of the subscriber while in the Suspended state.
 *
 * @author joe.chen@redknee.com
 */
public class HybridPrepaidSuspendedSubscriberState extends AbstractSubscriberState
{

    /**
     * Returns an instance of <code>HybridPrepaidSuspendedSubscriberState</code>.
     *
     * @return An instance of <code>HybridPrepaidSuspendedSubscriberState</code>.
     */
    public static HybridPrepaidSuspendedSubscriberState instance()
    {
        if (instance == null)
        {
            instance = new HybridPrepaidSuspendedSubscriberState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>HybridPrepaidSuspendedSubscriberState</code>.
     */
    protected HybridPrepaidSuspendedSubscriberState()
    {
        super(SubscriberStateEnum.SUSPENDED);
    }


    /**
     * {@inheritDoc}
     */
    public Collection<SubscriberStateEnum> getStatesPermittedForTransition(final Context ctx, final StateAware oldStateOwner)
    {
        return allowedStates;
    }


    /**
     * {@inheritDoc}
     */
    public Collection<SubscriberStateEnum> getStatesPermittedForManualTransition(final Context ctx, final StateAware oldStateOwner)
    {
        return allowedManualStates;
    }

    /**
     * States allowed to transition into.
     */
    private static Set<SubscriberStateEnum> allowedStates;

    /**
     * States allowed to transition into manually.
     */
    private static Set<SubscriberStateEnum> allowedManualStates;

    {
        final Set<SubscriberStateEnum> states = new HashSet<SubscriberStateEnum>();
        // cron task, manual
        states.add(SubscriberStateEnum.ACTIVE);
        // manual
        states.add(SubscriberStateEnum.INACTIVE);
        // same state
        states.add(SubscriberStateEnum.SUSPENDED);

        allowedStates = Collections.unmodifiableSet(states);

        allowedManualStates = Collections.unmodifiableSet(states);
    }

    /**
     * Singleton instance.
     */
    private static HybridPrepaidSuspendedSubscriberState instance;
} // class
