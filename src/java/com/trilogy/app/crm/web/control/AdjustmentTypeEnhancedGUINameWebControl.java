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

import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIProperty;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;

/**
 * Displays the adjustment type name in a tree-like view in the enhanced GUI.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class AdjustmentTypeEnhancedGUINameWebControl extends
        TextFieldWebControl
{
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        AdjustmentTypeEnhancedGUIProperty property = (AdjustmentTypeEnhancedGUIProperty) ctx
                .get(AbstractWebControl.BEAN);
        for (int j = 0; j < property.getLevel(); j++)
        {
            out.print("&nbsp;&nbsp;&nbsp;&nbsp;");
        }

        MessageMgr mmgr = new MessageMgr(ctx, property.getClass());
        if (property.isCategory())
        {
            out.println(mmgr.get("TreeFolder",
                    "<img src=\"/images/openFolder.gif\" alt=\"--\" />"));
        }
        else
        {
            out
                    .println(mmgr
                            .get("ChildNode",
                                    "<img src=\"/images/org/javalobby/icons/16x16/Document.gif\" alt=\"--\" />"));
        }

        String str = obj == null ? "" : obj.toString();
        out.print(str);
    }
}
