package com.trilogy.app.crm.subscriber.provision;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;


public class ProvisionableAuxServiceProvisionHlrHome 
extends HomeProxy
{
	public ProvisionableAuxServiceProvisionHlrHome(Home home)
	{
		super(home); 
	}
	
    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
        Subscriber newSub = (Subscriber) obj;
        try
        {
        	if (newSub.getAuxServiceToBeReprovisioned()!= null)
        	{	
        		provision(ctx, newSub,  newSub.getAuxServiceToBeReprovisioned());
        	}
        	 
       	}
       	catch (Exception e)
       	{
       		new MajorLogMsg(this, "fail to provision aux service for subscriber " + newSub.getId() + " to HLR" , e).log(ctx); 

       	}finally
       	{
       		newSub.setAuxServiceToBeReprovisioned( null);	
       	}

        return super.store(ctx, obj);
    }

    
    
    private void provision(Context ctx, Subscriber newSub, Collection subAuxServices)
    throws HomeException
    {
        for (Iterator it = subAuxServices.iterator(); it.hasNext();) 
        {
      		SubscriberAuxiliaryService subServ = (SubscriberAuxiliaryService) it.next(); 
        	try 
        	{
        		AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(ctx, subServ.getAuxiliaryServiceIdentifier()); 
        		if (service.getType().equals(AuxiliaryServiceTypeEnum.Provisionable)
                            || service.getType().equals(AuxiliaryServiceTypeEnum.Vpn))
        		{	
        			StateChangeAuxiliaryServiceSupport.provisionHlr(ctx, subServ, service, newSub,this);
        		}	
        	} catch (Throwable t)
        	{
           		new MajorLogMsg(this, "fail to provision aux service " + subServ.getAuxiliaryServiceIdentifier() 
        				+ "for subscriber " + newSub.getId() + " to HLR" , t).log(ctx); 

        	}
        }
    }
}
