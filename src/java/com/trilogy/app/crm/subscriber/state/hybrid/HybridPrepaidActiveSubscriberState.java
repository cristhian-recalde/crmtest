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
 * Handles processing of the subscriber while in the Active state.
 *
 * @author joe.chen@redknee.com
 */
public class HybridPrepaidActiveSubscriberState extends AbstractSubscriberState
{

    /**
     * Returns an instance of <code>HybridPrepaidActiveSubscriberState</code>.
     *
     * @return An instance of <code>HybridPrepaidActiveSubscriberState</code>.
     */
    public static HybridPrepaidActiveSubscriberState instance()
    {
        if (instance == null)
        {
            instance = new HybridPrepaidActiveSubscriberState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>HybridPrepaidActiveSubscriberState</code>.
     */
    protected HybridPrepaidActiveSubscriberState()
    {
        super(SubscriberStateEnum.ACTIVE);
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
        // account state change, manual
        states.add(SubscriberStateEnum.SUSPENDED);
        // manual
        states.add(SubscriberStateEnum.INACTIVE);
        // same state
        states.add(SubscriberStateEnum.ACTIVE);

        allowedStates = Collections.unmodifiableSet(states);

        allowedManualStates = Collections.unmodifiableSet(states);
    }

    /**
     * Singleton instance.
     */
    private static HybridPrepaidActiveSubscriberState instance;
} // class
