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

package com.trilogy.app.crm.duplicatedetection;

import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionSubscriptionResult;

/**
 * Adapter for subscriber into detection search result.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class SubscriberDetectionResultAdapter implements Adapter, Function
{
    private static SubscriberDetectionResultAdapter instance = new SubscriberDetectionResultAdapter();

    private static final long serialVersionUID = 1L;

    protected SubscriberDetectionResultAdapter()
    {
        // empty
    }

    public static SubscriberDetectionResultAdapter instance()
    {
        return instance;
    }

    @Override
    public Object f(final Context ctx, final Object obj)
    {
        final Subscriber subscriber = (Subscriber) obj;
		final DuplicateAccountDetectionSubscriptionResult result =
		    new DuplicateAccountDetectionSubscriptionResult();
        result.setSubscriptionType(subscriber.getSubscriptionType());
        result.setPhoneNumber(subscriber.getMSISDN());
		result.setState(subscriber.getState());

        return result;
    }

    @Override
    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        return null;
    }

    @Override
    public Object adapt(final Context ctx, final Object obj)
    {
        return f(ctx, obj);
    }
}
