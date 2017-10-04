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
package com.trilogy.app.crm.bulkloader.generic;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;

/**
 * Set the Display mode for the PropertyBulkloadActionEnum web control depending on the selection
 * of the ProperyInfo.
 * 
 * If the PropertyInfo is a Collection, then allow the "Additional Instructions" to be editable. 
 * @author angie.li@redknee.com
 *
 */
public class PropertyBulkloadActionEnumWebControl extends EnumWebControl 
{
    private static final PropertyBulkloadActionEnum DEFAULT_PROPERTY_BULKLOAD_ACTION_ENUM = PropertyBulkloadActionEnum.NONE;

    PropertyBulkloadActionEnumWebControl()
    {
        super(PropertyBulkloadActionEnum.COLLECTION);
    }
    
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Context subCtx = ctx.createSubContext();
        PropertyBulkloadActionEnum action  = (PropertyBulkloadActionEnum) obj;
        
        BulkloadPropertyInfo bean = (BulkloadPropertyInfo)subCtx.get(AbstractWebControl.BEAN);

        if (bean.getPropertyInfo() == null ||
                (bean.getPropertyInfo() != null && 
                !java.util.Collection.class.isAssignableFrom(bean.getPropertyInfo().getType()) ) )
        {
            //Display editable field only for Collections
            subCtx.put("MODE", DISPLAY_MODE);
            // If it is not a Collection then place Blank as the PropertyBulkloadActionEnum selection to display
            action = DEFAULT_PROPERTY_BULKLOAD_ACTION_ENUM;
        }
        super.toWeb(subCtx, out, name, action);
    }

    public Object fromWeb(Context ctx, ServletRequest req, String name)
        throws NullPointerException
    {
        Object ret = super.fromWeb(ctx, req, name);
        if (ret == null)
        {
            //The Web Control is read only and it could not resolve the default value
            ret = DEFAULT_PROPERTY_BULKLOAD_ACTION_ENUM;
        }
        return ret;
    }
}
