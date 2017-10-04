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

package com.trilogy.app.crm.auth;

import com.trilogy.framework.auth.permission.PermissionInfo;
import com.trilogy.framework.xhome.auth.bean.PermissionRow;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Adapts PermissionRow to PermissionInfo and vice versa.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class PermissionRowToPermissionInfoAdapter implements Adapter
{

    private static final long serialVersionUID = 1L;
    private static final PermissionRowToPermissionInfoAdapter instance_ = new PermissionRowToPermissionInfoAdapter();

    public static PermissionRowToPermissionInfoAdapter instance()
    {
        return instance_;
    }

    private PermissionRowToPermissionInfoAdapter()
    {
        // empty
    }

    /**
     * @see com.redknee.framework.xhome.home.Adapter#adapt(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object adapt(Context ctx, Object obj)
    {
        PermissionRow row = (PermissionRow) obj;
	PermissionInfo info = new PermissionInfo();
        info.setName(row.getPermission());
        return info;
    }

    /**
     * @see com.redknee.framework.xhome.home.Adapter#unAdapt(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object unAdapt(Context ctx, Object obj)
    {
        String key = obj.toString();
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Key = " + key);
        }
        PermissionRow row = new PermissionRow();
        row.setPermission(key);
        return row;
    }
}
