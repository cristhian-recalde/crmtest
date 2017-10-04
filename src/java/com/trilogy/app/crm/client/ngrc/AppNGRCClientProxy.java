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


/**
 * Simple proxy class for AppNGRCClient interface
 * 
 * @author asim.mahmood@redknee.com
 * @since 8.5
 * 
 */
public class AppNGRCClientProxy implements AppNGRCClient
{

    private AppNGRCClient delegate_;


    public AppNGRCClientProxy(Context ctx, AppNGRCClient delegate)
    {
        this.delegate_ = delegate;
    }


    @Override
    public void addOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, String deviceType, boolean confirmationRequired)
            throws ClientException
    {
        getDelegate().addOptIn(ctx, msisdn, spid, baseRatePlan, baseOptIn, roamingRatePlan, roamingOptIn, deviceType, confirmationRequired);
    }


    @Override
    public void deleteOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
            int roamingRatePlan, int roamingOptIn, Integer delayedOptOut) throws ClientException
    {
        getDelegate().deleteOptIn(ctx, msisdn, spid, baseRatePlan, baseOptIn, roamingRatePlan, roamingOptIn, delayedOptOut);
    }
    
    
    @Override
    public void updateImsi(Context ctx, String oldIMSI, String newIMSI) throws ClientException
    {
        getDelegate().updateImsi(ctx, oldIMSI, newIMSI);
    }
    
    public AppNGRCClient getDelegate()
    {
        return this.delegate_;
    }
    
    public void setDelegate(AppNGRCClient delegate)
    {
        this.delegate_ = delegate;
    }
}
