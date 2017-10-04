package com.trilogy.app.crm.bean.webcontrol;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.ServiceFee2N;
import com.trilogy.app.crm.bean.ui.PricingVersionXInfo;
import com.trilogy.app.crm.bean.ui.ServiceProductKeyWebControl;
import com.trilogy.app.crm.bean.ui.ServiceProductXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class CustomServiceProductKeyWebControl extends
		ServiceProductKeyWebControl {

	public CustomServiceProductKeyWebControl() {
		// TODO Auto-generated constructor stub
	}

	public CustomServiceProductKeyWebControl(boolean autoPreview) {
		super(autoPreview);
		// TODO Auto-generated constructor stub
	}

	public CustomServiceProductKeyWebControl(int listSize) {
		super(listSize);
		// TODO Auto-generated constructor stub
	}

	public CustomServiceProductKeyWebControl(int listSize, boolean autoPreview) {
		super(listSize, autoPreview);
		// TODO Auto-generated constructor stub
	}

	public CustomServiceProductKeyWebControl(int listSize,
			boolean autoPreview, Class baseClass, String sourceField,
			String targetField) {
		super(listSize, autoPreview, baseClass, sourceField, targetField);
		// TODO Auto-generated constructor stub
	}

	public CustomServiceProductKeyWebControl(int listSize,
			boolean autoPreview, boolean isOptional) {
		super(listSize, autoPreview, isOptional);
		// TODO Auto-generated constructor stub
	}

	public CustomServiceProductKeyWebControl(int listSize,
			boolean autoPreview, boolean isOptional, boolean allowCustom) {
		super(listSize, autoPreview, isOptional, allowCustom);
		// TODO Auto-generated constructor stub
	}

	public CustomServiceProductKeyWebControl(int listSize,
			boolean autoPreview, Object optionalValue) {
		super(listSize, autoPreview, optionalValue);
		// TODO Auto-generated constructor stub
	}

	public CustomServiceProductKeyWebControl(int listSize,
			boolean autoPreview, Object optionalValue, Class baseClass,
			String sourceField, String targetField) {
		super(listSize, autoPreview, optionalValue, baseClass, sourceField,
				targetField);
		// TODO Auto-generated constructor stub
	}

	public CustomServiceProductKeyWebControl(int listSize,
			boolean autoPreview, Object optionalValue, boolean allowCustom) {
		super(listSize, autoPreview, optionalValue, allowCustom);
		// TODO Auto-generated constructor stub
	}
	@Override
	public Home getHome(Context ctx) {
		Object bean = ctx.get(AbstractWebControl.BEAN);
		if (bean instanceof ServiceFee2N) {
			ServiceFee2N serviceFee2N = (ServiceFee2N)bean;
			//((ServiceFee2N)bean).setParam();
			return super.getHome(ctx).where(ctx,new And().add(new EQ(ServiceProductXInfo.PRODUCT_ID, serviceFee2N.getServiceId())));
		} else {
			return super.getHome(ctx);
		}
	}
	@Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj) {
		super.toWeb(ctx, out, name, obj);
		Object bean = ctx.get(AbstractWebControl.BEAN);
		if (bean instanceof ServiceFee2N) 
			((ServiceFee2N)bean).setParam();
	}
}
