/*
 *  TestProvisionBundleSubscriberHome.java
 *
 *  Author : victor.stratan@redknee.com
 *  Date   : Mar 2, 2006
 *
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
package com.trilogy.app.crm.bundle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeTransientHome;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionTransientHome;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.exception.BundleAlreadyExistsException;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.subscriber.charge.support.BundleChargingSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestFakeCRMBundleProfile;
import com.trilogy.app.crm.unit_test.TestFakeCRMSubscriberBucketProfile;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.product.bundle.manager.api.v21.BundleService;

/**
 * Unit tests for the ProvisionBundleSubscriberHome.
 *  
 * @author victor.stratan@redknee.com
 *
 */
public class TestProvisionBundleSubscriberHome extends ContextAwareTestCase
{
    public static final String BAN = "123";
    public static final String SUB_ID = BAN + "-45";
    public static final int BC_ID = 12345;
    public static final String MSISDN = "6470000123";

    public TestProvisionBundleSubscriberHome(String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestProvisionBundleSubscriberHome.class);

        return suite;
    }

    // INHERIT
    public void setUp()
    {
        super.setUp();

        Context ctx = getContext();

        /*
        * Put in our fake bundle service
        */
        /* Our fake bundle service to see what methods were called */
        FakeBundleService fakeBundleService = new FakeBundleService();
        ctx.put(BundleService.class, fakeBundleService);

        bundleService_ = new TestFakeCRMBundleProfile(ctx);
        ctx.put(com.redknee.app.crm.bundle.service.CRMBundleProfile.class, bundleService_);

        bucketService_ = new TestFakeCRMSubscriberBucketProfile(ctx);
        ctx.put(com.redknee.app.crm.bundle.service.CRMSubscriberBucketProfile.class, bucketService_);

        home_ = new ProvisionBundleSubscriberHome(getContext(), null);

        bundleFees_ = new LinkedList();
        loyaltyBundleFees_ = new LinkedList();
        bundles_ = new LinkedList();
        TestBucketProvHome.createTestBundles(ctx, bundleFees_, bundles_, loyaltyBundleFees_);

        /*
        * Put in our fake SubscriberBucketHome
        */
        Home home = new SubscriberBucketTransientHome(ctx);
        ctx.put(SubscriberBucketHome.class, home);

        Home bun_home = new BundleProfileTransientHome(ctx);
        ctx.put(BundleProfileHome.class, bun_home);

        try
        {
            for (BundleProfile bundleProfile : bundles_)
            {
                bundleService_.createBundle(ctx, bundleProfile);
            }
        }
        catch (BundleManagerException e)
        {
            fail("Setup failed to create beans");
        }
        catch (BundleAlreadyExistsException e)
        {
            fail("Setup failed to create beans");
        }
    }


    // INHERIT
    public void tearDown()
    {
        super.tearDown();

        home_ = null;
        bundleFees_ = null;
    }

    public void _testBundleDeprovisioningOnSubscriberDeactivation()
    {
        Context ctx = getContext();

        Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.INACTIVE);

        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        Map bundles = new HashMap();
        Map overusage = new HashMap();
        int k = 1;
        for (Iterator i = bundleFees_.iterator(); i.hasNext() && k < 2; k++)
        {
            BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
            overusage.put(Long.valueOf(fee.getId()), Integer.valueOf(k * 10));
        }

        sub.setBundles(bundles);
        oldSub.setBundles(bundles);


        Home msisdn_home = new MsisdnTransientHome(ctx);
        ctx.put(MsisdnHome.class, msisdn_home);

        Msisdn msisdn = new Msisdn();
        msisdn.setMsisdn(MSISDN);
        msisdn.setSpid(1);
        msisdn.setBAN(BAN);

        Home sub_home = new SubscriberTransientHome(ctx);
        ctx.put(SubscriberHome.class, sub_home);

        Home trans_home = new TransactionTransientHome(ctx);
        ctx.put(TransactionHome.class, trans_home);

        Home buncat_home = new BundleCategoryTransientHome(ctx);
        ctx.put(BundleCategoryHome.class, buncat_home);

        BundleCategory buncat = new BundleCategory();
        buncat.setCategoryId(3);
        buncat.setSpid(1);
        buncat.setName("cat name");
        buncat.setUnitType(UnitTypeEnum.EVENT_SMS_MMS);

        Home pp_home = new PricePlanTransientHome(ctx);
        ctx.put(PricePlanHome.class, pp_home);

        PricePlan pp = new PricePlan();
        pp.setId(123);
        pp.setCurrentVersion(45);
        pp.setSpid(1);

        Home ppv_home = new PricePlanVersionTransientHome(ctx);
        ctx.put(PricePlanVersionHome.class, ppv_home);

        PricePlanVersion ppv = new PricePlanVersion();
        ppv.setId(123);
        ppv.setVersion(45);
        ppv.setOverusageVoiceRate(1);
        ppv.setOverusageSmsRate(2);
        ppv.setOverusageDataRate(3);

        sub.setPricePlan(123);
        sub.setPricePlanVersion(45);

        oldSub.setPricePlan(123);
        oldSub.setPricePlanVersion(45);

        Home adj_home = new AdjustmentTypeTransientHome(ctx);
        ctx.put(AdjustmentTypeHome.class, adj_home);

        AdjustmentType adj = new AdjustmentType();
        adj.setCode(BundleChargingSupport.OVERUSAGE_ADJUSTMENT);
        adj.setName("some adjustment");

        try
        {
            msisdn_home.create(ctx, msisdn);
            sub_home.create(ctx, oldSub);
            buncat_home.create(ctx, buncat);

            pp_home.create(ctx, pp);
            ppv_home.create(ctx, ppv);

            adj_home.create(ctx, adj);
        }
        catch (HomeException e)
        {
        }

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called times */
        assertEquals(0, bucketService_.removedBuckets_.size());

        /* Check switchSundles was not called */
        assertEquals(0, bundleService_.switchBundlesMsisdnList_.size());
        // TODO no more deactivation deprovisioning ?
