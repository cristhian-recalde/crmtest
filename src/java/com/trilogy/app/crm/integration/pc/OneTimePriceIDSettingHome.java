package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.price.OneTimePrice;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class OneTimePriceIDSettingHome extends HomeProxy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OneTimePriceIDSettingHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException {

		OneTimePrice oneTimePrice = (OneTimePrice) obj;
		if (oneTimePrice.getOneTimePriceId() == 0) {
			oneTimePrice.setOneTimePriceId(getNextIdentifier(ctx));
		}

		LogSupport.info(ctx, this, "[OneTimePriceIDSettingHome.create] oneTimePrice ID set to: " + oneTimePrice.getOneTimePriceId());

		return super.create(ctx, obj);
	}

	private long getNextIdentifier(Context ctx) throws HomeException {
		IdentifierSequenceSupportHelper.get((Context) ctx).ensureSequenceExists(ctx, IdentifierEnum.ONE_TIME_PRICE_ID,
				1, Long.MAX_VALUE);
		return IdentifierSequenceSupportHelper.get((Context) ctx).getNextIdentifier(ctx,
				IdentifierEnum.ONE_TIME_PRICE_ID, null);
	}
}
