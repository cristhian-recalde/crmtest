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

package com.trilogy.app.crm.web.acctmenu;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.trilogy.framework.auth.AuthMgr;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.io.StringPrintWriter;
import com.trilogy.framework.xhome.menu.XMenu;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.agent.AgentRequestServicer;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.web.lastviewed.LastViewedWebAgent;
import com.trilogy.framework.xhome.web.renderer.DetailRenderer;
import com.trilogy.framework.xhome.web.renderer.MenuPaneDetailRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.web.xmenu.MenuPane;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;

/**
 * A Border for displaying Account and Subscriber menus vertically on the left
 * of the main screen.
 */
public class AccountMenuBorder extends Session implements Border
{

    public static final String LAST_VIEWED_PERMISSION = "app.crm.lastviewed";

    public static final String PATH_PERMISSION = "app.crm.path";

    static void outputDisabledMenu(PrintWriter out, XMenu menu)
    {
        // This should be moved into the RK style sheet
        out.print("<font size=\"2\" face=\"verdana\" color=\"#aaaaaa\">");
        out.print(menu.getLabel().replaceAll(" ", "&nbsp;"));
        out.print("</font>");
    }

    protected final RequestServicer accountMenu_;

    protected final RequestServicer subMenu_;
    protected final RequestServicer accountSubSearchMenu_;

    protected final RequestServicer path_;

    public AccountMenuBorder(Context ctx)
    {
        accountSubSearchMenu_ = new AgentRequestServicer(new MenuPane(ctx, "AcctSubSearchMenu"));

        accountMenu_ = new AgentRequestServicer(new MenuPane(ctx, "SubMenuAccountMgm")
        {
            // Disable all links except "Browse" if no Account is defined
            // I hate to hard-code the menu key into the code like this but the
            // XMenu
            // bean doesn't provide any means for me to define the requirement
            @Override
            public void outputLink(Context ctx, XMenu menu, Link link,
                    PrintWriter out)
            {
                if (menu.getKey().equals("SubMenuAccountEdit"))
                {
                    super.outputLink(ctx, menu, link, out);
                }
                else if(menu.getKey().equals("SubMenuSubAccountEdit"))
                {
                    Account account = (Account) getSession(ctx).get(Account.class);
                    if(null == account || account.isIndividual(ctx))
                    {
                        outputDisabledMenu(out, menu);
                    }
                    else
                    {
                        super.outputLink(ctx, menu, link, out);
                    }
                }
                else if(menu.getKey().endsWith("appCrmLocateMsisdn"))
                {
                    super.outputLink(ctx, menu, link, out);
                }
                else if (menu.getKey().equals("SubMenuAccountHistory"))
                {
                    Account account = (Account) getSession(ctx).get(Account.class);

                    if ((account == null) || account.isPrepaid() || !account.isResponsible())
                    {
                        outputDisabledMenu(out, menu);
                    }
                    else
                    {
                        super.outputLink(ctx, menu, link, out);
                    }
                }
                else if (menu.getKey().equals("appCRMConvertAccount"))
                {
                    Account account = (Account) getSession(ctx).get(Account.class);
                    
                    if ((account==null) || (!validAccountForConversion(ctx, account)))
                    {
                        outputDisabledMenu(out, menu);
                    }
                    else
                    {
                        super.outputLink(ctx,menu,link,out);
                    }
                }
                else if(getSession(ctx).has(Account.class))
                {
                    super.outputLink(ctx, menu, link, out);
                }
                else
                {
                    outputDisabledMenu(out, menu);
                }
            }
        });

        subMenu_ = new AgentRequestServicer(new MenuPane(ctx, "SubMenuSubscriberMgm")
        {
            @Override
            public void outputLink(Context ctx, XMenu menu, Link link,
                    PrintWriter out)
            {
                if (menu.getKey().equals("SubMenuSubProfileEdit"))
                {
                    Account account = (Account) getSession(ctx).get(Account.class);
                    if (account == null || !account.isIndividual(ctx))
                    {
                        outputDisabledMenu(out, menu);
                    }
                    else
                    {
                        super.outputLink(ctx, menu, link, out);
                    }
                }
                else
                {
                    if (getSession(ctx).has(Subscriber.class))
                    {
                        super.outputLink(ctx, menu, link, out);
                    }
                    else
                    {
                        outputDisabledMenu(out, menu);
                    }
                }
            }
        });

        path_ = new AccountPathBorder();
    }

