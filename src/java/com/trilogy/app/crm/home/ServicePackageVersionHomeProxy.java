/*
 * Created on Nov 11, 2005
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

import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author psperneac
 */
public class ServicePackageVersionHomeProxy extends HomeProxy
{
    public ServicePackageVersionHomeProxy(Home delegate)
    {
        super(delegate);
    }

    public void addProxy(Context ctx, HomeProxy proxy)
    {
        super.addProxy(ctx, proxy);
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        ServicePackageVersion proposedVersion=(ServicePackageVersion) obj;
        
        Home home=(Home) ctx.get(ServicePackageHome.class);
        ServicePackage pack=(ServicePackage) home.find(ctx, Integer.valueOf(proposedVersion.getId()));
        if(pack!=null)
        {
            int version = pack.getNextVersion();

            // because this is the home of the templates, we set the current version so we can track them.
            pack.setCurrentVersion(version);
            pack.setNextVersion(version + 1);
            home.store(ctx,pack);

            // Explicitly override whatever version might have been set -- the
            // proposed version could not know for sure what it is to be.
            proposedVersion.setVersion(version);
        }
        else
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,"Cannot find ServicePackage for "+proposedVersion.getId(),null).log(ctx);
            }
        }

        return super.create(ctx, obj);
    }

    /**
     * Returns if this is a newly created service package version.
     */
    private boolean isNewVersion(ServicePackageVersion pp)
    {
        return (pp.getVersion() == 0);
    }
}
