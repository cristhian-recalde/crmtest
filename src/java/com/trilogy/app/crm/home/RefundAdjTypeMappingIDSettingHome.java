package com.trilogy.app.crm.home;


import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMapping;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class RefundAdjTypeMappingIDSettingHome extends HomeProxy{

	public RefundAdjTypeMappingIDSettingHome(Context ctx, Home delegate)
	{
		super(ctx, delegate);
	}
	
	public Object create(Context ctx, Object obj) throws HomeException,
	HomeInternalException
	{
		//AccountReasonCodeMapping arcmBean = (AccountReasonCodeMapping)obj;
		RefundAdjustmentTypeMapping ratmBean = (RefundAdjustmentTypeMapping)obj;
		
		if((ratmBean!=null)&&(ratmBean.getID()==0))
		{
			ratmBean.setID((int) getNextIdentifier(ctx));
		}
		
		return super.create(ctx, ratmBean);
	}
	 private long getNextIdentifier(Context ctx) throws HomeException
	 {
		 IdentifierSequenceSupportHelper.get((Context)ctx).ensureSequenceExists(ctx, IdentifierEnum.REFUNMAPPING_ID, 1, Long.MAX_VALUE);
		    
	        return IdentifierSequenceSupportHelper.get((Context)ctx).getNextIdentifier(ctx, IdentifierEnum.REFUNMAPPING_ID, null);
	 }

}
