package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.SimcardHlrBulkLoadTask;
import com.trilogy.app.crm.bean.SimcardHlrBulkLoadTaskHome;
import com.trilogy.app.crm.bean.SimcardHlrBulkLoadTaskXDBHome;
import com.trilogy.app.crm.bean.SimcardHlrBulkLoadTaskXInfo;
import com.trilogy.app.crm.home.BackgroundTaskAwareHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class SimcardHlrBulkloadTaskHomePipelineFactory implements PipelineFactory {

	public SimcardHlrBulkloadTaskHomePipelineFactory() {
		super();
	}

	public Home createPipeline(final Context ctx, final Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {

		Home home = StorageSupportHelper.get(ctx).createHome(ctx,
				SimcardHlrBulkLoadTask.class, SimcardHlrBulkLoadTaskXInfo.DEFAULT_TABLE_NAME);
		home = new BackgroundTaskAwareHome<SimcardHlrBulkLoadTask>(SimcardHlrBulkLoadTask.class,home);
		ctx.put(SimcardHlrBulkLoadTaskHome.class, home);
		return home;

	}
}
