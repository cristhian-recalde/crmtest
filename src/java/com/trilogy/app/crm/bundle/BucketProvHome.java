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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bundle.exception.BucketAlreadyExistsException;
import com.trilogy.app.crm.bundle.exception.BucketDoesNotExistsException;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.exception.SubscriberProfileDoesNotExistException;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.app.crm.home.sub.SubscriptionDeactivateValidator;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.MapVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Bucket provisioning home.
 * Provisions SubscriberBucketAPIHome with data taken from Subscriber.BundleFee.
 *
 * @author kevin.greer@redknee.com
 * @author victor.stratan@redknee.com
 */
public class BucketProvHome extends HomeProxy
{
    public BucketProvHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj)
        throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	Subscriber sub = (Subscriber) obj;
        // provisioning all selected bundles
        final Map<Long, SubscriberBucket> buckets = getBuckets(ctx, sub);
        addLoyaltyBuckets(ctx, sub, buckets);
        
        // Only provision bundles if state not equals Pending. Otherwise, profile won't exist.
        if (!sub.getState().equals(SubscriberStateEnum.PENDING))
        {
            // provision after successful creation
            provision(
                    ctx,
                    sub.getSpid(),
                    Collections.EMPTY_MAP,
                    buckets, null, sub);
        }


        return getDelegate(ctx).create(ctx, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj)
        throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	Subscriber sub = (Subscriber) obj;
        
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        if (sub == null)
        {
            throw new HomeException("Can't store NULL subscriber object.");
        }

        if (oldSub == null)
        {
            oldSub = (Subscriber) find(ctx, obj);
        }

        // This looks a little odd given that the same check is just above, but it is correct
        if (oldSub == null)
        {
            throw new HomeException("Can't store(" + getIdentitySupport(ctx).ID(obj)
                    + ") because entry doesn't exist.");
        }

        if (oldSub.getState() != SubscriberStateEnum.INACTIVE && sub.getState() == SubscriberStateEnum.INACTIVE)
        {
            sub = (Subscriber) getDelegate(ctx).store(ctx, obj);
            unprovisionAllBundles(ctx, oldSub, sub);
            return sub;
        }
        else if (oldSub.getState() == SubscriberStateEnum.INACTIVE && sub.getState() == SubscriberStateEnum.INACTIVE)
        {
            // if an Inactive subscriber is updated, do nothing
            return getDelegate(ctx).store(ctx, obj);
        }
        else if (oldSub.getState() == SubscriberStateEnum.INACTIVE && sub.getState() != SubscriberStateEnum.INACTIVE)
        {
            // reprovision all bundles during reactivation, after successfull update
            final Object result = getDelegate(ctx).store(ctx, obj);

            final Map<Long, SubscriberBucket> buckets = getBuckets(ctx, sub);
            addLoyaltyBuckets(ctx, sub, buckets);

            provision(
                    ctx,
                    sub.getSpid(),
                    Collections.EMPTY_MAP,
                    buckets, null, sub);

            return result;
        }

        boolean provisionBundles = false;
        final List<SubscriberBucket> createAux = new ArrayList<SubscriberBucket>();
        final List<SubscriberBucket> removeAux = new ArrayList<SubscriberBucket>();
        final List<Long> oldIds = new ArrayList<Long>();
        final List<Long> newIds = new ArrayList<Long>();
        Map<Long, SubscriberBucket> newBundles = null;
        Set<Long> oldMRCBundles = new HashSet<Long>();
        Set<Long> newMRCBundles = new HashSet<Long>();
        Set<Long> oldMRCServices = new HashSet<Long>();
        Set<Long> newMRCServices = new HashSet<Long>();
        
        boolean isPickNPay = isPicknPay(ctx, sub) && isPicknPay(ctx, oldSub);
        
        if(isPickNPay)
        {
        	//get MRC bundle Ids
            oldMRCBundles = getMRCBundleIds(ctx, oldSub);
            newMRCBundles = getMRCBundleIds(ctx, sub);
            
            oldMRCServices = getMRCServiceIds(ctx, oldSub);
            newMRCServices = getMRCServiceIds(ctx, sub);
        }
        
        boolean changeInMRCGroup = !(oldMRCBundles.equals(newMRCBundles) && oldMRCServices.equals(newMRCServices));

        // check if anything has changed or not
        // so we can avoid doing any work if
        // not required
        if (oldSub.getPricePlan() != sub.getPricePlan()
                || EnumStateSupportHelper.get(ctx).isLeavingState(oldSub, sub, SubscriberStateEnum.PENDING)
                || oldSub.getPricePlanVersion() != sub.getPricePlanVersion()
                || !oldSub.getBundles().keySet().equals(sub.getBundles().keySet())
                || ctx.get(Common.FORCE_BM_PROVISION_CALL) != null
                || !oldSub.getPointsBundles().keySet().equals(sub.getPointsBundles().keySet())
                || (isPickNPay && changeInMRCGroup))
        		// Allow for Pick n pay subscribers bcz on MRC service change bundles has to be reset
        {
            provisionBundles = true;
            final Map<Long, SubscriberBucket> oldBundles = getBuckets(ctx, oldSub);
            addLoyaltyBuckets(ctx, oldSub, oldBundles);

            newBundles = getBuckets(ctx, sub);
            addLoyaltyBuckets(ctx, sub, newBundles);

            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "BucketProvHome store: oldBundles:" + oldBundles, null);
                LogSupport.debug(ctx, this, "BucketProvHome store: newBundles:" + newBundles, null);
                if(isPickNPay && changeInMRCGroup)
                {
                	LogSupport.debug(ctx, this, "BucketProvHome store: oldMRCBundles:" + oldMRCBundles, null);
                	LogSupport.debug(ctx, this, "BucketProvHome store: newMRCBundles:" + newMRCBundles, null);
                	LogSupport.debug(ctx, this, "BucketProvHome store: oldMRCServices:" + oldMRCServices, null);
                	LogSupport.debug(ctx, this, "BucketProvHome store: newMRCServices:" + newMRCServices, null);
                }
            }
            
            

            diffBundles(ctx, oldSub, sub, oldBundles, newBundles, createAux, removeAux, oldIds, newIds);
            markProvisionedBundles(ctx, createAux, removeAux, oldIds, newIds, oldSub, sub);
            
        	if(isPickNPay && changeInMRCGroup)
        	{
        		handleMRCGroup(ctx, sub, oldSub, newBundles, newIds, oldIds,newMRCBundles);
        	}
        }

        sub = (Subscriber) getDelegate(ctx).store(ctx, sub);

        if (provisionBundles)
        {
            provision(ctx, sub, oldSub, sub.getSpid(), newBundles, createAux, removeAux, oldIds, newIds, new Date());
        }

        return sub;
    }


    private void addLoyaltyBuckets(final Context ctx, final Subscriber sub, final Map<Long, SubscriberBucket> buckets)
        throws HomeException
    {
        final Function feeToBucket = new BundleFeeToSubscriberBucketApiFunction(ctx, sub);
        Map<Long, BundleFee> pointsBundles = sub.getPointsBundles();
        for (Map.Entry<Long, BundleFee> entry : pointsBundles.entrySet())
        {
            final Long bundleID = entry.getKey();
            final BundleFee fee = entry.getValue();
            
            final SubscriberBucket bucket = (SubscriberBucket) feeToBucket.f(ctx, fee);

            // Points Bundles bucket are behaving as auxiliary buckets
            bucket.setAuxiliary(true);

            buckets.put(bundleID, bucket);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Context ctx, final Object obj)
        throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[remove].....");
    	// removal of buckets is handled in ProvisionBundleSubscriberHome.remove()
        getDelegate(ctx).remove(ctx, obj);
    }

    /**
     * @param ctx the operating context
     * @param sub this subscriber's buckets will be returned
     * @return a Map of the suplied Subscriber's SubscriberBucketApi's
     * @throws HomeException if bucket collection fails in Framework classes
     */
    public static Map<Long, SubscriberBucket> getBuckets(final Context ctx, final Subscriber sub)
        throws HomeException
    {
        final Map<Long, SubscriberBucket> result = new HashMap<Long, SubscriberBucket>();
        final Function transform = new BundleFeeToSubscriberBucketApiFunction(ctx, sub);

        Map<Long, BundleFee> subscribedBundles = SubscriberBundleSupport.getSubscribedBundles(ctx, sub);
        for (Map.Entry<Long, BundleFee> entry : subscribedBundles.entrySet())
        {
            result.put(entry.getKey(), (SubscriberBucket) transform.f(ctx, entry.getValue()));
        }

        return result;
    }
    
    /**
     * This method sends the bundle to BM for bucket provisioning.
     *
     * @param ctx        operating context
     * @param spid       the spid this msisdn belongs to
     * @param oldBundles the bundles of the old subscriber
     * @param newBundles the bundles of the new subscriber
     * @param oldSub     old subscriber
     * @param newSub     new subscriber
     * @throws HomeException the exception that occured in BM wrapped ina HomeException. This should stop processing.
     */
    public void provision(final Context ctx, final int spid,
            final Map<Long, SubscriberBucket> oldBundles, final Map<Long, SubscriberBucket> newBundles, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        provision(ctx, spid, oldBundles, newBundles, oldSub, newSub, new Date());
    }
    
    public void provision(final Context ctx, final int spid,
            final Map<Long, SubscriberBucket> oldBundles, final Map<Long, SubscriberBucket> newBundles, final Subscriber oldSub, final Subscriber newSub, final Date chargeDate)
        throws HomeException
    {
        final List<SubscriberBucket> createAux = new ArrayList<SubscriberBucket>();
        final List<SubscriberBucket> removeAux = new ArrayList<SubscriberBucket>();
        final List<Long> oldIds = new ArrayList<Long>();
        final List<Long> newIds = new ArrayList<Long>();

        diffBundles(ctx, oldSub, newSub, oldBundles, newBundles, createAux, removeAux, oldIds, newIds);
        markProvisionedBundles(ctx, createAux, removeAux, oldIds, newIds, oldSub, newSub);
        provision(ctx, newSub, oldSub, spid, newBundles, createAux, removeAux, oldIds, newIds, chargeDate);
    }

    /**
     * This method calculates the delta for BM bucket provisioning.
     *
     * @param ctx        the context
     * @param oldBundles the bundles of the old subscriber
     * @param newBundles the bundles of the new subscriber
     */
    public void diffBundles(final Context ctx, final Subscriber oldSub, final Subscriber newSub,
            final Map<Long, SubscriberBucket> oldBundles, final Map<Long, SubscriberBucket> newBundles,
            final Collection<SubscriberBucket> createAux, final Collection<SubscriberBucket> removeAux, 
            final Collection<Long> oldIds, final Collection<Long> newIds)
    {
        final Collection<Long> provisionedIDs;
        if (oldSub == null)
        {
            provisionedIDs = Collections.emptyList();
        }
        else
        {
            // need to use newSub MSISDN in case of MSISDN switch
            provisionedIDs = SubscriberBundleSupport.getProvisionedOnBundleManager(ctx, newSub.getMSISDN(),
                    (int) newSub.getSubscriptionType());
        }

        // remove Aux bundles from oldBundles
        for (final Iterator<Map.Entry<Long, SubscriberBucket>> i = oldBundles.entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, SubscriberBucket> oldEntry = i.next();
            final Object oldID = oldEntry.getKey();
            final SubscriberBucket oldBucket = oldEntry.getValue();

            if (oldBucket.isAuxiliary())
            {
                final SubscriberBucket newBucket = newBundles.get(oldID);

                if (newBucket == null)
                {
                    // need to remove from oldBundles because it is auxiliary
                    // and it's not selected any more
                    i.remove();
                    if (provisionedIDs.contains(oldID))
                    {
                        // attempt to remove from BM only if the bundle is provisioned
                        removeAux.add(oldBucket);
                    }
                }
                else if (newBucket.isAuxiliary())
                {
                    // need to remove from oldBundles because it is auxiliary
                    // and it's still selected as auxiliary
                    i.remove();
                    if (provisionedIDs.contains(oldID))
                    {
                        // if the bundle is provisioned we don't need to do anything, so remove from new list
                        // to refrase: remove from new list only if already provisioned
                        newBundles.remove(oldID);
                    }
                    else
                    {
                        // the bundle is both in new and old list, but it is not provisioned on BM, so create it
                        createAux.add(newBucket);
                        newBundles.remove(oldID);
                    }
                }
            }
        }

        // remove Aux bundles from newBundles
        for (final Iterator<Map.Entry<Long, SubscriberBucket>> i = newBundles.entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, SubscriberBucket> newEntry = i.next();
            final Object newID = newEntry.getKey();
            final SubscriberBucket newBucket = newEntry.getValue();

            if (newBucket.isAuxiliary())
            {
                // no need to check that it doesn't appear in the oldBundles
                // because if it did then both bundles would have already
                // been removed in the first pass above
                if (!provisionedIDs.contains(newID))
                {
                    // add only if the bundle is NOT already provisioned
                    createAux.add(newBucket);
                }

                i.remove();
            }
        }

        // clear both bundle maps of same bundles and delete NOT provisioned bundles from oldBundles
        for (final Iterator<Map.Entry<Long, SubscriberBucket>> i = oldBundles.entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, SubscriberBucket> oldEntry = i.next();
            final Long oldID = oldEntry.getKey();

            if (!provisionedIDs.contains(oldID))
            {
                // if the bundle is NOT provisioned we need to remove it ONLY from old list
                i.remove();
            }
            else
            {
                // if it's there then remove it, if not - no harm done
                final SubscriberBucket result = newBundles.remove(oldID);
                // if it was removed the result is not null, so we need to remove it from old list as well
                if (result != null)
                {
                    i.remove();
                }
            }
        }

        // delete provisioned bundles from newBundles
        for (final Iterator<Map.Entry<Long, SubscriberBucket>> i = newBundles.entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, SubscriberBucket> newEntry = i.next();
            final Long newID = newEntry.getKey();
            if (provisionedIDs.contains(newID))
            {
                // if the bundle is provisioned we need to remove it from new list
                // to refrase: remove from new list only if already provisioned
                i.remove();
            }
        }

        oldIds.addAll(oldBundles.keySet());
        newIds.addAll(newBundles.keySet());
    }
    
    public void handleMRCGroup(final Context ctx,final Subscriber newSub ,final Subscriber oldSub,final Map<Long, SubscriberBucket> newBundles, 
    		final Collection<Long> newIds, final Collection<Long> oldIds, Set<Long> newMRCBundles) throws HomeException
    {
        final Collection<Long> provisionedIDs;
        //MRC bundle id which are already provisoned to BM
        final Set<Long> provisionedMRCBundleIds = new HashSet<Long>();
        
        Map<Long, SubscriberBucket> bundleList = null;        
        
        if (oldSub == null)
        {
            provisionedIDs = Collections.emptyList();
        }
        else
        {
            provisionedIDs = SubscriberBundleSupport.getProvisionedOnBundleManager(ctx, newSub.getMSISDN(),
                    (int) newSub.getSubscriptionType());
        }       
        
        
        for(Long bundleId :newMRCBundles)
        {
        	if(provisionedIDs.contains(bundleId))
        	{
        		provisionedMRCBundleIds.add(bundleId);
        		
        		if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this, "Bundle ID : " + bundleId + " for Subscriber ID : " + newSub.getId() + 
							"falls under MRC group is already provisioned so it is going to be removed from BM then going to be added");
				}
        	}
        }
		
		// Get the bucket list which is not being modified
		bundleList = getBuckets(ctx, newSub);
		
		// adding it back back to new bundles to provide recurrence scheme while reprovisioning
        for (final Iterator<Map.Entry<Long, SubscriberBucket>> i = bundleList.entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, SubscriberBucket> newEntry = i.next();
            final Long newID = newEntry.getKey();
            if (provisionedMRCBundleIds.contains(newID))
            {
                newBundles.put(newEntry.getKey(),newEntry.getValue());
            }
        }
        
		// MRC group bundles has to be reset thats why they have to put in both oldIds and newIds
        oldIds.addAll(provisionedMRCBundleIds);
        newIds.addAll(provisionedMRCBundleIds);
    }
    
    private Set<Long> getMRCBundleIds(final Context ctx,final Subscriber subscriber)
    {
    	final Set<Long> bundleIds = new HashSet<Long>();
    	
    	for(Iterator i = SubscriberBundleSupport.getSubscribedBundles(ctx, subscriber).values().iterator(); i.hasNext();)
		{
			BundleFee bundleFee = (BundleFee) i.next();

			if ( bundleFee != null && bundleFee.getApplyWithinMrcGroup())
			{
				bundleIds.add(bundleFee.getId());
			}
		}
    	
    	return bundleIds;
    }
    
    private Set<Long> getMRCServiceIds(final Context ctx,Subscriber subscriber) throws HomeException
    {
    	final Set<Long> serviceIds = new HashSet<Long>();
    	Map services = subscriber.getPricePlan(ctx).getServiceFees(ctx);
    	
    	for (ServiceFee2ID serviceFee2ID : subscriber.getServices(ctx))
		{
			ServiceFee2 serviceFee = (ServiceFee2) services.get(serviceFee2ID);

			if ( serviceFee != null && serviceFee.getApplyWithinMrcGroup())
			{
				serviceIds.add(serviceFee2ID.getServiceId());
			}
		}
    	
    	return serviceIds;
    }


    public void markProvisionedBundles(final Context ctx, final Collection<SubscriberBucket> createAux, final Collection<SubscriberBucket> removeAux,
            final Collection<Long> oldIds, final Collection<Long> newIds, final Subscriber oldSub, final Subscriber newSub)
    {
        // TODO this setting is useless as this represents the view of CRM on things, which is useless
        // TODO consider removing this logic and provision Bundles field alltogether.
        final Collection<Long> createIDs = new ArrayList<Long>();
        for (SubscriberBucket bucket : createAux)
        {
            createIDs.add(bucket.getBundleId());
        }

        final Collection<Long> removedIDs = new ArrayList<Long>();
        for (SubscriberBucket bucket : removeAux)
        {
            removedIDs.add(bucket.getBundleId());
        }

        if (oldSub != null)
        {
            oldSub.bundleUnProvisioned(oldIds);
            oldSub.bundleProvisioned(newIds);

            oldSub.bundleUnProvisioned(removedIDs);
            oldSub.bundleProvisioned(createIDs);
        }


        if (newSub != null)
        {
            newSub.bundleUnProvisioned(oldIds);
            newSub.bundleProvisioned(newIds);

            newSub.bundleUnProvisioned(removedIDs);
            newSub.bundleProvisioned(createIDs);
        }
    }

    /**
     * This method makes the calls for BM bucket provisioning.
     *
     * @param ctx the operating context
     * @param newSub the Subscriber that we change
     * @param oldSub old values for the Subscriber that we change
     * @param spid   the spid this msisdn belongs to
     * @throws HomeException the exception that occured in BM wrapped in a HomeException. This should stop processing.
     */
    public void provision(final Context ctx, final Subscriber newSub, final Subscriber oldSub, final int spid, final Map<Long, SubscriberBucket> newBundles,
            final List<SubscriberBucket> createAux, final List<SubscriberBucket> removeAux, final List<Long> oldIds, final List<Long> newIds, final Date chargeDate)
        throws HomeException
    {
        CRMBundleProfile service = (CRMBundleProfile)ctx.get(CRMBundleProfile.class);
        CRMSubscriberBucketProfile bucketService = (CRMSubscriberBucketProfile) ctx.get(CRMSubscriberBucketProfile.class);
        boolean successSwitchBundles = true;

        final String msisdn = newSub.getMSISDN();
        try
        {
            // if both lists are empty, there was no bundle provisioning and we have no need to bother BM
            if (!(oldIds.isEmpty() && newIds.isEmpty()))
            {
            	final Collection bucketOptions = new ArrayList(
            			(List) Visitors.forEach(ctx, newBundles, new MapVisitor(SubscriberBucketXInfo.OPTIONS)));
            	Map<Long, Long> overusages = service.switchBundles(
            			ctx,
            			msisdn,
            			spid,
            			(int) newSub.getSubscriptionType(),
            			oldIds,
            			newIds,
            			bucketOptions);

            	// we should not charge bundle overusage before we do refund in case of price plan change. 
            	// overusage is moved to charger which can handle both charge and refund properly. 
            	// BundleChargingSupport.overUsageCharge
            	newSub.setBundleOverUsage(overusages);
            }
            else
            {
                newSub.setBundleOverUsage(new HashMap<Long, Long>());
            }
        }
        catch (AgentException e)
        {
            successSwitchBundles = false;
            throw new HomeException(e);
        }
        catch (BundleManagerException e)
        {
            successSwitchBundles = false;
            throw new HomeException("Unable to (un)provisioning bundle(s) on URCS: " + e.getMessage(), e);
        }
        catch (SubscriberProfileDoesNotExistException e)
        {
            successSwitchBundles = false;
            throw new HomeException(e);
        }
        finally
        {
            
            for (Long id : oldIds)
            {
                ServiceStateEnum state = successSwitchBundles?ServiceStateEnum.UNPROVISIONED:ServiceStateEnum.UNPROVISIONEDWITHERRORS;
    
                try
                {
                    BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, id);
                    SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, newSub,
                            HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.BUNDLE, bundle, state);
                }
                catch (InvalidBundleApiException e)
                {
                    final String message = "Error creating subscriber subscription history for bundle profile '" + id
                            + "' unprovisioning to subscriber '" + msisdn + "' due to error: "
                            + e.getMessage();
                    LogSupport.minor(ctx, this, message, e);
                }
            }
            
            for (Long id : newIds)
            {
                ServiceStateEnum state = successSwitchBundles?ServiceStateEnum.PROVISIONED:ServiceStateEnum.PROVISIONEDWITHERRORS;
                
                try
                {
                    BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, id);
                    SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, newSub,
                            HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.BUNDLE, bundle, state);
                }
                catch (InvalidBundleApiException e)
                {
                    final String message = "Error creating subscriber subscription history for bundle profile '" + id
                            + "' provisioning to subscriber '" + msisdn + "' due to error: "
                            + e.getMessage();
                    LogSupport.minor(ctx, this, message, e);
                }
            }
        }
        
        for (final Iterator i = removeAux.iterator(); i.hasNext();)
        {
            SubscriberBucket fee = (SubscriberBucket) i.next();
            ServiceStateEnum state = ServiceStateEnum.UNPROVISIONED;
            try
            {
                newSub.getBundleOverUsage().put(Long.valueOf(fee.getBundleId()),
                        bucketService.removeBucket(ctx, spid, msisdn, fee.getSubscriptionType(), fee.getBundleId()));
            }
            catch (BundleManagerException e)
            {
                // Cannot rethrow exception because switchBundles already completed
                final Exception expt = new HomeException(e.getMessage(), e);
                final ExceptionListener exptListener = (ExceptionListener) ctx.get(ExceptionListener.class);
                if (exptListener != null)
                {
                    exptListener.thrown(expt);
                }
                LogSupport.minor(ctx, this, "BundleService Error while un-provisioning Aux. bundle.", e);
                state = ServiceStateEnum.UNPROVISIONEDWITHERRORS;
            }
            catch (BucketDoesNotExistsException e)
            {
                LogSupport.minor(ctx, this, "BundleService Error while un-provisioning Aux. bundle.", e);
                state = ServiceStateEnum.UNPROVISIONEDWITHERRORS;
            }
            finally
            {
                try
                {
                    BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, fee.getBundleId());
                    SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, newSub,
                            HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.AUXBUNDLE, bundle, state);
                }
                catch (InvalidBundleApiException e)
                {
                    final String message = "Error creating subscriber subscription history for bundle profile '" + fee.getBundleId()
                            + "' unprovisioning to subscriber '" + msisdn + "' due to error: "
                            + e.getMessage();
                    LogSupport.minor(ctx, this, message, e);
                }
            }
        }        

        /* create new Aux Bundles
         * The new interface accepts
        */
        for (SubscriberBucket bucket : createAux)
        {
            ServiceStateEnum state = ServiceStateEnum.PROVISIONED;
            try
            {
                bucketService.createBucket(ctx, bucket);
            }
            catch (BucketAlreadyExistsException e)
            {
                state = ServiceStateEnum.PROVISIONEDWITHERRORS;
                handleException(ctx, e.getMessage(), e);
            }
            catch (BundleManagerException e)
            {
                state = ServiceStateEnum.PROVISIONEDWITHERRORS;
                handleException(ctx, e.getMessage(), e);
            }
            finally
            {
                try
                {
                    BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bucket.getBundleId());
                    SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, newSub, 
                            HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.AUXBUNDLE, bundle, state);
                }
                catch (InvalidBundleApiException e)
                {
                    final String message = "Error creating subscriber subscription history for bundle profile '" + bucket.getBundleId()
                            + "' provisioning to subscriber '" + msisdn + "' due to error: "
                            + e.getMessage();
                    LogSupport.minor(ctx, this, message, e);
                }
            }
        }
    }
    
    /**
     * 
     * @param ctx
     * @param sub - Subscribers whose bundles need to be un-provisioned
     * @throws HomeException
     */
    public void unprovisionAllBundles(Context ctx, Subscriber sub, Subscriber newSub) throws HomeException
    {
        ctx = ctx.createSubContext();
        ctx.put(SubscriptionDeactivateValidator.WRITE_OFF, true);
        final Map<Long, SubscriberBucket> oldBundles = getBuckets(ctx, sub);
        addLoyaltyBuckets(ctx, sub, oldBundles);
        provision(ctx, sub.getSpid(), oldBundles, Collections.EMPTY_MAP, sub, newSub);
    }

    /**
     * Returns a Map of bundles produced for the received Map by removing One Time Bundles.
     * A new Map is returned, Map in parameter is not modified.
     *
     * @param ctx the operting context
     * @param bundles map to analyze
     * @return the resulting map
     */
    public static Map<Long, SubscriberBucket> removeOneTimeBundles(final Context ctx, final Map<Long, SubscriberBucket> bundles)
    {
        final Map<Long, SubscriberBucket> map = new HashMap<Long, SubscriberBucket>(bundles);

        try
        {
            for (final Iterator<Map.Entry<Long, SubscriberBucket>> it = map.entrySet().iterator(); it.hasNext();)
            {
                // TODO 2007-05-09 this is potentialy making a number of calls to BM. Optimize: get a list in one call
                BundleProfile profile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, it.next().getKey());
                if (profile.getRecurrenceScheme().isOneTime())
                {
                    it.remove();
                }
            }
	    }
        catch (HomeException e)
        {
            new MajorLogMsg(BucketProvHome.class, "Bundle Manager Error occured while getting the bundles", e).log(ctx);
        }
        catch (InvalidBundleApiException e)
        {
            new MajorLogMsg(BucketProvHome.class, "Bundle Manager Error occured while getting the bundles", e).log(ctx);
        }
        return map;
   }
 

    private void handleException(Context ctx, Throwable t)
    {
        handleException(ctx, "Error provisioning buckets. Error [" + ((t == null) ? "Unkown" : t.getMessage()) + "]", t);
    }


    private void handleException(Context ctx, String message, Throwable t)
    {
        FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new IllegalStateException(message, t));
        new DebugLogMsg(this, message, t);
        new MinorLogMsg(this, message + ". Error [ " + ((t == null) ? "Unkown" : t.getMessage()) + " ]", null);
    }
    
    private boolean isPicknPay(Context ctx,Subscriber sub)
    {
    	boolean picknPayFlag = false;
        PricePlan pp;
		try {
			pp = HomeSupportHelper.get(ctx).findBean(ctx, PricePlan.class, new EQ(PricePlanXInfo.ID, sub.getPricePlan()));

	        picknPayFlag = pp.getPricePlanSubType().equals(PricePlanSubTypeEnum.PICKNPAY);
	        
		} catch (HomeException e) {			
			LogSupport.minor(ctx, this,  "Error while finding the subscriber price plan home");
		}
		
		return picknPayFlag;

    }
}
