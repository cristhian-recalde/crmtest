package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.dunning.DunningWaiver;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


public class DunningWavierHomePipeline implements PipelineFactory{

	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		
		Home home = StorageSupportHelper.get(ctx).createHome(
				ctx, DunningWaiver.class, "DUNNINGWAIVER");
		
		return home;
	}

}
