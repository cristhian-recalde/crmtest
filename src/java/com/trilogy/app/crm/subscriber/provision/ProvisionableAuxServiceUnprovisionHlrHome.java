package com.trilogy.app.crm.subscriber.provision;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.priceplan.PricePlanVersionUpdateAgent;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * this home will unprovision HLR for provisionable aux service
 * it cover the following cases that full reprovision in service home 
 * need delete subscriber profile on hlr. 
 * 
 * 1. msisdn change, 
 * 2. subscriber type conversion
 * 3. ppv change with service that need update HLR. 
 * 
 * It will not handle delta aux services provisioning. which is already taken care by 
 * SubscriberAuxiliaryServiceCreationHome. 
 * 
 *  this home must put before SubscriberProvisionServicesHome, in which service will be reprovisioned. 
 * 
 * @author lxia
 *
 */
public class ProvisionableAuxServiceUnprovisionHlrHome 
extends HomeProxy
{

	public ProvisionableAuxServiceUnprovisionHlrHome(Home home)
	{
		super(home); 
	}
	
    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context,java.lang.Object)
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {

        Subscriber newSub = (Subscriber) obj;
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        final boolean intentToRemoveOldMsisdnFromHlr = SubscriberProvisionHlrGatewayHome.isIntentToRemoveMsisnFromHlrGateway(ctx);
        if(intentToRemoveOldMsisdnFromHlr )
        {
            String msg = MessageFormat.format(
                "Skipping 'AuxServices Unprovision HLR Commands' for subscriber {0}; ProvisionCommand Name: {1}; oldMsisdn: {2}; newMsisdn: {3}", 
                    new Object[]{oldSub.getId(), 
                        SubscriberProvisionHlrGatewayHome.getProvisionCommandName(ctx),
                            oldSub.getMsisdn(), newSub.getMsisdn()});
            LogSupport.info(ctx, this, msg);
        }
        
        if (!ctx.getBoolean(SubscriberServicesProvisioningHome.getSubscriberVisitingCountContextKey(newSub), false) 
                && !intentToRemoveOldMsisdnFromHlr
        )
        {	
        	try
        	{
        	    CRMSpid spid = SpidSupport.getCRMSpid(ctx, newSub.getSpid());
        		if ( (!SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN()) 
        		        && spid != null 
        		        && !spid.getSkipReProvisioningAuxServicesOnChangeMsisdn() )
        		        || !oldSub.getSubscriberType().equals(newSub.getSubscriberType()))
        		{
        			newSub.setAuxServiceToBeReprovisioned(oldSub.getAuxiliaryServices(ctx)); 	
        		}
        		else  if ( isFullProvisioningTriggered(ctx, oldSub, newSub))
        		{
        			newSub.setAuxServiceToBeReprovisioned(getRetendedProvisionableAuxServices(ctx, oldSub, newSub));        			             
        		}
        		
        		if ( newSub.getAuxServiceToBeReprovisioned() != null )
        		{
        			unprovision(ctx, oldSub,  newSub.getAuxServiceToBeReprovisioned());
        		}	
        	}
        	catch (Exception e)
        	{
           		new MajorLogMsg(this, "fail to unprovision aux service for subscriber " + oldSub.getId() + " to HLR" , e).log(ctx); 

         	}
        }
        return super.store(ctx, obj);
    }
	
    
    private void unprovision(Context ctx, Subscriber oldSub, Collection subAuxServices)
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
        			StateChangeAuxiliaryServiceSupport.unProvisionHlr(ctx, subServ, service, oldSub, this);
        		}	
        	} catch (Throwable t)
        	{
        		new MajorLogMsg(this, "fail to unprovision aux service " + subServ.getAuxiliaryServiceIdentifier() 
        				+ "for subscriber " + oldSub.getId() + " to HLR" , t).log(ctx); 
        	}
        }
    }
	
    private Collection getRetendedProvisionableAuxServices(Context ctx, Subscriber oldSub, Subscriber newSub)
    {
    	Collection ret = new ArrayList(); 
    	
    	for (Iterator i = oldSub.getAuxiliaryServices(ctx).iterator(); i.hasNext();)
    	{
    		SubscriberAuxiliaryService subServ = (SubscriberAuxiliaryService) i.next(); 
    		
    		for(Iterator ni = newSub.getAuxiliaryServices(ctx).iterator(); ni.hasNext();)
    		{
    			SubscriberAuxiliaryService subServ1 = (SubscriberAuxiliaryService) ni.next();
    			if (subServ.getAuxiliaryServiceIdentifier() == subServ1.getAuxiliaryServiceIdentifier())
    			{
    				ret.add(subServ); 
    			}
    		}
    		
    	}
    	
    	
    	return ret; 
    }
    
    private boolean isFullProvisioningTriggered(Context ctx, Subscriber oldSub, Subscriber newSub)
    throws HomeException
    {
        // we should follow same policy with service, but unfortunately the logic 
    	// in SubscriberProvisionServiceHome is pretty fuzzy and fishy. The strategy in
    	// trunk is not as flexible as the one in 7.3, and we are losing feature to support
    	// fast price plan version updating for promotion. could need change to code of 7.3 
    	// when confirmed with SDA. 
    	
    	if ( newSub != null && oldSub != null)
        {
                        
            if(!SubscriberSupport.isSamePricePlanVersion(ctx, oldSub, newSub)  
                    && ctx.getBoolean(PricePlanVersionUpdateAgent.PRICE_PLAN_VERSION_UPDATE, false)
                    && SystemSupport.isReProvisionAuxServiceOnPricePlanChange(ctx))
            {
                return true;
            }
            else
            {
                return false;
            }

        }
       return false; 
    }
}
