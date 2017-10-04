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
package com.trilogy.app.crm.writeoff;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Zero out tax paid in original transaction if it's a credit to written-off account
 *
 * @author ray.chen@redknee.com
 */
@SuppressWarnings("serial")
public class PaymentToWrittenOffAccountTransactionHome extends HomeProxy
{
    public PaymentToWrittenOffAccountTransactionHome(Home delegate)
    {
        super(delegate);
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Transaction tran = (Transaction) obj;
        
        if (tran.getAmount()<0)
        {
            Account account = AccountSupport.getAccount(ctx, tran.getBAN());
            if (account.getWrittenOff())
            {
                tran.setTaxPaid(0L);
            }
        }
        
        return super.create(ctx, obj);
    }
 
    
}