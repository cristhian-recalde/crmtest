package com.trilogy.app.crm.inboundfile.home;

import com.trilogy.app.crm.bean.FeeAndPenalty;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * The objective of this class is to set the
 * ID of FeeAndPenalty table by using identifiersequencer
 * @author skambab
 *
 */
public class FeeAndPenaltyIDSettingHome extends HomeProxy
{
	public FeeAndPenaltyIDSettingHome(Context ctx,Home delegate)
	{
		super(ctx, delegate);
	}
	
	@Override
	public Object create(Context ctx, Object obj) throws HomeException,HomeInternalException
	{
		if((obj != null)&&(obj instanceof FeeAndPenalty))
		{
			FeeAndPenalty bean = (FeeAndPenalty)obj;
			bean.setID((int) getNextIdentifier(ctx));
			
			return super.create(ctx, bean);
		}
		return super.create(ctx, obj);
	}
	
	private long getNextIdentifier(Context ctx) throws HomeException
	{
		
		 IdentifierSequenceSupportHelper.get((Context)ctx).ensureSequenceExists(ctx, IdentifierEnum.FEEANDPENALTY_ID, 1, Long.MAX_VALUE);
		    
	     return IdentifierSequenceSupportHelper.get((Context)ctx).getNextIdentifier(ctx, IdentifierEnum.FEEANDPENALTY_ID, null);
	}
}
