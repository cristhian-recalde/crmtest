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

import com.trilogy.product.bundle.manager.api.BundleService;
import com.trilogy.product.bundle.manager.api.BundleServiceException;
import com.trilogy.product.bundle.manager.api.BundleServiceInternalException;


public class TestBundleServiceServer
   extends    ContextAwareSupport
   implements BundleService
{
   
	public TestBundleServiceServer(Context context) throws IllegalArgumentException
	{
		super();
		setContext(context);
	}

	public short updateBucketActive(String msisdn, int spid, long bundleid, boolean active, boolean prorate) throws BundleServiceException, BundleServiceInternalException
	{
		return 0;
	}

	public short switchBundles(String msisdn, int spid, List oldBundleIds, List newBundleIds, boolean keepUsage) throws BundleServiceException, BundleServiceInternalException
	{
		return 0;
	}

	public short decreaseBalanceLimit(String msisdn, int spid, long bundleid, int amount) throws BundleServiceException, BundleServiceInternalException
	{
		return 0;
	}

	public Collection addSubscriberBulkload(List newBuckets) throws BundleServiceException, BundleServiceInternalException
	{
		return null;
	}

	public short updateBucketActive(Context ctx, String msisdn, int spid, long bundleid, boolean active, boolean prorate) throws BundleServiceException, BundleServiceInternalException
	{
		return 0;
	}

	public short switchBundles(Context ctx, String msisdn, int spid, List oldBundleIds, List newBundleIds, boolean keepUsage) throws BundleServiceException, BundleServiceInternalException
	{
		return 0;
	}

	public short decreaseBalanceLimit(Context ctx, String msisdn, int spid, long bundleid, int amount) throws BundleServiceException, BundleServiceInternalException
	{
		return 0;
	}

	public Collection addSubscriberBulkload(Context ctx, List newBuckets) throws BundleServiceException, BundleServiceInternalException
	{
		return null;
	}

	public short updateAllBucketsActive(String msisdn, int spid, boolean active, boolean prorate) throws BundleServiceException, BundleServiceInternalException, HomeException
	{
		return 0;
	}

	public short switchBundlesForPlan(String pricePlan, int spid, List oldBundleIds, List newBundleIds, List recOptions) throws BundleServiceException, BundleServiceInternalException, HomeException
	{
		return 0;
	}

	public short increaseBalanceLimit(String msisdn, int spid, long bundleid, int amount) throws BundleServiceException, BundleServiceInternalException, HomeException
	{
		return 0;
	}

	public short updateAllBucketsActive(Context ctx, String msisdn, int spid, boolean active, boolean prorate) throws BundleServiceException, BundleServiceInternalException, HomeException
	{
		return 0;
	}

	public short switchBundlesForPlan(Context ctx, String pricePlan, int spid, List oldBundleIds, List newBundleIds, List recOptions) throws BundleServiceException, BundleServiceInternalException, HomeException
	{
		return 0;
	}

	public short increaseBalanceLimit(Context ctx, String msisdn, int spid, long bundleid, int amount) throws BundleServiceException, BundleServiceInternalException, HomeException
	{
		return 0;
	}
   
   public Collection getAllSubscriberBalances(Context ctx, String msisdn)
      throws HomeException
   {
      return Collections.EMPTY_LIST;
   }
   
   public Collection getAllSubscriberBalances(String msisdn)
      throws HomeException
   {
      return Collections.EMPTY_LIST;
   }

/* (non-Javadoc)
 * @see com.redknee.product.bundle.manager.api.BundleServiceCI#switchBundles(java.lang.String, int, java.util.List, java.util.List, java.util.List)
 */
public Map switchBundles(String msisdn, int spid, List oldBundleIds, List newBundleIds, List recOptions) throws BundleServiceException, BundleServiceInternalException, HomeException
{
	return null;
}

/* (non-Javadoc)
 * @see com.redknee.product.bundle.manager.api.BundleServiceCI#removeSubscriberBucket(java.lang.String, int, long)
 */
public int removeSubscriberBucket(String msisdn, int spid, long bundleId) throws BundleServiceException, BundleServiceInternalException, HomeException
{
	return 0;
}

/* (non-Javadoc)
 * @see com.redknee.product.bundle.manager.api.BundleServiceSPI#switchBundles(com.redknee.framework.xhome.context.Context, java.lang.String, int, java.util.List, java.util.List, java.util.List)
 */
public Map switchBundles(Context ctx, String msisdn, int spid, List oldBundleIds, List newBundleIds, List recOptions) throws BundleServiceException, BundleServiceInternalException, HomeException
{
	return null;
}

/* (non-Javadoc)
 * @see com.redknee.product.bundle.manager.api.BundleServiceSPI#removeSubscriberBucket(com.redknee.framework.xhome.context.Context, java.lang.String, int, long)
 */
public int removeSubscriberBucket(Context ctx, String msisdn, int spid, long bundleId) throws BundleServiceException, BundleServiceInternalException, HomeException
{
	return 0;
}

public short updateBucketStatus(String arg0) throws BundleServiceException,
        BundleServiceInternalException, HomeException
{
    return 0;
}

public short updateBucketStatus(Context arg0, String arg1)
        throws BundleServiceException, BundleServiceInternalException,
        HomeException
{
    return 0;
}

}
