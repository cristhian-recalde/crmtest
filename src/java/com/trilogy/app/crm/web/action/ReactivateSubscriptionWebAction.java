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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.MessageManagerText;


/**
 * @author Simar Singh
 */

public class ReactivateSubscriptionWebAction extends SubscriptionWebConfrimAction
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ReactivateSubscriptionWebAction()
    {
        super("reactivateSubscription", "Reactivate");
    }


    @Override
    public void execute(Context ctx) throws AgentException
    {
        Context subCtx = ctx.createSubContext();
        final String subscriberId = WebAgents.getParameter(subCtx, "key");
        Subscriber sub = null;
        try
        {
            sub = SubscriberSupport.lookupSubscriberForSubId(subCtx, subscriberId);
            if (sub != null && isAllowed(subCtx, sub))
            {
                sub.setState(SubscriberStateEnum.ACTIVE);
                ((Home) ctx.get(SubscriberHome.class)).store(subCtx, sub);
            }
        }
        catch (Exception e)
        {
            LogSupport
                    .minor(subCtx, this, "Error trying to retrieve Subscriber with Subscriber ID: " + subscriberId, e);
            WebAgents.getWriter(subCtx).println(
                    "<font color=\"red\">" + "Error encountered during re-activation of subscription: " + subscriberId
                            + ". Error [" + e.getMessage() + "]" + "</font><br/><br/>");
        }
        ContextAgents.doReturn(subCtx);
    }


    @Override
    protected String getConfirmationMessage(Context ctx, Subscriber sub)
    {
        final MessageMgr manager = new MessageMgr(ctx, this);
        final Object[] values = new Object[1];
        values[0] = sub.getId();
        return CONFIRM_TEXT.get(manager, values);
    }


    @Override
    protected boolean isAllowed(Context ctx, Subscriber subscriber)
    {
        if (subscriber.getState() == SubscriberStateEnum.DORMANT)
        {
            return true;
        }
        return false;
    }
    
    private static final MessageManagerText CONFIRM_TEXT = new MessageManagerText(
            "ReactivateSubscriptionWebAction.CONFIRM_TEXT", "Do you want to re-activate Subscription {0} ?");
}