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
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * Debug proxy class for AppNGRCClient. Prints incoming method parameters.
 * 
 * @author asim.mahmood@redknee.com
 * @since 8.5
 * 
 */
public class AppNGRCClientDebug extends AppNGRCClientProxy
{

    public AppNGRCClientDebug(Context ctx, AppNGRCClient delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public void addOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, String deviceType, boolean confirmationRequired)
            throws ClientException
    {
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            final String msg = String.format(
                    "addOptIn(msisdn=%s, spid=%s, baseRatePlan=%s, baseOptIn=%s, roamingRatePlan=%s, roamingOptIn=%s, deviceType=%s, confirmationRequired=%s)", 
                    String.valueOf(msisdn), 
                    String.valueOf(spid),
                    String.valueOf(baseRatePlan), 
                    String.valueOf(baseOptIn),
                    String.valueOf(roamingRatePlan),
                    String.valueOf(roamingOptIn),
                    String.valueOf(deviceType), 
                    String.valueOf(confirmationRequired)
                    );
                    LogSupport.debug(ctx, this, msg);
        }

        super.addOptIn(ctx, msisdn, spid, baseRatePlan, baseOptIn, roamingRatePlan, roamingOptIn, deviceType,
                confirmationRequired);
        
    }

    @Override
    public void deleteOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, Integer delayedOptOut) throws ClientException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            final String msg = String.format(
                    "deleteOptIn(msisdn=%s, spid=%s, baseRatePlan=%s, baseOptIn=%s, roamingRatePlan=%s, roamingOptIn=%s, delayedOptOut=%s)", 
                    String.valueOf(msisdn), 
                    String.valueOf(spid),
                    String.valueOf(baseRatePlan), 
                    String.valueOf(baseOptIn),
                    String.valueOf(roamingRatePlan),
                    String.valueOf(roamingOptIn),
                    String.valueOf(delayedOptOut)
                    );
                    LogSupport.debug(ctx, this, msg);
        }
        super.deleteOptIn(ctx, msisdn, spid, baseRatePlan, baseOptIn, roamingRatePlan, roamingOptIn, delayedOptOut);
    }
    
    @Override
    public void updateImsi(Context ctx, String oldIMSI, String newIMSI) throws ClientException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            final String msg = String.format(
                    "updateImsi(oldIMSI=%s, newIMSI=%s)", 
                    String.valueOf(oldIMSI), 
                    String.valueOf(newIMSI)
                    );
            LogSupport.debug(ctx, this, msg);
        }
        super.updateImsi(ctx, oldIMSI, newIMSI);
    }

    
    
}
