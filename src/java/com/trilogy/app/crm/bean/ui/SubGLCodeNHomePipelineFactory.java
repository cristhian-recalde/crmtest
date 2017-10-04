package com.trilogy.app.crm.bean.ui;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.home.PipelineFactory;
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
public class SubGLCodeNHomePipelineFactory  implements PipelineFactory{

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		Home subGlCodeHome = StorageSupportHelper.get(ctx).createHome(ctx, SubGLCodeN.class, "SubGLCodeN");
		subGlCodeHome = new RemoveSubGLCodeVersionsHome(ctx, subGlCodeHome);
		return subGlCodeHome;
	}

}
