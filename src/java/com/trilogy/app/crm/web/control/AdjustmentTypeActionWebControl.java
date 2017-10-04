/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.xenum.EnumCollection;

import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.xhome.CustomEnumCollection;

/**
 * @author vcheng
 */
public class AdjustmentTypeActionWebControl extends PrimitiveWebControl
{
    private WebControl webControl_ = null;

    public AdjustmentTypeActionWebControl()
    {
        super();
    }


    /*
     * (non-Javadoc)
     *
     * @see com.redknee.framework.xhome.webcontrol.InputWebControl#fromWeb(com.redknee.framework.xhome.context.Context,
     *      javax.servlet.ServletRequest, java.lang.String)
     */
    public Object fromWeb(final Context ctx, final ServletRequest req, final String name) throws NullPointerException
    {
        if (webControl_ == null)
        {
            return null;
        }

        return webControl_.fromWeb(ctx, req, name);
    }


    /*
     * (non-Javadoc)
     *
     * @see com.redknee.framework.xhome.webcontrol.OutputWebControl#toWeb(com.redknee.framework.xhome.context.Context,
     *      java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final SysFeatureCfg sysCfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        final int oldDisplayMode = ctx.getInt("MODE");

        if (sysCfg.getAllowEitherAction())
        {
            webControl_ = new EnumWebControl(AdjustmentTypeActionEnum.COLLECTION, false);
        }
        else
        {
        	final AdjustmentTypeActionEnum action = (AdjustmentTypeActionEnum) obj;
        	//EITHER actions are not allowed to edit so putting it as readonly
        	if (action == AdjustmentTypeActionEnum.EITHER)
        	{
        		ctx.put("MODE", Integer.valueOf(DISPLAY_MODE));
        	}
            final EnumCollection newEnum = new CustomEnumCollection(
                    AdjustmentTypeActionEnum.CREDIT,
                    AdjustmentTypeActionEnum.DEBIT);

            webControl_ = new EnumWebControl(newEnum, false);
        }

        webControl_.toWeb(ctx, out, name, obj);
        ctx.put("MODE", Integer.valueOf(oldDisplayMode));
    }

}