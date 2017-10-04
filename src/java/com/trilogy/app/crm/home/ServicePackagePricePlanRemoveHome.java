/*
 * Created on Dec 15, 2005 11:27:49 AM
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.visitor.PricePlanVersionPackageSearchVisitor;

/**
 * @author psperneac
 */
public class ServicePackagePricePlanRemoveHome extends HomeProxy
{
    public ServicePackagePricePlanRemoveHome(Home delegate)
    {
        super(delegate);
    }

    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        ServicePackage pack=(ServicePackage) obj;
        Home home=(Home) ctx.get(PricePlanVersionHome.class);

        PricePlanVersionPackageSearchVisitor visitor=new PricePlanVersionPackageSearchVisitor(pack.getId());
        visitor=(PricePlanVersionPackageSearchVisitor) home.forEach(ctx,visitor);

        if(visitor.isFound())
        {
            throw new HomeException("This package template is in use in a price plan version " +
                    "and cannot be deleted");
        }

        super.remove(ctx, obj);
    }
}
