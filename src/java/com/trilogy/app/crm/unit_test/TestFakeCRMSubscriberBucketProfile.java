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
package com.trilogy.app.crm.unit_test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;

import com.trilogy.app.crm.bundle.StatusEnum;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.bundle.SubscriberBucketID;
import com.trilogy.app.crm.bundle.SubscriberBucketTransientHome;
import com.trilogy.app.crm.bundle.SubscriberBucketsAndBalances;
import com.trilogy.app.crm.bundle.exception.BucketAlreadyExistsException;
import com.trilogy.app.crm.bundle.exception.BucketDoesNotExistsException;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

/**
 * @author victor.stratan@redknee.com
 */
public class TestFakeCRMSubscriberBucketProfile implements CRMSubscriberBucketProfile
{
    final Home home_;

    ///* Counter storing number of times method was called
    public ArrayList<SubscriberBucket> createdBuckets_ = new ArrayList<SubscriberBucket>();
    public ArrayList<SubscriberBucket> removedBuckets_ = new ArrayList<SubscriberBucket>(); 
    public ArrayList<SubscriberBucket> updateStatusBuckets_ = new ArrayList<SubscriberBucket>();

    public TestFakeCRMSubscriberBucketProfile(final Context ctx)
    {
        home_ = new TransientFieldResettingHome(ctx, new SubscriberBucketTransientHome(ctx));
    }

    public void clearCounters()
    {
        createdBuckets_.clear();
        removedBuckets_.clear();
        updateStatusBuckets_.clear();
    }

    public SubscriberBucket getBucketById(final Context ctx, final long bucketId) throws BucketDoesNotExistsException, BundleManagerException
    {
        return null;
    }

    public Home getBuckets(final Context ctx, final String msisdn, int subscriptionType) throws BundleManagerException
    {
        // TODO implement
        return home_;
    }

    public List getBalances(final Context ctx, final String msisdn, int subscriptionType) throws BucketDoesNotExistsException, BundleManagerException
    {
        return null;
    }

    public void createBucket(final Context ctx, final String msisdn, int subscriptionType, final long bundleId) throws BucketAlreadyExistsException, BundleManagerException
    {
        final SubscriberBucket bucket = new SubscriberBucket();
        bucket.setMsisdn(msisdn);
        bucket.setSubscriptionType(subscriptionType);
        bucket.setBundleId(bundleId);
        createdBuckets_.add(bucket);
        try
        {
            home_.create(ctx, bucket);
        }
        catch (HomeException e)
        {
            throw new BundleManagerException("Error in createBucket() ", e);
        }
    }

    public void createBuckets(final Context ctx, final Collection subs, final Collection buckets) throws BucketAlreadyExistsException, BundleManagerException
    {
        Collection<SubscriberBucket> typedBuckets = buckets;
        for (SubscriberBucket bucket : typedBuckets)
        {
            createdBuckets_.add(bucket);
            try
            {
                home_.create(ctx, bucket);
            }
            catch (HomeException e)
            {
                throw new BundleManagerException("Error in createBuckets() ", e);
            }
        }
    }

    public void createBucket(final Context ctx, final SubscriberBucket bucket) throws BucketAlreadyExistsException, BundleManagerException
    {
        createdBuckets_.add(bucket);
        try
        {
            home_.create(ctx, bucket);
        }
        catch (HomeException e)
        {
            throw new BundleManagerException("Error in createBuckets() ", e);
        }
    }

    public SubscriberBucket getBucket(final Context ctx, final String msisdn, int subscriptionType, final long bundleId) throws BucketDoesNotExistsException, BundleManagerException
    {
        return null;
    }

    public void updateBucket(final Context ctx, final String msisdn, final int spid, int subscriptionType, final long bundleId, final boolean active, final boolean prorate) throws BucketDoesNotExistsException, BundleManagerException
    {

    }

    public long removeBucket(final Context ctx, final int spid, final String msisdn, int subscriptionType, final long bundleId) throws BucketDoesNotExistsException, BundleManagerException
    {
        final SubscriberBucketID bucketID = new SubscriberBucketID(msisdn, bundleId);
        final SubscriberBucket bucket = new SubscriberBucket();
        bucket.setMsisdn(msisdn);
        bucket.setSubscriptionType(subscriptionType);
        bucket.setBundleId(bundleId);
        removedBuckets_.add(bucket);

        try
        {
            home_.remove(ctx, bucketID);
        }
        catch (HomeException e)
        {
            throw new BundleManagerException("Error in removeBucket() ", e);
        }

        return 0;
    }

    public Map removeBuckets(final Context ctx, final Collection msisdns, final Collection bundleLists) throws BundleManagerException
    {
        Collection<SubscriberBucket> typedBuckets = bundleLists;
        for (SubscriberBucket bucket : typedBuckets)
        {
            removedBuckets_.add(bucket);
            final SubscriberBucketID bucketID = new SubscriberBucketID(bucket.getMsisdn(), bucket.getBundleId());
            try
            {
                home_.remove(ctx, bucketID);
            }
            catch (HomeException e)
            {
                throw new BundleManagerException("Error in removeBuckets() ", e);
            }
        }

        return new HashMap();
    }

    public int increaseBalanceLimit(final Context ctx, final String msisdn, final int spid, int subscriptionType, final long bundleId, final long amount) throws BundleManagerException
    {
        return 0;
    }

    public int decreaseBalanceLimit(final Context ctx, final String msisdn, final int spid, int subscriptionType, final long bundleId, final long amount) throws BundleManagerException
    {
        return 0;
    }

    public void updateBucketStatus(final Context ctx, final String msisdn, final int spid, int subscriptionType, final long bundleId, final boolean status, final boolean prorated) throws BucketDoesNotExistsException, BundleManagerException
    {
        final SubscriberBucket bucket = new SubscriberBucket();
        bucket.setSpid(spid);
        bucket.setMsisdn(msisdn);
        bucket.setSubscriptionType(subscriptionType);
        bucket.setBundleId(bundleId);
        bucket.setStatus(status ? StatusEnum.ACTIVE : StatusEnum.SUSPENDED);
        updateStatusBuckets_.add(bucket);
    }

	@Override
	public int increaseBalanceLimit(Context ctx, String msisdn, int spid,
			int subscriptionType, long bundleId, long amount,
			Parameter[] inParamSet,Map<Short, Parameter> outParameterMap) throws BundleManagerException {
		return 0;
	}

	@Override
	public int decreaseBalanceLimit(Context ctx, String msisdn, int spid,
			int subscriptionType, long bundleId, long amount,
			Parameter[] inParamSet,Map<Short, Parameter> outParameterMap) throws BundleManagerException {
		return 0;
	}

    @Override
    public SubscriberBucketsAndBalances getBucketsWithCategorySummary(
            Context ctx, String msisn) throws BundleManagerException
    {
        return new SubscriberBucketsAndBalances();
    }
}
