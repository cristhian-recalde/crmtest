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
package com.trilogy.app.crm.api.rmi.impl;

import java.security.Principal;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.api.rmi.impl.MobileNumbersImplTest.FakeAuthSPI;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateHome;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateTransientHome;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesTransientHome;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.contract.SubscriptionContractHome;
import com.trilogy.app.crm.contract.SubscriptionContractTermHome;
import com.trilogy.app.crm.contract.SubscriptionContractTermTransientHome;
import com.trilogy.app.crm.contract.SubscriptionContractTransientHome;
import com.trilogy.app.crm.home.sub.DestinationStateByTypeCreateHome;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestFakeLicenseMgr;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;
import com.trilogy.app.crm.unit_test.TestSetupAdjustmentTypes;
import com.trilogy.app.crm.unit_test.TestSetupInvoiceHistory;
import com.trilogy.app.crm.unit_test.TestSetupPricePlanAndServices;
import com.trilogy.app.crm.unit_test.TestSetupTransactions;
import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.CRMException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ValidationException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ValidationExceptionEntry;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackage;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.MutableSubscriptionBilling;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionBilling;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionState;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionStateEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionStatus;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.MutableSubscriptionProfile;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.SubscriptionProfile;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionPricePlan;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionProfileQueryResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionRating;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;

