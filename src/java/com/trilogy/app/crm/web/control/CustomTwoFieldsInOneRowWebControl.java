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

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.NullWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * @author victor.stratan@redknee.com
 */
public class CustomTwoFieldsInOneRowWebControl extends ProxyWebControl
{
    protected PropertyInfo propInfo_;
    protected WebControl second_;

    public CustomTwoFieldsInOneRowWebControl(final WebControl delegate,
            final PropertyInfo propInfo)
    {
        super(delegate);
        propInfo_ = propInfo;
    }

    public CustomTwoFieldsInOneRowWebControl(final WebControl delegate,
            final PropertyInfo propInfo, final WebControl second)
    {
        super(delegate);
        propInfo_ = propInfo;
        second_ = second;
    }

    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String p2, final Object p3)
    {
        super.toWeb(ctx, out, p2, p3);

        if (ctx.getBoolean("TABLE_MODE", true))
        {
            // do not do anything if in TABLE_MODE. Assume the worst.
            return;
        }

        // label
        out.print("<th valign=\"center\" align=\"right\">");
        out.print(propInfo_.getLabel(ctx));
        out.print("</th><td>");

        if (second_ == null)
        {
            second_ = (WebControl) propInfo_.getInstanceOf(ctx, WebControl.class);
        }
        if (second_ == null)
        {
            second_ = NullWebControl.instance();
        }

        final Object bean = ctx.get(AbstractWebControl.BEAN);
        //final String name = propInfo_.getXInfo().getName();
        // you would think that the above code is the right way, unfortunately...
        final String name = "";

        // second control
        second_.toWeb(ctx, out, name + SEPERATOR + propInfo_.getName(), propInfo_.get(bean));
    }
}
