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
package com.trilogy.app.crm.bundle;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Filter the BundleProfileHome to select only loyalty point bundles.
 *
 * @author victor.stratan@redknee.com
 */
public class PointBundlesProxyWebControl extends ProxyWebControl
{
    public PointBundlesProxyWebControl(final WebControl delegate)
    {
        super(delegate);
    }

    @Override
    public Context wrapContext(final Context parentCtx)
    {
        if (LogSupport.isDebugEnabled(parentCtx))
        {
            LogSupport.debug(parentCtx, this, "Filtering for loyalty point bundles", null);
        }

        Context ctx = parentCtx.createSubContext();
        
        Home pointBundleHome = SubscriberBundleSupport.filterPointBundles(ctx);
        if (pointBundleHome == null)
        {
            LogSupport.major(ctx, this, "Unable to filter the Point Bundles.  Check preceding log messages for errors.", null);
            
            // Throw an exception here to prevent the screen from loading properly.  Subsequent updates can corrupt points bundle data.
            throw new NullPointerException("Unable to filter the Point Bundles.  Check logs for errors.");
        }

        ctx.put(BundleProfileHome.class, pointBundleHome);

        // disable actions
        ActionMgr.disableActions(ctx);
        
        AbstractWebControl.setMode(ctx, BundleFeeXInfo.SERVICE_PREFERENCE, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, BundleFeeXInfo.FEE, ViewModeEnum.NONE);
        //AbstractWebControl.setMode(ctx, BundleFeeXInfo.SERVICE_PERIOD, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, BundleFeeXInfo.START_DATE, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, BundleFeeXInfo.END_DATE, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, BundleFeeXInfo.PAYMENT_NUM, ViewModeEnum.NONE);

        return ctx;
    }
}
