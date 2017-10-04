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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * Account suspended state.
 *
 * @author joe.chen@redknee.com
 */
public class AccountSuspendedState extends AccountState
{

    /**
     * Returns an instance of <code>AccountSuspendedState</code>.
     *
     * @return An instance of <code>AccountSuspendedState</code>.
     */
    public static AccountSuspendedState instance()
    {
        if (instance == null)
        {
            instance = new AccountSuspendedState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>AccountSuspendedState</code>.
     */
    protected AccountSuspendedState()
    {
        super(AccountStateEnum.SUSPENDED);
    }


    /**
     * {@inheritDoc}
     */
    public Collection<AccountStateEnum> getStatesPermittedForTransition(final Context ctx, final StateAware oldStateOwner)
    {
        return allowedStates;
    }


    /**
     * {@inheritDoc}
     */
    public Collection<AccountStateEnum> getStatesPermittedForManualTransition(final Context ctx, final StateAware oldStateOwner)
    {
        final List<AccountStateEnum> states = new ArrayList<AccountStateEnum>();
        states.add(AccountStateEnum.INACTIVE);

        final Account oldAccount = (Account) oldStateOwner;
        Account parentAccount = null;
        try
        {
            parentAccount = oldAccount.getParentAccount(ctx);
        }
        catch (final HomeException exception)
        {
            LogSupport.minor(ctx, this, "Cannot find parent of account " + oldAccount.getBAN(), exception);
        }

        if (validateParentAccountActivation(ctx, oldAccount, parentAccount))
        {
            states.add(AccountStateEnum.ACTIVE);
        }

        states.add(AccountStateEnum.SUSPENDED);

        return states;
    }

    /**
     * States allowed to transition into.
     */
    private static Set<AccountStateEnum> allowedStates;

    {
        final Set<AccountStateEnum> states = new HashSet<AccountStateEnum>();

        // manual (both), cron task (both), parent (non-responsible)
        states.add(AccountStateEnum.ACTIVE);
        // manual (responsible), account parent (non-responsible)
        states.add(AccountStateEnum.IN_COLLECTION);
        // manual (both), account parent (non-responsible)
        states.add(AccountStateEnum.INACTIVE);
        // same state
        states.add(AccountStateEnum.SUSPENDED);

        allowedStates = Collections.unmodifiableSet(states);
    }

    /**
     * Singleton instance.
     */
    private static AccountSuspendedState instance;

}
