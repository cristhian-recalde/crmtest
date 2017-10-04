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
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bundle.BundleFee;
import com.trilogy.app.crm.bundle.web.EnhancedBundleFeeTableWebControl;


/**
 * The web control to ensure the correct fee for an unselected bundle. This is needed
 * because the key web control automatically selects the first available bundle (since it
 * is not optional), but the fee are not appropriately set by default.
 *
 * @author cindy.wong@redknee.com
 * @since 2008-07-29
 */
public class BundleFeeFeeWebControl extends ProxyWebControl
{

    /**
     * Create a new instance of <code>BundleFeeFeeWebControl</code>.
     *
     * @param delegate
     *            Delegate of this web control.
     */
    public BundleFeeFeeWebControl(final WebControl delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        // find the bean
        BundleFee fee = (BundleFee) ctx.get(AbstractWebControl.BEAN, null);
        Long actualFee = (Long) obj;
        if (fee == null || fee.getId() == BundleFee.DEFAULT_ID)
        {
            // look up the correct bundle
            fee = (BundleFee) ctx.get(EnhancedBundleFeeTableWebControl.FIRST_AUX_BUNDLE_KEY);
            if (fee != null)
            {
                actualFee = Long.valueOf(fee.getFee());
            }
        }
        super.toWeb(ctx, out, name, actualFee);
    }
}