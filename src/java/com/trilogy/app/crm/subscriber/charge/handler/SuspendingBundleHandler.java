package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.provision.SuspendEntityActionSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class SuspendingBundleHandler
extends GenericHandler
{
    public SuspendingBundleHandler(ChargeRefundResultHandler handler)
    {
        super(handler); 
    }

    public SuspendingBundleHandler()
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
                 suspendBundle(ctx, ret.getSubscriber(), 
                         (BundleFee)ret.getChargableObject());
             }
      }   
    }

    
    private int suspendBundle(Context ctx, Subscriber sub, BundleFee fee)
    {
        int ret = ERROR_HANDLE_RESULT_SUCCESS; 
        try{
            if (SuspendEntityActionSupport.suspendBundle(ctx, sub, fee) != 
                SuspendEntityActionSupport.SUCCESS )
            {
                ret = ERROR_HANDLE_RESULT_FAIL_SUSPEND;    
            }
        } catch (Exception e)
        {
            new MinorLogMsg(this, "fail to suspend service for prepaid subscriber " +
               sub.getId() + 
               fee.getId(),e).log(ctx);
            ret = ERROR_HANDLE_RESULT_FAIL_SUSPEND;
        }

        return ret; 
    }
}
