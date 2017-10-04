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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;

/**
 * Gets a transient collection of AccountInfrmations and creates a line for each account information
 * author amedina
 */
public class CashierAcumulatorInformationVisitor implements Visitor 
{

    public CashierAcumulatorInformationVisitor(CashierGzipCSVHome home, POSLogWriter logWriter, LifecycleAgentScheduledTask agent)
    {
        numberProcessed = 0;
        numberSuccessfullyProcessed = 0;
        logger = logWriter;
        csvHome = home;
        agent_ = agent;
    }
    
    public CashierAcumulatorInformationVisitor(CashierGzipCSVHome home, POSLogWriter logWriter)
    {
        this(home, logWriter, null);
    }

	
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.visitor.Visitor#visit(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException 
	{
		if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()) && !LifecycleStateEnum.RUN.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running. Remaining accounts will be processed next time.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }

        final PMLogMsg pmLogMsg = new PMLogMsg(CashierAcumulatorInformationVisitor.class.getName(), "visit");
        // Log OMs
        new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_CASHIER_RECORD_ATTEMPT).log(ctx);
        

        final AccountAccumulator record = (AccountAccumulator) obj;
        AccountInformation info = null;
        //Write the record
        try
        {

	        Collection acumulators = POSReportSupport.getAccountAccumulatorInformations(ctx, record.getBan(), logger);
	        
	        if (!acumulators.isEmpty())
	        {
		        Iterator iter = acumulators.iterator();
	
		        while (iter.hasNext())
		        {
		        	info = (AccountInformation) iter.next();
		        	writeLine(ctx, info, record, pmLogMsg);
		        }
	        }
        }
        catch (HomeException e)
        {
        	String id = (info == null)? record.getBan(): info.getMSISDN();
            POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                    "Failed to write cashier record to Cashier POS file for  msisdn=" + id + " Exception : " + e.getMessage(), 
                    e);
            logger.thrown(pe);
            
            //Log OMs
            new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_CASHIER_RECORD_FAILURE).log(ctx);
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
	}

    /**
	 * @param ctx
	 * @param info
	 * @param record
     * @param pmLogMsg
     * @throws HomeException
     * @throws HomeInternalException
	 */
	private void writeLine(Context ctx, AccountInformation info, AccountAccumulator record, PMLogMsg pmLogMsg) 
	throws HomeInternalException, HomeException 
	{
        numberProcessed++;
        boolean create = false;

        final String accountName = (info != null? POSReportSupport.formatAccountName(info) : "");
        final String accountAddress = (info != null ? POSReportSupport.formatAccountAddress(info) : "");

        Cashier cashier = null;
        try
        {
            cashier = (Cashier) csvHome.find(ctx, new CashierID(record.getBan(),
                    POSReportSupport.formatAccountMSISDN(info.getMSISDN())));
        }
        catch(HomeException e)
        {
            logger.writeToLog("Failed to look up Cashier record for msisdn=" + info.getMSISDN());
        }
        if (cashier == null)
        {
            cashier = new Cashier();
            create = true;
        }
        
        cashier = updateCashier(ctx, cashier, accountName, 
                info.getMSISDN(), accountAddress, record, info.getCurrency(), info.getSpid());
        
        if (create)
        {
            csvHome.create(ctx, cashier);
        }
        else
        {
            csvHome.store(ctx, cashier);
        }
        
        logger.writeToLog("Extracted msisdn=" + info.getMSISDN());
        numberSuccessfullyProcessed++;
        
        //Log OMs
        new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_CASHIER_RECORD_SUCCESS).log(ctx);
	}


    /**
     * Updates the Cashier record with the parameters provided.
     * @param cashier
     * @param accountName
     * @param msisdn
     * @param record
     * @param string
     * @return
     */
    private Cashier updateCashier(Context ctx,
            Cashier cashier, 
            String accountName, 
            String msisdn, 
            String address,
            AccountAccumulator record, 
			String currencyId,
			int spid)
    {
        cashier.setName(POSReportSupport.formatValue(accountName, Cashier.NAME_WIDTH));
        cashier.setMsisdn(POSReportSupport.formatAccountMSISDN(msisdn));
        cashier.setBan(record.getBan());
        cashier.setBalance(POSReportSupport.formatAmount(ctx, currencyId, record.getBalance(), record.getBan(), logger, BALANCE_WIDTH));
        cashier.setAddress(POSReportSupport.formatValue(address, Cashier.ADDRESS_WIDTH));
        cashier.setDateOfExtraction(POSReportSupport.formatDate(record.getDateOfExtraction()));
        cashier.setCurrDate(POSReportSupport.formatDate(new Date()));
        cashier.setLastPaid(POSReportSupport.formatAmount(ctx, currencyId, record.getLastPaymentAmount(), record.getBan(), logger, LASTPAID_WIDTH));
        cashier.setLastDate(POSReportSupport.formatDate(record.getLastPaymentDate()));
        cashier.setArrears(POSReportSupport.formatAmount(ctx, currencyId, record.getBalanceForward(), record.getBan(), logger, ARREARS_WIDTH));
        cashier.setSpid(String.valueOf(spid));
        return cashier;
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
     * for which cashier records were successfully made
     * @return
     */
    public int getNumberSuccessfullyProcessed()
    {
        return numberSuccessfullyProcessed;
    }
    
    /** the number of subscribers processed by this visitor */
    private int numberProcessed;
    /** the number of subscribers processed successfully into cashier records by this visitor */
    private int numberSuccessfullyProcessed;
    /** Log Writer **/
    protected POSLogWriter logger;
    /** Gzipped CSVHome **/
    protected CashierGzipCSVHome csvHome;
    /** Processor which invoked this visitor **/
    private static final String PROCESSOR_NAME = "CashierProcessor";
    private static int BALANCE_WIDTH = 15;
    private static int LASTPAID_WIDTH = 10;
    private static int ARREARS_WIDTH = 15;
    private final LifecycleAgentScheduledTask agent_;

}
