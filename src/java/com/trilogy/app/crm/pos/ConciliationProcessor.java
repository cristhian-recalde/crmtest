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

import java.util.Date;

import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;

/**
 * File Writer for Conciliation POS record
 * 
 * For testing purposes, a currentDate parameter was added to the Conciliation Processor.
 * The currentDate will replace all the "new Date()" initializations, so that we may 
 * run this cron task for dates other than today.  
 * 
 * Not delimited, fixed length reports are generated using:
 * 1) CSV Home with blank delimiter
 * 2) Customized GET BODY tags in the bean model file
 * 
 * @author Angie Li
 */
public class ConciliationProcessor extends POSFileWriteUtils implements PointOfSaleFileWriter 
{
    /**
     * Initializes the processor with a date and the log writer 
     * @param ctx
     * @param date
     */
    public ConciliationProcessor(Context ctx, final Date date, LifecycleAgentScheduledTask agent)
    {
        // Initialize the Log File Writer
        logWriter_ = new POSLogWriter(ctx, "ConciliationProcessor", this);
        currentDate_ = date;
        agent_ = agent;
    }
    
    public ConciliationProcessor(Context ctx, final Date date)
    {
        this(ctx, date, null);
    }
    
    public void writeFile(Context ctx) throws HomeException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "writeFile");
        
        // Get all Payment Transactions done within the last day
        final ConciliationGzipCSVHome csvHome = getConciliationCSVHome(ctx);
            
        Date midnightToday = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(currentDate_);
        Date midnightYesterday = CalendarSupportHelper.get(ctx).getDayBefore(midnightToday);
        
        Home home = CoreTransactionSupportHelper.get(ctx).getTransactionsHome(
                ctx,
                True.instance(),
                midnightYesterday,
                midnightToday);
        
        ConciliationVisitor visitor = new ConciliationVisitor(csvHome, getLogger(), agent_);
        try
        {
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
        String filename = PointOfSale.POS_CONCILIATION_FILE;
        PointOfSaleConfiguration config = (PointOfSaleConfiguration) ctx.get(PointOfSaleConfiguration.class);
        if (config != null
                && config.getConciliationFileName().trim().length() > 0)
        {
            filename = config.getConciliationFileName().trim();
        }
        return filename;
    }
    
    /**
     * Initializes the POS Conciliation file and creates a CSVHome that writes to it.
     * Returns the ConciliationCSVHome.
     * 
     * @param ctx
     * @return
     * @throws HomeException
     */
    protected ConciliationGzipCSVHome getConciliationCSVHome(Context ctx) throws HomeException
    {
        String filePath = initFileWriter(ctx, this);
        /* Write to a GZIP compressed file.  Use ConciliationCSVHome to save 
         * it as an uncompressed file. */
        /* Not delimited, fixed length reports are achieved by a customized CSVSupport used by the GzipHome */
        return new ConciliationGzipCSVHome(ctx, filePath);
    }
    
    /**
     * Append a summary for Conciliation processor to the end of the log
     * @param numProcessed
     * @param numSuccessProcessed
     * @param el
     */
    private void writeSummary(int numProcessed, int numSuccessProcessed, ExceptionListener el)
    {
        getLogger().writeToLog("*********************SUMMARY***************************");
        getLogger().writeToLog("Number of Payment Transactions Processed: " + numProcessed);
        getLogger().writeToLog("Successful Conciliation Records Processed: " + numSuccessProcessed);
        getLogger().writeToLog("Failed Conciliation Records Processed: " + (numProcessed - numSuccessProcessed));
        if (el instanceof POSExceptionListener)
        {
            getLogger().writeToLog("Total Number of Errors during processing: " + ((POSExceptionListener)el).getNumberOfErrors());
        }
        getLogger().writeToLog("*********************SUMMARY***************************");
    }
    
    /** The current date passed in from the Cron task**/
    private Date currentDate_;
    
    private final LifecycleAgentScheduledTask agent_;

    private static final String PM_MODULE = ConciliationProcessor.class.getName();
}
