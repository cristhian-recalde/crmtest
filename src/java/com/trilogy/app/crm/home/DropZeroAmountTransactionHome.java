/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.support.SubscriberSupport;

public class DropZeroAmountTransactionHome extends HomeProxy
{
    public DropZeroAmountTransactionHome(Home delegate)
    {
        super(delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        SysFeatureCfg sysCfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        Transaction transaction = (Transaction) obj;

        if (sysCfg.getDropZeroAmountTransaction()
                && transaction.getAmount() == 0L
                && transaction.getExpiryDaysExt() == 0)
        {
            PayeeEnum payee = transaction.getPayee();
            Subscriber subs = (Subscriber) ctx.get(Subscriber.class);
            if (subs==null || subs.getId()==null || !subs.getId().equals(transaction.getSubscriberID()))
            {
                subs =  SubscriberSupport.lookupSubscriberForMSISDN(ctx,transaction.getMSISDN(), transaction.getTransDate());
            }
            if (subs == null || (payee == PayeeEnum.Subscriber && subs.getSubscriberType() == SubscriberTypeEnum.PREPAID))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Dropping transaction (receiptNum=" + transaction.getReceiptNum()
                            + ") due to zero amount", null).log(ctx);
                }

                return obj;
            }
        }

        return super.create(ctx, obj);
    }
}