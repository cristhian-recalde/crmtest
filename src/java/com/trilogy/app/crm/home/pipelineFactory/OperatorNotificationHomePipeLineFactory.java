package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.OperatorNotification;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.inboundfile.home.OperatorNotificationIDSettingHome;
import com.trilogy.app.crm.inboundfile.validators.OperatorNotificationSPIDValidator;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

public class OperatorNotificationHomePipeLineFactory implements PipelineFactory{

	@Override
	public Home createPipeline(Context ctx, Context ctx1)throws RemoteException, HomeException, IOException, AgentException
	{
		Home home = StorageSupportHelper.get(ctx).createHome(ctx,OperatorNotification.class, "OperatorNotification", true);
		home = new OperatorNotificationIDSettingHome(ctx, home);
		home = new ValidatingHome(new OperatorNotificationSPIDValidator(), new OperatorNotificationSPIDValidator(), home);
		home = new SpidAwareHome(ctx, home);
		return home;
	}

}
