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
package com.trilogy.app.crm.web.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.service.XRemoteRequestServicer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.invoice.config.InvoiceServerRemoteServicerConfig;


/**
 * This request servicer is an extension of FW's remote menu request servicer.  The FW one takes
 * a static URL string pointing to the remote application, as well as static username/password.
 * 
 * This extension of it reuses the configuration provided in InvoiceServerRemoteServicerConfig
 * to do the same thing.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.5
 */
/**
 * Pass account identifier to Invoice Server when statement generation is requested from BSS
 */
public class InvoiceServerRemoteRequestServicer extends XRemoteRequestServicer implements ContextAware
{
    public InvoiceServerRemoteRequestServicer(Context ctx, String cmd)
    {
        this(ctx, cmd, cmd);
    }
    
    public InvoiceServerRemoteRequestServicer(Context ctx, String cmd, boolean isAccountFromContext)
    {
        this(ctx, cmd, cmd, isAccountFromContext);
    }
    
    public InvoiceServerRemoteRequestServicer(Context ctx, String localCmd, String remoteCmd)
    {
        this(ctx, localCmd, remoteCmd, false);
    }
    
    public InvoiceServerRemoteRequestServicer(Context ctx, String localCmd, String remoteCmd,boolean isAccountFromContext)
    {
        super("http://RKINVOICE:11100/AppCrmInvoice/home", remoteCmd, localCmd);
        setContext(ctx);
        isAccountFromContext_ = isAccountFromContext;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException
    {
        try
        {
            super.service(ctx, req, res);
        }
        catch (IOException e)
        {
            new MajorLogMsg(this, "Unable to connect to Invoice Server.  Attempted=" + getURI()
                    + ", Menu CMD=" + this.getRemoteCmd()
                    + ", User=" + this.getUsername(), e).log(ctx);

            Context subCtx = ctx.createSubContext();
            Link link = new Link(subCtx);
            String cmd = WebAgents.getParameter(subCtx, "cmd");
            link.add("cmd", cmd);

            PrintWriter out = WebAgents.getWriter(subCtx);

            out.print("<table><tr><td>");
            
            ButtonRenderer br = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
            br.linkButton(out, ctx, ".RETRY_CMD", "Retry", link);
            
            out.print("</td></tr></table>");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getURI()
    {
        InvoiceServerRemoteServicerConfig invoiceServerConnectionInfo = getInvoiceServerConnectionInfo();
        if (invoiceServerConnectionInfo != null)
        {
            String protocol = invoiceServerConnectionInfo.getSecure() ? "https" : "http";
            String hostName = invoiceServerConnectionInfo.getHostName();
            String appPort = invoiceServerConnectionInfo.getBasePort();
            String webAppName = invoiceServerConnectionInfo.getWebApplicationName();
            return protocol + "://" + hostName + ":" + appPort + "/" + webAppName + "/home";
        }
        return super.getURI();
    }

    /**
     * {@inheritDoc}
     */
    public String getPassword(Context ctx)
    {
        InvoiceServerRemoteServicerConfig invoiceServerConnectionInfo = getInvoiceServerConnectionInfo();
        if (invoiceServerConnectionInfo != null)
        {
            return URLEncoder.encode(invoiceServerConnectionInfo.getPassword(ctx));
        }
        return URLEncoder.encode(super.getPassword());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername()
    {
        InvoiceServerRemoteServicerConfig invoiceServerConnectionInfo = getInvoiceServerConnectionInfo();
        if (invoiceServerConnectionInfo != null)
        {
            return URLEncoder.encode(invoiceServerConnectionInfo.getUserId());
        }
        return URLEncoder.encode(super.getUsername());
    }
    
    
    @Override
    public String getRemoteCmd()
    {
        String cmd = super.getRemoteCmd();
        if (isAccountFromContext())
        {
            Context ctx = ContextLocator.locate();
            if(ctx == null)
            {
                ctx = getContext();
            }
            if(ctx == null)
            {
                return cmd;
            }
            Account account = (Account) ctx.get(Account.class);
            if (account != null)
            {
                cmd = cmd + "&.accountIdentifier=" + account.getBAN();
            }
            else
            {
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "Account was expected in context with key Account.class");
                }
            }
        }
        return cmd;
    }
    
    public InvoiceServerRemoteServicerConfig getInvoiceServerConnectionInfo()
    {
        Context ctx = getContext();
        
        if (!ctx.has(InvoiceServerRemoteServicerConfig.class))
        {
            Context appCtx = (Context) ctx_.get("Application");
            if (appCtx != null)
            {
                ctx = appCtx;
                setContext(ctx);
            }
        }
        
        return (InvoiceServerRemoteServicerConfig) ctx.get(InvoiceServerRemoteServicerConfig.class);
    }

    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return ctx_;
    }

    /**
     * {@inheritDoc}
     */
    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }
    
    public boolean isAccountFromContext()
    {
        return isAccountFromContext_;
    }

    protected Context ctx_ = null;
    private boolean isAccountFromContext_;
}
