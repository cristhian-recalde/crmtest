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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.UpdateReasonEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.priceplan.PricePlanVersionUpdateAgent;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.priceplan.PricePlanVersionUpdateAgent;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
/**
 *
 * Updates subscriber price plan version to the current version if they're transitioning to ACTIVE state 
 * from SUSPENDED or EXPIRED state to give the validators the expected price plan version rather than 
 * the version the subscriber used when they became expired, suspended.
 * 
 * @author ltang
 */
public class SubscriberPricePlanUpdateHome extends HomeProxy
{

    public SubscriberPricePlanUpdateHome(Home delegate)
    {
        super(delegate);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        Subscriber newSub = (Subscriber)obj;
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        PricePlan pp = null;
        
        try
        {
       	 	final Home pricePlanHome = (Home) ctx.get(PricePlanHome.class);
         	 pp = (PricePlan) pricePlanHome.find(ctx, Long.valueOf(newSub.getPricePlan()));
            
        }
        catch (final HomeException exception)
        {
            String msg = "Failed to look up current price plan version for plan "
                + newSub.getPricePlan();
            
            new MinorLogMsg(
                    this,
                    msg
                    + " for subscriber "
                    + newSub.getId(),                      
                    exception).log(ctx);
        }
        
        // TT 7070950686: We want subscribers in a state transition of Expired -> Active or 
        // Suspended -> Active to trigger a price plan version update in order to pass validation
        // of member bundles during reactivation
        if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, FROM_VALID_STATES, SubscriberStateEnum.ACTIVE)
        		// TT 12050455039
        		// As per new implementation Expired Subscriber's state in DB is still active. So this is kind of hack where even if Old State and New States
        		// are active, we are checking for PricePlan version update. No harm for already active Subscribers.
        		|| ( (oldSub.getStateWithExpired().getIndex() == SubscriberStateEnum.EXPIRED_INDEX) && (newSub.getStateWithExpired().getIndex() == SubscriberStateEnum.ACTIVE_INDEX)) )
        {
            final PricePlanVersion currentVersion;

            try
            {
                currentVersion = PricePlanSupport.getCurrentVersion(ctx, newSub.getPricePlan());
            }
            catch (final HomeException exception)
            {
                String msg = "Failed to look up current price plan version for plan "
                    + newSub.getPricePlan();
                
                new MinorLogMsg(
                        this,
                        msg
                        + " for subscriber "
                        + newSub.getId(),                      
                        exception).log(ctx);
                
                throw new HomeException(msg);
            }
        	
            if (newSub.getPricePlanVersion() != currentVersion.getVersion())
            {
                // Need to reset not only the price plan version id but the provisioned services as well
                // or else the charging home will calculate rates from the old services for the new service
                newSub.switchPricePlan(ctx, currentVersion.getId(), currentVersion.getVersion()); 
                ctx.put(PRICE_PLAN_VERSION_AND_STATE_CHANGE, true);
            }
        }else if(newSub.getPricePlan() != oldSub.getPricePlan() && newSub.getUpdateReason() == UpdateReasonEnum.BULKLOAD)
        {
        	newSub.switchPricePlan(ctx,newSub.getPricePlan(), newSub.getPricePlanVersion());
        }
        
        // TCBSUP-542 Condition - Subscriber chooses to unsubscribe a default bundle on Price plan. 
        // Later a new price plan version is created.
        // When 'Priceplan version update agent..' task runs to apply the new priceplan version on Subscriber it
        // reprovisions the default bundle which Subscriber had chosen to unsubscribe earlier.
        
        
        if( oldSub != null && newSub.getPricePlan() == oldSub.getPricePlan() && 
        		newSub.getPricePlanVersion() != oldSub.getPricePlanVersion()
        	)
        {
        	// Identify and remove previously un-subscribed Bundles first
        	
        	Set <Long> bundlesToRemove = getBundlesToUnprovision(ctx, oldSub, newSub, pp);
        	if(bundlesToRemove != null)
        	{
	       	 	if (LogSupport.isDebugEnabled(ctx))
	            {
	                String msg = "Bundles to Remove [" + bundlesToRemove +"]";
	                LogSupport.debug(ctx, this, msg);
	            }
	        	
	       	 	newSub.getBundles().keySet().removeAll(bundlesToRemove);
        	}
       	 	
        	// Identify and remove previously un-subscribed services now
        	Set <Long> servicesToRemove = getServicesToUnprovision(ctx, oldSub, newSub, pp);
        	
        	if(servicesToRemove != null)
        	{
	       	 	if (LogSupport.isDebugEnabled(ctx))
	            {
	                String msg = "Services to Remove [" + servicesToRemove +"]";
	                LogSupport.debug(ctx, this, msg);
	            }
	        	
	       	 	newSub.getServices(ctx).removeAll(servicesToRemove);

	       	 	// Remove services from newSub.intentToProvisionServices_
	       	 	for(Long serviceId: servicesToRemove)
	       	 	{
	       	 		newSub.removeServiceFromIntentToProvisionServices(ctx, serviceId);
	       	 	}
        	}
        	
        }
        
        return super.store(ctx, newSub);
    }

    private Map deepcloneSubnewlyProvisionedBundleMap(Map bundles) {
		
   	 final Map<Long, BundleFee> newlyProvisionedBundleMap = new HashMap<Long, BundleFee>();
   	 newlyProvisionedBundleMap.putAll(bundles);
   	 return newlyProvisionedBundleMap;
   	
	}

    private Set<Long> getBundlesToUnprovision(Context ctx, Subscriber oldSub, Subscriber newSub, PricePlan pp) throws HomeException
    {
    	if(oldSub == null)
    		return null;
    	// Get old PP bundles
    	PricePlanVersion oldsubPPVersion = PricePlanSupport.getVersion(ctx, pp, oldSub.getPricePlanVersion()); 
        Map<Long, BundleFee> oldSubPPBundle = SubscriberBundleSupport.getPricePlanBundles(ctx,pp,oldsubPPVersion);
       
        // Get New PP bundles
        PricePlanVersion newsubPPVersion = PricePlanSupport.getVersion(ctx, pp, newSub.getPricePlanVersion());
        Map<Long, BundleFee> newSubPPBundle = SubscriberBundleSupport.getPricePlanBundles(ctx,pp,newsubPPVersion);
        
        // Get old Subscribed bundles
    	Set<Long> oldsubBundleSet = oldSub.getBundles().keySet();
    	
        // working map
        //final Map newsubBundleMap = deepcloneSubnewlyProvisionedBundleMap(newSub.getBundles());
        final Map newsubBundleMap = deepcloneSubnewlyProvisionedBundleMap(newSubPPBundle);
        
    	//Keep all bundles in new map which exist in old PP version
    	newsubBundleMap.keySet().retainAll(oldSubPPBundle.keySet());
    	
    	//Remove all bundles which are currently subscribed by subscriber
    	//What remains is the bundles which are NOT subscribed by subscriber, which should not be checked while applying new PP version
    	newsubBundleMap.keySet().removeAll(oldsubBundleSet);       	      	
    	
    	//Finally iterate through all bundles and check if any of the bundle was Optional in previous PP version and
    	//Mandatory or Default in new version. If found then remove it from current set. Such bundle should be checked by default.
    	final Iterator it = newsubBundleMap.entrySet().iterator();
    	
       	 while (it.hasNext())
         {
             final Map.Entry entry = (Map.Entry) it.next();
             final Long key = (Long) entry.getKey();
             final BundleFee bundle = (BundleFee) entry.getValue();
             final ServicePreferenceEnum mode = bundle.getServicePreference();
             
    			if(oldSubPPBundle.keySet().contains(key))  
    			{
    				final BundleFee oldbundle = (BundleFee)oldSubPPBundle.get(key);
    				final ServicePreferenceEnum oldmode = oldbundle.getServicePreference();
		             if ( 	(mode == ServicePreferenceEnum.MANDATORY || mode == ServicePreferenceEnum.DEFAULT) && 
		            		(oldmode == ServicePreferenceEnum.OPTIONAL) )
		             {
		            	 it.remove();
		             }
    			}

         }   	
    	
       	 return newsubBundleMap.keySet();
    }
    
   
    private Set<Long> getServicesToUnprovision(Context ctx, Subscriber oldSub, Subscriber newSub, PricePlan pp) throws HomeException
    {
    	if(oldSub == null)
    		return null;
    	// Get old PP version
    	com.redknee.app.crm.bean.core.PricePlanVersion oldsubPPVersion = PricePlanSupport.getVersion(ctx, pp, oldSub.getPricePlanVersion()); 
    	Map<ServiceFee2ID, ServiceFee2> oldSubPPServices = oldsubPPVersion.getServiceFees(ctx);
    	
        // Get New PP version
        com.redknee.app.crm.bean.core.PricePlanVersion newsubPPVersion = PricePlanSupport.getVersion(ctx, pp, newSub.getPricePlanVersion());
        Map<ServiceFee2ID, ServiceFee2> newSubPPServices = newsubPPVersion.getServiceFees(ctx);
        
   	 	//Get Old PP Version Services
   	 	Set<ServiceFee2ID> oldPPServices = oldSubPPServices.keySet();
   	 
   	 	//Get New PP Version Services
   	 	Set<ServiceFee2ID> newPPServices = newSubPPServices.keySet();
   	 	
   	 	// Old Subscribers services
   		Set<ServiceFee2ID> oldsubServices =  oldSub.getServices(ctx);
   		
   		final Map newsubPPServicesMap = deepcloneSubnewlyProvisionedBundleMap(newSubPPServices);
   		
    	//Keep all services in new map which exist in old PP version
   		newsubPPServicesMap.keySet().retainAll(oldSubPPServices.keySet());
    	
    	//Remove all bundles which are currently subscribed by subscriber
    	//What remains is the bundles which are NOT subscribed by subscriber, which should not be checked while applying new PP version
   		newsubPPServicesMap.keySet().removeAll(oldsubServices);       	      	
    	
    	//Finally iterate through all bundles and check if any of the bundle was Optional in previous PP version and
    	//Mandatory or Default in new version. If found then remove it from current set. Such bundle should be checked by default.
    	final Iterator it = newsubPPServicesMap.entrySet().iterator();
    	
       	 while (it.hasNext())
         {
             final Map.Entry entry = (Map.Entry) it.next();
             final Long key = (Long) entry.getKey();
             final ServiceFee2 fees = (ServiceFee2) entry.getValue();
             final ServicePreferenceEnum mode = fees.getServicePreference();
             
             
    			if(oldSubPPServices.keySet().contains(key))  
    			{
    				final ServiceFee2 oldService = (ServiceFee2)oldSubPPServices.get(key);
    				final ServicePreferenceEnum oldmode = oldService.getServicePreference();
		             if ( 	(mode == ServicePreferenceEnum.MANDATORY || mode == ServicePreferenceEnum.DEFAULT) && 
		            		(oldmode == ServicePreferenceEnum.OPTIONAL) )
		             {
		            	 it.remove();
		             }
    			}

         }  
   		
   		return newsubPPServicesMap.keySet();
    }
    
    public static String PRICE_PLAN_VERSION_AND_STATE_CHANGE = "PricePlanVersionAndStateChange";
   
    // Go to PricePlanVersiionUpdateVistor - You will we skip PPV Version update for thes
    // states, hence we must update to latest PPV when the sub transitions to active state
    // from these states. 
    public static final Set<SubscriberStateEnum> FROM_VALID_STATES = Collections
            .unmodifiableSet(new HashSet<SubscriberStateEnum>(Arrays.asList(SubscriberStateEnum.SUSPENDED,
                    SubscriberStateEnum.EXPIRED, SubscriberStateEnum.IN_COLLECTION, SubscriberStateEnum.IN_ARREARS)));
    
}
