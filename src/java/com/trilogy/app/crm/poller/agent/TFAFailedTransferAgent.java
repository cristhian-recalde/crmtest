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
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemTransactionMethodsConstants;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.TransactionMethodHome;
import com.trilogy.app.crm.bean.TransactionMethodXInfo;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.TFAFailedTransferProcessor;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.transfer.TransferException;
import com.trilogy.app.crm.transfer.TransferExceptionHome;
import com.trilogy.app.crm.transfer.TransferFailureTypeEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * Receives an ER 293 and creates a Transfer Exception for reprocessing
 * 
 * @author ltang
 *
 */
public class TFAFailedTransferAgent implements ContextAgent
{
    private CRMProcessor processor_;

    private static final String PM_MODULE = TFAFailedTransferAgent.class.getName();

    private static final int TFA_FAILED_ER_INDEX_SPID = 2;
    private static final int TFA_FAILED_ER_INDEX_CONTRIBUTORMSISDN = 3;
    private static final int TFA_FAILED_ER_INDEX_RECIPIENTMSISDN = 4;
    private static final int TFA_BILLING_ER_INDEX_TRANSDATE = 5;
    private static final int TFA_FAILED_ER_INDEX_ADJUSMENTTYPE = 6;
    private static final int TFA_FAILED_ER_INDEX_ADJUSMENTREASON = 7;
    private static final int TFA_FAILED_ER_INDEX_AMOUNT = 8;
    private static final int TFA_FAILED_ER_INDEX_TRANSACTIONID = 9;
    private static final int TFA_FAILED_ER_INDEX_SUBSCRIPTIONTYPE = 11;
    private static final int TFA_FAILED_ER_INDEX_AGENT = 12;
    private static final int TFA_FAILED_ER_INDEX_TRANSACTIONMETHOD = 13;
    private static final int TFA_FAILED_ER_INDEX_PAYMENTDETAILS = 14;
    private static final int TFA_FAILED_ER_INDEX_BAN = 15;
    private static final int TFA_FAILED_ER_INDEX_SUBSCRIPTIONID = 16;   // not known by TFA
    private static final int TFA_FAILED_ER_INDEX_LOCATIONCODE = 18;
    private static final int TFA_FAILED_ER_NUMBER_OF_FIELDS = 22;
    
    public TFAFailedTransferAgent(CRMProcessor processor)
    {
        super();
        processor_ = processor;
    }
    
    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        List params = new ArrayList();
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
                case TFAFailedTransferProcessor.TFA_FAILED_TRANSFER_ER_IDENTIFIER:
                {
                    createTransferException(ctx, new Date(info.getDate()), params);
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
            new MinorLogMsg(this, "Failed to process ER " + TFAFailedTransferProcessor.TFA_FAILED_TRANSFER_ER_IDENTIFIER + " because of Exception " + t.getMessage(), t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally 
        {
            pmLogMsg.log(ctx);
        }
    }

    /**
     * Creates a Transfer Exception from the parameters captured in ER 293
     * 
     * @param ctx
     * @param date
     * @param params
     */
    private void createTransferException(Context ctx, Date date, List params)
    {                
        new DebugLogMsg(this, "createTransferException(): Start", null).log(ctx);
        String msisdn = "";
        try
        {
            new DebugLogMsg(this, "Attempting to create TransferException", null).log(ctx);               
         
            CRMProcessorSupport.validateErFields(params, TFA_FAILED_ER_NUMBER_OF_FIELDS);
            
            TransferException transfer = new TransferException();
            
            // retrieve spid
            int spid = Integer.parseInt(CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_SPID));
            transfer.setSpid(spid);
            
            // retrieve msisdn
            msisdn = CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_CONTRIBUTORMSISDN);
            transfer.setMsisdn(msisdn);            
            validateMsisdn(ctx, spid, msisdn);
            
            // retrieve recipient msisdn
            String recipientMsisdn = CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_RECIPIENTMSISDN);
            transfer.setRecipientMsisdn(recipientMsisdn);
            
