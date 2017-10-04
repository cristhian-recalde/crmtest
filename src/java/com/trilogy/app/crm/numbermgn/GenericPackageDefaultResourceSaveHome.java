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
package com.trilogy.app.crm.numbermgn;

import com.trilogy.app.crm.resource.ResourceDeviceDefaultPackage;
import com.trilogy.app.crm.resource.ResourceDeviceDefaultPackageHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Create and update default package-resource link bean.
 *
 * @author simar.singh@redknee.com
 */
public class GenericPackageDefaultResourceSaveHome extends HomeProxy
{
    /**
     * For serialization. Although Serializing HomeProxies is a bad idea. 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor accepts delegate Home parameter.
     *
     * @param home delegate Home
     */
    public GenericPackageDefaultResourceSaveHome(Home home)
    {
        super(home);
    }

    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final GenericPackage request = (GenericPackage) obj;
        final GenericPackage result = (GenericPackage) super.create(ctx, obj);
        final String defaultResourceId = result.getDefaultResourceID();
        if (null != defaultResourceId && defaultResourceId.length() > 0)
        {
            final ResourceDeviceDefaultPackage link = new ResourceDeviceDefaultPackage();
            link.setResourceID(result.getDefaultResourceID());
            link.setPackID(request.getPackId());
            final Home linkHome = (Home) ctx.get(ResourceDeviceDefaultPackageHome.class);
            linkHome.remove(ctx, link);
            linkHome.create(ctx, link);
        }
        return result;
    }
}
