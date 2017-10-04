package com.trilogy.app.crm.bean.ui;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.CatalogEntityEnum;
import com.trilogy.app.crm.home.core.CoreAdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.app.crm.home.CatalogEntityHistoryAdapterHome;

public class ProductHomePipelineFactory extends CoreAdjustmentTypeHomePipelineFactory{
	

	 @Override
	    public Home createPipeline(Context ctx, Context serverCtx)
	            throws RemoteException, HomeException, IOException
	    {
	        Home systemHome = StorageSupportHelper.get(ctx).createHome(ctx, Product.class, "PRODUCT");
	        // Entry for CatalogEvent History
	        systemHome = new CatalogEntityHistoryAdapterHome(ctx, systemHome, CatalogEntityEnum.Product);
	        systemHome = new ProductIDSettingHome(ctx, systemHome);
	        return systemHome;
	    }

	

}
