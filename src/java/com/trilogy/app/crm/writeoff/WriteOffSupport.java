/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.writeoff;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import com.trilogy.framework.core.bean.Application;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberInvoice;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CurrencyPrecisionSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Static class supporting Write-off operations
 * 
 * @author alpesh.champeneri@redknee.com
 */
public final class WriteOffSupport
{

    private static final int ER_CLASS = 1100;
    private static final int POST_WRITE_OFF_ER_1143 = 1143;
    private static Home transHome = null;

    private WriteOffSupport()
    {
    }


    public static Object getWriteOffAdjustmentPredicate()
    {
        final Or or = new Or();
        or.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, AdjustmentTypeEnum.WriteOff_INDEX));
        or.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, AdjustmentTypeEnum.WriteOffReversal_INDEX));
        or.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, AdjustmentTypeEnum.WriteOffTax_INDEX));
        return or;
    }


    public static long getAdjustmentTypeAmountForSubId(Context ctx, String subId, int adjType)
    {
        And where = new And();
        where.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, subId));
        where.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, adjType));
        return sumUpTransactions(ctx, where);
    }


    public static long getWrittenOffTaxAmountForSubId(Context ctx, String subId)
    {
        return getAdjustmentTypeAmountForSubId(ctx, subId, AdjustmentTypeEnum.WriteOffTax_INDEX);
    }


    public static long getWrittenOffAmountForSubId(Context ctx, String subId)
    {
        And where = new And();
        where.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, subId));
        where.add(getWriteOffAdjustmentPredicate());
        return sumUpTransactions(ctx, where);
    }


    public static long getMonthToDateWriteOffAmountForSubId(Context ctx, String subId, Date preInvoiceDate)
    {
        And predicate = new And();
        predicate.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, subId));
        predicate.add(getWriteOffAdjustmentPredicate());
        predicate.add(new GT(TransactionXInfo.RECEIVE_DATE, preInvoiceDate));
        return sumUpTransactions(ctx, predicate);
    }


    public static long getTotalWriteOffAmountForSubId(Context ctx, String subId, SubscriberInvoice preInvoice)
    {
        if (preInvoice == null)
            throw new IllegalArgumentException("The previous invoice is null!");
        long preAmount = preInvoice.getWriteOffAmount();
        Date preInvoiceDate = preInvoice.getInvoiceDate();
        long monthToDateAmount = getMonthToDateWriteOffAmountForSubId(ctx, subId, preInvoiceDate);
        return preAmount + monthToDateAmount;
    }


    public static long getTotalWriteOffAmountForSub(Context ctx, Subscriber sub)
    {
        String subId = sub.getId();
        SubscriberInvoice previousInvoice = InvoiceSupport.getMostRecentSubscriberInvoice(ctx, subId);
        if (previousInvoice == null)
        {
            previousInvoice = new SubscriberInvoice();
            previousInvoice.setWriteOffAmount(0);
            previousInvoice.setInvoiceDate(getYesterday(SubscriberSupport.lookupEarliestActivityDate(ctx, sub)));
        }
        return getTotalWriteOffAmountForSubId(ctx, subId, previousInvoice);
    }


    public static long getTotalWriteOffAmountForAccount(Context ctx, Account account)
    {
        String ban = account.getBAN();
        Invoice preInvoice = InvoiceSupport.getMostRecentInvoice(ctx, ban);
        if (preInvoice == null)
        {
            Date firstActivityDate = AccountSupport.lookupEarliestActivityDate(ctx, account);
            preInvoice = new Invoice();
            preInvoice.setWriteOffAmount(0L);
            preInvoice.setInvoiceDate(getYesterday(firstActivityDate));
        }
        return getTotalWriteOffAmountForAccount(ctx, account, preInvoice);
    }


    public static long getTotalWriteOffAmountForAccount(Context ctx, Account account, Invoice preInvoice)
    {
        if (preInvoice == null)
            throw new IllegalArgumentException("The previous invoice is null!");
        long preAmount = preInvoice.getWriteOffAmount();
        Date preInvoiceDate = preInvoice.getInvoiceDate();
        long monthToDateAmount = getMonthToDateWriteOffAmountForAccount(ctx, account, preInvoiceDate);
        return preAmount + monthToDateAmount;
    }


    public static long getMonthToDateWriteOffAmountForAccount(Context ctx, Account account, Date preInvoiceDate)
    {
        HashSet<String> bans = new HashSet<String>();
        try
        {
            Collection<Account> accounts = AccountSupport.getNonResponsibleAccounts(ctx, account);
            for(Account account_ : accounts)
            {
                bans.add(account_.getBAN());
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, WriteOffSupport.class.getName(), "Failed to fetch non-responsible subaccounts.", e);
        }
        final And predicate = new And();
        if (bans.size() > 1)
        {
            predicate.add(new In(TransactionXInfo.BAN, bans));
        }
        else
        {
            predicate.add(new EQ(TransactionXInfo.BAN, account.getBAN()));
        }
        predicate.add(getWriteOffAdjustmentPredicate());
        predicate.add(new GT(TransactionXInfo.RECEIVE_DATE, preInvoiceDate));
        return sumUpTransactions(ctx, predicate);
    }


    private static long sumUpTransactions(Context ctx, Object predicate)
    {
        long ret = 0;
        Collection<Transaction> transactionCollection = getTransactions(ctx, predicate);
        for (Transaction transaction : transactionCollection)
        {
            ret += transaction.getAmount();
        }
        return ret;
    }


    @SuppressWarnings("unchecked")
    private static Collection<Transaction> getTransactions(Context ctx, Object predicate)
    {
        // somehow, the regular Transaction Home pipeline filter out the write-off
        // transactions
        // so that this db home will be engaged to do query
     
        
        if (transHome == null)
        {
           transHome = (Home)ctx.get(TransactionHome.class);//StorageSupportHelper.get(ctx).createHome(ctx, Transaction.class, "TRANSACTION");
        }
        
        Collection<Transaction> ret = new ArrayList<Transaction>();
        try
        {
            ret = transHome.select(ctx, predicate);
        }
        catch (HomeException e)
        {
            LogSupport
                    .minor(ctx, WriteOffSupport.class.getName(), "Exception found when querying Transaciton Home.", e);
        }
        return ret;
    }

    


    public static AdjustmentType getWriteOffAdjustmentType(Context ctx) throws HomeException
    {
        return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.WriteOff);
    }


    public static AdjustmentType getWriteOffTaxAdjustmentType(Context ctx) throws HomeException
    {
        return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.WriteOffTax);
    }


    public static void generatePostWriteOffPaymentER(Context ctx, Account account, long originalPayment,
            long writeOffPayment, long refNum)
    {
        String[] fields = new String[]
            {account.getBAN(), String.valueOf(account.getBillCycleID()), account.getLastName(), account.getFirstName(),
                    String.valueOf(refNum), account.getAccountName(), String.valueOf(account.getState().getIndex()),
                    String.valueOf(writeOffPayment), ER1143_DATE_FORMAT.format(new Date()),
                    String.valueOf(originalPayment)};
        new ERLogMsg(POST_WRITE_OFF_ER_1143, ER_CLASS, "Post Write-off Payment", account.getSpid(), fields).log(ctx);
    }

    private static SimpleDateFormat ER1143_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");


    public static boolean isWriteOffTransaction(Context ctx, Transaction transaction)
    {
        int type = transaction.getAdjustmentType();
        return AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, type, AdjustmentTypeEnum.WriteOffCat);
    }


    public static Date getYesterday(final Date today)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.HOUR, -24);
        return calendar.getTime();
    }
/*
 *  Convert currency value in "$(xxxx.xx)" format.
 */
    public static String getFormattedCurrencyValue(Context ctx, long value)
    {
        String formattedValue = String.valueOf(value);
        boolean isNagative = false;
        if(value < 0)
        {
            value = value * -1;
            isNagative = true;
        }
        
        Application application = (Application) ctx.get(Application.class);
        String currencyCode= application.getLocaleIsoCurrency();
        Currency currency =  CurrencyPrecisionSupportHelper.get(ctx).getCurrency(ctx,currencyCode);
        formattedValue = CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx, currency, new Long(value));
        
        if(formattedValue != null && formattedValue.length() > 1){
            
            String val = formattedValue.substring(currency.getSymbol().length());
            StringBuilder str = new StringBuilder();
            str.append(currency.getSymbol());
            if(isNagative)
            {
                str.append("(");
            }
            str.append(val);
            if(isNagative)
            {
                str.append(")");
            }
            
            return str.toString();
        }
        return formattedValue;
    }

    public static Date getDateWithNoTimeOfDay(final Date date)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTime();
    }
}