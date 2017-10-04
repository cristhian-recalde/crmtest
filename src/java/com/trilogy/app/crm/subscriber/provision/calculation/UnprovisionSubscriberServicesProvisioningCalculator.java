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
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;


/**
 * This class is responsible for calculating the services needing unprovisioning, based on the
 * currently provisioned or suspended, elegible to provision, and provisioned with errors or
 * unprovisioned with errors subscriber services.
 * 
 * @author Marcio Marques
 * @since 8_6
 * 
 */
class UnprovisionSubscriberServicesProvisioningCalculator extends AbstractSubscriberServicesProvisioningCalculator
{

    /**
     * Create a new instance of <code>UnprovisionSubscriberServicesProvisioningCalculator</code>.
     * 
     * @param oldSubscriber
     * @param newSubscriber
     */
    UnprovisionSubscriberServicesProvisioningCalculator(final Subscriber oldSubscriber, final Subscriber newSubscriber)
    {
        super(oldSubscriber, newSubscriber);
    }
    

    /**
     * Calculates the list of subscriber services to be unprovisioned.
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
            final Map<Short, Set<SubscriberServices>> currentResults) throws HomeException
    {
        final Map<Short, Set<SubscriberServices>> result;
        
        if (!isSubscriberCreation())
        {
            result = calculateServicesToUnprovisionOnStore(ctx, servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices);
        }
        else
        {
            result = new HashMap<Short, Set<SubscriberServices>>();
            final Set<SubscriberServices> services = new HashSet<SubscriberServices>();
            result.put(UNPROVISION, services);
        }
        
        return result;
    }

    
    /**
     * Calculates the list of subscriber services to be unprovisioned on a store.
     * 
     * @param ctx
     * @param servicesToRetry
     * @param elegibleServices
     * @param currentlyProvisionedServices
     * @param currentlySuspendedServices
     * @return
     * @throws HomeException
     */
    protected Map<Short, Set<SubscriberServices>> calculateServicesToUnprovisionOnStore(final Context ctx,
            final Collection<SubscriberServices> servicesToRetry, final Collection<SubscriberServices> elegibleServices,
            final Collection<SubscriberServices> currentlyProvisionedServices,
            final Collection<SubscriberServices> currentlySuspendedServices) throws HomeException
    {
        final Map<Short,Set<SubscriberServices>> result;

        if (needsFullUnprovision(ctx))
        {
            if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
            {
                StringBuilder sb = createLogHeader("Full subscriber services unprovision required",
                        getOldSubscriber(), false);
                LogSupport.info(ctx, this, sb.toString(), null);
            }
            
            result = calculateServicesToUnprovisionForFullUnprovision(ctx, servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices);
        }
        else
        {
            result = calculateServicesToUnprovisionForDeltaUnprovision(ctx, servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices);
        }
        
        return result;
    }

    
    /**
     * Calculates services to be unprovisioned during a full unprovision.
     * @param ctx
     * @param servicesToRetry
     * @param elegibleServices
     * @param currentlyProvisionedServices
     * @param currentlySuspendedServices
     * @throws HomeException
     */
    private Map<Short, Set<SubscriberServices>> calculateServicesToUnprovisionForFullUnprovision(final Context ctx,
            final Collection<SubscriberServices> servicesToRetry, final Collection<SubscriberServices> elegibleServices,
            final Collection<SubscriberServices> currentlyProvisionedServices,
            final Collection<SubscriberServices> currentlySuspendedServices) throws HomeException
    {
        final Map<Short,Set<SubscriberServices>> result = new HashMap<Short,Set<SubscriberServices>>();
        final Set<SubscriberServices> services = new HashSet<SubscriberServices>();
        result.put(this.UNPROVISION, services);

        services.addAll(currentlyProvisionedServices);
        services.addAll(currentlySuspendedServices);

        // Unprovision all suspended services only if it's deactivation or price plan change.
        // TODO: Right now we only unprovision prepaid suspended services, as for postpaid
        // suspended services they might be already unprovisioned. We should fix the
        // Suspend/Resume agents for all services to to actually only suspend or resume
        // the service, and the Unprovision to not throw an error it the service is not
        // provisioned.
        boolean includeSuspendedServices = getOldSubscriber() != null
                && getOldSubscriber().isPrepaid()
                && (isDeactivation(ctx)
                    || !isSamePricePlanVersion());  
        
        boolean includeSuspendedServices_postpaid = getOldSubscriber() != null
                && getOldSubscriber().isPostpaid()
                && (isDeactivation(ctx));
        
        if(!includeSuspendedServices_postpaid)
        {//Go in If only if subscriber is not postpaid and call is not Deactivation.For Postpaid deactivation all suspended services should be unprovisioned , they should not be removed as below.
            if (!includeSuspendedServices)
            {
                services.removeAll(ServiceChargingSupport.getCrossed(ctx, currentlySuspendedServices,
                    elegibleServices));
            }
        }

        // Don't unprovision services that are expected to be provisioned and are clct
        // enabled if the subscriber is cltc suspended, as they will get suspended again.
        boolean clctRemovalNotRequired = !isDeactivation(ctx)
                && isSamePricePlanVersion();
        
        if (clctRemovalNotRequired)
        {
            Collection<SubscriberServices> expectedCLCTServices = getExpectedCLCTSuspendedServices(ctx);
            services.removeAll(ServiceChargingSupport.getCrossed(ctx, services, expectedCLCTServices));
        }
        
        // Services with errors should also be unprovisioned, as they will be reprovisioned later.
        if (servicesToRetry != null)
        {
            // Removing services already being unprovisioned from the list of service with
            // errors.
            servicesToRetry.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToRetry, 
                    services));
            services.addAll(servicesToRetry);
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = createLogHeader("Full services unprovisioning calculation", 
                    getNewSubscriber(), true);
            sb.append("includeSuspendedServices = ");
            sb.append(includeSuspendedServices);
            sb.append(", clctRemovalNotRequired = ");
            sb.append(clctRemovalNotRequired);
            LogSupport.debug(ctx, this, sb.toString(), null);
        }

        return result;
    }
    

    /**
     * Calculates services to be unprovisioned during a delta unprovisioning.
     * @param ctx
     * @param servicesToRetry
     * @param elegibleServices
     * @param currentlyProvisionedServices
     * @param currentlySuspendedServices
     * @return
     * @throws HomeException
     */
    private Map<Short, Set<SubscriberServices>> calculateServicesToUnprovisionForDeltaUnprovision(final Context ctx,
            final Collection<SubscriberServices> servicesToRetry, final Collection<SubscriberServices> elegibleServices,
            final Collection<SubscriberServices> currentlyProvisionedServices,
            final Collection<SubscriberServices> currentlySuspendedServices) throws HomeException
    {
        final Map<Short,Set<SubscriberServices>> result = new HashMap<Short,Set<SubscriberServices>>();
        final Set<SubscriberServices> services = new HashSet<SubscriberServices>();
        result.put(this.UNPROVISION, services);
        
        // Any service currently provisioned or suspended which is not selected should be unprovisioned.
        services.addAll(currentlyProvisionedServices);
        services.addAll(currentlySuspendedServices);
        services.removeAll(ServiceChargingSupport.getCrossed(ctx,services, elegibleServices));
        
        // Services with errors should also be unprovisioned, as they will be reprovisioned later.
        if (servicesToRetry != null)
        {
            // Removing services already being unprovisioned from the list of service with
            // errors.
            servicesToRetry.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToRetry, 
                    services));
            services.addAll(servicesToRetry);
        }
        
        return result;
    }

}
