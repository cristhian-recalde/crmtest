package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.CatalogEntityHistory;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.ProductPrice;
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
public class CatalogEntityHistoryIDSettingHome extends HomeProxy {

	private static final long serialVersionUID = 1L;

	public CatalogEntityHistoryIDSettingHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}
	
	public Object create(Context ctx, Object obj) throws HomeException,
	HomeInternalException {
		
		CatalogEntityHistory catalogEntityHistory = (CatalogEntityHistory) obj;
		Long longId = getNextIdentifier(ctx);
		int id = longId.intValue();
		if (catalogEntityHistory.getId() == 0) {
			catalogEntityHistory.setId(id);
		}

		LogSupport.info(ctx, this, "CatalogEntityHistory ID set to: " + catalogEntityHistory.getId());

		return super.create(ctx, obj);
	}

	@SuppressWarnings("deprecation")
	private long getNextIdentifier(Context ctx) throws HomeException {
		IdentifierSequenceSupportHelper.get((Context) ctx)
		.ensureSequenceExists(ctx, IdentifierEnum.ID, 1,
				Long.MAX_VALUE);
		return IdentifierSequenceSupportHelper.get((Context) ctx)
		.getNextIdentifier(ctx, IdentifierEnum.ID, null);
	}

}
