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
package com.trilogy.app.crm.writeoff;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * 
 * 
 * @author alpesh.champeneri@redknee.com
 */
@SuppressWarnings("serial")
public class PostWrittenOffSubPaymentTransactionHome extends HomeProxy
{

    /**
     * @param delegate
     */
    public PostWrittenOffSubPaymentTransactionHome(Home delegate)
    {
        super(delegate);
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework
     * .xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Object ret = super.create(ctx, obj);
        Transaction txn = (Transaction) obj;
        if (!AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, txn.getAdjustmentType(),
                AdjustmentTypeEnum.StandardPayments))
        {
            return ret;
        }
        Subscriber sub = SubscriberSupport.getSubscriber(ctx, txn.getSubscriberID());
        boolean bWrittenOff = sub.getWrittenOff();
        if (!bWrittenOff)
        {
            return ret;
        }
        long origPayment = txn.getAmount();
        long writtenOffTotal = 0;
        final Account acct = AccountSupport.getAccount(ctx, txn.getAcctNum());
        // final Account acct = txn.getAccount(ctx);
        if (acct != null && (acct.getGroupType() != GroupTypeEnum.SUBSCRIBER))
        {
            writtenOffTotal = WriteOffSupport.getTotalWriteOffAmountForAccount(ctx, acct);
        }
        else
        {
            writtenOffTotal = WriteOffSupport.getTotalWriteOffAmountForSub(ctx, sub);
        }
        if (writtenOffTotal >= 0)
        {
            return ret; // do not apply reversal to sub with credit
        }
        long payment = // writtenOffTotal > 0 ? writtenOffTotal : (writtenOffTotal * -1);
        origPayment < writtenOffTotal ? writtenOffTotal : origPayment;
        Transaction writeOffReversal = cloneTxn(ctx, txn);
        if (writeOffReversal != null)
        {
            writeOffReversal.setReceiptNum(Transaction.DEFAULT_RECEIPTNUM);
            writeOffReversal.setPayee(PayeeEnum.Subscriber);
            writeOffReversal.setAdjustmentType(AdjustmentTypeEnum.WriteOffReversal_INDEX);
            writeOffReversal.setAmount(-payment);
            writeOffReversal.setTaxPaid(0l);
            Home home = (Home) ctx.get(TransactionHome.class);
            Context subCtx = ctx.createSubContext();
            subCtx.put(Subscriber.class, sub);
            LogSupport.info(ctx, this, "About to generate write off reveral transaction for sub: " + sub.getId() + " and Amount :"+ -payment);
            try
            {
                home.create(subCtx, writeOffReversal);
                WriteOffSupport.generatePostWriteOffPaymentER(
                        ctx,
                        acct,
                        origPayment,
                        payment,
                        (txn.getExtTransactionId() != null && txn.getExtTransactionId() != "") ? Long.parseLong(txn
                                .getExtTransactionId()) : 0);
            }
            catch (Exception e)
            {
                LogSupport.minor(subCtx, this, "Failed to generate write off reversal transaction.", e);
            }
            String parentBAN = acct.getParentBAN();
            if (acct != null && (acct.getGroupType() != GroupTypeEnum.SUBSCRIBER))
            {
                
                writtenOffTotal = WriteOffSupport.getTotalWriteOffAmountForAccount(ctx, acct);
            }
            else
            {
                writtenOffTotal = WriteOffSupport.getTotalWriteOffAmountForSub(ctx, sub);
            }
            if (writtenOffTotal >= 0)
            {
                SubscriberWriteOffProcessor.setWriteOffFlag(ctx, sub, false);
                AccountWriteOffProcessor.setWriteOffFlag(ctx, acct, false);
                
                if(parentBAN != null && parentBAN.length() > 0)
                {
                    final Account acc = AccountSupport.getAccount(ctx, parentBAN);
                    AccountWriteOffProcessor.setWriteOffFlag(ctx, acc, false);
                }
            }
        }
        return ret;
    }

    private Transaction cloneTxn(Context ctx, Transaction txn)
    {
        Transaction writeOffReversal = null;
        try
        {
            writeOffReversal = (Transaction) txn.clone();
        }
        catch (CloneNotSupportedException e)
        {
            LogSupport
                    .minor(ctx, this, "Failed to clone the transaction when creating write-off reversal transaction.");
        }
        return writeOffReversal;
    }
}