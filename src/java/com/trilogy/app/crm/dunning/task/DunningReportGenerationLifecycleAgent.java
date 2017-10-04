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
package com.trilogy.app.crm.dunning.task;

import java.util.Date;

import com.trilogy.app.crm.dunning.DunningProcess;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.invoice.InvoiceCalculationSupport;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Generates dunning reports.
 * 
 * @author Marcio Marques
 * @since 9.0
 * 
 */
public class DunningReportGenerationLifecycleAgent extends AbstractDunningReportLifecycleAgent
{

    private static final long serialVersionUID = 1L;


    /**
     * Creates a DunningReportGenerationLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public DunningReportGenerationLifecycleAgent(Context ctx, final String agentId) throws AgentException
    {
        super(ctx, agentId);
    }


    /**
     * {@inheritDoc}
     */
    protected void startTask(Context ctx, Date date, Integer spid) throws DunningProcessException
    {
    	
    	
    	ctx.put(InvoiceCalculationSupport.DUNNING_TASK, true);
    	LogSupport.minor(ctx, this, "Dunning flag set  === "+ ctx.getBoolean(InvoiceCalculationSupport.DUNNING_TASK,false), null);
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Generating report for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("'.");
            LogSupport.debug(ctx, this, sb.toString());
        }
        final DunningProcess dunningProcess = (DunningProcess) ctx.get(DunningProcess.class);
        if (spid == null)
        {
            dunningProcess.generateReport(ctx, date, this);
        }
        else
        {
            dunningProcess.generateSpidReport(ctx, date, spid.intValue(), this);
        }
        LogSupport.minor(ctx, this, " === startTask END ===", null);
    }


    /**
     * Gets the dunning report generation date.
     * 
     * @param context
     *            The operating context.
     * @return The "current date" for the dunning report generation run.
     * @throws AgentException
     *             thrown if any Exception is thrown during date parsing. Original
     *             Exception is linked.
     */
    protected Date getReportDate(final Context context)
    {
        Date reportDate = super.getReportDate(context);
        
        /**
         * Sprint 8: Dunning Spillover:
         * As per the requirement now report can also be generated for past dates 
         */
        /*if (reportDate.before(CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(CalendarSupportHelper.get(context).getRunningDate(context))))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to generating report for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(reportDate));
            sb.append("': The date is in the past.");
            LogSupport.major(context, this, sb.toString());
            throw new CronContextAgentException(sb.toString());
        }*/

        return reportDate;
    }



}
