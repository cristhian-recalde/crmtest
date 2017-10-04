package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.CreditCardPrefixRateMap;
import com.trilogy.app.crm.bean.CreditCardPrefixRateMapTransientHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.xhome.home.TotalCachingHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.app.crm.home.CreditCardPrefixRateMapIDSettingHome;

/**
 * 
 * Creates a home pipeline for {@link CreditCardPrefixRateMap}
 * 
 * @author @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class CreditCardPrefixRateMapHomePipelineFactory implements
		PipelineFactory {

	public CreditCardPrefixRateMapHomePipelineFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		
		Home home = StorageSupportHelper.get(ctx).createHome(ctx, CreditCardPrefixRateMap.class, PaymentGatewayIntegrationConstants.PREFIX_RATE_MAP_TABLE_NAME);
		home = new CreditCardPrefixRateMapIDSettingHome(ctx, "CreditCardPrefixRateMapID_seq", home);
		home = new TotalCachingHome(ctx, new CreditCardPrefixRateMapTransientHome(ctx), home);
		home = new SortingHome(ctx, new MsisdnPrefixDescendingComparator(), home);
		
		return home;
	}

}
