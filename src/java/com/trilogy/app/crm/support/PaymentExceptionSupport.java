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
import java.security.Principal;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQDay;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.payment.PaymentException;
import com.trilogy.app.crm.bean.payment.PaymentExceptionHome;
import com.trilogy.app.crm.bean.payment.PaymentExceptionXInfo;
import com.trilogy.app.crm.bean.payment.PaymentFailureTypeEnum;
import com.trilogy.app.crm.bean.payment.ReprocessException;
import com.trilogy.app.crm.web.control.PaymentExceptionResolutionSubIDWebControl;
import com.trilogy.util.snippet.log.Logger;

/**
 * Utility methods to look up, create, and modify Payment Exception Records 
 */
public class PaymentExceptionSupport 
{
    /**
     * Returns the PaymentException record with the following parameters.
     * May return NULL if no such PaymentException exists in the system.
     * @param ctx
     * @param ban - BAN for the query, could be null
     * @param msisdn - MSISDN, for the query, cannot be null
     * @param transactionDate
     * @param adjustmentType - Adjustment type of failed transaction
     * @param failureType - Account, Subscriber, or Muli-Subscriber failure type.
     * @return
     * @throws HomeException
     */
    public static PaymentException getPaymentException(
            final Context ctx, 
            final String ban,
            final String msisdn, 
            final Date transactionDate,
            final int adjustmentType,
            final PaymentFailureTypeEnum failureType)
    {
        PaymentException record = null;
        try 
        {
            if (msisdn == null)
            {
                throw new HomeException("PaymentExceptionSupport.getAllPaymentException, " +
                        "Msisdn is empty for Payment Exception query.");
            }
            And predicate = new And();
            predicate.add(new EQDay(PaymentExceptionXInfo.TRANS_DATE, transactionDate));
            predicate.add(new EQ(PaymentExceptionXInfo.ADJUSTMENT_TYPE, Integer.valueOf(adjustmentType)));
            predicate.add(new EQ(PaymentExceptionXInfo.TYPE, failureType));
            predicate.add(new EQ(PaymentExceptionXInfo.MSISDN, msisdn));
            if (ban != null && ban.trim().length() > 0)
            {
                predicate.add(new EQ(PaymentExceptionXInfo.BAN, ban));
            }
            
            Home eHome = (Home) ctx.get(PaymentExceptionHome.class);
            record = (PaymentException) eHome.find(ctx, predicate);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, 
                    "PaymentExcpetionSupport.getPaymentException", 
                    " Failed to query Payment Exception for the following parameters: " +
                    " BAN= " + (ban!=null? ban :"") +
                    " MSISDN" + (msisdn!= null? msisdn : "") +
                    " DATE=" + transactionDate.getTime() +
                    " ADJUSTMENTTYPE=" + adjustmentType, e);
            }
        }
        return record;
    }
    
    public static PaymentException getPaymentException(
            final Context ctx, 
            final String ban,
            final String msisdn 
            )
    {
        PaymentException record = null;
        try 
        {
            if (msisdn == null)
            {
                throw new HomeException("PaymentExceptionSupport.getAllPaymentException, " +
                        "Msisdn is empty for Payment Exception query.");
            }
            And predicate = new And();           
            
            predicate.add(new EQ(PaymentExceptionXInfo.MSISDN, msisdn));
            if (ban != null && ban.trim().length() > 0)
            {
                predicate.add(new EQ(PaymentExceptionXInfo.BAN, ban));
            }
            
            Home eHome = (Home) ctx.get(PaymentExceptionHome.class);
            record = (PaymentException) eHome.find(ctx, predicate);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, 
                    "PaymentExcpetionSupport.getPaymentException", 
                    " Failed to query Payment Exception for the following parameters: " +
                    " BAN= " + (ban!=null? ban :"") +
                    " MSISDN" + (msisdn!= null? msisdn : ""));
            }
        }
        return record;
    }
    
    public static boolean isPaymentExceptionRecordPresent(
            final Context ctx, 
            final String ban,
            final String msisdn )
    {
    	PaymentException record = getPaymentException(ctx,ban,msisdn);
    	if(record == null)
    	{
    		return false;
    	}
    	return true;
    }
    
    /**
     * Retrieves the Payment Exception matching the given ID.
     * @param ctx
     * @param id
     * @return
     * @throws HomeException
     */
    public static PaymentException getPaymentExceptionByID(Context ctx, Long id)
        throws HomeException
    {
        Home home = (Home) ctx.get(PaymentExceptionHome.class);
        if (home == null)
        {
            throw new HomeException("The PaymentExceptionHome is not installed in the Context.");
        }
        return (PaymentException) home.find(ctx, id); 
    }
    
    /**
     * Return the SPID associated with the given PaymentException record, 
     * retrieved from the SPID assignment of the record's associated Account.
     * @param ctx
     * @param exception Throws exception if the Account cannot be retrieved, 
     * identified by record's BAN.
     */
    public static int getSpid(Context ctx, PaymentException exception)
        throws ReprocessException
    {
        int spid = 0;
        try
        {
            Account account = AccountSupport.getAccount(ctx, exception.getBan());
            spid = account.getSpid();
        }
        catch(HomeException e)
        {
            throw new ReprocessException("Error while trying to reprocess Failed Payment.  " +
                    "Failed to look up Account for BAN=" + exception.getBan(),
                    e);
        }
        catch (NullPointerException npe)
        {
            throw new ReprocessException("Error while trying to reprocess Failed Payment.  " +
                    "No such Account exists, BAN=" + exception.getBan(),
                    npe);
        }
        return spid;
    }
    
    
    /**
     * Creates a Payment Exception record in the system, with the properties given.
     * If the Payment Exception record exists, then the counters are increased and the 
     * record is updated in the system.
     * @param ctx
     * @param trans
     * @param failureType
     * @param subscriberIds
     */
    public static void createOrUpdatePaymentExceptionRecord(
            final Context ctx, 
            final Transaction trans,
            final PaymentFailureTypeEnum failureType,
            final String subscriberIds)
    {
        Home eHome = (Home) ctx.get(PaymentExceptionHome.class);
        
        /* A Payment Exception record is considered to be the "same" record when the
         * BAN, MSIDN, Transaction Date, Adjustment Type, and Failure Type are the same. */ 
        PaymentException record = getPaymentException(
                ctx, 
                trans.getBAN(),
                trans.getMSISDN(), 
                trans.getTransDate(),
                trans.getAdjustmentType(),
                failureType);
        
        if (record == null)
        {
            // Create record
            record = new PaymentException();
            record.setType(failureType);
            record.setBan(trans.getBAN());
            record.setSubscriberIds(subscriberIds!= null ? subscriberIds :"");
            record.setMsisdn(trans.getMSISDN());
            record.setAmount(trans.getAmount());
            record.setAgent(trans.getAgent());
            record.setAdjustmentType(trans.getAdjustmentType());
            record.setTransDate(trans.getTransDate());
            record.setExtTransactionId(trans.getExtTransactionId());
            record.setTpsFileName(trans.getAgent());
            record.setLocationCode(trans.getLocationCode());
            record.setPaymentDetails(trans.getPaymentDetails());
            record.setTransactionMethod(trans.getTransactionMethod());
            record.setSubscriptionTypeId(trans.getSubscriptionTypeId());
            
            User principal = (User) ctx.get(Principal.class);
            if (principal != null)
            {
                record.setLastAttemptAgent(principal.getId());
            }
            else
            {
                record.setLastAttemptAgent(trans.getAgent());
            }
            
            try
            {
                eHome.create(record);
                new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_EXCEPTION_CREATED, 1).log(ctx);
            }
            catch (HomeException e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, 
                        "PaymentExcpetionSupport.createOrUpdatePaymentExceptionRecord", 
                        " Failed to create the PaymentException Record=" + record);
                }
            }
        }
        else
        {
            try
            {
                updatePaymentExceptionCounters(ctx, record);
            }
            catch (HomeException e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, 
                        "PaymentExcpetionSupport.createOrUpdatePaymentExceptionRecord", 
                        " Failed to store the PaymentException Record=" + record);
                }
            }
        }
    }
    
    /**
     * Advance the given record's "Attempts" counters.
     * Advance the number of attempts by one, the last attempt date to the current date and time,
     * and the last attempt agent to the agent currently logged in.
     * @param ctx
     * @param record
     */
    public static void updatePaymentExceptionCounters(Context ctx, PaymentException record)
        throws HomeException
    {
        Home eHome = (Home) ctx.get(PaymentExceptionHome.class);
        
        // Update counters.
        record.setAttempts(record.getAttempts() + 1);
        record.setLastAttemptDate(new Date());
        record.setLastAttemptAgent(findCurrentAgent(ctx));
        
        try
        {
            eHome.store(ctx,record);
            new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_EXCEPTION_UPDATED, 1).log(ctx);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, 
                    "PaymentExcpetionSupport.updatePaymentExceptionCounters", 
                    " Failed to update the markers for Attempts in the PaymentException Record=" + record);
            }
            throw e;
        }
    }
    
    /**
     * Creates a Payment Transaction record in the system, with the given Payment Exception record properties. 
     */
    public static void createTransactionRecord(
            final Context ctx, 
            final PaymentException exception)
        throws ReprocessException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, 
                    "PaymentExceptionSupport.createTransactionRecord", 
                    "Begin creating Payment Transaction for the given Payment Exception record=[" + exception +"].");
        }

        Context subCtx = ctx.createSubContext();
        
        // Perform some Validation
        validatePaymentExceptionRecord(exception);
        
        // Format Transaction
        Transaction trans = adaptPaymentException(subCtx, exception);
        
        try 
        {
            //TODO: look up Subscriber and place it in the context.
            /* SubscriberIdentifierSettingHome in the Transaction pipeline sets the Subscriber ID
             * after a lookup to the Msisdn History.  Since the Payment Exception has already chosen
             * the Subscriber to receive the payment, we have to place the selected Subscriber in the 
             * Context to override the Msisdn History lookup.
             * In addition, we must make sure that the Subscriber's Msisdn is the same as the 
             * Payment Exceptions' (and hence, payment Transaction's) Msisdn. 
             */
            if (trans.getSubscriberID().length() > 0)
            {
                Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(subCtx, trans.getSubscriberID());
                if (sub == null)
                {
                    throw new ReprocessException("No such subscriber exists in the system. ID=" + trans.getSubscriberID());
                }
                sub.setMSISDN(trans.getMSISDN());
                subCtx.put(Subscriber.class, sub);
            }

            //Create Transaction
            Home home = (Home) subCtx.get(TransactionHome.class);
            home.create(trans);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, 
                        "PaymentExceptionSupport.createTransactionRecord", 
                        "Failed to create the Transaction for Payment Exception recordID=[" + exception.getId() +"].");
            }
            
            throw new ReprocessException("Error while trying to reprocess Failed Payment.  " +
                    "Failed to create the Transaction while processing Failed Payment: " + e.getMessage(), 
                    e);
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, 
                    "PaymentExceptionSupport.createTransactionRecord", 
                    "Successfully created Payment Transaction for the given Payment Exception recordID=[" + exception.getId() +"].");
        }
    }
    
    /**
     * Perform some validation on the given Payment Exception record
     * to be transformed to a Transaction. 
     * @param exception
     * @throws ReprocessException
     */
    private static void validatePaymentExceptionRecord(PaymentException exception)
        throws ReprocessException
    {
        if ( (exception.getType().equals(PaymentFailureTypeEnum.MULTISUB)
                || exception.getType().equals(PaymentFailureTypeEnum.SUBSCRIBER))
                && (exception.getSelectedSubcriberID().indexOf(',') > 0 
                   || exception.getSelectedSubcriberID().equals(PaymentExceptionResolutionSubIDWebControl.OPTIONAL_VALUE)))
        {
                throw new ReprocessException("Error while trying to reprocess Failed Payment.  " +
                        "Please select one Subscriber to receive the payment before processing.");
        }
    }
    
    /**
     * The subset of Transaction fields adapted from PaymentException records 
     * is the subset polled by the TPS poller. (See ConvertTpsToTransactionAgent)
     * @param context
     * @param exception
     * @return
     * @throws ReprocessException
     */
    private static Transaction adaptPaymentException(
            Context context, 
            PaymentException exception)
        throws ReprocessException
    {
        User principal = (User) context.get(Principal.class);
        int spid = getSpid(context, exception);
        
        Transaction trans;
        
        try
        {
            trans = (Transaction) XBeans.instantiate(Transaction.class, context);
        }
        catch(Exception ex)
        {
            throw new ReprocessException("Fail to instantiate transaction bean", ex);
        }

        trans.setBAN(exception.getBan());
        try
        {
            final Account account = HomeSupportHelper.get(context).findBean(context, Account.class, exception.getBan());
            trans.setResponsibleBAN(account.getResponsibleBAN());
        }
        catch (HomeException e)
        {
            Logger.minor(context, PaymentExceptionSupport.class,
                    "Unable to retrieve Account [" + exception.getBan() + "]", e);
        }

        if (exception.getType().equals(PaymentFailureTypeEnum.MULTISUB)
                || exception.getType().equals(PaymentFailureTypeEnum.SUBSCRIBER))
        {
            //Validation has been done.
            trans.setSubscriberID(exception.getSelectedSubcriberID());    
            trans.setPayee(PayeeEnum.Subscriber);
        }
        else 
        {
            trans.setPayee(PayeeEnum.Account);
        }
        trans.setMSISDN(exception.getMsisdn());
        trans.setAmount(exception.getAmount());
        trans.setAgent(exception.getAgent());
        trans.setAdjustmentType(exception.getAdjustmentType());
        trans.setAction(AdjustmentTypeActionEnum.DEBIT); //payments should always be Debit
        trans.setGLCode(AdjustmentTypeSupportHelper.get(context).getGLCodeForAdjustmentType(context, exception.getAdjustmentType(), spid));
        trans.setTransDate(exception.getTransDate());
        trans.setReceiveDate(new Date());
        trans.setExtTransactionId(exception.getExtTransactionId());
        trans.setLocationCode(exception.getLocationCode());
        trans.setPaymentDetails(exception.getPaymentDetails());
        trans.setTransactionMethod(exception.getTransactionMethod());
        trans.setCSRInput("Reprocessed by Agent='" + principal.getId() + "' on " + formatDate(new Date()));
        trans.setSubscriptionTypeId(exception.getSubscriptionTypeId());
        
        return trans;
    }
    
    /**
     * Returns the given Date formatted using the constant Simple Date Format DATE_FORMAT
     * @param date
     * @return
     */
    private static String formatDate(final Date date)
    {
        StringBuffer result = new StringBuffer();
        // TODO 2010-10-01 DateFormat access needs synchronization
        DATE_FORMAT.format(new Date(), result, new FieldPosition(0));
        return result.toString();
    }
    
    /**
     * Returns the Name of the Current user that is logged in.  
     * Returns a blank string if the user cannot be found.
     * @param context
     * @return
     */
    private static String findCurrentAgent(Context context)
    {
        String value = "";
        User principal = (User) context.get(Principal.class);
        if (principal != null)
        {
            value = principal.getId();
        }
        else
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, "PaymentExceptionSupport.findCurrentAgent", "Could not retrieve current User.");
            }
        }
        return value;
    }
    
    final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd,yyyy"); 

}
