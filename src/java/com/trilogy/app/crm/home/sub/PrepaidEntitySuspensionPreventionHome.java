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
import java.util.HashMap;
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
import com.trilogy.app.crm.home.sub.suspensionPrevention.PrepaidAuxiliaryServiceSuspensionPreventionPredicate;
import com.trilogy.app.crm.home.sub.suspensionPrevention.PrepaidBundleSuspensionPreventionPredicate;
import com.trilogy.app.crm.home.sub.suspensionPrevention.PrepaidServiceSuspensionPreventionPredicate;
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
public class PrepaidEntitySuspensionPreventionHome extends HomeProxy
{
    
    public static final String UNPROV_NOTE_MSG = "UNPROV_NOTE_MSG";

    public PrepaidEntitySuspensionPreventionHome(Context ctx, Home home)
    {
        super(ctx, home);
    }

    
    @Override
    public Object store(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	Subscriber sub = (Subscriber)obj;
        if(!sub.isPrepaid())
            return super.store(ctx, obj);
        
        
        sub = (Subscriber)super.store(ctx, sub);
        boolean didUnprovision = false;
        
        /*
         * Bundles
         */
        final Map<Long, BundleFee> suspendedBundles = sub.getSuspendedBundles(ctx);
        final Set<Long> unprovisionedBundleIds = new HashSet<Long>();
        didUnprovision |= SuspensionPreventionSupport.<Long, BundleFee>unprovisionSuspendedEntities(ctx,
                suspendedBundles.keySet(), 
                    sub.getBundles(), 
                        new PrepaidBundleSuspensionPreventionPredicate(suspendedBundles), unprovisionedBundleIds);
        
        /*
         * Services
         */
        final Map<ServiceFee2ID, ServiceFee2> suspendedServices = sub.getSuspendedServices(ctx);
        final Set<ServiceFee2ID> unprovisionedServices = new HashSet<ServiceFee2ID>();
        
        //TODO putting unwanted fix for now, need to validate
        Map<ServiceFee2ID, ServiceFee2> svcKeyMap = new HashMap<ServiceFee2ID, ServiceFee2>();
        Set<ServiceFee2ID> serviceFee2IDs = sub.getServices(ctx);
        for (ServiceFee2ID serviceFee2ID : serviceFee2IDs) {
        	svcKeyMap.put(serviceFee2ID, null);
		}
        didUnprovision |= SuspensionPreventionSupport.<ServiceFee2ID, ServiceFee2>unprovisionSuspendedEntities(ctx,
                suspendedServices.keySet(), 
                svcKeyMap, 
                        new PrepaidServiceSuspensionPreventionPredicate(suspendedServices), unprovisionedServices);
        for(ServiceFee2ID service: unprovisionedServices)
        {
            sub.removeServiceFromIntentToProvisionServices(ctx, service.getServiceId());
        }
        
        
        /*
         * Aux Services
         */
        List<SubscriberAuxiliaryService> allSubAuxServices = sub.getAuxiliaryServices(ctx);
        Collection<SubscriberAuxiliaryService> auxServicesToSuspend = 
            SuspensionPreventionSupport.<SubscriberAuxiliaryService>getSuspendedEntitiesCollectionToUnprovision(ctx,
                sub.getSuspendedAuxServicesList(ctx), 
                        new PrepaidAuxiliaryServiceSuspensionPreventionPredicate());
        
        Set<Long> unprovisionedAuxServiceIds = new HashSet<Long>();
        for(SubscriberAuxiliaryService auxServToSuspend : auxServicesToSuspend)
            unprovisionedAuxServiceIds.add(Long.valueOf(auxServToSuspend.getIdentifier()));
        
        for(Iterator<SubscriberAuxiliaryService> i = allSubAuxServices.iterator(); i.hasNext(); )
            if(unprovisionedAuxServiceIds.contains(Long.valueOf(i.next().getIdentifier())))
                i.remove();
        didUnprovision |= !auxServicesToSuspend.isEmpty();
        
        if(didUnprovision)
        {
            ctx.put(UNPROV_NOTE_MSG, true);
            if(LogSupport.isDebugEnabled(ctx))
            {
                String msg = MessageFormat.format(
                    "Unprovisioning suspended Services: {0}, Bundles: {1}, Aux-Services: {2} for subscriber {3}", 
                        new Object[]{
                            unprovisionedServices, unprovisionedBundleIds, unprovisionedAuxServiceIds, sub
                    });
                LogSupport.debug(ctx, this, msg);
               
            }
            return super.store(ctx, sub);
        }
        
        return sub;
    }
}