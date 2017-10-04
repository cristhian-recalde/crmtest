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
package com.trilogy.app.crm.home;

import java.util.Collection;

import com.trilogy.app.crm.bas.tps.TPSSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Verifies if this transaction is a payment, if so, it must reject it if
 * there are more than two subscribers in the MSISDN history and they have outstanding
 * amount.
 * @author arturo.medina@redknee.com
 *
 */
public class TransactionInMultipleSubscribersHome extends HomeProxy
{

    /**
     * Unique constructor that receives the delegate to continue the pipeline.
     * @param delegate the home to delegate the chain in the pipeline
     */
    public TransactionInMultipleSubscribersHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Transaction txn = (Transaction) obj;

        /*
         * If we already know the subscriber ID (GUI) let it go through, try to reject otherwise
         * Also we need to check if the payments is 0 no need to verify the history since
         * it's only synching the subscriber's amount
         */
        if (CoreTransactionSupportHelper.get(ctx).isPayment(ctx, txn)
                && (txn.getSubscriberID() == null || txn.getSubscriberID().length() <= 0)
                && (txn.getAmount() != 0))
        {
            LogSupport.debug(ctx, this, "Transaction is Payment, "
                    + "verifying if there are more subscribers with the same MSISDN ");

            TPSSupport.getSubscriber(ctx, txn.getBAN(), txn.getMSISDN(), txn.getTransDate());
        }
        return super.create(ctx, obj);
    }



    /**
     * The serial version UID
     */
    private static final long serialVersionUID = -661168881424419772L;

}
