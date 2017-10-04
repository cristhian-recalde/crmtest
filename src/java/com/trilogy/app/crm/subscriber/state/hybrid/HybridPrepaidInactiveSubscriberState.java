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
 * Handles processing of the subscriber while in the Inactive state.
 *
 * @author gary.anderson@redknee.com
 */
public class HybridPrepaidInactiveSubscriberState extends AbstractSubscriberState
{

    /**
     * Returns an instance of <code>HybridPrepaidInactiveSubscriberState</code>.
     *
     * @return An instance of <code>HybridPrepaidInactiveSubscriberState</code>.
     */
    public static HybridPrepaidInactiveSubscriberState instance()
    {
        if (instance == null)
        {
            instance = new HybridPrepaidInactiveSubscriberState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>HybridPrepaidInactiveSubscriberState</code>.
     */
    protected HybridPrepaidInactiveSubscriberState()
    {
        super(SubscriberStateEnum.INACTIVE);
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
        // same state
        states.add(SubscriberStateEnum.INACTIVE);

        allowedStates = Collections.unmodifiableSet(states);

        allowedManualStates = Collections.unmodifiableSet(states);
    }

    /**
     * Singleton instance.
     */
    private static HybridPrepaidInactiveSubscriberState instance;
} // class
