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

import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.crm.client.ipcg.CDMASubscriberProv;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.product.s5600.ipcg.provisioning.SubscriberProv;
import com.trilogy.util.snippet.log.Logger;


/**
 * IPCG client.
 *
 * @author daniel.zhang@redknee.com
 */
public class CDMAProductS5600IpcgCorbaClient extends ProductS5600IpcgCorbaClient<CDMASubscriberProv>
{
    private static final String SERVICE_NAME = "ProductS5600IpcgCorbaClient (CDMA)";
    private static final String SERVICE_DESCRIPTION = "CORBA client for IPCG Susbscriber Provisioning Services (CDMA)";

    private static final String PM_MODULE = CDMAProductS5600IpcgCorbaClient.class.getName();

    /**
     * Create a new instance of <code>ProductS5600IpcgClient</code>.
     *
     * @param ctx
     *            The operating context.
     * @param propertiesKey
     *            The CORBA client properties key.
     */
    public CDMAProductS5600IpcgCorbaClient(final Context ctx)
    {
        super(ctx, SERVICE_NAME, SERVICE_DESCRIPTION, CDMASubscriberProv.class);
    }

    @Override
    protected String getPMModule()
    {
        return PM_MODULE;
    }
    
    @SuppressWarnings("unchecked")
    protected SubscriberProv getClient(final Context ctx) throws RemoteServiceException
    {
        final Object client = ctx.get(CDMASubscriberProv.class);

        if (client == null || !SubscriberProv.class.isInstance(client))
        {
            throw new RemoteServiceException(FAILED, "Failure: unable to get the client instance of remote service.");
        }

        return (SubscriberProv) client;
    }


    // this method is semantically SAME with getClient(), but provided to support the old
    // CORBA clients that always validate service references by checking null.
    @SuppressWarnings("unchecked")
    protected SubscriberProv getService()
    {
        final Object client = getContext().get(CDMASubscriberProv.class);

        if (client == null || !SubscriberProv.class.isInstance(client))
        {
            Logger.major(getContext(), this, "Failure: unable to get the client instance of remote service.");
            return null;
        }

        return (SubscriberProv) client;
    }

    
}
