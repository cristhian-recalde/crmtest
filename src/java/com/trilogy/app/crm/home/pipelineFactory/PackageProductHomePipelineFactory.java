package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;
import com.trilogy.app.crm.bean.CatalogEntityEnum;
import com.trilogy.app.crm.bean.ui.PackageProduct;
import com.trilogy.app.crm.bean.ui.PackageProductIDSettingHome;
import com.trilogy.app.crm.home.CatalogEntityHistoryAdapterHome;
import com.trilogy.app.crm.home.PackageProductToServiceAdapterHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.ServiceCreationFromPackageProductHome;
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

public class PackageProductHomePipelineFactory implements PipelineFactory {

	public PackageProductHomePipelineFactory(){

	}

	@Override
	public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException, AgentException {
		
		Home packageHome = StorageSupportHelper.get(ctx).createHome(ctx, PackageProduct.class, "PackageProduct");
		//Create the ID for PackageProduct
		packageHome = new PackageProductIDSettingHome(ctx, packageHome);
		packageHome = new PackageProductToServiceAdapterHome(ctx, packageHome);
        // Entry for CatalogEvent History creation 
		packageHome = new CatalogEntityHistoryAdapterHome(ctx, packageHome, CatalogEntityEnum.PackageProduct);
		packageHome = new ServiceCreationFromPackageProductHome(ctx, packageHome);
        return packageHome;
	}

}
