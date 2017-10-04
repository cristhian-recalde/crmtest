package com.trilogy.app.crm.integration.pc;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.CatalogEntityEnum;
import com.trilogy.app.crm.bean.price.OneTimePrice;
import com.trilogy.app.crm.home.CatalogEntityHistoryAdapterHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class OneTimePriceHomePipelineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		
		LogSupport.debug(ctx, this, "[OneTimePriceHomePipelineFactory.createPipeline] Creating pipeline for One time price");

        Home oneTimePriceHome = StorageSupportHelper.get(ctx).createHome(ctx, OneTimePrice.class, "ONETIMEPRICE");
        // Entry for CatalogEvent History creation
        oneTimePriceHome = new CatalogEntityHistoryAdapterHome(ctx, oneTimePriceHome, CatalogEntityEnum.OneTimePrice);
        oneTimePriceHome = new OneTimePriceIDSettingHome(ctx, oneTimePriceHome);
        return oneTimePriceHome ;

	}
}
