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

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;


/**
 * Provides a proxy that switches between its given delegate and an
 * IpcgTestClient as necessary.  To enable the test client, create a valid
 * license for "DEV - Test IPCG Client".
 *
 * @author gary.anderson@redknee.com
 */
class IpcgTestClientSwitch
    extends IpcgClientProxy
{
    /**
     * Key used to determine whether or not license is given to use the test
     * client.
     */
    public static final String LICENSE_KEY = "DEV - Test IPCG Client";


    /**
     * Creates a new switch for the given delegate.
     *
     * @param delegate The IpcgClient to which this switch delegates if the test
     * client is not enabled.
     */
    public IpcgTestClientSwitch(final IpcgClient delegate)
    {
        super(delegate);
        testClient_ = new IpcgTestClient();
    }


    /**
     * Returns a testing client if testing is enabled and returns the
     * preconfigured delegate otherwise.
     *
     * @param context The operating context.
     *
     * @return An appropriate client.
     */
    public IpcgClient getDelegate(final Context context)
    {
        final LicenseMgr manager = (LicenseMgr)context.get(LicenseMgr.class);

        final IpcgClient client;

        if (manager.isLicensed(context, LICENSE_KEY))
        {
            client = testClient_;
        }
        else
        {
            client = super.getDelegate(context);
        }

        return client;
    }


    /**
     * The testing client to use if testing is enabled.
     */
    private final IpcgClient testClient_;

} // class

