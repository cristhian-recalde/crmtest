package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.PaymentFTRecords;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.inboundfile.home.PaymentFileRecordTrackerIDSettingHome;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class PaymentFileTrackerRecordHomePipeLineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		Home home = StorageSupportHelper.get(ctx).createHome(ctx, PaymentFTRecords.class, "PaymentFTRecords");
		home = new PaymentFileRecordTrackerIDSettingHome(ctx, home);
		return home;
	}

}
