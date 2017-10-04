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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.IsNull;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.MapVisitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.product.bundle.manager.provision.bundle.RecurrenceScheme;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.ChargingLevelEnum;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.exception.BundleDoesNotExistsException;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationHome;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationXInfo;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.ServicePackageSupportHelper;

/**
 * Helper methods for working with Subscriber bundle information.
 *
 * @author kevin.greer@redknee.com
 * @author victor.stratan@redknee.com
 */
public class SubscriberBundleSupport
{
    /**
     * Class name of this class, used in logging.
     */
    private static final String CLASS_NAME = SubscriberBundleSupport.class.getName();
 
    /**
     * Creates a new <code>SubscriberBundleSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private SubscriberBundleSupport()
    {
        // empty
    }

    /**
     * Add the supplied BundleFee to the supplied Map provided the map doesn't already
     * contain a bundle with the same Id.
     *
     * @param map The map to which to add the new BundleFee
     * @param fee The BundleFee to be added.
     */
    protected static void addFee(final Map<Long, BundleFee> map, final BundleFee fee)
    {
        final Long key = Long.valueOf(fee.getId());
        if (!map.containsKey(key))
        {
            map.put(key, fee);
        }
    }

    /**
     * Create Map of Bundles from Subscriber's PricePlan. Uses getRawPricePlan() i.e. the
     * cached Price Plan. Collecting bundles: 1. PricePlan Bundles 2. Package Bundles
     *
     * @param ctx The operating Context
     * @param sub Subscriber for which to return bundles.
     * @return A mapping of (bundle fee ID, bundle fee).
     */
    public static Map<Long, BundleFee> getPricePlanBundles(final Context ctx, final Subscriber sub)
    {
        Map<Long, BundleFee> map = new HashMap<Long, BundleFee>();
        try
        {
            final PricePlanVersion ppv = sub.getRawPricePlanVersion(ctx);
            if (ppv == null)
            {
                LogSupport.major(ctx, CLASS_NAME, "Missing PricePlanVersion for subscriber " + sub.getId(), null);
                return map;
            }
            final Home pricePlanHome = (Home) ctx.get(PricePlanHome.class);
            final PricePlan pp = (PricePlan) pricePlanHome.find(ctx, Long.valueOf(sub.getPricePlan()));
            map = getPricePlanBundles(ctx, pp, ppv);

        }
        catch (final Throwable t)
        {
            LogSupport.major(ctx, CLASS_NAME, "Internal Error " + sub.getId(), t);
        }
        return map;
    }
    

    /**
     * Creates a map of all bundles in given Price Plan, including in Packages.
     * 
     * @param ctx
     * @param pp        reference to PricePlan
     * @param ppv       reference to PricePlanVersion
     * @return
     */
    public static Map<Long, BundleFee> getPricePlanBundles(Context ctx, PricePlan pp, PricePlanVersion ppv)
    {
        final Map<Long, BundleFee> map = new HashMap<Long, BundleFee>();

        final Date now = new Date();
        final Date nowPlus20y = CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, now);

        final Map<Long, BundleFee> ppvMap = ppv.getServicePackageVersion().getBundleFees();
        final Map<Integer, ServicePackageFee> pkgMap = ppv.getServicePackageVersion().getPackageFees();
        // 1. PricePlan Bundles
        for (BundleFee fee : ppvMap.values())
        {
            try
            {
            BundleProfile bundle = fee.getBundleProfile(ctx, pp.getSpid());
            // filter out expired one time bundles
            if (!bundle.getRecurrenceScheme().isOneTime() || bundle.getEndDate() == null
                    || !bundle.getEndDate().before(now) || bundle.getEndDate().equals(new Date(0)))
            {
                fee.setSource("Price Plan: " + pp.getName());
                Date endDate = nowPlus20y;
                
                if (bundle.getRecurrenceScheme().equals(RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL))
                {
                    if (bundle.getInterval() == DurationTypeEnum.MONTH_INDEX)
                    {
                        endDate = CalendarSupportHelper.get(ctx).findDateMonthsAfter(bundle.getValidity(), now);
                    }
                    else if(bundle.getInterval() == DurationTypeEnum.DAY_INDEX)
                    {
                        endDate = CalendarSupportHelper.get(ctx).findDateDaysAfter(bundle.getValidity(), now);
                    }
                    else if(bundle.getInterval() == DurationTypeEnum.BCD_INDEX)
                    {
                        endDate = fee.getEndDate();
                    }
                }
                fee.setStartDate(now);
                /*
                 * Sujeet: What are we doing here? I have commented the line one below the next.
                 * If this was really intended, please uncomment back, and write comments to indicate the intention.
                 */
                fee.setEndDate(endDate);
                // fee.setEndDate(nowPlus20y);
                
                addFee(map, fee);
            }
            }
            catch(Exception e)
            {
                LogSupport.major(ctx, CLASS_NAME, "Internal Error " + pp.getId(), e);
            }
        }
        
