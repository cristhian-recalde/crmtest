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

import com.trilogy.framework.xhome.beans.DefaultExceptionListener;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagementHome;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagementTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.client.AppPinManagerClient;
import com.trilogy.app.crm.client.AppPinManagerTestClient;
import com.trilogy.app.crm.home.sub.SubscriberPinManagerUpdateHome;
import com.trilogy.app.crm.home.sub.SubscriberVoiceMsisdnHome;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;

public class TestCrmPinManagement extends ContextAwareTestCase 
{
    public TestCrmPinManagement(String name)
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
        final TestSuite suite = new TestSuite(TestCrmPinManagement.class);
        return suite;
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#setUp()
     */
    protected void setUp() 
    {
        super.setUp();
        /*
         * TestSetupMobileNumbers.setup(getContext()) is done by 
         * TestSetupAccountHierarchy.setup(Context, boolean)
         */
        TestSetupAccountHierarchy.setup(getContext(), false);
        //Install the home decorator that updates PIN Manager
        Home subHome = (Home)getContext().get(SubscriberHome.class);
        //Install the Home decaorator that updates the Subscription Association/Disassociation.
        subHome = new SubscriberPinManagerUpdateHome(getContext(), subHome);
        subHome = new SubscriberVoiceMsisdnHome(subHome);
        //Home to simulate the SubscriberPipeLineContextPrepareHome
        subHome = new HomeProxy(getContext(), subHome)
        {
            //Used to set context for the Subscriber.
            public Object create(Context ctx, Object obj)
                throws HomeException, HomeInternalException
            {
                Subscriber sub = (Subscriber) obj;
                sub.setContext(ctx);
                return getDelegate(ctx).create(ctx, sub);
            }    
            
            public Object store(Context ctx, Object obj)
                throws HomeException, HomeInternalException
            {
                Subscriber bean = (Subscriber) obj;
                Subscriber sub = SubscriberSupport.getSubscriber(getContext(), bean.getId());
                getContext().put(Lookup.OLDSUBSCRIBER, sub);
                
                return getDelegate(ctx).store(ctx, obj);
            }
        };  
        getContext().put(SubscriberHome.class, subHome);
        
        Home pinHome = new AcquiredMsisdnPINManagementTransientHome(getContext());
        getContext().put(AcquiredMsisdnPINManagementHome.class, pinHome);
        
        //Install Pin Manager with connection up.
        setupAppPinManagerClient(true);
        
        try
        {
            TestSetupAccountHierarchy.setupSubscriber(getContext(), TestSetupAccountHierarchy.ACCOUNT1_BAN, 
                SUB_ID, SubscriberTypeEnum.PREPAID, SUB_MSISDN,
                SubscriberStateEnum.AVAILABLE, TestSetupAccountHierarchy.START_DATE);
        }
        catch(HomeException e)
        {
            fail("Failed setup due to " + e.getMessage());
        }
        
        //Install an exception listener
        getContext().put(ExceptionListener.class, new DefaultExceptionListener());
    }
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#tearDown()
     */
    protected void tearDown() 
    {
        super.tearDown();
    }
    
    private void setupAppPinManagerClient(boolean isConnected)
    {
        getContext().put(AppPinManagerClient.class, new AppPinManagerTestClient(isConnected));
    }
    
    private int getPINValue(String msisdn)
    {
        AppPinManagerTestClient client = (AppPinManagerTestClient) getContext().get(AppPinManagerClient.class);
        return client.retrieveTestPINValue(msisdn).intValue();
    }

    public void testSetup()
    {
        TestSetupAccountHierarchy.testSetup(getContext());
        assertNotNull(getContext().get(AppPinManagerClient.class));
    }
    
