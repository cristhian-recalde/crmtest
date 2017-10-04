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

package com.trilogy.app.crm.web.agent;

import java.io.PrintWriter;
import java.util.Date;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;

import com.trilogy.app.crm.bean.AutoDepositReleaseForm;
import com.trilogy.app.crm.bean.AutoDepositReleaseFormWebControl;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.deposit.AutoDepositRelease;
import com.trilogy.app.crm.deposit.AutoDepositReleaseFormDatesValidator;
import com.trilogy.app.crm.deposit.DefaultDepositReleaseTransactionCreator;
import com.trilogy.app.crm.deposit.DefaultSubscriberReleaseVisitor;
import com.trilogy.app.crm.support.SpidSupport;

/**
 * This Web Agent allows manually triggering of Auto Deposit Release.
 *
 * @author cindy.wong@redknee.com
 */
public class AutoDepositReleaseWebAgent extends ServletBridge implements WebAgent
{

    /**
     * Module name.
     */
    public static final String MODULE = "RunAutoDepositRelease";

    /**
     * Web control for Auto Deposit Release Form.
     */
    private final AutoDepositReleaseFormWebControl webControl_ = new AutoDepositReleaseFormWebControl();

    /**
     * {@inheritDoc}
     */
    public void execute(final Context context) throws AgentException
    {
        final PrintWriter out = getWriter(context);
        final AutoDepositReleaseForm form = new AutoDepositReleaseForm();
        final Context subContext = context.createSubContext();
        final MessageMgr messageManager = new MessageMgr(subContext, MODULE);
        final HTMLExceptionListener listener = new HTMLExceptionListener(messageManager);
        subContext.put(ExceptionListener.class, listener);
        boolean displayMessage = false;

        subContext.put("MODE", OutputWebControl.EDIT_MODE);

        if (getParameter(context, "Run") != null)
        {
            displayMessage = true;
            this.webControl_.fromWeb(subContext, form, getRequest(context), "");

            try
            {
                // validate the date
                AutoDepositReleaseFormDatesValidator.instance().validate(subContext, form);
            }
            catch (IllegalPropertyArgumentException exception)
            {
                listener.thrown(exception);
            }

            final Date activeDate = form.getActiveDate();
            final int spid = form.getServiceProvider();
            CRMSpid serviceProvider;
            try
            {
                serviceProvider = SpidSupport.getCRMSpid(context, spid);
            }
            catch (HomeException exception)
            {
                throw new AgentException("Cannot retrieve SPID", exception);
            }
            final AutoDepositRelease autoDepositRelease = new AutoDepositRelease(serviceProvider, activeDate,
                new DefaultSubscriberReleaseVisitor(), DefaultDepositReleaseTransactionCreator.getInstance());
            autoDepositRelease.execute(context);
        }

        final FormRenderer renderer = (FormRenderer) context.get(FormRenderer.class, DefaultFormRenderer.instance());
        renderer.Form(out, context);

        if (listener.hasErrors())
        {
            listener.toWeb(subContext, out, "", form);
        }
        else if (displayMessage)
        {
            out.print("<pre><center><font size=\"1\" face=\"Verdana\" color=\"green\"><b>Auto Deposit Release started.</b></font></center></pre>");
        }

        out.println("<table><tr><td>");
        this.webControl_.toWeb(subContext, out, "", form);
        out.println("</td></tr></table>");
        out.println("<input type=\"submit\" value=\"Run\" name=\"Run\" />");
        out.println("<input type=\"reset\" />");
        renderer.FormEnd(out);
        out.println("<br />");
    }
}
