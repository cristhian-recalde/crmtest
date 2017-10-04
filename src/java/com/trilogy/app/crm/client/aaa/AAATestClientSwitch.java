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

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;


/**
 * Provides a proxy that switches between its given delegate and an
 * AAATestClient as necessary.  To enable the test client, create a valid
 * license for "DEV - Test AAA Client".
 *
 * @author gary.anderson@redknee.com
 */
public
class AAATestClientSwitch
    extends AAAClientProxy
{
    /**
     * Key used to determine whether or not license is given to use the test
     * client.
     */
    public static final String LICENSE_KEY = "DEV - Test AAA Client";


    /**
     * Creates a new proxy for the given delegate.
     *
     * @param delegate The AAAClient to which this proxy delegates.
     */
    public AAATestClientSwitch(final AAAClient delegate)
    {
        super(delegate);
    }


    /**
     * Returns a testing client if testing is enabled and returns the
     * preconfigured delegate otherwise.  A testing client can be placed in the
     * context with the AAATestClient class as the key.  If such a client is not
     * found, an instance of AAATestClient is returned.
     *
     * {@inheritDoc}
     */
    public AAAClient getDelegate(final Context context)
    {
        final LicenseMgr manager = (LicenseMgr)context.get(LicenseMgr.class);

        final AAAClient client;

        if (manager.isLicensed(context, LICENSE_KEY))
        {
            client = (AAAClient)context.get(AAATestClient.class, TEST_CLIENT);
        }
        else
        {
            client = super.getDelegate(context);
        }

        return client;
    }


    /**
     * A Testing client to be used when in testing mode.
     */
    private static final AAAClient TEST_CLIENT = new AAATestClient();


} // class