    public void testMsisdnManagementClaimReleaseMsisdn()
    {
        String ban = TestSetupAccountHierarchy.getIndividualAccountIdentifier(TestSetupAccountHierarchy.SUB1_ID);
        String msisdn = "333333330";
        {
            int originalPINValue = getPINValue(msisdn);
            try
            {
                MsisdnManagement.claimMsisdn(getContext(), msisdn, ban, true, "");
            }
            catch(MsisdnAlreadyAcquiredException e)
            {
                fail("Failed test setup." + e.getMessage());
            }
            catch(HomeException e)
            {
                fail("Failed test setup." + e.getMessage());
            }
            int currentPINValue = getPINValue(msisdn);
            assertEquals("MsisdnManagement.claimMsisdn is not supposed to change the MSISDN's PIN.",
                    originalPINValue, currentPINValue);
        }
        {
            int originalPINValue = getPINValue(msisdn);
            try
            {
                MsisdnManagement.releaseMsisdn(getContext(), msisdn, ban, "");

            }
            catch(HomeException e)
            {
                fail("Failed test setup." + e.getMessage());
            }
            int currentPINValue = getPINValue(msisdn);
            assertEquals("MsisdnManagement.releaseMsisdn is not supposed to change the MSISDN's PIN.",
                    originalPINValue, currentPINValue);
        }
    }
    
    public void testSubscriberPinManagerUpdateHomeActivateSubscription()
    {
        try
        {
            Subscriber sub = SubscriberSupport.getSubscriber(getContext(), SUB_ID);
            Home home = (Home)getContext().get(SubscriberHome.class);

            {
                final int originalPINValue = getPINValue(sub.getMSISDN());
                //check PIN is previously unprovisioned.
                assertTrue("Original PIN was expected to be unprovisioned", originalPINValue == 0);
                
                //Save the subscription with no change in state nor MSISDN change
                home.store(getContext(), sub);

                final int currentPINValue = getPINValue(sub.getMSISDN());

                assertEquals(originalPINValue, currentPINValue);
            }

            {
                final int originalPINValue = getPINValue(sub.getMSISDN());
                //check PIN is previously provisioned.
                assertTrue("Original PIN was expected to be unprovisioned", originalPINValue == 0);
                
                //Activate Subscriber
                sub.setState(SubscriberStateEnum.ACTIVE);
                home.store(getContext(), sub);

                final int currentPINValue = getPINValue(sub.getMSISDN());

                //check the new PIN is provisioned
                assertTrue(currentPINValue > 0);
                //Check the PIN was provisioned just once
                assertEquals("PIN was not provisioned in Pin Manager", 
                        AppPinManagerTestClient.provision(originalPINValue).intValue(), currentPINValue);
            }
            
            {
                final int originalPINValue = getPINValue(sub.getMSISDN());
                //check PIN is previously provisioned.
                assertTrue("Original PIN was expected to be provisioned", originalPINValue > 0);
                
                //Deactivate the Subscriber
                sub.setState(SubscriberStateEnum.INACTIVE);
                home.store(getContext(), sub);

                final int currentPINValue = getPINValue(sub.getMSISDN());

                //check the new PIN is provisioned
                assertTrue(currentPINValue < 0);
                //Check the PIN was unprovisioned (deleted) just once
                assertEquals("PIN was not unprovisioned (deleted) in Pin Manager", 
                        AppPinManagerTestClient.unprovision(originalPINValue).intValue(), currentPINValue);
            }
        }
        catch(HomeException e)
        {
            fail("Failed test setup " + e.getMessage());
        }
    }
    
    /**
     * Test deactivating subscription when there still remains more associations.
     */
    public void testSubscriberPinManagerUpdateHomeDeactivateSubscription()
    {
        try
        {
            //Setup other associations with the same MSISDN: Mobile wallet.
            TestSetupAccountHierarchy.setupSingleSubscriberSubscription(getContext(), 
                    TestSetupAccountHierarchy.getIndividualAccountIdentifier(SUB_ID), SUB_ID+"-2", 
                    SubscriberTypeEnum.PREPAID, SUB_MSISDN,
                    SubscriberStateEnum.AVAILABLE, TestSetupAccountHierarchy.START_DATE, 
                    SubscriptionTypeEnum.MOBILE_WALLET_INDEX);
            
            Subscriber sub = SubscriberSupport.getSubscriber(getContext(), SUB_ID);
            Home home = (Home)getContext().get(SubscriberHome.class);
    
            {
                final int originalPINValue = getPINValue(sub.getMSISDN());
                //check PIN is previously provisioned.
                assertTrue("New MSISDN's PIN was expected to be unprovisioned", originalPINValue == 0);
                
                //Deactivate the Subscriber
                sub.setState(SubscriberStateEnum.INACTIVE);
                home.store(getContext(), sub);
    
                final int currentPINValue = getPINValue(sub.getMSISDN());
    
                //check the new PIN is provisioned
                assertEquals("Expected no change in PIN since there are still associations",
                        originalPINValue, currentPINValue);
            }
        }
        catch(HomeException e)
        {
            fail("Failed test setup " + e.getMessage());
        }
    }

