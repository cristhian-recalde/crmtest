package com.trilogy.app.crm.home;

import com.trilogy.app.crm.DataModelAdaptionSupport;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.ServiceProductXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class ServiceProductToServiceAdapterHome extends HomeProxy {

	public ServiceProductToServiceAdapterHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeInternalException, HomeException {
		Object ret =  super.create(ctx,obj);
		ServiceProduct version = (ServiceProduct)ret;
		DataModelAdaptionSupport.createServiceFromServiceProduct(ctx, version);
		return ret;
	}
	
	@Override
	public Object store(Context ctx, Object obj) throws HomeException,	HomeInternalException {
		Object ret =  super.store(ctx, obj);
		ServiceProduct version = (ServiceProduct)ret;
		DataModelAdaptionSupport.updateServiceFromServiceProduct(ctx, version);
		return ret;
	}
	
	@Override
	public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException {
		super.remove(ctx, obj);
		ServiceProduct version = (ServiceProduct)obj;
		DataModelAdaptionSupport.updateServiceOnServiceProductRemove(ctx, version);
	}
}
