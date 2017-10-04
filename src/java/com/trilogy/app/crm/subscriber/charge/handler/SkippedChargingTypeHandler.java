package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.framework.xhome.context.Context;

public class SkippedChargingTypeHandler 
extends AbstractFilterHandler
{
    public SkippedChargingTypeHandler()
    {
        
    }
    
    public SkippedChargingTypeHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    
    protected boolean isRejected(Context ctx, ChargableItemResult ret)
    {
        return ret.getChargeResult() == ChargingConstants.TRANSACTION_SKIPPED_UNSUPPORTED_TYPE; 
    }

}
