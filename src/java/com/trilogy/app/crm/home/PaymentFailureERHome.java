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

import com.trilogy.app.crm.bas.tps.TPSProcessor;
import com.trilogy.app.crm.bas.tps.pipe.DuplicateMSISDNException;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.SystemTransactionMethodsConstants;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.payment.PaymentFailureTypeEnum;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.PaymentExceptionSupport;
import com.trilogy.app.crm.transaction.PrepaidPaymentException;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.DefaultExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * If the transaction is a payment we need to intercept any error and log the
 * 1124 ER to keep track of the problem, as well as log a PaymentException Record.
 * @author arturo.medina@redknee.com
 *
 */
public class PaymentFailureERHome extends HomeProxy
{

    /**
     * Unique constructor that receives the delegate to continue the pipeline.
     * @param delegate the home to delegate the chain in the pipeline
     */
    public PaymentFailureERHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Transaction txn = (Transaction) obj;
        try
        {
            return super.create(ctx, obj);
        }
        catch (DuplicateMSISDNException e)
        {
            LogSupport.debug(ctx, this, "DuplicateMSISDNException has occured with the following list : "
                    + e.getSubscribersList()
                    + " Adding a 1124 ER ");

            writeMultipleSubscribersER(ctx, e.getSubscribersList(), txn, DUPLICATE_MSISDN_EXCEPTION, e);
            //Log Payment Exception Record
            createOrUpdatePaymentExceptionRecord(ctx, txn, PaymentFailureTypeEnum.MULTISUB, e.getSubscribersList());
            throw e;
        }
        catch (HomeException e)
        {
            // Only update payment exception records if the transaction is not from a failed transfer.
            // Failed transfers are handled with transfer exception records.
            if (txn.getTransactionMethod() != SystemTransactionMethodsConstants.TRANSACTION_METHOD_TRANSFER)
            {
                boolean prepaidException = false;
                
                LogSupport.debug(ctx, this, "General Error occured on transaction for subscriber "
                        + txn.getSubscriberID()
                        + " Adding a 1124 ER ",
                        e);
    
                writeMultipleSubscribersER(ctx, "", txn, 1, e);
    
                
                // Checking whether the cause of the exception was a prepaid payment exception and marking it as so.
                if (e.getCause()!=null && (e.getCause() instanceof CompoundIllegalStateException))
                {
                    
                    final DefaultExceptionListener defaultListener = new DefaultExceptionListener();
                    ((CompoundIllegalStateException) e.getCause()).rethrow(defaultListener);
                    
                    for (Object t : defaultListener.getExceptions())
                    {
                        if (t instanceof PrepaidPaymentException)
                        {
                            prepaidException = true;
                            break;
                        }
                    }
                }
                //Log Payment Exception Record
                if (prepaidException)
                {
                    createOrUpdatePaymentExceptionRecord(ctx, txn, PaymentFailureTypeEnum.PREPAID, txn.getSubscriberID());
                }
                else
                {
                    createOrUpdatePaymentExceptionRecord(ctx, txn, null, txn.getSubscriberID());
                }
            }
            throw e;
        }
        catch (Throwable e)
        {            
            // Only update payment exception records if the transaction is not from a failed transfer.
            // Failed transfers are handled with transfer exception records.
            if (txn.getTransactionMethod() != SystemTransactionMethodsConstants.TRANSACTION_METHOD_TRANSFER)
            {
                LogSupport.debug(ctx, this, "General Error occured on transaction for subscriber "
                        + txn.getSubscriberID()
                        + " Adding a 1124 ER ",
                        e);
    
                writeMultipleSubscribersER(ctx, "", txn, GENERAL_EXCEPTION, e);
                //Log Payment Exception Record
                createOrUpdatePaymentExceptionRecord(ctx, txn, null, txn.getSubscriberID());
            } 
            throw new HomeException(e);
        }
    }

    /**
     * Writes in the ER 1124 the information regarding the TPS file and writes a Major
     * message on the log
     * @param ctx The context to get any system information
     * @param txn The transaction record to get the payment information
     * @param subscriberToken the number of subscriber with outstanding owing
     * @param resultCode the result code to write on the ER
     * @param t The exception needed to write on the Major log
     *
     */
    private void writeMultipleSubscribersER(final Context ctx,
            final String subscriberToken,
            final Transaction txn,
            final int resultCode,
            final Throwable t)
    {
        if (CoreTransactionSupportHelper.get(ctx).isPayment(ctx, txn)
                && (txn.getPayee() == PayeeEnum.Account || ctx.has(TPSProcessor.class)))
        {
            LogSupport.debug(ctx, this, "General Error occured on the payment for BAN "
                    + txn.getBAN()
                    + " Adding a 1124 ER ",
                    t);

            final String fileName = CoreTransactionSupportHelper.get(ctx).getTPSFileName(ctx);

            ERLogger.writeMultipleSubscribersInHistoryER(ctx,
                    subscriberToken,
                    fileName,
                    txn,
                    resultCode);

        }

        LogSupport.major(ctx, this,
                "Failed to generate the transaction " + txn.getAdjustmentType()
                + " for subscriber " + txn.getSubscriberID(),
                t);
    }
    
    /**
     * Create or Modify the Payment Exception Record belonging to the
     * given transaction
     * @param ctx
     * @param trans
     * @param failureType
     * @param subscriberIds
     */
    private void createOrUpdatePaymentExceptionRecord(
            final Context ctx, 
            final Transaction trans,
            PaymentFailureTypeEnum failureType, 
            final String subscriberIds)
    {
        if (CoreTransactionSupportHelper.get(ctx).isStandardPayment(ctx, trans))
        {
            if (failureType == null)
            {
                if (trans.getMSISDN() == null 
                        || trans.getSubscriberID() == null)
                {
                    failureType = PaymentFailureTypeEnum.ACCOUNT;     
                }
                else
                {
                    failureType = PaymentFailureTypeEnum.SUBSCRIBER;
                }
            }
            PaymentExceptionSupport.createOrUpdatePaymentExceptionRecord(
                    ctx, 
                    trans,
                    failureType,
                    subscriberIds);
        }
    }

    /**
     * The serial version UID
     */
    private static final long serialVersionUID = 2325381946171439420L;

    /**
     * ER code for a duplicate exception
     */
    public static final int DUPLICATE_MSISDN_EXCEPTION = 2;

    /**
     * ER code for a general payment error
     */
    public static final int GENERAL_EXCEPTION = 1;
}
