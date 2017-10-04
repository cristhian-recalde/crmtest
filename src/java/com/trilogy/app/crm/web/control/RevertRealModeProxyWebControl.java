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

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.CommonFramework;

/**
 *
 * @author victor.stratan@redknee.com
 */
public class RevertRealModeProxyWebControl extends ProxyWebControl
{
    /**
     * Constructor with delegate.
     * @param delegate delegate passed to ProxyWebControl constructor
     */
    public RevertRealModeProxyWebControl(final WebControl delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    public void toWeb(final Context ctx, final PrintWriter p1, final String p2, final Object p3)
    {
        if (ctx.has(CommonFramework.REAL_MODE))
        {
            final int mode = ctx.getInt(CommonFramework.REAL_MODE);
            if (ctx.getInt(DisplaySubscriberServicesWebControl.MODE) != mode)
            {
                final Context subCtx = ctx.createSubContext();
                subCtx.put(DisplaySubscriberServicesWebControl.MODE, mode);
                super.toWeb(subCtx, p1, p2, p3);
                return;
            }
        }
        super.toWeb(ctx, p1, p2, p3);
    }
}
