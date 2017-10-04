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
package com.trilogy.app.crm.home.account;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.AccntGrpScreeningTemp;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Pipeline for Group Screening Template
 * 
 * @author ankit.nagpal
 * @since 9.9
 */

public class AccountsGroupScreeningTemplatePipelineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context arg1)
			throws RemoteException, HomeException, IOException, AgentException {
		
		LogSupport.info(ctx, this, "Installing Group Template Home");
		Home home = StorageSupportHelper.get(ctx).createHome(ctx, AccntGrpScreeningTemp.class, "AccntGrpScreeningTemplate");
		home = new SpidAwareHome(ctx, home);
		
		return home;
	}

}
