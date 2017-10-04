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

import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.util.corba.ConnectionListener;

import com.trilogy.service.rating.RatePlanInfo;


/**
 * Provides an interface for communicating with ECP to retrieve the list of
 * available rate plans.
 *
 * @author gary.anderson@redknee.com
 */
public
interface EcpRatePlanClient
    extends ContextAware, RemoteServiceStatus, ConnectionListener
{
    /**
     * Get a list of available rate plans for the given service provider.
     *
     * @param spid The service provider identifier.
     *
     * @return The list of available rate plans.
     *
     * @exception EcpRatePlanClientException
     */
    RatePlanInfo[] getRatePlans(int spid) throws EcpRatePlanClientException;
    
}
