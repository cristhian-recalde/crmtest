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
package com.trilogy.app.crm.bundle.home;


import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.beans.xi.XInfoAdapter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bundle.SubcriberBucketModelBundleManager;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.support.BundleSupportHelper;

/**
 * Adapts the CRM bucket to the RMI Bundle manager beans
 * All enums and beans the XInfo doesn't understand will be mapped here
 * @author arturo.medina@redknee.com
 *
 */
public class SubscriberBucketRetrievalXInfoAdapter extends XInfoAdapter
{
    /**
     * Default constructor
     * @param source
     * @param destination
     */
    public SubscriberBucketRetrievalXInfoAdapter(XInfo source, XInfo destination)
    {
        super(source, destination);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object adapt(Context ctx, Object source)
    throws HomeException
    {
        SubscriberBucket dest = (SubscriberBucket)super.adapt(ctx, source);
        SubcriberBucketModelBundleManager generator = BundleSupportHelper.get(ctx).getSubscriberBucketModel(ctx);
        dest = generator.adaptSubscriberBucket(ctx, source, dest);
        return dest;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object unAdapt(Context ctx, Object destination) throws HomeException
    {
        Object result = super.unAdapt(ctx, destination);
        
        SubscriberBucket bucket = (SubscriberBucket)destination;
        
        SubcriberBucketModelBundleManager generator = BundleSupportHelper.get(ctx).getSubscriberBucketModel(ctx);
        result = generator.unAdaptSubscriberBucket(ctx, bucket, result);
        return result;
    }


    /**
     * 
     */
    private static final long serialVersionUID = 7270902555076928611L;

}
 