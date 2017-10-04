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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.bean.PPVModificationRequest;
import com.trilogy.app.crm.bean.PPVModificationRequestStateEnum;
import com.trilogy.app.crm.bean.PPVModificationRequestWebControl;
import com.trilogy.app.crm.bean.PPVModificationRequestXInfo;
import com.trilogy.app.crm.priceplan.task.PricePlanVersionModificationApplierVisitor;
import com.trilogy.app.crm.priceplan.task.PricePlanVersionModificationLifecycleAgent;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.web.action.PricePlanVersionCancelModificationAction;
import com.trilogy.app.crm.web.action.PricePlanVersionModificationAction;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * Request servicer used to create a price plan version modification request.
 * 
 * @author Marcio Marques
 * @since 9.2
 */
public class PricePlanVersionModificationRequestServicer implements RequestServicer
{
    public static final String MSG_KEY_BACK_BUTTON_NAME    = "PricePlanVersionModificationRequestServicer.BackButtonName";
    public static final String MSG_KEY_SAVE_BUTTON_NAME    = "PricePlanVersionModificationRequestServicer.SaveButtonName";
    public static final String MSG_KEY_PREVIEW_BUTTON_NAME = "PricePlanVersionModificationRequestServicer.PreviewButtonName";

    static Set<String>         sessionSet                  = Collections.synchronizedSet(new HashSet<String>());

    private WebControl         webControl                  = new PPVModificationRequestWebControl();


