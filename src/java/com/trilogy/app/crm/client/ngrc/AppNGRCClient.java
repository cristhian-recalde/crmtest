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

package com.trilogy.app.crm.client.ngrc;


import com.trilogy.app.crm.client.ClientException;
import com.trilogy.framework.xhome.context.Context;

/**
 * Interface to NGRC's SOAP provision interface (SubscriberProv63)
 * 
 * @author asim.mahmood@redknee.com
 * @since 8.5
 */

public interface AppNGRCClient
{

    /**
     * Used to add a base or roaming OptIn. For each call, either baseOptIn or roamingOptIn must be set, but not both.
     * e.g. when setting a roamingOptIn, set baseRatePlan and baseOptIn to -1.
     * 
     * 
     * @param ctx
     * @param msisdn
     * @param spid
     * @param baseRatePlan
     * @param baseOptIn
     * @param roamingRatePlan
     * @param roamingOptIn
     * @param deviceType
     * @param confirmationRequired
     * @throws ClientException
     */
    public void addOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, String deviceType, boolean confirmationRequired)
            throws ClientException;

    /**
     * Used to remove a base or roaming OptIn. For each call, either baseOptIn or roamingOptIn must be set, but not both.
     * e.g. when removing a roamingOptIn, set baseRatePlan and baseOptIn to -1.
     * 
     * @param ctx
     * @param msisdn
     * @param spid
     * @param baseRatePlan
     * @param baseOptIn
     * @param roamingRatePlan
     * @param roamingOptIn
     * @param delayedOptOut
     * @throws ClientException
     */
    public void deleteOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, Integer delayedOptOut) throws ClientException;
    
    
    /**
     * Used to change the IMSI.
     * Note: This method will fail if NGRC is in CDMA mode (where IMSI is the primary key).
     * 
     * @param ctx
     * @param oldIMSI
     * @param newIMSI
     * @throws ClientException
     */
    public void updateImsi(Context ctx, String oldIMSI, String newIMSI) throws ClientException;
}