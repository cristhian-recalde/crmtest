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

package com.trilogy.app.crm.poller.agent;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemTransactionMethodsConstants;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.TransactionMethodHome;
import com.trilogy.app.crm.bean.TransactionMethodXInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.TFABillingProcessor;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.support.TransferSupport;
import com.trilogy.app.crm.transfer.TransferCDRTransactionCreator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * Receives a CDR 375 and stores it in the transaction home
 * 
 * @author ltang
 *
 */
public class TFABillingAgent implements ContextAgent
{
    private CRMProcessor processor_; 
    private final Transaction defaultThirdPartyTrans_ = new Transaction();

    private static final String PM_MODULE = TFABillingAgent.class.getName();
    private static final int TFA_BILLING_ER_INDEX_SPID = 2;
    private static final int TFA_BILLING_ER_INDEX_MSISDN = 3;
    private static final int TFA_BILLING_ER_INDEX_BILLINGDATE = 4;
    private static final int TFA_BILLING_ER_INDEX_ADJUSTMENTTYPE = 5;
    private static final int TFA_BILLING_ER_INDEX_ADJUSMENTDESCRIPTION = 6; // used to indicate destination msisdn
    private static final int TFA_BILLING_ER_INDEX_AMOUNT = 7;
    private static final int TFA_BILLING_ER_INDEX_TRANSACTIONID = 8;  
    private static final int TFA_BILLING_ER_INDEX_NEWBALANCE = 10;   
    private static final int TFA_BILLING_ER_INDEX_SUBSCRIPTIONTYPE = 12;   
    private static final int TFA_BILLING_ER_INDEX_AGENT = 13;
    private static final int TFA_BILLING_ER_INDEX_TRANSACTIONMETHOD = 14;
    private static final int TFA_BILLING_ER_INDEX_SUBSCRIPTIONID = 17; // not known by TFA
    private static final int TFA_BILLING_ER_NUMBER_OF_FIELDS = 23 ;
    
    public TFABillingAgent(CRMProcessor processor)
    {
        super();
        processor_ = processor;
        defaultThirdPartyTrans_.setBAN("");
        defaultThirdPartyTrans_.setSpid(-1);
        defaultThirdPartyTrans_.setSubscriberID(TransferSupport.EXTERNAL_ID);
    }
    
    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        List<String> params = new ArrayList<String>();
        ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");
                       
        try
        {    
            try 
            {
                CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',',info.getErid(), this);
            } 
            catch (FilterOutException e)
            {
                return; 
            }
            
