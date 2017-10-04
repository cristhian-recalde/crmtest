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
package com.trilogy.app.crm.home;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.*;


/**
 * Provide a home decorator to clear the Promise-To-Pay Date for an Account
 * if the Account is not in Promise-To-Pay state.
 *
 * @author jimmy.ng@redknee.com
 */
public class AccountPromiseToPayDateClearingHome
    extends HomeProxy
{
    /**
     * Creates a new AccountPromiseToPayDateClearingHome for the given delegate.
     *
     * @param context The operating context.
     * @param delegate The delegate to which we pass searches.
     */
    public AccountPromiseToPayDateClearingHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }

    /**
     * INHERIT
     */
    public Object create(Context ctx, Object obj)
        throws HomeException
    {
        final Account account = (Account) obj;
        
        if (account.getState() != AccountStateEnum.PROMISE_TO_PAY)
        {
            account.setPromiseToPayDate(null);
        }
        
        return super.create(ctx,account);
    }

    // INHERIT
    public Object store(Context ctx,final Object obj)
        throws HomeException
    {
        final Account account = (Account) obj;
        
        if (account.getState() != AccountStateEnum.PROMISE_TO_PAY)
        {
            account.setPromiseToPayDate(null);
        }
        
        return super.store(ctx,account);
    }
} // class
