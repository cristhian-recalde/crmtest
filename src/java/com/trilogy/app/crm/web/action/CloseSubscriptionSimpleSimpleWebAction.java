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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.support.SubscriberSupport;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * @author Simar Singh
 */
public class CloseSubscriptionSimpleSimpleWebAction extends SubscriptionWebConfrimAction
{

    public CloseSubscriptionSimpleSimpleWebAction()
    {
        super("closeSubscription", "Close");
    }


    public void execute(Context ctx) throws AgentException
    {
        Context subCtx = ctx.createSubContext();
        final String subscriberId = WebAgents.getParameter(subCtx, "key");
        Subscriber sub = null;
        try
        {
            sub = SubscriberSupport.lookupSubscriberForSubId(subCtx, subscriberId);
            if (sub != null)
            {
                sub.setState(SubscriberStateEnum.INACTIVE);
                ((Home) ctx.get(SubscriberHome.class)).store(subCtx, sub);
            }
            else
            {
                new InfoLogMsg(this, "Subscriber-Subscription: " + subscriberId + " invalid for close operation", null)
                        .log(subCtx);
            }
        }
        catch (Exception e)
        {
            LogSupport
                    .minor(subCtx, this, "Error trying to retrieve Subscriber with Subscriber ID: " + subscriberId, e);
            WebAgents.getWriter(subCtx).println(
                    "<font color=\"red\">" + "Error was encounteted during de-activation of Subscription: " + subscriberId
                            + ". Error [" + e.getMessage() + "]" + "</font><br/><br/>");
        }
        ContextAgents.doReturn(subCtx);
    }


    public String getConfirmationMessage(Context ctx, Subscriber sub)
    {
        return "Do you want to close Subscriber-Subscription " + sub.getId() + " ?";
    }


    protected boolean isAllowed(Context ctx, Subscriber subscriber)
    {
        try
        {
            if (subscriber.getState() == SubscriberStateEnum.DORMANT
                    && subscriber.isPrepaid())
            {
                return true;
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error fetching subscriber's subscription: " + subscriber.getId(), e).log(ctx);
        }
        return false;
    }
}