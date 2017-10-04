package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.bean.ChargableItem;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.framework.xhome.context.Context;

public  class GenericHandler 
implements ChargeRefundResultHandler, ChargingConstants
{
    
    public GenericHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    public GenericHandler()
    {
        
    }
    
    public void handleTransaction(Context ctx,  ChargableItemResult ret)
    {
        if (ret.getChargeResult() == TRANSACTION_SUCCESS)
        {
            handleSuccess(ctx, ret);            
        } else 
        {
            handleError(ctx, ret); 
        }
        
        if(delegate_ != null)
        {
            delegate_.handleTransaction(ctx, ret);
        }
    }    


    
    public void setDelegate(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }
    
    protected void handleSuccess(Context ctx, ChargableItemResult ret)
    {
        
    }
    
    protected void handleError(Context ctx, ChargableItemResult ret)
    {
        
    }
    

    protected ChargeRefundResultHandler getDelegate()
    {
        return delegate_; 
    }
    
    /**
     * A flag is introduced at each item - 'BundleProdile', 'Service',...
     * to indicate if item (option) should be suspended if insufficient balance
     * while provisioning the new item (option). Suspension will be carried out if
     * the flag is configured false to induce legacy behavior.
     * 
     * @param ret
     * @return
     */
    public static boolean isItemMarkedForProvisioningRestriction(ChargableItemResult ret)
    {
        /*
         *
         */
        boolean restrictProvisioning = 
            ret.getChargableObjectRef() instanceof ChargableItem ? 
                    ((ChargableItem)ret.getChargableObjectRef()).getRestrictProvisioning() :
                        ret.getChargableObject() instanceof ChargableItem ? 
                                ((ChargableItem)ret.getChargableObject()).getRestrictProvisioning() : false;
        return restrictProvisioning;
    }

    ChargeRefundResultHandler delegate_; 
}
