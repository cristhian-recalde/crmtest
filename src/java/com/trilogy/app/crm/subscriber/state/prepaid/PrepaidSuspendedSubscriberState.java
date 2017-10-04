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
package com.trilogy.app.crm.subscriber.state.prepaid;

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
 * @author gary.anderson@redknee.com
 */
public class PrepaidSuspendedSubscriberState extends AbstractSubscriberState
{

    /**
     * Returns an instance of <code>PrepaidSuspendedSubscriberState</code>.
     *
     * @return An instance of <code>PrepaidSuspendedSubscriberState</code>.
     */
    public static PrepaidSuspendedSubscriberState instance()
    {
        if (instance == null)
        {
            instance = new PrepaidSuspendedSubscriberState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>PrepaidSuspendedSubscriberState</code>.
     */
    protected PrepaidSuspendedSubscriberState()
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
        Set<SubscriberStateEnum> states = new HashSet<SubscriberStateEnum>();
        // ER447
        states.add(SubscriberStateEnum.ACTIVE);
        // Account suspended
        states.add(SubscriberStateEnum.LOCKED);
        // Cron task
        states.add(SubscriberStateEnum.EXPIRED);
        // manual
        states.add(SubscriberStateEnum.INACTIVE);
        // same state
        states.add(SubscriberStateEnum.SUSPENDED);
        allowedStates = Collections.unmodifiableSet(states);

        states = new HashSet<SubscriberStateEnum>();
        states.add(SubscriberStateEnum.INACTIVE);
        // same state
        states.add(SubscriberStateEnum.SUSPENDED);
        allowedManualStates = Collections.unmodifiableSet(states);
    }

    /**
     * Singleton instance.
     */
    private static PrepaidSuspendedSubscriberState instance;
} // class
