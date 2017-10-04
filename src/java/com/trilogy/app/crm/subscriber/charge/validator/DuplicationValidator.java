package com.trilogy.app.crm.subscriber.charge.validator;

import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.framework.xhome.context.Context;

public interface DuplicationValidator 
{
	public int validate(Context ctx, ChargableItemResult ret) 
	throws Exception; 
}
