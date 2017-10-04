/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.unit_test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryTransientHome;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.AccountXDBHome;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.BlackListHome;
import com.trilogy.app.crm.bean.BlackListTransientHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidTransientHome;
import com.trilogy.app.crm.bean.CreditCardInfoHome;
import com.trilogy.app.crm.bean.CreditCardInfoTransientHome;
import com.trilogy.app.crm.bean.CreditCardInfoXDBHome;
import com.trilogy.app.crm.bean.IdFormat;
import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.bean.IdentificationXInfo;
import com.trilogy.app.crm.bean.MsisdnEntryTypeEnum;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.NoteHome;
import com.trilogy.app.crm.bean.NoteTransientHome;
import com.trilogy.app.crm.bean.NoteXInfo;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCategoryHome;
import com.trilogy.app.crm.bean.SubscriberCategoryTransientHome;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TransactionMethodHome;
import com.trilogy.app.crm.bean.TransactionMethodTransientHome;
import com.trilogy.app.crm.bean.TransactionMethodXDBHome;
import com.trilogy.app.crm.bean.account.AccountAttachmentManagementConfig;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.bean.account.AccountIdentificationHome;
import com.trilogy.app.crm.bean.account.AccountIdentificationTransientHome;
import com.trilogy.app.crm.bean.account.AccountIdentificationXDBHome;
import com.trilogy.app.crm.bean.account.AccountRole;
import com.trilogy.app.crm.bean.account.AccountRoleHome;
import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.bean.account.ContactHome;
import com.trilogy.app.crm.bean.account.ContactTransientHome;
import com.trilogy.app.crm.bean.account.ContactXDBHome;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerHome;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerTransientHome;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerXDBHome;
import com.trilogy.app.crm.bean.account.SubscriptionClass;
import com.trilogy.app.crm.bean.account.SubscriptionClassHome;
import com.trilogy.app.crm.bean.account.SubscriptionClassRow;
import com.trilogy.app.crm.bean.account.SubscriptionClassXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeHome;
import com.trilogy.app.crm.bean.account.SubscriptionTypeXInfo;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.home.account.AccountLazyLoadedPropertyUpdateHome;
import com.trilogy.app.crm.numbermgn.MsisdnAlreadyAcquiredException;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.app.crm.unit_test.utils.TransientNoteIdentifierSettingHome;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;
import com.trilogy.app.crm.xhome.home.UserAgentHome;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.LastModifiedAwareHome;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This class sets up an Account Hierarchy used for general 
 * Unit tests.
 * 
 * It doesn't use the CRM Account or Subscriber pipelines.
 * By default the setup in this unit test uses Transient Homes. 
 * However, the setup for this test can take a boolean parameter, "installXDB", 
 * in the setup() method, that installs XBDHome for Account and Subscriber Pipelines, 
 * writing to temporary DB Tables.  This is useful only when running a test that triggers 
 * an action in a different Server (for example, Invoice Server).
 * 
 * **Changes for Mobile Money CRM 8.0**
 * For every Subscriber, there will be an Individual account registered to be its 
 * Subscriber Account.  The identifier BAN for the account is made up of the 
 * Subscriber Identifier + "-NR" (See the NR_SUB_ACCOUNT_SUFFIX constant at the bottom of the class).
 * See getIndividualAccountIdentifier() for easy way to retrieve this BAN. 
 * 
 * Test Hierarchy:
 * Account: ACCOUNT1_BAN (Responsible)
 *   -> Subscriber: SUB1_ID (Postpaid, Active)
 *   => Child Account: ACCOUNT2_BAN  (Non-responsible)
 *       --> Subscriber: SUB2_ID (Postpaid, Active)
 *       --> Subscriber: SUB4_ID (Postpaid, Inactive)
 *       --> Subscriber: SUB3_ID (Postpaid, Active)
 *       
 * Account: ACCOUNT4_BAN (Responsible)
 *   -> Subscriber: SUB5_ID (Postpaid, Inactive)
 *   -> Subscriber: SUB6_ID (Postpaid, Active) 
 *   
 * Account: ACCOUNT3_BAN (Responsible, Pooled)
 *   -> Subscriber: SUB7_ID (Postpaid, Active, Group Leader)
 *   -> Subscriber: SUB8_ID (Prepaid, Active) 
 *   
 * Account: ACCOUNT5_BAN (Responsible, Individual with multiple Subscriptions)
 *   -> Subscriber: SUB9_ID, SUB9_MSISDN (Prepaid, Active, Air Time Wallet)
 *   -> Subscriber: SUB10_ID, SUB9_MSISDN (Prepaid, Active, Mobile Wallet)
 *   -> Subscriber: SUB11_ID, SUB9_MSISDN (Prepaid, Active, Network Wallet)
 *   
 * Also setup in this class are:
 *   + CRM Service Provider (Spid)
 *   + Default Account Type
 *   + Default Bill Cycle
 *   + Mobile Numbers (MSISDN) and Mobile Number Homes
 *   + Default Currency
 *   + Note Homes
 *   
 * @author Angie Li
 * 
 */
