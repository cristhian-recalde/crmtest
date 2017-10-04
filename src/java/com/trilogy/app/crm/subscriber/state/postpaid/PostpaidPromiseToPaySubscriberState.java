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
 * Handles processing of the subscriber while in the Locked state.
 *
 * @author gary.anderson@redknee.com
 */
public class PostpaidPromiseToPaySubscriberState extends AbstractSubscriberState
{

    /**
     * Returns an instance of <code>PostpaidPromiseToPaySubscriberState</code>.
     *
     * @return An instance of <code>PostpaidPromiseToPaySubscriberState</code>.
     */
    public static PostpaidPromiseToPaySubscriberState instance()
    {
        if (instance == null)
        {
            instance = new PostpaidPromiseToPaySubscriberState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>PostpaidPromiseToPaySubscriberState</code>.
     */
    protected PostpaidPromiseToPaySubscriberState()
    {
        super(SubscriberStateEnum.PROMISE_TO_PAY);
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
        return allowedNonIndividualStates;
    }


    /**
     * {@inheritDoc}
     */
    public Collection<SubscriberStateEnum> getStatesPermittedForManualTransition(final Context ctx, final StateAware oldStateOwner)
    {
        final Subscriber subscriber = (Subscriber) oldStateOwner;
        if (isIndividual(ctx, subscriber) && isResponsible(ctx, subscriber))
        {
            return allowedIndividualResponsibleManualStates;
        }
        return allowedNonIndividualManualStates;
    }

    /**
     * States allowed to transition into.
     */
    private static Set<SubscriberStateEnum> allowedNonIndividualStates;

    /**
     * States allowed to transition into for individual responsible subscriber.
     */
    private static Set<SubscriberStateEnum> allowedIndividualResponsibleStates;

    /**
     * States allowed to transition into manually.
     */
    private static Set<SubscriberStateEnum> allowedNonIndividualManualStates;

    /**
     * States allowed to transition into manually for individual responsible subscriber.
     */
    private static Set<SubscriberStateEnum> allowedIndividualResponsibleManualStates;
    {
        Set<SubscriberStateEnum> states = new HashSet<SubscriberStateEnum>();

        // account state change, cron task
        states.add(SubscriberStateEnum.ACTIVE);
        // account state change, manual
        states.add(SubscriberStateEnum.SUSPENDED);
        // dunning
        states.add(SubscriberStateEnum.NON_PAYMENT_WARN);
        // dunning
        states.add(SubscriberStateEnum.NON_PAYMENT_SUSPENDED);
        // dunning
        states.add(SubscriberStateEnum.IN_ARREARS);
        // account state change
        states.add(SubscriberStateEnum.IN_COLLECTION);
        // manual
        states.add(SubscriberStateEnum.INACTIVE);
        // same state
        states.add(SubscriberStateEnum.PROMISE_TO_PAY);
        allowedIndividualResponsibleStates = Collections.unmodifiableSet(states);
        allowedNonIndividualStates = Collections.unmodifiableSet(states);

        states = new HashSet<SubscriberStateEnum>();
        states.add(SubscriberStateEnum.INACTIVE);
        states.add(SubscriberStateEnum.PROMISE_TO_PAY);
        states.add(SubscriberStateEnum.SUSPENDED);
        allowedNonIndividualManualStates = Collections.unmodifiableSet(states);

        states = new HashSet<SubscriberStateEnum>(states);
        states.add(SubscriberStateEnum.ACTIVE);
        allowedIndividualResponsibleManualStates = Collections.unmodifiableSet(states);
    }

    /**
     * Singleton instance.
     */
    private static PostpaidPromiseToPaySubscriberState instance;
} // class
