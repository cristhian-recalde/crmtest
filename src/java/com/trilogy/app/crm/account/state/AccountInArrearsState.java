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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.BlackListConfig;
import com.trilogy.app.crm.bean.BlackTypeEnum;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.blacklist.BlackListSupport;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * Account in-arrears state.
 *
 * @author joe.chen@redknee.com
 */
public class AccountInArrearsState extends AccountState
{

    /**
     * Returns an instance of <code>AccountInArrearsState</code>.
     *
     * @return An instance of <code>AccountInArrearsState</code>.
     */
    public static AccountInArrearsState instance()
    {
        if (instance == null)
        {
            instance = new AccountInArrearsState();
        }
        return instance;
    }


    /**
     * Create a new instance of <code>AccountInArrearsState</code>.
     */
    protected AccountInArrearsState()
    {
        super(AccountStateEnum.IN_ARREARS);
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
    private static AccountInArrearsState instance;

    /**
     * States allowed to transition into.
     */
    private static Set<AccountStateEnum> allowedStates;

    {
        final Set<AccountStateEnum> states = new HashSet<AccountStateEnum>();

        /*
         * manual (responsible), dunning payment (responsible), account parent
         * (non-responsible)
         */
        states.add(AccountStateEnum.ACTIVE);
        // manual (both), account parent (non-responsible)
        states.add(AccountStateEnum.SUSPENDED);
        // dunning (responsible), account parent (non-responsible)
        states.add(AccountStateEnum.NON_PAYMENT_WARN);
        // dunning (responsible), account parent (non-responsible)
        states.add(AccountStateEnum.NON_PAYMENT_SUSPENDED);
        // manual (responsible), account parent (non-responsible)
        states.add(AccountStateEnum.IN_COLLECTION);
        // manual (responsible), account parent (non-responsible)
        states.add(AccountStateEnum.PROMISE_TO_PAY);
        // manual (both), account parent (non-responsible)
        states.add(AccountStateEnum.INACTIVE);

        // same state
        states.add(AccountStateEnum.IN_ARREARS);

        allowedStates = Collections.unmodifiableSet(states);
    }


    /**
     * {@inheritDoc}
     */
    public Collection<AccountStateEnum> getStatesPermittedForManualTransition(final Context ctx, final StateAware oldStateOwner)
    {
        final List<AccountStateEnum> states = new ArrayList<AccountStateEnum>();
        states.add(AccountStateEnum.INACTIVE);
        states.add(AccountStateEnum.SUSPENDED);
        // same state
        states.add(AccountStateEnum.IN_ARREARS);

        final Account oldAccount = (Account) oldStateOwner;
        if (oldAccount.isResponsible())
        {
            if (SystemSupport.supportsInCollection(ctx))
            {
                states.add(AccountStateEnum.IN_COLLECTION);
            }
            states.add(AccountStateEnum.PROMISE_TO_PAY);

            try
            {
                /*
                 * search for the service access rules defined in BlackListConfig if we
                 * allow gray-listed number to be re-activated.
                 */
                final BlackListConfig config = BlackListSupport.getGreyListConfig(ctx);
                if (isInArrearReactivationAllowed(ctx, config, oldAccount))
                {
                    states.add(AccountStateEnum.ACTIVE);
                }
            }
            catch (final HomeException e)
            {
                new MajorLogMsg(this, "No blacklist home", e).log(ctx);
            }
        }
        return states;
    }


    /**
     * Determines whether the account is allowed to be reactivated from in-arrears state.
     *
     * @param ctx
     *            The operating context.
     * @param config
     *            Blacklist configuration.
     * @param realAccount
     *            Account to be reactivated.
     * @return Returns <code>true</code> if the account is allowed to be reactivated
     *         from in-arrears state.
     * @throws HomeException
     *             Thrown if there are problems determining the black list status.
     */
    private boolean isInArrearReactivationAllowed(final Context ctx, final BlackListConfig config,
        final Account realAccount) throws HomeException
    {
        boolean accountIdsGrayListed = false;
        Home accountIdHome = realAccount.identifications(ctx);
        Iterator i = accountIdHome.selectAll(ctx).iterator();
        while(i.hasNext())
        {
            AccountIdentification ai = (AccountIdentification)i.next();
            accountIdsGrayListed = accountIdsGrayListed || BlackTypeEnum.GRAY.equals(ai.getIsIdListed(ctx));
        }

        return accountIdsGrayListed && config != null && config.getExistingAccountReactivation();
    }

}
