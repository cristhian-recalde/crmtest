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

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.RMIHomeServer;
import com.trilogy.framework.xhome.home.ReadOnlyHome;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnOwnershipHome;
import com.trilogy.app.crm.support.DeploymentTypeSupportHelper;
import com.trilogy.app.crm.xhome.MsisdnOwnershipAdapterHomeProxy;
import com.trilogy.app.crm.xhome.MsisdnOwnershipManagementHome;

/**
 * Creates the home pipeline for the MsisdnOwnership Home which is simply a adapter home 
 * that utilzes the MsisdnHome and a client to TFA for group association.
 * 
 * @author rpatel
 */
public class MsisdnOwnershipHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx)
        throws RemoteException, HomeException, IOException, AgentException
    {
        Home msisdnHome = (Home)ctx.get(MsisdnHome.class);
        msisdnHome = new MsisdnOwnershipAdapterHomeProxy(ctx, msisdnHome);
        msisdnHome = new LicenseHome(ctx, LicenseConstants.MULTI_LANGUAGE, 
                                        new MsisdnOwnershipLanguageSettingHome(ctx, msisdnHome));
        msisdnHome = new MsisdnOwnershipManagementHome(ctx, msisdnHome);
        
        ctx.put(MsisdnOwnershipHome.class, msisdnHome);
        ctx.put("MsisdnOwnership", msisdnHome);

        // [rpatel] exposing the RMI home for future use
        if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
        {
            new RMIHomeServer(serverCtx,
                new ReadOnlyHome(
                    new PMHome(
                        ctx,
                        MsisdnOwnershipHome.class.getName() + ".rmiserver",
                        (Home) ctx.get(MsisdnOwnershipHome.class))),
                        MsisdnOwnershipHome.class.getName()).register();
        }


        return msisdnHome;
    }

}
