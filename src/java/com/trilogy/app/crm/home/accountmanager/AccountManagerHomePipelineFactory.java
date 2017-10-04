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

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.account.AccountManager;
import com.trilogy.app.crm.bean.account.AccountManagerHistory;
import com.trilogy.app.crm.bean.account.AccountManagerHistoryHome;
import com.trilogy.app.crm.bean.account.AccountManagerHistoryTransientHome;
import com.trilogy.app.crm.bean.account.AccountManagerHome;
import com.trilogy.app.crm.bean.account.AccountManagerTransientHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;

/**
 * Pipeline factory for Account Manager.
 * 
 * @author ltang
 *
 */
public class AccountManagerHomePipelineFactory implements PipelineFactory
{
    public AccountManagerHomePipelineFactory() {}

    public Home createPipeline(Context ctx, Context serverCtx)
    throws RemoteException, HomeException, IOException
    {
        return createPipeline(ctx, serverCtx, false);
    }

    public Home createPipeline(Context ctx, Context serverCtx, boolean installTransient)
    throws RemoteException, HomeException, IOException
    {
        /*
         *  AccountManagerHistory
         */
        Home accountManagerHistoryHome;
        if (installTransient)
        {
            accountManagerHistoryHome = new AccountManagerHistoryTransientHome(ctx);
        }
        else
        {
            accountManagerHistoryHome = StorageSupportHelper.get(ctx).createHome(ctx, AccountManagerHistory.class, "ACCOUNTMANAGERHISTORY");
        }
        accountManagerHistoryHome = new IdentifierSettingHome(
                ctx,
                accountManagerHistoryHome,
                IdentifierEnum.ACCOUNTMANAGERHISTORY_ID, null);

        IdentifierSequenceSupportHelper.get(ctx).ensureNextIdIsLargeEnough(ctx, IdentifierEnum.ACCOUNTMANAGERHISTORY_ID, accountManagerHistoryHome);

        accountManagerHistoryHome = new SortingHome(accountManagerHistoryHome);

        ctx.put(AccountManagerHistoryHome.class, accountManagerHistoryHome);

        /*
         * AccountManager
         */
        Home accountManagerHome;
        if (installTransient)
        {
            accountManagerHome = new AccountManagerTransientHome(ctx);
        }
        else
        {
            accountManagerHome = StorageSupportHelper.get(ctx).createHome(ctx, AccountManager.class, "ACCOUNTMANAGER");
        }
        accountManagerHome = new AccountManagerHistoryLogHome(ctx, accountManagerHome);
        accountManagerHome = new AccountManagerNotInUseHome(ctx, accountManagerHome);

        accountManagerHome = new SortingHome(accountManagerHome);

        accountManagerHome = new ValidatingHome(new AccountManagerIdValidator(), accountManagerHome);
        
        accountManagerHome = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx,
                accountManagerHome, AccountManager.class);

        ctx.put(AccountManagerHome.class, accountManagerHome);

        return null;
    }
}
