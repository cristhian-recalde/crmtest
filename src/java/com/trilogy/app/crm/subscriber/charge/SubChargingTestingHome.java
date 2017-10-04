package com.trilogy.app.crm.subscriber.charge;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class SubChargingTestingHome extends HomeProxy {

    public SubChargingTestingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
    
    
    public Object create(Context ctx, Object obj) throws HomeException
    {
        Object newObj = super.create(ctx, obj); 
        print(ctx, newObj); 
        return newObj; 
    }
    
    public Object store(Context ctx, Object obj) throws HomeException
    {
        Object newObj = super.store(ctx, obj); 
               
        print(ctx, newObj); 
        return newObj; 

    }

     public void print(Context ctx, Object obj)
    {
         AbstractSubscriberCharger charger = (AbstractSubscriberCharger) ctx.get(AbstractSubscriberCharger.class); 
        
         if ( charger != null )
         {     
             print(charger.getPackagesToBeCharged(),"pacakge to be charged"); 
             print(charger.getPackagesToBeRefund(), "packages to be refund"); 
             print(charger.getServicesToBeCharged(), "services to be charged"); 
             print(charger.getServicesToBeRefund(), "services to be refund"); 
             print(charger.getBundlesToBeCharged(), "bundles to be charged"); 
             print(charger.getBundlesToBeRefund(), "bundles to be refund"); 
             print(charger.getAuxServicesToBeCharged(), "aux to be charged"); 
             print(charger.getAuxServicesToBeRefund(), "aux to be refund"); 
         }
        
    }
    
    
    public void print(Collection c, String header)
    {
        System.out.println(header); 
        for ( Iterator i = c.iterator(); i.hasNext();)
        {
            System.out.println(i.next().toString()); 
        }
    }
    
}
