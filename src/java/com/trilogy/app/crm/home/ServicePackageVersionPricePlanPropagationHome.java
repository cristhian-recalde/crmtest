/*
 * Created on Nov 3, 2005 2:33:42 PM
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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.visitor.ServicePackageVersionPricePlanPropagationVisitor;

import java.util.Iterator;

/**
 * This home will propagate version package creations as new priceplan versions
 *
 * @author psperneac
 */
public class ServicePackageVersionPricePlanPropagationHome extends HomeProxy
{
    public ServicePackageVersionPricePlanPropagationHome(Home delegate)
    {
        super(delegate);
    }

    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        ServicePackageVersion version=(ServicePackageVersion) obj;

        // fill in the versions for the fees that doen't have it yet
        Home spHome=(Home) ctx.get(ServicePackageHome.class);
        if(version.getPackageFees()!=null)
        {
            for(Iterator i=version.getPackageFees().keySet().iterator();i.hasNext();)
            {
                Object key=i.next();
                ServicePackageFee fee=(ServicePackageFee) version.getPackageFees().get(key);

                if(fee.getPackageVersionId()==ServicePackageFee.DEFAULT_PACKAGEVERSIONID)
                {
                    ServicePackage sp=(ServicePackage) spHome.find(ctx,
                            new EQ(ServicePackageXInfo.ID, Integer.valueOf(fee.getPackageId())));

                    if(sp!=null)
                    {
                        fee.setPackageVersionId(sp.getCurrentVersion());
                    }
                }
            }
        }

        Object ret=super.create(ctx, obj);

        Home ppHome=(Home) ctx.get(PricePlanHome.class);

        ppHome.forEach(ctx,new ServicePackageVersionPricePlanPropagationVisitor(version));

        return ret;
    }
}
