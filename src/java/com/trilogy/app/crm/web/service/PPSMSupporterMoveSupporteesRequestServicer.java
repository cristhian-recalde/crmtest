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
import java.util.HashSet;
import java.util.Set;

//import javax.jain.InvalidArgumentException;
import java.lang.IllegalArgumentException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.language.MessageMgrAware;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterMoveRequest;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterMoveRequestWebControl;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtensionHome;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * Request servicer used to move supportee subscribers to a new supporter.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class PPSMSupporterMoveSupporteesRequestServicer implements RequestServicer
{
    public static final String MSG_KEY_BACK_BUTTON_NAME                          = "PPSMSupporterMoveRequest.BackButtonName";
    public static final String MSG_KEY_VALIDATE_BUTTON_NAME                      = "PPSMSupporterMoveRequest.ValidateButtonName";
    public static final String MSG_KEY_MOVE_BUTTON_NAME                          = "PPSMSupporterMoveRequest.MoveButtonName";   
    public static final String MSG_KEY_MOVE_REQUEST_VALIDATE                     = "PPSMSupporterMoveRequest.validate";
    public static final String MSG_KEY_MOVE_REQUEST_VALIDATE_WARNINGS            = "PPSMSupporterMoveRequest.validateWithWarnings";
    public static final String MSG_KEY_MOVE_REQUEST_VALIDATE_NOTHING             = "PPSMSupporterMoveRequest.validateNothing";
    public static final String MSG_KEY_MOVE_REQUEST_WARNING_HEADER               = "PPSMSupporterMoveRequest.warningHeader";
    public static final String MSG_KEY_MOVE_REQUEST_WARNING_CONFIRMATION_HEADER  = "PPSMSupporterMoveRequest.warningConfirmationHeader";
    public static final String MSG_KEY_MOVE_REQUEST_SUCCESS                      = "PPSMSupporterMoveRequest.success";
    public static final String MSG_KEY_MOVE_REQUEST_FAILURE                      = "PPSMSupporterMoveRequest.failure";
    public static final String MSG_KEY_DIRTY_MOVE                                = "PPSMSupporterMoveRequest.dirtyMove";
    public static final String MSG_KEY_INCOMPLETE                                = "PPSMSupporterMoveRequest.incomplete";
    public static final String MSG_KEY_BUTTON_NAME_CTX_KEY                       = "PPSMSupporterMove_Key_Button_Name_Ctx_Key";
    
    public static final String PARAM_VALIDATION_CHECKSUM = "checksum";
    public static final String PARAM_MOVE_COMPLETE = "finished";
    
    static Set<String> sessionSet = Collections.synchronizedSet(new HashSet<String>());
    
    private WebControl webControl = new PPSMSupporterMoveRequestWebControl();
    public final void service(final Context ctx, final HttpServletRequest req,
            final HttpServletResponse res) throws IOException
    {
        final PrintWriter out = res.getWriter();

        PPSMSupporterMoveRequest request = new PPSMSupporterMoveRequest();

        HttpSession session = req.getSession();
        if (WebAgents.getParameter(ctx, "oldSubscriptionId")!=null)
        {
            request.setOldSubscriptionId(WebAgents.getParameter(ctx, "oldSubscriptionId"));
        }
        final Context sCtx = ctx.createSubContext();
        
        final ButtonRenderer buttonRenderer =
                (ButtonRenderer) sCtx.get(
                        ButtonRenderer.class,
                        DefaultButtonRenderer.instance());

        MessageMgr manager = new MessageMgr(sCtx, this);

        HTMLExceptionListener exceptions = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
        if (exceptions == null)
        {
            exceptions = new HTMLExceptionListener(manager);
            sCtx.put(HTMLExceptionListener.class, exceptions);
        }
        sCtx.put(ExceptionListener.class, exceptions);

        webControl.fromWeb(sCtx, request, req, "");

        String oldChecksum = req.getParameter(PARAM_VALIDATION_CHECKSUM);
        String newChecksum = String.valueOf(request.hashCode());
        String activeChecksum = oldChecksum;
        boolean isDirty = !SafetyUtil.safeEquals(oldChecksum, newChecksum);
        boolean isComplete = !isDirty && (req.getParameter(PARAM_MOVE_COMPLETE) != null);
        boolean isValidated = false;
        
        String msg = "";
        String backButtonText = manager.get(MSG_KEY_BACK_BUTTON_NAME, " Back ");
        String validateButtonText = manager.get(MSG_KEY_VALIDATE_BUTTON_NAME, "Validate");
        String typeOfRequest = (String)ctx.get(MSG_KEY_BUTTON_NAME_CTX_KEY, MSG_KEY_MOVE_BUTTON_NAME);
        String moveButtonText = manager.get(typeOfRequest, "Change PPSM supporter");
        if (!exceptions.hasErrors()
                && !request.hasErrors(sCtx))
        {
            try
            {   
                if (!sessionSet.add(session.getId()))
                {
                    String engMsg = "The previous request isn't complete yet, please try again later.";
                    new MinorLogMsg(this, engMsg, null).log(sCtx);
                    throw new HomeException(manager.get(MSG_KEY_INCOMPLETE, engMsg));
                }
                
                if (buttonRenderer.isButton(sCtx, validateButtonText))
                {
                    if (!isComplete)
                    {
                        validate(sCtx, request);
                        newChecksum = String.valueOf(request.hashCode());
                        if (!request.hasErrors(sCtx))
                        {
                            if (request.hasWarnings(sCtx))
                            {
                                msg = manager.get(MSG_KEY_MOVE_REQUEST_VALIDATE_WARNINGS, 
                                        "The request is OK (with warnings).  Click \"{0}\" to execute the request.",
                                        new String[] {moveButtonText});                        
                            }
                            else
                            {
                                msg = manager.get(MSG_KEY_MOVE_REQUEST_VALIDATE, 
                                        "The request is OK.  Click \"{0}\" to execute the request.",
                                        new String[] {moveButtonText});
                            }
                            isValidated = true;
                        }
                        activeChecksum = newChecksum;
                        isDirty = false;
                    }
                    else
                    {
                        msg = manager.get(MSG_KEY_MOVE_REQUEST_VALIDATE_NOTHING, 
                                "The request already executed successfully.  Nothing to validate.");
                    }
                }
                else if (buttonRenderer.isButton(sCtx, moveButtonText))
                {
                    if (!isDirty)
                    {
                        if (!isComplete)
                        {
                            move(sCtx, request);
                            isComplete = true;
                        }
                        
                        // EMPTY exception listener after copying to request.reportWarning()
                        msg = request.getSuccessMessage(sCtx); 
                    }
                    else
                    {
                        request.reportError(sCtx,  new MoveException( request, 
                                manager.get(MSG_KEY_DIRTY_MOVE, 
                                        "The request has been modified.  Please click \"{0}\" again.",
                                        new String[] {validateButtonText})));
                    }
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
            catch (final MoveException exception)
            {
                request.reportError(sCtx, exception);
                msg = manager.get(MSG_KEY_MOVE_REQUEST_FAILURE, 
                        "The operation failed for request: {0}",
                        new String[] {request.toString()});
                new MajorLogMsg(this, "Problem occured during the operation.", exception).log(sCtx);
            }
            catch (final Throwable throwable)
            {
                request.reportError(sCtx, throwable);
                new MajorLogMsg(this, "Unexpected problem occured during the operation.", throwable).log(sCtx);
            }
            finally
            {
                extractWarningsFromExceptionListener(ctx, request, exceptions);
                sessionSet.remove(session.getId());
            }
        }

        if (isComplete || isValidated)
        {
            sCtx.put("MODE", OutputWebControl.DISPLAY_MODE);   
            sCtx.put("PPSMSupporterMoveRequest.newMSISDN.mode", ViewModeEnum.READ_ONLY);
        }
        else
        {
            sCtx.put("MODE", OutputWebControl.EDIT_MODE);
        }

        final FormRenderer formRenderer =
                (FormRenderer) sCtx.get(FormRenderer.class, DefaultFormRenderer.instance());

        formRenderer.Form(out, sCtx);

        out.print("<table>");
        if (msg != null && msg.trim().length() > 0)
        {
            boolean showRedText = exceptions.hasErrors()
                || request.hasErrors(sCtx);
            
            out.println("<tr><td>&nbsp;</td></tr><tr><td align=\"center\"><b style=\"color:" + (showRedText ? "red" : "green") + ";\">");
            out.print(msg);
            out.println("</b></td></tr><tr><td>&nbsp;</td></tr>");
        }

        out.print("<tr><td>");

        if (exceptions.hasErrors()
                || request.hasErrors(sCtx))
        {   
            for (Throwable error : request.getErrors(sCtx))
            {
                exceptions.thrown(error);
            }
            exceptions.toWeb(sCtx, out, "", request);
            out.print("</td></tr><tr><td>&nbsp;</td></tr><tr><td>");
        }

        String onMoveClick = null;
        if (request.hasWarnings(sCtx))
        {
            out.println("<b>");
            out.print(manager.get(MSG_KEY_MOVE_REQUEST_WARNING_HEADER, "Warnings:"));
            out.println("</b></td></tr><tr><td>");

            StringBuilder warningMsg = new StringBuilder(manager.get(
                    MSG_KEY_MOVE_REQUEST_WARNING_CONFIRMATION_HEADER, 
                    "Please acknowledge that you have read, understand, and accept the following warnings before proceeding with this operation:"));
            
            warningMsg.append("\\n\\n");
            
            HTMLExceptionListener warningListener = new HTMLExceptionListener(manager);
            Set<MoveWarningException> warnings = request.getWarnings(sCtx);
            for (MoveWarningException warning : warnings)
            {
                warningMsg.append("- ").append(warning.getMessage(false)).append("\\n");
                warningListener.thrown(warning);
            }
            warningListener.toWeb(sCtx, out, "", request);
            out.print("</td></tr><tr><td>&nbsp;</td></tr><tr><td>");
            
            onMoveClick = "try{return confirm('" + warningMsg + "');}catch(everything){}";
        }
        
        webControl.toWeb(sCtx, out, "", request);

        out.print("</td></tr><tr><th align=\"right\">");

        if (!isComplete)
        {
            if (!isValidated)
            {
                buttonRenderer.inputButton(out, sCtx, this.getClass(), validateButtonText, false);   
            }
            else
            {
                buttonRenderer.inputButton(out, sCtx, this.getClass(), backButtonText, false);
            }
        }
        
        
        
        if (!exceptions.hasErrors()
                && !isComplete
                && !isDirty
                && isValidated)
        {
            buttonRenderer.inputButton(out, sCtx, this.getClass(), moveButtonText, false, onMoveClick);   
        }
        
        BeanWebController.outputHelpLink(sCtx, req, res, out, buttonRenderer);

        out.print("</th></tr></table>");
        
        if (activeChecksum != null)
        {
            out.print("<input type=\"hidden\" name=\"" + MoveRequestServicer.PARAM_VALIDATION_CHECKSUM + "\" value=\"" + activeChecksum + "\" />");
        }
        
        if (isComplete)
        {
            out.print("<input type=\"hidden\" name=\"" + MoveRequestServicer.PARAM_MOVE_COMPLETE + "\" value=\"X\" />");
        }

        formRenderer.FormEnd(out);        


        
       // redirect(context, ogirinalCmd, oldSupporterID);
    }
    
    public static void move(Context ctx, PPSMSupporterMoveRequest request) throws HomeException, MoveException
    {
        PPSMSupporterSubExtension oldExtension;
        PPSMSupporterSubExtension newExtension;
        
        try
        {
            oldExtension = PPSMSupporterSubExtension.getPPSMSupporterSubscriberExtension(ctx, request.getOldSubscriptionId());
            oldExtension.setContext(ctx);
            newExtension = PPSMSupporterSubExtension.getPPSMSupporterSubscriberExtension(ctx, request.getNewSubscriptionId());
            newExtension.setContext(ctx);
        }
        catch (HomeException e)
        {
            throw new MoveException(request, "Unable to retrieve PPSM Supporter Subscriber Extension profiles: " + e.getMessage(), e);
        }
        
        for (String chargingTemplate: oldExtension.getChargingTemplates(ctx))
        {
            if (!newExtension.getChargingTemplates(ctx).contains(chargingTemplate))
            {
                newExtension.getChargingTemplates(ctx).add(chargingTemplate);
            }
        }
        
        for (String screeningTemplate: oldExtension.getScreeningTemplates(ctx))
        {
            if (!newExtension.getScreeningTemplates(ctx).contains(screeningTemplate))
            {
                newExtension.getScreeningTemplates(ctx).add(screeningTemplate);
            }
        }
        
        try
        {
            Home supporterHome = (Home) ctx.get(PPSMSupporterSubExtensionHome.class);
            supporterHome.store(ctx, newExtension);
        }
        catch (HomeException e)
        {
            String msg = "Unable to add templates to new PPSM Supporter. No supported subscriptions were moved.";
            LogSupport.minor(ctx, PPSMSupporterMoveSupporteesRequestServicer.class, msg + ": " + e.getMessage(), e);
            throw new MoveException(request, msg, e);
        }
        
        Home supporteeHome = (Home) ctx.get(PPSMSupporteeSubExtensionHome.class);
        
        for (PPSMSupporteeSubExtension supporteeExtension : oldExtension.getSupportedSubscribers())
        {
            try
            {
                supporteeExtension.setSupportMSISDN(request.getNewMSISDN());
                supporteeHome.store(ctx, supporteeExtension);
            }
            catch (HomeException e)
            {
                String msg = "Unable to change PPSM Supporter for subscription '" + supporteeExtension.getSubId() + "'";
                LogSupport.minor(ctx, PPSMSupporterMoveSupporteesRequestServicer.class, msg + ": " + e.getMessage(), e);
                throw new MoveException(request, msg, e);
            }
        }
        
        Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
        if (subscriber!=null)
        {
            subscriber.setSubExtensions(null);
        }

    }

    public static void validate(Context ctx, PPSMSupporterMoveRequest request)
    {
        try
        {
            Subscriber newSupporter = SubscriberSupport.lookupSubscriberForMSISDN(ctx, request.getNewMSISDN());
            Subscriber oldSupporter = SubscriberSupport.getSubscriber(ctx, request.getOldSubscriptionId());
            if (newSupporter == null)
            {
                request.reportError(ctx, new IllegalArgumentException("Subscription with MSISDN='" + request.getNewMSISDN() + "' is invalid."));
            }
            else if (newSupporter.getId().equals(request.getOldSubscriptionId()))
            {
                request.reportError(ctx, new IllegalArgumentException("Subscription with MSISDN='" + request.getNewMSISDN() + "' (ID='" +  newSupporter.getId() + "') is the current supporter."));
            }
            else if (newSupporter.getSpid()!=oldSupporter.getSpid())
            {
                request.reportError(ctx, new IllegalArgumentException("Subscription with MSISDN='" + request.getNewMSISDN() + "' (ID='" +  newSupporter.getId() + "') is in a different SPID."));
            }
            else
            {
                PPSMSupporterSubExtension extension = PPSMSupporterSubExtension.getPPSMSupporterSubscriberExtension(ctx, newSupporter.getId());
                request.setNewSubscriptionId(newSupporter.getId());
                request.setNewSubscriptionBAN(newSupporter.getBAN());
                if (extension == null)
                {
                    request.reportError(ctx, new IllegalArgumentException("Subscription with MSISDN='" + request.getNewMSISDN() + "' (ID='" +  newSupporter.getId() + "') is not a PPSM Supporter."));
                }
                else if (!extension.isEnabled())
                {
                    request.reportWarning(ctx, new MoveWarningException(request, "Subscription with MSISDN='" + request.getNewMSISDN() + "' (ID='" +  newSupporter.getId() + "') is a PPSM Supporter but support is currently not enabled."));
                }
            }
        }
        catch (HomeException e)
        {
            request.reportError(ctx, new HomeException("Unexpected error while retrieving subscription with MSISDN='" + request.getNewMSISDN() + "': " + e.getMessage(), e));
        }
        
    }
    
    private void extractWarningsFromExceptionListener(final Context ctx, MoveRequest request,
            HTMLExceptionListener exceptions)
    {
        if (exceptions != null && exceptions.getExceptions() != null)
        {
            for (Object exception : exceptions.getExceptions())
            {
                Throwable throwable = (Throwable) exception;
                final String msg;
                if (throwable instanceof IllegalPropertyArgumentException)
                {
                    IllegalPropertyArgumentException ipae = (IllegalPropertyArgumentException)throwable;
                    PropertyInfo propertyInfo = ipae.getPropertyInfo();
                    
                    final String messageText;
                    MessageMgrAware mmgrAware = (MessageMgrAware)XBeans.getInstanceOf(ctx, ipae, MessageMgrAware.class);
                    if (mmgrAware != null)
                    {
                        messageText = mmgrAware.toString(ctx, new MessageMgr(ctx, this));
                    }
                    else
                    {
                        messageText = ipae.getMessageText();
                    }
                    
                    if (propertyInfo != null)
                    {
                        msg = "Error with property "
                            + propertyInfo.getBeanClass().getSimpleName() + "." + propertyInfo.getName()
                            + ": " + messageText;
                    }
                    else
                    {
                        msg = messageText;
                    }
                }
                else
                {
                    msg = throwable.getMessage();
                }
                request.reportWarning(ctx, new MoveWarningException(request, msg, throwable));
            }
            exceptions.clear();
        }
    }    
    

}
