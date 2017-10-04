package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.support.ChargeRefundResultHandlerSupport;
import com.trilogy.framework.xhome.context.Context;

public class SubscriberHistoryHandler extends GenericHandler
{
    
    public SubscriberHistoryHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    public SubscriberHistoryHandler()
    {
        
    }
     
    public void handleError(Context ctx,  ChargableItemResult ret)
    {
    }
    
    
    public void handleSuccess(Context ctx,  ChargableItemResult ret)
    {
    	ChargeRefundResultHandlerSupport.createSubscriptionHistory(ctx, ret); 
    }
    
    

}
    