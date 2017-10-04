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
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.Lookup;

/**
 * 
 *
 * @author victor.stratan@redknee.com
 */
public class SubscriberResourceDeviceSaveHome extends HomeProxy
{
    /**
     * For serialization. Although Serializing HomeProxies is a bad idea. 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor accepts delegate Home parameter.
     *
     * @param home delegate Home
     */
    public SubscriberResourceDeviceSaveHome(final Home delegate)
    {
        super(delegate);
    }

    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber request = (Subscriber) obj;
        final Subscriber result = (Subscriber) super.create(ctx, obj);
        if (request.getResourceID(ctx) != null && request.getResourceID(ctx).length() > 0)
        {
            updateResourceDeviceSubscription(ctx, request.getResourceID(ctx), result.getId());
        }
        return result;
    }

    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber request = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final Subscriber result = (Subscriber) super.store(ctx, obj);
        if (request.getResourceID(ctx) != null && request.getResourceID(ctx).length() > 0)
        {
            if (!request.getResourceID(ctx).equals(oldSub.getResourceID(ctx)))
            {
                updateResourceDeviceSubscription(ctx, request.getResourceID(ctx), result.getId());
                if (oldSub.getResourceID(ctx) != null && oldSub.getResourceID(ctx).length() > 0)
                {
                    releaseResourceDevice(ctx, oldSub.getResourceID(ctx));
                }
            }
        }
        else
        {
            if (oldSub.getResourceID(ctx) != null && oldSub.getResourceID(ctx).length() > 0)
            {
                releaseResourceDevice(ctx, oldSub.getResourceID(ctx));
            }
        }
        return result;
    }

    protected void updateResourceDeviceSubscription(final Context ctx,
            final String resourceID, final String subscriptionID) throws HomeException
    {
        final Home home = (Home) ctx.get(ResourceDeviceHome.class);

        final EQ condition = new EQ(ResourceDeviceXInfo.RESOURCE_ID, resourceID);
        final ResourceDevice resource = (ResourceDevice) home.find(ctx, condition);

        resource.setSubscriptionID(subscriptionID);
        resource.setState(ResourceDeviceStateEnum.IN_USE_INDEX);

        home.store(ctx, resource);
    }

    protected void releaseResourceDevice(final Context ctx,
            final String resourceID) throws HomeException
    {
        final Home home = (Home) ctx.get(ResourceDeviceHome.class);

        final EQ condition = new EQ(ResourceDeviceXInfo.RESOURCE_ID, resourceID);
        final ResourceDevice resource = (ResourceDevice) home.find(ctx, condition);

        resource.setSubscriptionID("");
        resource.setState(ResourceDeviceStateEnum.RETURNED_INDEX);

        home.store(ctx, resource);
    }
}
