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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.home.sub.suspensionPrevention.PostpaidAuxiliaryServiceSuspensionPreventionPredicate;
import com.trilogy.app.crm.home.sub.suspensionPrevention.PostpaidBundleSuspensionPreventionPredicate;
import com.trilogy.app.crm.home.sub.suspensionPrevention.PostpaidServiceSuspensionPreventionPredicate;
import com.trilogy.app.crm.support.SuspensionPreventionSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author sbanerjee
 *
 */
public class PostpaidEntitySuspensionPreventionHome extends HomeProxy
{

    public PostpaidEntitySuspensionPreventionHome(Context ctx, Home home)
    {
        super(ctx, home);
    }
    
    
    @Override
    public Object create(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	Subscriber sub = (Subscriber)obj;
        if(!sub.isPostpaid())
            return super.create(ctx, obj);
        
        sub = (Subscriber)super.create(ctx, sub);

        /*
         * Once the subscription is created , we need to unprovision the services based on RP flag and charging mode of the service attached(one time/multiday etc).
         * Call store to update the subscription.
         */
        boolean didUnprovision = isSuspendedEntitiesUnprovisioned(ctx, sub);
        if(didUnprovision)
        {
            return super.store(ctx,sub);
        }
        return sub;
    }

    
    @Override
    public Object store(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	Subscriber sub = (Subscriber)obj;
        if(!sub.isPostpaid())
            return super.store(ctx, obj);
        
        sub = (Subscriber)super.store(ctx, sub);

        boolean didUnprovision = isSuspendedEntitiesUnprovisioned(ctx, sub);
        if(didUnprovision)
        {
            return super.store(ctx, sub);
        }
        return sub;
    }

    
    private boolean isSuspendedEntitiesUnprovisioned(Context ctx, Subscriber sub)
    {
        boolean didUnprovision = false;
        
        /*
         * Bundles
         */
        final Map<Long, BundleFee> suspendedBundles = sub.getSuspendedBundles(ctx);
        final Set<Long> unprovisionedBundleIds = new HashSet<Long>();
        didUnprovision |= SuspensionPreventionSupport.<Long, BundleFee>unprovisionSuspendedEntities(ctx,
                suspendedBundles.keySet(), 
                    sub.getBundles(), 
                        new PostpaidBundleSuspensionPreventionPredicate(suspendedBundles),
                        unprovisionedBundleIds);
        
        /*
         * Services
         */
        final Map<ServiceFee2ID, ServiceFee2> suspendedServices = sub.getSuspendedServices(ctx);
        final Set<ServiceFee2ID> unprovisionedServiceIds = new HashSet<ServiceFee2ID>();
        didUnprovision |= SuspensionPreventionSupport.<ServiceFee2ID, ServiceFee2>unprovisionSuspendedEntities(ctx,
                suspendedServices.keySet(), 
                    sub.getServices(ctx), 
                        new PostpaidServiceSuspensionPreventionPredicate(suspendedServices),
                            unprovisionedServiceIds);
        for(ServiceFee2ID serviceFee2ID: unprovisionedServiceIds)
        {
            sub.removeServiceFromIntentToProvisionServices(ctx, serviceFee2ID.getServiceId());
        }
        
        /*
         * Aux Services
         */
        List<SubscriberAuxiliaryService> allSubAuxServices = sub.getAuxiliaryServices(ctx);
        Collection<SubscriberAuxiliaryService> auxServicesToUnsuspend = 
            SuspensionPreventionSupport.<SubscriberAuxiliaryService>getSuspendedEntitiesCollectionToUnprovision(ctx,
                sub.getSuspendedAuxServicesList(ctx), 
                        new PostpaidAuxiliaryServiceSuspensionPreventionPredicate());
        
        final Set<Long> unprovisionedAuxServiceIds = new HashSet<Long>();
        for(SubscriberAuxiliaryService auxServToPreventSuspension : auxServicesToUnsuspend)
            unprovisionedAuxServiceIds.add(Long.valueOf(auxServToPreventSuspension.getIdentifier()));
        
        for(Iterator<SubscriberAuxiliaryService> i = allSubAuxServices.iterator(); i.hasNext(); )
            if(unprovisionedAuxServiceIds.contains(Long.valueOf(i.next().getIdentifier())))
                i.remove();
        didUnprovision |= !auxServicesToUnsuspend.isEmpty();
        
        if(LogSupport.isDebugEnabled(ctx))
        {
            String msg = MessageFormat.format(
                "Unprovisioning suspended Services: {0}, Bundles: {1}, Aux-Services: {2} for subscriber {3}", 
                    new Object[]{
                        unprovisionedServiceIds, unprovisionedBundleIds, unprovisionedAuxServiceIds, sub
                });
            LogSupport.debug(ctx, this, msg);
        }
        return didUnprovision;
    }
}
