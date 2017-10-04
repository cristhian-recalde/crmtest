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
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.subscriber.state.AbstractSubscriberState;


/**
 * Handles processing of the subscriber while in the Locked state.
 *
 * @author gary.anderson@redknee.com
 */
public class PrepaidLockedSubscriberState extends AbstractSubscriberState
{

    /**
     * Returns an instance of <code>PrepaidLockedSubscriberState</code>.
     *
     * @return An instance of <code>PrepaidLockedSubscriberState</code>.
     */
    public static PrepaidLockedSubscriberState instance()
    {
        if (instance == null)
        {
            instance = new PrepaidLockedSubscriberState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>PrepaidLockedSubscriberState</code>.
     */
    protected PrepaidLockedSubscriberState()
    {
        super(SubscriberStateEnum.LOCKED);
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
        Collection<SubscriberStateEnum> result = new HashSet(allowedManualStates);

        if (oldStateOwner instanceof Subscriber)
        {
            Account account = null;
            try
            {
                account = ((Subscriber) oldStateOwner).getAccount(ctx);
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Failed to fetch account by subscriber.",e);
            }

            if (account==null || AccountStateEnum.SUSPENDED.equals(account.getState()))
            {
                result.remove(SubscriberStateEnum.ACTIVE);
            }
        }
        
        return result;
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
        // account state change, manual
        states.add(SubscriberStateEnum.ACTIVE);
        // same state
        states.add(SubscriberStateEnum.LOCKED);
        // de-active 
        states.add(SubscriberStateEnum.INACTIVE);
        allowedStates = Collections.unmodifiableSet(states);

        states = new HashSet<SubscriberStateEnum>();
        states.add(SubscriberStateEnum.ACTIVE);
        // same state
        states.add(SubscriberStateEnum.LOCKED);
        states.add(SubscriberStateEnum.INACTIVE);
        allowedManualStates = Collections.unmodifiableSet(states);
    }

    /**
     * Singleton instance.
     */
    private static PrepaidLockedSubscriberState instance;
} // class
