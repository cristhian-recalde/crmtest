/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.subscriber.state.prepaid;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.subscriber.state.AbstractSubscriberState;
import com.trilogy.framework.xhome.context.Context;

/**
 * Handles processing of the subscriber while in the Active state.
 * 
 * @author joe.chen@redknee.com
 */
public class PrepaidActiveSubscriberState extends AbstractSubscriberState
{

    /**
     * Returns an instance of <code>PrepaidActiveSubscriberState</code>.
     * 
     * @return An instance of <code>PrepaidActiveSubscriberState</code>.
     */
    public static PrepaidActiveSubscriberState instance()
    {
        if (instance == null)
        {
            instance = new PrepaidActiveSubscriberState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>PrepaidActiveSubscriberState</code>.
     */
    protected PrepaidActiveSubscriberState()
    {
        super(SubscriberStateEnum.ACTIVE);
    }


    /**
     * {@inheritDoc}
     */
    public Collection<SubscriberStateEnum> getStatesPermittedForTransition(final Context ctx,
            final StateAware oldStateOwner)
    {
        return allowedStates;
    }


    /**
     * {@inheritDoc}
     */
    public Collection<SubscriberStateEnum> getStatesPermittedForManualTransition(final Context ctx, final StateAware oldStateOwner)
    {
        if (oldStateOwner instanceof Subscriber)
        {
            // state transition may depend on Subscription Attributes
            Subscriber subscriber = (Subscriber) oldStateOwner;
            final SubscriptionType subscriptionType = subscriber.getSubscriptionType(ctx);
            if (null != subscriptionType && subscriptionType.isWallet())
            {
                return allowedManualStatesForWallets;
            }
            return allowedManualStates;
        }
        return allowedManualStates;
    }

    /**
     * States allowed to transition into.
     */
    private final static Set<SubscriberStateEnum> allowedStates;
    /**
     * States allowed to transition into manually for Service Subs.
     */
    private final static Set<SubscriberStateEnum> allowedManualStates;
    /**
     * States allowed to transition into manually for wallet Subs.
     */
    private final static Set<SubscriberStateEnum> allowedManualStatesForWallets;
    
    static
    {
        // build state transition sets
        Set<SubscriberStateEnum> states = new HashSet<SubscriberStateEnum>();
        {
            states.add(SubscriberStateEnum.SUSPENDED);
            states.add(SubscriberStateEnum.LOCKED);
            states.add(SubscriberStateEnum.EXPIRED);
            states.add(SubscriberStateEnum.INACTIVE);
            states.add(SubscriberStateEnum.ACTIVE);
            states.add(SubscriberStateEnum.DORMANT);
            allowedStates = Collections.unmodifiableSet(states);
        }
        {
            // manual transition states for Service Subs
            states = new HashSet<SubscriberStateEnum>();
            states.add(SubscriberStateEnum.INACTIVE);
            states.add(SubscriberStateEnum.LOCKED);
            states.add(SubscriberStateEnum.ACTIVE);
            allowedManualStates = Collections.unmodifiableSet(states);
        }
        {
            // manual transition states for wallet Subs
            states = new HashSet<SubscriberStateEnum>();
            states.add(SubscriberStateEnum.INACTIVE);
            states.add(SubscriberStateEnum.ACTIVE);
            states.add(SubscriberStateEnum.LOCKED);
            states.add(SubscriberStateEnum.DORMANT);
            allowedManualStatesForWallets = Collections.unmodifiableSet(states);
        }
    }
    /**
     * Singleton instance.
     */
    private static PrepaidActiveSubscriberState instance;
} // class
