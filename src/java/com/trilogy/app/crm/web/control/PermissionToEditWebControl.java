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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved. 
 *
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.security.Permission;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.support.AuthSupport;


/**
 * Provides a web control decorator that causes the delegate control to appear
 * in DISPLAY mode rather than EDIT mode if the principle in the context does
 * not have a given permission.
 *
 * @author gary.anderson@redknee.com
 */
public
class PermissionToEditWebControl
    extends ProxyWebControl
{
    /**
     * Creates a new PermissionToEditWebControl for the given delegate web
     * control and permission.
     *
     * @param delegate The web control to which we delegate.
     * @param permission The permission required to for the control to be
     * editable.
     *
     * @exception IllegalArgumentException Thrown if the permission is null.
     */
    public PermissionToEditWebControl(
        final WebControl delegate,
        final Permission permission)
    {
        super(delegate);

        if (permission == null)
        {
            throw new IllegalArgumentException(
                "The permission is required to be non-null.");
        }
        
        permission_ = permission;
    }


    // INHERIT
    @Override
    public void toWeb(
        final Context ctx,
        final PrintWriter out,
        final String name,
        final Object obj)
    {
        final int mode = ctx.getInt("MODE", DISPLAY_MODE);

        final Context subContext = ctx.createSubContext();
        
        if (mode == EDIT_MODE
                && !AuthSupport.hasPermission(ctx, permission_))
        {
            subContext.put("MODE", DISPLAY_MODE);
        }

        super.toWeb(subContext, out, name, obj);
    }
    
        
    /**
     * The permissions required to make the control editable in edit mode.
     */
    private final Permission permission_;
    
    
} // class
