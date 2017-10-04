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

import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningReportVisitor;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;


/**
 * Visitor responsible to visit a report during dunning report refresh. This visitor will
 * generate an XStatement to retrieve the accounts to be processed and give them to an
 * DunningReportGenerationXStatementVisitor to process.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningReportRefreshReportVisitor extends DunningReportGenerationReportVisitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a DunningReportRefreshReportVisitor visitor.
     * 
     * @param context
     * @param reports
     * @param lifecycleAgent
     */
    public DunningReportRefreshReportVisitor(final Context context)
    {
        super(context, new ArrayList<DunningReport>(), null);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(Context context, Object obj) throws AgentException, AbortVisitException
    {
        DunningReport report = (DunningReport) obj;
        addVisitedReport(report);
        addReport(report);
        try
        {
            super.visit(context, report);
        }
        finally
        {
            if (report.getStatus() == DunningReportStatusEnum.REFRESHING_INDEX)
            {
                report.setStatus(DunningReportStatusEnum.PENDING_INDEX);
            }
            //AbstractDunningReportVisitor.updateReport(context, report);
            resetVisitedReports();
            resetReports();
        }
    }


    /**
     * {@inheritDoc}
     */
    protected void finalize() throws Throwable
    {
        super.finalize();
    }
}
