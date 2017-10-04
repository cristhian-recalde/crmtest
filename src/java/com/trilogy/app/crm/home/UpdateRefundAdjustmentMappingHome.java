package com.trilogy.app.crm.home;

import java.util.Collection;

import com.trilogy.app.crm.bean.RefundAdjustmentTypeMapping;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMappingHome;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMappingXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class UpdateRefundAdjustmentMappingHome extends HomeProxy{

	 public UpdateRefundAdjustmentMappingHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}
	

	@Override
	public Object store(Context ctx, Object obj) throws HomeException,HomeInternalException
	{
		if((obj instanceof RefundAdjustmentTypeMapping) && (obj !=null))
		{
			RefundAdjustmentTypeMapping ratmBean = (RefundAdjustmentTypeMapping)obj;
			Home home = (Home) ctx.get(RefundAdjustmentTypeMappingHome.class);
			And filter = new And();
			filter.add(new EQ(RefundAdjustmentTypeMappingXInfo.SPID,ratmBean.getSpid()));
			filter.add(new EQ(RefundAdjustmentTypeMappingXInfo.ACCOUNT_STATE,ratmBean.getAccountState()));
			filter.add(new EQ(RefundAdjustmentTypeMappingXInfo.REASON_CODE,ratmBean.getReasonCode()));
			Collection<RefundAdjustmentTypeMapping> col = home.select(ctx, filter);
			if(col.size()>0)
			{
				for(RefundAdjustmentTypeMapping currBean : col)
				{
					if((currBean.getID()==ratmBean.getID()))
					{
						super.store(ctx, ratmBean);
					}else
					{
						throw new HomeException("Reason Code already exists please select other reasoncode");
					}
				}
			}else
			{
				return super.store(ctx, ratmBean);
			}	
				
		}
		return super.store(ctx, obj);
	}
}
