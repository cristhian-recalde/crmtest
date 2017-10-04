package com.trilogy.app.crm.client;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.product.bundle.manager.provision.v5_0.category.CategoryProvision;


public class TestBMBundleCategoryCorbaClient extends BMBundleCategoryCorbaClient
{
    public TestBMBundleCategoryCorbaClient(Context ctx)
    {
        super(ctx);
    }
    
    protected void init()
    {
        new DebugLogMsg(this, "TestBMBundleCategoryCorbaClient initialized", null);        
    }
    
    protected synchronized CategoryProvision getService()
    {
        if (categoryService_ == null)
        {
            categoryService_ = new TestCategoryProvision();
        }
        return categoryService_;
    }
    
}
