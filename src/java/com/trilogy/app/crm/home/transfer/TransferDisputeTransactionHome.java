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

package com.trilogy.app.crm.home.transfer;

import java.util.ArrayList;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.app.crm.transfer.TransferDisputeTransactionSupport;
import com.trilogy.app.crm.transfer.TransferDisputeStatusEnum;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class TransferDisputeTransactionHome
    extends HomeProxy
{
    public TransferDisputeTransactionHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    public Object create(Context ctx, Object bean)
        throws HomeException, HomeInternalException
    {
        TransferDispute dispute = (TransferDispute)bean;
        try
        {
            Transaction t = TransferDisputeTransactionSupport.blockRecipientBalance(ctx, dispute);
            if(null != t)
            {
                LongHolder l = new LongHolder();
                l.setValue(t.getReceiptNum());
                ArrayList a = new ArrayList();
                dispute.setAssociatedTransactions(a);
                dispute.getAssociatedTransactions().add(l);
            }
            dispute.setBlockBalanceFailed(false);
        }
        catch(OcgTransactionException e)
        {
            dispute.setBlockBalanceFailed(true);
            dispute.setBlockBalanceFailedReason(e.getErrorCode());
            dispute.setBlockBalanceFailedReasonDesc(e.getMessage());
        }

        //ERLogger.generateTransferDisputEr(ctx, dispute);

        return super.create(ctx, dispute);
    }

    public Object cmd(Context ctx, Object arg)
        throws HomeException,  HomeInternalException
    {
        if(arg instanceof TransferDisputeCmd)
        {
            TransferDisputeCmd cmd = (TransferDisputeCmd)arg;
            long transferDisputeId = cmd.transferDisputeId_;
            TransferDispute dispute = (TransferDispute)find(ctx, transferDisputeId);

            if(null == dispute)
            {
                LogSupport.major(ctx, this, "Unable to find transfer dispute with ID[" + transferDisputeId + "]");
                throw new HomeException("Unable to find transfer dispute with ID[" + transferDisputeId + "]");
            }

            if(TransferDisputeCmd.ACCEPT_CMD.equals(cmd.cmd_))
            {
                acceptTransferDispute(ctx, dispute);
            }
            else if(TransferDisputeCmd.CANCEL_CMD.equals(cmd.cmd_))
            {
                cancelTransferDispute(ctx, dispute);
            }
            else
            {
                LogSupport.major(ctx, this, "Unrecognized Transfer Dispute Cmd [" + cmd.cmd_ + "]");
                throw new HomeException("Unrecognized Transfer Dispute Cmd [" + cmd.cmd_ + "]");
            }

            return null;
        }
        else
        {
            return super.cmd(ctx, arg);
        }
    }

    private void acceptTransferDispute(Context ctx, TransferDispute dispute)
        throws HomeException
    {
        // validate

        // check if the balance for the recipient was blocked
        if(dispute.isBlockBalanceFailed())
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Blocking balance [" + dispute.getBlockedBalance() + "] from receipient [" + dispute.getRecpSubId() + "]");
            }

            try
            {
                Transaction t = TransferDisputeTransactionSupport.blockRecipientBalance(ctx, dispute);
                if(null != t)
                {
                    LongHolder l = new LongHolder();
                    l.setValue(t.getReceiptNum());
                    dispute.getAssociatedTransactions().add(l);
                }
            }
            catch(Exception e)
            {
                throw new HomeException("Unable to block balance for recipient.", e);
            }
        }

        try
        {
            Transaction t = TransferDisputeTransactionSupport.refundContributor(ctx, dispute);
            if(null != t)
            {
                LongHolder l = new LongHolder();
                l.setValue(t.getReceiptNum());
                dispute.getAssociatedTransactions().add(l);
            }
        }
        catch(Exception e)
        {
            // need to create a failed transaction
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Unable to refund [" + dispute.getRefundAmount() + "] to contributor [" + dispute.getContSubId() + "]. Creating failed transaction.");
            }
        }

        dispute.setState(TransferDisputeStatusEnum.ACCEPTED);
        //ERLogger.generateTransferDisputEr(ctx, dispute);

        store(ctx, dispute);
    }

    private void cancelTransferDispute(Context ctx, TransferDispute dispute)
        throws HomeException
    {
        // validate

        // check if the balance for the recipient was blocked
        if(!dispute.isBlockBalanceFailed())
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Unblocking balance [" + dispute.getBlockedBalance() + "] for receipient [" + dispute.getRecpSubId() + "]");
            }
            try
            {
                Transaction t = TransferDisputeTransactionSupport.unblockRecipientBalance(ctx, dispute);
                if(null != t)
                {
                    LongHolder l = new LongHolder();
                    l.setValue(t.getReceiptNum());
                    dispute.getAssociatedTransactions().add(l);
                }
            }
            catch(Exception e)
            {
                throw new HomeException("Unable to unblock balance for recipient.", e);
            }
        }

        dispute.setState(TransferDisputeStatusEnum.REJECTED);
        store(ctx, dispute);
    }
}