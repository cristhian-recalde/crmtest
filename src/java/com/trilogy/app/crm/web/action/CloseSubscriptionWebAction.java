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

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.webcontrol.XTestIgnoreWebControl;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.home.sub.SubscriptionDeactivateValidator;
import com.trilogy.app.crm.subscriber.state.postpaid.PostpaidSubscriberStateSupport;
import com.trilogy.app.crm.subscriber.state.prepaid.PrepaidSubscriberStateSupport;
import com.trilogy.app.crm.support.CurrencyPrecisionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.util.MessageManagerText;


/**
 * @author Simar Singh
 */
public class CloseSubscriptionWebAction extends SubscriptionWebConfrimAction
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public CloseSubscriptionWebAction()
    {
        super("closeSubscription", "De-activate");
    }


    @Override
    public void execute(Context ctx) throws AgentException
    {
        ctx = ctx.createSubContext();
        final String subscriberId = WebAgents.getParameter(ctx, "key");
        Subscriber sub = null;
        PrintWriter out = WebAgents.getWriter(ctx);
        try
        {
            sub = SubscriberSupport.lookupSubscriberForSubId(ctx, subscriberId);
            if (sub != null)
            {
                final MessageMgr manager = new MessageMgr(ctx, this);
                if (isAllowedDirectClose(ctx))
                {
                    
                    if (sub.isPrepaid() && SystemSupport.supportsAllowWriteOffForPrepaidSubscription(ctx) && !sub.isPooledMemberSubscriber(ctx))
                    {
                        // balance write-off is meaningful only to pre-paid.
                        long balance = sub.getBalanceRemaining();
                        if (balance > 0)
                        {
                            out.println(
                                    BALANCE_WRITEOFF_TEXT.get(manager, getMessageValues(ctx, sub, balance)));
                        }
                    }
                }
                else
                {
                    ctx.put(SubscriptionDeactivateValidator.WRITE_OFF, false);
                }
                
                sub.setState(SubscriberStateEnum.INACTIVE);
                ((Home) ctx.get(SubscriberHome.class)).store(ctx, sub);
                final Object[] values = new Object[1];
                values[0] = subscriberId;
                
                if (SubscriberStateEnum.INACTIVE.equals(sub.getState()))
                {
                    out.println(SUCCESS_TEXT.get(manager, values));
                }
            }
            else
            {
                new InfoLogMsg(this, "Subscription: " + subscriberId + " invalid for de-activate (close) operation", null).log(ctx);
            }
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, this, "Error trying to retrieve Subscriber with Subscriber ID: " + subscriberId, e);
            out.println(
                    "<font color=\"red\">" + "Error encountered during de-activation of Subscription : " + subscriberId
                            + ". Error [" + e.getMessage() + "]" + "</font><br/><br/>");
            
            out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
            out.println("<!-- Stacktrace:");
            e.printStackTrace(out);
            out.println(" -->");
            out.println(XTestIgnoreWebControl.IGNORE_END);
        }
        ContextAgents.doReturn(ctx);
    }


    @Override
    public String getConfirmationMessage(Context ctx, Subscriber sub)
    {
        final MessageMgr manager = new MessageMgr(ctx, this);
        if (isAllowedDirectClose(ctx) && sub.isPrepaid() && !sub.isPooledMemberSubscriber(ctx))
        {
            if (SystemSupport.supportsAllowWriteOffForPrepaidSubscription(ctx))
            {
                // prepaid require special advice of write-off remaining balance
                // postpaid subscriptions have everything accounted for in invoice
                final long balance = sub.getBalanceRemaining();
                if (balance > 0)
                {
                    return CONFIRM_FULL_TEXT.get(manager, getMessageValues(ctx, sub, balance));
                }
            }
            else
            {
                return CONFIRM_NO_WRITE_FULL_TEXT.get(manager, getMessageValues(ctx,sub));
                
            }
        }
        final Object[] values = new Object[1];
        values[0] = sub.getId();
        return CONFIRM_TEXT.get(manager, values);
    }


    @Override
    protected boolean isAllowed(Context ctx, Subscriber subscriber)
    {
        try
        {
            if (SubscriberTypeEnum.PREPAID == subscriber.getSubscriberType())
            {
                return PrepaidSubscriberStateSupport.instance().getState(ctx, subscriber)
                        .isManualStateTransitionPermitted(ctx, subscriber, SubscriberStateEnum.INACTIVE);
            }
            else
            {
                return PostpaidSubscriberStateSupport.instance().getState(ctx, subscriber).isManualStateTransitionPermitted(
                        ctx, subscriber, SubscriberStateEnum.INACTIVE);
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error fetching subscriber's subscription: " + subscriber.getId(), e).log(ctx);
        }
        return false;
    }


    protected boolean isAllowedDirectClose(Context ctx)
    {
        try
        {
            return ApiSupport.authorizeUser(ctx, DIRECT_CLOSE_PERMISSION);
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Error fetching user details", t).log(ctx);
        }
        return false;
    }


    protected Object[] getMessageValues(Context ctx, Subscriber sub, long balance)
    {
        final Object[] values = new Object[3];
        values[0] = sub.getId();
        values[1] = sub.getCurrency(ctx);
        values[2] = CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx, sub.getCurrency(ctx), balance);
        return values;
    }

    protected Object[] getMessageValues(Context ctx, Subscriber sub)
    {
        final Currency currency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);
        final Object[] values = new Object[1];
        values[0] = sub.getId();
        return values;
    }
    public final static SimplePermission DIRECT_CLOSE_PERMISSION = new SimplePermission(
            "special.app.crm.subsriber.directclose");
    /**
     * The main header of the section in the message manager.
     */
    private static final MessageManagerText BALANCE_WRITEOFF_TEXT = new MessageManagerText(
            "CloseSubscriptionWebAction.BALANCE_WRITEOFF_TEXT", "<font color=\"red\">"
                    + "For Subscription [ {0} ], the remaining balance of [ {1} {2} ] need`s to be written-off."
                    + " Please check transacton records for more details." + " </font><br/><br/>");
    private static final MessageManagerText SUCCESS_TEXT = new MessageManagerText(
            "CloseSubscriptionWebAction.SUCCES_TEXT", "<font color=\"red\">"
                    + "Subscription [ {0} ] successfully de-activated." + "</font><br/><br/>");
    private static final MessageManagerText CONFIRM_FULL_TEXT = new MessageManagerText(
            "CloseSubscriptionWebAction.CONFIRM_FULL_TEXT", "Subscription {0} has a balance of [ {1} {2} ]. "
                    + "The balance amount must be written-off and all services will be de-activated with this operation. Do you want to proceed ?");
    private static final MessageManagerText CONFIRM_NO_WRITE_FULL_TEXT = new MessageManagerText(
            "CloseSubscriptionWebAction.CONFIRM_NO_WRITE_FULL_TEXT", "Subscription {0} will be deactivated "
            +" along with all the services with this operation. Do you want to proceed ?");

    private static final MessageManagerText CONFIRM_TEXT = new MessageManagerText(
            "CloseSubscriptionWebAction.CONFIRM_TEXT",
            "Balance amount if any, must be written-off and all services will be de-activated with this operaton. Do you want to de-activate Subscription {0} ?");
}