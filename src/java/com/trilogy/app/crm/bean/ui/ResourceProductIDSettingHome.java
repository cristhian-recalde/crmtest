package com.trilogy.app.crm.bean.ui;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class ResourceProductIDSettingHome extends HomeProxy {

	private static final long serialVersionUID = 1L;

	public ResourceProductIDSettingHome(Context ctx, Home delegate)
	{
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
	HomeInternalException 
	{

		ResourceProduct resourceProduct = (ResourceProduct)obj;
		long versionId = getNextIdentifier(ctx);
		if(resourceProduct.getProductVersionID() == 0)
		{
			resourceProduct.setProductVersionID(versionId);
		}

		LogSupport.info(ctx, this, "Package Product ProductVersionId set to: " + resourceProduct.getProductVersionID());

		return super.create(ctx, obj);
	}

	private long getNextIdentifier(Context ctx) throws HomeException
	{
		IdentifierSequenceSupportHelper.get((Context)ctx).ensureSequenceExists(ctx, IdentifierEnum.RESOURCEPRODUCT_ID, 1, Long.MAX_VALUE);
		return IdentifierSequenceSupportHelper .get((Context) ctx).getNextIdentifier(ctx, IdentifierEnum.RESOURCEPRODUCT_ID, null);
	}

}