    public final void service(final Context context, final HttpServletRequest req, final HttpServletResponse res)
            throws IOException
    {
        Context sCtx = context.createSubContext();
        final PrintWriter out = res.getWriter();

        HttpSession session = req.getSession();

        MessageMgr manager = new MessageMgr(sCtx, this);

        final ButtonRenderer buttonRenderer = (ButtonRenderer) sCtx.get(ButtonRenderer.class,
                DefaultButtonRenderer.instance());

        String backButtonText = manager.get(MSG_KEY_BACK_BUTTON_NAME, " Back ");
        String saveButtonText = manager.get(MSG_KEY_SAVE_BUTTON_NAME, "Save Modification");
        String previewButtonText = manager.get(MSG_KEY_PREVIEW_BUTTON_NAME, "Preview");

        boolean showRedText = false;
        PPVModificationRequest request = null;

        HTMLExceptionListener exceptions = (HTMLExceptionListener) context.get(HTMLExceptionListener.class);
        if (exceptions == null)
        {
            exceptions = new HTMLExceptionListener(manager);
            sCtx.put(HTMLExceptionListener.class, exceptions);
        }
        sCtx.put(ExceptionListener.class, exceptions);

        long id = -1;
        int version = -1;
        String action = null;

        if (buttonRenderer.isButton(sCtx, saveButtonText) || buttonRenderer.isButton(sCtx, previewButtonText))
        {
            sCtx.put("MODE", OutputWebControl.CREATE_MODE);
            request = (PPVModificationRequest) webControl.fromWeb(sCtx, req, "");
            id = request.getPricePlanIdentifier();
            version = request.getPricePlanVersion();
            action = PricePlanVersionModificationAction.DEFAULT_KEY;
        }
        else
        {
            final String sKey = WebAgents.getParameter(sCtx, ".versionskey");
            if (sKey != null)
            {
                final String[] keys = sKey.split("`");
                if (keys.length != 2)
                {
                    final AgentException exception = new AgentException("Key not found or not understood.");
                    exceptions.thrown(exception);
                }
                else
                {
                    id = Long.parseLong(keys[0]);
                    version = Integer.parseInt(keys[1]);
                    action = req.getParameter(".versionsaction");
                }
            }
        }

        HomeOperationEnum operation = null;
        if (action != null)
        {

            And filter = new And();
            filter.add(new EQ(PPVModificationRequestXInfo.PRICE_PLAN_IDENTIFIER, Long.valueOf(id)));
            filter.add(new EQ(PPVModificationRequestXInfo.PRICE_PLAN_VERSION, Integer.valueOf(version)));
            filter.add(new EQ(PPVModificationRequestXInfo.STATUS, Integer
                    .valueOf(PPVModificationRequestStateEnum.PENDING_INDEX)));

            try
            {
                request = HomeSupportHelper.get(sCtx).findBean(sCtx, PPVModificationRequest.class, filter);

                if (PricePlanVersionModificationAction.DEFAULT_KEY.equals(action))
                {
                    if (request == null)
                    {
                        operation = HomeOperationEnum.CREATE;
                        sCtx.put("MODE", OutputWebControl.CREATE_MODE);
                        request = new PPVModificationRequest(sCtx, id, version);
                        request.setStatus(PPVModificationRequestStateEnum.PENDING_INDEX);
                        request.setCreatedDate(null);
                        request.setActivationDate(CalendarSupportHelper.get(sCtx).getDateWithNoTimeOfDay(
                                        CalendarSupportHelper.get(sCtx).getRunningDate(sCtx)));
                        final User principal = (User) context.get(java.security.Principal.class, new User());
                        request.setAgentId(principal.getId().trim().equals("") ? "System" : principal.getId());
                    }
                    else
                    {
                        sCtx.put("MODE", OutputWebControl.EDIT_MODE);
                        operation = HomeOperationEnum.STORE;
                    }

                    if (buttonRenderer.isButton(sCtx, previewButtonText))
                    {
                        request = new PPVModificationRequest();
                    }

                    request = modifyPricePlanVersion(sCtx, request, req, res, out, buttonRenderer, previewButtonText,
                            saveButtonText);
                }
                else if (PricePlanVersionCancelModificationAction.DEFAULT_KEY.equals(action))
                {
                    operation = HomeOperationEnum.REMOVE;
                    sCtx.put("MODE", OutputWebControl.DISPLAY_MODE);
                    cancelPricePlanVersionModification(sCtx, request);
                    if (request == null)
                    {
                        request = new PPVModificationRequest();
                        request.setPricePlanIdentifier(id);
                        request.setPricePlanVersion(version);
                        request.reportError(sCtx, new HomeException("Price plan " + id + " version " + version
                                + " has no pending modifications to be canceled."));
                    }

                }
                else
                {
                    request.reportError(sCtx, new UnsupportedOperationException("Unsupported action."));
                }
            }
            catch (HomeException e)
            {
                if (request == null)
                {
                    request = new PPVModificationRequest();
                    request.setPricePlanIdentifier(id);
                    request.setPricePlanVersion(version);
                    request.reportError(sCtx, new HomeException("Unable to retrieve price plan " + id + " version "
                            + version + " pending modification: " + e.getMessage(), e));
                }
            }
        }

        String msg = null;
        if (exceptions.hasErrors() || request.hasErrors(sCtx))
        {
            showRedText = true;
            msg = "Unable to proceed with operation";
            if (operation == HomeOperationEnum.CREATE)
            {
                sCtx.put("MODE", OutputWebControl.CREATE_MODE);
            }
            else
            {
                sCtx.put("MODE", OutputWebControl.EDIT_MODE);
            }
        }
        else if (buttonRenderer.isButton(sCtx, saveButtonText)
                || (PricePlanVersionCancelModificationAction.DEFAULT_KEY.equals(action)))
        {
            if (request.hasWarnings(sCtx))
            {
                showRedText = true;
                msg = request.getPartialSuccessMessage(sCtx, operation);
                sCtx.put("MODE", OutputWebControl.EDIT_MODE);
            }
            else
            {
                msg = request.getSuccessMessage(sCtx, operation);
                sCtx.put("MODE", OutputWebControl.DISPLAY_MODE);
                if (request.getStatus() == PPVModificationRequestStateEnum.PROCESSED_INDEX)
                {
                    msg += " and activated.";
                }
            }
        }
        else
        {
            if (operation == HomeOperationEnum.CREATE)
            {
                sCtx.put("MODE", OutputWebControl.CREATE_MODE);
            }
            else
            {
                sCtx.put("MODE", OutputWebControl.EDIT_MODE);
            }
        }

        final FormRenderer formRenderer = (FormRenderer) sCtx.get(FormRenderer.class, DefaultFormRenderer.instance());

        formRenderer.Form(out, sCtx);

        out.print("<table>");
        if (msg != null)
        {
            out.println("<tr><td>&nbsp;</td></tr><tr><td align=\"center\"><b style=\"color:"
                    + (showRedText ? "red" : "green") + ";\">");
            out.print(msg);
            out.println("</b></td></tr><tr><td>&nbsp;</td></tr>");
        }

        out.print("<tr><td>");

        if (request != null && request.hasErrors(sCtx))
        {
            for (Throwable error : request.getErrors(sCtx))
            {
                if (error.getCause() instanceof CompoundIllegalStateException)
                {
                    ((CompoundIllegalStateException) error.getCause()).rethrow(exceptions);
                }
                else
                {
                    exceptions.thrown(error);
                }
            }
        }

        if (request != null && request.hasWarnings(sCtx))
        {
            for (Throwable error : request.getWarnings(sCtx))
            {
                if (error.getCause() instanceof CompoundIllegalStateException)
                {
                    ((CompoundIllegalStateException) error.getCause()).rethrow(exceptions);
                }
                else
                {
                    exceptions.thrown(error);
                }
            }
        }

        if (exceptions.hasErrors())
        {
            exceptions.toWeb(sCtx, out, "", request);
            out.print("</td></tr><tr><td>&nbsp;</td></tr><tr><td>");
        }

        if (request != null)
        {
            webControl.toWeb(sCtx, out, "", request);
        }

        out.print("</td></tr><tr><th align=\"right\">");

        out.println("<table border=\"0\"><tr><td>");

        if (request != null)
        {
            if ((PricePlanVersionModificationAction.DEFAULT_KEY.equals(action))
                    && (!buttonRenderer.isButton(sCtx, saveButtonText) || request.hasErrors(sCtx)))
            {
                buttonRenderer.inputButton(out, sCtx, this.getClass(), previewButtonText, false);
            }

            out.println("<a href=\"/AppCrm/home?cmd=appCRMPricePlanMenu&amp;key=" + request.getPricePlanIdentifier() + "\">");
            String buttonMsg = backButtonText;

            out.print(" <img  name=\"");
            out.print(WebAgents.rewriteName(sCtx, MSG_KEY_BACK_BUTTON_NAME + DefaultButtonRenderer.BUTTON_KEY));
            out.print("\" id=\"button-");
            out.print(WebAgents.rewriteName(sCtx, MSG_KEY_BACK_BUTTON_NAME + DefaultButtonRenderer.BUTTON_KEY));
            out.print("\" src=\"" + sCtx.get(DefaultButtonRenderer.BUTTONRENDERER_SERVICE_KEY, "ButtonRenderServlet")
                    + "?.src=default&amp;.label=");
            out.print(buttonMsg);
            out.print("\"");
            out.print(" border=\"0\" align=\"right\"");
            out.print(" alt=\"");
            out.print(buttonMsg);
            out.print("\"/> ");
            out.println("</a>");
        }

        if ((PricePlanVersionModificationAction.DEFAULT_KEY.equals(action))
                && (!buttonRenderer.isButton(sCtx, saveButtonText) || request.hasErrors(sCtx)))
        {
            buttonRenderer.inputButton(out, sCtx, this.getClass(), saveButtonText, false);
            BeanWebController.outputHelpLink(sCtx, req, res, out, buttonRenderer);
        }
        out.println("</td></tr></table>");

        out.print("</th></tr></table>");

        formRenderer.FormEnd(out);

    }


