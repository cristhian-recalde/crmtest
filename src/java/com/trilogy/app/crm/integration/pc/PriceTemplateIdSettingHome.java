package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.ui.PriceTemplate;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author AChatterjee
 *
 */
public class PriceTemplateIdSettingHome extends HomeProxy {

	private static final long serialVersionUID = 1L;

	private static final Long PRICE_TEMPLATE_ID_START_NUMBER = 200000000L;

	private static final Long PRICE_TEMPLATE_ID_END_NUMBER = 299999999L;

	public PriceTemplateIdSettingHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {

		LogSupport.debug(ctx, this, "[PriceTemplateIdSettingHome.create] Preforming Create Operation");
		
		PriceTemplate priceTemplate = (PriceTemplate) obj;
		long priceTemplateId = getNextIdentifier(ctx);
		
		if (priceTemplate.getID() == 0) {
			priceTemplate.setID(priceTemplateId);
		}

		LogSupport.info(ctx, this, "[PriceTemplateIdSettingHome.create] PriceTemplate ID is set to: "
				+ priceTemplate.getID());

		return super.create(ctx, obj);
	}

	@SuppressWarnings("deprecation")
	private long getNextIdentifier(Context ctx) throws HomeException {
		IdentifierSequenceSupportHelper.get((Context) ctx)
				.ensureSequenceExists(ctx, IdentifierEnum.PRICE_TEMPLATE_ID,
						PRICE_TEMPLATE_ID_START_NUMBER,
						PRICE_TEMPLATE_ID_END_NUMBER);
		return IdentifierSequenceSupportHelper.get((Context) ctx)
				.getNextIdentifier(ctx, IdentifierEnum.PRICE_TEMPLATE_ID, null);
	}

}
