package com.trilogy.app.crm.home;

import com.trilogy.app.crm.DataModelAdaptionSupport;
import com.trilogy.app.crm.bean.ui.PricingVersion;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.ServicePricing;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class ServicePricingVersionToServiceUpdateHome extends HomeProxy {

	public ServicePricingVersionToServiceUpdateHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,	HomeInternalException {
		Object ret = super.create(ctx, obj);
		PricingVersion pricing = (PricingVersion)ret;
		ServiceProduct nVersion = DataModelAdaptionSupport.getServiceProductFromCompatibilityGroup(ctx, pricing.getCompatibilityGroup());
		
		if(nVersion!=null) // if there is a service version associated with this pricing version then update
			DataModelAdaptionSupport.createServiceFromServiceProduct(ctx, nVersion);
		return ret;
	}
	
	@Override
	public Object store(Context ctx, Object obj) throws HomeException,HomeInternalException {
		long oldCompatibilityGroup = DataModelAdaptionSupport.getCompatibilityGroupOnPricingVersion(ctx,((PricingVersion)obj).getVersionId());
		
		Object ret = super.store(ctx, obj);
		
		PricingVersion pricing = (PricingVersion)ret;
		ServiceProduct nVersion = DataModelAdaptionSupport.getServiceProductFromCompatibilityGroup(ctx, pricing.getCompatibilityGroup());
		 
		// if there is a service version associated with this pricing version then update
		if(nVersion!=null)
			DataModelAdaptionSupport.updateServiceFromServiceProduct(ctx, nVersion);
		// remove the entry for old compatibility group
		if(oldCompatibilityGroup!=pricing.getCompatibilityGroup()){
			ServiceProduct oldVersion = DataModelAdaptionSupport.getServiceProductFromCompatibilityGroup(ctx, oldCompatibilityGroup);
			if(oldVersion!=null) // if there is a service version associated with this pricing version then update
				DataModelAdaptionSupport.updateServiceOnServiceProductRemove(ctx, oldVersion);
		}
		return ret;
	}
	
	@Override
	public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException {
		super.remove(ctx, obj);
		PricingVersion pricing = (PricingVersion)obj;
		ServiceProduct nVersion = DataModelAdaptionSupport.getServiceProductFromCompatibilityGroup(ctx, pricing.getCompatibilityGroup());
		
		if(nVersion!=null) // if there is a service version associated with this pricing version then update
			DataModelAdaptionSupport.updateServiceOnServiceProductRemove(ctx, nVersion);
	}
}