    /**
     * checks if the Convert Billing Type option should be disabled or not
     * @param ctx
     * @param account
     * @return
     */
    private boolean validAccountForConversion(Context ctx, Account account)
    {
        boolean result = false;

        if (account != null)
        {
            AccountStateEnum state = account.getState();
			if (state != null)
            {
                result = ((account.getState().equals(AccountStateEnum.ACTIVE))
				        && (!account.getSystemType().equals(
				            SubscriberTypeEnum.HYBRID)) && account
				        .isIndividual(ctx));
            }
        }
        return result;
    }

    ///////////////////////////////////////////// impl Border

    @Override
    public void service(Context ctx, final HttpServletRequest req,
            HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        if(ctx.has(DISPLAYED))
        {
            delegate.service(ctx, req, res);
            return;
        }

        PrintWriter out = res.getWriter();
        DetailRenderer r = new MenuPaneDetailRenderer();
        final PrintWriter pw = new StringPrintWriter();
        final Context subCtx = ctx.createSubContext();

        try
        {
            WebAgents.setWriter(subCtx, pw);

            HttpServletResponse res2 = new HttpServletResponseWrapper(res)
            {
                @Override
                public PrintWriter getWriter()
                {
                    return pw;
                }
            };

            // Map "" (default) and "AcctSubSubMenus" (parent menu) to the real
            // "SubMenuAccountEdit" Browse menu "cmd"
            HttpServletRequest req2 = new HttpServletRequestWrapper(req)
            {
                @Override
                public String getParameter(String key)
                {
                    String ret = super.getParameter(key);

                    if ("cmd".equals(key))
                    {
                        if ("".equals(ret) || "AcctSubSubMenus".equals(ret))
                        {
                            WebAgents.setParameter(subCtx, "cmd",
                                    "SubMenuAccountEdit");
                            return "SubMenuAccountEdit";
                        }
                    }

                    return ret;
                }
            };

            subCtx.put(HttpServletResponse.class, res2);
            subCtx.put(HttpServletRequest.class, req2);
            subCtx.put(DISPLAYED, DISPLAYED);

            delegate.service(subCtx, req2, res2);
            subCtx.put(HttpServletResponse.class, res);

            out.println("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
            // we need more then the 10% but this will force the column to be only as wide as the text
            out.println("<tr><td width=\"10%\" valign=\"top\">");
            accountSubSearchMenu_.service(subCtx, req2, res);
            accountMenu_.service(subCtx, req2, res);
            out.println("<br/></br/>");

            subMenu_.service(ctx, req, res);
            out.println("<br/></br/>");

            if (new AuthMgr(ctx).check(LAST_VIEWED_PERMISSION))
            {
                try
                {
                    new AgentRequestServicer(LastViewedWebAgent.instance()).service(ctx, req2, res);
                }
                catch (ServletException e)
                {
                }
            }

            out.println("</td><td width=\"18\"></td><td width=\"1\" bgcolor=\"gray\"></td>");
            out.println("<td width=\"18\"></td>");
            out.println("</td><td align=\"center\" valign=\"top\">");
            if (new AuthMgr(ctx).check(PATH_PERMISSION))
            {
                path_.service(ServletBridge.createSubContext(ctx, req2, res), req, res);
            }
            out.println("<center>");
            out.println(pw);
            out.println("</center>");
            out.println("</td></tr></table>");
        }
        catch (Throwable t)
        {
            // TODO: make a ThrowableWebControl or something similiar
            // to standardize the way that we print out exceptions
            t.printStackTrace(out);
        }
    }

    private static final String DISPLAYED = "DISPLAYED";
}