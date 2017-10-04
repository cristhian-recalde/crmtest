package com.trilogy.app.crm.home;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.CatalogEntityHistory;
import com.trilogy.app.crm.bean.ProductPrice;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * @author AChatterjee
 *
 */

public class CatalogEntityHistoryHomePipelineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException, AgentException {
		
		Home home = StorageSupportHelper.get(ctx).createHome(ctx, CatalogEntityHistory.class, "CatalogEntityHistory");
		//home = new CatalogEntityHistoryAdapterHome(ctx, home);
		home = new CatalogEntityHistoryIDSettingHome(ctx, home);
		return home;
	}

}
