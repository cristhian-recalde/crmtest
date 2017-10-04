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

package com.trilogy.app.crm.validator;

import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.core.ServiceFee2;

/**
 * This validator will check permissions of service against user. If he is not authorized user to 
 * access the price plan service or auxiliary service then do not allow user to provision/unprovision the service.
 * TT#13061425017
 * @author shailesh.makhijani
 * @since 9.6.0
 */
public class SubscriberServicesPermissionsValidator implements Validator {

    private static Validator instance;


    private SubscriberServicesPermissionsValidator()
    {
    }


    public static Validator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberServicesPermissionsValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
     public void validate(final Context ctx, final Object object) 
     {
         final Subscriber newSub = (Subscriber) object;
         final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
         
         CompoundIllegalStateException compound = new CompoundIllegalStateException();
         

         //for new subscription
         try {
             if (oldSub == null){
                 checkServicePermission(ctx, newSub.getServices(ctx));
                 checkAuxiliaryServicePermission(ctx, newSub.getAuxiliaryServiceIds(ctx));
                 return;
             } 

             //PP change
             if (newSub.getPricePlan(ctx) != null && newSub.getPricePlan() != oldSub.getPricePlan()){
                 checkServicePermission(ctx,  newSub.getPricePlan(ctx).getServices(ctx));

             }  //PP version change
             else if (newSub.getPricePlan(ctx) != null && newSub.getPricePlanVersion() != oldSub.getPricePlanVersion()){
                 checkServicePermission(ctx,  newSub.getPricePlan(ctx).getServices(ctx));

             }  //PP version update
             else if (isPricePlanVersionUpdate(ctx, newSub)){       
                 checkServicePermission(ctx,  newSub.getPricePlan(ctx).getServices(ctx));
             }
             // For update subscription
             else {
                 Set<ServiceFee2ID> oldServices = oldSub.getServices(ctx);
                 Set<ServiceFee2ID> newServices = newSub.getServices(ctx);

                 // TCBSUP-557 - Service Permissions validation failure when enable\disable service
                 // User have permissions to certain sevices.
                 // newServices contains only those services for which user have permissions
                 // need to do the same for oldServices, before Permission validation remove services for which
                 // user don't have Permissions from oldServices set.
                 if (LogSupport.isDebugEnabled(ctx))
                 {
	                 String msg = "old subscriber's [" + oldSub.getId() + "] services before removeNonPermittedServices() [" + oldServices +"]";
	                 LogSupport.minor(ctx, this, msg);
                 }
                 
                 removeNonPermittedServices(ctx, oldServices);
                 
                 if (LogSupport.isDebugEnabled(ctx))
                 {
	                 String msg = "old subscriber's [" + oldSub.getId() + "] services after removeNonPermittedServices() [" + oldServices +"]";
	                 LogSupport.minor(ctx, this, msg);
                 }
 
                 
                 Collection<ServiceFee2ID> diff  =  CollectionUtils.disjunction(oldServices, newServices);
                 
                 if (LogSupport.isDebugEnabled(ctx))
                 {
	                 String msg = "Services after disjunction() [" + diff +"]";
	                 LogSupport.minor(ctx, this, msg);
                 }
                 
                 checkServicePermission(ctx, diff);
            }

             Set<Long> oldAuxServices = oldSub.getAuxiliaryServiceIds(ctx);
             Set<Long> newAuxServices = newSub.getAuxiliaryServiceIds(ctx);

             Collection<Long> diffAuxServices  =  CollectionUtils.disjunction(oldAuxServices, newAuxServices);
             checkAuxiliaryServicePermission(ctx, diffAuxServices);
             
         }catch(HomeException e){
             compound.thrown(e);
         }
         compound.throwAll();
     }
     
     /**
      * @param context
      * @param serviceIds
      * @return serviceId
     * @throws HomeException 
      */
     private void checkServicePermission(Context ctx,  Collection<ServiceFee2ID> services) throws IllegalPropertyArgumentException, HomeException {
         /*for (ServiceFee2ID service : services) {
        	 Long serviceId = service.getServiceId();
             if(!hasAccessRights(ctx, serviceId)){
                 throw new IllegalPropertyArgumentException(SubscriberXInfo.PRICE_PLAN,
                         "User does not have sufficient priviledges to provision/unprovision Price Plan serviceId "+ serviceId );
             }
         }*/
     }

