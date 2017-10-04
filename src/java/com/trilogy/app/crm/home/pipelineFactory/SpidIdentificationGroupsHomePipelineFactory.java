/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.SpidIdentificationGroupsListsValidator;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;

/**
 * @author cindy.wong@redknee.com
 * @since 2011-03-04
 */
public class SpidIdentificationGroupsHomePipelineFactory implements
    PipelineFactory
{

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
	    throws RemoteException, HomeException, IOException, AgentException
	{
		Home home = CoreSupport.bindHome(ctx, SpidIdentificationGroups.class);
		home = new AuditJournalHome(ctx, home);
		home = new SortingHome(ctx, home);
		home =
		    new RMIClusteredHome(ctx, SpidIdentificationGroups.class.getName(),
		        home);
		home =
		    new AdapterHome(ctx,
		        new com.redknee.app.crm.home.SpidIdentificationGroupsAdapter(),
		        home);
		home = new SpidAwareHome(ctx, home);
		home =
		    new ValidatingHome(new SpidIdentificationGroupsListsValidator(),
		        home);
		home =
		    ConfigChangeRequestSupportHelper.get(ctx)
		        .registerHomeForConfigSharing(ctx, home,
		            SpidIdentificationGroups.class);
		return home;
	}

}