            switch (Integer.parseInt(info.getErid()))
            {
                case TFABillingProcessor.TFA_CDR_IDENTIFIER:
                {
                    createTransaction(ctx, new Date(info.getDate()), params);
                    break;
                }
                default:
                {
                    // Unknown TFA ER -- Ignore.
                }
            }
        }
        catch (final Throwable t)
        {
            new MinorLogMsg(this, "Failed to process ER " + TFABillingProcessor.TFA_CDR_IDENTIFIER + " because of Exception " + t.getMessage(), t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally 
        {
            pmLogMsg.log(ctx);
        }
    }

    /**
     * Creates a transaction record from the parameters captured from CDR 375
     * 
     * @param ctx
     * @param date
     * @param params
     */
    private void createTransaction(Context ctx, Date date, List<String> params)
    {        
        new DebugLogMsg(this, "createTransaction(): Begin", null).log(ctx);
        String msisdn = "";
        try
        {
            CRMProcessorSupport.validateErFields(params, TFA_BILLING_ER_NUMBER_OF_FIELDS);
            
            // retrieve the MSISDN of the entity that is being charged
            msisdn = CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_MSISDN);
            
            // retrieve subscription type
            long subscriptionType = Long.parseLong(CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_SUBSCRIPTIONTYPE));            
            
            // retrieve the date and time the transaction occurred
            final String billingDateStr = CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_BILLINGDATE);
            final Date billingDate;            
            if ( billingDateStr.length() == 0 || billingDateStr.equals("") )
            {
                billingDate = null;
            }
            else
            {
                try
                {
                    billingDate = CRMProcessorSupport.getDate(billingDateStr);
                }
                catch (ParseException e)
                {
                    final String formattedMsg = MessageFormat.format(
                        "Could not parse billingDate String \"{0}\".",
                        new Object[]{ billingDateStr });

                    throw new HomeException(formattedMsg, e);
                }
            }

            
            // retrieve adjustment description -- mapped to CSR input field
            final String csrInput = CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_ADJUSMENTDESCRIPTION);
            
            // retrieve amount of the particular transfer transaction
            final long amount = Long.parseLong(CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_AMOUNT));

            // retrieve new balance
            String newBalanceStr = CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_NEWBALANCE);
            if (newBalanceStr.length() == 0 || newBalanceStr.equals(""))
            {
                newBalanceStr = "0";
            }
            long newBalance = Long.parseLong(newBalanceStr);
            
            // retrieve adjustment type
            final int adjustmentTypeInt = Integer.parseInt(CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_ADJUSTMENTTYPE));            
            final AdjustmentType adjustmentType = mapToTFATransactionType(ctx, adjustmentTypeInt);
            
            // retrieve agent
            String csrIdentifier = CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_AGENT);
            if (csrIdentifier.length() == 0 || csrIdentifier.equals(""))
            {
                csrIdentifier = SystemSupport.SYSTEM_AGENT;
            }            

            // retrieve transaction method
            long transactionMethod;
            String transactionMethodStr = (CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_TRANSACTIONMETHOD));
            if (transactionMethodStr.length() == 0 || transactionMethodStr.equals(""))
            {
                transactionMethod = SystemTransactionMethodsConstants.TRANSACTION_METHOD_TRANSFER;
            }
            else
            {
                Home home = (Home) ctx.get(TransactionMethodHome.class);
                TransactionMethod method = (TransactionMethod) home.find(ctx, new EQ(TransactionMethodXInfo.NAME, transactionMethodStr));
                if (method != null)
                {
                    transactionMethod = method.getIdentifier();
                }
                // not found, set default
                else
                {
                    transactionMethod = SystemTransactionMethodsConstants.TRANSACTION_METHOD_TRANSFER;
                }
            }
            
            // retrieve external transaction Id
            String extTransactionId = CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_TRANSACTIONID, "");

         // do a subscriber lookup based on the retrieved MSISDN
            
            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Attempting to create transaction for CDR 375: [MSISDN= " + msisdn + "] "
                        + "[SUBSCRIPTION" + subscriptionType + "]"
                        + "[CSRInput=" + csrInput + "]"
                        + "[Amount=" + amount + "] "
                        + "[NewBalance=" + newBalance + "] "
                        + "[AdjustmentType=" + adjustmentType + "] "
                        + "[CsrIdentifier=" + csrIdentifier + "] "
                        + "[BillingDate=" + billingDate + "] "
                        + "[ExtTransactionId=" + extTransactionId + "] "
                        + "[TransactionMethod=" + transactionMethod + "]", null).log(ctx);
            }
            
            
            // validate transaction amount and type
            validateAmount(ctx, adjustmentType, amount);
            
            Subscriber subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn, subscriptionType, billingDate);
            if(null==subscriber)
            {
                //we have a third party subscriber
                Home transHome = (Home)ctx.get(Common.DISCOUNT_PLAIN_TXN_HOME);
                final Transaction transaction = (Transaction) defaultThirdPartyTrans_.clone();
                transaction.setMSISDN(msisdn);
                transaction.setSubscriptionTypeId(subscriptionType);
                transaction.setSpid(Integer.parseInt(CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_SPID)));
                transaction.setCSRInput(csrInput);
                transaction.setAmount(amount);
                transaction.setBalance(newBalance);
                transaction.setTransDate(billingDate);
                transaction.setTransactionMethod(transactionMethod);
                transaction.setExtTransactionId(extTransactionId);
                transaction.setAdjustmentType(adjustmentType.getCode());
                transHome.create(ctx,transaction);
                if(LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Successfully created transaction for CDR 375 - External", null).log(ctx);
                }
                return;
            }
            
            // create a transaction record for the polled CDR 375 in CRM
            TransferCDRTransactionCreator.getInstance().createTransaction(ctx, subscriber, amount, newBalance, adjustmentType, 
                                                            csrIdentifier, billingDate, csrInput, extTransactionId, transactionMethod);

            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Successfully created transaction for CDR 375", null).log(ctx);
            }
            
        }
        catch (final Throwable t)
        {
            String msg = "Failed to create transaction record for TFA transfer for subscriber mobile number , \""
                + msisdn
                + "\". " + t.getMessage(); 
            
            new MinorLogMsg(this, msg, t).log(ctx);
            
            final IllegalStateException newException = new IllegalStateException(msg);                   
            
            newException.initCause(t);
            throw newException;
        }
        new DebugLogMsg(this, "createTransaction(): End", null).log(ctx);
    }

    /**
     * Validates the amount based on the adjustment type
     * @param ctx
     * @param adjustmentType
     * @param amount
     * @throws HomeException
     */
    private void validateAmount(Context ctx, AdjustmentType adjustmentType, long amount) throws HomeException
    {
        if (adjustmentType.getAction() == AdjustmentTypeActionEnum.CREDIT)
        {
            if (amount > 0)
            {
                throw new HomeException("For credit transactions, the amount should be negative");
            }
        }
        else if (adjustmentType.getAction() == AdjustmentTypeActionEnum.DEBIT)
        {
            if (amount < 0)
            {
                throw new HomeException("For debit transactions, the amount should be positive");
            }
        }
    }

    private AdjustmentType mapToTFATransactionType(final Context ctx, final int adjustmentTypeInt)
    throws HomeException
    {
        switch (adjustmentTypeInt)
        {
            // debit towards contributor
            case (1): 
                return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.TransferFundsContributorDebit_INDEX);
            // credit towards recipient
            case (2):
                return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.TransferFundsRecipientCredit_INDEX); 
            // surplus for contributor
            case (3):
                return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.TransferFundsContributorSurplus_INDEX);
            // surplus for recipient
            case (4):
                return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.TransferFundsRecipientSurplus_INDEX);
            // discount for contributor
            case (5):
                return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.TransferFundsContributorDiscount_INDEX);
            // discount for recipient
            case (6):
                return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.TransferFundsRecipientDiscount_INDEX);
            default:
                throw new IllegalStateException("Invalid Adjustment Type Mapping ID " + adjustmentTypeInt);
        }
    }
}