//        assertEquals(1, fakeProfileService.removeSubscriberProfileCall);
//        assertEquals(MSISDN, fakeProfileService.removed.get(Integer.valueOf(1)));

        /* Check it tried to add 3 bundles to the SubscriberBucketHome */
        assertEquals(0, bucketService_.createdBuckets_.size());
        try
        {
            // check transactions created
            Collection c = trans_home.selectAll();
            // todo no remove - no overusage
            assertEquals(0, c.size());
        }
        catch (Exception e)
        {
            fail("Home exception: " + e.getMessage());
        }
    }



    public void _testLoyaltyBundleDeprovisioningOnSubscriberDeactivation()
    {
        Context ctx = getContext();

        Map pointBundles = new HashMap();
        Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        oldSub.setPointsBundles(pointBundles);

        Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.INACTIVE);
        sub.setPointsBundles(pointBundles);

        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        Map bundles = new HashMap();
        Map overusage = new HashMap();
        overusage.put(Long.valueOf(4), Integer.valueOf(40));

        sub.setBundles(bundles);
        oldSub.setBundles(bundles);

        // todo
//        FakeProfileService fakeProfileService = (FakeProfileService) ctx.get(ProfileService.class);
//        fakeProfileService.setOverusages(overusage);

        Home msisdn_home = new MsisdnTransientHome(ctx);
        ctx.put(MsisdnHome.class, msisdn_home);

        Msisdn msisdn = new Msisdn();
        msisdn.setMsisdn(MSISDN);
        msisdn.setSpid(1);
        msisdn.setBAN(BAN);

        Home sub_home = new SubscriberTransientHome(ctx);
        ctx.put(SubscriberHome.class, sub_home);

        Home trans_home = new TransactionTransientHome(ctx);
        ctx.put(TransactionHome.class, trans_home);

        Home buncat_home = new BundleCategoryTransientHome(ctx);
        ctx.put(BundleCategoryHome.class, buncat_home);

        BundleCategory buncat = new BundleCategory();
        buncat.setCategoryId(5);
        buncat.setSpid(1);
        buncat.setName("cat name");
        buncat.setUnitType(UnitTypeEnum.POINTS);

        Home pp_home = new PricePlanTransientHome(ctx);
        ctx.put(PricePlanHome.class, pp_home);

        PricePlan pp = new PricePlan();
        pp.setId(123);
        pp.setCurrentVersion(45);
        pp.setSpid(1);

        Home ppv_home = new PricePlanVersionTransientHome(ctx);
        ctx.put(PricePlanVersionHome.class, ppv_home);

        PricePlanVersion ppv = new PricePlanVersion();
        ppv.setId(123);
        ppv.setVersion(45);
        ppv.setOverusageVoiceRate(1);
        ppv.setOverusageSmsRate(2);
        ppv.setOverusageDataRate(3);

        sub.setPricePlan(123);
        sub.setPricePlanVersion(45);

        oldSub.setPricePlan(123);
        oldSub.setPricePlanVersion(45);

        Home adj_home = new AdjustmentTypeTransientHome(ctx);
        ctx.put(AdjustmentTypeHome.class, adj_home);

        AdjustmentType adj = new AdjustmentType();
        adj.setCode(BundleChargingSupport.OVERUSAGE_ADJUSTMENT);
        adj.setName("some adjustment");

        try
        {
            msisdn_home.create(ctx, msisdn);
            sub_home.create(ctx, oldSub);
            buncat_home.create(ctx, buncat);

            pp_home.create(ctx, pp);
            ppv_home.create(ctx, ppv);

            adj_home.create(ctx, adj);
        }
        catch (HomeException e)
        {
        }

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called times */
        FakeBundleService fakeBundleService = (FakeBundleService) ctx.get(BundleService.class);
        assertEquals(0, fakeBundleService.removeSubscriberBucketCallCount_);

        /* Check switchSundles was not called */
        assertEquals(0, fakeBundleService.switchBundlesCallCount_);

        // todo
