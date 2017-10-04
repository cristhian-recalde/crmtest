package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.CatalogEntityEnum;
import com.trilogy.app.crm.bean.ProductPrice;
import com.trilogy.app.crm.home.CatalogEntityHistoryAdapterHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.ProductPriceIDSettingHome;
import com.trilogy.app.crm.home.validator.ProductPriceConstraintValidator;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;

/**
 * 
 * @author AChatterjee
 *
 */
public class ProductPriceHomePipelineFactory implements PipelineFactory{

	
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		Home home = StorageSupportHelper.get(ctx).createHome(ctx, ProductPrice.class, "PRODUCTPRICE");
		home = new ProductPriceIDSettingHome(ctx, home);
		home = new ValidatingHome(new ProductPriceConstraintValidator(), home);
		// Entry for CatalogEvent History
		home= new CatalogEntityHistoryAdapterHome(ctx,home,CatalogEntityEnum.ProductPrice);
		return home;
	}

}
