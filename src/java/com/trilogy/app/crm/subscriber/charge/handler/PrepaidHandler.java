package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;

public class PrepaidHandler extends
GenericHandler
{

    public PrepaidHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    public PrepaidHandler()
    {
        
    }

    
    public void handleError(Context ctx, ChargableItemResult ret)
    {
        super.handleError(ctx, ret);   
        //boolean restrictProvisioning = GenericHandler.isItemMarkedForProvisioningRestriction(ret);
        // should stop continue 
        if (ret.getChargeResult() == TRANSACTION_FAIL_OCG 
                //&& !restrictProvisioning
                )
        {
        	ret.setRunningState(RUNNING_CONTINUE_SUSPEND); 
        }
    }
    
    
}
