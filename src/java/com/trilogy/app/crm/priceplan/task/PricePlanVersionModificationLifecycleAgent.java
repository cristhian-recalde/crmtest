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
package com.trilogy.app.crm.priceplan.task;

import java.util.Date;

import com.trilogy.app.crm.bean.PPVModificationRequestHome;
import com.trilogy.app.crm.bean.PPVModificationRequestStateEnum;
import com.trilogy.app.crm.bean.PPVModificationRequestXInfo;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Lifecycle Agent responsible for applying price plan version modification.
 * @author Marcio Marques
 * @since 9.2
 *
 */
public class PricePlanVersionModificationLifecycleAgent extends LifecycleAgentScheduledTask
{
    private static final long serialVersionUID = 1L;


    public PricePlanVersionModificationLifecycleAgent(Context ctx, final String agentId) throws AgentException
    {
        super(ctx, agentId);
    }


    /**
     * {@inheritDoc}
     */
    protected void start(Context ctx) throws LifecycleException
    {
        try
        {
            final Date date = getDate(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Running Price Plan Version Modification task for date '");
                sb.append(CoreERLogger.formatERDateDayOnly(date));
                sb.append("'.");
                LogSupport.debug(ctx, this, sb.toString());
            }

            Home home = (Home) ctx.get(PPVModificationRequestHome.class);

            Predicate predicate = new And().add(
                    new EQ(PPVModificationRequestXInfo.STATUS, Integer
                            .valueOf(PPVModificationRequestStateEnum.PENDING_INDEX))).add(
                    new GTE(PPVModificationRequestXInfo.ACTIVATION_DATE, date));

            Visitor visitor = new PricePlanVersionModificationApplierVisitor(this);
            
            Context sCtx = ctx.createSubContext();
            sCtx.put(PRICE_PLAN_VERSION_MODIFICATION_AGENT, Boolean.TRUE);

            home.forEach(sCtx, visitor, predicate);
        }
        catch (final Throwable exception)
        {
            final String message = exception.getMessage();
            LogSupport.minor(ctx, getClass().getName(), message, exception);
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean isEnabled(Context ctx)
    {
        return true;
    }


    private Date getDate(final Context context)
    {
        Date date = getParameter1(context, Date.class);
        if (date == null)
        {
            date = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(
                    CalendarSupportHelper.get(context).getRunningDate(context));
        }
        return date;
    }
    
    public static final String PRICE_PLAN_VERSION_MODIFICATION_AGENT = "PricePlanVersionModificationAgent";

}
