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
package com.trilogy.app.crm.bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.app.crm.priceplan.BundleFeeExecutionOrderComparator;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServicePackageSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;

/**
 * Handles special CRM logic that doesn't map directly to BM's subscriber home interface.
 * This home has to be placed in the subscriber pipeline before BucketProvHome.
 * 
 * This home only updates the subscriber's bundle state on BM. For bundle provisioning
 * see <code>com.redknee.app.crm.bundle.BucketProvHome</code> class. 
 *
 * @author candy.wong@redknee.com
 * @author victor.stratan@redknee.com
 * @author asim.mahmood@redknee.com
 */
public class ProvisionBundleSubscriberHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ProvisionBundleSubscriberHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public Object store(Context ctx, Object bean)
            throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
        CRMSubscriberBucketProfile bucketService = (CRMSubscriberBucketProfile)ctx.get(CRMSubscriberBucketProfile.class);

        final Subscriber newSub = (Subscriber) bean;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        List<BundleProfile> suspendBundles = new ArrayList<BundleProfile>();
        List<BundleProfile> activateBundles = new ArrayList<BundleProfile>();
        
        
        if (newSub == null)
        {
            throw new HomeException("Can't store NULL subscriber object.");
        }

        //Calculate which buckets to change state, pre-Store
        try
        {
        	Account account = (Account) ctx.get(Account.class);
            if (account == null || account.getBAN() == null)
            {
                account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class,newSub);
            }
        	 
        	 CRMSpid spid = SpidSupport.getCRMSpid(ctx, account.getSpid());
        	 
        	 /*
        	  * if isEnableBundleSuspension then at the time of Subscriber state change from Active to IN_ARREARS or IN_COLLECTION 
        	  * then only suspend bundles when going into In-Arrears or In-Collection State
        	  *  
        	  */
        		 if (oldSub != null && !spid.isSuspendBundlesInArrearState()
                         && EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, 
                                 Arrays.asList(
                                         SubscriberStateEnum.ACTIVE, 
                                         SubscriberStateEnum.PROMISE_TO_PAY
                                         ), 
                                 Arrays.asList(
                                         SubscriberStateEnum.EXPIRED, 
                                         SubscriberStateEnum.SUSPENDED, 
                                         SubscriberStateEnum.LOCKED
                                         )))
                 {
                     suspendBundles = getBundlesToSuspend(ctx, newSub, oldSub);
                 }         		
        	
        		 if (oldSub != null && spid.isSuspendBundlesInArrearState()
                         && EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, 
                                 Arrays.asList(
                                         SubscriberStateEnum.ACTIVE, 
                                         SubscriberStateEnum.PROMISE_TO_PAY,
                                         SubscriberStateEnum.IN_ARREARS,
                                         SubscriberStateEnum.NON_PAYMENT_WARN,
                                         SubscriberStateEnum.NON_PAYMENT_SUSPENDED
                                                                                  
                                        ), 
                                 Arrays.asList(
                                         SubscriberStateEnum.EXPIRED, 
                                         SubscriberStateEnum.SUSPENDED,
                                         SubscriberStateEnum.LOCKED,
                                         SubscriberStateEnum.IN_ARREARS,
                                         SubscriberStateEnum.IN_COLLECTION)))
                 {
                     suspendBundles = getBundlesToSuspend(ctx, newSub, oldSub);
                 } 
        		
        	        	 
            else if (oldSub != null  && !spid.isSuspendBundlesInArrearState()
                    && EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, 
                            Arrays.asList(
                                    SubscriberStateEnum.EXPIRED,
                                    SubscriberStateEnum.SUSPENDED,
                                    SubscriberStateEnum.LOCKED), 
                            Arrays.asList(
                                    SubscriberStateEnum.ACTIVE,
                                    SubscriberStateEnum.PROMISE_TO_PAY)))
            {
                activateBundles = getBundlesToActivate(ctx, newSub, oldSub);
            }
            else if (oldSub != null  && spid.isSuspendBundlesInArrearState()
                    && EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, 
                            Arrays.asList(
                                    SubscriberStateEnum.EXPIRED,
                                    SubscriberStateEnum.SUSPENDED,
                                    SubscriberStateEnum.LOCKED,
                                    SubscriberStateEnum.IN_ARREARS,
                                    SubscriberStateEnum.IN_COLLECTION), 
                            Arrays.asList(
                                    SubscriberStateEnum.ACTIVE,
                                    SubscriberStateEnum.PROMISE_TO_PAY)))
            {
                activateBundles = getBundlesToActivate(ctx, newSub, oldSub);
            }
        		 
        		 
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error while calculating local subscriber bucket states. Subscriber "
                    + newSub.getMSISDN(), e).log(ctx);

            final ExceptionListener exptListener = (ExceptionListener) ctx.get(ExceptionListener.class);
            if (exptListener != null)
            {
                exptListener.thrown(e);
            }
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Subscriber " + newSub.getMSISDN() + " state changed, bundles calcuated to be suspended: " + 
                    suspendBundles + ", activated: " + activateBundles, null).log(ctx);
        }

        bean = super.store(ctx, bean);
        
        //Update the bucket state post-Store, this avoids any need for roll-back on exception
        try
        {
            for (BundleProfile bundle : suspendBundles)
            {
                bucketService.updateBucketStatus(ctx, newSub.getMSISDN(), newSub.getSpid(),
                        (int) oldSub.getSubscriptionType(), bundle.getBundleId(),
                        false, !bundle.getSmartSuspensionEnabled());
            }
            for (BundleProfile bundle : activateBundles)
            {
                bucketService.updateBucketStatus(ctx, newSub.getMSISDN(), newSub.getSpid(),
                        (int) newSub.getSubscriptionType(), bundle.getBundleId(),
                        true, !bundle.getSmartSuspensionEnabled());
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Subscriber " + newSub.getMSISDN() + " state changed, successfully suspended: " + 
                        suspendBundles + ", successfully activated: " + activateBundles, null).log(ctx);
            }

        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error while modifying bucket state on BM. Subscriber "
                    + newSub.getMSISDN(), e).log(ctx);

            final ExceptionListener exptListener = (ExceptionListener) ctx.get(ExceptionListener.class);
            if (exptListener != null)
            {
                exptListener.thrown(e);
            }
        }
        
        return bean;
    }

    private List<BundleProfile> getBundlesToActivate(Context ctx, final Subscriber newSub, final Subscriber oldSub) 
            throws HomeException, InvalidBundleApiException
    {
        List <BundleProfile> bundles = new ArrayList<BundleProfile>();
        
        /*
        * Can't just blanket update all buckets using bs.updateAllBucketsActive(), have to iterate through and
        * prorate minutes only for those with smart suspension off
        * Need to use oldSub because bundles will be switched in the next pipeline decorator.
        */
        final Map<Long, BundleFee> bundlesMap = SubscriberBundleSupport.getSubscribedBundlesWithPointsBundles(ctx, oldSub);
        List<BundleFee> sortedBundleFee = new ArrayList(bundlesMap.values());
        Collections.sort(sortedBundleFee, new BundleFeeExecutionOrderComparator(ctx, true));
        for (final Iterator<BundleFee> i = sortedBundleFee.iterator(); i.hasNext();)
        {
            final BundleFee bundle = (BundleFee) i.next();

            // treat separatly bundles that come from packages
            if (bundle.getSource().startsWith("Package"))
            {
                continue;
            }

            // don't unsuspend bundles in BM that are still suspended in CRM
            final boolean suspended = SuspendedEntitySupport.isSuspendedEntity(ctx, newSub.getId(),
                    bundle.getId(), SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, BundleFee.class);
            if (suspended)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Bundle state for Subscriber " + newSub.getMSISDN() + 
                            " not activated since bundle is suspended in BSS.", null).log(ctx);
                }
                continue;
            }

            BundleProfile bundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundle.getId());
            // If smart suspension is enabled, no prorate charge is done, and thus,
            //  minutes should not be prorated and vice versa
            bundles.add(bundleProfile);

        }

        bundles.addAll(addServicePackageBundles(ctx, oldSub));
        
        return bundles;
    }

    @SuppressWarnings("rawtypes")
    private List <BundleProfile> addServicePackageBundles(Context ctx, final Subscriber oldSub) throws HomeException, InvalidBundleApiException
    {
        List <BundleProfile> bundles = new ArrayList<BundleProfile>();
        // Need to use oldSub because bundles will be switched in the next pipeline decorator.
        final PricePlanVersion version = oldSub.getRawPricePlanVersion(ctx);
        if (version != null)
        {
            final Collection packageFees = version.getServicePackageVersion().getPackageFees().values();
            for (final Iterator pi = packageFees.iterator(); pi.hasNext();)
            {
                final ServicePackageFee fee = (ServicePackageFee) pi.next();
    
                // don't unsuspend bundles in BM that are still suspended in CRM
                final boolean suspended = SuspendedEntitySupport.isSuspendedEntity(ctx, oldSub.getId(),
                        fee.getPackageId(), SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, ServicePackage.class);
                if (suspended)
                {
                    continue;
                }
    
                final ServicePackageVersion ver = ServicePackageSupportHelper.get(ctx).getCurrentVersion(ctx, fee.getPackageId());
                List<BundleFee> sortedBundleFee = new ArrayList(ver.getBundleFees().values());
                Collections.sort(sortedBundleFee, new BundleFeeExecutionOrderComparator(ctx, true));
                for (final Iterator i = sortedBundleFee.iterator(); i.hasNext();)
                {
                    final BundleFee bundle = (BundleFee) i.next();
    
                    BundleProfile bundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundle.getId());
                    
                    bundles.add(bundleProfile);
                    // If smart suspension is enabled, no prorate charge is done, and thus, minutes should not be prorated and vice versa
    
                }
            }
        }
        return bundles;
    }

    private List<BundleProfile> getBundlesToSuspend(Context ctx, final Subscriber newSub, final Subscriber oldSub)
            throws HomeException, InvalidBundleApiException
    {
        List <BundleProfile> bundles = new ArrayList<BundleProfile>();
        /*
        * Can't just blanket update all buckets using bs.updateAllBucketsActive(), have to iterate through and
        * prorate minutes only for those with smart suspension off
        * Need to use oldSub because bundles will be switched in the next pipeline decorator.
        */
        final Map<Long, BundleFee> bundlesMap = SubscriberBundleSupport.getSubscribedBundlesWithPointsBundles(ctx, oldSub);
        List<BundleFee> sortedBundleFee= new ArrayList(bundlesMap.values());
        Collections.sort(sortedBundleFee, new BundleFeeExecutionOrderComparator(ctx, true));
        for (final Iterator<BundleFee> i = sortedBundleFee.iterator(); i.hasNext();)
        {
            BundleFee bundle = (BundleFee) i.next();

            BundleProfile bundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundle.getId());

            bundles.add(bundleProfile);
            // If smart suspension is enabled, no prorate refund is done, and thus,
            // minutes should not be prorated and vice versa

        }
        
        return bundles;
    }
}
