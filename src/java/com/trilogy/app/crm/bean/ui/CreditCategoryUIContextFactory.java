package com.trilogy.app.crm.bean.ui;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;


public class CreditCategoryUIContextFactory
{
    public CreditCategoryUIContextFactory(CreditCategory category)
    {
        this.category_ = category;
    }


    public Object create(Context fCtx)
    {
        if (bean_ == null)
        {
            try
            {
                bean_ = new CreditCategoryUIAdapter(fCtx).adapt(fCtx, category_);
            }
            catch (HomeException e)
            {
                new DebugLogMsg(this, "Error occurred creating UI version of credit catgory: " + e.getMessage(), e).log(fCtx);
            }
        }
        return bean_;
    }
    
    private final CreditCategory category_;
    private Object bean_ = null;
}
