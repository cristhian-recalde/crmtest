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

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.RegistrationStatusEnum;
import com.trilogy.app.crm.filter.AccountRegistrationPendingPredicate;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * Register action sets the registration state of an account to registered, then updates it.
 * 
 * It is the home pipeline's job to validate the registration state change.
 * 
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class RegisterAccountAction extends SimpleWebAction
{
    private static final long serialVersionUID = 1L;
    
    protected static final String REGISTRATION_SUCCESS_MSG_KEY = "Account.RegistrationSuccess";
    protected static final String REGISTRATION_FAIL_MSG_KEY = "Account.RegistrationFail";

    public RegisterAccountAction()
    {
        super("registerAccount", "Register");
    }
    
    
    @Override
    public boolean isEnabled(Context ctx, Object bean)
    {
        return new AccountRegistrationPendingPredicate().f(ctx, bean);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLinkDetail(Context ctx, PrintWriter out, Object bean, Link link)
    {
        // Output key as CMD so that WebController will execute this action
        out.println("<input type=\"hidden\" name=\"CMD\" value=\""+getKey().replaceAll("\"", "&quot;")+"\" />");
        
        // Output the form input button.  This allows the CSR to simultaneously populate registration fields
        // and register the account.
        ButtonRenderer br = (ButtonRenderer)ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
        br.inputButton(out, ctx, this.getClass(), getLabel(), false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Link modifyLink(Context ctx, Object bean, Link link)
    {
        // Output key as CMD so that WebController will execute this action
        link.add("CMD", getKey());
        return link;
    }


    @Override
    public void execute(Context ctx) throws AgentException
    {
        // WebController puts the updated bean in the context under key "bean" prior to calling action's execute() method.
        Account account  = (Account) ctx.get("bean");
        MessageMgr mmgr = new MessageMgr(ctx, this);
        PrintWriter out = WebAgents.getWriter(ctx);
        
        try
        {
            final String ban;
            if (account == null)
            {
                ban = WebAgents.getParameter(ctx, "key");
                account = AccountSupport.getAccount(ctx, ban);
            }
            else
            {
                ban = account.getBAN();
            }
            if (account != null)
            {
                account.setRegistrationStatus(RegistrationStatusEnum.REGISTERED);
                HomeSupportHelper.get(ctx).storeBean(ctx, account);
                outputSuccessMessage(mmgr, out);
            }
        }
        catch (Exception e)
        {
            outputFailureMessage(mmgr, out);
            if (e.getCause() instanceof IllegalStateException)
            {
                handleValidationError(ctx, mmgr, out, e.getCause());
            }
            else
            {
                handleValidationError(ctx, mmgr, out, e);
            }
        }
        
        out.print("<br/>");
        
        ContextAgents.doReturn(ctx);
    }


    protected void outputSuccessMessage(MessageMgr mmgr, PrintWriter out)
    {
        out.print("<b>");
        out.println(mmgr.get(REGISTRATION_SUCCESS_MSG_KEY, "Account registered successfully."));
        out.print("</b>");
    }


    protected void outputFailureMessage(MessageMgr mmgr, PrintWriter out)
    {
        out.print("<b>");
        out.print("<font color=\"red\">");
        out.print(mmgr.get(REGISTRATION_FAIL_MSG_KEY, "Account registration failed!"));
        out.print("</font>");
        out.print("</b>");
    }


    protected void handleValidationError(Context ctx, MessageMgr mmgr, PrintWriter out, Throwable t)
    {
        ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (!(el instanceof HTMLExceptionListener))
        {
            el = (ExceptionListener) ctx.get(HTMLExceptionListener.class);
        }
        if (el != null)
        {
            el.thrown(t);
        }
        if (!(el instanceof HTMLExceptionListener))
        {
            HTMLExceptionListener hel = new HTMLExceptionListener(mmgr);
            hel.thrown(t);
            hel.toWeb(ctx, out, "", null);
        }
    }
}