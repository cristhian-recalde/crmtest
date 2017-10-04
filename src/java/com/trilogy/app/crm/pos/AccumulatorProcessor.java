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

import com.trilogy.app.crm.bas.directDebit.EnhancedParallVisitor;
import com.trilogy.app.crm.support.PointOfSaleConfigurationSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.ParallelVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
/**
 * Processes the collection of accounts and updates/creates the 
 * Account Accumulators. 
 * @author Angie Li
 */
public class AccumulatorProcessor 
{
    /**
     * Initializes the processor with a AccountHome predicate filter
     * @param ctx
     * @param predicate - predicate used to filter AccountHome to which we apply AccountAccumulatorVisitor
     */
    public AccumulatorProcessor(Context ctx, Predicate predicate, String message)
    {
        predicate_ = predicate;
        // Initialize the Log File Writer
        logWriter_ = new POSLogWriter(ctx, message);
        processName_ = message;
    }
    
    /**
     * Visit collection of accounts given by the predicate and create/update 
     * an AccountAccumulator record
     * @param ctx
     * @throws HomeException
     */
    public void update(Context ctx, final Date currentDate, Home home, Visitor visitor) throws HomeException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "update");
        
        getLogger().writeToLog(processName_ + " for date=" + currentDate.toString() + " has begun.");
        
        PointOfSaleConfiguration config = PointOfSaleConfigurationSupport.getPOSConfig(ctx);
        
        home = home.where(ctx, predicate_);
        EnhancedParallVisitor pv = null;
        try{
            pv = new EnhancedParallVisitor(config.getThreadCount(), visitor);
            home.forEach(ctx, pv);
        }
        
        finally
        {
    	        	 try
    	             {
    	        		pv.shutdown(EnhancedParallVisitor.TIME_OUT_FOR_SHUTTING_DOWN);
    	             }
    	             catch (final Exception e)
    	             {
    	                 LogSupport.major(ctx, this, "Exception caught during wait for completion of all Accumulator Processor Threads", e);
    	             }
        }
        
        CountProcessVisitor counter = (CountProcessVisitor) visitor;
                
        writeSummary(counter.getNumberVisited(), counter.getNumberProcessed(), counter.getNumberSuccessfullyProcessed(), getLogger().getExceptionListener());
        
        pmLogMsg.log(ctx);
        getLogger().closeLogFileWriter();
    }
    
    /**
     * Returns the Log File Writer for this processor.
     * @return
     */
    public POSLogWriter getLogger()
    {
        return logWriter_;
    }
    
    /** Log Writer for this processor **/
    protected POSLogWriter logWriter_;
    
    /**
     * Append a summary for Cashier processor to the end of the log
     * @param numProcessed
     * @param numSuccessProcessed
     * @param el
     */
    private void writeSummary(int numVisited, int numProcessed, int numSuccessProcessed, ExceptionListener el)
    {
        getLogger().writeToLog("*********************SUMMARY***************************");
        getLogger().writeToLog("Number of Beans Visited: " + numVisited);
        getLogger().writeToLog("Number of Beans Accumulated: " + numProcessed);
        getLogger().writeToLog("Successful Accumulation Records saved: " + numSuccessProcessed);
        getLogger().writeToLog("Failed Accumulations: " + (numProcessed - numSuccessProcessed));
        if (el instanceof POSExceptionListener)
        {
            getLogger().writeToLog("Total Number of Errors during processing: " + ((POSExceptionListener)el).getNumberOfErrors());
        }
        getLogger().writeToLog("*********************SUMMARY***************************");
    } 
    
    /** Predicate used to filter Account Home **/
    private Predicate predicate_;
    
    private static final String PM_MODULE = AccumulatorProcessor.class.getName();
    
    /**
     * For log messages purpose to distinguish between MSISDN and Account visitors
     */
    private String processName_;
    
    
}
