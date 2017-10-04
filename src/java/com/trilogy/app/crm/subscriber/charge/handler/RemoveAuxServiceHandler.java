package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class RemoveAuxServiceHandler 
extends GenericHandler
{
    
    public RemoveAuxServiceHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    public RemoveAuxServiceHandler()
    {
        
    }
    
     public void handleError(Context ctx, ChargableItemResult ret)
    {
        super.handleError(ctx, ret); 
        SubscriberAuxiliaryService subAuxService = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServicesBySubIdAndSvcId(ctx, 
                ret.getSubscriber().getId(), ret.getId()); 
        if (subAuxService != null)
        {
            try
            {
                SubscriberAuxiliaryServiceSupport.removeSubscriberAuxiliaryService(ctx, subAuxService);
            }
            catch (HomeException homeEx)
            {
                new MinorLogMsg(this, "Unable to remove subscriber auxiliary service " + subAuxService, homeEx).log(ctx);
            }
        }
    }
 
}
