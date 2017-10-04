package com.trilogy.app.crm.subscriber.charge;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.framework.xhome.context.Context;

public class SubscriberRecurringChargeCharger 
extends AbstractSubscriberCharger
{

    public SubscriberRecurringChargeCharger(Subscriber sub)
    {
        this.subscriber = sub; 
    }
    
    public int calculate(Context ctx, ChargeRefundResultHandler handler) {
        // TODO Auto-generated method stub
        return OPERATION_NOT_SUPPORT;
    }

    public int charge(Context ctx, ChargeRefundResultHandler handler) {
        // TODO Auto-generated method stub
        return OPERATION_NOT_SUPPORT;
    }

    public int chargeAndRefund(Context ctx, ChargeRefundResultHandler handler) {
        // TODO Auto-generated method stub
        return OPERATION_NOT_SUPPORT;
    }

    public int getChargerType() {
        // TODO Auto-generated method stub
        return CHARGER_TYPE_RECURRING_CHARGE_SUBSCRIBER;
    }

    public int refund(Context ctx, ChargeRefundResultHandler handler) {
        // TODO Auto-generated method stub
        return OPERATION_NOT_SUPPORT;
    }

}
