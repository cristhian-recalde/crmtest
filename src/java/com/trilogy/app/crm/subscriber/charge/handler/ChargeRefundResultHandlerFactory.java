package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.framework.xhome.context.Context;

public interface ChargeRefundResultHandlerFactory 
{
    public ChargeRefundResultHandler create(Context ctx, ChargableItemResult source); 
        
}