    public void testSubscriberPinManagerUpdateHomeChangeMSISDN()
    {
        try
        {
            Subscriber sub = SubscriberSupport.getSubscriber(getContext(), SUB_ID);
            Home home = (Home)getContext().get(SubscriberHome.class);
            //Activate Subscriber
            sub.setState(SubscriberStateEnum.ACTIVE);
            home.store(getContext(), sub);
    
            {
                //New MSISDN is not expected to be provisioned on Pin Manager
                final String newMsisdn = "77777777"; 
                final int newMsisdnPIN =  getPINValue(newMsisdn);
                assertTrue("New MSISDN's PIN was expected to be unprovisioned", newMsisdnPIN == 0);
    
                //Original MSISDN is already provisioned in Pin Manager 
                final int originalPINValue = getPINValue(SUB_MSISDN);
                //check PIN is previously provisioned.
                assertTrue("Original PIN was expected to be provisioned", originalPINValue > 0);
                
                /* The new MSISDN must already be created in the system.  We will use MsisdnManagement.claimMsisdn to
                 * take care of creating the "external" MSISDN and claiming it for the account.  
                 * In a previous test testMsisdnManagementClaimReleaseMsisdn verified that the 
                 * Claim logic doesn't change the PIN. */
                try
                {
                    MsisdnManagement.claimMsisdn(getContext(), newMsisdn, sub.getBAN(), true, "Unit-Test-testSubscriberPinManagerUpdateHomeChangeMSISDN");
                }
                catch(MsisdnAlreadyAcquiredException e)
                {
                    // ignore this exception.
                }
                
                //Change MSISDN and Save.
                sub.setMSISDN(newMsisdn);
                home.store(getContext(), sub);
    
                //PIN of new MSISDN
                final int currentPINValue = getPINValue(sub.getMSISDN());
    
                //check the new PIN is provisioned
                assertTrue(currentPINValue > 0);
                //check the PIN was provisioned only once
                assertEquals("PIN was generated more than once.", 
                        AppPinManagerTestClient.provision(newMsisdnPIN).intValue(), currentPINValue);

                //Check the old PIN was deleted
                final int oldPINValueOnPM = getPINValue(SUB_MSISDN);
                assertTrue("PIN not deleted", oldPINValueOnPM < 0);
                //check it was unprovisioned (deleted) just one.
                assertEquals("PIN was unprovisioned (deleted) in Pin Manager more than once", 
                        AppPinManagerTestClient.unprovision(originalPINValue).intValue(), oldPINValueOnPM);
            }
        }
        catch(HomeException e)
        {
            fail("Failed test setup " + e.getMessage());
        }
    }
    
