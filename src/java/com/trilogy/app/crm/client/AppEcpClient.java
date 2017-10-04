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

package com.trilogy.app.crm.client;

import com.trilogy.app.osa.ecp.provision.ServiceParameter;
import com.trilogy.app.osa.ecp.provision.SubsProfile;


/**
 * ECP provisioning client interface.
 *
 * @author cindy.wong@redknee.com
 * 
 * Refactored to make it a pure wrapper interface
 * @author rchen
 * @since June 25, 2009 
 */
public interface AppEcpClient
{
    /**
     * Active state for prepaid.
     */
    short ACTIVE = com.redknee.app.osa.ecp.provision.PREPAIDSTATE_ACTIVE.value;
    /**
     * Inactive state for prepaid.
     */
    short INACTIVE = com.redknee.app.osa.ecp.provision.PREPAIDSTATE_INACTIVE.value;
    /**
     * Suspended state for prepaid.
     */
    short SUSPENDED = com.redknee.app.osa.ecp.provision.PREPAIDSTATE_SUSPENDED.value;
    /**
     * Deactivated state for prepaid.
     */
    short DEACTIVATED = com.redknee.app.osa.ecp.provision.PREPAIDSTATE_DEACTIVATED.value;
    /**
     * Dunned warning state.
     */
    short DUNNED_WARNING = com.redknee.app.osa.ecp.provision.DUNNED_WARNING.value;
    /**
     * Dunned suspended state.
     */
    short DUNNED_SUSPENDED = com.redknee.app.osa.ecp.provision.DUNNED_SUSPENDED.value;
    /**
     * Barred state.
     */
    short BARRED = com.redknee.app.osa.ecp.provision.BARRED.value;
    /**
     * Expired state.
     */
    short EXPIRED = com.redknee.app.osa.ecp.provision.EXPIRED.value;
    /**
     * Available state.
     */
    short AVAILABLE = com.redknee.app.osa.ecp.provision.AVAILABLE.value;
    /**
     * Name of the CORBA client property.
     */
    String SERVICE_NAME = "AppEcpClient";


    /**
     * Adds a subscriber to ECP.
     *
     * @param msisdn
     *            MSISDN of the subscriber.
     * @param spid
     *            Service provider ID.
     * @param imsi
     *            IMSI of the subscriber.
     * @param currency
     *            Default currency of the subscriber.
     * @param ratePlan
     *            ECP rate plan ID.
     * @param expiry
     *            Expiry date of the subscriber.
     * @param classOfService
     *            Class of service ID of the subscriber.
     * @param state
     *            Subscriber state.
     * @param pin
     *            PIN of the subscriber.
     * @param language
     *            Language preference of the subscriber.
     * @param timeRegionId
     *            Time zone ID.
     * @param groupAccount
     *            Group account ID.
     * @param billingNumber
     *            The billing number to charge against.
     * @return Result code of the subscriber creation.
     */
    int addSubscriber(final String msisdn, final int spid, final String imsi, final String currency,
        final int ratePlan, final long expiry, final int classOfService, final int state, final String pin,
        final int language, final String timeRegionId, final String groupAccount, final String billingNumber);


    /**
     * Adds a subscriber to ECP.
     *
     * @param msisdn
     *            MSISDN of the subscriber.
     * @param spid
     *            Service provider ID.
     * @param imsi
     *            IMSI of the subscriber.
     * @param currency
     *            Default currency of the subscriber.
     * @param ratePlan
     *            ECP rate plan ID.
     * @param expiry
     *            Expiry date of the subscriber.
     * @param classOfService
     *            Class of service ID of the subscriber.
     * @param state
     *            Subscriber state.
     * @param pin
     *            PIN of the subscriber.
     * @param language
     *            Language preference of the subscriber.
     * @param timeRegionId
     *            Time zone ID.
     * @param groupAccount
     *            Group account ID.
     * @return Result code of the subscriber creation.
     * @deprecated Use
     *             {@link #addSubscriber(String, int, String, String, int, long, int, int, String, int, String,
     *             String, String)} instead.
     */
    @Deprecated
    int addSubscriber(final String msisdn, final int spid, final String imsi, final String currency,
        final int ratePlan, final long expiry, final int classOfService, final int state, final String pin,
        final int language, final String timeRegionId, final String groupAccount);


    /**
     * Adds a subscriber to ECP.
     *
     * @param profile
     *            Subscriber profile to be added.
     * @return Result code of the subscriber creation.
     */
    int addSubscriber(final SubsProfile profile);


    /**
     * Updates a subscriber in ECP.
     *
     * @param msisdn
     *            MSISDN of the subscriber.
     * @param spid
     *            Service provider ID.
     * @param imsi
     *            IMSI of the subscriber.
     * @param currency
     *            Default currency of the subscriber.
     * @param ratePlan
     *            ECP rate plan ID.
     * @param expiry
     *            Expiry date of the subscriber.
     * @param classOfService
     *            Class of service ID of the subscriber.
     * @param state
     *            Subscriber state.
     * @param pin
     *            PIN of the subscriber.
     * @param language
     *            Language preference of the subscriber.
     * @param timeRegionId
     *            Time zone ID.
     * @param groupAccount
     *            Group account ID.
     * @param billingNumber
     *            The MSISDN to charge against.
     * @return Result code of the subscriber update.
     */
    int updateSubscriber(final String msisdn, final int spid, final String imsi, final String currency,
        final int ratePlan, final long expiry, final int classOfService, final int state, final String pin,
        final int language, final String timeRegionId, final String groupAccount, final String billingNumber);


