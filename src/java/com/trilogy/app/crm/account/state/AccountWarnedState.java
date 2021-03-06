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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.state.StateAware;


/**
 * Account warned state.
 *
 * @author joe.chen@redknee.com
 */
public class AccountWarnedState extends AccountState
{

    /**
     * Returns an instance of <code>AccountWarnedState</code>.
     *
     * @return An instance of <code>AccountWarnedState</code>.
     */
    public static AccountWarnedState instance()
    {
        if (instance == null)
        {
            instance = new AccountWarnedState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>AccountWarnedState</code>.
     */
    protected AccountWarnedState()
    {
        super(AccountStateEnum.NON_PAYMENT_WARN);
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
        final Account oldAccount = (Account) oldStateOwner;

        if (oldAccount.isResponsible())
        {
            return allowedResponsibleManualStates;
        }
        return allowedManualStates;
    }

    /**
     * States allowed to transition into.
     */
    private static Set<AccountStateEnum> allowedStates;

    /**
     * States allowed to manually transition into.
     */
    private static Set<AccountStateEnum> allowedManualStates;

    /**
     * States allowed to manually transition into when account is responsible.
     */
    private static Set<AccountStateEnum> allowedResponsibleManualStates;

    {
        Set<AccountStateEnum> states = new HashSet<AccountStateEnum>();

        // dunning payment (responsible), account parent (non-responsible)
        states.add(AccountStateEnum.ACTIVE);
        // manual (both), account parent (non-responsible)
        states.add(AccountStateEnum.SUSPENDED);
        // dunning (responsible), account parent (non-responsible)
        states.add(AccountStateEnum.NON_PAYMENT_SUSPENDED);
        // dunning (responsible), account parent (non-responsible)
        states.add(AccountStateEnum.IN_ARREARS);
        // manual (responsible), account parent (non-responsible)
        states.add(AccountStateEnum.PROMISE_TO_PAY);
        // manual (both), account parent (non-responsible)
        states.add(AccountStateEnum.INACTIVE);

        // same state
        states.add(AccountStateEnum.NON_PAYMENT_WARN);

        allowedStates = Collections.unmodifiableSet(states);

        states = new HashSet<AccountStateEnum>();
        states.add(AccountStateEnum.INACTIVE);
        states.add(AccountStateEnum.SUSPENDED);
        // same state
        states.add(AccountStateEnum.NON_PAYMENT_WARN);

        allowedManualStates = Collections.unmodifiableSet(states);

        states = new HashSet<AccountStateEnum>(states);
        states.add(AccountStateEnum.PROMISE_TO_PAY);
        allowedResponsibleManualStates = Collections.unmodifiableSet(states);
    }

    /**
     * Singleton instance.
     */
    private static AccountWarnedState instance;
}
