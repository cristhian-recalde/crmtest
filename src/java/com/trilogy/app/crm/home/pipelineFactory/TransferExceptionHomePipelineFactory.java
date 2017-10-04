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
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.transfer.TransferException;
import com.trilogy.app.crm.transfer.TransferExceptionHome;

/**
 * Pipeline factory for Transfer Exception.
 *
 */
public class TransferExceptionHomePipelineFactory implements PipelineFactory
{
    public TransferExceptionHomePipelineFactory() {}

    public Home createPipeline(Context ctx, Context serverCtx)
            throws RemoteException, HomeException, IOException, AgentException 
    {
        Home transferExceptionHome = StorageSupportHelper.get(ctx).createHome(ctx, TransferException.class, "TRANSFEREXCEPTION");
        transferExceptionHome = new IdentifierSettingHome(
                       ctx,
                       transferExceptionHome,
                       IdentifierEnum.TRANSFEREXCEPTION_ID, null);
        
        IdentifierSequenceSupportHelper.get(ctx).ensureNextIdIsLargeEnough(ctx, IdentifierEnum.TRANSFEREXCEPTION_ID, transferExceptionHome);
              
        transferExceptionHome = new SortingHome(transferExceptionHome);
        
        ctx.put(TransferExceptionHome.class, transferExceptionHome);
        
        return transferExceptionHome;
    }

}
