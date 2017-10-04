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

import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.Enum;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.BlackListConfig;
import com.trilogy.app.crm.bean.BlackTypeEnum;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.blacklist.BlackListSupport;
import com.trilogy.app.crm.state.EnumState;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.state.StateChangeException;
import com.trilogy.app.crm.support.EnumStateSupportHelper;

/**
 * Abstract base class for account states.
 *
 * @author joe.chen@redknee.com
 */
public abstract class AccountState implements EnumState
{

    /**
     * Create a new instance of <code>AccountState</code>.
     *
     * @param stateEnum
     *            State enum representing this state.
     */
    protected AccountState(final Enum stateEnum)
    {
        this.representativeState_ = stateEnum;
    }


    /**
     * {@inheritDoc}
     */
    public void entering(final Context context, final StateAware curStateOwner) throws StateChangeException
    {
        // no-op by default.
    }


    /**
     * {@inheritDoc}
     */
    public void leaving(final Context context, final StateAware curStateOwner) throws StateChangeException
    {
        // no-op by default.
    }


    /**
     * {@inheritDoc}
     */
    public void transition(final Context context, final StateAware curStateOwner, final StateAware nextStateOwner)
    {
        // no-op by default.
    }


    /**
     * {@inheritDoc}
     */
    public Enum getRepresentativeStateType()
    {
        return this.representativeState_;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isStateTransitionPermitted(final Context ctx, final StateAware oldStateOwner, final Enum type)
    {
        return EnumStateSupportHelper.get(ctx).isOneOfStates(type, getStatesPermittedForTransition(ctx, oldStateOwner));
    }


    /**
     * {@inheritDoc}
     */
    public boolean isManualStateTransitionPermitted(final Context ctx, final StateAware oldStateOwner, final Enum type)
    {
        return EnumStateSupportHelper.get(ctx).isOneOfStates(type, getStatesPermittedForManualTransition(ctx, oldStateOwner));
    }


    /**
     * Validates whether this is a valid black list activation.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            Account to be verified.
     * @return Returns <code>true</code> if this is a blacklist activation.
     * @throws HomeException
     *             Thrown if there are problems looking up the blacklist status.
     */
    protected boolean validBlackListActivation(final Context ctx, final Account account) throws HomeException
    {
        boolean valid = false;

        /*
         * search for the service access rules defined in BlackListConfig if we allow
         * black-listed number to be re-activated
         */

        boolean accountIdsBlackListed = false;
        final BlackListConfig config = BlackListSupport.getBlackListConfig(ctx);
        Home accountIdHome = account.identifications(ctx);
        Iterator i = accountIdHome.selectAll(ctx).iterator();
        while(i.hasNext())
        {
            AccountIdentification ai = (AccountIdentification)i.next();
            accountIdsBlackListed = accountIdsBlackListed || BlackTypeEnum.BLACK.equals(ai.getIsIdListed(ctx));
        }

        if(accountIdsBlackListed && config != null && config.getExistingAccountReactivation())
        {
            valid = true;
        }
        return valid;
    }


    /**
     * Determines if the parent account is in a state which the provided child account is
     * allowed to be activated.
     *
     * @param ctx
     *            The operating context.
     * @param oldAccount
     *            account to be activated.
     * @param parentAccount
     *            Parent account.
     * @return Returns <code>true</code> if the parent account is in a state which the
     *         provided child account is allowed to be activated.
     */
    protected boolean validateParentAccountActivation(final Context ctx, final Account oldAccount,
        final Account parentAccount)
    {
        boolean validToAdd = false;
        if (oldAccount.isResponsible())
        {
            if (parentAccount == null)
            {
                validToAdd = true;
            }
            else
            {
                if (!parentAccount.getState().equals(AccountStateEnum.INACTIVE))
                {
                    validToAdd = true;
                }
            }
        }
        else
        {
            // parent must not be null !!!
            if (parentAccount.getState().equals(AccountStateEnum.ACTIVE))
            {
                validToAdd = true;
            }
        }

        return validToAdd;
    }

    /**
     * The representative state type of this state handler.
     */
    protected Enum representativeState_ = null;
}
