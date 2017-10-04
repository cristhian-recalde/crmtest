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
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.RMIHomeServer;
import com.trilogy.framework.xhome.home.ReadOnlyHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.home.core.CoreMsisdnHomePipelineFactory;
import com.trilogy.app.crm.numbermgn.MobileNumPoolMonitorHome;
import com.trilogy.app.crm.numbermgn.MsisdnChangeAppendHistoryHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.DeploymentTypeSupportHelper;

/**
 * Creates the home pipeline for the Msisdn Home.
 * @author arturo.medina@redknee.com
 */
public class MsisdnHomePipelineFactory extends CoreMsisdnHomePipelineFactory
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(final Context ctx, final Context serverCtx)
        throws RemoteException, HomeException, IOException, AgentException
    {
        // Base MSISDN home should already be installed by AppCrmCore
        Home msisdnHome = (Home) ctx.get(MsisdnHome.class);

        msisdnHome = new SharedBeanGenericSelectionHome<com.redknee.app.crm.bean.Msisdn>(ctx, msisdnHome);
        msisdnHome = new NoSelectAllHome(msisdnHome);
        msisdnHome = new MsisdnChangeAppendHistoryHome(msisdnHome);
        msisdnHome = new MsisdnRemovalValidatorHome(msisdnHome);
        msisdnHome = new MobileNumPoolMonitorHome(msisdnHome);
        msisdnHome = new ValidatingHome(msisdnHome, MsisdnValidator.instance());
        msisdnHome = new MsisdnPortHandlingHome(msisdnHome);
        msisdnHome = new MsisdnStateChangeMonitorHome(msisdnHome);
        msisdnHome = new MsisdnMultiSpidSharingHome(msisdnHome);
        ctx.put("Msisdn", msisdnHome);

        // [jhughes] remote by Selfcare
        if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
        {
            new RMIHomeServer(serverCtx,
                new ReadOnlyHome(
                    new PMHome(
                        ctx,
                        MsisdnHome.class.getName() + ".rmiserver",
                        (Home) ctx.get(MsisdnHome.class))),
                        MsisdnHome.class.getName()).register();
        }
        
		msisdnHome =
		    ConfigChangeRequestSupportHelper.get(ctx)
		        .registerHomeForConfigSharing(ctx, msisdnHome, Msisdn.class);

		return msisdnHome;
    }

}
