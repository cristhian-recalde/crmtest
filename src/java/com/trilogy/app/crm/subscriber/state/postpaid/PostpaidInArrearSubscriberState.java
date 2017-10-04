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
package com.trilogy.app.crm.subscriber.state.postpaid;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.subscriber.state.AbstractSubscriberState;


/**
 * Handles processing of the subscriber while in the Expired state.
 *
 * @author gary.anderson@redknee.com
 */
public class PostpaidInArrearSubscriberState extends AbstractSubscriberState
{

    /**
     * Returns an instance of <code>PostpaidInArrearSubscriberState</code>.
     *
     * @return An instance of <code>PostpaidInArrearSubscriberState</code>.
     */
    public static PostpaidInArrearSubscriberState instance()
    {
        if (instance == null)
        {
            instance = new PostpaidInArrearSubscriberState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>PostpaidInArrearSubscriberState</code>.
     */
    protected PostpaidInArrearSubscriberState()
    {
        super(SubscriberStateEnum.IN_ARREARS);
    }


    /**
     * {@inheritDoc}
     */
    public Collection<SubscriberStateEnum> getStatesPermittedForTransition(final Context ctx, final StateAware oldStateOwner)
    {
        final Subscriber subscriber = (Subscriber) oldStateOwner;
        if (isIndividual(ctx, subscriber) && isResponsible(ctx, subscriber))
        {
            return allowedIndividualResponsibleStates;
        }
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
     * States allowed to transition into for individual responsible subscriber.
     */
    private static Set<SubscriberStateEnum> allowedIndividualResponsibleStates;

    /**
     * States allowed to transition into manually.
     */
    private static Set<SubscriberStateEnum> allowedManualStates;

    {
        Set<SubscriberStateEnum> states = new HashSet<SubscriberStateEnum>();

        // dunning
        states.add(SubscriberStateEnum.ACTIVE);
        // account state change
        states.add(SubscriberStateEnum.SUSPENDED);
        // dunning
        states.add(SubscriberStateEnum.NON_PAYMENT_WARN);
        // dunning
        states.add(SubscriberStateEnum.NON_PAYMENT_SUSPENDED);
        // account state change
        states.add(SubscriberStateEnum.IN_COLLECTION);
        // manual
        states.add(SubscriberStateEnum.INACTIVE);
        // same state
        states.add(SubscriberStateEnum.IN_ARREARS);

        allowedStates = Collections.unmodifiableSet(states);

        states = new HashSet<SubscriberStateEnum>(states);
        states.add(SubscriberStateEnum.PROMISE_TO_PAY);
        allowedIndividualResponsibleStates = Collections.unmodifiableSet(states);

        states = new HashSet<SubscriberStateEnum>();
        states.add(SubscriberStateEnum.INACTIVE);
        // same state
        states.add(SubscriberStateEnum.IN_ARREARS);
        allowedManualStates = Collections.unmodifiableSet(states);

    }

    /**
     * Singleton instance.
     */
    private static PostpaidInArrearSubscriberState instance;
} // class
