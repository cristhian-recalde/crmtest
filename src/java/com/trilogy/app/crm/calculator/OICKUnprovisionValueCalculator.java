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
package com.trilogy.app.crm.calculator;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;


/**
 * Value Calculator to retrieve subscriber's OICK unprovisioning command.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public class OICKUnprovisionValueCalculator extends OICKProvisionValueCalculator
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAdvanced(Context ctx)
    {
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        if (sub != null)
        {
            return sub.getUnprovisionOICK(ctx);
        }
        return null;
    }
}
