package com.trilogy.app.crm.subscriber.charge;

import java.util.Collection;
import java.util.HashSet;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;

public class SubscriberServiceCharger extends        AbstractCrmCharger 
{
  

    
    public SubscriberServiceCharger(Subscriber sub,  
    		SubscriberServices subService)
    {
    	
    	this.subService_ = subService;
        this.subscriber_ = sub; 
    }


    public int charge(Context ctx, ChargeRefundResultHandler handler)
    {
        Collection<SubscriberServices> subServices = new HashSet();
        subServices.add(getSubscriberService());
        if (getSubscriberService() != null)
        {
            return ServiceChargingSupport.applyServicesChargeByIds(ctx, subServices, subscriber_, subscriber_, false,
                    null, ACTION_PROVISIONING_CHARGE, null, RUNNING_SUCCESS);
        }
        return ChargingConstants.RUNNING_SUCCESS;
    }

    public int chargeAndRefund(Context ctx, ChargeRefundResultHandler handler) {
        // TODO Auto-generated method stub
        return OPERATION_NOT_SUPPORT;
    }

    public int getChargerType() {
        // TODO Auto-generated method stub
        return CHARGER_TYPE_SUBSCRIBER_SERVICER;
    }    
    

    public int refund(Context ctx, ChargeRefundResultHandler handler)
    {
        Collection<SubscriberServices> subServices = new HashSet();
        if (subService_ != null)
        {
        	Service service = ServiceChargingSupport.getServiceById(ctx, subService_.getServiceId());
        	if (service != null && service.isRefundable()) 
        	{
        		subServices.add(subService_);
        		if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Refund to be applied for Service ID : " + service.getIdentifier() + " for Subscriber ID : " + subService_.getSubscriberId() + ", as Is Refundable flag : " + service.isRefundable());
                }
        		return ServiceChargingSupport.applyServicesChargeByIds(ctx, subServices, subscriber_, subscriber_, false,
        				null, ACTION_PROVISIONING_REFUND, null, RUNNING_SUCCESS);
        	}
        }
        return ChargingConstants.RUNNING_SUCCESS;
    }

    
    private Service getService()
    {
    	if (service_ == null && subService_ != null)
    	{
    		service_ = subService_.getService();  
    	}
    	
    	return service_; 
    }
    
    private SubscriberServices getSubscriberService()
    {
        return subService_;
    }
    protected Subscriber subscriber_;
    protected Service service_; 
    protected SubscriberServices subService_; 
}