public class SubscribersImplTest extends ContextAwareTestCase 
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public SubscribersImplTest(final String name)
    {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * {@inheritDoc}
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(SubscribersImplTest.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
        super.setUp();
        try
        {
            impl_ = new SubscribersImpl(getContext());
        }
        catch(Exception e)
        {
            fail("Failed Impl setup. " + e.getMessage());
        }
        
        // Install Test Authenticator
        getContext().put(AuthSPI.class, new FakeAuthSPI());
        getContext().put(Principal.class, new User());
        TestSetupAccountHierarchy.setup(getContext(), testWithXDB);
        
        /* Decorate SubscriberHome.  This introduces even more setup (Licenses) which I'm not going to
         * bother with just for a Unit Test.  */
        /*
        Home subHome = (Home) getContext().get(SubscriberHome.class);
        final SubscriberHomeFactory subHomeFactory = new SubscriberHomeFactory(subHome);
        Home subscriberHome = subHomeFactory.createPipeline(getContext(), getContext());
        getContext().put(SubscriberHome.class, subscriberHome);
        */
        //Decorate the Subscriber Pipeline with minimum decorators to simulate pipeline behaviour.
        Home subHome = (Home) getContext().get(SubscriberHome.class);
        subHome = new DestinationStateByTypeCreateHome(subHome);
        getContext().put(SubscriberHome.class, subHome);
        //Decorate the Account Pipeline to deal with Individual Subscriber Account Creation.
        Home acctHome = (Home) getContext().get(AccountHome.class);
        getContext().put(AccountHome.class, acctHome);
        
        TestSetupAdjustmentTypes.setup(getContext());
        TestSetupInvoiceHistory.setup(getContext());
        TestSetupTransactions.setup(getContext(), false);
        TestSetupPricePlanAndServices.setup(getContext());
        
        //Create SCT
        setupSCT(getContext());
        
        //Configure SPID
        configureSpid(getContext());
        
        //Install Fake License Manager
        getContext().put(LicenseMgr.class, new TestFakeLicenseMgr());
        //Setup Subscriber Services
        getContext().put(SubscriberServicesHome.class, new SubscriberServicesTransientHome(getContext()));
        getContext().put(SubscriptionContractHome.class, new SubscriptionContractTransientHome(getContext()));
        getContext().put(SubscriptionContractTermHome.class, new SubscriptionContractTermTransientHome(getContext()));
    }
    
    private void configureSpid(Context context)
    {
        try
        {
            Home home = (Home)context.get(CRMSpidHome.class);
            CRMSpid spid = (CRMSpid) home.find(context, Integer.valueOf(TestSetupAccountHierarchy.SPID_ID));
            spid.setAutoCreateCardPackage(true);
            spid.setAutoCreateMSISDN(true);
            spid.setAutoCreateMSISDNGroup(1);
            spid.setAllowToSpecifySubscriberId(true);
            spid.setAllowToSpecifyBAN(true);
            home.store(context, spid);
        }
        catch (Exception e)
        {
            fail("Failed to configure Service Provider.");
        }
    }
    
    private void setupSCT(Context context)
    {
        context.put(SctAuxiliaryServiceHome.class, new SctAuxiliaryServiceTransientHome(context));
        context.put(SubscriberAuxiliaryServiceHome.class, new SubscriberAuxiliaryServiceTransientHome(context));
        // Doesn't support Hybrid Prepaid
        SysFeatureCfg cfg;
        try
        {
            cfg = (SysFeatureCfg) XBeans.instantiate(SysFeatureCfg.class, context);
        }
        catch (Exception e)
        {
            new DebugLogMsg(this, InstantiationException.class.getSimpleName() + " occurred in " + SubscribersImplTest.class.getSimpleName() + ".setupSCT(): " + e.getMessage(), e).log(context);
            cfg = new SysFeatureCfg();
        }
        context.put(SysFeatureCfg.class, cfg);
        
        Home home =  new ServiceActivationTemplateTransientHome(getContext());
        context.put(ServiceActivationTemplateHome.class, home);
        
        ServiceActivationTemplate sct;
        try
        {
            sct = (ServiceActivationTemplate) XBeans.instantiate(ServiceActivationTemplate.class, context);
        }
        catch (Exception e)
        {
            new DebugLogMsg(this, InstantiationException.class.getSimpleName() + " occurred in " + SubscribersImplTest.class.getSimpleName() + ".setupSCT(): " + e.getMessage(), e).log(context);
            sct = new ServiceActivationTemplate();
        }
        sct.setIdentifier(TEMPLATE_ID);
        sct.setName("CRM API Subscription Creation Template");
        sct.setInitialBalance(10000L);
        sct.setMaxBalance(10000L);
        sct.setMaxRecharge(1000L);
        sct.setTechnology(TechnologyEnum.GSM);
        sct.setPricePlan(TestSetupPricePlanAndServices.DEFAULT_PRICE_PLAN_ID);
        sct.setSpid(TestSetupAccountHierarchy.SPID_ID);
        sct.setAuxiliaryServices(new ArrayList());
        try
        {
            home.create(context, sct);
        }
        catch (Exception e)
        {
            fail("Failed setting up SCT");
        }
    }
    
    @Override
    public void tearDown()
    {
        try
        {
            TestSetupTransactions.deleteTransactions(getContext());
            TestSetupInvoiceHistory.tearDown(getContext());
            TestSetupAccountHierarchy.completelyTearDown(getContext());
            
            /* TODO: delete all Subscribers and Accounts created in this test.  The TestSetupAccountHierarchy only 
             * deletes those created in that test suite.
             */
            
            //TestSetupPricePlanAndServices tear down
            super.tearDown();
        }
        catch (Exception e)
        {
            //Do nothing. Test is over
        }
    }
    
    
    
    private SubscriptionProfile createSubscriptionProfile(String accountID, String cardPackageID, String identifier, PaidType paidType, String msisdn)
    {
        SubscriptionProfile profile = new SubscriptionProfile();
        profile.setAccountID(accountID);
        profile.setCardPackageID(cardPackageID);
        profile.setIdentifier(identifier);
        profile.setSpid(1);
        profile.setTechnologyType(TechnologyTypeEnum.GSM.getValue());
        profile.setPaidType(paidType);
        profile.setMobileNumber(msisdn);
        return profile;
    }
    
    private SubscriptionStatus createStatus(SubscriptionState state)
    {
        SubscriptionStatus status = new SubscriptionStatus();
        status.setState(state);
        return status;
    }
    
    private SubscriptionRating createRating()
    {
        SubscriptionRating rating = new SubscriptionRating();
        rating.setPrimaryPricePlanID(TestSetupPricePlanAndServices.DEFAULT_PRICE_PLAN_ID);
        return rating;
    }
    
    private SubscriptionBilling createBilling()
    {
        SubscriptionBilling billing = new SubscriptionBilling();
        billing.setBillingLanguage("en");
        billing.setChargeToPostpaid(Boolean.FALSE);
        return billing;
    }
    private SubscriptionPricePlan creatPricePlanOptions()
    {
        SubscriptionPricePlan options = new SubscriptionPricePlan();
        
        return options;
    }
    
    private SubscriptionReference createSubscriptionReference(final String subId)
    {
        SubscriptionReference ref = new SubscriptionReference();
        ref.setIdentifier(subId);
        return ref;
    }
    
    
    private BaseSubscriptionExtension[]  createExtensions()
    {
        BaseSubscriptionExtension[]  extensions = new BaseSubscriptionExtension[]{};
        return extensions;
    }

    
    private CardPackage createCardPackage(String id)
    {
        CardPackage cardPackage = new CardPackage();
        cardPackage.setIdentifier(id);
        cardPackage.setTechnology(TechnologyTypeEnum.GSM.getValue());
        cardPackage.setPackageGroupID("UnitTestGroup");
        cardPackage.setSpid(TestSetupAccountHierarchy.SPID_ID);
        cardPackage.setDealer("UnitTestDealer");
        cardPackage.setImsi("1");
        return cardPackage;
    }
    
    /**
     * Test Validation for API method Create Subscription (Postpaid)
     * The Subscription pipeline is not installed as when the application is running. So we can't test the anything
     * besides the API validation.
     */
    public void testCreatePostpaidSubscription()
    {
        // Pending State
        String packageId = "123456789";
        SubscriptionProfile profile = createSubscriptionProfile(TestSetupAccountHierarchy.ACCOUNT1_BAN, 
                packageId, TestSetupAccountHierarchy.ACCOUNT1_BAN+"-20", PaidTypeEnum.POSTPAID.getValue(), "5551116666");
        SubscriptionStatus status = createStatus(SubscriptionStateEnum.PENDING.getValue()); 
        SubscriptionRating rating = createRating();
        SubscriptionBilling billing = createBilling();
        SubscriptionPricePlan options = creatPricePlanOptions();
        BaseSubscriptionExtension[] extensions = createExtensions();
        GenericParameter[] parameters = new GenericParameter[0];

        CardPackage cardPackage = createCardPackage(packageId);
        
        try
        {
            impl_.createSubscription(TestPackage.createRequestHeader(), TEMPLATE_ID, profile, status, rating, billing, cardPackage,options,extensions,parameters);
        }
        catch (CRMExceptionFault e) 
        {
            printErrors(e);
            assertTrue(e.getFaultMessage().getCRMException() instanceof ValidationException);
        }
        
        //Available state
        status = createStatus(SubscriptionStateEnum.AVAILABLE.getValue()); 
        
        try
        {
            impl_.createSubscription(TestPackage.createRequestHeader(), TEMPLATE_ID, profile, status, rating, billing, cardPackage,options,extensions,parameters);
        }
        catch (CRMExceptionFault e) 
        {
            printErrors(e);
            assertTrue(e.getFaultMessage().getCRMException() instanceof ValidationException);
        }
        
        //Active state
        status = createStatus(SubscriptionStateEnum.ACTIVE.getValue()); 
        
        try
        {
            impl_.createSubscription(TestPackage.createRequestHeader(), TEMPLATE_ID, profile, status, rating, billing, cardPackage,options,extensions,parameters);
        }
        catch (CRMExceptionFault e) 
        {
            printErrors(e);
            fail("Creating Active Postpaid Subscription is supposed to pass. " + e.getMessage());
        }
    }
    
    /**
     * Test Validation for API method Create Subscription (Prepaid).
     * The Subscription pipeline is not installed as when the application is running. So we can't test the anything
     * besides the API validation.
     */
    public void testCreatePrepaidSubscription()
    {
        // Pending state
        String packageId = "122456789";
        SubscriptionProfile profile = createSubscriptionProfile(TestSetupAccountHierarchy.ACCOUNT1_BAN, 
                packageId, TestSetupAccountHierarchy.ACCOUNT1_BAN+"-21", PaidTypeEnum.PREPAID.getValue(), "4451116666");
        SubscriptionStatus status = createStatus(SubscriptionStateEnum.PENDING.getValue()); //Pending
        SubscriptionRating rating = createRating();
        SubscriptionBilling billing = createBilling();
        CardPackage cardPackage = createCardPackage(packageId);
        SubscriptionPricePlan options = creatPricePlanOptions();
        BaseSubscriptionExtension[] extensions = createExtensions();
        GenericParameter[] parameters = new GenericParameter[0];
        try
        {
            impl_.createSubscription(TestPackage.createRequestHeader(), TEMPLATE_ID, profile, status, rating, billing,
                    cardPackage, options, extensions, parameters);
        }
        catch (CRMExceptionFault e)
        {
            printErrors(e);
            assertTrue(e.getFaultMessage().getCRMException() instanceof ValidationException);
        }
        // Available state
        status = createStatus(SubscriptionStateEnum.AVAILABLE.getValue());
        try
        {
            impl_.createSubscription(TestPackage.createRequestHeader(), TEMPLATE_ID, profile, status, rating, billing,
                    cardPackage, options, extensions, parameters);
        }
        catch (CRMExceptionFault e)
        {
            printErrors(e);
            fail("Creating Available Prepaid Subscription is supposed to pass. " + e.getMessage());
        }
        // Active state
        status = createStatus(SubscriptionStateEnum.ACTIVE.getValue()); // Active
        packageId = "123456700";
        profile = createSubscriptionProfile(TestSetupAccountHierarchy.ACCOUNT1_BAN, packageId,
                TestSetupAccountHierarchy.ACCOUNT1_BAN + "-22", PaidTypeEnum.PREPAID.getValue(), "5551117777");
        cardPackage = createCardPackage(packageId);
        try
        {
            impl_.createSubscription(TestPackage.createRequestHeader(), TEMPLATE_ID, profile, status, rating, billing,
                    cardPackage, options, extensions, parameters);
        }
        catch (CRMExceptionFault e)
        {
            printErrors(e);
            fail("Creating Active Prepaid Subscription is supposed to pass. " + e.getMessage());
        }
    }
    
    public void testUpdateSubscriptionProfile()
    {
        TestSetupAccountHierarchy.testSetup(getContext());
        
        SubscriptionReference subscriptionRef = new SubscriptionReference();
        subscriptionRef.setAccountID(TestSetupAccountHierarchy.ACCOUNT1_BAN);
        subscriptionRef.setIdentifier(TestSetupAccountHierarchy.SUB1_ID);
        subscriptionRef.setMobileNumber(TestSetupAccountHierarchy.SUB1_MSISDN);
        subscriptionRef.setSpid(Integer.valueOf(1));
        subscriptionRef.setState(SubscriptionStateEnum.ACTIVE.getValue());
        
        MutableSubscriptionProfile profile = new MutableSubscriptionProfile();
        
        MutableSubscriptionBilling billing = new MutableSubscriptionBilling();
        
        GenericParameter[] parameters = new GenericParameter[0];

        try
        {
            impl_.updateSubscriptionProfile(TestPackage.createRequestHeader(), subscriptionRef, profile, billing,parameters);
        }
        catch (CRMExceptionFault e) 
        {
            printErrors(e);
            fail("Updating Active Postpaid Subscription is supposed to pass. " + e.getMessage());
        }
    }
    
   
    public void testUpdateSubscriptionContract()
    {
        
        // Pending State
        String packageId = "123356789";
        SubscriptionProfile profile = createSubscriptionProfile(TestSetupAccountHierarchy.ACCOUNT1_BAN, 
                packageId, TestSetupAccountHierarchy.ACCOUNT1_BAN+"-20", PaidTypeEnum.POSTPAID.getValue(), "55111116666");
        SubscriptionStatus status = createStatus(SubscriptionStateEnum.ACTIVE.getValue()); 
        SubscriptionRating rating = createRating();
        rating.setContractID(Long.valueOf(1));
        SubscriptionBilling billing = createBilling();
        SubscriptionPricePlan options = creatPricePlanOptions();
        BaseSubscriptionExtension[] extensions = createExtensions();
        GenericParameter[] parameters = new GenericParameter[0];

        CardPackage cardPackage = createCardPackage(packageId);
        
        try
        {
            SubscriptionReference ref = impl_.createSubscription(TestPackage.createRequestHeader(), TEMPLATE_ID,
                    profile, status, rating, billing, cardPackage, options, extensions, parameters);
            assertTrue(ref.getState() == SubscriptionStateEnum.ACTIVE.getValue());
            SubscriptionProfileQueryResults result = impl_.getSubscriptionProfile(TestPackage.createRequestHeader(),
                    ref, null);
            assertTrue(result.getRating().getContractID() == 1);
        }
        catch (CRMExceptionFault e) 
        {
            printErrors(e);
            assertTrue(e.getFaultMessage().getCRMException() instanceof ValidationException);
        }
        
      
    }
    
    
    
    /**
     * Prints the given Error for Debugging
     * @param e
     */
    private void printErrors(Exception e)
    {
        assertTrue(e instanceof CRMExceptionFault);
        assertNotNull(((CRMExceptionFault)e).getFaultMessage());
        
        CRMException crmException = ((CRMExceptionFault)e).getFaultMessage().getCRMException();
        assertNotNull(crmException);
        System.out.println(e.getMessage());
        if (crmException instanceof ValidationException)
        {
            final ValidationException exception = (ValidationException) crmException;
            final ValidationExceptionEntry[] entries = exception.getEntries();
            for (int i = 0 ; i<entries.length; i++)
            {
                System.out.print("NAME: " + entries[i].getName());
                assertTrue(entries[i].getName().indexOf(" ") == -1);
                System.out.println(" EXPLANATION: " + entries[i].getExplanation());
            }
        }
    }
    
    
    private static SubscribersImpl impl_ = null;
    private Long TEMPLATE_ID = Long.valueOf(1);
    
    // This test is meant to be run off-line
    private final boolean testWithXDB = false;
}
