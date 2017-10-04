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
package com.trilogy.app.crm.home;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bulkloader.generic.BulkloadConstants;
import com.trilogy.app.crm.subscriber.charge.AbstractCrmCharger;
import com.trilogy.app.crm.subscriber.charge.AbstractSubscriberCharger;
import com.trilogy.app.crm.subscriber.charge.PostProvisionSubscriberCharger;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * This class charges auxiliary services during bulkload.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class SubscriberAuxiliaryServiceBulkloadChargingHome extends HomeProxy
{

    private static final long serialVersionUID = 1L;

    public SubscriberAuxiliaryServiceBulkloadChargingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }
    
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        if (ctx.get(BulkloadConstants.GENERIC_BEAN_BULKLOAD_CSV_COMMAND) != null)
        {

            Context subCtx = ctx.createSubContext();
            
            String keyString = ((SubscriberAuxiliaryService) obj)
                    .getSubscriberIdentifier();
            
            synchronized (getLock(keyString))
            {
                Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx, ((SubscriberAuxiliaryService) obj)
                        .getSubscriberIdentifier());
                addOldSubscriberToContext(subCtx, subscriber);
                
                final SubscriberAuxiliaryService service = (SubscriberAuxiliaryService) super.create(subCtx, obj);
                
                subscriber.getAuxiliaryServices(ctx).add(service);
                AbstractCrmCharger charger = new PostProvisionSubscriberCharger(subscriber);
                ctx.put(AbstractSubscriberCharger.class, charger);
                
                charger.chargeAndRefund(subCtx, null);
                
                return service;
            }
            

        }
        else
        {
            return super.create(ctx, obj);
        }
        
    }
    
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        if (ctx.get(BulkloadConstants.GENERIC_BEAN_BULKLOAD_CSV_COMMAND) != null)
        {
            final SubscriberAuxiliaryService service = (SubscriberAuxiliaryService) obj;
            Context subCtx = ctx.createSubContext();

            String keyString = ((SubscriberAuxiliaryService) obj)
                    .getSubscriberIdentifier();
            
            synchronized (getLock(keyString))
            {
                Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx, ((SubscriberAuxiliaryService) obj)
                        .getSubscriberIdentifier());
                addOldSubscriberToContext(subCtx, subscriber);
        
                super.remove(subCtx, obj);
                
                subscriber.getAuxiliaryServices(ctx).remove(service);
                AbstractCrmCharger charger = new PostProvisionSubscriberCharger(subscriber);
                ctx.put(AbstractSubscriberCharger.class, charger);
                
                charger.chargeAndRefund(subCtx, null);
            }
        }
        else
        {
            super.remove(ctx, obj);
        }
    }
    
    private synchronized Object getLock(String keyString)
    {
        Object lock; 

        WeakReference<String> lockID = lockKeyMap_.get(keyString);
        if (lockID == null)
        {
            lockID = new WeakReference<String>(keyString);
            lockKeyMap_.put(keyString, lockID);
        }

        // Hold a strong reference to the key so that it doesn't get GC'd
        String key = lockID.get();
        
        lock = lockMap_.get(key);
        if (lock == null)
        {
            lock = new Object();
            lockMap_.put(key, lock);
            if (!lockKeyMap_.containsKey(key))
            {
                lockKeyMap_.put(key, lockID);
            }
        }
        return lock;
    }
    

    private void addOldSubscriberToContext(Context subCtx, Subscriber subscriber) throws HomeException
    {
        try
        {
            Subscriber oldSubscriber = (Subscriber) subscriber.deepClone();
            oldSubscriber.setContext(subCtx);
            oldSubscriber.getSuspendedBundles(subCtx);
            oldSubscriber.getSuspendedPackages(subCtx);
            oldSubscriber.getSuspendedAuxServices(subCtx);
            oldSubscriber.getSuspendedServices(subCtx);
            oldSubscriber.getCLTCServices(subCtx);
            oldSubscriber.getAuxiliaryServices(subCtx);
            oldSubscriber.resetProvisionedAuxServiceIdsBackup();
            oldSubscriber.resetProvisionedAuxServiceBackup();
            oldSubscriber.getProvisionedAuxServiceIdsBackup(subCtx);
            oldSubscriber.getProvisionedAuxServiceBackup(subCtx);
            oldSubscriber.freeze();
            subCtx.put(Lookup.OLD_FROZEN_SUBSCRIBER, oldSubscriber);
        }
        catch (CloneNotSupportedException e)
        {
            // should not happen.
        }
    }

    protected Map<String, WeakReference<String>> lockKeyMap_ = new WeakHashMap<String, WeakReference<String>>();
    protected Map<String, Object> lockMap_ = new WeakHashMap<String, Object>();
}
