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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This class is responsible for calculating the services needing provisioning, based on the
 * currently provisioned or suspended, elegible to provision, and provisioned with errors or
 * unprovisioned with errors subscriber services.
 *
 * @author Marcio Marques
 * @since 8_6
 *
 */
class ProvisionSubscriberServicesProvisioningCalculator extends AbstractSubscriberServicesProvisioningCalculator
{

    /**
     * Create a new instance of <code>ProvisionSubscriberServicesProvisioningCalculator</code>.
     *
     * @param oldSubscriber
     * @param newSubscriber
     * @param servicesToUnprovision
     */
    ProvisionSubscriberServicesProvisioningCalculator(final Subscriber oldSubscriber,
            final Subscriber newSubscriber)
    {
        super(oldSubscriber, newSubscriber);
    }


    /**
     * Calculates the list of subscriber services to be provisioned.
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

        if (isSubscriberCreation())
        {
            result = calculateServicesToProvisionOnCreation(ctx, elegibleServices);

        }
        else if (!getNewSubscriber().isInFinalState())
        {
            result = calculateServicesToProvisionOnStore(ctx, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices, currentResults);
        }
        else
        {
            result = new HashMap<Short,Set<SubscriberServices>>();
            result.put(PROVISION, new HashSet<SubscriberServices>()); // TT#12091907002
        }

        return result;
    }


    /**
     * Calculate the list of subscriber services to be provisioned during a creation.
     * @param ctx
     * @param elegibleServices
     * @return
     * @throws HomeException
     */
    private Map<Short, Set<SubscriberServices>> calculateServicesToProvisionOnCreation(final Context ctx,
            final Collection<SubscriberServices> elegibleServices) throws HomeException
    {
        final Map<Short,Set<SubscriberServices>> result = new HashMap<Short,Set<SubscriberServices>>();
        final Set<SubscriberServices> services = new HashSet<SubscriberServices>();
        result.put(this.PROVISION, services);

        if (!EnumStateSupportHelper.get(ctx).isOneOfStates(getNewSubscriber(),
                getUnprovisionStates(ctx, getNewSubscriber())))
        {
             // Provision all services elegible for provisioning.
            services.addAll(elegibleServices);

            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = createLogHeader("Provisioning all elegible subscriber services", getNewSubscriber(), false);
                LogSupport.debug(ctx, this, sb.toString(), null);
            }
        }
        else
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = createLogHeader("Subscriber doesn't need service provisioning as it's been created in an unprovisioned state",
                        getNewSubscriber(), false);
                LogSupport.debug(ctx, this, sb.toString(), null);
            }
        }

        return result;
    }

    /**
     * Calculates services to be provisioned taking in consideration the services being unprovisioned.
     * @param ctx
     * @param elegibleServices
     * @param currentlyProvisionedServices
     * @param currentlySuspendedServices
     * @throws HomeException
     */
    private Map<Short, Set<SubscriberServices>> calculateServicesToProvisionOnStore(final Context ctx,
            final Collection<SubscriberServices> elegibleServices,
            final Collection<SubscriberServices> currentlyProvisionedServices,
            final Collection<SubscriberServices> currentlySuspendedServices,
            final Map<Short, Set<SubscriberServices>> currentResults) throws HomeException
    {
        final Map<Short,Set<SubscriberServices>> result = new HashMap<Short,Set<SubscriberServices>>();
        final Set<SubscriberServices> services = new HashSet<SubscriberServices>();
        final Set<SubscriberServices> servicesToUnprovision = currentResults.get(UNPROVISION);
        result.put(this.PROVISION, services);

        // Since all the checkboxes stay selected on deactivation, we don't want to
        // reprovision those services. Hence, we don't all add back those services
        if (!EnumStateSupportHelper.get(ctx).isEnteringState(this.getOldSubscriber(), this.getNewSubscriber(),
                SubscriberStateEnum.INACTIVE))
        {
            services.addAll(elegibleServices);
        }

        // Currently provisioned and suspended services shouldn't be provisioned
        services.removeAll(ServiceChargingSupport.getCrossed(ctx,services, currentlyProvisionedServices));
        services.removeAll(ServiceChargingSupport.getCrossed(ctx,services, currentlySuspendedServices));

        // Adds services that need reprovision.
        if (needsReprovisionToActive(ctx))
        {
            List<SubscriberServices> servicesThatNeedReprovision = getServicesToReprovision(ctx);
            servicesThatNeedReprovision.remove(ServiceChargingSupport.getCrossed(ctx, servicesThatNeedReprovision,
                    services));
            services.addAll(servicesThatNeedReprovision);
        }

        // Services unprovisioned that are expected to be provisioned should be reprovisioned.
        if (servicesToUnprovision != null
                && servicesToUnprovision.size() > 0
                && (!EnumStateSupportHelper.get(ctx).isEnteringState(this.getOldSubscriber(), this.getNewSubscriber(),
                        SubscriberStateEnum.INACTIVE)))
        {
            Collection<SubscriberServices> unprovisionedServicesSupposedToBeProvisioned = new HashSet<SubscriberServices>(elegibleServices);
            unprovisionedServicesSupposedToBeProvisioned.retainAll(ServiceChargingSupport.getCrossed(ctx,unprovisionedServicesSupposedToBeProvisioned, servicesToUnprovision));
            services.addAll(unprovisionedServicesSupposedToBeProvisioned);
        }

        return result;
    }


    /**
     * Verifies if state change requires services with "Reprovision To Active" to be reprovisioned.
     * @param ctx
     * @return
     */
    private boolean needsReprovisionToActive(final Context ctx)
    {
        boolean result = false;
        if (getNewSubscriber() != null)
        {
            if (getNewSubscriber().isPostpaid())
            {
                /*
                 * These state transitions are defined in the state transition table,
                 * annotated with 'Based on the Reprovision on Active field'
                 * Documentation: SgR.CRM.1343, HLD OID 6964 (old)
                 */
                result = EnumStateSupportHelper.get(ctx).isTransition(
                        getOldSubscriber(),
                        getNewSubscriber(),
                        Arrays.asList(SubscriberStateEnum.SUSPENDED, SubscriberStateEnum.NON_PAYMENT_SUSPENDED,
                                SubscriberStateEnum.NON_PAYMENT_WARN, SubscriberStateEnum.IN_ARREARS,
                                SubscriberStateEnum.IN_COLLECTION),
                        Arrays.asList(SubscriberStateEnum.ACTIVE, SubscriberStateEnum.PROMISE_TO_PAY));
            }
            else
            {
                // true prepaid
                final SysFeatureCfg config = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
                if (config.isForceReprovisionForPrepaid())
                {
                    result = EnumStateSupportHelper.get(ctx).isTransition(getOldSubscriber(), getNewSubscriber(), SubscriberStateEnum.INACTIVE,
                            SubscriberStateEnum.AVAILABLE);
                }
            }
        }
        return result;
    }


    /**
     * Returns the set of services which needs to be reprovisioned.
     *
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param sub
     *            The new subscriber.
     * @return Set of services which needs to be reprovisioned.
     * @throws HomeException
     *             Thrown if there are problems determining what services should be
     *             reprovisioned.
     */
    private List<SubscriberServices> getServicesToReprovision(final Context ctx) throws HomeException
    {
        final List<SubscriberServices> servicesNeedReProvision = new ArrayList<SubscriberServices>();
        if (getNewSubscriber() != null)
        {
            // it already excludes CLCT services whose threshold has been crossed
            Collection<SubscriberServices> provisionedSubSvcs = getNewSubscriber().getProvisionSubServicesExpected(ctx);
            for (final SubscriberServices subService : provisionedSubSvcs)
            {
                Service service = subService.getService(ctx);
                if ((getNewSubscriber().isPrepaid() && subService.getProvisionedState().equals(ServiceStateEnum.SUSPENDED)) ||
                        service.isReprovisionOnActive())
                {
                    servicesNeedReProvision.add(subService);
                }
            }
        }

        if (LogSupport.isDebugEnabled(ctx) && servicesNeedReProvision.size()>0)
        {
            StringBuilder sb = createLogHeader("Services in need of reprovision calculation", getNewSubscriber(), true);
            appendServicesListToLog(sb, "Services to be reprovisioned", servicesNeedReProvision, false);
            LogSupport.debug(ctx, this, sb.toString(), null);
        }

        return servicesNeedReProvision;
    }

}
