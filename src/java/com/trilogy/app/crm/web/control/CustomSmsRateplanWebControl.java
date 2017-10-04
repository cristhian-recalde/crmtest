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

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.smsb.ProvisionedRatePlansKeyWebControl;

/** Customized Web control for rendering SMS Rateplans used during new PriceplanVersion creation.
 * Depends upon a flag viz "Enable SMS RatePlan" in System Feature Configuration.
 * True --  Use a Drop down menu
 * False -- Use a text box.
 * @author amit.baid@redknee.com
 */

public class CustomSmsRateplanWebControl extends ProxyWebControl
{
    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.ProxyWebControl#toWeb
     * (com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final SysFeatureCfg sysCfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        final boolean enableDropDown = sysCfg.getEnableSMSRatePlan();
        final ProvisionedRatePlansKeyWebControl dropDown = new ProvisionedRatePlansKeyWebControl(1 , true ,
                                        new KeyWebControlOptionalValue("--" , Long.valueOf(0L)));
        final  PreviewLongWebControl textBox = new PreviewLongWebControl(textFieldSize_);
        if (enableDropDown)
        {
            setDelegate(dropDown);
        }
        else
        {
            setDelegate(textBox);
        }
        super.toWeb(ctx, out, name, obj);
    }
    /* @Override
     * (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.ProxyWebControl#fromWeb
     * (com.redknee.framework.xhome.context.Context, java.lang.Object, javax.servlet.ServletRequest, java.lang.String)
     */
    public void fromWeb(final Context ctx, final Object obj, final ServletRequest req, final String name)
    {
        super.fromWeb(ctx, obj, req, name);
    }
    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.ProxyWebControl#getDelegate
     * (com.redknee.framework.xhome.context.Context)
     */
    public WebControl getDelegate(final Context ctx)
    {
        return super.getDelegate(ctx);
    }
    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.ProxyWebControl#setDelegate
     * (com.redknee.framework.xhome.webcontrol.WebControl)
     */
    public WebControl setDelegate(final WebControl delegate)
    {
       return super.setDelegate(delegate);
    }
    /**
     * textFieldSize_ : size of the Text box.
     */
    private short textFieldSize_ = 20;
}
