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
package com.trilogy.app.crm.home.accountmanager;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.account.AccountManager;
import com.trilogy.app.crm.support.AccountManagerHistorySupport;


/**
 * Logs all AccountManager modifications into AccountManagerHistory
 * 
 * @author ltang
 * 
 */
public class AccountManagerHistoryLogHome extends HomeProxy
{

    public AccountManagerHistoryLogHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }


    @Override
    public Object create(Context ctx, Object obj) throws HomeException
    {
        AccountManagerHistorySupport.createAccountManagerHistory(ctx, (AccountManager) obj, this);
        return super.create(ctx, obj);
    }


    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        ((AccountManager) obj).setLastModified(new Date());

        AccountManagerHistorySupport.createAccountManagerHistory(ctx, (AccountManager) obj, this);
        return super.store(ctx, obj);
    }


    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        AccountManagerHistorySupport.removeAllAccountManagerHistory(ctx, (AccountManager) obj, this);
        super.remove(ctx, obj);
    }
}
