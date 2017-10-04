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

import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.account.AccountManagerKeyWebControl;
import com.trilogy.app.crm.bean.account.AccountTypeSelectionEnum;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * Custom web control to display a textbox field if the Account Manager drop down feature
 * is disabled. Else, display a drop down if it is enabled.
 * 
 * @author ltang
 * 
 */
public class CustomAccountManagerKeyWebControl extends AccountManagerKeyWebControl
{

    public static final KeyWebControlOptionalValue DEFAULT = new KeyWebControlOptionalValue("--", "");


    public CustomAccountManagerKeyWebControl()
    {
    }


    public CustomAccountManagerKeyWebControl(boolean autoPreview)
    {
        super(autoPreview);
    }


    public CustomAccountManagerKeyWebControl(int listSize)
    {
        super(listSize);
    }


    public CustomAccountManagerKeyWebControl(int listSize, boolean autoPreview)
    {
        super(listSize, autoPreview);
    }


    public CustomAccountManagerKeyWebControl(int listSize, boolean autoPreview, boolean isOptional)
    {
        super(listSize, autoPreview, isOptional);
    }


    public CustomAccountManagerKeyWebControl(int listSize, boolean autoPreview, boolean isOptional, boolean allowCustom)
    {
        super(listSize, autoPreview, isOptional, allowCustom);
    }


    public CustomAccountManagerKeyWebControl(int listSize, boolean autoPreview, Object optionalValue)
    {
        super(listSize, autoPreview, optionalValue);
    }


    public CustomAccountManagerKeyWebControl(int listSize, boolean autoPreview, Object optionalValue,
            boolean allowCustom)
    {
        super(listSize, autoPreview, optionalValue, allowCustom);
    }


    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        if (isFieldEnabled(ctx))
        {
            if (!SystemSupport.isAccountManagerDropDownEnabled(ctx))
            {
                new TextFieldWebControl().toWeb(ctx, out, name, obj);
            }
            else
            {
                Context subCtx = ctx.createSubContext();
                if (!AuthSupport.hasPermission(subCtx, new SimplePermission("app.crm.accountmgr.edit")))
                {
                    subCtx.put("MODE", DISPLAY_MODE);
                }

                super.toWeb(subCtx, out, name, obj);
            }
        }
    }


    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        if (!SystemSupport.isAccountManagerDropDownEnabled(ctx))
        {
            Object bean = ctx.get(AbstractWebControl.BEAN);
            if (bean instanceof Account)
            {
                Account account = (Account) bean;
                if (account.isRootAccount())
                {
                    return new TextFieldWebControl().fromWeb(ctx, req, name);
                }
            }
            else
            {
                return new TextFieldWebControl().fromWeb(ctx, req, name);
            }
        }
        return super.fromWeb(ctx, req, name);
    }


    /**
     * Check whether the Account Manager field should be visible/editable on the Account
     * profile
     * 
     * @param ctx
     * @return
     */
    public static boolean isFieldEnabled(Context ctx)
    {
        SysFeatureCfg sysCfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        boolean isFieldEnabled = true;

        Object bean = ctx.get(AbstractWebControl.BEAN);
        if (bean instanceof Account)
        {
            Account account = (Account) bean;
            if (sysCfg.isEnableAccountManagerDropdown())
            {
                if (AccountTypeSelectionEnum.ROOT.equals(sysCfg.getAccountManagerSelection()))
                {
                    isFieldEnabled = account.isRootAccount();
                }
                else if (AccountTypeSelectionEnum.RESPONSIBLE.equals(sysCfg.getAccountManagerSelection()))
                {
                    isFieldEnabled = account.getResponsible();
                }
            }
            else if (!account.isRootAccount())
            {
                isFieldEnabled=false;
            }
        }
        return isFieldEnabled;
    }

}