    /**
     * Updates a subscriber in ECP.
     *
     * @param msisdn
     *            MSISDN of the subscriber.
     * @param spid
     *            Service provider ID.
     * @param imsi
     *            IMSI of the subscriber.
     * @param currency
     *            Default currency of the subscriber.
     * @param ratePlan
     *            ECP rate plan ID.
     * @param expiry
     *            Expiry date of the subscriber.
     * @param classOfService
     *            Class of service ID of the subscriber.
     * @param state
     *            Subscriber state.
     * @param pin
     *            PIN of the subscriber.
     * @param language
     *            Language preference of the subscriber.
     * @param timeRegionId
     *            Time zone ID.
     * @param groupAccount
     *            Group account ID.
     * @return Result code of the subscriber update.
     * @deprecated Use
     *             {@link #updateSubscriber(String, int, String, String, int, long, int, int, String, int, String,
     *             String, String)} instead.
     */
    @Deprecated
    int updateSubscriber(final String msisdn, final int spid, final String imsi, final String currency,
        final int ratePlan, final long expiry, final int classOfService, final int state, final String pin,
        final int language, final String timeRegionId, final String groupAccount);


    /**
     * Update a subscriber in ECP.
     *
     * @param profile
     *            Subscriber profile to be updated.
     * @return Result code of the subscriber update.
     */
    int updateSubscriber(final SubsProfile profile);


    /**
     * Adds an additional MSISDN to a subscriber. The subscriber must already exist when
     * trying to add.
     *
     * @param mainMsisdn
     *            Main MSISDN of the subscriber.
     * @param aMsisdn
     *            Additional MSISDN to be added.
     * @return Result code of the action.
     */
    short addAMsisdn(final String mainMsisdn, final String aMsisdn);


    /**
     * Updates the subscriber's state in ECP.
     *
     * @param msisdn
     *            The subscriber's mobile voice number.
     * @param state
     *            The new state of the subscriber.
     * @return Result code of the action.
     */
    int updateSubscriberState(final String msisdn, final int state);


    /**
     * Updates the group account of the subscriber.
     *
     * @param msisdn
     *            Subscriber MSISDN.
     * @param groupAccount
     *            New group account ID.
     * @return Result code of the update.
     */
    int updateGroupAccount(final String msisdn, final String groupAccount);


    /**
     * Updates the rate plan of the subscriber.
     *
     * @param msisdn
     *            Subscriber MSISDN.
     * @param ratePlan
     *            New rate plan ID.
     * @return Result code of the action.
     */
    int updateRatePlan(final String msisdn, final int ratePlan);


    /**
     * Updates the subscriber profile in ECP with the new IMSI.
     *
     * @param msisdn
     *            The subscriber's mobile number.
     * @param imsi
     *            The subscriber's IMSI.
     * @return The ECP result code.
     */
    int updateImsi(final String msisdn, final String imsi);


    /**
     * Updates subscriber's profile in ECP with the new class of service.
     *
     * @param msisdn
     *            The subscriber's mobile number.
     * @param classOfService
     *            The subscriber's class of service.
     * @return The ECP result code.
     */
    int updateClassOfService(final String msisdn, final int classOfService);


    /**
     * Sets whether Friends and Family functionality in ECP is enebled.
     *
     * @param msisdn
     *            The subscriber's MSISDN.
     * @param enabled
     *            True if Friends and Family functionality should be enabled; false
     *            otherwise.
     * @return The ECP result code.
     * @exception IllegalStateException
     *                Thrown if no service connection can be established to ECP.
     */
    short setFriendsAndFamilyEnabled(final String msisdn, final boolean enabled);


    /**
     * Deletes a subscriber from ECP.
     *
     * @param msisdn
     *            Subscriber MSISDN.
     * @return Result code of the action.
     */
    int deleteSubscriber(final String msisdn);


    /**
     * Retrieves the subscriber profile from ECP.
     *
     * @param msisdn
     *            Subscriber MSISDN.
     * @return Subscriber profile.
     */
    SubsProfile getSubsProfile(final String msisdn);


    /**
     * Enables the homezone flag in ecp for the subscriber.
     *
     * @param msisdn
     *            Subscriber MSISDN.
     * @return Result code of the action.
     */
    short enableHomezone(final String msisdn);


    /**
     * Disables the homezone flag in ecp for the subscriber.
     *
     * @param msisdn
     *            Subscriber MSISDN.
     * @return Result code of the action.
     */
    short disableHomezone(final String msisdn);


    /**
     * Updates a particular service in ECP. (Value set to true or false)
     *
     * @param msisdn
     *            The subscriber's MSISDN.
     * @param paramSet
     *            The set of ServiceParameter object, The object is a key-value
     *            pair(serviceID, true/false)
     * @return The ECP result code.
     * @exception IllegalStateException
     *                Thrown if no service connection can be established to ECP.
     */
    short updateServices(final String msisdn, final ServiceParameter[] paramSet);


    /**
     * Updates a MSISDN in ECP.
     *
     * @param oldMsisdn
     *            The existing MSISDN.
     * @param newMsisdn
     *            The new MSISDN to change into.
     * @return The ECP result code.
     * @exception IllegalStateException
     *                Thrown if no service connection can be established to ECP.
     */
    short changeMsisdn(final String oldMsisdn, final String newMsisdn);


    /**
     * Updates an AMSISDN in ECP.
     *
     * @param ownerMsisdn
     *            The main MSISDN which owns the AMSISDN.
     * @param oldAMsisdn
     *            The existing AMSISDN.
     * @param newAMsisdn
     *            The new AMSISDN to change into.
     * @return The ECP result code.
     * @exception IllegalStateException
     *                Thrown if no service connection can be established to ECP.
     */
    short changeAMsisdn(final String ownerMsisdn, final String oldAMsisdn, final String newAMsisdn);
}
