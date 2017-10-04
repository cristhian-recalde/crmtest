package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;
import com.trilogy.app.crm.bean.CatalogEntityEnum;
import com.trilogy.app.crm.bean.ui.ResourceProduct;
import com.trilogy.app.crm.bean.ui.ResourceProductIDSettingHome;
import com.trilogy.app.crm.home.CatalogEntityHistoryAdapterHome;
//import com.trilogy.app.crm.home.ResourceProductToServiceAdapterHome;
import com.trilogy.app.crm.home.PipelineFactory;
//import com.trilogy.app.crm.home.ServiceCreationFromResourceProductHome;
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

public class ResourceProductHomePipelineFactory implements PipelineFactory {

	public ResourceProductHomePipelineFactory(){

	}

	@Override
	public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException, AgentException {
		
		Home packageHome = StorageSupportHelper.get(ctx).createHome(ctx, ResourceProduct.class, "ResourceProduct");
		//Create the ID for ResourceProduct
		packageHome = new ResourceProductIDSettingHome(ctx, packageHome);
		//packageHome = new ResourceProductToServiceAdapterHome(ctx, packageHome);
        // Entry for CatalogEvent History creation 
		packageHome = new CatalogEntityHistoryAdapterHome(ctx, packageHome, CatalogEntityEnum.ResourceProduct);
		//packageHome = new ServiceCreationFromResourceProductHome(ctx, packageHome);
        return packageHome;
	}

}
