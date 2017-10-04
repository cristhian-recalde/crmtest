/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bundle.BundleAuxiliaryService;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bundle.BundleFeeToSubscriberBucketApiFunction;
import com.trilogy.app.crm.bundle.BundleFeeXInfo;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.bundle.exception.BucketDoesNotExistsException;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;


/**
 * Various support methods for handling bundle auxiliary services
 * 
 * @author dannyng
 * 
 */
public class BundleAuxiliaryServiceSupport
{

    public BundleAuxiliaryServiceSupport()
    {
        super();
    }

    /**
     * 
     * @param ctx
     * @param bundleFee
     * @param sub
     * @return
     */
    public static SubscriberBucket adaptBundleFeeToSubscriberBucketApi(Context ctx, BundleFee bundleFee,
            Subscriber sub) throws HomeException
    {
        BundleFeeToSubscriberBucketApiFunction adapter = new BundleFeeToSubscriberBucketApiFunction(ctx, sub);
        return (SubscriberBucket) adapter.f(ctx, bundleFee);
    }

    /**
     * 
     * @param ctx
     * @param bundleFee
     * @param sub
     * @return
     * @throws HomeException
     */
    public static BundleAuxiliaryService adaptBundleFeeToBundleAuxiliaryService(Context ctx, BundleFee bundleFee,
            Subscriber sub) throws HomeException
    {
        BundleAuxiliaryService bundle = new BundleAuxiliaryService();
        // Copy all the bundleFee values over to the bundle
        try
        {
            XBeans.copy(ctx, BundleFeeXInfo.instance(), bundleFee, BundleAuxiliaryServiceXInfo.instance(), bundle);
        }
        catch (Exception e)
        {
            throw new HomeException(e);
        }
        bundle.setSubscriberId(sub.getId());
        bundle.setProvisioned(false);
        return bundle;
    }

    /**
     * 
     * @param ctx
     * @param bundle
     * @return
     * @throws HomeException
     */
    public static BundleFee adaptBundleAuxiliaryServiceToBundleFee(Context ctx, BundleAuxiliaryService bundle) throws HomeException
    {
        BundleFee bundleFee = new BundleFee();
        // Copy all the bundleFee values over to the bundle
        try
        {
            XBeans.copy(ctx, BundleAuxiliaryServiceXInfo.instance(), bundle, BundleFeeXInfo.instance(), bundleFee);
        }
        catch (Exception e)
        {
            throw new HomeException(e);
        }
        return bundleFee;
    }

    
    /**
     * 
     * @param ctx
     * @param subscriptionType subscription type of the subscription
     * @param msisdn
     * @param bundleId id of the bucket
     * @return
     * @throws HomeException
     */
    public static boolean isBucketProvisioned(Context ctx, int subscriptionType, String msisdn, long bundleId)
            throws HomeException
    {
        boolean provisioned = false;
        try
        {
            CRMSubscriberBucketProfile service = (CRMSubscriberBucketProfile) ctx.get(CRMSubscriberBucketProfile.class);
            // Check if bundled actually got provisioned
            SubscriberBucket foundBucket = service.getBucket(ctx, msisdn, subscriptionType, bundleId);
            /* if we found it then it got provisioned, if not, then it didn't */
            provisioned = (foundBucket != null); 
        }
        catch (BucketDoesNotExistsException ex)
        {
        	new InfoLogMsg(
        			BundleAuxiliaryServiceSupport.class.getName(), "isBucketProvisioned(): Bucket " + bundleId + " from msisdn " + msisdn + " does not exists"
            , ex).log(ctx);
        }
        catch (BundleManagerException ex)
        {
            LogSupport.major(ctx, BundleAuxiliaryServiceSupport.class.getName(), "isBucketProvisioned(): Error while getting the bucket " + bundleId + " from msisdn " + msisdn, ex);
            throw new HomeException(ex);
        }
        return provisioned;
    }
}
