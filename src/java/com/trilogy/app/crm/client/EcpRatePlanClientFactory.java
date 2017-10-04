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

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;


/**
 * Provides access to the EcpRatePlanClient.  This factory will return the test
 * object if the "DEV - Test CORBA Client" license is valid, otherwise the
 * actual CORBA client will be used.
 *
 * @author gary.anderson@redknee.com
 */
public
class EcpRatePlanClientFactory
    implements ContextFactory
{
    /**
     * Key used to determine whether or not license is given to use the test
     * client.
     */
    public static final String LICENSE_KEY = "DEV - Test CORBA Client";


    /**
     * Creates the client factory.
     *
     * @param context The operating context.
     */
    public EcpRatePlanClientFactory(final Context context)
    {
        corbaClient_ = new EcpRatePlanCorbaClient(context);
        testClient_ = new EcpRatePlanTestClient();
    }


    /**
     * {@inheritDocs}
     */
    public Object create(final Context context)
    {
        final LicenseMgr manager = (LicenseMgr)context.get(LicenseMgr.class);

        final EcpRatePlanClient client;

        if (manager.isLicensed(context, LICENSE_KEY))
        {
            client = testClient_;
        }
        else
        {
            client = corbaClient_;
        }

        return client;
    }


    /**
     * The actual CORBA client.
     */
    private final EcpRatePlanCorbaClient corbaClient_;

    /**
     * The test client,
     */
    private final EcpRatePlanTestClient testClient_;

}
