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
package com.trilogy.app.crm.transfer;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.transfer.TransferDisputeEREventHome;
import com.trilogy.app.crm.home.transfer.TransferDisputeNotificationHome;
import com.trilogy.app.crm.home.transfer.TransferDisputeTransactionHome;
import com.trilogy.app.crm.support.StorageSupportHelper;


public class TransferDisputeHomePipelineFactory implements PipelineFactory
{

    public TransferDisputeHomePipelineFactory()
    {
        super();
    }
    
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        Home transferDisputeHome = StorageSupportHelper.get(ctx).createHome(ctx, TransferDispute.class, "TRANSFERDISPUTE");
        transferDisputeHome = new TransferDisputeEREventHome(ctx, transferDisputeHome);
        transferDisputeHome = new TransferDisputeNotificationHome(ctx, transferDisputeHome);
        transferDisputeHome = new TransferDisputeTransactionHome(ctx, transferDisputeHome);
        ctx.put(TransferDisputeHome.class, transferDisputeHome); 
        
        return transferDisputeHome;
    } 
}
