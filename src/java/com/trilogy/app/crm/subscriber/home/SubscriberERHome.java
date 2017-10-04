package com.trilogy.app.crm.subscriber.home;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.home.MsisdnPortHandlingHome;
import com.trilogy.app.crm.log.ERLogger;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


public class SubscriberERHome extends HomeProxy {

    public SubscriberERHome(final Context ctx, final Home delegate)
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
        Subscriber sub=null; 
        try {
            sub = (Subscriber) super.create(ctx, obj); 
        } catch (HomeException e)
        {
            throw e; 
        }finally
        {            
            // The name of ER 761 is confusing, for true prepaid, it is not activation
            // it is only creation. 
            Subscriber subscriber = sub==null?(Subscriber)obj:sub;
            ERLogger.logActivationER(ctx, subscriber);
            if(subscriber.getMsisdn().equals(ctx.get(MsisdnPortHandlingHome.MSISDN_PORT_KEY)))
            {
                ERLogger.logPortInER(ctx, subscriber );
            }
        }
       
         return sub; 
    }
    
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        Subscriber sub=null; 
        try {
            sub = (Subscriber) super.store(ctx, obj); 
        } catch (HomeException e)
        {
            throw e; 
        }finally
        {           
            Subscriber subscriber = sub==null?(Subscriber)obj:sub;
            ERLogger.logModificationER(ctx, subscriber );
            if(subscriber.getMsisdn().equals(ctx.get(MsisdnPortHandlingHome.MSISDN_PORT_KEY)))
            {
                ERLogger.logPortInER(ctx, subscriber );
            }
        }
        return sub; 
    }    

    public void remove(Context ctx, Object obj) throws HomeException
    {
        super.remove(ctx, obj);
        Subscriber sub = (Subscriber) obj;
        int resultCode = 0;
        ERLogger.logRemovalER(ctx, sub.getSpid(), sub.getMsisdn(), sub.getBAN(), sub.getSubscriptionClass(), resultCode);
        
    }
    
    


    
}
