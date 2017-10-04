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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.menu.XMenu;
import com.trilogy.framework.xhome.menu.XMenuHome;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.calldetail.PrepaidCallingCardWebControl;
import com.trilogy.app.crm.bean.core.PrepaidCallingCard;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.search.PrepaidCallingCardRequest;
import com.trilogy.app.crm.bean.search.PrepaidCallingCardRequestWebControl;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.support.PricePlanSupport;


public class PrepaidCallingCardRequestServicer implements RequestServicer
{

    /**
     * Creates a new MoveSubscriberRequestServicer.
     */
    public PrepaidCallingCardRequestServicer()
    {
        webControl_ = new PrepaidCallingCardRequestWebControl();
        callingCardWebControl_ = new PrepaidCallingCardWebControl();
    }


    /**
     * {@inheritDoc}
     */
    public final void service(final Context context, final HttpServletRequest request,
            final HttpServletResponse response) throws IOException
    {
        final HttpSession session = request.getSession();
        final PrintWriter out = response.getWriter();
        final PrepaidCallingCardRequest form = new PrepaidCallingCardRequest();
        final Context subContext = context.createSubContext();

        String msg = null;
        boolean msgIsProblemReport = false;

        final MessageMgr manager = new MessageMgr(subContext, this);
        final HTMLExceptionListener exceptions = new HTMLExceptionListener(manager);
        final ButtonRenderer buttonRenderer = (ButtonRenderer) subContext.get(ButtonRenderer.class,
                DefaultButtonRenderer.instance());
        subContext.put(ExceptionListener.class, exceptions);
        subContext.put(HTMLExceptionListener.class, exceptions);
        PrepaidCallingCard prepaidCallingCard = null;
        webControl_.fromWeb(subContext, form, request, "");
        GeneralConfig config = (GeneralConfig) subContext.get(GeneralConfig.class);

        if (form.getSerial().length() > 0)
        {
            String subscriberID = config.getPrepaidCallingCardPrefix() + form.getSerial().trim();

            final SubscriberProfileProvisionClient client = BalanceManagementSupport
                    .getSubscriberProfileProvisionClient(context);

            if (client == null)
            {
                msgIsProblemReport = true;
                msg = "No SubscriberProfileProvisionClient found in context.";

                StringBuilder sb = new StringBuilder();
                sb.append("No SubscriberProfileProvisionClient found in context while retrieving ");
                sb.append("Prepaid Calling Card '");
                sb.append(subscriberID);
                sb.append("'");
                new MajorLogMsg(this.getClass().getName(), sb.toString(), null).log(subContext);
            }
            else
            {
                try
                {
                    if (LogSupport.isEnabled(context, SeverityEnum.INFO))
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Retrieving Prepaid Calling Card '");
                        sb.append(subscriberID);
                        sb.append("'");
                        LogSupport.info(context, this, sb.toString());
                    }

                    prepaidCallingCard = retrievePrepaidCallingCard(subContext, client, subscriberID, form);

                    if (prepaidCallingCard == null)
                    {
                        if (LogSupport.isEnabled(context, SeverityEnum.INFO))
                        {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Prepaid Calling Card '");
                            sb.append(subscriberID);
                            sb.append("' not found.");
                            LogSupport.info(context, this, sb.toString());
                        }
                        msgIsProblemReport = true;
                        msg = "Calling card not found.";
                    }
                    else
                    {
                        if (LogSupport.isEnabled(context, SeverityEnum.INFO))
                        {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Prepaid Calling Card '");
                            sb.append(subscriberID);
                            sb.append("' successfully retrieved.");
                            LogSupport.info(context, this, sb.toString());
                        }
                    }
                }
                catch (Exception exception)
                {
                    exceptions.thrown(exception);
                    msgIsProblemReport = true;
                    msg = "Problem occured during prepaid calling card search.";
                    new MajorLogMsg(this, msg, exception).log(subContext);
                }
            }
        }

