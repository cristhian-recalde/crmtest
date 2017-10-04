/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.filter.AdjustmentTypeByAuth;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

public class AdjustmentTypeCheckPermissionProxyWebControl extends
        ProxyWebControl
{

    public AdjustmentTypeCheckPermissionProxyWebControl(
            final WebControl delegate)
    {
        super(delegate);
    }

    public Context wrapContext(Context ctx)
    {

        if (ctx.getInt("MODE", CREATE_MODE) != DISPLAY_MODE)
        {
            Context subCtx = ctx.createSubContext();
            final Home adjustmentHome = (Home) ctx
                    .get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_READ_ONLY_HOME);

            Home whereHome = adjustmentHome.where(ctx,
                    new AdjustmentTypeByAuth(subCtx, false));

            subCtx.put(AdjustmentTypeHome.class, whereHome);
            return subCtx;
        }

        return ctx;

    }

}
