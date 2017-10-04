package com.trilogy.app.crm.integration.pc;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.CatalogEntityEnum;
import com.trilogy.app.crm.bean.price.RecurringPrice;
import com.trilogy.app.crm.home.CatalogEntityHistoryAdapterHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class RecurringPriceHomePipelineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		
		LogSupport.debug(ctx, this, "[RecurringPriceHomePipelineFactory.createPipeline] Start");

        Home recurringPriceHome = StorageSupportHelper.get(ctx).createHome(ctx, RecurringPrice.class, "RECURRINGPRICE");
        
        // Entry for CatalogEvent History creation 
        recurringPriceHome = new CatalogEntityHistoryAdapterHome(ctx, recurringPriceHome, CatalogEntityEnum.RecurringPrice);
        
        recurringPriceHome = new RecurringPriceIDSettingHome(ctx, recurringPriceHome);

        return recurringPriceHome ;
    }

}
