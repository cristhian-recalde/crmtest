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
 * Account in-collection state.
 *
 * @author joe.chen@redknee.com
 */
public class AccountInCollectionState extends AccountState
{

    /**
     * Returns an instance of <code>AccountInCollectionState</code>.
     *
     * @return An instance of <code>AccountInCollectionState</code>.
     */
    public static AccountInCollectionState instance()
    {
        if (instance == null)
        {
            instance = new AccountInCollectionState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>AccountInCollectionState</code>.
     */
    protected AccountInCollectionState()
    {
        super(AccountStateEnum.IN_COLLECTION);
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

        // manual (responsible), account parent (non-responsible)
        states.add(AccountStateEnum.ACTIVE);
        /*
         * manual (both), cron task (responsible), account parent (non-responsible)
         */
        states.add(AccountStateEnum.INACTIVE);
        // same state
        states.add(AccountStateEnum.IN_COLLECTION);

        allowedStates = Collections.unmodifiableSet(states);

        states = new HashSet<AccountStateEnum>();
        states.add(AccountStateEnum.INACTIVE);
        // same state
        states.add(AccountStateEnum.IN_COLLECTION);

        allowedManualStates = Collections.unmodifiableSet(states);

        states = new HashSet<AccountStateEnum>(states);
        states.add(AccountStateEnum.ACTIVE);
        allowedResponsibleManualStates = Collections.unmodifiableSet(states);
    }

    /**
     * Singleton instance.
     */
    private static AccountInCollectionState instance;

}
