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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;

public class AttachFileWebAction
    extends SimpleWebAction
{
    public AttachFileWebAction()
    {
        super("attachFile", "Attachments");
    }

    public void execute(Context ctx)
        throws AgentException
    {
        Context subCtx = ctx.createSubContext();
        final String ban = WebAgents.getParameter(subCtx, "key");
        ContextAgents.doReturn(subCtx);
    }

    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        link = modifyLink(ctx, bean, link);
        link.add("cmd", "AccountAttachments");
        //link.add("CMD", "New");

        out.print("<a href=\"");
        link.write(out);
        //out.print("\" onclick=\"try{return confirm('Proceed with dispute of ");
        //out.print(XBeans.getIdentifier(bean));
        //out.print("?');}catch(everything){}\">");
        out.print(getLabel());
        out.print("</a>");
    }

    public void writeLinkDetail(Context ctx, PrintWriter out, Object bean, Link link)
    {
        MessageMgr mmgr = new MessageMgr(ctx, this);
        ButtonRenderer br = (ButtonRenderer)ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
        link = modifyLink(ctx, bean, link);
        link.add("cmd", "AccountAttachments");
        //link.add("CMD", "New");

        out.print("<a href=\"");
        link.write(out);
        //out.print("\" onclick=\"try{return confirm('Proceed to attachments ");
        //out.print(XBeans.getIdentifier(bean));
        //out.print("?');}catch(everything){}\">");
        out.print(mmgr.get("WebAction."+getKey()+".DetailLabel", br.getButton(ctx, getKey(), mmgr.get("WebAction."+getKey()+".Label", getLabel()))));
        out.print("</a>");
    }

}