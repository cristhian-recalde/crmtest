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
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroupXInfo;

/**
 * Web control used to write identification groups to the web. It writes the description,
 * and also the id as a hidden field.
 * 
 * @author marcio.marques@redknee.com
 *
 */
public class IdentificationGroupDescriptionWebControl extends AbstractWebControl
{
    private WebControl idWebControl;
    private WebControl descWebControl;
    private String idName;
    private String descName;
    
    public IdentificationGroupDescriptionWebControl()
    {
        idWebControl = new HiddenLongWebControl(16);
        descWebControl = new TextFieldWebControl(30);
        idName = "idGroup";
        descName = "group";
    }

    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        AccountIdentificationGroup aig = (AccountIdentificationGroup) ctx.get(AbstractWebControl.BEAN);
        if (aig!=null)
        {
            idWebControl.toWeb(ctx, out, name + idName, Integer.valueOf(aig.getIdGroup()));
        }
        
        ViewModeEnum fieldMode = getMode(ctx, AccountIdentificationGroupXInfo.GROUP);
        if (ctx.getInt("MODE", DISPLAY_MODE) == DISPLAY_MODE
                || fieldMode == ViewModeEnum.NONE
                || fieldMode == ViewModeEnum.READ_ONLY)
        {
            out.println("<input type=\"hidden\" name=\"" + name + descName +"\" value=\""+String.valueOf(obj).replaceAll("\"", "&quot;")+"\" />");
        }
        descWebControl.toWeb(ctx, out, name + descName, obj);
    }
    
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        AccountIdentificationGroup aig = (AccountIdentificationGroup) ctx.get(AbstractWebControl.BEAN);
        if (aig!=null)
        {
            Object id = idWebControl.fromWeb(ctx, req, name + idName);
            aig.setIdGroup(((Number) id).intValue());
        }
        Object desc = descWebControl.fromWeb(ctx, req, name + descName);
        return desc;

    }


    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
        AccountIdentificationGroup aig = (AccountIdentificationGroup) ctx.get(AbstractWebControl.BEAN);
        if (aig!=null)
        {
            Object id = idWebControl.fromWeb(ctx, req, name + idName);
            aig.setIdGroup(((Number) id).intValue());
        }
        descWebControl.fromWeb(ctx, obj, req, name + descName);
    }
}
