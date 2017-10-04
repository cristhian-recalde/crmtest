/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestSuite;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleTransientHome;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.exception.BucketAlreadyExistsException;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.exception.BundleAlreadyExistsException;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestFakeCRMBundleProfile;
import com.trilogy.app.crm.unit_test.TestFakeCRMSubscriberBucketProfile;
import org.junit.Test;

/**
 * Unit tests for the BucketProvHome.
 * 
 * @author danny.ng@redknee.com
 * @author victor.stratan@redknee.com
 */
public class TestBucketProvHome extends ContextAwareTestCase
{

    public static final String BAN = "12345";
    public static final String SUB_ID = "12345-76";
    public static final int BC_ID = 12345;
    public static final int SPID = 12;
    public static final int SUBSCR_TYPE = 101;
    public static final String MSISDN = "6470000123";
    public static final int PP_ID = 45;
    public static final int PPV_ID = 1;


    public TestBucketProvHome(final String name)
    {
        super(name);
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be
     * invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     * 
     * @return A new suite of Tests for execution.
     */
    public static junit.framework.Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be
     * invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     * 
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static junit.framework.Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestBucketProvHome.class);

        return suite;
    }


    @Override
    public void setUp()
    {
        super.setUp();

        final Context ctx = getContext();

        bundleService_ = new TestFakeCRMBundleProfile(ctx);
        ctx.put(com.redknee.app.crm.bundle.service.CRMBundleProfile.class, bundleService_);

        bucketService_ = new TestFakeCRMSubscriberBucketProfile(ctx);
        ctx.put(com.redknee.app.crm.bundle.service.CRMSubscriberBucketProfile.class, bucketService_);

        subscriber_ = new Subscriber();
        subscriber_.setId(SUB_ID);
        subscriber_.setBAN(BAN);
        subscriber_.setSpid(SPID);
        subscriber_.setMSISDN(MSISDN);

        subHome_ = new SubscriberTransientHome(getContext());
        home_ = new BucketProvHome(getContext(), subHome_);
        bundleFees_ = new ArrayList();
        loyaltyBundleFees_ = new ArrayList();
        final List bundles = new ArrayList();
        createTestBundles(ctx, bundleFees_, bundles, loyaltyBundleFees_);

        final Home bcHome = new BillCycleTransientHome(ctx);
        ctx.put(BillCycleHome.class, bcHome);

        final Home accHome = new AccountTransientHome(ctx);
        ctx.put(AccountHome.class, accHome);

        final Home ppHome = new PricePlanTransientHome(ctx);
        ctx.put(PricePlanHome.class, ppHome);

        final Home ppvHome = new PricePlanVersionTransientHome(ctx);
        ctx.put(PricePlanVersionHome.class, ppvHome);

        try
        {
            bcHome.create(ctx, createTestBC());
            accHome.create(ctx, createTestAccount());
            ppHome.create(ctx, createTestPricePlan());
            ppvHome.create(ctx, createTestPricePlanVersion());

            final Iterator iterator = bundles.iterator();
            while (iterator.hasNext())
            {
                final BundleProfile bundle = (BundleProfile) iterator.next();
                bundleService_.createBundle(ctx, bundle);
            }
        }
        catch (HomeException e)
        {
            fail("Setup failed to create beans");
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


    @Override
    public void tearDown()
    {
        super.tearDown();

        home_ = null;
        bundleFees_ = null;
    }


    /**
     * No attempt should be made to add or remove buckets from BM when the two
     * list of bundles provided to the <code>provision()</code> method are
     * identical and all bundles are reported as provisioned by BM.
     * 
     * @throws HomeException if thrown by underlying calls
     */
    @Test
    public void testProvisionWithNoBundleChangesNoPPbundlesAllBundlesOnBM() throws HomeException,
            BundleManagerException, BucketAlreadyExistsException
    {
        final Context ctx = getContext();

        final Map buckets = new HashMap();
        // Convert our bundleFees into a Map of SubscriberBucket objects
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            final SubscriberBucket api = new SubscriberBucket();
            api.setBundleId(fee.getId());
            api.setSubscriptionType(SUBSCR_TYPE);
            api.setAuxiliary(true);
            api.setMsisdn(MSISDN);
            api.setSpid(SPID);
            buckets.put(Long.valueOf(fee.getId()), api);
        }

        /* Create 2nd bundle to pass in as the "new" set of bundles */
        final Map buckets2 = new HashMap();
        buckets2.putAll(buckets);

        // mark all bundles provisioned on BM
        markBundlesProvisioned(ctx, bundleFees_, 1001);
        clearCounters();

        try
        {
            home_.provision(ctx, 1, buckets, buckets2, subscriber_, subscriber_);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 0);
    }


    /**
     * All bundles are provisioned on BM during <code>provision()</code> call
     * even though bundles lists provided to the method are identical, but no
     * bundles are reported as provisioned by BM.
     */
    @Test
    public void testProvisionWithNoBundleChangesNoPPbundlesNoBundlesOnBM()
    {
        final Context ctx = getContext();

        final Map buckets = new HashMap();
        // Convert our bundleFees into a Map of SubscriberBucket objects
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            final SubscriberBucket api = new SubscriberBucket();
            api.setBundleId(fee.getId());
            api.setSubscriptionType(SUBSCR_TYPE);
            api.setAuxiliary(true);
            api.setMsisdn(MSISDN);
            api.setSpid(SPID);
            buckets.put(Long.valueOf(fee.getId()), api);
        }

        /* Create 2nd bundle to pass in as the "new" set of bundles */
        final Map buckets2 = new HashMap();
        buckets2.putAll(buckets);

        try
        {
            home_.provision(ctx, 1, buckets, buckets2, subscriber_, subscriber_);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * No attempt is made to add or remove a buckets from BM because the two
     * list of bundles provided to the <code>provision()</code> method are
     * identical and all bundles are provisioned on BM
     * 
     * @throws HomeException if thrown by underlying calls
     */
    @Test
    public void testProvisionWithNoBundleChangesAllBundlsOnBM() throws HomeException, BundleManagerException,
            BucketAlreadyExistsException
    {
        final Context ctx = getContext();

        final Map buckets = new HashMap();
        // Convert our bundleFees into a Map of SubscriberBucket objects
        // make some PP bundles
        int k = 0;
        for (final Iterator i = bundleFees_.iterator(); i.hasNext(); ++k)
        {
            final BundleFee fee = (BundleFee) i.next();
            final SubscriberBucket api = new SubscriberBucket();
            api.setBundleId(fee.getId());
            api.setSubscriptionType(SUBSCR_TYPE);
            api.setAuxiliary(!(k < 4 && k % 2 == 0));
            api.setMsisdn(MSISDN);
            api.setSpid(SPID);
            buckets.put(Long.valueOf(fee.getId()), api);
        }

        /* Create 2nd bundle to pass in as the "new" set of bundles */
        final Map buckets2 = new HashMap();
        buckets2.putAll(buckets);

        // mark all bundles provisioned on BM
        markBundlesProvisioned(ctx, bundleFees_, 1001);
        clearCounters();

        try
        {
            home_.provision(ctx, 1, buckets, buckets2, subscriber_, subscriber_);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 0);
    }


    /**
     * All bundles are provisioned on BM during <code>provision()</code> call
     * even though bundles lists provided to the method are identical, but no
     * bundles are reported as provisioned by BM.
     */
    @Test
    public void testProvisionWithNoBundleChangesNoBundlsOnBM()
    {
        final Context ctx = getContext();

        final Map buckets = new HashMap();
        // Convert our bundleFees into a Map of SubscriberBucket objects
        // make some PP bundles
        int ppBundlesCount = 0;
        int k = 0;
        for (final Iterator i = bundleFees_.iterator(); i.hasNext(); ++k)
        {
            final BundleFee fee = (BundleFee) i.next();
            final SubscriberBucket api = new SubscriberBucket();
            api.setBundleId(fee.getId());
            api.setSubscriptionType(SUBSCR_TYPE);
            api.setAuxiliary(!(k < 4 && k % 2 == 0));
            if (!api.getAuxiliary())
            {
                ppBundlesCount++;
            }
            api.setMsisdn(MSISDN);
            api.setSpid(SPID);
            buckets.put(Long.valueOf(fee.getId()), api);
        }

        /* Create 2nd bundle to pass in as the "new" set of bundles */
        final Map buckets2 = new HashMap();
        buckets2.putAll(buckets);

        try
        {
            home_.provision(ctx, 1, buckets, buckets2, subscriber_, subscriber_);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        final int expectedSwitchCalls = ppBundlesCount > 0 ? 1 : 0;
        final int expectedBundles = bundleFees_.size() - ppBundlesCount;
        assertBundleServiceState(ctx, 1);
    }


    /**
     * A call is made to add a bucket for every bundle in the new list because
     * the old list of bundles provided to the <code>provision()</code> method
     * is empty and no bundles are reported as provisioned on BM Only auxiliary
     * bundles are used.
     */
    @Test
    public void testProvisionWithBundlesAddedNoBundlesOnBM()
    {
        final Context ctx = getContext();

        final Map buckets = new HashMap();
        // Convert our bundleFees into a Map of SubscriberBucket objects
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            final SubscriberBucket api = new SubscriberBucket();
            api.setBundleId(fee.getId());
            api.setSubscriptionType(SUBSCR_TYPE);
            api.setAuxiliary(true);
            api.setMsisdn(MSISDN);
            api.setSpid(SPID);
            buckets.put(Long.valueOf(fee.getId()), api);
        }

        /* Create 2nd bundle to pass in as the "new" set of bundles */
        final Map bucketsEmpty = new HashMap();

        try
        {
            home_.provision(ctx, 1, bucketsEmpty, buckets, subscriber_, subscriber_);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did try to add 3 bundles to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * No attempt is made to add buckets to BM because all bundles are
     * provisioned on BM even though the two list of bundles provided to the
     * <code>provision()</code> method are different. Only auxiliary bundles are
     * used.
     * 
     * @throws HomeException if thrown by underlying calls
     */
    @Test
    public void testProvisionWithBundlesAddedAllBundlesOnBM() throws HomeException, BundleManagerException,
            BucketAlreadyExistsException
    {
        final Context ctx = getContext();

        final Map buckets = new HashMap();
        // Convert our bundleFees into a Map of SubscriberBucket objects
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            final SubscriberBucket api = new SubscriberBucket();
            api.setBundleId(fee.getId());
            api.setSubscriptionType(SUBSCR_TYPE);
            api.setAuxiliary(true);
            api.setMsisdn(MSISDN);
            api.setSpid(SPID);
            buckets.put(Long.valueOf(fee.getId()), api);
        }

        /* Create 2nd bundle to pass in as the "new" set of bundles */
        final Map bucketsEmpty = new HashMap();

        // mark all bundles provisioned on BM
        markBundlesProvisioned(ctx, bundleFees_, 1001);
        clearCounters();

        try
        {
            home_.provision(ctx, 1, bucketsEmpty, buckets, subscriber_, subscriber_);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did try to add 3 bundles to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 0);
    }


    /**
     * A call is made to remove a bucket for every bundle in the old list
     * because the new list of bundles provided to the <code>provision()</code>
     * method is empty and all bundles are provisioned on BM. Only auxiliary
     * bundles are used.
     * 
     * @throws HomeException if thrown by underlying calls
     */
    @Test
    public void testProvisionWithBundlesRemovedAllBundlesOnBM() throws HomeException, BundleManagerException,
            BucketAlreadyExistsException
    {
        final Context ctx = getContext();

        final Map buckets = new HashMap();
        // Convert our bundleFees into a Map of SubscriberBucket objects
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            final SubscriberBucket api = new SubscriberBucket();
            api.setBundleId(fee.getId());
            api.setSubscriptionType(SUBSCR_TYPE);
            api.setAuxiliary(true);
            api.setMsisdn(MSISDN);
            api.setSpid(SPID);
            buckets.put(Long.valueOf(fee.getId()), api);
        }

        /* Create 2nd bundle to pass in as the "new" set of bundles */
        final Map bucketsEmpty = new HashMap();

        // mark all bundles provisioned on BM
        markBundlesProvisioned(ctx, bundleFees_, 1001);
        clearCounters();

        try
        {
            home_.provision(ctx, 1, buckets, bucketsEmpty, subscriber_, subscriber_);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was called 3 times */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * No calls to remove are made even though the new list of bundles provided
     * to the <code>provision()</code> method is empty because bundles are not
     * provisioned on BM. Only auxiliary bundles are used.
     */
    @Test
    public void testProvisionWithBundlesRemovedNoBundlesOnBM()
    {
        final Context ctx = getContext();

        final Map buckets = new HashMap();
        // Convert our bundleFees into a Map of SubscriberBucket objects
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            final SubscriberBucket api = new SubscriberBucket();
            api.setBundleId(fee.getId());
            api.setSubscriptionType(SUBSCR_TYPE);
            api.setAuxiliary(true);
            api.setMsisdn(MSISDN);
            api.setSpid(SPID);
            buckets.put(Long.valueOf(fee.getId()), api);
        }

        /* Create 2nd bundle to pass in as the "new" set of bundles */
        final Map bucketsEmpty = new HashMap();

        try
        {
            home_.provision(ctx, 1, buckets, bucketsEmpty, subscriber_, subscriber_);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was called 3 times */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 0);
    }


    /**
     * Test that the home.create() creates bundles for a Prepaid subscriber in
     * Pending state. Only auxiliary bundles are used.
     */
    @Test
    public void testHomeCreate_prepaidPending()
    {
        final Context ctx = getContext();

        final Subscriber sub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.PENDING);

        final Map bundles = new HashMap();
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
        }

        sub.setBundles(bundles);

        try
        {
            home_.create(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.create() creates bundles for a Prepaid subscriber in
     * Pending state. Only auxiliary bundles are used.
     */
    @Test
    public void testHomeCreate_prepaidAvailable()
    {
        final Context ctx = getContext();

        final Subscriber sub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.AVAILABLE);

        final Map bundles = new HashMap();
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
        }

        sub.setBundles(bundles);

        try
        {
            home_.create(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.create() creates bundles for a Prepaid subscriber in
     * Active state. Only auxiliary bundles are used.
     */
    @Test
    public void testHomeCreate_prepaidActive()
    {
        final Context ctx = getContext();

        final Subscriber sub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.ACTIVE);

        final Map bundles = new HashMap();
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
        }

        sub.setBundles(bundles);

        try
        {
            home_.create(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it added 3 bundles to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.create() does not create bundles for a Postpaid
     * subscriber in Pending state. Only auxiliary bundles are used.
     */
    @Test
    public void testHomeCreate_postpaidPending()
    {
        final Context ctx = getContext();

        final Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.PENDING);

        final Map bundles = new HashMap();
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
        }

        sub.setBundles(bundles);

        try
        {
            home_.create(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check removeSubscriberBucket was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.create() creates bundles for a Postpaid subscriber in
     * Active state. Only auxiliary bundles are used.
     */
    @Test
    public void testHomeCreate_postpaidActive()
    {
        final Context ctx = getContext();

        final Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);

        final Map bundles = new HashMap();
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
        }

        sub.setBundles(bundles);

        try
        {
            home_.create(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did try to add 3 bundles to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.create() creates Points Bundle for a Postpaid
     * subscriber in Active state. Only one Points Bundle is used.
     */
    @Test
    public void testHomeCreate_LoyaltyBundle_alone_postpaidActive()
    {
        final Context ctx = getContext();

        final Map pointBundles = new HashMap();
        final BundleFee loyaltyBundle = (BundleFee) loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        sub.setBundles(bundles);

        try
        {
            home_.create(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did try to add 1 bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);

        try
        {
            Visitors.forEach(ctx, bucketService_.createdBuckets_, new Visitor()
            {

                public void visit(final Context vCtx, final Object bean) throws AgentException, AbortVisitException
                {
                    assertEquals(loyaltyBundle.getId(), ((SubscriberBucket) bean).getBundleId());
                }
            });
        }
        catch (Exception e)
        {
            fail("Exception: " + e.getMessage());
        }

    }


    /**
     * Test that the home.create() creates Points Bundle for a Postpaid
     * subscriber in Active state. Only one Points Bundle is used.
     */
    @Test
    public void testHomeCreate_LoyaltyBundle_withOverBundles_postpaidActive()
    {
        final Context ctx = getContext();

        final Map pointBundles = new HashMap();
        final Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
        }

        sub.setBundles(bundles);

        try
        {
            home_.create(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /*
         * Check it tried to add 4 bundles (3 auxiliary, 1 points) to the
         * SubscriberBucketHome
         */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.create() creates Points Bundle for a Postpaid
     * subscriber in Active state. Only one Points Bundle is used.
     */
    @Test
    public void testHomeCreate_LoyaltyBundle_withOverBundles_postpaidPending()
    {
        final Context ctx = getContext();

        final Map pointBundles = new HashMap();
        final Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.PENDING);
        sub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
        }

        sub.setBundles(bundles);

        try
        {
            home_.create(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it tried to add 1 bundle (1 points) to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.create() creates Points Bundle for a Postpaid
     * subscriber in Active state. Only one Points Bundle is used.
     */
    @Test
    public void testHomeCreate_LoyaltyBundle_withOverBundles_prepaidActive()
    {
        final Context ctx = getContext();

        final Map pointBundles = new HashMap();
        final Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
        }

        sub.setBundles(bundles);

        try
        {
            home_.create(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /*
         * Check it tried to add 4 bundles (3 auxiliary, 1 points) to the
         * SubscriberBucketHome
         */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.create() creates Points Bundle for a Postpaid
     * subscriber in Active state. Only one Points Bundle is used.
     */
    @Test
    public void testHomeCreate_LoyaltyBundle_withOverBundles_prepaidAvailable()
    {
        final Context ctx = getContext();

        final Map pointBundles = new HashMap();
        final Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.AVAILABLE);
        sub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
        }

        sub.setBundles(bundles);

        try
        {
            home_.create(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it tried to add 1 bundle (1 points) to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.create() creates Points Bundle for a Postpaid
     * subscriber in Pending state. Only one Points Bundle is used.
     */
    @Test
    public void testHomeCreate_LoyaltyBundle_withOverBundles_prepaidPending()
    {
        final Context ctx = getContext();

        final Map pointBundles = new HashMap();
        final Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.PENDING);
        sub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
        }

        sub.setBundles(bundles);

        try
        {
            home_.create(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it tried to add 1 bundle (1 points) to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.store() does not do anything for Postpaid subscriber
     * in Active state if it has the same Points Bundle. Only one Points Bundle
     * is used.
     * 
     * @throws HomeException if thrown by underlying calls
     */
    @Test
    public void testHomeStore_LoyaltyBundle_alone_nochange_AllBundlesOnBM() throws HomeException,
            BundleManagerException, BucketAlreadyExistsException
    {
        final Context ctx = getContext();

        final Map pointBundles = new HashMap();
        final Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        final Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        oldSub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        sub.setBundles(bundles);
        oldSub.setBundles(bundles);

        try
        {
            subHome_.create(sub);
        }
        catch (HomeException e)
        {
            fail("Exception during test SETUP" + e.getMessage());
        }
        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        // mark all bundles provisioned on BM
        markBundlesProvisioned(ctx, bundleFees_, 1001);
        clearCounters();

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 0);
    }


    /**
     * Test that the home.store() reprovisions bundles for Postpaid subscriber
     * in Active state if no bundles are reported as provisioned on BM even
     * though it has the same Points Bundle list is the same. Currently this
     * test will not reprovision bundles because of one optimisation: if lists
     * are the same no action will be performed. Otherwise any update on
     * subscriber will trigger a BM query. Only one Points Bundle is used.
     */
    @Test
    public void testHomeStore_LoyaltyBundle_alone_nochange_NoBundlesOnBM()
    {
        final Context ctx = getContext();

        final Map pointBundles = new HashMap();
        final Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        final Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        oldSub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        sub.setBundles(bundles);
        oldSub.setBundles(bundles);

        try
        {
            subHome_.create(sub);
        }
        catch (HomeException e)
        {
            fail("Exception during test SETUP" + e.getMessage());
        }
        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx,0);
    }


    /**
     * Test that the home.store() does not do anything for Postpaid subscriber
     * in Active state if it has the same Points Bundle. Only one Points Bundle
     * is used.
     */
    @Test
    public void testHomeStore_LoyaltyBundle_alone_nochange_Available2active()
    {
        final Context ctx = getContext();

        final Map pointBundles = new HashMap();
        final Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        final Subscriber oldSub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.AVAILABLE);
        oldSub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        sub.setBundles(bundles);
        oldSub.setBundles(bundles);

        try
        {
            subHome_.create(sub);
        }
        catch (HomeException e)
        {
            fail("Exception during test SETUP" + e.getMessage());
        }
        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 0);
    }


    /**
     * Test that the home.store() does not do anything for Postpaid subscriber
     * in Active state if it has the same Points Bundle. Only one Points Bundle
     * is used.
     */
    @Test
    public void testHomeStore_LoyaltyBundle_alone_nochange_Pending2active()
    {
        final Context ctx = getContext();

        final Map pointBundles = new HashMap();
        final Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        final Subscriber oldSub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.PENDING);
        oldSub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        sub.setBundles(bundles);
        oldSub.setBundles(bundles);

        try
        {
            subHome_.create(sub);
        }
        catch (HomeException e)
        {
            fail("Exception during test SETUP" + e.getMessage());
        }
        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 0);
    }


    /**
     * Test that the home.store() does not do anything for Postpaid subscriber
     * in Active state if it has the same Points Bundle. Only one Points Bundle
     * is used.
     */
    @Test
    public void testHomeStore_LoyaltyBundle_withOthers_nochange_Pending2active()
    {
        final Context ctx = getContext();

        final Map pointBundles = new HashMap();
        final Object loyaltyBundle = loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        final Subscriber oldSub = createTestSub(SubscriberTypeEnum.PREPAID, SubscriberStateEnum.PENDING);
        oldSub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        for (final Iterator i = bundleFees_.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            bundles.put(XBeans.getIdentifier(fee), fee);
        }

        sub.setBundles(bundles);
        oldSub.setBundles(bundles);

        try
        {
            subHome_.create(sub);
        }
        catch (HomeException e)
        {
            fail("Exception during test SETUP" + e.getMessage());
        }
        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /*
         * Check it tried to add 3 bundles (3 auxiliary, no points) to the
         * SubscriberBucketHome
         */
        assertBundleServiceState(ctx, 0);
    }


    /**
     * Test that the home.store() removes the old bundle and creates the new one
     * for Postpaid subscriber in Active state if it one Points Bundle is
     * replaces by another. Only one Points Bundle is used.
     * 
     * @throws HomeException if thrown by underlying calls
     */
    @Test
    public void testHomeStore_LoyaltyBundle_alone_change_AllBundlesOnBM() throws HomeException, BundleManagerException,
            BucketAlreadyExistsException
    {
        final Context ctx = getContext();

        Map pointBundles = new HashMap();
        final BundleFee oldLoyaltyBundle = (BundleFee) loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(oldLoyaltyBundle), oldLoyaltyBundle);

        final Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        oldSub.setPointsBundles(pointBundles);

        pointBundles = new HashMap();
        final BundleFee loyaltyBundle = (BundleFee) loyaltyBundleFees_.get(1);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        sub.setBundles(bundles);
        oldSub.setBundles(bundles);

        try
        {
            subHome_.create(sub);
        }
        catch (HomeException e)
        {
            fail("Exception during test SETUP" + e.getMessage());
        }
        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        // mark all bundles provisioned on BM
        final List provisioned = new ArrayList(1);
        provisioned.add(oldLoyaltyBundle);
        markBundlesProvisioned(ctx, provisioned, 1001);
        clearCounters();

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was called 1 time for the old bundle */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it tried to add 1 bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.store() removes the old bundle and creates the new one
     * for Postpaid subscriber in Pending state if it one Points Bundle is
     * replaces by another. Only one Points Bundle is used.
     * 
     * @throws HomeException if thrown by underlying calls
     */
    @Test
    public void testHomeStore_LoyaltyBundle_alone_change_stillPending_AllBundlesOnBM() throws HomeException,
            BundleManagerException, BucketAlreadyExistsException
    {
        final Context ctx = getContext();

        Map pointBundles = new HashMap();
        final BundleFee oldLoyaltyBundle = (BundleFee) loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(oldLoyaltyBundle), oldLoyaltyBundle);

        final Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.PENDING);
        oldSub.setPointsBundles(pointBundles);

        pointBundles = new HashMap();
        final BundleFee loyaltyBundle = (BundleFee) loyaltyBundleFees_.get(1);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.PENDING);
        sub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        sub.setBundles(bundles);
        oldSub.setBundles(bundles);

        try
        {
            subHome_.create(sub);
        }
        catch (HomeException e)
        {
            fail("Exception during test SETUP" + e.getMessage());
        }
        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        // mark all bundles provisioned on BM
        final List provisioned = new ArrayList(1);
        provisioned.add(oldLoyaltyBundle);
        markBundlesProvisioned(ctx, provisioned, 1001);
        clearCounters();

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was not called */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it did not try to add a bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    /**
     * Test that the home.store() removes the old bundle and creates the new one
     * for Postpaid subscriber in Active state if it one Points Bundle is
     * replaces by another. Only one Points Bundle is used.
     * 
     * @throws HomeException if thrown by underlying calls
     */
    @Test
    public void testHomeStore_LoyaltyBundle_alone_change_pending2active_AllBundlesOnBM() throws HomeException,
            BundleManagerException, BucketAlreadyExistsException
    {
        final Context ctx = getContext();

        Map pointBundles = new HashMap();
        final BundleFee oldLoyaltyBundle = (BundleFee) loyaltyBundleFees_.get(0);
        pointBundles.put(XBeans.getIdentifier(oldLoyaltyBundle), oldLoyaltyBundle);

        final Subscriber oldSub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.PENDING);
        oldSub.setPointsBundles(pointBundles);

        pointBundles = new HashMap();
        final BundleFee loyaltyBundle = (BundleFee) loyaltyBundleFees_.get(1);
        pointBundles.put(XBeans.getIdentifier(loyaltyBundle), loyaltyBundle);

        final Subscriber sub = createTestSub(SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);
        sub.setPointsBundles(pointBundles);

        final Map bundles = new HashMap();
        sub.setBundles(bundles);
        oldSub.setBundles(bundles);

        try
        {
            subHome_.create(sub);
        }
        catch (HomeException e)
        {
            fail("Exception during test SETUP" + e.getMessage());
        }
        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        // mark all bundles provisioned on BM
        final List provisioned = new ArrayList(1);
        provisioned.add(oldLoyaltyBundle);
        markBundlesProvisioned(ctx, provisioned, 1001);
        clearCounters();

        try
        {
            home_.store(ctx, sub);
        }
        catch (HomeException e)
        {
            fail("Home exception: " + e.getMessage());
        }

        /* Check remove was called 1 time for the old bundle */
        /* Check switchBundles was not called */
        /* Check updateBucketActive was not called */
        /* Check it tried to add 1 bundle to the SubscriberBucketHome */
        assertBundleServiceState(ctx, 1);
    }


    private void assertBundleServiceState(final Context ctx, final int expectedSwitchBundlesCallCount)
    {
        assertEquals("createBucket was called incorrect number of times",
        /* expected */0,
        /* actual */bucketService_.createdBuckets_.size());

        assertEquals("removeSubscriberBucket was called incorrect number of times",
        /* expected */0,
        /* actual */bucketService_.removedBuckets_.size());

        assertEquals("switchBundles was called incorrect number of times",
        /* expected */expectedSwitchBundlesCallCount,
        /* actual */bundleService_.switchBundlesMsisdnList_.size());

        assertEquals("updateBucketActive was called incorrect number of times",
        /* expected */0,
        /* actual */bucketService_.updateStatusBuckets_.size());
    }


    private Account createTestAccount()
    {
        final Account account = new Account();
        account.setBAN(BAN);
        account.setBillCycleID(BC_ID);
        account.setSpid(SPID);
        return account;
    }


    private Subscriber createTestSub(final SubscriberTypeEnum type, final SubscriberStateEnum state)
    {
        final Subscriber sub = new Subscriber();
        sub.setSubscriberType(type);
        sub.setState(state);
        sub.setId(SUB_ID);
        sub.setBAN(BAN);
        sub.setMSISDN(MSISDN);
        sub.setSpid(SPID);
        sub.setPricePlan(PP_ID);
        sub.setPricePlanVersion(PPV_ID);
        return sub;
    }


    private BillCycle createTestBC()
    {
        final BillCycle bc = new BillCycle();
        bc.setBillCycleID(BC_ID);
        bc.setSpid(SPID);
        bc.setDayOfMonth(15);
        return bc;
    }


    private PricePlan createTestPricePlan()
    {
        final PricePlan pp = new PricePlan();
        pp.setId(PP_ID);
        pp.setSpid(SPID);
        pp.setCurrentVersion(PPV_ID);
        pp.setNextVersion(PPV_ID + 1);
        return pp;
    }


    private PricePlanVersion createTestPricePlanVersion()
    {
        final PricePlanVersion ppv = new PricePlanVersion();
        ppv.setId(PP_ID);
        ppv.setVersion(PPV_ID);
        return ppv;
    }


    /*
     * Create some test bundles
     */
    static protected void createTestBundles(final Context ctx, final List bundleFees, final List bundles,
            final List loyaltyBundleFees)
    {
        /*
         * Create some test bundles
         */
        for (int i = 1; i < 6; i++)
        {
            final BundleFee fee = new BundleFee();
            fee.setId(i);
            fee.setFee(i * 10);
            // skushwaha fee.setMandatory(false);
            fee.setServicePreference(ServicePreferenceEnum.OPTIONAL);
            fee.setPaymentNum(0);
            fee.setServicePeriod(ServicePeriodEnum.MONTHLY);
            fee.setSource(BundleFee.AUXILIARY);
            fee.setStartDate(new Date());
            fee.setEndDate(new Date());

            final BundleProfile bundle = new BundleProfile();
            bundle.setSpid(1);
            bundle.setBundleId(i);
            bundle.setContext(ctx);
            if (i < 4)
            {
                bundleFees.add(fee);

                bundle.setAuxiliary(true);
                bundle.setSegment(BundleSegmentEnum.POSTPAID);
                bundle.setQuotaScheme(QuotaTypeEnum.FIXED_QUOTA);
                BundleCategoryAssociation association = new BundleCategoryAssociation();
                association.setCategoryId(3);
                association.setType(UnitTypeEnum.VOLUME_SECONDS_INDEX);
                Map map = new HashMap();
                map.put(new Object(), association);
                bundle.setBundleCategoryIds(map);
            }
            else
            {
                loyaltyBundleFees.add(fee);

                bundle.setAuxiliary(true);
                bundle.setSegment(BundleSegmentEnum.HYBRID);
                bundle.setQuotaScheme(QuotaTypeEnum.MOVING_QUOTA);
                BundleCategoryAssociation association = new BundleCategoryAssociation();
                association.setCategoryId(5);
                association.setType(UnitTypeEnum.VOLUME_SECONDS_INDEX);
                Map map = new HashMap();
                map.put(new Object(), association);
                bundle.setBundleCategoryIds(map);
            }

            bundles.add(bundle);
        }
    }


    protected void markBundlesProvisioned(final Context ctx, final Collection bundles, int subscriptionType) throws BundleManagerException,
            BucketAlreadyExistsException
    {
        for (final Iterator it = bundles.iterator(); it.hasNext();)
        {
            long bundleID = 0;
            final Object obj = it.next();
            if (obj instanceof BundleProfile)
            {
                bundleID = ((BundleProfile) obj).getBundleId();
            }
            else if (obj instanceof SubscriberBucket)
            {
                bundleID = ((SubscriberBucket) obj).getBundleId();
            }
            else if (obj instanceof BundleFee)
            {
                bundleID = ((BundleFee) obj).getId();
            }

            bucketService_.createBucket(ctx, MSISDN, subscriptionType, bundleID);
        }
    }


    protected void clearCounters()
    {
        bundleService_.clearCounters();
        bucketService_.clearCounters();
    }

    /* The home we're testing */
    BucketProvHome home_ = null;

    /* The home to store objects for testing */
    Home subHome_ = null;

    TestFakeCRMBundleProfile bundleService_;
    TestFakeCRMSubscriberBucketProfile bucketService_;

    /* Some bundles for testing */
    List<BundleFee> bundleFees_;
    List<BundleFee> loyaltyBundleFees_;

    /* Testing subscriber stub */
    Subscriber subscriber_ = null;
}
