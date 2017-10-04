/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.web.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.bundle.BundleFee;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.account.AccountExtensionHolder;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtensionHome;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.SubscriptionPoolProperty;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveManager;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.language.MessageMgrAware;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class MoveRequestServicer implements RequestServicer
{   
    public static final String MSG_KEY_VALIDATE_BUTTON_NAME                      = "MoveRequest.ValidateButtonName";
    public static final String MSG_KEY_MOVE_BUTTON_NAME                          = "MoveRequest.MoveButtonName";   
    public static final String MSG_KEY_MOVE_REQUEST_VALIDATE                     = "MoveRequest.validate";
    public static final String MSG_KEY_MOVE_REQUEST_VALIDATE_WARNINGS            = "MoveRequest.validateWithWarnings";
    public static final String MSG_KEY_MOVE_REQUEST_VALIDATE_NOTHING             = "MoveRequest.validateNothing";
    public static final String MSG_KEY_MOVE_REQUEST_WARNING_HEADER               = "MoveRequest.warningHeader";
    public static final String MSG_KEY_MOVE_REQUEST_WARNING_CONFIRMATION_HEADER  = "MoveRequest.warningConfirmationHeader";
    public static final String MSG_KEY_MOVE_REQUEST_SUCCESS                      = "MoveRequest.success";
    public static final String MSG_KEY_MOVE_REQUEST_FAILURE                      = "MoveRequest.failure";
    public static final String MSG_KEY_DIRTY_MOVE                                = "MoveRequest.dirtyMove";
    public static final String MSG_KEY_INCOMPLETE                                = "MoveRequest.incomplete";
    public static final String MSG_KEY_BUTTON_NAME_CTX_KEY                       = "Move_Key_Button_Name_Ctx_Key";
    
    public static final String PARAM_VALIDATION_CHECKSUM = "checksum";
    public static final String PARAM_MOVE_COMPLETE = "finished";
    
    static Set<String> sessionSet = Collections.synchronizedSet(new HashSet<String>());

    /**
     * @{inheritDoc}
     */
    @Override
    public void service(
            final Context ctx,
            final HttpServletRequest req,
            final HttpServletResponse res)
            throws ServletException, IOException
    {
        final PrintWriter out = res.getWriter();
        MoveRequest request = (MoveRequest) ctx.get(MoveRequest.class);
        if (request == null)
        {
            out.println("<table><tr><td align=\"center\"><b style=\"color:red;\">");
            out.print("DEVELOPER ERROR: No request initialized.  Use a border to inject a request into the context first!");
            out.println("</b></td></tr></table>");
            return;
        }
        
        final WebControl wc = (WebControl) XBeans.getInstanceOf(ctx, request, WebControl.class);
        if (wc == null)
        {
            String requestName = "";
            if (request != null)
            {
                requestName = request.getClass().getSimpleName();
            }            
            out.println("<table><tr><td align=\"center\"><b style=\"color:red;\">");
            out.print("The request of type '" + requestName + "' can't be performed through the GUI.");
            out.println("</b></td></tr></table>");
            return;
        }
        
        HttpSession session = req.getSession();

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

        wc.fromWeb(sCtx, request, req, "");
        
        // Setting bundles start time to midnight to avoid problem with check-sum.
        if (request instanceof ConvertAccountGroupTypeRequest)
        {
            Collection<Extension> extensions = ((ConvertAccountGroupTypeRequest) request).getExtensions();
            String ban = ((ConvertAccountGroupTypeRequest)request).getExistingBAN();
            for (Extension extension : extensions)
            {
                if (extension instanceof PoolExtension)
                {
                    for (BundleFee fee : (Collection<BundleFee>) ((PoolExtension) extension).getPoolBundles().values())
                    {
                        fee.setStartDate(CalendarSupportHelper.get(sCtx).getDateWithNoTimeOfDay(fee.getStartDate()));
                    }
                    
                    for (SubscriptionPoolProperty subscriptionPoolProperty : (Collection<SubscriptionPoolProperty>) ((PoolExtension)extension).getSubscriptionPoolProperties().values())
                    {
                        try
                        {
                            final Home home = (Home) ctx.get(AccountHome.class);
                            final Account account = (Account) home.find(ctx, ban);
                            if (account != null && (SubscriberTypeEnum.HYBRID.equals(account.getSystemType()) || 
                                    SubscriberTypeEnum.POSTPAID.equals(account.getSystemType())) && subscriptionPoolProperty.getInitialPoolBalance() <= 0)
                            {
                                String engMsg = "Hybrid or Postpaid subscriber cannot have zero credit limit";
                                new MinorLogMsg(this, engMsg, null).log(sCtx);
                                throw new HomeException(engMsg);
                            }
                            
                            And accountSelectAllPredicate = new And();
                            
                            // To fetch all the child account with that particular ban and is in active state 
                            accountSelectAllPredicate.add(new EQ(AccountXInfo.PARENT_BAN, ban));
                            accountSelectAllPredicate.add(new EQ(AccountXInfo.STATE, AccountStateEnum.ACTIVE));
                            
                            Collection<Account> accounts = home.where(ctx, accountSelectAllPredicate).selectAll();
                            Home sHome = (Home) ctx.get(SubscriberHome.class);
                            Or selectAllChildAccountPredicate = new Or();
                            selectAllChildAccountPredicate.add(new EQ(SubscriberXInfo.BAN, ban));
                            for (Account parentAccount : accounts)
                            {
                                selectAllChildAccountPredicate.add(new EQ(SubscriberXInfo.BAN, parentAccount.getBAN()));
                            }
                            Collection<Subscriber> subs = sHome.where(ctx, selectAllChildAccountPredicate).selectAll();
                            for (Subscriber subscriber : subs)
                            {
                                if (account != null
                                        && (SubscriberTypeEnum.HYBRID.equals(account.getSystemType()) || SubscriberTypeEnum.POSTPAID
                                                .equals(account.getSystemType()))
                                        && subscriptionPoolProperty.getInitialPoolBalance() < subscriber
                                                .getMonthlySpendLimit())
                                {
                                    String engMsg = "Hybrid or Postpaid subscriber cannot have credit limit less then monthly spend limit";
                                    new MinorLogMsg(this, engMsg, null).log(sCtx);
                                    throw new HomeException(engMsg);
                                }
                                
                                if (subscriber.getMonthlySpendLimit() == -1)
                                {
                                    Home ccHome = (Home) ctx.get(CreditCategoryHome.class);
                                    
                                    And predicate = new And();
                                    predicate.add(new EQ(CreditCategoryXInfo.CODE, account.getCreditCategory()));
                                    predicate.add(new EQ(CreditCategoryXInfo.SPID, account.getSpid()));
                                    CreditCategory creditCategory = (CreditCategory) ccHome.find(ctx, predicate);
                                    if (account != null
                                            && (SubscriberTypeEnum.HYBRID.equals(account.getSystemType()) || SubscriberTypeEnum.POSTPAID
                                                    .equals(account.getSystemType()))
                                            && subscriptionPoolProperty.getInitialPoolBalance() < creditCategory
                                                    .getMonthlySpendLimit())
                                    {
                                        String errMsg = "Hybrid or POSTPAID Subscriber cannot have Monthly Spend Limit more than Credit Limit";
                                        new MinorLogMsg(this, errMsg, null).log(sCtx);
                                        throw new HomeException(errMsg);
                                    }
                                }
                            }
                        }                       
                        catch (HomeException exception)
                        {
                                request.reportError(sCtx, exception);
                        }
                    }
                }
            }
            
        }

        String oldChecksum = req.getParameter(PARAM_VALIDATION_CHECKSUM);
        String newChecksum = String.valueOf(request.hashCode());
        String activeChecksum = oldChecksum;
        boolean isDirty = !SafetyUtil.safeEquals(oldChecksum, newChecksum);
        boolean isComplete = !isDirty && (req.getParameter(PARAM_MOVE_COMPLETE) != null);
        
        String msg = "";
        String validateButtonText = manager.get(MSG_KEY_VALIDATE_BUTTON_NAME, "Validate");
        String typeOfRequest = (String)ctx.get(MSG_KEY_BUTTON_NAME_CTX_KEY, MSG_KEY_MOVE_BUTTON_NAME);
        String moveButtonText = manager.get(typeOfRequest, "Move");
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
                
                MoveManager moveMgr = new MoveManager();
                sCtx.put(MoveManager.class, moveMgr);
                
                if (buttonRenderer.isButton(sCtx, validateButtonText))
                {
                    if (!isComplete)
                    {
                        moveMgr.validate(sCtx, request);
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
                            request = moveMgr.move(sCtx, request);
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
                
                if (buttonRenderer.isButton(sCtx, "Preview"))
                {
                	wc.fromWeb(sCtx, request, req, "");
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

        if (isComplete)
        {
            setPricePlan(sCtx, request);
            sCtx.put("MODE", OutputWebControl.DISPLAY_MODE);   
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
        
        wc.toWeb(sCtx, out, "", request);

        out.print("</td></tr><tr><th align=\"right\">");

        if (!isComplete)
        {
            buttonRenderer.inputButton(out, sCtx, this.getClass(), validateButtonText, false);   
        }
        if (!exceptions.hasErrors()
                && !isComplete
                && !isDirty)
        {
            buttonRenderer.inputButton(out, sCtx, this.getClass(), moveButtonText, false, onMoveClick);   
        }
        
        buttonRenderer.inputButton(out, sCtx, this.getClass(), "Preview", false);
        
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
    
    private void setPricePlan(Context ctx, MoveRequest request)
    {
        try
        {
            if (request instanceof AccountMoveRequest)
            {
                final Home groupPricePlanHome = (Home) ctx.get(GroupPricePlanExtensionHome.class);
                GroupPricePlanExtension groupPricePlanExtension = (GroupPricePlanExtension) groupPricePlanHome.find(
                        ctx, ((AccountMoveRequest) request).getNewParentBAN());
                Account newAccount = ((AccountMoveRequest) request).getNewAccount(ctx);
                
                Account oldAccount = ((AccountMoveRequest) request).getOldAccount(ctx);
                final Home subHome = (Home) ctx.get(SubscriberHome.class);
                Subscriber sub = newAccount.getIndividualSubscriber(ctx);
                Subscriber oldsub = oldAccount.getIndividualSubscriber(ctx);
                long pricePlanId = 0;
                if (groupPricePlanExtension != null)
                {
                    pricePlanId = groupPricePlanExtension.getGroupPricePlan(sub);
                }
                if (pricePlanId > 0 && oldsub.getPricePlan() != pricePlanId)
                {
                    sub.switchPricePlan(ctx, pricePlanId);
                    subHome.store(ctx, sub);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}