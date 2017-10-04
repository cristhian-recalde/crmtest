package com.trilogy.app.crm.client;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.SubscriberBucketProvision;


public class TestBMSubscriberBucketCorbaClient extends BMSubscriberBucketCorbaClient
{
    public TestBMSubscriberBucketCorbaClient(Context ctx)
    {
        super(ctx);
    }
    
    protected void init()
    {
        new DebugLogMsg(this, "TestBMSubscriberBucketCorbaClient initialized", null);        
    }
    
    protected synchronized SubscriberBucketProvision getBucketService()
    {
        if (bucketService_ == null)
        {
            bucketService_ = new TestSubscriberBucketProvision();
        }
        return bucketService_;
    }    
}
