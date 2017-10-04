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

import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.dunning.DunningAgent;
import com.trilogy.app.crm.dunning.DunningProcessContextAgent;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportRecordHome;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningXStatementVisitor;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;

/**
 * Visitor responsible to visit XStatements during dunning. This visitor will execute the
 * XStatement to retrieve the accounts to be processed and give them to
 * DunningProcessThreadPoolVisitor to process.
 * 
 * @author Marcio Marques
 * @since 9.0
 */

public class DunningReportProcessingXStatementVisitor extends AbstractDunningXStatementVisitor
{
    /**
     * Create a DunningReportProcessingXStatementVisitor visitor.
     * @param report
     * @param lifecycleAgent
     */
    public DunningReportProcessingXStatementVisitor(final DunningReport report, final LifecycleAgentSupport lifecycleAgent)
    {
        super(report.getReportDate(), new DunningReportRecordProcessingVisitor(report), lifecycleAgent);
        report_ = report;
        
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void onItemFailure()
    {
        report_.increaseFailedToProcessRecords();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void onItemSuccess()
    {
        report_.increaseSuccessfullyProcessedRecords();
    }
    
    private DunningReport report_;

	@Override
	protected DunningAgent getExecuter() {
		
		return new DunningProcessContextAgent(this);
	}
	
	@Override
	protected Home getHomeToVisit(Context context)
	{
		Home drrHome = (Home) context.get(DunningReportRecordHome.class);
		return drrHome;
	}
}
