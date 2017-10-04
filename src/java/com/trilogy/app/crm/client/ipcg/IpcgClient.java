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
package com.trilogy.app.crm.client.ipcg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.product.s5600.ipcg.rating.provisioning.RatePlan;
import com.trilogy.product.s5600.iprc.rating.provisioning.IprcRatePlan;


/**
 * Provides a common interface for interacting with the IPCG external service.
 *
 * @author gary.anderson@redknee.com
 */
public
interface IpcgClient
{
    /**
     * Gets all of the available IPCG rate plans.
     *
     * @param context The operating context.
     * @return All of the available IPCG rate plans.
     *
     * @exception IpcgRatingProvException Thrown if problems occur during provisioning.
     */
    RatePlan[] getAllRatePlans(Context context)
        throws IpcgRatingProvException;
    
    /**
     * Gets all of the available IPCG rate plans that are applicable to the requested Service Provider, 
     * including the rate plans that are configured available for All Service Providers.
     *
     * @param context The operating context.
     * @param spid The Service Provider
     * @return All of the available IPCG rate plans.
     *
     * @exception IpcgRatingProvException Thrown if problems occur during provisioning.
     */
    IprcRatePlan[] getAllRatePlans(Context context, int spid)
        throws IpcgRatingProvException;


    /**
     * Adds a new subscriber profile to IPCG.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to create an IPCG profile.
     * @param billingCycleDate The billing cycle of the subscriber.
     * @param timeZone The time-zone of the subscriber.
     * @param ratePlan The IPCG rate plan of the subscriber.
     * @param scpId The SCP identifier of the subscriber.
     * @param subBasedRatingEnabled True if enabled; false otherwise.
     * @param serviceGrade The service grade of the subscriber.
     *
     * @return Zero for success; non-zero otherwise.
     *
     * @exception IpcgSubProvException Thrown if problems occur during provisioning.
     */
    int addSub(
        Context context,
        Subscriber subscriber,
        short billingCycleDate,
        String timeZone,
        int ratePlan,
        int scpId,
        boolean subBasedRatingEnabled,
        int serviceGrade)
        throws IpcgSubProvException;


    /**
     * Updates the subscriber profile in IPCG.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to update the IPCG profile.
     * @param ratePlan The preferred IPCG rate plan of the subscriber.
     * @param serviceGrade The preferred service grade of the subscriber.
     *
     * @return Zero for success; non-zero otherwise.
     *
     * @exception IpcgSubProvException Thrown if problems occur during provisioning.
     */
    int addChangeSub(
        Context context,
        Subscriber subscriber,
        short billingCycleDate,
        int ratePlan,
        int serviceGrade)
        throws IpcgSubProvException;


    /**
     * Updates the subscriber profile in IPCG.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to update the IPCG profile.
     * @param billCycleDate The preferred bill cycle date of the subscriber.
     *
     * @return Zero for success; non-zero otherwise.
     *
     * @exception IpcgSubProvException Thrown if problems occur during provisioning.
     */
    int addChangeSubBillCycleDate(
        Context context,
        Subscriber subscriber,
        short billCycleDate)
        throws IpcgSubProvException;

    /**
     * Sets the enabled flag of the IPCG profile.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to update the enabled flag.
     * @param enabled True if the profile is enabled; false otherwise.
     *
     * @return Zero for success; non-zero otherwise.
     *
     * @exception IpcgSubProvException Thrown if problems occur during provisioning.
     */
    int setSubscriberEnabled(Context context, Subscriber subscriber, boolean enabled)
        throws IpcgSubProvException;

    /**
     * Determines whether or not a profile exists in IPCG for the given
     * subscriber.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to check for a profile.
     * @return True if a profile exists; false otherwise.
     *
     * @exception IpcgSubProvException Thrown if problems occur during provisioning.
     */
    boolean isSubscriberProfileAvailable(Context context, Subscriber subscriber)
        throws IpcgSubProvException;

    /**
     * Removes the subscriber's profile from IPCG.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to remove a profile from IPCG.
     *
     * @return Zero for success; non-zero otherwise.
     *
     * @exception IpcgSubProvException Thrown if problems occur during provisioning.
     */
    int removeSubscriber(Context context, Subscriber subscriber)
        throws IpcgSubProvException;

} // interface
