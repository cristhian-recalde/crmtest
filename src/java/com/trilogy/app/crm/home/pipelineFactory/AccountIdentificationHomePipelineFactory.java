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

import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.SpidAwareAccountIdentificationInvocationHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;

/**
 * @author cindy.wong@redknee.com
 * @since 2011-03-03
 */
public class AccountIdentificationHomePipelineFactory implements
    PipelineFactory
{
	private static final AccountIdentificationHomePipelineFactory instance =
	    new AccountIdentificationHomePipelineFactory();

	public static AccountIdentificationHomePipelineFactory instance()
	{
		return instance;
	}

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
	    throws RemoteException, HomeException, IOException, AgentException
	{
		Home home =
		    StorageSupportHelper.get(ctx).createHome(ctx,
		        AccountIdentification.class, "ACCOUNTIDENTIFICATION");
		home = new SpidAwareAccountIdentificationInvocationHome(home);
		home = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(
		    ctx, home, AccountIdentification.class);
		return home;
	}

}
