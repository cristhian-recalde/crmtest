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
package com.trilogy.app.crm.home;

import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.WalletReport;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;

/**
 * Creates the ghost wallet report
 * @author arturo.medina@redknee.com
 *
 */
public class WalletReportPredictionHome extends HomeProxy
{
    /**
     * 
     */
    private static final long serialVersionUID = 864048220071773696L;

    /**
     * Creates a new InvoicePredictionHome.
     * @param ctx 
     * @param ban 
     *
     * @param delegate The home to delegate to.
     */
    public WalletReportPredictionHome(Context ctx, String ban, Home delegate)
    {
        super(ctx, delegate);
        setBan(ban);
    }

    /**
     * @return the ban associated to this home
     */
    public String getBan()
    {
       return ban_;
    }

    /**
     * @param ban
     */
    public void setBan(String ban)
    {
       ban_ = ban;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection select(Context ctx, Object where)
        throws HomeException, UnsupportedOperationException
    {
        final Collection list = super.select(ctx,where);     

        final String accountNumber = getBan();
        
        if (accountNumber != null)
        {
            final Account account =
                (Account)ReportUtilities.findByPrimaryKey(
                    ctx,
                    AccountHome.class,
                    accountNumber);

            if (account != null)
            {
                //account must have at least one prepaid subscriber
                final long existingSubscriberCount = 
                    getNumberOfPrepaidSubscribers(ctx,
                            account,
                            CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));

                long existingPrepaidTranasctionCount = 0; 
               
                if (existingSubscriberCount < 1)
                {
                    //account must have at least one prepaid subscriber
                    existingPrepaidTranasctionCount =
                        getExistingPrepaidWalletTransactionCount(
                            ctx,
                                accountNumber,
                                account.getLastBillDate(),
                                CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()));
                }
                
                
                if (existingSubscriberCount + existingPrepaidTranasctionCount > 0)
                {
                    final Date nextBillingDate = InvoiceSupport.calculateNextBillingDate(ctx, account.getBillCycleID());
                    
                    final WalletReport predictedReport = new WalletReport();
                    predictedReport.setBAN(accountNumber);
                    predictedReport.setGeneratedDate(new Date());
                    predictedReport.setReportDate(nextBillingDate);
                    predictedReport.setGhost(true);
                    
                    list.add(predictedReport);
                }                   
            }
        }

        return list;
    }

    private long getExistingPrepaidWalletTransactionCount(Context ctx,
            String accountNumber, Date lastBillDate, Date dateWithNoTimeOfDay)
    {
        And filter = new And();
        filter.add(new EQ(TransactionXInfo.BAN, accountNumber));
        filter.add(new GTE(TransactionXInfo.TRANS_DATE, (lastBillDate!=null?lastBillDate:new Date(0))));
        filter.add(new LT(TransactionXInfo.TRANS_DATE, dateWithNoTimeOfDay));
        filter.add(new In(TransactionXInfo.ADJUSTMENT_TYPE,
                AdjustmentTypeSupportHelper.get(ctx).getSelfAndDescendantCodes(ctx, AdjustmentTypeEnum.MobileMoney)));

        long txns = 0;
        
        try
        {
            txns = HomeSupportHelper.get(ctx).getBeanCount(ctx, Subscriber.class, filter);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error determining number of wallet transactions for account " + accountNumber + " from " + lastBillDate + " to " + dateWithNoTimeOfDay, e).log(ctx);
        }
        
        return txns;
    }

    private long getNumberOfPrepaidSubscribers(Context ctx, Account account, Date date)
    {
        And filter = new And();
        
        filter.add(new EQ(SubscriberXInfo.BAN, account.getBAN()));
        filter.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.PREPAID));
        filter.add(new LT(SubscriberXInfo.START_DATE, date));

        long subs = 0;
        try
        {
            subs = HomeSupportHelper.get(ctx).getBeanCount(ctx, Subscriber.class, filter);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error determining number of prepaid subscribers in account " + account.getBAN(), e).log(ctx);
        }
        return subs;
    }


    private String ban_;
}
