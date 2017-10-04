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
package com.trilogy.app.crm.factory;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.msp.MSP;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Transaction;


/**
 * Creates default Transaction beans.
 *
 * @author gary.anderson@redknee.com
 */
public
class TransactionFactory
    extends com.redknee.app.crm.factory.core.TransactionFactory
{
    public TransactionFactory(ContextFactory delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx)
    {
        Transaction txn     = (Transaction) super.create(ctx);

        Account     account = (Account)    ctx.get(Account.class);
        Subscriber  sub     = (Subscriber) ctx.get(Subscriber.class);
        
        if ( account != null )
        {
            txn.setBAN(account.getBAN());
            txn.setSpid(account.getSpid());
            MSP.setBeanSpid(ctx, account.getSpid());        

        }
        
        if ( sub != null )
        {
            txn.setSubscriberID(sub.getId());
            if (sub.getSubscriptionType()!=-1)
            {
                txn.setSubscriptionTypeId(sub.getSubscriptionType());
            }
            txn.setMSISDN(sub.getMSISDN());
            txn.setBAN(sub.getBAN());
            txn.setSpid(sub.getSpid());
            MSP.setBeanSpid(ctx, sub.getSpid());    
        }
        
        return txn;
    }

} // class
