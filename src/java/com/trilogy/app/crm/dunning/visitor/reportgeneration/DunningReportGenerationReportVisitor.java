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
package com.trilogy.app.crm.dunning.visitor.reportgeneration;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.dunning.DunningProcessServer;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportRecordHome;
import com.trilogy.app.crm.dunning.DunningReportRecordXDBHome;
import com.trilogy.app.crm.dunning.DunningReportRecordXInfo;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningReportVisitor;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningXStatementVisitor;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeCmdEnum;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;


/**
 * Visitor responsible to visit reports during dunning report generation. For each report,
 * this visitor will generate an XStatement to retrieve the accounts to be processed and
 * give them to an DunningReportGenerationXStatementVisitor to process.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningReportGenerationReportVisitor extends AbstractDunningReportVisitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a DunningReportGenerationReportVisitor visitor.
     * 
     * @param context
     * @param reports
     * @param lifecycleAgent
     */
    public DunningReportGenerationReportVisitor(final Context context, final Collection<DunningReport> reports,
            final LifecycleAgentSupport lifecycleAgent)
    {
        super(lifecycleAgent);
        
        ctx_ = context;
        reports_ = reports;
        visitedReports_ = new ArrayList<DunningReport>();
    }


    /**
     * {@inheritDoc}
     */
    protected AbstractDunningXStatementVisitor getXStatementVisitor(DunningReport report)
    {
        return new DunningReportGenerationXStatementVisitor(report, getLifecycleAgent());
    }


    /**
     * {@inheritDoc}
     */
    public void visit(Context context, Object obj) throws AgentException, AbortVisitException
    {
        DunningReport report = (DunningReport) obj;
        
        if(report.isUpToDate()){
        	return;
        }
 
        Home home = (Home) context.get(DunningReportRecordHome.class);
        try
        {
            home.cmd(context, HomeCmdEnum.SAVE_DISABLE);
        }
        catch (HomeException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to disable home saving during Dunning Report generation for '");
            sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
            sb.append("' and SPID '");
            sb.append(report.getSpid());
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.minor(context, this, sb.toString());
        }
        
        XStatement xStatement = calculateXStatement(context, report);
        try
        {
            getXStatementVisitor(report).visit(context, xStatement);

            
            if (LogSupport.isEnabled(context, SeverityEnum.INFO))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Dunning Report successfully refreshed for '");
                sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
                sb.append("' and SPID '");
                sb.append(report.getSpid());
                sb.append("'");
                LogSupport.info(context, this, sb.toString());
            }
        }finally
        {
            
            try
            {
                home.cmd(context, HomeCmdEnum.SAVE_ENABLE);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to enable home saving during Dunning Report generation for '");
                sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
                sb.append("' and SPID '");
                sb.append(report.getSpid());
                sb.append("': ");
                sb.append(e.getMessage());
                LogSupport.minor(context, this, sb.toString());
            }
            
            
        }
    }


    
    /**
     * {@inheritDoc}
     */
    protected XStatement calculateXStatement(Context context, DunningReport report)
    {
        XStatement filter = null;
        if (visitedReports_.size() == 0)
        {
            filter = DunningProcessServer.getSpidFilter(context,report.getSpid());
        }
        else
        {
            DunningReport previousCalculatedDunningReport = visitedReports_.get(0);
            if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Refreshing dunning report for '");
                sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
                sb.append("' only for records in the dunning report for '");
                sb.append(CoreERLogger.formatERDateDayOnly(previousCalculatedDunningReport.getReportDate()));
                sb.append("'");
                LogSupport.debug(context, this, sb.toString());
            }
            String tableName = MultiDbSupportHelper.get(context).getTableName(context,
                    DunningReportRecordHome.class,
                    DunningReportRecordXInfo.DEFAULT_TABLE_NAME);

            filter = new SimpleXStatement("BAN IN (SELECT BAN FROM "  + tableName + " WHERE REPORTDATE = "
                    + previousCalculatedDunningReport.getReportDate().getTime() + " AND SPID = "
                    + previousCalculatedDunningReport.getSpid() + ")");
        }
        return filter;
    }


    /**
     * {@inheritDoc}
     */
    protected void validate(Context context, DunningReport report)
    {
        if (DunningReportStatusEnum.PROCESSED_INDEX == report.getStatus())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Dunning Report for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
            sb.append("' and SPID '");
            sb.append(report.getSpid());
            sb.append("' is already processed and cannot be refreshed.");
            LogSupport.minor(context, this, sb.toString());
            throw new IllegalStateException(sb.toString());
        }
        else if (DunningReportStatusEnum.PROCESSING_INDEX == report.getStatus())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Dunning Report for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
            sb.append("' and SPID '");
            sb.append(report.getSpid());
            sb.append("' is being processed and cannot be refreshed.");
            LogSupport.minor(context, this, sb.toString());
            throw new IllegalStateException(sb.toString());
        }
        else if (DunningReportStatusEnum.ACCEPTED_INDEX == report.getStatus())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Dunning Report for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
            sb.append("' and SPID '");
            sb.append(report.getSpid());
            sb.append("' is already accepted and cannot be refreshed.");
            LogSupport.minor(context, this, sb.toString());
            throw new IllegalStateException(sb.toString());
        }
    }


    


    protected void resetReports()
    {
        reports_.clear();
    }
    
    protected void resetVisitedReports()
    {
        visitedReports_.clear();
    }
    
    protected void addReport(final DunningReport report)
    {
        reports_.add(report);
    }

    protected void addVisitedReport(final DunningReport report)
    {
        visitedReports_.add(report);
    }

    private Context ctx_;
    private Collection<DunningReport> reports_;
    private ArrayList<DunningReport> visitedReports_;
}
