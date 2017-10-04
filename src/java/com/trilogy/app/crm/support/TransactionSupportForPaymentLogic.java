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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.util.snippet.log.Logger;

/**
 * Another Transaction Support class. It is kept apart from com.redknee.app.crm.support.TransactionSupport
 * since that class is over cluttered and requires refactoring and some methods to be deprecated.
 * It is more useful for us to maintain specific Transaction retrieval support classes (i.e. for 
 * making charges, separate from making payments) since each query can be highly customized.
 * 
 * 
 * This Class was added when porting the new Account Level Payment Splitting Logic from 
 * CRM 7.3.  In CRM 7.3, this class was called com.redknee.app.crm.support.NewTransactionSupport.
 * The class was renamed to make it search-friendly.
 *  
 * @since 8.2, Sept 21, 2009. 
 * 
 * 
 * @author Larry Xia
 * @author Angie Li
 *
 */
public abstract class TransactionSupportForPaymentLogic {

    public static Predicate getTransactionsOfAdjustmentType(final Context ctx, 
            int adjustmentType, 
            And and)
    {
        if ( and != null ){
            and.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, Integer.valueOf(adjustmentType)));
            return and;
        }

        return new EQ(TransactionXInfo.ADJUSTMENT_TYPE, Integer.valueOf(adjustmentType)); 
    }


    public static Transaction findSubscriberLastTransactionOfAdjustmentTypeInCurrentBillingCycle(final Context ctx, 
            final Subscriber sub, final int adjustmentType)
    throws HomeException
    {

        return findSubscriberLastTransactionOfAdjustmentType( ctx,  sub.getId(), 
                adjustmentType, getCurrentBillingCycleStartDate(ctx, sub)); 

    }

    public static Transaction findSubscriberLastTransactionOfAdjustmentTypeInCurrentBillingCycle(final Context ctx, 
            final Subscriber sub, final String supportSubscriberID, final int adjustmentType)
    throws HomeException
    {

        return findSubscriberLastTransactionOfAdjustmentType( ctx,  sub.getId(), supportSubscriberID, 
                adjustmentType, getCurrentBillingCycleStartDate(ctx, sub), null); 

    }


    public static Transaction findSubscriberLastTransactionOfAdjustmentType(final Context ctx, 
            final String subId, final int adjustmentType, Date startDate)
    throws HomeException
    {
        return findSubscriberLastTransactionOfAdjustmentType(ctx, subId, adjustmentType, startDate, null); 
    }

    public static Transaction findSubscriberLastTransactionOfAdjustmentType(final Context ctx, 
            final Subscriber sub, final int adjustmentType, Date startDate, Date endDate)
    throws HomeException
    {
        return findSubscriberLastTransactionOfAdjustmentType(ctx, sub.getId(), adjustmentType, startDate, endDate); 
    }


    public static Transaction findSubscriberLastTransactionOfAdjustmentType(final Context ctx, 
            final String subId, final int adjustmentType, Date startDate, Date endDate)
    throws HomeException
    {
        return findSubscriberLastTransactionOfAdjustmentType(ctx, subId, null, adjustmentType, startDate, endDate); 
    }

    public static Transaction findSubscriberLastTransactionOfAdjustmentType(final Context ctx, 
            final String subId, final String supportedSubscriberId, final int adjustmentType, Date startDate, Date endDate)
    throws HomeException
    {

        And and = new And(); 
        if( startDate != null)
        {    
            and.add(new GTE(TransactionXInfo.TRANS_DATE, startDate));
        } 
        if (endDate != null)
        {    
            and.add(new LTE(TransactionXInfo.TRANS_DATE, endDate));
        } 

        if ( supportedSubscriberId != null )
        {
            and.add(new EQ(TransactionXInfo.SUPPORTED_SUBSCRIBER_ID, supportedSubscriberId));

        }

        and.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, Integer.valueOf(adjustmentType))); 
        and.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, subId));


        Home home = getTransactionsOfAdjustmentType(ctx, and); 
        Transaction trans = null; 

        Collection c = home.selectAll(ctx); 
        for( Iterator i = c.iterator(); i.hasNext(); ){
            Transaction t = (Transaction) i.next(); 
            if ( trans == null || trans.getTransDate().before(t.getTransDate()))
            {
                trans = t;  
            }

        }

        return trans; 
    }


    public static Date getCurrentBillingCycleStartDate(Context ctx, Subscriber sub)
    throws HomeException
    {
        int billCycleDay = SubscriberSupport.getBillCycleDay(ctx, sub);
        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(ctx).getHandler(ChargingCycleEnum.MONTHLY);
        return handler.calculateCycleStartDate(ctx, new Date(), billCycleDay, sub.getSpid());
    }


    public static Home getTransactionsOfAdjustmentType(final Context ctx, 
            final Predicate predicate)
    throws HomeException
    {
        Home home = (Home) ctx.get(TransactionHome.class); 

        if ( predicate != null ){
            return home.where(ctx, predicate); 
        }

        throw new HomeException("the condition is null"); 
    }



    /** 
     * create a  transaction 
     * @param rate
     * @param adj
     * @param fee
     * @param agent
     * @return Transaction
     */
    static public Transaction createTransaction( 
            final Context ctx,  
            final int adj, 
            final double chargeAmount,
            final Subscriber sub)
    {
        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, ctx);
        }
        catch (Exception exception)
        {
            // TODO log?
            return null;
        }

        transaction.setBAN(sub.getBAN());
        try
        {
            transaction.setResponsibleBAN(sub.getAccount(ctx).getResponsibleBAN());
        }
        catch (HomeException e)
        {
            Logger.minor(ctx, TransactionSupportForPaymentLogic.class, "Unable to retrieve Account [" + sub.getBAN() + "]", e);
        }

        transaction.setMSISDN(sub.getMSISDN());
        transaction.setSubscriberID(sub.getId());
        transaction.setSpid(sub.getSpid());
        transaction.setAdjustmentType(adj);

        final long amount = Math.round(chargeAmount);

        transaction.setAmount(amount);
        transaction.setReceiveDate(new Date()); 

        transaction.setTaxPaid(0);

        //transaction.setAgent(agent);
        transaction.setSubscriptionCharge(true);
        transaction.setReasonCode(Long.MAX_VALUE);
        transaction.setExemptCreditLimitChecking(true);

        return transaction; 
    }


    public static Transaction getSubscriberTransation(Context ctx, Subscriber sub, int adjustmentType)
    throws HomeException
    {
        return getSubscriberTransaction(ctx, sub, adjustmentType, null, null); 
    }

    public static Transaction getTransactionOfCurrentBillignCycle(Context ctx, Subscriber sub, int adjustmentType)
    throws HomeException
    {
        return getSubscriberTransaction(ctx, sub, adjustmentType, getCurrentBillingCycleStartDate(ctx, sub), null); 

    }          

    public static Transaction getSubscriberTransaction(final Context ctx, 
            final Subscriber sub, final int adjustmentType, Date startDate, Date endDate)
    throws HomeException
    {
        Transaction trans = TransactionSupportForPaymentLogic.findSubscriberLastTransactionOfAdjustmentType(ctx, sub, adjustmentType, startDate, endDate);

        if (trans == null && sub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
        {
            Service service = AdjustmentTypeSupportHelper.get(ctx).getServiceForThisAdjustmentType(ctx,adjustmentType);
            if(service != null)
            {
                Subscriber postpaidSub = SubscriberSupport.validateSubscriberPPSM(ctx, sub); 
                if( postpaidSub != null)
                {
                    trans = TransactionSupportForPaymentLogic.findSubscriberLastTransactionOfAdjustmentType(ctx,                 
                            postpaidSub.getId(), sub.getId(), adjustmentType, startDate, endDate);
                }       
            }

        }

        return trans;
    }


    public static void setSubscriberID(final Context ctx, final Transaction transaction)
    throws HomeException
    {
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        if (sub == null || !sub.getMSISDN().equals(transaction.getMSISDN()))
        {
            final Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, transaction.getMSISDN());
            if (msisdn == null)
            {
                throw new HomeException("Could not find Msisdn for \"" + transaction.getMSISDN() + "\".");
            }
            sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn.getMsisdn(), CalendarSupportHelper.get(ctx).getDayAfter(transaction.getTransDate()));
            if (sub == null)
            {
                throw new HomeException("No subscriber found for MSISDN = " + transaction.getMSISDN()
                        + " and transaction date = " + transaction.getTransDate());
            }
        }
        transaction.setSubscriberID(sub.getId());
        // set the BAN as well to ensure that the subscriber id and ban of transaction matches
        transaction.setBAN(sub.getBAN());
    }


    public static Transaction createTransactionInstance(
            final Context context,
            final Subscriber subscriber,
            final long amount,
            final long newBalance,
            final int adjType,
            final boolean prorated,
            final boolean limitExemption,
            final Date billingDate,
            final Date receivingDate,
            final String csrInput,
            final int expiryDaysExt)
    {
        String glCode= AdjustmentTypeSupportHelper.get(context).getGLCodeForAdjustmentType(context, adjType, subscriber.getSpid()); 

        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, context);
        }
        catch (Exception exception)
        {
            new InfoLogMsg(TransactionSupportForPaymentLogic.class, "Unable to instantiate Transaction bean", exception).log(context);
            return null;
        }

        transaction.setAmount(amount);
        transaction.setBalance(newBalance);
        transaction.setAdjustmentType(adjType);
        transaction.setSubscriptionCharge(prorated);
        transaction.setExemptCreditLimitChecking(limitExemption); 
        transaction.setTransDate(billingDate);
        transaction.setReceiveDate(receivingDate);
        transaction.setCSRInput(csrInput);
        transaction.setExpiryDaysExt((short)expiryDaysExt);


        transaction.setBAN(subscriber.getBAN());
        try
        {
            transaction.setResponsibleBAN(subscriber.getAccount(context).getResponsibleBAN());
        }
        catch (HomeException e)
        {
            Logger.minor(context, TransactionSupportForPaymentLogic.class, "Unable to retrieve Account [" + subscriber.getBAN() + "]", e);
        }

        transaction.setMSISDN(subscriber.getMSISDN());
        transaction.setSpid(subscriber.getSpid());
        transaction.setSubscriberID(subscriber.getId());

        transaction.setReasonCode(Long.MAX_VALUE);
        transaction.setFromVRAPoller(false);
        transaction.setGLCode(glCode);
        transaction.setAgent(CoreTransactionSupportHelper.get(context).getCsrIdentifier(context));

        return transaction; 
    }


    /**
     * A helper method to make a clone of the original transaction.
     *
     * @param transaction The original transaction.
     *
     * @return Transaction The clone of the original transaction.
     */
    public static Transaction cloneTransaction(final Transaction trans)
    throws HomeException
    {
        Transaction clone = null;

        try
        {
            clone = (Transaction) trans.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new HomeException("Failed to clone the original transaction");
        }

        return clone;
    }




    public static Collection findRecurringChargeOfCurrentBillingCycle(final Context ctx, 
            final Subscriber sub,  final int adjustmentType)
    throws HomeException
    {

        And and = new And(); 
        and.add(new GTE(TransactionXInfo.TRANS_DATE, getCurrentBillingCycleStartDate(ctx, sub)));
        and.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, Integer.valueOf(adjustmentType))); 
        and.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, sub.getId()));
        and.add(new EQ(TransactionXInfo.AGENT, "ApplyRecurCharges"));

        Home home = getTransactionsOfAdjustmentType(ctx, and); 
        Transaction trans = null; 

        return home.selectAll(ctx); 

    }
}
