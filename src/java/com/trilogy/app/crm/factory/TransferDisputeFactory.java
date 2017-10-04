package com.trilogy.app.crm.factory;

import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.app.crm.transfer.TransfersView;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xlog.log.LogSupport;

public class TransferDisputeFactory
    implements ContextFactory
{
    public Object create(Context ctx)
    {
        TransferDispute dispute = new TransferDispute();
        TransfersView transfer = (TransfersView)ctx.get(TransfersView.class);
        if(null != transfer)
        {
            dispute.setContSubId(transfer.getContSubId());
            dispute.setRecpSubId(transfer.getRecpSubId());
            dispute.setExtTransactionId(transfer.getExtTransactionId());
            dispute.setBlockedBalance(transfer.getRecipAmt() + transfer.getRecpSurplus());
            dispute.setRefundAmount(transfer.getContAmt() + transfer.getContSurplus());
            dispute.setContSubAccount(transfer.getContSubAccount());
            dispute.setRecpSubAccount(transfer.getRecpSubAccount());
            dispute.setContAmount(transfer.getContAmt());
            dispute.setRecpAmount(transfer.getRecipAmt());
            dispute.setTransferDate(transfer.getTransferDate());
        }

        return dispute;
    }
}