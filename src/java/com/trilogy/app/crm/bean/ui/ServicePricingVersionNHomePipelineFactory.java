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
package com.trilogy.app.crm.bean.ui;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.ServicePricingVersionToServiceUpdateHome;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author bhushan.deshmukh@redknee.com
 * @since 10.3.6
 */
public class ServicePricingVersionNHomePipelineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException 
	{
		
		Home pricingVersionHome = StorageSupportHelper.get(ctx).createHome(ctx,PricingVersion.class, "PricingVersionN");
		LogSupport.info(ctx, this, "FINDME: Installing the PrcingVersion");
		pricingVersionHome = new PricingVersionSettingHome(ctx, pricingVersionHome);
		pricingVersionHome = new ServicePricingVersionToServiceUpdateHome(ctx, pricingVersionHome);
        return pricingVersionHome;		
	
	}

}
