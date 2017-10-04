package com.trilogy.app.crm.bundle;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.product.bundle.manager.api.v21.BundleService;
import com.trilogy.product.bundle.manager.api.v21.BundleServiceException;
import com.trilogy.product.bundle.manager.api.v21.BundleServiceInternalException;

public class FakeBundleService implements BundleService
{
    ///* Counter storing number of times method was called
    public int removeSubscriberBucketCallCount_ = 0;
    public int switchBundlesCallCount_ = 0;
    public int updateBucketActiveCallCount_ = 0;
    public HashMap oldBundles = new HashMap();
    public HashMap newBundles = new HashMap();
    public HashMap deactivatedBundles = new HashMap();
    public HashMap deactivatedBundleMsisdn = new HashMap();
    public HashMap activatedBundles = new HashMap();
    public HashMap activatedBundleMsisdn = new HashMap();

    public short updateBucketActive(String msisdn, int spid, long bundleId, boolean active, boolean prorate) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return updateBucketActive(getContext(), msisdn, spid, bundleId, active, prorate);
    }

    public short updateAllBucketsActive(String msisdn, int spid, boolean active, boolean prorate) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    public Map switchBundles(String msisdn, int spid, List oldBundleIds, List newBundleIds, List recOptions) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return switchBundles(getContext(), msisdn, spid, oldBundleIds, newBundleIds, recOptions);
    }

    public short switchBundlesForPlan(String pricePlan, int spid, List oldBundleIds, List newBundleIds, List recOptions) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        switchBundlesCallCount_++;
        return 0;
    }

    public short increaseBalanceLimit(String msisdn, int spid, long bundleId, int amount) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    public short decreaseBalanceLimit(String msisdn, int spid, long bundleId, long amount) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    public Collection getAllSubscriberBalances(String msisdn) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return null;
    }

    public Collection addSubscriberBulkload(List newBuckets) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return null;
    }

    public int removeSubscriberBucket(String msisdn, int spid, long bundleId) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return removeSubscriberBucket(getContext(), msisdn, spid, bundleId);
    }

    public short updateBucketActive(Context ctx, String msisdn, int spid, long bundleId, boolean active, boolean prorate) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        updateBucketActiveCallCount_++;
        if (active)
        {
            activatedBundles.put(Integer.valueOf(updateBucketActiveCallCount_), Long.valueOf(bundleId));
            activatedBundleMsisdn.put(Integer.valueOf(updateBucketActiveCallCount_), msisdn);
        }
        else
        {
            deactivatedBundles.put(Integer.valueOf(updateBucketActiveCallCount_), Long.valueOf(bundleId));
            deactivatedBundleMsisdn.put(Integer.valueOf(updateBucketActiveCallCount_), msisdn);
        }
        return 0;
    }

    public short updateAllBucketsActive(Context ctx, String msisdn, int spid, boolean active, boolean prorate) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    public Map switchBundles(Context ctx, String msisdn, int spid, List oldBundleIds, List newBundleIds, List recOptions) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        switchBundlesCallCount_++;
        Integer key = Integer.valueOf(switchBundlesCallCount_);
        oldBundles.put(key, oldBundleIds);
        newBundles.put(key, newBundleIds);
        return new HashMap();
    }

    public short switchBundlesForPlan(Context ctx, String pricePlan, int spid, List oldBundleIds, List newBundleIds, List recOptions) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    public short increaseBalanceLimit(Context ctx, String msisdn, int spid, long bundleId, int amount) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    public short decreaseBalanceLimit(Context ctx, String msisdn, int spid, long bundleId, int amount) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    public Collection getAllSubscriberBalances(Context ctx, String msisdn) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return null;
    }

    public Collection addSubscriberBulkload(Context ctx, List newBuckets) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return null;
    }

    public int removeSubscriberBucket(Context ctx, String msisdn, int spid, long bundleId) throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        removeSubscriberBucketCallCount_++;
        return 0;
    }

    public Context getContext()
    {
        return null;
    }

    public void setContext(Context context)
    {
    }

    public Collection getAllSubscriberBalancesV26(String arg0)
            throws BundleServiceException, BundleServiceInternalException,
            HomeException
    {
        return null;
    }

    public short updateBucketStatus(String arg0) throws BundleServiceException,
            BundleServiceInternalException, HomeException
    {
        return 0;
    }

    public Collection getAllSubscriberBalancesV26(Context arg0, String arg1)
            throws BundleServiceException, BundleServiceInternalException,
            HomeException
    {
        return null;
    }

    public short updateBucketStatus(Context arg0, String arg1)
            throws BundleServiceException, BundleServiceInternalException,
            HomeException
    {
        return 0;
    }

    @Override
    public short increaseBalanceLimit(String msisdn, int spid, long bundleId, long amount)
            throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    @Override
    public short increaseBalanceLimit(Context ctx, String msisdn, int spid, long bundleId, long amount)
            throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    @Override
    public short decreaseBalanceLimit(Context ctx, String msisdn, int spid, long bundleId, long amount)
            throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }
}