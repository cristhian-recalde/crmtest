package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.provision.SuspendEntityActionSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class SuspendingAuxServiceHandler
extends GenericHandler
{
    
    public SuspendingAuxServiceHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    public SuspendingAuxServiceHandler()
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
              suspendAuxService(ctx, ret.getSubscriber(), 
                    (AuxiliaryService)ret.getChargableObject());
            }   
        }
    }
    
    private int suspendAuxService(Context ctx, Subscriber sub, AuxiliaryService fee)
    {
        int ret = ERROR_HANDLE_RESULT_SUCCESS; 
        try{
            if (SuspendEntityActionSupport.suspendAuxService(ctx, sub, fee, this) != 
                SuspendEntityActionSupport.SUCCESS )
            {
                ret = ERROR_HANDLE_RESULT_FAIL_SUSPEND;    
            }
        } catch (Exception e)
        {
            new MinorLogMsg(this, "fail to suspend auxiliary service " + fee.getID() + 
                    "  for prepaid subscriber " +
                    sub.getId(),e).log(ctx);
            ret = ERROR_HANDLE_RESULT_FAIL_SUSPEND;
        }

        return ret; 
    }
}