//        assertEquals(1, fakeProfileService.removeSubscriberProfileCall);
//        assertEquals(MSISDN, fakeProfileService.removed.get(Integer.valueOf(1)));

        /* Check it tried to add 3 bundles to the SubscriberBucketHome */
        assertEquals(0, bucketService_.createdBuckets_.size());
        try
        {
            // check transactions created
            Collection c = trans_home.selectAll();
            assertEquals(0, c.size());
        }
        catch (Exception e)
        {
            fail("Home exception: " + e.getMessage());
        }
    }


    public void _testLoyaltyBundleDeprovisioning()
    {
        Context ctx = getContext();

        Map pointBundles = new HashMap();
        Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.INACTIVE);
        sub.setPointsBundles(pointBundles);

        Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        oldSub.setPointsBundles(pointBundles);

        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        Map bundles = new HashMap();
        sub.setBundles(bundles);

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called times */
        FakeBundleService fakeBundleService = (FakeBundleService) ctx.get(BundleService.class);
        assertEquals(0, fakeBundleService.removeSubscriberBucketCallCount_);

        /* Check switchSundles was not called */
        assertEquals(0, fakeBundleService.switchBundlesCallCount_);

//        FakeProfileService fakeProfileService = (FakeProfileService) ctx.get(ProfileService.class);
//        assertEquals(1, fakeProfileService.removeSubscriberProfileCall);
//        assertEquals(MSISDN, fakeProfileService.removed.get(Integer.valueOf(1)));

        /* Check it tried to add 0 bundles to the SubscriberBucketHome */
        assertEquals(0, bucketService_.createdBuckets_.size());
    }

    public void testLoyaltyBundleSuspending() throws BundleManagerException, BundleAlreadyExistsException
    {
        Context ctx = getContext();

        Map pointBundles = new HashMap();
        BundleFee loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.SUSPENDED);
        sub.setPointsBundles(pointBundles);

        Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        oldSub.setPointsBundles(pointBundles);

        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        Map bundles = new HashMap();
        sub.setBundles(bundles);

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called times */
        assertEquals(0, bucketService_.removedBuckets_.size());

        /* Check switchSundles was not called */
        assertEquals(0, bundleService_.switchBundlesMsisdnList_.size());

        assertEquals(1, bucketService_.updateStatusBuckets_.size());
        SubscriberBucket bucket = bucketService_.updateStatusBuckets_.get(0);
        assertEquals(StatusEnum.SUSPENDED, bucket.getStatus());
        assertEquals(MSISDN, bucket.getMsisdn());
        assertEquals(loyaltyBundle.getId(), bucket.getBundleId());

        // todo
