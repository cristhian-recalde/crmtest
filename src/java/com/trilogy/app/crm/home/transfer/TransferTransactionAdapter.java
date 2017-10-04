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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.support.TransferSupport;
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.app.crm.transfer.TransferDisputeHome;
import com.trilogy.app.crm.transfer.TransferDisputeStatusEnum;
import com.trilogy.app.crm.transfer.TransferDisputeXInfo;
import com.trilogy.app.crm.transfer.TransferException;
import com.trilogy.app.crm.transfer.TransferExceptionHome;
import com.trilogy.app.crm.transfer.TransferExceptionXInfo;
import com.trilogy.app.crm.transfer.TransferFailureStateEnum;
import com.trilogy.app.crm.transfer.TransfersView;

public class TransferTransactionAdapter
    implements Adapter
{
    public Object adapt(Context ctx, Object obj)
    {
        TransfersView t = new TransfersView();
        Collection transactions = (Collection)obj;

        if(null == transactions || transactions.isEmpty())
        {
            return null;
        }

        adaptTransactions(ctx, t, transactions);
        checkFailedTransfersView(ctx, t);
        checkDisputedTransfersView(ctx, t);

        return t;
    }

    private void checkDisputedTransfersView(final Context ctx, TransfersView transfer)
    {
        Home h = (Home)ctx.get(TransferDisputeHome.class);
        And where = new And();
        Or or = new Or();
        or.add(new EQ(TransferDisputeXInfo.STATE, TransferDisputeStatusEnum.INITIATED));
        or.add(new EQ(TransferDisputeXInfo.STATE, TransferDisputeStatusEnum.ASSIGNED));
        or.add(new EQ(TransferDisputeXInfo.STATE, TransferDisputeStatusEnum.ACCEPTED));
        where.add(new EQ(TransferDisputeXInfo.EXT_TRANSACTION_ID, transfer.getExtTransactionId()));
        where.add(or);
        try
        {
            Collection c = h.select(ctx, where);
            if(!c.isEmpty())
            {
                TransferDispute dispute = (TransferDispute)c.iterator().next();
                transfer.setCurrentDisputeId(dispute.getDisputeId());
            }
        }
        catch(Exception e)
        {
            LogSupport.minor(ctx, this, "Error looking up transfer disputes.", e);
        }
    }

    private void checkFailedTransfersView(final Context ctx, TransfersView transfer)
    {
        Home home = (Home)ctx.get(TransferExceptionHome.class);
        And where = new And();
        where.add(new EQ(TransferExceptionXInfo.EXT_TRANSACTION_ID, transfer.getExtTransactionId()));
        where.add(new EQ(TransferExceptionXInfo.STATE, TransferFailureStateEnum.FAILED));

        try
        {
            TransferException te = (TransferException)home.find(ctx, where);
            if(null != te)
            {
                transfer.setFailedTransferId(te.getId());
            }
        }
        catch(HomeException e)
        {
            LogSupport.minor(ctx, this, "Error trying to look up failed transfers.", e);
        }
    }

    private void adaptTransactions(final Context ctx, TransfersView transfers, Collection transactions)
    {
        Date transDate = null;
        String extTransactionNum = "";
        long contribAmt = 0;
        long contribSurplus = 0;
        long recipAmt = 0;
        long recipSurplus = 0;
        String contribBAN = "";
        String contribSubId = "";
        String contriMsisdn = "";
        String recipBAN = "";
        String recipSubId = "";
        String recipMsisdn = "";
        
        Iterator i = transactions.iterator();
        while(i.hasNext())
        {
            Transaction tran = (Transaction)i.next();
            boolean isUpdated = false;
            
            if(TransferSupport.isContributorAmountAdjustmentType(ctx, tran.getAdjustmentType()))
            {
                contribAmt = tran.getAmount();
                if("".equals(contribBAN))
                {
                    contribBAN = tran.getBAN();
                    contribSubId = tran.getSubscriberID();
                    contriMsisdn = tran.getMSISDN();
                }
                if("".equals(extTransactionNum))
                {
                    extTransactionNum = tran.getExtTransactionId();
                }
                if(null == transDate)
                {
                    transDate = tran.getTransDate();
                }
            }
            else if(TransferSupport.isContributorSurplusDiscountAdjustmentType(ctx, tran.getAdjustmentType()))
            {
                if("".equals(contribBAN))
                {
                    contribBAN = tran.getBAN();
                    contribSubId = tran.getSubscriberID();
                    contriMsisdn = tran.getMSISDN();
                }
                if("".equals(extTransactionNum))
                {
                    extTransactionNum = tran.getExtTransactionId();
                }
                if(null == transDate)
                {
                    transDate = tran.getTransDate();
                }
                contribSurplus += tran.getAmount();
            }
            else if(TransferSupport.isRecipientAmountAdjustmentType(ctx, tran.getAdjustmentType()))
            {
                if("".equals(recipBAN))
                {
                    recipBAN = tran.getBAN();
                    recipSubId = tran.getSubscriberID();
                    recipMsisdn = tran.getMSISDN();
                }
                if("".equals(extTransactionNum))
                {
                    extTransactionNum = tran.getExtTransactionId();
                }
                if(null == transDate)
                {
                    transDate = tran.getTransDate();
                }
                recipAmt = -tran.getAmount();
            }
            else if(TransferSupport.isRecipientSurplusDiscountAdjustmentType(ctx, tran.getAdjustmentType()))
            {
                if("".equals(recipBAN))
                {
                    recipBAN = tran.getBAN();
                    recipSubId = tran.getSubscriberID();
                    recipMsisdn = tran.getMSISDN();
                }
                if("".equals(extTransactionNum))
                {
                    extTransactionNum = tran.getExtTransactionId();
                }
                if(null == transDate)
                {
                    transDate = tran.getTransDate();
                }
                recipSurplus -= tran.getAmount();
            }
            else
            {
                LogSupport.info(ctx, this, "Unrecognized AdjustmentType [" + tran.getAdjustmentType() + "] for external transaction [" + tran.getExtTransactionId() + "]");
            }
            
        }

        // check for blank BANs/SubIds and treat them as operator transfers
        if("".equals(contribBAN))
        {
            if ("".equals(contribSubId))
            {
                contribBAN = TransferSupport.OPERATOR_ID;
                contribSubId = TransferSupport.OPERATOR_ID;
                contriMsisdn = TransferSupport.OPERATOR_ID;
                contribAmt = recipAmt;
                contribSurplus = recipSurplus;
            }
            else if (TransferSupport.EXTERNAL_ID.equals(contribSubId))
            {
                contribBAN = TransferSupport.EXTERNAL_ID;
            }
        }
        else if("".equals(recipBAN))
        {
            if ("".equals(recipSubId))
            {
                recipBAN = TransferSupport.OPERATOR_ID;
                recipSubId = TransferSupport.OPERATOR_ID;
                recipMsisdn = TransferSupport.OPERATOR_ID;
                recipAmt = contribAmt;
                recipSurplus = contribSurplus;
            }
            else if (TransferSupport.EXTERNAL_ID.equals(recipSubId))
            {
                recipBAN = TransferSupport.EXTERNAL_ID;
            }
        }

        transfers.setExtTransactionId(extTransactionNum);
        transfers.setContSubId(contribSubId);
        transfers.setRecpSubId(recipSubId);
        transfers.setContAmt(contribAmt);
        transfers.setRecipAmt(recipAmt);
        transfers.setContSurplus(contribSurplus);
        transfers.setRecpSurplus(recipSurplus);
        transfers.setTransferDate(transDate);
        transfers.setContSubAccount(contribBAN);
        transfers.setRecpSubAccount(recipBAN);
        transfers.setCMSISDN(contriMsisdn);
        transfers.setRMSISDN(recipMsisdn);
    }

    // we do not unadapt TransfersView to a transaction
    public Object unAdapt(Context ctx, Object obj)
    {
        return null;
    }
}