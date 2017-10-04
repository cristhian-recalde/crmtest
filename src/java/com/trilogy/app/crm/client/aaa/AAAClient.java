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
package com.trilogy.app.crm.client.aaa;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TDMAPackage;


/**
 * Provides an interface for provisioning AAA (authentication, authorisation,
 * and accounting) services.
 *
 * @author gary.anderson@redknee.com
 */
public
interface AAAClient
{
    /**
     * Creates a subscriber profile.
     *
     * @param context The operating context
     * @param subscriber The subscriber for which to create a AAA profile.
     *
     * @exception AAAClientException Thrown if there is an error while
     * provisioning the profile.
     */
    void createProfile(Context context, Subscriber subscriber)
        throws AAAClientException;


    /**
     * Deletes a subscriber profile.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to delete a AAA profile.
     *
     * @exception AAAClientException Thrown if there is an error while deleting
     * the profile.
     */
    void deleteProfile(Context context, Subscriber subscriber)
        throws AAAClientException;


    /**
     * Indictes whether or not the profile of the givine subscriber is enabled.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to check the profile.
     * @return True if the subscriber's profile is enabled; false otherwise.
     *
     * @exception AAAClientException Thrown if there is an error while checking
     * the profile status.
     */
    boolean isProfileEnabled(Context context, Subscriber subscriber)
        throws AAAClientException;


    /**
     * Indictes whether or not the profile of the givine subscriber is enabled.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to check the profile.
     * @param enabled True if the subscriber's profile is enabled; false otherwise.
     *
     * @exception AAAClientException Thrown if there is an error while setting
     * the profile status.
     */
    void setProfileEnabled(Context context, Subscriber subscriber, boolean enabled)
        throws AAAClientException;


    /**
     * Updates the subscriber's profile.
     *
     * @param context The operating context.
     * @param oldSubscriber The subscriber with old information.
     * @param newSubscriber The subscriber with new information.
     *
     * @exception AAAClientException Thrown if there is an error while updating
     * the profile.
     */
    void updateProfile(Context context, Subscriber oldSubscriber, Subscriber newSubscriber)
        throws AAAClientException;


    /**
     * Updates the package's profile.
     *
     * @param context The operating context.
     * @param oldPackage The package with old information.
     * @param newPackage The package with new information.
     *
     * @exception AAAClientException Thrown if there is an error while updating
     * the profile.
     */
    void updateProfile(Context context, TDMAPackage oldPackage, TDMAPackage newPackage)
        throws AAAClientException;

} // interface
