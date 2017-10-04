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
 */package com.trilogy.app.crm.web.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionXInfo;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.XTestIgnoreWebControl;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Action used to remove ppsm supportee extensions. This action
 * redirects back to the previews menu, which makes it usable from the
 * subscriber advanced features screen.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class PPSMSupporteeRemovalAction extends SimpleWebAction
{
    public PPSMSupporteeRemovalAction()
    {
        super("removeSupportee", "Remove");
    }

    public PPSMSupporteeRemovalAction(String key, String label)
    {
        super(key, label);
    }

    public Link modifyLink(Context ctx, Object bean, Link link)
    {   
        PPSMSupporteeSubExtension extension = (PPSMSupporteeSubExtension) bean;
        Link result =  new Link(link);
        final String originalCmd = (String) link.getMap().get("cmd");
        final String newCmd = "AppCrmSubscriberPPSMSupporteeExtensions";
        result.addRaw("action", "removeSupportee");
        result.addRaw("cmd", newCmd);
        result.addRaw("CMD", "removeSupportee");
        Subscriber supporter = (Subscriber) ctx.get(Subscriber.class);
        if (originalCmd!=newCmd && supporter!=null && supporter.getMSISDN().equals(extension.getSupportMSISDN()))
        {
            result.add("supporterID", supporter.getId());
            result.add("originalCmd", originalCmd);
        }
        return result;
    }
    
    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
       link = modifyLink(ctx, bean, link);
       PPSMSupporteeSubExtension extension = (PPSMSupporteeSubExtension) bean;
       out.print("<a href=\"");
       link.write(out);
       out.print("\" onclick=\"return confirm('Remove MSISDN ");
       out.print(extension.getMSISDN());
       out.print(" from list of supported subscriptions?');\">");
       out.print(getLabel());
       out.print("</a>");
    }
   

    public void execute(Context ctx) throws AgentException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "PPSM Supportee Removal Action INVOKED.");
        }
        String key = WebAgents.getParameter(ctx, "key");
        PrintWriter out = WebAgents.getWriter(ctx);
        Home h = (Home) ctx.get(PPSMSupporteeSubExtensionHome.class);
        String ogirinalCmd = WebAgents.getParameter(ctx, "originalCmd");
        String supporterID = WebAgents.getParameter(ctx, "supporterID");
        try
        {
            PPSMSupporteeSubExtension bean = (PPSMSupporteeSubExtension) h.find(new EQ(PPSMSupporteeSubExtensionXInfo.SUB_ID, key));
            if (bean!=null)
            {
                h.remove(ctx, bean);
                if (ogirinalCmd!=null && !ogirinalCmd.isEmpty())
                {
                    String message = "Successfully removed subscription with MSISDN='" + bean.getMSISDN() + "' from supported subscriptions list.";
                    message = "<table width=\"70%\"><tr><td><center><b>" + message + "</b></center></td></tr></table>";
                    out.println(message);
                }
                else
                {
                    out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
                    out.println("<center><b>Entry: ''" + String.valueOf(key) + "'' deleted!</b></center>");
                    out.println(XTestIgnoreWebControl.IGNORE_END);
                }
            }

            Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
            if (subscriber!=null)
            {
                subscriber.setSubExtensions(null);
            }

        }
        catch (HomeException e)
        {
            throw new AgentException(e);
        }
        

        if (ogirinalCmd!=null && !ogirinalCmd.isEmpty())
        {
            Link link = new Link(ctx);
            link.remove("action");
            link.remove("key");
            link.add("cmd", ogirinalCmd);
            link.add("key", supporterID);

            WebAgents.setParameter(ctx, "key", null);
            WebAgents.setParameter(ctx, "action", null);

            try
            {
                WebAgents.service(ctx, link.write(), WebAgents.getWriter(ctx));
            }
            catch (ServletException ex)
            {
                throw new AgentException("Fail to redirect to " + ogirinalCmd, ex);
            }
            catch (IOException ioEx)
            {
                throw new AgentException("Fail to redirect to " + ogirinalCmd, ioEx);
            }            
        }
        else
        {
            returnToSummaryView(ctx);
        }
    } 
}
