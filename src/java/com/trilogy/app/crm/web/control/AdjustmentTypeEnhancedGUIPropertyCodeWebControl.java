/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.beans.facets.java.lang.IntegerWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * @author cindy.wong@redknee.com
 * 
 */
public class AdjustmentTypeEnhancedGUIPropertyCodeWebControl extends
        ProxyWebControl
{

    public AdjustmentTypeEnhancedGUIPropertyCodeWebControl()
    {
        super(new IntegerWebControl());
        readOnlyWebControl_ = new ReadOnlyWebControl(getDelegate());
    }

    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        getReadOnlyWebControl().toWeb(ctx, out, name, obj);
        out.print("<input type=\"hidden\" name=\"");
        out.print(name);
        out.print("\" value=\"");
        out.print(obj == null ? "" : obj.toString());
        out.print("\" />");
    }

    public WebControl getReadOnlyWebControl()
    {
        return readOnlyWebControl_;
    }

    protected WebControl readOnlyWebControl_;
}
