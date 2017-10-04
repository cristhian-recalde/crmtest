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

import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.home.MsisdnGroupValidatingHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.SharedBeanGenericSelectionHome;
import com.trilogy.app.crm.home.SharedBeanSpidOverridingHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.technology.TechnologyAwareHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoRemoveAllHome;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

/**
 * Creates the home pipeline for the MsisdnGroup Home.
 * @author amedina
 *
 */
public class MsisdnGroupHomePipelineFactory implements PipelineFactory
{

    @Override
    public Home createPipeline(final Context ctx, final Context serverCtx)
    throws RemoteException, HomeException, IOException, AgentException
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, MsisdnGroup.class, "MsisdnGroup");
        home = new NotifyingHome(home);
        home = new SortingHome(home);
        home = new MsisdnGroupValidatingHome(ctx,home);
        home = new TechnologyAwareHome(ctx,home);
        home = new SpidAwareHome(ctx, home);
        home = new SharedBeanGenericSelectionHome<MsisdnGroup>(ctx, home);
        home = new SharedBeanSpidOverridingHome<MsisdnGroup>(ctx, home);
        home = new NoRemoveAllHome(home);
		home =
		    ConfigChangeRequestSupportHelper.get(ctx)
		        .registerHomeForConfigSharing(ctx, home, MsisdnGroup.class);
        ctx.put(MsisdnGroupHome.class, home);
        return home;
    }

}
