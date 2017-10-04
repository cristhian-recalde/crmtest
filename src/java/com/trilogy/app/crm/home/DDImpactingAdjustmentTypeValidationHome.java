package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.DDImpactingAdjustmentType;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class DDImpactingAdjustmentTypeValidationHome extends HomeProxy{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DDImpactingAdjustmentTypeValidationHome(Context ctx,Home home){
		super(home);
	}
	
	@Override
    public Object create(Context ctx, Object obj) throws HomeException
    {
		
		DDImpactingAdjustmentType adjTypObj=(DDImpactingAdjustmentType)obj;
		if ((adjTypObj.getAdjustmentType()).size() ==0 || (adjTypObj.getAdjustmentType()) == null)
		{
			throw new HomeException("Atleast One Adjustment Type Should be Added");
		}
		return super.create(ctx, adjTypObj);
		
    }
	
	@Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
		
		DDImpactingAdjustmentType adjTypObj=(DDImpactingAdjustmentType)obj;
		if ((adjTypObj.getAdjustmentType()).size() ==0 || (adjTypObj.getAdjustmentType()) == null)
		{
			throw new HomeException("Atleast One Adjustment Type Should be Added");
		}
		return super.store(ctx, adjTypObj);
		
    }
	
	

}
