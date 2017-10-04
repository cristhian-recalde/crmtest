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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.util.CollectionsUtils;
import com.trilogy.app.crm.util.Index2D;
import com.trilogy.app.crm.util.MapUtils;
import com.trilogy.app.crm.util.TypedPredicate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author sbanerjee
 *
 */
public class PostpaidRetryRecurRechargeVisitor 
    extends RetryRecurRechargeVisitor
        implements Visitor
{
    private static final HashSet<ServicePackage> EMPTY_SERVICE_PKG_SET = new HashSet<ServicePackage>();

    /**
     * @param ctx
     * @param subscriber
     * @return
     */
    @Override
    protected Map<Long, Map<Long, SubscriberAuxiliaryService>> getSubscriberSuspendedAuxiliaryServices(
            final Context ctx, final Subscriber subscriber)
    {
        final Map<Index2D<Long>, SubscriberAuxiliaryService> sourceAuxServices2D = MapUtils.as2DMap(subscriber.getSuspendedAuxServices(ctx));
        final Map<Index2D<Long>, SubscriberAuxiliaryService> destAuxServices2D = new HashMap<Index2D<Long>, SubscriberAuxiliaryService>();
        
        MapUtils.filter(ctx, destAuxServices2D, sourceAuxServices2D, 
                new TypedPredicate<Map.Entry<Index2D<Long>, SubscriberAuxiliaryService>>()
                    {
                        @Override
                        public boolean f(
                                Context ctx,
                                Entry<Index2D<Long>, SubscriberAuxiliaryService> auxSrv)
                                throws AbortVisitException
                        {
                            try
                            {
                                return auxSrv.getValue().getAuxiliaryService(ctx).isRestrictProvisioning();
                            } 
                            catch (Exception e)
                            {
                                return false;
                            }
                        }
                    });
        
        final Map<Long, Map<Long, SubscriberAuxiliaryService>> destAuxServices = MapUtils.<Long, SubscriberAuxiliaryService>asNestedMap(destAuxServices2D);
        
        if(LogSupport.isDebugEnabled(ctx))
        {
            String msg = MessageFormat.format(
                "SUSPENDED RP AUX-Services: Subscriber: {0} | suspended aux-services: {1}", 
                    new Object[]{subscriber.getId(), destAuxServices.keySet()});
            LogSupport.debug(ctx, this, msg);
        }
        
        return destAuxServices;
    }


    /**
     * @param ctx
     * @param subscriber
     * @return
     */
    protected Collection<BundleFee> getSubscriberSuspendedBundles(final Context ctx,
            final Subscriber subscriber)
    {
        final Collection<BundleFee> sourceBundles = subscriber.getSuspendedBundles(ctx).values();
        Collection<BundleFee> destBundles = new HashSet<BundleFee>(); 
        
        CollectionsUtils.<BundleFee>filter(ctx, destBundles, sourceBundles, new TypedPredicate<BundleFee>()
            {
                @Override
                public boolean f(Context ctx, BundleFee bFee)
                        throws AbortVisitException
                {
                    try
                    {
                        return bFee.getBundleProfile(ctx, subscriber.getSpid()).isRestrictProvisioning();
                    } 
                    catch (Exception e)
                    {
                        return false;
                    }
                }
            });
        
        if(LogSupport.isDebugEnabled(ctx))
        {
            String msg = MessageFormat.format(
                "SUSPENDED RP Bundles: Subscriber: {0} | suspended bundles: {1}", 
                    new Object[]{subscriber.getId(), destBundles});
            LogSupport.debug(ctx, this, msg);
        }
        
        return destBundles;
    }


    /**
     * @param ctx
     * @param subscriber
     * @return
     */
    protected Map<ServiceFee2ID, ServiceFee2> getSubscriberSuspendedServices(final Context ctx,
            final Subscriber subscriber)
    {
        final Map<ServiceFee2ID, ServiceFee2> sourceServices = subscriber.getSuspendedServices(ctx);
        final Map<ServiceFee2ID, ServiceFee2> destServices = new HashMap<ServiceFee2ID, ServiceFee2>();
        
        MapUtils.filter(ctx, destServices, sourceServices, new TypedPredicate<Map.Entry<ServiceFee2ID, ServiceFee2>>()
            {
                @Override
                public boolean f(Context ctx, Entry<ServiceFee2ID, ServiceFee2> sFee)
                        throws AbortVisitException
                {
                    try
                    {
                        return sFee.getValue().getService(ctx).isRestrictProvisioning();
                    } 
                    catch (Exception e)
                    {
                        return false;
                    }
                }
            });
        
        if(LogSupport.isDebugEnabled(ctx))
        {
            String msg = MessageFormat.format(
                "SUSPENDED RP Services: Subscriber: {0} | suspended services: {1}", 
                    new Object[]{subscriber.getId(), destServices.keySet()});
            LogSupport.debug(ctx, this, msg);
        }
        
        return destServices;
    }


    /**
     * @param ctx
     * @param subscriber
     * @return
     */
    protected Collection<ServicePackage> getSubscriberSuspendedPackages(final Context ctx,
            final Subscriber subscriber)
    {
        /*
         * RP flag is not supported for Service Packages.
         */
        
        if(LogSupport.isDebugEnabled(ctx))
        {
            String msg = MessageFormat.format(
                "SUSPENDED RP Packages: Subscriber: {0} | suspended packages: NONE | Packages with RP=true not supported", 
                    new Object[]{subscriber.getId()});
            LogSupport.debug(ctx, this, msg);
        }
        
        return EMPTY_SERVICE_PKG_SET;
    }
}