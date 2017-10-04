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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * Visits the AccountAccumulator and calls the CashierSubscriberVisitor 
 * to create Cashier records.
 * 
 * @author Angie Li 
 */
public class CashierAccountVisitor implements Visitor 
{
    public CashierAccountVisitor(CashierGzipCSVHome home, POSLogWriter logWriter)
    {
        numberProcessed = 0;
        numberSuccessfullyProcessed = 0;
        logger = logWriter;
        csvHome = home;
    }

    public void visit(Context ctx, Object obj)
    {
        final AccountAccumulator record = (AccountAccumulator) obj;
        
        Account account = POSReportSupport.getAccount(ctx, record.getBan(), logger);
        if (account == null)
        {
            String msg = "Failed to retrieve account ban=" 
                         + record.getBan() 
                         + ". Cannot set Name or Address into cashier record.";
            logger.writeToLog(msg);
        }
        final String accountName = (account != null? POSReportSupport.formatAccountName(account) : "");
        final String accountAddress = (account != null ? POSReportSupport.formatAccountAddress(account) : "");
        
        try
        {
            Home subscriberHome = (Home) POSReportSupport.getImmediateChildrenSubHome(ctx, record.getBan());
            if (subscriberHome != null)
            {
                CashierSubscriberVisitor subVisitor = new CashierSubscriberVisitor(accountName, accountAddress, record, csvHome, logger);
                subscriberHome.forEach(ctx, subVisitor);
                
                numberProcessed += subVisitor.getNumberProcessed();
                numberSuccessfullyProcessed += subVisitor.getNumberSuccessfullyProcessed();
            }
        }
        catch (HomeException e)
        {
            POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                                                                 "Failed to retrieve subscribers for ban=" + record.getBan(), 
                                                                 e);
            logger.thrown(pe);
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
}
