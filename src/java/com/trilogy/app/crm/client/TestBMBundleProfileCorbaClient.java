package com.trilogy.app.crm.client;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.product.bundle.manager.provision.v5_0.bundle.BundleProfileProvision;


public class TestBMBundleProfileCorbaClient extends BMBundleProfileCorbaClient
{
    public TestBMBundleProfileCorbaClient(Context ctx)
    {
        super(ctx);
    }
    
    protected void init()
    {
        new DebugLogMsg(this, "TestBMBundleProfileCorbaClient initialized", null);        
    }
    
    protected synchronized BundleProfileProvision getService()
    {
        if (bundleProfileService_ == null)
        {
            bundleProfileService_ = new TestBundleProfileProvision();            
        }
        return bundleProfileService_;
    }
    
    protected BundleProfileProvision bundleProfileService_;

}
