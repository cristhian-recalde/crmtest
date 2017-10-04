package com.trilogy.app.crm.home.account;


import java.util.Collection;

import com.trilogy.app.crm.bean.RefundAdjustmentTypeMapping;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMappingHome;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMappingXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class RefundAdjustmenttypeMappingFieldsValidator implements Validator{

	@Override
	public void validate(Context ctx, Object obj)	throws IllegalStateException
	{
		
		RefundAdjustmentTypeMapping adjmappingBean = (RefundAdjustmentTypeMapping)obj;
		
		if(adjmappingBean!=null)
		{
			CompoundIllegalStateException cise = new CompoundIllegalStateException();
			try
			{
				
				
				String message = "Entity already created for the combination of account state " + adjmappingBean.getAccountState()+" and reason code "+ adjmappingBean.getReasonCode();
				Home home = (Home) ctx.get(RefundAdjustmentTypeMappingHome.class);
				And filter = new And();
				filter.add(new EQ(RefundAdjustmentTypeMappingXInfo.ACCOUNT_STATE,adjmappingBean.getAccountState()));
				filter.add(new EQ(RefundAdjustmentTypeMappingXInfo.REASON_CODE,adjmappingBean.getReasonCode()));
				
				Collection<RefundAdjustmentTypeMapping> col = home.select(ctx, filter);
				if(col.size()>0)
				{
					cise.thrown(new HomeException(message));
				}
					
			} catch (HomeException e)
			{
				LogSupport.major(ctx, this, "Exception occured while retrieving the data :"+e.getMessage());
				e.printStackTrace();
			}
			cise.throwAll();
		}
	}

}
