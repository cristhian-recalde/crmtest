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
package com.trilogy.app.crm.bundle.driver;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.product.bundle.manager.api.v21.BundleService;
import com.trilogy.product.bundle.manager.api.v21.BundleServiceException;
import com.trilogy.product.bundle.manager.api.v21.BundleServiceInternalException;

public class TestBundleServiceServerv21 extends ContextAwareSupport implements BundleService
{
    public TestBundleServiceServerv21(final Context context) throws IllegalArgumentException
    {
        super();
        setContext(context);
    }

    /**
     * {@inheritDoc}
     */
    public short updateBucketStatus(final String msisdn) throws BundleServiceException, BundleServiceInternalException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public short updateBucketStatus(final Context context, final String msisdn)
        throws BundleServiceException, BundleServiceInternalException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public short updateBucketActive(final String msisdn, final int spid, final long bundleid, final boolean active,
            final boolean prorate)
        throws BundleServiceException, BundleServiceInternalException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public short decreaseBalanceLimit(final String msisdn, final int spid, final long bundleid, final long amount)
        throws BundleServiceException, BundleServiceInternalException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public Collection addSubscriberBulkload(final List newBuckets)
        throws BundleServiceException, BundleServiceInternalException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public short updateBucketActive(final Context ctx, final String msisdn, final int spid, final long bundleid,
            final boolean active, final boolean prorate)
        throws BundleServiceException, BundleServiceInternalException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public short decreaseBalanceLimit(final Context ctx, final String msisdn, final int spid, final long bundleid,
            final int amount)
        throws BundleServiceException, BundleServiceInternalException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public Collection addSubscriberBulkload(final Context ctx, final List newBuckets)
        throws BundleServiceException, BundleServiceInternalException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public short updateAllBucketsActive(final String msisdn, final int spid, final boolean active,
            final boolean prorate)
        throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public short switchBundlesForPlan(final String pricePlan, final int spid, final List oldBundleIds,
            final List newBundleIds, final List recOptions)
        throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public short increaseBalanceLimit(final String msisdn, final int spid, final long bundleid, final int amount)
        throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public short updateAllBucketsActive(final Context ctx, final String msisdn, final int spid, final boolean active,
            final boolean prorate)
        throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public short switchBundlesForPlan(final Context ctx, final String pricePlan, final int spid,
            final List oldBundleIds, final List newBundleIds, final List recOptions)
        throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public short increaseBalanceLimit(final Context ctx, final String msisdn, final int spid, final long bundleid, final int amount)
        throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public Collection getAllSubscriberBalances(final Context ctx, final String msisdn)
        throws HomeException
    {
        return Collections.EMPTY_LIST;
    }

    /**
     * {@inheritDoc}
     */
    public Collection getAllSubscriberBalances(final String msisdn)
        throws HomeException
    {
        return Collections.EMPTY_LIST;
    }

    /**
     * {@inheritDoc}
     */
    public Map switchBundles(final String msisdn, final int spid, final List oldBundleIds, final List newBundleIds,
            final List recOptions)
        throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return Collections.EMPTY_MAP;
    }

    /**
     * {@inheritDoc}
     */
    public int removeSubscriberBucket(final String msisdn, final int spid, final long bundleId)
        throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public Map switchBundles(final Context ctx, final String msisdn, final int spid, final List oldBundleIds,
            final List newBundleIds, final List recOptions)
        throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return Collections.EMPTY_MAP;
    }

    /**
     * {@inheritDoc}
     */
    public int removeSubscriberBucket(final Context ctx, final String msisdn, final int spid, final long bundleId)
        throws BundleServiceException, BundleServiceInternalException, HomeException
    {
        return 0;
    }
    
    public Collection getAllSubscriberBalancesV26(String msisdn)
		throws BundleServiceException,  BundleServiceInternalException,  HomeException

    {
		    
		    return Collections.EMPTY_LIST;
    }
    
    public Collection getAllSubscriberBalancesV26(Context ctx, String msisdn)
		throws BundleServiceException,  BundleServiceInternalException,  HomeException
    {
		
		return Collections.EMPTY_LIST;
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