        // PricePlan Packages
        final Home packageHome = (Home) ctx.get(ServicePackageHome.class);

        // 2. Package Bundles
        for (ServicePackageFee pkgFee : pkgMap.values())
        {
            try
            {
                final ServicePackage pkg = (ServicePackage) packageHome.find(ctx, Integer
                        .valueOf(pkgFee.getPackageId()));
                final Map<Long, BundleFee> bundlesMap = ServicePackageSupportHelper.get(ctx).getCurrentVersion(ctx, pkg.getId())
                        .getBundleFees();
                final StringBuilder sb = new StringBuilder();
                sb.append("Package: ");
                sb.append(pkg.getName());
                if (pkg.getChargingLevel() == ChargingLevelEnum.PACKAGE)
                {
                    sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Fee: ");
                    final Currency currency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);
                    sb.append(currency.formatValue(pkg.getRecurringRecharge()));
                }
                final String source = sb.toString();
                for (BundleFee fee : bundlesMap.values())
                {
                    if (pkgFee.isMandatory())
                    {
                        fee.setServicePreference(ServicePreferenceEnum.MANDATORY);
                    }
                    fee.setSource(source);
                    fee.setStartDate(now);
                    fee.setEndDate(nowPlus20y);
                    addFee(map, fee);
                }
            }
            catch (final Throwable t)
            {
                LogSupport.major(ctx, CLASS_NAME, "Internal Error " + pp.getId(), t);
            }
        }
        return map;
    }

    /**
     * Create Map of auxiliary bundles that a Subscriber could subscribe to, including
     * ones that aren't currently selected. This is probably *NOT* the method that you're
     * looking for. See getSubscribedBundles().
     *
     * @param ctx
     *            The operating Context
     * @param sub
     *            Subscriber for which to return bundles.
     * @return A map of (bundle fee ID, bundle fee) of all bundles (not auxiliary) which
     *         the subscriber MAY subscribe to.
     */
    public static Map getAvailableAuxiliaryBundles(final Context ctx, final Subscriber sub)
    {
        Map map = new HashMap();
        final Home bundleHome = (Home) ctx.get(BundleProfileHome.class);
        try
        {
            map = getAvailableAuxiliaryBundles(ctx, sub.getSpid(), sub.getSubscriberType());
        }
        catch (final HomeException exception)
        {
            LogSupport.info(ctx, SubscriberBundleSupport.class,
                "Exception caught when looking up all available bundles for subscriber " + sub.getId(), exception);
        }
        return map;
    }


    /**
     * Create Map of auxiliary bundles that a Subscriber could subscribe to, including
     * ones that aren't currently selected. This is probably *NOT* the method that you're
     * looking for. See getSubscribedBundles().
     * 
     * @param ctx
     *            The operating Context
     * @param spid 
     *            CRM SPID for which to return bundles. Set to -1 for all SPIDs.
     * @param subType
     *            Subscriber for which to return bundles.
     * @return A map of (bundle fee ID, bundle fee) of all bundles (not auxiliary) which
     *         the subscriber MAY subscribe to.
     */
    public static Map getAvailableAuxiliaryBundles(final Context ctx, int spid, final SubscriberTypeEnum subType)
            throws HomeException
    {
        final Map map = new HashMap();
        final Home bundleHome = (Home) ctx.get(BundleProfileHome.class);
        final And and = new And();
        and.add(new EQ(BundleProfileXInfo.AUXILIARY, Boolean.TRUE));
        and.add(new EQ(BundleProfileXInfo.ENABLED, Boolean.TRUE));
        if (spid > 0)
        {
            and.add(new EQ(BundleProfileXInfo.SPID, spid));
        }
        final BundleSegmentEnum segment;
        if (subType == SubscriberTypeEnum.POSTPAID)
        {
            segment = BundleSegmentEnum.POSTPAID;
        }
        else
        {
            segment = BundleSegmentEnum.PREPAID;
        }
        and.add(new EQ(BundleProfileXInfo.SEGMENT, segment));
        final Collection bundles = bundleHome.select(ctx, and);
        for (final Iterator iterator = bundles.iterator(); iterator.hasNext();)
        {
            final BundleProfile bundle = (BundleProfile) iterator.next();
            if (bundle.isEnabled() && bundle.isAuxiliary())
            {
                final BundleFee fee = new BundleFee();
                fee.setId(bundle.getBundleId());
                fee.setSource(BundleFee.AUXILIARY);
                fee.setFee(bundle.getAuxiliaryServiceCharge());
                fee.setServicePreference(ServicePreferenceEnum.OPTIONAL);
                fee.setBundleProfile(bundle);
                addFee(map, fee);
            }
        }
        return map;
    }

	/**
	 * Create Map of auxiliary bundles that a Subscriber could subscribe to or
	 * already has, including
	 * ones that aren't currently selected and deprecated ones. This is probably
	 * *NOT* the method that you're
	 * looking for. See getSubscribedBundles().
	 * 
	 * @param ctx
	 *            The operating Context
	 * @param spid
	 *            CRM SPID for which to return bundles. Set to -1 for all SPIDs.
	 * @param subType
	 *            Subscriber for which to return bundles.
	 * @return A map of (bundle fee ID, bundle fee) of all bundles (not
	 *         auxiliary) which
	 *         the subscriber MAY subscribe to.
	 */
	public static Map<Long, BundleFee>
	    getAvailableAuxiliaryBundlesForSubscriber(
	    final Context ctx, int spid,
	        final SubscriberTypeEnum subType, Subscriber sub)
	        throws HomeException
	{
		final Map<Long, BundleFee> map = new HashMap<Long, BundleFee>();
		final Home bundleHome = (Home) ctx.get(BundleProfileHome.class);
		final And and = new And();
		and.add(new EQ(BundleProfileXInfo.AUXILIARY, Boolean.TRUE));

		if (spid > 0)
		{
			and.add(new EQ(BundleProfileXInfo.SPID, spid));
		}
		final BundleSegmentEnum segment;
		if (subType == SubscriberTypeEnum.POSTPAID)
		{
			segment = BundleSegmentEnum.POSTPAID;
		}
		else
		{
			segment = BundleSegmentEnum.PREPAID;
		}
		and.add(new EQ(BundleProfileXInfo.SEGMENT, segment));
		Map<Long, BundleFee> subscribedBundles = getSubscribedBundles(ctx, sub);

		final Collection bundles = bundleHome.select(ctx, and);
		for (final Iterator iterator = bundles.iterator(); iterator.hasNext();)
		{
			final BundleProfile bundle = (BundleProfile) iterator.next();
			boolean eligible =
			    subscribedBundles
			        .containsKey(Long.valueOf(bundle.getBundleId()))
			        || bundle.isEnabled();
			if (eligible && bundle.isAuxiliary())
			{
				final BundleFee fee = new BundleFee();
				fee.setId(bundle.getBundleId());
				fee.setSource(BundleFee.AUXILIARY);
				fee.setFee(bundle.getAuxiliaryServiceCharge());
				fee.setServicePreference(ServicePreferenceEnum.OPTIONAL);
				fee.setBundleProfile(bundle);
				addFee(map, fee);
			}
		}
		return map;
	}
    
    /**
     * Create Map of Bundles that a Subscriber could subscribe to (not including auxiliary
     * bundles that aren't currently selected). This is probably *NOT* the method that
     * you're looking for. See getSubscribedBundles().
     *
     * @param ctx The operating Context
     * @param sub Subscriber for which to return bundles.
     * @return A map of (bundle fee ID, bundle fee) of all bundles (not auxiliary) which
     *         the subscriber MAY subscribe to.
     */
    public static Map<Long, BundleFee> getAllBundles(final Context ctx, final Subscriber sub)
    {
        final Map<Long, BundleFee> ppBundleFeeMap;
        
        //TT#13020107017  - Setting the spid from Subscriber in context which would be helpful for fetching.
        //Not creating Subcontext here. It's responsbility of whoever is invoking this method.
        com.redknee.framework.xhome.msp.MSP.setBeanSpid(ctx, sub.getSpid());
        
        if (sub.getPricePlan() != AbstractSubscriber.DEFAULT_PRICEPLAN)
        {
            ppBundleFeeMap = getPricePlanBundles(ctx, sub);
        }
        else
        {
            ppBundleFeeMap = new HashMap<Long, BundleFee>();
        }
        
        Map<Long, BundleFee> map = new HashMap<Long, BundleFee> ();
        
        if(!ppBundleFeeMap.isEmpty())
        {
        	Iterator<BundleFee> j = ppBundleFeeMap.values().iterator();
        	while(j.hasNext())
        	{
        		BundleFee ppBundleFee = j.next();
        		BundleFee fee;
				try 
				{
					fee = (BundleFee) ppBundleFee.deepClone();
					map.put(fee.getId(), fee);
				} 
				catch (CloneNotSupportedException e) 
				{
					LogSupport.minor(ctx, SubscriberBundleSupport.class, "Exception occured while cloning PP version bundles ", e);
					// continue with original map
					map.clear();
					map = ppBundleFeeMap;
				}
        	}
        }

        for (Iterator<Map.Entry<Long, BundleFee>> i = sub.getBundles().entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, BundleFee> entry = i.next();
            final Long key = entry.getKey();
            final BundleFee fee = entry.getValue();
            final BundleFee ppFee = map.get(key);

            // the fee.isAuxiliarySource() check prevents previously optional bundles
            // from being migrated to auxiliary once they are removed from the PP
            if (ppFee == null && fee.isAuxiliarySource())
            {
                // add check that it is actually auxiliary
                // treat as selected auxiliary bundle
                try
                {
                    // TODO 2007-01-16 Optimise this to make only one call to BM to determine the auxiliary bundles
                    BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, fee.getId());

                    if (bundle != null && bundle.isAuxiliary()
                            && (!bundle.getRecurrenceScheme().isOneTime()
                                    || bundle.getEndDate() == null
                            || !bundle.getEndDate().before(new Date())
                            || bundle.getEndDate().equals(new Date(0))))
                    {
                        fee.setSource(BundleFee.AUXILIARY);
                        fee.setFee(bundle.getAuxiliaryServiceCharge());
                        fee.setServicePreference(ServicePreferenceEnum.OPTIONAL);
                        addFee(map, fee);
                    }
                }
                catch (final HomeException hEx)
                {
                    LogSupport.minor(ctx, CLASS_NAME, "cannot look up bundle " + fee.getId(), hEx);
                }
                catch (InvalidBundleApiException e)
                {
                    new MinorLogMsg(CLASS_NAME, "cannot look up bundle " + fee.getId(), e).log(ctx);
                }
            }
            // fillin dates for non mandatory PP bundles
            if (ppFee != null && !(fee.getServicePreference() == ServicePreferenceEnum.MANDATORY))
            {
                ppFee.setStartDate(fee.getStartDate());
                ppFee.setRepurchaseHappened(fee.getRepurchaseHappened());
                ppFee.setEndDate(fee.getEndDate());
            }
            
            
            /*
             * Set next recurring charge date
             */
            if(ppFee != null)
            {
            	ppFee.setNextRecurringChargeDate(fee.getNextRecurringChargeDate());
            }
            
        	
        }
        return map;
    }

    /**
     * Create Map of Bundles that a Subscriber is subscribed to. This set includes all
     * Mandatory bundles from the priceplan along with those from sub.getBundles() which
     * do not have their start-date in the future and are either Auxiliary or else
     * PricePlan-Optional. This is the method that you are probably looking for.
     *
     * @param ctx The operating Context
     * @param sub Subscriber for which to return bundles.
     * @return A map of (bundle fee ID, bundle fee) of all bundles the subscriber is
     *         subscribed to.
     */
    public static Map<Long, BundleFee> getSubscribedBundles(final Context ctx, final Subscriber sub)
    {
        final Map<Long, BundleFee> inMap = getAllBundles(ctx, sub);
        final Map<Long, BundleFee> outMap = new HashMap<Long, BundleFee>();
        final Date today = new Date();
        
        //TT#13020107017  - Setting the spid from Subscriber in context which would be helpful for fetching.
        //Not creating Subcontext here. It's responsbility of whoever is invoking this method.
        com.redknee.framework.xhome.msp.MSP.setBeanSpid(ctx, sub.getSpid());
        
        for (final Iterator<Map.Entry<Long, BundleFee>> i = inMap.entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, BundleFee> entry = i.next();
            final Long key = entry.getKey();
            final BundleFee fee = entry.getValue();
            // Danny: This version has been modified to only exclude start
            // dates in the future. It use to also exclude end dates
            // in the past, but I needed to include ones with end dates in
            // the past for end date deprovisioning
            if(sub.getBundles() != null)
            {
            if (fee.getServicePreference() == ServicePreferenceEnum.MANDATORY
                    || sub.getBundles().containsKey(key)
                    && (fee.getStartDate() == null || fee.getStartDate().compareTo(today) <= 0))
            {
            	Map<Long, BundleFee> subBundleFeeMap = sub.getBundles();
            	final BundleFee bundleFee = subBundleFeeMap.get(key);
            	if(bundleFee != null)
            	{
                fee.setNextRecurringChargeDate(bundleFee.getNextRecurringChargeDate());
            	fee.setRepurchaseHappened(bundleFee.getRepurchaseHappened());
                outMap.put(key, fee);
            	}
            }
            }
        }
        return outMap;
    }
    
    /**
     * 
     * @param ctx
     * @param sub
     * @param nextRecurringChargeDate : return bundle-fee map whose next recurring charge date is equal to next-recurring-charge-date passed in as a parameter.
     * @return
     */
    public static Map<Long, BundleFee> getSubscribedBundles(final Context ctx, final Subscriber sub, final Date nextRecurringChargeDate)
    {
        final Map<Long, BundleFee> inMap = getAllBundles(ctx, sub);
        final Map<Long, BundleFee> outMap = new HashMap<Long, BundleFee>();
        final Date today = new Date();
        boolean notificationOnly = ctx.getBoolean(RecurringRechargeSupport.NOTIFICATION_ONLY);
        
        for (final Iterator<Map.Entry<Long, BundleFee>> i = inMap.entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, BundleFee> entry = i.next();
            final Long key = entry.getKey();
            final BundleFee fee = entry.getValue();
            // Danny: This version has been modified to only exclude start
            // dates in the future. It use to also exclude end dates
            // in the past, but I needed to include ones with end dates in
            // the past for end date deprovisioning
            if (fee.getServicePreference() == ServicePreferenceEnum.MANDATORY
                    || sub.getBundles().containsKey(key)
                    && (fee.getStartDate() == null || fee.getStartDate().compareTo(today) <= 0))
            {
            	Map<Long, BundleFee> subBundleFeeMap = sub.getBundles();
            	if(nextRecurringChargeDate != null)
            	{
            		Date nextRecurWithNoTimeOfDay = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(nextRecurringChargeDate);
            		Date nextRecurFromBundleFeeWithTimeOfDay = subBundleFeeMap.get(key).getNextRecurringChargeDate();
            		/*
            		 * nextRecurFromBundleFeeWithTimeOfDay is NULL for ONE TIME bundle
            		 */
            		Date nextRecurFromBundleFee = ((nextRecurFromBundleFeeWithTimeOfDay== null)?null: CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(nextRecurFromBundleFeeWithTimeOfDay));

            		if(nextRecurFromBundleFee != null)
            		{
            			if
                    	(
                    			( !notificationOnly && nextRecurFromBundleFee.compareTo(nextRecurWithNoTimeOfDay) > 0 ) // NRC is greater than billingDate, bundle not chargeable 
                    				||
                    			( notificationOnly && nextRecurFromBundleFee.compareTo(nextRecurWithNoTimeOfDay) != 0)  // NRC not equals billingDate, subscriber not notifiable.
                    	)	
                    	{
                    		continue;
                    	}

            		}
            	}
            	
            	
            	fee.setNextRecurringChargeDate(subBundleFeeMap.get(key).getNextRecurringChargeDate());
                outMap.put(key, fee);
            }
        }
        return outMap;
    }
    

    /**
     * Create Map of Bundles that a Subscriber is subscribed to, including Point Bundles.
     *
     * @param ctx The operating Context
     * @param sub Subscriber for which to return bundles.
     * @return A map of (bundle fee ID, bundle fee) of all bundles the subscriber is
     *         subscribed to, including point bundles.
     */
    public static Map<Long, BundleFee> getSubscribedBundlesWithPointsBundles(final Context ctx, final Subscriber sub)
    {
        final Map<Long, BundleFee> pointMap = sub.getPointsBundles();
        final Map<Long, BundleFee> outMap = getSubscribedBundles(ctx, sub);
        // Add loyalty points bundles to the map
        for (final Iterator<Map.Entry<Long, BundleFee>> i = pointMap.entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, BundleFee> entry = i.next();
            final Long key = entry.getKey();
            final BundleFee fee = entry.getValue();
            fee.setSource("Loyalty Points");
            outMap.put(key, fee);
        }
        return outMap;
    }

    /**
     * Create and return a Collection of bundle IDs that are provisioned for a MSISDN on
     * Bundle Manager.
     *
     * @param ctx    the operating context
     * @param msisdn MSISDN of a subscriber to be queried
     * @param subscriptionType type of subscription
     * @return the Collection of bundle IDs
     */
    public static Collection<Long> getProvisionedOnBundleManager(final Context ctx, final String msisdn, int subscriptionType)
    {
        CRMSubscriberBucketProfile bucketService = (CRMSubscriberBucketProfile) ctx.get(CRMSubscriberBucketProfile.class);
        final Collection<Long> result = new HashSet<Long>();
        if (bucketService != null)
        {
            try
            {
                // cannot use forEach() - BM does not support it
                final Collection<SubscriberBucket> buckets = bucketService.getBuckets(ctx, msisdn, subscriptionType).selectAll();
                for (SubscriberBucket bucket : buckets)
                {
                    result.add(bucket.getBundleId());
                }
            }
            catch (final HomeException e)
            {
                LogSupport.minor(ctx, CLASS_NAME, "Cannot getProvisionedOnBundleManager() " + msisdn, e);
            }
            catch (BundleManagerException e)
            {
                LogSupport.minor(ctx, CLASS_NAME, "Cannot getProvisionedOnBundleManager() " + msisdn, e);
            }
        }
        return result;
    }

    /**
     * Gets all the one time bundles within a set of bundle id's
     *
     * @param ctx
     * @param bundleFees
     * @return
     */
    public static Collection<BundleProfile> filterOneTimeBundles(final Context ctx, final Collection<BundleFee> bundleFees)
    {
        Set<Long> bundleIDs = new HashSet<Long>();
        for (BundleFee bundleFee : bundleFees)
        {
            bundleIDs.add(bundleFee.getId());
        }

        try
        {
            CRMBundleProfile service = (CRMBundleProfile) ctx.get(CRMBundleProfile.class);
            return service.getOneTimeBundles(ctx, bundleIDs).selectAll();
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, CLASS_NAME, "Cannot obtain OneTimeBundle list!", e);
        }
        return new ArrayList<BundleProfile>();
    }

    /**
     * Return a subContext that has a filtered bundles home based on price plan
     * association table, bundle segment type and auxiliary flag.
     *
     * @param ctx           Context
     * @param ppv           Price Plan Version for which to check associations table
     * @param type          what type of bundles to select: prepaid or postpaid
     * @param onlyAuxiliary select only auxiliary bundles or all bundles
     * @return subcontext with filtering bundles home
     * @throws HomeException Thrown if the price plan cannot be found due to configuration.
     */
    public static Context filterBundlesOnPricePlan(final Context ctx, final PricePlan pricePlan, 
            final SubscriberTypeEnum type, final boolean onlyAuxiliary) 
        throws HomeException
    {
        Context subCtx = ctx.createSubContext();

        Home bundleHome = null;
        Home assocHome = (Home) subCtx.get(RatePlanAssociationHome.class);
        CRMBundleProfile service = (CRMBundleProfile) ctx.get(CRMBundleProfile.class);

        if (assocHome == null)
        {
            throw new HomeException("System error: no RatePlanAssociationHome found in context.");
        }
        final String voiceRatePlan = pricePlan.getVoiceRatePlan();
        final String smsRatePlan = pricePlan.getSMSRatePlan();
        final String dataRatePlan = pricePlan.getDataRatePlan();
        final Or or = new Or();
        if (voiceRatePlan.length() > 0)
        {
            or.add(new EQ(RatePlanAssociationXInfo.VOICE_RATE_PLAN, voiceRatePlan));
        }
        if (smsRatePlan.length() > 0)
        {
            or.add(new EQ(RatePlanAssociationXInfo.SMS_RATE_PLAN, smsRatePlan));
        }
        if (dataRatePlan.length() >= 0)
        {
            or.add(new EQ(RatePlanAssociationXInfo.DATA_RATE_PLAN, dataRatePlan));
        }
        final BundleSegmentEnum segment;
        if (type == SubscriberTypeEnum.POSTPAID)
        {
            segment = BundleSegmentEnum.POSTPAID;
        }
        else
        {
            segment = BundleSegmentEnum.PREPAID;
        }
        try
        {
            // find applicable bundleIds from RatePlanAssociationHome
            final Collection<Long> bundleIds = (Collection<Long>) assocHome.where(subCtx, or).forEach(subCtx,
                    new MapVisitor(RatePlanAssociationXInfo.BUNDLE_ID));

            // filter the BundleProfileApiHome to bundles that matches the rate
            // plan association
            bundleHome = service.getBundlesBySegment(ctx, segment, bundleIds, onlyAuxiliary);

            bundleHome = filterExpiredOneTimeBundles(ctx, bundleHome);

            // save the home to the subCtx
            subCtx.put(BundleProfileHome.class, bundleHome);
        }
        catch (final HomeException hEx)
        {
            LogSupport.minor(subCtx, CLASS_NAME,
                    "fail to filter bundle IDs from BundleProfileApiHome based on rate plan assocation table", hEx);
        }
        catch (BundleDoesNotExistsException e)
        {
            new MinorLogMsg(CLASS_NAME,
                    "fail to filter bundle IDs from BundleProfileApiHome based on rate plan assocation table",
                    e).log(subCtx);
        }
        catch (BundleManagerException e)
        {
            new MinorLogMsg(CLASS_NAME,
                    "fail to filter bundle IDs from BundleProfileApiHome based on rate plan assocation table",
                    e).log(subCtx);
        }
        return subCtx;
    }

    /**
     * Return a Bundle Profile Home that has a filtered only for point bundles.
     *
     * @param ctx Context
     * @return Filtered bundle profile home for point bundles
     */
    public static Home filterPointBundles(final Context ctx)
    {
        Home bundleHome = null;
        
        CRMBundleProfile service = (CRMBundleProfile) ctx.get(CRMBundleProfile.class);
        if (service != null)
        {
            try
            {
                bundleHome = service.getBundlesPointBundlesByQuotaScheme(ctx, QuotaTypeEnum.MOVING_QUOTA);
            }
            catch (BundleManagerException e)
            {
                new MinorLogMsg(CLASS_NAME,
                        "fail to filter bundle IDs from BundleProfileHome based point bundles",
                        e).log(ctx);
            }   
        }
        else
        {
            new MajorLogMsg(CLASS_NAME,
                    "no bundle profile service available",
                    null).log(ctx);
        }

        return bundleHome;
    }

    public static Home filterExpiredOneTimeBundles(Context ctx, Home home)
    {
        Or or = new Or();
        or.add(new EQ(BundleProfileXInfo.RECURRENCE_SCHEME, Integer.valueOf(RecurrenceScheme._RECUR_CYCLE_FIXED_DATETIME)));
        or.add(new EQ(BundleProfileXInfo.RECURRENCE_SCHEME, Integer.valueOf(RecurrenceScheme._RECUR_CYCLE_FIXED_INTERVAL)));
        or.add(new EQ(BundleProfileXInfo.RECURRENCE_SCHEME, Integer.valueOf(RecurrenceScheme._ONE_OFF_FIXED_INTERVAL)));
        or.add(new GTE(BundleProfileXInfo.END_DATE, new Date()));
        or.add(new EQ(BundleProfileXInfo.END_DATE, new Date(0)));
        or.add(new IsNull(BundleProfileXInfo.END_DATE));

        return home.where(ctx, or);
    }


    public static Collection<BundleProfile> selectBundlesFromListOfType(final Context ctx, final Set ids, final GroupChargingTypeEnum type)
        throws HomeException
    {
        Collection<BundleProfile> bundles = null;
        try
        {
            CRMBundleProfile service = (CRMBundleProfile)ctx.get(CRMBundleProfile.class);
            bundles = service.getBundlesByGroupScheme(ctx, type, ids, true).selectAll();
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, CLASS_NAME, "selectBundlesFromListOfType: Exception while getting the desired bundles: " + e.getMessage(), e);
        }
        return bundles;
    }

}
