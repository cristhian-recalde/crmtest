package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.provision.SuspendEntityActionSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class SuspendingServiceHandler
extends GenericHandler
{
    
    public SuspendingServiceHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    public SuspendingServiceHandler()
    {
        
    }

    public void handleError(Context ctx, ChargableItemResult ret)
    {
        //if (!GenericHandler.isItemMarkedForProvisioningRestriction(ret))
        {

            switch( ret.getChargeResult())
            {
            case TRANSACTION_FAIL_OCG:
            case  TRANSACTION_SKIPPED_SUSPEND: 
              suspendService(ctx, ret.getSubscriber(), 
                    (ServiceFee2)ret.getChargableObject());
            }   
        }
    }
    
    private int suspendService(Context ctx, Subscriber sub, ServiceFee2 fee)
    {
        int ret = ERROR_HANDLE_RESULT_SUCCESS; 
        try{
            if (SuspendEntityActionSupport.suspendService(ctx, sub, fee) != 
                SuspendEntityActionSupport.SUCCESS )
            {
                ret = ERROR_HANDLE_RESULT_FAIL_SUSPEND;    
            }
        } catch (Exception e)
        {
            new MinorLogMsg(this, "fail to suspend service " + fee.getServiceId() + 
                    "  for prepaid subscriber " +
                    sub.getId(),e).log(ctx);
            ret = ERROR_HANDLE_RESULT_FAIL_SUSPEND;
        }

        return ret; 
    }
}
