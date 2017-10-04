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
package com.trilogy.app.crm.transfer;

import java.util.Date;

import com.trilogy.app.crm.bean.AbstractTransaction;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.support.TransferSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.product.s2100.ErrorCode;


public class TransferDisputeTransactionSupport
{

    /**
     * 
     * @param ctx
     * @param dispute
     * @return - OCG result code
     * @throws - HomeException
     */
    public static Transaction blockRecipientBalance(Context ctx, final TransferDispute dispute) throws HomeException,
            OcgTransactionException
    {
        Subscriber sub = null;
        try
        {
            if(TransferSupport.OPERATOR_ID.equals(dispute.getRecpSubId()))
            {
                return null;
            }
            sub = SubscriberSupport.getSubscriber(ctx, dispute.getRecpSubId());
        }
        catch (HomeException e)
        {
            new MinorLogMsg(TransferDisputeTransactionSupport.class.getName(),
                    "Recipient Balance could not be blocked because the Subscriber could not be found: "
                            + dispute.getDisputeId(), e).log(ctx);
            throw e;
        }
        if (sub != null)
        {
            try
            {
                return TransactionSupport.createTransaction(ctx, sub, dispute.getBlockedBalance(), 0, AdjustmentTypeSupportHelper.get(ctx)
                        .getAdjustmentType(ctx, AdjustmentTypeEnum.DisputeRecipientBlockDebit), false, false, "",
                        new Date(), new Date(), "", 0, dispute.getExtTransactionId(),
                        AbstractTransaction.DEFAULT_TRANSACTIONMETHOD);
                
            }
            catch (OcgTransactionException oe)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(oe, "blockRecipientBalance() - OCG Failed the recipient debit with result code: "
                            + oe.getErrorCode() + " for dispute :" + dispute.getDisputeId(), oe).log(ctx);
                }
                throw oe;
            }
            catch (HomeException e)
            {
                new MinorLogMsg(TransferDisputeTransactionSupport.class.getName(),
                        "Recipient Balance could not be blocked because the Subscriber could not be found: "
                                + dispute.getDisputeId(), e).log(ctx);
                throw e;
            }
            catch (Throwable t)
            {
                new MinorLogMsg(TransferDisputeTransactionSupport.class.getName(), "Recipient Balance could not be blocked because of internal error "
                        + dispute.getDisputeId(), t).log(ctx);
                throw new HomeException("Internal Error", t);
            }
        }
        else
        {
            throw new HomeException("Subscriber not found: " + dispute.getRecpSubId());
        }
    }


    /**
     * 
     * @param ctx
     * @param dispute
     * @return - the newly created transaction
     * @throws - HomeException
     */
    public static Transaction refundContributor(Context ctx, final TransferDispute dispute) throws HomeException,
            OcgTransactionException
    {
        Subscriber sub = null;
        try
        {
            if(TransferSupport.OPERATOR_ID.equals(dispute.getContSubId()))
            {
                LogSupport.info(ctx, TransferDisputeTransactionSupport.class.getName(), "\"Refunding\" to Operator.");
                return null;
            }
            sub = SubscriberSupport.getSubscriber(ctx, dispute.getContSubId());
        }
        catch (HomeException e)
        {
            new MinorLogMsg(TransferDisputeTransactionSupport.class.getName(), "Contributer could not be credited because the Subscriber could not be found: "
                    + dispute.getDisputeId(), e).log(ctx);
            throw e;
        }
        if (sub != null)
        {
            try
            {
                return TransactionSupport.createTransaction(ctx, sub, -dispute.getRefundAmount(), 0, AdjustmentTypeSupportHelper.get(ctx)
                        .getAdjustmentType(ctx, AdjustmentTypeEnum.DisputeContributerRefundCredit), false, false, "",
                        new Date(), new Date(), "", 0, dispute.getExtTransactionId(),
                        AbstractTransaction.DEFAULT_TRANSACTIONMETHOD);
            }
            catch (OcgTransactionException oe)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(TransferDisputeTransactionSupport.class.getName(), "refundContributor() - OCG Failed the contributor credit with result code: "
                            + oe.getErrorCode() + " for dispute :" + dispute.getDisputeId(), oe).log(ctx);
                }
                throw oe;
            }
            catch (HomeException e)
            {
                new MinorLogMsg(TransferDisputeTransactionSupport.class.getName(),
                        "Contributer could not be credited because the Subscriber could not be found: "
                                + dispute.getDisputeId(), null).log(ctx);
                throw e;
            }
            catch (Throwable t)
            {
                new MinorLogMsg(TransferDisputeTransactionSupport.class.getName(), "Contributer could not be credited because of internal error "
                        + dispute.getDisputeId(), t).log(ctx);
                throw new HomeException("Internal Error", t);
            }
        }
        else
        {
            throw new HomeException("Subscriber not found: " + dispute.getContSubId());
        }
    }


    /**
     * 
     * @param ctx
     * @param dispute
     * @return - OCG result code
     * @throws - HomeException
     */
    public static Transaction unblockRecipientBalance(Context ctx, final TransferDispute dispute) throws HomeException,
            OcgTransactionException
    {
        Subscriber sub = null;
        try
        {
            if(TransferSupport.OPERATOR_ID.equals(dispute.getRecpSubId()))
            {
                return null;
            }
            sub = SubscriberSupport.getSubscriber(ctx, dispute.getRecpSubId());
        }
        catch (HomeException e)
        {
            new MinorLogMsg(TransferDisputeTransactionSupport.class.getName(),
                    "Recipient Balance could not be un-blocked because the Subscriber could not be found: "
                            + dispute.getDisputeId(), null).log(ctx);
            throw e;
        }
        if (sub != null)
        {
            try
            {
                return TransactionSupport.createTransaction(ctx, sub, -dispute.getBlockedBalance(), 0, AdjustmentTypeSupportHelper.get(ctx)
                        .getAdjustmentType(ctx, AdjustmentTypeEnum.DisputeUnblockRecipientCredit), false, false, "",
                        new Date(), new Date(), "", 0, dispute.getExtTransactionId(),
                        AbstractTransaction.DEFAULT_TRANSACTIONMETHOD);
            }
            catch (OcgTransactionException oe)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(TransferDisputeTransactionSupport.class.getName(), "unblockRecipientBalance() - OCG Failed the recipient debit with result code: "
                            + oe.getErrorCode() + " for dispute :" + dispute.getDisputeId(), oe).log(ctx);
                }
                throw oe;
            }
            catch (HomeException e)
            {
                new MinorLogMsg(TransferDisputeTransactionSupport.class.getName(),
                        "Recipient Balance could not be un-blocked because the Subscriber could not be found: "
                                + dispute.getDisputeId(), null).log(ctx);
                throw e;
            }
            catch (Throwable t)
            {
                new MinorLogMsg(TransferDisputeTransactionSupport.class.getName(), "Recipient Balance could not be un-blocked because of internal error "
                        + dispute.getDisputeId(), t).log(ctx);
                throw new HomeException("Internal Error", t);
            }
        }
        else
        {
            throw new HomeException("Subscriber not found: " + dispute.getRecpSubId());
        }
    }


    /**
     * 
     * Apply the dispute fee according to TransferDispute::applyDisputeFeeTo
     * 
     * @param ctx
     * @param dispute
     * @return - OCG result code
     * @throws - HomeException
     */
    public static Transaction applyDisputeFee(Context ctx, final TransferDispute dispute) throws HomeException,
            OcgTransactionException
    {
        if (dispute.isDisputFeeApplied())
        {
            throw new HomeException("Dispute fee already applied");
        }
        Subscriber sub = null;
        try
        {
            if (DisputeInitiatorEnum.CONTRIBUTOR_INDEX == dispute.getApplyDisputeFeeTo().getIndex())
            {
                if(TransferSupport.OPERATOR_ID.equals(dispute.getContSubId()))
                {
                    throw new HomeException("Cannot apply dispute fee to operator.");
                }
                sub = SubscriberSupport.getSubscriber(ctx, dispute.getContSubId());
            }
            else
            {
                if(TransferSupport.OPERATOR_ID.equals(dispute.getRecpSubId()))
                {
                    throw new HomeException("Cannot apply dispute fee to operator.");
                }
                sub = SubscriberSupport.getSubscriber(ctx, dispute.getRecpSubId());
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(dispute, "Dispute fee could not be applied because the Subscriber could not be found: "
                    + dispute.getDisputeId(), e).log(ctx);
            throw e;
        }
        if (sub != null)
        {
            try
            {
                long disputeFee;
                CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
                if (spid.getDisputeFeeTypePercent())
                {
                    disputeFee = (long) (dispute.getBlockedBalance() * spid.getDisputeFeePercent());
                }
                else
                {
                    disputeFee = spid.getDisputeFeeAmount();
                }
                
                return TransactionSupport.createTransaction(ctx, sub, disputeFee, 0,
                        AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.ApplyDisputeFee), false, false,
                        "", new Date(), new Date(), "", 0, dispute.getExtTransactionId(),
                        AbstractTransaction.DEFAULT_TRANSACTIONMETHOD);
            }
            catch (OcgTransactionException oe)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(oe, "applyDisputeFee() - OCG Failed the recipient debit with result code: "
                            + oe.getErrorCode() + " for dispute :" + dispute.getDisputeId(), oe).log(ctx);
                }
                throw oe;
            }
            catch (HomeException e)
            {
                new MinorLogMsg(dispute, "Dispute fee could not be applied because the Subscriber could not be found: "
                        + dispute.getDisputeId(), e).log(ctx);
                throw e;
            }
            catch (Throwable t)
            {
                new MinorLogMsg(dispute, "Dispute fee could not be applied because of internal error "
                        + dispute.getDisputeId(), t).log(ctx);
                throw new HomeException("Internal Error", t);
            }
        }
        else
        {
            throw new HomeException("Subscriber not found: " + dispute.getRecpSubId());
        }
    }
    

    /**
     * 
     * Apply the dispute fee according to TransferDispute::isSubscriberInDispute
     * 
     * @param ctx
     * @param subscriber-id
     * @return - true if disputes exist
     * @throws - HomeException
     */
    public static boolean isSubscriberInDispute(Context ctx, String subsriberID) throws HomeException
    {
        // this statement checks if there are any disputes (open but not rejected or
        // accepted) for the subscription
        if (HomeSupportHelper.get(ctx).hasBeans(ctx, TransferDispute.class, new And().add(
                new Or().add(new EQ(TransferDisputeXInfo.RECP_SUB_ID, subsriberID)).add(
                        new EQ(TransferDisputeXInfo.CONT_SUB_ID, subsriberID))).add(
                new NEQ(TransferDisputeXInfo.STATE, TransferDisputeStatusEnum.ACCEPTED)).add(
                new NEQ(TransferDisputeXInfo.STATE, TransferDisputeStatusEnum.REJECTED))))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}