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

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionXDBHome;
import com.trilogy.app.crm.home.transaction.TransactionUnifiedReceiptSettingHome;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.xhome.home.TransactionIdentifierSettingHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Provides a class from which to create the pipeline of Home decorators that process a
 * Unnapplied Transaction travelling between the application and the given delegate.
 *
 */
public class UnappliedTransactionHomePipelineFactory implements PipelineFactory
{
    /**
     * Private constructor to discourage instantiation.
     */
    public UnappliedTransactionHomePipelineFactory()
    {
        // Empty
    }

    /**
     * Creates and installs pipelines for Transaction.
     *
     * @param context
     *            The application context.
     * @param serverContext
     *            The context used for remote services.
     * @return The Home representing the head of the pipeline.
     * @exception AgentException
     *                Thrown if there are any problems creating the pipeline.
     */
    public Home createPipeline(final Context context, final Context serverContext) throws AgentException
    {
        Home home = null;

        LogSupport.info(context, this, "Installing the Unapplied Transaction home ");
        home = StorageSupportHelper.get(context).createHome(context, Transaction.class, "UnappliedTransaction");
        home = new TransactionIdentifierSettingHome(context, home , IdentifierEnum.UNAPPLIED_TRANSACTION_ID.getDescription());
        home = new TransactionUnifiedReceiptSettingHome(home);
        LogSupport.info(context, this, "Unapplied Transaction Home installed successfully");
        return home;
    }
}
