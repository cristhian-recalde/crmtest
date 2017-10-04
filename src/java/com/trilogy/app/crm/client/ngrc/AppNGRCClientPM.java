/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client.ngrc;


import com.trilogy.app.crm.client.ClientException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * 
 * @author asim.mahmood@redknee.com
 * @since 8.5
 * 
 */
public class AppNGRCClientPM extends AppNGRCClientProxy
{

    public AppNGRCClientPM(Context ctx, AppNGRCClient delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public void addOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, String deviceType, boolean confirmationRequired)
            throws ClientException
    {
        PMLogMsg pm = new PMLogMsg(AppNGRCClient.class.getName(), "addOptIn", msisdn);
        super.addOptIn(ctx, msisdn, spid, baseRatePlan, baseOptIn, roamingRatePlan, roamingOptIn, deviceType,
                confirmationRequired);
        pm.log(ctx);
    }

    @Override
    public void deleteOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, Integer delayedOptOut) throws ClientException
    {
        PMLogMsg pm = new PMLogMsg(AppNGRCClient.class.getName(), "deleteOptIn", msisdn);
        super.deleteOptIn(ctx, msisdn, spid, baseRatePlan, baseOptIn, roamingRatePlan, roamingOptIn, delayedOptOut);
        pm.log(ctx);
    }

    @Override
    public void updateImsi(Context ctx, String oldIMSI, String newIMSI) throws ClientException
    {
        PMLogMsg pm = new PMLogMsg(AppNGRCClient.class.getName(), "updateImsi", oldIMSI);
        super.updateImsi(ctx, oldIMSI, newIMSI);
        pm.log(ctx);
    }
    
}
