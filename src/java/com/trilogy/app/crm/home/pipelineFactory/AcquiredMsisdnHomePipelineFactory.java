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

import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagementHome;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagementTransientHome;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagementXDBHome;
import com.trilogy.app.crm.home.AcquiredMsisdnSetAuthenticationHome;
import com.trilogy.app.crm.home.PipelineFactory;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Creates the home pipeline for the MsisdnOwnership Home which is simply a adapter home 
 * that utilzes the MsisdnHome and a client to TFA for group association.
 * 
 * @author rpatel
 */
public class AcquiredMsisdnHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx)
        throws RemoteException, HomeException, IOException, AgentException
    {
        ctx.put(AcquiredMsisdnPINManagementHome.class, new AcquiredMsisdnSetAuthenticationHome(ctx,
                new AcquiredMsisdnPINManagementXDBHome(ctx, "ACQUIREDMSISDNPINMANAGEMENT")));
        ctx.put(AcquiredMsisdnPINManagementTransientHome.class, new AcquiredMsisdnPINManagementTransientHome(ctx));
        return (Home) ctx.get(AcquiredMsisdnPINManagementHome.class);
    }
}
