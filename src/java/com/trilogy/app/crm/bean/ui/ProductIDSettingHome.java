package com.trilogy.app.crm.bean.ui;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class ProductIDSettingHome extends HomeProxy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProductIDSettingHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {

		Product product = (Product) obj;
		long productID = getNextIdentifier(ctx);
		if (product.getProductId() == 0) {
			product.setProductId(productID);
		}

		LogSupport.info(ctx, this,
				"Product type ID set to: " + product.getProductId());

		return super.create(ctx, obj);
	}

	@SuppressWarnings("deprecation")
	private long getNextIdentifier(Context ctx) throws HomeException {
		IdentifierSequenceSupportHelper.get((Context) ctx)
				.ensureSequenceExists(ctx, IdentifierEnum.PRODUCT_ID, 1,
						Long.MAX_VALUE);
		return IdentifierSequenceSupportHelper.get((Context) ctx)
				.getNextIdentifier(ctx, IdentifierEnum.PRODUCT_ID, null);
	}
} // class
