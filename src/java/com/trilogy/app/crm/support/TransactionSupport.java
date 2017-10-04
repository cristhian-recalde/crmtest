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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionOwnerTypeEnum;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.invoice.AdjustmentTypeCategoryAccumulator;
import com.trilogy.app.crm.poller.event.TFABillingProcessor;
import com.trilogy.app.crm.poller.event.VRAERProcessor;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.transaction.TransactionReceiveDateComparator;
import com.trilogy.util.snippet.log.Logger;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.MaxVisitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.bean.*;


/**
 * Provides support methods for dealing with Transactions.
 * 
 * @author gary.anderson@redknee.com
 */
public final class TransactionSupport
{

    /**
     * Number of days in a year.
     */
    private static final int NUMBER_OF_DAYS = 365;

    /**
     * PPSM GL code.
     */
    public static final String GLCODE_PPSM = "default";


    /**
     * Creates a new <code>TransactionSupport</code> instance. This method is made private
     * to prevent instantiation of utility class.
     */
    private TransactionSupport()
    {
        // empty
    }


    /**
     * create a transaction
     * 
     * @param ctx
     * @param adj
     * @param chargeAmount
     * @param sub
     * @return Transaction
     */
    static public Transaction createTransaction(
            final Context ctx,
            final int adj,
            final BigDecimal chargeAmount,
            final Subscriber sub)
    {
        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, ctx);
        }
        catch (Exception exception)
        {
            new InfoLogMsg(TransactionSupport.class, "Unable to instnatiate transction bean", exception).log(ctx);
            return null;
        }

        transaction.setBAN(sub.getBAN());
        try
        {
            transaction.setResponsibleBAN(sub.getAccount(ctx).getResponsibleBAN());
        }
        catch (HomeException e)
        {
            Logger.minor(ctx, TransactionSupport.class, "Unable to retrieve Account [" + sub.getBAN() + "]", e);
        }

        transaction.setMSISDN(sub.getMSISDN());
        transaction.setSubscriberID(sub.getId());
        transaction.setSpid(sub.getSpid());
        transaction.setAdjustmentType(adj);

        final long amount;

        // Always round the absolute value to get the correct rounding.
        if (chargeAmount.signum() == -1)
        {
            amount = -1 * (chargeAmount.abs().setScale(0, BigDecimal.ROUND_HALF_UP).longValue());
        }
        else
        {
            amount = chargeAmount.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        }

        transaction.setAmount(amount);
        transaction.setReceiveDate(new Date());
        transaction.setSubscriberType(sub.getSubscriberType());
        transaction.setSubscriptionType(sub.getSubscriptionType());
        transaction.setTaxPaid(0);
        transaction.setSubscriptionCharge(true);

        //transaction.setAgent(agent);
        //transaction.setProrated(true);
        transaction.setReasonCode(Long.MAX_VALUE);
        transaction.setExemptCreditLimitChecking(true);

        return transaction;
    }
    
    /**
     * create a transaction
     * 
     * @param ctx
     * @param adj
     * @param chargeAmount
     * @param sub -- CUG Owner
     * @param supportedSub -- CUG member
     * @return Transaction
     */
    static public Transaction createCUGOwnerTransaction(
            final Context ctx,
            final int adj,
            final BigDecimal chargeAmount,
            final Subscriber sub, String supportedSubscriber)
    {
        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, ctx);
        }
        catch (Exception exception)
        {
            new InfoLogMsg(TransactionSupport.class, "Unable to instantiate transaction bean", exception).log(ctx);
            return null;
        }
        transaction.setBAN(sub.getBAN());
        try
        {
            transaction.setResponsibleBAN(sub.getAccount(ctx).getResponsibleBAN());
        }
        catch (HomeException e)
        {
            Logger.minor(ctx, TransactionSupport.class, "Unable to retrieve Account [" + sub.getBAN() + "]", e);
        }

        transaction.setMSISDN(sub.getMSISDN());
        transaction.setSubscriberID(sub.getId());
        transaction.setSupportedSubscriberID(supportedSubscriber);
        transaction.setSpid(sub.getSpid());
        transaction.setAdjustmentType(adj);

        final long amount;

        // Always round the absolute value to get the correct rounding.
        if (chargeAmount.signum() == -1)
        {
            amount = -1 * (chargeAmount.abs().setScale(0, BigDecimal.ROUND_HALF_UP).longValue());
        }
        else
        {
            amount = chargeAmount.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        }

        transaction.setAmount(amount);
        transaction.setReceiveDate(new Date());
        transaction.setSubscriberType(sub.getSubscriberType());
        transaction.setSubscriptionType(sub.getSubscriptionType());
        transaction.setTaxPaid(0);
        transaction.setSubscriptionCharge(true);

        //transaction.setAgent(agent);
        //transaction.setProrated(true);
        transaction.setReasonCode(Long.MAX_VALUE);
        transaction.setExemptCreditLimitChecking(true);

        return transaction;
    }


    /**
     * Creates and stores a transaction with the given information.
     * 
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber to charge.
     * @param amount
     *            The amount by which to charge.
     * @param type
     *            The type of the adjustment.
     * @return The transaction created.
     * @exception HomeException
     *                Thrown if there are problems accessing or using data in the homes
     *                provided by the context.
     */
    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final long amount,
            final AdjustmentType type) throws HomeException
    {
        return createTransaction(context, subscriber, amount, type, new Date(), "");
    }


    public static Transaction createPlainTransaction(final Context context, final Subscriber subscriber,
            final long amount, final AdjustmentType type) throws HomeException
    {
        return createPlainTransaction(context, subscriber, amount, type, new Date(), "");
    }


    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final long amount,
            final AdjustmentType type, String csrInput) throws HomeException
    {
        return createTransaction(context, subscriber, amount, type, new Date(), csrInput);
    }


    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final long amount,
            final AdjustmentType type, final boolean limitExemption, final Date billingDate) throws HomeException
    {
        return createTransaction(
                    context,
                    subscriber,
                    amount,
                    0,
                    type,
                    limitExemption,
                    billingDate,
                    "");
    }


    /**
     * Creates and stores a transaction with the given information.
     * 
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber to charge.
     * @param amount
     *            The amount by which to charge.
     * @param newBalance
     *            The new balance of the subscriber.
     * @param type
     *            The type of the adjustment.
     * @param limitExemption
     *            True if credit limit check is enable; false otherwise.
     * @param billingDate
     *            The effective billing date of the transaction.
     * @param csrInput
     *            The charge details set by CSR.
     * @return The transaction created.
     * @exception HomeException
     *                Thrown if there are problems accessing or using data in the homes
     *                provided by the context.
     */
    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final long amount,
            final long newBalance, final AdjustmentType type, final boolean limitExemption, final Date billingDate,
            final String csrInput) throws HomeException
    {
        final String csrIdentifier = CoreTransactionSupportHelper.get(context).getCsrIdentifier(context);

        return createTransaction(context, subscriber, amount, newBalance, type, false, limitExemption, csrIdentifier,
                billingDate, new Date(), csrInput);
    }


    /**
     * Creates and stores a transaction with the given information.
     * 
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber to charge.
     * @param amount
     *            The amount by which to charge.
     * @param type
     *            The type of the adjustment.
     * @param prorated
     *            True if this is a prorated charge; false otherwise.
     * @param limitExemption
     *            True if credit limit check is enable; false otherwise.
     * @param csrInput
     *            The charge details set by CSR.
     * @return The transaction created.
     * @exception HomeException
     *                Thrown if there are problems accessing or using data in the homes
     *                provided by the context.
     */
    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final long amount,
            final AdjustmentType type, final boolean prorated, final boolean limitExemption, final String csrInput)
            throws HomeException
    {
        final String csrIdentifier = CoreTransactionSupportHelper.get(context).getCsrIdentifier(context);

        return createTransaction(context, subscriber, amount, 0, type, prorated, limitExemption, csrIdentifier,
                new Date(), new Date(), csrInput);
    }


    /**
     * Creates and stores a transaction with the given information.
     * 
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber to charge.
     * @param amount
     *            The amount by which to charge.
     * @param type
     *            The type of the adjustment. transaction.
     * @param billingDate
     *            The effective billing date of the transaction.
     * @param csrInput
     *            The charge details set by CSR.
     * @return The transaction created.
     * @exception HomeException
     *                Thrown if there are problems accessing or using data in the homes
     *                provided by the context.
     */
    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final long amount,
            final AdjustmentType type, final Date billingDate, final String csrInput) throws HomeException
    {
        final String csrIdentifier = CoreTransactionSupportHelper.get(context).getCsrIdentifier(context);

        // TODO 2007-05-30 the transaction date and receive date should be reversed?

        /*
         * Balance: as long as it's not from the VRA ER, this balance will be overwritten
         * after the call to the OCG.
         */
        return createTransaction(context, subscriber, amount, 0, type, false, false, csrIdentifier, billingDate,
                new Date(), csrInput);
    }


    public static Transaction createPlainTransaction(final Context context, final Subscriber subscriber,
            final long amount, final AdjustmentType type, final Date billingDate, final String csrInput)
            throws HomeException
    {
        final String csrIdentifier = CoreTransactionSupportHelper.get(context).getCsrIdentifier(context);

        // TODO 2007-05-30 the transaction date and receive date should be reversed?

        /*
         * Balance: as long as it's not from the VRA ER, this balance will be overwritten
         * after the call to the OCG.
         */
        return createPlainTransaction(context, subscriber, amount, 0, type, false, false, csrIdentifier, billingDate,
                new Date(), csrInput);
    }


    /**
     * Creates and stores a transaction with the given information.
     * 
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber to charge.
     * @param amount
     *            The amount by which to charge.
     * @param newBalance
     *            The new balance after this transaction has been made.
     * @param type
     *            The type of the adjustment.
     * @param prorated
     *            True if this is a prorated charge; false otherwise.
     * @param limitExemption
     *            True if credit limit check is enable; false otherwise.
     * @param csrIdentifier
     *            The identifier of the agent creating the transaction.
     * @param billingDate
     *            The effective billing date of the transaction.
     * @param receivingDate
     *            The date this transaction is received.
     * @param csrInput
     *            The charge details set by CSR.
     * @return The transaction created.
     * @exception HomeException
     *                Thrown if there are problems accessing or using data in the homes
     *                provided by the context.
     */
    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final long amount,
            final long newBalance, final AdjustmentType type, final boolean prorated, final boolean limitExemption,
            final String csrIdentifier, final Date billingDate, final Date receivingDate, final String csrInput)
            throws HomeException
    {
        final int expiryDaysExt = 0;
        return createTransaction(context, subscriber, amount, newBalance, type, prorated, limitExemption,
                csrIdentifier, billingDate, receivingDate, csrInput, expiryDaysExt);
    }


    public static Transaction createPlainTransaction(final Context context, final Subscriber subscriber,
            final long amount, final long newBalance, final AdjustmentType type, final boolean prorated,
            final boolean limitExemption, final String csrIdentifier, final Date billingDate, final Date receivingDate,
            final String csrInput) throws HomeException
    {
        final int expiryDaysExt = 0;
        return createPlainTransaction(context, subscriber, amount, newBalance, type, prorated, limitExemption,
                csrIdentifier, billingDate, receivingDate, csrInput, expiryDaysExt);
    }


    /**
     * Creates and stores a transaction with the given information.
     * 
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber to charge.
     * @param amount
     *            The amount by which to charge.
     * @param newBalance
     *            The new balance after this transaction has been made.
     * @param type
     *            The type of the adjustment.
     * @param isSubscriptionCharge
     *            True if this is a subscription charge; false otherwise.
     * @param limitExemption
     *            True if credit limit check is enable; false otherwise.
     * @param csrIdentifier
     *            The identifier of the agent creating the transaction.
     * @param billingDate
     *            The effective billing date of the transaction.
     * @param receivingDate
     *            The date this transaction is received.
     * @param csrInput
     *            The charge details set by CSR.
     * @param expiryDaysExt
     *            Number of days the expiry date is extended.
     * @return The transaction created.
     * @exception HomeException
     *                Thrown if there are problems accessing or using data in the homes
     *                provided by the context.
     */
    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final long amount,
            final long newBalance, final AdjustmentType type, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier, final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt) throws HomeException
    {
        final Transaction transaction = createSubscriberTransactionObject(context, subscriber, amount, newBalance,
                type, isSubscriptionCharge, limitExemption, csrIdentifier, billingDate, receivingDate, csrInput,
                expiryDaysExt);

        return createTransactionRecord(context, subscriber, transaction);
    }
    
    /**
     * Creates and stores a transaction with the given information.
     * 
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber to charge.
     * @param amount
     *            The amount by which to charge.
     * @param newBalance
     *            The new balance after this transaction has been made.
     * @param type
     *            The type of the adjustment.
     * @param isSubscriptionCharge
     *            True if this is a subscription charge; false otherwise.
     * @param limitExemption
     *            True if credit limit check is enable; false otherwise.
     * @param csrIdentifier
     *            The identifier of the agent creating the transaction.
     * @param billingDate
     *            The effective billing date of the transaction.
     * @param receivingDate
     *            The date this transaction is received.
     * @param csrInput
     *            The charge details set by CSR.
     * @param expiryDaysExt
     *            Number of days the expiry date is extended.
     * @param payee
     *            Payee for the transaction       
     * @return The transaction created.
     * @exception HomeException
     *                Thrown if there are problems accessing or using data in the homes
     *                provided by the context.
     */
    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final long amount,
            final long newBalance, final AdjustmentType type, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier, final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt, PayeeEnum payee) throws HomeException
    {
        final Transaction transaction = createSubscriberTransactionObject(context, subscriber, amount, newBalance,
                type, isSubscriptionCharge, limitExemption, csrIdentifier, billingDate, receivingDate, csrInput,
                expiryDaysExt);
        
        transaction.setPayee(payee);

        return createTransactionRecord(context, subscriber, transaction);
    }
    
    
    /**
     * 
     * @param context
     * @param subscriber
     * @param amount
     * @param newBalance
     * @param type
     * @param discountedAdjType
     * @param prorated
     * @param limitExemption
     * @param csrIdentifier
     * @param billingDate
     * @param receivingDate
     * @param csrInput
     * @param action 
     * @param linkedAdjustmentTypeId
     * @return
     * @throws HomeException
     */
    public static Transaction createLinkedTransaction(final Context context, final Subscriber subscriber, final String supportedSubscriberID, final long amount,
            final long newBalance, final AdjustmentType type, final int taxAuth, final boolean prorated, final boolean limitExemption,
            final String csrIdentifier, final Date billingDate, final Date receivingDate, final String csrInput, final AdjustmentTypeActionEnum action,final int linkedAdjustmentTypeId)
            throws HomeException
    {
        final int expiryDaysExt = 0;
        return createLinkedTransaction(context, subscriber, supportedSubscriberID, amount, newBalance, type, taxAuth, prorated, limitExemption,
                csrIdentifier, billingDate, receivingDate, csrInput, expiryDaysExt, action,linkedAdjustmentTypeId);
    }
    
    
    /**
     * 
     * @param context
     * @param subscriber
     * @param amount
     * @param newBalance
     * @param type
     * @param discountedAdjType
     * @param isSubscriptionCharge
     * @param limitExemption
     * @param csrIdentifier
     * @param billingDate
     * @param receivingDate
     * @param csrInput
     * @param expiryDaysExt
     * @param action
     * @param linkedAdjustmentTypeId 
     * @return
     * @throws HomeException
     */
    public static Transaction createLinkedTransaction(final Context context, final Subscriber subscriber, final String supportedSubscriberID,final long amount,
            final long newBalance, final AdjustmentType type, final int taxAuth, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier, final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt, final AdjustmentTypeActionEnum action,final int linkedAdjustmentTypeId) throws HomeException
    {
        final Transaction transaction = createSubscriberTransactionObject(context, subscriber, amount, newBalance,
                type, taxAuth, isSubscriptionCharge, limitExemption, csrIdentifier, billingDate, receivingDate, csrInput,
                expiryDaysExt);
        if(null != supportedSubscriberID){
        	transaction.setSupportedSubscriberID(supportedSubscriberID);
        }
        if(-1!=linkedAdjustmentTypeId)
        {
        	transaction.setLinkedAdjustmentType(linkedAdjustmentTypeId);
        }
        transaction.setAction(action);
        return createTransactionRecord(context, subscriber, transaction);
    }

    public static Transaction createPlainTransaction(final Context context, final Subscriber subscriber,
            final long amount, final long newBalance, final AdjustmentType type, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier, final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt) throws HomeException
    {
        Date receiveDate = oneSecondBefore(context, billingDate);
        final Transaction transaction =
                createSubscriberTransactionObject(context, subscriber, amount,
                        newBalance, type,
                        isSubscriptionCharge, limitExemption, csrIdentifier, billingDate, receiveDate, csrInput,
                        expiryDaysExt);

        /*
         * TT 10070714058
         */
        transaction.setTransDate(new Date());

        return createPlainTransactionRecord(context, subscriber, transaction);
    }


    /**
     * @param context
     * @param subscriber
     * @param amount
     * @param newBalance
     * @param type
     * @param isSubscriptionCharge
     * @param limitExemption
     * @param csrIdentifier
     * @param billingDate
     * @param receivingDate
     * @param csrInput
     * @param expiryDaysExt
     * @param transactionMethod
     * @return
     * @throws HomeException
     */
    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final long amount,
            final long newBalance, final AdjustmentType type, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier, final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt, final String extTransactionId,
            final long transactionMethod)
            throws HomeException
    {
        return createTransaction(context, subscriber, amount, newBalance, type, isSubscriptionCharge, limitExemption,
                csrIdentifier, billingDate, receivingDate, csrInput, expiryDaysExt, extTransactionId,
                transactionMethod, Transaction.DEFAULT_PAYMENTAGENCY, Transaction.DEFAULT_PAYMENTDETAILS,
                Transaction.DEFAULT_LOCATIONCODE);
    }
    
    
    /**
     * 
     * @param context
     * @param subscriber
     * @param amount
     * @param newBalance
     * @param type
     * @param discountedAdjType
     * @param isSubscriptionCharge
     * @param limitExemption
     * @param csrIdentifier
     * @param billingDate
     * @param receivingDate
     * @param csrInput
     * @param expiryDaysExt
     * @param action 
     * @return
     * @throws HomeException
     */
    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final String supportedSubscriberID,final long amount,
            final long newBalance, final AdjustmentType type, final int taxAuth, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier, final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt, final AdjustmentTypeActionEnum action) throws HomeException
    {
        final Transaction transaction = createSubscriberTransactionObject(context, subscriber, amount, newBalance,
                type, taxAuth, isSubscriptionCharge, limitExemption, csrIdentifier, billingDate, receivingDate, csrInput,
                expiryDaysExt);
        if(null != supportedSubscriberID){
        	transaction.setSupportedSubscriberID(supportedSubscriberID);
        }
        transaction.setAction(action);
        return createTransactionRecord(context, subscriber, transaction);
    }


    /**
     * @param context
     * @param subscriber
     * @param amount
     * @param newBalance
     * @param type
     * @param isSubscriptionCharge
     * @param limitExemption
     * @param csrIdentifier
     * @param billingDate
     * @param receivingDate
     * @param csrInput
     * @param expiryDaysExt
     * @param transactionMethod
     * @param paymentAgency
     * @param paymentDetails
     * @param locationCode
     * @return
     * @throws HomeException
     */
    public static Transaction createTransaction(final Context context,
            final Subscriber subscriber, final long amount, final long newBalance,
            final AdjustmentType type, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier,
            final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt,
            final String extTransactionId, final long transactionMethod,
            final String paymentAgency, final String paymentDetails,
            final String locationCode) throws HomeException
    {
        final Transaction transaction = createSubscriberTransactionObject(context, subscriber, amount, newBalance,
                type, isSubscriptionCharge, limitExemption, csrIdentifier, billingDate, receivingDate, csrInput,
                expiryDaysExt);

        transaction.setExtTransactionId(extTransactionId);
        transaction.setTransactionMethod(transactionMethod);
        transaction.setPaymentAgency(paymentAgency);
        transaction.setPaymentDetails(paymentDetails);
        transaction.setLocationCode(locationCode);

        return createTransactionRecord(context, subscriber, transaction);
    }


    /**
     * @param context
     * @param account
     * @param amount
     * @param newBalance
     * @param type
     * @param isSubscriptionCharge
     * @param limitExemption
     * @param csrIdentifier
     * @param billingDate
     * @param receivingDate
     * @param csrInput
     * @param expiryDaysExt
     * @param extTransactionId
     * @param transactionMethod
     * @param paymentAgency
     * @param paymentDetails
     * @param locationCode
     * @return
     * @throws HomeException
     */
    public static Transaction createAccountTransaction(final Context context,
            final Account account, final long amount, final long newBalance,
            final AdjustmentType type, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier,
            final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt,
            final String extTransactionId, final long transactionMethod,
            final String paymentAgency, final String paymentDetails,
            final String locationCode) throws HomeException
    {

        Transaction transaction = createTransactionObject(context, account.getSpid(), amount, newBalance, type,
                isSubscriptionCharge, limitExemption, csrIdentifier, billingDate, receivingDate, csrInput,
                expiryDaysExt);
        transaction.setOwnerType(TransactionOwnerTypeEnum.SUBSCRIBER);
        transaction.setBAN(account.getBAN());
        transaction.setResponsibleBAN(account.getResponsibleBAN());
        transaction.setSubscriberType(account.getSubscriberType());
        transaction.setPayee(PayeeEnum.Account);

        return transaction;
    }

    
    /**
     * @param context
     * @param spid
     * @param amount
     * @param newBalance
     * @param type
     * @param isSubscriptionCharge
     * @param limitExemption
     * @param csrIdentifier
     * @param billingDate
     * @param receivingDate
     * @param csrInput
     * @param expiryDaysExt
     * @param ban
     * @throws HomeException
     */
    public static Transaction createAccountPaymentTransaction(final Context context,
            final int spid, final long amount, final long newBalance,
            final AdjustmentType type, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier,
            final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt,
            final String ban) throws HomeException
    {
    	
        Transaction transaction = createTransactionObject(context, spid, amount, newBalance, type,
                isSubscriptionCharge, limitExemption, csrIdentifier, billingDate, receivingDate, csrInput,
                expiryDaysExt);
        transaction.setOwnerType(TransactionOwnerTypeEnum.SUBSCRIBER);
        transaction.setBAN(ban);
        transaction.setResponsibleBAN(ban);
        transaction.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        transaction.setPayee(PayeeEnum.Account);
        
        final Context subCtx = context.createSubContext();
        transaction = HomeSupportHelper.get(subCtx).createBean(subCtx, transaction);

        return transaction;
    }
    
    
    /**
     * 
     * @param context
     * @param subscriber
     * @param amount
     * @param newBalance
     * @param type
     * @param isSubscriptionCharge
     * @param limitExemption
     * @param csrIdentifier
     * @param billingDate
     * @param receivingDate
     * @param csrInput
     * @param expiryDaysExt
     * @return
     * @throws HomeException
     */
    public static Transaction createSubscriberTransactionObject(final Context context,
            final Subscriber subscriber,
            final long amount, final long newBalance, final AdjustmentType type, final int taxAuth, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier, final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt)
            throws HomeException
    {

        int spid = subscriber.getSpid();

        final Transaction transaction = createTransactionObject(context, spid, amount, newBalance, type,
                isSubscriptionCharge, limitExemption, csrIdentifier, billingDate, receivingDate, csrInput,
                expiryDaysExt);

        final Account account = subscriber.getAccount(context);

        transaction.setBAN(subscriber.getBAN());
        transaction.setResponsibleBAN(account.getResponsibleBAN());
        
        /* Check for the system config whether the bill period is enabled or not,
         * Set the Bill Period ID in the transaction only if Bill Period Enabled
         */
       /* SysFeatureCfg cfg = (SysFeatureCfg)context.get(SysFeatureCfg.class);
        if(cfg.getEnableBillPeriodModule())
        {
        	if(LogSupport.isDebugEnabled(context))
        	{
        		LogSupport.debug(context, TransactionSupport.class, "ReceivedDate :"+transaction.getReceiveDate() +"  TransactionDate  :"+transaction.getTransDate());
        	}
        	if((transaction.getReceiveDate().compareTo(transaction.getTransDate())==-1)||(transaction.getReceiveDate().compareTo(transaction.getTransDate())==0))
        	{
        		transaction.setBillPeriodID(account.getBillPeriodId(context, transaction.getReceiveDate()));
        	}
        	else
        	{
        		transaction.setBillPeriodID(account.getBillPeriodId(context, transaction.getTransDate()));
        	}
        	
        }*/
     
       //TODO Manish
       // if(subscriber != null && SubscriberSupport.isMandatory(context, subscriber))-->Commenting the isMandatory function for MSISDN/packageId
        if(subscriber != null)
        {
            transaction.setMSISDN(subscriber.getMSISDN());
        }
        transaction.setSubscriberID(subscriber.getId());
        transaction.setSubscriptionTypeId(subscriber.getSubscriptionType());
        transaction.setOwnerType(TransactionOwnerTypeEnum.SUBSCRIBER);
        transaction.setSubscriberType(subscriber.getSubscriberType());
        transaction.setTaxAuthority(taxAuth);


        return transaction;
    }  

    /**
     * @param context
     * @param subscriber
     * @param amount
     * @param newBalance
     * @param type
     * @param isSubscriptionCharge
     * @param limitExemption
     * @param csrIdentifier
     * @param billingDate
     * @param receivingDate
     * @param csrInput
     * @param expiryDaysExt
     * @param transactionMethod
     * @return
     * @throws HomeException
     */
    public static Transaction createSubscriberTransactionObject(final Context context,
            final Subscriber subscriber,
            final long amount, final long newBalance, final AdjustmentType type, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier, final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt)
            throws HomeException
    {
        int spid = subscriber.getSpid();

        final Transaction transaction = createTransactionObject(context, spid, amount, newBalance, type,
                isSubscriptionCharge, limitExemption, csrIdentifier, billingDate, receivingDate, csrInput,
                expiryDaysExt);

        final Account account = subscriber.getAccount(context);

        transaction.setBAN(subscriber.getBAN());
        transaction.setResponsibleBAN(account.getResponsibleBAN());
        transaction.setMSISDN(subscriber.getMSISDN());
        transaction.setSubscriberID(subscriber.getId());
        transaction.setSubscriptionTypeId(subscriber.getSubscriptionType());
        transaction.setOwnerType(TransactionOwnerTypeEnum.SUBSCRIBER);
        transaction.setSubscriberType(subscriber.getSubscriberType());


        return transaction;
    }


    /**
     * @param context
     * @param spid
     * @param amount
     * @param newBalance
     * @param type
     * @param isSubscriptionCharge
     * @param limitExemption
     * @param csrIdentifier
     * @param billingDate
     * @param receivingDate
     * @param csrInput
     * @param expiryDaysExt
     * @return
     * @throws HomeException
     */
    private static Transaction createTransactionObject(final Context context,
            int spid, final long amount, final long newBalance,
            final AdjustmentType type, final boolean isSubscriptionCharge,
            final boolean limitExemption, final String csrIdentifier,
            final Date billingDate, final Date receivingDate,
            final String csrInput, final int expiryDaysExt) throws HomeException
    {
        if (type == null)
        {
            throw new HomeException("Adjustment-Type is not availble. Please pass a valid adjustment type, "
                    + "availble for the Service Provider and Transaction Context");
        }

        final String glCode = type.getGLCodeForSPID(context, spid);

        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, context);
        }
        catch (Exception exception)
        {
            throw new HomeException("Unable to instantiate transaction bean", exception);
        }


        transaction.setSpid(spid);
        transaction.setAdjustmentType(type.getCode());
        transaction.setAgent(csrIdentifier);
        transaction.setAmount(amount);
        transaction.setGLCode(glCode);
        transaction.setSubscriptionCharge(isSubscriptionCharge);
        transaction.setTransDate(billingDate);
        transaction.setReceiveDate(receivingDate);
        transaction.setExemptCreditLimitChecking(limitExemption);
        transaction.setCSRInput(csrInput);
        transaction.setExpiryDaysExt((short) expiryDaysExt);
        transaction.setReasonCode(Long.MAX_VALUE);

        final boolean isFromVRAER = context.getBoolean(VRAERProcessor.TRANSACTION_FROM_VAR_ER_POLLER, false);
        transaction.setFromVRAPoller(isFromVRAER);

        // if this is true, the transaction does not get forwarded to OCG. Used for polling of CDR 375
        final boolean isFromTFACDR = context.getBoolean(TFABillingProcessor.TRANSACTION_FROM_TFA_CDR_POLLER, false);
        transaction.setFromTFAPoller(isFromTFACDR);

        // Currently only the VRA ER gives a real balance. OCG only gives 0.
        // if (isFromVRAER)
        {
            transaction.setBalance(newBalance);
        }
        return transaction;
    }


    private static Date oneSecondBefore(Context context, Date billingDate)
    {
        billingDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(billingDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(billingDate);

        long timeInMillis = (cal.getTimeInMillis() - 1000);

        cal.setTimeInMillis(timeInMillis);
        return cal.getTime();
    }


    public static Transaction createTransactionRecord(final Context context, final Subscriber subscriber,
            final Transaction transaction) throws HomeException
    {
        final Context subCtx = context.createSubContext();
        subCtx.put(Subscriber.class, subscriber);
        final Home home = (Home) context.get(TransactionHome.class);

        if (home == null)
        {
            throw new HomeException("System error: no TransactionHome found in context.");
        }
        if(transaction.getFromTFAPoller())
        {
            if(transaction.getBalance()!= 0)
            {
                SubscriptionNotificationSupport.sendTFANotification(context, subscriber, transaction);
            }
        }
        return (Transaction) home.create(subCtx, transaction);
    }


    public static Transaction createPlainTransactionRecord(final Context context, final Subscriber subscriber,
            final Transaction transaction) throws HomeException
    {
        final Context subCtx = context.createSubContext();
        subCtx.put(Subscriber.class, subscriber);
        final Home home = (Home) context.get(Common.DISCOUNT_PLAIN_TXN_HOME);

        if (home == null)
        {
            throw new HomeException("System error: no TransactionHome found in context.");
        }

        return (Transaction) home.create(subCtx, transaction);
    }


    /**
     * Creates a transaction.
     * 
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber this transaction is created for.
     * @param amount
     *            The amount of the transaction.
     * @param desc
     *            Description of the transaction.
     * @param adjustmentType
     *            The adjustment type of the transaction.
     * @param glcode
     *            GL code of the transaction.
     * @return The transaction created.
     * @throws HomeException
     *             Thrown if there are problems creating this transaction.
     */
    public static Transaction createTransaction(final Context context, final Subscriber subscriber, final long amount,
            final String desc, final int adjustmentType, final String glcode) throws HomeException
    {
        final Home transactionHome = (Home) context.get(TransactionHome.class);
        if (transactionHome == null)
        {
            throw new HomeException("System Error: TransactionHome does not exist in context");
        }

        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, context);
        }
        catch (Exception exception)
        {
            throw new HomeException("Unable to instantiate transaction bean", exception);
        }


        transaction.setBAN(subscriber.getBAN());
        transaction.setResponsibleBAN(subscriber.getAccount(context).getResponsibleBAN());
        transaction.setSubscriberID(subscriber.getId());
        transaction.setAgent(subscriber.getUser());
        transaction.setReasonCode(Long.MAX_VALUE);
        transaction.setMSISDN(subscriber.getMSISDN());
        transaction.setAdjustmentType(adjustmentType);
        transaction.setAmount(amount);
        transaction.setPaymentAgency("default");
        transaction.setLocationCode("");
        transaction.setExemptCreditLimitChecking(true);
        transaction.setCSRInput(desc);
        transaction.setGLCode(glcode);
        transaction.setSubscriptionCharge(true);
        transaction.setSubscriptionTypeId(subscriber.getSubscriptionType());
        transaction.setSubscriberType(subscriber.getSubscriberType());

        final Context subCtx = context.createSubContext();
        subCtx.put(Subscriber.class, subscriber);

        return (Transaction) transactionHome.create(subCtx, transaction);
    }


    /**

     * Calculates the interest payment and creates a new transaction with the Interest
     * Payment adjustment type.
     * 
     * @param ctx
     *            The operating context.
     * @param subs
     *            The subscriber to create this interest payment for.
     * @param txn
     *            The deposit or deposit release transaction triggering this interest
     *            payment.
     * @param lastDeposit
     *            The deposit to calculate interest on.
     * @throws HomeException
     *             Thrown by home.
     */
    public static void createInterestPayment(final Context ctx, final Subscriber subs, final Transaction txn,
            final long lastDeposit) throws HomeException
    {
        final AdjustmentType type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                AdjustmentTypeEnum.InterestPayment);
        final long amount = calculateInterestPayment(ctx, subs, txn, lastDeposit);
        if (amount != 0)
        {
            /*
             * Cindy Wong: TT 6122743030 - interest payment transaction should happen on
             * the same day as the deposit release.
             */
            createTransaction(ctx, subs, amount, type, txn.getTransDate(), "Deposit interest payment");
        }
    }


    /**
     * Calculates the interest amount.
     * 
     * @param ctx
     *            The operating context.
     * @param subs
     *            The subscriber to create this interest payment for.
     * @param trans
     *            The deposit or deposit release transaction triggering this interest
     *            payment.
     * @param lastDeposit
     *            The deposit to calculate interest on.
     * @return Interest payment amount.
     * @throws HomeException
     *             Thrown by home.
     */
    private static long calculateInterestPayment(final Context ctx, final Subscriber subs, final Transaction trans,
            final long lastDeposit) throws HomeException
    {
        double total = 0;
        final Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        if (spidHome != null)
        {
            final CRMSpid spid = (CRMSpid) spidHome.find(ctx, Integer.valueOf(trans.getSpid()));

            if (spid != null)
            {
                /*
                 * assigning the data to local variables for better DEBUGGING
                 */
                final double interest = spid.getInterestRate() / 100 / NUMBER_OF_DAYS;

                /*
                 * Cindy Wong: TT 6122743030 - interest should be calculated on the day of
                 * transaction.
                 */
                final long days = CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(subs.getDepositDate(),
                        trans.getTransDate());
                LogSupport.debug(ctx, "TransactionSupport", "calculateInterestPayment: deposit =" + lastDeposit
                        + " interest=" + interest + " number of days= " + days);

                total = lastDeposit * interest * days;
            }
        }
        return Math.round(total);
    }

    /**
     * Checks if the adjustment type hasn't been refunded within the billing cycle.
     * 
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber being examined.
     * @param adjustType
     *            Adjustment type being examined.
     * @param billingDate
     *            Billing date to use.
     * @return Returns <code>true</code> if there has been no refund of the adjustment
     *         type within the billing cycle, <code>false</code> otherwise.
     * @throws HomeException
     *             Thrown if there are problems retrieving the relevant transactions for
     *             examination.
     */
    public static boolean cycleNotRefunded(final Context parentCtx, final Subscriber sub, final Object item,
            final ChargedItemTypeEnum itemType, final ServicePeriodEnum servicePeriod, final int adjustType,
            final long amount, final Date billingDate) throws HomeException
    {
        Context ctx = parentCtx.createSubContext();
        ctx.put(Subscriber.class, sub);

        final CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
        final int billCycleDay = SubscriberSupport.getBillCycleDay(ctx, sub);
        ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(servicePeriod);

        final Date startDate;
        final Date endDate = handler.calculateCycleEndDate(ctx, billingDate, billCycleDay, spid.getId(), sub.getId(),
                item);
        final Date actualStartDate = handler.calculateCycleStartDate(ctx, billingDate, billCycleDay, spid.getId(),
                sub.getId(), item);
        boolean isPrebilled = spid.isPrebilledRecurringChargeEnabled()
                && ChargingCycleEnum.MONTHLY.equals(servicePeriod.getChargingCycle()) && !sub.isPrepaid();

        if (isPrebilled)
        {
            startDate = CalendarSupportHelper.get(ctx).getDayBefore(actualStartDate);
        }
        else
        {
            startDate = actualStartDate;
        }

        return cycleNotRefunded(ctx, sub, adjustType, amount, startDate, endDate);
    }


    private static boolean cycleNotRefunded(final Context ctx, final Subscriber sub, final int adjustType,
            final long amount, final Date startDate, final Date endDate) throws HomeException
    {
        final And condition = new And();
        condition.add(new GTE(TransactionXInfo.RECEIVE_DATE, startDate));
        condition.add(new LTE(TransactionXInfo.RECEIVE_DATE, endDate));
        condition.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, sub.getId()));
        condition.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, adjustType));
        MaxVisitor lastTransactionVisitor = new MaxVisitor(new TransactionReceiveDateComparator(false));
        lastTransactionVisitor = (MaxVisitor) ((Home) ctx.get(TransactionHome.class)).where(ctx, condition).forEach(
                ctx,
                lastTransactionVisitor);
        final Transaction lastTransaction = (Transaction) lastTransactionVisitor.getValue();
        final boolean result;
        if (lastTransaction == null)
        {
            // if there is no transaction, we can say no refund was made
            result = true;
        }
        else
        {
            // if the amount in last committed transaction is of a sign opposite that of
            // original charge amount, we can say that amount is reversed
            result = lastTransaction.getAmount() * amount <= 0;
        }
        return result;
    }


    /**
     * Checks if the same adjustment type charged within the billing cycle.
     * 
     * @param ctx
     *            The operating context.
     * @param account
     *            The account being examined.
     * @param adjustType
     *            The adjustment type being examined.
     * @param startDate
     *            Start date of the date range to check.
     * @param endDate
     *            End date of the date range to check.
     * @param isPrebilled
     *            Whether prebilling was enabled.
     * @return Returns <code>true</code> if there is unrefunded charges for the
     *         adjustment type within the provided date range, <code>false</code> otherwise.
     * @throws HomeException
     *             Thrown if there are problems retrieving the transactions for
     *             examination.
     */
    private static boolean isChargedAndNotRefunded(final Context ctx, final Account account, final int adjustType,
            long amount, final Date startDate, final Date endDate, final boolean isPrebilled) throws HomeException
    {
        final And condition = new And();
        condition.add(new GTE(TransactionXInfo.RECEIVE_DATE, startDate));
        condition.add(new LT(TransactionXInfo.RECEIVE_DATE, endDate));
        condition.add(new EQ(TransactionXInfo.BAN, account.getBAN()));
        condition.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, Integer.valueOf(adjustType)));
        MaxVisitor lastTransactionVisistor = new MaxVisitor(new TransactionReceiveDateComparator(false));
        lastTransactionVisistor = (MaxVisitor) ((Home) ctx.get(TransactionHome.class)).where(ctx, condition).forEach(
                ctx,
                lastTransactionVisistor);
        final Transaction lastTransaction = (Transaction) lastTransactionVisistor.getValue();
        boolean result;
        if (null != lastTransaction)
        {
            boolean isPrepaid = lastTransaction.getSubscriberType().equals(SubscriberTypeEnum.PREPAID);
            result = isChargedAndNotRefunded(ctx, lastTransaction, amount, startDate, endDate, isPrebilled, isPrepaid);
        }
        else
        {
            // if lastTransaction is null it means that we do not have any transactions in
            // he given start and end dates (cycle)
            result = false;
        }
        return result;
    }


    /**

     * Determines if an adjustment type is chargeable to an account. It is chargeable if
     * it not been charged or refunded in later date, and if has either not been charged
     * or has already been refunded in the current bill cycle.
     * 
     * @param context
     *            The operating context.
     * @param account
     *            The account to be charged.
     * @param adjustmentType
     *            The adjustment type to be charged.
     * @param startDate
     *            Start date.
     * @param endDate
     *            End date.
     * @param billingDate
     *            Billing date.
     * @param isPrebilled
     *            Whether prebilling is enabled.
     * @return Whether the adjustment type is chargeable.
     * @throws HomeException
     *             Thrown if there are problems determining whether the service has been
     *             charged.
     */
    public static boolean isChargeable(final Context context, final Account account, final int adjustmentType,
            final long amount, final Date startDate, final Date endDate, final Date billingDate,
            final boolean isPrebilled) throws HomeException
    {
        final And condition = new And();
        condition.add(new GTE(TransactionXInfo.RECEIVE_DATE, startDate));
        condition.add(new EQ(TransactionXInfo.BAN, account.getBAN()));
        condition.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, adjustmentType));
        MaxVisitor lastTransactionVisitor = new MaxVisitor(new TransactionReceiveDateComparator(false));
        lastTransactionVisitor = (MaxVisitor) ((Home) context.get(TransactionHome.class)).where(context, condition)
                .forEach(context, lastTransactionVisitor);
        final Transaction lastTransaction = (Transaction) lastTransactionVisitor.getValue();
        if (LogSupport.isDebugEnabled(context))
        {
            LogSupport.debug(context, TransactionSupport.class, "Determining whether adjustment type " + adjustmentType
                    + " is chargeable to Account [ " + account.getBAN() + "] on " + billingDate + " (prebilling=["
                    + isPrebilled + "])");
        }
        final boolean result;
        // no transactions in bill cycle = not charged
        if (null == lastTransaction)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, TransactionSupport.class,
                        "Chargeable = TRUE: No transaction in current billing cycle or later");
            }
            result = true;
        }
        else
        {
            // transactions dated after billing date = charged
            if (!billingDate.after(lastTransaction.getReceiveDate()))
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    LogSupport.debug(context, TransactionSupport.class,
                            "Chargeable = FALSE: transaction exist after billing date");
                }
                result = false;
            }
            else
            {
                result = !isChargedAndNotRefunded(context, account, adjustmentType, amount, startDate, billingDate,
                        isPrebilled);
                if (LogSupport.isDebugEnabled(context))
                {
                    LogSupport.debug(context, TransactionSupport.class, "Chargeable = " + result
                            + " according to TransactionSupport.isChargedAndNotRefunded()");
                }
            }
        }
        return result;
    }


    /**
     * This method uses last transaction for an adjustment to compare it with a charge
     * being testified to decide whether it was the a charge or it's reversal
     * 
     * @param ctx
     * @param lastTransaction
     *            - last Transaction for which the amount is supposed to be reversed
     * @param amount
     *            - Amount that is suppose to reverse the last transactions
     * @param startDate
     *            - start date of the transaction and reversal cycle
     * @param endDate
     *            - start date of the transaction and reversal cycle. It is usually the
     *            billing date
     * @param isPrebilled
     * @return
     * @throws HomeException
     */
    private static boolean isChargedAndNotRefunded(final Context ctx, final Transaction lastTransaction, long amount,
            final Date startDate, final Date endDate, final boolean isPrebilled, boolean isPrepaid)
            throws HomeException
    {
        final boolean result;
        // This day will be used for prebilled charges
        final Date dayAfterStartDate = CalendarSupportHelper.get(ctx).getDayAfter(startDate);
        if (lastTransaction != null)
        {
            final boolean isChargedAndNotRefunded;
            // doing the calculation of above variable in a local scope
            // for business use case, the above boolean should used; calculation is dirty
            // and hence separated. Expect a better way (using charging history) to find
            // charge cases
            {
                final boolean isCharged;
                final boolean isReversed;
                if (lastTransaction.getAmount() == 0)
                {
                    // if the charge in last transaction was 0; we can't really say it was
                    // charge or a reversal. Therefore we assume that if the amount being
                    // testified is positive,
                    // it were charged. Since the last transaction does not confirm that
                    // charge was applied, we negate the scope for refund
                    isCharged = (amount > 0);
                    isReversed = false;
                }
                else
                {
                    // if the last transaction amount (- or +) we are sure that a charge
                    // was applied
                    isCharged = true;
                    // Now this condition compares the sing of last transaction with
                    // charge amount to see if it is a reversal
                    isReversed = ((lastTransaction.getAmount() * amount) <= 0);
                }
                // The if-else and this assignment can be redesigned with one boolean but
                // the logic is sneaky, better be readeable
                isChargedAndNotRefunded = isCharged && !isReversed;
            }
            if (isPrepaid || !isPrebilled)
            {
                result = isChargedAndNotRefunded;
            }
            else
            {
                // this code is ugly..it assumes based on the flat (isPrebilled) that
                // caller had adjusted the billing cycle and readjusts it's again
                // we are mixing logic with data. Be ware !!
                /*
                 * we need this if check because in the case of pre-billing are loading
                 * the transaction from the bill cycle plus one previous day. And the bill
                 * cycle time frame may contain the pre-billed transaction from the next
                 * bill cycle. So, we take into account only the non-prorated transactions
                 * from the billing day only. And prorated transactions that happened in
                 * the bill cycle.
                 */
                if (lastTransaction.isSubscriptionCharge())
                {
                    result = isChargedAndNotRefunded && !lastTransaction.getReceiveDate().before(dayAfterStartDate);
                }
                else
                {
                    result = isChargedAndNotRefunded && lastTransaction.getReceiveDate().before(dayAfterStartDate)
                            && !lastTransaction.getReceiveDate().before(startDate);
                }
            }
        }
        else
        {
            // if lastTransaction is null it means that we do not have any transactions in
            // he given start and end dates (cycle)
            result = false;
        }
        return result;
    }


    public static void setSubscriberID(final Context ctx, final Transaction transaction) throws HomeException
    {
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        if (sub == null || !sub.getMSISDN().equals(transaction.getMSISDN()))
        {
            final Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, transaction.getMSISDN());
            if (null == msisdn)
            {
                throw new HomeException("Could not find MSISDN for \"" + transaction.getMSISDN() + "\".");
            }

            sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn.getMsisdn(),
                    CalendarSupportHelper.get(ctx).getDayAfter(transaction.getTransDate()));
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
    
    
    
    /**
     * Gets the Transactions for the given account within the given period.
     *
     * @param context
     *            The operating context.
     * @param accountIdentifier
     *            The Account identifier.
     * @param start
     *            The start date of the period (inclusive).
     * @param end
     *            The end date of the period (inclusive).
     * @return A collection of Transactions.
     */
    public static Collection getTransactionsForAccountInclusive(final Context context, final String accountIdentifier,
        final Date start, final Date end)
    {

		Home home = (Home) context.get(TransactionHome.class);
		Collection transactions = null;
        try
        {
        	final And and = new And();

        	and.add(new EQ(TransactionXInfo.BAN, accountIdentifier));
        	and.add(new GTE(TransactionXInfo.RECEIVE_DATE, start));
        	and.add(new LTE(TransactionXInfo.RECEIVE_DATE, end));

        	home = home.where(context,and);
            transactions = home.selectAll();
        }
        catch (final HomeException exception)
        {
           /* ReportUtilities.logMajor(context, TransactionSupport.class.getName(),
                    "Failed getting Transaction records for BAN \"{0}\" " + "with \"{1}\" <= date < \"{2}\".", new String[]
                    { accountIdentifier, start.toString(),end.toString() }, exception);*/

        }


        return transactions;
    }

    
    
    public static long calculateTotalAccountPayments(final Context ctx,final Account account, final boolean includeNonResponsibleSubAccounts, final Date startDate, final Date endDate)
    throws DunningProcessException
    {
        try
        {
            final AdjustmentTypeCategoryAccumulator payments = new AdjustmentTypeCategoryAccumulator(ctx,
                AdjustmentTypeEnum.StandardPayments);
    
            final AdjustmentTypeCategoryAccumulator otherCharges = new AdjustmentTypeCategoryAccumulator(ctx,
                AdjustmentTypeEnum.Other);
    
            final AdjustmentTypeCategoryAccumulator paymentPlanCredits = new AdjustmentTypeCategoryAccumulator(ctx,
                AdjustmentTypeEnum.PaymentPlanLoanCredit);
    
            final AdjustmentTypeCategoryAccumulator paymentPlanReversals = new AdjustmentTypeCategoryAccumulator(ctx,
                AdjustmentTypeEnum.PaymentPlanLoanReversal);
    
            final Collection<Transaction> transactions;
            
            if (includeNonResponsibleSubAccounts)
            {
                transactions = new ArrayList<Transaction>();
                for (Account subAccount : AccountSupport.getNonResponsibleAccounts(ctx, account))
                {
                    transactions.addAll(CoreTransactionSupportHelper.get(ctx).getTransactionsForAccountInclusive(ctx, subAccount
                            .getBAN(), startDate, endDate));
                }
            }
            else
            {
                transactions = CoreTransactionSupportHelper.get(ctx).getTransactionsForAccountInclusive(ctx, account
                        .getBAN(), startDate, endDate);
            }
    
            final Iterator transactionIterator = transactions.iterator();
            while (transactionIterator.hasNext())
            {
                final Transaction transaction = (Transaction) transactionIterator.next();
                payments.accumulate(transaction);
    
                /*
                 * [Cindy Wong] 2008-04-11: Consider account entering and exiting payment
                 * plan as well, as they both affect the balance.
                 */
                paymentPlanCredits.accumulate(transaction);
                paymentPlanReversals.accumulate(transaction);
    
                if (transaction.getAmount() < 0)
                {
                    otherCharges.accumulate(transaction);
                }
            }
    
            return payments.getAmount() + otherCharges.getAmount() + paymentPlanCredits.getAmount()
                + paymentPlanReversals.getAmount();
        }
        catch (final HomeException xp)
        {
            throw new DunningProcessException("calculatePaymentsSinceDate error. " + xp + ", account"
                + account.getBAN(), xp);
        }
    }
    
} // class
