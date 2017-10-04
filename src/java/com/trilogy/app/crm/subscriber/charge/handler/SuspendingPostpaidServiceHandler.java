package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.provision.SuspendEntityActionSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class SuspendingPostpaidServiceHandler
extends GenericHandler
{
    
    public SuspendingPostpaidServiceHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    public SuspendingPostpaidServiceHandler()
    {
        
    }

    public void handleError(Context ctx, ChargableItemResult ret)
    {
        final ServiceFee2 serviceFee = (ServiceFee2)ret.getChargableObject();
        
        try
        {
            if(!serviceFee.getService(ctx).isRestrictProvisioning())
                return;
            
            switch( ret.getChargeResult())
            {
            case TRANSACTION_FAIL_OCG:
                //ret.setRunningState(RUNNING_CONTINUE_SUSPEND); 
                //$FALL-THROUGH$
                case  TRANSACTION_SKIPPED_SUSPEND: 
                suspendService(ctx, ret.getSubscriber(), serviceFee);
            }
        } 
        catch (Exception e)
        {
            if(LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, this, 
                        "Could not fetch BundleProfile for service ID: "+ 
                        serviceFee.getServiceId(), e);
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
                    "  for postpaid subscriber " +
                    sub.getId(),e).log(ctx);
            ret = ERROR_HANDLE_RESULT_FAIL_SUSPEND;
        }

        return ret; 
    }
}
