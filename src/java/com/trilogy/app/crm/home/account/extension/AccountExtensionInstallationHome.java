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
package com.trilogy.app.crm.home.account.extension;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.extension.ExtensionInstallationHome;
import com.trilogy.app.crm.extension.InstallableExtension;
import com.trilogy.app.crm.extension.MovableExtension;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class AccountExtensionInstallationHome extends ExtensionInstallationHome
{

    public AccountExtensionInstallationHome(Home delegate)
    {
        super(delegate);
    }

    public AccountExtensionInstallationHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        boolean processed = false;

        if (obj instanceof MovableExtension
                && ctx.getBoolean(MovableExtension.MOVE_IN_PROGRESS_CTX_KEY, false))
        {
            // Skip extension's normal install/update/uninstall logic.  Rely on move logic to call move() for this extension.
            processed = true;
        }
        else if( obj instanceof InstallableExtension )
        {   
            InstallableExtension extension = (InstallableExtension)obj;
            Account account = null;
            Account oldAccount = null;
            if( extension instanceof AccountExtension )
            {
                account = ((AccountExtension)extension).getAccount(ctx);
                Home accountHome = (Home)ctx.get(AccountHome.class);
                oldAccount = (Account)accountHome.find(ctx, account.getBAN());
                if( account != null && oldAccount != null )
                {
                    try
                    {
                        if( EnumStateSupportHelper.get(ctx).isLeavingState(oldAccount, account, AccountStateEnum.INACTIVE) )
                        {
                            // Install the extension when account is leaving deactivated state
                            extension.install(ctx);
                            processed = true;
                        }
                        else if( EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, account, AccountStateEnum.INACTIVE) )
                        {
                            // Uninstall the extension upon account deactivation
                            extension.uninstall(ctx);
                            processed = true;
                        }
                    }
                    catch (ExtensionInstallationException e)
                    {
                        if( e.wasExtensionUpdated() )
                        {
                            HomeException he = new HomeException("Partial failure updating " + extension.getName(ctx) + " extension. " + e.getMessage(), e);
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                        }
                        else
                        {
                            HomeException he =  new HomeException("Failed to update " + extension.getName(ctx) + " extension. " + e.getMessage(), e); 
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                        }
                    }
                }
            }
        }

        Object resultBean = null;
        
        if( !processed )
        {
            // Run the default extension installation home
            resultBean = super.store(ctx, obj);
        }
        else
        {
            // Skip the default extension installation home
            resultBean = getDelegate().store(ctx, obj);
        }
        
        return resultBean;
    }
}
