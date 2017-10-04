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
package com.trilogy.app.crm.filter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.web.xmenu.service.XMenuService;

import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.SysFeatureCfgXInfo;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * @author ltang
 * 
 *         Predicate returns TRUE when Account Manager Dropdown is enabled.
 */
public class AccountManagerEnabledPredicate implements Predicate, PropertyChangeListener
{

    private static Context ctx_ = null;
    private static AccountManagerEnabledPredicate instance_ = null;


    /**
     * Provides access to a singleton instance of this class.
     * 
     * @return A singleton instance of this class.
     */
    public static AccountManagerEnabledPredicate getInstance(Context ctx)
    {
        if (instance_ == null)
        {
            instance_ = new AccountManagerEnabledPredicate();
            ctx_ = ctx;
            SysFeatureCfg config = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
            config.addPropertyChangeListener(instance_);
        }

        return instance_;
    }


    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        return SystemSupport.isAccountManagerDropDownEnabled(ctx);
    }


    public void propertyChange(PropertyChangeEvent evt)
    {
        Object newValue = evt.getNewValue();

        // If a the 'Enable Account Manager Dropdown' configuration has changed, clear the xmenu cache
        // to force a rebuild
        if (SysFeatureCfgXInfo.ENABLE_ACCOUNT_MANAGER_DROPDOWN.getName().equals(evt.getPropertyName()))
        {
            ((XMenuService) ctx_.get(XMenuService.class)).invalidate();
        }
    }
}
