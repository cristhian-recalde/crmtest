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

import com.trilogy.app.crm.bundle.SubcriberBucketModelBundleManager;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.product.bundle.manager.api.BalanceApplicationXMLHome;
import com.trilogy.product.bundle.manager.api.BundleCategoryApiXMLHome;
import com.trilogy.product.bundle.manager.api.BundleProfileApiXMLHome;
import com.trilogy.product.bundle.manager.api.SubscriberBucketRetrievalApiXMLHome;
import com.trilogy.product.bundle.manager.api.v21.SubscriberBucketApiXMLHome;

/**
 * Bundle Manager Driver Proxy
 * Uses XMLHomes
 *
 * @author kgreer
 */
public class TestXMLBMDriver extends TestBMDriver implements Constants
{

    public TestXMLBMDriver()
    {
    }

    public TestXMLBMDriver(final Context ctx)
    {
        super(ctx);
    }

    @Override
    public void initHomes()
    {
        bundleProfileHome_ = new BundleProfileApiXMLHome(getContext(),
                CoreSupport.getFile(getContext(), BUNDLE_PROFILE_HOME));
        bundleCategoryHome_ = new BundleCategoryApiXMLHome(getContext(),
                CoreSupport.getFile(getContext(), BUNDLE_CATEGORY_HOME));
        bundleProfileHomev21_ = new com.redknee.product.bundle.manager.api.v21.BundleProfileApiXMLHome(getContext(),
                CoreSupport.getFile(getContext(), BUNDLE_PROFILE_HOME_V21));
        bundleCategoryHomev21_ = new com.redknee.product.bundle.manager.api.v21.BundleCategoryApiXMLHome(getContext(),
                CoreSupport.getFile(getContext(), BUNDLE_CATEGORY_HOME_V21));
        subscriberBucketHome_ = new SubscriberBucketApiXMLHome(getContext(),
                CoreSupport.getFile(getContext(), SUB_BUCKET_HOME));
        subscriberBucketRetrievalHome_ = new SubscriberBucketRetrievalApiXMLHome(getContext(),
                CoreSupport.getFile(getContext(), SUB_BUCKET_RETRIEVAL_HOME));
        balanceApplicationHome_ = new BalanceApplicationXMLHome(getContext(),
                CoreSupport.getFile(getContext(), BALANCE_APPLICATION_HOME));

        subscriberBucketHomev21_ = new com.redknee.product.bundle.manager.api.v21.SubscriberBucketApiXMLHome(
                getContext(), CoreSupport.getFile(getContext(), SUB_BUCKET_HOME_V21));
        subscriberBucketRetrievalHomev21_ =
                new com.redknee.product.bundle.manager.api.v21.SubscriberBucketRetrievalApiXMLHome(getContext(),
                        CoreSupport.getFile(getContext(), SUB_BUCKET_RETRIEVAL_HOME_V21));

        final SubcriberBucketModelBundleManager generator = BundleSupportHelper.get(getContext()).getSubscriberBucketModel(getContext());

        subscriberBucketRetrievalHomev26_ = generator.getSubscriberBucketXMLHome(
                getContext(), CoreSupport.getFile(getContext(), SUB_BUCKET_RETRIEVAL_HOME_V26));

    }
}


