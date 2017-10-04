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
package com.trilogy.app.crm.home.sub;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.refactoring.ServiceRefactoring_RefactoringClass;
import com.trilogy.app.crm.subscriber.provision.BulkServiceUpdateHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.urcs.client.legacy.smsb.SvcProvision9Plugin;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * @author rattapattu
 */
public class SubscriberServicesSaveHome extends HomeProxy
{
	public SubscriberServicesSaveHome(Home delegate) 
	{
		super(delegate);
	}	
	
	/**
	 * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public Object create(Context ctx, Object obj) throws HomeException 
	{
	    Subscriber newSub = (Subscriber)obj;
		saveSubscriberServices(ctx,newSub);	
		return super.create(ctx, obj);
	}
    
	/**
	 * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public Object store(Context ctx, Object obj) throws HomeException 
	{
		Subscriber newSub = (Subscriber)obj;
		saveSubscriberServices(ctx,newSub);		
		Object superStore = super.store(ctx, obj);
		cleanSubscriberServices(ctx, newSub);
		/* 
		 *   Fix for TT 9082032036
		 *   When the subscription state is changed from Activated to De-Activated state , All subscriber services
		 *   related to that subscription were unprovisioned from URCS and their corresponding state 
		 *   in SubscriberServiceHome,which is a transient home were set to unprovisioned. 
		 *   This caused the services to be removed from the transient home and hence causing a NullPointerException
		 *   mentioned in TT.This exception occured before after removal of those services , the
		 *   SubscriberSubscriptionHistorySupport.addChargingHistory tries to access those records.
		 *   Delayed the removal of Subscriber services to that all required operations are performed before removal.
		 *    
		*/
		
