package com.trilogy.app.crm.bean.ui;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class PackageProductIDSettingHome extends HomeProxy {

	private static final long serialVersionUID = 1L;

	public PackageProductIDSettingHome(Context ctx, Home delegate)
	{
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
	HomeInternalException 
	{

		PackageProduct packageProduct = (PackageProduct)obj;
		long versionId = getNextIdentifier(ctx);
		if(packageProduct.getProductVersionID() == 0)
		{
			packageProduct.setProductVersionID(versionId);
		}

		LogSupport.info(ctx, this, "Package Product ProductVersionId set to: " + packageProduct.getProductVersionID());

		return super.create(ctx, obj);
	}

	private long getNextIdentifier(Context ctx) throws HomeException
	{
		IdentifierSequenceSupportHelper.get((Context)ctx).ensureSequenceExists(ctx, IdentifierEnum.PACKAGEPRODUCT_ID, 1, Long.MAX_VALUE);
		return IdentifierSequenceSupportHelper .get((Context) ctx).getNextIdentifier(ctx, IdentifierEnum.PACKAGEPRODUCT_ID, null);
	}

}
