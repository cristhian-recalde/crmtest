package com.trilogy.app.crm.bean.webcontrol;

import java.util.Collection;
import java.util.HashSet;

import com.trilogy.app.crm.bean.ui.CompatibilityGroupKeyWebControl;
import com.trilogy.app.crm.bean.ui.CompatibilityGroupXInfo;
import com.trilogy.app.crm.bean.ui.PricingVersion;
import com.trilogy.app.crm.bean.ui.PricingVersionXDBHome;
import com.trilogy.app.crm.bean.ui.PricingVersionXInfo;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.ServiceProductXDBHome;
import com.trilogy.app.crm.bean.ui.ServiceProductXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class CustomCompatibilityGroupKeyWebControl extends
		CompatibilityGroupKeyWebControl {

	public CustomCompatibilityGroupKeyWebControl() {
		// TODO Auto-generated constructor stub
	}

	public CustomCompatibilityGroupKeyWebControl(boolean autoPreview) {
		super(autoPreview);
		// TODO Auto-generated constructor stub
	}

	public CustomCompatibilityGroupKeyWebControl(int listSize) {
		super(listSize);
		// TODO Auto-generated constructor stub
	}

	public CustomCompatibilityGroupKeyWebControl(int listSize,
			boolean autoPreview) {
		super(listSize, autoPreview);
		// TODO Auto-generated constructor stub
	}

	public CustomCompatibilityGroupKeyWebControl(int listSize,
			boolean autoPreview, Class baseClass, String sourceField,
			String targetField) {
		super(listSize, autoPreview, baseClass, sourceField, targetField);
		// TODO Auto-generated constructor stub
	}

	public CustomCompatibilityGroupKeyWebControl(int listSize,
			boolean autoPreview, boolean isOptional) {
		super(listSize, autoPreview, isOptional);
		// TODO Auto-generated constructor stub
	}

	public CustomCompatibilityGroupKeyWebControl(int listSize,
			boolean autoPreview, boolean isOptional, boolean allowCustom) {
		super(listSize, autoPreview, isOptional, allowCustom);
		// TODO Auto-generated constructor stub
	}

	public CustomCompatibilityGroupKeyWebControl(int listSize,
			boolean autoPreview, Object optionalValue) {
		super(listSize, autoPreview, optionalValue);
		// TODO Auto-generated constructor stub
	}

	public CustomCompatibilityGroupKeyWebControl(int listSize,
			boolean autoPreview, Object optionalValue, Class baseClass,
			String sourceField, String targetField) {
		super(listSize, autoPreview, optionalValue, baseClass, sourceField,
				targetField);
		// TODO Auto-generated constructor stub
	}

	public CustomCompatibilityGroupKeyWebControl(int listSize,
			boolean autoPreview, Object optionalValue, boolean allowCustom) {
		super(listSize, autoPreview, optionalValue, allowCustom);
		// TODO Auto-generated constructor stub
	}
	@Override
	public Home getHome(Context ctx) {
		int mode = ctx.getInt("MODE", DISPLAY_MODE);
		if (mode != DISPLAY_MODE){
			Object bean = ctx.get(AbstractWebControl.BEAN);
			HashSet<Long> compatibilitySet = new HashSet<Long>();
			if (bean instanceof ServiceProduct) {
				ServiceProduct version = (ServiceProduct) bean;
				Home serviceVersionHome = (Home) ctx
						.get(ServiceProductXDBHome.class);
				try {
					Collection<ServiceProduct> serviceVersions = serviceVersionHome.selectAll();
					// Include the saved compatibility group
					ServiceProduct currentDBVersion = (ServiceProduct) serviceVersionHome.find(ctx, new And().add(new EQ(ServiceProductXInfo.PRODUCT_ID,version.getProductId()))
							.add(new EQ(ServiceProductXInfo.PRODUCT_VERSION_ID,version.getProductVersionID())));
					
					for (ServiceProduct serviceVersion : serviceVersions)
						compatibilitySet.add(serviceVersion.getCompatibilityGroup());
					
					compatibilitySet.remove(currentDBVersion.getCompatibilityGroup());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				return super.getHome(ctx).where(
						ctx,
						new Not(new In(CompatibilityGroupXInfo.ID,
								compatibilitySet)));
			} else if (bean instanceof PricingVersion) {

				PricingVersion version = (PricingVersion) bean;
				Home home = (Home) ctx.get(PricingVersionXDBHome.class);
				try {
					Collection<PricingVersion> priceVersions = home.selectAll();
					
					PricingVersion dbPriceVersion = (PricingVersion) home.find(ctx, new And().add(new EQ(PricingVersionXInfo.ID,version.getId()))
							.add(new EQ(PricingVersionXInfo.VERSION_ID,version.getVersionId())));
					
					for (PricingVersion priceVersion : priceVersions)
						compatibilitySet.add(priceVersion.getCompatibilityGroup());
					
					compatibilitySet.remove(dbPriceVersion.getCompatibilityGroup());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				return super.getHome(ctx).where(
						ctx,
						new Not(new In(CompatibilityGroupXInfo.ID,
								compatibilitySet)));
			} else {
				return super.getHome(ctx);
			}
		}else {
			return super.getHome(ctx);
		}
	
	}

}
