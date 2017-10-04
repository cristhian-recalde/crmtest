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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;

/*
 * Transaction Visitor that processes Standard Payment Transactions
 * to create Conciliation records for POS report generation.
 * 
 * @author Angie Li
 */
public class ConciliationVisitor implements Visitor
{
    public ConciliationVisitor(ConciliationGzipCSVHome home, POSLogWriter logWriter, LifecycleAgentScheduledTask agent)
    {
        numberProcessed = 0;
        numberSuccessfullyProcessed = 0;
        logger = logWriter;
        csvHome = home;
        agent_ = agent;
    }
    
    public ConciliationVisitor(ConciliationGzipCSVHome home, POSLogWriter logWriter)
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

        Transaction trans = (Transaction)obj;
        
        if (CoreTransactionSupportHelper.get(ctx).isStandardPayment(ctx, trans))
        {
            final PMLogMsg pmLogMsg = new PMLogMsg(ConciliationVisitor.class.getName(), "visit");
            // Log OMs
            new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_CONCILIATION_RECORD_ATTEMPT).log(ctx);
            numberProcessed++;
            
            Conciliation conciliation = new Conciliation();
            conciliation.setPayNum(PointOfSale.trimLength(String.valueOf(trans.getReceiptNum()), PAY_NUM_MAX_LENGTH, true));
            Date date = (trans.getTransDate() != null) ? trans.getTransDate() : new Date(0);
            conciliation.setTxnDate(formatDate(date));
            /* Since Payment Amounts in Transactions are always negative then we need the absolute 
             * value for Conciliation records */
            conciliation.setAmount(POSReportSupport.formatAmount(ctx, Math.abs(trans.getAmount()), trans.getBAN(), logger, AMOUNT_MAX_LENGTH));
            
            // Write record to file
            try
            {
                csvHome.create(ctx, conciliation);
                numberSuccessfullyProcessed++;
                logger.writeToLog("Extracted transaction=" + trans.getReceiptNum());
                
                //Log OMs
                new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_CONCILIATION_RECORD_SUCCESS).log(ctx);
            }
            catch (HomeException e)
            {
                POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                        "Failed to write conciliation record for trans=" + trans.getReceiptNum(), 
                        e);
                logger.thrown(pe);
                
                //Log OMs
                new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_CONCILIATION_RECORD_FAILURE).log(ctx);
            }
            finally
            {
                pmLogMsg.log(ctx);
            }
        }
    }

    /**
     * Formats the Date to MM/dd/yy hh:mm:ss
     * @param date
     * @return
     */
    public String formatDate(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss");
        return dateFormat.format(date);
    }
    
    /**
     * Returns the number of payment transactions processed by the visitor
     * @return
     */
    public int getNumberProcessed()
    {
        return numberProcessed;
    }
    
    /**
     * Returns the number of payment transactions processed by the visitor
     * for which conciliation records were successfully made
     * @return
     */
    public int getNumberSuccessfullyProcessed()
    {
        return numberSuccessfullyProcessed;
    }
    
    /** the number of payment transactions processed by this visitor */
    private int numberProcessed;
    /** the number of payment transactionsprocessed successfully into conciliation records by this visitor */
    private int numberSuccessfullyProcessed;
    /** Log Writer **/
    protected POSLogWriter logger;
    /** Gzipped CSVHome **/
    protected ConciliationGzipCSVHome csvHome;
    /** Processor which invoked this visitor **/
    private static final String PROCESSOR_NAME = "ConciliationProcessor";
    /** Pay Num field's max length */
    private static final int PAY_NUM_MAX_LENGTH = 13;
    private static final int AMOUNT_MAX_LENGTH = 15;
    private final LifecycleAgentSupport agent_;
}
