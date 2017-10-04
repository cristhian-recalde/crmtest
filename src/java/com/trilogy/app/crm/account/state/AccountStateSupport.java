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

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;

import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.state.AbstractEnumStateAware;
import com.trilogy.app.crm.state.EnumState;


/**
 * This is basically a copy of SubscriberStateSupport, since Subscriber Provisioning home
 * needs to be designed and re-architected, so just copy for now!
 *
 * @author joe.chen@redknee.com
 */
public class AccountStateSupport extends AbstractEnumStateAware
{

    /**
     * Create a new instance of <code>AccountStateSupport</code>.
     */
    protected AccountStateSupport()
    {
        // empty
    }


    /**
     * Returns an instance of <code>AccountStateSupport</code>.
     *
     * @return An instance of <code>AccountStateSupport</code>.
     */
    public static AccountStateSupport instance()
    {
        if (instance == null)
        {
            instance = new AccountStateSupport();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected EnumCollection getEnumCollection()
    {
        return AccountStateEnum.COLLECTION;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public EnumState getState(final Context context, final Enum type)
    {
        if (type == null)
        {
            return AccountNullState.instance();
        }
        return findStateInstance(type, ACCOUNT_STATES);
    }

    /**
     * Account states.
     */
    private static final Map ACCOUNT_STATES = new HashMap();

    /*
     * Java refresh course: the following is a class initialization block, which gets
     * executed once when the class is loaded.
     */
    {
        ACCOUNT_STATES.put(AccountStateEnum.ACTIVE, AccountActiveState.instance());
        ACCOUNT_STATES.put(AccountStateEnum.SUSPENDED, AccountSuspendedState.instance());
        ACCOUNT_STATES.put(AccountStateEnum.NON_PAYMENT_WARN, AccountWarnedState.instance());
        ACCOUNT_STATES.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, AccountDunnedState.instance());
        ACCOUNT_STATES.put(AccountStateEnum.IN_ARREARS, AccountInArrearsState.instance());
        ACCOUNT_STATES.put(AccountStateEnum.IN_COLLECTION, AccountInCollectionState.instance());
        ACCOUNT_STATES.put(AccountStateEnum.PROMISE_TO_PAY, AccountPromiseToPayState.instance());
        ACCOUNT_STATES.put(AccountStateEnum.INACTIVE, AccountInactiveState.instance());
    }

    /**
     * Singleton instance.
     */
    private static AccountStateSupport instance;
}
