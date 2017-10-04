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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.DefaultDetailRenderer;
import com.trilogy.framework.xhome.web.renderer.DetailRenderer;
import com.trilogy.framework.xhome.web.support.WebSupport;
import com.trilogy.framework.xhome.webcontrol.AbstractTableWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountWebControl;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.config.AccountRequiredField;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * This web control only renders account fields that are configured for registration.
 * 
 * It also performs fromWeb on the given account applying the changes available.  If no
 * account is provided to the method, then it will check the database and the context
 * for the existing account to use as a starting point.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class AccountRegistrationWebControl implements WebControl
{
    public static final String ACCOUNT_REGISTRATION_TITLE = "Account.RegistrationTitle";

    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        Account account = null;
            
        String ban = req.getParameter(name + OutputWebControl.SEPERATOR + AccountXInfo.BAN.getName());
        if (ban != null)
        {
            try
            {
                // Load the account from the database prior to applying updates from the GUI
                account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class, ban);
            }
            catch (HomeException e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Error retrieving account with BAN " + ban + " from home.", e).log(ctx);
                }
            }
            
            if (account == null)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Account with BAN " + ban + " not found in home.  Checking context...", null).log(ctx);
                }
                account = BeanLoaderSupportHelper.get(ctx).getBean(ctx, Account.class);
                if (account != null
                        && !ban.equals(account.getBAN()))
                {
                    account = null;
                }
            }
            
            if (account != null)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Account with BAN " + ban + " found!", null).log(ctx);
                }
                fromWeb(ctx, account, req, name);
            }
            else
            {
                new MinorLogMsg(this, "Unable to update registration information due to failure to load account with BAN " + ban, null).log(ctx);
                throw new NullPointerException("Account " + ban + " not found!");
            }
        }
        else
        {
            throw new NullPointerException("Null BAN Value");
        }
        
        return account;
    }

    public void fromWeb(Context ctx, Object bean, ServletRequest req, String name)
    {
        new AccountWebControl().fromWeb(ctx, bean, req, name);
    }

    public void toWeb(Context parentCtx, PrintWriter out, String name, Object bean)
    {
        Context ctx = parentCtx.createSubContext();
        ctx.put("MODE", OutputWebControl.EDIT_MODE);
        
        Context secureCtx = ctx.createSubContext();
        secureCtx.put("MODE", OutputWebControl.DISPLAY_MODE);

        MessageMgr mmgr = new MessageMgr(ctx, this);

        DetailRenderer renderer = (DetailRenderer) ctx.get(DetailRenderer.class, DefaultDetailRenderer.instance());

        renderer.Table(ctx,
                out,
                AccountXInfo.instance().getLabel(ctx)
                + " - " + mmgr.get(ACCOUNT_REGISTRATION_TITLE, "Registration"));

        if (!(bean instanceof Account))
        {
            bean = BeanLoaderSupportHelper.get(ctx).getBean(ctx, Account.class);
        }
         
        if (bean instanceof Account)
        {
            Account account = (Account) bean;
            
            // Some nested WebControls may want to know about the whole bean rather
            // than just their property so we put it in the Context for them
            ctx.put(AbstractWebControl.BEAN, account);
            ctx.put(Account.class, account);

            // Not in table mode so set the TABLE_MODE to false.  Used by individual web controls
            ctx.put("TABLE_MODE", false);

            // persistent hidden field for primary key
            out.println("<input type=\"hidden\" name=\"" + name + OutputWebControl.SEPERATOR + AccountXInfo.BAN.getName() +"\" value=\""+ String.valueOf(AccountXInfo.BAN.get(account)).replaceAll("\"", "&quot;")+"\" />");
            
            Map<String, AccountRequiredField> fields = account.getRegistrationFields(ctx);
            for (PropertyInfo property : (List<PropertyInfo>) AccountXInfo.PROPERTIES)
            {
                if (property != null
                        && (AccountXInfo.BAN.equals(property)
                                || fields.containsKey(property.getName())))
                {
                    String propertyName = property.getName();
                    
                    Context subCtx = ctx.createSubContext();
                    if (AccountXInfo.BAN.equals(property))
                    {
                        subCtx.put("MODE", OutputWebControl.DISPLAY_MODE);
                    }

                    // Some nested WebControls may want to know about the particular
                    // bean property they are dealing with.
                    subCtx.put(AbstractWebControl.PROPERTY, property);
                    
                    if (AccountXInfo.IDENTIFICATION_GROUP_LIST.equals(property))
                    {
                        subCtx.put(AbstractWebControl.NUM_OF_BLANKS, 0);
                        subCtx.put(AbstractTableWebControl.HIDE_CHECKBOX, Boolean.TRUE);
                        subCtx.put(AbstractTableWebControl.DISABLE_NEW, Boolean.TRUE);
                    }

                    ViewModeEnum mode = AbstractWebControl.getMode(subCtx, 
                            property.getXInfo().getName() + "." + propertyName);

                    if ( mode != ViewModeEnum.NONE )
                    {
                        String label = property.getLabel(subCtx);
                        String postLabel = property.getPostLabel(subCtx);
                        String shortHelp = property.getShortHelp(subCtx);
                        WebControl wc = (WebControl) property.getInstanceOf(subCtx, WebControl.class);

                        renderer.TR(ctx,
                                out,
                                WebSupport.labelize(subCtx, name + OutputWebControl.SEPERATOR + propertyName,
                                        label));

                        out.print("<font TITLE=\"");
                        out.print(shortHelp);
                        out.print("\">");
                        if (wc != null)
                        {
                            wc.toWeb( ( mode == ViewModeEnum.READ_ONLY ) ? secureCtx : subCtx, out, name + OutputWebControl.SEPERATOR + propertyName, property.get(account));
                        }
                        out.print("</font>");

                        if (postLabel != null && postLabel.trim().length() > 0)
                        {
                            out.print("&nbsp;<b><font color=\"#003366\">");
                            out.print(postLabel);
                            out.print("</font></b>");
                        }

                        renderer.TREnd(ctx, out);
                    }
                }
            }
        }

        renderer.TableEnd(ctx, out);
    }
}
