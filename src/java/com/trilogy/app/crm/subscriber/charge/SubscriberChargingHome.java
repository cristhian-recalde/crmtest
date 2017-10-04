package com.trilogy.app.crm.subscriber.charge;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.BooleanHolder;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class SubscriberChargingHome 
extends HomeProxy
{

    public SubscriberChargingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
     }
    
    /**
     * in future we should support both precharge and postcharge, but 
     * for new we support postcharge only
     * @param ctx
     * @param obj
     * @return
     * @throws HomeException
     */
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        ctx.put(ClosedUserGroupSupport.SUBSCRIBER_AS_CUGMEMEBR, new BooleanHolder(false));
        final Object newObj = super.create(ctx, obj); 
        postCharge(ctx, newObj); 
        return newObj; 
    }
    
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        ctx.put(ClosedUserGroupSupport.SUBSCRIBER_AS_CUGMEMEBR, new BooleanHolder(false));
        final Object newObj = super.store(ctx, obj); 
        postCharge(ctx, newObj); 
        return newObj; 

    }    
    
    
    public void postCharge(final Context ctx, final Object obj)
    {
        CrmCharger charger = (CrmCharger) ctx.get(CrmCharger.class); 
        if ( charger == null )
        {               
            charger = new PostProvisionSubscriberCharger((Subscriber)obj);
            ctx.put(AbstractSubscriberCharger.class, charger);
        } 
        int ret = charger.chargeAndRefund(ctx, null);
        
        SubscriberProvisionResultCode.addChargeAmount(ctx, (long)charger.getTotalChargedAmount());  
        if (ret != ChargingConstants.OPERATION_SUCCESS)
        {
            createErrorMessage(ctx, charger);
        }
    }
    
    
    public void createErrorMessage(Context ctx, CrmCharger charger )
    {
        
    }

  
}
