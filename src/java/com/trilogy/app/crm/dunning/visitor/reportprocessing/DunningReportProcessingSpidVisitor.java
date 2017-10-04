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

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportRecordMatureStateEnum;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.app.crm.dunning.DunningReportXInfo;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningReportSpidVisitor;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningReportVisitor;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningXStatementVisitor;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.DunningReportRecordSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Visitor responsible to process SPIDs during dunning report processing. For each SPID,
 * this visitor will retrieve the reports that should be processed and, and give them to
 * an DunnigReportProcessingReportVisitor to process.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningReportProcessingSpidVisitor extends AbstractDunningReportSpidVisitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new DunningReportProcessingSpidVisitor visitor.
     * 
     * @param date
     * @param lifecycleAgent
     */
    public DunningReportProcessingSpidVisitor(final Date date, final LifecycleAgentSupport lifecycleAgent)
    {
        super(date, lifecycleAgent);
        reportVisitor_ = null;
    }


    protected AbstractDunningReportVisitor getReportVisitor(Context context, Collection<DunningReport> reports)
    {
        if (reportVisitor_ == null)
        {
            reportVisitor_ = new DunningReportProcessingReportVisitor(getLifecycleAgent());
        }
        return reportVisitor_;
    }


    /**
     * {@inheritDoc}
     */
    protected DunningReportStatusEnum getRequiredStatus()
    {
        return DunningReportStatusEnum.ACCEPTED;
    }


    /**
     * {@inheritDoc}
     */
    protected DunningReportStatusEnum getTemporaryStatus()
    {
        return DunningReportStatusEnum.PROCESSING;
    }


    /**
     * {@inheritDoc}
     */
    protected void initialize(final Context ctx, final int spid)
    {
    	if (SystemSupport.isDunningReportAutoAccept(ctx, spid))
        {
            try
            {
                Collection<DunningReport> reports = HomeSupportHelper.get(ctx).getBeans(
                        ctx,
                        DunningReport.class,
                        new And().add(new LTE(DunningReportXInfo.REPORT_DATE, CalendarSupportHelper.get(ctx)
                                .getDateWithLastSecondofDay(getRunningDate())))
                                 .add(new EQ(DunningReportXInfo.STATUS, Integer.valueOf(DunningReportStatusEnum.APPROVED_INDEX)))
                                 .add(new EQ(DunningReportXInfo.SPID, spid)),
                        true,
                        DunningReportXInfo.REPORT_DATE
                        );
                
                for (DunningReport report : reports)
                {
	                 boolean success = DunningReportRecordSupport.updateDunningReportRecordMatureState(ctx, report, DunningReportRecordMatureStateEnum.ACCEPTED_INDEX,
	                        DunningReportRecordMatureStateEnum.APPROVED_INDEX);
	                if(success)
	                {
	                    DunningReport newDunningReportToCreate = DunningReportRecordSupport.cloneDunningReport(report);
                        newDunningReportToCreate.setStatus(DunningReportStatusEnum.ACCEPTED_INDEX);                        
                        HomeSupportHelper.get(ctx).createBean(ctx,newDunningReportToCreate);	                    
	                    
                        HomeSupportHelper.get(ctx).removeBean(ctx,report);
	                }
                }
            }
            catch (HomeException e)
            {
                StringBuilder cause = new StringBuilder();
                cause.append("Unable to auto accept dunning reports for SPID '");
                cause.append(spid);
                cause.append("'");
                StringBuilder sb = new StringBuilder();
                sb.append(cause.toString());
                sb.append(": ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    protected String getProcessName()
    {
        return DunningReportRecordProcessingVisitor.getVisitorProcessName();
    }


    /**
     * {@inheritDoc}
     */
    protected boolean isReportsOrderAscending()
    {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    protected void preVisiting(Context context, Collection<DunningReport> reports)
    {
    }


    /**
     * {@inheritDoc}
     */
    protected void postVisiting(Context context, Collection<DunningReport> reports)
    {
    }


    /**
     * {@inheritDoc}
     */
    protected AbstractDunningXStatementVisitor getPredicateVisitor(DunningReport report)
    {
        return new DunningReportProcessingXStatementVisitor(report, getLifecycleAgent());
    }

    private DunningReportProcessingReportVisitor reportVisitor_ = null;


	@Override
	protected Object getReportStatusFilter() {
		return new Or().add(new EQ(DunningReportXInfo.STATUS, Integer.valueOf(getRequiredStatus().getIndex())))
        .add(new EQ(DunningReportXInfo.STATUS, Integer.valueOf(getTemporaryStatus().getIndex())));
	}
}
