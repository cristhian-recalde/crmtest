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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.product.s5600.ipcg.provisioning.SubscriberProv;


/**
 * IPCG client.
 *
 * @author daniel.zhang@redknee.com
 */
public class GSMProductS5600IpcgCorbaClient extends ProductS5600IpcgCorbaClient<SubscriberProv>
{
    private static final String SERVICE_NAME = "ProductS5600IpcgCorbaClient (GSM)";
    private static final String SERVICE_DESCRIPTION = "CORBA client for IPCG Susbscriber Provisioning Services (GSM)";

    private static final String PM_MODULE = GSMProductS5600IpcgCorbaClient.class.getName();

    /**
     * Create a new instance of <code>ProductS5600IpcgClient</code>.
     *
     * @param ctx
     *            The operating context.
     * @param propertiesKey
     *            The CORBA client properties key.
     */
    public GSMProductS5600IpcgCorbaClient(final Context ctx)
    {
        super(ctx, SERVICE_NAME, SERVICE_DESCRIPTION, SubscriberProv.class);
    }

    @Override
    protected String getPMModule()
    {
        return PM_MODULE;
    }
    
    
}
