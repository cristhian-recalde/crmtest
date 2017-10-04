package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.framework.xhome.context.Context;

public interface ChargeRefundResultHandler 
{
    public void handleTransaction(Context ctx, ChargableItemResult ret);
    public void setDelegate(ChargeRefundResultHandler handler);
    
}
