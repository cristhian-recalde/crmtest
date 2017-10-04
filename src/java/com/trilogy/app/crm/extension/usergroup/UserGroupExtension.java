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
package com.trilogy.app.crm.extension.usergroup;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.app.crm.support.UserGroupSupport;


/**
 * 
 *
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class UserGroupExtension extends AbstractUserGroupExtension
{

    /**
     * {@inheritDoc}
     */
    public String getSummary(Context ctx)
    {
        return "UserGroup=" + this.getGroupName();
    }

    /**
     * @param ctx The operating context
     * @return This extension's User Group bean
     */
    public CRMGroup getGroup(Context ctx)
    {        
	CRMGroup group = null;
        try
        {
            group = UserGroupSupport.getCRMGroup(ctx, this.getGroupName());
        }
        catch (HomeException e)
        {
        } 
        
        if( group != null && SafetyUtil.safeEquals(group.getName(), this.getGroupName()) )
        {
            return group;
        }
        
        return null;
    }
}
