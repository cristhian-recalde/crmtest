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

import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.home.NotInUseAccountHome;
import com.trilogy.app.crm.home.core.CoreAccountCategoryHomePipelineFactory;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

/**
 * @author cindy.wong@redknee.com
 * @since 9.0
 */
public class AccountCategoryHomePipelineFactory extends
    CoreAccountCategoryHomePipelineFactory
{

	protected AccountCategoryHomePipelineFactory()
	{
		// TODO
	}

	public static AccountCategoryHomePipelineFactory instance()
	{
		if (instance == null)
		{
			instance = new AccountCategoryHomePipelineFactory();
		}
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Home createPipeline(final Context ctx, final Context serverCtx)
	    throws RemoteException, HomeException, IOException
	{
		// Base home should already be installed by AppCrmCore
		return decorateHome((Home) ctx.get(AccountCategoryHome.class), ctx,
		    serverCtx);
	}

	/**
	 * Decorates the provided home with the full pipeline.
	 * 
	 * @param originalHome
	 *            The original home.
	 * @param context
	 *            The operating context.
	 * @param serverContext
	 *            The server context.
	 * @return The home decorated with the full account type pipeline.
	 * @throws IOException
	 *             Thrown if RMI clustering fails.
	 * @throws HomeException
	 *             Thrown if RMI clustering fails.
	 * @throws RemoteException
	 *             Thrown if RMI clustering fails.
	 */
	public Home decorateHome(final Home originalHome, final Context context,
	    final Context serverContext) throws RemoteException, HomeException,
	    IOException
	{
		Home home = originalHome;

		home =
		    new NotInUseAccountHome(
		        context,
		        "Cannot update or delete Account Type that is in use by existing Accounts.",
		        home);
		home = new SpidAwareHome(context, home);
		home = new SortingHome(home);
        home =
            ConfigChangeRequestSupportHelper.get(context)
                .registerHomeForConfigSharing(context, home, AccountCategory.class);
		return home;
	}

	/**
	 * Singleton instance.
	 */
	private static AccountCategoryHomePipelineFactory instance;

}
