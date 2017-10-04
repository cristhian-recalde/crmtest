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
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.PackageType;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.bean.core.TDMAPackage;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * 
 * 
 * @author VijayG
 * @since 9.6
 */
public class PackageStateTypeCheckHome extends HomeProxy
{

    public PackageStateTypeCheckHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }


    public PackageStateTypeCheckHome(Context ctx)
    {
        super(ctx);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        PackageType packageType = (PackageType) obj;
        Home home = (Home) ctx.get(TDMAPackageHome.class);
        TDMAPackage object = null;
        long availablePackages = 0l;
        try
        {
            final HomeSupport homeSupport = HomeSupportHelper.get(ctx);
            availablePackages = homeSupport.getBeanCount(ctx, home,
                    new EQ(TDMAPackageXInfo.PACKAGE_TYPE, packageType.getId()));
        }
        catch (HomeException e)
        {
            final String message = "Unable to determine package of package type";
            new MinorLogMsg(this, String.format(message, packageType.getId()), e).log(ctx);
        }
        if (availablePackages > 0)
        {
            throw new HomeException("Cannot delete, this package type is being used by one of TDMA/CDMA Package");
        }
        super.remove(ctx, obj);
    }
}
