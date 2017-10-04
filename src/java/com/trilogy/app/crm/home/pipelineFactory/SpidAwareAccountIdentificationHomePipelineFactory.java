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

import com.trilogy.app.crm.bean.account.SpidAwareAccountIdentification;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NullHome;

/**
 * @author suyash.gaidhani@redknee.com	
 * @since 9.2
 */
public class SpidAwareAccountIdentificationHomePipelineFactory implements
    PipelineFactory
{
	private static final SpidAwareAccountIdentificationHomePipelineFactory instance =
	    new SpidAwareAccountIdentificationHomePipelineFactory();

	public static SpidAwareAccountIdentificationHomePipelineFactory instance()
	{
		return instance;
	}

	@Override
	/**
	 * This pipeline is only meant for config sharing. The real AccountIdentification bean will be stored to DB via the original pipeline.
	 */
	public Home createPipeline(Context ctx, Context serverCtx)
	    throws RemoteException, HomeException, IOException, AgentException
	{
		Home home = new NullHome(ctx);
		    
		home = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(
		    ctx, home, SpidAwareAccountIdentification.class);
		return home;
	}

}
