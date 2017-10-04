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
package com.trilogy.app.crm.dunning.visitor.accountprocessing;

import java.util.Date;

import com.trilogy.app.crm.dunning.DunningAccountProcessingAgent;
import com.trilogy.app.crm.dunning.DunningAgent;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningXStatementVisitor;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.ContextAgent;


/**
 * Visitor responsible to visit XStatements during dunning process. This visitor will execute the
 * XStatement to retrieve the accounts to be processed and give them to
 * DunningProcessThreadPoolVisitor to process.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningProcessingAccountXStatementVisitor extends AbstractDunningXStatementVisitor
{
    /**
     * Creates a DunningProcessingAccountXStatementVisitor visitor.
     * @param runningDate
     * @param lifecycleAgent
     */
    public DunningProcessingAccountXStatementVisitor(Date runningDate, LifecycleAgentSupport lifecycleAgent)
    {
        super(runningDate, new DunningProcessingAccountVisitor(runningDate), lifecycleAgent);
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

	@Override
	protected DunningAgent getExecuter() {
		return new DunningAccountProcessingAgent(this);
	}
}
