package com.trilogy.app.crm.home.sub;

import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * 
 * @author odeshpande
 *
 */
public class VoiceMailServiceProvisionSupport {
	
	public static boolean isVMServiceAlreadyPresent(final Context ctx, final Subscriber sub)
    {
    	boolean vmServicePresent = false;
    	
    	final Set<SubscriberServices> provisionedServices = sub.getIntentToProvisionServices(ctx);
        if (provisionedServices == null || provisionedServices.size()== 0)
        {
            return vmServicePresent;
        }
        SubscriberServices vmSubscriberServiceAssociation = getVMSubscriberServiceIfPresent(ctx,provisionedServices);
        if(vmSubscriberServiceAssociation != null)
        {
        	
        		ServiceStateEnum serviceState = vmSubscriberServiceAssociation.getProvisionedState();
        		/*
        		 * Note : Unprovisioned or UnprovisionedWithError service will never be returned from getIntentToProvisionServices.
        		 */
        		if(serviceState != ServiceStateEnum.PROVISIONEDWITHERRORS && serviceState != ServiceStateEnum.PENDING)
        		{
        			vmServicePresent = true;
        		}
        	
        }
        return vmServicePresent;
    }
    
    public static boolean isVMServiceProvisionedNow(final Context ctx, final Subscriber sub)
    {
    	boolean vmServicePresent = false;
    	final Set svcIdSet = sub.getProvisionedServices(ctx);
        if (svcIdSet == null)
        {
            return vmServicePresent;
        }
        Service vmService = getVMServiceIfPresent(ctx,svcIdSet);
        if(vmService !=null)
        {
        	vmServicePresent = true;
        }
        return vmServicePresent;
    }
    
    public static Service getVMServiceIfPresent(Context ctx,Set svcIdSet)
    {
    	Service vmService = null;
    	final Home svcHome = (Home) ctx.get(ServiceHome.class);
        if (svcHome == null)
        {
            new MajorLogMsg(VoiceMailServiceProvisionSupport.class.getName(), "ServiceHome not found in context", null).log(ctx);
            return vmService;
        }
    	final Iterator svcIterator = svcIdSet.iterator();
        while (svcIterator.hasNext())
        {
            final Long svcId = (Long) svcIterator.next();
            try
            {
                final Service svc = (Service) svcHome.find(svcId);
                if (svc != null && (svc.getType() == ServiceTypeEnum.VOICEMAIL || 
                		(svc.getType() == ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY && 
                		svc.getSPGServiceType() == ServiceProvisioningGatewaySupport.VM_SERVICE_SPG_SERVICE_ID) ))
                {
                	vmService = svc;
                	return vmService;
                }
            }
            catch (HomeException he)
            {
                new MajorLogMsg(VoiceMailServiceProvisionSupport.class.getName(), "Exception while searching Service for ID:" + svcId , null).log(ctx);
            }
        }
        return vmService;
    }
    
    public static SubscriberServices getVMSubscriberServiceIfPresent(Context ctx,Set<SubscriberServices> provisionedServices)
    {
    	SubscriberServices vmServiceAssociation = null;
    	        
    	final Iterator svcIterator = provisionedServices.iterator();
        while (svcIterator.hasNext())
        {
            final SubscriberServices association = (SubscriberServices) svcIterator.next();
            final Service svc = (Service) association.getService(ctx);
            if (svc != null && (svc.getType() == ServiceTypeEnum.VOICEMAIL || 
            		(svc.getType() == ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY && 
            		svc.getSPGServiceType() == ServiceProvisioningGatewaySupport.VM_SERVICE_SPG_SERVICE_ID) ))
            {
            	vmServiceAssociation = association;
            	return vmServiceAssociation;
            }
            
        }
        return vmServiceAssociation;
    }
    
    

}