            // retrieve subscription type
            long subscriptionType = Long.parseLong(CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_SUBSCRIPTIONTYPE));
            transfer.setSubscriptionType(subscriptionType);            
            
            // retrieve agent
            String agent = CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_AGENT);
            if (agent.length() == 0 || agent.equals(""))
            {
                agent = SystemSupport.SYSTEM_AGENT;
            }     
            transfer.setAgent(agent);
            
            // retrieve amount
            transfer.setAmount(Long.parseLong(CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_AMOUNT)));
            
            // retrieve transaction id
            String extTransactionNumStr = CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_TRANSACTIONID);
            if (extTransactionNumStr.length() > 0 && !extTransactionNumStr.equals(""))
            {
                transfer.setExtTransactionId(extTransactionNumStr);                
            }
            
            // retrieve the date and time the transaction occurred
            final String transDateStr = CRMProcessorSupport.getField(params, TFA_BILLING_ER_INDEX_TRANSDATE);
            final Date transDate;            
            if ( transDateStr.length() == 0 || transDateStr.equals("") )
            {
                transDate = null;
            }
            else
            {
                try
                {
                    transDate = CRMProcessorSupport.getDate(transDateStr);
                }
                catch (ParseException e)
                {
                    final String formattedMsg = MessageFormat.format(
                        "Could not parse transDate String \"{0}\".",
                        new Object[]{ transDateStr });

                    throw new HomeException(formattedMsg, e);
                }
            }
            transfer.setTransDate(transDate);            

            // retrieve ban
            String banStr = CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_BAN);
            if (banStr.length() == 0 || banStr.equals(""))
            {
                
                banStr = MsisdnSupport.getBAN(ctx, msisdn, transDate);
            }
            transfer.setBan(banStr);
            
            // retrieve subscription id
            String subscriptionId = CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_SUBSCRIPTIONID);
            if (subscriptionId.length() == 0 || subscriptionId.equals(""))
            {
                subscriptionId = SubscriberSupport.lookupSubscriberIdForMSISDN(ctx, msisdn, subscriptionType, transDate);
            }
            else // if subscription ID is provided, MSISDN & BAN are ignored                
            {
                Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(ctx, subscriptionId);
                transfer.setBan(sub.getBAN());
                transfer.setMsisdn(sub.getMSISDN());
            }
            transfer.setSubscriptionId(subscriptionId);           

            // retrieve type of adjustment that needs to be done
            short adjustmentTypeInt = Short.parseShort(CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_ADJUSMENTTYPE));
            transfer.setType(TransferFailureTypeEnum.get(adjustmentTypeInt));
            
            // retrieve adjustment reason
            transfer.setReason(CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_ADJUSMENTREASON));
            
            // retrieve transaction method
            long transactionMethod = SystemTransactionMethodsConstants.TRANSACTION_METHOD_TRANSFER;
            String transactionMethodStr = CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_TRANSACTIONMETHOD);
            if (transactionMethodStr.length() > 0 && !transactionMethodStr.equals(""))
            {
                // set default
                transfer.setTransactionMethod(transactionMethod);
            }
            else
            {
                Home home = (Home) ctx.get(TransactionMethodHome.class);
                TransactionMethod method = (TransactionMethod) home.find(ctx, new EQ(TransactionMethodXInfo.NAME, transactionMethodStr));
                if (method != null)
                {
                    transfer.setTransactionMethod(method.getIdentifier());
                }
                // not found, set default
                else
                {
                    transfer.setTransactionMethod(transactionMethod);
                }
            }

            // retrieve payment details
            transfer.setPaymentDetails(CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_PAYMENTDETAILS));

            // retrieve location code
            transfer.setLocationCode(CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_LOCATIONCODE));
            
            // retrieve agent
            transfer.setAgent(CRMProcessorSupport.getField(params, TFA_FAILED_ER_INDEX_AGENT));
            
            new DebugLogMsg(this, "Attempting to create transfer exception record for ER 293: "
                    + "[TransferException=" + transfer + "]", null).log(ctx);
            
            // validate transaction amount
            validateAmount(ctx, transfer);
            
            // create transfer exception
            Home transferExceptionHome = (Home) ctx.get(TransferExceptionHome.class);           
            transferExceptionHome.create(transfer);

            new OMLogMsg(Common.OM_MODULE, Common.OM_TRANSFER_EXCEPTION_CREATED, 1).log(ctx);
            new DebugLogMsg(this, "Successfully created TransferException", null).log(ctx);

        }
        catch (final Throwable t)
        {
            String msg = "Failed to create TransferException for subscriber mobile number , \""
                + msisdn
                + "\". " + t.getMessage(); 
            
            new MinorLogMsg(this, msg, t).log(ctx);
            
            final IllegalStateException newException = new IllegalStateException(msg);
                               
            newException.initCause(t);
            throw newException;
        }        

        new DebugLogMsg(this, "createTransferException(): End", null).log(ctx);
    }

    /**
     * Validates the amount based on the transaction type
     * @param ctx
     * @param transfer
     * @throws HomeException
     */
    private void validateAmount(Context ctx, TransferException transfer) throws HomeException
    {
        if (transfer.getType() == TransferFailureTypeEnum.CREDIT)
        {
            if (transfer.getAmount() > 0)
            {
                throw new HomeException("For credit transactions, the amount should be negative");
            }
        }
        else if (transfer.getType() == TransferFailureTypeEnum.DEBIT)
        {
            if (transfer.getAmount() < 0)
            {
                throw new HomeException("For debit transactions, the amount should be positive");
            }
        }
    }

    /**
     * Validates whether the msisdn exists in the system
     * @param ctx
     * @param spid
     * @param number
     * @throws HomeException
     */
    private void validateMsisdn(Context ctx, int spid, String number) throws HomeException
    {
        Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, spid, number);
        if (msisdn == null)
        {
            throw new IllegalArgumentException("Msisdn " + number + " does not exist in spid " + spid);
        }
    }
    
}
