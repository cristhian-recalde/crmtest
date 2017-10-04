package com.trilogy.app.crm.refactoring;

import com.trilogy.framework.xhome.home.HomeException;

/**
 * Since Service Refactoring is such a huge task, I had to resort to making up a fake
 * class to keep track of all the areas that still needed refactoring.
 * 
 * This class Identifies all areas of refactoring still to be completed for the 
 * Service Refactoring effort.
 * Simply trace to where these methods are being used to find out refactoring areas.
 * Also, once the areas of refactoring have been addressed, then remove the unused methods
 * from this class
 * @author ali
 *
 */
public final class ServiceRefactoring_RefactoringClass 
{
	public static void lookupSuspendedServicesUsingSubscriberServices()
		throws HomeException
	{
		/* Change the calling method to call
		       SubscriberServicesSupport.getSuspendedServices(context, subscriberId);
		 if the calling method really deals with querying for Services.  If it is 
		 querying for Service Packages, Bundles, Aux Services leave it alone. */
		
		// CRM 8.0, Service Refactoring, from now on Subscriber get all Suspended Services is done by querying the 
    	// SubscriberServices table
        throw new HomeException("System Error: Checking for Suspended Services using Suspended Entities is not supported.");
	}
	
	public static void unsuspendSuspendedServicesUsingSubscriberServices()
		throws HomeException
	{
		/* Change the calling method to use
		 * 		Subscriber.removeSuspendedService (removes from suspendedEntity table and activates service)
		 * But this has to be carefully thought through.  By removing services from Suspended Entities (old implementation)
		 * does this necessarily mean that we are activating the service?
		 * Subscriber.removeSuspendedService should probably delegate to a SubscriberServicesSupport method.
		 */
		// CRM 8.0, Service Refactoring, from now on Subscriber get all Suspended Services is done by querying the 
    	// SubscriberServices table
        throw new HomeException("System Error: \"Unsuspending\" a service no longer should be done through " +
        		"Suspended Entities.  Use SubscriberServicesSupport methods.");
	}
	
	public static boolean isServiceSuspendedUsingSubscriberServices()
		throws HomeException
	{
		/* Change the calling method to use a support method in 
		 *    SubscriberServicesSupport (not yet implemented) 
		 * to determine whether or not the given service is Suspended. 
		 * Possibly by calling Subscriber 
		 */
		// CRM 8.0, Service Refactoring, from now on Subscriber get all Suspended Services is done by querying the 
    	// SubscriberServices table
        throw new HomeException("System Error: Checking whether a Service is Suspended should no longer " +
        		"be done through Suspended Entities.  Use SubscriberServicesSupport methods.");
	}
	
	public static void doesRemoveSuspendedServiceMeanDeleteFromSuspendedEntityOrActivate(boolean activate)
	{
		/*
		 * I noticed in some places "remove Suspended Service" means:
		 *   + Deleting deleting from Suspended Entity and Subscriber.suspendedServices_ (the service falls off the Subscriber profile) (A)
		 *   + Delete from Suspended Entity because the service is now "Active" 
		 *   review all uses and make distinct calls to
		 *   A) deleteSuspendedService  (clear)
		 *   B) activateSuspendedService
		 *   
		 *   related issue: deleteAllSubscriberServicesWhenDeactivatingOrExpiring()
		 */
	}
	
	public static void deleteAllSubscriberServicesWhenDeactivatingOrExpiring()
	{
		/*
		 * On Subscriber Deactivation and Expiry, do we delete all the Subscriber Services?
		 */
	}
	
	public static void implementRealSubscriberServiceSuspendedDueToCLTC()
	{
		/* For Service Refactoring Phase I, CLTC suspended services will be
		 * indicated by a state PROVISIONED + Suspend Reason CLTC. 
		 * In the latter phases, when service provisioning and charging are
		 * done at the same time, the mode to indicate CLTC suspension should
		 * be state=SUSPENDED + reason=CLTC.
		 * How to deal with Charging??*/
	}
	
	public static void createNotesForExpiredServices()
	{
		/*
		 * Expired services are simply unprovisioned.  But the Subscriber Note must say that the 
		 * Service has been EXPIRED.  Make sure this doesn't depend on the old ServiceStateEnum.EXPIRED.
		 */
	}
	
	public static void defineProvisionedService()
	{
		/*
		 * Are provisioned services only those that have state ServiceStateEnum.PROVISIONED?
		 * or is ServiceStateEnum.PROVISIONEDWITHERRORS provisioned?
		 * 
		 * I think the original design called only for ServiceStateEnum.PROVISIONED.  All other
		 * states have to resolve to ServiceStateEnum.PROVISIONED, before being "provisioned".
		 * 
		 * ServiceStateEnum.PROVISIONEDWITHERRORS is this charged?
		 */
	}
	
	public static void handleChargingExceptions()
	{
		/* 
		 * Handle the exceptions due to charging and creating ServiceProvisioningError records (and
		 * updating the SubscriberService record states appropriately.
		 */
		com.redknee.app.crm.bean.service.ServiceStateEnum state = com.redknee.app.crm.bean.service.ServiceStateEnum.UNPROVISIONEDWITHERRORS;
		state = com.redknee.app.crm.bean.service.ServiceStateEnum.PROVISIONEDWITHERRORS;
		//state = com.redknee.app.crm.bean.service.ServiceStateEnum.SUSPENDEDWITHERRORS;
	}
	
	public static void handleNonGuiOperations()
	{
		//We can no longer rely on Subscriber.serviceForDisplay being empty to indicate a non-gui operation.
	}
	
}
