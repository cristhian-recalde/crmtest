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
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Abstract class for dunning report lifecycle agents
 * 
 * @author Marcio Marques
 * @since 9.1.1
 * 
 */
public abstract class AbstractDunningReportLifecycleAgent extends LifecycleAgentScheduledTask
{
    private static final long serialVersionUID = 1L;

    /**
     * Creates a AbstractDunningReportLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public AbstractDunningReportLifecycleAgent(Context ctx, String agentId) throws AgentException
    {
        super(ctx, agentId);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnabled(Context ctx)
    {
        return (LicensingSupportHelper.get(ctx).isLicensed(ctx, CoreCrmLicenseConstants.POSTPAID_LICENSE_KEY)
                || LicensingSupportHelper.get(ctx).isLicensed(ctx, CoreCrmLicenseConstants.HYBRID_LICENSE_KEY)) && 
                DunningReport.isDunningReportSupportEnabled(ctx);
    }
    
    protected abstract void startTask(Context ctx, Date date, Integer spid) throws DunningProcessException;

    /**
     * {@inheritDoc}
     */
    protected void start(Context ctx) throws LifecycleException
    {
        try
        {
            final Date date = getReportDate(ctx);
            final Integer spid = getSpid(ctx);
            CRMSpid crmSpid = null;
            int advanceReportDays = 2;
            Date advDate = date;
            try 
            {
            	if(spid != null)
            	{
            		crmSpid = SpidSupport.getCRMSpid(ctx, spid);
            		if(crmSpid != null)
            		{
            			advanceReportDays = crmSpid.getAdvanceDunningReportDays();		            
            			CalendarSupport calSupp = CalendarSupportHelper.get(ctx);
            			advDate = calSupp.findDateDaysAfter(advanceReportDays, date);
            		}
            	}
			} 
            catch (HomeException e) 
            {
            	 LogSupport.major(ctx, getClass().getName(), "Failed to retrieve SPID object for spid id="+spid, e);;
			}
            
            startTask(ctx, advDate, spid);

        }
        catch (final DunningProcessException exception)
        {
            final String message = exception.getMessage();
            LogSupport.minor(ctx, getClass().getName(), message, exception);
        }
    }
    
    /**
     * Gets the dunning report processing date.
     * 
     * @param context
     *            The operating context.
     * @return The "current date" for the dunning report processing run.
     * @throws AgentException
     *             thrown if any Exception is thrown during date parsing. Original
     *             Exception is linked.
     */
    protected Date getReportDate(final Context context)
    {
        Date reportDate = getParameter1(context, Date.class);
        if (reportDate==null)
        {
            reportDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(CalendarSupportHelper.get(context).getRunningDate(context));
        }
        return reportDate;
    }

    /**
     * Gets the spid for the dunning report generation run.
     * 
     * @param context
     *            The operating context.
     * @return The "spid" for the dunning report generation run.
     * @throws AgentException
     *             thrown if any Exception is thrown during date parsing. Original
     *             Exception is linked.
     */
    protected Integer getSpid(final Context context)
    {
        return getParameter2(context, Integer.class);
    }
}
