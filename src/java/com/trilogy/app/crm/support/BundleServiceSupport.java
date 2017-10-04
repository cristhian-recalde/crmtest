/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.exception.BundleDoesNotExistsException;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;


/**
 * Various support methods for bundle services <p/> Taken heavily from ServiceSupport and
 * modified for use with bundles. I am sure I can do something clever like adapting the
 * objects so only one Support class is needed but I don't have the time right now :(.
 *
 * @author danny.ng@redknee.com
 */
public final class BundleServiceSupport
{

    /**
     * Creates a new <code>BundleServiceSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private BundleServiceSupport()
    {
        // empty
    }


    /**
     * Whether a bundle is suspension prorated.
     *
     * @param ctx
     *            The operating context.
     * @param api
     *            Bundle.
     * @return Returns whether the suspension is prorated.
     */
    public static boolean isSuspensionProrated(Context ctx, BundleProfile api)
    {
        return !api.getSmartSuspensionEnabled();
    }


    /**
     * Whether a bundle's unsuspension is prorated.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber to be unsuspended.
     * @param api
     *            Bundle to be unsuspended.
     * @param adjustType
     *            Adjustment type.
     * @param chargingDay
     *            Day of charging.
     * @return Returns whether the unsuspension is prorated.
     * @throws HomeException
     *             Thrown if there are problems determining whether the unsuspension is
     *             prorated.
     */
    public static boolean isUnsuspendProrate(Context ctx, Subscriber sub, BundleFee item, BundleProfile api, ChargedItemTypeEnum itemType, int adjustType, long amount,
                                             Date chargingDay)
            throws HomeException
    {

        if (!api.getSmartSuspensionEnabled())
        {
            return true;
        }
        return !RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(ctx, sub, item, itemType, item.getServicePeriod(), adjustType, amount, chargingDay);
    }


    /**
     * Determines whether a particular service of the subscriber has been unsuspended.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber.
     * @param adjustType
     *            Adjustment type of the service.
     * @return Whether the service with the provided adjustment type has been unsuspended
     *         from the subscriber.
     */
    public static boolean isUnsuspend(final Context ctx, final Subscriber sub, final int adjustType)
    {
        final Collection trans = CoreTransactionSupportHelper.get(ctx).getTransactionsForSubAdjustment(ctx, sub.getId(), adjustType,
            new Limit(1));
        return trans != null && trans.size() > 0;
    }


    /**
     * Returns the bundle with the provided identifier.
     *
     * @param context
     *            The operating context.
     * @param spId
     *            service provider identifier.
     * @param id
     *            Bundle identifier.
     * @return The identified bundle.
     * @throws HomeException
     *             Thrown if there are problems looking up the bundle.
     * @throws BundleManagerException
     * @throws BundleDoesNotExistsException
     */
    public static BundleProfile getBundle(Context context, int spId, long bundleId)
        throws HomeException, BundleDoesNotExistsException, BundleManagerException
    {
        return getBundle(context, getService(context), spId, bundleId);
    }


    /**
     * Returns the bundle with the provided identifier.
     * 
     * @param context
     *            The operating context.
     * @param service
     *            The BM service.
     * @param spId
     *            service provider identifier.
     * @param bundleId
     *            Bundle identifier.
     * @return The identified bundle.
     * @throws HomeException
     *             Thrown if there are problems looking up the bundle.
     * @throws BundleManagerException
     * @throws BundleDoesNotExistsException
     */
    public static BundleProfile getBundle(Context context, CRMBundleProfile service, int spId, long bundleId)
        throws HomeException, BundleDoesNotExistsException, BundleManagerException
    {
        if (service == null)
        {
            throw new HomeException("System error: bundle home cannot be null");
        }
        return service.getBundleProfile(context, spId, bundleId);
    }


    /**
     * Returns a collection of bundles belonging to a service provider.
     *
     * @param context
     *            The operating context.
     * @param spid
     *            Service provider identifier.
     * @return A collection of bundles belonging to the provided service provider.
     * @throws HomeException
     *             Thrown if there are problems looking up the bundles.
     * @throws BundleManagerException
     * @throws UnsupportedOperationException
     */
    public static Collection getBundlesBySpid(final Context context, final int spid)
        throws HomeException, UnsupportedOperationException, BundleManagerException
    {
        return getBundlesBySpid(context, getService(context), spid);
    }


    /**
     * Returns a collection of bundles belonging to a service provider.
     *
     * @param context
     *            The operating context.
     * @param service
     *            Bundle service.
     * @param spid
     *            Service provider identifier.
     * @return A collection of bundles belonging to the provided service provider.
     * @throws HomeException
     *             Thrown if there are problems looking up the bundles.
     * @throws BundleManagerException
     * @throws UnsupportedOperationException
     */
    public static Collection getBundlesBySpid(final Context context, final CRMBundleProfile service, final Integer spid)
        throws HomeException, UnsupportedOperationException, BundleManagerException
    {
        if (service == null)
        {
            throw new HomeException("System error: bundle home cannot be null");
        }
        return service.getBundlesBySPID(context, spid.intValue()).selectAll();
    }


    /**
     * Returns the bundle service.
     *
     * @param context
     *            The operating context.
     * @return Bundle home.
     * @throws HomeException
     *             Thrown if bundle home does not exist.
     */
    public static CRMBundleProfile getService(final Context context) throws HomeException
    {
        CRMBundleProfile service = (CRMBundleProfile) context.get(CRMBundleProfile.class);
        if (service == null)
        {
            throw new HomeException("System error: Bundle service does not exist");
        }
        return service;
    }
}
