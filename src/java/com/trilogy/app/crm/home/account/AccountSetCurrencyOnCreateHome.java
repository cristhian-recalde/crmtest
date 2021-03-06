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
package com.trilogy.app.crm.home.account;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Account;

/**
 * Updates readonly field Currency based on SPID value.
 *
 * @author victor.stratan@redknee.com
 */
public class AccountSetCurrencyOnCreateHome extends HomeProxy
{

    public AccountSetCurrencyOnCreateHome(final Home delegate)
    {
        super(delegate);
    }

    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Account account = (Account) obj;
        account.setCurrencyFromServiceProvider(ctx, account.getSpid());

        return super.create(ctx, account);
    }
}
