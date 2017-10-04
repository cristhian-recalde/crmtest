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
package com.trilogy.app.crm.account;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistoryHome;
import com.trilogy.app.crm.bean.BillCycleHistoryXInfo;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class BillCycleChangeLifecycleAgent extends LifecycleAgentScheduledTask
{
    public BillCycleChangeLifecycleAgent(Context ctx, String agentId) throws AgentException
    {
        super(ctx, agentId);

        visitor_ = new BillCycleChangeVisitor(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void start(Context ctx) throws LifecycleException
    {
        Home home = (Home) ctx.get(BillCycleHistoryHome.class);
        
        try
        {
            CalendarSupport calendarSupport = CalendarSupportHelper.get(ctx);
            
            Date date = getParameter1(ctx, Date.class);
            if (date == null)
            {
                date = calendarSupport.getRunningDate(ctx);
            }
            else
            {
                new InfoLogMsg(this, "Bill Cycle Change Task running with simulated execution date provided parameter #1 of task configuration: " + date, null).log(ctx);
            }
            
            Calendar cal = calendarSupport.dateToCalendar(date);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            
            int tomorrowDay = cal.get(Calendar.DAY_OF_MONTH);
            new InfoLogMsg(this, "Bill Cycle Change Task processing pending bill cycle change records having old bill cycle day = " + tomorrowDay, null).log(ctx);
            
            And filter = new And();
            filter.add(new EQ(BillCycleHistoryXInfo.STATUS, BillCycleChangeStatusEnum.PENDING));
            filter.add(new EQ(BillCycleHistoryXInfo.OLD_BILL_CYCLE_DAY, tomorrowDay));

            home.forEach(ctx, visitor_, filter);
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Error occurred in lifecycle agent [" + getAgentId()
                    + "] while executing bill cycle change(s)", e).log(ctx);
        }
    }

    private Visitor visitor_;
}
