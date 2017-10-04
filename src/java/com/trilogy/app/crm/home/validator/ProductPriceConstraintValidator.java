package com.trilogy.app.crm.home.validator;

import com.trilogy.app.crm.bean.ProductPrice;
import com.trilogy.app.crm.bean.ProductPriceHome;
import com.trilogy.app.crm.bean.ProductPriceXInfo;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;

public class ProductPriceConstraintValidator implements Validator {

	@Override
	public void validate(Context ctx, Object arg1) throws IllegalStateException {
		ProductPrice productPrice = (ProductPrice) arg1;

		And filter = new And();
		filter.add(new EQ(ProductPriceXInfo.PRICE_PLAN_ID, productPrice
				.getPricePlanId()));
		filter.add(new EQ(ProductPriceXInfo.PRICE_PLAN_VERSION_ID, productPrice
				.getPricePlanVersionId()));
		filter.add(new EQ(ProductPriceXInfo.PRODUCT_ID, productPrice
				.getProductId()));
		filter.add(new EQ(ProductPriceXInfo.PATH, productPrice.getPath()));

		LogSupport.debug(ctx, this, "Filter value " + filter.toString());

		Home home = (Home) ctx.get(ProductPriceHome.class);
		ProductPrice pPrice = null;
		try {
			pPrice = (ProductPrice) home.find(ctx, filter);
		} catch (HomeInternalException e) {
			throw new IllegalStateException("Could not fetch product price");
		} catch (HomeException e) {
			throw new IllegalStateException("Could not fetch product price");
		}
		if (pPrice != null) {
			throw new IllegalStateException(
					ProductPriceXInfo.PRICE_PLAN_ID.getSQLName()
							+ " "
							+ ProductPriceXInfo.PRICE_PLAN_VERSION_ID
									.getSQLName()
							+ " "
							+ ProductPriceXInfo.PRODUCT_ID.getSQLName()
							+ " "
							+ ProductPriceXInfo.PATH
							+ " "
							+ "combination already exists in ProductPrice table.");
		}
	}
}
