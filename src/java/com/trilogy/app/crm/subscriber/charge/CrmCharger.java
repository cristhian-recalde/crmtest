package com.trilogy.app.crm.subscriber.charge;

import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.framework.xhome.context.Context;


/**
 * generic charger interface, could support account and spid level charge in future
 * @author lxia
 *
 */
public interface CrmCharger 
{
     public int chargeAndRefund(Context ctx,
            ChargeRefundResultHandler handler);

     public int charge(Context ctx,
             ChargeRefundResultHandler handler);
     
     public int refund(Context ctx,
             ChargeRefundResultHandler handler);
                    

     public double getTotalChargedAmount(); 
     public double getTotalRefundAmount(); 
     public double getTotalToBeChargedAmount(); 
     public double getTotalToBeRefundAmount(); 
     public double getTotalFailedChargeAmount(); 
     public double getTotalFailedRefundAmount(); 

     public int getChargerType();
}
