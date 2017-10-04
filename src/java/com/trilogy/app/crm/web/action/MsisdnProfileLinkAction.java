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
package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.ui.Msisdn;

/**
 * @author jke
 */
public class MsisdnProfileLinkAction extends SimpleWebAction
{

    public MsisdnProfileLinkAction()
    {
        super("ownerProfile", "Profile");
    }


    /*
     * public MsisdnProfileLinkAction(Permission permission) { this();
     * setPermission(permission); }
     */
    public MsisdnProfileLinkAction(String key, String label)
    {
        super(key, label);
    }

/*
    public void execute(Context ctx) throws AgentException
    {
        //  String strAction = WebAgents.getParameter(ctx, "action");
        //  String key = WebAgents.getParameter(ctx, "key");
        try
        {
            System.out.println("MsisdnProfileLinkAction - execute");
            final Home msisdn_home = (Home) ctx.get(MsisdnHome.class);
            if (msisdn_home == null)
            {
                throw new HomeException("Could not find MsisdnHome in context.");
            }
            WebAgents.setParameter(ctx, "key", null);
            Link link = new Link(ctx);
            link.add("cmd", WebAgents.getParameter(ctx, "cmd"));
            //  link.addRaw("key", msisdn.getSubscriberID());
            link.addRaw("cmd", "SubMenuSubProfileEdit");
            WebAgents.service(ctx.createSubContext(), link.write(), WebAgents.getWriter(ctx));
        }
        catch (Exception x)
        {
            throw new AgentException(x);
        }
    }
*/

    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        final Msisdn msisdn = (Msisdn) bean;

        if (msisdn == null)
        {
            return;
        }

        final String ban = msisdn.getBAN();
        if (ban != null && ban.length() > 0)
        {
            link.remove("key");
            //link.remove("query");
            link.remove(".search.limit");
            link.remove(".search.msisdn");
            link.remove("SearchCMD.x");

            // populate subscriber msisdn
            try
            {
                final Home home = (Home) ctx.get(AccountHome.class);
                final Account account = (Account) home.find(ctx, ban);
                if (account != null)
                {
                    // http://hostname:9260/AppCrm/home?cmd=SubMenuAccountEdit&key=134
                    link.addRaw("cmd", "SubMenuAccountEdit");
                    link.addRaw("action", "display");
                    link.addRaw("key", account.getBAN());
                    link.writeLink(out, getLabel());
                }
            }
            catch (HomeException hEx)
            {
                LogSupport.minor(ctx, this, "Unable to retreive Account \"" + ban + "\"", hEx);
            }
        }
    }
}
