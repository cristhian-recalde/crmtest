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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ReportTypeEnum;
import com.trilogy.app.crm.web.border.InvoiceAccountNoteWebBorder;
import com.trilogy.app.crm.web.service.InvoiceServerRemoteRequestServicer;


/**
 * Provides a delivery action for sending reports (Invoice or Wallet Reports)
 *
 * TODO: Property support different delivery options and wallet reports.  Email is the only one when this was implemented.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class InvoiceDeliveryAction extends SimpleWebAction
{

    /**
     * Default constructor that initializes the invoice preview screen
     */
    public InvoiceDeliveryAction()
    {
        super();
    }

    /**
     * Default constructor that initializes the invoice preview screen
     */
    public InvoiceDeliveryAction(String action, String label)
    {
        super(action, label);
    }

    /**
     * {@inheritDoc}
     */
    public InvoiceDeliveryAction(SimplePermission permission,
            String action, String label)
    {
        this(action, label);
        setPermission(permission);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        final Object id = WebAgents.getParameter(ctx, "key");
        WebAgents.setParameter(ctx, "key", null);
        
        try
        {
            new InvoiceServerRemoteRequestServicer(ctx, "SubMenuAccountInvoices")
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public String getURI()
                {
                    StringBuilder uri = new StringBuilder(super.getURI());
                    uri.append("?send=y&key=");
                    try
                    {
                        uri.append(URLEncoder.encode(String.valueOf(id), "UTF-8"));
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        new MinorLogMsg(this, "Unsupported encoding: " + e.getMessage(), e).log(ctx);
                    }
                    return uri.toString();
                }
            }.service(ctx,
                    (HttpServletRequest)  ctx.get(HttpServletRequest.class),
                    (HttpServletResponse) ctx.get(HttpServletResponse.class));
        }
        catch (Exception e)
        {
            throw new AgentException(e);
        }

        super.execute(ctx);

        // return to summary view.
        Context subCtx = ctx.createSubContext();
        Link link = new Link(subCtx);
        String cmd = WebAgents.getParameter(subCtx, "cmd");
        link.add("cmd", cmd);
        try
        {
            WebAgents.service(subCtx, link.write(), WebAgents.getWriter(subCtx));
        }
        catch (ServletException ex)
        {
            throw new AgentException("fail to redirect to " + cmd, ex);
        }
        catch (IOException ioEx)
        {
            throw new AgentException("fail to redirect to " + cmd, ioEx);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        final Link newLink = new Link(link);

        // Add a note message to the link.  This is used by the InvoiceAccountNoteWebBorder to
        // automatically create an account note to indicate an access attempt.
        newLink.addRaw(InvoiceAccountNoteWebBorder.MESSAGE, 
                " - " + ReportTypeEnum.INVOICE + " delivery - ");

        super.writeLink(ctx, out, bean, newLink);
    }
}