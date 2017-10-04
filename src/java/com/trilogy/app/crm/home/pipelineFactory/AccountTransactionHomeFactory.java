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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.app.crm.xhome.home.TransactionIdentifierSettingHome;


/**
 * Create Account transaction home.
 *
 * @author victor.stratan@redknee.com
 * @since 8.6
 */
public class AccountTransactionHomeFactory implements PipelineFactory
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(final Context ctx, final Context serverCtx)
        throws RemoteException, HomeException, IOException, AgentException
    {
        Home home;
        home = StorageSupportHelper.get(ctx).createHome(ctx, Transaction.class, "ACCOUNTTRANSACTION", false);

        home = new AdapterHome(
                ctx, 
                home, 
                new ExtendedBeanAdapter<Transaction, com.redknee.app.crm.bean.core.Transaction>(
                        com.redknee.app.crm.bean.Transaction.class, 
                        com.redknee.app.crm.bean.core.Transaction.class));

        home = new TransactionIdentifierSettingHome(ctx, home, "AccountTransactionID_seq");

        ctx.put(Common.ACCOUNT_TRANSACTION_HOME, home);

        return home;
    }

}
