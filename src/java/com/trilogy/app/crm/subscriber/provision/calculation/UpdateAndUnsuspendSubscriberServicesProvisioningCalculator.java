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
package com.trilogy.app.crm.subscriber.provision.calculation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * This class is responsible for calculating the services needing update and unsuspension, based on
 * the currently provisioned or suspended, elegible to provision, and provisioned with errors or
 * unprovisioned with errors subscriber services.
 * 
 * @author Marcio Marques
 * @since 8_6
 * 
 */
class UpdateAndUnsuspendSubscriberServicesProvisioningCalculator extends AbstractSubscriberServicesProvisioningCalculator
{
    
    /**
     * Create a new instance of <code>UpdateAndUnsuspensionSubscriberServicesProvisioningCalculator</code>.
     * 
     * @param oldSubscriber
     * @param newSubscriber
     */
    UpdateAndUnsuspendSubscriberServicesProvisioningCalculator(final Subscriber oldSubscriber, final Subscriber newSubscriber)
    {
        super(oldSubscriber, newSubscriber);
    }
    
    
    /**
     * Calculates the list of subscriber services to be updated and unsuspended.
     * 
     * @param ctx
     * @param servicesToRetry
     * @param elegibleServices
     * @param currentlyProvisionedServices
     * @param currentlySuspendedServices
     * @return
     * @throws HomeException
     */
    protected Map<Short, Set<SubscriberServices>> calculate(final Context ctx,
            final Collection<SubscriberServices> servicesToRetry, final Collection<SubscriberServices> elegibleServices,
            final Collection<SubscriberServices> currentlyProvisionedServices,
            final Collection<SubscriberServices> currentlySuspendedServices,
            final Map<Short, Set<SubscriberServices>> currentServices) throws HomeException
    {
        final Map<Short, Set<SubscriberServices>> result = new HashMap<Short, Set<SubscriberServices>>();
        final Set<SubscriberServices> servicesToUnsuspend = new HashSet<SubscriberServices>();
        final Set<SubscriberServices> servicesToUpdate = new HashSet<SubscriberServices>();
        result.put(this.UNSUSPEND, servicesToUnsuspend);
        result.put(this.UPDATE, servicesToUpdate);

        // Only needed when it's a price plan (version) change and no full unprovision is done.
        //TODO: We need even if it's full unprovision if subscriber is in CLCT, as some services are CLCT suspended and 
        // therefore won't be updated.
        if (!isSubscriberCreation() && !isSamePricePlanVersion() && !getNewSubscriber().isInFinalState() && (!needsFullUnprovision(ctx) || isCLCTSuspended()))
        {
            
            // We need the intent to provision as we also need the services that will be activated in the future.
            Set<SubscriberServices> services = getNewSubscriber().getIntentToProvisionServices(ctx);

            for (final SubscriberServices service : services)
            {
                SubscriberServices existingService = SubscriberServicesSupport.getSubscriberServiceRecord(ctx, getOldSubscriber()
                        .getId(), service.getServiceId(), service.getPath());
                
                if (existingService!=null)
                {
                    if (isUpdateNeeded(existingService, service))
                    {
                        servicesToUpdate.add(existingService);
                    }

                    if (isUnsuspensionNeeded(existingService))
                    {
                        servicesToUnsuspend.add(existingService);
                        servicesToUpdate.add(existingService);
                    }
                }        
            }
        }
        
        return result;
    }
    


    /**
     * Verify if update is needed. If so, update service mandatory field, and its service
     * period, start date, and end date.
     * 
     * @param existingService
     * @param service
     * @return
     */
    private boolean isUpdateNeeded(final SubscriberServices existingService, final SubscriberServices service)
    {
        // If it's a price plan change update services with different mandatory or service
        // period data, update service.
        boolean result = false;
        if (existingService.getMandatory() != service.getMandatory()
                || existingService.getServicePeriod() != service.getServicePeriod())
        {
            existingService.setMandatory(service.getMandatory());
            existingService.setServicePeriod(service.getServicePeriod());
            existingService.setStartDate(service.getStartDate());
            existingService.setEndDate(service.getEndDate());
            result = true;
        }
        
        return result;
    }
    
    
    /**
     * Verify if unsuspension is needed. If so, update the subscriber provisioned state.
     * 
     * @param existingService
     * @return
     */
    private boolean isUnsuspensionNeeded(SubscriberServices existingService)
    {
        boolean result = false;
        
        // Suspended services should be updated and unsuspended as they will be recharged (in case of prepaid).
        if ((getNewSubscriber().isPrepaid() && existingService.getProvisionedState().equals(ServiceStateEnum.SUSPENDED))
                || (getNewSubscriber().isPostpaid()
                        && existingService.getProvisionedState().equals(ServiceStateEnum.SUSPENDED) && SuspendReasonEnum.CLCT
                            .equals(existingService.getSuspendReason())))
        {
            existingService.setProvisionedState(ServiceStateEnum.PROVISIONED);
            existingService.setSuspendReason(SuspendReasonEnum.NONE);
            result = true;
        }
        
        return result;
    }
}
