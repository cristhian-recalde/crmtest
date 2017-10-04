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
package com.trilogy.app.crm.subscriber.state;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.Enum;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.state.EnumState;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.state.StateChangeException;
import com.trilogy.app.crm.support.EnumStateSupportHelper;


/**
 * Provides the interface required of a SubscriberState processing object.
 *
 * @author joe.chen@redknee.com
 */
public abstract class AbstractSubscriberState implements EnumState
{

    /**
     * Map of account states to subscriber states.
     */
    private static final Map<AccountStateEnum, SubscriberStateEnum> ACCOUNT_TO_SUBSCRIBER_STATES = new HashMap<AccountStateEnum, SubscriberStateEnum>();

    /*
     * Java refresh course: the following is a class initialization block, which gets
     * executed once when the class is loaded.
     */
    static
    {
        ACCOUNT_TO_SUBSCRIBER_STATES.put(AccountStateEnum.ACTIVE, SubscriberStateEnum.ACTIVE);
        ACCOUNT_TO_SUBSCRIBER_STATES.put(AccountStateEnum.SUSPENDED, SubscriberStateEnum.SUSPENDED);
        ACCOUNT_TO_SUBSCRIBER_STATES.put(AccountStateEnum.INACTIVE, SubscriberStateEnum.INACTIVE);
        ACCOUNT_TO_SUBSCRIBER_STATES.put(AccountStateEnum.NON_PAYMENT_WARN, SubscriberStateEnum.NON_PAYMENT_WARN);
        ACCOUNT_TO_SUBSCRIBER_STATES.put(AccountStateEnum.NON_PAYMENT_SUSPENDED,
            SubscriberStateEnum.NON_PAYMENT_SUSPENDED);
        ACCOUNT_TO_SUBSCRIBER_STATES.put(AccountStateEnum.PROMISE_TO_PAY, SubscriberStateEnum.PROMISE_TO_PAY);
        ACCOUNT_TO_SUBSCRIBER_STATES.put(AccountStateEnum.IN_ARREARS, SubscriberStateEnum.IN_ARREARS);
        ACCOUNT_TO_SUBSCRIBER_STATES.put(AccountStateEnum.IN_COLLECTION, SubscriberStateEnum.IN_COLLECTION);
    }


    /**
     * Returns the subscriber state corresponding to the account state.
     *
     * @param account
     *            Account in question.
     * @return The subscriber state corresponding to the state of the account.
     */
    public static SubscriberStateEnum translateAccountState(final Account account)
    {
        return translateAccountState(account.getState());
    }


    /**
     * Returns the subscriber state corresponding to the account state.
     *
     * @param accountState
     *            Account state.
     * @return The subscriber state corresponding to the account state.
     */
    public static SubscriberStateEnum translateAccountState(final AccountStateEnum accountState)
    {
        final SubscriberStateEnum subscriberState = ACCOUNT_TO_SUBSCRIBER_STATES.get(accountState);
        if (subscriberState == null)
        {
            throw new IllegalArgumentException("Account state index not valid: " + accountState);
        }
        return subscriberState;
    }


    /**
     * Create a new instance of <code>AbstractSubscriberState</code>.
     *
     * @param representiveStateEnum
     *            Subscriber state enum.
     */
    protected AbstractSubscriberState(final Enum representiveStateEnum)
    {
        this.representativeState_ = representiveStateEnum;
    }


    /**
     * {@inheritDoc}
     */
    public void entering(final Context context, final StateAware curStateOwner) throws StateChangeException
    {
        // empty
    }


    /**
     * {@inheritDoc}
     */
    public void leaving(final Context context, final StateAware curStateOwner) throws StateChangeException
    {
        // empty
    }


    /**
     * {@inheritDoc}
     */
    public void transition(final Context context, final StateAware curStateOwner, final StateAware nextStateOwner)
    {
        // empty
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
     * Determines if the subscriber directly belongs to an individual account.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber.
     * @return Returns <code>true</code> if the subscriber directly belongs to an
     *         individual account.
     */
    protected boolean isIndividual(final Context ctx, final Subscriber sub)
    {
        final Account account = getAccount(ctx, sub);

        return account.isIndividual(ctx);
    }


    /**
     * Determines if the subscriber directly belongs to a responsible account.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber.
     * @return Returns <code>true</code> if the subscriber directly belongs to a
     *         responsible account.
     */
    protected boolean isResponsible(final Context ctx, final Subscriber sub)
    {
        final Account account = getAccount(ctx, sub);

        return account.isResponsible();
    }


    /**
     * Return the account directly owning the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber.
     * @return The account directly owning the subscriber.
     */
    protected Account getAccount(final Context ctx, final Subscriber sub)
    {
        Account account = null;
        try
        {
            account = sub.getAccount(ctx);
        }
        catch (final HomeException e)
        {
            final IllegalStateException exp = new IllegalStateException(
                "Subscriber's ban is not valid or system error " + sub.getBAN() + ", err=" + e);
            exp.initCause(e);
            throw exp;
        }
        return account;

    }

    /**
     * The representative state type of this state handler.
     */
    protected Enum representativeState_ = null;

}
