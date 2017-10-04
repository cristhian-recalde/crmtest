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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.account.AccountManager;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * Verifies that an AccountManager is not in use on attempts to remove it from the system
 * 
 * @author ltang
 * 
 */
public class AccountManagerNotInUseHome extends HomeProxy
{

    public AccountManagerNotInUseHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }


    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        AccountManager accountMgr = (AccountManager) obj;
        AccountManager oldAccountMgr = HomeSupportHelper.get(ctx).findBean(ctx, AccountManager.class, accountMgr.getAccountMgrId());

        if (oldAccountMgr == null)
        {
            throw new HomeException("Failed to lookup Account Manager " + accountMgr.getAccountMgrId());
        }
        if (accountMgr.getSpid() != oldAccountMgr.getSpid())
        {
            if (isAccountManagerInUse(ctx, accountMgr.getAccountMgrId()))
            {
                throw new HomeException(
                        "Unable to update Service Provider of Account Manager [ID="
                                + accountMgr.getAccountMgrId()
                                + "].  Please ensure Account Manager is not in use prior to attempt to update Service Provider.");
            }
        }
        return super.store(ctx, obj);
    }


    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        AccountManager accountMgr = (AccountManager) obj;
        if (isAccountManagerInUse(ctx, accountMgr.getAccountMgrId()))
        {
            throw new HomeException("Unable to remove Account Manager [ID=" + accountMgr.getAccountMgrId()
                    + "].  Please ensure Account Manager is not in use prior to attempt to remove.");
        }
        super.remove(ctx, obj);
    }


    private boolean isAccountManagerInUse(final Context ctx, String accountManagerId) throws HomeException
    {
        return HomeSupportHelper.get(ctx).hasBeans(ctx, Account.class, new EQ(AccountXInfo.ACCOUNT_MGR, accountManagerId));
    }
}
