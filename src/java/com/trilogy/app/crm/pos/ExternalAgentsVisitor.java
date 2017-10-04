/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.pos;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * Visits all the AccountAccumulators and extracts the External Agents information
 * Uses the ExternalAgentsCSVHome to write to the POS External Agents file.
 * 
 * @author Angie Li
 */
public class ExternalAgentsVisitor implements Visitor 
{
	public ExternalAgentsVisitor(ExternalAgentsGzipCSVHome home, POSLogWriter logWriter, LifecycleAgentScheduledTask agent)
    {
        numberProcessed = 0;
        numberSuccessfullyProcessed = 0;
        logger = logWriter;
        csvHome = home;
        agent_ = agent;
    }
    
    public ExternalAgentsVisitor(ExternalAgentsGzipCSVHome home, POSLogWriter logWriter)
    {
        this(home, logWriter, null);
    }
    
    public void visit(Context ctx, Object obj)
    {
        if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()) && !LifecycleStateEnum.RUN.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running. Remaining accounts will be processed next time.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }

        final PMLogMsg pmLogMsg = new PMLogMsg(ExternalAgentsVisitor.class.getName(), "visit");
        numberProcessed++;
        // Log OMs
        new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_EXTERNALAGENTS_RECORD_ATTEMPT).log(ctx);
        
        AccountAccumulator record = (AccountAccumulator) obj;
        
        ExternalAgents externalAgent = new ExternalAgents();
        //Is it prefearable to store the Name and the Address in the Account Accumulator OR
        //to look up the account again here?
        Account account = POSReportSupport.getAccount(ctx, record.getBan(), logger);
        
        externalAgent = updateExternalAgent(ctx, externalAgent, account, record);
        
        //Store record to file
        try
        {
            csvHome.create(ctx, externalAgent);
            numberSuccessfullyProcessed++;
            logger.writeToLog("Extracted account=" + record.getBan());
            
            // Log OMs
            new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_EXTERNALAGENTS_RECORD_SUCCESS).log(ctx);
        }
        catch (HomeException e)
        {
            POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                    "Failed to save external agents record ban=" + record.getBan(), 
                    e);
            logger.thrown(pe);
            
            // Log OMs
            new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_EXTERNALAGENTS_RECORD_FAILURE).log(ctx);
        }
        finally
		{
        	pmLogMsg.log(ctx);
		}
    }
    
    /**
     * Returns the number of subscribers processed by the visitor
     * @return
     */
    public int getNumberProcessed()
    {
        return numberProcessed;
    }
    
    /**
     * Returns the number of subscribers processed by the visitor
     * for which external agents records were successfully made
     * @return
     */
    public int getNumberSuccessfullyProcessed()
    {
        return numberSuccessfullyProcessed;
    }
    
    /**
     * Updates the External Agents record with the provided parameters.
     * @param ctx
     * @param externalAgent
     * @param account
     * @param record
     * @return
     */
    private ExternalAgents updateExternalAgent(Context ctx, 
            ExternalAgents externalAgent, 
            Account account, 
            AccountAccumulator record)
    {
        if (record != null)
        {
            externalAgent.setBan(POSReportSupport.formatValue(record.getBan(), ExternalAgents.BAN_WIDTH));
            externalAgent.setBalance(POSReportSupport.formatAmount(ctx, account.getCurrency(), record.getBalance(), record.getBan(), logger, BALANCE_MAX));
            externalAgent.setDateOfExtraction(POSReportSupport.formatDate(record.getDateOfExtraction()));
            if (account != null)
            {
                externalAgent.setName(POSReportSupport.formatAccountName(account));
                externalAgent.setAddress(POSReportSupport.formatAccountAddress(account));
                externalAgent.setSpid(String.valueOf(account.getSpid()));
            }
        }
        return externalAgent;
    }
        
    /** the number of accounts processed by this visitor */
    private int numberProcessed;
    /** the number of accounts processed successfully into external agents records by this visitor */
    private int numberSuccessfullyProcessed;
    /** Log Writer */
    protected POSLogWriter logger;
    /** Gzipped CSVHome */
    protected ExternalAgentsGzipCSVHome csvHome;
    /** Processor which invoked this visitor */
    private static final String PROCESSOR_NAME = "ExternalAgentsProcessor";
    private static int BALANCE_MAX = 15;
    private final LifecycleAgentScheduledTask agent_;
    /**
	 * 
	 */
	private static final long serialVersionUID = 7961656870706899855L;
}
