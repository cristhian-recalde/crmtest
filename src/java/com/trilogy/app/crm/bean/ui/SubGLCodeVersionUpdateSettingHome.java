package com.trilogy.app.crm.bean.ui;

import java.util.List;

import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class SubGLCodeVersionUpdateSettingHome extends HomeProxy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SubGLCodeVersionUpdateSettingHome(Context ctx, Home home) {
		super(ctx, home);
	}
	
	@Override
	public Object store(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		
		super.store(ctx, obj);
		SubGLCodeVersionN subGLCodeVersion = (SubGLCodeVersionN)obj;
		List<AdjustmentTypeNVersion> adjustmentTypeNVersionList = getAdjustmentTypeNVersion(ctx,subGLCodeVersion);
		updateOldAdjustmentType(ctx,adjustmentTypeNVersionList);
		return obj;
	}

	private void updateOldAdjustmentType(
			Context ctx, List<AdjustmentTypeNVersion> adjustmentTypeNVersionList) throws HomeInternalException, HomeException {
		for(AdjustmentTypeNVersion adjustmentTypeNVersion:adjustmentTypeNVersionList){
			Home  adjustmentTypeNVersionHome =  (Home) ctx.get(AdjustmentTypeNVersionHome.class);
			adjustmentTypeNVersionHome.store(ctx,adjustmentTypeNVersion);
		}
	}

	private List<AdjustmentTypeNVersion> getAdjustmentTypeNVersion(Context ctx,
			SubGLCodeVersionN subGLCodeVersion) {
		List<AdjustmentTypeNVersion> adjustmentTypeNVersionList = null;
		try{
			And filter = new And();
			filter.add(new EQ(AdjustmentTypeNVersionXInfo.SUB_GLCODE_VID, subGLCodeVersion.getVersionId()));
			adjustmentTypeNVersionList =  (List<AdjustmentTypeNVersion>) HomeSupportHelper.get(ctx).getBeans(ctx, AdjustmentTypeNVersion.class,filter);
		   }catch (HomeException e) {
		   LogSupport.minor(ctx, this,"Error:geting AdjustmentTypeNVersion"+ e,e);			
		}
		return adjustmentTypeNVersionList;
	}
	
}