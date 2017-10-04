/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

/**
 * @author Simar Singh
 */
public abstract class SubscriptionWebConfrimAction extends SimpleWebAction
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public SubscriptionWebConfrimAction(String key, String label)
    {
        super(key, label);
    }


    public abstract void execute(Context ctx) throws AgentException;


    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        if (bean instanceof Subscriber)
        {
            Subscriber sub = (Subscriber) bean;
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
        if (bean instanceof Subscriber)
        {
            Subscriber sub = (Subscriber) bean;
            if (isAllowed(ctx, sub))
            {
                MessageMgr mmgr = new MessageMgr(ctx, this);
                ButtonRenderer br = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
                link = modifyLink(ctx, bean, link);
                out.print("<a href=\"");
                link.write(out);
                out.print("\" onclick=\"try{return confirm('" + getConfirmationMessage(ctx, sub)
                        + "');}catch(everything){}\">");
                out.print(mmgr.get("WebAction." + getKey() + ".DetailLabel", br.getButton(ctx, getKey(), mmgr.get(
                        "WebAction." + getKey() + ".Label", getLabel()))));
                out.print("</a>");
            }
        }
    }


    protected String getConfirmationMessage(Context ctx, Subscriber sub)
    {
        return "Do you want to continue?";
    }


    protected boolean isAllowed(Context ctx, Subscriber subscriber)
    {
        return true;
    }
    
    
}