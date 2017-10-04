package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.PriceList;
import com.trilogy.app.crm.bean.ui.PricingVersionKeyWebControl;
import com.trilogy.app.crm.bean.ui.PricingVersionXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class CustomPricingVersionKeyWebControl extends
		PricingVersionKeyWebControl {

	public CustomPricingVersionKeyWebControl() {
		// TODO Auto-generated constructor stub
	}

	public CustomPricingVersionKeyWebControl(boolean autoPreview) {
		super(autoPreview);
		// TODO Auto-generated constructor stub
	}

	public CustomPricingVersionKeyWebControl(int listSize) {
		super(listSize);
		// TODO Auto-generated constructor stub
	}

	public CustomPricingVersionKeyWebControl(int listSize, boolean autoPreview) {
		super(listSize, autoPreview);
		// TODO Auto-generated constructor stub
	}

	public CustomPricingVersionKeyWebControl(int listSize, boolean autoPreview,
			boolean isOptional) {
		super(listSize, autoPreview, isOptional);
		// TODO Auto-generated constructor stub
	}

	public CustomPricingVersionKeyWebControl(int listSize, boolean autoPreview,
			Object optionalValue) {
		super(listSize, autoPreview, optionalValue);
		// TODO Auto-generated constructor stub
	}

	public CustomPricingVersionKeyWebControl(int listSize, boolean autoPreview,
			boolean isOptional, boolean allowCustom) {
		super(listSize, autoPreview, isOptional, allowCustom);
		// TODO Auto-generated constructor stub
	}

	public CustomPricingVersionKeyWebControl(int listSize, boolean autoPreview,
			Object optionalValue, boolean allowCustom) {
		super(listSize, autoPreview, optionalValue, allowCustom);
		// TODO Auto-generated constructor stub
	}

	public CustomPricingVersionKeyWebControl(int listSize, boolean autoPreview,
			Class baseClass, String sourceField, String targetField) {
		super(listSize, autoPreview, baseClass, sourceField, targetField);
		// TODO Auto-generated constructor stub
	}

	public CustomPricingVersionKeyWebControl(int listSize, boolean autoPreview,
			Object optionalValue, Class baseClass, String sourceField,
			String targetField) {
		super(listSize, autoPreview, optionalValue, baseClass, sourceField,
				targetField);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Home getHome(Context ctx) {
		Object bean = ctx.get(AbstractWebControl.BEAN);
		if (bean instanceof PriceList) {
			PriceList list = (PriceList)bean;
			return super.getHome(ctx).where(ctx,new And().add(new EQ(PricingVersionXInfo.ID, list.getPriceId())));
		} else {
			return super.getHome(ctx);
		}
	}

}
