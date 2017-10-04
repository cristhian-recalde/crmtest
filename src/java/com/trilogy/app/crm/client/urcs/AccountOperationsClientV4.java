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
package com.trilogy.app.crm.client.urcs;

import java.util.TimeZone;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.client.RemoteServiceException;

/**
 * @author sbanerjee
 * @since 9.3.1
 */
public interface AccountOperationsClientV4
{
    /**
     * 
     * @param ctx
     * @param msisdn
     * @param subscriberTz Needed as input for date calculations in Subscriber Time-zone.
     * @param currency
     * @param bundleProfile
     * @param erReference
     * @return
     * @throws RemoteServiceException
     */
    BundleTopupResponse topupBundle (Context ctx, String msisdn, TimeZone subscriberTz, 
            String currency, BundleProfile bundleProfile, String erReference) throws RemoteServiceException;
    
    /**
     * 
     * @return
     */
    boolean isAlive(Context ctx);
}
