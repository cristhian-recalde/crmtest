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

import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.dunning.DunningProcess;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.invoice.InvoiceCalculationSupport;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleException;
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
public class DunningProcessLifecycleAgent extends LifecycleAgentScheduledTask
{
    private static final long serialVersionUID = 1L;


    /**
     * Creates a DunningReportGenerationLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public DunningProcessLifecycleAgent(Context ctx, final String agentId) throws AgentException
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

        	ctx.put(InvoiceCalculationSupport.DUNNING_TASK, true);
        	LogSupport.minor(ctx, this, "Dunning flag set  === "+ ctx.getBoolean(InvoiceCalculationSupport.DUNNING_TASK,false), null); 
            final Date date = getReportDate(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Dunning processing for date '");
                sb.append(CoreERLogger.formatERDateDayOnly(date));
                sb.append("'.");
                LogSupport.debug(ctx, this, sb.toString());
            }
            final DunningProcess dunningProcess = (DunningProcess) ctx.get(DunningProcess.class);
            dunningProcess.processAllAccounts(ctx, date, this);
        }
        catch (final DunningProcessException exception)
        {
            final String message = exception.getMessage();
            LogSupport.minor(ctx, getClass().getName(), message, exception);
        }
        finally 
        {
        	LogSupport.minor(ctx, this, " === start END ===", null);
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean isEnabled(Context ctx)
    {
        return (LicensingSupportHelper.get(ctx).isLicensed(ctx, CoreCrmLicenseConstants.POSTPAID_LICENSE_KEY)
                || LicensingSupportHelper.get(ctx).isLicensed(ctx, CoreCrmLicenseConstants.HYBRID_LICENSE_KEY)) && 
                !DunningReport.isDunningReportSupportEnabled(ctx);
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
    private Date getReportDate(final Context context)
    {
        Date reportDate = getParameter1(context, Date.class);
        if (reportDate==null)
        {
            reportDate = new Date();
        }
        return reportDate;
    }

    /**
     * The date format used for specifying the "current date" in parameter 1. This format
     * is currently consistent with other CronAgents.
     */
    private static final String DATE_FORMAT_STRING = "yyyyMMdd";
}
