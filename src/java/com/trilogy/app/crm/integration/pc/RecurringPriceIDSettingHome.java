package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.price.RecurringPrice;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class RecurringPriceIDSettingHome extends HomeProxy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RecurringPriceIDSettingHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException {

		RecurringPrice recurringPrice = (RecurringPrice) obj;
		if (recurringPrice.getRecurringPriceId() == 0) {
			recurringPrice.setRecurringPriceId(getNextIdentifier(ctx));
		}

		LogSupport.info(ctx, this, "[RecurringPriceIDSettingHome.create] recurringPrice ID set to: " + recurringPrice.getRecurringPriceId());

		return super.create(ctx, obj);
	}

	private long getNextIdentifier(Context ctx) throws HomeException {
		IdentifierSequenceSupportHelper.get((Context) ctx).ensureSequenceExists(ctx, IdentifierEnum.RECURRING_PRICE_ID,
				1, Long.MAX_VALUE);
		return IdentifierSequenceSupportHelper.get((Context) ctx).getNextIdentifier(ctx,
				IdentifierEnum.RECURRING_PRICE_ID, null);
	}
}
