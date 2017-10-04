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
package com.trilogy.app.crm.subscriber.agent;

import java.util.Date;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtensionXInfo;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;

import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * 
 *
 * @author asim.mahmood@redknee.com
 * @since 9.2
 */
public class FixedStopPricePlanSubscriberExtensionLifecycleAgent extends LifecycleAgentScheduledTask
{
    public FixedStopPricePlanSubscriberExtensionLifecycleAgent(Context ctx, String agentId) throws AgentException
    {
        super(ctx, agentId);

        visitor_ = new FixedStopPricePlanSubscriberUpdateVisitor(this);      
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void start(Context ctx) throws LifecycleException
    {
        try
        {
            Home home = HomeSupportHelper.get(ctx).getHome(ctx, FixedStopPricePlanSubExtension.class);
            
            CalendarSupport calendarSupport = CalendarSupportHelper.get(ctx);
            
            Date date = getParameter1(ctx, Date.class);
            if (date == null)
            {
                date = calendarSupport.getRunningDate(ctx);
            }
            else
            {
                ctx.put(CommonTime.RUNNING_DATE, date);
                new InfoLogMsg(this, "Subscriber Price Plan End Task running with simulated execution date provided parameter #1 of task configuration: " + date, null).log(ctx);
            }
            
            new InfoLogMsg(this, "Subscriber Price Plan End Task processing subscriptions with price plan end date before " + date, null).log(ctx);
            
            And filter = new And();
            filter.add(new LTE(FixedStopPricePlanSubExtensionXInfo.END_DATE, date));

            home.forEach(ctx, visitor_, filter);
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Error occurred in lifecycle agent [" + getAgentId()
                    + "] while executing Subscriber Price Plan End Task", e).log(ctx);
        }
    }


    
    private Visitor visitor_;
}
