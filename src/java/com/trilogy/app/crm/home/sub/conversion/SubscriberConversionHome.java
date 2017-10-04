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
package com.trilogy.app.crm.home.sub.conversion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.Lookup;

/**
 * Executes the process to convert a subscriber (validation, process and pipeline delegation).
 *
 * @author arturo.medina@redknee.com
 */
public class SubscriberConversionHome extends HomeProxy
{
    /**
     * @param ctx
     * @param delegate
     */
    public SubscriberConversionHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
        convertions_ = new ArrayList();
    }

    /**
     * {@inheritDoc}
     */
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        if (obj instanceof Subscriber)
        {
            final Subscriber newSubs = (Subscriber) obj;
            final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
            Subscriber subscriber = null;
            final Iterator iter = convertions_.iterator();

            while (iter.hasNext() && subscriber == null)
            {
                final SubscriberConversion executor = (SubscriberConversion) iter.next();
                subscriber = executor.convertSubscriber(ctx, oldSub, newSubs, getDelegate());
            }

            //no conversion, going with the flow
            if (subscriber == null)
            {
                subscriber = (Subscriber) super.store(ctx, obj);
            }

            return subscriber;
        }

        return obj;
    }

    public SubscriberConversionHome add(final SubscriberConversion convertion)
    {
        convertions_.add(convertion);
        return this;
    }

    //private attributes
    private List convertions_;
}