//        FakeProfileService fakeProfileService = (FakeProfileService) ctx.get(ProfileService.class);
//        assertEquals(0, fakeProfileService.removeSubscriberProfileCall);

        /* Check it tried to add 0 bundles to the SubscriberBucketHome */
        assertEquals(0, bucketService_.createdBuckets_.size());
    }

    public void testLoyaltyBundleUnSuspending()
    {
        Context ctx = getContext();

        Map pointBundles = new HashMap();
        BundleFee loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.SUSPENDED);
        oldSub.setPointsBundles(pointBundles);

        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        Map bundles = new HashMap();
        sub.setBundles(bundles);

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called times */
        assertEquals(0, bucketService_.removedBuckets_.size());

        /* Check switchSundles was not called */
        assertEquals(0, bundleService_.switchBundlesMsisdnList_.size());

        assertEquals(1, bucketService_.updateStatusBuckets_.size());
        SubscriberBucket bucket = bucketService_.updateStatusBuckets_.get(0);
        assertEquals(StatusEnum.ACTIVE, bucket.getStatus());
        assertEquals(MSISDN, bucket.getMsisdn());
        assertEquals(loyaltyBundle.getId(), bucket.getBundleId());

        /* Check it tried to add 0 bundles to the SubscriberBucketHome */
        assertEquals(0, bucketService_.createdBuckets_.size());
    }

    
    public void testLoyaltyBundleSuspendingExceptionRollback() throws BundleManagerException, BundleAlreadyExistsException
    {

        Context ctx = getContext();

        Map pointBundles = new HashMap();
        BundleFee loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.SUSPENDED);
        sub.setPointsBundles(pointBundles);

        Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        oldSub.setPointsBundles(pointBundles);

        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        Map bundles = new HashMap();
        sub.setBundles(bundles);

        try
        {
            home_.setDelegate(new HomeProxy(ctx) {
                @Override
                public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
                {
                    throw new HomeException("Test exception case.");
                }
            });
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            //Expected
            assertEquals(e.getMessage(), "Test exception case.");
        }

        /* Check remove was not called times */
        assertEquals(0, bucketService_.removedBuckets_.size());

        /* Check switchSundles was not called */
        assertEquals(0, bundleService_.switchBundlesMsisdnList_.size());

        assertEquals(0, bucketService_.updateStatusBuckets_.size());

        /* Check it tried to add 0 bundles to the SubscriberBucketHome */
        assertEquals(0, bucketService_.createdBuckets_.size());
        
        home_.setDelegate(null);
    }
        
    
    public void testLoyaltyBundleUnSuspendingExceptionRollback()
    {
        Context ctx = getContext();

        Map pointBundles = new HashMap();
        BundleFee loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.SUSPENDED);
        oldSub.setPointsBundles(pointBundles);

        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        Map bundles = new HashMap();
        sub.setBundles(bundles);

        try
        {
            home_.setDelegate(new HomeProxy(ctx) {
                @Override
                public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
                {
                    throw new HomeException("Test exception case.");
                }
            });
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            //Expected
            assertEquals(e.getMessage(), "Test exception case.");
        }

        /* Check remove was not called times */
        assertEquals(0, bucketService_.removedBuckets_.size());

        /* Check switchSundles was not called */
        assertEquals(0, bundleService_.switchBundlesMsisdnList_.size());

        assertEquals(0, bucketService_.updateStatusBuckets_.size());

        /* Check it tried to add 0 bundles to the SubscriberBucketHome */
        assertEquals(0, bucketService_.createdBuckets_.size());
        
        home_.setDelegate(null);
    }    
    
    private Account createTestAccount()
    {
        Account account = new Account();
        account.setBAN(BAN);
        account.setBillCycleID(BC_ID);
        account.setSpid(1);
        return account;
    }

    private Subscriber createTestSub(SubscriberTypeEnum type, SubscriberStateEnum state)
    {
        Subscriber sub = new Subscriber();
        sub.setSubscriberType(type);
        sub.setState(state);
        sub.setBAN(BAN);
        sub.setId(SUB_ID);
        sub.setMSISDN(MSISDN);
        sub.setSpid(1);
        return sub;
    }

    /* The home we're testing */
    ProvisionBundleSubscriberHome home_ = null;

    TestFakeCRMBundleProfile bundleService_;
    TestFakeCRMSubscriberBucketProfile bucketService_;

    /* Some bundles for testing */
    List<BundleFee> bundleFees_;
    List<BundleFee> loyaltyBundleFees_;
    List<BundleProfile> bundles_;

    /* Testing subscriber stub */
    Subscriber subscriber_ = null;
}

