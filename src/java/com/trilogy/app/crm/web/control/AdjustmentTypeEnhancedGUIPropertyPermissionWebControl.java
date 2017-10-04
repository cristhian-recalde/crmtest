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
import java.util.Iterator;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIPermissionEnum;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIProperty;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.entity.EntityInfo;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.support.WebSupport;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.xenum.Enum;

/**
 * Web control for Adjustment Type Enhanced GUI permission.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class AdjustmentTypeEnhancedGUIPropertyPermissionWebControl extends
        EnumWebControl
{

    public AdjustmentTypeEnhancedGUIPropertyPermissionWebControl()
    {
        super(AdjustmentTypeEnhancedGUIPermissionEnum.COLLECTION, false);
    }

    /**
     * Enable/disable changing permission based on flag in bean.
     * 
     * @see com.redknee.framework.xhome.webcontrol.ProxyWebControl#getDelegate(com.redknee.framework.xhome.context.Context)
     */
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        AdjustmentTypeEnhancedGUIProperty bean = (AdjustmentTypeEnhancedGUIProperty) ctx
                .get(AbstractWebControl.BEAN);

        Number number = (Number) obj;
        Enum localEnum = this.getEnumCollection(ctx).get((short) 0);
        for (Iterator it = this.getEnumCollection(ctx).iterator(); it.hasNext();)
        {
            localEnum = (Enum) it.next();
            if (localEnum.getIndex() == number.shortValue())
            {
                break;
            }
        }

        int mode = ctx.getInt("MODE", DISPLAY_MODE);

        switch (mode)
        {
        case EDIT_MODE:
        case CREATE_MODE:
            out.print("<select id=\"");
            out.print(WebSupport.fieldToId(ctx, name));
            out.print("\" name=\"");
            out.print(name);
            out.print("\" size=\"");
            out.print(String.valueOf(getSize()));
            out.print("\"");

            /*
             * [Cindy Wong] 2010-02-17: disable the web control if it's not editable.
             */
            if (!bean.isPermissionEditable())
            {
                out.print(" disabled=\"disabled\"");
            }

            /*
             * calls the script on change
             */
            out.print(" onChange=\"permissionUpdate(event)\"");
            
            out.println(">");

            for (Iterator i = getEnumCollection(ctx).iterator(); i.hasNext();)
            {
                com.redknee.framework.xhome.xenum.Enum e = (com.redknee.framework.xhome.xenum.Enum) i
                        .next();

                out.print("<option value=\"");
                out.print(String.valueOf(e.getIndex()));
                out.print("\"");
                if (e.equals(localEnum))
                {
                    out.print(" selected=\"selected\"");
                }
                out.print(">");
                out.print(getDescription(ctx, e));
                out.println("</option>");
            }
            out.println("</select>");
            break;

        case DISPLAY_MODE:
        default:
            if (localEnum != null)
            {
                out.print(localEnum.getDescription(ctx));
            }
        }
    }

    public Object fromWeb(Context ctx, ServletRequest req, String name)
            throws NullPointerException
    {
        Enum e = (Enum) super.fromWeb(ctx, req, name);
        return new Short(e.getIndex());
    }
}
