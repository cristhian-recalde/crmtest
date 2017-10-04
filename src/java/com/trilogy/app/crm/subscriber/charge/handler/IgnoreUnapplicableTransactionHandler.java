package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.framework.xhome.context.Context;

public class IgnoreUnapplicableTransactionHandler extends AbstractFilterHandler {

    public IgnoreUnapplicableTransactionHandler()
    {
    }
    
    public IgnoreUnapplicableTransactionHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    protected boolean isRejected(Context ctx, ChargableItemResult ret) 
    {
        return ret.getChargeResult() == ChargingConstants.TRANSACTION_SKIPPED_DUPLICATE_CHARGE ||
        ret.getChargeResult() == ChargingConstants.TRANSACTION_SKIPPED_DUPLICATE_REFUND ||
        ret.getChargeResult() == ChargingConstants.TRANSACTION_SKIPPED_NO_CHARGE_IN_BILLING_CYCLE; 
    }

}
