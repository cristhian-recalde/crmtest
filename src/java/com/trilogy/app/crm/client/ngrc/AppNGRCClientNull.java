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
public class AppNGRCClientNull implements AppNGRCClient
{

    static AppNGRCClientNull instance_ = null;
    
    public static synchronized AppNGRCClientNull instance()
    {
        if (instance_ == null)
        {
            instance_ = new AppNGRCClientNull();
        }
        return instance_;
    }

    protected AppNGRCClientNull()
    {
        
    }

    public void addOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, String deviceType, boolean confirmationRequired)
            throws ClientException
    {
        return;
    }

    public void deleteOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, Integer delayedOptOut) throws ClientException
    {
        return;
    }
    
    public void updateImsi(Context ctx, String oldIMSI, String newIMSI) throws ClientException
    {
        return;
    }
    
}
