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
package com.trilogy.app.crm.web.agent;

import java.io.PrintWriter;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.web.control.AccountTopologyTreeWebControl;

/**
 * WebAgent for displaying Account usage information.
 *
 * @author victor.stratan@redknee.com
 */
public class AccountTopologyWebAgent extends WebAgents implements WebAgent
{
    protected WebControl wc_ = new AccountTopologyTreeWebControl();

    public AccountTopologyWebAgent()
    {
    }

    public void execute(final Context ctx) throws AgentException
    {
        final PrintWriter out = WebAgents.getWriter(ctx);
        final Account account = (Account) ctx.get(Account.class);
        int limit = SystemSupport.getAccountTopologyViewLimit(ctx);

        if (account == null)
        {
            out.println("<font color=red>No Account selected!</font>");
            return;
        }

        final Context subCtx = ctx.createSubContext();

        Home home = (Home) ctx.get(SubscriberHome.class);
        home.where(ctx, new Limit(limit + 1));

        subCtx.put(SubscriberHome.class, home);

        home = (Home) ctx.get(AccountHome.class);
        home.where(ctx, new Limit(limit + 1));

        subCtx.put(AccountHome.class, home);

        out.println("<table><tr><td>");

        List list = null;
        try
        {
            // this call should use the subscontext
            list = AccountSupport.getTopology(subCtx, account);

            if (list.size() > limit)
            {
                out.println("<font color=red>Account too large for topology view.</font>");
            }
            else
            {
                wc_.toWeb(subCtx, WebAgents.getWriter(ctx), "", list);
            }
        }
        catch (HomeException e)
        {
            out.println("<font color=red>Error on Account topology retreival.</font>");
            LogSupport.debug(ctx, this, "", e);
        }

        out.println("</td></tr></table>");
    }
}