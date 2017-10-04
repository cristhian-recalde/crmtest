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
package com.trilogy.app.crm.resource;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.util.snippet.log.Logger;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.ResourceDevice;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;

/**
 * 
 *
 * @author victor.stratan@redknee.com
 */
public class SubscriberResourceDealerCodeUpdateDecorator extends HomeProxy
{
    /**
     * For serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param delegate
     */
    public SubscriberResourceDealerCodeUpdateDecorator(final Home delegate)
    {
        super(delegate);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber sub = (Subscriber) super.create(ctx, obj);

        final String resourceID = sub.getResourceID(ctx);
        if (resourceID != null && resourceID.length() > 0)
        {
            updateResourceDealerCode(ctx, sub.getDealerCode(), resourceID);
        }

        return sub;
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber sub = (Subscriber) super.store(ctx, obj);
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        final String resourceID = sub.getResourceID(ctx);
        if (resourceID != null && resourceID.length() > 0
                && !sub.getDealerCode().equals(oldSub.getDealerCode()))
        {
            updateResourceDealerCode(ctx, sub.getDealerCode(), resourceID);
        }

        return sub;
    }

    /**
     * @param ctx
     * @param sub
     * @param resourceID
     */
    private void updateResourceDealerCode(final Context ctx, final String subDealerCode, final String resourceID)
    {
        final ResourceDevice resource = ResourceDevice.getResourceDevice(ctx, resourceID);
        if (!resource.getDealerCode().equals(subDealerCode))
        {
            resource.setDealerCode(subDealerCode);
            try
            {
                HomeSupportHelper.get(ctx).storeBean(ctx, resource);
            }
            catch (HomeException e)
            {
                final String msg = "Unable to update Dealer Code for Resource Device ["
                    + resourceID + "]. " + e.getMessage();
                Logger.major(ctx, this, msg, e);
                final HomeException ex = new HomeException(msg, e);
                FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, ex);
            }
        }
    }

}
