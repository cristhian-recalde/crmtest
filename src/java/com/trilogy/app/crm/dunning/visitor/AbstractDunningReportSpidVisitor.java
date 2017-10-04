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
package com.trilogy.app.crm.dunning.visitor;

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.app.crm.dunning.DunningReportXInfo;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyXInfo;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;


/**
 * Visitor responsible to process SPIDs during dunning. For each SPID, this visitor will
 * retrieve the reports that should be processed and, and give them to an
 * AbstractDunningReportVisitor to process.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public abstract class AbstractDunningReportSpidVisitor implements Visitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new AbstractDunningReportSpidVisitor visitor.
     * 
     * @param date
     * @param lifecycleAgent
     */
    public AbstractDunningReportSpidVisitor(final Date date, final LifecycleAgentSupport lifecycleAgent)
    {
        runningDate_ = date;
        lifecycleAgent_ = lifecycleAgent;
    }


    /**
     * {@inheritDoc}
     */
    public void visit(final Context context, final Object obj) throws AgentException, AbortVisitException
    {
        CRMSpid spid = (CRMSpid) obj;

        final Context subContext = context.createSubContext();
        
        subContext.put(CRMSpid.class, spid);
        
        String spidCurrency = spid.getCurrency();
        try
        {
            Currency currency = HomeSupportHelper.get(context).findBean(context, Currency.class,
                new EQ(CurrencyXInfo.CODE, spidCurrency));
            subContext.put(Currency.class, currency);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(subContext))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to retrieve currency '");
                sb.append(spidCurrency);
                sb.append("':");
                sb.append(e.getMessage());
                LogSupport.debug(subContext, this, sb.toString());
            }
        }

        
        if (LogSupport.isEnabled(subContext, SeverityEnum.INFO))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Starting ");
            sb.append(getProcessName());
            sb.append(" for reports with date before or on '");
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
            sb.append("'");
            LogSupport.info(subContext, this, sb.toString());
        }
        initialize(subContext, spid.getSpid());
        try
        {
            Collection<DunningReport> reports = retrieveReports(subContext, spid.getSpid());
            if (reports != null)
            {
                preVisiting(subContext, reports);
                try
                {
                    for (DunningReport report : reports)
                    {
                        getReportVisitor(subContext, reports).visit(subContext, report);
                    }
                }
                finally
                {
                    postVisiting(subContext, reports);
                }
            }
        }
        catch (DunningProcessException e)
        {
            throw new AgentException(e);
        }
    }


    /**
     * Retrieve a list of the reports to be iterated over.
     * 
     * @param context
     * @param spid
     * @return
     * @throws DunningProcessException
     */
    private Collection<DunningReport> retrieveReports(final Context context, final int spid)
            throws DunningProcessException
    {
        if (LogSupport.isDebugEnabled(context))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Retrieving ");
            sb.append(getRequiredStatus().getDescription());
            sb.append(" dunning reports with date before or on '");
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
            sb.append("' and SPID '");
            sb.append(spid);
            sb.append("'");
            LogSupport.debug(context, this, sb.toString());
        }
        And filter = new And();
        filter.add(new LTE(DunningReportXInfo.REPORT_DATE, CalendarSupportHelper.get(context)
                .getDateWithLastSecondofDay(runningDate_)));
        filter.add(new EQ(DunningReportXInfo.SPID, Integer.valueOf(spid)));
        filter.add(getReportStatusFilter());
        try
        {
            Collection<DunningReport> reports = HomeSupportHelper.get(context).getBeans(context, DunningReport.class,
                    filter, isReportsOrderAscending(), DunningReportXInfo.REPORT_DATE);
            return reports;
        }
        catch (HomeException e)
        {
            String cause = "Unable to find " + getRequiredStatus().getDescription() + " reports";
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" with date before or on '");
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
            sb.append("' and SPID '");
            sb.append(spid);
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.major(context, this, sb.toString(), e);
            throw new DunningProcessException(cause, e);
        }
    }
    
    abstract protected Object getReportStatusFilter();

    /**
     * Gets the process name.
     * 
     * @return
     */
    abstract protected String getProcessName();


    /**
     * Action to be performed in the beginning of the visit.
     * 
     * @param ctx
     * @param spid
     */
    protected abstract void initialize(Context ctx, int spid);
    


    /**
     * Retrieves the required status of reports for processing.
     * 
     * @return
     */
    abstract protected DunningReportStatusEnum getRequiredStatus();


    /**
     * Retrieves the temporary status in which reports will be during processing.
     * 
     * @return
     */
    abstract protected DunningReportStatusEnum getTemporaryStatus();


    /**
     * Indicates whether reports should be ordered ascending or not.
     * 
     * @return
     */
    abstract protected boolean isReportsOrderAscending();


    /**
     * Returns the visitor responsible to visit each report.
     * 
     * @param context
     * @param reports
     * @return
     */
    abstract protected Visitor getReportVisitor(final Context context,
            final Collection<DunningReport> reports);


    /**
     * Actions executed prior to visiting a report.
     * 
     * @param context
     * @param reports
     */
    abstract protected void preVisiting(final Context context, final Collection<DunningReport> reports);


    /**
     * Actions executed after visiting a report.
     * 
     * @param context
     * @param reports
     */
    abstract protected void postVisiting(final Context context, final Collection<DunningReport> reports);


    protected void setRunningDate(Date runningDate)
    {
        runningDate_ = runningDate;
    }


    protected Date getRunningDate()
    {
        return runningDate_;
    }


    protected LifecycleAgentSupport getLifecycleAgent()
    {
        return lifecycleAgent_;
    }

    private Date runningDate_;
    private LifecycleAgentSupport lifecycleAgent_;
}
