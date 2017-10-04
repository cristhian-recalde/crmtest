package com.trilogy.app.crm.inboundfile.home;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.PaymentFTRecords;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class PaymentFileRecordTrackerIDSettingHome extends HomeProxy{

	public PaymentFileRecordTrackerIDSettingHome(Context ctx,Home delegate)
	{
		super(ctx, delegate);
	}
	public Object create(Context ctx, Object obj) throws HomeException,HomeInternalException
	{
		if((obj != null)&&(obj instanceof PaymentFTRecords))
		{
			PaymentFTRecords bean = (PaymentFTRecords) obj;
			bean.setID((int) getNextIdentifier(ctx));
			return super.create(ctx, bean);
		}
		return super.create(ctx, obj);
	}
	private long getNextIdentifier(Context ctx) throws HomeException
	{
		
		 IdentifierSequenceSupportHelper.get((Context)ctx).ensureSequenceExists(ctx, IdentifierEnum.PAYMENT_FT_ID, 1, Long.MAX_VALUE);
		    
	     return IdentifierSequenceSupportHelper.get((Context)ctx).getNextIdentifier(ctx, IdentifierEnum.PAYMENT_FT_ID, null);
	}
}
