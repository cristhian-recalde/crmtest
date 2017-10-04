/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.calculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.Lookup;

/**
 * Value calculator for subscriber state change.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class SubscriberStateValueCalculator extends
        AbstractSubscriberStateValueCalculator
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Object> getDependentContextKeys(Context ctx)
    {
        final List keyCollection = new ArrayList();
        keyCollection.add(Subscriber.class);
        keyCollection.add(Lookup.OLDSUBSCRIBER);
        return keyCollection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAdvanced(Context ctx)
    {
        Object value = null;
        Subscriber subscriber = null;
        if (SubscriberStateTypeEnum.OLD_STATE_INDEX == getStateType())
        {
            subscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        }
        else
        {
            subscriber = (Subscriber) ctx.get(Subscriber.class);
        }

        if (subscriber != null)
        {
            value = subscriber.getState();
        }

        return value;
    }
}
