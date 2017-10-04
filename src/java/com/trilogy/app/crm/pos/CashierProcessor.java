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
 * File Writer for the Cashier POS record
 * 
 * Not delimited, fixed length reports are generated using:
 * 1) CSV Home with blank delimiter
 * 2) Customized GET BODY tags in the bean model file
 * 
 * @author Angie Li
 */
public class CashierProcessor extends POSFileWriteUtils implements PointOfSaleFileWriter 
{
    /**
     * Initializes the Log Writer for this processor
     * @param ctx
     */
    public CashierProcessor(Context ctx, LifecycleAgentScheduledTask agent)
    {
        // Initialize the Log File Writer
        logWriter_ = new POSLogWriter(ctx, "CashierProcessor", this);
        agent_ = agent;
    }
    
    public CashierProcessor(Context ctx)
    {
        this(ctx, null);
    }
    /**
     * Visits all the AccountAccumulators and extracts the Cashier Information.
     * Uses the CashierCSVHome to write to the POS Cashier file.
     */
    public void writeFile(Context ctx) throws HomeException 
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "writeFile");
        
        Home accumulatorHome = (Home) ctx.get(AccountAccumulatorHome.class);
        final CashierGzipCSVHome csvHome = getCashierCSVHome(ctx);

        //CashierAccountVisitor accountVisitor = new CashierAccountVisitor(csvHome, getLogger());
        CashierAcumulatorInformationVisitor accountVisitor = new CashierAcumulatorInformationVisitor(csvHome, getLogger(), agent_);
        
        try
        {
            accumulatorHome.forEach(ctx, accountVisitor);
            
            try 
            {
                //Write the contents of the entire home to the CSV file only once.
                csvHome.writeFile(ctx);
            }
            catch (HomeException e)
            {
                POSProcessorException pe = new POSProcessorException(PM_MODULE, e);
                getLogger().thrown(pe);
            }
            
            writeSummary(accountVisitor.getNumberProcessed(), accountVisitor.getNumberSuccessfullyProcessed(), getLogger().getExceptionListener());
            
        }
        finally 
        {
            pmLogMsg.log(ctx);
            getLogger().closeLogFileWriter();
        }
    }

    
    public String getFileName(Context ctx) 
    {
        String filename = PointOfSale.POS_CASHIER_FILE;
        PointOfSaleConfiguration config = (PointOfSaleConfiguration) ctx.get(PointOfSaleConfiguration.class);
        if (config != null
                && config.getCashierFileName().trim().length() > 0)
        {
            filename = config.getCashierFileName().trim();
        }
        return filename;
    }

    /**
     * Initializes the POS Cashier file and creates a CSVHome that writes to it.
     * Returns the CashierCSVHome.
     * 
     * @param ctx
     * @return
     * @throws HomeException
     */
    protected CashierGzipCSVHome getCashierCSVHome(Context ctx) throws HomeException
    {
        String filePath = initFileWriter(ctx, this);
        
        /* Write to a GZIP compressed file.  Use CashierCSVHome to save 
         * it as an uncompressed file. */ 
        /* Not delimited, fixed length reports are achieved by a customized CSVSupport used by the GzipHome */
        return new CashierGzipCSVHome(ctx, filePath);
    }
    
    /**
     * Append a summary for Cashier processor to the end of the log
     * @param numProcessed
     * @param numSuccessProcessed
     * @param el
     */
    private void writeSummary(int numProcessed, int numSuccessProcessed, ExceptionListener el)
    {
        getLogger().writeToLog("*********************SUMMARY***************************");
        getLogger().writeToLog("Number of Subscribers Processed: " + numProcessed);
        getLogger().writeToLog("Successful Cashier Records Processed: " + numSuccessProcessed);
        getLogger().writeToLog("Failed Cashier Records Processed: " + (numProcessed - numSuccessProcessed));
        if (el instanceof POSExceptionListener)
        {
            getLogger().writeToLog("Total Number of Errors: " + ((POSExceptionListener)el).getNumberOfErrors());
        }
        getLogger().writeToLog("*********************SUMMARY***************************");
    }
    
    private final LifecycleAgentScheduledTask agent_;

    private static final String PM_MODULE = CashierProcessor.class.getName();
}
