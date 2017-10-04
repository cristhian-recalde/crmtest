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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.DebtCollectionAgency;
import com.trilogy.app.crm.bean.DebtCollectionAgencyHome;
import com.trilogy.app.crm.home.core.CoreDebtCollectionAgencyHomePipelineFactory;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.home.DebtCollectionAgencyIDSettingHome;

/**
 * @author cindy.wong@redknee.com
 * @since 2011-03-03
 */
public class DebtCollectionAgencyHomePipelineFactory extends
    CoreDebtCollectionAgencyHomePipelineFactory
{

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
	    throws RemoteException, HomeException, IOException, AgentException
	{
		Home home = (Home) ctx.get(DebtCollectionAgencyHome.class);
		home =
		    ConfigChangeRequestSupportHelper.get(ctx)
		        .registerHomeForConfigSharing(ctx, home,
		            DebtCollectionAgency.class);
		home =  new DebtCollectionAgencyIDSettingHome(ctx, home);
		return home;

	}
}
