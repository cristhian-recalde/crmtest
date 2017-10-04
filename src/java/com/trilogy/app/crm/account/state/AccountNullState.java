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
package com.trilogy.app.crm.account.state;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.state.StateAware;


/**
 * Account state null object.
 *
 * @author joe.chen@redknee.com
 */
public class AccountNullState extends AccountState
{

    /**
     * Returns an instance of <code>AccountNullState</code>.
     *
     * @return An instance of <code>AccountNullState</code>.
     */
    public static AccountNullState instance()
    {
        if (instance == null)
        {
            instance = new AccountNullState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>AccountNullState</code>.
     */
    protected AccountNullState()
    {
        super(null);
    }


    /**
     * {@inheritDoc}
     */
    public Collection<AccountStateEnum> getStatesPermittedForManualTransition(final Context ctx, final StateAware oldStateOwner)
    {
        return allowedStates;
    }


    /**
     * {@inheritDoc}
     */
    public Collection<AccountStateEnum> getStatesPermittedForTransition(final Context ctx, final StateAware oldStateOwner)
    {
        return allowedStates;
    }

    /**
     * Singleton instance.
     */
    private static AccountNullState instance;
    /**
     * States allowed to transition into.
     */
    private static Set<AccountStateEnum> allowedStates;
    {
        final Set<AccountStateEnum> states = new HashSet<AccountStateEnum>();

        // account creation
        states.add(AccountStateEnum.ACTIVE);
        allowedStates = Collections.unmodifiableSet(states);

    }
} // class
