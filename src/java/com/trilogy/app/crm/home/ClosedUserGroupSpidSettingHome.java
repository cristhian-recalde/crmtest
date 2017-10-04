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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Sets the CUG spid based on the selected CUG Template.
 * @author marcio.marques@redknee.com
 */
public class ClosedUserGroupSpidSettingHome extends HomeProxy
{
    
    public ClosedUserGroupSpidSettingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }
    
    public Object create(Context ctx, Object obj) throws HomeException
    {
        FrameworkSupportHelper.get(ctx).initExceptionListener(ctx, this);
        setCugSpid(ctx, (ClosedUserGroup) obj);
        Object cug = (ClosedUserGroup) super.create(ctx,obj);
        FrameworkSupportHelper.get(ctx).printCapturedExceptionsAsWarnings(ctx, this);
        return cug;
    }
    

    public Object store(Context ctx, Object obj) throws HomeException
    {
        FrameworkSupportHelper.get(ctx).initExceptionListener(ctx, this);
        setCugSpid(ctx, (ClosedUserGroup) obj);
        Object cug = (ClosedUserGroup) super.store(ctx,obj);
        FrameworkSupportHelper.get(ctx).printCapturedExceptionsAsWarnings(ctx, this);
        return cug;
    }
    
    private void setCugSpid(Context ctx, final ClosedUserGroup cug) throws HomeException
    {
            if (cug.getCugTemplateID()>0)
            {
                ClosedUserGroupTemplate cugTemplate = ClosedUserGroupSupport.getCugTemplate(ctx, cug.getCugTemplateID());
                cug.setSpid(cugTemplate.getSpid());
            }
      
    }

}
