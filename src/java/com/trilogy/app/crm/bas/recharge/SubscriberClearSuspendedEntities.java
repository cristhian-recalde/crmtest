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

package com.trilogy.app.crm.bas.recharge;

import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.filter.AuxiliaryServiceByIdentifier;

import java.util.*;

/**
 * After going through Subscriber Services provisioning and charging in the Subscriber
 * pipeline, clean up after suspended services.
 * 
 * If the Subscriber has suspended any Services, Bundles, Auxiliary Services, Service Packages,
 * clean up after suspended services
 * 
 * Cleaning up:
 * Remove the Suspended Service if:
 * 	+ Subscriber enters the Expired or Deactivated state 
 *  + the Service is not among the Subscriber's "selected services"
 *
 */
public class SubscriberClearSuspendedEntities extends HomeProxy
{
    public SubscriberClearSuspendedEntities(Home delegate)
    {
        super(delegate);
    }

    public Object store(Context ctx, Object obj) throws HomeException
    {
        Subscriber sub = (Subscriber) super.store(ctx, obj);
        if (sub.hasSuspended(ctx))
        {
            clearSuspendedUnprovisioned(ctx, sub);
        }
        // TODO 2007-03-13 clear all suspended if sub is deactivated or expired

        return sub;
    }

    private void clearSuspendedUnprovisioned(Context ctx, Subscriber sub)
    {
        Iterator it;
        PricePlanVersion version = null;

        try
        {
            version = sub.getRawPricePlanVersion(ctx);
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, this, "Cannot obtain PricePlan for sub = " + sub.getId(), e);
        }

        if (version == null)
        {
            return;
        }

        // if sub is INNACTIVE, clear all records
        boolean clearAll = sub.getState() == SubscriberStateEnum.INACTIVE;

        try
        {
            Map packageFees = version.getServicePackageVersion().getPackageFees();
            Map packages = new HashMap(sub.getSuspendedPackages(ctx));

            it = packages.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry entry = (Map.Entry) it.next();
                Object key = entry.getKey();
                if (clearAll || !packageFees.containsKey(key))
                {
                    final ServicePackage servicePackage = (ServicePackage) entry.getValue();
                    sub.removeSuspendedPackage(ctx, servicePackage);
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, this, "Cannot erase package suspention record for sub = " + sub.getId(), e);
        }

        try
        {
            Set serviceFees = sub.getIntentToProvisionServiceIds();
            Map services = new HashMap(sub.getSuspendedServices(ctx));
            Map PPserviceFees = version.getServicePackageVersion().getServiceFees();
            it = services.keySet().iterator();
            while (it.hasNext())
            {
                Object key = it.next();
                if (clearAll || !serviceFees.contains(key) || (!PPserviceFees.containsKey(key) && serviceFees.contains(key)))
                {
                    ServiceFee2 serviceFee = new ServiceFee2();
                    serviceFee.setServiceId(((Long)key).longValue());
                    sub.deleteSuspendedService(ctx, serviceFee);
                    com.redknee.app.crm.refactoring.ServiceRefactoring_RefactoringClass.deleteAllSubscriberServicesWhenDeactivatingOrExpiring();
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, this, "Cannot erase service suspention record for sub = " + sub.getId(), e);
        }

        try
        {
            Map bundleFees = SubscriberBundleSupport.getSubscribedBundles(ctx, sub);
            Map bundles = new HashMap(sub.getSuspendedBundles(ctx));
            Map PPbundleFees = new HashMap(version.getServicePackageVersion().getBundleFees());
            // put auxiliary bundles into PPbundleFees
            Map auxBundles = sub.getBundles();
            it = auxBundles.keySet().iterator();
            while (it.hasNext())
            {
                Object o = it.next();
                PPbundleFees.put(o, auxBundles.get(o));
            }
            it = bundles.keySet().iterator();
            while (it.hasNext())
            {
                Object key = it.next();
                if (clearAll || !bundleFees.containsKey(key) || (!PPbundleFees.containsKey(key) && bundleFees.containsKey(key)))
                {
                    BundleFee bundleFee = new BundleFee();
                    bundleFee.setId(((Long)key).longValue());
                    sub.removeSuspendedBundles(ctx, bundleFee);
                }
            }

        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, this, "Cannot erase bundle suspension record for sub = " + sub.getId(), e);
        }

        try
        {
            final Collection activeAssociations = SubscriberAuxiliaryServiceSupport.getActiveSubscriberAuxiliaryServices(
                    ctx, sub, new Date());
            final Collection services = SubscriberAuxiliaryServiceSupport.getAuxiliaryServiceCollection(
                    ctx, activeAssociations, null);

            final Map<Long, Map<Long, SubscriberAuxiliaryService>> suspendedAuxServices = new HashMap<Long, Map<Long, SubscriberAuxiliaryService>>(
                    sub.getSuspendedAuxServices(ctx));

                final Iterator<Long> iter = suspendedAuxServices.keySet().iterator();
                while (iter.hasNext())
                {
                    final Long id = iter.next();
                    final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryServicById(ctx, id.longValue());
                    final Map<Long, SubscriberAuxiliaryService> associations = suspendedAuxServices.get(id);
                    final boolean isCUG = service.isCUG(ctx) || service.isPrivateCUG(ctx);
                    final AuxiliaryService auxSrv = (AuxiliaryService) CollectionSupportHelper.get(ctx).findFirst(ctx, services,
                        new AuxiliaryServiceByIdentifier(service));
                    if (clearAll || auxSrv == null && !isCUG)
                    {
                        for (final SubscriberAuxiliaryService association : associations.values())
                        {
                            sub.removeSuspendedAuxService(ctx, association);
                        }
                    }
                }


        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, this, "Cannot erase bundle suspention record for sub = " + sub.getId(), e);
        }
    }
}
