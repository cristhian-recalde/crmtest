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

import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;


public abstract class AbstractConfirmingAction extends SimpleWebAction
{

    /**
     * author simar.singh@redknee.com
     * a simple class to allow we-actions to ask for configurable messages 
     * in confirmation dailogue boxex
     */
    private static final long serialVersionUID = 1L;


    public AbstractConfirmingAction()
    {
        super("key", "Label");
    }


    public AbstractConfirmingAction(String key, String label)
    {
       super(key,label);
    }


    abstract public void execute(Context ctx) throws AgentException;


    public void printMessage(PrintWriter out, String msg)
    {
        out.println("<font color=\"green\">" + msg + "</font><br/><br/>");
    }


    public void printError(PrintWriter out, String error)
    {
        out.println("<font color=\"red\">" + error + "</font><br/><br/>");
    }


    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        if (bean instanceof AccountAttachment)
        {
            AccountAttachment sub = (AccountAttachment) bean;
            if (isAllowed(ctx, sub))
            {
                link = modifyLink(ctx, bean, link);
                out.print("<a href=\"");
                link.write(out);
                out.print("\" onclick=\"try{return confirm('" + getConfirmationMessage(ctx, sub)
                        + "');}catch(everything){}\">");
                out.print(getLabel());
                out.print("</a>");
            }
        }
    }


    public void writeLinkDetail(Context ctx, PrintWriter out, Object bean, Link link)
    {
        if (bean instanceof AccountAttachment)
        {
            AccountAttachment attachment = (AccountAttachment) bean;
            if (isAllowed(ctx, attachment))
            {
                MessageMgr mmgr = new MessageMgr(ctx, this);
                ButtonRenderer br = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
                link = modifyLink(ctx, bean, link);
                out.print("<a href=\"");
                link.write(out);
                out.print("\" onclick=\"try{return confirm('" + getConfirmationMessage(ctx, attachment)
                        + "');}catch(everything){}\">");
                out.print(mmgr.get("WebAction." + getKey() + ".DetailLabel", br.getButton(ctx, getKey(), mmgr.get(
                        "WebAction." + getKey() + ".Label", getLabel()))));
                out.print("</a>");
            }
        }
    }


    protected String getConfirmationMessage(Context ctx, AccountAttachment attachment)
    {
        return "Do you wish to continue?";
    }


    protected boolean isAllowed(Context ctx, AccountAttachment attachment)
    {
        return true;
    }
}
