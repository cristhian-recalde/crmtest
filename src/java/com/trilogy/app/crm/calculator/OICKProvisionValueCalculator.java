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

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;


/**
 * Value Calculator to retrieve subscriber's OICK provisioning command.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public class OICKProvisionValueCalculator extends AbstractValueCalculator implements XCloneable
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Object> getDependentContextKeys(Context ctx)
    {
        Collection<Object> result = new ArrayList<Object>();
        result.add(Subscriber.class);
        return result;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Object getValueAdvanced(Context ctx)
    {
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        if (sub != null)
        {
            return sub.getProvisionOICK(ctx);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean transientEquals(Object o)
    {
        return equals(o);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean persistentEquals(Object o)
    {
        return equals(o);
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
