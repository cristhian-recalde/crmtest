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

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.StringHolder;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.product.s5600.ipcg.provisioning.SubscriberProfile;
import com.trilogy.product.s5600.ipcg.provisioning.SubscriberProfileHolder;
import com.trilogy.product.s5600.ipcg.provisioning.SubscriberProv;


/**
 * IPCG client.
 *
 * @author daniel.zhang@redknee.com
 */
public interface ProductS5600IpcgClient<T extends SubscriberProv>
{

    /**
     * Adds a new subscriber.
     *
     * @param sub
     *            CRM subscriber to be added.
     * @param billingCycleDate
     *            Bill cycle day.
     * @param timeZone
     *            Time zone of the subscriber.
     * @param ratePlan
     *            Data rate plan of the subscriber.
     * @param scpId
     *            SCP ID.
     * @param subBasedRatingEnabled
     *            Whether subscriber-based rating is enabled.
     * @param serviceGrade
     *            Service grade.
     * @return Result code of the CORBA call.
     * @throws IpcgSubProvException
     *             Thrown if there are problems communicating with IPCG CORBA service.
     */
    public int addSub(final Context ctx, final Subscriber sub, final short billingCycleDate, final String timeZone, final int ratePlan,
        final int scpId, final boolean subBasedRatingEnabled, final int serviceGrade) throws IpcgSubProvException;
    /**
     * Update subscriber.
     *
     * @param subProfile
     *            Subscriber profile.
     * @param reason
     *            Reason of the update.
     * @param tag
     *            Tag of the update.
     * @return Result code from IPCG CORBA service.
     * @deprecated Pascal from data team suggested to use the
     *             {@link #addChangeSub(SubscriberProfile)} instead.
     */
    @Deprecated
    public int updateSub(final SubscriberProfile subProfile, final StringHolder reason, final IntHolder tag);

    /**
     * Changes the subscriber's bill cycle day. If the subscriber does not exist on IPCG,
     * create the subscriber with the new bill cycle day instead.
     *
     * @param sub
     *            The subscriber to be updated.
     * @param billingCycleDate
     *            New bill cycle day.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int addChangeSubBillCycleDate(final Subscriber sub, final short billingCycleDate)
        throws IpcgSubProvException;


    /**
     * Updates the subscriber. If the subscriber does not exist on IPCG, create the
     * subscriber with the new parameters instead.
     *
     * @param sub
     *            The subscriber to be updated.
     * @param billingCycleDate
     *            New bill cycle day.
     * @param ratePlan
     *            IPCG rate plan.
     * @param serviceGrade
     *            IPCG service grade.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int addChangeSub(final Context ctx, final Subscriber sub, final short billingCycleDate, final int ratePlan,
        final int serviceGrade) throws IpcgSubProvException;


    /**
     * Enable subscriber.
     *
     * @param msisdn
     *            Subscriber MSISDN.
     * @param enabled
     *            Whether to enable or disable the subscriber.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int enableSubscriber(final String msisdn, final boolean enabled) throws IpcgSubProvException;

    /**
     * Updates the subscriber. If the subscriber does not exist on IPCG, create the
     * subscriber instead.
     *
     * @param subProfile
     *            The (IPCG) subscriber to be updated.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int addChangeSub(final SubscriberProfile subProfile) throws IpcgSubProvException;


    /**
     * Retrieves the subscriber profile from IPCG.
     *
     * @param msisdn
     *            MSISDN of the subscriber to be retrieved.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int getSub(final String msisdn) throws IpcgSubProvException;


    /**
     * Retrieves the subscriber profile from IPCG.
     *
     * @param msisdn
     *            MSISDN of the subscriber to be retrieved.
     * @param subProfile
     *            Holder of the subscriber profile to be retrieved.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int getSub(final String msisdn, final SubscriberProfileHolder subProfile) throws IpcgSubProvException;


    /**
     * Removes subscriber from IPCG.
     *
     * @param msisdn
     *            MSISDN of the subscriber to be removed.
     * @return Result code of IPCG CORBA service.
     * @throws IpcgSubProvException
     *             Thrown if there are problems connecting to IPCG CORBA service.
     */
    public int deleteSub(final String msisdn) throws IpcgSubProvException;

    /**
     * Adds package plan.
     *
     * @param msisdn
     *            MSISDN of the subscriber to be updated.
     * @param packagePlanId
     *            New package plan ID.
     * @param socName
     *            SOC name.
     * @return Result code from IPCG CORBA service.
     */
    public int addPackagePlan(final String msisdn, final int packagePlanId, final String socName);


    /**
     * Removes a subscriber from a package plan.
     *
     * @param msisdn
     *            MSISDN of the subscriber to be updated.
     * @param packagePlanId
     *            New package plan ID.
     * @return Result code from IPCG CORBA service.
     */
    public int deletePackagePlan(final String msisdn, final int packagePlanId);


    /**
     * Updates the MSISDN of a subscriber.
     *
     * @param oldMsisdn
     *            Old MSISDN.
     * @param newMsisdn
     *            New MSISDN.
     * @return Result code from IPCG CORBA service.
     */
    public int updateMsisdn(final String oldMsisdn, final String newMsisdn);

    /**
     * Determines whether a MSISDN exists.
     *
     * @param msisdn
     *            MSISDN to be looked up.
     * @return Result code from IPCG CORBA service.
     */
    public int isMsisdnExist(final String msisdn);
}
