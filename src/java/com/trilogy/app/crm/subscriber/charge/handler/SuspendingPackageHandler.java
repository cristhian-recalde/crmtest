package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.provision.SuspendEntityActionSupport;
import com.trilogy.app.crm.support.SpidSupport;

public class SuspendingPackageHandler 
extends GenericHandler
{
    
    public SuspendingPackageHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    public SuspendingPackageHandler()
    {
        
    }
   
    
    @Override
    public void handleError(Context ctx, ChargableItemResult ret)
    {
        switch( ret.getChargeResult())
        {
        case TRANSACTION_FAIL_OCG:
        case  TRANSACTION_SKIPPED_SUSPEND: 
            final ServicePackageFee fee = (ServicePackageFee) ret.getChargableObject();
            final ServicePackage pack = (ServicePackage) ret.getChargableObjectRef();
            suspendPackage(ctx, ret.getSubscriber(), pack, fee);
        }
    }    

    private int suspendPackage(Context ctx, 
            Subscriber sub, 
            ServicePackage pack,
            ServicePackageFee fee)
    {
        int ret = ERROR_HANDLE_RESULT_SUCCESS; 

        try{ 
            if ( pack != null)
            {
                if (SuspendEntityActionSupport.unsuspendPackage(ctx, sub, pack) != 
                    SuspendEntityActionSupport.SUCCESS )
                {
                    ret = ERROR_HANDLE_RESULT_FAIL_SUSPEND;    
                }

             } else 
            {
                ret = ERROR_HANDLE_RESULT_FAIL_FIND_PACK;
            }
        
        
        } catch (Exception e)
        {
            new MinorLogMsg(this, "fail to suspend service package " + 
                fee.getPackageId() + "for prepaid subscriber " +
                sub.getId() 
                ,e).log(ctx);
            ret = ERROR_HANDLE_RESULT_FAIL_SUSPEND;
        }
    
        return ret; 
    }
 
}
