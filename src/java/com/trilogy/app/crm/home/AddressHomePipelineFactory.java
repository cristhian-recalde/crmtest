package com.trilogy.app.crm.home;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.AddressHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
//import com.trilogy.framework.xlog.log.LogSupport;

public class AddressHomePipelineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException 
	{
		//Home addressHome = StorageSupportHelper.get(ctx).createHome(ctx, Address.class, "ADDRESS");
		Home addressHome = (Home)ctx.get(AddressHome.class);
		//LogSupport.info(ctx, this, "AddressHome:"+addressHome.toString());
		
		addressHome = new AddressIdentifierSettingHome(ctx, addressHome);
		addressHome = new AddressValidatingHome(ctx,addressHome);
		
		return addressHome;
	}

}
