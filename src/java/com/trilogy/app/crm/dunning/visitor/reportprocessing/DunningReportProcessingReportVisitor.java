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
package com.trilogy.app.crm.dunning.visitor.reportprocessing;

import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportRecordHome;
import com.trilogy.app.crm.dunning.DunningReportRecordMatureStateEnum;
import com.trilogy.app.crm.dunning.DunningReportRecordStatusEnum;
import com.trilogy.app.crm.dunning.DunningReportRecordXInfo;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningReportVisitor;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningXStatementVisitor;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeCmdEnum;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;


/**
 * Visitor responsible to visit reports during dunning report processing. For each report,
 * this visitor will generate an XStatement to retrieve the accounts to be processed and
 * give them to an DunningReportProcessingXStatementVisitor to process.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningReportProcessingReportVisitor extends AbstractDunningReportVisitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a DunningReportProcessingReportVisitor visitor
     * 
     * @param lifecycleAgent
     */
    public DunningReportProcessingReportVisitor(final LifecycleAgentSupport lifecycleAgent)
    {
        super(lifecycleAgent);
    }


    /**
     * {@inheritDoc}
     */
    protected AbstractDunningXStatementVisitor getXStatementVisitor(DunningReport report)
    {
        return new DunningReportProcessingXStatementVisitor(report, getLifecycleAgent());
    }


    /**
     * {@inheritDoc}
     */
    protected void validate(final Context context, final DunningReport report)
    {
        if (DunningReportStatusEnum.PROCESSED_INDEX == report.getStatus())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot process the Dunning Report for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
            sb.append("' and SPID '");
            sb.append(report.getSpid());
            sb.append("' because it is already processed.");
            LogSupport.minor(context, this, sb.toString());
            throw new IllegalStateException(sb.toString());
        }
        else if (DunningReportStatusEnum.ACCEPTED_INDEX != report.getStatus()
                && DunningReportStatusEnum.PROCESSING_INDEX != report.getStatus())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot process the Dunning Report for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
            sb.append("' and SPID '");
            sb.append(report.getSpid());
            sb.append("' because it is not accepted.");
            LogSupport.minor(context, this, sb.toString());
            throw new IllegalStateException(sb.toString());
        }
    }


       
    protected XStatement calculateXStatement(final Context context, final DunningReport report)
    {
        And condition = new And();
        condition.add(new EQ(DunningReportRecordXInfo.REPORT_DATE,report.getReportDate()))
        .add(new EQ(DunningReportRecordXInfo.SPID,report.getSpid()))
        .add(new EQ(DunningReportRecordXInfo.FORECASTED_LEVEL,report.getNextLevel()))
        .add(new EQ(DunningReportRecordXInfo.RECORD_MATURITY,DunningReportRecordMatureStateEnum.ACCEPTED_INDEX))
        .add(new EQ(DunningReportRecordXInfo.STATUS,DunningReportRecordStatusEnum.PENDING_INDEX));
        return condition;
    }


    /**
     * {@inheritDoc}
     */
    public void visit(Context context, Object obj) throws AgentException, AbortVisitException
    {
        DunningReport report = (DunningReport) obj;
        DunningReport updatedReport = null;
        validate(context, report);
        XStatement xStatement = calculateXStatement(context, report);
        
        try
        {
            
        	updatedReport = updateReportStatus(context,report,DunningReportStatusEnum.PROCESSING_INDEX,false);
            
        	getXStatementVisitor(updatedReport).visit(context, xStatement);
            updatedReport.setStatus(DunningReportStatusEnum.PROCESSED_INDEX);
            
        }
        catch (AgentException e)
        {
            //TODO do we really need to change processed to accepted if any exception ?
        	if (updatedReport.getSuccessfullyProcessedRecords() > 0 || updatedReport.getFailedToProcessRecords() > 0
                    || updatedReport.getNumberOfRecords() == 0)
            {
                if (LogSupport.isEnabled(context, SeverityEnum.INFO))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Dunning Report parcially processed for '");
                    sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
                    sb.append("'. ");
                    LogSupport.info(context, this, sb.toString());
                }
                updatedReport.setStatus(DunningReportStatusEnum.PROCESSED_INDEX);
            }
            else
            {
            	updatedReport.setStatus(DunningReportStatusEnum.ACCEPTED_INDEX);
                throw e;
            }
        }
        catch (AbortVisitException e)
        {
            //report.setStatus(DunningReportStatusEnum.ACCEPTED_INDEX);
            throw e;
        }catch(DunningProcessException e)
        {
        	throw new AbortVisitException(e);
        }
        catch (IllegalStateException e)
        {
            // Update report if number of failed or successfully processed records is
            // different from 0.
            if (report.getFailedToProcessRecords() > 0 || report.getSuccessfullyProcessedRecords() > 0)
            {
                if (LogSupport.isEnabled(context, SeverityEnum.INFO))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Dunning Report parcially processed for '");
                    sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
                    sb.append("'. ");
                    LogSupport.info(context, this, sb.toString());
                }
                report.setStatus(DunningReportStatusEnum.PROCESSED_INDEX);
            }
            else
            {
                report.setStatus(DunningReportStatusEnum.ACCEPTED_INDEX);
            }
            throw e;
        }
        finally
        {
           updateReport(context,report,updatedReport);
           //report = updateReportStatus(context,report,DunningReportStatusEnum.PROCESSED_INDEX,true);     
        }
    }
    
    private DunningReport updateReportStatus(Context context,DunningReport oldreport,int updatedStatus,boolean remove) throws DunningProcessException
    {
    	DunningReport updatedReport = null;
    	try{
    		updatedReport = (DunningReport)oldreport.deepClone();
    	}catch(CloneNotSupportedException e)
    	{
    		LogSupport.minor(context, this, "Problem cloning DunningReport",e);
    		throw new DunningProcessException(e);
    	}
    	updatedReport.setStatus(updatedStatus);
    	if(remove)
    		updateReport(context,oldreport,updatedReport);
    	return updatedReport;
    }
}
