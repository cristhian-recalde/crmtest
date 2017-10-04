package com.trilogy.app.crm.home;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.DiscountTransactionHist;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class DiscountTransactionHistPipelineFactory implements PipelineFactory{

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		Home home = StorageSupportHelper.get(ctx).createHome(ctx, DiscountTransactionHist.class, "DISCOUNTTRANSACTIONHIST");
		home = new UpdateTimeStampHome(ctx, home);
		home = new DiscountTransactionHistIdentifierSettingHome(home); 
		return home;
	}
}
