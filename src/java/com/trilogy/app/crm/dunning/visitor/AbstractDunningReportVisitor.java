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

import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Visitor responsible to visit reports during dunning. For each report, this visitor will
 * generate an XStatement to retrieve the accounts to be processed and give them to an
 * AbstractDunningXStatementVisitor to process.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public abstract class AbstractDunningReportVisitor implements Visitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new AbstractDunningReportVisitor visitor.
     * 
     * @param lifecycleAgent
     */
    public AbstractDunningReportVisitor(final LifecycleAgentSupport lifecycleAgent)
    {
        lifecycleAgent_ = lifecycleAgent;
    }


    /**
     * Returns the visitor responsible to visit each XStatement generated to retrieve the
     * accounts to visit during report generation.
     * 
     * @param context
     * @param reports
     * @return
     */
    abstract protected AbstractDunningXStatementVisitor getXStatementVisitor(final DunningReport report);


    /**
     * Validate the report for processing.
     * 
     * @param context
     * @param report
     */
    abstract protected void validate(final Context context, final DunningReport report);


    /**
     * Calculate the XStatement to be passed to the next visitor.
     * 
     * @param context
     * @param report
     * @return
     */
    abstract protected XStatement calculateXStatement(final Context context, final DunningReport report);


        
    public static void updateReport(Context ctx, DunningReport oldReport,DunningReport updatedReport)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Updating report for '");
            sb.append(CoreERLogger.formatERDateDayOnly(oldReport.getReportDate()));
            sb.append("' on database");
            LogSupport.debug(ctx, AbstractDunningReportVisitor.class, sb.toString());
        }
        try
        {
        	updatedReport.setContext(ctx);
        	updatedReport.setRecords(null);
        	
        	HomeSupport homeHelper = HomeSupportHelper.get(ctx);
        	homeHelper.createBean(ctx, updatedReport);
        	homeHelper.removeBean(ctx, oldReport);
        }
        catch (HomeException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to update report: ");
            sb.append(e.getMessage());
            LogSupport.minor(ctx, AbstractDunningReportVisitor.class, sb.toString(), e);
        }
    }


    protected LifecycleAgentSupport getLifecycleAgent()
    {
        return lifecycleAgent_;
    }

    private LifecycleAgentSupport lifecycleAgent_;
}
