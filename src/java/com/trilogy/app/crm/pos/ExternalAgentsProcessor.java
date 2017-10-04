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

import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * File Writer for the External Agents POS record
 * 
 * Not delimited, fixed length reports are generated using:
 * 1) CSV Home with blank delimiter
 * 2) Customized GET BODY tags in the bean model file
 * 
 * @author Angie Li
 */
public class ExternalAgentsProcessor extends POSFileWriteUtils implements PointOfSaleFileWriter 
{
    /**
     * Initializes the processor with a log writer
     * @param ctx
     */
    public ExternalAgentsProcessor(Context ctx, LifecycleAgentScheduledTask agent)
    {
        logWriter_ = new POSLogWriter(ctx, "ExternalAgentsProcessor", this);
        agent_ = agent;
    }

    public ExternalAgentsProcessor(Context ctx)
    {
        this(ctx, null);
    }

    
    /**
     * Uses an ExternalAgentsVisitor to go through all the AccountAccumulator
     * records and extracts the External Agents information.
     * Uses the ExternalAgentsCSVHome to write to the POS External Agents file.
     */
    public void writeFile(Context ctx) throws HomeException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "writeFile");
        
        Home home = (Home) ctx.get(AccountAccumulatorHome.class);
        final ExternalAgentsGzipCSVHome csvHome = getExternalAgentsCSVHome(ctx);

        try
        {
            ExternalAgentsVisitor visitor = new ExternalAgentsVisitor(csvHome, getLogger(), agent_);
            home.forEach(ctx, visitor);
            
            try
            {
                // Write the contents of the entire home to the CSV file only once.
                csvHome.writeFile(ctx);
            }
            catch (HomeException e)
            {
                POSProcessorException pe = new POSProcessorException(PM_MODULE, e);
                getLogger().thrown(pe);
            } 
            
            writeSummary(visitor.getNumberProcessed(), visitor.getNumberSuccessfullyProcessed(), getLogger().getExceptionListener());
        
        }
        finally 
        {
            pmLogMsg.log(ctx);
            getLogger().closeLogFileWriter();
        }
    }
    
    public String getFileName(Context ctx)
    {
        String filename = PointOfSale.POS_EXTERNAL_AGENTS_FILE;
        PointOfSaleConfiguration config = (PointOfSaleConfiguration) ctx.get(PointOfSaleConfiguration.class);
        if (config != null 
                && config.getExternalAgentFileName().trim().length() > 0)
        {
            filename = config.getExternalAgentFileName().trim();
        }
        return filename;
    }
    
    /**
     * Initializes the POS External Agents file and creates a CSVHome that writes to it.
     * Returns the ExternalAgentsCSVHome.
     * 
     * @param ctx
     * @return
     * @throws HomeException
     */
    protected ExternalAgentsGzipCSVHome getExternalAgentsCSVHome(Context ctx) throws HomeException
    {
        String filePath = initFileWriter(ctx, this);
        /* Write to a GZIP compressed file.  Use ExternalAgentsCSVHome to save 
         * it as an uncompressed file. */
        /* Not delimited, fixed length reports are achieved by a customized CSVSupport used by the GzipHome */
        return new ExternalAgentsGzipCSVHome(ctx, filePath);
    }
    
    /**
     * Append a summary for External Agents processor to the end of the log
     * @param numProcessed
     * @param numSuccessProcessed
     * @param el
     */
    private void writeSummary(int numProcessed, int numSuccessProcessed, ExceptionListener el)
    {
        getLogger().writeToLog("*********************SUMMARY***************************");
        getLogger().writeToLog("Number of Accounts Processed: " + numProcessed);
        getLogger().writeToLog("Successful External Agents Records Processed: " + numSuccessProcessed);
        getLogger().writeToLog("Failed External Agents Records Processed: " + (numProcessed - numSuccessProcessed));
        if (el instanceof POSExceptionListener)
        {
            getLogger().writeToLog("Total Number of Errors: " + ((POSExceptionListener)el).getNumberOfErrors());
        }
        getLogger().writeToLog("*********************SUMMARY***************************");
    }
    
    private final LifecycleAgentScheduledTask agent_;

    private static final String PM_MODULE = ExternalAgentsProcessor.class.getName();
}
