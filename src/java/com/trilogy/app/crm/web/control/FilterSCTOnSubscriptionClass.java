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

package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author daniel.lee@redknee.com
 *
 */

public class FilterSCTOnSubscriptionClass
    extends ProxyWebControl
{
    public FilterSCTOnSubscriptionClass(final WebControl delegate)
    {
        super(delegate);
    }

    public Context wrapContext(final Context ctx)
    {
        final Subscriber bean = (Subscriber) ctx.get(AbstractWebControl.BEAN);
        Context subCtx = ctx.createSubContext();

        final Object homeKey = getHomeKey(ctx);
        final Home home = (Home) subCtx.get(homeKey);

        final Home alteredHome = home.where(ctx, new EQ(ServiceActivationTemplateXInfo.SUBSCRIPTION_CLASS, bean.getSubscriptionClass()));
        subCtx.put(homeKey, alteredHome);

        return subCtx;
    }

    private Object getHomeKey(final Context ctx)
    {
        boolean found = false;
        Object ret = null;
        WebControl delegate = this;

        while(!found && null != delegate && (delegate instanceof ProxyWebControl))
        {
            delegate = ((ProxyWebControl)delegate).getDelegate();
            if(delegate instanceof AbstractKeyWebControl)
            {
                found = true;
            }
        }

        if(found)
        {
            ret = ((AbstractKeyWebControl)delegate).getHomeKey();
        }
        else
        {
            LogSupport.major(ctx, this, "System Error: This webcontrol must be used with a KeyWebControl in it's delegate list.");
        }

        return ret;
    }
}