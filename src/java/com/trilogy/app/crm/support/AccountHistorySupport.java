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
package com.trilogy.app.crm.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AccountHistory;
import com.trilogy.app.crm.bean.AccountHistoryTransientHome;
import com.trilogy.app.crm.bean.AccountHistoryTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.elang.OracleIn;


/**
 * @author larry.xia@redknee.com
 * @author victor.stratan@redknee.com
 */
public class AccountHistorySupport
{
    private static final String CLASS_NAME = AccountHistorySupport.class.getName();


    public static Home mergeHomesByBan(Context ctx, String ban, Date startDate, Date endDate)
    {
        Home home = new AccountHistoryTransientHome(ctx);
        CoreTransactionSupport support = CoreTransactionSupportHelper.get(ctx);
        Set<Long> accountIDSet = new HashSet<Long>();

        Set<Integer> adjustmentsSet = AdjustmentTypeSupportHelper.get(ctx).getSelfAndDescendantCodes(ctx,
                AdjustmentTypeEnum.Payments);

        final And where = new And();
        where.add(new EQ(TransactionXInfo.RESPONSIBLE_BAN, ban));
        where.add(new OracleIn(TransactionXInfo.ADJUSTMENT_TYPE, adjustmentsSet));

        Context subCtx = ctx.createSubContext();
        subCtx.put(TransactionHome.class, ctx.get(Common.ACCOUNT_TRANSACTION_HOME));

        Collection<Transaction> accountTransactions = support.getTransactions(subCtx,
                where, startDate, endDate);
        for (Transaction transaction : accountTransactions)
        {
            accountIDSet.add(Long.valueOf(transaction.getReceiptNum()));

            AccountHistory hist = AccountHistorySupport.convertTransactionToHistory(ctx, transaction,
                    AccountHistoryTypeEnum.ACCOUNT_TRANSACTION);
            try
            {
                home.create(ctx, hist);
            }
            catch (Exception e)
            {
                new MinorLogMsg(CLASS_NAME, "Unable to merge Account Transaction [" + transaction.getReceiptNum()
                        + "] for responsible Account [" + ban + "] " + e.getMessage(), e).log(ctx);
            }
        }

        Collection<Transaction> transactions = support.getTransactions(ctx,
                where, startDate, endDate);
        for (Transaction transaction : transactions)
        {
            if (transaction.getAccountReceiptNum() > 0
                    && accountIDSet.contains(Long.valueOf(transaction.getAccountReceiptNum())))
            {
                continue;
            }

            AccountHistory hist = AccountHistorySupport.convertTransactionToHistory(ctx, transaction,
                    AccountHistoryTypeEnum.TRANSACTION);
            try
            {
                home.create(ctx, hist);
            }
            catch (Exception e)
            {
                new MinorLogMsg(CLASS_NAME, "Unable to merge Transaction [" + transaction.getReceiptNum()
                        + "] for responsible Account [" + ban + "] " + e.getMessage(), e).log(ctx);
            }
        }

        List<Invoice> invoices = null;
        try
        {
            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            invoices = service.getInvoicesForAccount(ctx, ban, startDate, endDate);
        }
        catch (Exception e)
        {
            new MinorLogMsg(CLASS_NAME, "Unable to get Invoices  for responsible Account [" + ban + "] "
                    + e.getMessage(), e).log(ctx);
            invoices = new ArrayList<Invoice>();
        }

        for (Invoice invoice : invoices)
        {
            if (invoice.getInvoiceId() != null)
            {
                try
                {
                    AccountHistory hist = AccountHistorySupport.convertInvoiceToHistory(ctx, invoice);
                    home.create(ctx, hist);
                }
                catch (Exception e)
                {
                    new MinorLogMsg(CLASS_NAME, "Unable to merge Invoice [" + invoice.getInvoiceId()
                            + "] for responsible Account [" + ban + "] " + e.getMessage(), e).log(ctx);
                }
            }
        }

        return home;
    }


    public static AccountHistory convertTransactionToHistory(Context ctx,
            Transaction transaction,
            AccountHistoryTypeEnum type)
    {
        AccountHistory hist = new AccountHistory();
        try
        {
            hist.setKeyDate(transaction.getTransDate());
            hist.setId((Long.valueOf(transaction.getReceiptNum())).toString());
            hist.setBAN(transaction.getAcctNum());
            hist.setAdjustmentType(transaction.getAdjustmentType());
            hist.setAgent(transaction.getAgent());
            hist.setAmount(transaction.getAmount());
            hist.setCreditCardNumber(transaction.getCreditCardNumber());
            hist.setCSRInput(transaction.getCSRInput());
            hist.setGLCode(transaction.getGLCode());
            hist.setSubscriberType(transaction.getSubscriberType());
            hist.setExpDate(transaction.getExpDate());
            hist.setExtTransactionId(transaction.getExtTransactionId());
            hist.setLocationCode(transaction.getLocationCode());
            hist.setMSISDN(transaction.getMSISDN());
            hist.setPayee(transaction.getPayee());
            hist.setPaymentAgency(transaction.getPaymentAgency());
            hist.setPaymentDetails(transaction.getPaymentDetails());
            hist.setProrated(transaction.getSubscriptionCharge());
            //hist.setReceiptNum(trans.getReceiptNum());
            hist.setReceiveDate(transaction.getReceiveDate());
            hist.setSpid(transaction.getSpid());
            hist.setTransDate(transaction.getTransDate());
            hist.setTaxPaid(transaction.getTaxPaid());
            hist.setType(type);
        }
        catch (Exception e)
        {
            new MinorLogMsg(CLASS_NAME, "Unable to convert Transaction [" + transaction.getReceiptNum() + "] "
                    + e.getMessage(), e).log(ctx);
        }
        return hist;
    }


    public static AccountHistory convertInvoiceToHistory(Context ctx, Invoice invoice)
    {
        AccountHistory hist = new AccountHistory();
        try
        {
            //For "View" action, the Invoice's Generated Date is required for lookup
            hist.setKeyDate(invoice.getInvoiceDate());
            if (invoice.getInvoiceId() != null && invoice.getInvoiceId().length() > 0)
            {
                //hist.setInvoiceId(inv.getInvoiceId());
                hist.setId(invoice.getInvoiceId());
            }
            else
            {
                //hist.setInvoiceId("not avail");
                hist.setId("not avail");
            }
            hist.setType(AccountHistoryTypeEnum.INVOICE);
            hist.setBAN(invoice.getBAN());
            hist.setCurrentAmount(invoice.getCurrentAmount());
            hist.setDueDate(invoice.getDueDate());
            //hist.setGeneratedDate( inv.getGeneratedDate()); 
            //hist.setInvoiceDate( inv.getInvoiceDate());
            hist.setTotalAmount(invoice.getTotalAmount());
            //hist.setRootInvoiceId( inv.getRootInvoiceId());
            //hist.setInvoiceId(inv.getInvoiceId()); 
            hist.setMSISDN(invoice.getMSISDN());
            //hist.setTaxAmount( inv.getTaxAmount()); 
            //hist.setSpid( inv.getSpid());
            //hist.setURL(inv.getURL()); 
        }
        catch (Exception e)
        {
            new MinorLogMsg(CLASS_NAME, "Unable to convert Invoice [" + invoice.getInvoiceId() + "] "
                    + e.getMessage(), e).log(ctx);
        }
        return hist;

    }

}
