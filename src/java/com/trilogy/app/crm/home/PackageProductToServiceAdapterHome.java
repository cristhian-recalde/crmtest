package com.trilogy.app.crm.home;

import com.trilogy.app.crm.DataModelAdaptionSupport;
import com.trilogy.app.crm.bean.ui.PackageProduct;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * 
 * @author AChatterjee
 *
 */
public class PackageProductToServiceAdapterHome extends HomeProxy {

	private static final long serialVersionUID = 1L;
	
	public PackageProductToServiceAdapterHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeInternalException, HomeException {
		Object ret =  super.create(ctx,obj);
		PackageProduct packageProduct = (PackageProduct)ret;
		DataModelAdaptionSupport.createServiceFromPackageProduct(ctx, packageProduct);
		return ret;
	}
	
	@Override
	public Object store(Context ctx, Object obj) throws HomeException,	HomeInternalException {
		Object ret =  super.store(ctx, obj);
		PackageProduct packageProduct = (PackageProduct)ret;
		DataModelAdaptionSupport.updateServiceFromPackageProduct(ctx, packageProduct);
		return ret;
	}
	
	@Override
	public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException {
		super.remove(ctx, obj);
		PackageProduct packageProduct = (PackageProduct)obj;
		DataModelAdaptionSupport.updateServiceOnPackageProductRemove(ctx, packageProduct);
	}

}
