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

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;

/**
 * Value calculator for transfer dispute.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class TransferDisputeSubscriberValueCalculator extends
        AbstractTransferDisputeSubscriberValueCalculator
{
    public static final String CONTRIBUTOR_SUBSCRIBER_KEY = "TransferDisputeSubscriberValueCalculator.CONTRIBUTOR_SUBSCRIBER";
    public static final String RECIPIENT_SUBSCRIBER_KEY = "TransferDisputeSubscriberValueCalculator.RECIPIENT_SUBSCRIBER";

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Object> getDependentContextKeys(Context ctx)
    {
        Collection dependencies = new ArrayList();
        dependencies.add(CONTRIBUTOR_SUBSCRIBER_KEY);
        dependencies.add(RECIPIENT_SUBSCRIBER_KEY);
        return dependencies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAdvanced(Context ctx)
    {
        Object value = null;

        Object bean = null;
        if (TransferDisputeSubscriberTypeEnum.CONTRIBUTOR_INDEX == getSubscriberType())
        {
            bean = ctx.get(CONTRIBUTOR_SUBSCRIBER_KEY);
        }
        else
        {
            bean = ctx.get(RECIPIENT_SUBSCRIBER_KEY);
        }

        Context subCtx = ctx.createSubContext();
        subCtx.put(Subscriber.class, bean);

        return super.getValueAdvanced(subCtx);
    }
}