    /**
     * Test Change MSISDN for subscription when there still remains more associations.
     */
    public void testSubscriberPinManagerUpdateHomeChangeMSISDNWithAssociations()
    {
        try
        {
            //Setup other associations with the same MSISDN: Mobile wallet.
            TestSetupAccountHierarchy.setupSingleSubscriberSubscription(getContext(), 
                    TestSetupAccountHierarchy.getIndividualAccountIdentifier(SUB_ID), SUB_ID+"-2", 
                    SubscriberTypeEnum.PREPAID, SUB_MSISDN,
                    SubscriberStateEnum.AVAILABLE, TestSetupAccountHierarchy.START_DATE, 
                    SubscriptionTypeEnum.MOBILE_WALLET_INDEX);
            
            Subscriber sub = SubscriberSupport.getSubscriber(getContext(), SUB_ID);
            Home home = (Home)getContext().get(SubscriberHome.class);
            //Activate Subscriber
            sub.setState(SubscriberStateEnum.ACTIVE);
            home.store(getContext(), sub);
            getContext().put(Lookup.OLDSUBSCRIBER, sub);
            
            {
                //New MSISDN is not expected to be provisioned on Pin Manager
                final String newMsisdn = "77777777"; 
                final int newMsisdnPIN =  getPINValue(newMsisdn);
                assertTrue("New MSISDN's PIN was expected to be unprovisioned", newMsisdnPIN == 0);
    
                //Original MSISDN is already provisioned in Pin Manager 
                final int originalPINValue = getPINValue(SUB_MSISDN);
                //check PIN is previously provisioned.
                assertTrue("Original PIN was expected to be provisioned", originalPINValue > 0);
                
                /* The new MSISDN must already be created in the system.  We will use MsisdnManagement.claimMsisdn to
                 * take care of creating the "external" MSISDN and claiming it for the account.  
                 * In a previous test testMsisdnManagementClaimReleaseMsisdn verified that the 
                 * Claim logic doesn't change the PIN. */
                try
                {
                    MsisdnManagement.claimMsisdn(getContext(), newMsisdn, sub.getBAN(), true, "Unit-Test-testSubscriberPinManagerUpdateHomeChangeMSISDN");
                }
                catch(MsisdnAlreadyAcquiredException e)
                {
                    // ignore this exception.
                }
                
                //Change MSISDN and Save.
                sub.setMSISDN(newMsisdn);
                home.store(getContext(), sub);
    
                //PIN of new MSISDN
                final int currentPINValue = getPINValue(newMsisdn);
    
                //check the new PIN is provisioned
                assertTrue(currentPINValue > 0);
                //check the PIN was provisioned only once
                assertEquals("PIN was generated more than once.", 
                        AppPinManagerTestClient.provision(newMsisdnPIN).intValue(), currentPINValue);
                
                //Check the old PIN still remains
                final int oldPINNewValue = getPINValue(SUB_MSISDN);
                assertTrue(oldPINNewValue > 0);
                //check the old PIN remains the same original PIN
                assertEquals("PIN for original MSISDN changed when it should not have", 
                        originalPINValue, oldPINNewValue);
            }
        }
        catch(HomeException e)
        {
            fail("Failed test setup " + e.getMessage());
        }
    }
    
    public void testPinManagerConnectionDown()
    {
        //Install a connection to a PIN Manager which is down.
        setupAppPinManagerClient(false);
        /* This means that all PINs previously registered are now deleted.  
         * Luckily we don't need it for this test. */
        Subscriber sub = null;
        Home home = null;
        try
        {
            sub = SubscriberSupport.getSubscriber(getContext(), SUB_ID);
            home = (Home)getContext().get(SubscriberHome.class);
        }
        catch(HomeException e)
        {
            fail("Failed test setup " + e.getMessage());
        }   
        
        try
        {
            {
                final int originalPINValue = getPINValue(sub.getMSISDN());
                //check PIN is previously provisioned.
                assertTrue("Original PIN was expected to be unprovisioned", originalPINValue == 0);
                
                //Activate Subscriber
                sub.setState(SubscriberStateEnum.ACTIVE);
                //This home doesn't have much more than the PIN update decorator.
                home.store(getContext(), sub);
                
                DefaultExceptionListener el = (DefaultExceptionListener) getContext().get(ExceptionListener.class);
                assertTrue("Errors due to connection being down were supposed to be logged. ", el.hasErrors());
            }
        }
        catch(HomeException e)
        {
            e.printStackTrace();
            fail("Errors during PIN generation are supposed to be quietly logged to the application.  " +
                    "But this error is stopping the Subscriber Update: " + e.getMessage());
        }
    }

    final static String SUB_MSISDN = "333333333";
    final static String SUB_ID = "99922201-1";
    
}
