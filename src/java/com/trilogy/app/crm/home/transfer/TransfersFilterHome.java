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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.TransferSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class TransfersFilterHome
    extends HomeProxy
{
    public TransfersFilterHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    public Collection select(Context ctx, Object where)
        throws HomeException, HomeInternalException
    {
        // the GUI borders should have already filtered out the transactions
        // account or subscription ID already.
        Set<String> extTranNumSet = new HashSet<String>();
        
        Collection<Transaction> transactions = HomeSupportHelper.get(ctx).getBeans(ctx, Transaction.class, 
                new In(TransactionXInfo.ADJUSTMENT_TYPE, TransferSupport.getMMAdjustmentTypes(ctx)));
        
        for (Transaction transaction : transactions)
        {
            extTranNumSet.add(transaction.getExtTransactionId());
        }

        In in = new In(TransactionXInfo.EXT_TRANSACTION_ID, extTranNumSet);
        return getDelegate().select(ctx, in);
    }
}