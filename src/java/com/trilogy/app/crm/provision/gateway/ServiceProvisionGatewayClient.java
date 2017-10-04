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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;



/**
 * 
 *
 * @author victor.stratan@redknee.com
 * @since 8.5
 */
public interface ServiceProvisionGatewayClient
{
    /**
     * Provision services on Service Provision Gateway. This will add and/or remove services.
     */
    void provision(Context ctx, Set<Long> removeList, 
            Set<Long> addList, Map<Integer, String> values,
            Subscriber sub) throws ServiceProvisionGatewayException;

    /**
     * Update services on Service Provision Gateway. This will NOT add or remove services.
     * A service may be removed and added as the only possible update call flow.
     * @param values TODO
     */
    void update(Context ctx, Set<Long> currentList, Map<Integer, 
            String> keyValues, Map<Integer, String> values,
            Subscriber sub) throws ServiceProvisionGatewayException;

    /**
     * Execute special command on Service Provision Gateway related to a service.
     * @param values TODO
     */
    void execute(Context ctx, int command, long serviceID, 
            Map<Integer, String> values, Subscriber sub) throws ServiceProvisionGatewayException;

}
