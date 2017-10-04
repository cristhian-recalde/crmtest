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
package com.trilogy.app.crm.client.dcrm;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.dynamics.crm.DCRMServiceSupport;
import com.trilogy.dynamics.crm.DynamicsCrmConfiguration;


/**
 * Provides support methods for the DCRM support in AppCrm.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmSupport
{
    /**
     * License key for Microsoft Dynamics CRM Integration.
     */
    public static final String DCRM_INTEGRATION_LICENSE = "DCRM_INTEGRATION";


    /**
     * Calls into the InterfaceDynamicsCrm project for its basic initialization,
     * then sets-up the AppCrm specific initialization.
     *
     * @param context The application context.
     */
    public static void install(final Context context)
    {
        DCRMServiceSupport.install(context);
        final DcrmChangePropagation propagation = new DcrmChangePropagation(context);
        propagation.addDefaultSyncs(context);
        context.put(DcrmChangePropagation.class, propagation);
    }


    /**
     * Determines whether or not the integration with DCRM is enabled.
     *
     * @param context The operating context.
     * @return True if the integration with DCRM is enabled; false otherwise.
     */
    public static boolean isEnabled(final Context context)
    {
        final DynamicsCrmConfiguration config = DCRMServiceSupport.getConfig(context);
        final boolean isEnabled = isLicensed(context) && config != null && config.isEnabled();
        return isEnabled;
    }


    /**
     * Provides a simple proxy to the system's
     * {@link DcrmChangePropagation#getSync} method.
     *
     * @param context The operating context.
     * @param key The key used when the support was originally added.
     * @return The synchronization support that was added with the given key.
     */
    public static DcrmSync getSync(final Context context, final String key)
    {
        final DcrmChangePropagation propagation = (DcrmChangePropagation)context.get(DcrmChangePropagation.class);
        return propagation.getSync(key);
    }


    /**
     * Determines whether or not the integration with DCRM is licensed.
     *
     * @param context The operating context.
     * @return True if the integration with DCRM is licensed; false otherwise.
     */
    private static boolean isLicensed(final Context context)
    {
        final LicenseMgr licenses = (LicenseMgr)context.get(LicenseMgr.class);
        final boolean isLicensed = licenses.isLicensed(context, DCRM_INTEGRATION_LICENSE);
        return isLicensed;
    }
}