        if (prepaidCallingCard == null || msgIsProblemReport)
        {
            // print any exceptions that have occurred
            if (exceptions.hasErrors())
            {
                exceptions.toWeb(subContext, out, "", form);
            }
            displaySearchForm(subContext, out, buttonRenderer, form, msg);
        }
        else
        {
            try
            {
                Home home = (Home) context.get(XMenuHome.class);
                XMenu menu = (XMenu) home.find("appCRMPCCCallDetail");
                if (menu != null)
                {
                    Link link = new Link(context);
                    link.add("cmd", menu.getKey());
                    out.println("<script type=\"text/javascript\"> ");
                    out.print("document.location.href=\"" + link.write() + "\"");
                    out.println("</script>");
                }
            }
            catch (Exception ex)
            {
                String errorMsg = "Error retrieving in redirecting the page to PrepaidCallingCardCallDetails";
                new MajorLogMsg(this, errorMsg, ex).log(context);
            }
        }

    }


    private PrepaidCallingCard retrievePrepaidCallingCard(Context context, SubscriberProfileProvisionClient client,
            String subscriberID, PrepaidCallingCardRequest form)
    throws HomeException, SubscriberProfileProvisionException
    {
        PrepaidCallingCard prepaidCallingCard = null;

        List<Parameters> subscriptions = client.queryAllSubscriptionsForSubscriber(context, subscriberID);
        if (subscriptions != null && subscriptions.size() > 0)
        {
            Parameters subscription = subscriptions.get(0);
            prepaidCallingCard = fillInPrepaidCallingCard(context, client, subscription, form.getSerial());
        }

        return prepaidCallingCard;
    }


    private void redirectToPCCCallDetailPage(Context context, HttpServletResponse response) throws IOException
    {
        try
        {
            Home home = (Home) context.get(XMenuHome.class);
            XMenu menu = (XMenu) home.find("appCRMPCCCallDetail");
            if (menu != null)
            {
                Link link = new Link(context);
                link.add("cmd", menu.getKey());
          //      response.set
                response.sendRedirect(link.write());
            }
        }
        catch (HomeException e)
        {
            String msg = "Error retrieving prepaid calling card call details menu.";
            new MajorLogMsg(this, msg, e).log(context);
        }
    }


    private void displaySearchForm(Context subContext, PrintWriter out, ButtonRenderer buttonRenderer,
            PrepaidCallingCardRequest form, String msg)
    {
        final FormRenderer formRenderer = (FormRenderer) subContext.get(FormRenderer.class,
                DefaultFormRenderer.instance());
        formRenderer.Form(out, subContext);

        displaySearchTable(subContext, out, buttonRenderer, form, webControl_, true, msg);

        formRenderer.FormEnd(out);
    }


    /**
     * Display the search prepaid calling card table.
     * 
     * @param ctx
     * @param out
     * @param buttonRenderer
     * @param form
     * @param webControl
     * @param msg
     */
    public static void displaySearchTable(Context ctx, PrintWriter out, ButtonRenderer buttonRenderer,
            PrepaidCallingCardRequest form, PrepaidCallingCardRequestWebControl webControl, boolean error, String msg)
    {
        ctx.put("MODE", OutputWebControl.EDIT_MODE);
        out.print("<table>");

        if (msg != null)
        {
            if (error)
            {
                out.println("<tr><td align=\"center\"><b style=\"color:red;\">");
            }
            else
            {
                out.println("<tr><td align=\"center\"><b style=\"color:orange;\">");
            }
            out.print(msg);
            out.println("</b></td></tr>");
        }

        out.print("<tr><td>");
        webControl.toWeb(ctx, out, "", form);
        buttonRenderer.inputButton(out, ctx, SEARCH_BUTTON_NAME);
        out.print("</td></tr>");

        out.print("</table>");
    }


    private PrepaidCallingCard fillInPrepaidCallingCard(Context context, SubscriberProfileProvisionClient client,
            Parameters subscription, String subscriberID) throws HomeException, SubscriberProfileProvisionException
    {
        PrepaidCallingCard prepaidCallingCard = new PrepaidCallingCard();
        prepaidCallingCard.setSerial(subscriberID);

        prepaidCallingCard.setState(com.redknee.product.bundle.manager.api.StatusEnum.get(
                (short) subscription.getState()).getDescription());
        prepaidCallingCard.setPin(subscription.getMsisdn());
        prepaidCallingCard.setBalance(subscription.getBalance());
        prepaidCallingCard.setExpiryDate(subscription.getExpiryDate());
        prepaidCallingCard.setActivationDate(subscription.getActivationDate());
        prepaidCallingCard.setCreationDate(subscription.getCreationDate());
        prepaidCallingCard.setSpid(subscription.getSpid());
        prepaidCallingCard.setCurrency(subscription.getCurrency());

        try
        {
            PricePlanVersion ppv = PricePlanSupport.getCurrentVersion(context, subscription.getPricePlan());
            PricePlan pp = ppv.getPricePlan(context);
            prepaidCallingCard.setPricePlan(pp.getName());
        }
        catch (IllegalArgumentException e)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Price Plan (Card Type) for prepaid calling card '");
                sb.append(subscriberID);
                sb.append("' not retrieved by SubscriberProfileProvisionClient.queryAllSubscriptionsForSubscriber()");
                sb.append(" method. Calling SubscriberProfileProvisionClient.getSubscriptionType().");
                LogSupport.debug(context, this, sb.toString());
            }

            // TODO: Querying URCS again to retrieve the price plan.
            // Remove it after URCS is updated and we don't need this
            // code. Just set price plan to Unknown in case of exception.
            Subscriber sub = new Subscriber();
            sub.setId(subscriberID);
            sub.setMSISDN(subscription.getMsisdn());
            sub.setSubscriptionType(subscription.getSubscriptionType());

            Parameters completeSubscription = client.querySubscriptionProfile(context, sub);
            if (completeSubscription != null)
            {
                try
                {
                    PricePlan pp = PricePlanSupport.getPlan(context, completeSubscription.getPricePlan());
                    prepaidCallingCard.setPricePlan(pp.getName());
                }
                catch (IllegalArgumentException exc)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Price Plan (Card Type) for prepaid calling card '");
                    sb.append(subscriberID);
                    sb.append("' not retrieved by URCS.");
                    new MinorLogMsg(this, sb.toString(), exc);

                    prepaidCallingCard.setPricePlan(UNKNOWN_PRICE_PLAN);
                }
                catch (Exception exc)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Error retrieving prepaid calling card Price Plan (Card Type) '");
                    sb.append(completeSubscription.getPricePlan());
                    sb.append("' from CRM.");
                    new MinorLogMsg(this, sb.toString(), exc);

                    prepaidCallingCard.setPricePlan(completeSubscription.getPricePlan()
                            + UNKNOWN_PRICE_PLAN_DESCRIPTION);
                }
            }
        }
        catch (Exception e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Error retrieving prepaid calling card Price Plan (Card Type) '");
            sb.append(subscription.getPricePlan());
            sb.append("' from CRM.");
            new MinorLogMsg(this, sb.toString(), e);

            prepaidCallingCard.setPricePlan(subscription.getPricePlan() + UNKNOWN_PRICE_PLAN_DESCRIPTION);
        }

        // Adding the Prepaid Calling Card to the context.
        Context session = Session.getSession(context);
        session.put(PrepaidCallingCard.class, prepaidCallingCard);
        return prepaidCallingCard;
    }

    /**
     * The webcontrol used to represent the form.
     */
    private final PrepaidCallingCardWebControl callingCardWebControl_;
    /**
     * The webcontrol used to represent the form.
     */
    private final PrepaidCallingCardRequestWebControl webControl_;
    /**
     * Name of the move button.
     */
    public static final String SEARCH_BUTTON_NAME = "Search";
    /**
     * Name of the move button.
     */
    public static final String MORE_BUTTON_NAME = "More call details";

    private static final String UNKNOWN_PRICE_PLAN = "Unknown";

    public static final String UNKNOWN_PRICE_PLAN_DESCRIPTION = " - " + UNKNOWN_PRICE_PLAN;

}
