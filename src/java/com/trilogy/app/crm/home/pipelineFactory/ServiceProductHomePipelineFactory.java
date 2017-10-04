package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.CatalogEntityEnum;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.ServiceProductIDSettingHome;
import com.trilogy.app.crm.extension.ExtensionForeignKeyAdapter;
import com.trilogy.app.crm.extension.ExtensionHandlingHome;
import com.trilogy.app.crm.extension.service.ServiceNExtension;
import com.trilogy.app.crm.extension.service.ServiceNExtensionXInfo;
import com.trilogy.app.crm.home.CatalogEntityHistoryAdapterHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.ServiceProductToServiceAdapterHome;
import com.trilogy.app.crm.integration.pc.ServiceCreationHome;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class ServiceProductHomePipelineFactory implements PipelineFactory {

	public ServiceProductHomePipelineFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		
		Home serviceHome = StorageSupportHelper.get(ctx).createHome(ctx, ServiceProduct.class,"ServiceProduct");		
		serviceHome = new ExtensionHandlingHome<ServiceNExtension>(ctx,ServiceNExtension.class,ServiceNExtensionXInfo.SERVICE_ID, serviceHome);
        serviceHome = new AdapterHome(serviceHome,new ExtensionForeignKeyAdapter(ServiceNExtensionXInfo.SERVICE_ID));
        serviceHome = new ServiceProductIDSettingHome(ctx, serviceHome);
        serviceHome = new ServiceProductToServiceAdapterHome(ctx, serviceHome);
        // Entry for CatalogEvent History creation 
		serviceHome = new CatalogEntityHistoryAdapterHome(ctx, serviceHome, CatalogEntityEnum.ServiceProduct);
		serviceHome = new ServiceCreationHome(ctx,serviceHome);
        return serviceHome;
	}

}
