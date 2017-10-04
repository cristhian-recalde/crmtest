/*
 * Created on Jun 9, 2006
 *
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

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.account.AccountManager;
import com.trilogy.app.crm.bean.account.AccountManagerHistoryHome;
import com.trilogy.app.crm.bean.account.AccountManagerHistoryXInfo;
import com.trilogy.app.crm.bean.account.AccountManagerHome;
import com.trilogy.app.crm.home.account.AccountAcctMgrValidator;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;


/**
 * Unit tests for Account Manager
 * 
 * @author ltang
 */
public class TestAccountManager extends ContextAwareTestCase
{

    public TestAccountManager(final String name)
    {
        super(name);
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     * 
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by the Redknee Xtest code, which provides the application's operating context.
     * 
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestAccountManager.class);

        return suite;
    }


    //INHERIT
    @Override
    public void setUp()
    {
        super.setUp();

        setupAccountsSubscribers();

        SysFeatureCfg config = getSysFeatureCfg(getContext());
        getContext().put(SysFeatureCfg.class, config);

        setupAccountManagerHome(getContext(), true);

        setupAccountManager(getContext(), ACCOUNT_MANAGER_ID_1, ACCOUNT_MANAGER_NAME_1, ACCOUNT_MANAGER_SPID_1);
        setupAccountManager(getContext(), ACCOUNT_MANAGER_ID_2, ACCOUNT_MANAGER_NAME_2, ACCOUNT_MANAGER_SPID_2);
    }


    // INHERIT
    @Override
    public void tearDown()
    {
        //tear down here
        deleteAccountsSubscribers();

        super.tearDown();
    }


    public void testCreateAccountManager()
    {
        Context ctx = getContext();

        try
        {
            Home accountManagerHome = (Home) ctx.get(AccountManagerHome.class);
            Home accountManagerHistoryHome = (Home) ctx.get(AccountManagerHistoryHome.class);

            assertEquals("Create AccountManager", accountManagerHome.selectAll().size(), 2);
            assertEquals("Create AccountManagerHistory", accountManagerHistoryHome.selectAll().size(), 2);
        }
        catch (Exception e)
        {
            fail("Failed to validate number of records created in AccountManager and AccountManagerHistory homes "
                    + e.getMessage());
        }
    }


    public void testEditAccountManager()
    {
        Context ctx = getContext();

        try
        {
            Home accountManagerHome = (Home) ctx.get(AccountManagerHome.class);
            AccountManager accountManager = HomeSupportHelper.get(ctx).findBean(ctx, AccountManager.class, ACCOUNT_MANAGER_ID_1);

            assertNotNull("AccountManager is null", accountManager);

            accountManager.setName("edited");
            accountManagerHome.store(accountManager);

            accountManager = HomeSupportHelper.get(ctx).findBean(ctx, AccountManager.class, ACCOUNT_MANAGER_ID_1);

            assertNotNull("AccountManager is null", accountManager);
            assertEquals("Update to AccountManager name", "edited", accountManager.getName());

            Home accountManagerHistoryHome = (Home) ctx.get(AccountManagerHistoryHome.class);
            Collection col = accountManagerHistoryHome.select(new EQ(AccountManagerHistoryXInfo.ACCOUNT_MGR_ID,
                    ACCOUNT_MANAGER_ID_1));
            assertEquals("AccountManagerHistory modification record log", 2, col.size()); // 1 history record for create, 1 history record for edit
        }
        catch (HomeException e)
        {
            fail("Failed to edit AccountManager " + e.getMessage());
        }
    }


    public void testAssignAndDeleteAccountManager()
    {
        Context ctx = getContext();
        Home accountHome = (Home) ctx.get(AccountHome.class);
        Account account = null;
        try
        {
            account = (Account) accountHome.find(new EQ(AccountXInfo.BAN, TestSetupAccountHierarchy.ACCOUNT1_BAN));

            assertNotNull("Account is null", account);

            account.setAccountMgr(ACCOUNT_MANAGER_ID_1);
            accountHome.store(account);

            assertEquals("Assign AccountManager to Account", ACCOUNT_MANAGER_ID_1, account.getAccountMgr());
        }
        catch (HomeException e)
        {
            fail("Failed to assign AccountManager to Account " + e.getMessage());
        }

        Home accountManagerHome = (Home) ctx.get(AccountManagerHome.class);
        AccountManager accountManager = null;
        try
        {
            accountManager = HomeSupportHelper.get(ctx).findBean(ctx, AccountManager.class, ACCOUNT_MANAGER_ID_1);

            assertNotNull("AccountManager is null", accountManager);

            accountManagerHome.remove(accountManager); // delete should fail since it's still in use
        }
        catch (HomeException e)
        {
            // ignore
        }

        try
        {
            accountManager = HomeSupportHelper.get(ctx).findBean(ctx, AccountManager.class, ACCOUNT_MANAGER_ID_1);

            assertNotNull("AccountManager is null", accountManager); // shouldn'tve been deleted since AccountManager is in use
        }
        catch (HomeException e)
        {
            // ignore
        }
    }


    public void testDeleteAccountManager()
    {
        Context ctx = getContext();

        Home accountManagerHome = (Home) ctx.get(AccountManagerHome.class);
        AccountManager accountManager = null;

        try
        {
            accountManager = HomeSupportHelper.get(ctx).findBean(ctx, AccountManager.class, ACCOUNT_MANAGER_ID_1);

            assertNotNull("Account manager is null", accountManager);

            accountManagerHome.remove(accountManager);

            Home accountManagerHistoryHome = (Home) ctx.get(AccountManagerHistoryHome.class);
            Collection col = accountManagerHistoryHome.select(new EQ(AccountManagerHistoryXInfo.ACCOUNT_MGR_ID,
                    ACCOUNT_MANAGER_ID_1));

            assertEquals("AccountManagerHistory not cleared on delete", 0, col.size()); // all history should've been deleted as well
        }
        catch (HomeException e)
        {
            fail("Failed to delete AccountManager " + e.getMessage());
        }
    }


    private SysFeatureCfg getSysFeatureCfg(Context ctx)
    {
        SysFeatureCfg config = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        if (config == null)
        {
            config = new SysFeatureCfg();
            config.setEnableAccountManagerDropdown(true);
        }

        return config;
    }


    private void setupAccountsSubscribers()
    {
        TestSetupAccountHierarchy.setup(getContext(), true);

        getContext().put(AccountHome.class,
                new ValidatingHome(AccountAcctMgrValidator.instance(), (Home) getContext().get(AccountHome.class)));
    }


    private void deleteAccountsSubscribers()
    {
        TestSetupAccountHierarchy.deleteAccountsSubscribers(getContext());
    }


    private void setupAccountManagerHome(Context ctx, boolean installTransient)
    {
        try
        {
            new AccountManagerHomePipelineFactory().createPipeline(ctx, null, installTransient);
        }
        catch (Exception e)
        {
            fail("Failed to set up AccountManagerHome " + e.getMessage());
        }
    }


    /** 
     * 
     */
    private static void setupAccountManager(Context ctx, String id, String name, int spid)
    {
        Home home = (Home) ctx.get(AccountManagerHome.class);
        AccountManager accountManager = new AccountManager();
        accountManager.setAccountMgrId(id);
        accountManager.setName(name);
        accountManager.setSpid(spid);

        try
        {
            home.create(ctx, accountManager);
        }
        catch (HomeException e)
        {
            fail("Failed to set up AccountManager " + e.getMessage());
        }
    }

    public static String ACCOUNT_MANAGER_ID_1 = "ACMGR1";
    public static String ACCOUNT_MANAGER_NAME_1 = "Account Manager 1";
    public static int ACCOUNT_MANAGER_SPID_1 = TestSetupAccountHierarchy.SPID_ID;

    public static String ACCOUNT_MANAGER_ID_2 = "ACMGR2";
    public static String ACCOUNT_MANAGER_NAME_2 = "Account Manager 2";
    public static int ACCOUNT_MANAGER_SPID_2 = -1;
}
