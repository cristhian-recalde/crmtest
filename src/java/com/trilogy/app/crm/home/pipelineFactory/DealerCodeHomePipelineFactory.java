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

import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.home.DealerCodeERLogHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;

/**
 * Pipeline factory for dealer code home.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class DealerCodeHomePipelineFactory implements PipelineFactory
{

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
	    throws RemoteException, HomeException, IOException, AgentException
	{
		/*
		 * [Cindy Wong] TT#10072614023: Switching dealer code to to database
		 * home. Also, there is no need to cluster.
		 */
		Home home =
		    StorageSupportHelper.get(ctx).createHome(ctx, DealerCode.class,
		        "DEALERCODE");
		home = new NotifyingHome(home);
		home = new AuditJournalHome(ctx, home);
		home = new DealerCodeERLogHome(home);
		home = new SpidAwareHome(ctx, home);
		home = new SortingHome(home);
		home =
		    ConfigChangeRequestSupportHelper.get(ctx)
		        .registerHomeForConfigSharing(ctx, home, DealerCode.class);
		home = new PMHome(ctx, "DealerCode Creation And Access", home);
		return home;
	}

}
