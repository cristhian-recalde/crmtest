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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.app.crm.transfer.TransfersView;
import com.trilogy.app.crm.transfer.TransfersViewHome;
import com.trilogy.app.troubleticket.Common;
import com.trilogy.app.troubleticket.entity.EntityInterface;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.LogSupport;

public class DisputeTransferWebAction
    extends SimpleWebAction
{
    public DisputeTransferWebAction()
    {
        super("disputeTransfer", "Dispute Transfer");
    }

    public void execute(Context ctx) throws AgentException
    {
        Context subCtx = ctx.createSubContext();
        String key = WebAgents.getParameter(subCtx, "key");
        Home h = (Home)subCtx.get(TransfersViewHome.class);

        try
        {
            final TransfersView transfer = (TransfersView)h.find(subCtx, key);
            subCtx.put(TransfersView.class, transfer);
            final TransferDispute transferDispute =  (TransferDispute)XBeans.instantiate(TransferDispute.class, subCtx);
            subCtx.put(Common.TT_ENTITY_CREATE_TYPE, TransferDispute.class);
            subCtx.put(EntityInterface.class, transferDispute);
            subCtx.put(Subscriber.class, HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class,transfer.getContSubId()));
        }
        catch(Exception e)
        {
            LogSupport.minor(subCtx, this, "Error trying to retrieve Transfer [" + key + "]", e);
            throw new AgentException(e);
        }
        ContextAgents.doReturn(subCtx);
    }

    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        link = modifyLink(ctx, bean, link);
        link.add("cmd", "appTroubleticketHidden");
        link.add("CMD", "New");

        out.print("<a href=\"");
        link.write(out);
        out.print("\" onclick=\"try{return confirm('Proceed with dispute of ");
        out.print(XBeans.getIdentifier(bean));
        out.print("?');}catch(everything){}\">");
        out.print(getLabel());
        out.print("</a>");
    }

    public void writeLinkDetail(Context ctx, PrintWriter out, Object bean, Link link)
    {
        MessageMgr mmgr = new MessageMgr(ctx, this);
        ButtonRenderer br = (ButtonRenderer)ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
        link = modifyLink(ctx, bean, link);
        link.add("cmd", "appTroubleticketHidden");
        link.add("CMD", "New");

        out.print("<a href=\"");
        link.write(out);
        out.print("\" onclick=\"try{return confirm('Proceed with dispute of ");
        out.print(XBeans.getIdentifier(bean));
        out.print("?');}catch(everything){}\">");
        out.print(mmgr.get("WebAction."+getKey()+".DetailLabel", br.getButton(ctx, getKey(), mmgr.get("WebAction."+getKey()+".Label", getLabel()))));
        out.print("</a>");
    }

}