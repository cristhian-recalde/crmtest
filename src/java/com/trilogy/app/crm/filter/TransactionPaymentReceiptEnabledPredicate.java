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

package com.trilogy.app.crm.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.util.snippet.log.Logger;


public class TransactionPaymentReceiptEnabledPredicate implements Predicate
{
    /**
     * For serialization.
     */
    private static final long serialVersionUID = 1L;
    private static TransactionPaymentReceiptEnabledPredicate instance_ = null;

    public static TransactionPaymentReceiptEnabledPredicate instance()
    {
        if( instance_ == null )
        {
            instance_ = new TransactionPaymentReceiptEnabledPredicate();
        }
        return instance_;
    }

    public boolean f(final Context ctx, final Object obj) throws AbortVisitException
    {
        if (obj == null)
        {
            // for some bizarre reason Framework sends a null into this Predicate.
            return false;
        }

        if (!(obj instanceof Transaction))
        {
            if (Logger.isInfoEnabled())
            {
                Logger.info(ctx, this, "Only Transaction is supported",
                        new RuntimeException("Only Transaction is supported"));
            }
            return false;
        }
        Transaction transaction = (Transaction) obj;
        return (CoreTransactionSupportHelper.get(ctx).isDeposit(ctx, transaction) || CoreTransactionSupportHelper.get(ctx).isPayment(ctx, transaction)) 
            && PayeeEnum.Subscriber.equals(transaction.getPayee());
    }
}