     /**
      * @param context
      * @param serviceIds
      * @return serviceId
     * @throws HomeException 
      */
     private void checkAuxiliaryServicePermission(Context ctx,  Collection<Long> serviceIds) throws IllegalPropertyArgumentException, HomeException {
         for (Long serviceId : serviceIds) {
             if(!hasAccessRightsForAuxiliaryService(ctx, serviceId)){
                 throw new IllegalPropertyArgumentException(SubscriberXInfo.AUXILIARY_SERVICES,
                         "User does not have sufficient priviledges to provision/unprovision auxiliary serviceId "+ serviceId );
             }
         }
     }
     

    private boolean hasAccessRightsForAuxiliaryService(Context ctx, long auxiliaryServiceId) throws HomeException
    {
        boolean result = true;
        AuxiliaryService auxiliaryService = AuxiliaryServiceSupport.getAuxiliaryService(ctx, auxiliaryServiceId);
        if (auxiliaryService != null)
        {
            result = SubscriberServicesSupport.hasPermission(ctx, auxiliaryService.getPermission());
        }
        return result;
    }


    private boolean hasAccessRights(Context ctx, long serviceId) throws HomeException
    {
        boolean result = true;
        Service service = ServiceSupport.getService(ctx, serviceId);
        if (service != null)
        {
            result = SubscriberServicesSupport.hasPermission(ctx, service.getPermission());
        }
        return result;
    }
     
    
    /**
     * @param context
     * @param serviceIds
     * @return void
    * @throws None 
     */
	private void removeNonPermittedServices(Context ctx,  Collection<ServiceFee2ID> serviceFee2IDs) throws IllegalPropertyArgumentException, HomeException
	{
		if (serviceFee2IDs != null && serviceFee2IDs.size() > 0) {
			for (Iterator<ServiceFee2ID> iter = serviceFee2IDs.iterator(); iter.hasNext();) {
				ServiceFee2ID serviceFee2ID = iter.next();
				if (!hasAccessRights(ctx, serviceFee2ID.getServiceId())) {
					iter.remove();
				}
			}
		}
	}
	
    /**
     * Overloaded function
     * @param context
     * @param serviceIds
     * @return serviceId
    * @throws HomeException 
     */
    private void checkServicePermission(Context ctx,  Collection<Long> serviceIds, Subscriber newSub) throws IllegalPropertyArgumentException, HomeException {
   	 final Map serviceFeesMap = SubscriberServicesSupport.getServiceFees(ctx, newSub);
        for (Long serviceId : serviceIds) 
        {    	 
        	String msg;
       	 	final ServiceFee2 fees = (ServiceFee2) serviceFeesMap.get(serviceId);
	            
	       	 // TBSSUP-557
	       	 //If the service is Mandatory, it can't be enabled or disabled so no need to check permissions for it.
	       	 if( 	(!hasAccessRights(ctx, serviceId)) && 
	           		(fees != null) && 
	           		(!fees.getServicePreference().equals(ServicePreferenceEnum.MANDATORY) ) 
	           	){
	       		 	msg = "User does not have sufficient priviledges to provision/unprovision Price Plan serviceId "+ serviceId;
		             if (LogSupport.isDebugEnabled(ctx))
		             {
		                 LogSupport.minor(ctx, this, msg);
		             }
	                throw new IllegalPropertyArgumentException(SubscriberXInfo.PRICE_PLAN, msg);
	            }
        }
    }
    
    
     /**
      * Checks if it is a Subscriber's PricePlan version update request.
      * 
      * @param ctx
      * @param newSub
      * @return
      */
     private boolean isPricePlanVersionUpdate(final Context ctx, final Subscriber newSub)
     {
         PricePlanVersion currentPricePlan = null;
         final CompoundIllegalStateException compound = new CompoundIllegalStateException();
         try
         {
             currentPricePlan = PricePlanSupport.getCurrentVersion(ctx, newSub.getPricePlan());
         }
         catch (HomeException e)
         {
             String msg = "Failed to look up current price plan version for plan " + newSub.getPricePlan();
             LogSupport.minor(ctx, this, msg + " for subscriber " + newSub.getId(), e);
             compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.PRICE_PLAN, msg));
             compound.throwAll();
         }

         if (currentPricePlan == null)
         {
             String msg = "The current price plan version NOT FOUND for plan " + newSub.getPricePlan();
             LogSupport.minor(ctx, this, msg + " for subscriber " + newSub.getId());
             compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.PRICE_PLAN, msg));
             compound.throwAll();
         }

         if (newSub.getPricePlanVersion() != currentPricePlan.getVersion())
         {
             return true;
         }

         return false;
     }

}
