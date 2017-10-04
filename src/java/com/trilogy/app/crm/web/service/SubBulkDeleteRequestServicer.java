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
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.SubBulkDelete;
import com.trilogy.app.crm.bean.SubBulkDeleteWebControl;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.numbermgn.NumberMgnSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.PackageSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * This class is a http request servicer response for bulk deleting subscribers
 * 
 * @author rattapattu
 */
public class SubBulkDeleteRequestServicer implements RequestServicer
{
    public final static String DEFAULT_TITLE = "<b>MSISDN Bulk Delete: </b>";

    protected WebControl wc_ = new SubBulkDeleteWebControl();

    protected String title_ = DEFAULT_TITLE;

    protected String buttonString_ = "Delete";

    protected String request_ = "Delete subscribers";

    public SubBulkDeleteRequestServicer()
    {
    }

    public SubBulkDeleteRequestServicer(String title)
    {
        super();
        setTitle(title);
    }

    public String getTitle()
    {
        return title_;
    }

    public void setTitle(String title)
    {
        title_ = title;
    }

    /**
     * Returns the buttonString_.
     * 
     * @return String
     */
    public String getButtonString()
    {
        return buttonString_;
    }

    /**
     * Returns the request_.
     * 
     * @return String
     */
    public String getRequest()
    {
        return request_;
    }

    /**
     * Sets the buttonString_.
     * 
     * @param buttonString
     *            The buttonString_ to set
     */
    public void setButtonString(String buttonString)
    {
        this.buttonString_ = buttonString;
    }

    /**
     * Sets the request_.
     * 
     * @param request
     *            The request_ to set
     */
    public void setRequest(String request)
    {
        this.request_ = request;
    }

    /** Template method to be overriden by subclasses as required. * */
    public void outputPreForm(Context ctx, HttpServletRequest req,
            HttpServletResponse res)
    {
        // nop
    }

    /** Template method to be overriden by subclasses as required. * */
    public void outputPostForm(Context ctx, HttpServletRequest req,
            HttpServletResponse res)
    {
        // nop
    }

    public void service(Context ctx, HttpServletRequest req,
            HttpServletResponse res) throws ServletException, IOException
    {
        PrintWriter out = res.getWriter();

        ctx = ctx.createSubContext();
        ctx.put("MODE", OutputWebControl.EDIT_MODE);

        final SubBulkDelete form = new SubBulkDelete();

        final MessageMgr manager = new MessageMgr(ctx, this);

        final HTMLExceptionListener exceptions = new HTMLExceptionListener(
                manager);
        ctx.put(ExceptionListener.class, exceptions);

        final ButtonRenderer buttonRenderer = (ButtonRenderer) ctx.get(
                ButtonRenderer.class, DefaultButtonRenderer.instance());

        try
        {
            wc_.fromWeb(ctx, form, req, "");
        }
        catch (IllegalStateException e)
        {
        }

        if (buttonRenderer.isButton(ctx, "Delete"))
        {
            boolean continueProcessing = true;
            try
            {
                validateUserInput(form);
            }
            catch (IllegalStateException e)
            {
                out.println("<b> Error : " + e.getMessage() + "</b>");
                continueProcessing = false;
            }

            if (continueProcessing)
            {
                try
                {
                    SubscriberDeletingVisitor visitor = deleteSubscribers(ctx, form);
                    
                    if(visitor.getCount()>0)
                    {
	                    if (visitor.getProcessed() > 0)
	                    {
	                        out.println("<b>" + visitor.getProcessed()
	                                + " subscribers  were deleted successfully</b>");
	                    }
	                    
	                    if(visitor.getErrors().trim().length()>0)
	                    {
	                        out.println("<br>");
	                        out.println("<b>The following errors were observed");
	                        out.println(visitor.getErrors());
	                        out.println("<br>");
	                    }
                    }    
                    else
                    {
                        out.println("<b>There are no PREPAID subscribers who are in avialable state within the given msisdn range</b>");
                    }
                }
                catch (Exception e)
                {
                    new MajorLogMsg(this,
                            "Error occured while deleting subscribers", e)
                            .log(ctx);
                }
            }
        }

        FormRenderer frend = (FormRenderer) ctx.get(FormRenderer.class,
                DefaultFormRenderer.instance());

        frend.Form(out, ctx);
        out.print("<table><tr><td>");
        wc_.toWeb(ctx, out, "", form);

        out.print("</td></tr><tr><th align=right>");

        buttonRenderer.inputButton(out, ctx, this.getClass(), "Preview", false);
        buttonRenderer.inputButton(out, ctx, this.getClass(), "Delete", false);
        outputHelpLink(ctx, out, buttonRenderer);

        out.println("</th></tr></table>");

        frend.FormEnd(out);
        out.println("<br/>");
    }

    /**
     * @param form
     */
    private void validateUserInput(SubBulkDelete form)
            throws IllegalStateException
    {
        String msisdn = form.getStartingMsisdn();
        if (msisdn == null || msisdn.trim().length() == 0)
        {
            throw new IllegalStateException("Starting msisdn cannot be empty");
        }

        try
        {
            Long.parseLong(msisdn);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalStateException(
                    "Starting msisdn has to be a number");
        }

        if (form.getSubNumToDelete() < 0)
        {
            throw new IllegalStateException(
                    "Subscribers to delete has to be a positive number");
        }

    }

    /**
     * @param form
     * 
     * The subscriber should be prepaid and available
     * 
     * This is not an everyday task hence not worried about performance at all.
     * Some of the optimizations are limited to the features supported by the
     * framework. therefore in some cases had no choice
     */
    private SubscriberDeletingVisitor deleteSubscribers(Context ctx, SubBulkDelete form)
            throws Exception
    {
        final Home subHome = (Home) ctx.get(SubscriberHome.class);

        long start = Long.parseLong(form.getStartingMsisdn());
        long end = start + form.getSubNumToDelete();

        SubscriberDeletingVisitor visitor = new SubscriberDeletingVisitor();
        /*
         * In order to retrieve the stats recorded by the visitor we need to use the visitor returned by
         * the home operation.
         * 
         * RMI is passed by value not reference. (the reference is only valid in the local memory space.)
         * 
         */
        return (SubscriberDeletingVisitor)subHome.where(ctx, new SimpleXStatement(getSqlClause(start, end)))
                .forEach(ctx, visitor);                
    }

    // TODO: convert to ELang, ??? Does to_number() force database to not use
    // index?
    private String getSqlClause(long start, long end)
    {
        return "subscriberType=" + SubscriberTypeEnum.PREPAID_INDEX
                + " AND state=" + SubscriberStateEnum.AVAILABLE_INDEX
                + " AND msisdn>=" + start + " AND msisdn<=" + end;
    }

    /**
     * Calls com.redknee.framework.xhome.webcontrol.BeanWebController.outputHelpLink()
     *
     * @param context the current context
     * @param out the current PrintWriter
     */
    private void outputHelpLink(final Context context, final PrintWriter out, final ButtonRenderer buttonRenderer)
    {
    	// in the future we might need to specify the HttpServletRequest and HttpServletResponse
        BeanWebController.outputHelpLink(context, null, null, out, buttonRenderer);
    }


} // class