    private void cancelPricePlanVersionModification(Context ctx, PPVModificationRequest request)
    {
        if (request != null)
        {
            try
            {
                HomeSupportHelper.get(ctx).removeBean(ctx, request);
            }
            catch (HomeException e)
            {
                request.reportError(ctx, new HomeException("Unable to remove price plan " + request.getPricePlanIdentifier()
                        + " version " + request.getPricePlanVersion() + " modification: " + e.getMessage(), e));
            }
        }
    }


    private PPVModificationRequest modifyPricePlanVersion(Context ctx, PPVModificationRequest request, final HttpServletRequest req,
            final HttpServletResponse res, PrintWriter out, ButtonRenderer buttonRenderer, String previewButtonText,
            String saveButtonText)
    {
        final Context sCtx = ctx.createSubContext();
        try
        {
            if (buttonRenderer.isButton(sCtx, saveButtonText))
            {
                webControl.fromWeb(sCtx, request, req, "");
                if (request.getId()<=0)
                {
                    request.setCreatedDate(CalendarSupportHelper.get(ctx).getRunningDate(ctx));
                    request = HomeSupportHelper.get(ctx).createBean(ctx, request);
                    if (!request.hasErrors(sCtx))
                    {
                        sCtx.put("MODE", OutputWebControl.DISPLAY_MODE);
                    }
                }
                else
                {
                    request = HomeSupportHelper.get(ctx).storeBean(ctx, request);
                    if (!request.hasErrors(sCtx))
                    {
                        sCtx.put("MODE", OutputWebControl.DISPLAY_MODE);
                    }
                }
                
                if (!request.hasErrors(sCtx))
                {
                    Date now = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
                    // Activation is today
                    if (!(request.getActivationDate().after(now)))
                    { 
                        sCtx.put(PricePlanVersionModificationLifecycleAgent.PRICE_PLAN_VERSION_MODIFICATION_AGENT, Boolean.TRUE);
                        PricePlanVersionModificationApplierVisitor visitor = new PricePlanVersionModificationApplierVisitor(null);
                        visitor.visit(sCtx, request);
                        if (request.getStatus() != PPVModificationRequestStateEnum.PROCESSED_INDEX)
                        {
                            request.reportWarning(ctx, new HomeException(
                                    "Unable to immediate activate price plan version. Please check logs for details, and click 'Modify' again or run the '"
                                            + CronConstant.PRICE_PLAN_VERSIONS_MODIFICATIONS_AGENT_DESCRIPTION
                                            + "' task to activate it."));
                        }
                    }
                }
            }
            else if (buttonRenderer.isButton(sCtx, previewButtonText))
            {
                webControl.fromWeb(sCtx, request, req, "");
            }
        }
        catch (final IllegalPropertyArgumentException exception)
        {
            request.reportError(sCtx, exception);
        }
        catch (final IllegalStateException exception)
        {
            request.reportError(sCtx, exception);
        }
        catch (final Throwable throwable)
        {
            request.reportError(sCtx, throwable);
        }
        return request;
    }

}
