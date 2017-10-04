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

package com.trilogy.app.crm.bundle;

import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.support.BillCycleSupport;

/**
 * Function for converting from BundleFee to SubscriberBucketApi.
 * Subscriber state should not change between creation of this object and usage.
 *
 * @author kevin.greer@redknee.com
 * @author victor.stratan@redknee.com
 */
public class BundleFeeToSubscriberBucketApiFunction implements Function
{
    /**
     * Subscriber for which to do the conversion.
     */
    private final Subscriber sub_;
    private final int billDay_;

    /**
     * Constructor requires the subscriber for which to do the conversion.
     *
     * @param sub Subscriber for which to do the conversion
     */
    public BundleFeeToSubscriberBucketApiFunction(final Context ctx, final Subscriber sub) throws HomeException
    {
        sub_ = sub;
        if (sub_.getState() == SubscriberStateEnum.AVAILABLE
                || sub_.getState() == SubscriberStateEnum.PENDING)
        {
            billDay_ = -1;
        }
        else
        {
            billDay_ = BillCycleSupport.getBillCycleForBan(ctx, sub_.getBAN()).getDayOfMonth();
        }
    }

    /**
     * Function method. Converts BundleFee to SubscriberBucketApi.
     * If subscriber is not activated then BillingDate is set to -1, otherwise it does not matter.
     *
     * @param ctx operating context
     * @param obj BundleFee to convert
     * @return resulting SubscriberBucketApi
     */
    public Object f(final Context ctx, final Object obj)
    {
        BundleFee fee = (BundleFee) obj;
        SubscriberBucket bucket = new SubscriberBucket();

        bucket.setMsisdn(sub_.getMSISDN());
        bucket.setSpid(sub_.getSpid());
        bucket.setSubscriptionType((int) sub_.getSubscriptionType());
        bucket.setBundleId(fee.getId());
        bucket.setAuxiliary(fee.isAuxiliarySource());

        final RecurrenceOptions options = bucket.getOptions();
        options.setBundleId(fee.getId());
        options.setBillingDatePresent(true);
        options.setBillingDate(billDay_);
        options.setExpiryTime(fee.getEndDate().getTime());

        return bucket;
    }
}


