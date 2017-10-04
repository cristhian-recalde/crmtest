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
package com.trilogy.app.crm.extension.subscriber;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bundle.BucketProvHome;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.license.LicenseAware;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * 
 *
 * @author asim.mahmood@redknee.com
 * @since 9.2
 */
public class FixedStopPricePlanSubExtension extends AbstractFixedStopPricePlanSubExtension implements LicenseAware 
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void install(Context ctx) throws ExtensionInstallationException
    {
        // Put subscriber in the context so that downstream pipelines can retrieve it
        Subscriber sub = getSubscriber(ctx);
        ctx.put(Subscriber.class, sub);
        
        boolean needsUpdate = false;
        
        try
        {
            //Change priceplan if given
            if (isPricePlanSwitch(ctx, sub) )
            {
                sub.switchPricePlan(ctx, this.getPrimaryPricePlanId());
                needsUpdate = true;
            }
            
            //Update balanceExpiry if given and different from current
            if(this.getBalanceExpiry() != null && !this.getBalanceExpiry().equals(sub.getExpiryDate()))
            {
                sub.setExpiryDate(this.getBalanceExpiry());
                SubscriberSupport.updateExpiryDateSubscriptionProfile(ctx, sub);
                needsUpdate = true;
            }

            if (needsUpdate)
            {
                sub = HomeSupportHelper.get(ctx).storeBean(ctx, sub);
            }
        }
        catch (HomeException e)
        {
            final String msg = "Unable to add extension for subscriber [" + sub.getId() + "]. Cause: " + e.getMessage();
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new ExtensionInstallationException(msg, e, false, true);
        }
        
    }

    /**
     * Switches the priceplan if different from current price plan if given, and changes state from Barred to Active
     */
    @Override
    public void uninstall(Context ctx) throws ExtensionInstallationException
    {
        // Put subscriber in the context so that downstream pipelines can retrieve it
        Subscriber sub = getSubscriber(ctx);
        ctx.put(Subscriber.class, sub);
        
        //Do nothing if deactivated
        if (sub == null || sub.isInFinalState())
        {
            return;
        }
        
        
        boolean needsUpdate = false;
        
        try
        {
    
            
            //Update balanceExpiry if given and different from current
            if(this.getBalanceExpiry() != null && !this.getBalanceExpiry().equals(sub.getExpiryDate()))
            {
                needsUpdate = true;
                sub.setExpiryDate(this.getBalanceExpiry());
                SubscriberSupport.updateExpiryDateSubscriptionProfile(ctx, sub);
            }
            
            if (sub.getState() == SubscriberStateEnum.LOCKED)
            {
                needsUpdate = true;
                sub.setState(SubscriberStateEnum.ACTIVE);
    
                //remove cps buckets, the sub pipeline will add them back in
                reprovisionBuckets(ctx, sub, CalendarSupportHelper.get(ctx).getRunningDate(ctx), true);             
            }

            //Change priceplan if given
            if (isPricePlanSwitch(ctx, sub) )
            {
                sub.switchPricePlan(ctx, this.getPrimaryPricePlanId());
                needsUpdate = true;
            }
            
            if (needsUpdate)
            {
                sub = HomeSupportHelper.get(ctx).storeBean(ctx, sub);
            }
        }
        catch (HomeException e)
        {
            final String msg = "Unable to removed extension for subscriber [" + sub.getId() + "]. Cause: " + e.getMessage();
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new ExtensionInstallationException(msg, e, false, true);
        }

    }

    /**
     *  
     *  1a. If fixedDate is in past and sub.state = active, set the sub.state = barred, cps.state = suspended
     *  1b. Otherwise if fixedDate is in future and sub.state = barred, set sub.state = active and cps.state = active (if sub.state = active)
     *  2. If date is in future and is changed, re-provision the CPS buckets
     *  3. if balanceExpiry set, set the subscriber's expiry to balanceExpiry and store (possibly set to CPS)
     * 
     */
    @Override
    public void update(Context ctx) throws ExtensionInstallationException
    {
        final CalendarSupport calendar = CalendarSupportHelper.get(ctx);
      
        // Put subscriber in the context so that downstream pipelines can retrieve it
        Subscriber sub = getSubscriber(ctx);
        ctx.put(Subscriber.class, sub);
        
        // Validate date
        if (this.getEndDate() == null)
        {
            throw new ExtensionInstallationException("End date must not be null", false, true);
        }
        // Clear time
        this.setEndDate(calendar.getDateWithNoTimeOfDay(this.getEndDate()));
      
        final FixedStopPricePlanSubExtension oldExtension = getOriginalExtension(ctx, sub);
        final Date today = calendar.getRunningDate(ctx);
        boolean needsUpdate = false;
        boolean needsBucketsReprovisioned = false;
        
        try
        {
            //Update balanceExpiry if given and different from current
            if(this.getBalanceExpiry() != null && !this.getBalanceExpiry().equals(sub.getExpiryDate()))
            {
                sub.setExpiryDate(this.getBalanceExpiry());
                SubscriberSupport.updateExpiryDateSubscriptionProfile(ctx, sub);
                needsUpdate = true;
            }
            
            //If endDate is today or in past
            if (this.getEndDate() != null && this.getEndDate().compareTo(today) <= 0)
            {
                if (sub.getState() == SubscriberStateEnum.ACTIVE)
                {
                    sub.setState(SubscriberStateEnum.LOCKED);
                    ctx.put(FixedStopPricePlanSubExtension.FIXED_STOP_PRICEPLAN_SUB_SUSPEND, true);
                    needsUpdate = true;
                }
            }
            else if (this.getEndDate() != null && this.getEndDate().compareTo(today) > 0)
            {
                if (sub.getState() == SubscriberStateEnum.LOCKED)
                {
                    sub.setState(SubscriberStateEnum.ACTIVE);
                    needsUpdate = true;
                }
             
                //Change priceplan if given
                if (isPricePlanSwitch(ctx, sub) )
                {
                    sub.switchPricePlan(ctx, this.getPrimaryPricePlanId());
                    needsUpdate = true;
                }

                //re-provision buckets on end date change
                if (this.getEndDate() != oldExtension.getEndDate())
                {
                    needsBucketsReprovisioned = true;
                }
            }
            
            if (needsUpdate)
            {
                sub = HomeSupportHelper.get(ctx).storeBean(ctx, sub);
            }

        }
        catch (HomeException e)
        {
            final String msg = "An error occured when updating subscriber [" + sub.getId() + "]. Cause: " + e.getMessage();
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new ExtensionInstallationException(msg, e, false, true);
        }
        
        try
        {
            if (needsBucketsReprovisioned)
            {
                reprovisionBuckets(ctx, sub, today, false);
            }
        }
        catch (HomeException e)
        {
            //non-fatal exception
            throw new ExtensionInstallationException(e.getMessage(), e.getCause(), true, false);
        }

    }

    /**
     * Re-provision subscriber price plan bundles that are already provisioned on Bundle Manager
     * 
     * @param ctx
     * @param sub
     * @param today
     */
    private void reprovisionBuckets(Context ctx, Subscriber sub, Date today, boolean isRemoveOnly) throws HomeException
    {
        if (isReprovisionOfBucketsDisabled(ctx))
        {
            return;
        }
        
        final BucketProvHome bundleProv = new BucketProvHome(ctx, null);
        final Map<Long, SubscriberBucket> crmBundles = BucketProvHome.getBuckets(ctx, sub);
        final Collection<Long> provisionedIDs = SubscriberBundleSupport.getProvisionedOnBundleManager(ctx, sub.getMSISDN(),
                (int) sub.getSubscriptionType());            
        
        //filter auxiliary bundles
        for (Iterator<Map.Entry<Long, SubscriberBucket>> i = crmBundles.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry<Long, SubscriberBucket> entry = i.next();
            if (entry.getValue().isAuxiliary())
            {
                i.remove();
            }
        }
        //join with provisioned bundles, i.e. only re-provision what has already been provisioned
        crmBundles.keySet().retainAll(provisionedIDs);
                
        try
        {
            //remove buckets
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Removing bundles for subscriber " + sub.getMSISDN()
                    + " (ID=" + sub.getId() + ") (BUNDLES=" + crmBundles.keySet() + ")", null).log(ctx);
            }
            bundleProv.provision(
                    ctx, sub.getSpid(), 
                    new HashMap<Long, SubscriberBucket>(crmBundles), Collections.EMPTY_MAP, 
                    sub, sub, 
                    today);
            new InfoLogMsg(this, "The following bundles were removed from Bundle Manager for "
                    + "MSISDN " + sub.getMSISDN() + ": " + crmBundles.keySet(), null).log(ctx);
            
            //add them back
            if (!isRemoveOnly)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Adding bundles for subscriber " + sub.getMSISDN()
                        + " (ID=" + sub.getId() + ") (BUNDLES=" + crmBundles.keySet() + ")", null).log(ctx);
                }
                bundleProv.provision(
                        ctx, sub.getSpid(), 
                        Collections.EMPTY_MAP, new HashMap<Long, SubscriberBucket>(crmBundles), 
                        sub, sub);
                new InfoLogMsg(this, "The following bundles were added to Bundle Manager for subscription "
                        + sub.getMSISDN() + " (ID=" + sub.getId() + "): "
                        + crmBundles.keySet(), null).log(ctx);
        }
        }
        catch (HomeException e)
        {
            final String msg = "Failed to remove and add one or more of the following bundles: " + crmBundles.keySet()
                    + " for subscription [" + sub.getId() + "]";
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new HomeException(msg, e);
        }
        
    }

    private FixedStopPricePlanSubExtension getOriginalExtension(Context ctx, Subscriber sub)
    {
        Predicate filter = new EQ(FixedStopPricePlanSubExtensionXInfo.SUB_ID, sub.getId());
        List<FixedStopPricePlanSubExtension> existingExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, FixedStopPricePlanSubExtension.class, filter);
        FixedStopPricePlanSubExtension oldExtension = null;
        if (existingExtensions != null && existingExtensions.size() > 0)
        {
            oldExtension = existingExtensions.get(0);
            ctx.put(Lookup.OLD_FIXED_STOP_PRICEPLAN_SUB_EXTENSION, oldExtension);
        }
        return oldExtension;
    }

    private boolean isPricePlanSwitch(Context ctx, Subscriber sub)
    {
        return !EnumStateSupportHelper.get(ctx).isOneOfStates(sub, SubscriberStateEnum.INACTIVE, SubscriberStateEnum.LOCKED) &&
                this.getPrimaryPricePlanId() != AbstractFixedStopPricePlanSubExtension.DEFAULT_PRIMARYPRICEPLANID &&
                this.getPrimaryPricePlanId() != sub.getPricePlan();
    }

    /**
     * Remove the extension on de-activation. This avoids getting picked up by deactivating cron task again.
     */
    public void deactivate(Context ctx) throws ExtensionInstallationException
    {
        try
        {
            HomeSupportHelper.get(ctx).removeBean(ctx, this);
        }

        catch (HomeException e)
        {
            final String msg = "Failed to remove the extension for subscription [" + this.getSubId() + "]";
            new MinorLogMsg(this, msg, e).log(ctx);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void move(Context ctx, Object newContainer) throws ExtensionInstallationException
    {
        //NOP
    }

    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        final CalendarSupport calendar = CalendarSupportHelper.get(ctx);

        // Put subscriber in the context so that downstream pipelines can retrieve it
        Subscriber sub = getSubscriber(ctx);
        ctx.put(Subscriber.class, sub);
        
        //Validate end date
        Date today = calendar.getRunningDate(ctx);
        today = calendar.getEndOfDay(today);
        if (this.getEndDate() == null)
        {
            throw new IllegalStateException("End date cannot be empty.");
        }
        
        if (ctx.get(HomeOperationEnum.class) == HomeOperationEnum.CREATE &&
                this.getEndDate().before(today))
        {
            throw new IllegalStateException("End date [" + this.getEndDate() + "] cannot be before [" + today + "]");
        }
        
        //Clear time
        this.setEndDate(calendar.getDateWithNoTimeOfDay(this.getEndDate()));      
    }

    @Override
    public boolean isValidForSubscriberType(SubscriberTypeEnum subscriberType)
    {
        return subscriberType ==  SubscriberTypeEnum.PREPAID;
    }

    @Override
    public boolean isLicensed(Context ctx)
    {
        return LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.SUBSCRIBER_FIXED_STOP_PRICEPLAN_LICENSE);
    }
    
    protected boolean isReprovisionOfBucketsDisabled(Context ctx)
    {
        return LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.SUBSCRIBER_FIXED_STOP_PRICEPLAN_BUCKET_REPROV_LICENSE);
    }
    
    
    public static final String FIXED_STOP_PRICEPLAN_SUB_SUSPEND = "FixedStopPricePlanSubExtension.Suspend";

}
