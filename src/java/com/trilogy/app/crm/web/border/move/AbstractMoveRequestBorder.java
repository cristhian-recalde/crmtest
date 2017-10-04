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
package com.trilogy.app.crm.web.border.move;

import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.move.MoveRequest;


/**
 * Abstract class providing common functionality to move request borders.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public abstract class AbstractMoveRequestBorder implements Border
{
    protected void setViewModes(Context ctx, MoveRequest request, Map<String, ViewModeEnum> viewModeMap)
    {
        if (request != null
                && viewModeMap != null
                && viewModeMap.size() > 0)
        {
            PMLogMsg pm = new PMLogMsg(AbstractMoveRequestBorder.class.getName(), "setViewModes()");
            try
            {
                XInfo xinfo = (XInfo) XBeans.getInstanceOf(ctx, request, XInfo.class);
                if (xinfo != null)
                {
                    List properties = xinfo.getProperties(ctx);
                    for(Object propertyObj : properties)
                    {
                        if (propertyObj instanceof PropertyInfo)
                        {
                            PropertyInfo property = (PropertyInfo)propertyObj;
                            ViewModeEnum viewMode = viewModeMap.get(property.getName());
                            if (viewMode != null)
                            {
                                String modeKey = property.getXInfo().getName() 
                                + "." + property.getName()
                                + ".mode";
                                ctx.put(modeKey, viewMode);
                            }
                        }
                    }
                }
            }
            finally
            {
                pm.log(ctx);
            }
        }
    }
}
