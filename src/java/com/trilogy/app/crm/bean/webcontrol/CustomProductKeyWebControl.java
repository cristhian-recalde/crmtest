package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.ServiceCategoryXInfo;
import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ProductKeyWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class CustomProductKeyWebControl extends ProductKeyWebControl{
	
	public CustomProductKeyWebControl() {
		// TODO Auto-generated constructor stub
	}

	public CustomProductKeyWebControl(boolean autoPreview) {
		super(autoPreview);
		// TODO Auto-generated constructor stub
	}

	public CustomProductKeyWebControl(int listSize) {
		super(listSize);
		// TODO Auto-generated constructor stub
	}

	public CustomProductKeyWebControl(int listSize, boolean autoPreview) {
		super(listSize, autoPreview);
		// TODO Auto-generated constructor stub
	}

	public CustomProductKeyWebControl(int listSize, boolean autoPreview,
			Class baseClass, String sourceField, String targetField) {
		super(listSize, autoPreview, baseClass, sourceField, targetField);
		// TODO Auto-generated constructor stub
	}

	public CustomProductKeyWebControl(int listSize, boolean autoPreview,
			boolean isOptional) {
		super(listSize, autoPreview, isOptional);
		// TODO Auto-generated constructor stub
	}

	public CustomProductKeyWebControl(int listSize, boolean autoPreview,
			boolean isOptional, boolean allowCustom) {
		super(listSize, autoPreview, isOptional, allowCustom);
		// TODO Auto-generated constructor stub
	}

	public CustomProductKeyWebControl(int listSize, boolean autoPreview,
			Object optionalValue) {
		super(listSize, autoPreview, optionalValue);
		// TODO Auto-generated constructor stub
	}

	public CustomProductKeyWebControl(int listSize, boolean autoPreview,
			Object optionalValue, Class baseClass, String sourceField,
			String targetField) {
		super(listSize, autoPreview, optionalValue, baseClass, sourceField,
				targetField);
		// TODO Auto-generated constructor stub
	}

	public CustomProductKeyWebControl(int listSize, boolean autoPreview,
			Object optionalValue, boolean allowCustom) {
		super(listSize, autoPreview, optionalValue, allowCustom);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Home getHome(Context ctx) {
		Object bean = ctx.get(AbstractWebControl.BEAN);
		int spid = 0;
		/*if (bean instanceof ServiceN) {
			spid = ((ServiceN) bean).getSpid();

			if (spid > 0) {
				return super.getHome(ctx).where(ctx,
						new And().add(new EQ(ServiceCategoryXInfo.SPID, spid)));
			}
		}*/
		return super.getHome(ctx);
	}
}
