package com.trilogy.app.crm.subscriber.charge;

import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.framework.xhome.context.Context;


/**
 * apply charge before provisioning. not supported for now. 
 * @author lxia
 *
 */


public class PreProvisionSubscriberCharger 
extends AbstractSubscriberProvisioningCharger
{
    public PreProvisionSubscriberCharger()
    {
     
    }

    protected int calculateOnCreate(Context ctx,
            ChargeRefundResultHandler handler) {
        // TODO Auto-generated method stub
        return CALCULATION_SKIPPED;
    }

    protected int calculateOnMultipleUpdate(Context ctx,
            ChargeRefundResultHandler handler) {
        // TODO Auto-generated method stub
        return CALCULATION_SKIPPED;
    }


    protected int calculateOnSimplePPVChange(Context ctx,
            ChargeRefundResultHandler handler) {
        // TODO Auto-generated method stub
        return CALCULATION_SKIPPED;
    }

    protected int calculateOnSimpleProvisionUnprovison(Context ctx,
            ChargeRefundResultHandler handler) {
        // TODO Auto-generated method stub
        return CALCULATION_SKIPPED;
    }

    protected int calculateOnSimpleStateChange(Context ctx,
            ChargeRefundResultHandler handler) {
        // TODO Auto-generated method stub
        return CALCULATION_SKIPPED;
    }


    public int handleChargeOnMoveSubscriber(Context ctx,
            ChargeRefundResultHandler errorHandler) {
        // TODO Auto-generated method stub
        return CALCULATION_SKIPPED;
    }

    public int handleRefundOnMoveSubscriber(Context ctx,
            ChargeRefundResultHandler errorHandler) {
        // TODO Auto-generated method stub
        return CALCULATION_SKIPPED; 
    }
 
    public int getChargerType()
    {
        return CHARGER_TYPE_PRE_PROVISION_SUBSCRIBER;
    }

}
