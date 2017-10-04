/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.processor.subscription.strategy;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bundle.BundleFeeToSubscriberBucketApiFunction;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;


/**
 * Responsible for moving bundles from the old subscription to the new one.
 * 
 * The fact that BundleCopyMoveStrategy uses the BucketProvHome to accomplish this
 * means that charging is also performed here.  This is not ideal.
 * 
 * TODO: Remove BundleCopyMoveStrategy's dependency on BucketProvHome and make
 * this strategy so that it only deals with moving the bundle records as required.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.2
 */
public class BundleConversionCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends BundleCopyMoveStrategy<SMR>
{
    public BundleConversionCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        if (oldBundles_ != null && oldBundles_.size() > 0)
        {
            removeOldBundles(ctx, request, oldBundles_);
        }

        Subscriber newSubscription = request.getNewSubscription(ctx);
        Map<Long, BundleFee> bundles = SubscriberBundleSupport.getPricePlanBundles(ctx, newSubscription);
        
        final Map<Long, SubscriberBucket> newBundles = new HashMap<Long, SubscriberBucket>();
        final Map<Long, BundleFee> newProvisionedBundles = new HashMap<Long, BundleFee>();
        
        try
        {
            final Function transform = new BundleFeeToSubscriberBucketApiFunction(ctx, newSubscription);
            for (BundleFee fee : bundles.values())
            {
                if ( fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY) || 
                        fee.getServicePreference().equals(ServicePreferenceEnum.DEFAULT))
                {
                    newBundles.put(fee.getId(), (SubscriberBucket) transform.f(ctx, fee));
                    newProvisionedBundles.put(fee.getId(), fee);
                }
            }
        }
        catch (HomeException homeEx)
        {
            new MinorLogMsg(this, " Unable to find bundles for the new Subscription" + newSubscription.getId(), homeEx).log(ctx);
        }
        
        newSubscription.setBundles(newProvisionedBundles);

        // Don't call super, because we are over-riding BundleCopyMoveStrategy's createNewEntity logic, not decorating it.
        getDelegate().createNewEntity(ctx, request);
        
        if (newBundles.size() > 0)
        {
            createNewBundles(ctx, request, newBundles);
        }
    }
    
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {   
        super.removeOldEntity(ctx, request);
    }
}
