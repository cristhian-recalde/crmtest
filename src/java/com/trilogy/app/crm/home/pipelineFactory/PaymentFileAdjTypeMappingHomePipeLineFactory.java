package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.PaymentFileAdjTypeMapping;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.inboundfile.home.PaymentFileAdjMappingIDSettingHome;
import com.trilogy.app.crm.inboundfile.home.UpdatePaymentFileAdjTypeMappingTimeAndUserHome;
import com.trilogy.app.crm.inboundfile.validators.PaymentFileAdjTypeMappingValidator;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

public class PaymentFileAdjTypeMappingHomePipeLineFactory implements PipelineFactory{

	@Override
	public Home createPipeline(Context ctx, Context ctx1)throws RemoteException, HomeException, IOException, AgentException
	{
		Home home = StorageSupportHelper.get(ctx).createHome(ctx,PaymentFileAdjTypeMapping.class, "PaymentFileAdjTypeMapping", true);
		home = new UpdatePaymentFileAdjTypeMappingTimeAndUserHome(ctx, home);
		home = new PaymentFileAdjMappingIDSettingHome(ctx, home);
		home = new ValidatingHome(new PaymentFileAdjTypeMappingValidator(), new PaymentFileAdjTypeMappingValidator(), home);
		home = new SpidAwareHome(ctx, home);
		return home;
	}

}
