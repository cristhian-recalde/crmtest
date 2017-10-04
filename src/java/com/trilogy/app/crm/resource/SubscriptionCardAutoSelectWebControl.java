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

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.util.snippet.log.Logger;

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * 
 *
 * @author victor.stratan@redknee.com
 */
public class SubscriptionCardAutoSelectWebControl extends ProxyWebControl
{

    /**
     * @param delegate
     */
    public SubscriptionCardAutoSelectWebControl(WebControl delegate)
    {
        super(delegate);
    }

    public void fromWeb(Context ctx, Object bean, ServletRequest p2, String p3)
    {
        super.fromWeb(ctx, bean, p2, p3);
        final Subscriber sub = (Subscriber) bean;
        setCardPackage(ctx, sub);
    }

    public Object fromWeb(Context ctx, ServletRequest p1, String p2)
    {
        final Subscriber sub = (Subscriber) super.fromWeb(ctx, p1, p2);
        setCardPackage(ctx, sub);
        return sub;
    }

    protected void setCardPackage(final Context ctx, final Subscriber sub)
    {
        if (AbstractSubscriber.DEFAULT_PACKAGEID.equals(sub.getPackageId()))
        {
            final String resourceID = sub.getResourceID(ctx);
            if (resourceID != null && !resourceID.equals(AbstractSubscriber.DEFAULT_RESOURCEID))
            {
                final EQ where = new EQ(ResourceDeviceDefaultPackageXInfo.RESOURCE_ID, resourceID);
                try
                {
                    final ResourceDeviceDefaultPackage link;
                    link = HomeSupportHelper.get(ctx).findBean(ctx, ResourceDeviceDefaultPackage.class, where);
                    if (link != null)
                    {
                        sub.setPackageId(link.getPackID());
                    }
                }
                catch (HomeException e)
                {
                    Logger.minor(ctx, this, "Unable to retrieve ResourceDeviceDefaultPackage for ResourceDevice ["
                            + resourceID + "]", e);
                }
            }
        }
    }
}
