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
package com.trilogy.app.crm.web.control;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.extension.account.AbstractAccountExtension;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.AccountExtensionHolder;
import com.trilogy.app.crm.extension.account.AccountExtensionSupport;
import com.trilogy.app.crm.bean.Account;


/**
 * This extension will apply fromWeb() to the extension that is currently stored in the DB.
 *
 * @author Aaron Gourley
 * @since 7.4.16
 */
public class AccountExtensionReadOnlySupportedWebControl extends ProxyWebControl
{
    public AccountExtensionReadOnlySupportedWebControl(WebControl delegate)
    {
        super(delegate);
    }


    @Override
    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
        if( obj instanceof AccountExtension )
        {
            AccountExtension ext = (AccountExtension)obj;
            XInfo xinfo = (XInfo)XBeans.getInstanceOf(ctx, ext.getClass(), XInfo.class);
            PropertyInfo id = xinfo.getID();
            if( id != null && String.class.getName().equals(id.getType().getName()) )
            {
                // Get the BAN
                String ban = ext.getBAN();
                if( AbstractAccountExtension.DEFAULT_BAN.equals(ban) )
                {
                    Object beanObj = ctx.get(AbstractWebControl.BEAN);
                    Context curCtx = ctx;
                    while( curCtx != null && beanObj instanceof AccountExtensionHolder )
                    {
                        curCtx = ((Context)curCtx.get(".."));
                        beanObj = curCtx.get(AbstractWebControl.BEAN);
                    }
                    if( beanObj instanceof Account )
                    {
                        ban = ((Account)beanObj).getBAN();
                    }
                }
                
                // Get the extension for the given BAN
                if( !AbstractAccountExtension.DEFAULT_BAN.equals(ban) )
                {
                    Home extHome = AccountExtensionSupport.getExtensionHome(ctx, ext);
                    try
                    {
                        AccountExtension dbExtension = (AccountExtension)extHome.find(ctx, ban);
                        if( dbExtension != null )
                        {
                            // Use the DB extension as the starting point for fromWeb() to be applied to
                            XBeans.copy(ctx, dbExtension, obj);
                        }   
                    }
                    catch (HomeException e)
                    {
                        new DebugLogMsg(this, e.getClass().getSimpleName() + " occurred in " + AccountExtensionReadOnlySupportedWebControl.class.getSimpleName() + ".fromWeb(): " + e.getMessage(), e).log(ctx);
                    }
                }
            }
        }
        super.fromWeb(ctx, obj, req, name);
    }

}
