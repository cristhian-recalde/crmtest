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
package com.trilogy.app.crm.extension.account;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;


/**
 * Base class for application specific account extensions.
 *
 * @author Aaron Gourley
 * @since 
 */
public class AccountExtension extends AbstractAccountExtension
{
    /**
     * @{inheritdoc}
     */
    public String getName(Context ctx)
    {
        return AccountExtensionSupport.getExtensionName(ctx, this.getClass());
    }

    /**
     * @{inheritdoc}
     */
    public String getDescription(Context ctx)
    {
        return AccountExtensionSupport.getExtensionDescription(ctx, this.getClass());
    }
    
    public ExtensionAware getParentBean(Context ctx)
    {
        return (ExtensionAware) ctx.get(AccountExtensionSupport.ACCOUNT_EXTENSION_PARENT_BEAN_CTX_KEY);
    }
    
    /**
     * @{@inheritDoc}
     */
    public String getSummary(Context ctx)
    {
        return "BAN=" + this.getBAN();
    }

    /**
     * @param ctx The operating context
     * @return This extension's account
     */
    public Account getAccount(Context ctx)
    {
        Account account = BeanLoaderSupportHelper.get(ctx).getBean(ctx, Account.class);
        if( account != null 
                && (AbstractAccountExtension.DEFAULT_BAN.equals(this.getBAN())
                        || SafetyUtil.safeEquals(account.getBAN(), this.getBAN())) )
        {
            return account;
        }

        if( AbstractAccountExtension.DEFAULT_BAN.equals(this.getBAN()) )
        {
            return null;
        }
        
        try
        {
            account = AccountSupport.getAccount(ctx, this.getBAN());
        }
        catch (HomeException e)
        {
        } 
        
        if( account != null && SafetyUtil.safeEquals(account.getBAN(), this.getBAN()) )
        {
            return account;
        }
        
        return null;
    }
}
