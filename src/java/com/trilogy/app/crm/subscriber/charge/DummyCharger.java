package com.trilogy.app.crm.subscriber.charge;

import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.framework.xhome.context.Context;

public class DummyCharger implements CrmCharger {

    public int charge(Context ctx, ChargeRefundResultHandler handler) {
        return ChargingConstants.OPERATION_SUCCESS;
    }

    public int chargeAndRefund(Context ctx, ChargeRefundResultHandler handler) {
        return ChargingConstants.OPERATION_SUCCESS;
    }

    public int getChargerType() {
        // TODO Auto-generated method stub
        return ChargingConstants.OPERATION_SUCCESS;
    }

    public double getTotalChargedAmount() {
        return 0;
    }

    public double getTotalFailedChargeAmount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getTotalFailedRefundAmount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getTotalRefundAmount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getTotalToBeChargedAmount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getTotalToBeRefundAmount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int refund(Context ctx, ChargeRefundResultHandler handler) {
        return ChargingConstants.OPERATION_SUCCESS;
    }

}
