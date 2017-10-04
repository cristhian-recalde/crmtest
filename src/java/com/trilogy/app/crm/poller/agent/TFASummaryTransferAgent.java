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
package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.TfaRmiConfig;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.TFAFailedTransferProcessor;
import com.trilogy.app.crm.poller.event.TFASummaryProcessor;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.transfer.TransferPartyEnum;
import com.trilogy.app.crm.transfer.Transfers;
import com.trilogy.app.crm.transfer.TransfersHome;


/**
 * Receives an ER 290 and creates a Transfer Entity for reporting
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class TFASummaryTransferAgent implements ContextAgent
{

    private CRMProcessor processor_;
    private static final String PM_MODULE = TFASummaryTransferAgent.class.getName();
    //private static final int TFA_SUMMARY_ER_INDEX_SPID = 2;
    private static final int TFA_SUMMARY_ER_NUMBER_OF_FIELDS = 54;
    private static final int TFA_SUMMARY_ER_INDEX_EXTRTRANSNUM = 3;
    private static final int TFA_SUMMARY_ER_INDEX_CONTR_ID = 4;
    // private static final int TFA_SUMMARY_ER_INDEX_CONTR_CHARGING_TYPE = 5;
    private static final int TFA_SUMMARY_ER_INDEX_CONTR_GROUPID = 6;
    private static final int TFA_SUMMARY_ER_INDEX_CONTR_SUBSCRIPTION = 8;
    private static final int TFA_SUMMARY_ER_INDEX_RECP_ID = 20;
    // private static final int TFA_SUMMARY_ER_INDEX_RECP_CHARGING_TYPE = 21;
    private static final int TFA_SUMMARY_ER_INDEX_RECP_GROUPID = 22;
    private static final int TFA_SUMMARY_ER_INDEX_RECP_SUBSCRIPTION = 24;
    private static final int TFA_SUMMARY_ER_INDEX_RESULT = 32;
    private static final int TFA_SUMMARY_ER_INDEX_CONTRACT_ID = 40; // fix required
    private static final int TFA_SUMMARY_ER_INDEX_AGREEMENT_ID = 42;
    private static final int TFA_SUMMARY_ER_INDEX_INTERFACE_CALLED = 43;
    private static final int TFA_SUMMARY_ER_INDEX_TRANS_DATE = 47;
    private static final int TFA_SUMMARY_ER_INDEX_TRANS_TYPE = 48;
    private static final String TFA_SUMMARY_ER_VALUE_INTERFACE_TRANSFERFUND = "TransferFund".intern();


    public TFASummaryTransferAgent(CRMProcessor processor)
    {
        super();
        processor_ = processor;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.
     * xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");
        try
        {
            // checking for ER ID is unnecessary;
            // it gets checked before the record is handed over to handler/processor
            // checking it just for better decoupling.
            if (TFASummaryProcessor.TFA_SUMMARY_ER_IDENTIFIER == Integer.parseInt(info.getErid()))
            {
                final List<String> params = new ArrayList<String>();
                try
                {
                    CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info
                            .getErid(), this);
                }
                catch (FilterOutException e)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "ER Filtered out: " + info.getErid(), null).log(ctx);
                    }
                    return;
                }
                final String interfaceCalled = CRMProcessorSupport.getField(params,
                        TFA_SUMMARY_ER_INDEX_INTERFACE_CALLED);
                final String extTransNumStr = CRMProcessorSupport.getField(params, TFA_SUMMARY_ER_INDEX_EXTRTRANSNUM);
                if (interfaceCalled != null && !"".equals(interfaceCalled)
                        && TFA_SUMMARY_ER_VALUE_INTERFACE_TRANSFERFUND.equals(interfaceCalled))
                {
                    if (extTransNumStr.length() > 0 && !extTransNumStr.equals(""))
                    {
                        switch (Integer.parseInt(CRMProcessorSupport
                                .getField(params, TFA_SUMMARY_ER_INDEX_RESULT)))
                        {
                        // create transfer for only successful results
                        case 0:
                            createTransfer(ctx, extTransNumStr, new Date(info.getDate()), params);
                            break;
                        default: {
                            if (LogSupport.isDebugEnabled(ctx))
                            {
                                new DebugLogMsg(this, "Skipping Polled ER " + info.getErid() + " for failed transfer: "
                                        + extTransNumStr, null).log(ctx);
                            }
                        }
                        }
                    }
                    else
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, "Skipping the ER " + info.getErid()
                                    + ". Missing External transaction Num: " + extTransNumStr, null).log(ctx);
                        }
                        return;
                    }
                }
                else
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Skipping the ER " + info.getErid() + ". Not a transfer-event: "
                                + extTransNumStr, null).log(ctx);
                    }
                    return;
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Wrong ER with " + info.getErid() + ". Being fed to: "
                            + this.getClass().getName(), null).log(ctx);
                }
                return;
            }
        }
        catch (final Throwable t)
        {
            new MinorLogMsg(this, "Failed to process ER "
                    + TFAFailedTransferProcessor.TFA_FAILED_TRANSFER_ER_IDENTIFIER + " because of Exception "
                    + t.getMessage(), t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
    }


    /**
     * Creates transfers corresponding to TFA 290 ER - Transfer Fund (Summary ER)
     * 
     * @param ctx
     * @param date
     * @param params
     */
    private void createTransfer(Context ctx, String exttrTransId, Date date, List<String> params)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "createTransfer(): Start", null).log(ctx);
        }
        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Attempting to create a Transfer", null).log(ctx);
            }
            CRMProcessorSupport.validateErFields(params, TFA_SUMMARY_ER_NUMBER_OF_FIELDS);
            final String contrMsisdn = CRMProcessorSupport.getField(params, TFA_SUMMARY_ER_INDEX_CONTR_ID);
            final String contrGroupIDStr = CRMProcessorSupport.getField(params, TFA_SUMMARY_ER_INDEX_CONTR_GROUPID);
            final String contrSubscriptionTypeStr = CRMProcessorSupport.getField(params,
                    TFA_SUMMARY_ER_INDEX_CONTR_SUBSCRIPTION);
            final String recpMsisdn = CRMProcessorSupport.getField(params, TFA_SUMMARY_ER_INDEX_RECP_ID);
            final String recpGroupIDStr = CRMProcessorSupport.getField(params, TFA_SUMMARY_ER_INDEX_RECP_GROUPID);
            final String recpSubscriptionTypeStr = CRMProcessorSupport.getField(params,
                    TFA_SUMMARY_ER_INDEX_RECP_SUBSCRIPTION);
            final String contractIDStr = CRMProcessorSupport.getField(params, TFA_SUMMARY_ER_INDEX_CONTRACT_ID);
            final String agreementIDStr = CRMProcessorSupport.getField(params, TFA_SUMMARY_ER_INDEX_AGREEMENT_ID);
            //final String spidStr = CRMProcessorSupport.getField(params, TFA_SUMMARY_ER_INDEX_SPID);
            // final String interfaceCalled = CRMProcessorSupport.getField(params,
            // TFA_SUMMARY_ER_INDEX_INTERFACE_CALLED);
            final String transferTypeStr = CRMProcessorSupport.getField(params, TFA_SUMMARY_ER_INDEX_TRANS_TYPE);
            final String transferDateStr = CRMProcessorSupport.getField(params, TFA_SUMMARY_ER_INDEX_TRANS_DATE);
            Transfers transfer = new Transfers();
            // retrieve spid
            //transfer.setSpid(Integer.parseInt(spidStr));
            transfer.setExtTransactionId(exttrTransId);
            transfer.setContributorPartyType(getPartyType(ctx, contrMsisdn, Long.parseLong(contrSubscriptionTypeStr),
                    date));
            transfer.setContributorGroupID(Long.parseLong(contrGroupIDStr));
            transfer
                    .setRecipientPartyType(getPartyType(ctx, recpMsisdn, Long.parseLong(recpSubscriptionTypeStr), date));
            transfer.setRecipientGroupID(Long.parseLong(recpGroupIDStr));
            transfer.setAgreementID(Long.parseLong(agreementIDStr));
            transfer.setContractID(Long.parseLong(contractIDStr));
            if(transferDateStr.length()>0 && !"".equals(transferDateStr))
            {
                transfer.setTransferDate(CRMProcessorSupport.getDate(transferDateStr));
            } else
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this,"Transaction date missing; using er timesatmp. Transaction: " + transfer.getExtTransactionId(),null).log(ctx);
                }
                transfer.setTransferDate(date);
            }
            
            // logging transaction date
            transfer.setTransferType(Integer.parseInt(transferTypeStr));// needs to be changed when ER 290 starts logging
            // transfer type
            // retrieve recipient msisdn
            // retrieve the date and time the transaction occurred
            /*
             * final String transDateStr = CRMProcessorSupport.getField(params,
             * TFA_BILLING_ER_INDEX_TRANSDATE); final Date transDate; if
             * (transDateStr.length() == 0 || transDateStr.equals("")) { transDate = null;
             * } else { try { transDate = CRMProcessor.getDate(transDateStr);; } catch
             * (ParseException e) { final String formattedMsg =
             * MessageFormat.format("Could not parse transDate String \"{0}\".", new
             * Object[] {transDateStr}); throw new HomeException(formattedMsg, e); } }
             * transfer.setTransDate(transDate);
             */
            Home transfersHome = (Home) ctx.get(TransfersHome.class);
            transfersHome.create(transfer);
            new OMLogMsg(Common.OM_MODULE, Common.OM_TRANSFER_CREATED, 1).log(ctx);
            new DebugLogMsg(this, "Successfully created Transfer", null).log(ctx);
        }
        catch (final Throwable t)
        {
            String msg = "Failed to create Transfer for subscriber mobile number , \"" + exttrTransId + "\". "
                    + t.getMessage();
            new MinorLogMsg(this, msg, t).log(ctx);
            final IllegalStateException newException = new IllegalStateException(msg);
            newException.initCause(t);
            throw newException;
        }
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "createTransfer(): End", null).log(ctx);
        }
    }


    /**
     * The method returns the Party-Type for a participant in a transfer.
     * 
     * @param ctx
     * @param spid
     * @param number
     * @param subscriptionType
     * @param date
     * @return
     * @throws HomeException
     */
    private int getPartyType(Context ctx, final String number, final long subscriptionTypeID, final Date date)
            throws HomeException
    {
        if (subscriptionTypeID == ((TfaRmiConfig) (ctx.get(TfaRmiConfig.class))).getOperatorSubscriptionTypeID())
        {
            return TransferPartyEnum.OPERATOR_INDEX;
        }
        // Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, spid, number);
        // if (msisdn == null)
        // {
        // return TransferPartyEnum.EXTERNAL_INDEX;
        // }
        // else
        // {
        final String subID = SubscriberSupport.lookupSubscriberIdForMSISDN(ctx, number, subscriptionTypeID, date);
        if (subID == null)
        {
            return TransferPartyEnum.EXTERNAL_INDEX;
        }
        else
        {
            return TransferPartyEnum.INTERNAL_INDEX;
        }
        // }
    }
}
