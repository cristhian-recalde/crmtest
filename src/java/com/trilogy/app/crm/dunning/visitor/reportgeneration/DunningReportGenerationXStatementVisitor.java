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

import com.trilogy.app.crm.dunning.DunningAgent;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningAccountProcessingAgent;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningXStatementVisitor;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.ContextAgent;


/**
 * Visitor responsible to visit XStatements during dunning report generation. This visitor will execute the
 * XStatement to retrieve the accounts to be processed and give them to
 * DunningProcessThreadPoolVisitor to process.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningReportGenerationXStatementVisitor extends AbstractDunningXStatementVisitor
{

    /**
     * Creates a DunningReportGenerationXStatementVisitor visitor.
     * @param report
     * @param lifecycleAgent
     */
    public DunningReportGenerationXStatementVisitor(DunningReport report, LifecycleAgentSupport lifecycleAgent)
    {
        super(report.getReportDate(), new DunningReportRecordGenerationAccountVisitor(report), lifecycleAgent);
        
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void onItemFailure()
    {
        
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void onItemSuccess()
    {
    }
    
    protected DunningAgent getExecuter()
    {
    	return new DunningAccountProcessingAgent(this);
    }

}
