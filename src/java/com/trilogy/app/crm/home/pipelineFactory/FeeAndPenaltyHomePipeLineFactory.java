package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.FeeAndPenalty;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.inboundfile.home.FeeAndPenaltyIDSettingHome;
import com.trilogy.app.crm.inboundfile.validators.FeeAndPenaltyReasonCodeValidator;
import com.trilogy.app.crm.inboundfile.validators.FeeAndPenaltyStoreValidator;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

/**
 * This Pipe Line is introduced to manage homes for FeeAndPenalty configurations
 * @author skambab
 *
 */
public class FeeAndPenaltyHomePipeLineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context ctx1)
			throws RemoteException, HomeException, IOException, AgentException {
		Home home = StorageSupportHelper.get(ctx).createHome(ctx,FeeAndPenalty.class, "FeeAndPenalty", true);
		home = new FeeAndPenaltyIDSettingHome(ctx, home);
		home = new ValidatingHome(new FeeAndPenaltyReasonCodeValidator(), new FeeAndPenaltyStoreValidator(), home);
		home = new SpidAwareHome(ctx, home);
		
		return home;
	}

}
