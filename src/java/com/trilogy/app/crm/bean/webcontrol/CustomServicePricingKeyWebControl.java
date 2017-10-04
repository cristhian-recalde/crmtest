package com.trilogy.app.crm.bean.webcontrol;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;

import com.trilogy.app.crm.bean.PriceList;
import com.trilogy.app.crm.bean.ui.PricingVersion;
import com.trilogy.app.crm.bean.ui.PricingVersionHome;
import com.trilogy.app.crm.bean.ui.PricingVersionXInfo;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.ServiceProductHome;
import com.trilogy.app.crm.bean.ui.ServiceProductXInfo;
import com.trilogy.app.crm.bean.ui.ServicePricingKeyWebControl;
import com.trilogy.app.crm.bean.ui.ServicePricingXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class CustomServicePricingKeyWebControl extends
		ServicePricingKeyWebControl {

	public CustomServicePricingKeyWebControl() {
		// TODO Auto-generated constructor stub
	}

	public CustomServicePricingKeyWebControl(boolean autoPreview) {
		super(autoPreview);
		// TODO Auto-generated constructor stub
	}

	public CustomServicePricingKeyWebControl(int listSize) {
		super(listSize);
		// TODO Auto-generated constructor stub
	}

	public CustomServicePricingKeyWebControl(int listSize, boolean autoPreview) {
		super(listSize, autoPreview);
		// TODO Auto-generated constructor stub
	}

	public CustomServicePricingKeyWebControl(int listSize, boolean autoPreview,
			Class baseClass, String sourceField, String targetField) {
		super(listSize, autoPreview, baseClass, sourceField, targetField);
		// TODO Auto-generated constructor stub
	}

	public CustomServicePricingKeyWebControl(int listSize, boolean autoPreview,
			boolean isOptional) {
		super(listSize, autoPreview, isOptional);
		// TODO Auto-generated constructor stub
	}

	public CustomServicePricingKeyWebControl(int listSize, boolean autoPreview,
			boolean isOptional, boolean allowCustom) {
		super(listSize, autoPreview, isOptional, allowCustom);
		// TODO Auto-generated constructor stub
	}

	public CustomServicePricingKeyWebControl(int listSize, boolean autoPreview,
			Object optionalValue) {
		super(listSize, autoPreview, optionalValue);
		// TODO Auto-generated constructor stub
	}

	public CustomServicePricingKeyWebControl(int listSize, boolean autoPreview,
			Object optionalValue, Class baseClass, String sourceField,
			String targetField) {
		super(listSize, autoPreview, optionalValue, baseClass, sourceField,
				targetField);
		// TODO Auto-generated constructor stub
	}

	public CustomServicePricingKeyWebControl(int listSize, boolean autoPreview,
			Object optionalValue, boolean allowCustom) {
		super(listSize, autoPreview, optionalValue, allowCustom);
		// TODO Auto-generated constructor stub
	}

	/*@Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj) {
       int mode = ctx.getInt("MODE", DISPLAY_MODE);
	   if (mode == DISPLAY_MODE)
	   {
		Object bean = ctx.get(AbstractWebControl.BEAN);
		Long id =  ctx.get("SERVICEID")!=null?(Long)ctx.get("SERVICEID"):null;
		Long versionId = ctx.get("SERVICEVERSIONID")!=null?(Long)ctx.get("SERVICEVERSIONID"):null;
		long compatibilityGroupId = -1;
		if (bean instanceof PriceList && id!=null && versionId!=null) {
			HashSet<Long> priceIdSet = new HashSet<Long>();
			Home serviceVersionHome = (Home) ctx.get(ServiceProductnHome.class);
			And and = new And();
			and.add(new EQ(ServiceProductXInfo.ID,id));
			and.add(new EQ(ServiceProductXInfo.VERSION_ID,versionId));
			try {
				ServiceProduct serviceNP = (ServiceProduct) serviceVersionHome.find(ctx, and);
				compatibilityGroupId = serviceNPn!=null?serviceNP.getCompatibilityGroup():compatibilityGroupId;
				Home pricingVersionHome = (Home) ctx.get(PricingVersionHome.class);
				Collection<PricingVersion> versions = pricingVersionHome.select(ctx, new EQ(PricingVersionXInfo.COMPATIBILITY_GROUP,compatibilityGroupId));
				for(PricingVersion version:versions)
					priceIdSet.add(version.getId());
			} catch (Exception e) {
				e.printStackTrace();
			}			
			super.toWeb(ctx, out, name,new In(ServicePricingXInfo.ID, priceIdSet));
		} else 
			super.toWeb(ctx, out, name, obj);
		}else
			super.toWeb(ctx, out, name, obj);
	}*/
	@Override
	public Home getHome(Context ctx) {
		Object bean = ctx.get(AbstractWebControl.BEAN);
		Long id =  ctx.get("SERVICEID")!=null?(Long)ctx.get("SERVICEID"):null;
		Long versionId = ctx.get("SERVICEVERSIONID")!=null?(Long)ctx.get("SERVICEVERSIONID"):null;
		long compatibilityGroupId = -1;
		if (bean instanceof PriceList && id!=null && versionId!=null) {
			HashSet<Long> priceIdSet = new HashSet<Long>();
			Home serviceVersionHome = (Home) ctx.get(ServiceProductHome.class);
			And and = new And();
			and.add(new EQ(ServiceProductXInfo.PRODUCT_ID,id));
			and.add(new EQ(ServiceProductXInfo.PRODUCT_VERSION_ID,versionId));
			try {
				ServiceProduct serviceNP = (ServiceProduct) serviceVersionHome.find(ctx, and);
				compatibilityGroupId = serviceNP!=null?serviceNP.getCompatibilityGroup():compatibilityGroupId;
				Home pricingVersionHome = (Home) ctx.get(PricingVersionHome.class);
				Collection<PricingVersion> versions = pricingVersionHome.select(ctx, new EQ(PricingVersionXInfo.COMPATIBILITY_GROUP,compatibilityGroupId));
				for(PricingVersion version:versions)
					priceIdSet.add(version.getId());
			} catch (Exception e) {
				e.printStackTrace();
			}			
			return super.getHome(ctx).where(ctx,new In(ServicePricingXInfo.ID, priceIdSet));
		} else {
			return super.getHome(ctx);
		}
	}
	
}
