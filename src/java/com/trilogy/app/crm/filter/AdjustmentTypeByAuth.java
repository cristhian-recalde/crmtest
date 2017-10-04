/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import java.security.Principal;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author arturo.medina@redknee.com
 * 
 */
public class AdjustmentTypeByAuth implements Predicate
{
    /**
     * explicitly adding a serialVersionUID
     */
    private static final long serialVersionUID = 10L;

    /**
     * @param ctx
     * @param allowNoGroup
     */
    public AdjustmentTypeByAuth(Context ctx, boolean allowNoGroup)
    {
        User user = (User) ctx.get(Principal.class);
        if (user != null)
        {
            LogSupport.debug(ctx, this, "User not null ");
            groupName_ = user.getGroup();
        }
        else
        {
            LogSupport.debug(ctx, this, "User null ");
        }

        this.allowNoGroup_ = allowNoGroup;
    }

    /**
     * (@inheritDoc)
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        AdjustmentType adj = (AdjustmentType) obj;
        boolean fallsInPermissions = false;

        Home groupHome = (Home) ctx.get(CRMGroupHome.class);

        if (groupHome != null)
        {
            if (allowNoGroup_ == true && groupName_ == null)
            {
                fallsInPermissions = true;
            }
            else
            {
                /*
                 * [Cindy Wong] 2010-01-29: Add recursive check.
                 */
                for (;;)
                {
                    fallsInPermissions = AuthSupport.checkPermission(groupHome,
                            groupName_, new SimplePermission(adj
                                    .getPermission()));
                    if (fallsInPermissions)
                    {
                        break;
                    }
                    AdjustmentType parent = adj.getParent(ctx);
                    if (parent == null)
                    {
                        break;
                    }
                    adj = parent;
                }
            }
        }

        return fallsInPermissions;
    }

    protected String groupName_ = null;

    protected boolean allowNoGroup_ = false;
}
