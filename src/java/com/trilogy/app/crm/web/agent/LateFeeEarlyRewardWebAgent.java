/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.agent;

import java.io.PrintWriter;
import java.util.Date;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardForm;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardFormWebControl;
import com.trilogy.app.crm.paymentprocessing.InvoicePaymentProcessingAgent;
import com.trilogy.app.crm.paymentprocessing.LateFeeAgent;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.SpidSupport;

/**
 * @author cindy.wong@redknee.com
 * @since 8.4
 */
public class LateFeeEarlyRewardWebAgent extends ServletBridge implements WebAgent
{
    /**
     * Module name.
     */
    public static final String MODULE = "RunLateFeeEarlyReward";

    /**
     * Web control for Auto Deposit Release Form.
     */
    private final LateFeeEarlyRewardFormWebControl webControl_ = new LateFeeEarlyRewardFormWebControl();

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Context context) throws AgentException
    {
        final PrintWriter out = getWriter(context);
        final LateFeeEarlyRewardForm form = new LateFeeEarlyRewardForm();
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

            final Date invoiceDueDate = form.getInvoiceDueDate();
            final int spid = form.getServiceProvider();
			final String ban = form.getBAN();

            try
            {
                SpidSupport.getCRMSpid(context, spid);
            }
            catch (final HomeException exception)
            {
                throw new AgentException("Cannot retrieve SPID", exception);
            }

			Account account = null;
			try
			{
				account = AccountSupport.getAccount(context, ban);
			}
			catch (HomeException exception)
			{
				throw new AgentException("Fail to retrieve account " + ban,
				    exception);
			}

			if (account == null)
			{
				throw new AgentException("Cannot find account " + ban);
			}
			else if (!account.isResponsible())
			{
				throw new AgentException(
				    "Cannot generate late fee for non-responsible account "
				        + ban);
			}

            subContext.put(InvoicePaymentProcessingAgent.BILLING_DATE, invoiceDueDate);
            subContext.put(CRMSpidHome.class, ((Home) context.get(CRMSpidHome.class)).where(context, new EQ(
                    CRMSpidXInfo.ID, spid)));
			subContext.put(BillCycleHome.class, ((Home) context
			    .get(BillCycleHome.class)).where(context, new EQ(
			    BillCycleXInfo.SPID, spid)));
			subContext.put(InvoicePaymentProcessingAgent.ACCOUNT, account);
            final LateFeeAgent agent = new LateFeeAgent();
            agent.execute(subContext);

        }

        final FormRenderer renderer = (FormRenderer) context.get(FormRenderer.class, DefaultFormRenderer.instance());
        renderer.Form(out, context);

        if (listener.hasErrors())
        {
            listener.toWeb(subContext, out, "", form);
        }
        else if (displayMessage)
        {
            out.print("<pre><center><font size=\"1\" face=\"Verdana\" color=\"green\"><b>");
			out.println("Late Fee generation started.</b></font></center></pre>");
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