public class TestSetupAccountHierarchy extends ContextAwareTestCase
{
    public TestSetupAccountHierarchy(String name)
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
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);
        final TestSuite suite = new TestSuite(TestSetupAccountHierarchy.class);
        return suite;
    }
    
    @Override
    public void setUp()
    {
        super.setUp();
        setup(getContext(), false);
        
    }
    
    //  INHERIT
    @Override
    public void tearDown()  
    {
        //tear down here
        completelyTearDown(getContext());
        
        super.tearDown();
    }
    
    /**
     * Tear down the environment that was set up for this test.
     * At each point of the teardown if an error occurs just log it and continue the tearDown.
     * @param ctx
     */
    public static void completelyTearDown(Context ctx)
    {
        deleteAccountsSubscribers(ctx);
        deleteBillCycle(ctx);
        deleteAccountType(ctx);
        deleteMsisdn(ctx);
        deleteCurrency(ctx);
        deleteNotes(ctx);
        //TestSetupMobileNumbers tearDown is not suitable.
        //TestSetupMobileNumbers.tearDown(ctx);
        
        deleteIdentification(ctx);
        deleteAccountRole(ctx);
        deleteSubscriptionClass(ctx);
        deleteSubscriptionType(ctx);
        
        //Allow the configuration setup to be run again.
        setAllowSetupInstallation(ctx, true);
    }

    /**
     * Return TRUE if the installation of the configuration setup is permitted.
     * @param ctx
     * @return
     */
    public static boolean getAllowSetupInstallation(Context ctx) 
    {
        return ctx.getBoolean(TestSetupAccountHierarchy.class, true);
    }
    
    /**
     * Sets the flag to allow configuration overwriting. 
     * @param ctx
     * @param b  If TRUE, allows configuration setup to run again (potentially overwriting old setup).
     */
    public static void setAllowSetupInstallation(Context ctx, boolean b) 
    {
        ctx.put(TestSetupAccountHierarchy.class, b);
    }

    /**
     * Install the CRM Spid Transient Home
     * @param ctx
     */
    public static void setupSpidHome(Context ctx) throws HomeException
    {
        //Create a transient home and install into the context
        Home spidHome = new TransientFieldResettingHome(ctx, new CRMSpidTransientHome(ctx));
        ctx.put(CRMSpidHome.class, spidHome);
    }
    
    /**
     * Setup SPID to be used when creating accounts and subs
     * @param ctx
     */
    public static void setupSpid(Context ctx) throws HomeException
    {
        Home spidHome = (Home) ctx.get(CRMSpidHome.class);

        if (ctx.getBoolean(SETUP_SPID_KEY, true))
        {
            if (spidHome.find(ctx, SPID_ID) == null)
            {
                CRMSpid spid = new CRMSpid();
                spid.setId(SPID_ID);
                spid.setName("Unit Test SPID");
                spid.setBillCycle(BILL_CYCLE_ID);
                try
                {
                    spidHome.create(spid);
                }
                catch (Exception e)
                {
                    logDebugMsg(ctx, "Error while creating test SPID. " + e.getMessage(), e);
                }

                //Prevent overwriting SPID creation.
                ctx.put(SETUP_SPID_KEY, false);
            }
        }
    }
    /**
     * Creates account in an account hierarchy.  By default, creates all default test Subscriber and Accounts.
     * @param ctx the operating context
     */
    public static void setup(Context ctx, final boolean installXDB)
    {
        setup(ctx, installXDB, true);
    }
    
    /**
     * Creates account in an account hierarchy
     * @param ctx the operating context
     * @param installAcctSubs if TRUE, indicates the default Accounts and Subscribers in the test Harness should be created.
     * Otherwise, no default accounts and subscribers will be created.
     */
    public static void setup(Context ctx, final boolean installXDB, final boolean installAcctSubs)
    {
        if (getAllowSetupInstallation(ctx))
        {
            try
            {
                //Install homes if this is a unit test run off-line
                if (!UnitTestSupport.isTestRunningInXTest(ctx))
                {
                    //Setup Homes
                    setupSpidHome(ctx);
                    UnitTestSupport.createHome(ctx, AccountCategory.class);
                    UnitTestSupport.createHome(ctx, BillCycle.class);
                    UnitTestSupport.createHome(ctx, Currency.class);
                    setupNoteHomes(ctx);
                    setupIdentificationHome(ctx);
                    setupBlackListHome(ctx);
                    setupAccountHome(ctx, installXDB);
                    setupSubscriberHome(ctx, installXDB);
                    setupTransactionMethodHome(ctx, installXDB);
                    TestSetupMobileNumbers.setup(ctx);
                    setupSubscriberCategoryHome(ctx);
                    UnitTestSupport.createHome(ctx, SubscriptionType.class);
                    UnitTestSupport.createHome(ctx, SubscriptionClass.class);
                    UnitTestSupport.createHome(ctx, AccountRole.class);
                    UnitTestSupport.createHome(ctx, SpidIdentificationGroups.class);
                    UnitTestSupport.createHome(ctx, TransferDispute.class);
                    
                    ctx.put(AccountAttachmentManagementConfig.class, new AccountAttachmentManagementConfig());
                }
                
                //Setup Data
                setupSpid(ctx);
                setupAccountType(ctx);
                setupBillCycle(ctx);
                setupCurrency(ctx);
                setupIdentification(ctx);
                setupSubscriptionType(ctx);
                setupSubscriptionClass(ctx);
                setupAccountRole(ctx);
                
                if (installAcctSubs)
                {
                    setupDefaultAccountsAndSubscribers(ctx);
                }
            }
            catch (Exception e)
            {
                /* Delete the accounts and subscribers that were set up before the failure.
                 * tearDown will not occur if the failure is in setUp.
                 */
                deleteAccountsSubscribers(ctx);
                fail("Failed to basic test setup. " + e.getMessage());
            }

            //Prevent setup from overwriting when it is called multiple times.
            setAllowSetupInstallation(ctx, false);
        }
        else
        {
            LogSupport.debug(ctx, TestSetupAccountHierarchy.class.getName(), 
                    "Skipping TestSetupAccountHierarchy.setup since it has already been run.");
        }
    }
    
    private static void setupSubscriberCategoryHome(Context ctx) 
    {
        ctx.put(SubscriberCategoryHome.class, new TransientFieldResettingHome(ctx, new SubscriberCategoryTransientHome(ctx)));
    }

    /**
     * Delete account and subscribers in the account hierarchy
     * @param ctx the operating context
     */
    public static void deleteAccountsSubscribers(Context ctx)
    {
        //Remove Accounts
        for(String ban : ALL_TEST_ACCOUNT_BANS)
        {
            deleteAccount(ctx, ban);
        }

        //Remove Subscribers
        for(String subIds : ALL_TEST_SUBS_IDS)
        {
            deleteSubscriber(ctx, subIds);
        }
    }
    
    /**
     * Tests if the Account Hierarchy (including Subs) was set up and 
     * persists in the system.
     */
    public void testSetup()
    {
        testSetup(getContext());
    }
    
    public static void testSetup(Context ctx)
    {
        try
        {
            Home spidHome = (Home)ctx.get(CRMSpidHome.class);
            assertNotNull(spidHome);
            assertNotNull(spidHome.find(ctx, Integer.valueOf(SPID_ID)));
            
            Home bcHome = (Home) ctx.get(BillCycleHome.class);
            assertNotNull(bcHome);
            BillCycle cycle = new BillCycle();
            cycle.setBillCycleID(BILL_CYCLE_ID);
            assertNotNull("Test Bill cycle doesn't exist in the system.", bcHome.find(cycle));
        }
        catch(Exception e)
        {
            fail("Failed Setup due to: " + e.getMessage());
        }
        assertNotNull("SubscriberCategoryHome is null in context.", ctx.get(SubscriberCategoryHome.class));
        
        // Account verify
        Home acctHome = (Home) ctx.get(AccountHome.class); 
        try
        {
            assertNotNull("No AccountHome was in the Context",acctHome);
            
            assertNotNull(AccountSupport.getAccount(ctx, ACCOUNT1_BAN));
            assertNotNull(AccountSupport.getAccount(ctx, ACCOUNT2_BAN));
            assertNotNull(AccountSupport.getAccount(ctx, ACCOUNT3_BAN));
            assertNotNull(AccountSupport.getAccount(ctx, ACCOUNT4_BAN));
            assertNotNull(AccountSupport.getAccount(ctx, ACCOUNT5_BAN));

        }
        catch (Exception e)
        {
            fail("Failed Account Verify. " + e.getMessage());
        }
        
        // Subscriber verify
        Home subHome = (Home) ctx.get(SubscriberHome.class); 
        try
        {
            assertNotNull("No SubscriberHome was in the Context",subHome);
            
            Collection coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, ACCOUNT1_BAN)).selectAll();    
            assertEquals(0, coll.size());
            
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, getIndividualAccountIdentifier(SUB1_ID))).selectAll();    
            assertEquals(1, coll.size());
            
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, ACCOUNT2_BAN)).selectAll();    
            assertEquals(0, coll.size());
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, getIndividualAccountIdentifier(SUB2_ID))).selectAll();    
            assertEquals(1, coll.size());
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, getIndividualAccountIdentifier(SUB3_ID))).selectAll();    
            assertEquals(1, coll.size());
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, getIndividualAccountIdentifier(SUB4_ID))).selectAll();    
            assertEquals(1, coll.size());
            
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, ACCOUNT3_BAN)).selectAll();    
            assertEquals(0, coll.size());
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, getIndividualAccountIdentifier(SUB7_ID))).selectAll();    
            assertEquals(1, coll.size());
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, getIndividualAccountIdentifier(SUB8_ID))).selectAll();    
            assertEquals(1, coll.size());
            
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, ACCOUNT4_BAN)).selectAll();    
            assertEquals(0, coll.size());
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, getIndividualAccountIdentifier(SUB5_ID))).selectAll();    
            assertEquals(1, coll.size());
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, getIndividualAccountIdentifier(SUB6_ID))).selectAll();    
            assertEquals(1, coll.size());
            
            coll = subHome.where(ctx, new EQ(SubscriberXInfo.BAN, ACCOUNT5_BAN)).selectAll();    
            assertEquals(3, coll.size());
        }
        catch (Exception e)
        {
            fail("Failed Subscriber Verify. " + e.getMessage());
        }
        
        // Currency verify
        Home currencyHome = (Home) ctx.get(CurrencyHome.class); 
        try
        {
            Collection coll = currencyHome.selectAll();    
            assertEquals(1, coll.size());
            
        }
        catch (Exception e)
        {
            fail("Failed Currency Verify. " + e.getMessage());
        }
        
        // Subscription Type verify
        try
        {
            Home typeHome = (Home) ctx.get(SubscriptionTypeHome.class); 
            Collection coll = typeHome.selectAll();    
            assertEquals(3, coll.size());
        }
        catch (Exception e)
        {
            fail("Failed Subscription Type Verify. " + e.getMessage());
        }
        
        // Subscription Class
        try
        {
            Home typeHome = (Home) ctx.get(SubscriptionClassHome.class); 
            Collection coll = typeHome.selectAll();    
            assertEquals(2, coll.size());
        }
        catch (Exception e)
        {
            fail("Failed Subscription Class Verify. " + e.getMessage());
        }
        
        // Account Role
        try
        {
            Home typeHome = (Home) ctx.get(AccountRoleHome.class); 
            Collection coll = typeHome.selectAll();    
            assertEquals(1, coll.size());
        }
        catch (Exception e)
        {
            fail("Failed Account Role Verify. " + e.getMessage());
        }
    }
    
    public void testSetupSubscriberSubscriptions()
    {
        TestSetupSubscriberServices.setup(getContext(), false);
        TestSetupSubscriberServices.testSetup(getContext());
    }
    
    /**
     * puts the test AccountXDBHome (with test Table) into the context under
     * AccountHome.class key.
     * @param installXDB - if the TRUE, then the homes to install will use the XDB homes.
     *                 if FALSE, install transient Homes
     */
    public static void setupAccountHome(Context ctx, final boolean installXDB)
    {
        Home acctHome_ = new TransientFieldResettingHome(ctx, new AccountTransientHome(ctx));
        if (installXDB)
        {
            /* Invoice Server won't be able to access the test table. 
             * Falling back to the default XDB home pointing to the account table. */
            acctHome_ = new AccountXDBHome(ctx, DB_TABLE_ACCOUNT);
        }
        
        // Low-level home (used by move logic and possibly elsewhere)
        ctx.put(Common.ACCOUNT_CACHED_HOME, acctHome_);
        acctHome_ = new ContextualizingHome(ctx, acctHome_);
        
        // Add adapters that are required for foreign-key type "transient" feilds
        acctHome_ = new AccountLazyLoadedPropertyUpdateHome(ctx, acctHome_);
        ctx.put(AccountHome.class, acctHome_);
        
        // Set up all of the homes that should be used to store foreign-key "transient" information
        Home acctContactHome = new TransientFieldResettingHome(ctx, new ContactTransientHome(ctx));
        if (installXDB)
        {
            /* Invoice Server won't be able to access the test table. 
             * Falling back to the default XDB home pointing to the account table. */
            acctContactHome = new ContactXDBHome(ctx, DB_TABLE_ACCOUNT_CONTACT);
        }
        acctContactHome = new HomeProxy(ctx, acctContactHome)
        {
            @Override
            public Object create(Context pCtx, Object obj) throws HomeException
            {
                Contact contact = (Contact) obj;
                contact.setId(id_++);
                return super.create(pCtx, obj);
            }
            
            private long id_ = Contact.DEFAULT_ID;
        };
        ctx.put(ContactHome.class, acctContactHome);
        
        Home acctSecHome = new TransientFieldResettingHome(ctx, new SecurityQuestionAnswerTransientHome(ctx));
        if (installXDB)
        {
            /* Invoice Server won't be able to access the test table. 
             * Falling back to the default XDB home pointing to the account table. */
            acctSecHome = new SecurityQuestionAnswerXDBHome(ctx, DB_TABLE_ACCOUNT_SECURITY);
        }
        ctx.put(SecurityQuestionAnswerHome.class, acctSecHome);

        Home acctIdentificationHome = new TransientFieldResettingHome(ctx, new AccountIdentificationTransientHome(ctx));
        if (installXDB)
        {
            /* Invoice Server won't be able to access the test table. 
             * Falling back to the default XDB home pointing to the account table. */
            acctIdentificationHome = new AccountIdentificationXDBHome(ctx, DB_TABLE_ACCOUNT_IDENTIFICATION);
        }
        ctx.put(AccountIdentificationHome.class, acctIdentificationHome);

        Home ccInfoHome = new TransientFieldResettingHome(ctx, new CreditCardInfoTransientHome(ctx));
        if (installXDB)
        {
            /* Invoice Server won't be able to access the test table. 
             * Falling back to the default XDB home pointing to the account table. */
            ccInfoHome = new CreditCardInfoXDBHome(ctx, DB_TABLE_CREDIT_CARD_INFO);
        }
        ctx.put(CreditCardInfoHome.class, ccInfoHome);
    }
    
    /**
     * puts the test AccountXDBHome (with test Table) into the context under
     * AccountHome.class key.
     * @param installXDB - if the TRUE, then the homes to install will use the XDB homes.
     *                 if FALSE, install transient Homes
     */
    public static void setupSubscriberHome(Context ctx, final boolean installXDB)
    {
        Home subsHome_ = new TransientFieldResettingHome(ctx, new SubscriberTransientHome(ctx));
        if (installXDB)
        {
            /* Invoice Server won't be able to access the test table. 
             * Falling back to the default XDB home pointing to the subscriber table. */
            subsHome_ = new SubscriberXDBHome(ctx, DB_TABLE_SUBSCRIBER);
        }
        ctx.put(SubscriberXDBHome.class, subsHome_);
        
        subsHome_ = new ContextualizingHome(ctx, subsHome_);
        ctx.put(SubscriberHome.class, subsHome_);
    }
    
    /**
     * puts the test TransactionMethodXDBHome (with test Table) into the context under
     * TransactionMethodHome.class key.
     * @param installXDB - if the TRUE, then the homes to install will use the XDB homes.
     *                 if FALSE, install transient Homes
     */
    public static void setupTransactionMethodHome(Context ctx, final boolean installXDB)
    {
        Home methodHome_ = new TransientFieldResettingHome(ctx, new TransactionMethodTransientHome(ctx));
        if (installXDB)
        {
            /* Invoice Server won't be able to access the test table. 
             * Falling back to the default XDB home pointing to the payment plan table. */
            methodHome_ = new TransactionMethodXDBHome(ctx, DB_TABLE_TRANSACTION_METHOD);
        }
        ctx.put(TransactionMethodHome.class, methodHome_);
    }
    
    /**
     * Install the BillCycleTransient Home and create the default Bill Cycle 1.
     * @param ctx
     */
    public static void setupBillCycle(Context ctx)
    {
        Home home = (Home) ctx.get(BillCycleHome.class);
        
        BillCycle cycle = new BillCycle();
        cycle.setBillCycleID(BILL_CYCLE_ID);
        cycle.setDayOfMonth(1);
        cycle.setDescription("Unit Test Bill Cycle");
        cycle.setSpid(SPID_ID);
        try
        {
            home.create(cycle);
        }
        catch (Exception e)
        {
            logDebugMsg(ctx, "Failed to create bill cycle.", e);
        }
    }
    
    /**
     * Install the AccountTypeTransientHome and create the default Account Type.
     * @param ctx
     */
    public static void setupAccountType(Context ctx)
    {
        Home home = (Home) ctx.get(AccountCategoryHome.class);
		Home accountCategoryHome = new AccountCategoryTransientHome(ctx);
        AccountCategory type = new AccountCategory();
        type.setIdentifier(ACCOUNT_TYPE_NON_INDIVIDUAL);
        type.setName("Unit Test Non-Indv");
        type.setBillingMessage("Unit Test Account Type message");
        try
        {
            home.create(ctx, type);
			accountCategoryHome.create(ctx, type);
        }
        catch(Exception e)
        {
            logDebugMsg(ctx, "Failed to setup the Non-individual Account Type. " + e.getMessage(), e);
        }
        
        type = new AccountCategory();
        type.setIdentifier(ACCOUNT_TYPE_INDIVIDUAL);
        type.setName("Unit Test Individual");
        type.setBillingMessage("Unit Test Account Type message");
        try
        {
            home.create(ctx, type);
			accountCategoryHome.create(ctx, type);
        }
        catch(Exception e)
        {
            logDebugMsg(ctx, "Failed to setup the Individual Account Type. " + e.getMessage(), e);
        }
        
        type = new AccountCategory();
        type.setIdentifier(ACCOUNT_TYPE_POOLED);
        type.setName("Unit Test Pooled");
        type.setBillingMessage("Unit Test Account Type message");
        try
        {
            home.create(ctx, type);
			accountCategoryHome.create(ctx, type);
        }
        catch(Exception e)
        {
            logDebugMsg(ctx, "Failed to setup the Pooled Account Type. " + e.getMessage(), e);
        }

		ctx.put(AccountCategoryHome.class, home);
		ctx.put(AccountCategoryHome.class, accountCategoryHome);
    }
    
    public static void setupCurrency(Context context)
    {
        Home home = (Home) context.get(CurrencyHome.class);

        Currency currency = new Currency();
        currency.setCode(DEFAULT_CURRENCY);
        currency.setName("Unit Test Currency " + DEFAULT_CURRENCY);
        currency.setPrecision(0.01);
        currency.setFormat("0.00");
        currency.setSymbol("$");
        try
        {
            home.create(context, currency);
        }
        catch(Exception e)
        {
            logDebugMsg(context, "Failed to setup Currency. " + e.getMessage(), e);
        }
    }
    
    public static void setupNoteHomes(Context context)
    {
        //Account Note Home
        context.put(Common.ACCOUNT_NOTE_HOME,
                new NoSelectAllHome(
                        new LastModifiedAwareHome(
                                new UserAgentHome(context,
                                        new TransientNoteIdentifierSettingHome(
                                                new TransientFieldResettingHome(context, new NoteTransientHome(context)))))));

        //Subscriber Note Home
        context.put(
                NoteHome.class,
                new NoSelectAllHome(
                        new LastModifiedAwareHome(
                                new UserAgentHome(context,
                                        new TransientNoteIdentifierSettingHome(
                                                new TransientFieldResettingHome(context, new NoteTransientHome(context)))))));
    }
    
    private static void setupIdentificationHome(Context context) throws HomeException
    {
        UnitTestSupport.createHome(context, Identification.class);
    }
    
    private static void setupIdentification(Context context)
    {
        Home home = (Home) context.get(IdentificationHome.class);
        
        try
        {
            Identification type = new Identification();
            type.setCode(DEFAULT_IDENTIFICATION_TYPE);
            type.setDesc("UNIT TEST ID");
            type.setSpid(SPID_ID);
            IdFormat format = new IdFormat();
            type.setFormat(format);
            type.setExample(DEFAULT_IDENTIFICATION_EXAMPLE);
            home.create(context, type);
        }
        catch (Exception e)
        {
            logDebugMsg(context, "Failed to create the default Identification " + e.getMessage(), e);
        }
    }
    
    private static void setupBlackListHome(final Context context)
    {
        context.put(BlackListHome.class, new TransientFieldResettingHome(context, new BlackListTransientHome(context)));
    }
    
    /**
     * Create the default Subscriber Account Role.
     * @param ctx
     */
    private static void setupAccountRole(final Context ctx)
    {
        Home home = (Home) ctx.get(AccountRoleHome.class);
        
        //Setup Default Account Role
        try
        {
            AccountRole role = new AccountRole();
            role.setId(SUBSCRIBER_ACCOUNT_ROLE);
            role.setName("Subscriber (Unit Test)");
            ArrayList<SubscriptionClassRow> allowedSubscriptions = new ArrayList<SubscriptionClassRow>();
            {
                SubscriptionClassRow row = new SubscriptionClassRow();
                row.setSubscriptionClass(DEFAULT_AIRTIME_SUBSCRIPTION_CLASS);
                allowedSubscriptions.add(row);
                row = new SubscriptionClassRow();
                row.setSubscriptionClass(DEFAULT_WALLET_SUBSCRIPTION_CLASS);
                allowedSubscriptions.add(row);
            }
            role.setAllowedSubscriptionClass(allowedSubscriptions);
            home.create(role);
        }
        catch(Exception e)
        {
            logDebugMsg(ctx, "Failed to create account Roles. " + e.getMessage(), e);
        }
    }
    
    /**
     * Create the default airtime subscription class
     * @param ctx
     */
    private static void setupSubscriptionClass(final Context ctx)
    {
        Home home = (Home) ctx.get(SubscriptionClassHome.class);
        
        //Setup Default class: Airtime only
        try
        {
            SubscriptionClass subscriptionClass = new SubscriptionClass();
            subscriptionClass.setId(DEFAULT_AIRTIME_SUBSCRIPTION_CLASS);
            subscriptionClass.setName("Mobile Airtime (Unit Test)");
            subscriptionClass.setDescription("Mobile Airtime Subscription Class for Unit Tests");
            subscriptionClass.setSubscriptionType(getSubscriptionTypeId(SubscriptionTypeEnum.AIRTIME_INDEX));
            subscriptionClass.setTechnologyType(TechnologyEnum.GSM_INDEX);
            subscriptionClass.setSegmentType(SubscriberTypeEnum.HYBRID_INDEX);

            home.create(subscriptionClass);
        }
        catch(Exception e)
        {
            logDebugMsg(ctx, "Failed to setup SubscriptionClasses. " + e.getMessage(), e);
        }
        
        try
        {
            SubscriptionClass subscriptionClass = new SubscriptionClass();
            subscriptionClass.setId(DEFAULT_WALLET_SUBSCRIPTION_CLASS);
            subscriptionClass.setName("Mobile Wallet (Unit Test)");
            subscriptionClass.setDescription("Mobile Wallet Subscription Class for Unit Tests");
            subscriptionClass.setSubscriptionType(getSubscriptionTypeId(SubscriptionTypeEnum.MOBILE_WALLET_INDEX));
            subscriptionClass.setTechnologyType(TechnologyEnum.GSM_INDEX);
            subscriptionClass.setSegmentType(SubscriberTypeEnum.HYBRID_INDEX);

            home.create(subscriptionClass);
        }
        catch(Exception e)
        {
            logDebugMsg(ctx, "Failed to setup SubscriptionClasses. " + e.getMessage(), e);
        }
    }
    
    /**
     * Create 3 default Subscription types. Airtime, Network, Mobile Money
     * @param ctx
     */
    private static void setupSubscriptionType(final Context ctx)
    {
        Home home = (Home) ctx.get(SubscriptionTypeHome.class);
        //Setup Default types: Airtime, Network, Mobile Money Wallets
        try
        {
            SubscriptionType type = new SubscriptionType();
            type.setId(getSubscriptionTypeId(SubscriptionTypeEnum.AIRTIME_INDEX));
            type.setName("Airtime");
            type.setDescription("Airtime Subscription Type for Unit Tests");
            type.setType(SubscriptionTypeEnum.AIRTIME_INDEX);
            home.create(type);
            
            type = new SubscriptionType();
            type.setId(getSubscriptionTypeId(SubscriptionTypeEnum.MOBILE_WALLET_INDEX));
            type.setName("Mobile Wallet");
            type.setDescription("Mobile Wallet Subscription Type for Unit Tests");
            type.setType(SubscriptionTypeEnum.MOBILE_WALLET_INDEX);
            home.create(type);
            
            type = new SubscriptionType();
            type.setId(getSubscriptionTypeId(SubscriptionTypeEnum.NETWORK_WALLET_INDEX));
            type.setName("Network Wallet");
            type.setDescription("Network Wallet Subscription Type for Unit Tests");
            type.setType(SubscriptionTypeEnum.NETWORK_WALLET_INDEX);
            home.create(type);
        }
        catch(Exception e)
        {
            logDebugMsg(ctx, "Failed to setup SubscriptionTypes. " + e.getMessage(), e);
        }

    }
    
    private static void releaseMsisdn(Context ctx, String mobileNumber, String accountIdentifier, String subscriberId)
        throws HomeException
    {
        /* Release the Mobile Number from NR Subscriber Account since it is the 
         * Subscriber account that has claimed the MSISDN (as opposed to the Group
         * Account).
         */
        //Disassociate Subscription before Releasing the MSISDN.
        Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, mobileNumber);
        MsisdnManagement.deassociateMsisdnWithSubscription(ctx, mobileNumber, subscriberId, "voiceMsisdn");
        /* We have to pause for a few milliseconds to allow MsisdnMgmtHistory to be written so we 
         * can query it.*/
        //pause(ctx);
        MsisdnManagement.releaseMsisdn(ctx, mobileNumber, accountIdentifier, "UnitTest-TestSetupAccountHierachy");
        
        //Additional step because the Msisdn is claimed by the NR Subscriber Account and not the Group Account.
        msisdn.reset();
        Home msisdnHome = (Home) ctx.get(MsisdnHome.class);
        msisdnHome.store(msisdn);
    }
    
    
    /**
     * Create Msisdn profile with the given information and claim it for the given subscriber and account.
     * Associate it as a Voice Msisdn.
     * @param id  Mobile Number
     * @param subscriberID  Subscriber Identifier
     * @param ban Account Identifier
     */
    private static void claimAndAssociateMsisdn(
            Context ctx, 
            String id, 
            String subscriberID, 
            String ban,
            SubscriberTypeEnum subscriberType) throws HomeException
    {
        /* Remove the code that will create the Msisdn bean in the system,
         * and instead delegate the creation to MsisdnManagement methods
         * by indicating isExternal=TRUE. */
        try
        {
            MsisdnManagement.claimMsisdn(ctx, id, ban, true, "UnitTest-TestSetupAccountHierachy");
        }
        catch(MsisdnAlreadyAcquiredException e)
        {
            // ignore.  We will sometimes create subscriptions using already claimed MSISDNs
        }   
        MsisdnManagement.associateMsisdnWithSubscription(ctx, id, subscriberID, "voiceMsisdn");
    }
    

    /**
     * Setup Accounts and Subscribers according to the class description.
     * @param ctx
     * @throws HomeException
     */
    public static void setupDefaultAccountsAndSubscribers(Context ctx) 
    {
        try
        {
            //Root Account
            setupAccount(ctx, ACCOUNT1_BAN, "", true);
            setupSubscriber(ctx, ACCOUNT1_BAN, SUB1_ID, SubscriberTypeEnum.POSTPAID, SUB1_MSISDN,
                    SubscriberStateEnum.ACTIVE, START_DATE);

            //Child Account
            setupAccount(ctx, ACCOUNT2_BAN, ACCOUNT1_BAN, false);
            //Deactivated Subscriber 
            setupSubscriber(ctx, ACCOUNT2_BAN, SUB4_ID, SubscriberTypeEnum.POSTPAID, SUB2_MSISDN,
                    SubscriberStateEnum.INACTIVE, CalendarSupportHelper.get(ctx).findDateDaysBefore(30, START_DATE));
            //Release the Mobile Number from ACCOUNT2_BAN+"-NR" since it is the Subscriber account that has claimed the MSISDN.
            releaseMsisdn(ctx, SUB2_MSISDN, getIndividualAccountIdentifier(SUB4_ID), SUB4_ID);

            //First Active Subscriber, with Reused Mobile Number
            pause(ctx);
            setupSubscriber(ctx, ACCOUNT2_BAN, SUB2_ID, SubscriberTypeEnum.POSTPAID, SUB2_MSISDN,
                    SubscriberStateEnum.ACTIVE, CalendarSupportHelper.get(ctx).getDayAfter(START_DATE));
            //Second Active Subscriber
            setupSubscriber(ctx, ACCOUNT2_BAN, SUB3_ID, SubscriberTypeEnum.POSTPAID, SUB3_MSISDN,
                    SubscriberStateEnum.ACTIVE, START_DATE);

            // Multiple Subscriber Account
            setupAccount(ctx, ACCOUNT4_BAN, "", true);
            setupSubscriber(ctx, ACCOUNT4_BAN, SUB5_ID, SubscriberTypeEnum.POSTPAID, SUB5_MSISDN,
                    SubscriberStateEnum.INACTIVE, START_DATE);
            setupSubscriber(ctx, ACCOUNT4_BAN, SUB6_ID, SubscriberTypeEnum.POSTPAID, SUB6_MSISDN,
                    SubscriberStateEnum.ACTIVE, START_DATE);

            // Pooled Subscriber Account
            setupAccount(ctx, ACCOUNT3_BAN, "", true, ACCOUNT_TYPE_POOLED, SUB7_MSISDN, SubscriberTypeEnum.HYBRID);
            setupSubscriber(ctx, ACCOUNT3_BAN, SUB7_ID, SubscriberTypeEnum.POSTPAID, SUB7_MSISDN,
                    SubscriberStateEnum.ACTIVE, START_DATE);
            setupSubscriber(ctx, ACCOUNT3_BAN, SUB8_ID, SubscriberTypeEnum.PREPAID, SUB8_MSISDN,
                    SubscriberStateEnum.ACTIVE, START_DATE);

            // Individual Account with 3 Subscriptions
            setupAccount(ctx, ACCOUNT5_BAN, "", true, ACCOUNT_TYPE_INDIVIDUAL, "", SubscriberTypeEnum.PREPAID);
            setupSingleSubscriberSubscription(ctx, ACCOUNT5_BAN, SUB9_ID, SubscriberTypeEnum.PREPAID,
                    SUB9_MSISDN, SubscriberStateEnum.ACTIVE, START_DATE, SubscriptionTypeEnum.AIRTIME_INDEX);
            setupSingleSubscriberSubscription(ctx, ACCOUNT5_BAN, SUB10_ID, SubscriberTypeEnum.PREPAID,
                    SUB9_MSISDN, SubscriberStateEnum.ACTIVE, START_DATE, SubscriptionTypeEnum.MOBILE_WALLET_INDEX);
            setupSingleSubscriberSubscription(ctx, ACCOUNT5_BAN, SUB11_ID, SubscriberTypeEnum.PREPAID,
                    SUB9_MSISDN, SubscriberStateEnum.ACTIVE, START_DATE, SubscriptionTypeEnum.NETWORK_WALLET_INDEX);
        }
        catch (Exception e)
        {
            /* Delete the accounts and subscribers that were set up before the failure.
             * tearDown will not occur if the failure is in setUp.
             */
            deleteAccountsSubscribers(ctx);
            logDebugMsg(ctx, "Failed to set up Accounts and Subscribers. " + e.getMessage(), e);
        }
    }

    /**
     * Pause for 100 milliseconds
     * @param ctx
     */
    public static void pause(Context ctx) 
    {
        try
        {
            Thread.sleep(100); // Too quick and the association will not be ready by the time we start the test.
        }
        catch (InterruptedException e)
        {
            LogSupport.debug(ctx, TestSetupAccountHierarchy.class, "Encountered an InterruptedException, this could affect the results " +
                    " of the test, but then again it might not.  We don't stop the test and instead hope that " +
            " this really long log message will lag the unit test enough to make a difference.");
        }
    }

    /** 
     * Set up a VALID Non-Individual Account for testing.
     * @param ban - account identifier
     * @param parent - parent account identifier
     * @param isResponsible - indicates whether or not this account will be responsible for it's own balance.
     */
    public static void setupAccount(Context ctx, String ban, 
            String parent, boolean isResponsible) throws HomeException
    {
        setupAccount(ctx, ban, parent, isResponsible, ACCOUNT_TYPE_NON_INDIVIDUAL, "", SubscriberTypeEnum.HYBRID);
    }
    
    /** 
     * Set up a VALID Account for testing.
     * @param ban - account identifier
     * @param parent - parent account identifier
     * @param accountType - account type identifier
     * @param paidType - Postpaid, Prepaid or Hybrid.
     */
    public static void setupAccount(Context ctx, String ban, 
            String parent, boolean isResponsible, long accountType,
            String groupMSISDN, SubscriberTypeEnum paidType) throws HomeException
    {
        Home home = (Home) ctx.get(AccountHome.class); 
        
        Account account = new Account();
        
        account.setContext(ctx);
        
        account.setBAN(ban);
        account.setParentBAN(parent);
        account.setSystemType(paidType);
        account.setType(accountType);
        account.setSpid(SPID_ID);
        account.setBillCycleID(BILL_CYCLE_ID);
        account.setCurrency(DEFAULT_CURRENCY);
        account.setResponsible(isResponsible);
        // todo rewrite using extentions if it matters
        //account.setGroupMSISDN(groupMSISDN);
        account.setVpnMSISDN(groupMSISDN);

        setupForeignKeyAccountFields(account);
        
        account.setRole(SUBSCRIBER_ACCOUNT_ROLE);
        account.setState(AccountStateEnum.ACTIVE);
        
        try
        {
            home.create(ctx,account);
        }
        catch(Exception he)
        {
            throw new HomeException("Failed to set up account=" + ban, he);
        }
    }

    /**
     * @param account
     */
    private static void setupForeignKeyAccountFields(Account account)
    {
        // Person contact fields (transient)
        account.setContactTel("8765309");
        account.setDateOfBirth(new Date());
        account.setBillingCity("Los Angeles");
        account.setBillingCountry("USA");
        
        // Bank contact fields (transient)
        account.setBankAccountName("Jenny Chequing");
        account.setBankAccountNumber("86-75309");
        account.setBankName("Bank of America");
        
        // Company contact fields (transient)
        account.setTradingName("RKN");
        
        // Security questions & answers (3 required/transient)
        List<SecurityQuestionAnswer> securityList = new ArrayList<SecurityQuestionAnswer>();
        SecurityQuestionAnswer qA = new SecurityQuestionAnswer();
        SecurityQuestionAnswer qB = new SecurityQuestionAnswer();
        SecurityQuestionAnswer qC = new SecurityQuestionAnswer();
        qA.setQuestion("A");
        qA.setAnswer("A");
        qB.setQuestion("B");
        qB.setAnswer("B");
        qC.setQuestion("C");
        qC.setAnswer("C");
        securityList.add(qA);
        securityList.add(qB);
        securityList.add(qC);
        account.setSecurityQuestionsAndAnswers(securityList);

        // Identification pieces (2 required/transient)
        List<AccountIdentification> identList = new ArrayList<AccountIdentification>();
        AccountIdentification ident1 = new AccountIdentification();
        AccountIdentification ident2 = new AccountIdentification();
        ident1.setIdType(1);
        ident1.setIdNumber("11111111");
        ident2.setIdType(2);
        ident2.setIdNumber("22222222");
        identList.add(ident1);
        identList.add(ident2);
        AccountIdentificationGroup aig = new AccountIdentificationGroup();
        aig.setIdentificationList(identList);
        account.getIdentificationGroupList().add(aig);
    }
    
    
    /**
     * Setup a Valid Subscriber for Testing.
     * Will create this subscriber under a new Subscriber Account. 
     * @param ctx
     * @param ban
     * @param id
     * @param type
     * @param msisdn
     * @throws HomeException
     */
    public static void setupSubscriber(Context ctx, 
            String ban, 
            String id, 
            SubscriberTypeEnum type, 
            String msisdn, 
            SubscriberStateEnum state, 
            Date startDate) throws HomeException
    {
        //Setup Non-Responsible account
        String nonResponsibleSubscriberAccountBAN = getIndividualAccountIdentifier(id);
        setupAccount(ctx, nonResponsibleSubscriberAccountBAN, ban, false, ACCOUNT_TYPE_INDIVIDUAL, "", type);
                
        //Setup single Subscription
        setupSingleSubscriberSubscription(ctx, nonResponsibleSubscriberAccountBAN, id, 
                type, msisdn, state, startDate, SubscriptionTypeEnum.AIRTIME_INDEX);
    }
    
    /**
     * Setup a Valid Subscriber for Testing (AIRTIME)
     * Msisdn assigned to the subscriber is by default created as External MSISDN.
     * @param ctx
     * @param ban
     * @param id
     * @param type
     * @param msisdn
     * @param subscriptionType TODO
     * @throws HomeException
     */
    public static void setupSingleSubscriberSubscription(Context ctx, 
            String ban, 
            String id, 
            SubscriberTypeEnum type, 
            String msisdn, 
            SubscriberStateEnum state, 
            Date startDate, 
            int subscriptionType) throws HomeException
    {
        Home home = (Home) ctx.get(SubscriberHome.class);
        Subscriber sub = new Subscriber();
        sub.setBAN(ban);
        sub.setId(id);
        sub.setSubscriberType(type);
        sub.setMSISDN(msisdn);
        sub.setMsisdnEntryType(MsisdnEntryTypeEnum.EXTERNAL_INDEX);
        sub.setSubscriptionType(getSubscriptionTypeId(subscriptionType));
        sub.setDateCreated(startDate);
        sub.setStartDate(startDate);
        sub.setState(state);
        //Price Plan IDs coincide with SubscriptionTypes
        sub.setPricePlan(getSubscriptionTypeId(subscriptionType));
        sub.setPricePlanVersion(TestSetupPricePlanAndServices.pricePlanVersionID_);
        sub.setSpid(SPID_ID);
        
        try
        {
            home.create(ctx, sub);
        }
        catch (Exception e)
        {
            throw new HomeException("Failed to set up sub=" + id, e);
        }
        
        //Create and Claim Msisdn
        //We don't need to manually do this if the Xtest is running in a real application
        if (!UnitTestSupport.isTestRunningInXTest(ctx))
        {
            claimAndAssociateMsisdn(ctx, msisdn, id, ban, type);
        }
    }
    
    
    /** 
     * Delete Account.
     * @param ban - account identifier
     */
    private static void deleteAccount(Context ctx, String ban) 
    {
        Home home = (Home) ctx.get(AccountHome.class); 
        if (home != null)
        {
            try
            {
                Account account = (Account) home.find(ctx, ban);

                if (account != null)
                {
                    home.remove(ctx,account);
                }
            }
            catch(Exception he)
            {
                logDebugMsg(ctx, "Failed to remove account=" + ban + " due to " + he.getMessage(), he);
            }
        }
    }
    
    /**
     * Delete Subscriber.
     * @param ctx
     * @param id
     * @throws HomeException
     */
    private static void deleteSubscriber(Context ctx, String id) 
    {
        Home home = (Home) ctx.get(SubscriberHome.class);
        if (home != null)
        {
            try
            {
                Subscriber sub = (Subscriber)home.find(ctx, id);

                if (sub != null)
                {
                    home.remove(ctx, sub);
                }
            }
            catch (Exception e)
            {
                logDebugMsg(ctx, "Failed to remove sub=" + id + " due to " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Delete all Bill Cycles
     * @param ctx
     */
    private static void deleteBillCycle(Context ctx)
    {
        Home home = (Home) ctx.get(BillCycleHome.class);
        if (home != null)
        {
            try 
            {
                Predicate predicate = new EQ(BillCycleXInfo.BILL_CYCLE_ID, Integer.valueOf(BILL_CYCLE_ID));
                home.removeAll(ctx, predicate);
            } 
            catch (Exception e) 
            {
                logDebugMsg(ctx, "Failed to delete all bill cycles.", e);
            }        
        }
    }
    
    /**
     * Delete all account types
     * @param ctx
     */
    private static void deleteAccountType(Context ctx)
    {
        HashSet<Long> set = new HashSet<Long>();
        set.add(ACCOUNT_TYPE_NON_INDIVIDUAL);
        set.add(ACCOUNT_TYPE_INDIVIDUAL);
        set.add(ACCOUNT_TYPE_POOLED);
        Home home = (Home) ctx.get(AccountCategoryHome.class);
        if (home != null)
        {
            try 
            {
                Predicate predicate = new In(AccountCategoryXInfo.IDENTIFIER, set);
                home.removeAll(ctx, predicate);
            } 
            catch (Exception e) 
            {
                logDebugMsg(ctx, "Failed to delete all Account Types.", e);
            }        
        }
    }
    
    /**
     * Delete all Msisdns
     * @param ctx
     */
    private static void deleteMsisdn(Context ctx)
    {
        Home home = (Home) ctx.get(MsisdnHome.class);
        if (home != null)
        {
            try 
            {
                Predicate predicate = new In(MsisdnXInfo.MSISDN, ALL_TEST_MSISDNS);
                home.removeAll(ctx, predicate);
            } 
            catch (Exception e) 
            {
                logDebugMsg(ctx, "Failed to delete all test Msisdns.", e);
            }
        }
    }
    
    private static void deleteCurrency(Context context)
    {
        Home home = (Home) context.get(CurrencyHome.class);
        if (home != null)
        {
            try
            {
                Currency currency = new Currency();
                currency.setCode(DEFAULT_CURRENCY);
                home.remove(currency);
            }
            catch(Exception e)
            {
                logDebugMsg(context, "Failed to delete default test Currency. " + e.getMessage(), e);
            }
        }
    }
    
    private static void deleteNotes(Context context)
    {
        Home nHome = (Home)context.get(NoteHome.class);
        Home aHome = (Home)context.get(Common.ACCOUNT_NOTE_HOME);
        try
        {
            if (nHome != null)
            {
                In predicate = new In(NoteXInfo.ID_IDENTIFIER, ALL_TEST_SUBS_IDS);
                nHome.where(context, predicate).removeAll();
            }
            if (aHome != null)
            {
                In predicate = new In(NoteXInfo.ID_IDENTIFIER, ALL_TEST_ACCOUNT_BANS);
                aHome.where(context, predicate).removeAll();
            }
        }
        catch(Exception e)
        {
            //Errors while tearing down unit test. Do nothing.
            logDebugMsg(context, e.getMessage(), e);
        }
    }
    
    private static void deleteIdentification(Context context)
    {
        try
        {
            Home home = (Home) context.get(IdentificationHome.class);

            Predicate predicate = new EQ(IdentificationXInfo.CODE, DEFAULT_IDENTIFICATION_TYPE);
            home.removeAll(context, predicate);
        }
        catch(Exception e)
        {
            //Errors while tearing down unit test. Do nothing.
            logDebugMsg(context, e.getMessage(), e);
        }
    }
    
    private static void deleteSubscriptionType(Context context)
    {
        Home home = (Home) context.get(SubscriptionTypeHome.class);
        //Setup Default types: Airtime, Network, Mobile Money Wallets
        try
        {
            Predicate predicate = new In(SubscriptionTypeXInfo.ID, TEST_SUBSCRIPTION_TYPES);
            home.removeAll(context, predicate);
        }
        catch(Exception e)
        {
            //Errors while tearing down unit test. Do nothing.
            logDebugMsg(context, e.getMessage(), e);
        }
    }
    
    private static void deleteSubscriptionClass(Context context)
    {
        HashSet<Long> set = new HashSet<Long>();
        set.add(DEFAULT_AIRTIME_SUBSCRIPTION_CLASS);
        set.add(DEFAULT_WALLET_SUBSCRIPTION_CLASS);
        Home home = (Home) context.get(SubscriptionClassHome.class);

        try
        {
            Predicate predicate = new In(SubscriptionClassXInfo.ID, set);
            home.removeAll(context, predicate);
        }
        catch(Exception e)
        {
            //Errors while tearing down unit test. Do nothing.
            logDebugMsg(context, e.getMessage(), e);
        }
    }
    
    private static void deleteAccountRole(Context context)
    {
        Home home = (Home) context.get(AccountRoleHome.class);

        //Setup Default Account Role
        try
        {
            AccountRole role = new AccountRole();
            role.setId(SUBSCRIBER_ACCOUNT_ROLE);
            home.remove(role);
        }
        catch(Exception e)
        {
            //Errors while tearing down unit test. Do nothing.
            logDebugMsg(context, e.getMessage(), e);
        }
    }
    
    /**
     * Log Debug message about the exception that occurred.
     * @param ctx
     * @param msg
     * @param e
     */
    private static void logDebugMsg(Context ctx, String msg, Throwable e) 
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(TestSetupAccountHierarchy.class, msg, e).log(ctx);
        }
    }

    /**
     * The NR Suffix has to be changed to a number since Invoice Record enforce a pattern 
     * of only numeric symbols. 
     * @param subscriberId
     * @return
     */
    public static String getIndividualAccountIdentifier(String subscriberId)
    {
        return subscriberId.replace('-', '0') + NR_SUB_ACCOUNT_SUFFIX;
    }
    
    public static long getSubscriptionTypeId(int originalId)
    {
        return SUBSCRIPTION_TYPE_PREFIX + originalId;
    }
    
    /**
     * Used to add the Account to the clean up process.
     * @param ban
     */
    public static void addTestAccountBan(String ban)
    {
        ALL_TEST_ACCOUNT_BANS.add(ban);
    }
    
    /**
     * Used to add the Subscriber to the clean up process.
     * @param ban
     */
    public static void addTestSubId(String subId)
    {
        ALL_TEST_SUBS_IDS.add(subId);
    }
    
    /**
     * Used to add the M to the clean up process.
     * @param ban
     */
    public static void addTestMsisdn(String msisdn)
    {
        ALL_TEST_MSISDNS.add(msisdn);
    }

    public static final int SPID_ID = 1;
    public static final String ACCOUNT1_BAN = "99910001";
    public static final String ACCOUNT2_BAN = "99910002";
    public static final String ACCOUNT3_BAN = "99910003";
    public static final String ACCOUNT4_BAN = "99910004";
    public static final String ACCOUNT5_BAN = "99910005";
    public static Set<String> ALL_TEST_ACCOUNT_BANS = new HashSet<String>();
    static
    {
        ALL_TEST_ACCOUNT_BANS.add(ACCOUNT1_BAN);
        ALL_TEST_ACCOUNT_BANS.add(ACCOUNT2_BAN);
        ALL_TEST_ACCOUNT_BANS.add(ACCOUNT3_BAN);
        ALL_TEST_ACCOUNT_BANS.add(ACCOUNT4_BAN);
        ALL_TEST_ACCOUNT_BANS.add(ACCOUNT5_BAN);
    }
    public static final String SUB1_ID = "99910001-1";
    public static final String SUB2_ID = "99910002-1";
    public static final String SUB3_ID = "99910002-3";
    public static final String SUB4_ID = "99910002-2";
    public static final String SUB5_ID = "99910004-1";
    public static final String SUB6_ID = "99910004-2";
    public static final String SUB7_ID = "99910003-1";
    public static final String SUB8_ID = "99910003-2";
    public static final String SUB9_ID = "99910005-1";
    public static final String SUB10_ID = "99910005-2";
    public static final String SUB11_ID = "99910005-3";
    public static Set<String> ALL_TEST_SUBS_IDS = new HashSet<String>();
    static
    {
        ALL_TEST_SUBS_IDS.add(SUB1_ID);
        ALL_TEST_SUBS_IDS.add(SUB2_ID);
        ALL_TEST_SUBS_IDS.add(SUB3_ID);
        ALL_TEST_SUBS_IDS.add(SUB4_ID);
        ALL_TEST_SUBS_IDS.add(SUB5_ID);
        ALL_TEST_SUBS_IDS.add(SUB6_ID);
        ALL_TEST_SUBS_IDS.add(SUB7_ID);
        ALL_TEST_SUBS_IDS.add(SUB8_ID);
        ALL_TEST_SUBS_IDS.add(SUB9_ID);
        ALL_TEST_SUBS_IDS.add(SUB10_ID);
        ALL_TEST_SUBS_IDS.add(SUB11_ID);
    }
    public static final String SUB1_MSISDN = "8881234567";
    public static final String SUB2_MSISDN = "8881234568";
    public static final String SUB3_MSISDN = "8881234569";
    public static final String SUB5_MSISDN = "8881234570";
    public static final String SUB6_MSISDN = "8881234571";
    public static final String SUB7_MSISDN = "8881234572";
    public static final String SUB8_MSISDN = "8880001111";
    public static final String SUB9_MSISDN = "8881234800";
    public static Set<String> ALL_TEST_MSISDNS = new HashSet<String>();
    static
    {
        ALL_TEST_MSISDNS.add(SUB1_MSISDN);
        ALL_TEST_MSISDNS.add(SUB2_MSISDN);
        ALL_TEST_MSISDNS.add(SUB3_MSISDN);
        ALL_TEST_MSISDNS.add(SUB5_MSISDN);
        ALL_TEST_MSISDNS.add(SUB6_MSISDN);
        ALL_TEST_MSISDNS.add(SUB7_MSISDN);
        ALL_TEST_MSISDNS.add(SUB8_MSISDN);
        ALL_TEST_MSISDNS.add(SUB9_MSISDN);
    }
    public static final Date START_DATE = new Date(1136091601000L); //January 1, 2006
    public static final long ACCOUNT_TYPE_NON_INDIVIDUAL = 99120;
    public static final long ACCOUNT_TYPE_INDIVIDUAL = 99130;
    public static final long ACCOUNT_TYPE_POOLED = 99140;
    public static final int BILL_CYCLE_ID = 991;
    private static final String DB_TABLE_ACCOUNT = "UNITTESTACCOUNT";
    private static final String DB_TABLE_ACCOUNT_CONTACT = "UNITTESTACCOUNTCONTACT";
    private static final String DB_TABLE_ACCOUNT_SECURITY = "UNITTESTACCOUNTSEC";
    private static final String DB_TABLE_ACCOUNT_IDENTIFICATION = "UNITTESTACCOUNTIDENT";
    private static final String DB_TABLE_CREDIT_CARD_INFO = "UNITTESTCREDITCARDINFO";
    private static final String DB_TABLE_SUBSCRIBER = "UNITTESTSUBSCRIBER";
    private static final String DB_TABLE_TRANSACTION_METHOD = "UNITTESTTRANSMETHOD";
    //Framework X-Test currency
    public static final String DEFAULT_CURRENCY = "FXT";
    public static final int DEFAULT_IDENTIFICATION_TYPE = 992;
    public static final String DEFAULT_IDENTIFICATION_EXAMPLE = "1111111";
    
    /* The setup of SubscriptionType is identified by the 
     * SubscriptionType enumeration values.   
     * For this test, we prefix the Identifiers with "99". See getSubscriptionTypeId(short)
     * AIRTIME = 1 -- SubscriptionTypeEnum.AIRTIME_INDEX
     * MOBILE_WALLET = 2 -- SubscriptionTypeEnum.MOBILE_WALLET_INDEX
     * NETWORK_WALLET = 3 -- SubscriptionTypeEnum.NETWORK_WALLET_INDEX
     */
    public static Set<Long> TEST_SUBSCRIPTION_TYPES = new HashSet<Long>();
    static
    {
        TEST_SUBSCRIPTION_TYPES.add(Long.valueOf(getSubscriptionTypeId(SubscriptionTypeEnum.AIRTIME_INDEX)));
        TEST_SUBSCRIPTION_TYPES.add(Long.valueOf(getSubscriptionTypeId(SubscriptionTypeEnum.MOBILE_WALLET_INDEX)));
        TEST_SUBSCRIPTION_TYPES.add(Long.valueOf(getSubscriptionTypeId(SubscriptionTypeEnum.NETWORK_WALLET_INDEX)));
    }
    public static final long SUBSCRIPTION_TYPE_PREFIX = 990L;
    public static final long DEFAULT_AIRTIME_SUBSCRIPTION_CLASS = 995L;
    public static final long DEFAULT_WALLET_SUBSCRIPTION_CLASS = 996L;
    public static final long SUBSCRIBER_ACCOUNT_ROLE = 996L;
    /**
     * The NR Suffix has to be changed to a number since Invoice Record enforce a pattern 
     * of only numeric symbols. 
     */
    public static final String NR_SUB_ACCOUNT_SUFFIX = "000";
    public static final String SETUP_SPID_KEY = TestSetupAccountHierarchy.class.getName()+ ".SPID";
}
