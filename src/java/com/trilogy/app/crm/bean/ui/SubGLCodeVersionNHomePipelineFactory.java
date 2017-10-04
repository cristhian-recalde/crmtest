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

import com.trilogy.app.crm.home.SubGLCodeVersionNHomeProxy;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This will implement AccountZipCodeFilterCriteria implementation of methods from com.redknee.app.crm.invoice.bean.BillRunFilterCriteria.
 * @author bhushan.deshmukh@redknee.com
 * @since 10.3.6
 */

public class SubGLCodeVersionNHomePipelineFactory implements PipelineFactory{

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		Home glCodeNHome = StorageSupportHelper.get(ctx).createHome(ctx,SubGLCodeVersionN.class, "SubGLCodeVersionN");
		LogSupport.info(ctx, this, "Installing the SubGLCode");
		//glCodeNHome = new SubGLCodeNSettingHome(ctx, glCodeNHome);
		glCodeNHome = new SubGLCodeVersionUpdateSettingHome(ctx,glCodeNHome);
		glCodeNHome = new SubGLCodeVersionNHomeProxy(ctx, glCodeNHome);
        return glCodeNHome;		
        }

}
