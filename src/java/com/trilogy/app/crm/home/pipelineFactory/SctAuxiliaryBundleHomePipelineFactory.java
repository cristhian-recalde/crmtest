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

import com.trilogy.app.crm.bean.SctAuxiliaryBundle;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;

/**
 * Creates the pipeline for SctAuxiliaryBundleHome.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class SctAuxiliaryBundleHomePipelineFactory implements PipelineFactory
{
    private static PipelineFactory instance_;
    public static PipelineFactory instance()
    {
        if (instance_ == null)
        {
            instance_ = new SctAuxiliaryBundleHomePipelineFactory();
        }
        return instance_;
    }
    protected SctAuxiliaryBundleHomePipelineFactory()
    {
    }

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws RemoteException, HomeException,
        IOException, AgentException
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, SctAuxiliaryBundle.class, "SCTAUXILIARYBUNDLE");
        
        return home;
    }
}
