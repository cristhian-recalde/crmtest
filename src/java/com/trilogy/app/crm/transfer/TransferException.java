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
package com.trilogy.app.crm.transfer;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.core.SubscriptionType;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class TransferException extends AbstractTransferException
{

    /**
     * {@inheritDoc}
     */
    public SubscriptionType getSubscriptionType(Context ctx)
    {
        return SubscriptionType.getSubscriptionType(ctx, getSubscriptionType());
    }


    /**
     * {@inheritDoc}
     */
    public long getIdentifier()
    {
        return getId();
    }


    /**
     * {@inheritDoc}
     */
    public void setIdentifier(long value)
    {
        setId(value);
    }

}
