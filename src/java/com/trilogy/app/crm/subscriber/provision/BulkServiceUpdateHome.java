/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.subscriber.provision;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.SPGServiceStateMappingConfig;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.provision.IPCProvisionAgent;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberServicesBulkUpdateSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 *  Introduced as part of Verizon Integration Feature to achieve bulk/ snapshot updation of
 *  subscriberServices on verizon / HLR
 *  @author isha.aderao@redknee.com
 */
public class BulkServiceUpdateHome extends HomeProxy
{
	private static final long serialVersionUID = -5596165489090122643L;
    private static final String BULK_SERVICE_UPDATE_HOME = BulkServiceUpdateHome.class.getName();
    public static final int HLR_ERROR = 3011;
	
      
    /**
     * @param ctx
     * @param delegate
     */
    public BulkServiceUpdateHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }
    
    
    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        
    	if(LogSupport.isDebugEnabled(ctx))
		{
    		LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "In CREATE ");
		}
		final Subscriber subscriber = (Subscriber) obj;
		Object returnedSubscriber = null;
		boolean isBulkServiceUpdate = Boolean.FALSE;
		
		List <SubscriberServices> newProvisionedServicesList = new ArrayList<SubscriberServices>(); 
		List <SubscriberServices> oldProvisionedServicesList = new ArrayList<SubscriberServices>();
		Map<ServiceFee2ID, SubscriberServices>  OSLMap = new HashMap<ServiceFee2ID, SubscriberServices>();

		Map<ServiceFee2ID, SubscriberServices>  subscriberServiceMap = new HashMap<ServiceFee2ID, SubscriberServices>();
		 
		
		try 
    	{
		    SPGServiceStateMappingConfig spgServiceStateMappingConfig = SubscriberServicesBulkUpdateSupport.getSPGProvisionedServiceStateMapping(ctx, subscriber);
            if(null != spgServiceStateMappingConfig)
            {
                isBulkServiceUpdate = (!spgServiceStateMappingConfig.isSkipBulkServiceUpdateForSubscriberCreation()) && SubscriberServicesBulkUpdateSupport.isBulkServiceUpdate(ctx, subscriber);
            }
			 
		} 
    	catch (HomeException e) 
    	{
			LogSupport.major(ctx, BULK_SERVICE_UPDATE_HOME,
						"Skipping bulkServiceUpdate for subscriber "+ subscriber.getId()
								+ " because of Exception while retrieving Provision Command : "+e.getMessage());
			isBulkServiceUpdate = Boolean.FALSE;
		}
    	if (!isBulkServiceUpdate) 
 		{
 			LogSupport.info(ctx, BULK_SERVICE_UPDATE_HOME,
 							"Skipping BulkServiceUpdate for subscriber " + subscriber.getId()
 									+ " as bulkServiceUpdate provision command is not configured for the service provider "+ subscriber.getSpid()
 									+ " OR HLR is disabled OR SkipBulkServiceUpdateForSubscriberCreation is enabled.");
 			if(LogSupport.isDebugEnabled(ctx))
 			{
 				LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "calling super.create() ");
 			}
 			// discontinue this home and delegate to next home
 			returnedSubscriber = super.create(ctx, subscriber);
 		}
    	else
    	{
			OSLMap =  SubscriberServicesSupport.getAllSubscribersServicesRecords(ctx, subscriber.getId());
			if(LogSupport.isDebugEnabled(ctx))
	    	{
	    		LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "old Services List for subscriber [ "+subscriber.getId()+ "] is : "+OSLMap);
	    	}
			oldProvisionedServicesList = SubscriberServicesBulkUpdateSupport.preprocessing(ctx, OSLMap, subscriber);
		
			if(LogSupport.isDebugEnabled(ctx))
 			{
				LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "super.create() ");
 			}
			returnedSubscriber = super.create(ctx, subscriber);
			
			
			boolean isHlrUpdated = 	SubscriberServicesBulkUpdateSupport.postProcessing(ctx, subscriber, OSLMap, oldProvisionedServicesList, newProvisionedServicesList);
			
			if(!isHlrUpdated && isRollbackRequired(ctx,subscriber))
           	{
				subscriberServiceMap =  SubscriberServicesSupport.getAllSubscribersServicesRecords(ctx, subscriber.getId());
				
				List <Long> subscriberServiceIDList  = new ArrayList<Long>();
				List <Long> oldProvisionedServiceIDList = new ArrayList<Long>();
				
				List <Long> provisionedServiceList = new ArrayList<Long>();
				List <Long> unProvisionedServiceList = new ArrayList<Long>();
				
				Service serviceBean = null;
				 for (SubscriberServices oldSubscriberService : oldProvisionedServicesList)
				 {
					 serviceBean = ServiceSupport.getService(ctx, oldSubscriberService.getServiceId());
					
					 if (serviceBean.getType().getIndex() == ServiceTypeEnum.EXTERNAL_PRICE_PLAN_INDEX)
					 {
						 oldProvisionedServiceIDList.add(oldSubscriberService.getServiceId());
						 provisionedServiceList.add(oldSubscriberService.getServiceId());
						
					 }
	                 
				 }//END OF FOR
				
				
				for (final Iterator<SubscriberServices> iter = subscriberServiceMap.values().iterator(); iter.hasNext();)
		        {
					SubscriberServices oldSubscriberService = iter.next();
					
					serviceBean = ServiceSupport.getService(ctx, oldSubscriberService.getServiceId());
					
					 if (serviceBean.getType().getIndex() == ServiceTypeEnum.EXTERNAL_PRICE_PLAN_INDEX)
					 {
						 subscriberServiceIDList .add(oldSubscriberService.getServiceId());
						 unProvisionedServiceList.add(oldSubscriberService.getServiceId());
					 }
				}
								
				provisionedServiceList.removeAll(subscriberServiceIDList );
				unProvisionedServiceList.removeAll(oldProvisionedServiceIDList);
				
				rollbackSubscriberServiceState(ctx,subscriber,provisionedServiceList,unProvisionedServiceList);
           	}
    	}
    	
        return returnedSubscriber;
        
    }
    
    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
    	if(LogSupport.isDebugEnabled(ctx))
		{
    		LogSupport.debug(ctx, BulkServiceUpdateHome.class.getName(), "In STORE ");
		}
    	final Subscriber subscriber = (Subscriber) obj;
	    Object returnedSubscriber = null;
	    boolean isBulkServiceUpdate = Boolean.FALSE;
	    final Subscriber oldSub = subscriber;
	    
	    	     
	    List <SubscriberServices> newProvisionedServicesList = new ArrayList<SubscriberServices>(); 
	    List <SubscriberServices> oldProvisionedServicesList = new ArrayList<SubscriberServices>();	
		Map<ServiceFee2ID, SubscriberServices>  OSLMapSubscriber = new HashMap<ServiceFee2ID, SubscriberServices>();
		Map<ServiceFee2ID, SubscriberServices>  OSLMap = new HashMap<ServiceFee2ID, SubscriberServices>();
		Map<ServiceFee2ID, SubscriberServices>  subscriberServiceMap = new HashMap<ServiceFee2ID, SubscriberServices>();			 
 		try 
    	{
			isBulkServiceUpdate = SubscriberServicesBulkUpdateSupport.isBulkServiceUpdate(ctx, subscriber);
		} 
    	catch (HomeException e) 
    	{
			LogSupport.major(ctx, BULK_SERVICE_UPDATE_HOME,
						"Skipping bulkServiceUpdate for subscriber "+ subscriber.getId()
								+ " because of Exception while retrieving Provision Command : "+e.getMessage());
			isBulkServiceUpdate = Boolean.FALSE;
		}
    	if (!isBulkServiceUpdate) 
 		{
 			LogSupport.info(ctx, BULK_SERVICE_UPDATE_HOME,
 							"Skipping BulkServiceUpdate for subscriber " + subscriber.getId()
 									+ " as bulkServiceUpdate provision command is not configured for the service provider "+ subscriber.getSpid()
 									+ " OR HLR is disabled OR SkipBulkServiceUpdateForSubscriberCreation is enabled.");
 			if(LogSupport.isDebugEnabled(ctx))
 			{
 				LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "calling super.store() ");
 			}
 			// delegate to next home
 			returnedSubscriber = super.store(ctx, subscriber);
 		}
    	else
    	{
    		OSLMapSubscriber =  SubscriberServicesSupport.getAllSubscribersServicesRecords(ctx, subscriber.getId());
			if(LogSupport.isDebugEnabled(ctx))
	    	{
	    		LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "old Services List for subscriber [ "+subscriber.getId()+ "] is : "+OSLMapSubscriber);
	    	}
			oldProvisionedServicesList = SubscriberServicesBulkUpdateSupport.preprocessing(ctx, OSLMapSubscriber, subscriber);		
			if(LogSupport.isDebugEnabled(ctx))
 			{
				LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "super.store() ");
 			}
			
			returnedSubscriber = super.store(ctx, subscriber);
			
			//REMOVE PROVISIONWITHERROR SERVICE FROM OSL			
			
			for (final Iterator<SubscriberServices> iter = OSLMapSubscriber.values().iterator(); iter.hasNext();)
	        {
				SubscriberServices newSubscriberService = iter.next();
				
				if(newSubscriberService.getProvisionedState().equals(ServiceStateEnum.PROVISIONEDWITHERRORS))
				{
					continue;
				}else
				{
					OSLMap.put(new ServiceFee2ID(newSubscriberService.getServiceId(),newSubscriberService.getPath()), newSubscriberService);
				}				
	        }
			
			boolean isHlrUpdated = SubscriberServicesBulkUpdateSupport.postProcessing(ctx, subscriber, OSLMap, oldProvisionedServicesList, newProvisionedServicesList);           	
          
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "isHlrUpdated : "+isHlrUpdated+".");				
			}
			
           	if(!isHlrUpdated  && isRollbackRequired(ctx,subscriber))
           	{
           		subscriberServiceMap =  SubscriberServicesSupport.getAllSubscribersServicesRecords(ctx, subscriber.getId());
    			
    			List <Long> newSubscriberServiceIDList = new ArrayList<Long>();
    			List <Long> oldProvisionedServiceIDList = new ArrayList<Long>();
    			
    			List <Long> servicesToBeMarkedAsUnprovisionedWithErrors = new ArrayList<Long>();
    			List <Long> servicesToBeMarkedAsProvisionedWithErrors = new ArrayList<Long>();
    			Service serviceBean = null;
    			
    			 for (SubscriberServices oldSubscriberService : oldProvisionedServicesList)
    			 {
    				 serviceBean = ServiceSupport.getService(ctx, oldSubscriberService.getServiceId());
    				 
    				 if (serviceBean.getType().getIndex() == ServiceTypeEnum.EXTERNAL_PRICE_PLAN_INDEX)
    				 {
    				 	 oldProvisionedServiceIDList.add(oldSubscriberService.getServiceId());
    				 	 servicesToBeMarkedAsUnprovisionedWithErrors.add(oldSubscriberService.getServiceId());
    				 	
    				 }
                     
    			 }//END OF FOR
    			
    			
    			for (final Iterator<SubscriberServices> iter = subscriberServiceMap.values().iterator(); iter.hasNext();)
    	        {
    				SubscriberServices newSubscriberService = iter.next();
    				
    				serviceBean = ServiceSupport.getService(ctx, newSubscriberService.getServiceId());
					
					 if (serviceBean.getType().getIndex() == ServiceTypeEnum.EXTERNAL_PRICE_PLAN_INDEX)
					 {
						 newSubscriberServiceIDList.add(newSubscriberService.getServiceId());
						 servicesToBeMarkedAsProvisionedWithErrors.add(newSubscriberService.getServiceId());
					 }
    			}
    			
    	   		if(LogSupport.isDebugEnabled(ctx))
    			{
    				LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "servicesToBeMarkedAsUnprovisionedWithErrors : "+servicesToBeMarkedAsUnprovisionedWithErrors+"."+"newSubscriberServiceIDList:"+newSubscriberServiceIDList);
    				LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "servicesToBeMarkedAsProvisionedWithErrors :" +servicesToBeMarkedAsProvisionedWithErrors + "."+"oldProvisionedServiceIDList:"+oldProvisionedServiceIDList);
    			}
    			
    			servicesToBeMarkedAsUnprovisionedWithErrors.removeAll(newSubscriberServiceIDList);
    			servicesToBeMarkedAsProvisionedWithErrors.removeAll(oldProvisionedServiceIDList);
    			
    			rollbackSubscriberServiceState(ctx,subscriber,servicesToBeMarkedAsUnprovisionedWithErrors,servicesToBeMarkedAsProvisionedWithErrors);
           		
           	}
			
    	}
         
        return returnedSubscriber;
    }
    
    private Subscriber rollbackSubscriberServiceState(Context ctx, Subscriber subscriber,			
			List<Long> servicesToBeMarkedAsUnprovisionedWithErrors,			
			List<Long> servicesToBeMarkedAsProvisionedWithErrors
			) throws HomeException{
		
    	
   		SubscriberServices newlyAssociatedService =null;
   		SubscriberServices currentSubscriberService = null;
   		
   		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "servicesToBeMarkedAsUnprovisionedWithErrors : "+servicesToBeMarkedAsUnprovisionedWithErrors+".");
			LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME, "servicesToBeMarkedAsProvisionedWithErrors :" +servicesToBeMarkedAsProvisionedWithErrors + ".");
		}
   		
   		if(servicesToBeMarkedAsUnprovisionedWithErrors != null && servicesToBeMarkedAsUnprovisionedWithErrors.size()>0)
   		{
   			
   			ServiceStateEnum state = null; 
   			
   			//Associate the services to subscriber first before marking them as unprovisionedWithErrors
   			reattachSubscriberService(ctx,servicesToBeMarkedAsUnprovisionedWithErrors,subscriber);
   		 
   			 for (long serviceId : servicesToBeMarkedAsUnprovisionedWithErrors)
			 {
				 newlyAssociatedService = SubscriberServicesSupport.getSubscriberServiceRecord(ctx, subscriber.getId(), serviceId, SubscriberServicesUtil.DEFAULT_PATH);
				 
				state = ServiceStateEnum.UNPROVISIONEDWITHERRORS;
				newlyAssociatedService.setSubscriberId(subscriber.getId());
				newlyAssociatedService.setProvisionActionState(false);
				
				subscriber.updateSubscriberService(ctx, newlyAssociatedService, state);
				
				SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, subscriber, 
                        HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.SERVICE, newlyAssociatedService.getService(ctx), state);
				
                 
			 }//END OF FOR
   		}
   		
   		if(servicesToBeMarkedAsProvisionedWithErrors != null && servicesToBeMarkedAsProvisionedWithErrors.size()>0)
   		{
   			ServiceStateEnum state = null;
   			
			 for (long serviceId : servicesToBeMarkedAsProvisionedWithErrors)
			 {
				 
				 currentSubscriberService =  SubscriberServicesSupport.getSubscriberServiceRecord(ctx, subscriber.getId(), serviceId, SubscriberServicesUtil.DEFAULT_PATH);	
				 
				 state = ServiceStateEnum.PROVISIONEDWITHERRORS;
				 
				 currentSubscriberService.setProvisionActionState(false);
				 subscriber.updateSubscriberService(ctx, currentSubscriberService, state);
				 
				 SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, subscriber, 
						 HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.SERVICE, currentSubscriberService.getService(ctx), state);
				
				 
			 }//END OF FOR
   		}
   		
   		return subscriber;
		
	}
    
    
    public static boolean isRollbackRequired(Context ctx,Subscriber sub) 
    {
    	 CRMSpid spid = null;
         try
         {
             spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
         }
         catch (HomeException e)
         {
        	 if(LogSupport.isDebugEnabled(ctx))
  			{
 				LogSupport.debug(ctx, BULK_SERVICE_UPDATE_HOME,e);
  			} 
         }
  
        return  spid.getBulkupdateRollbackSupport();
    }
    
    
    public void reattachSubscriberService(Context ctx,List<Long> servicesToBeReAttached,Subscriber subscriber) throws HomeException
    {
    	
    		Service serviceBean = null;
			List<SubscriberServices> provServices = new ArrayList<SubscriberServices>();
    	
    	for (long serviceId : servicesToBeReAttached)
		 {			 
    		serviceBean = ServiceSupport.getService(ctx, serviceId);
    		SubscriberServices bean = createSubscriberServicesBean(ctx, subscriber, ServiceStateEnum.PROVISIONED, true, serviceBean);
	        provServices.add(bean);	  
		 }
    	
    	
        Set<SubscriberServices> provServiceSet = new HashSet<SubscriberServices>();
        for (SubscriberServices subService : provServices)
        {
            provServiceSet.add(subService);
        }
      
        ctx.put(IPCProvisionAgent.SKIP_DATA_SVC_CHECK, Boolean.TRUE);
        
        subscriber.setIntentToProvisionServices(provServiceSet);
        
        final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();
        SubscriberServicesSupport.provisionSubscriberServices(ctx, subscriber, provServices, subscriber,
                resultCodes);
    	
   }
    
    
    protected SubscriberServices createSubscriberServicesBean(Context ctx, Subscriber subscriber, ServiceStateEnum provisionedState, boolean mandatory, Service svc) throws HomeException
    {
        final SubscriberServices result = new SubscriberServices();
        
        result.setContext(ctx);
        
        //As we are re-attaching the service, that means it was not present with the subscriber earlier.
        //Assuming that the service is not-mandatory.
        result.setMandatory(false);
        result.setProvisionedState(provisionedState);
        result.setService(svc);
        result.setServiceId(svc.getID());
        result.setServicePeriod(svc.getChargeScheme());
        result.setSubscriberId(subscriber.getId());
        result.setSuspendReason(SuspendReasonEnum.NONE);
        result.setSubscriberServiceDates(ctx, new Date());

        return result;
       
    }
    
    
    
}
