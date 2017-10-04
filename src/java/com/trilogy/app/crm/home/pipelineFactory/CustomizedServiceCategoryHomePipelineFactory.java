package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;
import com.trilogy.app.crm.bean.ServiceCategoryHome;
import com.trilogy.app.crm.home.ServiceCategoryDeleteValidationHome;
import com.trilogy.app.crm.home.ServiceCategoryPipelineFactory;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class CustomizedServiceCategoryHomePipelineFactory extends ServiceCategoryPipelineFactory{
	
	@Override
	public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException, AgentException {
		
		// Base home is already  installed by AppCrmCore named ServiceCategoryPipelineFactory
        Home home = (Home) ctx.get(ServiceCategoryHome.class);
        
        home = new ServiceCategoryDeleteValidationHome(ctx, home);
        
		return home;
	}

}
