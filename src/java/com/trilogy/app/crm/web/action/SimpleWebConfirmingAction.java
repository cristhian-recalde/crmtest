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
import java.security.Permission;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;


/**
 **/
public class SimpleWebConfirmingAction extends SimpleWebAction
{

    private static final long serialVersionUID = 1L;


    public SimpleWebConfirmingAction()
    {
        this("confirm", "Confirm");
    }


    public SimpleWebConfirmingAction(Permission permission)
    {
        this();
        setPermission(permission);
    }


    public SimpleWebConfirmingAction(String key, String label)
    {
        super(key, label);
        defaultHelpText_ = "Confirm the changes and additions if any.";
    }


    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        if (isConfirmEnabledInTableView(ctx, bean))
        {
            writeLinkConfirmingInTableView(ctx, out, bean, link);
        }
        else
        {
            super.writeLink(ctx, out, bean, link);
        }
    }


    public void writeLinkDetail(Context ctx, PrintWriter out, Object bean, Link link)
    {
        if (isConfirmEnabledInDetailView(ctx, bean))
        {
            writeLinkConfrimingInDetailView(ctx, out, bean, link);
        }
        else
        {
            super.writeLinkDetail(ctx, out, bean, link);
        }
    }


    protected String getConfirmationMessage(Context ctx, Object bean)
    {
        return defaultHelpText_;
    }


    /**
     * Extend-Override it to have predicate/logic
     * 
     * @param ctx
     * @param bean
     * @return
     */
    protected boolean isConfirmEnabledInDetailView(Context ctx, Object bean)
    {
        return true;
    }


    /**
     * Extend-Override it to have predicate/logic
     * 
     * @param ctx
     * @param bean
     * @return
     */
    protected boolean isConfirmEnabledInTableView(Context ctx, Object bean)
    {
        return false;
    }


    private void writeLinkConfrimingInDetailView(Context ctx, PrintWriter out, Object bean, Link link)
    {
        MessageMgr mmgr = new MessageMgr(ctx, this);
        ButtonRenderer br = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
        link = modifyLink(ctx, bean, link);
        out.print("<a href=\"");
        link.write(out);
        out.print("\" onclick=\"try{return confirm('" + getConfirmationMessage(ctx, bean)
                + "');}catch(everything){}\">");
        out.print(mmgr.get("WebAction." + getKey() + ".DetailLabel",
                br.getButton(ctx, getKey(), mmgr.get("WebAction." + getKey() + ".Label", getLabel()))));
        out.print("</a>");
    }


    private void writeLinkConfirmingInTableView(Context ctx, PrintWriter out, Object bean, Link link)
    {
        link = modifyLink(ctx, bean, link);
        out.print("<a href=\"");
        link.write(out);
        out.print("\" onclick=\"try{return confirm('" + getConfirmationMessage(ctx, bean)
                + "');}catch(everything){}\">");
        out.print(getLabel());
        out.print("</a>");
    }
}
