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
package com.trilogy.app.crm.home.sub;

import java.util.Collection;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.bean.DiscountClassHome;
import com.trilogy.app.crm.bean.DiscountClassXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;

/**
 * Validates the presence of proper Discount class is used
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 */
public final class SubscriberDiscountClassValidator extends AbstractSubscriberValidator
{
    private static final SubscriberDiscountClassValidator INSTANCE = new SubscriberDiscountClassValidator();

    /**
     * Prevents initialization
     */
    private SubscriberDiscountClassValidator()
    {
    }

    public static SubscriberDiscountClassValidator instance()
    {
        return INSTANCE;
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Subscriber sub = (Subscriber) obj;     
        
        if (sub.getDiscountClass() != 0 )
        {
            Home home = (Home) ctx.get(DiscountClassHome.class);
            final Object condition = new EQ(DiscountClassXInfo.ID, Integer.valueOf(sub.getDiscountClass()));

            Collection collection = null;
            try
            {
                collection = home.select(ctx,condition);
            }
            catch (Exception e)
            {

            }
            if (collection == null || collection.size() != 1 )
            {
                final CompoundIllegalStateException el = new CompoundIllegalStateException();
                el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.DISCOUNT_CLASS ,
                        "Invalid Discountclass " + sub.getDiscountClass() + " type."));
                el.throwAll();
            }
        }
    }

}
