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
package com.trilogy.app.crm.provision.gateway;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.provision.service.ProvisionService;
import com.trilogy.app.crm.provision.service.ProvisionServiceHelper;
import com.trilogy.app.urcs.client.CorbaConnectionPlugin;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.proxy.corba.AbstractCorbaClient;
import com.trilogy.util.corba.CorbaClientException;

/**
 * Needed in order to reuse the URCS client capabilities.
 *
 * @author victor.stratan@redknee.com
 * @since 8.5
 */
public class ServiceProvisionGatewayPlugin extends CorbaConnectionPlugin<ProvisionService>
{
    /**
     */
    public ServiceProvisionGatewayPlugin()
    {
        super(ProvisionService.class);
    }


    @Override
    protected AbstractCorbaClient<ProvisionService> createCorbaClient(
            final Context ctx,
            final CorbaClientProperty property)
    {
        return new AbstractCorbaClient<ProvisionService>(ctx, property)
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected ProvisionService internalConnect() throws CorbaClientException
            {
                return ProvisionServiceHelper.narrow(getServant());
            }
        };
    }

}
