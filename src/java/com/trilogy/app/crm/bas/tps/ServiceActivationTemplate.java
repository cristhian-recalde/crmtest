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
package com.trilogy.app.crm.bas.tps;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.core.SubscriptionClass;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class ServiceActivationTemplate extends AbstractServiceActivationTemplate
{
    /**
     * {@inheritDoc}
     */
    public SubscriptionClass getSubscriptionClass(Context ctx)
    {
        return SubscriptionClass.getSubscriptionClass(ctx, getSubscriptionClass());
    }

    
    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return context_;
    }


    /**
     * {@inheritDoc}
     */
    public void setContext(final Context context)
    {
        context_ = context;
    }


    /**
     * The operating context.
     */
    private transient Context context_;
}
