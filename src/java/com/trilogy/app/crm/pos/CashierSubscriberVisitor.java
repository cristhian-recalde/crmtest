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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * Visits the each Subscriber and creates Cashier records.
 * 
 * @author Angie Li 
 */
public class CashierSubscriberVisitor implements Visitor 
{
	public CashierSubscriberVisitor(
            String name, 
            String address,
            AccountAccumulator aRecord,
            CashierGzipCSVHome home, 
            POSLogWriter logWriter)
    {
        numberProcessed = 0;
        numberSuccessfullyProcessed = 0;
        accountName = name;
        accountAddress = address;
        record = aRecord;
        logger = logWriter;
        csvHome = home;
    }

    public void visit(Context ctx, Object obj)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(CashierSubscriberVisitor.class.getName(), "visit");
        // Log OMs
        new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_CASHIER_RECORD_ATTEMPT).log(ctx);
        
        numberProcessed++;
        boolean create = false;
        
        Subscriber subscriber = (Subscriber) obj;
         
        //Write the record
        try
        {
            Cashier cashier = null;
            try
            {
                cashier = (Cashier) csvHome.find(POSReportSupport.formatAccountMSISDN(subscriber.getMSISDN()));
            }
            catch(HomeException e)
            {
                logger.writeToLog("Failed to look up Cashier record for msisdn=" + subscriber.getMSISDN());
            }
            if (cashier == null)
            {
                cashier = new Cashier();
                create = true;
            }
            
            cashier = updateCashier(ctx, cashier, accountName, 
                    subscriber.getMSISDN(), accountAddress, record);
            
            if (create)
            {
                csvHome.create(ctx, cashier);
            }
            else
            {
                csvHome.store(ctx, cashier);
            }
            
            logger.writeToLog("Extracted msisdn=" + subscriber.getMSISDN());
            numberSuccessfullyProcessed++;
            
            //Log OMs
            new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_CASHIER_RECORD_SUCCESS).log(ctx);
        }
        catch (HomeException e)
        {
            POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                    "Failed to write cashier record to Cashier POS file for sub-id=" + subscriber.getId() + " msisdn=" + subscriber.getMSISDN() + " Exception : " + e.getMessage(), 
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
     * Updates the Cashier record with the parameters provided.
     * @param cashier
     * @param accountName
     * @param msisdn
     * @param record
     * @return
     */
    private Cashier updateCashier(Context ctx,
            Cashier cashier, 
            String accountName, 
            String msisdn, 
            String address,
            AccountAccumulator record)
    {
        cashier.setName(accountName);
        cashier.setMsisdn(POSReportSupport.formatAccountMSISDN(msisdn));
        cashier.setBan(POSReportSupport.formatValue(record.getBan(), Cashier.BAN_WIDTH));
        cashier.setBalance(POSReportSupport.formatAmount(ctx, record.getBalance(), record.getBan(), logger, BALANCE_WIDTH));
        cashier.setAddress(address);
        cashier.setDateOfExtraction(POSReportSupport.formatDate(record.getDateOfExtraction()));
        cashier.setCurrDate(POSReportSupport.formatDate(new Date()));
        cashier.setLastPaid(POSReportSupport.formatAmount(ctx, record.getLastPaymentAmount(), record.getBan(), logger, LASTPAID_WIDTH));
        cashier.setLastDate(POSReportSupport.formatDate(record.getLastPaymentDate()));
        cashier.setArrears(POSReportSupport.formatAmount(ctx, record.getBalanceForward(), record.getBan(), logger, ARREARS_WIDTH));
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
    
    /** the number of accounts processed by this visitor */
    private int numberProcessed;
    /** the number of accounts processed successfully into cashier records by this visitor */
    private int numberSuccessfullyProcessed;
    /** Name on account profile that is being extracted **/
    private String accountName;
    /** Address on account profile that is being extracted **/
    private String accountAddress;
    /** Account Accumulation record being extracted **/
    private AccountAccumulator record;
    /** Log Writer **/
    protected POSLogWriter logger;
    /** Gzipped CSVHome **/
    protected CashierGzipCSVHome csvHome;
    /** Processor which invoked this visitor **/
    private static final String PROCESSOR_NAME = "CashierProcessor";
    private static int BALANCE_WIDTH = 15;
    private static int LASTPAID_WIDTH = 10;
    private static int ARREARS_WIDTH = 15;
    /**
	 * 
	 */
	private static final long serialVersionUID = 8168019754067756098L;
}
