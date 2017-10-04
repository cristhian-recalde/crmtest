package com.trilogy.app.crm.home;
import java.io.IOException;
import java.rmi.RemoteException;
import com.trilogy.app.crm.bean.ui.PricingTemplate;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author kkadam
 *
 */
public class PricingTemplateHomePipelineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
        LogSupport.info(ctx, this, "Installing the Pricing Template");
		Home pricingTemplateHome = StorageSupportHelper.get(ctx).createHome(ctx, PricingTemplate.class, "PricingTemplate");
		LogSupport.info(ctx, this, "Installing complete Pricing Template");
		return pricingTemplateHome;
	}

}
