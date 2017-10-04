/*
 * LicenseHome.java
 * 
 * Author : danny.ng@redknee.com Date : 2006 April 12
 * 
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.LoggingHome;
import com.trilogy.framework.xhome.home.NullHome;

/**
 * Adds licensing behaviour on a home.
 * <BR>
 * It bypasses the delegate if the system does not have a license.
 * 
 * Code graciously copied from Gary's suggestion.
 * 
 * @author gary.anderson@redknee.com
 * @author danny.ng@redknee.com
 * @since  2006 April 12
 *
 */
public class LicenseHome extends HomeProxy
{
    /**
     * Generated UID
     */
    private static final long serialVersionUID = 1360509354947210430L;


    /**
     * @param ctx
     * @param delegate Home to wrap license behaviour
     * @param licenseKey
     */
    public LicenseHome(final Context ctx, final String licenseKey, final Home delegate)
    {
        super(ctx, delegate);
        licenseKey_ = licenseKey;
    }
    
    
    /**
     * Overrides the getDelegate method so that it delegates 
     * to provided home if the system has the appropriate license,
     * otherwise, it delegates to the provided home's delegate
     */
    @Override
    public Home getDelegate(final Context ctx)
    {
        Home delegate = super.getDelegate(ctx);
        if (!isLicensed(ctx))
        {
            if (delegate instanceof HomeProxy)
            {
                // The delegate is disabled, so skip it
                delegate = ((HomeProxy) delegate).getDelegate();
            }
            else
            {
                // If the data layer is licensed, then return a logging home as a
                // placeholder so we can see why calls to this home are failing.
                delegate = NullHome.instance();
                delegate = new LoggingHome(ctx, "Method Not Supported (License check fail: '" + getLicense() + "')", delegate);
            }
        } 
        return delegate;
    }
    

    /**
     * Checks if the system has the correct license
     * 
     * @param ctx
     * @return True if CRM has an enabled license to use PIN Manager, false otherwise
     */
    private boolean isLicensed(final Context ctx)
    {
        final LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
        return lMgr.isLicensed(ctx, getLicense());
    }
    
    
    public void setLicense(final String licenseKey)
    {
        licenseKey_ = licenseKey;
    }
    
    
    public String getLicense()
    {
        return licenseKey_;
    }
    
    
    /**
     * The license key that we check to determine if we bypass or use the passed home
     */
    private String licenseKey_;
}
