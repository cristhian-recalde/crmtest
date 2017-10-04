package com.trilogy.app.crm.home;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.DepositTypeHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class DepositTypeHomePipelineFactory extends com.redknee.app.crm.home.pipelineFactory.CoreDepositTypeHomePipelineFactory{

	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		// Core already installed part of this pipeline
		Home home = (Home) ctx.get(DepositTypeHome.class);
		home = new DepositCreateAdjustmentTypeHome(home);
		return home;

	}
}
