package com.trilogy.app.crm.provision.corba.api.ecareservices;

import com.trilogy.app.crm.Common;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;

public abstract class EcareServices implements Common
{

    public EcareServices()
    {
        super();
    }

    
    public boolean attemptRate(final Context ctx, final String licenseKey)
    {
        LicenseMgr lMgr = (LicenseMgr)ctx.get(LicenseMgr.class);

        return lMgr.attemptRate(ctx, licenseKey);
    }
    
    public boolean isLicensed(final Context ctx, final String licenseKey)
    {
        LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
        
        return lMgr.isLicensed(ctx, licenseKey);
    }
}
