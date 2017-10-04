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
import com.trilogy.framework.xhome.elang.And;
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
public class SubscriptionResourceAutoSelectWebControl extends ProxyWebControl
{

    /**
     * @param delegate
     */
    public SubscriptionResourceAutoSelectWebControl(WebControl delegate)
    {
        super(delegate);
    }

    public void fromWeb(Context ctx, Object bean, ServletRequest p2, String p3)
    {
        super.fromWeb(ctx, bean, p2, p3);
        final Subscriber sub = (Subscriber) bean;
        setResourceDevice(ctx, sub);
    }

    public Object fromWeb(Context ctx, ServletRequest p1, String p2)
    {
        final Subscriber sub = (Subscriber) super.fromWeb(ctx, p1, p2);
        setResourceDevice(ctx, sub);
        return sub;
    }

    protected void setResourceDevice(final Context ctx, final Subscriber sub)
    {
        // this prevents NullPointerException when there are other issues causing sub to be null
        // not having this check will cause this NPE to mask the root cause
        if (sub == null)
        {
            return;
        }

        if (sub.getResourceID(ctx) == null || sub.getResourceID(ctx).length() == 0)
        {
            final String cardPackageID = sub.getPackageId();
            if (cardPackageID != null && !cardPackageID.equals(AbstractSubscriber.DEFAULT_PACKAGEID))
            {
                final And where = new And();
                where.add(new EQ(ResourceDeviceDefaultPackageXInfo.PACK_ID, cardPackageID));
                where.add(new EQ(ResourceDeviceDefaultPackageXInfo.PACK_TECHNOLOGY,
                        Integer.valueOf(sub.getTechnology().getIndex())));
                try
                {
                    final ResourceDeviceDefaultPackage link;
                    link = HomeSupportHelper.get(ctx).findBean(ctx, ResourceDeviceDefaultPackage.class, where);
                    if (link != null)
                    {
                        sub.setResourceID(link.getResourceID());
                    }
                }
                catch (HomeException e)
                {
                    Logger.minor(ctx, this, "Unable to retrieve ResourceDeviceDefaultPackage for CardPackage ["
                            + cardPackageID + "]", e);
                }
            }
        }
    }
}
