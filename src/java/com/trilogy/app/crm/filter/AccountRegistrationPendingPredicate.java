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
package com.trilogy.app.crm.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.RegistrationStatusEnum;


/**
 * Returns true if the given account (or account referenced by BANAware object)
 * satisfies any criteria for account registration and is not already registered.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class AccountRegistrationPendingPredicate extends AccountRegistrationEnabledPredicate
{
    public AccountRegistrationPendingPredicate()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        Account account = getAccount(ctx, obj);
        if (account != null)
        {
            int registrationStatus = account.getRegistrationStatus();
            if (registrationStatus != RegistrationStatusEnum.NOT_APPLICABLE_INDEX
                    && registrationStatus != RegistrationStatusEnum.REGISTERED_INDEX)
            {
                return super.f(ctx, account);
            }
        }
        return false;
    }
}
