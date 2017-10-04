package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.bean.ui.ServiceHome;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class ServiceCreationHome extends HomeProxy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceCreationHome(Context ctx, Home serviceHome) {
		super(ctx, serviceHome);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
	HomeInternalException {
		ServiceProduct serviceProduct = (ServiceProduct)obj;
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, this, "[ServiceCreationHome.create] Create Service product with ID:"+String.valueOf(serviceProduct.getProductId()));
		}
		super.create(ctx, serviceProduct);
		if(serviceProduct!=null){
			try{
				Home serviceHome = (Home) ctx.get(ServiceHome.class);
				Service service = (Service) serviceHome.find(ctx, serviceProduct.getProductId());
				if(service == null){
					HomeSupportHelper.get(getContext()).createBean(getContext(), ServiceProductToServiceAdapter.adaptService(ctx,serviceProduct));
					LogSupport.info(ctx, this,"[ServiceCreationHome.create] Adapt and store Service Successfully ");
				}else{
					LogSupport.info(ctx, this,"[ServiceCreationHome.create] Service with ID (" + serviceProduct.getProductId() + ") exists. Will not be created");
				}
			}catch(Exception e){
				LogSupport.minor(ctx, this,"Error in Service Creation: "+e,e);
			}
		}
		return serviceProduct;
	}

}
