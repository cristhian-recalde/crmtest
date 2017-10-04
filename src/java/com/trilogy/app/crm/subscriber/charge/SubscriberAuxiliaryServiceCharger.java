package com.trilogy.app.crm.subscriber.charge;

import java.util.Collection;
import java.util.HashSet;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

public class SubscriberAuxiliaryServiceCharger 
extends        AbstractCrmCharger 
{
  
    public SubscriberAuxiliaryServiceCharger(Subscriber sub,  
    		SubscriberAuxiliaryService subService)
    {
    	this.subService = subService;
        this.subscriber = sub; 
    }

    public int charge(Context ctx, ChargeRefundResultHandler handler) {
       Collection <SubscriberAuxiliaryService> subServices = new HashSet(); 
       subServices.add(subService); 
       return AuxServiceChargingSupport.applyAuxServicesChargeByIds(ctx, subServices, subscriber, 
               subscriber, false, null,ACTION_PROVISIONING_CHARGE , null, RUNNING_SUCCESS); 
                   
    }

    public int chargeAndRefund(Context ctx, ChargeRefundResultHandler handler) {
        // TODO Auto-generated method stub
        return OPERATION_NOT_SUPPORT;
    }

    public int getChargerType() {
        // TODO Auto-generated method stub
        return CHARGER_TYPE_SUBSCRIBER_AUX_SERVICER;
    }    
    

    public int refund(Context ctx, ChargeRefundResultHandler handler) 
    {
        Collection <SubscriberAuxiliaryService> subServices = new HashSet(); 
        if (subServices != null)
        {
        	AuxiliaryService auxService = AuxServiceChargingSupport.getAuxiliaryServicById(ctx, subService.getAuxiliaryServiceIdentifier());
        	if ( auxService != null && auxService.isRefundable())
        	{
        		subServices.add(subService);
        		
        		if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Refund to be applied for AuxiliaryService ID : " + auxService.getIdentifier() + " for Subscriber ID : " + subService.getSubscriberIdentifier() + ", as Is Refundable flag : " + auxService.isRefundable());
                }
        	}
        }
        
        return AuxServiceChargingSupport.applyAuxServicesChargeByIds(ctx, subServices, subscriber, 
                subscriber, false, null,ACTION_PROVISIONING_REFUND , null, RUNNING_SUCCESS); 
    }

    
    protected Subscriber subscriber;
    protected SubscriberAuxiliaryService subService; 
}
