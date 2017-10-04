package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.provision.SuspendEntityActionSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;

public class SuspendedEntityReleaseHandler 
extends GenericHandler
{
    public SuspendedEntityReleaseHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    public SuspendedEntityReleaseHandler()
    {
        
    }
   
    @Override
    public void handleSuccess(Context ctx,  ChargableItemResult ret)
    {
        super.handleSuccess(ctx, ret);
        if (ret.getSubscriber().getSuspendedServices(ctx).containsKey(XBeans.getIdentifier(ret.getChargableObject())))
        {
            unSuspend(ctx, ret);
        }
    
    }
    
    private int unSuspend(Context ctx,  ChargableItemResult ret)
    {
        switch ( ret.getChargableItemType())
        {
        case CHARGABLE_ITEM_SERVICE:
            return SuspendEntityActionSupport.unsuspendService(ctx, ret.getSubscriber(), 
                    (ServiceFee2)ret.getChargableObject());
        
        case CHARGABLE_ITEM_PACKAGE:         
            return SuspendEntityActionSupport.unsuspendPackage(ctx, ret.getSubscriber(), 
                    (ServicePackage)ret.getChargableObjectRef());
        
        case CHARGABLE_ITEM_BUNDLE:
            return SuspendEntityActionSupport.unsuspendBundle(ctx, ret.getSubscriber(), 
                    (BundleFee)ret.getChargableObject());
        
        case CHARGABLE_ITEM_AUX_SERVICE:
            return SuspendEntityActionSupport.unsuspendAuxService(ctx, ret.getSubscriber(), (AuxiliaryService)ret.getChargableObject(), this);        
        }
        return 0;
    
    }
    
    


}