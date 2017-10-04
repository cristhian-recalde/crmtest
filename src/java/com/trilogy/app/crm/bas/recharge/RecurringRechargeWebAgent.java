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

package com.trilogy.app.crm.bas.recharge;

import java.io.PrintWriter;
import java.util.Date;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;

import com.trilogy.app.crm.bean.RecurringRechargeForm;
import com.trilogy.app.crm.bean.RecurringRechargeFormWebControl;
import com.trilogy.app.crm.bean.RecurringRechargeScopeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.support.RecurringRechargeSupport;


/**
 * Web agent to run recurring recharge.
 *
 * @author cindy.wong@redknee.com
 * @since 28-Apr-08
 */
public class RecurringRechargeWebAgent extends ServletBridge implements WebAgent
{

    /**
     * Module name.
     */
    public static final String MODULE = "RunRecurringRecharge";

    /**
     * Web control for Recurring Recharge Form.
     */
    private final RecurringRechargeFormWebControl webControl_ = new RecurringRechargeFormWebControl();


    /**
     * {@inheritDoc}
     */
    public void execute(final Context context) throws AgentException
    {
        final PrintWriter out = getWriter(context);
        final RecurringRechargeForm form = new RecurringRechargeForm();
        final Context subContext = context.createSubContext();
        final MessageMgr messageManager = new MessageMgr(subContext, MODULE);
        final HTMLExceptionListener listener = new HTMLExceptionListener(messageManager);
        final ButtonRenderer buttonRenderer =
            (ButtonRenderer) context.get(
                    ButtonRenderer.class,
                    DefaultButtonRenderer.instance());

        subContext.put(ExceptionListener.class, listener);
        boolean displayMessage = false;

        subContext.put("MODE", OutputWebControl.EDIT_MODE);

        if (buttonRenderer.isButton(context, RUN_BUTTON))
        {
            displayMessage = true;
            this.webControl_.fromWeb(subContext, form, getRequest(context), "");

            try
            {
                final Date billingDate = form.getBillingDate();
                RecurringRechargeScopeEnum scope = form.getScope();
                final ChargingCycleEnum chargingCycle = form.getChargingCycle();

                if (scope == null)
                {
                    scope = RecurringRechargeScopeEnum.Account;
                }

                displayMessage = true;
                switch (scope.getIndex())
                {
                    case RecurringRechargeScopeEnum.Subscriber_INDEX:
                    {
                        RecurringRechargeSupport.applySubscriberRecurringRecharge(subContext, form
                            .getSubscriberIdentifier(), chargingCycle, billingDate);
                        break;
                    }
                    case RecurringRechargeScopeEnum.Account_INDEX:
                    {
                        RecurringRechargeSupport.applyAccountRecurringRecharge(subContext, form.getAccountIdentifier(),
                                chargingCycle, billingDate);
                        break;
                    }
                    case RecurringRechargeScopeEnum.BillingCycle_INDEX:
                    {
                        RecurringRechargeSupport.applyBillCycleRecurringRecharge(subContext, form.getBillCycleID(),
                                chargingCycle, billingDate);
                        break;
                    }
                    case RecurringRechargeScopeEnum.ServiceProvider_INDEX:
                    {
                        RecurringRechargeSupport.applySpidRecurringRecharge(subContext, form.getSpid(), chargingCycle,
                            billingDate);
                        break;
                    }
                    default:
                        displayMessage = false;
                }
            }
            catch (final Throwable throwable)
            {
                listener.thrown(throwable);
            }

        }
        else if (buttonRenderer.isButton(context, PREVIEW_BUTTON))
        {
            this.webControl_.fromWeb(subContext, form, getRequest(context), "");
        }

        final FormRenderer renderer = (FormRenderer) context.get(FormRenderer.class, DefaultFormRenderer.instance());
        renderer.Form(out, context);

        if (listener.hasErrors())
        {
            listener.toWeb(subContext, out, "", form);
        }
        else if (displayMessage)
        {
            out.print("<pre><center><font size=\"1\" face=\"Verdana\" color=\"green\">");
            out.print("<b>Recurring recharge started.</b></font></center></pre>");
        }

        out.println("<table><tr><td>");
        this.webControl_.toWeb(subContext, out, "", form);
        out.println("</td></tr></table>");
        buttonRenderer.inputButton(out, context, this.getClass(), PREVIEW_BUTTON, false);   
        buttonRenderer.inputButton(out, context, this.getClass(), RUN_BUTTON, false);   
        buttonRenderer.inputButton(out, context, this.getClass(), RESET_BUTTON, false);   
        renderer.FormEnd(out);
        out.println("<br />");
    }
    
    public static final String RUN_BUTTON  = "Run";
    public static final String PREVIEW_BUTTON  = "Preview";
    public static final String RESET_BUTTON  = "Reset";


}
