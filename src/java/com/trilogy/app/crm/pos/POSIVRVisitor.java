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

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * Visits all the AccountAccumulator records and extracts the IVR information
 * For the POS report generation.
 * 
 * @author Angie Li
 */
public class POSIVRVisitor implements Visitor
{
	public POSIVRVisitor(POSIVRExtractGzipCSVHome home, POSLogWriter logWriter, LifecycleAgentScheduledTask agent)
    {
        numberProcessed = 0;
        numberSuccessfullyProcessed = 0;
        logger = logWriter;
        csvHome = home;
        agent_ = agent;
    }

    public POSIVRVisitor(POSIVRExtractGzipCSVHome home, POSLogWriter logWriter)
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

        final PMLogMsg pmLogMsg = new PMLogMsg(POSIVRVisitor.class.getName(), "visit");
        //Log OMs
        new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_IVR_RECORD_ATTEMPT).log(ctx);
        numberProcessed++;
        
        SubscriberAccumulator record = (SubscriberAccumulator) obj;
        
        //Write record to file
        try
        {
            Msisdn msisdn = MsisdnSupport.getMsisdn(ctx,record.getMsisdn());
            
            if (msisdn.getSubscriberType() == SubscriberTypeEnum.PREPAID)
            {
            	LogSupport.debug(ctx, this,"Subscriber " + msisdn.getSubscriberID() + "with MSISDN " + msisdn.getMsisdn() + "is prepaid now skipping it");
                logger.writeToLog("Subscriber " + msisdn.getSubscriberID() + "with MSISDN " + msisdn.getMsisdn() + "is prepaid now skipping it");
            	return;
            }
            
            POSIVRExtract ivrExtract = new POSIVRExtract();
            
            Account account = POSReportSupport.getAccount(ctx, record.getBan(), logger);
            ivrExtract = updateIVR(ctx, ivrExtract, record, account);
            csvHome.create(ctx, ivrExtract);
            numberSuccessfullyProcessed++;
            logger.writeToLog("Extracted ban=" + record.getBan());
            
            //Log OMs
            new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_IVR_RECORD_SUCCESS).log(ctx);
        }
        catch (HomeException e)
        {
            POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                    "Failed to write IVR Extract record for ban=" + record.getBan() + " : " + e.getMessage(), 
                    e);
            logger.thrown(pe);
            
            //Log OMs
            new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_IVR_RECORD_FAILURE).log(ctx);
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
    }
    

    /**
     * Updates the External Agents record with the provided parameters.
     * @param ctx
     * @param externalAgent
     * @param account
     * @param record
     * @return
     */
    private POSIVRExtract updateIVR(
            Context ctx, 
            POSIVRExtract ivrExtract, 
            SubscriberAccumulator record,
            Account account)
    {
        if (record != null)
        {
            ivrExtract.setBalance(POSReportSupport.formatIVRAmount(ctx, record.getBalance(), record.getBan(), logger, BALANCE_MAX_LENGTH));
            ivrExtract.setBan(POSReportSupport.formatValue(record.getBan(), POSIVRExtract.BAN_WIDTH));
            ivrExtract.setDateOfExtraction(formatDate(record.getDateOfExtraction()));
            ivrExtract.setMsisdn(POSReportSupport.formatValue(record.getMsisdn(), POSIVRExtract.MSISDN_WIDTH));
            ivrExtract.setDateOfExtraction(formatDate(record.getDateOfExtraction()));
            ivrExtract.setInvoiceDueDate(formatDate(record.getInvoiceDueDate()));
            ivrExtract.setInvoiceAmount(POSReportSupport.formatAmount(ctx, record.getInvoiceAmount(), record.getBan(), logger, INVOICE_MAX_LENGTH));
            ivrExtract.setAdjustment(POSReportSupport.formatAmount(ctx, record.getAdjustments(), record.getBan(), logger, INVOICE_MAX_LENGTH));
            if (account!=null)
            {
                ivrExtract.setSpid(String.valueOf(account.getSpid()));
            }
        }
        return ivrExtract;
    }

    /**
     * Formats the Date to yyMMdd
     * @param date
     * @return a String displaying the date as yyMMdd
     */
    public String formatDate(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date);
    }
    
    /**
     * Returns the number of accounts processed by the visitor
     * @return
     */
    public int getNumberProcessed()
    {
        return numberProcessed;
    }
    
    /**
     * Returns the number of accounts processed by the visitor
     * for which cashier records were successfully made
     * @return
     */
    public int getNumberSuccessfullyProcessed()
    {
        return numberSuccessfullyProcessed;
    }
    
    /** the number of accounts processed by this visitor */
    private int numberProcessed;
    /** the number of accounts processed successfully into IVR records by this visitor */
    private int numberSuccessfullyProcessed;
    /** Log Writer */
    protected POSLogWriter logger;
    /** Gzipped CSVHome */
    protected POSIVRExtractGzipCSVHome csvHome;
    /** Processor which invoked this visitor */
    private static final String PROCESSOR_NAME = "POSIVRProcessor";
    private static int BALANCE_MAX_LENGTH = 13;
    private static int INVOICE_MAX_LENGTH = 10;
    /**
	 * 
	 */
	private static final long serialVersionUID = -338483231017325208L;
    private final LifecycleAgentSupport agent_;
    
}