		return superStore;
	}

	/**
	 * @see com.redknee.framework.xhome.home.HomeSPI#remove(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public void remove(Context ctx, Object oldSub) throws HomeException 
	{
		super.remove(ctx, oldSub);
	}
	
	private void saveSubscriberServices(Context ctx, Subscriber newSub)
	    throws HomeException
    {
        Set newServices = newSub.getAllNonUnprovisionedStateServices();
        
        if (newServices == null)
        {
           throw new HomeException("System error: newServices data is not persisted as expected");
        }

        //if((newServices.size()==0) && (newSub.getServices().size()>0) )
        {
            /*
             * The fact that new services is empty means it's not a gui operation 
             * (unless there is a price plan with no mandatory services and the user selects no services, which is extreamly rare)
             * Why? bcos getServicesForDisplay() is returning the field servicesForDisplay in subscriber which is a transient field.
             * it only gets populated during a gui create or update.
             * 
             * if for some reason there is a price plan which has no mandatory price plan and has no services selected
             * then the fact that I check the newSub.getServices() will handle the situation
             */
            
            // This is not a GUI operation.
            // Services are created or updated only during gui operation.
        //    return;
        	ServiceRefactoring_RefactoringClass.handleNonGuiOperations();
        }
        //else
        {
			
            /* Previous to version 8_0, CRM recorded the Service selection 
             * "intent" by pre-storing the Subscriber Services records (even creating
             * the new services in Pending state, and removing unselected Services).
             * In 8_0 onwards CRM will keep track of the new selection intent in 
             * the transient field Subscriber.ServicesForDisplay.
             * This information will be used to provision Services in the 
             * SubscriberProvisionServicesHome.
             * amedina You are right this has been implemented already,
             * there is one problem though, PENDING services??
             * We have to at least store those. 
             * ali Arturo is correct.
             * SubscriberServicesProvisionHome omits provisioning of PENDING Services.
             * See updatePendingServices. 
             */
            
            updatePendingServices(ctx, newSub, newServices);

            // Updating services records to reflect changes on start/end date.
            updateServicesRecords(ctx, newSub, newServices);

        }
    }


    
    private void updateServicesRecords(Context ctx, Subscriber newSub, Set newServices)
    {
        Map<ServiceFee2ID, SubscriberServices> currentServices = SubscriberServicesSupport.getSubscribersServices(ctx, newSub.getId());

        for (Iterator it = newServices.iterator(); it.hasNext();)
        {
            SubscriberServices newBean = (SubscriberServices) it.next();
            try
            {
            	ServiceFee2ID serviceFee2ID = new ServiceFee2ID(newBean.getServiceId(),newBean.getPath());
                
                SubscriberServices subCurServices = currentServices.remove(serviceFee2ID);
                // if something was removed the result is not null
                if ( subCurServices != null && (hasStartAndEndDatesChanged(subCurServices, newBean) || hasServiceQuantityChanged(subCurServices,newBean) || hasPersonalizedFeeChanged(subCurServices,newBean)) 
                		&& !subCurServices.isMandatory())
                {
                    SubscriberServicesSupport.updateSubscriberServiceRecord(ctx, newBean);
                }
            }
            catch (Exception e)
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this,e.getMessage(),e).log(ctx);
                }
            }

        }
        
    }
    
    private boolean hasServiceQuantityChanged(SubscriberServices subCurServices, SubscriberServices newBean) {
    	return subCurServices.getServiceQuantity() != newBean.getServiceQuantity();
	}
    
    private boolean hasStartAndEndDatesChanged(SubscriberServices subService1, SubscriberServices subService2)
    {
        boolean result = false;
        
        if (!subService1.getStartDate().equals(subService2.getStartDate()))
        {
            result = true;
        }
        else if (!subService1.getEndDate().equals(subService2.getEndDate()))
        {
            result = true;
        }                    
        return result;
        
    }
    /**
     * Persists newly selected PENDING services, and removes SubscriberService records
     * for unselected PENDING services.
     * @param ctx
     * @param newSub
     * @param newServices  newly selected services
     */
    private void updatePendingServices(Context ctx, Subscriber newSub, Set newServices)
    {
    	/* Persisting and Deleting Pending services have to be done separately (through different 
		 * iterations of the newServices list, because:
		 *  0) newServices is a set of SubscriberServices and we can only easily retrieve a 
		 *     set of Service Identifiers of the selected Services. These cannot be trivially compared
		 *     (On the other hand, retrieving the SubscriberServices objects and comparing them would 
		 *     perform too many unnecessary comparisons (i.e. comparing dates, states, etc.)
		 *  1) Persisting the service requires for the service id to be in the "newServices" list
		 *  2) Deleting the service requires for the service id to be absent from the "newServices" list. 
		 */
    	
    	/* This list (pendingServices) will keep track of the pendingServices that have been unselected from 
    	 * the Subscriber Profile.
		 * First, we'll initialize this list with all the PENDING Services currently with persistent records.
		 * Then we will remove from this list the services that are still selected.  After iterating through
		 * all newly selected services, we will delete all unselected PENDING services.  */
		Predicate filter = new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PENDING); 

		Map<Long, SubscriberServices> pendingServiceIdsToRemove = new HashMap<Long, SubscriberServices>();
		for(SubscriberServices svcToRemove: SubscriberServicesSupport.getSubscriberServicesByFilter(ctx, newSub.getId(), filter))
		    pendingServiceIdsToRemove.put(Long.valueOf(svcToRemove.getServiceId()), svcToRemove);
		
        final Date today = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
    	
    	// Persist newly Selected Pending Services
    	Iterator iter = newServices.iterator();
        while (iter.hasNext())
        {
        	SubscriberServices svc = (SubscriberServices) iter.next();
        	Date startDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(svc.getStartDate());

        	Date subStartDate = newSub.getStartDate();
        	if (subStartDate == null)
        	{
        	    subStartDate = new Date();
        	}
        	subStartDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subStartDate);
        	
        	//If it is postpaid subscription, we want to create services in pending and not provision them
        	if ( startDate.after(today) || ( newSub.isPostpaid() && subStartDate.after(today)))
        	{
        		try
        		{
        		    /*
                     * Comparing the IDs (not objects) so as to make it fool-proof.
                     * For instance, if Date or any field is changed, the object hash-codes
                     * won't match.
                     */
        			if (pendingServiceIdsToRemove.containsKey(Long.valueOf(svc.getServiceId())))
        			{
        				// Pending service is still selected.  Remove it from the list.
        			    pendingServiceIdsToRemove.remove(Long.valueOf(svc.getServiceId()));
        			}
        			
        			// Persist the PENDING Service
        			SubscriberServicesSupport.createOrModifySubcriberService(ctx,
        					newSub,
        					svc.getServiceId(), 
        					ServiceStateEnum.PENDING);
        		}
        		catch (HomeException e)
        		{
        			LogSupport.major(ctx, this, "Failed to create pending service=" 
        					+ svc.getServiceId() + " for subscriber=" + newSub.getId(), e);
        		}
        	}
        }
        
        // Remove all remaining services in the list of unselected PENDING services.
		for(SubscriberServices serviceRecord : pendingServiceIdsToRemove.values())
		{
        	SubscriberServicesSupport.deleteSubscriberServiceRecord(ctx,serviceRecord);
		}
    }
    
    /**
     * Run the necessary clean up to restore services for provisioning process
     * @param ctx
     * @param sub
     */
    private void cleanSubscriberServices(final Context ctx, final Subscriber sub)
    {
    	removeAllUnprovisionedServices(ctx, sub);
    	if(!BulkServiceUpdateHome.isRollbackRequired(ctx, sub))
    	{
    		retryFailedUnprovisioning(ctx, sub);
    	}
    }
    
    /**
     * Delete all Subscriber Services in the UNPROVISION (exact match) state, otherwise
     * it will be picked up by the SubscriberServicesChargingHome and be considered for
     * refunding.
     * 
     * Running this cleanup method is necessary for automatic-error correction.  From 
     * time to time subscriber services are left in the UNPROVISIONED state and are not 
     * deleted from the SubscriberServices table (probably due to exceptions during processing).
     * We can be certain that UNPROVISIONED services are those that have been removed successfully 
     * from external clients, though we cannot be sure that they were refunded correctly yet.
     * To correct missed refunds/charges we will rely on the Subscriber Services history feature
     * 
     * @param ctx
     * @param sub
     */
    private void removeAllUnprovisionedServices(final Context ctx, final Subscriber sub)
    {
    	final String subscriberId = sub.getId();
    	Predicate filter = new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.UNPROVISIONED);
		Set<SubscriberServices> cleanupServices = SubscriberServicesSupport.getSubscriberServicesByFilter(ctx, subscriberId, filter);

		for(SubscriberServices subService : cleanupServices)
	    {
		    SubscriberServicesSupport.deleteSubscriberServiceRecord(ctx, subService);
		    
	    }
    }


    /**
     * To retry Failed provisioning set the provisioned state to PROVISIONED.
     * 
     * Since the service is not selected (in Subscriber.servicesForDisplay) then it will
     * be considered for deprovisioning.
     * 
     * ali: Another way to achieve retry of Unprovisioning would have been to specifically
     * code for that case. However, to maximize code reuse, I have decided to simply mark
     * the services in UNPROVISIONEDWITHERRORS state to try the unprovisioning again. This
     * minimizes possible paths of maintenance. 
     * 
     * simar.singh@redknee.com: I think ali's
     * intent was to mark the intended services to PROVISIONEDWITHERRORS. Making changes
     * now, but will get it reviewed by ali
     * 
     * @param ctx
     * @param sub
     */
    private void retryFailedUnprovisioning(final Context ctx, final Subscriber sub)
    {
    	CRMSpid crmSpid = null;
    	try
    	{
    		crmSpid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
    	}
    	catch (HomeException ex)
    	{
    		LogSupport.minor(ctx, this, "Spid not available.");
    	}
    	final String subscriberId = sub.getId();
    	Predicate filter = new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.UNPROVISIONEDWITHERRORS);
		Set<SubscriberServices> failedUnprovisionServices = SubscriberServicesSupport.getSubscriberServicesByFilter(ctx, subscriberId, filter);
		Set<Long> serviceIdsIntendedToProvision = new HashSet<Long>();
		Set<SubscriberServices> intentToProvisionServices = sub.getIntentToProvisionServices(ctx);
		for(SubscriberServices svcToProvision: intentToProvisionServices)
		    serviceIdsIntendedToProvision.add(Long.valueOf(svcToProvision.getServiceId()));
		
		for (SubscriberServices service : failedUnprovisionServices)
		{
			try
			{
			    /*
			     * Comparing the IDs (not objects) so as to make it fool-proof.
			     */
                if (intentToProvisionServices.contains(service) || 
                        serviceIdsIntendedToProvision.contains(Long.valueOf(service.getServiceId())))
				{
                	if (crmSpid != null && !crmSpid.getSkipRetryOfProvOrUnprovOfFailedServicesOnSubscriptionUpdate()) 
            		{
                		SubscriberServicesSupport.createOrModifySubcriberService(ctx, sub, service.getServiceId(), ServiceStateEnum.PROVISIONEDWITHERRORS, service.getSuspendReason(), service);	
            		}
				}
			}
			catch (HomeException e)
			{
				new MajorLogMsg(this, "Failed to submit the Service=" + service.getServiceId() + " to reattempt unprovisioning.", e).log(ctx);
			}
		}
		
    }
   
    private boolean hasPersonalizedFeeChanged(SubscriberServices subService1, SubscriberServices subService2) {
        boolean result = false;
      
      if (subService1.getPersonalizedFee()!=subService2.getPersonalizedFee())
      {
          result = true;
      }
      if (subService1.getIsfeePersonalizationApplied()!=subService2.getIsfeePersonalizationApplied())
      {
          result = true;
      }
                
      return result;
    }

}
