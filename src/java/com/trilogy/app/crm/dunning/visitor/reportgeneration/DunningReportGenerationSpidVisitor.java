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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.CreditEventTypeEnum;
import com.trilogy.app.crm.bean.CreditNotificationEvent;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.app.crm.dunning.DunningReportXInfo;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningReportSpidVisitor;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningReportVisitor;
import com.trilogy.app.crm.dunning.visitor.reportprocessing.DunningReportRecordProcessingVisitor;
import com.trilogy.app.crm.extension.creditcategory.NoticeScheduleCreditCategoryExtension;
import com.trilogy.app.crm.extension.creditcategory.NoticeScheduleCreditCategoryExtensionXInfo;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQDay;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Visitor responsible to process SPIDs during dunning report generation. For each SPID,
 * this visitor will retrieve the reports that should be processed and, and give them to
 * an DunnigReportGenerationReportVisitor to process.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningReportGenerationSpidVisitor extends AbstractDunningReportSpidVisitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new DunningReportGenerationSpidVisitor visitor.
     * 
     * @param date
     * @param lifecycleAgent
     */
    public DunningReportGenerationSpidVisitor(final Date date, final LifecycleAgentSupport lifecycleAgent)
    {
        super(date, lifecycleAgent);
        reportVisitor_ = null;
    }


    /**
     * {@inheritDoc}
     */
    protected DunningReportGenerationReportVisitor getReportVisitor(Context context, Collection<DunningReport> reports)
    {
        if (reportVisitor_ == null)
        {
            reportVisitor_ = new DunningReportGenerationReportVisitor(context, reports, getLifecycleAgent());
        }
        return reportVisitor_;
    }


    /**
     * {@inheritDoc}
     */
    protected DunningReportStatusEnum getRequiredStatus()
    {
        return DunningReportStatusEnum.PENDING;
    }


    /**
     * {@inheritDoc}
     */
    protected DunningReportStatusEnum getTemporaryStatus()
    {
        return DunningReportStatusEnum.REFRESHING;
    }


    /**
     * Creates a report for the given date if it doesn't exist, and set the running date
     * to the date of the report with greatest date, as all the reports should be
     * refreshed. {@inheritDoc}
     */
    protected void initialize(Context ctx, int spid)
    {
        try
        {
            DunningReport report = HomeSupportHelper.get(ctx).findBean(
                    ctx,
                    DunningReport.class,
                    new And().add(new EQDay(DunningReportXInfo.REPORT_DATE, getRunningDate())).add(
                            new EQ(DunningReportXInfo.SPID, spid)));
            if (report == null)
            {
                /*And futureNotPendingReportFilter = new And();
                futureNotPendingReportFilter.add(new EQ(DunningReportXInfo.SPID, Integer.valueOf(spid)));
                futureNotPendingReportFilter.add(new GT(DunningReportXInfo.REPORT_DATE, CalendarSupportHelper.get(ctx)
                        .getDateWithLastSecondofDay(getRunningDate())));
                futureNotPendingReportFilter.add(new Not(new EQ(DunningReportXInfo.STATUS, Integer
                        .valueOf(DunningReportStatusEnum.PENDING_INDEX))));
                if (HomeSupportHelper.get(ctx).hasBeans(ctx, DunningReport.class, futureNotPendingReportFilter))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("There is already a non-pending report with date greater than '");
                    sb.append(CoreERLogger.formatERDateDayOnly(getRunningDate()));
                    sb.append("' for SPID '");
                    sb.append(spid);
                    sb.append("'");
                    LogSupport.minor(ctx, this, sb.toString());
                    throw new IllegalStateException(sb.toString());
                }*/
                if (LogSupport.isDebugEnabled(ctx))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Creating Dunning Report for '");
                    sb.append(CoreERLogger.formatERDateDayOnly(getRunningDate()));
                    sb.append("' and SPID '");
                    sb.append(spid);
                    sb.append("'");
                    LogSupport.debug(ctx, this, sb.toString());
                }
                report = new DunningReport();
                report.setStatus(DunningReportStatusEnum.REFRESHING_INDEX);
                report.setReportDate(getRunningDate());
                report.setSpid(spid);
                report.setNumberOfRecords(0);
                report.setFailedToProcessRecords(0);
                report.setUpToDate(false);
                report.setSuccessfullyProcessedRecords(0);
                HomeSupportHelper.get(ctx).createBean(ctx, report);
            }
            if(report != null && report.getStatus() == DunningReportStatusEnum.PENDING_INDEX)
            {
            	throw new IllegalStateException("Report Already Exists");
            }
            // Always update all the pending reports, starting by the one in the future.
            And pendingReportWithGreaterDateFilter = new And();
            pendingReportWithGreaterDateFilter.add(new EQ(DunningReportXInfo.SPID, Integer.valueOf(spid)));
            pendingReportWithGreaterDateFilter.add(new GT(DunningReportXInfo.REPORT_DATE, CalendarSupportHelper
                    .get(ctx).getDateWithLastSecondofDay(getRunningDate())));
            pendingReportWithGreaterDateFilter.add(new Or().add(new EQ(DunningReportXInfo.STATUS, Integer
                    .valueOf(DunningReportStatusEnum.PENDING_INDEX))).add(new EQ(DunningReportXInfo.STATUS, Integer
                            .valueOf(DunningReportStatusEnum.REFRESHING_INDEX))));
            Collection<DunningReport> reports = HomeSupportHelper.get(ctx).getBeans(ctx, DunningReport.class,
                    pendingReportWithGreaterDateFilter, 1, false, DunningReportXInfo.REPORT_DATE);
            if (reports != null && reports.size() > 0)
            {
                report = reports.iterator().next();
                setRunningDate(report.getReportDate());
            }
            
            
        }
        catch (HomeException e)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("Dunning Report generation failed. Unable to retrieve or create Dunning Report for date '");
            cause.append(CoreERLogger.formatERDateDayOnly(getRunningDate()));
            cause.append("' and SPID '");
            cause.append(spid);
            cause.append("'");
            StringBuilder sb = new StringBuilder();
            sb.append(cause.toString());
            sb.append(": ");
            sb.append(e.getMessage());
            LogSupport.minor(ctx, this, sb.toString(), e);
            throw new IllegalStateException(cause.toString());
        }
    }

    
    /**
     * Sets all the reports to the temporaty state. {@inheritDoc}
     */
    protected void preVisiting(Context context, Collection<DunningReport> reports)
    {
        
    }


    /**
     * Groups all DunningReportRecords as per forcastedLevel and recordMaturity and 
     * Creates corresponding DunningReport entries in PENDING/APPROVE state.
     */
    protected void postVisiting(Context context, Collection<DunningReport> reports)
    {
        for (DunningReport report : reports)
        {
            
            try
            {
            final String reportRecordSql = new String("insert into dunningreport (SPID,REPORTDATE,NEXTLEVEL,UPTODATE,"
                              +"STATUS,NUMBEROFRECORDS) select spid as SPID,reportDate as REPORTDATE,forecastedLevel as NEXTLEVEL,'y'"
                              +",CASE WHEN recordMaturity =1 THEN 5 ELSE recordMaturity END as STATUS,count(*) as NUMBEROFRECORDS from "
                              +"dunningreportrecord where reportdate="+report.getReportDate().getTime()+" and spid="+report.getSpid()  
                              +" group by spid,reportdate,forecastedLevel,recordMaturity");
            final XDB xdb = (XDB) context.get(XDB.class);
            
            int noOfRecords=xdb.execute(context,reportRecordSql);
            if(noOfRecords>0)
            {
            	LogSupport.debug(context, this, "Added "+noOfRecords+" to DunningReport for running Date "+getRunningDate()); 
            }
            Home home=HomeSupportHelper.get(context).getHome(context,DunningReport.class);
            home.remove(context,report);
            }catch (final HomeException e)
            {
                 String cause = "Error while inserting";
                 StringBuilder sb = new StringBuilder();
                 sb.append(cause);
                 sb.append(": ");
                 sb.append(e.getMessage());
                 LogSupport.major(context, this, sb.toString(), e);
                 throw new IllegalStateException("Dunning Report is not updated with report records: " + cause, e);
            }
            
            
        }
        //Group by all drrs , delete dr in refreshing state , insert group by result
        //previous reports update if drr present
      
        getReportVisitor(context, reports).resetReports();
    }



    /**
     * {@inheritDoc}
     */
    protected boolean isReportsOrderAscending()
    {
        return false;
    }


    /**
     * {@inheritDoc}
     */
    protected String getProcessName()
    {
        return DunningReportRecordProcessingVisitor.getVisitorProcessName();
    }

    private DunningReportGenerationReportVisitor reportVisitor_ = null;


	@Override
	protected Object getReportStatusFilter() {
		
        return new EQ(DunningReportXInfo.STATUS, Integer.valueOf(getTemporaryStatus().getIndex()));
	}
}
