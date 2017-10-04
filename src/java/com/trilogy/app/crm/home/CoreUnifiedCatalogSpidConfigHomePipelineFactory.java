package com.trilogy.app.crm.home;


import java.io.IOException;
import java.rmi.RemoteException;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfig;

public class CoreUnifiedCatalogSpidConfigHomePipelineFactory implements PipelineFactory{
	
	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		
			
			Home home = CoreSupport.bindHome(ctx, UnifiedCatalogSpidConfig.class);
			
			return home;
		
	}

}
