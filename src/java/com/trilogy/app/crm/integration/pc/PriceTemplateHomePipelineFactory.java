package com.trilogy.app.crm.integration.pc;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.ui.PriceTemplate;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author AChatterjee
 *
 */
public class PriceTemplateHomePipelineFactory implements PipelineFactory{

	@Override
	public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException, AgentException {
		
        LogSupport.info(ctx, this, "[createPipeline] Installing the Price Template ");

        Home priceTemplateHome = StorageSupportHelper.get(ctx).createHome(ctx, PriceTemplate.class, "PRICETEMPLATE");
        priceTemplateHome = new PriceTemplateNameCheckingHome(ctx, priceTemplateHome);
        priceTemplateHome  = new PriceTemplateCompSpecCheckingHome(ctx, priceTemplateHome);
        priceTemplateHome = new SortingHome(priceTemplateHome);
        priceTemplateHome = new PriceTemplateIdSettingHome(ctx, priceTemplateHome);		
        return priceTemplateHome;
    }

}
