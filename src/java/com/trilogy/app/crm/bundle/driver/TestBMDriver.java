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

import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.SubcriberBucketModelBundleManager;
import com.trilogy.app.crm.bundle.SubscriberBucketHome;
import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.product.bundle.manager.api.BalanceApplicationXDBHome;
import com.trilogy.product.bundle.manager.api.BundleCategoryApiHome;
import com.trilogy.product.bundle.manager.api.BundleCategoryApiXDBHome;
import com.trilogy.product.bundle.manager.api.BundleProfileApiXDBHome;
import com.trilogy.product.bundle.manager.api.BundleService;
import com.trilogy.product.bundle.manager.api.SubscriberBucketApiXDBHome;
import com.trilogy.product.bundle.manager.api.SubscriberBucketRetrievalApiXDBHome;

/**
 * Bundle Manager Driver Proxy
 * Uses XDBHomes
 *
 * @author kgreer
 */
public class TestBMDriver extends AbstractTestBMDriver implements ContextAware
{
    protected Home bundleProfileHome_;
    protected Home bundleCategoryHome_;
    protected Home bundleProfileHomev21_;
    protected Home bundleCategoryHomev21_;
    protected Home subscriberBucketHome_;
    protected Home subscriberBucketRetrievalHome_;
    protected Home balanceApplicationHome_;

    protected Home subscriberBucketHomev21_;
    protected Home subscriberBucketRetrievalHomev21_;
    protected Home subscriberBucketRetrievalHomev26_;

    protected BundleService bundleService_;

    protected com.redknee.product.bundle.manager.api.v21.BundleService bundleServicev21_;

    protected Context context_ = new ContextSupport();

    private static final String SERVICE_NAME = "BundleManager(Test)";
    private static final String SERVICE_DESCRIPTION = "Test stub for Bundle Manager";

    public TestBMDriver()
    {
    }

    public TestBMDriver(Context ctx)
    {
        setContext(ctx);
    }

    public Context getContext()
    {
        return context_;
    }

    public void setContext(Context ctx)
    {
        context_ = ctx;
    }

    protected void initHomes()
    {
        final SubcriberBucketModelBundleManager generator = BundleSupportHelper.get(getContext()).getSubscriberBucketModel(getContext());

        bundleProfileHome_ = new BundleProfileApiXDBHome(getContext());
        bundleCategoryHome_ = new BundleCategoryApiXDBHome(getContext());
        bundleProfileHomev21_ = new com.redknee.product.bundle.manager.api.v21.BundleProfileApiXDBHome(getContext());
        bundleCategoryHomev21_ = new com.redknee.product.bundle.manager.api.v21.BundleCategoryApiXDBHome(getContext());
        subscriberBucketHome_ = new SubscriberBucketApiXDBHome(getContext());
        subscriberBucketRetrievalHome_ = new SubscriberBucketRetrievalApiXDBHome(getContext());
        balanceApplicationHome_ = new BalanceApplicationXDBHome(getContext());

        subscriberBucketHomev21_ = new com.redknee.product.bundle.manager.api.v21.SubscriberBucketApiXDBHome(getContext());
        subscriberBucketRetrievalHomev21_ = new com.redknee.product.bundle.manager.api.v21.SubscriberBucketRetrievalApiXDBHome(getContext());
        
        subscriberBucketRetrievalHomev26_ = generator.getSubscriberBucketXDBHome(getContext());
    }

    protected void initServices()
    {
        bundleService_ = new TestBundleServiceServer(getContext());
    }

    // Homes

    public Home bundleProfileApiHome()
    {
        return bundleProfileHome_;
    }

    public Home bundleCategoryApiHome()
    {
        return bundleCategoryHome_;
    }

    public Home bundleProfileApiHomev21()
    {
        return bundleProfileHomev21_;
    }

    public Home bundleCategoryApiHomev21()
    {
        return bundleCategoryHomev21_;
    }

    public Home subscriberBucketApiHome()
    {
        return subscriberBucketHome_;
    }

    public Home subscriberBucketRetrievalApiHome()
    {
        return subscriberBucketRetrievalHome_;
    }

    public Home balanceApplicationHome()
    {
        return balanceApplicationHome_;
    }


    public Home subscriberBucketApiHomev21()
    {
        return subscriberBucketHomev21_;
    }

    public Home subscriberBucketRetrievalApiHomev21()
    {
        return subscriberBucketRetrievalHomev21_;
    }

    public Home subscriberBucketRetrievalApiHomev26()
    {
        return subscriberBucketRetrievalHomev26_;
    }

    // Services

    public BundleService bundleService()
    {
        return bundleService_;
    }

    public com.redknee.product.bundle.manager.api.v21.BundleService bundleServicev21()
    {
        return bundleServicev21_;
    }

    public void injectBundleManagerHomes(final Context ctx)
    {
        ctx.put(BundleProfileHome.class, bundleCategoryHomev21_);
        ctx.put(BundleCategoryApiHome.class, bundleCategoryHomev21_);
        ctx.put(SubscriberBucketHome.class, subscriberBucketHomev21_);
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceDescription()
     */
    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceName()
     */
    public String getName()
    {
        return SERVICE_NAME;
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#isServiceAlive()
     */
    public boolean isAlive()
    {
        return true;
    }
    
	/**
	 * @{inheritDoc}
	 */
	public void install(Context ctx)
	{
	    initHomes();
	    initServices();
	}
	 
	/**
	 * @{inheritDoc}
	 */
	public void uninstall(Context ctx)
	{
	}

    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus("Unknown", 0, isAlive());
    }

    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }
}


