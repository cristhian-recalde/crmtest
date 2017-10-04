/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.processor.subscription.strategy;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bundle.BucketProvHome;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Responsible for moving bundles from the old subscription to the new one.
 * 
 * The fact that this strategy uses the BucketProvHome to accomplish this
 * means that charging is also performed here.  This is not ideal.
 * 
 * TODO: Remove the dependency on BucketProvHome and make this strategy
 * so that it only deals with moving the bundle records as required.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class BundleCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public BundleCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, SMR request) throws IllegalStateException
    {    
        super.initialize(ctx, request);
        
        Subscriber originalSubscription = request.getOriginalSubscription(ctx);
        if (originalSubscription != null)
        {
            try
            {
                oldBundles_ = BucketProvHome.getBuckets(ctx, originalSubscription);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error retrieving buckets for subscription " + originalSubscription.getId(), e).log(ctx);
            } 
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        Set<BundleProfile> availableBundleProfiles = validateBuckets(ctx, request, cise);
        validatePoolBundles(ctx, request, availableBundleProfiles, cise);
        cise.throwAll();
        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }
    
    /**
     * @{inheritDoc}
     */
    private Set<BundleProfile> validateBuckets(Context ctx, SMR request, CompoundIllegalStateException cise) throws IllegalStateException
    {
        final Set<BundleProfile> profiles = new HashSet<BundleProfile>();
        for (SubscriberBucket bucket : oldBundles_.values())
        {
            long bundleId = bucket.getBundleId();
            try
            {
                BundleProfile profile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundleId);
                if (null == profile)
                {
                    cise.thrown(new IllegalPropertyArgumentException(SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID,
                            "Bundle profile " + bundleId + " provisioned to subscription (ID="
                                    + request.getOldSubscriptionId() + ") is not avaialable."));
                } else
                {
                    profiles.add(profile);
                }
            }
            catch (Exception e)
            {
                cise.thrown(new IllegalPropertyArgumentException(SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID,
                        "Bundle profile " + bundleId + " provisioned to subscription (ID="
                                + request.getOldSubscriptionId() + ") cannot be retrieved."));
            }
        }
        return profiles;
    }
    
    
    /**
     * @{inheritDoc}
     */
    private void validatePoolBundles(Context ctx, SMR request, Set<BundleProfile> availBundleProfiles ,CompoundIllegalStateException cise) throws IllegalStateException
    {
        try
        {
            Subscriber oldSub = request.getOldSubscription(ctx);
            Subscriber newSub = request.getNewSubscription(ctx);
            if (oldSub.isPooledMemberSubscriber(ctx))
            {
                if (!newSub.isPooledMemberSubscriber(ctx))
                {
                    for (BundleProfile profile : availBundleProfiles)
                    {
                        if (GroupChargingTypeEnum.MEMBER_BUNDLE == profile.getGroupChargingScheme())
                        {
                            final StringBuilder messageBuilder;
                            {
                                messageBuilder = new StringBuilder("Subscription [" + oldSub.getId()
                                        + "] with MSISDN [" + oldSub.getMsisdn()
                                        + "] moving out of Pool should not contain member bundle [");
                                messageBuilder.append(" ( ID: ").append(profile.getBundleId()).append(", Name: ")
                                        .append(profile.getName()).append(" )");
                            }
                            cise.thrown(new IllegalPropertyArgumentException(
                                    SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, messageBuilder.toString()));
                        }
                    }
                }
            }
        }
        catch (Throwable t)
        {
            final String message = "Validation of pooled member bundle provisioned to subscription (ID="
                    + request.getOldSubscriptionId() + "). Reason (" + t.getMessage() + ")";
            cise.thrown(new IllegalPropertyArgumentException(SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, message));
            new DebugLogMsg(this, message, t);
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        Map<Long, SubscriberBucket> bundles = oldBundles_;
        if (bundles != null)
        {
            // Remove the one time bundles, because we should not
            // provision/unprovision them
            new DebugLogMsg(this, "Removing one-time bundles to prevent provisioning/unprovisioning them during move...", null).log(ctx);
            bundles = Collections.unmodifiableMap(BucketProvHome.removeOneTimeBundles(ctx, bundles));
            if (LogSupport.isDebugEnabled(ctx))
            {
                if (oldBundles_.size() > bundles.size())
                {
                    new DebugLogMsg(this, "Removed one-time bundles successfully.", null).log(ctx);
                }
                else
                {
                    new DebugLogMsg(this, "No one-time bundles to remove.", null).log(ctx);
                }
            }
            if (bundles.size() > 0)
            {
                removeOldBundles(ctx, request, bundles);
            }
        }

        super.createNewEntity(ctx, request);

        if (bundles != null
                && bundles.size() > 0)
        {
            createNewBundles(ctx, request, bundles);
        }
    }
    
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {   
        super.removeOldEntity(ctx, request);
    }
    
    protected void createNewBundles(Context ctx, SMR request, Map<Long, SubscriberBucket> newBundles) throws MoveException
    {
        Subscriber newSubscription = request.getNewSubscription(ctx);
        
        final BucketProvHome bundleProv = new BucketProvHome(ctx, null);

        try
        {
            new DebugLogMsg(this, "Adding bundles for moving subscriber " + newSubscription.getMSISDN()
                    + " (ID=" + newSubscription.getId() + ") (BUNDLES=" + newBundles.keySet() + ")", null).log(ctx);
            bundleProv.provision(
                    ctx, newSubscription.getSpid(), 
                    Collections.EMPTY_MAP, new HashMap<Long, SubscriberBucket>(newBundles), 
                    newSubscription, newSubscription);
            new InfoLogMsg(this, "The following bundles were added to Bundle Manager for subscription "
                    + newSubscription.getMSISDN() + " (ID=" + newSubscription.getId() + "): "
                    + newBundles.keySet(), null).log(ctx);
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Failed to add one or more of the following bundles: " + newBundles.keySet(),
                    e).log(ctx);
            throw new MoveException(request, "Error occurred adding bundles for subscription (ID="
                    + newSubscription.getId() + ").", e);
        }
    }
    
    protected void removeOldBundles(Context ctx, SMR request, Map<Long, SubscriberBucket> oldBundles) throws MoveException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        
        final BucketProvHome bundleProv = new BucketProvHome(ctx, null);

        try
        {
            // Must pretend we are removing old bundles at the time the move started.
            // Must have transaction time = move start time because by now the MSISDN is associated with the new subscription.
            Date moveStartTime = (Date) ctx.get(MoveConstants.MOVE_START_TIME_CTX_KEY, new Date());

            new DebugLogMsg(this, "Removing bundles for moving subscriber " + oldSubscription.getMSISDN()
                    + " (ID=" + oldSubscription.getId() + ") (BUNDLES=" + oldBundles.keySet() + ")", null).log(ctx);
            bundleProv.provision(
                    ctx, oldSubscription.getSpid(), 
                    new HashMap<Long, SubscriberBucket>(oldBundles), Collections.EMPTY_MAP, 
                    oldSubscription, oldSubscription, 
                    moveStartTime);
            new InfoLogMsg(this, "The following bundles were removed from Bundle Manager for "
                    + "MSISDN " + oldSubscription.getMSISDN() + ": " + oldBundles.keySet(), null).log(ctx);
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Failed to remove one or more of the following bundles: " + oldBundles.keySet(),
                    e).log(ctx);
            throw new MoveException(request, "Error occurred removing bundles for subscription (ID="
                    + oldSubscription.getId() + ")", e);
        }
    }

    protected Map<Long, SubscriberBucket> oldBundles_ = null;
}
