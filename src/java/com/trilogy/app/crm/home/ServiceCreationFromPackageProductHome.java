package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.ui.PackageProduct;
import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.bean.ui.ServiceHome;
import com.trilogy.app.crm.integration.pc.PackageProductToServiceAdapter;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;


public class ServiceCreationFromPackageProductHome extends HomeProxy {

	private static final long serialVersionUID = 1L;

	public ServiceCreationFromPackageProductHome(Context ctx, Home serviceHome) {

		super(ctx, serviceHome);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
	HomeInternalException {
		PackageProduct packageProduct = (PackageProduct)obj;
		super.create(ctx, packageProduct);
		if(packageProduct!=null){
			try{
				Home serviceHome = (Home) ctx.get(ServiceHome.class);
				Service service = (Service) serviceHome.find(ctx, packageProduct.getProductId());
				if(service == null){
					HomeSupportHelper.get(getContext()).createBean(getContext(), PackageProductToServiceAdapter.adaptService(ctx,packageProduct));
					LogSupport.info(ctx, this,"Adapt and store Service Successfully ");
				}
				else{
					LogSupport.debug(ctx, this,"Service with ID [" + packageProduct.getProductId() + "] exists. Will not be created");
				}
			}catch(Exception e){
				LogSupport.minor(ctx, this,"Error in Service Creation: "+e,e);
			}
		}
		return packageProduct;
	}

}
