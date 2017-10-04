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
package com.trilogy.app.crm.dunning;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

/**
 * Provides support for initiating the dunning process.
 *
 * @author gary.anderson@redknee.com
 */
public class DunningRequestServicer implements RequestServicer
{
    /**
     * Creates a new DunningRequestServicer.
     */
    public DunningRequestServicer()
    {
        webControl_ = new DunningProcessFormWebControl();
    }

    // INHERIT
    public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res)
        throws ServletException, IOException
    {
        final PrintWriter out = res.getWriter();

        // not used (PS)
        // final ColourSettings colours = ColourSettings.getSettings(ctx);
        // final String cmd = req.getParameter("cmd");

        final Context subContext = ctx.createSubContext();
        subContext.put("MODE", OutputWebControl.EDIT_MODE);

        final DunningProcessForm form = new DunningProcessForm();

        String msg = null;
        boolean msgIsProblemReport = false;

        final ButtonRenderer buttonRenderer = (ButtonRenderer) subContext.get(ButtonRenderer.class,
                DefaultButtonRenderer.instance());

        // TODO - 2003-11-10 - Should I be using MessageMgr here?

        if (buttonRenderer.isButton(subContext, "Preview"))
        {
            webControl_.fromWeb(subContext, form, req, "");
        }
        else if (buttonRenderer.isButton(subContext, "Process"))
        {
            webControl_.fromWeb(subContext, form, req, "");

            try
            {
                DunningProcessFormDatesValidator.instance().validate(subContext, form);

                switch (form.getScope().getIndex())
                {
                    case DunningProcessScopeEnum.ALL_INDEX:
                    {
                        getDunningProcess(ctx).processAllAccounts(subContext, form.getProcessDate());

                        msg = "Dunning process initiated for all accounts.  ";
                        break;
                    }
                    case DunningProcessScopeEnum.ACCOUNT_INDEX:
                    {
                        getDunningProcess(ctx).processAccount(subContext, form.getProcessDate(),
                                form.getAccountIdentifier());

                        msg = "Dunning process initiated for account " + form.getAccountIdentifier() + ".  ";
                        break;
                    }
                    case DunningProcessScopeEnum.SPID_INDEX:
                    {
                        getDunningProcess(ctx).processAllAccountsWithServiceProviderID(subContext,
                                form.getProcessDate(), form.getSpid());

                        msg = "Dunnings process initiated for service provider " + form.getSpid() + ".  ";
                        break;
                    }
                    case DunningProcessScopeEnum.BILLCYCLE_INDEX:
                    {
                        getDunningProcess(ctx).processAllAccountsWithBillCycleID(subContext, form.getProcessDate(),
                                form.getBillCycleID());

                        msg = "Dunnings process initiated for billing cycle " + form.getBillCycleID() + ".  ";
                        break;
                    }
                    default:
                    {
                        throw new IllegalStateException(
                                "Could not generate dunnings.  Unknow identifier type selected.");
                    }
                }
            }
            catch (final IllegalStateException exception)
            {
                msg = exception.getMessage();
                msgIsProblemReport = true;
            }
            catch (final IllegalPropertyArgumentException exception)
            {
                msg = exception.getMessage();
                msgIsProblemReport = true;
            }
            catch (final DunningProcessException e)
            {
                msg = e.getMessage();
                msgIsProblemReport = true;
            }
            catch (final Exception e)
            {
                msg = e.getMessage();
                msgIsProblemReport = true;
            }
        }

        final FormRenderer formRenderer = (FormRenderer) subContext.get(FormRenderer.class,
                DefaultFormRenderer.instance());

        formRenderer.Form(out, subContext);

        out.print("<table>");
        if (msg != null)
        {
            if (!msgIsProblemReport)
            {
                out.println("<tr><td align=\"center\"><b style=\"color:green;\">");
            }
            else
            {
                out.println("<tr><td align=\"center\"><b style=\"color:red;\">");
            }

            out.print(msg);

            out.println("</b></td></tr>");
        }

        out.print("<tr><td>");
        webControl_.toWeb(subContext, out, "", form);

        out.print("</td></tr><tr><th align=\"right\">");

        buttonRenderer.inputButton(out, subContext, this.getClass(), "Preview", false);
        buttonRenderer.inputButton(out, subContext, this.getClass(), "Process", false);
        outputHelpLink(subContext, out, buttonRenderer);

        out.print("</th></tr></table>");

        formRenderer.FormEnd(out);
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

    public DunningProcess getDunningProcess(final Context ctx)
    {
        return (DunningProcess) ctx.get(DunningProcess.class);
    }

    /**
     * The webcontrol used to represent the form.
     */
    protected final DunningProcessFormWebControl webControl_;

} // class
