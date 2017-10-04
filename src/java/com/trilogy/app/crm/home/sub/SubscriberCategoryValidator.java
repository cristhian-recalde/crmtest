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
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;

import com.trilogy.app.crm.bean.SubscriberCategory;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCategoryXInfo;
import com.trilogy.app.crm.bean.SubscriberXInfo;

/**
 * Validates the presence of proper proper subscriber category
 *
 * @author kumaran.sivasubramaniam@redknee.com
 */
public final class SubscriberCategoryValidator extends AbstractSubscriberValidator
{
    private static final SubscriberCategoryValidator INSTANCE = new SubscriberCategoryValidator();

    /**
     * Prevents initialization
     */
    private SubscriberCategoryValidator()
    {
    }

    public static SubscriberCategoryValidator instance()
    {
        return INSTANCE;
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Subscriber sub = (Subscriber) obj;     
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        
        if ( sub.getSubscriberCategory() != 0 )
        {
            Home home = (Home) ctx.get(com.redknee.app.crm.bean.SubscriberCategoryHome.class);
            SubscriberCategory subCat = null;
            try
            {
                subCat = (SubscriberCategory) home.find(ctx, sub.getSubscriberCategory());
            }
            catch (Exception e)
            {
		    
            }
            if (subCat == null)
            {
                el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.SUBSCRIBER_CATEGORY,
							       "Cannot find Subscriber category id: " + sub.getSubscriberCategory()));
                el.throwAll();
            }
            else if (subCat.getSpid() != sub.getSpid())
            {
                el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.SUBSCRIBER_CATEGORY, 
							       "Subscriber category code with wrong spid: " + subCat.getSpid()));
                el.throwAll();
            }
        
        }
    }

}
