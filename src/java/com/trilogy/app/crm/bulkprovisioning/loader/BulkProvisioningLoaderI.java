package com.trilogy.app.crm.bulkprovisioning.loader;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;


public interface BulkProvisioningLoaderI
{
    public void provision(Context ctx, Object obj, Object source);
    
    public void update(Context ctx, Object obj, Object source);
    
    public void initialize(Context ctx, String[] parameters) throws HomeException;
    
}
