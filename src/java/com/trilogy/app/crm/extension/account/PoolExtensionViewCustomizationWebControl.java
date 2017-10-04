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
package com.trilogy.app.crm.extension.account;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.WebControlSupportHelper;

/**
 * @author victor.stratan@redknee.com
 */
public class PoolExtensionViewCustomizationWebControl extends ProxyWebControl
{
    public PoolExtensionViewCustomizationWebControl(final WebControl delegate)
    {
        super(delegate);
    }

    @Override
    public void toWeb(final Context ctx, final PrintWriter p1, final String p2, final Object p3)
    {
        final Context subContext;

        if (!LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.RK_DEV_LICENSE))
        {
            subContext = ctx.createSubContext();
            WebControlSupportHelper.get(ctx).hideProperty(ctx, SubscriptionPoolPropertyXInfo.PROVISIONED);
        }
        else
        {
            subContext = ctx;
        }

        super.toWeb(subContext, p1, p2, p3);
    }
}
