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
package com.trilogy.app.crm.numbermgn;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryTransientHome;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidTransientHome;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.client.AppPinManagerClient;
import com.trilogy.app.crm.client.AppPinManagerTestClient;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestSetupMobileNumbers;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public class TestMsisdnManagement extends ContextAwareTestCase 
{


    public TestMsisdnManagement(String name)
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
        final TestSuite suite = new TestSuite(TestMsisdnManagement.class);
        return suite;
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#setUp()
     */
    @Override
    protected void setUp() 
    {
        super.setUp();
        try 
        {
            // Storage
            setupSpidHome();
            setupAccountHome();
            setupSubscriberHome();
            
            TestSetupMobileNumbers.setup(getContext());
            
            // Service
            setupAppPinManagerClient();
            
            // Entities
            setupMsisdnGroup();
            setupSpid();
            setupAccountType();
            setupAccount(getContext(), ACCOUNT_BAN, "", INDIVIDUAL_ACCOUNTTYPE);
            setupAccount(getContext(), OTHER_ACCOUNT_BAN, "", INDIVIDUAL_ACCOUNTTYPE);
            setupAccount(getContext(), ACCOUNT_WITH_SUBS_BAN, "", INDIVIDUAL_ACCOUNTTYPE);
            setupMsisdns();
            setupSubscriber(getContext(), ACCOUNT_WITH_SUBS_BAN, SUB_ID, SUB_IN_ACCOUNT_MSISDN);
            setupSubscriber();
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Setup Error: " + e.getMessage(), e).log(getContext());
        }

    }
    

    private void setupMsisdnMgmtHistoryHome()
    {
        Home home = new MsisdnMgmtHistoryTransientHome(getContext());
        home = new AdapterHome(
                getContext(), 
                home, 
                new ExtendedBeanAdapter<com.redknee.app.crm.numbermgn.MsisdnMgmtHistory, com.redknee.app.crm.bean.core.MsisdnMgmtHistory>(
                        com.redknee.app.crm.numbermgn.MsisdnMgmtHistory.class, 
                        com.redknee.app.crm.bean.core.MsisdnMgmtHistory.class));
        getContext().put(MsisdnMgmtHistoryHome.class, home);
        
    }

    private void setupMsisdns()
    {
        try
        {
            Home home = (Home)getContext().get(MsisdnHome.class);
            Msisdn msisdn = new Msisdn();
            msisdn.setMsisdn(SUB_MSISDN);
            msisdn.setTechnology(TechnologyEnum.GSM);
            msisdn.setState(MsisdnStateEnum.IN_USE);
            msisdn.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            msisdn.setSpid(SPID);
            msisdn.setBAN(ACCOUNT_BAN);
            msisdn.setGroup(OTHER_MSISDN_GROUP_ID);
            
            home.create(msisdn);
            
            msisdn = new Msisdn();
            msisdn.setMsisdn(GSM_MSISDN);
            msisdn.setTechnology(TechnologyEnum.GSM);
            msisdn.setState(MsisdnStateEnum.AVAILABLE);
            msisdn.setSpid(SPID);
            msisdn.setGroup(OTHER_MSISDN_GROUP_ID);
            
            home.create(msisdn);

            msisdn = new Msisdn();
            msisdn.setMsisdn(OTHER_SPID_MSISDN);
            msisdn.setTechnology(TechnologyEnum.GSM);
            msisdn.setState(MsisdnStateEnum.AVAILABLE);
            msisdn.setSpid(OTHER_SPID);
            msisdn.setGroup(OTHER_MSISDN_GROUP_ID);
            
            home.create(msisdn);
            
            msisdn = new Msisdn();
            msisdn.setMsisdn(HELD_BY_OTHER_ACCOUNT_MSISDN);
            msisdn.setTechnology(TechnologyEnum.GSM);
            msisdn.setState(MsisdnStateEnum.HELD);
            msisdn.setBAN(OTHER_ACCOUNT_BAN);
            msisdn.setSpid(SPID);
            msisdn.setGroup(OTHER_MSISDN_GROUP_ID);
            
            home.create(msisdn);
            
            msisdn = new Msisdn();
            msisdn.setMsisdn(CDMA_MSISDN);
            msisdn.setTechnology(TechnologyEnum.CDMA);
            msisdn.setState(MsisdnStateEnum.AVAILABLE);
            msisdn.setSpid(SPID);
            msisdn.setGroup(OTHER_MSISDN_GROUP_ID);
            
            home.create(msisdn);

            msisdn = new Msisdn();
            msisdn.setMsisdn(PREPAID_MSISDN);
            msisdn.setTechnology(TechnologyEnum.GSM);
            msisdn.setState(MsisdnStateEnum.AVAILABLE);
            msisdn.setSubscriberType(SubscriberTypeEnum.PREPAID);
            msisdn.setSpid(SPID);
            msisdn.setGroup(OTHER_MSISDN_GROUP_ID);
            
            home.create(msisdn);
            
            msisdn = new Msisdn();
            msisdn.setMsisdn(POSTPAID_MSISDN);
            msisdn.setTechnology(TechnologyEnum.GSM);
            msisdn.setState(MsisdnStateEnum.AVAILABLE);
            msisdn.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            msisdn.setSpid(SPID);
            msisdn.setGroup(OTHER_MSISDN_GROUP_ID);
            
            home.create(msisdn);        

            msisdn = new Msisdn();
            msisdn.setMsisdn(EXTERNAL_MSISDN);
            msisdn.setTechnology(TechnologyEnum.GSM);
            msisdn.setState(MsisdnStateEnum.AVAILABLE);
            msisdn.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            msisdn.setSpid(SPID);
            msisdn.setGroup(EXTERNAL_MSISDN_GROUP_ID);
            msisdn.setExternal(true);
            
            home.create(msisdn);    
            
            msisdn = new Msisdn();
            msisdn.setMsisdn(SUB_IN_ACCOUNT_MSISDN);
            msisdn.setTechnology(TechnologyEnum.GSM);
            msisdn.setState(MsisdnStateEnum.IN_USE);
            msisdn.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            msisdn.setSpid(SPID);
            msisdn.setBAN(ACCOUNT_WITH_SUBS_BAN);
            msisdn.setExternal(true);
            msisdn.setGroup(EXTERNAL_MSISDN_GROUP_ID);
            
            home.create(msisdn);
            
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Setup Spid Error: " + e.getMessage(), e).log(getContext());
        }
    }

    private void setupSpid()
    {
        try
        {
            CRMSpid spid = new CRMSpid();
            spid.setId(SPID);
            spid.setExternalMSISDNGroup(EXTERNAL_MSISDN_GROUP_ID);
            
            Home home = (Home)getContext().get(CRMSpidHome.class);
            home.create(spid);
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Setup Spid Error: " + e.getMessage(), e).log(getContext());
        }
        
    }

    private void setupAppPinManagerClient()
    {
        getContext().put(AppPinManagerClient.class, new AppPinManagerTestClient(true));
    }

    private void setupSpidHome()
    {
        Home home = new CRMSpidTransientHome(getContext());
        getContext().put(CRMSpidHome.class, home);
    }

   
    private void setupMsisdnGroup()
    {
        
        try
        {
            Home home = (Home) getContext().get(MsisdnGroupHome.class);
            MsisdnGroup group = new MsisdnGroup();
            group.setName("ExternalGroup");
            group.setId(EXTERNAL_MSISDN_GROUP_ID);
            group.setTechnology(TechnologyEnum.GSM);
            
            home.create(group);
            
            group = new MsisdnGroup();
            group.setName("OtherGroup");
            group.setId(OTHER_MSISDN_GROUP_ID);
            group.setTechnology(TechnologyEnum.GSM);
            
            home.create(group);

        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Setup MsisdnGroup Error: " + e.getMessage(), e).log(getContext());
        }
        
    }
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#tearDown()
     */
    @Override
    protected void tearDown() 
    {
        super.tearDown();
    }

    private void setupSubscriberHome()
    {
        Home home = new SubscriberTransientHome(getContext());
        getContext().put(SubscriberHome.class, home);
    }
    
    /**
     * Set up a Subscriber to provision to the AppVpnClient.
     * Set SubscriberTransientHome as SubscriberHome.class
     */
    private void setupSubscriber() throws HomeException
    {
        Home home = (Home) getContext().get(SubscriberHome.class);
        // Create the default Subscriber
        Subscriber subs = new Subscriber();
        subs.setBAN(ACCOUNT_BAN);
        subs.setId(ACCOUNT_BAN+"-1");
        subs.setMSISDN(SUB_MSISDN);
        try
        {
            home.create(getContext(), subs);
        }
        catch(HomeException he)
        {
            throw new HomeException("Failed to set up subscriber in CRM, msisdn=" + SUB_MSISDN, he);
        }
    }
    
    /**
     * Set up an AccountType with ID 123 (non-individual, converge).
     * Set AccountTypeTransientHome as AccountTypeHome.class.
     */
    private void setupAccountType() 
    {
		Home accountCategoryHome =
		    new AccountCategoryTransientHome(getContext());
        try
        {
            final AccountCategory accountType = new AccountCategory();
            accountType.setIdentifier(GROUP_ACCOUNTTYPE);
			accountCategoryHome.create(getContext(), accountType);
        }
        catch (HomeException e)
        {
            fail("Failed to create an AccountType for testing in CRM.");
        }
		getContext().put(AccountCategoryHome.class, accountCategoryHome);
        
        try
        {
            final AccountCategory accountType = new AccountCategory();
            accountType.setIdentifier(INDIVIDUAL_ACCOUNTTYPE);
            accountCategoryHome.create(getContext(), accountType);
        }
        catch (HomeException e)
        {
            fail("Failed to create an AccountType for testing in CRM.");
        }
    }
    
    private void setupAccountHome()
    {
        Home home = new AccountTransientHome(getContext());
        getContext().put(AccountHome.class, home);
    }
    
    /** 
     * Set up a VALID Account for testing.
     * @param ban - account identifier
     * @param parent - parent account identifier
     */
    private void setupAccount(Context ctx, String ban, String parent, int type) throws HomeException
    {
        try
        {
            Home home = (Home) getContext().get(AccountHome.class);
            Account account = new Account();
            account.setBAN(ban);
            account.setParentBAN(parent);
            account.setSystemType(SubscriberTypeEnum.POSTPAID);
            account.setType(type);  // Non-Individual, Converge, Mom
            account.setSpid(SPID);
            account.setVpn(false);
            account.setIcm(false);

            home.create(ctx,account);
        }
        catch(HomeException he)
        {
            throw new HomeException("Failed to set up account=" + ban + " in CRM.", he);
        }
    }
    
    
    /** 
     * Set up a VALID Subscriber for testing.
     * @param ban - account identifier
     * @param parent - parent account identifier
     */
    private void setupSubscriber(Context ctx, String ban, String subId, String msisdn) throws HomeException
    {
        try
        {
            Home home = (Home) getContext().get(SubscriberHome.class);
            Subscriber sub = new Subscriber();
            sub.setBAN(ban);
            sub.setId(subId);
            sub.setMSISDN(msisdn);
            sub.setSubscriptionType(SubscriptionType.getINSubscriptionType(ctx).getId());
            
            MsisdnManagement.associateMsisdnWithSubscription(getContext(), msisdn, sub, "voiceMsisdn");
            Thread.sleep(100); // Too quick and the association will not be ready by the time we start the test.
            home.create(ctx,sub);
        }
        catch(HomeException he)
        {
            throw new HomeException("Failed to set up subscriber=" + subId + " in CRM.", he);
        }
        catch (InterruptedException e)
        {
            throw new HomeException("Unable to sleep, encounterd a InterruptedException", e);
        }
    }
    
    public void testClaimMsisdnThatsAlreadyOwned() throws HomeException
    {
        try
        {
            MsisdnManagement.claimMsisdn(getContext(), SUB_MSISDN, ACCOUNT_BAN, false, "testClaimMsisdnThatsAlreadyOwned()");
            fail("ClaimMsisdn call succeeded when trying to claim a MSISDN that is already claimed by the specified Account.  Expected a MsisdnAlreadyAcquiredException.");
        }
        catch (HomeException he)
        {
            fail("Encountered a HomeException when trying to claim a MSISDN that is already claimed by the specified Account.  Expected a MsisdnAlreadyAcquiredException. [message=" + he.getMessage() + "]");
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            // pass
            return;
        }
    }
    
    public void testClaimMsisdnWhereMsisdnIsHeldBySameAccount() throws HomeException
    {
        try
        {
            MsisdnManagement.claimMsisdn(getContext(), HELD_BY_OTHER_ACCOUNT_MSISDN, OTHER_ACCOUNT_BAN, false, "testClaimMsisdnWhereMsisdnIsHeldBySameAccount()");
        }
        catch (HomeException he)
        {
            fail("Encountered a HomeException when trying to acquire a currently held msisdn that was held by the same account.  Expected to successfully claim the msisdn. [message=" + he.getMessage() + "]");
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            fail("Encountered a MsisdnAlreadyAcquiredException when trying to acquire a currently held msisdn that was held by the same account.  Expected to successfully claim the msisdn.");
        }
        Msisdn msisdn = MsisdnSupport.getMsisdn(getContext(), HELD_BY_OTHER_ACCOUNT_MSISDN);
        assertSame(msisdn.getState(), MsisdnStateEnum.IN_USE);
        assertSame(msisdn.getBAN(), OTHER_ACCOUNT_BAN);
    }
    
    public void testClaimMsisdnWhereSystemTypeDoesntMatch() throws HomeException
    {
        try
        {
            MsisdnManagement.claimMsisdn(getContext(), PREPAID_MSISDN, ACCOUNT_BAN, false, "testClaimMsisdnWhereSystemTypeDoesntMatch()");
            fail("ClaimMsisdn call succeeded when trying to aquire a Prepaid MSISDN for Postpaid Account.  Expected to get a HomeException.");
        }
        catch (HomeException he)
        {
            //pass
            return;
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            fail("Encountered a MsisdnAlreadyAquiredException when trying to aquire a Prepaid MSISDN for Postpaid Account.  Expected to get a HomeException.");
        }
    }

    public void testClaimMsisdnThatsHeldByAnotherAccount() throws HomeException
    {
        try
        {
            MsisdnManagement.claimMsisdn(getContext(), HELD_BY_OTHER_ACCOUNT_MSISDN, ACCOUNT_BAN, false, "testClaimMsisdnThatsHeldByAnotherAccount()");
            fail("ClaimMsisdn call succeeded when trying to aquire a held MSISDN for a different account.  Expected to get a HomeException.");
        }
        catch (HomeException he)
        {
            //pass
            return;
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            fail("Encountered a MsisdnAlreadyAquiredException when trying to aquire a held MSISDN for a different account.  Expected to get a HomeException.");
        }
    }
    
    public void testClaimMsisdnWhereAccountDoesntExist() throws HomeException
    {
        try
        {
            MsisdnManagement.claimMsisdn(getContext(), SUB_MSISDN, NON_EXISTING_ACCOUNT_BAN, false, "testClaimMsisdnWhereAccountDoesntExist()");
            fail("ClaimMsisdn call succeeded when trying to aquire a held MSISDN for a non-existent account.  Expected to get a HomeException.");
        }
        catch (HomeException he)
        {
            //pass
            return;
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            fail("Encountered a MsisdnAlreadyAquiredException when trying to aquire a held MSISDN for a non-existent account.  Expected to get a HomeException.");
        }
    }
    
    public void testClaimMsisdnWhereSpidDoesntMatch() throws HomeException
    {
        try
        {
            MsisdnManagement.claimMsisdn(getContext(), OTHER_SPID_MSISDN, ACCOUNT_BAN, false, "testClaimMsisdnWhereSpidDoesntMatch()");
            fail("ClaimMsisdn call succeeded when trying to aquire a MSISDN that is of a different SPID than the account.  Expected to get a HomeException.");
        }
        catch (HomeException he)
        {
            //pass
            return;
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            fail("Encountered a MsisdnAlreadyAquiredException when trying to aquire a MSISDN that is of a different SPID than the account.  Expected to get a HomeException.");
        }
    }

    public void testClaimMsisdnWhereInternalMsisdnDoesntExist() throws HomeException
    {
        try
        {
            MsisdnManagement.claimMsisdn(getContext(), NON_EXISTING_MSISDN, ACCOUNT_BAN, false, "testClaimMsisdnWhereInternalMsisdnDoesntExist()");
            fail("ClaimMsisdn call succeeded when trying to aquire a Internal MSISDN that doesn't exist.  Expected to get a HomeException.");
        }
        catch (HomeException he)
        {
            //pass
            return;
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            fail("Encountered a MsisdnAlreadyAquiredException when trying to aquire a Internal MSISDN that doesn't exist.  Expected to get a HomeException.");
        }
    }
    
    public void testClaimExternalMsisdnWhereExternalMsisdnDoesntExist() throws HomeException
    {
        try
        {
            MsisdnManagement.claimMsisdn(getContext(), NON_EXISTING_MSISDN, ACCOUNT_BAN, true, "testClaimExternalMsisdnWhereExternalMsisdnDoesntExist()");
        }
        catch (HomeException he)
        {
            fail("Encountered a HomeException when trying to aquire a External MSISDN that doesn't exist.  Expected to successfully claim the misisdn.");
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            fail("Encountered a MsisdnAlreadyAquiredException when trying to aquire a Internal MSISDN that doesn't exist.  Expected to successfully claim the msisdn.");
        }
        Msisdn msisdn = MsisdnSupport.getMsisdn(getContext(), NON_EXISTING_MSISDN);
        assertNotNull("ClaimMsisdn succeeded but the Msisdn isn't in the MsisdnHome", msisdn);
        assertSame("ClaimMsisdn succeeded but the BAN in the Msisdn bean isn't set to the BAN that claimed it", msisdn.getBAN(), ACCOUNT_BAN);
        assertSame("ClaimMsisdn succeeded but the state of the MSISDN didn't change to IN_USE", msisdn.getState(), MsisdnStateEnum.IN_USE);
        assertTrue("ClaimMsisdn succeeded but the Msisdn isn't marked as External", msisdn.isExternal());
    }
    
    public void testClaimExternalMsisdnWhereExternalMsisdnExist() throws HomeException
    {
        try
        {
            MsisdnManagement.claimMsisdn(getContext(), EXTERNAL_MSISDN, ACCOUNT_BAN, true, "testClaimExternalMsisdnWhereExternalMsisdnExist()");
        }
        catch (HomeException he)
        {
            fail("Encountered a HomeException when trying to aquire a External MSISDN that doesn't exist.  Expected to successfully claim the misisdn.");
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            fail("Encountered a MsisdnAlreadyAquiredException when trying to aquire a Internal MSISDN that doesn't exist.  Expected to successfully claim the msisdn.");
        }
        Msisdn msisdn = MsisdnSupport.getMsisdn(getContext(), EXTERNAL_MSISDN);
        assertNotNull("ClaimMsisdn succeeded but the Msisdn isn't in the MsisdnHome", msisdn);
        assertSame("ClaimMsisdn succeeded but the BAN in the Msisdn bean isn't set to the BAN that claimed it", msisdn.getBAN(), ACCOUNT_BAN);
        assertSame("ClaimMsisdn succeeded but the state of the MSISDN didn't change to IN_USE", msisdn.getState(), MsisdnStateEnum.IN_USE);
        assertTrue("ClaimMsisdn succeeded but the Msisdn isn't marked as External", msisdn.isExternal());
    }
    
    public void testClaimMsisdnInternalMsisdnWhichIsCurrentlyExternal() throws HomeException
    {
        try
        {
            MsisdnManagement.claimMsisdn(getContext(), EXTERNAL_MSISDN, ACCOUNT_BAN, false, "testClaimMsisdnInternalMsisdnWhichIsCurrentlyExternal()");
            fail("ClaimMsisdn call succeeded when trying claim a External MSISDN as a Internal MSISDN.  Expected to get a HomeException.");
        }
        catch (HomeException he)
        {
            //pass
            return;
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            fail("Encountered a MsisdnAlreadyAquiredException when trying claim a External MSISDN as a Internal MSISDN.  Expected to get a HomeException.");
        }
    }
    
    public void testClaimMsisdnExternalMsisdnWhichIsCurrentlyInternal() throws HomeException
    {
        try
        {
            MsisdnManagement.claimMsisdn(getContext(), GSM_MSISDN, ACCOUNT_BAN, true, "testClaimMsisdnExternalMsisdnWhichIsCurrentlyInternal()");
            fail("ClaimMsisdn call succeeded when trying claim a Internal MSISDN as a External MSISDN.  Expected to get a HomeException.");
        }
        catch (HomeException he)
        {
            //pass
            return;
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            fail("Encountered a MsisdnAlreadyAquiredException when trying claim a Internal MSISDN as a External MSISDN.  Expected to get a HomeException.");
        }
    }  
    
    public void testClaimMsisdnByGroupAccount() throws HomeException
    {
        try
        {
            try
            {
                //Change the Account Type to Group Account.
                Account acct = AccountSupport.getAccount(getContext(), ACCOUNT_BAN);
                acct.setType(GROUP_ACCOUNTTYPE);
                Home home = (Home)getContext().get(AccountHome.class);
                home.store(acct);
            }
            catch(HomeException e)
            {
                fail("Failed to setup test case due to " + e.getMessage());
            }
            
            MsisdnManagement.claimMsisdn(getContext(), GSM_MSISDN, ACCOUNT_BAN, true, "testClaimMsisdnByGroupAccount()");
            fail("ClaimMsisdn call succeeded when trying claim by a Group Account.  Expected to get a HomeException.");
        }
        catch (HomeException he)
        {
            //pass
            return;
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            fail("Encountered a MsisdnAlreadyAquiredException when trying claim by a Group Account.  Expected to get a HomeException.");
        }
    }  
    
    public void testReleaseMsisdnSuccess() throws HomeException
    {
        try
        {
            MsisdnManagement.releaseMsisdn(getContext(), SUB_MSISDN, ACCOUNT_BAN, "testReleaseMsisdnSuccess()");
        }
        catch (HomeException he)
        {
            fail("Encountered a HomeException when trying to release MSISDN.  Expected successful release. [message=" + he.getMessage() + "]");
        }
        Msisdn msisdn = MsisdnSupport.getMsisdn(getContext(), SUB_MSISDN);
        assertNotNull("ClaimMsisdn succeeded but the Msisdn isn't in the MsisdnHome", msisdn);
        assertSame("ClaimMsisdn succeeded but the BAN in the Msisdn bean isn't set to the BAN that claimed it", msisdn.getBAN(), ACCOUNT_BAN);
        assertSame("ClaimMsisdn succeeded but the state of the MSISDN didn't change to HELD", msisdn.getState(), MsisdnStateEnum.HELD);
    }
    
    
    public void testReleaseMsisdnThatIsntOwnedByTheAccount() throws HomeException
    {
        try
        {
            MsisdnManagement.releaseMsisdn(getContext(), HELD_BY_OTHER_ACCOUNT_MSISDN, ACCOUNT_BAN, "testReleaseMsisdnThatIsntOwnedByTheAccount()");
            fail("Successfully released MSISDN where specified Account doesn't own the Msisdn.  Expected HomeException.");
        }
        catch (HomeException he)
        {
            // pass
            return;
        }
    }
    
    public void testReleaseMsisdnThatDoesntExist() throws HomeException
    {
        try
        {
            MsisdnManagement.releaseMsisdn(getContext(), NON_EXISTING_MSISDN, ACCOUNT_BAN, "testReleaseMsisdnThatDoesntExist()");
            fail("Successfully released MSISDN which doesn't exist.  Expected HomeException.");
        }
        catch (HomeException he)
        {
            // pass
            return;
        }
    }

    public void testReleaseMsisdnThatHasSubscriptions() throws HomeException
    {
        try
        {
            MsisdnManagement.releaseMsisdn(getContext(), SUB_IN_ACCOUNT_MSISDN, ACCOUNT_WITH_SUBS_BAN, "testReleaseMsisdnThatHasSubscriptions()");
            fail("Successfully released MSISDN which still has subscriptions associated with it.  Expected HomeException.");
        }
        catch (HomeException he)
        {
            // pass
            return;
        }
    }    
    
    private static String ACCOUNT_BAN = "10001";
    private static String OTHER_ACCOUNT_BAN = "10002";
    private static String NON_EXISTING_ACCOUNT_BAN = "10003";
    private static String ACCOUNT_WITH_SUBS_BAN = "10004";

    private static String SUB_ID = ACCOUNT_WITH_SUBS_BAN + "-1";
    
    private static String SUB_MSISDN = "8881234561";
    private static String GSM_MSISDN = "8881234562";
    private static String CDMA_MSISDN = "8881234563";
    private static String HELD_BY_OTHER_ACCOUNT_MSISDN = "8881234564";
    private static String EXTERNAL_MSISDN = "8881234565";
    private static String PREPAID_MSISDN = "8881234566";
    private static String POSTPAID_MSISDN = "8881234567";
    private static String OTHER_SPID_MSISDN = "8881234568";
    private static String NON_EXISTING_MSISDN = "8881234569";
    private static String SUB_IN_ACCOUNT_MSISDN = "8881234570";
    
    private static int EXTERNAL_MSISDN_GROUP_ID = 1;
    private static int OTHER_MSISDN_GROUP_ID = 2;
    
    private static final int SPID = 1;
    private static final int OTHER_SPID = 2;
    
    private static final int GROUP_ACCOUNTTYPE = 120;
    private static final int INDIVIDUAL_ACCOUNTTYPE = 130;
    
}
