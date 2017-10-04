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

import com.trilogy.app.crm.state.EnumStateAware;
import com.trilogy.app.crm.state.EnumStateTransitionSupport;


/**
 * Account state transition support.
 *
 * @author joe.chen@redknee.com
 */
public final class AccountStateTransitionSupport extends EnumStateTransitionSupport
{

    /**
     * Create a new instance of <code>AccountStateTransitionSupport</code>.
     */
    protected AccountStateTransitionSupport()
    {
        // empty
    }


    /**
     * Returns an instance of <code>AccountStateTransitionSupport</code>.
     *
     * @return An instance of <code>AccountStateTransitionSupport</code>.
     */
    public static AccountStateTransitionSupport instance()
    {
        if (instance == null)
        {
            instance = new AccountStateTransitionSupport();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public EnumStateAware getEnumStateSupport()
    {
        return this.enumStateSupport_;
    }

    /**
     * Enum state support.
     */
    private final EnumStateAware enumStateSupport_ = AccountStateSupport.instance();

    /**
     * Singleton instance.
     */
    private static AccountStateTransitionSupport instance;
} // class

