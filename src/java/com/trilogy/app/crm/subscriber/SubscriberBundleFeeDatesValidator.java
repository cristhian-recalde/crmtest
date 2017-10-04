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

package com.trilogy.app.crm.subscriber;

import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleFeeXInfo;
import com.trilogy.app.crm.home.DateValidator;
import com.trilogy.app.crm.support.Lookup;

/**
 * Validates the dates of a {@link BundleFee} object inside a {@link Subscriber}.
 *
 * @author cindy.wong@redknee.com
 */
public class SubscriberBundleFeeDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>SubscriberBundleFeeDatesValidator</code>.
     */
    protected SubscriberBundleFeeDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>SubscriberBundleFeeDatesValidator</code>.
     *
     * @return An instance of <code>SubscriberBundleFeeDatesValidator</code>.
     */
    public static SubscriberBundleFeeDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberBundleFeeDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final Subscriber newSubscriber = (Subscriber) object;
        final Subscriber oldSubscriber = (Subscriber) context.get(Lookup.OLDSUBSCRIBER);
        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
        if (config == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: GeneralConfig not found in context");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();

        final Map newBundleFees = newSubscriber.getBundles();
        Map oldBundleFees = null;
        if (oldSubscriber != null)
        {
            oldBundleFees = oldSubscriber.getBundles();
        }
        if (oldBundleFees == null)
        {
            oldBundleFees = Collections.EMPTY_MAP;
        }
        for (final Iterator iterator = newBundleFees.entrySet().iterator(); iterator.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final Object key = entry.getKey();
            final BundleFee newFee = (BundleFee) entry.getValue();
            final BundleFee oldFee = (BundleFee) oldBundleFees.get(key);

            try
            {
				validAfter(context, oldFee, newFee, BundleFeeXInfo.START_DATE,
				    Calendar.DAY_OF_MONTH, 0);
                // protect against really, really off dates.
				validBefore(context, oldFee, newFee, BundleFeeXInfo.START_DATE,
				    Calendar.YEAR, 100);
            }
            catch (IllegalPropertyArgumentException exception)
            {
                exceptions.thrown(exception);
            }

            try
            {
				validAfter(context, oldFee, newFee, BundleFeeXInfo.END_DATE,
				    Calendar.DAY_OF_MONTH, 0);
                // protect against really, really off dates.
				validBefore(context, oldFee, newFee, BundleFeeXInfo.END_DATE,
				    Calendar.YEAR, 100);
            }
            catch (IllegalPropertyArgumentException exception)
            {
                exceptions.thrown(exception);
            }

            try
            {
                BundleProfile bProfile = newFee.getBundleProfile(context, newSubscriber.getSpid());
                if (!bProfile.isEnabled() && oldFee == null )
                {
                	LogSupport.info(context, this, "Bundle [" + bProfile.getBundleId() +"] is disabled and cannot be assigned[added] to this Subscription");
                    exceptions.thrown(new IllegalPropertyArgumentException(BundleFeeXInfo.ID, "Bundle profile [ "
                            + newFee.getId() + "]is disabled and cannot be assigned[added] to this Subscription."));
                }
            }
            catch (Exception ex)
            {
                exceptions.thrown(new IllegalPropertyArgumentException(" Unable to load bundle profile ["
                        + newFee.getId() + "]", ex));
            }
            exceptions.throwAll();
        }
    }

    /**
     * Singleton instance.
     */
    private static SubscriberBundleFeeDatesValidator instance;
}
