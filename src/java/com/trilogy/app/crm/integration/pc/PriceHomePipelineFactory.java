package com.trilogy.app.crm.integration.pc;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.CatalogEntityEnum;
import com.trilogy.app.crm.bean.price.Price;
import com.trilogy.app.crm.home.CatalogEntityHistoryAdapterHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class PriceHomePipelineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		
		LogSupport.debug(ctx, this, "[PriceHomePipelineFactory.createPipeline] Creating pipeline for Price");

        Home priceHome = StorageSupportHelper.get(ctx).createHome(ctx, Price.class, "PRICE");
        // Entry for CatalogEvent History creation
        priceHome = new CatalogEntityHistoryAdapterHome(ctx, priceHome, CatalogEntityEnum.Price);

        return priceHome ;
    }

}
